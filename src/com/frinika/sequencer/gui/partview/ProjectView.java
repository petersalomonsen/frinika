/*
 * Created on Jun 22, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

package com.frinika.sequencer.gui.partview;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;

import com.frinika.sequencer.model.ProjectLane;

public class ProjectView extends LaneView {
	private static final long serialVersionUID = 1L;
	
	public ProjectView(ProjectLane lane) {
		super(lane);
		init();
	}
	

	protected void makeButtons() {
		add(new JLabel("PROJECT VIEW (TODO)"),gc);
			gc.weighty = 1.0;
			add(new Box.Filler(new Dimension(0, 0),
					new Dimension(10000, 10000), new Dimension(10000, 10000)),
					gc);

	}


}
