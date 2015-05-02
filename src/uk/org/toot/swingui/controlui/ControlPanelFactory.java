// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Observable;
import javax.swing.*;
import javax.swing.border.*;
import uk.org.toot.control.*;
import uk.org.toot.service.*;

import static uk.org.toot.misc.Localisation.*;

public class ControlPanelFactory extends Observable implements PanelFactory
{
    private Set<String> minimisedControlNames = new java.util.HashSet<String>();
    private JPopupMenu popupMenu = null;

    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    static {
        UIManager.put("Slider.verticalThumbIcon", new SliderKnobIcon());
    }

    public JComponent layout(CompoundControl control, int axis, boolean hasBorder, JComponent parent, boolean hasHeader) {
//        if ( control.getType() == null ) return parent;
        parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
        if ( hasBorder && !control.isNeverBordered() ) {
      	    parent.setBorder(new TitledBorder("")); // !!!
        } 
        // prepare the header
        if ( hasHeader ) {
        	JComponent header = createHeader(control, axis);
			header.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            if ( header instanceof JButton  ) { // !!! !!! !!! works but yuk
	        	header.setComponentPopupMenu(getPopupMenu());
            }
        	parent.add(header);
        }
        // force the header to the top if requested
       	if ( addGlue() ) {
           	parent.add(Box.createVerticalGlue());
//            System.out.println(control.getControlPath()+" VGlue in CPF.layout()");
       	}
        if ( isMinimised(control.getName()) ) return null; // no content display
        // THIS IS WHERE TO ADD HIDDEN INDICATORS e.g. compressor meters
        createTop(parent, control, BoxLayout.Y_AXIS);
		// now prepare the content pane
        final JPanel target = new JPanel();
        target.setLayout(new BoxLayout(target, axis));
        float ay = control.getAlignmentY();
        if ( ay >= 0f ) {
           	parent.setAlignmentY(ay);           	
        }
        parent.add(target);
        return target;
	}

    protected void createTop(JComponent parent, CompoundControl control, int axis) {
    }

    public JComponent createComponent(Control control, int axis, boolean hasHeader) {
        if ( control instanceof CompoundControl ) {
            CompoundControl cc = (CompoundControl)control;
            int a = axis;
            if ( cc.isAlwaysVertical() ) {
                a = BoxLayout.Y_AXIS;
            } else if ( cc.isAlwaysHorizontal() ) {
                a = BoxLayout.X_AXIS;
            }
            // service provider lookup
            // used to cause sound glitches
            if ( cc.hasCustomUI() ) {
            	JComponent comp = ControlPanelServices.createControlPanel(cc, a, null, this, axis == BoxLayout.X_AXIS, hasHeader);
	        	if ( comp != null ) return comp;
            }
            // default compound UI
            JComponent jc = createCompoundComponent(cc, a, null, this, true, hasHeader);
            jc.setVisible(cc.getVisibility() <= cc.getParent().getCurrentVisibility());
            return jc;
        } else if ( control instanceof FloatControl ) {
        	if ( control.isIndicator() ) {
        		return new FloatIndicatorPanel((FloatControl)control);
        	}
            JPanel floatPanel;
            floatPanel = new FloatControlPanel((FloatControl)control, axis);
   	        floatPanel.setAlignmentY(0.25f); // ??? !!!
            return floatPanel;
        } else if ( control instanceof BooleanControl ) {
            if ( control.isIndicator() ) {
                return new BooleanIndicatorPanel((BooleanControl)control);
            } else {
              	return new BooleanControlPanel((BooleanControl)control);
            }
        } else if ( control instanceof EnumControl ) {
            if ( control.isIndicator() ) {
            	return new EnumIndicatorPanel((EnumControl)control);
            } else {
	            return new EnumControlPanel((EnumControl)control);
            }
        } else { // !!! !!!
        }
        return null;
    }

    protected JComponent createCompoundComponent(CompoundControl c, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader) {
        return new CompoundControlPanel(c, axis, s, f, hasBorder, hasHeader);
	}

    protected boolean addGlue() { return false; }

    protected boolean canEdit() { return false; }

    protected boolean isMinimised(String name) {
        return minimisedControlNames.contains(name);
    }

    protected boolean toggleMinimised(String name) {
        if ( isMinimised(name) ) {
            minimisedControlNames.remove(name);
        } else {
            minimisedControlNames.add(name);
        }
        setChanged();
        notifyObservers(name);
        return isMinimised(name);
    }

    protected String shorten(String name) {
        // if there are two words return the last (e.g. Parametric EQ)
        // if there are 3 words return the first and last (e.g Dual Band Compressor
		String[] subs = name.split("\\s");
        final int n = subs.length;
        if ( n > 1 ) {
            if ( n == 2 ) return shorten(subs[0])+" "+shorten(subs[1]);
            if ( n == 3 ) return shorten(subs[0])+" "+shorten(subs[2]);
        }
       	if ( name.length() > 6 ) {
        	int trunc = 4;
            final int last = name.charAt(trunc-1);
            if ( "aeiou".indexOf(last) >= 0 ) trunc--; // don't end with vowel
           	return name.substring(0, trunc);
   		}
       return name;
    }

    //  override
    // !!! !!! !!! only needed for equivalence to canDelete
/*    protected boolean canBypass(CompoundControl c) {
        return false;
    } */

    protected JComponent createHeader(final CompoundControl control, int axis) {
        boolean isShort = false;
        String title = control.getName();
        if ( isMinimised(title) ) {
            title = shorten(title);
            isShort = true;
        }
        JComponent comp;
        if ( canEdit() && (control.getParent() == null || control.getParent().isPluginParent()) ) {
        	comp = new JButton(title);
	        comp.setBorder(BorderFactory.createRaisedBevelBorder());
        } else {
        	comp = new JLabel(title);
        }
        
        if ( isShort ) {
            comp.setToolTipText(control.getName());
        }
        return comp;
    }

    protected JPopupMenu createPopupMenu() {
        return new CompoundPopupMenu();
    }

    protected JPopupMenu getPopupMenu() {
        if ( popupMenu == null ) {
            popupMenu = createPopupMenu();
//        	popupMenu.setLightWeightPopupEnabled(false); // !! important
        }
        return popupMenu;
    }

    protected class CompoundPopupMenu extends JPopupMenu
    {
        public CompoundPopupMenu() {
        }

        // removeAll on hide!!!
        public void show(Component invoker, int x, int y) {
    		CompoundControlPanel compoundPanel =
                (CompoundControlPanel)invoker.getParent();
            CompoundControl control = compoundPanel.getControl();
            CompoundControl parentControl = control.getParent();
            String name = control.getName();
            removeAll();
            // if top level
            if ( parentControl.isPluginParent() ) {
            	if ( parentControl instanceof CompoundControlChain ) {
            		CompoundControlChain chain = (CompoundControlChain)parentControl;
            		if ( control.canBeInsertedBefore() ) {
            			add(new InsertMenu(control, chain));
            		}
            		if ( control.canBeMoved() ) {
            			add(new MoveMenu(control, chain));
            		}
            		if ( control.canBeDeleted() ) {
            			add(new DeleteAction(control.getName(), chain));
            		}
            	}
        		if ( CompoundControl.getPersistence() != null && control.hasPresets() ) {
        			addSeparator();
        			add(new LoadMenu(control));
        			add(new SaveAction(control));
        		}
        		if ( control.canLearn() ) {
        			addSeparator();
        			JCheckBoxMenuItem item = new JCheckBoxMenuItem(new LearnAction(control));
        			item.setSelected(control.getLearn());
        			add(item);
        		}
                int maxVis = control.getMaxVisibility(); 
                if ( maxVis > 0 ) {
                    addSeparator();
                    int curVis = control.getCurrentVisibility();
                    if ( curVis > maxVis ) curVis = maxVis;
                    if ( curVis > 0 ) {
                        add(new LessAction(curVis, control, compoundPanel));
                    }
                    if ( curVis < maxVis ) {
                        add(new MoreAction(curVis, control, compoundPanel));
                    }
                }
            }
            if ( control.canBeMinimized() && !control.isAlwaysVertical() ) {
            	String minOrMax = isMinimised(name) ?
                    getString("Maximise") : getString("Minimise");
                addSeparator();
            	add(new MinMaxAction(minOrMax, name));
            }
            super.show(invoker, x, y);
        }
    }

    /**
     * Create a MoveAction before all existing controls except this one
     */
    static protected class MoveMenu extends JMenu
    {
        private CompoundControlChain parentChain;

        public MoveMenu(CompoundControl control, CompoundControlChain parentControl) {
            super(getString("Move.before"));
            parentChain = parentControl;
            Control prev_c = null;
            int added = 0;
            for ( Control c : parentChain.getControls() ) {
                // inhibit pointless moves with no effect
                if ( c != control && prev_c != control &&
                     c.getName().length() > 0 &&
                     ((CompoundControl)c).canBeMovedBefore() ) {
                	add(new MoveAction(control.getName(), c.getName()));
                    added += 1;
                }
                prev_c = c;
            }
            if ( added == 0 ) setEnabled(false);
        }

        /**
         * Move the specified control before an existing control
         */
        protected class MoveAction extends AbstractAction
        {
            private String moveName;
            private String moveBeforeName;

            public MoveAction(String moveName, String moveBeforeName) {
                super(moveBeforeName);
                this.moveName = moveName;
                this.moveBeforeName = moveBeforeName;
            }

        	public void actionPerformed(ActionEvent e) {
            	parentChain.move(moveName, moveBeforeName);
            }
        }
    }


    /**
     * Create a MoveAction before all existing controls except this one
     */
    static protected class InsertMenu extends JMenu
    {
        private CompoundControlChain parentChain;

        public InsertMenu(CompoundControl control, CompoundControlChain parentControl) {
            super(getString("Insert"));
            parentChain = parentControl;
            List<ServiceDescriptor> inserts = parentChain.descriptors();
            Map<String, JMenu> menus = new java.util.HashMap<String, JMenu>();
            for ( ServiceDescriptor d : inserts ) {
                // if insert isn't compatible, continue
                // add to Insert category sub menu
                String category = d.getDescription();
                JMenu menu = menus.get(category);
                if ( menu == null ) {
                    menu = new JMenu(category);
                    add(menu);
                    menus.put(category, menu);
                }
                menu.add(new InsertAction(d.getName(), control.getName()));
            }
        }

        protected class InsertAction extends AbstractAction
        {
            private String insertName;
            private String insertBeforeName;

            public InsertAction(String insertName, String insertBeforeName) {
                super(insertName);
                this.insertName = insertName;
                this.insertBeforeName = insertBeforeName;
            }

        	public void actionPerformed(ActionEvent e) {
            	parentChain.insert(insertName, insertBeforeName);
            }
        }
    }


    static protected class DeleteAction extends AbstractAction
    {
        private String deleteName;
        private CompoundControlChain parentChain;

        public DeleteAction(String deleteName, CompoundControlChain chain) {
            super(getString("Delete"));
            this.deleteName = deleteName;
            parentChain = chain;
        }

    	public void actionPerformed(ActionEvent e) {
        	parentChain.delete(deleteName);
        }
    }

	static protected class LoadMenu extends JMenu
    {
        private CompoundControl control;
        public LoadMenu(CompoundControl control) {
            super(getString("Load.Preset"));
            this.control = control;
            CompoundControlPersistence persistence =
                CompoundControl.getPersistence();
            List<String> presets = persistence.getPresets(control);
            for ( String preset : presets ) {
                add(new LoadAction(preset));
            }
            if ( presets.size() == 0 ) setEnabled(false);
        }

        protected class LoadAction extends AbstractAction
        {
            public LoadAction(String preset) {
                super(preset);
            }

        	public void actionPerformed(ActionEvent e) {
            	CompoundControl.getPersistence().loadPreset(control, e.getActionCommand());
            }
        }
    }

	static protected class SaveAction extends AbstractAction
    {
        private CompoundControl control;

        public SaveAction(CompoundControl control) {
            super(getString("Save.Preset.As")+"...");
            this.control = control;
        }

    	public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(
                						getString("Save.Preset.As")+"...");
            if ( name != null ) {
            	CompoundControl.getPersistence().savePreset(control, name);
            }
        }
    }

    protected class MinMaxAction extends AbstractAction
    {
        private String name;

        public MinMaxAction(String action, String name) {
            super(action);
            this.name = name;
        }

    	public void actionPerformed(ActionEvent e) {
        	toggleMinimised(name);
        }
    }
    
	static protected class LearnAction extends AbstractAction
    {
        private CompoundControl control;

        public LearnAction(CompoundControl control) {
            super(getString("Learn"));
            this.control = control;
        }

    	public void actionPerformed(ActionEvent e) {
            control.setLearn(!control.getLearn());
        }
    }

    static protected class VisibilityAction extends AbstractAction
    {
        private CompoundControl control;
        private CompoundControlPanel panel;
        private int newVisibility;
        
        public VisibilityAction(String name, int vis, 
                CompoundControl control, CompoundControlPanel panel) {
            super(name);
            this.control = control;
            this.panel = panel;
            newVisibility = vis;
        }
        
        public void actionPerformed(ActionEvent arg0) {
            control.setCurrentVisibility(newVisibility);
            checkPanel(panel);
        }
        
        private void checkPanel(CompoundControlPanel p) {
            Container container = p.getContainer();
            for ( Component c : container.getComponents() ) {
                if ( c instanceof CompoundControlPanel ) {
                    CompoundControlPanel ccp = (CompoundControlPanel)c;
                    if ( ccp.getControl().getVisibility() <= newVisibility ) {
                        if ( !ccp.isVisible() ) ccp.setVisible(true);
                    } else {
                        if ( ccp.isVisible() ) ccp.setVisible(false);
                    }
                    checkPanel(ccp);
                }
            }
        }
    }

    static protected class LessAction extends VisibilityAction
    {
        public LessAction(int cur, CompoundControl control, CompoundControlPanel panel) {
            super(getString("Less"), cur-1, control, panel);
        }
    }

    static protected class MoreAction extends VisibilityAction
    {
        public MoreAction(int cur, CompoundControl control, CompoundControlPanel panel) {
            super(getString("More"), cur+1, control, panel);            
        }
    }
}
