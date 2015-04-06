/*
 * Created on March 9, 2007
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

package com.frinika.sequencer.gui;

import java.awt.GridBagConstraints;
import java.util.Collection;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * GUI component which is a JSlider with a number-field (JSpinner)
 * attached to it. This way, numerical values can be edited either
 * "visually" (using the slider) or "precise" entering the number textually.
 * Slider and umber-field are always in sync.
 *
 * @author Jens Gulden
 */
public class SliderNumberEditable extends JPanel {
    
    protected String prefix;

    protected String suffix;

    protected float value;

    protected float minimum = -100f;

    protected float maximum = 100f;

    protected float stepSize = 0;

    protected int orientation = SwingConstants.HORIZONTAL;
    
    protected ListenerSupport<ChangeListener, ChangeEvent> changeListeners = new ListenerSupport<ChangeListener, ChangeEvent>() {
        public void notify(ChangeListener l, ChangeEvent e) {
            l.stateChanged(e);
        }
    };
    
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JSlider slider;
    private javax.swing.JSpinner spinner;
    private javax.swing.JLabel suffixLabel;

    /** Creates new form BeanForm */
    public SliderNumberEditable() {
        initComponents();
        layoutComponents();
    }
    
    public SliderNumberEditable(float value, float minimum, float maximum, float stepSize, String prefix, String suffix, int orientation) { // float sliderFactor) {
        initComponents();
        setValue(value);
        setMinimum(minimum);
        setMaximum(maximum);
        setStepSize(stepSize);
        setPrefix(prefix);
        setSuffix(suffix);
        setOrientation(orientation);
        layoutComponents();
        validate();
    }

    @Override
    public void validate() {
        SpinnerNumberModel model = new SpinnerNumberModel((Float)value, (Float)minimum, (Float)maximum, (Float)stepSize);
        setModel(model);
        super.validate();
    }
    
    public void setModel(SpinnerNumberModel model) {
        slider.setModel(toBoundedRangeModel(model));
        spinner.setModel(model);
        if (((Float)model.getMaximum()).floatValue() == 100f) { // special: if maximum value is 100, force fix column size (independent from minimu, so 0 or -100 are possible as minimum for this)
        	JTextField tf = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
        	tf.setColumns(3); // will result in actually more than 3 characters, so negative values are fine
        }
    }

    public boolean isEnabled() {
        return slider.isEnabled();
    }
    
    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }
    
    public boolean getInverted() {
        return slider.getInverted();
    }
    
    public int getMajorTickSpacing() {
        return slider.getMajorTickSpacing();
    }
    
    public int getMinorTickSpacing() {
        return slider.getMinorTickSpacing();
    }
    
    public int getOrientation() {
        return slider.getOrientation();
    }
    
    public boolean getPaintLabels() {
        return slider.getPaintLabels();
    }
    
    public boolean getPaintTicks() {
        return slider.getPaintTicks();
    }
    
    public boolean getPaintTrack() {
        return slider.getPaintTrack();
    }
    
    public boolean getSnapToTicks() {
        return slider.getSnapToTicks();
    }
    
    public void setInverted(boolean b) {
        slider.setInverted(b);
    }
    
    public void setMajorTickSpacing(int n) {
        slider.setMajorTickSpacing(n);
    }
    
    public void setMinorTickSpacing(int n) {
        slider.setMinorTickSpacing(n);
    }
    
    public void setOrientation(int orientation) {
    	if (this.orientation != orientation) {
        	this.orientation = orientation;
            slider.setOrientation(orientation);
            layoutComponents();
            super.validate();
    	}
    }
    
    public void setPaintLabels(boolean b) {
        slider.setPaintLabels(b);
    }
    
    public void setPaintTicks(boolean b) {
        slider.setPaintTicks(b);
    }
    
    public void setPaintTrack(boolean b) {
        slider.setPaintTrack(b);
    }
    
    public void setSnapToTicks(boolean b) {
        slider.setSnapToTicks(b);
    }
    
    public static BoundedRangeModel toBoundedRangeModel(SpinnerNumberModel m) {
        return new DefaultBoundedRangeModel((int)((Float)(m.getValue())).floatValue(), 0, (int)((Float)(m.getMinimum())).floatValue(), (int)((Float)(m.getMaximum())).floatValue());
    }
    
    public void addChangeListener(ChangeListener l) {
        changeListeners.addListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeListeners.removeListener(l);
    }
    
    public Collection<ChangeListener> getChangeListeners() {
        return changeListeners.getListeners();
    }
    
    private void spinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        setValue( (Float)spinner.getModel().getValue() );
    }

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {
        setValue( slider.getModel().getValue() );
    }
    
    
    /**
     * Getter for property prefix.
     * @return Value of property prefix.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Setter for property prefix.
     * @param prefix New value of property prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        if (prefix != null) {
            prefixLabel.setText(prefix);
        } else {
        	remove(prefixLabel);
        }
    }

    /**
     * Getter for property suffix.
     * @return Value of property suffix.
     */
    public String getSuffix() {
        return this.suffix;
    }

    /**
     * Setter for property suffix.
     * @param suffix New value of property suffix.
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
        if (suffix != null) {
            suffixLabel.setText(suffix);
        } else {
        	remove(suffixLabel);
        }
    }

    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public float getValue() {
        return this.value;
    }

    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(float value) {
        if (value < minimum) value = minimum; else if (value > maximum) value = maximum;
        if (value != this.value) {
            this.value = value;
            slider.setValue(Math.round(value));
            spinner.setValue(value);
            changeListeners.notifyListeners(new ChangeEvent(this));
        }
    }

    /**
     * Getter for property minimum.
     * @return Value of property minimum.
     */
    public float getMinimum() {
        return this.minimum;
    }

    /**
     * Setter for property minimum.
     * @param minimum New value of property minimum.
     */
    public void setMinimum(float minimum) {
        this.minimum = minimum;
    }

    /**
     * Getter for property maximum.
     * @return Value of property maximum.
     */
    public float getMaximum() {
        return this.maximum;
    }

    /**
     * Setter for property maximum.
     * @param maximum New value of property maximum.
     */
    public void setMaximum(float maximum) {
        this.maximum = maximum;
    }


    /**
     * Getter for property stepSize.
     * @return Value of property stepSize.
     */
    public float getStepSize() {
        return this.stepSize;
    }

    /**
     * Setter for property stepSize.
     * @param stepSize New value of property stepSize.
     */
    public void setStepSize(float stepSize) {
        this.stepSize = stepSize;
    }

    private void initComponents() {
        slider = new javax.swing.JSlider();
        prefixLabel = new javax.swing.JLabel();
        spinner = new javax.swing.JSpinner();
        suffixLabel = new javax.swing.JLabel();

        this.setLayout(new java.awt.GridBagLayout());

        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });

        spinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerStateChanged(evt);
            }
        });
    }

    private void layoutComponents() {
    	this.removeAll();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        if (orientation == SwingConstants.HORIZONTAL) {
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
        } else { // vertical
            gridBagConstraints.fill = GridBagConstraints.VERTICAL;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridx = GridBagConstraints.REMAINDER;
        }
        add(slider, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        if (orientation == SwingConstants.HORIZONTAL) {
        	gridBagConstraints.anchor = GridBagConstraints.NORTH;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        }
        add(prefixLabel,gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        if (orientation == SwingConstants.HORIZONTAL) {
        	gridBagConstraints.anchor = GridBagConstraints.NORTH;
        }
        add(spinner, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        if (orientation == SwingConstants.HORIZONTAL) {
        	gridBagConstraints.anchor = GridBagConstraints.NORTH;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        }
        add(suffixLabel, gridBagConstraints);
    }
}
