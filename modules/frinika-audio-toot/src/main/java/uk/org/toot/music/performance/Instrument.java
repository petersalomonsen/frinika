package uk.org.toot.music.performance;

/**
 * An Instrument is a program on a channel.
 * Most likely a MIDI program on a MIDI channel.
 * @author st
 *
 */
public class Instrument 
{
	private int channel;
	private int program;
	
	public Instrument(int channel, int program) {
		setChannel(channel);
		setProgram(program);
	}
	
	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}
	
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	/**
	 * @return the program
	 */
	public int getProgram() {
		return program;
	}
	
	/**
	 * @param program the program to set
	 */
	public void setProgram(int program) {
		this.program = program;
	}
}
