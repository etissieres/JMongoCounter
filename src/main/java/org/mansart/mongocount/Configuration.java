package org.mansart.mongocount;

import java.util.ArrayList;

public final class Configuration {
    private ArrayList<Listener> listeners = new ArrayList<Listener>();
    private String dbname = "";
    private String collname = "";
    private int interval = 1;

    public String getDbname() {
        return this.dbname;
    }

    public String getCollname() {
        return this.collname;
    }

    public int getInterval() {
        return this.interval;
    }

    public void udpate(String dbname, String collname, int interval) {
        this.dbname = dbname;
        this.collname = collname;
        this.interval = interval;
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
}
