package com.objectedge.gs.YellowFlags.alert.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class SQLAlertModel extends Properties {

    private Connection connection;

    private Connection getConnection() {
        if (connection == null) {
            connection = new Connection(
                    (String) this.get("connectionUrl"),
                    (String) this.get("user"),
                    (String) this.get("pass"));
        }
        return connection;
    }

    public HashMap getResults() {
        HashMap map = new HashMap();
        List<Object[]> list = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        try {
            ResultSet rs = getConnection().query(getQuery());
            ResultSetMetaData md = rs.getMetaData();

            // Get column names
            for (int i=1; i <= md.getColumnCount(); i++)
                columns.add(md.getColumnName(i));

            // Get values
            while (rs.next()){
                Object[] row = new Object[md.getColumnCount()];
                for(int i=1; i <= md.getColumnCount(); ++i){
                    row[i-1] = rs.getObject(i);
                }
                list.add(row);
            }
            getConnection().closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        map.put("columns", columns.toArray(new String[0]));
        map.put("data", list.toArray(new Object[0][0]));
        return map;
    }


    public String getQuery() {
        return (String) get("query");
    }
    public Long getThreshold() {
        return new Long((String)get("alertThreshold"));
    }
    public String getCron() {
        return (String) get("cron");
    }
    public String getSubject() {
        return (String) get("subject");
    }
    public String getRecipients() {
        return (String) get("recipients");
    }
    public String getMessage() {
        return (String) get("message");
    }
    public Boolean getAppendResults() {
        Boolean append = Boolean.valueOf((String)get("appendResults"));
        return append != null ? append : false;
    }

}
