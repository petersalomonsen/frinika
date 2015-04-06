/*
 * Copyright (c) 2004-2010 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 *
 * http://www.frinika.com
 *
 * This file is part of Frinika.
 *
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.frinika.radio;

import com.frinika.project.ProjectContainer;
import java.util.ArrayList;
import java.util.HashSet;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class RadioAudioProcess implements AudioProcess {
    HashSet<RadioStreamTargetDataLine> tdLines = new HashSet<RadioStreamTargetDataLine>();
    ArrayList<RadioStreamTargetDataLine> newTdLines = new ArrayList<RadioStreamTargetDataLine>();
    ArrayList<RadioStreamTargetDataLine> closedTdLines = new ArrayList<RadioStreamTargetDataLine>();

    ProjectContainer project;

    public RadioAudioProcess(ProjectContainer project) {
        this.project = project;
    }


    public void open() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int processAudio(AudioBuffer buffer) {
        /**
         * Convert floats to bytes - this is for export wav
         */

        for(RadioStreamTargetDataLine tdLine : newTdLines)
            tdLines.add(tdLine);

        for(RadioStreamTargetDataLine tdLine : tdLines)
        {
            if(newTdLines.contains(tdLine))
                newTdLines.remove(tdLine);

            if(tdLine.isOpen())
            {
                int i = 0;
                for(int n = 0;n<buffer.getSampleCount();n++)
                {
                    //Left
                    float floatSample = buffer.getChannel(0)[n];
                    short sample;

                    if(floatSample>=1.0f)
                        sample=0x7fff;
                    else if(floatSample<=-1.0f)
                        sample=-0x8000;
                    else
                        sample = (short)(floatSample*0x8000);

                    byte[] frame = new byte[4];
                    frame[0] = (byte)(sample & 0xff);
                    frame[1] = (byte)((sample & 0xff00) >> 8);

                    //Right

                    floatSample = buffer.getChannel(1)[n];

                    if(floatSample>=1.0f)
                        sample=0x7fff;
                    else if(floatSample<=-1.0f)
                        sample=-0x8000;
                    else
                        sample = (short)(floatSample*0x8000);

                   frame[2] = (byte)(sample & 0xff);
                   frame[3] = ((byte)((sample & 0xff00) >> 8));

                   tdLine.addFrame(frame);
                }
            }
            else {
                closedTdLines.add(tdLine);
            }
        }
        for(RadioStreamTargetDataLine tdLine : closedTdLines)
            tdLines.remove(tdLine);

        closedTdLines.clear();

        return project.getOutputProcess().processAudio(buffer);
    }

    public TargetDataLine getNewTargetDataLine() throws LineUnavailableException {
        RadioStreamTargetDataLine tdl = new RadioStreamTargetDataLine();
        tdl.open();
        newTdLines.add(tdl);
        return tdl;
    }

    public void close() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
