/*
 * Created on Feb 8, 2007
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

/**
 * Interface to a reference-copy ("ghost") of a Part.
 * 
 * Ghosts appear as Parts in the Tracker-view, but do not contain their own 
 * events. Instead, Ghosts are internally linked to the origial Part from which
 * they have been created. They represent the original Part transparently, but
 * are not editable themselves. All changed applied to the original Part will
 * immediately take effect on Ghosts also. 
 * 
 * (Currently only implemented for MidiParts.)
 * 
 * @see MidiPartGhost
 * @see com.frinika.sequencer.gui.menu.RepeatAction
 * @author Jens Gulden
 */
public interface Ghost {

	public Part getReferredPart();
	
	public long getStartTick();
	
	public Lane getLane();
	
}
