package org.mansart.mongocount;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.mansart.mongocount.util.HumanDate;

import java.util.ArrayList;

public final class Counter implements Runnable {
    private final ArrayList<Listener> listeners = new ArrayList<>();
    private Configuration configuration = null;
    private Thread thread = null;
    private long startTimestamp = System.currentTimeMillis();

    public Counter(Configuration configuration) {
        this.configuration = configuration;
        this.initListeners();
    }

    private void initListeners() {
        this.configuration.addListener(new Configuration.Listener() {

            @Override
            public void onUpdate() {
                Counter.this.stop();
                Counter.this.startTimestamp = System.currentTimeMillis();
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
        for (Listener listener : this.listeners) {
            listener.onStart();
        }
    }

    private void notifyListenersOfCountStop() {
        for (Listener listener : this.listeners) {
            listener.onStop();
        }
    }

    private void notifyListenersOfCount(long time, long count) {
        for (Listener listener : this.listeners) {
            listener.onCount(time, count);
        }
    }

    private void notifyListenersOfCountError() {
        for (Listener listener : this.listeners) {
            listener.onError();
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
                long time = System.currentTimeMillis() - this.startTimestamp;
                System.out.println("[" + HumanDate.now() + "] " + count);
                this.notifyListenersOfCount(time, count);
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
        public void onCount(long time, long count);
        public void onError();
    }
}
