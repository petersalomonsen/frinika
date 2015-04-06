package com.frinika.project;

/*
 * Created on Jun 30, 2005
 *
 * Copyright (c) 2005 - Lightminds AS (http://www.lightminds.com)
 * 
 * This file is part of LM|AppletServer.
 * 
 * LM|AppletServer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LM|AppletServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LM|AppletServer; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MidiDeviceDescriptorClass {
	Class value();
}
