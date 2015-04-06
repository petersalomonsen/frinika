/*
 * Created on 21 Aug 2007
 *
 * Copyright (c) 2004-2007 Peter Johan Salomonsen
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

package com.frinika.tootX.plugins.reverb;



import com.frinika.tootX.plugins.*;
import java.awt.Color;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.delay.AbstractDelayControls.MixControl;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;


public class ReverbControls extends AudioControls implements
		ReverbProcessVariables {

	private FloatControl mix;
	private FloatControl level;
	private FloatControl room;
	private FloatControl damp;
	private FloatControl width;
	
	   protected static final ControlLaw UNITY_LIN_LAW = new LinearLaw(0f, 1f, "");
	   
	public ReverbControls() {
		super(Ids.REVERB_MODULE, "Reverb");
		mix  = new FloatControl(0,"mix",UNITY_LIN_LAW,.01f, .3f,"dry","mix","wet");
		mix.setInsertColor(Color.BLUE);
		level  = new FloatControl(0,"level",UNITY_LIN_LAW,.01f, .3f);
		level.setInsertColor(Color.BLUE);
		room = new FloatControl(1,"size",UNITY_LIN_LAW,.01f, .5f);
		room.setInsertColor(Color.YELLOW);
		damp = new FloatControl(2,"damp",UNITY_LIN_LAW,.01f, .5f);
		damp.setInsertColor(Color.YELLOW);
		width = new FloatControl(3,"width",UNITY_LIN_LAW,.01f, .5f);
		width.setInsertColor(Color.YELLOW);

		ControlColumn col=new ControlColumn();
			
		ControlRow row=new ControlRow();
		row=new ControlRow();		
		row.add(damp);
		row.add(width);
		col.add(row);
		row=new ControlRow();	
		row.add(level);
		row.add(mix);
		col.add(row);
		add(col);
	}

	public float getMix() {
		return mix.getValue();
	}
	
	public float getLevel() {
		return level.getValue();
	}

	
	public float getWidth() {
		return width.getValue();
	}

	public float getDamp() {
		return damp.getValue();
	}
	
	public float getRoomSize() {
		return room.getValue();
	}
	
	public boolean canBypass() { return true; }
	
}

