package org.mansart.mongocount;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.mansart.mongocount.exception.MongoException;

import java.net.UnknownHostException;
import java.util.ArrayList;

public final class Counter implements Configuration.Listener, Runnable {
    private ArrayList<Listener> listeners = new ArrayList<Listener>();
    private MongoClient client = null;
    private Configuration configuration = null;
    private Thread thread = null;

    public Counter(Configuration configuration) {
        this.configuration = configuration;
        this.configuration.addListener(this);
    }

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

    public void start() {
        if (!this.configuration.getDbname().isEmpty() &&
            !this.configuration.getCollname().isEmpty() &&
            this.configuration.getInterval() > 0) {

            this.thread = new Thread(this);
            this.thread.start();
        }
    }

    public void stop() {
        this.thread = null;
    }

    @Override
    public void onConfigurationUpdate() {
        this.stop();
        this.start();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListeners(long count) {
        for (Listener listener : this.listeners) {
            listener.onCount(count);
        }
    }

    @Override
    public void run() {
        DB db = this.client.getDB(this.configuration.getDbname());
        DBCollection coll = db.getCollection(this.configuration.getCollname());
        while (this.thread != null && Thread.currentThread().getId() == this.thread.getId()) {
            long count = coll.count();
            this.notifyListeners(count);
            try {
                Thread.sleep(this.configuration.getInterval() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface Listener {
        public void onCount(long count);
    }
}
