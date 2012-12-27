/**************************************************************************
 * vthierry@sophia.inria.fr, Copyright (C) 2004. All rights reserved. *
 **************************************************************************/

package org.javascool.tools;

// Used to catch exceptions
// Used for thread management
// Used for timer management

/**
 * Defines the implementation of a periodic task.
 * <div>The periodic task is:
 * <ul>
 * <li><b>defined</b> by a runnable called at each iteration, and one parameter
 * the <b><i>sampling-period</i></b>.</li>
 * <li><b>controlled</b> by the start/stop method
 * <li><a href="#start(int)">start()</a> (re)starting the iteration. <div>User
 * can overwrite the method with a construct of the form
 * <tt>public void start() { &lt;specific-code> super.start(); }</tt></div></li>
 * <li><a href="#stop()">stop()</a> which stops the iteration. <div>User can
 * overwrite the method with a construct of the form
 * <tt>public void stop() { super.stop(); &lt;specific-code> }</tt></div></li>
 * </ul>
 * </div>
 */
public class Sampler {
    private Runnable runnable;
    private int delay;

    // @bean
    public Sampler() {
    }

    /**
     * Sets the periodic task runnable.
     *
     * @param runnable The runnable to call periodically.
     * @return this
     */
    public Sampler setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    /**
     * Sets the periodic task sampling-period.
     *
     * @param delay The periodic task delay in milli-seconds.
     * @return this
     */
    public Sampler setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * (Re)starts the sampling of the runnable.
     */
    public void start() {
        error = null;
        (thread = new Thread(new Runnable() {
            // Loop with a time-period of samplingPeriod
            @Override
            public void run() {
                try {
                    for (loop = resume = true; loop; ) {
                        long t = System.currentTimeMillis();
                        Thread.yield();
                        if (resume) {
                            runnable.run();
                        }
                        spareTime = delay
                                - (int) (System.currentTimeMillis() - t);
                        if (spareTime > 0) {
                            try {
                                Thread.sleep(spareTime);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                } catch (Throwable e) {
                    error = e;
                }
                loop = resume = false;
            }
        })).start();
    }

    // This flag is true during sampling loop
    private boolean loop = false, resume = false;

    /**
     * Returns the iteraton thread.
     *
     * @return The thread with the periodic sampling or null if not running.
     */
    public Thread getThread() {
        return thread;
    }

    private Thread thread = null;

    /**
     * Pauses the iteration mechanism.
     */
    public void pause() {
        resume = false;
    }

    /**
     * Resumes the iteration after a pause.
     */
    public void resume() {
        resume = true;
    }

    /**
     * Tests if the iteration is paused
     *
     * @return True if the iteration has been paused via <tt>pause()</tt> false
     *         if the iteration has been resumed via <tt>resumed()</tt> or never
     *         paused.
     */
    public boolean isPaused() {
        return !resume;
    }

    /**
     * Returns the runtime-exception thrown by the iteration runnable.
     *
     * @return The runtime-exception thrown by the iteration runnable if any,
     *         else null.
     */
    public Throwable getThrowable() {
        return error;
    }

    private Throwable error = null;

    /**
     * Requires the sampling to stop.
     * <div>The current iteration, if any, terminates before stopping.</div>
     */
    public void stop() {
        loop = resume = false;
        thread = null;
    }

    /**
     * Returns the spare-time between two samplings.
     *
     * @return The last spare-time between two samplings, in milliseconds:
     *         <ul>
     *         <li>If negative, it indicates that the task overruns.</li>
     *         <li>It is equal to Integer.MIN_VALUE if no iteration has been
     *         completed.</li>
     *         <li>It depends on both the task execution time and other thread
     *         execution times.</li>
     *         </ul>
     */
    public int getSpareTime() {
        return spareTime;
    }

    private int spareTime = -Integer.MIN_VALUE;
}
