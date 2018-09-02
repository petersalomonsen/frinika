package uk.org.toot.audio.server;

public class DefaultStrategy extends Strategy {

	Thread thread;
	AudioTimingStrategy timingStrategy;
	
	DefaultStrategy(){
		timingStrategy=new SleepTimingStrategy();
		
	}
	
	/* (non-Javadoc)
	 * @see uk.org.toot.audio.server.Strategy#getSleepStrategy()
	 */
	public AudioTimingStrategy getSleepStrategy() {
		return new SleepTimingStrategy();
	}
	
	/* (non-Javadoc)
	 * @see uk.org.toot.audio.server.Strategy#run(java.lang.Runnable, java.lang.String)
	 */
	public void run(Runnable runner,String name) {
      	thread = new Thread(runner, name);
       	thread.start();	
	}

	/* (non-Javadoc)
	 * @see uk.org.toot.audio.server.Strategy#setPriority()
	 */
	public void setPriority() {
		thread.setPriority(timingStrategy.getThreadPriority());
	}

	/* (non-Javadoc)
	 * @see uk.org.toot.audio.server.Strategy#nanoTime()
	 */
	public long nanoTime() {
		
		return System.nanoTime();
	}

	@Override
	public void block(long nowNanos, long sleepNanos) {
		timingStrategy.block(nowNanos, sleepNanos);		
	}
	
	
}
