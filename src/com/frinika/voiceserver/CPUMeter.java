/*
 * Created on Nov 14, 2004
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
package com.frinika.voiceserver;

import com.frinika.audio.*;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class CPUMeter extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;

    int cpuPercent;
    JavaSoundVoiceServer audioOutput;

    JLabel cpuTextLabel = new JLabel();
	public CPUMeter(JavaSoundVoiceServer audioOutput)
	{
        setLayout(new GridLayout());
        add(new JLabel("Cpu usage: "));
        add(cpuTextLabel);
        
        this.audioOutput = audioOutput;
		Thread thr = new Thread(this);
		thr.start();
	}
	
	public void setCpuPercent(int cpuPercent)
	{
		if(cpuPercent>this.cpuPercent)
			this.cpuPercent = cpuPercent;
	}
		
    int statCount = 0;
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(400);
			} catch(Exception e) {}
			
			cpuTextLabel.setText(cpuPercent+" %");
            if(cpuPercent>90)
                cpuTextLabel.setForeground(Color.RED);
            else
                cpuTextLabel.setForeground(Color.BLACK);
			cpuPercent = 0;
            
            if((statCount++)%10==0)
            {
                audioOutput.printStats();
            }
		}
	}
}
