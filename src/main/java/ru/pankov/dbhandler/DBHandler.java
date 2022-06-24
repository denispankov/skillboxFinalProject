package ru.pankov.dbhandler;

import org.springframework.stereotype.Component;
import ru.pankov.siteparser.Page;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.Queue;

public class DBHandler {
    private static Connection connection;

    private static String dbName = "search_engine";
    private static String dbUser = "search_engine";
    private static String dbPass = "search_engine";
    private volatile Queue<Page> queuePage = new ArrayDeque<>();
    private MainThread mainThread;
    private static DBHandler dbHandler;
    private class MainThread extends Thread{
        public void run() {
            while (true) {
                Page page = queuePage.poll();
                if (page != null) {
                    String sql = "INSERT INTO site_index (path, code, content) VALUES ('" + page.getPageLink() + "'," + page.getStatusCode() + ",quote_literal($$" + page.getContent() + "$$))";
                    try {
                        Statement statement = getConnection().createStatement();
                        statement.execute(sql);
                        connection.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

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
}
