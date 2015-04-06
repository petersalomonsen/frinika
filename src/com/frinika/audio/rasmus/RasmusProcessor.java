package com.frinika.audio.rasmus;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.frinika.audio.io.AudioWriter;

import rasmus.interpreter.Interpreter;
import rasmus.interpreter.Variable;
import rasmus.interpreter.parser.ScriptParserException;
import rasmus.interpreter.sampled.AudioSession;

public class RasmusProcessor {

    public static void main(String args[]) throws Exception {

        File inputfile = new File("/home/pjl/massive.wav");
        File outputfile = new File("/home/pjl/output_wav_file.wav");
        String reverbfile = "/home/pjl/frinika/impulses/Deep Space.wav";
        File fil = new File(reverbfile);
        assert (fil.exists());
        String script = "output <- resample(2) <- file(input);";

//      String script = "output <- fftconvolution( File(\"" + reverbfile + "\") ) <- file(input);";
//	JFrame frame=new JFrame();
//	frame.setSize(800,500);
//	rasmus.editor.ExpressionEditor.editScript(frame,script);

        System.out.println(script);
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        process(inputfile, outputfile, format, script);

    }

    static void process(File inputfile, File outputfile, AudioFormat format, String script) throws Exception {

        if (!inputfile.exists()) {
            throw new Exception(" File not found " + inputfile);
        }


        try {
            AudioWriter audioWriter = new AudioWriter(outputfile, format);

            Interpreter interpreter = new Interpreter();
            interpreter.add("input", inputfile.getPath());

            interpreter.eval(script);

            // You don't have to call interpreter.commit
            // because autocommit is by default true
            Variable output = interpreter.get("output");

            AudioSession audiosession = new AudioSession(
                    format.getSampleRate(), format.getChannels());
            AudioInputStream audiostream = audiosession.asByteStream(output,
                    format);


            //
            long bytes_to_render = inputfile.length();

            byte[] buffer = new byte[512];

            long writeout = 0;
            while (writeout < bytes_to_render) {
                int ret = -1;
                ret = audiostream.read(buffer);

                if (ret == -1) {
                    System.out.println(" ret == -1 ");
                    break;
                }
                writeout += ret;
                //          System.out.println(" rendered : " + writeout);
                audioWriter.write(buffer, 0, ret);

            }
            audioWriter.close();
            audiostream.close();
            audiosession.close();
            interpreter.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ScriptParserException e1) {
            e1.printStackTrace();
        }
    }
}
