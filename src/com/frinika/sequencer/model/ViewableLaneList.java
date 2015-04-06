/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.model;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.Layout;

/**
 * Provides a view of the Lanes for display purposes.
 * If any lane is hidden / opened  / removed or added
 * This needs to get rebuilt and the partview redrawn.
 * 
 * @author Paul
 *
 */
public class ViewableLaneList implements Iterable<Lane>  {

	ProjectContainer project;
	Vector<Lane> lanes;
	int y;
	int id;
	
	public ViewableLaneList(ProjectContainer project){
		this.project=project;
		
		rebuild();
		
	}

	public void rebuild() {
		lanes=new Vector<Lane>();
		y=0;
		id=0;
		Lane root= project.getProjectLane();
		rebuild(root);
	}
	
	private void rebuild(Lane root) {
		if (!root.isHidden()) {
			
			int height = root.getHeight()*Layout.getLaneHeightScale();
			root.setDisplayPos(y, height,id++);
			y+= height;			
			lanes.add(root);
			
		}
		if (root.isOpen() && root.getChildren() != null ) {
			for (Lane child:root.getChildren()) {
				rebuild(child);
			}
		}
		
	}
	
	public List<Lane> getVisibleLanes() {
		return lanes;
	}

	public Iterator<Lane> iterator() {
		return lanes.iterator();
	}
	
}
