/*
 * Created on Feb 14, 2006
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
package uk.org.toot.misc;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Global class for retrieving the Locale (Language translation) to be used globally
 * by Toot.
 * 
 * @author Peter Johan Salomonsen
 * @author Steve Taylor
 */
public class Localisation
{
    static ResourceBundle strings = null;

    static {
        try {
	        strings = ResourceBundle.getBundle("TootLocalisation", Locale.getDefault());
    		System.out.println("Toot using language: "+Locale.getDefault().getDisplayLanguage());
        } catch ( Exception e ) {
            System.err.println("TootLocalisation properties not found");
            e.printStackTrace();
        }
    }

    public static final String getString(String key) {
        if ( strings == null ) return key;
        try {
            return strings.getString(key);
        } catch(Exception e) {
            return key.replace('.', ' ');
        }
    }
}