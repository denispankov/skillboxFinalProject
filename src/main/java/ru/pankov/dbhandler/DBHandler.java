package ru.pankov.dbhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.pankov.lemmanization.Lemmatizer;
import ru.pankov.siteparser.Page;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DBHandler {
    private Connection connection;
    @Value("${spring.datasource.url}")
    private String dbURL;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPass;
    private volatile Queue<Page> queuePage = new ArrayDeque<>();
    private MainThread mainThread;
    Lemmatizer lemmatizer;
    private class MainThread extends Thread{
        private boolean isShutdown = false;

        public void setShutdown(boolean shutdown) {
            isShutdown = shutdown;
        }

        public void run() {
            int timeOfDoNothing = 0;
            while (true) {
                if (timeOfDoNothing >= 3 && isShutdown == true){
                    return;
                }
                Page page = queuePage.poll();
                if (page != null) {
                    timeOfDoNothing = 0;
                    try {
                        connection.setSavepoint();
                        insertPage(page);
                        insertLemmas(page);
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

    private void insertPage(Page page) throws SQLException{
        String sqlPage = "INSERT INTO page (path, code, content) VALUES ('" + page.getPageLink() + "'," + page.getStatusCode() + ",quote_literal($$" + page.getContent() + "$$))";
        Statement statement = connection.createStatement();
        statement.execute(sqlPage);
    }

    private void insertLemmas(Page page) throws SQLException{
        Map<String, Double> lemmasTitle = lemmatizer.getLemmas(page.getTitleText()).entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e -> Double.parseDouble(e.getValue().toString())));
        Map<String, Double> lemmasBody = lemmatizer.getLemmas(page.getContentText()).entrySet().stream().collect(Collectors.toMap(e->e.getKey(), e -> Double.parseDouble(e.getValue().toString()) * 0.8));
        Map<String, Double> lemmas = Stream.of(lemmasBody, lemmasTitle).flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (value1, value2) -> value1 + value2));
        if (lemmasTitle.size()!=0 | lemmasBody.size() != 0) {
            StringBuilder sqlLemmas = new StringBuilder();
            sqlLemmas.append("INSERT INTO lemma (lemma, frequency) values");
            for (Map.Entry lemma : lemmas.entrySet()) {
                sqlLemmas.append("('" + lemma.getKey() + "', " + 1 + "),");
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
            for (Map.Entry lemma : lemmas.entrySet()) {
                sqlIndex.append("select " + pageId +
                                ",l.id" +
                                "," + String.format(Locale.US,"%.2f",lemma.getValue()) +
                                " from lemma l " +
                                "where l.id  = (select id from lemma l where l.lemma = '"+lemma.getKey()+"') " +
                                "union all ");
            }
            sqlIndex.delete(sqlIndex.length() - 10, sqlIndex.length());
            statement = connection.createStatement();
            statement.execute(sqlIndex.toString());
        }
    }
    public DBHandler(){

    }

    @PostConstruct
    public void init() {
        mainThread = new MainThread();
        mainThread.start();

        try {
            connection = DriverManager.getConnection(
                    dbURL, dbUser, dbPass);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPageIndex(Page page){
        queuePage.add(page);
    }

    public void shutdownMainThread(){
        mainThread.setShutdown(true);
    }

    public boolean isMainThreadRunning(){
        return mainThread.isAlive();
    }
}
