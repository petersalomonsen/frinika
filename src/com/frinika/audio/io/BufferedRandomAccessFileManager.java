/*
 * Created on Mar 11, 2007
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

package com.frinika.audio.io;

import java.io.IOException;
import java.util.Vector;

public class BufferedRandomAccessFileManager implements Runnable {

	Vector<BufferedRandomAccessFile> files;

	Vector<BufferedRandomAccessFile> filesToFill;

	Thread thread;

	private boolean sleeping = false;
        private boolean running = true;
        
	public BufferedRandomAccessFileManager() {
		files = new Vector<BufferedRandomAccessFile>();
		filesToFill = new Vector<BufferedRandomAccessFile>();

		thread = new Thread(this);
		thread.setPriority(Thread.MAX_PRIORITY); // Should be higher than the GUI etc.
		thread.start();
	}

	public void addClient(BufferedRandomAccessFile file) {
		synchronized (this) {
			files.add(file);
		}
	}

	public void removeClient(BufferedRandomAccessFile file) {
		synchronized (this) {
			files.remove(file);
		}
	}

	
	public void run() {
		while (running) {
			synchronized (this) {
				filesToFill.clear();
				for (BufferedRandomAccessFile file : files) {
					if (!file.isFull()) {
						filesToFill.add(file);
					}
				}

				if (!filesToFill.isEmpty()) {
					for (BufferedRandomAccessFile file : filesToFill) {
						try {
							file.fillBuffer();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					try {
						sleeping = true;
					//	System.out.println(" Sleepy poohs ");
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						sleeping = false;
					//	System.out.println(" Wakeup ");
					//	e.printStackTrace();
					}
				}
			}
		}

	}

    public void stop() {
        running = false;
        wakeup();
    }

    public void wakeup() {
            if (sleeping)
                    thread.interrupt();
    }

}
