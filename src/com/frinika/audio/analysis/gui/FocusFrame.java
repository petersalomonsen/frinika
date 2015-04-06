/*
 * Created on 24 Dec 2007
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

import java.awt.KeyboardFocusManager;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class FocusFrame extends JFrame {

	
	KeyboardFocusManager oldkbm;
	
	KeyboardFocusManager kbd;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean dorun=true;
	Thread thread;
	
	FocusFrame() {
		
		if (oldkbm == null) this.oldkbm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	
		KeyboardFocusManager.setCurrentKeyboardFocusManager(null);
		kbd = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		
		Runnable runner =new Runnable() {
		
			public synchronized void run() {
				
				while(dorun) {
					KeyboardFocusManager.setCurrentKeyboardFocusManager(kbd);
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					// e.printStackTrace();
					}
				}
			}
		};
		thread=new Thread(runner);
		thread.start();
		
		addWindowListener(new WindowListener() {

			public void windowOpened(WindowEvent arg0) {
			}

			public void windowClosing(WindowEvent evt) {
				evt.getWindow().dispose();
				KeyboardFocusManager.setCurrentKeyboardFocusManager(oldkbm);
				dorun=false;
			}

			public void windowClosed(WindowEvent arg0) {

			}

			public void windowIconified(WindowEvent arg0) {

			}

			public void windowDeiconified(WindowEvent arg0) {

			}

			public void windowActivated(WindowEvent arg0) {
				thread.interrupt();
			}

			public void windowDeactivated(WindowEvent arg0) {

			}
		});

	}
	
	public KeyboardFocusManager getKeyboardFocusManager() {
		return kbd;
	}
}
