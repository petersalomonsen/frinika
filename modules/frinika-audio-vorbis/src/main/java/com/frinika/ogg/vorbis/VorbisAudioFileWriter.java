/*
 * Created on Nov 26, 2010
 *
 * Copyright (c) 2010 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.ogg.vorbis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileWriter;
import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.vorbis_block;
import org.xiph.libvorbis.vorbis_comment;
import org.xiph.libvorbis.vorbis_dsp_state;
import org.xiph.libvorbis.vorbis_info;
import org.xiph.libvorbis.vorbisenc;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class VorbisAudioFileWriter extends AudioFileWriter {

    Type[] types = new Type[]{
        new Type("OGG", "ogg")
    };

    @Override
    public Type[] getAudioFileTypes() {
        return types;
    }

    @Override
    public Type[] getAudioFileTypes(AudioInputStream stream) {
        return types;
    }

    @Override
    public int write(AudioInputStream origStream, Type fileType, final OutputStream realOutput) throws IOException {
        final int writeCount[] = {0};
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                realOutput.write(b);
                writeCount[0]++;
            }
        };

        if (!isFileTypeSupported(fileType)) {
            throw new IllegalArgumentException("File type is not supported");
        }

        AudioFormat audioFormat = origStream.getFormat();
        AudioFormat supportedFormat = new AudioFormat(audioFormat.getSampleRate(), 16, 2, true, false);
        AudioInputStream stream;
        if (audioFormat.matches(supportedFormat)) {
            stream = origStream;
        } else {
            stream = AudioSystem.getAudioInputStream(supportedFormat, origStream);
        }

        vorbisenc encoder;
        ogg_stream_state os;	// take physical pages, weld into a logical stream of packets

        ogg_page og;	// one Ogg bitstream page.  Vorbis packets are inside
        ogg_packet op;	// one raw packet of data for decode

        vorbis_info vi;	// struct that stores all the static vorbis bitstream settings

        vorbis_comment vc;	// struct that stores all the user comments

        vorbis_dsp_state vd;	// central working state for the packet->PCM decoder
        vorbis_block vb;	// local working space for packet->PCM decode

        int READ = 1024;
        byte[] readbuffer = new byte[READ * 4 + 44];

        int page_count = 0;
        int block_count = 0;

        boolean eos = false;

        vi = new vorbis_info();

        encoder = new vorbisenc();

        Float qualityProperty = (Float) audioFormat.getProperty("quality");
        if (qualityProperty == null) {
            qualityProperty = .3f;
        }

        if (!encoder.vorbis_encode_init_vbr(vi, audioFormat.getChannels(),
                 (int) audioFormat.getSampleRate(),
                qualityProperty)) {
            throw new IOException("Failed to Initialize vorbisenc");
        }

        vc = new vorbis_comment();
        vc.vorbis_comment_add_tag("ENCODER", "Java Vorbis Encoder");

        vd = new vorbis_dsp_state();

        if (!vd.vorbis_analysis_init(vi)) {
            throw new IOException("Failed to Initialize vorbis_dsp_state");
        }

        vb = new vorbis_block(vd);

        java.util.Random generator = new java.util.Random();  // need to randomize seed
        os = new ogg_stream_state(generator.nextInt(256));

        //System.out.print( "Writing header." );
        ogg_packet header = new ogg_packet();
        ogg_packet header_comm = new ogg_packet();
        ogg_packet header_code = new ogg_packet();

        vd.vorbis_analysis_headerout(vc, header, header_comm, header_code);

        os.ogg_stream_packetin(header); // automatically placed in its own page
        os.ogg_stream_packetin(header_comm);
        os.ogg_stream_packetin(header_code);

        og = new ogg_page();
        op = new ogg_packet();

        while (!eos) {
            if (!os.ogg_stream_flush(og)) {
                break;
            }

            out.write(og.header, 0, og.header_len);
            out.write(og.body, 0, og.body_len);
            //System.out.print( "." );
        }
        //System.out.print(  "Done writing header.\n" );

        //System.out.print( "Encoding." );
        while (!eos) {

            int i;
            int bytes = stream.read(readbuffer, 0, READ * 4); // stereo hardwired here

            int break_count = 0;

            if (bytes == 0) {

                // end of file.  this can be done implicitly in the mainline,
                // but it's easier to see here in non-clever fashion.
                // Tell the library we're at end of stream so that it can handle
                // the last frame and mark end of stream in the output properly
                vd.vorbis_analysis_wrote(0);

            } else {

                // data to encode
                // expose the buffer to submit data
                float[][] buffer = vd.vorbis_analysis_buffer(READ);

                // uninterleave samples
                for (i = 0; i < bytes / 4; i++) {
                    buffer[0][vd.pcm_current + i] = ((readbuffer[i * 4 + 1] << 8) | (0x00ff & (int) readbuffer[i * 4])) / 32768.f;
                    buffer[1][vd.pcm_current + i] = ((readbuffer[i * 4 + 3] << 8) | (0x00ff & (int) readbuffer[i * 4 + 2])) / 32768.f;
                }

                // tell the library how much we actually submitted
                vd.vorbis_analysis_wrote(i);
            }

            // vorbis does some data preanalysis, then divvies up blocks for more involved
            // (potentially parallel) processing.  Get a single block for encoding now
            while (vb.vorbis_analysis_blockout(vd)) {

                // analysis, assume we want to use bitrate management
                vb.vorbis_analysis(null);
                vb.vorbis_bitrate_addblock();

                while (vd.vorbis_bitrate_flushpacket(op)) {

                    // weld the packet into the bitstream
                    os.ogg_stream_packetin(op);

                    // write out pages (if any)
                    while (!eos) {

                        if (!os.ogg_stream_pageout(og)) {
                            break_count++;
                            break;
                        }

                        out.write(og.header, 0, og.header_len);
                        out.write(og.body, 0, og.body_len);

                        // this could be set above, but for illustrative purposes, I do
                        // it here (to show that vorbis does know where the stream ends)
                        if (og.ogg_page_eos() > 0) {
                            eos = true;
                        }
                    }
                }
            }
            //System.out.print( "." );
        }

        //System.out.print( "Done encoding.\n" );
        return writeCount[0];
    }

    @Override
    public int write(AudioInputStream stream, Type fileType, File out) throws IOException {
        return write(stream, fileType, new FileOutputStream(out));
    }
}
