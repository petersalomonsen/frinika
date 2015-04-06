/*
 * Created on Mar 10, 2007
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

import com.frinika.audio.io.RandomAccessFileIF;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Provides an implementation of RandomAccessFileIF that uses a cyclic cache.
 * Collaborates with a BufferedRandomAccessFileManager.
 * This should be used to prefetch data and avoid disk seek glitches
 * 
 * 
 * File relative pointers
 * 
 *  ptr1 -> (ptr2-1)    files sample in buffer
 *  endPtr              end of file samples (+1)
 *  ptr                 being read from the cache.
 *  
 * @author pjl
 *
 */
public class BufferedRandomAccessFile implements RandomAccessFileIF  {

	private RandomAccessFile in;

	private byte[] cyclicCache;

	private int size; // size of the buffer

	private long ptr1; // position of my readPtr in samples from start of file

	private long ptr;

	private long endPtr;

	private boolean closing = false; // flag to say close me.

	private long ptr2; //

	private boolean reading = false; // true if audioProcess is reading

	private long ptr0;

	private BufferedRandomAccessFileManager manager;

	private boolean xrun=false;

	// private int nMissed=0; // how many bytes the audioProcess has lost and we
	// have not taken into account yet.

	public BufferedRandomAccessFile(RandomAccessFile in, int buffSize,
			BufferedRandomAccessFileManager manager) {
		this.in = in;
		this.cyclicCache = new byte[buffSize];
		this.ptr1 = 0;
		this.ptr2 = 0;
		try {
			this.endPtr = in.length();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.manager = manager;
		this.size = buffSize;
		assert (size > 0);
		try {
			this.ptr = in.getFilePointer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		manager.addClient(this);
	}

	/**
	 * Called from the thread filling buffer. 
	 * Makes sure audio thread is not using buffer.
	 *
	 */
	private void waitForRead() {
		while (reading) { // Can this ever happen if the processAudio is
			// real priority ?
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * ptr should already be set to postion in the file which we want to read
	 * from
	 * 
	 * @throws IOException
	 */
	void fillBuffer() throws IOException {
		
		waitForRead();

		// System.out.println(" fillBuffer " + available());

		if (ptr > ptr2 || ptr < ptr1 || xrun ) {
			xrun=false; 
//			System.out.println(" SEEK " + ptr);
			if (ptr < endPtr-size) { // plenty 
				in.seek(ptr);
				int nread=in.read(cyclicCache, 0, size);
				assert(nread==size);
				ptr1 = ptr;
				ptr0 = ptr1 % size;
				ptr2 = ptr1 + size;
			} else if (ptr < endPtr ){ //
				int nn=(int) (endPtr-ptr);
				in.seek(ptr);
				int nread=in.read(cyclicCache, 0,nn);
				assert(nread==nn);
				ptr1 = ptr;
				ptr0 = ptr1 % size;
				ptr2 = ptr1 + nn;
			} else {
				try {
					throw new Exception(" CAN NOT FILL BUFFER AFTER END OF FILE ");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {

			assert(ptr >= ptr1);
			ptr1 = ptr; // make room for more data

			int cPtr1 = (int) ((ptr1 - ptr0) % size);
			int cPtr2 = (int) ((ptr2 - ptr0) % size);

			
//			if (cPtr1 == cPtr2) {
//				System.out.println(" p1 = p2  should this be happening ? ");
//			}
//			
			long navail=(int) (endPtr-ptr2);
				
			assert(navail >= 0);
	
			if (cPtr2 > cPtr1) { // wrap around
				int n1 = size - cPtr2;

				if (n1 > 0) {
					if (n1 > navail) {
						int nn = in.read(cyclicCache, cPtr2, (int) navail);
						assert(nn==navail);
						ptr2 = ptr2+navail;
						return;
					}
					int nn = in.read(cyclicCache, cPtr2, n1);
					assert(nn == n1);
					ptr2 += n1;
					navail -=n1;
				}

				if (cPtr1 > 0) {

					if (cPtr1 > navail) {
						int nn = in.read(cyclicCache, 0 , (int) navail);
						assert(nn==navail);
						ptr2 = ptr2+navail;
						return;
					}
					
					int nn = in.read(cyclicCache, 0, cPtr1);
					ptr2+=cPtr1;
					assert (nn == cPtr1);
				}

			} else { // catch up
   				int n1 = cPtr1 - cPtr2;
				if (n1 > 0) {
					if (n1 > navail) {
						try {
							int nn = in.read(cyclicCache, cPtr2, (int) navail);
							assert(nn==navail);
							ptr2+=navail;
						} catch(Exception e) {
							System.out.println(cPtr1 + "  " +cPtr2 + "  " +navail+ "   "+  size);
							e.printStackTrace();
							xrun=true;
							return;
						}						
						return;
					}
					int nn = in.read(cyclicCache, cPtr2, n1);
					assert(nn==n1);
					ptr2+=n1;
				}
			}
		}
	}

	final public boolean isFull() {
		if (ptr2 >=endPtr) return true;
		if (xrun) return false;
		return availableInCache() == size;
	}

	public RandomAccessFile getRandomAccesFile() {
		return in;
	}

	final public int availableInCache() {
		return (int) (ptr2 - ptr);
	}

	/**
	 * read the next n bytes
	 * 
	 * @param byteBuffer
	 * @param offSet
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public int read(byte[] byteBuffer, int offSet, int n, boolean realTime)
			throws IOException {
		reading = true;

		if ( ptr >= endPtr) return 0; // TODO zeroize
		
		if (availableInCache() < n || (ptr > ptr2 || ptr < ptr1)) {
			if (realTime) {
			//	System.out.println("audio cache:  xrun");
				try {
					throw new Exception("audio cache:  xrun ");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				xrun=true;
				manager.wakeup();

			} else {
				System.out.println(" REALTIME read ");
				reading = false;
				fillBuffer();
				reading = true;
			}
		}
		// Here then we have enough data
		// . . . feed off the cache
		int cPtr1 = (int) ((ptr - ptr0) % size);
		int cPtr2 = (int) ((ptr2 - ptr0) % size);
		// System. out.println("pp " + ptr1 + " " + ptr2 + " " + ptr0);
		if (size - cPtr1 > n) { // one bite
			// System. out.println(cPtr1 + " " + offSet + " " + n + " " + size);
			System.arraycopy(cyclicCache, cPtr1, byteBuffer, offSet, n);
		} else { // two bite
			// System. out.println("Two bites");
			int nn = size - cPtr1;
			System.arraycopy(cyclicCache, cPtr1, byteBuffer, offSet, nn);
			int nn2 = n - nn;
			System.arraycopy(cyclicCache, 0, byteBuffer, offSet + nn, nn2);
		}

		ptr += n; // next read postion;
		reading = false;
		manager.wakeup();
		return n;

	}

	public long getFilePointer() {
		return ptr;
	}

	public long length() throws IOException {
		return in.length();
	}

	/**
	 * if (!realTime) may block. the buffer is filled else seek for processAudio
	 * thread. only sets the pointer (avoid synchronisation issues)
	 * 
	 * @param l
	 *            new file position
	 * @throws IOException
	 */
	public void seek(long l, boolean realTime) throws IOException {
		if (realTime) {
			ptr = l;
			fillBuffer();
		} else if (ptr != l) {
			ptr = l;
			manager.wakeup();
		}

	}

	public void close() {
		closing = true;
		manager.removeClient(this);
	}

	public RandomAccessFile getRandomAccessFile() {
		return in;
	}

}
