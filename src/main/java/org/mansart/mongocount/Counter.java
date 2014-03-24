package org.mansart.mongocount;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import org.mansart.mongocount.util.HumanDate;

import java.util.ArrayList;

public final class Counter implements Runnable {
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private Configuration configuration = null;
    private Thread thread = null;

    public Counter(Configuration configuration) {
        this.configuration = configuration;
        this.initListeners();
    }

    private void initListeners() {
        this.configuration.addListener(new Configuration.Listener() {

            @Override
            public void onUpdate() {
                Counter.this.stop();
                Counter.this.start();
            }

            @Override
            public void onError() {
                Counter.this.stop();
            }
        });
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

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListenersOfCountStart() {
        this.listeners.forEach(Listener::onStart);
    }

    private void notifyListenersOfCountStop() {
        this.listeners.forEach(Listener::onStop);
    }

    private void notifyListenersOfCount(long count) {
        this.listeners.forEach((Listener listener) -> listener.onCount(count));
    }

    private void notifyListenersOfCountError() {
        this.listeners.forEach(Listener::onError);
    }

    @Override
    public void run() {
        try {
            System.out.println(this.configuration.toString());
            MongoClient client = new MongoClient(this.configuration.getHost(), this.configuration.getPort());
            DB db = client.getDB(this.configuration.getDbname());
            DBCollection coll = db.getCollection(this.configuration.getCollname());
            while (this.thread != null && Thread.currentThread().getId() == this.thread.getId()) {
                String query = configuration.getQuery();
                long count = query.isEmpty() ? coll.count() : coll.count((DBObject) JSON.parse(query));
                System.out.println("[" + HumanDate.now() + "] " + count);
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
        public void onStart();
        public void onStop();
        public void onCount(long count);
        public void onError();
    }
}
