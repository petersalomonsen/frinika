package uk.org.toot.misc;

import java.util.List;

/**
 * This class knows the tempo.
 * It is needed in case there isn't something like a sequencer that has tempo
 * so that the user can set the tempo.
 * If there is something like a sequencer that has tempo it should set its
 * tempo using this class.
 * @author st
 *
 */
public class TimeSignature
{
	private static List<Listener> listeners = new java.util.ArrayList<Listener>();
	private static int numerator = 4;
	private static int denominator = 4;
	
	private TimeSignature() {} // prevent instantiation
	
	public static int getNumerator() {
		return numerator;
	}
	
	public static int getDenominator() {
		return denominator;
	}
	
	public static void setTimeSignature(int aNumerator, int aDenominator) {
		numerator = aNumerator;
		denominator = aDenominator;
		for ( Listener l : listeners ) {
			l.timeSignatureChanged(numerator, denominator);
		}
	}
	
	public static void addTimeSignatureListener(Listener listener) {
		if ( listener == null ) return;
		listeners.add(listener);
		listener.timeSignatureChanged(numerator, denominator);
	}
	
	public static void removeTimeSignatureListener(Listener listener) {
		if ( listener == null ) return;		
		listeners.remove(listener);
	}
    
    /**
     * A plugin wishing to receive time signature change notifications should implement this
     * interface and add (and remove) it using PluginSupport.
     * @author st
     *
     */
    public interface Listener
    {
        void timeSignatureChanged(int numerator, int denominator);
    }

}
