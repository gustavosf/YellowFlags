package com.objectedge.gs.YellowFlags.persistence;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class SQLite {

    private Connection connection;
    private String file;

    public SQLite(String file) {
        this.file = file;
    }

    public int insert(String table, HashMap<String,Object> values) {
        Object[] valuesArray = values.values().toArray(new Object[0]);
        String val = "("+String.join(",", Collections.nCopies(values.size(), "?"))+")";
        String col = String.join("\",\"", values.keySet().toArray(new String[0]));

        String sql = "INSERT INTO "+table+" (\""+col+"\") VALUES "+val;

        try {
            PreparedStatement st = getConnection().prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            setValues(st, valuesArray);
            if (st.execute()) {
                ResultSet generatedKeys = st.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            };
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int update(String table, Integer id, HashMap<String,Object> values) {
        Object[] valuesArray = values.values().toArray(new Object[0]);
        String sql = "UPDATE "+table+" SET ";
        List<String> set = new ArrayList<>();
        for (Map.Entry<String,Object> entry : values.entrySet()) {
            set.add(entry.getKey()+"=?");
        }
        sql += String.join(",", set);
        sql += " WHERE id="+id;

        try {
            PreparedStatement st = getConnection().prepareStatement(sql);
            setValues(st, valuesArray);
            return st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setValues(PreparedStatement st, Object[] valuesArray) throws SQLException {
        for (int i = 1; i <= valuesArray.length; i++) {
            Object value = valuesArray[i-1];
            if (value instanceof Integer) {
                st.setInt(i, (int) value);
            } else if (value instanceof Boolean) {
                st.setBoolean(i, (boolean) value);
            } else if (value instanceof Date) {
                st.setDate(i, (Date) value);
            } else if (value instanceof Long) {
                st.setLong(i, (long) value);
            } else {
                st.setString(i, value.toString());
            }
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:"+this.file);
                connection.setAutoCommit(true);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public ResultSet query(String sql) {
        try {
            return getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
            return null;
        }
    }

    public History getHistory() {
        return new History(this);
    }

}