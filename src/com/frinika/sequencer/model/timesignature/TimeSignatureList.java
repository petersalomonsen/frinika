/*
 * Created on May 16, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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
package com.frinika.sequencer.model.timesignature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

/**
 * Maintains a list of time signature changes.
 * 
 * TODO the iterators need to implement a locking mechanism to avoid concurrent
 * modification
 * 
 * Please use iterators if posible. Faster than repeated querries.
 * 
 * @author pjl
 * 
 */
public class TimeSignatureList implements Serializable {

    transient private TreeMap<Integer, TimeSignatureEvent> eventByBar;
    private Vector<TimeSignatureEvent> list;
    transient private boolean dirty = true;
    static double tol = 1e-6;

    public TimeSignatureList() {
        eventByBar = new TreeMap<Integer, TimeSignatureEvent>();
        list = new Vector<TimeSignatureEvent>();
        // eventByBeat = new TreeMap<Integer, TimeSignitureEvent>(new
        // Comparator<TimeSignitureEvent>() {
        // });

    }

    public void remove(int bar) {
        eventByBar.remove(bar);
        dirty = true;
    }

    public synchronized void add(int bar, int nBeatPerBar) {

        if (eventByBar.remove(bar) != null) {
            System.out.println(" TIME SIG AT" + bar + " REMOVED ");
        }

        eventByBar.put(bar, new TimeSignatureEvent(bar, nBeatPerBar));
        dirty = true;
    }

    public synchronized void remove(int tick1, int tick2) {
        eventByBar.subMap(tick1, tick2).clear();
        dirty = true;
    }

    public synchronized void reco() {
        if (!dirty) {
            return;
        }
        TimeSignatureEvent prev = null;
        list.clear();

        for (Map.Entry<Integer, TimeSignatureEvent> e : eventByBar.entrySet()) {
            TimeSignatureEvent ptr = e.getValue();
            if (prev != null) {
                ptr.beat = prev.beat + (ptr.bar - prev.bar) * prev.beatsPerBar;
            } else {
                ptr.beat = 0;
            }
            prev = ptr;
            list.add(ptr);
        }

        dirty = false;
    }

    public synchronized Vector<TimeSignatureEvent> getList() {
        reco();
        return list;
    }

    public class TimeSignatureEvent  implements Serializable {

        public final int bar;     // bar at which the Time Signiture Happens
        public int beat;          // Beat at which it happens (
        public final int beatsPerBar;

        TimeSignatureEvent(int bar, int nBeatPerbar) {
            this.beatsPerBar = nBeatPerbar;
            this.bar = bar;
            beat = -1;
        }

        void display() {
            System.out.print(beatsPerBar);
        }
    }

    public int getBeatAtBar(int bar) {
        TimeSignatureEvent ev = getTimeSignutureEventAtBar(bar);
        return ev.beat + ev.beatsPerBar * (bar - ev.bar);
    }

    public TimeSignatureEvent getTimeSignutureEventAtBar(int bar) {
        if (dirty) {
            reco();
        }
        int itick = bar + 1; //
        SortedMap<Integer, TimeSignatureEvent> head = eventByBar.headMap(itick);

        if (head.isEmpty()) {
            return null;
        }

        Integer lastKey = head.lastKey();
        TimeSignatureEvent ev = head.get(lastKey);
        return ev;
    }

    public int getBarContaining(int beat) {
        TimeSignatureEvent ev = getEventAtBeat(beat);
        return ev.bar + (beat - ev.beat) / ev.beatsPerBar;
    }
//	
//	public int getNearestBarTo(int beat) {
//		TimeSignatureEvent ev=getEventAtBeat(beat);
//		int barBefore= ev.bar+(beat-ev.beat)/ev.beatsPerBar;
//		
//		
//	}

    public TimeSignatureEvent getEventAtBeat(int beat) {
        if (dirty) {
            reco();
        }

        assert (beat >= 0);

        //	TimeSignatureEvent ret = null;
        //	TimeSignatureEvent next= null;

        Iterator<Map.Entry<Integer, TimeSignatureEvent>> iter = eventByBar.entrySet().iterator();

        TimeSignatureEvent ts1 = iter.next().getValue();  // Should always be one event
        if (!iter.hasNext()) {
            return ts1;
        }

        do {
            TimeSignatureEvent ts2 = iter.next().getValue();
            if (ts1.beat <= beat && ts2.beat > beat) {
                return ts1;
            }
            ts1 = ts2;
        } while (iter.hasNext());

        return ts1;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        reco();
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, IOException {
         in.defaultReadObject();
         eventByBar = new TreeMap<Integer, TimeSignatureEvent>();
         for (TimeSignatureEvent ev:list) {
             eventByBar.put(ev.bar,ev);
         }
         dirty=false;
     }

    void display() {
        reco();
        for (Map.Entry<Integer, TimeSignatureEvent> me : eventByBar.entrySet()) {
            System.out.println(me.getKey() + " @" + me.getValue().bar + "  * " + me.getValue().beat + " : " + " : " + me.getValue().beatsPerBar);
            // System.out.println(getTimeAt(me.getKey()+0.5));
        }
    }

    /**
     * Interface for an iterator
     *
     * @author pjl
     *
     */
    public interface QStepIterator {

        boolean hasNext();   // more

        void next();         // advance to next beat

        /**
         *
         * @return absolute number of beats
         */
        double getBeat();

        /**
         *
         * @return true if we are at a bar line
         */
        boolean isBar();

        /**
         *
         * @return bar which beat belongs
         */
        int getBar();
    };

    /**
     *
     * Iterates on bar between beat1 and beat2
     *
     * @author pjl
     *
     */
    public class QStepIteratorBar implements QStepIterator {

        public Iterator<Entry<Integer, TimeSignatureEvent>> tsIter;
        TimeSignatureEvent ts = null;
        TimeSignatureEvent tsNext = null;
        int beatNext = Integer.MAX_VALUE;
        int beatNow = Integer.MIN_VALUE;
        int beat2;
        int barNow;

        QStepIteratorBar(int beat1, int beat2) {
            if (dirty) {
                reco();
            }
            this.beat2 = beat2;
            tsIter = eventByBar.entrySet().iterator();
            ts = tsIter.next().getValue();

            while (tsIter.hasNext()) {
                tsNext = tsIter.next().getValue();
                if (tsNext.beat > beat1 + tol) {    // case when there is a event after beat1
                    beatNext = ts.beat + ((beat1 - ts.beat) / ts.beatsPerBar) * ts.beatsPerBar;
                    barNow = (ts.bar + ((beat1 - ts.beat) / ts.beatsPerBar)) - 1;
                    return;
                }
                ts = tsNext;
            }

            // Case when there is no event after beat1

            beatNext = ts.beat + ((beat1 - ts.beat) / ts.beatsPerBar) * ts.beatsPerBar;
            barNow = (ts.bar + ((beat1 - ts.beat) / ts.beatsPerBar)) - 1;
            tsNext = null;
        }

        public boolean hasNext() {
            return beatNext <= beat2;
        }

        public void next() {
            barNow++;
            beatNow = beatNext;
            beatNext = beatNow + ts.beatsPerBar;
            if (tsNext != null && beatNext == tsNext.beat) {
                ts = tsNext;
                if (tsIter.hasNext()) {
                    tsNext = tsIter.next().getValue();
                } else {
                    tsNext = null;
                }
            }
        }

        public double getBeat() {
            return beatNow;
        }

        public boolean isBar() {
            return true;
        }

        public int getBar() {
            return barNow;
        }
    }

    class QStepIteratorDef implements QStepIterator {

        public double beat;
        public boolean isBar;
        double step;
        int count;
        public Iterator<Entry<Integer, TimeSignatureEvent>> tsIter;
        TimeSignatureEvent ts;
        private TimeSignatureEvent tsNext;

        QStepIteratorDef(double beat1, double beat2, double step) {
            if (dirty) {
                reco();
            }
            tsIter = eventByBar.entrySet().iterator();
            beat = beat1 - step;
            this.step = step;
            count = (int) ((beat2 + tol - beat1) / step);
            while (tsIter.hasNext()) {
                ts = tsIter.next().getValue();
                if (ts.beat >= beat - tol) {
                    if (tsIter.hasNext()) {
                        tsNext = tsIter.next().getValue();
                    } else {
                        tsNext = null;
                    }
                    break;
                }
            }
        }

        public boolean hasNext() {
            return count >= 0;
        }

        public void next() {
            beat += step;
            isBar = Math.abs((beat + tol - ts.beat) % ts.beatsPerBar) < 2 * tol;
            count--;
            if (isBar && tsNext != null) {
                if (Math.abs(beat - tsNext.beat) < tol) {
                    ts = tsNext;
                    if (tsIter.hasNext()) {
                        tsNext = tsIter.next().getValue();
                    } else {
                        tsNext = null;
                    }
                }
            }

        }

        public double getBeat() {
            return beat;
        }

        public boolean isBar() {
            return isBar;
        }

        public int getBar() {
            return ts.bar + ((int) (beat - ts.beat)) / ts.beatsPerBar;
        }
    }

    /**
     *
     * Create an iterator between beat1 and beat2
     *
     * @param beat1  should be a multiple of step
     * @param beat2
     * @param step   should be a divisor of 1 OR negtive will step by whole bars
     *
     * @return
     */
    public QStepIterator createQStepIterator(double beat1, double beat2,
            double step) {
        if (step > 0) {
            return new QStepIteratorDef(beat1, beat2, step);
        } else {
            return new QStepIteratorBar((int) Math.ceil(beat1), (int) Math.floor(beat2));
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException, ClassNotFoundException {
        TimeSignatureList list = new TimeSignatureList();
        list.add(0, 4);
        list.add(2, 3);
        list.add(3, 5);

//		
//		int N = 1000;
//
//		int bar = 0;
//
//		for (int i = 0; i < N; i += 10) {
//			int nBeatPerBar = 2 + i % 3;
//			list.add(bar, nBeatPerBar);
//			bar += 3;
//		}
//		// list.display();
//
        double beat1 = 0;
        double beat2 = 20;
//		double step = 0.5;
        QStepIterator iter = list.createQStepIterator(beat1, beat2, 1);

        while (iter.hasNext()) {
            iter.next();
            System.out.print("@" + iter.getBeat());
            TimeSignatureEvent ev = list.getEventAtBeat((int) iter.getBeat());
            System.out.println(" Event =" + ev.beat + "  " + ev.bar + "  " + ev.beatsPerBar);
        }


        File tt = new File("/tmp/TS");

        OutputStream fout = new FileOutputStream(tt);

        ObjectOutputStream out = new ObjectOutputStream(fout);

        out.writeObject(list);

        out.close();

        InputStream fin = new FileInputStream(tt);

        ObjectInputStream in = new ObjectInputStream(fin);

        Object x=in.readObject();

        
        iter = ((TimeSignatureList)x).createQStepIterator(beat1, beat2, 1);

        while (iter.hasNext()) {
            iter.next();
            System.out.print("@" + iter.getBeat());
            TimeSignatureEvent ev = list.getEventAtBeat((int) iter.getBeat());
            System.out.println(" Event =" + ev.beat + "  " + ev.bar + "  " + ev.beatsPerBar);
        }




    }
}
