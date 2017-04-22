package com.objectedge.gs.YellowFlags;

import com.objectedge.gs.YellowFlags.alert.SQLTriggeredAlert;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class App {

    private Scheduler scheduler = new Scheduler();
    private Map<String,String> scheduleIds = new HashMap<>();

    public static void log(Throwable e, String msg, Object... args) {
        log(msg, args);
        e.printStackTrace();
    }
    public static void log(String msg, Object... args) {
        DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
        String message = "["+dateFormat.format(new Date())+"] ";
        System.out.println(message + MessageFormat.format(msg, args));
    }

    private void run(String path) throws Exception {
        log("[App] Scheduling alerts from path [{0}]", path);

        Path dir = Paths.get(path);
        File[] files = dir.toFile().listFiles((dir1, name) ->
                name.toLowerCase().endsWith(".properties"));

        if (files != null) for (File file : files) {
            scheduleAlert(file);
        }

        log("[App] Starting scheduler...");
        scheduler.start();

        log("[App] Starting file watcher service");
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                log(ex, "[Watcher] File watcher interrupted");
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path eventDir = (Path)key.watchable();
                Path fileName = eventDir.resolve(ev.context());
                String fileId = fileName.getFileName().toString();
                if (!fileId.endsWith(".properties")) continue;

                log("[Watcher] Event [{0}] on file [{1}]", kind.name(), fileId);
                if (kind == ENTRY_CREATE) {
                    scheduleAlert(fileName.toFile());
                } else if (kind == ENTRY_DELETE) {
                    descheduleAlert(fileId);
                } else if (kind == ENTRY_MODIFY) {
                    descheduleAlert(fileId);
                    scheduleAlert(fileName.toFile());
                }
            }
            boolean valid = key.reset();
            if (!valid) break;
        }


        try {
            Thread.sleep(1000L * 60L * 10L);
        } catch (InterruptedException e) { }
        // Stops the scheduler.
        scheduler.stop();
    }

    private void descheduleAlert(String fileId) {
        if (scheduleIds.get(fileId) != null) {
            scheduler.deschedule(scheduleIds.get(fileId));
            scheduleIds.remove(fileId);
            log("[Scheduler] Descheduled alert [{0}]", fileId);
        }
    }

    private void scheduleAlert(File file) {
        try {
            SQLTriggeredAlert alert = new SQLTriggeredAlert(file);
            log("[Scheduler] Scheduling [{0}] with cron [{1}]", alert.getId(), alert.getSchedule());
            scheduleIds.put(file.getName(), scheduler.schedule(alert.getSchedule(), alert));
        } catch (IOException e) {
            log(e,"[Scheduler] An error occurred while trying to schedule [{0}]", file);
        }
    }

    public static void main(String[] args) throws Exception {
        App example = new App();
        example.run("alerts");
    }

}
