/* 
 * Copyright (c) 2007 P.J.Leonard
 * 
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
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.SpectrogramDataListener;
import com.frinika.audio.analysis.SpectrumDataBuilder;

/**
 * 
 * notifies observers if it need to be redrawn (typically the panel(s))
 * 
 * update() redoes all the drawing.
 * 
 * SpectrogramListener
 *   - does image resize 
 *   - incrementally redraws as the data become ready.
 * 
 * @author pjl
 *
 */
public class SpectrogramImage extends Observable  implements SpectrogramDataListener,
		Observer {

	private static final long serialVersionUID = 1L;

	BufferedImage img;

	Graphics2D graphic;

	private int[] rgbarray;

	Dimension imageSize;

	Dimension size;

	int scaleX = 1;

	int scaleY = 2;

	private boolean dirty = true;

	private double thresh;

	private SpectrumDataBuilder data;

	static final int nLevel = 256;

	static Color fcol[] = new Color[nLevel];
	{
		for (int i = 0; i < nLevel; i++) {
			fcol[i] = new Color(255, 0, 0, i);
		}
	}

	int nChunks;

	int nBins;
	
	private int renderedCount = 0;

	Mapper mapper;

    /**
     *
     *
     * @param data
     * @param mapper
     */
	public SpectrogramImage(SpectrumDataBuilder data, Mapper mapper) {
		// setDoubleBuffered(false);
		this.mapper = mapper;
		this.data = data;
		// mapper.update(this, null);
	}

	void createGraphics() {

		imageSize = new Dimension(nChunks, nBins);
		size = new Dimension(nChunks * scaleX, nBins * scaleY);

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsConfiguration graphicsConfiguration = graphicsEnvironment
				.getDefaultScreenDevice().getDefaultConfiguration();
		img = graphicsConfiguration.createCompatibleImage(imageSize.width,
				imageSize.height, Transparency.BITMASK);
		graphic = img.createGraphics();
	}

	private void makeImage() {

		synchronized (data) {
			int chunksToRender=data.getChunkRenderedCount();
			System.out.println(renderedCount + "  ->  " + chunksToRender );
			nBins = data.getBinCount();
			nChunks=data.getSizeInChunks();
			if (nChunks == 0 || nBins == 0)
				return;

			if (imageSize == null || nBins != imageSize.height
					|| nChunks != imageSize.width)
				createGraphics();

			if (rgbarray == null || rgbarray.length < nBins) {
				rgbarray = new int[nBins];
			}

			float buffer[][] = data.getMagnitude();
			if (buffer == null)
				return;
			for (; renderedCount < chunksToRender; renderedCount++) {
				if (Thread.interrupted())
					return;
				for (int i = 0; i < nBins; i++) {
					int bin = nBins - i - 1;
					float val = mapper.eval(buffer[renderedCount][bin]);

					if (val < 0)
						val = 0.0f;
					if (val > 1.0)
						val = 1.0f;
					int c_r = (int) (255 * val);
					int c_g = c_r;
					int c_b = 255 - c_r;

					int color = (c_b) + (c_g << 8) + (c_r << 16) + (0xFF << 24);

					rgbarray[i] = color;
				}
				img.setRGB(renderedCount, 0, 1, imageSize.height, rgbarray, 0, 1);

			}
			setChanged();
			notifyObservers();
		}
	}

	public void notifySizeChange(Dimension d) {
		// if (d.equals(size))
		// return;
		renderedCount=0;
		System.out.println(" Remaking spectrogram image ");
		makeImage();
	}

	public void drawImage(Graphics2D g, int i, int j) {
	//	System.out.println(" Spectro DRAWIMAGE");
		if (img == null)
			return;
		g.setColor(Color.WHITE);
		g.drawImage(img, i, j, size.width, size.height, 0, 0, imageSize.width,
				imageSize.height, null);
		g.setColor(Color.GREEN);
		g.drawString(" Spectrogram ", i + 10, j + 10);
	}

	public int getHeight() {
		if (size == null)
			return 200;
		return size.height;
	}

	/**
	 * 
	 */
	public void update(Observable o, Object arg) {
		System.out.println(" Spectrogram image update ");
		//  null argument means I need to draw whole image 
		renderedCount=0;   
		makeImage();
	}

	public void notifyMoreDataReady() {
		makeImage();
	}

	public float pixToBin(int curY) {
		
		float bin=(int) (nBins-(curY+1.5)/scaleY);
		//System.out.println(bin);
		if (bin <0.0)bin=0.0f;
		
		return bin;
	}

}
