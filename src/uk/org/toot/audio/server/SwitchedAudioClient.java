/*
 * Created on 23-Feb-2007
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

package uk.org.toot.audio.server;

import java.util.Stack;

/**
 *  An adapter to allow switching of clients.
 *  It operatates as a stack so revert() will go back to the previous client installed.
 *  Client can be null (useful to disable the audio).
 *  
 *  To stop the server calling any clients work (e.g. to rescue CPU ovderload use setEnabled(false)
 *  
 * @author pjl
 * @author st tweaked a little for toot
 *
 */
public class SwitchedAudioClient implements AudioClient {
    /**
     * @supplierCardinality 0..1 
     */
	private AudioClient client;

	Stack<AudioClient> stack = new Stack<AudioClient>();
	boolean attached=true;
	
	public synchronized void work(int size) {
		if (attached && client != null)
			client.work(size);
	}

	public void setEnabled(boolean enabled) {
		attachServer(enabled);
	}
	
	public synchronized void installClient(AudioClient m) {
		if (client != null)
			client.setEnabled(false);
		client = m;
		if (client != null )
			client.setEnabled(true);
		stack.push(client);
	}

	
	public synchronized void revertClient() {
		if (client != null ) client.setEnabled(false);
		stack.pop();
		client = stack.peek();
		if (client != null )  client.setEnabled(true);
	}

	/*
	 * @deprecated - use setEnabled(boolean b) instead
	 */
	public synchronized void attachServer(boolean yes) {
		if (yes == attached) return;
		attached=yes;	
		
		if (attached) {
			if (client != null) client.setEnabled(true);
		} else {
			if (client != null) client.setEnabled(false);
		}
	}
	
}
