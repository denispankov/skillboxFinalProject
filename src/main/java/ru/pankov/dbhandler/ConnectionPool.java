package ru.pankov.dbhandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ConnectionPool {
    @Value("${spring.datasource.url}")
    private String dbURL;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPass;

    @Value("${spring.datasource.connection-pool-size}")
    private int poolSize;
    private List<Connection> connList = new ArrayList<>();
    private BlockingQueue<Connection> connectionsQueue;

    public Connection getConnection(){
        try {
            return connectionsQueue.poll(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void putConnection(Connection con){
        try {
            connectionsQueue.put(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init(){
        Connection connection;
        connectionsQueue = new ArrayBlockingQueue<>(poolSize);
        for(int i = 0; i < poolSize; i++) {
            try {
                connection = DriverManager.getConnection(
                        dbURL, dbUser, dbPass);
                connection.setAutoCommit(false);
                connList.add(connection);
                connectionsQueue.put(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        connectionsQueue.forEach(c -> {
            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
