// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import uk.org.toot.control.BooleanControl;
import javax.swing.*;

public class BooleanControlPanel extends ControlPanel
{
    private final BooleanControl control;
    private AbstractButton button;
    private ActionListener buttonListener;
    private Color buttonColor;

    public BooleanControlPanel(final BooleanControl control) {
        super(control);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.control = control;
        String name = abbreviate(control.getAnnotation());
		buttonColor = control.getStateColor(control.getValue()); 
        if ( !control.isMomentary() ) {
        	Icon icon = getIcon(name);
        	if ( icon == null ) {
        		button = new BooleanButton(name) {
        			@Override
        			public Color getBackground() {
        				return buttonColor;
        			}
        		};
        	} else {
        		button = new BooleanButton(icon);
        	}
    	    buttonListener = new ActionListener() {
           		public void actionPerformed(ActionEvent ae) {
           	        control.setValue(!control.getValue()); // toggle
       	    	}
    		};
        } else {
	        button = new JButton(name) {
	        	@Override
    	        public Dimension getMaximumSize() {
    	            Dimension size = super.getPreferredSize();
    	            size.width = control.getWidthLimit()+3;
                	return size;
            	}
        	};
    	    buttonListener = new ActionListener() {
           		public void actionPerformed(ActionEvent ae) {
           	        control.momentaryAction();
       	    	}
   	    	};
        }
        button.setBorder(BorderFactory.createRaisedBevelBorder());
//		button.setMargin(new Insets(0, 0, 0, 0));
        button.setAlignmentX(0.5f);
        add(button);
    }

    public void update(Observable obs, Object arg) {
		buttonColor = control.getStateColor(control.getValue());
		repaint();
    }

    public void addNotify() {
        super.addNotify();
   	    button.addActionListener(buttonListener);
    }

    public void removeNotify() {
   	    button.removeActionListener(buttonListener);
        super.removeNotify();
    }
    
    protected Icon getIcon(String name) {
    	if ( name.equals("5010") ) return new PowerIcon();
    	return null;
    }
    
    public class BooleanButton extends JButton
    {
    	private boolean small;
    	
    	public BooleanButton(String name) {
    		super(name);
    		this.small = name.length() < 2;
    	}
    	
    	public BooleanButton(Icon icon) {
    		super(icon);
    		setBackground(Color.WHITE);
    	}
    	
    	@Override
        public Dimension getMaximumSize() {
            Dimension size = super.getPreferredSize();
            size.width = small ? 21 : control.getWidthLimit();
        	return size;
    	}
    	@Override
        public Dimension getMinimumSize() {
            Dimension size = super.getPreferredSize();
            size.width = small ? 18 : 36;
        	return size;
    	}
    	
    }
    
    /**
     * IEC 5010 symbol as an Icon
     */
    public class PowerIcon implements Icon
    {    
        private int width = 16;
        private int height = 16;
        
        private BasicStroke stroke = new BasicStroke(2);
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    			 RenderingHints.VALUE_ANTIALIAS_ON);            
            g2d.setColor(buttonColor);
            g2d.setStroke(stroke);
            g2d.drawOval(x + 1, y + 1, width - 2, height - 2);
            g2d.drawLine(x + width/2, y + 5, x + width/2, y + height - 5);
            g2d.dispose();
        }
        
        public int getIconWidth() {
            return width;
        }
        
        public int getIconHeight() {
            return height;
        }
    }
}
