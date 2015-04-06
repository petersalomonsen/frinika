/*
 * Created on May 8, 2005
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.tools;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JProgressBar;

/**
 * Generic inputstream for updating a progressbar while reading. Use this between
 * an input stream reader and the inputstream to read.
 * @author Peter Johan Salomonsen
 *
 */
public class ProgressBarInputStream extends InputStream {

    JProgressBar progressBar;
    InputStream inputStream;
    
    /**
     * 
     * @param progressBar the progressbar to update
     * @param inputStream the inputstream to read
     */
    public ProgressBarInputStream(JProgressBar progressBar, InputStream inputStream)
    {
        this.progressBar = progressBar;
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        progressBar.setValue(progressBar.getMaximum()-inputStream.available());
        return(inputStream.read());
    }

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
        progressBar.setValue(progressBar.getMaximum()-inputStream.available());
		return inputStream.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
        progressBar.setValue(progressBar.getMaximum()-inputStream.available());
		return inputStream.read(b);
	}
    
    
}
