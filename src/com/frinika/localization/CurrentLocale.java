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
package com.frinika.localization;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Global class for retrieving the Locale (Language translation) to be used globally
 * by Frinika. This will be configurable from a resource bundle - coming soon.
 * 
 * @author Peter Johan Salomonsen
 */
public class CurrentLocale {
    static ResourceBundle messages = ResourceBundle.getBundle("messages",Locale.getDefault()); 
    static
    {
    	System.out.println("Using language: "+Locale.getDefault().getDisplayLanguage());
    }
    
    public static final String getMessage(String key)
    {
        try
        {
            return messages.getString(key);
        }
        catch(Exception e)
        {
            return key;
        }
    }
    
    /**
     * Test locale
     * @param args
     */
    public static void main(String[] args)
    {
    	System.out.println("Using language: "+Locale.getDefault().getDisplayLanguage());
    }
}
