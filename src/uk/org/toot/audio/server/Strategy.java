package uk.org.toot.audio.server;

public abstract class Strategy {

	int cnt=0;
	
//	public abstract AudioTimingStrategy getSleepStrategy();

	public abstract void run(Runnable runner, String name);

	public abstract void setPriority();

	public abstract long nanoTime();

	public void notifyLoad(long startTimeNanos, long endTimeNanos,
			long totalTimeNanos) {
		long overrun = (endTimeNanos - startTimeNanos) - totalTimeNanos;
	
		if (overrun > 0)
			System.out.println("Overrun " + overrun);
	}

	public abstract void block(long nowNanos, long sleepNanos);
		

}