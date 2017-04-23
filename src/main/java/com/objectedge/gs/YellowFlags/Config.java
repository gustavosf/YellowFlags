package com.objectedge.gs.YellowFlags;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

class Config {

    private static HashMap<String,Properties> files = new HashMap<>();
    public static String get(String name) {
        return get(name, "config");
    }

    public static String get(String property, String file) {
        if (files.get(file) == null) {
            Properties config = new Properties();
            InputStream is = Config.class.getResourceAsStream("/"+file+".properties");
            try {
                config.load(is);
                files.put(file, config);
            } catch (IOException e) {
                Log.info("[Config] Unable to load config for [{0}]", file);
            }
        }
        if (files.get(file) == null) {
            return null;
        } else {
            return files.get(file).getProperty(property);
        }
    }
}
