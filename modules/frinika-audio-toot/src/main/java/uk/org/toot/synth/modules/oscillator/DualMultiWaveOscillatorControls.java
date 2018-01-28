// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class DualMultiWaveOscillatorControls extends CompoundControl implements DualMultiWaveOscillatorVariables 
{
    private final static ControlLaw TUNING_LAW = new LinearLaw(1f, 3f, "");
    private final static ControlLaw WIDTH_LAW = new LinearLaw(0.01f, 0.99f, "");
    
	public final static int SAW_LEVEL = 0;
	public final static int SQR_LEVEL = 1;
	public final static int WIDTH = 2;
	public final static int TUNING = 3;
	
	private FloatControl sawLevelControl;
	private FloatControl sqrLevelControl;
	private FloatControl tuningControl;
	private FloatControl widthControl;
	private int idOffset = 0;
	private float width;
	private float tuningFactor;
	private float sawLevel;
	private float sqrLevel;

	private boolean syncMaster;
	
	public DualMultiWaveOscillatorControls(int instanceIndex, String name, int idOffset, boolean master) {
		this(OscillatorIds.DUAL_MULTI_WAVE_OSCILLATOR_ID, instanceIndex, name, idOffset, master);
	}

	public DualMultiWaveOscillatorControls(int id, int instanceIndex, String name, final int idOffset, boolean master) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		syncMaster = master;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}
	
    @Override
    protected void derive(Control c) {
		switch (c.getId()-idOffset) {
		case SAW_LEVEL:	sawLevel = deriveSawLevel();			break;
		case SQR_LEVEL:	sqrLevel = deriveSquareLevel();			break; 
		case WIDTH:		width = deriveWidth();					break;
		case TUNING:	tuningFactor = deriveTuningFactor(); 	break;
		}    	
    }
    
	private void createControls() {
		if ( !syncMaster ) {
			add(tuningControl = createTuningControl());
			derive(tuningControl);
		}
		add(sawLevelControl = createLevelControl(SAW_LEVEL, "Saw"));
		add(sqrLevelControl = createLevelControl(SQR_LEVEL, "Square"));
		add(widthControl = createWidthControl());
		derive(sawLevelControl);
		derive(sqrLevelControl);
		derive(widthControl);
	}

	protected FloatControl createLevelControl(int id, String name) {
        FloatControl control = new FloatControl(id+idOffset, getString(name), LinearLaw.UNITY, 0.01f, 0.5f);
        control.setInsertColor(Color.BLACK);
        return control;
	}
	
	protected FloatControl createWidthControl() {
        FloatControl control = new FloatControl(WIDTH+idOffset, getString("Width"), WIDTH_LAW, 0.01f, 0.5f){
            private final String[] presetNames = { "50%" };

            public String[] getPresetNames() {
                return presetNames;
            }

            public void applyPreset(String presetName) {
                if ( presetName.equals(getString("50%")) ) {
                    setValue(0.5f);
                }
            }        	

        };
        control.setInsertColor(Color.WHITE);
        return control;				
	}

	protected FloatControl createTuningControl() {
        FloatControl control = new FloatControl(TUNING+idOffset, getString("Sync"), TUNING_LAW, 0.01f, 1f);
        control.setInsertColor(Color.YELLOW);
        return control;		
	}
	
	private void deriveSampleRateIndependentVariables() {
		sawLevel = deriveSawLevel();
		sqrLevel = deriveSquareLevel();
		width = deriveWidth();
		tuningFactor = deriveTuningFactor();
	}

	private void deriveSampleRateDependentVariables() {
	}

	protected float deriveSawLevel() {
		return sawLevelControl.getValue();
	}
	
	protected float deriveSquareLevel() {
		return sqrLevelControl.getValue();
	}
	
	protected float deriveWidth() {
		if ( widthControl == null ) return 0.5f; // !!!
		return widthControl.getValue();
	}
	
	protected float deriveTuningFactor() {
		if ( tuningControl == null ) return 1f;
		return tuningControl.getValue();
	}
	
	public boolean isMaster() {
		return syncMaster;
	}

	public float getSawLevel() {
		return sawLevel;
	}

	public float getSquareLevel() {
		return sqrLevel;
	}

	public float getWidth() {
		return width;
	}
	
	public float getTuningFactor() {
		return tuningFactor;
	}
}
