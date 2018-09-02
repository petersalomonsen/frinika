// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.filter.FilterControlIds.*;
import static uk.org.toot.synth.modules.filter.FilterType.*;

import java.util.List;

import uk.org.toot.control.Control;
import uk.org.toot.control.EnumControl;

public class DualStateVariableFilterControls extends FilterControls
	implements DualStateVariableFilterVariables
{
	private EnumControl typeControl;
	private DualStateVariableFilterConfig type;
	
	public DualStateVariableFilterControls(int instanceIndex, String name, final int idOffset) {
		super(FilterIds.DUAL_STATE_VARIABLE_FILTER_ID, instanceIndex, name, idOffset);
	}

	protected void derive(Control c) {
		switch ( c.getId() - idOffset ) {
		case TYPE: type = deriveType(); break;
		default: super.derive(c); break;
		}
	}

	protected void createControls() {
		super.createControls();
		add(typeControl = createTypeControl());
	}
	
	protected void deriveSampleRateIndependentVariables() {
		super.deriveSampleRateIndependentVariables();
		type = deriveType();
	}
	
	// damp = MIN(2.0*(1.0 - pow(res, 0.25)), MIN(2.0, 2.0/freq - freq*0.5));
	// stability correction must be done in real-time
	protected float deriveResonance() {
		return (float)(2 * (1f - Math.pow(super.deriveResonance(), 0.25)));
	}

	protected DualStateVariableFilterConfig deriveType() {
		return (DualStateVariableFilterConfig)typeControl.getValue();
	}
	
	protected EnumControl createTypeControl() {
        EnumControl control = new TypeControl(TYPE+idOffset, getString("Type"));
        return control;				
	}

	public DualStateVariableFilterConfig getType() {
		return type;
	}
	
	public class TypeControl extends EnumControl
	{
		private List<DualStateVariableFilterConfig> values;
		
		public TypeControl(int id, String name) {
			super(id, name, null);
			values = new java.util.ArrayList<DualStateVariableFilterConfig>();
			createValues();
			setValue(getValues().get(0));
		}

		private void createValues() {
			values.add(new DualStateVariableFilterConfig("L2", LOW, OFF, 1));
			values.add(new DualStateVariableFilterConfig("L4", LOW, LOW, 1));
			values.add(new DualStateVariableFilterConfig("H2", HIGH, OFF, 1));
			values.add(new DualStateVariableFilterConfig("H4", HIGH, HIGH, 1));
			values.add(new DualStateVariableFilterConfig("N2", NOTCH, OFF, 1));
			values.add(new DualStateVariableFilterConfig("N4", NOTCH, NOTCH, 1));
			values.add(new DualStateVariableFilterConfig("B2", BAND, OFF, 1));
			values.add(new DualStateVariableFilterConfig("B4", BAND, BAND, 1));
			values.add(new DualStateVariableFilterConfig("P2", PEAK, OFF, 1));
			values.add(new DualStateVariableFilterConfig("P4", PEAK, PEAK, 1));
			values.add(new DualStateVariableFilterConfig("H2N2", HIGH, NOTCH, 1));
			values.add(new DualStateVariableFilterConfig("N2L2", NOTCH, LOW, 1));
			values.add(new DualStateVariableFilterConfig("H2N2T", HIGH, NOTCH, 2));
			values.add(new DualStateVariableFilterConfig("N2L2T", NOTCH, LOW, 2));
			values.add(new DualStateVariableFilterConfig("N2N2T", NOTCH, NOTCH, 2));
			values.add(new DualStateVariableFilterConfig("N2P2T", NOTCH, PEAK, 2));
			values.add(new DualStateVariableFilterConfig("P2N2T", PEAK, NOTCH, 2));
		}
		
		@Override
		public List getValues() {
			return values;
		}
		
	    public int getWidthLimit() { return 55; }		
	}
}
