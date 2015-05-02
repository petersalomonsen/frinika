package uk.org.toot.audio.server;

import java.util.List;

import uk.org.toot.audio.core.AudioBuffer;

/**
 * This class provides generic AudioServer functionality suitable for all AudioServer
 * implementationst.
 * It provides:
 * Buffer management (synchronous resizing)
 * Client Management (enable/diable/work)
 * Start/Stop management (for delayed start)
 * @author st
 *
 */
public abstract class AbstractAudioServer implements AudioServer
{
    private List<AudioBuffer> audioBuffers = new java.util.ArrayList<AudioBuffer>();

    protected int bufferFrames;

    private boolean startASAP = false;

    protected long startTimeNanos;
	protected long endTimeNanos;
	private long prevStartTimeNanos;
	private float load;

    /**
     * a single client, use Composite pattern for multi client
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    private AudioClient client;

    public AbstractAudioServer() {
        try {
	        Runtime.getRuntime().addShutdownHook(
	            new Thread() {
	            	public void run() {
	                	AbstractAudioServer.this.stop();
	            	}
	        	}
	    	);
        } catch ( Exception e ) {
        	System.out.println("AudioServer Failed to add Shutdown Hook");
        }
    }

    protected AudioBuffer _createAudioBuffer(String name) {
        return new AudioBuffer(name, 2, bufferFrames, getSampleRate()); // SR TODO
    }

    public AudioBuffer createAudioBuffer(String name) {
        AudioBuffer buffer = _createAudioBuffer(name);
        audioBuffers.add(buffer);
        return buffer;
    }

    public void removeAudioBuffer(AudioBuffer buffer)  {
        audioBuffers.remove(buffer);
    }

    protected void resizeBuffers(int bufferFrames) {
    	this.bufferFrames = bufferFrames;
        for ( AudioBuffer buffer : audioBuffers ) {
            buffer.changeSampleCount(bufferFrames, false); // don't keep old
        }
    }

    protected int getBufferFrames() {
    	return bufferFrames;
    }
    
	public float getLoad() {
		return load;
	}

    public void setClient(AudioClient client) {
        this.client = client;
        checkStart(); // start may be delayed waiting for a client to be set
    }

    protected void work() {
		prevStartTimeNanos = startTimeNanos;
        startTimeNanos = System.nanoTime();
        client.work(bufferFrames);
        endTimeNanos = System.nanoTime();

        // calculate client load
        load = (float)(endTimeNanos - startTimeNanos) / (startTimeNanos - prevStartTimeNanos);
    }

    public void start() {
        if ( isRunning() ) return;
        if ( canStart() ) {
        	client.setEnabled(true);
            startImpl();
            startASAP = false;
           	System.out.println("AudioServer started");
        } else {
	       	System.out.println("AudioServer start requested but delayed");
            startASAP = true;
        }
    }

    public void stop() {
        if ( !isRunning() ) return;
       	System.out.println("AudioServer stopping");
        stopImpl();
        client.setEnabled(false);
    }


    protected void checkStart() {
        if ( startASAP ) {
			if ( canStart() ) {
				client.setEnabled(true);
            	startImpl();
                startASAP = false;
               	System.out.println("AudioServer started");
            } else {
//                System.out.println("AudioServer start still delayed");
            }
        }
    }

    protected boolean canStart() {
        return client != null;
    }


	protected abstract void startImpl();

	protected abstract void stopImpl();

}
