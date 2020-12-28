package com.jayfella.jme.vehicle;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * A Thread used to preload a Loadable into the AssetCache.
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
    final private CountDownLatch latch;
    /**
     * thing to load
     */
    final private Loadable loadable;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Thread to load the specified Loadable.
     *
     * @param loadable the thing to load (not null)
     * @param latch notify the creator when done
     */
    Preloader(Loadable loadable, CountDownLatch latch) {
        this.loadable = loadable;
        this.latch = latch;
    }
    // *************************************************************************
    // new methods exposed

    @Override
    public void run() {
        loadable.load();
        latch.countDown();
    }
}
