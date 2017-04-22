package com.objectedge.gs.YellowFlags.alert.model;

import com.objectedge.gs.YellowFlags.App;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class Connection {
    private String url;
    private String pass;
    private String user;
    private java.sql.Connection connection;

    Connection(String url, String pass, String user) {
        this.url = url;
        this.pass = pass;
        this.user = user;
    }

    ResultSet query(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(sql);
    }

    private java.sql.Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
//        Class.forName("oracle.jdbc.driver.OracleDriver");
        connection = DriverManager.getConnection(url, pass, user);
        return connection;
    }

    void closeConnection() throws SQLException {
        if (connection != null && connection.isClosed()) {
            App.log("Closing connection...");
            connection.close();
            App.log("Connection closed.");
            connection = null;
        }
    }

}
