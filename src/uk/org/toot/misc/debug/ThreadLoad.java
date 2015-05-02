/* Copyright Steve Taylor 2006 */

package uk.org.toot.misc.debug;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.management.*;

public class ThreadLoad extends Observable
{
    protected static ThreadMXBean mxbean;
    protected static Timer timer;

    static {
        // set up ThreadMXBean
        mxbean = ManagementFactory.getThreadMXBean();
        if ( !mxbean.isThreadCpuTimeSupported() ) {
            System.out.println("Thread CPU Time is NOT supported");
        } else {
            if ( !mxbean.isThreadCpuTimeEnabled() ) {
                mxbean.setThreadCpuTimeEnabled(true);
            }
            if ( mxbean.isThreadContentionMonitoringSupported() ) {
                if ( !mxbean.isThreadContentionMonitoringEnabled() ) {
                    mxbean.setThreadContentionMonitoringEnabled(true);
                }
            } else {
	            System.out.println("Thread Contention Monitoring is NOT supported");
            }
        }
        // set up timer for polling id
        timer = new Timer("ThreadLoad Timer");
    }

    public ThreadLoad(String threadName, long milliseconds) {
        // get Id for name
        long threadId = getThreadId(threadName);
        if ( threadId < 0 ) {
            // throw Exception
            System.err.println(threadName+" thread id not found");
        } else {
        	timer.schedule(new Task(threadId), milliseconds, milliseconds); // initial delay, period
        }
    }

    protected static long getThreadId(String threadName) {
        long[] ids = mxbean.getAllThreadIds();
        for ( int i = 0; i < ids.length; i++ ) {
            if ( mxbean.getThreadInfo(ids[i]).getThreadName().equals(threadName) ) {
                return ids[i];
            }
        }
        return -1;
    }

    protected class Task extends TimerTask
    {
        private long id;
        private long prevNanos = 0;
        private long prevCpuNanos = 0;
        private long prevUserNanos = 0;
        private long prevBlockedMillis = 0;
        private long prevWaitedMillis = 0;

        public Task(long threadId) {
            id = threadId;
        }

        /**
         * Called periodically to derive thread loads
         */
        public void run() {
            // calculate elapsed time
            long nanos = System.nanoTime();
            long elapsed = nanos - prevNanos;
            prevNanos = nanos;
            Info loadInfo = new Info();
            // load average
            long cpuNanos = mxbean.getThreadCpuTime(id);
            loadInfo.cpu = (int)(100 * (cpuNanos - prevCpuNanos) / elapsed);
            prevCpuNanos = cpuNanos;
            // user average
            long userNanos = mxbean.getThreadUserTime(id);
            loadInfo.user = (int)(100 * (userNanos - prevUserNanos) / elapsed);
            prevUserNanos = userNanos;
            // derive thread information
            ThreadInfo info = mxbean.getThreadInfo(id);
            if ( info == null ) return;
            // convert elapsed from nanos to millis
			elapsed /= 1000000;
            // blocked average
            long blockedMillis = info.getBlockedTime();
            loadInfo.blocked = (int)(100 * (blockedMillis - prevBlockedMillis) / elapsed);
            prevBlockedMillis = blockedMillis;
            // waiting average
            long waitedMillis = info.getWaitedTime();
            loadInfo.waited = (int)(100 * (waitedMillis - prevWaitedMillis) / elapsed);
            prevWaitedMillis = waitedMillis;
            // notify observers of loadInfo
            System.out.println(info.getThreadName()+": "+loadInfo.cpu+"% "+loadInfo.user+"% "+loadInfo.blocked+"% "+loadInfo.waited+"%");
        }
    }

    public class Info
    {
        public int cpu;
        public int user;
        public int blocked;
        public int waited;
    }
}
