package com.objectedge.gs.YellowFlags.persistence;

import com.objectedge.gs.YellowFlags.App;
import com.objectedge.gs.YellowFlags.Log;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class History {
    private Integer id;
    private String alert;
    private Date date;
    private Long results;
    private Boolean alertThreshold = false;
    private Boolean alerted = false;
    private SQLite database;

    public History(SQLite database) {
        this.database = database;
    }

    private void fillWithResutSet(ResultSet rs) {
        try {
            id = rs.getInt("id");
            alert = rs.getString("alert");
            date = rs.getDate("date");
            results = rs.getLong("results");
            alertThreshold = rs.getBoolean("alertThreshold");
            alerted = rs.getBoolean("alerted");
        } catch (SQLException e) {
            Log.error(e,"[Persistence] Error trying to fill up History object");
        }
    }

    public boolean persist() {
        if (getId() == null) {
            int id = this.database.insert("history", toHashMap());
            if (id > 0) this.id = id;
            else return false;
        } else {
            this.database.update("history", getId(), toHashMap());
        }
        return true;
    }

    private HashMap<String,Object> toHashMap() {
        return new HashMap<String, Object>(){{
            put("alert", getAlert());
            put("date", getDate());
            put("results", getResults());
            put("alertThreshold", getAlertThreshold());
            put("alerted", getAlerted());
        }};
    }

    public Integer getId() {
        return id;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(Boolean alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public Boolean getAlerted() {
        return alerted;
    }

    public void setAlerted(Boolean alerted) {
        this.alerted = alerted;
    }

    public Long getResults() {
        return results;
    }

    public void setResults(Long results) {
        this.results = results;
    }

    public History findLast(String id) {
        String sql = "SELECT * FROM history WHERE alert='"+id+"' ORDER BY date DESC LIMIT 1";
        ResultSet results = this.database.query(sql);
        try {
            if (results != null && results.next()) fillWithResutSet(results);
            else Log.info("[Persistence] No history for alert for [{0}]", id);
        } catch (SQLException e) {
            Log.error(e, "[Persistence] Error trying to retrieve history for [{0}]", id);
        }
        return this;
    }

}
