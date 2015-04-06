/*
 * Created on Feb 1, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.model;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.frinika.global.FrinikaConfig;
import com.frinika.gui.DefaultOptionsBinder;
import com.frinika.gui.OptionsBinder;
import com.frinika.gui.OptionsEditor;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.partview.PartView;
import com.frinika.sequencer.gui.partview.TextPartEditor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * In-place editable text part.
 * 
 * @author Jens Gulden
 */
public class TextPart extends Part { //implements ConfigListener {

	private static final long serialVersionUID = 1L;

	public final static int DEFAULT_WIDTH = 10240; // ticks
	
	public final static String EMPTY_STRING = "...";

	protected String text = EMPTY_STRING;

	protected JTextArea renderLabel;

	transient private static HashMap<PartView,TextPartEditor> activeEditors = new HashMap<PartView,TextPartEditor>();
	
	/**
	 * @param lane
	 */
	public TextPart(TextLane lane) {
		super(lane);
		init();
	}
	
	private TextPart() { // for cloning
		super();
		init();
	}
	
	private void init() {
		//FrinikaConfig.addConfigListener(this);
		renderLabel = new JTextArea();
		renderLabel.setLineWrap(true);
		renderLabel.setWrapStyleWord(true);
		//renderLabel.setFont( FrinikaConfig.TEXT_LANE_FONT );
		renderLabel.setOpaque(false);
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		if ((this.text == null) || (!this.text.equals(text))) {
			this.text = text;
			((TextLane)lane).fireChangeEvent();
		}
	}
	
	public synchronized void startInplaceEdit(PartView partView) {
		TextPartEditor editor = activeEditors.get(partView);
		if (editor != null) {
			editor.editOK();
		}
		Rectangle r = partView.getPartBounds(this);
		r.x += partView.getVirtualScreenRect().x;
		r.y += partView.getVirtualScreenRect().y;
		editor = new TextPartEditor(this, partView, r);
		partView.validate();
		//partView.repaintItems();
		activeEditors.put(partView, editor);
	}

	public synchronized void endInplaceEdit(PartView partView) {
		activeEditors.remove(partView);
	}


//	/* (non-Javadoc)
//	 * @see com.frinika.sequencer.model.Part#attach()
//	 */
//	@Override
//	public void attach() {
//		// TODO Auto-generated method stub
//
//	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		TextPart p = new TextPart();
		p.lane = lane;
		p.text = text;
		p.setStartTick(getStartTick());
		p.setEndTick(getEndTick());
		return p;
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#commitEventsAdd()
	 */
	@Override
	public void commitEventsAdd() {
		// nop
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#commitEventsRemove()
	 */
	@Override
	public void commitEventsRemove() {
		// nop
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#copyBy(long, com.frinika.sequencer.model.Lane)
	 */
	@Override
	public void copyBy(double tick, Lane dst) {
		// nop
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#detach()
	 */
//	@Override
//	public void detach() {
//		// nop
//	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#drawThumbNail(java.awt.Graphics2D, java.awt.Rectangle, com.frinika.sequencer.gui.partview.PartView)
	 */
	@Override
	public void drawThumbNail(Graphics2D g, Rectangle rect, PartView partView) {
		renderLabel.setSize(rect.width, rect.height);
		//renderLabel.setText("<html>"+getText()+"</html>");
		renderLabel.setText(getText());
		renderLabel.setFont( FrinikaConfig.TEXT_LANE_FONT );
		g.setColor(Color.WHITE);
		g.fillRect(rect.x+1, rect.y+1, rect.width-1, rect.height-1); // fill (again) with white background (avoid double filling would require more subtle ColorScheme or changes to PartView)
		g.translate(rect.x, rect.y);
		//partView.add(renderLabel);
		renderLabel.paint(g);
		//partView.remove(renderLabel);
		g.translate(-rect.x, -rect.y);
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#moveContentsBy(long, com.frinika.sequencer.model.Lane)
	 */
	@Override
	public void moveContentsBy(double tick, Lane dstLane) {
		setStartTick (getStartTick() + tick);
		setEndTick(getEndTick() + tick);
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#moveItemsBy(long)
	 */
	@Override
	protected void moveItemsBy(long deltaTick) {
		// nop
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Part#onLoad()
	 */
	@Override
	public void onLoad() {
		// nop
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Selectable#deepCopy(com.frinika.sequencer.model.Selectable)
	 */
	public Selectable deepCopy(Selectable parent) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.Selectable#deepMove(long)
	 */
	public void deepMove(long tick) {
		// nop
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.EditHistoryRecordable#restoreFromClone(com.frinika.sequencer.model.EditHistoryRecordable)
	 */
	public void restoreFromClone(EditHistoryRecordable object) {
		// TODO Auto-generated method stub

	}

	/*public void configurationChanged(ChangeEvent event) {
		if (event.getSource() == FrinikaConfig._TEXT_LANE_FONT) {
			renderLabel.setFont( FrinikaConfig.TEXT_LANE_FONT );
		}
	}*/

	
	// --- context menu ------------------------------------------------------
	
	/**
	 * Fills the part's context menu with menu-items.
	 *  
	 * @param popup
	 */
	@Override
	protected void initContextMenu(final ProjectFrame frame, JPopupMenu popup) {
		/*JMenuItem item = new JMenuItem(new RepeatAction(frame));
		//item.setText(item.getText()+"..."); // hack
		item.setMnemonic(KeyEvent.VK_R);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));		
		popup.add(item);
		item = new JMenuItem(new SplitSelectedPartsAction(frame));
		popup.add(item);*/
		super.initContextMenu(frame, popup);
	}

	
	// --- properties panel --------------------------------------------------
	
	/**
	 * Create PropertiesPanel.
	 * 
	 * @param frame
	 * @return
	 */
	@Override
	protected OptionsEditor createPropertiesPanel(ProjectFrame frame) {
		return new TextPartPropertiesPanel(frame);
	}
	
	// --- inner class ---
	
	/**
	 * Instance returned via createProperitesPanel().
	 * 
	 * This is an example how type-specific Properties-Panels can be built.
	 * Currently, this just inherits all defaults. 
	 */
	protected class TextPartPropertiesPanel extends PropertiesPanel {
		
		protected OptionsBinder binder;
		
		/**
		 * Constructor.
		 * 
		 * @param frame
		 */
		protected TextPartPropertiesPanel(ProjectFrame frame) {
			super(frame);
		}
		
		/**
		 * Fills the panel with gui elements for editing the part's properties.
		 */
		@Override
		protected void initComponents() {
			super.initComponents();
			GridBagConstraints gc = new GridBagConstraints();
			gc.insets = new Insets(5,5,5,5);
			gc.gridwidth = GridBagConstraints.REMAINDER;
			gc.fill = GridBagConstraints.HORIZONTAL;
			JSeparator sep = new JSeparator();
			this.add(sep, gc);
			//gc = new GridBagConstraints();
			JPanel fontpanel = new JPanel();
			fontpanel.setLayout(new BorderLayout(5,5));
			final JTextField fontTextField = new JTextField();
			JButton fontButton = new JButton("Pick Font...");
			Map<Field, Object> bindMap = new HashMap<Field, Object>();
			bindMap.put(FrinikaConfig._TEXT_LANE_FONT.getField(), fontTextField);
			binder = new DefaultOptionsBinder(bindMap, null);
			fontTextField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateFont(fontTextField.getText());
				}
			});
			fontButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FrinikaConfig.pickFont(frame, fontTextField);
					updateFont(fontTextField.getText());
				}
			});
			fontpanel.add(new JLabel("Font:"), BorderLayout.WEST);
			fontpanel.add(fontTextField, BorderLayout.CENTER);
			fontpanel.add(fontButton, BorderLayout.EAST);
			this.add(fontpanel, gc);
		}

		private void updateFont(String s) { // will lead to immediate update in gui (via TextPartView's ConfigListener)
			Font font = FrinikaConfig.stringToFont(s);
			if (font != null) {
				FrinikaConfig._TEXT_LANE_FONT.set(font);
				frame.repaintPartView();
			}
		}
		
		/**
		 * Refreshes the GUI so that it reflects the model's current state.
		 */
		@Override
		public void refresh() {
			super.refresh();
			binder.refresh();
		}
		
		/**
		 * Updates the model so that it contains the values set by the user.
		 */
		@Override
		public void update() {
			super.update();
			binder.update();
		}
	}
	
}
