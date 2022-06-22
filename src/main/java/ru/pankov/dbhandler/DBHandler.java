package ru.pankov.dbhandler;

import java.sql.*;

public class DBHandler {
    private static Connection connection;

    private static String dbName = "search_engine";
    private static String dbUser = "search_engine";
    private static String dbPass = "search_engine";
    private static int batchCounter;
    private static PreparedStatement preparedStatement;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/" + dbName + "?rewriteBatchedStatements=true", dbUser, dbPass);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void beginBatchInsertIndex() {
        String sql = "INSERT INTO site_index (path, code, content) VALUES (?,?,quote_literal(?))";
        try {
            preparedStatement = getConnection().prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addToBatchInsertIndex(String path, int code, String content) {
        try {
            preparedStatement.setString(1, path);
            preparedStatement.setInt(2, code);
            preparedStatement.setString(3, content);
            preparedStatement.addBatch();
            preparedStatement.clearParameters();
            batchCounter++;
            if (batchCounter >= 10) {
                preparedStatement.executeBatch();
                batchCounter = 0;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void flushInsertIndex() {
        try {
            preparedStatement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
