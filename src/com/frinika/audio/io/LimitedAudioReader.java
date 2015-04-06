
/*
 *
 * Copyright (c) 2006 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.audio.io;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/**
 *
 *  A reader which provides a view of a portion of a wave file.
 * 
 * @author pjl
 */
public interface LimitedAudioReader extends AudioProcess {

    public int getChannels();

    public int getEnvelopedLengthInFrames();

    public AudioFormat getFormat();

    public double getSampleRate();

    public int processAudio(AudioBuffer buffer);

    public void seekEnvelopeStart(boolean b) throws IOException;

    public void seekFrameInEnvelope(long framePtr, boolean b) throws IOException;

}
