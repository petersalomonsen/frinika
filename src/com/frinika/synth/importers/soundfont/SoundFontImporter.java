/*
 * Created on Sep 24, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.importers.soundfont;
import java.io.*;

import com.frinika.synth.synths.MySampler;
import com.frinika.synth.synths.sampler.settings.sampledsoundsettingversions.SampledSound20050403;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SoundFontImporter {
	FileInputStream fis;
	long size;
	
	public short globalRelease;
	
	public File file;
	INSTChunk inst;
	IBAGChunk ibag;
	IGENChunk igen;
	IMODChunk imod;
	SHDRChunk shdr;
	Chunk chSmpl;
	
	int instrumentIndex;
	
	MySampler sampler;
	
	public SoundFontImporter(MySampler sampler)
	{
		this.sampler = sampler;
	}

	public void getSoundFont(File file) throws Exception
	{
		this.file = file;

        fis = new FileInputStream(file);
		chSmpl = new Chunk(fis,"smpl");
		fis.skip(chSmpl.length);
	
		new PHDRChunk(fis);
		inst = new INSTChunk(fis);
		ibag = new IBAGChunk(fis);
		imod = new IMODChunk(fis);
		igen = new IGENChunk(fis);
		shdr = new SHDRChunk(fis);
		
		fis.close();
	}
	
	public String[] getInstrumentNames()
	{
		return(inst.names);
	}
	
	public void getInstrument(int InstrNo) throws Exception
	{
		instrumentIndex = InstrNo;
		sampler.setInstrumentName(inst.names[InstrNo]);
		int ibagIndex = inst.instBagNdx[InstrNo];
		int nextIbagIndex = inst.instBagNdx[InstrNo+1];
		//System.out.println("IBAG index: "+ibagIndex);
		//System.out.println("Next IBAG index: "+nextIbagIndex);
		
		for(int z = ibagIndex;z<nextIbagIndex;z++)
		{
			//System.out.println("Zone: "+z);

			int instGenNdx =ibag.instGenNdx[z]; 
			int nextInstGenNdx =ibag.instGenNdx[z+1]; 
			
			//System.out.println("Gen index: "+instGenNdx);
			//System.out.println("Next gen index: "+nextInstGenNdx);

			int hiKey = 199;
			int loKey = 0;
			int hiVel = 127;
			int loVel = 0;
			int rootKey = -1;
			int sampleRate = 0;
			int loopStart = 0;
			int loopEnd = 0;
			int sampleMode = 0;
			int fineTune = 0;
            int scaleTune = 100;
            int exclusiveClass = 0;
			String sampleName = null;
			
			short[] sampleData = null;
			int sampleType = 0;
			
			for(int n = instGenNdx;n<nextInstGenNdx;n++)
			{
				if(igen.sfGenOper[n]==38)
				{
					globalRelease = (short)igen.genAmount[n];
				}
				if(igen.sfGenOper[n]==43)
				{
					//System.out.println(n+" sfGenOper: "+igen.sfGenOper[n]);
					//System.out.println(n+" genAmount: "+Integer.toHexString(igen.genAmount[n]));
					//System.out.println(n+" hi key: "+(igen.genAmount[n] >> 8));
					//System.out.println(n+" lo key: "+(igen.genAmount[n] & 0xff));
					hiKey = igen.genAmount[n] >> 8;
					loKey = igen.genAmount[n] & 0xff;
				}
				if(igen.sfGenOper[n]==44)
				{
					//System.out.println(n+" sfGenOper: "+igen.sfGenOper[n]);
					//System.out.println(n+" genAmount: "+Integer.toHexString(igen.genAmount[n]));
					//System.out.println(n+" hi vel: "+(igen.genAmount[n] >> 8));
					//System.out.println(n+" lo vel: "+(igen.genAmount[n] & 0xff));
					hiVel = igen.genAmount[n] >> 8;
					loVel = igen.genAmount[n] & 0xff;
				}
                if(igen.sfGenOper[n]==56)
                {
                    //System.out.println("scale tune: "+igen.genAmount[n]);
                    scaleTune = igen.genAmount[n];
                }
                if(igen.sfGenOper[n]==57)
                {
                    System.out.println("exclusive: "+igen.genAmount[n]);
                    exclusiveClass = igen.genAmount[n];
                }
                if(igen.sfGenOper[n]==58)
				{
					//System.out.println("root key: "+igen.genAmount[n]);
					rootKey = igen.genAmount[n];
				}
				if(igen.sfGenOper[n]==54)
				{
					sampleMode = igen.genAmount[n];
				}
				if(igen.sfGenOper[n]==52)
				{
					fineTune = (short)igen.genAmount[n];
				}

				if(igen.sfGenOper[n]==53)
				{
					//System.out.println(n+" sfGenOper: "+igen.sfGenOper[n]);
					//System.out.println(n+" genAmount: "+Integer.toHexString(igen.genAmount[n]));
//					System.out.println(n+" SampleName: "+shdr.names[igen.genAmount[n]]);
					sampleRate = shdr.sampleRate[igen.genAmount[n]];
					sampleType = shdr.sfSampleType[igen.genAmount[n]];
					loopStart = shdr.sampleStartLoop[igen.genAmount[n]]-shdr.sampleStart[igen.genAmount[n]];
					loopEnd = shdr.sampleEndLoop[igen.genAmount[n]]-shdr.sampleStart[igen.genAmount[n]];					

					if(rootKey == -1)
						rootKey = shdr.originalPitch[igen.genAmount[n]];
					if(shdr.samples[igen.genAmount[n]] == null)
					{
						FileInputStream fis = new FileInputStream(file);
						chSmpl = new Chunk(fis,"smpl");
						sampleData = new short[shdr.sampleEnd[igen.genAmount[n]]
											   -shdr.sampleStart[igen.genAmount[n]]];
						fis.skip(shdr.sampleStart[igen.genAmount[n]]*2);
						byte bSample[] = new byte[sampleData.length*2];
				
						fis.read(bSample);
				
						for(int sCount=0;sCount<bSample.length;sCount+=2)
							sampleData[sCount/2] = (short)((0xff & bSample[sCount+0]) + ((0xff & bSample[sCount+1]) * 256));
						fis.close();
				
						shdr.samples[igen.genAmount[n]] = sampleData;
					}
					else
						 sampleData = shdr.samples[igen.genAmount[n]];
					
					sampleName = shdr.names[igen.genAmount[n]];
				}
			}
			
			/*
			 * monoSample = 1, rightSample = 2, leftSample = 4, linkedSample = 8, RomMonoSample = 32769, RomRightSample = 32770
			 * At this moment only support for type sampleType 1,2,4
			 */
			if(sampleData!=null && 
					(sampleType==1 || 
					 sampleType==2 ||
					 sampleType==4))
			{
				for(int n = loKey;n<=hiKey; n++)
				{
					for(int v = loVel;v<=hiVel;v++)
					{
						if(sampler.sampledSounds[n][v]==null)
							sampler.insertSample(new SampledSound20050403(),n,v);
						if(sampleType==1)
						{
							sampler.sampledSounds[n][v].setLeftSamples(sampleData);
							sampler.sampledSounds[n][v].setRightSamples(sampleData);
						}
						if(sampleType==2)
						{
							sampler.sampledSounds[n][v].setRightSamples(sampleData);	
						}
						if(sampleType==4)
						{
							sampler.sampledSounds[n][v].setLeftSamples(sampleData);	
						}
					
						sampler.sampledSounds[n][v].setSampleMode(sampleMode);
						sampler.sampledSounds[n][v].setLoopStart(loopStart);
						sampler.sampledSounds[n][v].setLoopEnd(loopEnd);
						sampler.sampledSounds[n][v].setRootKey(rootKey);
						sampler.sampledSounds[n][v].setFineTune(fineTune);
                        sampler.sampledSounds[n][v].setScaleTune(scaleTune);
						sampler.sampledSounds[n][v].setSampleRate(sampleRate);
						sampler.sampledSounds[n][v].setRelease(globalRelease);
						sampler.sampledSounds[n][v].setSampleName(sampleName);
                        sampler.sampledSounds[n][v].setExclusiveClass(exclusiveClass);
					}
				}
			}
		}		

	}

	public int getInstrumentIndex()
	{
		return(instrumentIndex);
	}
    
	/**
	 * 
	 */
	public void showGUI() {
		new SoundFontImporterGUI(this);
	}	
}
