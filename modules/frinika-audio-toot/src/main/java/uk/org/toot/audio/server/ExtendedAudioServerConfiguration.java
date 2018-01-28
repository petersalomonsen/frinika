package uk.org.toot.audio.server;

import com.frinika.toot.PriorityAudioServer;
import java.util.Properties;

public class ExtendedAudioServerConfiguration extends AudioServerConfiguration
{
	private final static String USER_BUFFER = ".buffer.milliseconds";
	private final static String LATENCY = ".latency.milliseconds";
	private final static String PRIORITY = ".priority";
	
	private ExtendedAudioServer server;
	private boolean hasPriority;
	
	public ExtendedAudioServerConfiguration(ExtendedAudioServer server) {
		this.server = server;
		hasPriority = System.getProperty("os.name").equals("Linux") &&
			server instanceof PriorityAudioServer;
	}
	
	public Properties getProperties() {
		Properties p = new Properties();
		String k = server.getConfigKey();
		p.setProperty(k+USER_BUFFER, String.valueOf(server.getBufferMilliseconds()));
		p.setProperty(k+LATENCY, String.valueOf(server.getLatencyMilliseconds()));
		if ( hasPriority ) {
			PriorityAudioServer pas = (PriorityAudioServer)server;
			p.setProperty(k+PRIORITY, String.valueOf(pas.getPriority()));
		}
		return p;
	}

	public void applyProperties(Properties p) {
    	if ( p == null ) {
     	   System.err.println("null properties passed to ExtendedAudioServerConfiguration.applyProperties()");
     	   return;
     	}
		String value;
		String k = server.getConfigKey();
		value = p.getProperty(k+USER_BUFFER);
		if ( value != null ) {
			server.setBufferMilliseconds(Float.valueOf(value));
		}
		value = p.getProperty(k+LATENCY);
		if ( value == null ) { // frinika backwards compatibility
			value = p.getProperty(k+"_outputBuffer");
		}
		if ( value != null ) {
			server.setLatencyMilliseconds(Float.valueOf(value));
		}	
		if ( hasPriority ) {
			value = p.getProperty(k+PRIORITY);
			if ( value == null ) { // frinika backwards compatibility
				value = p.getProperty(k+"_priority");
			}
			if ( value != null ) {
				PriorityAudioServer pas = (PriorityAudioServer)server;
				pas.requestPriority(Integer.valueOf(value));
			}
		}
	}
}
