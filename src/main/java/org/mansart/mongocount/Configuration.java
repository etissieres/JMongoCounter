package org.mansart.mongocount;

import org.mansart.mongocount.util.JSONUtils;

import java.awt.*;
import java.util.ArrayList;

public final class Configuration {
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private String host = "localhost";
    private int port = 27017;
    private String dbname = "";
    private String collname = "";
    private String query = "";
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

    public String getQuery() {
        return query;
    }

    public int getInterval() {
        return this.interval;
    }

    public Color getColor() {
        return this.color;
    }

    public void udpate(String host, int port, String dbname, String collname, String query, int interval, Color color) {
        host = host.trim();
        dbname = dbname.trim();
        collname = collname.trim();
        query = query.trim();

        if (this.isValid(host, port, dbname, collname, query, interval)) {
            this.host = host;
            this.port = port;
            this.dbname = dbname;
            this.collname = collname;
            this.query = query;
            this.interval = interval;
            this.color = color;
            this.notifyListenersOfUpdate();
        } else {
            this.notifyListenersOfError();
        }
    }

    private boolean isValid(String host, int port, String dbname, String collname, String query, int interval) {
        return !host.isEmpty() &&
            port > 0 &&
            !dbname.isEmpty() &&
            !collname.isEmpty() &&
            interval > 0 &&
            (query.isEmpty() || JSONUtils.isValid(query));
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListenersOfUpdate() {
        this.listeners.forEach(Listener::onUpdate);
    }

    private void notifyListenersOfError() {
        this.listeners.forEach(Listener::onError);
    }

    public String toString() {
        return this.dbname + "." + this.collname + "@" + this.host + ":" + this.port + ";" + this.query;
    }

    public static interface Listener {
        public void onUpdate();
        public void onError();
    }
}
