/*
 * Created on Dec 25, 2004
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

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SampleGraph extends JPanel {

	private short[] samples;

	/**
	 * @param leftSamples
	 */
	public SampleGraph(short[] samples) {
		this.samples = samples;
	}

	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.scale((double)getWidth()/samples.length,(double)getHeight()/65536.0);
		g2d.translate(0,32768);
		for(int n=samples.length/getWidth();n<samples.length;n+=(samples.length/getWidth()))
		{
			g2d.drawLine(n-samples.length/getWidth(),samples[n-samples.length/getWidth()],
						n,samples[n]);
		}
	}

}
