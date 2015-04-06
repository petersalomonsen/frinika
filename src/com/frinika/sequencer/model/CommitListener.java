/*
 * Created on Feb 9, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.model;

import com.frinika.sequencer.model.MultiEvent;

/**
 * Listens to commit operations of MultiEvents to or from their corresponding Track.
 * Note that listeners get added or removed from the MultiEvent's owning part, not
 * the MultiEvent itself. 
 * 
 * @see MultiEvent.commitAdd
 * @see MultiEvent.commitRemove
 * @see MidiPart.addCommitListener
 * @see MidiPart.removeCommitListener
 * @author Jens Gulden
 */
public interface CommitListener {
	
	/**
	 * Notifies the listener that a event.commitAdd() has been performed.
	 * @param event note that MultiEvent is not a subclass of Java's Event class, here the source-object is directly passed as parameter to the listener-method without being wrapped in an Event 
	 */
	public void commitAddPerformed(MultiEvent event);

	/**
	 * Notifies the listener that a event.commitRemove() has been performed.
	 * @param event note that MultiEvent is not a subclass of Java's Event class, here the source-object is directly passed as parameter to the listener-method without being wrapped in an Event 
	 */
	public void commitRemovePerformed(MultiEvent event);
}
