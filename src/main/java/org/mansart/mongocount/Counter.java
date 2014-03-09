package org.mansart.mongocount;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public final class Counter implements Configuration.Listener, Runnable {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private Configuration configuration = null;
    private Thread thread = null;

    public Counter(Configuration configuration) {
        this.configuration = configuration;
        this.configuration.addListener(this);
    }

    public void start() {
        this.thread = new Thread(this);
        this.thread.start();
        this.notifyListenersOfCountStart();
    }

    public void stop() {
        this.thread = null;
        this.notifyListenersOfCountStop();
    }

    public boolean isStarted() {
        return this.thread != null && this.thread.isAlive();
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

    private void notifyListenersOfCountStart() {
        for (Listener listener : this.listeners) {
            listener.onCountStart();
        }
    }

    private void notifyListenersOfCountStop() {
        for (Listener listener : this.listeners) {
            listener.onCountStop();
        }
    }

    private void notifyListenersOfCount(long count) {
        for (Listener listener : this.listeners) {
            listener.onCount(count);
        }
    }

    private void notifyListenersOfCountError() {
        for (Listener listener : this.listeners) {
            listener.onCountError();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println(this.configuration.toString());
            MongoClient client = new MongoClient(this.configuration.getHost(), this.configuration.getPort());
            DB db = client.getDB(this.configuration.getDbname());
            DBCollection coll = db.getCollection(this.configuration.getCollname());
            while (this.thread != null && Thread.currentThread().getId() == this.thread.getId()) {
                long count = coll.count();
                System.out.println("[" + DATE_FORMAT.format(new Date()) + "] " + count);
                this.notifyListenersOfCount(count);
                Thread.sleep(this.configuration.getInterval() * 1000);
            }
            client.close();
        } catch (Exception e) {
            this.stop();
            this.notifyListenersOfCountError();
            System.out.println("Processing error...");
            e.printStackTrace();
        }
    }

    public static interface Listener {
        public void onCountStart();
        public void onCountStop();
        public void onCount(long count);
        public void onCountError();
    }
}
