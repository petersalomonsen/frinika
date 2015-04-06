/*
 * Created on Jan 1, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.synth.synths.sampler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SampleMapTableRenderer extends Component implements TableCellRenderer {
	SampleMapCellInfo current;
	
	SampleMapCellInfo[][] sampleMapCellInfo = new SampleMapCellInfo[96][128];
	
	public class SampleMapCellInfo {
		SampledSoundSettings sampledSound;
		int startX;
		boolean isEndColumn = true;
	}

	public SampleMapTableRenderer()
	{
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(isSelected)
			setBackground(Color.BLUE);
		
				
		if(sampleMapCellInfo[row][column]==null ||
				sampleMapCellInfo[row][column].sampledSound!=value)
		{
			findStartColumn(table,(SampledSoundSettings)value,row,column);			
			try
			{
				if(value.toString().equals(table.getValueAt(row,column+1).toString()))
					sampleMapCellInfo[row][column].isEndColumn = false;
			} catch(Exception e) {}
		}
		
		this.current = sampleMapCellInfo[row][column];
				
		return this;
	}

	int findStartColumn(JTable table, SampledSoundSettings value, int row, int column)
	{		
		try
		{
			if(value !=null && value.toString().equals(table.getValueAt(row,column).toString()))
			{
				int startColumn = findStartColumn(table,value,row,column-1);
				if(sampleMapCellInfo[row][column]==null)
				{
					sampleMapCellInfo[row][column] = new SampleMapCellInfo();
					sampleMapCellInfo[row][column].sampledSound = value;
					sampleMapCellInfo[row][column].startX = (startColumn - column);
				}
				
				return(startColumn);
			}
			else
				return column+1;
		}
		catch(NullPointerException e)
		{
			return column+1;
		}
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}
	
	public void paint(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();

		g.setColor(getBackground());
		g.fillRect(0,0,width,height);
		
		g.setColor(Color.BLACK);

		if(current!=null)
		{
			if(current.startX==0)
				g.drawLine(0,0,0,height);

			if(current.isEndColumn)
				g.drawLine(width-1,0,width-1,height);
			g.drawLine(0,0,width,0);
			g.drawLine(0,height-1,width,height-1);
			g.drawString(current.sampledSound.toString(),(current.startX*width)+10,12);
		}
	}		
}
