package com.objectedge.gs.YellowFlags;

import com.objectedge.gs.YellowFlags.alert.SQLTriggeredAlert;
import com.objectedge.gs.YellowFlags.persistence.SQLite;
import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class App {

    private static SQLite persistence = new SQLite(Config.get("persistence.db").toString());

    private Scheduler scheduler = new Scheduler();
    private Map<String,String> scheduleIds = new HashMap<>();

    public static void main(String[] args) throws Exception {
        App example = new App();
        example.run("alerts");
    }

    private void run(String path) throws Exception {
        Log.info("[App] Scheduling alerts from path [{0}]", path);

        Path dir = Paths.get(path);
        File[] files = dir.toFile().listFiles((dir1, name) ->
                name.toLowerCase().endsWith(".properties"));

        if (files != null) for (File file : files) {
            scheduleAlert(file);
        }

        Log.info("[App] Starting scheduler...");
        scheduler.start();

        Log.info("[App] Starting file watcher service");
        startWatcher(dir);

        // Execution is kept by the watcher

        // Stops the scheduler.
        scheduler.stop();
    }

    private void startWatcher(Path dir) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                Log.error(ex, "[Watcher] File watcher interrupted");
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path eventDir = (Path)key.watchable();
                Path fileName = eventDir.resolve(ev.context());
                String fileId = fileName.getFileName().toString();
                if (!fileId.endsWith(".properties")) continue;

                Log.info("[Watcher] Event [{0}] on file [{1}]", kind.name(), fileId);
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
    }

    private void descheduleAlert(String fileId) {
        if (scheduleIds.get(fileId) != null) {
            scheduler.deschedule(scheduleIds.get(fileId));
            scheduleIds.remove(fileId);
            Log.info("[Scheduler] Descheduled alert [{0}]", fileId);
        }
    }

    private void scheduleAlert(File file) {
        try {
            SQLTriggeredAlert alert = new SQLTriggeredAlert(file);
            Log.info("[Scheduler] Scheduling [{0}] with cron [{1}]",
                    alert.getId(), alert.getSchedule());
            scheduleIds.put(file.getName(), scheduler.schedule(alert.getSchedule(), alert));
        } catch (InvalidParameterException e) {
            Log.error("[Scheduler] An error occurred while trying to schedule [{0}]: {1}",
                    file, e.getMessage());
        }
        catch (IOException e) {
            Log.error(e,"[Scheduler] An error occurred while trying to schedule [{0}]", file);
        }
    }

    public static SQLite getPersistence() {
        return persistence;
    }

}
