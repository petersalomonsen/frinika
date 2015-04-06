/*
 * Created on Jun 15, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

package com.frinika.tootX.plugins.analysis;

import com.frinika.tootX.plugins.*;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.spi.AudioServiceProvider;

public class AnalysisServiceProvider extends AudioServiceProvider {
	public AnalysisServiceProvider() {
		super(uk.org.toot.audio.id.ProviderId.FRINIKA_PROVIDER_ID,
				"Frinika Plugins", "Analysis", "0.1");
		addControls(AnalysisControls.class, Ids.ANALYSIS_MODULE,"Analysis", "Analysis", "0.1");
		add(AnalysisProcess.class, "Analysis", "Stuff", "0.1");
	}

	public AudioProcess createProcessor(AudioControls c) {
		if (c instanceof AnalysisProcessVariables) {
			return new AnalysisProcess((AnalysisProcessVariables) c);
		}
		return null; // caller then tries another provider
	}
}


