package com.objectedge.gs.YellowFlags.alert;

import com.objectedge.gs.YellowFlags.App;
import com.objectedge.gs.YellowFlags.Mailer;
import com.objectedge.gs.YellowFlags.alert.model.SQLAlertModel;
import dnl.utils.text.table.TextTable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SQLTriggeredAlert implements Alert {
    private SQLAlertModel model;
    private File file;

    public SQLTriggeredAlert(File file) throws IOException {
        this.file = file;
        if (file.exists()) {
            model = new SQLAlertModel();
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            model.load(isr);
        }
    }

    public void run() {
        Map results = model.getResults();
        Object[][] data = (Object[][]) results.get("data");
        App.log("[{0}] Retornado [{1}] resultados", getId(), data.length);
        if (data.length > model.getThreshold()) {
            App.log("[{0}] Resultado acima do threshold", getId());

            String[] columns = (String[]) results.get("columns");
            String message = model.getMessage();
            if (model.getAppendResults()) {
                TextTable tt = new TextTable(columns, data);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                tt.printTable(ps, 0);
                message += new String(baos.toByteArray(), StandardCharsets.UTF_8);
            }

            Mailer mail = new Mailer();
            mail.send(model.getRecipients(), model.getSubject(),
                    "<pre style=\"font-size:14px\">"+message+"</pre>");

            App.log("[{0}] Disparado alerta para os recipiantes [{1}]",
                    getId(), model.getRecipients());

        }
    }

    public String getId() {
        return file.getName().replace(".properties", "");
    }

    public String getSchedule() {
        return (String)model.get("cron");
    }

}
