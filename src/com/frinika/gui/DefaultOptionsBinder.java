/*
 * Created on Jun 29, 2007
 *
 * Copyright (c) 2006-2007 Jens Gulden
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

package com.frinika.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;

import com.frinika.global.ConfigError;
import com.frinika.global.FrinikaConfig;

/**
 * Binds between data fields and GUI elements, in both directions.
 *
 * @author Jens Gulden
 */
public class DefaultOptionsBinder implements OptionsBinder {
	
	protected Map<Field, Object> bindMap;
	protected Map<String, Object> dynamicBindMap;
	protected Properties properties;
	protected Object bindInstance = null;
	protected Map<Field, Object> back;
	
	/*public DefaultOptionsBinder() {
		// nop
	}*/

	public DefaultOptionsBinder(Map<Field, Object> bindMap, Map<String, Object> dynamicBindMap, Properties properties) {
		this.bindMap = bindMap;
		this.dynamicBindMap = dynamicBindMap;
		this.properties = properties;
	}

	public DefaultOptionsBinder(Map<Field, Object> bindMap, Properties properties) {
		this(bindMap, null, properties);
	}

	public Object getBindInstance() {
		return bindInstance;
	}

	public void setBindInstance(Object bindInstance) {
		this.bindInstance = bindInstance;
	}
	
	/*public Map<Field, Object> getBindMap() {
		return bindMap;
	}

	public void setBindMap(Map<Field, Object> bindMap) {
		this.bindMap = bindMap;
	}*/

	
	/**
	 * Here the magic happens: set gui-elements according to data-fields.
	 * 
	 * @param component
	 * @param value
	 * @param fieldName
	 * @param fieldType
	 */
	protected void toGUI(Object component, Object value, String fieldName) {
		
		if ((component == null) || (value == null)) return;

		GUIAbstraction gui;
		
		if (component instanceof JTextField) {
			gui = new GUIAbstractionText(component);
			String s = FrinikaConfig.valueToString(value, fieldName, value.getClass());
			if (s == null) s = "";
			gui.setValue(s);
			
			
		} else if ((component instanceof JCheckBox) || (component instanceof JToggleButton)) {
			gui = new GUIAbstractionBoolean(component);
			boolean b = FrinikaConfig.isTrue(value);
			gui.setValue(b);

			
		} else if ( ( (component instanceof JSpinner) && (((JSpinner)component).getModel() instanceof SpinnerNumberModel) ) || (component instanceof JSlider)) {
			gui = new GUIAbstractionNumber(component);
			Class numberType = ((GUIAbstractionNumber)gui).getNumberType(); // int.class, long.class, float.class or double.class
			if (value instanceof Number) {
				Number num = (Number)value;
				if (numberType == int.class) {
					gui.setValue(num.intValue());
				} else if (numberType == long.class) {
					gui.setValue(num.longValue());
				} else if (numberType == float.class) {
					gui.setValue(num.floatValue());
				} else { //if (numberType == double.class) {
					gui.setValue(num.doubleValue());
				}
			} else { // value not a nuber originally
				String s = value.toString();
				if (numberType == int.class) {
					gui.setValue(Integer.parseInt(s));
				} else if (numberType == long.class) {
					gui.setValue(Long.parseLong(s));
				} else if (numberType == float.class) {
					gui.setValue(Float.parseFloat(s));
				} else { //if (numberType == double.class) {
					gui.setValue(Double.parseDouble(s));
				}
			}

		} else if ( (component instanceof JComboBox) || (component instanceof JList) || (component instanceof ButtonGroup) ) {
			gui = new GUIAbstractionSet(component);
			Object valueToBeSet = null;
			// compare all as strings
			String s = value.toString();
			for (Object o : ((GUIAbstractionSet)gui).getValues()) {
				if (s.equals(o.toString())) {
					valueToBeSet = o; // might of of different type than value, but equal as strings
				}
			}
			if ( (valueToBeSet == null) && (component instanceof JComboBox) && (((JComboBox)component).isEditable()) ) {
				valueToBeSet = value; // allow new value (not from original set) in editable comboboxes
			}
			gui.setValue(valueToBeSet);

		} else {
			throw new ConfigError("unsupported gui element for binding: "+component.getClass().getName());
		}
				
	}

	
	/**
	 * Here the magic happens: set data-field according to gui-elements.
	 * 
	 * @param component
	 * @param fieldName
	 * @param fieldType
	 * @return
	 */
	protected Object fromGUI(Object component, String fieldName, Class fieldType) {
		
		GUIAbstraction gui;
		
		if (component instanceof JTextField) {
			gui = new GUIAbstractionText(component);
			String s = (String)gui.getValue();
			return FrinikaConfig.stringToValue(s, fieldName, fieldType);
			
			
		} else if ((component instanceof JCheckBox) || (component instanceof JToggleButton)) {
			gui = new GUIAbstractionBoolean(component);
			boolean b = (Boolean)gui.getValue();
			if (boolean.class.isAssignableFrom(fieldType)) {
				return b;
			} else if (String.class.isAssignableFrom(fieldType)) {
				return b?"yes":"no";
			} else if (int.class.isAssignableFrom(fieldType)){
				return b?1:0;
			} else {
				throw new ConfigError("unsupported gui binding: JCheckBox - "+fieldType.getName());
			}

		} else if ( ( (component instanceof JSpinner) && (((JSpinner)component).getModel() instanceof SpinnerNumberModel) ) || (component instanceof JSlider)) {
			gui = new GUIAbstractionNumber(component);
			Number num = (Number)gui.getValue();
			if (int.class.isAssignableFrom(fieldType)){
				return num.intValue();
			} else if (long.class.isAssignableFrom(fieldType)){
				return num.longValue();
			} else if (float.class.isAssignableFrom(fieldType)){
				return num.floatValue();
			} else if (double.class.isAssignableFrom(fieldType)){
				return num.doubleValue();
			} else if (boolean.class.isAssignableFrom(fieldType)){
				return (num.intValue() != 0);
			} else if (String.class.isAssignableFrom(fieldType)){
				return num.toString();
			} else {
				throw new ConfigError("unsupported gui binding: JSpinner - (number value+)"+fieldType.getName());
			}

		} else if ( (component instanceof JComboBox) || (component instanceof JList) || (component instanceof ButtonGroup) ) {
			gui = new GUIAbstractionSet(component);
			Object value = gui.getValue();
			if (String.class.isAssignableFrom(fieldType)) {
				//return Config.valueToString(value, fieldName, fieldType)
				return (value != null) ? value.toString() : null;
			} else if (fieldType.isPrimitive()){ // all primitive field types
				return FrinikaConfig.stringToValue(value.toString(), fieldName, fieldType);
			} else {
				return value; // allow returning specific type
			}

		} else {
			throw new ConfigError("unsupported gui element for binding: "+component.getClass().getName());
		}
	}

	/**
	 * Refreshes the GUI so that it reflects the model's current state.
	 */
	
	public void refresh() {
		for (Map.Entry<Field, Object> e : bindMap.entrySet()) {
			Field field = e.getKey();
			Object component = e.getValue();
			try {
				Object value = field.get(bindInstance);
				toGUI(component, value, field.getName());
			} catch (IllegalAccessException iae) {
				System.err.println("error refreshing GUI from field "+field.getName());
			}
		}
		if (dynamicBindMap != null) {
			for (Map.Entry<String, Object> e : dynamicBindMap.entrySet()) {
				String key = e.getKey();
				Object component = e.getValue();
				Object value = properties.get(key);
				toGUI(component, value, key);
			}
		}
	}
	
	/**
	 * Updates the model so that it contains the values set by the user
	 */
	public void update() {
		for (Map.Entry<Field, Object> e : bindMap.entrySet()) {
			Field field = e.getKey();
			Object component = e.getValue();
			if (component != null) {
				Object value = fromGUI(component, field.getName(), field.getType());
				FrinikaConfig.setFieldValue(field, value); // will fire ChangeEvent if necessary
				/*try {
					field.set(bindInstance, value);
				} catch (IllegalAccessException iae) {
					System.err.println("error updating field "+field.getName()+" from GUI");
				}*/
			}
		}
		if (dynamicBindMap != null) {
			for (Map.Entry<String, Object> e : dynamicBindMap.entrySet()) {
				String key = e.getKey();
				Object component = e.getValue();
				Object value = fromGUI(component, key, String.class);
				String val = FrinikaConfig.valueToString(value, key,String.class);
				if (val != null) {
					properties.setProperty(key, val);
				}
			}
		}
	}
	
	
	public void backup() {
		back  = new HashMap<Field, Object>();;
		for (Field f : bindMap.keySet()) {
			try {
				back.put(f, f.get(bindInstance));
			} catch (IllegalAccessException iae) {
				System.err.println("error reading field " + f.getName());
			}
		}
	}

	public void restore() {
		for (Field f : bindMap.keySet()) {
			Object o = back.get(f);
			try {
				if (f.getDeclaringClass() == FrinikaConfig.class) { // make sure ChangeEvents are fired when Cancel leads to restoring old options
					FrinikaConfig.setFieldValue(f, o);
				} else {
					f.set(bindInstance, o);
				}
			} catch (IllegalAccessException iae) {
				System.err.println("error writing field "+f.getName());
			}
		}
	}

	// --- inner classes -----------------------------------------------------
	
	abstract class GUIAbstraction {
		
		abstract Object getValue();
		
		abstract void setValue(Object o);
		
	}
	
	
	class GUIAbstractionText extends GUIAbstraction {
		
		private JTextField textfield;
		
		GUIAbstractionText(Object component) {
			
			if (component instanceof JTextField) {
				
				textfield = (JTextField)component;
				
			}
			
		}
		
		Object getValue() {
			return textfield.getText();
		}
		
		void setValue(Object o) {
			textfield.setText(o.toString());
		}
		
	}
	
	
	class GUIAbstractionNumber extends GUIAbstraction {
		
		private JSpinner spinner = null;
		private JSlider slider = null;
		
		GUIAbstractionNumber(Object component) {
			if ( (component instanceof JSpinner) && (((JSpinner)component).getModel() instanceof SpinnerNumberModel) ) {
				spinner = (JSpinner)component;
			} else if (component instanceof JSlider) {
				slider = (JSlider)component;
			} else {
				throw new IllegalArgumentException("GUIAbstractionNumber must be constructed with JSpinner (with SpinnerNumberModel) or JSlider, is "+component.getClass().getName());
			}
		}
		
		Object getValue() {
			if (spinner != null) {
				return spinner.getValue();
			} else {
				return slider.getValue();
			}
		}
		
		void setValue(Object o) {
			if (spinner != null) {
				spinner.setValue(o);
			} else {
				if (o instanceof Number) {
					slider.setValue(((Number)o).intValue());
				}
			}
		}
		
		Class getNumberType() {
			return getValue().getClass();
		}
	}


	class GUIAbstractionSet extends GUIAbstraction {
		
		private JComboBox combobox = null;
		private JList list = null;
		private ButtonGroup buttongroup = null;
		
		GUIAbstractionSet(Object component) {
			
			if (component instanceof JComboBox) {
				combobox = (JComboBox)component;
			} else if (component instanceof JList) {
				list = (JList)component;
			} else if (component instanceof ButtonGroup) {
				buttongroup = (ButtonGroup)component;
			} else {
				throw new IllegalArgumentException("GUIAbstractionSet must be constructed with JComboBox, JList or ButtonGroup, is "+component.getClass().getName());
			}
			
		}
		
		Object getValue() {
			if (combobox != null) {
				return combobox.getSelectedItem();
			} else if (list != null) {
				return list.getSelectedValue();
			} else { // ButtonGroup
				return ((AbstractButton)buttongroup.getSelection()).getName(); // (name as value, untested)
			}
		}
		
		void setValue(Object value) {
			if (combobox != null) {
				combobox.setSelectedItem(value);
			} else if (list != null) {
				list.setSelectedValue(value, true);
			} else { // ButtonGroup
				if ((value != null) && (!(value instanceof String))) { // must be string to compare against abstractButton.getName()
					value = value.toString();
				}
				for (Enumeration<AbstractButton> e = buttongroup.getElements(); e.hasMoreElements(); ) {
					AbstractButton ab = e.nextElement();
					ab.setSelected((ab.getName().equals(value))); // buttons' values are their name-strings (untested)
				}
			}
		}
		
		Collection getValues() {
			if (combobox != null) {
				ArrayList a = new ArrayList();
				for (int i = 0; i < combobox.getItemCount(); i++) {
					a.add(combobox.getItemAt(i));
				}
				return a;
			} else if (list != null) {
				ListModel model = list.getModel();
				ArrayList a = new ArrayList();
				for (int i = 0; i < model.getSize(); i++) {
					a.add(model.getElementAt(i));
				}
				return a;
			} else { // ButtonGroup
				ArrayList a = new ArrayList();
				for (Enumeration<AbstractButton> e = buttongroup.getElements(); e.hasMoreElements(); ) {
					AbstractButton ab = e.nextElement();
					a.add(ab.getName());
				}
				return a;
			}
		}
	}
	
	
	class GUIAbstractionBoolean extends GUIAbstraction {
		
		private JCheckBox checkbox = null;
		private JToggleButton togglebutton = null;
		
		GUIAbstractionBoolean(Object component) {
			if (component instanceof JCheckBox) {
				checkbox = (JCheckBox)component;
			} else if (component instanceof JToggleButton) {
				togglebutton = (JToggleButton)component;
			} else {
				throw new IllegalArgumentException("GUIAbstractionBoolean must be constructed with JCheckBox or JToggleButton, is "+component.getClass().getName());
			}
		}
		
			
		Object getValue() {
			if (checkbox != null) {
				return checkbox.isSelected();
			} else {
				return togglebutton.isSelected();
			}
		}
		
		void setValue(Object o) {
			boolean b = FrinikaConfig.isTrue(o);
			if (checkbox != null) {
				checkbox.setSelected(b);
			} else {
				togglebutton.setSelected(b);
			}
		}
		
	}
	
}
