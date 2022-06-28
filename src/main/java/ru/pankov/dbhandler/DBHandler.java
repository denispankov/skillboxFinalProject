package ru.pankov.dbhandler;

import org.postgresql.util.PSQLException;
import ru.pankov.lemmanization.Lemmatizer;
import ru.pankov.siteparser.Page;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBHandler {
    private static Connection connection;

    private static String dbName = "search_engine";
    private static String dbUser = "search_engine";
    private static String dbPass = "search_engine";
    private volatile Queue<Page> queuePage = new ArrayDeque<>();
    private MainThread mainThread;
    private static DBHandler dbHandler;
    Lemmatizer lemmatizer = new Lemmatizer();
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
                        insertPage(page);
                        insertLemmas(page);
                        connection.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException ex) {
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
    };

    private void insertPage(Page page) throws SQLException{
        String sqlPage = "INSERT INTO page (path, code, content) VALUES ('" + page.getPageLink() + "'," + page.getStatusCode() + ",quote_literal($$" + page.getContent() + "$$))";
        Statement statement = getConnection().createStatement();
        statement.execute(sqlPage);
    }

    private void insertLemmas(Page page) throws SQLException{
        StringBuilder sqlLemmas = new StringBuilder();
        Map<String, Long> lemmasTitle = lemmatizer.getLemmas(page.getTitleText());
        Map<String, Long> lemmasBody = lemmatizer.getLemmas(page.getContentText());
        Map<String, Long> lemmas = Stream.of(lemmasBody, lemmasTitle).flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (value1, value2) -> value1 + value2 ));
        if (lemmasTitle.size()!=0 | lemmasBody.size() != 0) {
            sqlLemmas.append("INSERT INTO lemma (lemma, frequency) values");
            for (Map.Entry<String, Long> lemma : lemmas.entrySet()) {
                sqlLemmas.append("('" + lemma.getKey() + "', " + 1 + "),");
            }
            sqlLemmas.delete(sqlLemmas.length() - 1, sqlLemmas.length());
            sqlLemmas.append("ON CONFLICT (lemma) DO UPDATE " +
                    "  SET frequency = lemma.frequency + excluded.frequency;");
            Statement statement = getConnection().createStatement();
            statement.execute(sqlLemmas.toString());
        }
    }
    private DBHandler(){
        mainThread = new MainThread();
        mainThread.start();
    }

    public static DBHandler getInstance(){
        if (dbHandler == null) {
            dbHandler = new DBHandler();
        }
        return dbHandler;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/" + dbName + "?rewriteBatchedStatements=true", dbUser, dbPass);
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
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
