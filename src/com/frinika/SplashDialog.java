/*
 * Created on 27.2.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.frinika.project.gui.ProjectFrame;

public class SplashDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	int sel = 0;
	int ix = 0;
	
	JProgressBar processbar = new JProgressBar();
	{
		processbar.setMinimum(0);
		processbar.setMaximum(100);
	}
	
	public JProgressBar getProgressBar()
	{
		return processbar;
	}
	
	private static SplashDialog splash = null;
	public static SplashDialog getInstance()
	{		
		if(splash == null) splash = new SplashDialog();
		return splash;
	}
	
	public static boolean isSplashVisible()
	{
		if(splash == null) return false;		
		return splash.isVisible();
	}
	
	public static void showSplash()
	{
		SplashDialog splash = getInstance();
		splash.setVisible(true);
		splash.animation.start();		
	}
	
	public static void closeSplash()
	{
		if(splash == null) return;
		splash.setVisible(false);
		splash = null;
	}
	
	public static void main(String[] args)
	{
		showSplash();
	}

	int cloud_width;
	public SplashDialog()
	{		
		setUndecorated(true);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.WHITE);
		Icon welcome = new javax.swing.ImageIcon(ProjectFrame.class.getResource("/frinika.png"));
		JLabel label = new JLabel(welcome);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		label.setText(AboutDialog.MAIN_TITLE);
		label.setBorder(BorderFactory.createEmptyBorder(25,5,5,5));
		panel.add(label, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
		
		JPanel contentpane = new JPanel();
		contentpane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		contentpane.setLayout(new BorderLayout());
		contentpane.add(panel);
		setContentPane(contentpane);
		
		/*
		JPanel buttonpanel = new JPanel();
		buttonpanel.setOpaque(false);
		for (int i = 0; i < options.length; i++) {
			ix = i;
			JButton button = new JButton(options[i].toString());
			button.addActionListener(new ActionListener()
					{
					    int index = ix; 
						public void actionPerformed(ActionEvent e) {
							sel = index;
							setVisible(false);
						}
					});
			if(i == 0)
			{
				button.setDefaultCapable(true);
				getRootPane().setDefaultButton(button);
			}
			buttonpanel.add(button);
		}
		panel.add(buttonpanel, BorderLayout.CENTER); */
		panel.add(processbar);
		
		JPanel copyrightpanel = new JPanel();
		copyrightpanel.setOpaque(false);

		JLabel line = new JLabel(AboutDialog.COPYRIGHT_NOTICE);
		line.setHorizontalTextPosition(SwingConstants.CENTER);
		line.setFont(line.getFont().deriveFont(10f).deriveFont(Font.PLAIN));		
		
		copyrightpanel.add(line);

		panel.add(copyrightpanel, BorderLayout.SOUTH);
		
		setTitle("Welcome");
		
		pack();
		
	    Rectangle windowSize ;
	    Insets windowInsets;		
		
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();	    
	    if(gc == null) 
	        gc = getGraphicsConfiguration();	    
	    
	    if(gc != null) {
	    	windowSize = gc.getBounds();
	    } else {
	    	windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
	    }	    						
		
		Dimension size = getSize();		
		Point parent_loc = getLocation();			
		setLocation(parent_loc.x + windowSize.width/2 - (size.width/2),
				    parent_loc.y + windowSize.height/2 - (size.height/2));

		Icon frinika_light = new javax.swing.ImageIcon(ProjectFrame.class.getResource("/frinika_light_gradient.png"));
		light_label = new JLabel(frinika_light);
		light_label.setLocation(-400, 60);
		light_label.setSize(frinika_light.getIconWidth(), frinika_light.getIconHeight());				
		getLayeredPane().add(light_label, javax.swing.JLayeredPane.MODAL_LAYER);

		
		Icon frinika_cloud = new javax.swing.ImageIcon(ProjectFrame.class.getResource("/frinika_score.png"));
		cloud_width = frinika_cloud.getIconWidth();
		light_cloud1 = new JLabel(frinika_cloud);
		light_cloud1.setLocation(cloud_width, 75);
		light_cloud1.setSize(frinika_light.getIconWidth(), frinika_light.getIconHeight());				
		getLayeredPane().add(light_cloud1, javax.swing.JLayeredPane.MODAL_LAYER);		
		light_cloud2 = new JLabel(frinika_cloud);
		light_cloud2.setLocation(0, 75);
		light_cloud2.setSize(frinika_light.getIconWidth(), frinika_light.getIconHeight());				
		getLayeredPane().add(light_cloud2, javax.swing.JLayeredPane.MODAL_LAYER);
		
				
		
		Icon frinika_overscan = new javax.swing.ImageIcon(ProjectFrame.class.getResource("/frinika_overscan.png"));
		JLabel light_overscan = new JLabel(frinika_overscan);
		light_overscan.setLocation(22, 43);
		light_overscan.setSize(frinika_overscan.getIconWidth(), frinika_overscan.getIconHeight());				
		getLayeredPane().add(light_overscan, javax.swing.JLayeredPane.POPUP_LAYER);		
		
		
		
		
        Point loc = getLocation();
		BufferedImage img = null; //captureBackGround(); , skip transparent background
		if(img == null) 
		{
			JLabel border_panel = new JLabel();
			border_panel.setLocation(0, 0);
			border_panel.setSize(getSize());
			border_panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
			border_panel.setOpaque(false);
			getLayeredPane().add(border_panel, javax.swing.JLayeredPane.POPUP_LAYER);			
			return;
		}
		
		/*
        Kernel kernel = new Kernel(3, 3,
                new float[] {
                    1f/9f, 1f/9f, 1f/9f,
                    1f/9f, 1f/9f, 1f/9f,
                    1f/9f, 1f/9f, 1f/9f});		
        bak = new ConvolveOp(kernel).filter(bak, null);
        bak = new ConvolveOp(kernel).filter(bak, null);           
        		       
*/		
		
        Kernel kernel = new Kernel(3, 3,
                new float[] {
                    1f/9f, 1f/9f, 1f/9f,
                    1f/9f, 1f/9f, 1f/9f,
                    1f/9f, 1f/9f, 1f/9f});					
		
        img = img.getSubimage(loc.x, loc.y, size.width, size.height);
		ConvolveOp co = new ConvolveOp(kernel);
		img = co.filter(img, null);
		img = co.filter(img, null);
		img = co.filter(img, null);
		img = new RescaleOp(0.08f, 256f * 0.90f, null).filter(img, null);
				
		BufferedImage bak = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bak.createGraphics();
		//g.drawImage(img, -loc.x,-loc.y, null);						
		g.drawImage(img, 0,0, null);
		g.dispose();
		

		
		//Apply Mask
		BufferedImage mask;
		try {
			mask = ImageIO.read(ProjectFrame.class.getResource("/frinika_mask.png"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		int[] rgbData = new int[mask.getWidth()];
		int[] brgbData = new int[bak.getWidth()];
		int w = mask.getWidth();
		int bw = bak.getWidth();
		for (int i = 0; i < mask.getHeight(); i++) {
			mask.getRGB(0,i,w,1,rgbData,0,0);
			
			bak.getRGB(0,i+43,bw,1,brgbData,0,0);
			for (int j = 0; j < w; j++) {
				int m = (rgbData[j] & 0xFF) * 0x1000000;					
				brgbData[j+22] = (brgbData[j+22] & 0x00FFFFFF) + m; // (brgbData[j+22] & 0xFF000000); 
			}
			bak.setRGB(0,i+43,bw,1,brgbData,0,0);
		}
		
		/*
		JLabel baklabel = new JLabel(new ImageIcon(img));
		baklabel.setLocation(0, 0);
		baklabel.setSize(getSize());
		getLayeredPane().add(baklabel, javax.swing.JLayeredPane.DRAG_LAYER);
*/
		
		
		
		JLabel border_panel = new JLabel(new ImageIcon(bak));
		border_panel.setLocation(0, 0);
		border_panel.setSize(getSize());
		border_panel.setBorder(BorderFactory.createLineBorder(new Color(0f,0f,0f,0.3f), 2));
		border_panel.setOpaque(false);
		getLayeredPane().add(border_panel, javax.swing.JLayeredPane.POPUP_LAYER);

		
		// Copyright text back into
		
		JPanel textpanel = new JPanel();
		textpanel.setLayout(new BorderLayout());
		textpanel.setOpaque(false);
		
		label = new JLabel();
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		label.setText(AboutDialog.MAIN_TITLE);
		label.setBorder(BorderFactory.createEmptyBorder(25,5,5,5));
		{
			JPanel flowpanel = new JPanel();
			flowpanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
			flowpanel.setOpaque(false);
			flowpanel.add(label);
			textpanel.add(flowpanel, BorderLayout.NORTH);
		}
			
		copyrightpanel = new JPanel();		
		copyrightpanel.setOpaque(false);

		line = new JLabel(AboutDialog.COPYRIGHT_NOTICE);
		line.setHorizontalTextPosition(SwingConstants.CENTER);
		line.setFont(line.getFont().deriveFont(10f).deriveFont(Font.PLAIN));
		{
			JPanel flowpanel = new JPanel();
			flowpanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
			flowpanel.setOpaque(false);
			flowpanel.add(line);
			textpanel.add(flowpanel, BorderLayout.SOUTH);
		}
		
		textpanel.setSize(getSize().width, 140);
		textpanel.setLocation(0,getSize().height-170);
		
		getLayeredPane().add(textpanel, javax.swing.JLayeredPane.DRAG_LAYER);
		
		//bak.getAlphaRaster().
		
		
	}
	
	public BufferedImage captureBackGround( ) {
		

		
	    try {
	        Robot rbt = new Robot( );
	        Toolkit tk = Toolkit.getDefaultToolkit( );
	        Dimension dim = tk.getScreenSize( );
	        return rbt.createScreenCapture(
	        new Rectangle(0,0,(int)dim.getWidth( ),
	                          (int)dim.getHeight( )));
	    } catch (Exception ex) {	        
	        return null;
	    }
	}	
	
	JLabel light_label;
	JLabel light_cloud1;
	JLabel light_cloud2;
	Thread animation = new Thread()
	{
		boolean active = true;
		Runnable gui = new Runnable()
		{
			public void run()
			{
				Point loc1 = light_cloud1.getLocation();
				loc1.x -= 1;
				if(loc1.x < -cloud_width) loc1.x += 2*cloud_width;
				light_cloud1.setLocation(loc1);
				Point loc2 = light_cloud2.getLocation();
				loc2.x -= 1;
				if(loc2.x < -cloud_width) loc2.x += 2*cloud_width;
				light_cloud2.setLocation(loc2);
				
				
				Point loc = light_label.getLocation();
				loc.x += 3;
				if(loc.x > 350) loc.x = -400;
				light_label.setLocation(loc);
				if(!isVisible()) active = false;
			}
		};
		public void run()
		{
			while(active)
			{
				gui.run();
				try {
					Thread.sleep(70);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
		
}
