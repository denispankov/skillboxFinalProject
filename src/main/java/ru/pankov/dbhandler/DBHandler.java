package ru.pankov.dbhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pankov.lemmanization.Lemma;
import ru.pankov.lemmanization.Lemmatizer;
import ru.pankov.siteparser.Page;

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
    private volatile BlockingQueue<Page> queuePage = new ArrayBlockingQueue<>(10000);
    private QueueControllerThread queueThread;
    private Lemmatizer lemmatizer;
    private ConnectionPool connectionPool;
    private List<DBWriteThread> dbWriteThreads;
    private class QueueControllerThread extends Thread{
        public void run(){
            int timeOfDoNothing = 0;
            DBWriteThread DBThread = new DBWriteThread();
            DBThread.start();
            dbWriteThreads = new ArrayList<>();
            dbWriteThreads.add(DBThread);
            while(true) {
                if (timeOfDoNothing >= 5 && !existActiveDBWriter()){
                    System.out.println("connection pull closed");
                    connectionPool.close();
                    return;
                }
                if(queuePage.size() > 50 && connectionPool.getCurrentAvailableConnections() > 0) {
                    System.out.println("Create new db thread. QueuePage size is " + queuePage.size());
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

    private boolean existActiveDBWriter(){
        for (int i = 0; i < dbWriteThreads.size(); i++){
            if(dbWriteThreads.get(i).isAlive()){
                return true;
            }
        }
        return false;
    }

    private void insertPage(Page page, Connection connection) throws SQLException{
        String sqlPage = "INSERT INTO page (path, code, content) VALUES ('" + page.getPageLink() + "'," + page.getStatusCode() + ",quote_literal($$" + page.getContent() + "$$))";
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
            sqlLemmas.append("INSERT INTO lemma (lemma, frequency) values");
            for (Lemma lemma : lemmas) {
                sqlLemmas.append("('" + lemma.getLemma() + "', " + 1 + "),");
            }
            sqlLemmas.delete(sqlLemmas.length() - 1, sqlLemmas.length());
            sqlLemmas.append("ON CONFLICT (lemma) DO UPDATE " +
                    "  SET frequency = lemma.frequency + excluded.frequency;");
            Statement statement = connection.createStatement();
            statement.execute(sqlLemmas.toString());

            StringBuilder sqlIndex = new StringBuilder();
            sqlIndex.append("insert into \"index\"(page_id, lemma_id, \"rank\")");

            statement = connection.createStatement();
            String getPageSql = "select p.id from page p where p.path = '" + page.getPageLink() + "'";
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

    public void createPageIndex(Page page){
        queuePage.add(page);
    }

    @PostConstruct
    private void init(){
        if (queueThread == null) {
            queueThread = new QueueControllerThread();
            queueThread.start();
        }
    }
}
