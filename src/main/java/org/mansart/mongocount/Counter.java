package org.mansart.mongocount;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.mansart.mongocount.exception.MongoException;

import java.net.UnknownHostException;
import java.util.ArrayList;

public final class Counter implements Runnable {
    private ArrayList<Listener> listeners = new ArrayList<Listener>();
    private MongoClient client = null;
    private String dbname;
    private String collname;
    private int interval;
    private Thread thread = null;

    public void connect() throws MongoException {
        try {
            this.client = new MongoClient("localhost");
        } catch (UnknownHostException e) {
            throw new MongoException("Unable to setup", e);
        }
    }

    public void disconnect() {
        if (this.client != null) this.client.close();
    }

    public void configure(String dbname, String collname, int interval) {
        this.dbname = dbname;
        this.collname = collname;
        this.interval = interval;
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public void notifyListeners(long count) {
        for (Listener listener : this.listeners) {
            listener.onCount(count);
        }
    }

    @Override
    public void run() {
        DB db = this.client.getDB(this.dbname);
        DBCollection coll = db.getCollection(this.collname);
        while (Thread.currentThread().getId() == this.thread.getId()) {
            long count = coll.count();
            this.notifyListeners(count);
            try {
                Thread.sleep(this.interval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface Listener {
        public void onCount(long count);
    }
}
