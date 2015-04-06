/*
 * Created on Feb 19, 2005
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.filters;

import javax.swing.JFrame;

import com.frinika.audio.Decibel;

/**
 * @author Peter Johan Salomonsen
 *
 */
public final class LoPass {
    float previousSample = 0;
    float cutOff = 0.1f;
        
    public final void setCutOff(float cutOff)
    {
        this.cutOff = cutOff;
    }
    
    public final float filter(float sample)
    {
        float delta = sample-previousSample; 
        if(delta>cutOff)
            sample=previousSample+cutOff;
        else if(delta<-cutOff)
            sample=previousSample-cutOff;
        
        previousSample = sample;
        
        return(sample);
    }
    
    public static void main(String[] args)
    {
        
        final LoPass loPass = new LoPass();
        loPass.setCutOff(0.001f);
        final JFrame frame = new JFrame()
        {
            public void paint(java.awt.Graphics g)
            {
                g.clearRect(0,0,600,600);
                int prevX = 0;
                int prevY = 300;
                for(float n = 0;n<Math.PI*2;n+=0.01)
                {
                    float w1 = (float)Math.sin(n)*2;
                    float w2 = (float)Math.sin(n*32)/4f;
                    float w = loPass.filter(w1+w2);
                    int x = (int)((n/(Math.PI*2)) * 600f);
                    int y = (int)(((w)*100f)+300);
                    System.out.println(x+" "+y+" "+loPass.cutOff);
                    g.drawLine(prevX,prevY,x,y);
                    prevX = x;
                    prevY = y;
                }
            }
        };
        frame.setVisible(true);
        frame.setSize(600,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        new Thread()
        {
            public void run()
            {
                float cutOff = -1;
                while(cutOff>-100f)
                {
                    frame.repaint();
                    cutOff-=0.5;
                    loPass.setCutOff(Decibel.getAmplitudeRatio(cutOff));
                    try
                    {
                        Thread.sleep(500);
                    } catch(Exception e) {}
                }
                
            }
        }.start();
        
    }

}
