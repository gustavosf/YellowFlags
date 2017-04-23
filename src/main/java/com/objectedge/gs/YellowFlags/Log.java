package com.objectedge.gs.YellowFlags;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    public static final int LOG_LEVEL = 2;

    public static void log(String msg, Object... args) {
        DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
        String message = "["+dateFormat.format(new Date())+"] ";
        System.out.println(message + MessageFormat.format(msg, args));
    }

    public static void trace(String msg, Object... args) {
        if (LOG_LEVEL >= 0) log("[TRACE] "+msg, args);
    }

    public static void debug(String msg, Object... args) {
        if (LOG_LEVEL >= 1) log("[INFO] "+msg, args);
    }

    public static void info(String msg, Object... args) {
        if (LOG_LEVEL >= 2) log("[INFO] "+msg, args);
    }

    public static void error(Throwable e, String msg, Object... args) {
        if (LOG_LEVEL >= 3) {
            log("[ERROR] "+msg, args);
            e.printStackTrace();
        }
    }

    public static void error(String msg, Object... args) {
        if (LOG_LEVEL >= 3) log("[ERROR] "+msg, args);
    }
}
