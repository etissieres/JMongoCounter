package org.mansart.mongocount;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.mansart.mongocount.exception.MongoException;

import java.net.UnknownHostException;
import java.util.ArrayList;

public final class Counter implements Runnable {
    private boolean running = false;
    private String dbname;
    private String collname;
    private MongoClient client = null;
    private DB db = null;
    private DBCollection coll = null;
    private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public Counter(String dbname, String collname) {
        super();
        this.dbname = dbname;
        this.collname = collname;
    }

    public void connect() throws MongoException {
        try {
            this.client = new MongoClient("localhost");
            this.db = client.getDB(this.dbname);
            this.coll = db.getCollection(this.collname);
            this.running = true;
        } catch (UnknownHostException e) {
            throw new MongoException("Unable to setup", e);
        }
    }

    public void disconnect() {
        this.running = false;
        if (this.client != null) this.client.close();
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
        while (this.running) {
            long count = this.coll.count();
            this.notifyListeners(count);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface Listener {
        public void onCount(long count);
    }
}
