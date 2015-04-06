/*
 * Created on Mar 20, 2007
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
package com.frinika.audio.analysis.gui;

/* 
 * Draws a frequency plot with a keyboard (vertical slice of spectrogram)
 * 
 * 
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Observable;


import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.OscillatorNode;
import com.frinika.audio.analysis.SpectrogramDataListener;
import com.frinika.audio.analysis.SpectrumDataBuilder;
import com.frinika.audio.analysis.StaticSpectrogramSynth;

public class SpectralSliceImage extends Observable implements CursorObserver,
        SpectrogramDataListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    SpectrumDataBuilder spectroData;
    Mapper valMapper;
    Mapper freqMapper;
    long pixPtr = -1;
    int lastPix = -1;
    StaticSpectrogramSynth synth;
    private Rectangle renderRect;
    boolean isBlack[] = {false, true, false, false, true, false, true, false, false, true, false, true};
    float pixToFreq[];
	private float curBin;
    
    public SpectralSliceImage(SpectrumDataBuilder provider, Mapper valMapper,
            Mapper freqMapper, StaticSpectrogramSynth synth) {
        assert (freqMapper != null);
        this.spectroData = provider;
        this.freqMapper = freqMapper;
        this.valMapper = valMapper;
        this.synth = synth;

    // setBackground(Color.BLACK);
    }

    void setRect(Rectangle rect) {
        this.renderRect = rect;
        pixToFreq=new float[rect.height];
    }

    public void drawImage(Graphics2D g, int x, int y) {
        // System.out.println("Graph Paint");

        int w = renderRect.width;
        int h = renderRect.height;

        // top left;

        int xx = renderRect.x;
        int yy = renderRect.y;

        g.setColor(Color.BLACK);
        g.fillRect(xx, yy, w, h);


        // allow space for drawing piano keys.
        int hKey = h / 4;
        h -= hKey;

        g.setColor(Color.GREEN);

        float[] freq = spectroData.getFreqArray();
        float[] magn = spectroData.getMagnitudeAt(pixPtr);
     //   float[] magnX = spectroData.getSMagnitudeAt(pixPtr);

        if (magn == null) {
            g.drawString(" No data ... yet ", 10, 10);
            return;
        }

   //     System.out.println("bot freq "+freq[0]);


        assert (magn != null);
        if (pixPtr < 0) {
            return;
        }

        float[] pFreq = spectroData.getPhaseFreqAt(pixPtr);

        int n = magn.length;

        double scaleX = w;
        double scaleY = h;

        double fbot = freq[0];
        double ftop = freq[freq.length - 1];
        double a0Freq = 55.0 / 2;
        double semi = Math.pow(2.0, 1.0 / 12.0);
        double qemi = Math.pow(2.0, 1.0 / 24.0);

        int note = 0;
        int octave = 0;

        double f1 = a0Freq * qemi / semi;
        double f2 = f1 * semi;

        for (; f1 < ftop; f1 *= semi, f2 *= semi) {

            if (f2 > fbot) {
//            	boolean hiLite = f1 < freq[curBin] && f2 >= freq[curBin];
            	
                int xx1 = (int) (xx + scaleX * freqMapper.eval((float) f1));
                int xx2 = (int) (xx + scaleX * freqMapper.eval((float) f2));
                int hx = (int) ((xx2 - xx1) / 2.0);

                if (!isBlack[note]) {
                    if (isBlack[(note + 1) % 12]) {
                        xx2 += hx;
                    }
                    if (isBlack[(note + 11) % 12]) {
                        xx1 -= hx;
                    }
                    g.setColor(Color.WHITE);
                    g.fill3DRect(xx1, yy + h, xx2 - xx1, hKey, true);
                }
//         //       char nc=(char) ('A' + note);
//                String str=String.format("%c", nc);

            }
            note = note + 1;
            if (note >= 12) {
                note = 0;
                octave = octave + 1;
            }

        }
        note = 0;
        octave = 0;

        f1 = a0Freq * qemi / semi;
        f2 = f1 * semi;

        for (; f1 < ftop; f1 *= semi, f2 *= semi) {

            if (f2 > fbot) {
                int xx1 = (int) (xx + scaleX * freqMapper.eval((float) f1));
                int xx2 = (int) (xx + scaleX * freqMapper.eval((float) f2));
                int hx = (int) ((xx2 - xx1) / 2.0);

                if (isBlack[note]) {
                    g.setColor(Color.BLACK);
                    g.fill3DRect(xx1, yy + h, xx2 - xx1, (int) (hKey * 0.55), true);
                }
//         //       char nc=(char) ('A' + note);
//                String str=String.format("%c", nc);

            }
            note = note + 1;
            if (note >= 12) {
                note = 0;
                octave = octave + 1;
            }

        }

        // for (int j = 0; j < 2; j++) {

        int x1 = xx + (int) (freqMapper.eval(freq[0]) * scaleX);
        int y1 = yy + h - (int) (valMapper.eval(magn[0]) * scaleY);
        for (int i = 1; i < n; i++) {
            int x2 = xx + (int) (freqMapper.eval(freq[i]) * scaleX);
            int y2 = yy + h - (int) (valMapper.eval(magn[i]) * scaleY);

            // System.out.println(x1 + " " + y2);

            g.setColor(Color.GREEN);

            g.drawLine(x1, y1, x2, y2);
            if (pFreq != null) {
                int x3 = xx + (int) (freqMapper.eval(pFreq[i]) * scaleX);
                g.setColor(Color.RED);

                g.drawLine(x2, yy + h, x3, y2);
            }
            x1 = x2;
            y1 = y2;
        }

        // now the smooth version

        if (magn!= null) {
            x1 = xx + (int) (freqMapper.eval(freq[0]) * scaleX);
            y1 = yy + h - (int) (valMapper.eval(magn[0]) * scaleY);
            for (int i = 1; i < n; i++) {
                int x2 = xx + (int) (freqMapper.eval(freq[i]) * scaleX);
                int y2 = yy + h - (int) (valMapper.eval(magn[i]) * scaleY);

                // System.out.println(x1 + " " + y2);

                g.setColor(Color.RED);

                g.drawLine(x1, y1, x2, y2);
                if (pFreq != null) {
                    int x3 = xx + (int) (freqMapper.eval(pFreq[i]) * scaleX);
                    g.setColor(Color.RED);

                    g.drawLine(x2, yy + h, x3, y2);
                            }
                x1 = x2;
                y1 = y2;
            }
        }

        int pixlast = -100;

//        for (int i = 0; i < n; i++) {
//            int x2 = xx + (int) (freqMapper.eval(freq[i]) * scaleX);
//
//            if (x2 - pixlast > 50) {
//
//                g.setColor(Color.WHITE);
//                g.drawString(String.format(" %d ", (int) freq[i]), x2 - 2, yy + h - 2);
//                pixlast = x2;
//            }
//        }

        int i1=(int)curBin;
        if (i1 >= freq.length) i1=freq.length-1;
        int i2=i1+1;
        double fact2=curBin-i1;
        double fact1=1.0-fact2;
        
        int curX=xx + (int) (freqMapper.eval((float) (freq[i1]*fact1+freq[i2]*fact2) ) * scaleX);
          
        g.setColor(Color.WHITE);
      
        g.drawLine(curX, yy + h, curX, yy);
        
        
        if (synth != null) {

            for (OscillatorNode osc : synth.getOscillatorBank()) {
                if (!osc.active) {
                    continue;
                }
                x1 = xx + (int) (freqMapper.eval((float) osc.getFreq()) * scaleX);
                y1 = yy + h - (int) (valMapper.eval((float) osc.getAmp()) * scaleY);
                g.setColor(Color.BLUE);
                g.drawLine(x1, yy + h, x1, y1);
            }

        }

    }

    public void notifyCursorChange(int pix,float curBin) {
    	if (curBin == this.curBin  && pixPtr == pix) {
            return;
        }
        pixPtr = pix;
        this.curBin=curBin;
        setChanged();
        notifyObservers();
    // repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension(1000, 120);
    }

    public void notifySizeChange(Dimension d) {
        lastPix = -1;
        setChanged();
        notifyObservers();
    }

    public void notifyMoreDataReady() {
        int nextPix = spectroData.getChunkRenderedCount();
        if ((nextPix < lastPix || lastPix < pixPtr) && nextPix >= pixPtr) {
            setChanged();
            notifyObservers();
        }

        lastPix = nextPix;

    }
}
