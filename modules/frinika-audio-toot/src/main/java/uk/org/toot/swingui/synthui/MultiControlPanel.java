// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.synthui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.swingui.DisposablePanel;
import uk.org.toot.swingui.controlui.CompoundControlPanel;
import uk.org.toot.swingui.controlui.ControlPanelFactory;

public abstract class MultiControlPanel extends DisposablePanel
{
	protected final static String NONE = "<none>";

	private ButtonGroup buttonGroup = new ButtonGroup();
	private JToggleButton dummyButton = new JToggleButton();
	private JPanel westPanel;
	private JPanel centerPanel;
	private CardLayout cardLayout = new CardLayout();
	protected CompoundControl multiControls;
	private int max;

	public MultiControlPanel(CompoundControl controls, final int max, String title) { // cleanish
		setLayout(new BorderLayout());
		multiControls = controls;
		this.max = max;
		add(westPanel = createLeftComponent(), BorderLayout.WEST);
		add(centerPanel = createCenterComponent(), BorderLayout.CENTER);
		westPanel.setBorder(new TitledBorder(title));
		multiControls.addObserver(
			new Observer() {
				public void update(Observable obs, Object obj) {
					if ( obj instanceof Integer ) {
						int sel = ((Integer)obj).intValue();
						if ( sel < 0 || sel >= max ) return;
						viewControls(sel);
					}
				}
			}
		);
	}

	protected void dispose() {
		removeAll();
	}
	
	abstract protected Vector<String> getSelectionNames();
	
	abstract protected String getAnnotation(int chan);

	abstract protected CompoundControl getControls(int chan);
	
	abstract protected void setControls(int chan, CompoundControl controls);

	abstract protected CompoundControl createControls(String name);
	
	protected JPanel createUI(CompoundControl controls) {
		return new CompoundControlPanel(controls, 1, null,
				new ControlPanelFactory() {
			protected boolean canEdit() { return true; }
		}, true, true);
	}
	
	protected void viewControls(int chan, CompoundControl controls) {
		Component[] comps = westPanel.getComponents();
		Selector selector = (Selector)comps[chan];
		selector.viewControls(controls);
	}

	protected void viewControls(int sel) {
		viewControls(sel, getControls(sel));
	}
	
	protected JPanel createLeftComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		buttonGroup.add(dummyButton); // logical only, not displayed
		for ( int i = 0; i < max; i++ ) {
			panel.add(createSelector(i));
		}
		return panel;
	}

	protected JPanel createCenterComponent() {
		return new JPanel(cardLayout);
	}

	protected void checkSelection() { // clean
		Component[] comps = westPanel.getComponents();
		for ( int i = 0; i < comps.length; i++ ) {
			Selector selector = (Selector)comps[i];
			if ( selector.isAvailable() ) {
				selector.select();
				return;
			}
		}
		dummyButton.setSelected(true); // ensure selectors deselected
	}

	protected JPanel createSelector(int i) {
		return new Selector(i);
	}
	
	public class Selector extends JPanel
	{
		private int N = 48;
		private final JToggleButton button;
		private JComboBox combo;
		private boolean available = false;
		private JPanel ui;
		private int sel;
		private String cardId;
		private boolean disableCombo = false;

		public Selector(final int sel) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.sel = sel;
			cardId = String.valueOf(sel);

			button = new JToggleButton(getAnnotation(sel), false);
			button.setEnabled(false);
			Dimension maxSize = button.getMaximumSize();
			maxSize.width = N;
			button.setMaximumSize(maxSize);
			button.setMinimumSize(maxSize);
			button.setPreferredSize(maxSize);

			combo = new JComboBox(getSelectionNames());
			Dimension comboMaxSize = combo.getMaximumSize();
			comboMaxSize.height = maxSize.height;
			combo.setMaximumSize(comboMaxSize);
			combo.setPrototypeDisplayValue("A Fairly Long Name");

			add(combo);
			add(Box.createHorizontalGlue());
			add(button);

			buttonGroup.add(button);

			ActionListener comboActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ( disableCombo ) return;
					String s = (String)combo.getSelectedItem();
					setControls(s.equals(NONE) ? null : createControls(s));
				}
			};

			combo.addActionListener(comboActionListener);

			ActionListener buttonActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ( button.isSelected() ) showSelection();
				}
			};

			button.addActionListener(buttonActionListener);
			CompoundControl cc = getControls(sel);
			if ( cc != null ) {
				viewControls(cc);
			}
		}

		protected void setControls(CompoundControl controls) {
			MultiControlPanel.this.setControls(sel, controls); // !!!
		}

		public void viewControls(CompoundControl controls) {
			available = controls != null;
			button.setEnabled(available);
			button.setSelected(available);
			if ( ui != null ) {
				centerPanel.remove(ui);
				ui = null;
			}
			if ( available ) {
				disableCombo = true;
				// use annotation to avoid disambiguation/combo issue !!!
				combo.setSelectedItem(controls.getAnnotation()); // goes recursive!!!
				combo.setToolTipText(controls.getName());
				combo.requestFocusInWindow();
				disableCombo = false;
				ui = createUI(controls);
				if ( ui != null ) {
					centerPanel.add(ui, cardId);
					centerPanel.validate();
				}
				showSelection();					
			} else {
				checkSelection(); // select something else or dummy button		
			}
		}

		public boolean isAvailable() {
			return available;
		}

		public void select() {
			button.setSelected(true);
			showSelection();
		}

		protected void showSelection() {
			cardLayout.show(centerPanel, cardId);
		}
	} // end of SynthChannelSelector
}