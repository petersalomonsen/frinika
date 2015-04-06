package com.frinika.codeexamples;

import java.io.File;

import com.frinika.voiceserver.AudioContext;
import com.frinika.project.ProjectContainer;

/*
 * Created on Mar 8, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

/**
 * Example of loading a Frinika project and playing it without opening the Frinika gui.
 * @author Peter Johan Salomonsen
 */
public class StandaloneProjectPlayer {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File file = new File("/home/peter/mystudio/");
                for(final File f : file.listFiles())
                {
                    if(f.getName().contains(".frinika"))
                    {
                        System.out.println("Loading project "+f.getName());
                        Runnable projectPlayRunnable = new Runnable() {

                            public void run() {
                                ProjectContainer projectContainer = null;
                                try
                                {
                                    projectContainer = ProjectContainer.loadProject(f);
                                    projectContainer.getAudioServer().start();
                                    projectContainer.getSequencer().start();
                                    System.out.println("Playing");                                                                                           
                                } catch(Exception e) {
                                    projectContainer.close();
                                }
                            }
                        };
                        Thread thr = new Thread(projectPlayRunnable);
                        thr.start();
                        System.in.read();
                        thr.interrupt();
                        thr.join();
                        System.gc();
                    }
                }
	}

}
