package com.objectedge.gs.YellowFlags.alert;

import com.objectedge.gs.YellowFlags.App;
import com.objectedge.gs.YellowFlags.Log;
import com.objectedge.gs.YellowFlags.Mailer;
import com.objectedge.gs.YellowFlags.alert.model.SQLAlertModel;
import com.objectedge.gs.YellowFlags.persistence.History;
import dnl.utils.text.table.TextTable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.sql.Date;
import java.util.Map;

public class SQLTriggeredAlert implements Alert {
    private SQLAlertModel model;
    private File file;

    public SQLTriggeredAlert(File file) throws IOException {
        this.file = file;
        if (file.exists()) {
            model = new SQLAlertModel();
            InputStreamReader isr = new InputStreamReader(
                    new FileInputStream(file), "UTF-8");
            model.load(isr);
            if (!model.validate()) {
                throw new InvalidParameterException("File is not a valid SQL-based Alert");
            }
        } else {
            throw new InvalidParameterException("Alert file does not exist");
        }
    }

    public void run() {
        Map results = model.getResults();
        Object[][] data = (Object[][]) results.get("data");
        Log.info("[{0}] Retornado [{1}] resultados", getId(), data.length);

        History lastRun = App.getPersistence().getHistory().findLast(getId());
        History thisRun = App.getPersistence().getHistory();
        thisRun.setAlert(getId());
        thisRun.setResults((long)data.length);
        thisRun.setDate(new Date(new java.util.Date().getTime()));
        thisRun.setAlertThreshold(data.length > model.getThreshold());

        // Não exibir caso já tenha sido exibido quando passou do
        // threshold
        if (!lastRun.getAlertThreshold() && data.length > model.getThreshold()) {
            Log.info("[{0}] Resultado acima do threshold", getId());

            String[] columns = (String[]) results.get("columns");
            String message = model.getAlertMessage();
            if (model.getAppendResults()) {
                TextTable tt = new TextTable(columns, data);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                tt.printTable(ps, 0);
                message += new String(baos.toByteArray(), StandardCharsets.UTF_8);
            }

            Mailer mail = new Mailer();
            mail.send(model.getRecipients(), model.getAlertSubject(), message);

            Log.info("[{0}] Disparado alerta para os recipiantes [{1}]",
                    getId(), model.getRecipients());
            thisRun.setAlerted(true);
        }

        // Caso o threshold tenha descido para um valor normal, envia mensagem
        if (lastRun.getAlertThreshold() && data.length <= model.getThreshold()) {
            Log.info("[{0}] Alerta normalizado", getId());
            Mailer mail = new Mailer();
            mail.send(model.getRecipients(), model.getNormalizedSubject(), model.getNormlizedMessage());
            Log.info("[{0}] Disparado aviso de normalização para os recipiantes [{1}]",
                    getId(), model.getRecipients());
            thisRun.setAlerted(true);
        }

        thisRun.persist();
    }

    public String getId() {
        return file.getName().replace(".properties", "");
    }

    public String getSchedule() {
        return model.getCron();
    }

}
