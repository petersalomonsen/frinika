/*
 * Created on Dec 20, 2009
 *
 * Copyright (c) 2004-2009 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

package com.frinika.radio;

import com.frinika.project.ProjectContainer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;


/**
 * @author Peter Johan Salomonsen
 */
public class LocalOGGHttpRadio {

    static LocalOGGHttpRadio instance = new LocalOGGHttpRadio();

    final int port = 15000;

    ProjectContainer project = null;

    private LocalOGGHttpRadio()
    {

    }

    private void startListener()
    {
        final RadioAudioProcess rap = new RadioAudioProcess(project);
        project.getMixer().getMainBus().setOutputProcess(rap);

        Server server = new Server(15000);
        Context root = new Context(server,"/",Context.SESSIONS);
        root.addServlet(new ServletHolder(new HttpServlet() {

            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                TargetDataLine tdl = null;
                try {
                    InputStream is = req.getInputStream();
                    int c = is.read();
                    while(c!=-1)
                    {
                        c = is.read();
                    }
                    AudioFileFormat.Type oggType = null;
                    for(AudioFileFormat.Type aff : AudioSystem.getAudioFileTypes())
                    {
                        System.out.println(aff.getExtension());
                        if (aff.getExtension().equals("ogg"))
                        {
                            oggType = aff;
                        }
                    }

                    resp.setContentType("application/ogg");
                    OutputStream os = resp.getOutputStream();
                    System.out.println("Start writing");

                    tdl = rap.getNewTargetDataLine();
                    AudioSystem.write(new AudioInputStream(tdl), oggType, os);
                    System.out.println("Done writing");
               } catch(org.mortbay.jetty.EofException ex) {
                    System.out.println("Connection closed by listener");
               }
               catch (Exception ex) {
                    Logger.getLogger(LocalOGGHttpRadio.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally {
                    if(tdl!=null)
                    {
                        tdl.close();
                    }
                }
            }

        }),
                "/*");
        try {
            server.start();
        } catch (Exception ex) {
            Logger.getLogger(LocalOGGHttpRadio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void startRadio(ProjectContainer project)
    {
        instance.project = project;
        instance.startListener();
        
        Logger.getLogger(LocalOGGHttpRadio.class.getName()).info("Local OGG Http Radio started");
    }

    public static void stopRadio()
    {
        Logger.getLogger(LocalOGGHttpRadio.class.getName()).info("Local OGG Http Radio stopped");
    }
}
