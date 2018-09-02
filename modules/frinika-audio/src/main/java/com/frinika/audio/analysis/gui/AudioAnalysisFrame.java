/*
 * Created on Mar 21, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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
package com.frinika.audio.analysis.gui;

import com.frinika.audio.DynamicMixer;
import com.frinika.audio.io.AudioReaderFactory;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JMenuBar;

public class AudioAnalysisFrame extends FocusFrame {

    AudioAnalysisPanel panel;

    public AudioAnalysisFrame(AudioReaderFactory part, DynamicMixer mixer) {
        init(part, mixer);
    }

    private void init(AudioReaderFactory part, DynamicMixer mixer) {
        setJMenuBar(new JMenuBar());
        setContentPane(panel = new AudioAnalysisPanel(part, mixer, this, getKeyboardFocusManager()));
        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void windowClosing(WindowEvent e) {
                panel.dispose();
                //		this.dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void windowIconified(WindowEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // TODO Auto-generated method stub
            }
        });
    }
}
