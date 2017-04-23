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
        if (connection == null)
            connection = new Connection(getConnectionUrl(), getUsername(), getPassword());
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
        return getProperty("query");
    }
    public Long getThreshold() {
        return new Long(getProperty("alertThreshold"));
    }
    public String getCron() {
        return getProperty("cron");
    }
    public String getRecipients() {
        return getProperty("recipients");
    }
    public Boolean getAppendResults() {
        Boolean append = Boolean.valueOf((String)get("appendResults"));
        return append != null ? append : false;
    }
    public String getAlertMessage() {
        String message = (String) get("alert.message");
        return message != null ? message : "Alerta";
    }
    public String getAlertSubject() {
        return getProperty("alert.subject");
    }
    public String getNormalizedSubject() {
        String message = getProperty("normalized.subject");
        return message != null ? message : "Alerta normalizado";
    }
    public String getNormlizedMessage() {
        return getProperty("normalized.message");
    }

    private String getConnectionUrl() {
        return getProperty("connectionUrl");
    }
    private String getUsername() {
        return getProperty("user");
    }
    private String getPassword() {
        return getProperty("pass");
    }

    /**
     * Checks if properties file is filled up correctly for this kind
     * of alert
     * @return false if it does not meet all the required attribues
     */
    public boolean validate() {
        return getQuery() != null
                && getCron() != null
                && getRecipients() != null
                && getThreshold() != null
                && getConnection() != null
                && getUsername() != null
                && getPassword() != null;
    }
}
