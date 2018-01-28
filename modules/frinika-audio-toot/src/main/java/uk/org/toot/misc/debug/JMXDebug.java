/* Copyright Steve Taylor 2006 */

package uk.org.toot.misc.debug;

import java.lang.management.*;

public class JMXDebug
{
    private ThreadMXBean mxbean;
	private long maxcpums = 0;

    public JMXDebug() {
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
            top(); // !!! blocks main thread
        }
    }

    protected void top() {
        while ( true ) {
            try {
				_top();
            	Thread.sleep(5000);
            } catch ( InterruptedException ie ) {
            }
        }
    }

    protected void _top() {
        long[] ids = mxbean.getAllThreadIds();
        ThreadInfo info;
        String name;
        int cpums;
        for ( int i = 0; i < ids.length; i++ ) {
            cpums = (int)(mxbean.getThreadCpuTime(ids[i]) / 1000000);
            if ( cpums < maxcpums / 100 ) continue;
            if ( cpums > maxcpums ) maxcpums = cpums;
            info = mxbean.getThreadInfo(ids[i]);
            name = info.getThreadName();
            System.out.println(cpums+" "+info.getBlockedTime()+" "+info.getWaitedTime()+" "+name);
        }
    }
}
