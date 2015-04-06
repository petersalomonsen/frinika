/*
 * Created on Apr 11, 2007
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

package com.frinika.audio.analysis;

import java.util.Vector;


public abstract class DataBuilder {
	
	protected Vector<SpectrogramDataListener> sizeObservers = new Vector<SpectrogramDataListener>();
	
	private Thread runThread = null;

	private boolean isConstructing;

	public DataBuilder() {
		isConstructing = false;
		runThread=null;
	}


	/**
	 * Does the building. Should test Thread.interrupted() and return ASAP if true.
	 *
	 */
	protected abstract void doWork();

	
	public boolean isConstructing() {
		if (runThread == null)
			return false;
		return runThread.isAlive();
	}

	
	protected void abortConstruction() {
		
		/* could possibly put this on a thread if doWork is not responsive ?
		 * 
		 */
		while (isConstructing()) {
			runThread.interrupt();
			try {
				Thread.sleep(20); // bigger delays con
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		runThread=null;
	}

	protected void startConstruction() {
		
		// final Worker worker=createWorker();
		
		Runnable runner= new Runnable() {
			public void run() {
				System.out.println(" DO WORK ");
				doWork();
				runThread=null;
			}
		};
		
		runThread = new Thread(runner);
		runThread.start();
	}
	
	public void dispose() {
		sizeObservers.clear();		
	}

}
