/** 
 * Copyright (c) 2005 - Bob Lang (http://www.cems.uwe.ac.uk/~lrlang/)
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

package com.frinika.contrib.boblang;
//package uwejava;
/**
  Text conversion and formatting methods.  Each method is static.

  <p>The methods are divided into two groups:<ul>
     <li>Methods where numeric values are converted and formatted into 
         strings</li>
     <li>Methods where a string converted into a numeric value</li>
  </ul></p>
    
  <p>Methods for converting and formatting values into strings are:<ul>
  <li>Real formatting:<ul>
      <li>doubleToString (double d, int width, int decimal)</li>
      <li>floatToString  (float  f, int width, int decimal)</li>
      </ul>
      <p>where:<ul>
      	<li>width   = total field width in characters<br>
	              (Minimum allowed width is 4)</li>
	<li>decimal = number of digits after decimal point<br> 
	      	  (uses exponential notation if decimal is -ve)</li>

      	<p>If the number is too large to print in ddd.dd format, it will
	be output in exponential form.</p>
	
	<p>If the total field width is too small, then the width is 
	automatically increased</p>
      </ul>

    <li>Integer formatting:<ul>
      <li>longToString  (long l,  int width)</li>
      <li>intToString   (int i,   int width)</li>
      <li>shortToString (short s, int width)</li>
      </ul>
      <p>where: <ul>
        <li>width   = total field width in characters<br>
	  +ve width - use leading spaces<br>
	  -ve width - use leading zeroes<br>
	
	<p>If the total field width is too small, then the width is 
	automatically increased.  To format a value in the minimum
	number of characters, use a width of +1.</p></li>
      </ul>
</ul>

  <p>Methods for converting strings to values are:<ul>
    <li>Validity checks:<ul>
      <li>boolean isValidDouble (String s)</li>
      <li>boolean isValidFloat (String s)</li>
      <li>boolean isValidLong (String s)</li>
      <li>boolean isValidInt (String s)</li>
      <li>boolean isValidShort (String s)</li>
    </ul></li>
    
    <li>Conversion methods:<ul>
      <li>double StringToDouble (String s, double defaultValue)</li>
      <li>float StringToFloat (String s, float defaultValue)</li>
      <li>long StringToLong (String s, long defaultValue)</li>
      <li>int StringToInt (String s, int defaultValue)</li>
      <li>short StringToShort (String s, short defaultValue)</li>
      </ul>
    <p>where:<ul>
    <li>defaultValue - value returned if the string is invalid</li>
    </ul>
    </ul></li>
  </ul></p>

  @author  Bob Lang
  @version Thu Nov 15 2001
  @see     TextInput
  @see     TextOutput
*/
public class Convert {
  // ... PRIVATE DECLARATIONS ...
  
  // Version for this class
  private static final String 
    VERSION_STRING = "uwejava.Convert version dated Thu Nov 15 2001";
  
  // Conversion table for integer strings
  private static final char [] DIGIT_CHAR 
    = {'0','1','2','3','4','5','6','7','8','9',' ','-'};
  private static final int MARK_LEAD_SPACE = 10;
  private static final int MARK_MINUS = 11;
  
  // Standard width specifications
  private static final int MAX_WIDTH = 24;	   //Max numeric width 
  private static final int MIN_EXP_WIDTH = 10;	   //Min width for exp format
  private static final int MIN_DECIMAL_WIDTH = 4;  //Min width for dd.dd form
  private static final int DOUBLE_EXP_WIDTH = 3;   //double numbers use e+ddd
  private static final int FLOAT_EXP_WIDTH = 2;    //float numbers use e+dd

  // Largest number that can be printed in the form ddddd.dddd
  private static final double MAX_DECIMAL_VALUE = 9e18;  //From long type
  private static final int MAX_DECIMAL_DIGITS = 18;
  
  // Power lookup table to avoid calling the Math library
  private static double [] powerTable =
    {1.0,  1e1,  1e2,  1e3,  1e4,  1e5,  1e6,  1e7,  1e8,  1e9,
     1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19,
     1e20, 1e21, 1e22, 1e23, 1e24, 1e25, 1e26, 1e27, 1e28, 1e29};
  
  // ... PRIVATE METHODS ...

  // Convert a double real value to exponent string in form dd.dddde+ddd
  private static String convDoubleExp 
    (double d, int width, int decimal, int exp) 
  {
    String buffer;		// Build up output representation
    boolean negative;		// Processing a negative number
    
    int extraChars = 4;		// Sign, ".", "e" and Exponent sign 
    int powerOfTen = 0;		// Printed exponent part

    int pre;			// Number of digits before decimal point
    
    double minNorm, maxNorm;	// Used to put 1..9 in first digit position
    double round;		// Used for rounding last sig digit
    
    long preDigits;		// Int value of normalised value
    long postDigits;		// Int value of digits after decimal point
    
    // Can't handle negative numbers too well
    negative = d < 0.0;
    d = Math.abs (d);
    
    // Check the input formatting specifications
    
    // The exponent width is limited to 2 or 3 characters
    if (exp < 2) {
      exp = 2;
    }
    if (exp > 3) {
      exp = 3;
    }
    
    // Check the width and possibly adjust decimal
    if (width < MIN_EXP_WIDTH) {
      decimal = decimal + MIN_EXP_WIDTH - width;
      width = MIN_EXP_WIDTH;
    } //if
    if (width > MAX_WIDTH) {
      decimal = decimal - MAX_WIDTH + width;
      width = MAX_WIDTH;
    } //if
    
    // Check the number of decimal digits
    if (decimal > width - exp - extraChars - 1) {
      decimal = width - exp - extraChars - 1;
    } //if
    if (decimal <= 0) {
      decimal = 0;
      extraChars = 3;	// No decimal point so one less extra char
    } //if
    
    // Can't have more than 18 digits before decimal point
    pre = width - decimal - exp - extraChars;
    if (pre >= MAX_DECIMAL_VALUE) {
      pre = MAX_DECIMAL_DIGITS;
    } //if

    // Normalise the number so 1..9 will appear as first printed digit
    maxNorm = powerTable [pre];
    minNorm = maxNorm / 10.0;
    
    // Perform normalisation and calculate power of ten to go after 'e'
    while (d > maxNorm) { 
      d = d / 10.0;
      powerOfTen++;
    } // while
    while (d < minNorm) { 
      d = d * 10.0;
      powerOfTen--;
    } // while
    
    // Round up or down the decimal digits and re-normalise
    round = 0.5 / powerTable [decimal];
    d = d + round;
    while (d > maxNorm) { 
      d = d / 10.0;
      powerOfTen++;
    } // while

    // Get two long numbers representing numbers before and after decimal.
    preDigits = (long) d;		// Lose the decimal part
    postDigits = (long) ((d-preDigits) * powerTable [decimal]);
    
    // Build up the string buffer
    if (negative) {
      buffer = "-";
    }
    else {
      buffer = " ";
    } //if
    buffer = buffer + convLongLS (preDigits, pre);
    if (decimal != 0) {
      buffer += '.';
      buffer += convLongLZ (postDigits, decimal);
    } //if
    if (powerOfTen < 0) {
      buffer += "e-";
      powerOfTen = -powerOfTen;
    }
    else {
      buffer += "e+";
    } //if
    buffer += convLongLZ (powerOfTen, exp);

    // Return the final result
    return buffer;
  } // convDoubleExp ()
  
  // Convert a double real value to string in form dddd.dd
  private static String convDouble (double d, int width, int decimal) {
    String buffer;		// Build up output representation
    boolean negative;		// Processing a negative number
    
    int pre;			// Number of digits before decimal point
    double round;		// Used for rounding last sig digit
    
    long preDigits;		// Int value of normalised value
    long postDigits;		// Int value of digits after decimal point
    
    // Can't handle negative numbers too well
    negative = d < 0.0;
    d = Math.abs (d);
    
    // Check the input formatting specifications
    
    // Check the number of decimal digits
    if (decimal > width - 1) {
      decimal = width - 1;
    } //if
    if (decimal <= 0) {
      decimal = 0;
    } //if
    
    // Check the width and possibly adjust decimal
    if (width < MIN_DECIMAL_WIDTH) {
      //decimal = decimal + MIN_DECIMAL_WIDTH - width;
      width = MIN_DECIMAL_WIDTH;
    } //if
    if (width > MAX_WIDTH) {
      decimal = decimal - MAX_WIDTH + width;
      width = MAX_WIDTH;
    } //if
    
    // Calculate number of digits before decimal point
    pre = width - decimal - 1;
    if (decimal == 0) {
      pre++;		//No decimal point so we'll get an extra digit
    } //if
    
    // Round up or down the decimal digits
    round = 0.5 / powerTable [decimal];
    d = d + round;
    
    // Get two long numbers representing numbers before and after decimal.
    preDigits = (long) d;		// Lose the decimal part
    postDigits = (long) ((d-preDigits) * powerTable [decimal]);
    
    // Build up the string buffer
    if (negative) {
      preDigits = -preDigits;
    } //if
    buffer = convLongLS (preDigits, pre);
    if (decimal != 0) {
      buffer += '.';
      buffer += convLongLZ (postDigits, decimal);
    } //if

    // Return the final result
    return buffer;
  } // convDouble ()
  
  // Convert a long integer to a string (with leading spaces)
  private static String convLongLS (long l, int width) {
    String buffer;		// Build up output representation
    boolean negative;		// Processing a negative number
    long [] digits = 		// Digits discovered by successive mod 10
      new long [MAX_WIDTH];
    int i;			// Loop counter
    int digitValue;		// Value of each digit to be converted

    // Can't handle negative numbers
    negative = l < 0;
    if (negative) {
      l = -l;
    } //if
    
    // Check the input formatting specifications
    if (width > MAX_WIDTH) {
      width = MAX_WIDTH;
    } //if
    
    // Fill the digit array with marker values
    for (i=0; i<MAX_WIDTH; i++) {
      digits [i] = MARK_LEAD_SPACE;
    } // for
    
    // Extract the digits from the long number
    i = 0;
    digits [i] = l % 10;
    while (l > 0) {
      digits [i] = l % 10;
      l = l / 10;
      i++;
    } //while
    
    // Mark the sign position
    if (negative) {
      digits [i] = MARK_MINUS;
      i++;
    }
    
    // Catch the correct number of leading zeroes
    if (width > i) {
      i = width;
    } //if
    
    // Build up the return string
    buffer = "";
    i--;
    while (i >= 0) {
      digitValue = (int) digits [i];
      buffer += DIGIT_CHAR [digitValue];
      i--;
    } // while
    
    // Return final result
    return buffer;
  } // convLongLS ()
  
  // Convert a long integer to a string (with leading zeroes)
  private static String convLongLZ (long l, int width) {
    String buffer;		// Build up output representation
    boolean negative;		// Processing a negative number
    long [] digits = 		// Digits discovered by successive mod 10
      new long [MAX_WIDTH];
    int i;			// Loop counter
    int digitValue;		// Value of each digit to be converted

    //Create an empty buffer
    buffer = "";

    // Can't handle negative numbers
    negative = l < 0;
    if (negative) {
      //Convert the number to positive
      l = Math.abs (l);
      
      //Put the sign in the buffer and reduce width by one to compensate
      buffer = "-";
      width--;		//Doesn't matter if this goes to 0 or -ve
    } //if
    
    // Check the input formatting specifications
    if (width > MAX_WIDTH) {
      width = MAX_WIDTH;
    } //if
    
    // Fill the digit array with leading zeroes
    for (i=0; i<MAX_WIDTH; i++) {
      digits [i] = 0;
    } // for
    
    // Extract the digits from the long number
    i = 0;
    digits [i] = l % 10;
    while (l > 0) {
      digits [i] = l % 10;
      l = l / 10;
      i++;
    } //while
    
    // Catch the correct number of leading zeroes
    if (width > i) {
      i = width;
    } //if
    
    // Build up the return string
    i--;
    while (i >= 0) {
      digitValue = (int) digits [i];
      buffer += DIGIT_CHAR [digitValue];
      i--;
    } // while
    
    // Return final result
    return buffer;
  } // convLongLZ ()

  // ... DUMMY CONSTRUCTOR FOR JAVADOC PURPOSES ...
  
  /**
    This class only contains <code>static</code> methods.  There is no
    need to call its constructor.
  */
  public Convert () {
    // Do nothing
  } // Convert ()
  
  // ... PUBLIC STATIC CONVERSION ROUTINES ...
  
  /**
    Return the version of this class as a String
  */
  public static String version () {
    return VERSION_STRING;
  } // version ()
  
  /**
    Convert a double to a string.  
    See documentation at start of class for parameter usage.
    <P>Example call:</P>
    <P><CODE>String s = Convert.doubleToString (doubleValue, width, decimal);
    </CODE></P>
  */
  public static String doubleToString (double d, int width, int decimal) {
    if (decimal<0 || (Math.abs (d) > MAX_DECIMAL_VALUE)) {
      return convDoubleExp (d, width, Math.abs (decimal), DOUBLE_EXP_WIDTH);
    }
    else {
      return convDouble (d, width, decimal);
    } //if
  } // doubleToString ()
  
  /**
    Convert a float to a string.
    See documentation at start of class for parameter usage.
    <P>Example call:</P>
    <P><CODE>String s = Convert.floatToString (floatValue, width, decimal);
    </CODE></P>
  */
  public static String floatToString (float f, int width, int decimal) {
    if (decimal<0 || (Math.abs (f) > MAX_DECIMAL_VALUE)) {
      return convDoubleExp (f, width, Math.abs (decimal), FLOAT_EXP_WIDTH);
    }
    else {
      return convDouble (f, width, decimal);
    } //if
  } // floatToString ()
  
  /**
    Convert a long integer to a string.
    See documentation at start of class for parameter usage.
    <P>Example call:</P>
    <P><CODE>String s = Convert.longToString (longValue, width);
    </CODE></P>
  */
  public static String longToString (long l, int width) {
    if (width < 0) {
      return convLongLZ (l, Math.abs (width));
    }
    else {
      return convLongLS (l, width);
    } //if
  } // longToString ()
  
  /**
    Convert a standard integer to a string.
    See documentation at start of class for parameter usage.
    <P>Example call:</P>
    <P><CODE>String s = Convert.intToString (intValue, width);
    </CODE></P>
  */
  public static String intToString (int i, int width) {
    if (width < 0) {
      return convLongLZ (i, Math.abs (width));
    }
    else {
      return convLongLS (i, width);
    } //if
  } // intToString ()
  
  /**
    Convert a short integer to a string.
    See documentation at start of class for parameter usage.
    <P>Example call:</P>
    <P><CODE>String s = Convert.shortToString (shortValue, width);
    </CODE></P>
  */
  public static String shortToString (short s, int width) {
    if (width < 0) {
      return convLongLZ (s, Math.abs (width));
    }
    else {
      return convLongLS (s, width);
    } //if
  } // shortToString ()
  
  /**
    Test if the characters in a string may be converted into a double value.
    <P>Example call:</P>
    <P><CODE>boolean b = Convert.isValidDouble (digitString);
    </CODE></P>
  */
  public static boolean isValidDouble (String s) {
    // Declare variables
    Double d;
    boolean valid = false;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Double (s);
      valid = true;
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      valid = false;
    } // catch
    
    // Return the result
    //finally {
      return valid;
    //} // finally
  } // isValidDouble ()
  
  /**
    Convert a string to a double.  If the string is not a valid double
    value then the default value is returned.
    The string may be checked for validity by calling isValidDouble ()
    <P>Example call:</P>
    <P><CODE>double val = Convert.stringToDouble (digitString, defaultValue);
    </CODE></P>
  */
  public static double stringToDouble (String s, double defaultValue) {
    // Declare variables
    Double d;
    double result = defaultValue;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Double (s);
      result = d.doubleValue ();
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      result = defaultValue;
    } // catch
    
    // Return the result
    //finally {
      return result;
    //} // finally
  } // StringToDouble ()
  
  /**
    Test if the characters in a string may be converted into a float value.
    <P>Example call:</P>
    <P><CODE>boolean b = Convert.isValidFloat (digitString);
    </CODE></P>
  */
  public static boolean isValidFloat (String s) {
    // Declare variables
    Float d;
    boolean valid = false;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Float (s);
      valid = true;
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      valid = false;
    } // catch
    
    // Return the result
    //finally {
      return valid;
    //} // finally
  } // isValidFloat ()
  
  /**
    Convert a string to a float.  If the string is not a valid float
    value then the default value is returned.
    The string may be checked for validity by calling isValidFloat ()
    <P>Example call:</P>
    <P><CODE>float val = Convert.stringToFloat (digitString, (float) defaultValue);
    </CODE></P>
  */
  public static float stringToFloat (String s, float defaultValue) {
    // Declare variables
    Float d;
    float result = defaultValue;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Float (s);
      result = d.floatValue ();
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      result = defaultValue;
    } // catch
    
    // Return the result
    //finally {
      return result;
    //} // finally
  } // StringToFloat ()

  /**
    Test if the characters in a string may be converted into a long integer.
    <P>Example call:</P>
    <P><CODE>boolean b = Convert.isValidLong (digitString);
    </CODE></P>
  */
  public static boolean isValidLong (String s) {
    // Declare variables
    Long d;
    boolean valid = false;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Long (s);
      valid = true;
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      valid = false;
    } // catch
    
    // Return the result
    //finally {
      return valid;
    //} // finally
  } // isValidLong ()
  
  /**
    Convert a string to a long.  If the string is not a valid long
    value then the default value is returned.
    The string may be checked for validity by calling isValidLong ()
    <P>Example call:</P>
    <P><CODE>long val = Convert.stringToLong (digitString, (long) defaultValue);
    </CODE></P>
  */
  public static long stringToLong (String s, long defaultValue) {
    // Declare variables
    Long d;
    long result = defaultValue;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Long (s);
      result = d.longValue ();
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      result = defaultValue;
    } // catch
    
    // Return the result
    //finally {
      return result;
    //} // finally
  } // StringToLong ()

  /**
    Test if the characters in a string may be converted into an integer.
    <P>Example call:</P>
    <P><CODE>boolean b = Convert.isValidInt (digitString);
    </CODE></P>
  */
  public static boolean isValidInt (String s) {
    // Declare variables
    Integer d;
    boolean valid = false;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Integer (s);
      valid = true;
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      valid = false;
    } // catch
    
    // Return the result
    //finally {
      return valid;
    //} // finally
  } // isValidInt ()
  
  /**
    Convert a string to a int.  If the string is not a valid int
    value then the default value is returned.
    The string may be checked for validity by calling isValidInt ()
    <P>Example call:</P>
    <P><CODE>int val = Convert.stringToInt (digitString, defaultValue);
    </CODE></P>
  */
  public static int stringToInt (String s, int defaultValue) {
    // Declare variables
    Integer d;
    int result = defaultValue;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Integer (s);
      result = d.intValue ();
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      result = defaultValue;
    } // catch
    
    // Return the result
    //finally {
      return result;
    //} // finally
  } // StringToInt ()
  
  /**
    Test if the characters in a string may be converted into a short integer.
    <P>Example call:</P>
    <P><CODE>boolean b = Convert.isValidShort (digitString);
    </CODE></P>
  */
  public static boolean isValidShort (String s) {
    // Declare variables
    Short d;
    boolean valid = false;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Short (s);
      valid = true;
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      valid = false;
    } // catch
    
    // Return the result
    //finally {
      return valid;
    //} // finally
  } // isValidShort ()
  
  /**
    Convert a string to a short.  If the string is not a valid short
    value then the default value is returned.
    The string may be checked for validity by calling isValidShort ()
    <P>Example call:</P>
    <P><CODE>short val = Convert.stringToShort (digitString, (short) defaultValue);
    </CODE></P>
  */
  public static short stringToShort (String s, short defaultValue) {
    // Declare variables
    Short d;
    short result = defaultValue;
    
    // Try to format the string 
    try {
      s = s.trim ();
      d = new Short (s);
      result = d.shortValue ();
    } // try
    
    // Exception handler
    catch (NumberFormatException e) {
      result = defaultValue;
    } // catch
    
    // Return the result
    //finally {
      return result;
    //} // finally
  } // StringToShort ()
  
} //Convert


