package ru.pankov.dbhandler;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
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
@Scope("prototype")
public class ConnectionPool {
    @Value("${spring.datasource.url}")
    private String dbURL;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPass;

    private int poolSize = 10;
    private List<Connection> connList = new ArrayList<>();
    private BlockingQueue<Connection> connectionsQueue;

    private Logger logger;

    @Autowired
    @Qualifier("logger")
    public void setLogger(Logger logger){
        this.logger = logger;
    }

    public ConnectionPool(){

    }

    public ConnectionPool(int poolSize){
        this.poolSize = poolSize;
    }

    public Connection getConnection(){
        try {
            logger.info("Try to pull. Before pull - " + connectionsQueue.size());
            return connectionsQueue.poll(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getCurrentAvailableConnections(){
        return connectionsQueue.size();
    }

    public void putConnection(Connection con){
        try {
            connectionsQueue.put(con);
            logger.info("Try to put. After put - " + connectionsQueue.size());
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
