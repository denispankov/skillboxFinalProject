package ru.pankov.dbhandler;

import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.pankov.lemmanization.Lemma;
import ru.pankov.lemmanization.Lemmatizer;
import ru.pankov.search.SearchResult;
import ru.pankov.siteparser.Page;
import ru.pankov.siteparser.PageParser;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DBHandler {
    private volatile BlockingQueue<Page> queuePage = new ArrayBlockingQueue<>(100000);
    private ObjectProvider<ConnectionPool> connectionPoolObjectProvider;
    private QueueControllerThread queueThread;
    private Lemmatizer lemmatizer;
    private ConnectionPool connectionPool;
    private ConnectionPool connectionPoolAdditional;
    private List<DBWriteThread> dbWriteThreads;
    private PageParser pageParser;
    private Logger logger;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }
    private class QueueControllerThread extends Thread{
        public void run(){
            int timeOfDoNothing = 0;
            DBWriteThread DBThread = new DBWriteThread();
            DBThread.start();
            dbWriteThreads = new ArrayList<>();
            dbWriteThreads.add(DBThread);
            while(true) {
                if (timeOfDoNothing >= 5 && !existActiveDBWriter()){
                    logger.info("connection pull closed");
                    connectionPool.close();
                    return;
                }
                if(queuePage.size() > 50 && connectionPool.getCurrentAvailableConnections() > 0) {
                    logger.info("Create new db thread. QueuePage size is " + queuePage.size());
                    timeOfDoNothing = 0;
                    DBThread = new DBWriteThread();
                    DBThread.start();
                    dbWriteThreads.add(DBThread);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (queuePage.size() == 0){
                    timeOfDoNothing++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    timeOfDoNothing = 0;
                }
            }
        }
    }
    private class DBWriteThread extends Thread{
        Connection connection;
        public DBWriteThread(){
            super();
            connection = connectionPool.getConnection();
        }

        public void close(){
            connectionPool.putConnection(connection);
        }
        public void run() {
            int timeOfDoNothing = 0;
            Page page = null;
            while (true) {
                if (timeOfDoNothing >= 2){
                    close();
                    return;
                }
                try {
                    page = queuePage.poll(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (page != null) {
                    timeOfDoNothing = 0;
                    try {
                        connection.setSavepoint();
                        insertPage(page, connection);
                        insertLemmas(page, connection);
                        connection.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                        timeOfDoNothing++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Autowired
    public void setLemmatizer(Lemmatizer lemmatizer) {
        this.lemmatizer = lemmatizer;
    }

    @Autowired
    public void setConnectionPool(ConnectionPool connectionPool){
        this.connectionPool = connectionPool;
    }

    @Autowired
    public void setConnectionPoolRead(ConnectionPool connectionPoolRead){
        this.connectionPoolAdditional = connectionPoolRead;
    }

    @Autowired
    public void setPageParser(PageParser pageParser) {
        this.pageParser = pageParser;
    }

    private boolean existActiveDBWriter(){
        for (int i = 0; i < dbWriteThreads.size(); i++){
            if(dbWriteThreads.get(i).isAlive()){
                return true;
            }
        }
        return false;
    }

    private void insertPage(Page page, Connection connection) throws SQLException{
        String sqlPage = "INSERT INTO page (path, code, content, site_id) VALUES ('" + page.getRelativePageLink() + "'," + page.getStatusCode() + ",quote_literal($$" + page.getContent() + "$$), "+ page.getSiteId()+")";
        Statement statement = connection.createStatement();
        statement.execute(sqlPage);
    }

    private void insertLemmas(Page page, Connection connection) throws SQLException{
        List<Lemma> lemmasTitle = lemmatizer.getLemmas(page.getTitleText());
        List<Lemma> lemmasBody = lemmatizer.getLemmas(page.getContentText());
        lemmasTitle.forEach(l->l.setRank(l.getCount()));
        lemmasBody.forEach(l->l.setRank(l.getCount() * 0.8));
        Map<String, Double> lemmasMap = Stream.concat(lemmasBody.stream(), lemmasTitle.stream()).collect(Collectors.toMap(
                Lemma::getLemma,
                Lemma::getRank,
                (value1, value2) -> value1 + value2));
        List<Lemma> lemmas = lemmasMap.entrySet().stream()
                                                .map(l-> new Lemma(l.getKey(), l.getValue()))
                                                .sorted(Comparator.comparing(Lemma::getLemma)).collect(Collectors.toList());
        if (lemmasTitle.size()!=0 | lemmasBody.size() != 0) {
            StringBuilder sqlLemmas = new StringBuilder();
            sqlLemmas.append("INSERT INTO lemma (lemma, frequency, site_id) values");
            for (Lemma lemma : lemmas) {
                sqlLemmas.append("('" + lemma.getLemma() + "', " + 1 + ", " + page.getSiteId() + "),");
            }
            sqlLemmas.delete(sqlLemmas.length() - 1, sqlLemmas.length());
            sqlLemmas.append("ON CONFLICT (lemma) DO UPDATE " +
                    "  SET frequency = lemma.frequency + excluded.frequency;");
            Statement statement = connection.createStatement();
            statement.execute(sqlLemmas.toString());

            StringBuilder sqlIndex = new StringBuilder();
            sqlIndex.append("insert into \"index\"(page_id, lemma_id, \"rank\")");

            statement = connection.createStatement();
            String getPageSql = "select p.id from page p where p.path = '" +  page.getRelativePageLink() + "'";
            ResultSet rs = statement.executeQuery(getPageSql);
            rs.next();
            int pageId = rs.getInt("id");
            for (Lemma lemma : lemmas) {
                sqlIndex.append("select " + pageId +
                                ",l.id" +
                                "," + String.format(Locale.US,"%.2f",lemma.getRank()) +
                                " from lemma l " +
                                "where l.id  = (select id from lemma l where l.lemma = '"+lemma.getLemma()+"') " +
                                "union all ");
            }
            sqlIndex.delete(sqlIndex.length() - 10, sqlIndex.length());
            statement = connection.createStatement();
            statement.execute(sqlIndex.toString());
        }
    }

    public List<SearchResult> search(List<Lemma> lemmas){
        Connection connection = connectionPoolAdditional.getConnection();
        StringBuilder lemmasIntoSQL = new StringBuilder();
        List<SearchResult> searchResults = new ArrayList<>();
        for(Lemma lemma: lemmas){
            lemmasIntoSQL.append("'" + lemma.getLemma() + "', ");
        }
        lemmasIntoSQL.delete(lemmasIntoSQL.length() - 2 , lemmasIntoSQL.length() - 1);
        String searchSql = "with quantity_lemmas as (" +
                " select count(1) cnt" +
                "  from lemma" +
                ")," +
                "lemmas as (" +
                "select l.lemma " +
                "  ,l.frequency " +
                "  ,l.id " +
                "  from lemma l" +
                " where l.lemma in ("+ lemmasIntoSQL +")" +
                ")," +
                "tempor as (select l.lemma " +
                "  ,l.frequency " +
                "  ,l.frequency::float / (select cnt from quantity_lemmas) percent" +
                "  ,l.id" +
                "  from lemmas l)," +
                " res as (select string_agg(t.lemma, ', ') agg" +
                "    ,sum(i.\"rank\") rel" +
                "    ,count(1) cnt" +
                "    ,(select  count(1) from lemmas) cnt_all" +
                "    ,i.page_id " +
                "   from tempor t" +
                "   join \"index\" i on i.lemma_id = t.id" +
                "  where t.percent <= 0.055" +
                "  group by i.page_id )," +
                "  resul as (select r.agg" +
                "  ,r.cnt" +
                "  ,r.cnt_all" +
                "  ,r.rel / (select max(r.rel) from res r) rel_rel" +
                "  ,r.page_id" +
                "    from res r" +
                "   order by cnt desc,5 desc)" +
                "   select l.*" +
                "   ,p.\"content\"" +
                "   ,p.\"path\" " +
                "     from resul l" +
                "     join page p on p.id  = l.page_id" +
                "    limit 10";
        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(searchSql);
            while (rs.next()){
                searchResults.add(new SearchResult(rs.getString("path"),
                                                    pageParser.getHTMLTitle(rs.getString("content" )),
                                                    pageParser.getHTMLSnippet(rs.getString("content" ), lemmas),
                                                    rs.getDouble("rel_rel")));
            };
        }catch (Exception e){
            e.printStackTrace();
        }

        connectionPoolAdditional.putConnection(connection);
        return searchResults;
    }

    public void createPageIndex(Page page){
        queuePage.add(page);
    }

    @PostConstruct
    private void init(){
        if (queueThread == null) {
            queueThread = new QueueControllerThread();
            queueThread.start();
        }
        connectionPool = connectionPoolObjectProvider.getObject(10);
        connectionPoolAdditional = connectionPoolObjectProvider.getObject(3);
    }

    @Autowired
    public void setConnectionPoolObjectProvider(ObjectProvider<ConnectionPool> connectionPoolObjectProvider) {
        this.connectionPoolObjectProvider = connectionPoolObjectProvider;
    }

    public int addSite(String url){
        String sql = "INSERT INTO site(status,status_time,last_error,url,name) values" +
                "('INDEXING', CURRENT_TIMESTAMP, null, '"+ url +"', '" + url + "')" +
                "ON CONFLICT (url) DO UPDATE SET status  = 'INDEXING'" +
                "returning id;";
        Connection connection = connectionPoolAdditional.getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql.toString());
            rs.next();
            connection.commit();
            connectionPoolAdditional.putConnection(connection);
            return rs.getInt("id");
        }catch (Exception e){
            connectionPoolAdditional.putConnection(connection);
            e.printStackTrace();
        }

        return 0;
    }

    public void changeSiteStatus(String status, int siteId, String error){
        String sql = "update site" +
                "   set status = '" + status + "' " +
                ", last_error = '" + error + "'"+
                " where id  = "+ siteId +";";
        Connection connection = connectionPoolAdditional.getConnection();
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            connectionPoolAdditional.putConnection(connection);
        }catch (Exception e){
            connectionPoolAdditional.putConnection(connection);
            e.printStackTrace();
        }
    }

    public void clearData(){
        String sql = "delete from page;\n" +
                    "delete from lemma; \n" +
                    "delete from \"index\";\n" +
                    "delete from site;\n";
        Connection connection = connectionPoolAdditional.getConnection();
        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            connectionPoolAdditional.putConnection(connection);
        }catch (Exception e){
            connectionPoolAdditional.putConnection(connection);
            e.printStackTrace();
        }
    }

    public ResultStatistic getStatistic(){
        Connection connection = connectionPoolAdditional.getConnection();
        ResultStatistic resultStatistic = new ResultStatistic();
        String sql = "  select (select count(1)\n" +
                " \t       from site s) sites\n" +
                " \t   ,(select count(1)\n" +
                "  \t\t   from page) pages\n" +
                "  \t   ,(select count(1)\n" +
                "           from lemma) lemmas\n" +
                "       ,(select case when status = 'INDEXING' then 1 else 0 end\n" +
                "           from site\n" +
                "           order by 1 desc\n" +
                "           limit 1) status ";
        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                resultStatistic.setTotal(rs.getInt("sites")
                                        ,rs.getInt("pages")
                                        ,rs.getInt("lemmas")
                                        ,rs.getInt("status") == 1? true : false);
            };
        }catch (Exception e){
            e.printStackTrace();
        }

        sql = " select s.url \n" +
                "\t  ,s.\"name\" \n" +
                "\t  ,s.status \n" +
                "\t  ,s.status_time\n" +
                "\t  ,s.last_error \n" +
                "\t  ,(select count(1)\n" +
                "\t      from page p\n" +
                "\t     where s.id = p.site_id ) pages\n" +
                "\t  ,(select count(1)\n" +
                "\t      from lemma l\n" +
                "\t     where l.site_id  = s.id ) lemmas\n" +
                " from site s";
        try {

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                resultStatistic.addDetail(rs.getString("url")
                                         ,rs.getString("name")
                                         ,rs.getString("status")
                                         ,rs.getString("status_time")
                                         ,rs.getString("last_error")
                                         ,rs.getInt("pages")
                                         ,rs.getInt("lemmas"));
            };
        }catch (Exception e){
            e.printStackTrace();
        }

        connectionPoolAdditional.putConnection(connection);
        return resultStatistic;
    }


    public void clearIndexingQueue(){
        queuePage.clear();
    }
}
