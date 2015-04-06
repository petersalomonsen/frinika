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
import java.util.*;
/**
  Class to store an arbitrary number of graph points in a vector and
  provide fifo access to its elements
*/

public class GraphPointVector {
  private Vector
    v;                          // Vector to store the points
  private int
    count,                      // size of vector
    index;                      // index to next element in vector

  /**
    Constructor to create the vector and initialise attributes
  */
  public GraphPointVector () {
    v = new Vector ();
    count = 0;
    index = 0;
  } // GraphPointVector ()

  /**
    Add a point (GraphPoint object) to the end of the vector
  */
  public void addPoint (GraphPoint p) {
    v.add (p);
    count++;
  } // addPoint ()

  /**
    Get the first element (GraphPoint object) from the vector
  */
  public GraphPoint getFirstPoint () {
    count = v.size ();
    index = 0;
    return getNextPoint ();
  } // getFirstPoint ()

  /**
    Get the next element (GraphPoint object) from the vector
  */
  public GraphPoint getNextPoint () {
    // If out of bounds then return null
    if (index >= count) {
      return null;
    } // if
    else {
      // Get the element and bump the counter
      GraphPoint point = (GraphPoint) v.get (index);
      index++;
      return point;
    } // if
  } // getNextPoint ()
} // GraphPointVector
