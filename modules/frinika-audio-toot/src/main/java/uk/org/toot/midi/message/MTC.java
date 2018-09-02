package uk.org.toot.midi.message;

/**
 * A support class for MIDI Time Code.
 * @author st
 *
 */
public class MTC
{
	public static enum FrameRate
	{
		FPS_24		(0, 24),
		FPS_25		(1, 25),
		FPS_30DF	(2, 30), // TODO drop frame
		FPS_30		(3, 30);
		
		private final int index;
		private final float rate;
		
		FrameRate(int index, float rate) {
			this.index = index;
			this.rate = rate;
		}
		
		public int getIndex() { return index; }
		
		public float getRate() { return rate; }
	}
	
	public static class Time
	{
		public int hours;
		public int minutes;
		public int seconds;
		public int frames;
		public int fractionalFrames;
		
		public Time() {
			clear();
		}
		
		public void clear() {
			hours = minutes = seconds = frames = fractionalFrames = 0;
		}
	}
}
