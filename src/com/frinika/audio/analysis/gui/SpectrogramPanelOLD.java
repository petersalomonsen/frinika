/* 
 * Copyright (c) 2006, Karl Helgason
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.frinika.audio.analysis.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.frinika.audio.analysis.constantq.FreqToBin;

public class SpectrogramPanelOLD extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	BufferedImage img;

	Graphics2D graphic;

	private int[] rgbarray;

	Dimension imageSize;

	Dimension size;

	int scaleX = 2;

	int scaleY = 4;

	private FreqToBin f2b;

	private double[] freq;

	private double[][] buffer;

	private double[][] pFreq;

	private boolean dirty = true;

	private double thresh;

	private double[][] dbuff;

	static final int nLevel=256;
	static Color fcol[]=new Color[nLevel];
	{
		for (int i=0;i<nLevel;i++) {
			fcol[i]=new Color(255,0,0,i);
		}
	}
	
	
	public SpectrogramPanelOLD() {
		setDoubleBuffered(false);
	}

	void createGraphics(Dimension newSize) {
		imageSize = newSize;
		size = new Dimension(newSize.width * scaleX, newSize.height * scaleY);

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsConfiguration graphicsConfiguration = graphicsEnvironment
				.getDefaultScreenDevice().getDefaultConfiguration();
		img = graphicsConfiguration.createCompatibleImage(imageSize.width,
				imageSize.height, Transparency.BITMASK);
		graphic = img.createGraphics();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (dirty)
			makeImage();
		g.drawImage(img, 0, 0, size.width, size.height, 0, 0, imageSize.width,
				imageSize.height, this);
		if (thresh == 0.0 ) return;
		
		double f1=0;
		double f2;
		double v1=0.0;

		boolean active = false;
		Color c = new Color(255, 0, 0);
		int h=imageSize.height*scaleY;
		for (int j = 0; j < imageSize.height; j++) {
			int bin=imageSize.height-j-1;
			for (int i = 0; i < imageSize.width; i++) {
				if (buffer[i][bin] > thresh) {
					if (active) {
						f2 = f2b.getBin(pFreq[i][bin]);
						double val = (buffer[i][bin] + v1) / 2.0;
						int idx=(int)(255*val);
						if (idx> 255) idx=255;
						else if (idx<0)idx=0;
						g.setColor(fcol[idx]);
						g.drawLine((i - 1) * scaleX, (int)(h-f1 * scaleY - scaleY/2), i
								* scaleX, (int) (h-f2 * scaleY - scaleY/2));
						f1=f2;
					} else {
						f1 = f2b.getBin(pFreq[i][bin]);
						v1 = buffer[i][bin];
						active=true;
					}
					
				} else {
					active=false;
				}
			}
		}

	}

	public Dimension getPreferredSize() {
		return size;
	}

	public Dimension getMinimumSize() {
		return size;
	}

	public Dimension getMaximumSize() {
		return size;
	}

	public void setData(double[][] buffer, double pFreq[][], double freq[],
			double dt, FreqToBin f2b, double thresh,double dbuff[][]) {
		this.f2b = f2b;
		this.freq = freq;
		this.buffer = buffer;
		this.dbuff = dbuff;
		this.pFreq = pFreq;
		this.thresh = thresh;
		int w = buffer.length;
		int h = buffer[0].length;
		Dimension sizeNew = new Dimension(w, h);
		if (!sizeNew.equals(imageSize))
			createGraphics(sizeNew);
		repaint();
	}

	private void makeImage() {
		if (!dirty)
			return;
		dirty = false;
		int w = buffer.length;
		int h = buffer[0].length;

		if (rgbarray == null || rgbarray.length < h) {
			rgbarray = new int[h];
		}

		int nBin=imageSize.height;
		for (int j = 0; j < imageSize.width; j++) {
			for (int i = 0; i < imageSize.height; i++) {
				int bin=nBin-i-1;
				int c_g = (int) (buffer[j][bin] * 255.0);
				int c_r = (int) (dbuff[j][bin] * 255.0);;
				//if ( c_r < 150 ) c_r=0;
				int c_b =  (int) ((1.0-dbuff[j][bin]) * 255.0);
				//if (c_b< 150 ) c_b=0;
				
				
				if (c_r > 255)
					c_r = 255;
				if (c_g > 255)
					c_g = 255;
				if (c_b > 255)
					c_b = 255;
				if (c_r < 0)
					c_r = 0;
				if (c_g < 0)
					c_g = 0;
				if (c_b < 0)
					c_b = 0;

				int color = (c_b) + (c_g << 8) + (c_r << 16) + (0xFF << 24);

				rgbarray[i] = color;
				img.setRGB(j, 0, 1, imageSize.height, rgbarray, 0, 1);

			}
		}
	}
}
