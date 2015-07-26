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

package com.frinika.web;

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
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.servlet.ServletContainer;


/**
 * @author Peter Johan Salomonsen
 */
public class WebServicesController {

    static WebServicesController instance = new WebServicesController();

    final int port = 15000;

    ProjectContainer project = null;

    private WebServicesController()
    {

    }

    private void startListener()
    {
        final RadioAudioProcess rap = new RadioAudioProcess(project);
	if(project!=null) {
	    project.getMixer().getMainBus().setOutputProcess(rap);
	}
        Server server = new Server(15000);
	HandlerCollection hc = new HandlerCollection();
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
	
	ResourceHandler rh = new ResourceHandler();
	rh.setBaseResource(Resource.newClassPathResource("/com/frinika/web/content/"));

	hc.addHandler(rh);
	hc.addHandler(handler);
		
	server.setHandler(hc);
        handler.addServlet(new ServletHolder(new HttpServlet() {

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
               } catch(EofException ex) {
                    System.out.println("Connection closed by listener");
               }
               catch (Exception ex) {
                    Logger.getLogger(WebServicesController.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally {
                    if(tdl!=null)
                    {
                        tdl.close();
                    }
                }
            }

        }),
                "/frinika.ogg");
        
	ServletHolder sh = new ServletHolder(ServletContainer.class);  
        sh.setInitOrder(1);	
	sh.setInitParameter("jersey.config.server.provider.packages","com.frinika.web.rest");
	handler.addServlet(sh, "/restservices/*");
	
	try {
            server.start();
        } catch (Exception ex) {
            Logger.getLogger(WebServicesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void startRadio(ProjectContainer project)
    {
        instance.project = project;
        instance.startListener();
        
        Logger.getLogger(WebServicesController.class.getName()).info("Local OGG Http Radio started");
    }

    public static void stopRadio()
    {
        Logger.getLogger(WebServicesController.class.getName()).info("Local OGG Http Radio stopped");
    }
    
    
    public static void main(String[] args) {
	WebServicesController.startRadio(null);
    }
}
