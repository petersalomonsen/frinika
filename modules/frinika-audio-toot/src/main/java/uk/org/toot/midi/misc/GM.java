// Copyright (C) 2005 - 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.misc;

import uk.org.toot.music.tonality.Pitch;

/** Support for General MIDI Instrument Family and Family names. */
public class GM
{
	// GM Level 2
    public final static int HIGH_Q = 27;
    public final static int SLAP = 28;
    public final static int SCRATCH = 29;
    public final static int SCRATCH_2 = 30;
    public final static int STICKS = 31;
    public final static int SQUARE = 32;
    public final static int METRONOME = 33;
    public final static int METRONOME_2 = 34;

    // GM Level 1
    public final static int ACOUSTIC_BASS_DRUM = 35;
    public final static int BASS_DRUM_1 = 36;
    public final static int SIDE_STICK = 37;
    public final static int ACOUSTIC_SNARE = 38;
    public final static int HAND_CLAP = 39;
    public final static int ELECTRIC_SNARE = 40;
    public final static int LOW_FLOOR_TOM = 41;
    public final static int CLOSED_HI_HAT = 42;
    public final static int HI_FLOOR_TOM = 43;
    public final static int PEDAL_HI_HAT = 44;
    public final static int LOW_TOM = 45;
    public final static int OPEN_HI_HAT = 46;
    public final static int LOW_MID_TOM = 47;
    public final static int HI_MID_TOM = 48;
    public final static int CRASH_CYMBAL_1 = 49;
    public final static int HI_TOM = 50;
    public final static int RIDE_CYMBAL_1 = 51;
    public final static int CHINESE_CYMBAL = 52;
    public final static int RIDE_BELL = 53;
    public final static int TAMBOURINE = 54;
    public final static int SPLASH_CYMBAL = 55;
    public final static int COWBELL = 56;
    public final static int CRASH_CYMBAL_2 = 57;
    public final static int VIBRASLAP = 58;
    public final static int RIDE_CYMBAL_2 = 59;
    public final static int HI_BONGO = 60;
    public final static int LOW_BONGO = 61;
    public final static int MUTE_HI_CONGA = 62;
    public final static int OPEN_HI_CONGA = 63;
    public final static int LOW_CONGA = 64;
    public final static int HI_TIMBALE = 65;
    public final static int LOW_TIMBALE = 66;
    public final static int HI_AGOGO = 67;
    public final static int LOW_AGOGO = 68;
    public final static int CABASA = 69;
    public final static int MARACAS = 70;
    public final static int SHORT_WHISTLE = 71;
    public final static int LONG_WHISTLE = 72;
    public final static int SHORT_GUIRO = 73;
    public final static int LONG_GUIRO = 74;
    public final static int CLAVES = 75;
    public final static int HI_WOOD_BLOCK = 76;
    public final static int LOW_WOOD_BLOCK = 77;
    public final static int MUTE_CUICA = 78;
    public final static int OPEN_CUICA = 79;
    public final static int MUTE_TRIANGLE = 80;
    public final static int OPEN_TRIANGLE = 81;

    // GM Level 2
    public final static int SHAKER = 82;
    public final static int JINGLE_BELL = 83;
    public final static int BELL_TREE = 84;
    public final static int CASTANETS = 85;
    public final static int MUTE_SURDO = 86;
    public final static int OPEN_SURDO = 87;

    static public String melodicFamilyName(int family) {
        switch (family) {
            case 0:
                return "Piano";
            case 1:
                return "Chromatic Percussion";
            case 2:
                return "Organ";
            case 3:
                return "Guitar";
            case 4:
                return "Bass";
            case 5:
                return "Solo Strings";
            case 6:
                return "Ensemble";
            case 7:
                return "Brass";
            case 8:
                return "Reed";
            case 9:
                return "Pipe";
            case 10:
                return "Synth Lead";
            case 11:
                return "Synth Pad";
            case 12:
                return "Synth Effects";
            case 13:
                return "Ethnic";
            case 14:
                return "Percussive";
            case 15:
                return "Sound Effects";
        }
        return family + "?";
    }

    static public String melodicProgramName(int program) {
        switch (program) {
            case 0:
                return "Acoustic Grand Piano";
            case 1:
                return "Bright Acoustic Piano";
            case 2:
                return "Electric Grand Piano";
            case 3:
                return "Honky-tonk Piano";
            case 4:
                return "Rhodes Piano";
            case 5:
                return "Chorused Piano";
            case 6:
                return "Harpsichord";
            case 7:
                return "Clavinet";
            case 8:
                return "Celesta";
            case 9:
                return "Glockenspiel";
            case 10:
                return "Music Box";
            case 11:
                return "Vibraphone";
            case 12:
                return "Marimba";
            case 13:
                return "Xylophone";
            case 14:
                return "Tubular Bells";
            case 15:
                return "Dulcimer";
            case 16:
                return "Hammond Organ";
            case 17:
                return "Percussive Organ";
            case 18:
                return "Rock Organ";
            case 19:
                return "Church Organ";
            case 20:
                return "Reed Organ";
            case 21:
                return "Accordion";
            case 22:
                return "Harmonica";
            case 23:
                return "Tango Accordion";
            case 24:
                return "Acoustic Guitar (nylon)";
            case 25:
                return "Acoustic Guitar (steel)";
            case 26:
                return "Electric Guitar (jazz)";
            case 27:
                return "Electric Guitar (clean)";
            case 28:
                return "Electric Guitar (muted)";
            case 29:
                return "Overdriven Guitar";
            case 30:
                return "Distortion Guitar";
            case 31:
                return "Guitar Harmonics";
            case 32:
                return "Acoustic Bass";
            case 33:
                return "Electric Bass (finger)";
            case 34:
                return "Electric Bass (pick)";
            case 35:
                return "Fretless Bass";
            case 36:
                return "Slap Bass 1";
            case 37:
                return "Slap Bass 2";
            case 38:
                return "Synth Bass 1";
            case 39:
                return "Synth Bass 2";
            case 40:
                return "Violin";
            case 41:
                return "Viola";
            case 42:
                return "Cello";
            case 43:
                return "Contrabass";
            case 44:
                return "Tremelo Strings";
            case 45:
                return "Pizzicato Strings";
            case 46:
                return "Orchestral Harp";
            case 47:
                return "Timpani";
            case 48:
                return "String Ensemble 1";
            case 49:
                return "String Ensemble 2";
            case 50:
                return "SynthStrings 1";
            case 51:
                return "SynthStrings 2";
            case 52:
                return "Choir Aahs";
            case 53:
                return "Voice Oohs";
            case 54:
                return "Synth Voice";
            case 55:
                return "Orchestra Hit";
            case 56:
                return "Trumpet";
            case 57:
                return "Trombone";
            case 58:
                return "Tuba";
            case 59:
                return "Muted Trumpet";
            case 60:
                return "French Horn";
            case 61:
                return "Brass Section";
            case 62:
                return "Synth Brass 1";
            case 63:
                return "Synth Brass 2";
            case 64:
                return "Soprano Sax";
            case 65:
                return "Alto Sax";
            case 66:
                return "Tenor Sax";
            case 67:
                return "Baritone Sax";
            case 68:
                return "Oboe";
            case 69:
                return "English Horn";
            case 70:
                return "Bassoon";
            case 71:
                return "Clarinet";
            case 72:
                return "Piccolo";
            case 73:
                return "Flute";
            case 74:
                return "Recorder";
            case 75:
                return "Pan Flute";
            case 76:
                return "Bottle Blow";
            case 77:
                return "Shakuhachi";
            case 78:
                return "Whistle";
            case 79:
                return "Ocarina";
            case 80:
                return "Lead 1 (square)";
            case 81:
                return "Lead 2 (sawtooth)";
            case 82:
                return "Lead 3 (calliope lead)";
            case 83:
                return "Lead 4 (chiff lead)";
            case 84:
                return "Lead 5 (charang)";
            case 85:
                return "Lead 6 (voice)";
            case 86:
                return "Lead 7 (fifths)";
            case 87:
                return "Lead 8 (bass + lead)";
            case 88:
                return "Pad 1 (new age)";
            case 89:
                return "Pad 2 (warm)";
            case 90:
                return "Pad 3 (polysynth)";
            case 91:
                return "Pad 4 (choir)";
            case 92:
                return "Pad 5 (bowed)";
            case 93:
                return "Pad 6 (metallic)";
            case 94:
                return "Pad 7 (halo)";
            case 95:
                return "Pad 8 (sweep)";
            case 96:
                return "FX 1 (rain)";
            case 97:
                return "FX 2 (soundtrack)";
            case 98:
                return "FX 3 (crystal)";
            case 99:
                return "FX 4 (atmosphere)";
            case 100:
                return "FX 5 (brightness)";
            case 101:
                return "FX 6 (goblins)";
            case 102:
                return "FX 7 (echoes)";
            case 103:
                return "FX 8 (sci-fi)";
            case 104:
                return "Sitar";
            case 105:
                return "Banjo";
            case 106:
                return "Shamisen";
            case 107:
                return "Koto";
            case 108:
                return "Kalimba";
            case 109:
                return "Bagpipe";
            case 110:
                return "Fiddle";
            case 111:
                return "Shanai";
            case 112:
                return "Tinkle Bell";
            case 113:
                return "Agogo";
            case 114:
                return "Steel Drums";
            case 115:
                return "Woodblock";
            case 116:
                return "Taiko Drum";
            case 117:
                return "Melodic Tom";
            case 118:
                return "Synth Drum";
            case 119:
                return "Reverse Cymbal";
            case 120:
                return "Guitar Fret Noise";
            case 121:
                return "Breath Noise";
            case 122:
                return "Seashore";
            case 123:
                return "Bird Tweet";
            case 124:
                return "Telephone Ring";
            case 125:
                return "Helicopter";
            case 126:
                return "Applause";
            case 127:
                return "Gunshot";
        }
        return program + "?";
    }

    // hats
    public final static int[] HATS = {
    	OPEN_HI_HAT, CLOSED_HI_HAT, PEDAL_HI_HAT
    };

    // bass drums
    public final static int[] BASS_DRUMS = {
    	ACOUSTIC_BASS_DRUM, BASS_DRUM_1
    };

    // snares
    public final static int[] SNARES = {
    	ACOUSTIC_SNARE, ELECTRIC_SNARE, SIDE_STICK, HAND_CLAP
    };

    // toms
    public final static int[] TOMS = {
    	HI_TOM, HI_MID_TOM, LOW_MID_TOM, LOW_TOM, HI_FLOOR_TOM, LOW_FLOOR_TOM
    };

    // cymbals
    public final static int[] CYMBALS = {
    	RIDE_BELL, RIDE_CYMBAL_1, RIDE_CYMBAL_2, CRASH_CYMBAL_1,
    	CRASH_CYMBAL_2, CHINESE_CYMBAL, SPLASH_CYMBAL
    };

    // perc single
    public final static int[] PERCS = {
    	TAMBOURINE, COWBELL, VIBRASLAP, CABASA, MARACAS, CLAVES
    };

    // perc multi
    public final static int[] MULTI_PERCS = {
    	HI_BONGO, LOW_BONGO,
    	MUTE_HI_CONGA, OPEN_HI_CONGA, LOW_CONGA,
    	HI_TIMBALE, LOW_TIMBALE,
        HI_AGOGO, LOW_AGOGO,
        SHORT_WHISTLE, LONG_WHISTLE,
        SHORT_GUIRO, LONG_GUIRO,
    	HI_WOOD_BLOCK, LOW_WOOD_BLOCK,
	    MUTE_CUICA, OPEN_CUICA,
        MUTE_TRIANGLE, OPEN_TRIANGLE
    };

    static public int drumFamilyCount() { return 7; }

    static public String drumFamilyName(int f) {
        switch ( f ) {
        case 0: return "Cymbals";
        case 1: return "Hi Hats";
        case 2: return "Snares";
        case 3: return "Toms";
        case 4: return "Bass Drums";
        case 5: return "Percussion";
        case 6: return "Multi Percussion";
        default: return "?";
        }
    }

    static public int[] drumFamily(int f) {
        switch ( f ) {
        case 0: return CYMBALS;
        case 1: return HATS;
        case 2: return SNARES;
        case 3: return TOMS;
        case 4: return BASS_DRUMS;
        case 5: return PERCS;
        case 6: return MULTI_PERCS;
        default: return null;
        }
    }

    static public String drumProgramName(int program) {
        switch (program) {
        case 0: return "Standard Kit";
        case 1: return "Standard Kit 1";
        case 2: return "Standard Kit 2";
        case 3: return "Standard Kit 3";
        case 4: return "Standard Kit 4";
        case 5: return "Standard Kit 5";
        case 6: return "Standard Kit 6";
        case 7: return "Standard Kit 7";
        case 8: return "Room Kit";
        case 9: return "Room Kit 1";
        case 10: return "Room Kit 2";
        case 11: return "Room Kit 3";
        case 12: return "Room Kit 4";
        case 13: return "Room Kit 5";
        case 14: return "Room Kit 6";
        case 15: return "Room Kit 7";
        case 16: return "Power Kit";
        case 17: return "Power Kit 1";
        case 18: return "Power Kit 2";
        case 24: return "Electronic Kit";
        case 25: return "TR-808 Kit";
        case 32: return "Jazz Kit";
        case 33: return "Jazz Kit 1";
        case 34: return "Jazz Kit 2";
        case 35: return "Jazz Kit 3";
        case 36: return "Jazz Kit 4";
        case 40: return "Brush Kit";
        case 41: return "Brush Kit 1";
        case 42: return "Brush Kit 2";
        case 48: return "Orchestra Kit";
        case 56: return "Sound FX Kit";
        case 127: return "CM-64/CM-32L Kit";
        }
        return String.valueOf(program);
    }

    static public String drumName(int drum) {
        if (drum < HIGH_Q || drum > OPEN_SURDO) {
            return Pitch.name(drum);
        }
        switch (drum) {
        	// Level 2
        	case HIGH_Q:
        		return "High Q";
        	case SLAP:
        		return "Slap";
        	case SCRATCH:
        		return "Scratch";
        	case SCRATCH_2:
        		return "Scratch 2";
        	case STICKS:
        		return "Sticks";
        	case SQUARE:
        		return "Square";
        	case METRONOME:
        		return "Metronome";
        	case METRONOME_2:
        		return "Metronome 2";
    		// Level 1
            case ACOUSTIC_BASS_DRUM:
                return "Acoustic Bass Drum";
            case BASS_DRUM_1:
                return "Bass Drum 1";
            case SIDE_STICK:
                return "Side Stick";
            case ACOUSTIC_SNARE:
                return "Acoustic Snare";
            case HAND_CLAP:
                return "Hand Clap";
            case ELECTRIC_SNARE:
                return "Electric Snare";
            case LOW_FLOOR_TOM:
                return "Low Floor Tom";
            case CLOSED_HI_HAT:
                return "Closed Hi-Hat";
            case HI_FLOOR_TOM:
                return "High Floor Tom";
            case PEDAL_HI_HAT:
                return "Pedal Hi-Hat";
            case LOW_TOM:
                return "Low Tom";
            case OPEN_HI_HAT:
                return "Open Hi-Hat";
            case LOW_MID_TOM:
                return "Low-Mid Tom";
            case HI_MID_TOM:
                return "Hi-Mid Tom";
            case CRASH_CYMBAL_1:
                return "Crash Cymbal 1";
            case HI_TOM:
                return "High Tom";
            case RIDE_CYMBAL_1:
                return "Ride Cymbal 1";
            case CHINESE_CYMBAL:
                return "Chinese Cymbal";
            case RIDE_BELL:
                return "Ride Bell";
            case TAMBOURINE:
                return "Tambourine";
            case SPLASH_CYMBAL:
                return "Splash Cymbal";
            case COWBELL:
                return "Cowbell";
            case CRASH_CYMBAL_2:
                return "Crash Cymbal 2";
            case VIBRASLAP:
                return "Vibraslap";
            case RIDE_CYMBAL_2:
                return "Ride Cymbal 2";
            case HI_BONGO:
                return "Hi Bongo";
            case LOW_BONGO:
                return "Low Bongo";
            case MUTE_HI_CONGA:
                return "Mute Hi Conga";
            case OPEN_HI_CONGA:
                return "Open Hi Conga";
            case LOW_CONGA:
                return "Low Conga";
            case HI_TIMBALE:
                return "High Timbale";
            case LOW_TIMBALE:
                return "Low Timbale";
            case HI_AGOGO:
                return "High Agogo";
            case LOW_AGOGO:
                return "Low Agogo";
            case CABASA:
                return "Cabasa";
            case MARACAS:
                return "Maracas";
            case SHORT_WHISTLE:
                return "Short Whistle";
            case LONG_WHISTLE:
                return "Long Whistle";
            case SHORT_GUIRO:
                return "Short Guiro";
            case LONG_GUIRO:
                return "Long Guiro";
            case CLAVES:
                return "Claves";
            case HI_WOOD_BLOCK:
                return "Hi Wood Block";
            case LOW_WOOD_BLOCK:
                return "Low Wood Block";
            case MUTE_CUICA:
                return "Mute Cuica";
            case OPEN_CUICA:
                return "Open Cuica";
            case MUTE_TRIANGLE:
                return "Mute Triangle";
            case OPEN_TRIANGLE:
                return "Open Triangle";
            // Level 2
            case SHAKER:
            	return "Shaker";
            case JINGLE_BELL:
            	return "Jingle Bell";
            case BELL_TREE:
            	return "Bell Tree";
            case CASTANETS:
            	return "Castanets";
            case MUTE_SURDO:
            	return "Mute Surdo";
            case OPEN_SURDO:
            	return "Open Surdo";
        }
        return drum + "?";
    }
}
