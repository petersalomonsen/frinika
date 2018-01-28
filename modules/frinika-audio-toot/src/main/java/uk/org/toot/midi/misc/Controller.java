// Copyright (C) 2005 - 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.misc;

import uk.org.toot.midi.message.ChannelMsg;

/**
 * Provides Midi Controller information parameterized by controller number without knowledge of the resolution of the controller.
 * 
 * Although controller numbers inherently refer to 7 bit values, with some
 * controllers used as pairs resulting in 14 bit balues, we hide this
 * implementation detail. If a specified controller number is part
 * of a 14 bit controller we return information appropriate to the whole 14 bits.
 * @author Steve Taylor
 */
public class Controller {

        public final static int BANK_SELECT = 0;
        public final static int MODULATION = 1;
        public final static int BREATH = 2;
        public final static int CONTROLLER_3 = 3; // undefined
        public final static int FOOT = 4;
        public final static int PORTAMENTO = 5;
        public final static int DATA_ENTRY = 6;
        public final static int VOLUME = 7;
        public final static int BALANCE = 8;
        public final static int CONTROLLER_9 = 9; // undefined
        public final static int PAN = 10;
        public final static int EXPRESSION = 11;
        public final static int FX_1 = 12;
        public final static int FX_2 = 13;
        public final static int HOLD_PEDAL = 64;
        public final static int PORTAMENTO_SWITCH = 65;
        public final static int SUSTENUTO_PEDAL = 66;
        public final static int SOFT_PEDAL = 67;
        public final static int LEGATO = 68;
        public final static int HOLD_2_PEDAL = 69;
        public final static int VARIATION = 0x46;
        public final static int TIMBRE = 0x47;
        public final static int RELEASE = 0x48;
        public final static int ATTACK = 0x49;
        public final static int BRIGHTNESS = 0x4A;
        public final static int EXTERNAL_FX = 0x5B;
        public final static int TREMELO = 0x5C;
        public final static int CHORUS = 0x5D;
        public final static int DETUNE = 0x5E;
        public final static int PHASER = 0x5F;
        public final static int DATA_INCREMENT = 96;
        public final static int DATA_DECREMENT = 97;
        public final static int NON_REGISTERED_FINE = 98;
        public final static int NON_REGISTERED_COARSE = 99;
        public final static int REGISTERED_FINE = 100;
        public final static int REGISTERED_COARSE = 101;
        public final static int ALL_SOUND_OFF = 120;
        public final static int ALL_CONTROLLERS_OFF = 121;
        public final static int LOCAL_CONTROL = 122;
        public final static int ALL_NOTES_OFF = 123;
        public final static int OMNI_MODE_OFF = 124;
        public final static int OMNI_MODE_ON = 125;
        public final static int MONO_MODE = 126;
        public final static int POLY_MODE = 127;

        public final static int SWITCHES = 301; // out-of-band composite pseudo controller
        public final static int PITCH_BEND_PSEUDO = -ChannelMsg.PITCH_BEND;
        public final static int POLY_PRESSURE_PSEUDO = -ChannelMsg.POLY_PRESSURE;
        public final static int CHANNEL_PRESSURE_PSEUDO = -ChannelMsg.CHANNEL_PRESSURE;
        public final static int PROGRAM_PSEUDO = -ChannelMsg.PROGRAM_CHANGE;

    static public final int[] PERFORMANCE_CONTROLLERS =
        { PITCH_BEND_PSEUDO, CHANNEL_PRESSURE_PSEUDO, MODULATION, BREATH, FOOT, PORTAMENTO, EXPRESSION, SWITCHES };
    static public final int[] SOUND_CONTROLLERS =
        { PROGRAM_PSEUDO, VARIATION, TIMBRE, BRIGHTNESS, ATTACK, RELEASE };
    static public final int[] MIXER_CONTROLLERS =
        { VOLUME, PAN, BALANCE, FX_1, FX_2 };
    static public final int[] EFFECTS_CONTROLLERS =
        { EXTERNAL_FX, TREMELO, CHORUS, DETUNE, PHASER };
    static public final int[] UNDEFINED_CONTROLLERS =
        { CONTROLLER_3, CONTROLLER_9, 14, 15 };
    static public final int[] NO_CONTROLLERS = {};
    static public final int[] SWITCH_CONTROLLERS =
        { HOLD_PEDAL, PORTAMENTO_SWITCH, SUSTENUTO_PEDAL,
          SOFT_PEDAL, LEGATO, HOLD_2_PEDAL };
    private static final String[] CATEGORIES =
        { "Performance", "Sound", "Mixer", "Effects", "Undefined" };

    public static String[] getCategories() {
        return CATEGORIES;
    }

    public static int[] getControllers(String category) {
        if ("Performance".equals(category)) {
            return PERFORMANCE_CONTROLLERS;
        } else if ("Sound".equals(category)) {
            return SOUND_CONTROLLERS;
        } else if ("Mixer".equals(category)) {
            return MIXER_CONTROLLERS;
        } else if ("Effects".equals(category)) {
            return EFFECTS_CONTROLLERS;
        } else if ("Undefined".equals(category)) {
            return UNDEFINED_CONTROLLERS;
        }
        return NO_CONTROLLERS;
    }

    /**
     * Determine the property name for the specified controller.
     * @param controller the controller index
     * @return the property name for the specified controller
     */
    public static String propertyName(int controller) {
        switch (controller) {
            case SWITCHES:
                return "Switches";
            case PITCH_BEND_PSEUDO:
                return "Pitch Bend";
            case POLY_PRESSURE_PSEUDO:
                return "Poly Pressure";
            case CHANNEL_PRESSURE_PSEUDO:
                return "Channel Pressure";
            case PROGRAM_PSEUDO:
                return "Program";
            case BANK_SELECT:
                return "Bank Select";
            case MODULATION:
                return "Modulation";
            case BREATH:
                return "Breath";
            case FOOT:
                return "Foot";
            case PORTAMENTO:
                return "Portamento";
            case DATA_ENTRY:
                return "Data Entry";
            case VOLUME:
                return "Volume";
            case BALANCE:
                return "Balance";
            case PAN:
                return "Pan";
            case EXPRESSION:
                return "Expression";
            case FX_1:
                return "Effect 1";
            case FX_2:
                return "Effect 2";
            case HOLD_PEDAL:
                return "Hold Pedal";
            case PORTAMENTO_SWITCH:
                return "Portamento Switch";
            case SUSTENUTO_PEDAL:
                return "Sustenuto Pedal";
            case SOFT_PEDAL:
                return "Soft Pedal";
            case LEGATO:
                return "Legato";
            case HOLD_2_PEDAL:
                return "Hold2 Pedal";
            case VARIATION:
                return "Variation";
            case TIMBRE:
                return "Timbre";
            case RELEASE:
                return "Release";
            case ATTACK:
                return "Attack";
            case BRIGHTNESS:
                return "Brightness";
            case EXTERNAL_FX:
                return "External Effects";
            case TREMELO:
                return "Tremelo";
            case CHORUS:
                return "Chorus";
            case DETUNE:
                return "Detune";
            case PHASER:
                return "Phaser";
            case DATA_INCREMENT:
                return "Data Increment";
            case DATA_DECREMENT:
                return "Data Decrement";
            case NON_REGISTERED_FINE:
                return "NRPN fine";
            case NON_REGISTERED_COARSE:
                return "NRPN coarse";
            case REGISTERED_FINE:
                return "RPN fine";
            case REGISTERED_COARSE:
                return "RPN coarse";
            case ALL_SOUND_OFF:
                return "All Sound Off";
            case ALL_CONTROLLERS_OFF:
                return "All Controllers Off";
            case LOCAL_CONTROL:
                return "Local Control";
            case ALL_NOTES_OFF:
                return "All Notes Off";
            case OMNI_MODE_OFF:
                return "Omni Off";
            case OMNI_MODE_ON:
                return "Omni On";
            case MONO_MODE:
                return "Mono";
            case POLY_MODE:
                return "Poly";
            default:
                // should deal with lsb somehow?
                return "Controller " + String.valueOf(controller);
        }
    }

    /**
     * Determine whether the specified controller is just 7 bit (not half of 14 bit)
     * @param controller the controller index
     * @return whether the controller is just 7 bit
     */
    public static boolean is7bit(int controller) {
        return controller >= 0x40 || (controller < 0 && controller != PITCH_BEND_PSEUDO);
    }

    /**
     * Get the minimum value for the specified controller.
     * @param controller the controller index
     * @return the minimum value
     */
    public static int getMinimum(int controller) { return 0; }

    /**
     * Get the maximum value for the specified controller.
     * @param controller the controller index
     * @return the maximum value
     */
    public static int getMaximum(int controller) {
        return -1 + (is7bit(controller) ? 128 : 128 * 128);
    }

    /**
     * Get the default value for the specified controller.
     * http://www.borg.com/~jglatt/tech/midispec/ctloff.htm
     * @param controller the controller index
     * @return the default value
     */
    public static int getDefault(int controller) {
        switch (controller) {
            case VOLUME:
                return 100 * 128;
            case EXPRESSION:
                return getMaximum(controller); // maximum
            case PAN:
            case BALANCE:
            case PITCH_BEND_PSEUDO:
                return (getMaximum(controller) + 1) / 2; // center
            default:
                return 0;
        }
    }

    /**
     * Get the offset value for the specified controller.
     * @param controller the controller index
     * @return the offset value
     */
    public static int getOffset(int controller) {
        return (controller == PAN ||
            	controller == BALANCE ||
                controller == PITCH_BEND_PSEUDO) ?
            		(getMaximum(controller) + 1) / 2 : 0;
    }

    public static int getOrientation(int controller) {
        if (controller == PAN || controller == BALANCE)
            return javax.swing.SwingConstants.HORIZONTAL;
        return javax.swing.SwingConstants.VERTICAL;
    }
}
