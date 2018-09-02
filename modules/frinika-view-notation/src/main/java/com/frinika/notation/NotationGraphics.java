/*
 * Created on 16.4.2007
 *
 * Copyright (c) 2006-2007 Karl Helgason
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
package com.frinika.notation;

import com.frinika.gui.util.WindowUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class NotationGraphics {

    private boolean darkMode = WindowUtils.isDarkMode();

    public class Note {

        public float x = 0.0f;
        public int note = 0;
        public int dur = 0;
        public int linedir = 0;
        public int dotted = 0;
        public int accidental = 0;
        public int mark = 0;
        public Color color = null;

        public Note() {
        }

        public Note(int note, int dur) {
            this.x = cx;
            this.note = note;
            this.dur = dur;
        }

        public Note(int note, int dur, int dotted) {
            this.x = cx;
            this.note = note;
            this.dur = dur;
            this.dotted = dotted;
        }

        public Note(int note, int dur, int dotted, int accidental) {
            this.x = cx;
            this.note = note;
            this.dur = dur;
            this.dotted = dotted;
            this.accidental = accidental;
        }

        public Note(int note, int dur, int dotted, int accidental, int mark) {
            this.x = cx;
            this.note = note;
            this.dur = dur;
            this.dotted = dotted;
            this.accidental = accidental;
            this.mark = mark;
        }

        public Note(int note, int dur, int dotted, int accidental, int mark, int linedir) {
            this.x = cx;
            this.note = note;
            this.dur = dur;
            this.linedir = linedir;
            this.dotted = dotted;
            this.accidental = accidental;
            this.mark = mark;
        }
    }

    public final static int CLEF_F = -7;
    public final static int CLEF_C = 0;
    public final static int CLEF_G = 7;
    public final static int CLEF_NEUTRAL = 128;
    public final static int CLEF_TAB = 129;

    public final static int ACCIDENTAL_NATURAL = 32768;
    public final static int ACCIDENTAL_DOUBLE_SHARP = 200;
    public final static int ACCIDENTAL_SHARP_AND_A_HALF = 150;
    public final static int ACCIDENTAL_SHARP = 100;
    public final static int ACCIDENTAL_DEMISHARP = 50;
    public final static int ACCIDENTAL_DEMIFLAT = -50;
    public final static int ACCIDENTAL_FLAT = -100;
    public final static int ACCIDENTAL_FLAT_AND_A_HALF = -150;
    public final static int ACCIDENTAL_DOUBLE_FLAT = -200;

    // NOT IMPLEMTED YET!
    public final static int ARTICULATION_MARK_STACCATO = 0xE153;
    public final static int ARTICULATION_MARK_ACCENT = 0xE151;
    public final static int ARTICULATION_MARK_STOPPED_NOTE = 0xE153;
    public final static int ARTICULATION_MARK_OPEN_NOTE = 0xE24D;
    public final static int ARTICULATION_MARK_TENUTO = 0xE156;
    public final static int ARTICULATION_MARK_FERMATA = 0xE148;
    public final static int ARTICULATION_MARK_UP_BOW = 0xE15D;
    public final static int ARTICULATION_MARK_DOWN_BOW = 0xE15E;

    public final static int ORNAMENT_MARK_TRILL = 0xE161;
    public final static int ORNAMENT_MARK_MODRENT = 0xE174;
    public final static int ORNAMENT_MARK_TURN = 0xE160;

    public final static Font FONT_EMMENTALER = loadFont("/com/frinika/notation/Emmentaler-20.ttf");

    private Graphics2D g;

    private Font music_font;
    private float music_fontsize;
    private Stroke music_stroke;
    private Stroke music_stroke_tie;
    private Stroke music_stroke_dotted;
    //private Stroke music_streke_beam;

    private float beamheight;
    private float beamspace;

    private float grid_size;

    private float cx = 0;
    private float cy = 0;

    private int stafflinecount = 5;

    private boolean in_note_group = false;

    private static Font loadFont(String filename) {
        try {
            Font font;
            try (InputStream is = NotationGraphics.class.getResourceAsStream(filename)) {
                font = Font.createFont(Font.TRUETYPE_FONT, is);
            }
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public NotationGraphics() {
        setSize(25);
    }

    public void setSize(float size) {
        music_fontsize = size;
        music_font = FONT_EMMENTALER.deriveFont(music_fontsize);
        music_stroke = new BasicStroke(3.5f * (music_fontsize / 100f));
        music_stroke_tie = new BasicStroke(4.5f * (music_fontsize / 100f));
        //music_streke_beam = new BasicStroke(12.5f*(music_fontsize/100f),BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        music_stroke_dotted = new BasicStroke(
                3.5f * (music_fontsize / 100f),
                BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER,
                10.0f,
                new float[]{7.7f * (music_fontsize / 100f)},
                7.7f * (music_fontsize / 100f));
        grid_size = 25f * (music_fontsize / 100f);

        beamheight = grid_size * 0.25f;
        beamspace = grid_size * 0.4f;
    }

    public float getGridSize() {
        return grid_size;
    }

    public void setGraphics(Graphics2D g) {
        this.g = g;
    }

    public float getCurrentX() {
        return cx;
    }

    public float getCurrentY() {
        return cy;
    }

    public float getCurrentCol() {
        return cx / grid_size;
    }

    public float getCurrentLine() {
        return cy / grid_size;
    }

    public void absoluteX(float x) {
        cx = x;
    }
    // Set Y-location of Staff First Line

    public void absoluteY(float y) {
        cy = y;
    }

    public void relativeX(float x) {
        cx += x;
    }
    // Set Y-location of Staff First Line

    public void relativeY(float y) {
        cy += y;
    }

    public void absolute(float x) {
        cx = x * grid_size;
    }
    // Set Y-location of Staff First Line

    public void absoluteLine(float y) {
        cy = y * grid_size;
    }

    public void relative(float x) {
        cx += x * grid_size;
    }
    // Set Y-location of Staff First Line

    public void relativeLine(float y) {
        cy += y * grid_size;
    }

    public void setStaffLineCount(int count) {
        stafflinecount = count;
    }

    public void drawStaff(float width) {
        g.setStroke(music_stroke);
        float grid = getGridSize();

        for (int i = 0; i < stafflinecount; i++) {
            Line2D.Float line = new Line2D.Float(cx, cy - i * grid, cx + width, cy - i * grid);
            g.draw(line);
        }
    }

    public void drawBarLine() {
        drawBarLine(1);
    }

    public void drawDottedBarLine() {
        drawBarLine(0);
    }

    public void drawDoubleBarLine() {
        drawBarLine(2);
    }

    public void drawBarLine(int type) {
        float grid = getGridSize();
        if (type == 0) {
            g.setStroke(music_stroke_dotted);
            Line2D.Float line = new Line2D.Float(cx, cy - grid * (stafflinecount - 1), cx, cy);
            g.draw(line);
        }
        if (type == 1) {
            g.setStroke(music_stroke);
            Line2D.Float line = new Line2D.Float(cx, cy - grid * (stafflinecount - 1), cx, cy);
            g.draw(line);
        }
        if (type == 2) {
            g.setStroke(music_stroke);
            float w = grid * 0.24f;
            Line2D.Float line = new Line2D.Float(cx - w, cy - grid * (stafflinecount - 1), cx - w, cy);
            g.draw(line);
            line = new Line2D.Float(cx + w, cy - grid * (stafflinecount - 1), cx + w, cy);
            g.draw(line);
        }
    }

    public void drawClef(int clef) {
        if (clef == CLEF_F) {
            drawClef(clef, 6);
        } else if (clef == CLEF_C) {
            drawClef(clef, 4);
        } else if (clef == CLEF_G) {
            drawClef(clef, 2);
        } else if (clef == CLEF_NEUTRAL) {
            drawClef(clef, 4);
        } else {
            drawClef(clef, 4);
        }
    }

    public void drawClef(int clef, int line) {
        g.setFont(music_font);
        float grid = getGridSize();
        if (clef == CLEF_F) {
            g.drawString("" + (char) 0xE18B, cx, cy - grid * (line * 0.5f - 0.1f));
        }
        if (clef == CLEF_C) {
            g.drawString("" + (char) 0xE189, cx, cy - grid * (line * 0.5f - 0.1f));
        }
        if (clef == CLEF_G) {
            g.drawString("" + (char) 0xE18D, cx, cy - grid * (line * 0.5f - 0.1f));
        }
        if (clef == CLEF_NEUTRAL) {
            g.drawString("" + (char) 0xE18F, cx, cy - grid * (line * 0.5f - 0.1f));
        }
        if (clef == CLEF_TAB) {
            g.drawString("" + (char) 0xE191, cx, cy - grid * (line * 0.5f - 0.1f));
        }
    }

    public float drawFlatKeySignature(int... notes) {
        int[] accidentals = new int[notes.length];
        for (int i = 0; i < accidentals.length; i++) {
            accidentals[i] = -100;
        }
        return drawKeySignature(notes, accidentals);
    }

    public float drawSharpKeySignature(int... notes) {
        int[] accidentals = new int[notes.length];
        for (int i = 0; i < accidentals.length; i++) {
            accidentals[i] = 100;
        }
        return drawKeySignature(notes, accidentals);
    }

    public float drawKeySignature(int[] notes, int accidental) {
        int[] accidentals = new int[notes.length];
        for (int i = 0; i < accidentals.length; i++) {
            accidentals[i] = accidental;
        }
        return drawKeySignature(notes, accidentals);
    }

    public float drawKeySignature(int[] notes, int[] accidentals) {
        g.setFont(music_font);
        float x = 0;
        float grid = getGridSize();
        for (int i = 0; i < accidentals.length; i++) {

            int accidental = accidentals[i];
            int note = notes[i];

            if (accidental == -200) {
                g.drawString("" + (char) 0xE114, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.7f;
            }
            if (accidental == -150) {
                g.drawString("" + (char) 0xE113, x * grid + cx, cy - (note * grid * 0.5f));
                g.drawString("" + (char) 0xE112, x * grid + cx + grid, cy - (note * grid * 0.5f));
                x += 2.2f;
            }
            if (accidental == -100) {
                g.drawString("" + (char) 0xE112, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.2f;
            }
            if (accidental == -50) {
                g.drawString("" + (char) 0xE113, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.2f;
            }
            if (accidental == ACCIDENTAL_NATURAL) {
                g.drawString("" + (char) 0xE111, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.2f;
            }

            if (accidental == 50) {
                g.drawString("" + (char) 0xE10F, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.2f;
            }
            if (accidental == 100) {
                g.drawString("" + (char) 0xE10E, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.2f;
            }
            if (accidental == 150) {
                g.drawString("" + (char) 0xE110, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.8f;
            }
            if (accidental == 200) {
                g.drawString("" + (char) 0xE116, x * grid + cx, cy - (note * grid * 0.5f));
                x += 1.2f;
            }

        }

        return x;
    }

    public void drawTimeSignature(int a, int b) {
        float grid = getGridSize();
        g.setFont(music_font);
        g.drawString("" + (char) (0x0030 + a), cx, cy - grid * 2);
        g.drawString("" + (char) (0x0030 + b), cx, cy);
    }

    // type = 0 (common time)
    // type = 1 (cut time)
    public void drawTimeSignature(int type) {
        float grid = getGridSize();
        g.setFont(music_font);
        g.drawString("" + (char) (0xE193 + type), cx, cy - grid * 2);
    }

    // note = 0 is first_line (bottom line)	
    // dur =-2    (16 beat)  (quadruple whole note)
    // dur =-1    (8 beat)   (double whole note)
    // dur = 0    (4 beat)   (whole   note)
    // dur = 1    (2 beat)   (half    note)
    // dur = 2    (1 beat)   (quarter note)
    // dur = 3    (1/2 beat) (eighth  note)
    // dur = 4    (1/4 beat) (sixteenth note)
    // dur = 5    (1/8 beat)
    // linedir = -1 (show no line)
    // linedir = 0 (auto)
    // linedir = 1 (line is on right going up)
    // linedir = 2 (line is on left, going down)	
    public Note drawNote(int note, int dur) {
        return drawNote(new Note(note, dur));
    }

    public Note drawNote(int note, int dur, int dotted) {
        return drawNote(new Note(note, dur, dotted));
    }

    public Note drawNote(int note, int dur, int dotted, int accidental) {
        return drawNote(new Note(note, dur, dotted, accidental));
    }

    public Note drawNote(int note, int dur, int dotted, int accidental, int mark) {
        return drawNote(new Note(note, dur, dotted, accidental, mark));
    }

    public Note drawNote(int note, int dur, int dotted, int accidental, int mark, int linedir) {
        return drawNote(new Note(note, dur, dotted, accidental, mark, linedir));
    }

    public Note drawNote(Note n) {
        if (n.linedir == 0) {
            if (n.note > 4) {
                n.linedir = 2;
            } else {
                n.linedir = 1;
            }
        } else {
            if (n.linedir == -1) {
                n.linedir = 0;
            }
        }
        if (n.dur < 1) {
            n.linedir = 0;
        }

        boolean breakgroup = false;
        for (Note notepart : note_group_list) {
            // If x and last x is same, then don't break
            if (Math.abs(notepart.x - cx) < 0.0000001) {
                breakgroup = false;
                break;
            }
            if (n.linedir != 0 && notepart.linedir != 0) {
                if (notepart.linedir != n.linedir) {
                    int min = note_group_list.get(0).note;
                    int max = note_group_list.get(0).note;
                    for (Note notepart2 : note_group_list) {
                        if (notepart2.note < min) {
                            min = notepart2.note;
                        }
                        if (notepart2.note > max) {
                            max = notepart2.note;
                        }
                    }
                    if (n.note < min) {
                        min = n.note;
                    }
                    if (n.note > max) {
                        max = n.note;
                    }

                    if (max - min > 7) {
                        breakgroup = true;
                        break;
                    }
                }
            }
            if (notepart.dur <= 2) {
                if (Math.abs(notepart.x - cx) > 0.0000001) {
                    breakgroup = true;
                    break;
                }
            }
            if (n.dur <= 2) {
                if (notepart.dur > 2) {
                    breakgroup = true;
                    break;
                }
            }
        }

        if (breakgroup) {
            drawNotes();
        }

        addNoteToGroup(n);
        if (!in_note_group) {
            drawNotes();
        }

        return n;
    }

    private char getNoteHeadSymbol(int dur) {
        if (dur == -2) {
            return (char) 0xE1BD;
        } else if (dur == -1) {
            return (char) 0xE11A;
        } else if (dur == 0) {
            return (char) 0xE11B;
        } else if (dur == 1) {
            return (char) 0xE11C;
        } else {
            return (char) 0xE11D;
        }
    }

    private void drawLedge(float cx, int note) {
        g.setStroke(music_stroke);
        float grid = getGridSize();
        float notebasewidth = grid * 1.3f;

        float ledge_width = grid * 0.3f;
        for (int gi = 1; note <= -gi * 2; gi++) {
            Line2D.Float line = new Line2D.Float(cx - ledge_width, cy + grid * gi, cx + notebasewidth + ledge_width, cy + grid * gi);
            g.draw(line);
        }

        //for(int gi = stafflinecount+1; note >= gi*2; gi++)
        for (int gi = stafflinecount; note >= gi * 2; gi++) {
            Line2D.Float line = new Line2D.Float(cx - ledge_width, cy - grid * gi, cx + notebasewidth + ledge_width, cy - grid * gi);
            g.draw(line);
        }
    }

    private class TimePart {

        float x;
        int dur;
        int mark = 0;
        List<Note> notes = new ArrayList<>();
    }

    private TreeMap<Float, TimePart> note_group_xlist = new TreeMap<>();
    private ArrayList<Note> note_group_list = new ArrayList<>();

    private Note addNoteToGroup(Note notepart) {
        note_group_list.add(notepart);

        TimePart timepart = note_group_xlist.get(notepart.x);
        if (timepart == null) {
            timepart = new TimePart();
            timepart.x = notepart.x;
            timepart.dur = notepart.dur;
            timepart.mark = notepart.mark;
            note_group_xlist.put(notepart.x, timepart);
        }
        timepart.notes.add(notepart);

        return notepart;
    }

    private void drawNoteBase(Note notepart, int movefix) {
        drawLedge(notepart.x, notepart.note);

        int note = notepart.note;
        int dur = notepart.dur;
        int dotted = notepart.dotted;
        int accidental = notepart.accidental;
        float grid = getGridSize();
        float cx = notepart.x;

        float notebasewidth = grid * 1.3f;

        g.setFont(music_font);

        float mx = 0;
        if (movefix == -1) {
            mx = -notebasewidth;
        }

        Color bakcolor = null;
        if (notepart.color != null) {
            bakcolor = g.getColor();
            g.setColor(notepart.color);
        }

        if (accidental == -200) {
            g.drawString("" + (char) 0xE114, mx + cx - grid * 1.7f, cy - (note * grid * 0.5f));
        }
        if (accidental == -150) {
            g.drawString("" + (char) 0xE113, mx + cx - grid * 2.2f, cy - (note * grid * 0.5f));
            g.drawString("" + (char) 0xE112, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }
        if (accidental == -100) {
            g.drawString("" + (char) 0xE112, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }
        if (accidental == -50) {
            g.drawString("" + (char) 0xE113, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }
        if (accidental == ACCIDENTAL_NATURAL) {
            g.drawString("" + (char) 0xE111, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }

        if (accidental == 50) {
            g.drawString("" + (char) 0xE10F, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }
        if (accidental == 100) {
            g.drawString("" + (char) 0xE10E, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }
        if (accidental == 150) {
            g.drawString("" + (char) 0xE110, mx + cx - grid * 1.7f, cy - (note * grid * 0.5f));
        }
        if (accidental == 200) {
            g.drawString("" + (char) 0xE116, mx + cx - grid * 1.2f, cy - (note * grid * 0.5f));
        }

        if (movefix == 1) {
            mx = notebasewidth;
        }

        g.drawString("" + getNoteHeadSymbol(dur), cx + mx, cy - (note * grid * 0.5f));

        if (movefix == -1) {
            mx = 0;
        }
        float xx = 0;
        for (int i = 0; i < dotted; i++) {

            g.drawString("" + (char) 0xE119, cx + grid * 1.6f + mx + xx, cy - (note * grid * 0.5f));
            xx += grid * 0.6f;
        }

        if (bakcolor != null) {
            g.setColor(bakcolor);
        }

    }

    private void drawNotes() {
        if (note_group_list.size() == 0) {
            return;
        }

        float grid = getGridSize();
        float notebasewidth = grid * 1.3f;
        float stem_length = 3.5f * grid;

        g.setStroke(music_stroke);

        int linedir = note_group_list.get(0).linedir;

        TimePart[] parts = new TimePart[note_group_xlist.values().size()];
        note_group_xlist.values().toArray(parts);

        for (TimePart timepart : parts) {
            Note[] notes = new Note[timepart.notes.size()];
            timepart.notes.toArray(notes);
            Arrays.sort(notes, new Comparator<Note>() {
                @Override
                public int compare(Note o1, Note o2) {
                    return o2.note - o1.note;
                }
            });
            Note lastpart = null;
            for (Note notepart : timepart.notes) {
                if (lastpart != null && (Math.abs(lastpart.note - notepart.note) == 1)) {
                    if (linedir == 0 || linedir == 1) {
                        drawNoteBase(notepart, 1);
                    } else {
                        drawNoteBase(notepart, -1);
                    }
                } else {
                    drawNoteBase(notepart, 0);
                }
                lastpart = notepart;
            }

        }

        int maxdur = 0;
        for (int i = 0; i < note_group_list.size(); i++) {
            Note notepart = note_group_list.get(i);
            if (notepart.dur > maxdur) {
                maxdur = notepart.dur;
            }
        }
        if (maxdur > 4) {
            stem_length += (maxdur - 4) * (beamheight + beamspace);
        }

        {
            float x = note_group_list.get(0).x;
            boolean ok = true;
            int maxnote = note_group_list.get(0).note;
            int minnote = note_group_list.get(0).note;
            for (int i = 0; i < note_group_list.size(); i++) {
                Note notepart = note_group_list.get(i);
                if (notepart.note > maxnote) {
                    maxnote = notepart.note;
                }
                if (notepart.note < minnote) {
                    minnote = notepart.note;
                }
                if (notepart.linedir > linedir) {
                    linedir = notepart.linedir;
                }
                if (Math.abs(notepart.x - x) > 0.00000001f) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                int dur = note_group_list.get(0).dur;
                float cx = note_group_list.get(0).x;

                if (linedir == 1) {
                    Line2D.Float line = new Line2D.Float(cx + notebasewidth, cy - stem_length - (maxnote * grid * 0.5f), cx + notebasewidth, cy - (grid * 0.3f) - (minnote * grid * 0.5f));
                    g.draw(line);

                    if (dur == 3) {
                        g.drawString("" + (char) 0xE17F, cx + notebasewidth + (grid * 0.1f), cy - (stem_length) - (maxnote * grid * 0.5f));
                    }
                    if (dur == 4) {
                        g.drawString("" + (char) 0xE180, cx + notebasewidth + (grid * 0.1f), cy - (stem_length) - (maxnote * grid * 0.5f));
                    }
                    if (dur == 5) {
                        g.drawString("" + (char) 0xE181, cx + notebasewidth + (grid * 0.1f), cy - (stem_length) - (maxnote * grid * 0.5f));
                    }
                    if (dur == 6) {
                        g.drawString("" + (char) 0xE182, cx + notebasewidth + (grid * 0.1f), cy - (stem_length) - (maxnote * grid * 0.5f));
                    }

                }

                if (linedir == 2) {
                    Line2D.Float line = new Line2D.Float(cx, cy + stem_length - (minnote * grid * 0.5f), cx, cy + (grid * 0.3f) - (maxnote * grid * 0.5f));
                    g.draw(line);

                    if (dur == 3) {
                        g.drawString("" + (char) 0xE183, cx + (grid * 0.1f), cy + (stem_length) - (minnote * grid * 0.5f));
                    }
                    if (dur == 4) {
                        g.drawString("" + (char) 0xE186, cx + (grid * 0.1f), cy + (stem_length) - (minnote * grid * 0.5f));
                    }
                    if (dur == 5) {
                        g.drawString("" + (char) 0xE187, cx + (grid * 0.1f), cy + (stem_length) - (minnote * grid * 0.5f));
                    }
                    if (dur == 6) {
                        g.drawString("" + (char) 0xE188, cx + (grid * 0.1f), cy + (stem_length) - (minnote * grid * 0.5f));
                    }

                }
                note_group_list.clear();
                note_group_xlist.clear();
                return;
            }
        }

        {

            int maxnote = note_group_list.get(0).note;
            float maxnote_x = note_group_list.get(0).x;
            int minnote = note_group_list.get(0).note;
            float minnote_x = note_group_list.get(0).x;
            float minx = note_group_list.get(0).x;
            float maxx = note_group_list.get(0).x;
            float minx_note = note_group_list.get(0).note;
            float maxx_note = note_group_list.get(0).note;
            for (int i = 0; i < note_group_list.size(); i++) {
                Note notepart = note_group_list.get(i);
                if (notepart.note > maxnote) {
                    maxnote = notepart.note;
                    maxnote_x = notepart.x;
                }
                if (notepart.note < minnote) {
                    minnote = notepart.note;
                    minnote_x = notepart.x;
                }
                if (notepart.x > maxx) {
                    maxx_note = notepart.note;
                    maxx = notepart.x;
                }
                if (notepart.x < minx) {
                    minx_note = notepart.note;
                    minx = notepart.x;
                }
                //if(notepart.linedir > linedir) linedir = notepart.linedir;				
            }

            if (linedir == 1) {
                float h = 0;
                if (Math.abs(maxnote_x - minnote_x) > 0.00000001f) {
                    h = Math.abs((maxnote - minnote) * grid * 0.5f) / Math.abs(maxnote_x - minnote_x);
                }
                if (h > grid * 0.05f) {
                    h = grid * 0.05f;
                }

                // 1. find optimial h
                for (int i = 0; i < note_group_list.size(); i++) {
                    Note notepart = note_group_list.get(i);
                    int note = notepart.note;
                    float cx = notepart.x;
                    if (Math.abs(maxnote_x - cx) > 0.00000001f) {
                        float h2 = Math.abs((maxnote - note) * grid * 0.5f) / Math.abs(maxnote_x - cx);
                        if (h2 < h) {
                            h = h2;
                        }
                    }

                }

                if (maxnote_x != maxx && maxnote_x != minx) {
                    h = 0;
                }

                //g.setColor(Color.GREEN);
                for (int i = 0; i < note_group_list.size(); i++) {
                    Note notepart = note_group_list.get(i);
                    int note = notepart.note;
                    float cx = notepart.x;
                    if (minx_note < maxx_note) {
                        Line2D.Float line = new Line2D.Float(cx + notebasewidth, h * (maxx - cx) + cy - stem_length - (maxnote * grid * 0.5f), cx + notebasewidth, cy - (grid * 0.3f) - (note * grid * 0.5f));
                        g.draw(line);
                    } else {
                        Line2D.Float line = new Line2D.Float(cx + notebasewidth, h * (cx - minx) + cy - stem_length - (maxnote * grid * 0.5f), cx + notebasewidth, cy - (grid * 0.3f) - (note * grid * 0.5f));
                        g.draw(line);
                    }
                }

                //g.setStroke(music_streke_beam);
                g.setStroke(music_stroke);
                /*
				GeneralPath path = new GeneralPath();	
				if(minx_note < maxx_note)
				{
					path.moveTo(minx+notebasewidth, h*(maxx-minx)+cy-stem_length-(maxnote*grid*0.5f));
					path.lineTo(minx+notebasewidth, h*(maxx-minx)+cy-stem_length-(maxnote*grid*0.5f)+grid*0.5f);				
					path.lineTo(maxx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)+grid*0.5f);
					path.lineTo(maxx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f));				
				}
				else
				{
					path.moveTo(minx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f));
					path.lineTo(minx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)+grid*0.5f);				
					path.lineTo(maxx+notebasewidth, h*(maxx-minx)+cy-stem_length-(maxnote*grid*0.5f)+grid*0.5f);
					path.lineTo(maxx+notebasewidth, h*(maxx-minx)+cy-stem_length-(maxnote*grid*0.5f));
				}*/
 /*
				path.moveTo(minx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f));
				path.lineTo(minx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)+grid*0.5f);				
				path.lineTo(maxx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)+grid*0.5f);
				path.lineTo(maxx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)); */
 /*path.closePath();
				g.draw(path);
				g.fill(path);*/
                //Line2D.Float line = new Line2D.Float(minx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f), maxx+notebasewidth, cy-stem_length-(maxnote*grid*0.5f));		
                //g.draw(line);					

                for (int i = 0; i < parts.length; i++) {

                    float gy = 0;
                    for (int j = 3; j <= parts[i].dur; j++) {
                        float x1 = parts[i].x;
                        float x2 = parts[i].x;
                        if (i + 1 < parts.length) {
                            if (parts[i + 1].dur >= j) {
                                x2 = parts[i + 1].x;
                            } else {
                                x2 = (x2 + parts[i + 1].x) / 2.0f;
                            }
                        } else {
                            if (parts[i - 1].dur >= j) {
                                gy -= beamheight + beamspace;
                                continue;
                            }
                            x2 = (x2 + parts[i - 1].x) / 2.0f;
                        }

                        GeneralPath path = new GeneralPath();
                        if (minx_note < maxx_note) {
                            //g.setColor(Color.RED);
                            path.moveTo(x1 + notebasewidth, h * (maxx - x1) + cy - stem_length - (maxnote * grid * 0.5f) - gy);
                            path.lineTo(x1 + notebasewidth, h * (maxx - x1) + cy - stem_length - (maxnote * grid * 0.5f) - gy + beamheight);
                            path.lineTo(x2 + notebasewidth, h * (maxx - x2) + cy - stem_length - (maxnote * grid * 0.5f) - gy + beamheight);
                            path.lineTo(x2 + notebasewidth, h * (maxx - x2) + cy - stem_length - (maxnote * grid * 0.5f) - gy);
                        } else {
                            //g.setColor(Color.BLUE);
                            path.moveTo(x1 + notebasewidth, h * (x1 - minx) + cy - stem_length - (maxnote * grid * 0.5f) - gy);
                            path.lineTo(x1 + notebasewidth, h * (x1 - minx) + cy - stem_length - (maxnote * grid * 0.5f) - gy + beamheight);
                            path.lineTo(x2 + notebasewidth, h * (x2 - minx) + cy - stem_length - (maxnote * grid * 0.5f) - gy + beamheight);
                            path.lineTo(x2 + notebasewidth, h * (x2 - minx) + cy - stem_length - (maxnote * grid * 0.5f) - gy);
                        }

                        path.closePath();
                        g.draw(path);
                        g.fill(path);
                        g.setColor(Color.BLACK);

                        //line = new Line2D.Float(x1+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)-gy, x2+notebasewidth, cy-stem_length-(maxnote*grid*0.5f)-gy);		
                        //g.draw(line);
                        gy -= beamheight + beamspace;
                    }

                }

                g.setColor(Color.BLACK);

            }

            if (linedir == 2) {
                float h = 0;
                if (Math.abs(maxnote_x - minnote_x) > 0.00000001f) {
                    h = Math.abs((maxnote - minnote) * grid * 0.5f) / Math.abs(maxnote_x - minnote_x);
                }
                if (h > grid * 0.05f) {
                    h = grid * 0.05f;
                }

                // 1. find optimial h
                for (int i = 0; i < note_group_list.size(); i++) {
                    Note notepart = note_group_list.get(i);
                    int note = notepart.note;
                    float cx = notepart.x;
                    if (Math.abs(minnote_x - cx) > 0.00000001f) {
                        float h2 = Math.abs((minnote - note) * grid * 0.5f) / Math.abs(minnote_x - cx);
                        if (h2 < h) {
                            h = h2;
                        }
                    }

                }
                if (minnote_x != maxx && minnote_x != minx) {
                    h = 0;
                }

                for (int i = 0; i < note_group_list.size(); i++) {
                    Note notepart = note_group_list.get(i);
                    int note = notepart.note;
                    float cx = notepart.x;
                    //Line2D.Float line = new Line2D.Float(cx, cy+stem_length-(minnote*grid*0.5f), cx, cy+(grid*0.3f)-(note*grid*0.5f));		
                    //g.draw(line);			

                    if (minx_note < maxx_note) {
                        Line2D.Float line = new Line2D.Float(cx, -h * (cx - minx) + cy + stem_length - (minnote * grid * 0.5f), cx, cy + (grid * 0.3f) - (note * grid * 0.5f));
                        g.draw(line);
                    } else {
                        Line2D.Float line = new Line2D.Float(cx, -h * (maxx - cx) + cy + stem_length - (minnote * grid * 0.5f), cx, cy + (grid * 0.3f) - (note * grid * 0.5f));
                        g.draw(line);
                    }
                }

                g.setStroke(music_stroke);
                /*
				GeneralPath path = new GeneralPath();
				path.moveTo(minx, cy+stem_length-(minnote*grid*0.5f));
				path.lineTo(minx, cy+stem_length-(minnote*grid*0.5f)-grid*0.5f);
				path.lineTo(maxx, cy+stem_length-(minnote*grid*0.5f)-grid*0.5f);
				path.lineTo(maxx, cy+stem_length-(minnote*grid*0.5f));
				path.closePath();
				g.draw(path);
				g.fill(path);*/

                for (int i = 0; i < parts.length; i++) {

                    float gy = 0;
                    for (int j = 3; j <= parts[i].dur; j++) {
                        float x1 = parts[i].x;
                        float x2 = parts[i].x;
                        if (i + 1 < parts.length) {
                            if (parts[i + 1].dur >= j) {
                                x2 = parts[i + 1].x;
                            } else {
                                x2 = (x2 + parts[i + 1].x) / 2.0f;
                            }
                        } else {
                            if (parts[i - 1].dur >= j) {
                                gy += beamheight + beamspace;
                                continue;
                            }
                            x2 = (x2 + parts[i - 1].x) / 2.0f;
                        }

                        GeneralPath path = new GeneralPath();
                        /*
						path.moveTo(x1, cy+stem_length-(minnote*grid*0.5f)-gy);
						path.lineTo(x1, cy+stem_length-(minnote*grid*0.5f)-gy-grid*0.5f);
						path.lineTo(x2, cy+stem_length-(minnote*grid*0.5f)-gy-grid*0.5f);
						path.lineTo(x2, cy+stem_length-(minnote*grid*0.5f)-gy);
                         */
                        if (minx_note < maxx_note) {
                            //g.setColor(Color.GREEN);
                            path.moveTo(x1, h * (minx - x1) + cy + stem_length - (minnote * grid * 0.5f) - gy);
                            path.lineTo(x1, h * (minx - x1) + cy + stem_length - (minnote * grid * 0.5f) - gy - beamheight);
                            path.lineTo(x2, h * (minx - x2) + cy + stem_length - (minnote * grid * 0.5f) - gy - beamheight);
                            path.lineTo(x2, h * (minx - x2) + cy + stem_length - (minnote * grid * 0.5f) - gy);

                            /*							path.moveTo(x1+notebasewidth, h*(maxx-x1)+cy-stem_length-(maxnote*grid*0.5f)-gy);
							path.lineTo(x1+notebasewidth, h*(maxx-x1)+cy-stem_length-(maxnote*grid*0.5f)-gy+grid*0.5f);
							path.lineTo(x2+notebasewidth, h*(maxx-x2)+cy-stem_length-(maxnote*grid*0.5f)-gy+grid*0.5f);
							path.lineTo(x2+notebasewidth, h*(maxx-x2)+cy-stem_length-(maxnote*grid*0.5f)-gy);*/
                        } else {
                            //g.setColor(Color.PINK);
                            path.moveTo(x1, h * (x1 - maxx) + cy + stem_length - (minnote * grid * 0.5f) - gy);
                            path.lineTo(x1, h * (x1 - maxx) + cy + stem_length - (minnote * grid * 0.5f) - gy - beamheight);
                            path.lineTo(x2, h * (x2 - maxx) + cy + stem_length - (minnote * grid * 0.5f) - gy - beamheight);
                            path.lineTo(x2, h * (x2 - maxx) + cy + stem_length - (minnote * grid * 0.5f) - gy);
                            /*							
							path.moveTo(x1+notebasewidth, h*(x1-minx)+cy-stem_length-(maxnote*grid*0.5f)-gy);
							path.lineTo(x1+notebasewidth, h*(x1-minx)+cy-stem_length-(maxnote*grid*0.5f)-gy+grid*0.5f);
							path.lineTo(x2+notebasewidth, h*(x2-minx)+cy-stem_length-(maxnote*grid*0.5f)-gy+grid*0.5f);
							path.lineTo(x2+notebasewidth, h*(x2-minx)+cy-stem_length-(maxnote*grid*0.5f)-gy); */
                        }

                        path.closePath();
                        g.draw(path);
                        g.fill(path);
                        g.setColor(Color.BLACK);
                        //line = new Line2D.Float(x1, cy+stem_length-(minnote*grid*0.5f)-gy, x2, cy+stem_length-(minnote*grid*0.5f)-gy);		
                        //g.draw(line);
                        gy += beamheight + beamspace;
                    }

                }
            }

            note_group_list.clear();
            note_group_xlist.clear();
            return;
        }
    }

    public void startNoteGroup() {
        if (in_note_group) {
            return;
        }
        in_note_group = true;
    }

    public void endNoteGroup() {
        if (!in_note_group) {
            return;
        }
        in_note_group = false;
        drawNotes();
    }

    public void drawNoteTie(Note note1, Note note2) {
        g.setStroke(music_stroke_tie);

        float grid = getGridSize();

        float y1 = cy - (note1.note * grid * 0.5f);
        float y2 = cy - (note2.note * grid * 0.5f);
        float x1 = note1.x + grid * 0.5f;
        float x2 = note2.x + grid * 0.5f;

        CubicCurve2D.Float curve = new CubicCurve2D.Float(
                x1, y1,
                x1, y1 + grid * 1f,
                x2, y2 + grid * 1f,
                x2, y2);
        g.draw(curve);
    }

    public void drawRest(int dur) {
        drawRest(dur, 0);
    }

    public void drawRest(int dur, int dotted) {
        drawNotes();

        float grid = getGridSize();

        g.setFont(music_font);

        if (dur == -2) {
            g.drawString("" + (char) 0xE142, cx, cy - grid * 2.5f);
            g.drawString("" + (char) 0xE142, cx, cy - grid * 1.5f);
        } else if (dur == -1) {
            g.drawString("" + (char) 0xE142, cx, cy - grid * 2.5f);
        } else if (dur == 0) {
            g.drawString("" + (char) 0xE100, cx, cy - grid * 3);
        } else if (dur == 1) {
            g.drawString("" + (char) 0xE101, cx, cy - grid * 2);
        } else if (dur == 2) {
            g.drawString("" + (char) 0xE107, cx, cy - grid * 2);
        } else if (dur == 3) {
            g.drawString("" + (char) 0xE109, cx, cy - grid * 2);
        } else if (dur == 4) {
            g.drawString("" + (char) 0xE10A, cx, cy - grid * 3);
        } else if (dur == 5) {
            g.drawString("" + (char) 0xE10B, cx, cy - grid * 2);
        } else if (dur == 6) {
            g.drawString("" + (char) 0xE10C, cx, cy - grid * 3);
        } else if (dur == 7) {
            g.drawString("" + (char) 0xE10D, cx, cy - grid * 2);
        }

        float xx = 0;
        for (int i = 0; i < dotted; i++) {
            g.drawString("" + (char) 0xE119, cx + grid * 1.8f + xx, cy - (5 * grid * 0.5f));
            xx += grid * 0.6f;
        }
    }
}
