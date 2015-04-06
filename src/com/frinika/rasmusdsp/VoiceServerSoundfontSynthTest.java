package com.frinika.rasmusdsp;

import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceServer;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;


public class VoiceServerSoundfontSynthTest {

	public static MidiDevice findRasmusDSP() throws MidiUnavailableException
	{
//	  1. Find RasmusDSP Synthesizer
//	  -----------------------------

	   MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
	   MidiDevice.Info info = null;
	   for (int i = 0; i < infos.length; i++) {
		   if(infos[i].getName().equals("RasmusDSP Synthesizer"))
			   info = infos[i];
	   }
	   
	   return MidiSystem.getMidiDevice(info);
	}

    /**
     * @param args
     */
    public static void testSoundFont(VoiceServer voiceServer,MidiDevice mididevice) throws Exception {

//               2. Open RasmusDSP Synthesizer
//               -----------------------------

            

//               3. Get TargetLine from Synthesizer (if needed and supported)
//                  Must be done before calling "mididevice.getReceiver()"
//               ------------------------------------------------------------

            
            final TargetDataLine dataline = (TargetDataLine)((Mixer)mididevice).getLine( new Line.Info(TargetDataLine.class));
          AudioFormat.Encoding PCM_FLOAT = new AudioFormat.Encoding("PCM_FLOAT");
          AudioFormat format = new AudioFormat(PCM_FLOAT, 44100, 32, 2, 4*2, 4*2*44100, true);

          dataline.open(format);
                  
          
          
              
            
//               6. Read some data from the audio stream if dataline != null
//               -----------------------------------------------------------

                System.out.println("PCM_FLOAT Encoding used!");
                voiceServer.addTransmitter(new Voice(){
                        
                        byte[] streamBuffer = null;
                        FloatBuffer floatBuffer = null;
                        
                        @Override
                        public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
                                if(streamBuffer==null || streamBuffer.length!=buffer.length*4)
                                {
                                        ByteBuffer bytebuffer = ByteBuffer.allocate(buffer.length*4);
                                        streamBuffer = bytebuffer.array();
                                        floatBuffer = bytebuffer.asFloatBuffer();
                                }                                               
                                dataline.read(streamBuffer, 0, (endBufferPos-startBufferPos)*4);                                            
                                floatBuffer.position(0);
                                floatBuffer.get(buffer, startBufferPos, endBufferPos - startBufferPos);                                                 
                        }});
                
                
    }
    
    public static void loadBank(MidiDevice mididevice) throws Exception
    {
    	Soundbank soundbank = MidiSystem.getSoundbank(new File("soundfonts/Club.SF2"));
        ((Synthesizer)mididevice).loadAllInstruments(soundbank);
    }
    
    public static void playMidi(MidiDevice mididevice) throws Exception
    {
//      5. Get MIDI Receiver from Synthesizer
//      -------------------------------------

   Receiver receiver = mididevice.getReceiver();

   // Set the patch
   ShortMessage shm = new ShortMessage();
   Instrument instr = ((Synthesizer)mididevice).getAvailableInstruments()[0];
   
   
   System.out.println(instr.getName());
shm.setMessage(ShortMessage.CONTROL_CHANGE, 9, 0, 1);
receiver.send(shm, -1);
shm.setMessage(ShortMessage.CONTROL_CHANGE, 9, 0x20,0);
receiver.send(shm, -1);                         
shm.setMessage(ShortMessage.PROGRAM_CHANGE ,9,instr.getPatch().getProgram(),0);
receiver.send(shm, -1);
                
            boolean evenBeat = true;
            
    /**
     * Play the beat
     */
    while(true)
            {
            System.out.println("Kick");
        // Kick
                    shm = new ShortMessage();
                    shm.setMessage(ShortMessage.NOTE_ON,9,36,100);
                    receiver.send(shm, -1);

                    
        // Hat
        shm = new ShortMessage();
        shm.setMessage(ShortMessage.NOTE_ON,9,42,100);
        receiver.send(shm, -1);


                    Thread.sleep(300);


        // Open Hat
        shm = new ShortMessage();
        shm.setMessage(ShortMessage.NOTE_ON,9,46,100);
        receiver.send(shm, -1);


        Thread.sleep(300);


        // Snare
        shm = new ShortMessage();
        shm.setMessage(ShortMessage.NOTE_ON,9,40,100);
        receiver.send(shm, -1);


        // Hat
        shm = new ShortMessage();
        shm.setMessage(ShortMessage.NOTE_ON,9,42,100);
        receiver.send(shm, -1);


        Thread.sleep(300);

        // Open Hat
        shm = new ShortMessage();
        shm.setMessage(ShortMessage.NOTE_ON,9,46,100);
        receiver.send(shm, -1);


        Thread.sleep(150);


        // On even beats put in a kick for variation
        if(evenBeat)
        {
            // Kick
                shm = new ShortMessage();
                shm.setMessage(ShortMessage.NOTE_ON,9,36,70);
                receiver.send(shm, -1);
                
                Thread.sleep(150);
 

            }else
            {
                Thread.sleep(150);      
            }
            evenBeat = !evenBeat;
            



                }
//               7. Free everythink after user
//               -----------------------------
            //if(dataline != null) dataline.close();
            //mididevice.close();

    }

    public static void main(String[] args) throws Exception
    {
    	MidiDevice mididevice = findRasmusDSP();
    	mididevice.open();
    	loadBank(mididevice);
    	testSoundFont(new AudioContext().getVoiceServer(),mididevice);
    	
    	playMidi(mididevice);
    }
}
