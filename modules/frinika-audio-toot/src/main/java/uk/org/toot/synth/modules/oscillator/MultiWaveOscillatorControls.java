// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class MultiWaveOscillatorControls extends CompoundControl implements MultiWaveOscillatorVariables 
{
	private final static ControlLaw TUNING_LAW = new LinearLaw(-1f, 1f, "");
    private final static ControlLaw WIDTH_LAW = new LinearLaw(0.01f, 0.99f, "");

	public final static int WAVE = 0; // TODO move to OscillatorControlIds.java
	public final static int WIDTH = 1;
	public final static int DETUNE = 2;
	public final static int OCTAVE = 5;
	
	private FloatControl detuneControl;
	private EnumControl waveControl;
	private FloatControl widthControl;
	private EnumControl octaveControl;
	private int idOffset = 0;
	private MultiWave multiWave;
	private float width;
	private float detuneFactor;
	private int octave;

	private boolean master;
	
	public MultiWaveOscillatorControls(int instanceIndex, String name, int idOffset, boolean master) {
		this(OscillatorIds.MULTI_WAVE_OSCILLATOR_ID, instanceIndex, name, idOffset, master);
	}

	public MultiWaveOscillatorControls(int id, int instanceIndex, String name, final int idOffset, boolean master) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		this.master = master;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}
	
    @Override
    protected void derive(Control c) {
		switch ( c.getId()-idOffset ) {
		case WAVE:		multiWave = deriveMultiWave(); 			break;
		case DETUNE:	detuneFactor = deriveDetuneFactor(); 	break;
		case WIDTH:		width = deriveWidth();					break;
		case OCTAVE:	octave = deriveOctave();				break;
		}    	
    }
    
	private void createControls() {
		if ( getInstanceIndex() > 0 ) {
			add(detuneControl = createDetuneControl());
			derive(detuneControl);
		}
		ControlColumn cc = new ControlColumn() {
			public float getAlignmentY() { return 0.20f; }
		};
		cc.add(waveControl = createWaveControl());
		cc.add(octaveControl = createOctaveControl());
		add(cc);
		add(widthControl = createWidthControl());
		derive(waveControl);
		derive(octaveControl);
		derive(widthControl);
	}

	protected FloatControl createDetuneControl() {
        FloatControl control = new FloatControl(DETUNE+idOffset, getString("Detune"), TUNING_LAW, 0.0001f, 1f) {
			private final String[] presetNames = { getString("Off") };

			public String[] getPresetNames() {
				return presetNames;
			}

			public void applyPreset(String presetName) {
				if ( presetName.equals(getString("Off")) ) {
					setValue(0f);
				}
			}        	
		};
        control.setInsertColor(Color.MAGENTA);
        return control;						
	}
	
	protected EnumControl createWaveControl() {
		return new EnumControl(WAVE+idOffset, "Wave", "Square") {
			public List getValues() {
				return MultiWaves.getNames();
			}
		};
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

	private static String[] octaveArray = { "2", "1", "0", "-1", "-2" };
	private static List<String> octaveList = Arrays.asList(octaveArray);
	
	protected EnumControl createOctaveControl() {
		return new EnumControl(OCTAVE+idOffset, "Octave", "0") {
			public List getValues() {
				return octaveList;
			}
		};
	}
		
	private void deriveSampleRateIndependentVariables() {
		detuneFactor = deriveDetuneFactor();
		multiWave = deriveMultiWave();
		width = deriveWidth();
		octave = deriveOctave();
	}

	private void deriveSampleRateDependentVariables() {
	}

	protected MultiWave deriveMultiWave() {
		String name = (String)waveControl.getValue();
		return MultiWaves.get(name); // TODO takes a long time on Swing thread
	}
	
	protected float deriveWidth() {
		if ( widthControl == null ) return 0.5f; // !!!
		return widthControl.getValue();
	}
	
	protected float deriveDetuneFactor() {
		if ( detuneControl == null ) return 1f;
		return 1f - detuneControl.getValue() / 100;
	}
	
	protected int deriveOctave() {
		return Integer.parseInt((String)octaveControl.getValue());
	}
	
	public MultiWave getMultiWave() {
		return multiWave;
	}

	public float getWidth() {
		return width;
	}
	
	public float getSyncThreshold() {
		return 0f; // !!!
	}

	public float getDetuneFactor() {
		return detuneFactor;
	}
	
	public int getOctave() {
		return octave;
	}
	
	public boolean isMaster() {
		return master;
	}
}
