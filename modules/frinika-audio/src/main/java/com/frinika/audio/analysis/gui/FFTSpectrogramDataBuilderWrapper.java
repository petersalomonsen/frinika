/*
 * Created on 2 Jan 2008
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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
package com.frinika.audio.analysis.gui;

import com.frinika.audio.analysis.SpectrogramDataListener;
import com.frinika.audio.analysis.SpectrumController;
import com.frinika.audio.analysis.SpectrumDataBuilder;
import com.frinika.audio.analysis.StaticSpectrogramSynth;
import com.frinika.audio.analysis.dft.ChunkFeeder;
import com.frinika.audio.analysis.dft.FFTClient1;
import com.frinika.audio.analysis.dft.FFTSpectrogramControlable;
import com.frinika.audio.analysis.dft.FFTSpectrogramDataBuilder;
import com.frinika.audio.analysis.dft.FFTSpectrumController;
import com.frinika.audio.io.LimitedAudioReader;
import rasmus.interpreter.sampled.util.FFT;

public class FFTSpectrogramDataBuilderWrapper implements SpectrumDataBuilder, FFTSpectrogramControlable {

    ChunkFeeder feeder;
    FFTSpectrogramDataBuilder builder;
    FFTClient1 client;
    FFTSpectrumController controller;
//	double tOn;
//    double tOff;
    float fs;

    public FFTSpectrogramDataBuilderWrapper(LimitedAudioReader reader) {
        feeder = new ChunkFeeder();
        builder = new FFTSpectrogramDataBuilder();
        client = new FFTClient1(builder);
        controller = new FFTSpectrumController(this, reader);
        fs = (float) reader.getSampleRate();
    }

    @Override
    public void addSizeObserver(SpectrogramDataListener synth) {
        feeder.addSizeObserver(synth);
    }

    @Override
    public void dispose() {
        // TODO 
    }

    @Override
    public int getBinCount() {
        return builder.getBinCount();
    }

    @Override
    public int getChunkRenderedCount() {

        return client.getChunkRenderedCount();
    }

    @Override
    public float[] getFreqArray() {
        return builder.getFreqArray();
    }

    @Override
    public float[][] getMagnitude() {
        return client.getMagnitude();
    }

    @Override
    public float[] getMagnitudeAt(long chunkPtr) {
        return client.getMagnitudeAt(chunkPtr);
    }

    @Override
    public float[] getPhaseFreqAt(long chunkPtr) {
        return null;
    }

    @Override
    public int getSizeInChunks() {
        return client.getSizeInChunks();
    }

    @Override
    public boolean validAt(long chunkPtr) {
        return client.validAt(chunkPtr);
    }

    @Override
    public double getSampleRate() {
        // TODO Auto-generated method stub
        return fs;
    }

    @Override
    public void setParameters(int chunkSize, int fftsize, LimitedAudioReader reader) {
        feeder.setParameters(chunkSize, fftsize, reader, builder, client);

    }

    public SpectrumController getController() {
        // TODO Auto-generated method stub
        return controller;
    }

    @Override
    public StaticSpectrogramSynth getSynth() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FFT getFFT() {
        // TODO Auto-generated method stub
        return builder.getFFT();
    }

    public float[] getSMagnitudeAt(long chunkPtr) {
        return client.getSmagnitudeAt(chunkPtr);
    }

    public float[][] getSMagnitude() {
        return client.getSMagnitude();
    }
}
