/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.frinika.audio.io;

import java.io.IOException;

/**
 *
 *  Interface to provide LimitedAudioReader
 *
 * @author pjl
 */
public interface AudioReaderFactory {

    public LimitedAudioReader createAudioReader() throws IOException;

}
