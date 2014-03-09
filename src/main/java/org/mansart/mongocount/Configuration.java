package org.mansart.mongocount;

import java.awt.*;
import java.util.ArrayList;

public final class Configuration {
    private ArrayList<Listener> listeners = new ArrayList<>();
    private String host = "localhost";
    private int port = 27017;
    private String dbname = "";
    private String collname = "";
    private int interval = 1;
    private Color color = Color.RED;

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getDbname() {
        return this.dbname;
    }

    public String getCollname() {
        return this.collname;
    }

    public int getInterval() {
        return this.interval;
    }

    public Color getColor() {
        return this.color;
    }

    public void udpate(String host, int port, String dbname, String collname, int interval, Color color) {
        this.host = host;
        this.port = port;
        this.dbname = dbname;
        this.collname = collname;
        this.interval = interval;
        this.color = color;
        this.notifyListeners();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Listener listener : this.listeners) {
            listener.onConfigurationUpdate();
        }
    }

    public static interface Listener {
        public void onConfigurationUpdate();
    }

    public String toString() {
        return this.dbname + "." + this.collname + "@" + this.host + ":" + this.port;
    }
}
