// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import org.tritonus.share.sampled.FloatSampleBuffer;

/**
 * Encapsulates buffered multi-channel sampled audio.
 * 
 * It has a ChannelFormat and enables meta information to be attached to buffers.
 * It can convert to another ChannelFormat (1->N and N->1 only)
 * 
 * It has a real-time property to allow AudioProcesses to discriminate
 * between real-time and non-real-time for quality purposes etc.
 * @see uk.org.toot.audio.server.NonRealTimeAudioServer
 * 
 * It can swap channel pairs.
 */
public class AudioBuffer extends FloatSampleBuffer
{
    private MetaInfo metaInfo;

    /**
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    private ChannelFormat channelFormat;
    private boolean realTime = true;
    private String name = "unknown";

    public AudioBuffer(String name, int channelCount, int sampleCount, float sampleRate) {
        super(channelCount, sampleCount, sampleRate);
        this.name = name;
        channelFormat = guessFormat();
    }

    public String getName() { return name; }

    protected void setChannelCount(int count) {
        if ( count == getChannelCount() ) return;
        if ( count < getChannelCount() ) {
        	for ( int ch = getChannelCount() - 1; ch > count - 1; ch-- ) {
            	removeChannel(ch);
        	}
        } else {
            while ( getChannelCount() < count ) {
                addChannel(false);
            }
        }
    }

    public void setMetaInfo(MetaInfo info) {
        metaInfo = info;
    }

    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
    * This method may be used by an AudioProcess to determine whether it can use
    * high quality algorithms that would be impossible in real-time.
    * If it's not in real-time an AudioProcess may take as long as it needs.
    */
    public boolean isRealTime() {
        return realTime;
    }

    /**
    * This method is intended for use by
    * uk.org.toot.audio.server.NonRealTimeAudioServer
    * No good will come from you calling it.
    */
    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    /**
     * Guesses format.
     */
    protected ChannelFormat guessFormat() {
        switch ( getChannelCount() ) {
        case 1: return ChannelFormat.MONO;
        case 2: return ChannelFormat.STEREO;
        case 4: return ChannelFormat.QUAD;
        case 6: return ChannelFormat.FIVE_1;
        }
        return ChannelFormat.STEREO;
    }

    public ChannelFormat getChannelFormat() {
        return channelFormat;
    }

    /**
     * May call setChannelCount accordingly
     */
    public void setChannelFormat(ChannelFormat format) {
    	if ( channelFormat == format ) return;
        channelFormat = format;
        if ( channelFormat != null ) {
        	setChannelCount(channelFormat.getCount());
        }
    }

    public void convertTo(ChannelFormat format) {
        if ( channelFormat == format ) return; // already requested format
        if ( format.getCount() == 1 ) { 				// N -> 1
            mixDownChannels();
            channelFormat = format;
        } else if ( channelFormat.getCount() == 1 ) {	// 1 -> N
        	int nc = format.getCount();
        	int ns = getSampleCount();
        	float[] samples = getChannel(0);
        	float gain = 1f / nc;
        	for ( int s = 0; s < ns; s++ ) {
        		samples[s] *= gain;
        	}
            expandChannel(nc);
            // does LFE need tweaking ??? !!!
            channelFormat = format;
        } else {										// N -> M
	        // how do we convert other formats ???
	        // get format with highest channel count to do it
    	    // because only it knows about that many channels
        	@SuppressWarnings("unused")
			ChannelFormat convertingFormat =
                channelFormat.getCount() > format.getCount()
            		? channelFormat : format;
//        	if ( convertingFormat.convertTo(format) ) {
//        	}
        }
    }

    // if mono, convert to stereo
    public void monoToStereo() {
		if ( getChannelCount() < 2 ) {
			convertTo(ChannelFormat.STEREO);
		}
    }
    
    public void swap(int a, int b) {
        int ns = getSampleCount();
        float[] asamples = getChannel(a);
        float[] bsamples = getChannel(b);
        float tmp;
        for ( int s = 0; s < ns; s++ ) {
			tmp = asamples[s];
            asamples[s] = bsamples[s];
            bsamples[s] = tmp;
        }
    }

    /**
     * The square of the buffer, the first part of rms calculations.
     * The root and mean have to be done elsewhere because we cannot
     * maintain state.
     * @return
     */
    public float square() {
        int ns = getSampleCount();
        int nc = getChannelCount();
		float sumOfSquares = 0f;
		float[] samples;
		for ( int c = 0; c < nc; c++ ) {
			samples = getChannel(c);
			for ( int s = 0; s < ns; s++ ) {
				float sample = samples[s];
				sumOfSquares += sample * sample;
			}
		}
		return sumOfSquares / (nc * ns);
    }
    
    /*
     * Encodes all Left/Right pairs to Mid/Side pairs
     * M = 0.5 * (L + R)
     * S = 0.5 * (L - R)
     */
    public boolean encodeMidSide() {
        int[] lefts = channelFormat.getLeft();
        int[] rights = channelFormat.getRight();
        assert lefts.length == rights.length;
        if ( lefts.length == 0 ) return false;
        int np = lefts.length;
        int ns = getSampleCount();
        for ( int p = 0; p < np; p++ ) {
            float[] left = getChannel(lefts[p]);
            float[] right = getChannel(rights[p]);
            for ( int s = 0; s < ns; s++ ) {
                float mid = 0.5f * (left[s] + right[s]);
                float side = 0.5f * (left[s] - right[s]);
                left[s] = mid;      // left is now mid  
                right[s] = side;    // right is now side
            }
        }
        return true;
    }
    
    /*
     * Decodes all Mid/Side pairs to Left/Right pairs
     * L = M + S
     * R = M - S
     */
    public boolean decodeMidSide() {
        int[] mids = channelFormat.getLeft();
        int[] sides = channelFormat.getRight();
        assert mids.length == sides.length;
        if ( mids.length == 0 ) return false;
        int np = mids.length;
        int ns = getSampleCount();
        for ( int p = 0; p < np; p++ ) {
            float[] mid = getChannel(mids[p]);
            float[] side = getChannel(sides[p]);
            for ( int s = 0; s < ns; s++ ) {
                float left = mid[s] + side[s];
                float right = mid[s] - side[s];
                mid[s] = left;      // mid is now left
                side[s] = right;    // side is now right
            }
        }
        return true;
        
    }
    
    /**
     * MetaInfo holds meta information for an AudioBuffer.
     * MetaInfo is intentionally immutable.
     * 'observers' will be able to simply detect a different MetaInfo
     * if any information is changed.
     */
    static public class MetaInfo
    {
        private String sourceLabel;
        private String sourceLocation;

        // for release 2 backward compatibility with audioservers
        public MetaInfo(String sourceLabel) {
        	this(sourceLabel, "");
        }
        
        public MetaInfo(String sourceLabel, String sourceLocation) {
            this.sourceLabel = sourceLabel;
            this.sourceLocation = sourceLocation;
        }

        public String getSourceLabel() {
            return sourceLabel;
        }
        
        public String getSourceLocation() {
        	return sourceLocation;
        }
    }
}
