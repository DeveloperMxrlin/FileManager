/*
 * Copyright (c) 2022 Mxrlin
 * See LICENSE
 */

package mxrlin.file.misc;

public class Timer {

    private long started;
    private long stopped;

    public Timer(){
        started = -1;
        stopped = -1;
    }

    public void reset(){
        started = -1;
        stopped = -1;
    }

    public void start(){
        started = System.currentTimeMillis();
    }

    public void stop(){
        stopped = System.currentTimeMillis();
    }

    public long time(){
        return stopped-started;
    }

    public long stopAndTime(){
        stopped = System.currentTimeMillis();
        return time();
    }

}
