package com.jayfella.jme.vehicle;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * A Thread used to preload assets into the AssetCache.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class Preloader extends Thread {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Preloader.class.getName());
    // *************************************************************************
    // fields

    /**
     * notify the creator when done
     */
    final private CountDownLatch completionLatch;
    /**
     * things to load
     */
    final private Queue<Loadable> loadables;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Thread to load loadables from the specified Queue.
     *
     * @param loadables the things to load (not null)
     * @param completionLatch to notify the creator when done
     */
    Preloader(Queue<Loadable> loadables, CountDownLatch completionLatch) {
        this.loadables = loadables;
        this.completionLatch = completionLatch;
    }
    // *************************************************************************
    // new methods exposed

    @Override
    public void run() {
        Loadable loadable;
        while (true) {
            loadable = loadables.poll();
            if (loadable == null) {
                break;
            }

//            long startMillis = System.currentTimeMillis();
            loadable.load();

//            String name = loadable.getClass().getSimpleName();
//            long latencyMillis = System.currentTimeMillis() - startMillis;
//            float seconds = latencyMillis / 1_000f;
//            System.out.println("loaded " + name + " in " + seconds + " sec.");
        }

        completionLatch.countDown();
    }
}
