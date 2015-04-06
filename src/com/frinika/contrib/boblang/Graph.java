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
//import uwejava.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;

// import javax.swing.*;

/**
  Graph is a simple class that provides facilities for
  <ul>
  <li>storing points
  <li>plotting them as a dot or line graph on the screen
  <li>printing multiple graphs .
  </ul>

  <p>Several graphs can be printed in one window.
  The axes labels are worked out from the points themselves.

  @author: Bob Lang
*/
public class Graph extends Frame {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public static final Color
    WHITE      = Color.white,
    LIGHT_GRAY = Color.lightGray,
    GRAY       = Color.gray,
    DARK_GRAY  = Color.darkGray,
    BLACK      = Color.black,
    RED        = Color.red,
    PINK       = Color.pink,
    ORANGE     = Color.orange,
    YELLOW     = Color.yellow,
    GREEN      = Color.green,
    MAGENTA    = Color.magenta,
    CYAN       = Color.cyan,
    BLUE       = Color.blue;

  // Fixed constants for visibility options
  private static final boolean
    VISIBLE   = true,           // Visible line to current point
    INVISIBLE = false;          // No line to the current point

  // Fixed constant for the number of trace titles
  public static final int
    MAX_TRACE_TITLE = 4;

  // Title strings
  private String
    xAxisTitle,                 // Label for x axis
    yAxisTitle,                 // Label for y axis
    graphTitle,                 // Title at the top of graph
    subTitle;                   // Main sub title

  // Titles for individual plots (instead of a sub title)
  private String []
    traceTitles = new String [MAX_TRACE_TITLE];

  // Colour for each individual plot title
  private Color []
    traceTitleColours = new Color [MAX_TRACE_TITLE];

  // Current colour of the points being plotted
  private Color
    colour;

  // Various flags
  private boolean
    newGraph,
    exitWhenClosed;

  // Vector storing the points to be plotted
  private GraphPointVector
    points;

  // Maximum and minimum ranges of x-y co-ordinates
  private double
    xMax,
    yMax,
    xMin,
    yMin;

  /**
    Preferred constructor which provides labels for graph, x and y axes.
    Use empty strings if not all labels are applicable.
  */
  public Graph (String inGraphTitle, String inXTitle, String inYTitle) {
    // Clear the window and set up the listener
    clearGraph (inGraphTitle, inXTitle, inYTitle);
    setupWindowListener ();

    // Default window size
    setSize (640, 480);
  } // Graph (String,String,String)

  /**
    Constructor which does not provide any labels
  */
  public Graph () {
    this ("", "", "");
  } // Graph ()

  /**
    Method which clears out a graph so that the window can be used
    again.
  */
  public void clearGraph (String inGraphTitle,
                          String inXTitle,
                          String inYTitle)
  {
    // Set or change the title of the graph
    setGraphTitle (inGraphTitle);

    // Save the titles for later use
    graphTitle = inGraphTitle;
    xAxisTitle = inXTitle;
    yAxisTitle = inYTitle;

    // Clear out sub title and individual plot titles
    subTitle = "";
    for (int i = 0; i < MAX_TRACE_TITLE; i++) {
      traceTitles [i] = "";
    } // for

    // Other initialisation
    points = new GraphPointVector ();
    colour = BLACK;
    newGraph = true;

    // Max and min values
    xMax = 0.0;
    yMax = 0.0;
    xMin = 0.0;
    yMin = 0.0;
  } // clearGraph

  /**
    Start a new graph using the same axes and titles so that
    several graphs can be drawn on the same frame (presumably
    in different colours)
  */
  public void nextGraph () {
    colour = BLACK;
    newGraph = true;
  } // nextGraph ()

  /**
    Put a subtitle underneath the plotted graph
  */
  public void setSubTitle (String inSubTitle) {
    // Save the subtitle
    subTitle = inSubTitle;

    // Clear out any trace titles
    for (int i = 0; i < MAX_TRACE_TITLE; i++) {
      traceTitles [i] = "";
    } // for
  } // setSubTitle ()

  /**
    Put a title for the specified trace
  */
  public void setTraceTitle (int traceNumber,
                             Color inColour,
                             String inTitle)
  {
    // Clear out any subtitle
    subTitle = "";

    // Clear out any trace titles
    if (traceNumber < MAX_TRACE_TITLE) {
      traceTitleColours [traceNumber] = inColour;
      traceTitles [traceNumber] = inTitle;
    } // if
  } // setTraceTitle ()

  /**
    Set the colour of the subsequent points plotted on the graph
  */
  public void setColor (Color c) {
    colour = c;
  } // setColor (Color)

  /**
    setColor for English spellers!
  */
  public void setColour (Color c) {
    setColor (c);
  } // setColour (int)

  /**
    Add a new point to the current graph.  The graph is not
    actually shown until showGraph () is called.
  */
  public void add (double x, double y) {
    // System.out.println ("Adding " +x + " " + y);
    // Don't draw a line to the first point
    boolean visible = !newGraph;
    newGraph = false;

    // Put the point in the vector
    points.addPoint (new GraphPoint (colour, visible, x, y));

    // Find maximum and minimum values
    if (x > xMax)
      xMax = x;
    if (x < xMin)
      xMin = x;
    if (y > yMax)
      yMax = y;
    if (y < yMin)
      yMin = y;
  } // add (double, double)

  /**
    Integer version of add (x,y)
  */
  public void add (int x, int y) {
    double
      dx = x,
      dy = y;
    add (dx, dy);
  } // add (int, int)

  /**
    Skip to a different point.  Just place an "invisible" point in
    the list.
  */
  public void skipTo (double x, double y) {
    // Put the point in the vector
    points.addPoint (new GraphPoint (colour, INVISIBLE, x, y));
    newGraph = false;
  } // skipTo ()

  /**
    Integer version of skipTo()
  */
  public void skipTo (int x, int y) {
    double
      dx = x,
      dy = y;
    skipTo (dx, dy);
  } // skipTo ()

  /**
    Compulsory request to show the current graph(s) in a frame.
    This version will always close the program when the user
    closes the frame
  */
  public void showGraph () {
    //$ System.out.println ("showGraph");
    exitWhenClosed = true;
    repaint ();
    show ();
  } // showGraph

  /**
    Alternative version of showGraph which allows the programmer
    to control whether the program exits when the frame is closed.
  */
  public void showGraph (boolean inExitWhenClosed) {
    //$ System.out.println ("showGraph (b)");
    exitWhenClosed = inExitWhenClosed;
    repaint ();
    show ();
  } // showGraph (boolean)

  // **** GRAPH PAINT METHOD ********************************************
  // Attributes used for plotting the graphs
  private int
    xOffset,                    // X and Y offsets for printing multiple graphs
    yOffset;                    // (for screen use, these should be 0,0)
  private double
    xSpread,                    // Range of values to be plotted on the X axis
    ySpread;                    // Range of values to be plotted on the Y axis
  private int
    xAxisLength,                // Lengths of X and Y axes in pixels
    yAxisLength,
    xOrigin,                    // Position of X and Y origin (0,0)
    yOrigin;
  private int
    xBorder,                    // Border for writing text and labels
    yBorder;
  private int
    frameWidth,                 // Size of the frame in pixels
    frameHeight;
  private boolean
    printGraph;                 // This graph is to be printed on paper

  /**
    Standard paint method which draws the entire graph. Although marked
    public this method should not be called by the user program, only by
    the Java's windowing system.
  */
  public synchronized void paint (Graphics g) {
    //$ System.out.println ("Calling paint");
    // Not to be printed
    printGraph = false;

    // Set the offset at 0,0 relative to the start of the frame
    xOffset = 0;
    yOffset = 0;

    // calculate length of axes from window size minus a suitable border
    xBorder = 80;
    yBorder = 80;

    // Set up the frame width and height
    Dimension d = this.getSize ();
    frameWidth = d.width;
    frameHeight = d.height;

    // Draw the axes and plot the graphs
    setUpAxes ();
    drawAxes (g);
    plotGraphs (g);
  } // paint (Graphics)

  /**
    Similar to paint () method, but used to print the graph to a printer.
    The graphics object must have been previously set up by creating a
    PrintJob object.

    The x and y offsets, width and height potentially allow multiple graphs
    to be plotted on the same piece of paper
  */
  public synchronized void print (int inXOffset,
                                  int inYOffset,
                                  int printWidth,
                                  int printHeight,
                                  Graphics g)
  {
    // Print this graph on paper
    printGraph = true;

    // Set the offset at 0,0 relative to the start of the frame
    xOffset = inXOffset;
    yOffset = inYOffset;

    // calculate length of axes from window size minus a suitable border
    xBorder = 50;
    yBorder = 60;

    // Set up the frame width and height
    frameWidth = printWidth;
    frameHeight = printHeight;

    // Draw a border to surround the graph
    g.setColor (BLUE);
    g.drawRect (xOffset, yOffset, frameWidth, frameHeight);

    // Draw the axes and plot the graphs
    setUpAxes ();
    drawAxes (g);
    plotGraphs (g);
  } // print (Graphics)

  // **** PRIVATE METHODS **************************************************

  /**
    Set the title of the graph in the title bare
  */
  private void setGraphTitle (String inGraphTitle) {
    // Set the title and the default size of the graph
    graphTitle = "Graph";
    if (inGraphTitle != null) {
      if (inGraphTitle.length () > 1) {
        graphTitle = inGraphTitle;
      } // if
    } // if
    setTitle (graphTitle);
  } // setGraphTitle ()

  /**
    Method which sets up the window listener for this graph.
  */
  private void setupWindowListener () {
    addWindowListener (new WindowAdapter () {
      public void windowClosing (WindowEvent e) {
        dispose ();
        if (exitWhenClosed)
          System.exit (0);
      } // WindowClosing ()
    }); // addWindowListener ()
  } // setupWindowListener ()

  /**
    Calculate the X scaling factor
  */
  private int scaleX (double x) {
    return (int) ((x - xMin)/xSpread*xAxisLength) + xBorder;
  } // scaleX (double)

  /**
    Calculate the Y scaling factor
  */
  private int scaleY (double y) {
    return
      (int) (yAxisLength - ((y - yMin)/ySpread*yAxisLength)) + yBorder;
  } // scaleY (double)

  /**
    Set up the X and Y axes and origin
  */
  private void setUpAxes () {
    xAxisLength = (int) frameWidth - 2*xBorder;
    yAxisLength = (int) frameHeight - 2*yBorder;

    // Special case when asked to plot an empty graph
    if (xMin > xMax) {
      xMin = -1;
      xMax = 1;
    } // if
    else if (xMin == xMax) {
      xMin = xMin - 1;
      xMax = xMax + 1;
    } // if

    // Similar processing for Y axis
    if (yMin > yMax) {
      yMin = -1;
      yMax = 1;
    } // if
    else if (yMin == yMax) {
      yMin = yMin - 1;
      yMax = yMax + 1;
    } // if

    // calculate value spreads from mins and maxs which have
    // been recorded as we go
    xSpread = xMax - xMin;
    ySpread = yMax - yMin;

    // Calculate the origin positions
    if (xMin > 0)
      xOrigin = scaleX (xMin);
    else
      xOrigin = scaleX (0);
    if (yMin > 0)
      yOrigin = scaleY (yMin);
    else
      yOrigin = scaleY (0);
  } // setUpAxes ()

  /**
    Draw the X-Y axes onto the current frame and write in appropriate
    titles/subtitles/trace titles
  */
  private void drawAxes (Graphics g) {
    int xPos, yPos;
    g.setColor (BLACK);
    Font plain = g.getFont ();
    Font small = new Font (plain.getFamily (), Font.PLAIN, 10);
    Font bold = new Font (plain.getFamily (), Font.BOLD, 14);

    // Draw the X and Y axes
    g.drawLine (xBorder - 5 + xOffset,
                yOrigin + yOffset,
                xAxisLength + xBorder + 5 + xOffset,
                yOrigin + yOffset);
    g.drawLine (xOrigin + xOffset,
                yBorder - 5 + yOffset,
                xOrigin + xOffset,
                yAxisLength + yBorder + 5 + yOffset);

    // Put the titles on the X and Y axes
    xPos = frameWidth
         - g.getFontMetrics ().stringWidth (xAxisTitle)
         - xBorder/2;
    g.drawString (xAxisTitle, xPos + xOffset, yOrigin - 5 + yOffset);
    xPos = xOrigin - g.getFontMetrics ().stringWidth (yAxisTitle)/2;
    g.drawString (yAxisTitle, xPos + xOffset, yBorder - 8 + yOffset);

    // Draw the horizontal and vertical ticks
    g.setFont (small);
    drawHorizontalTicks (g);
    drawVerticalTicks (g);

    // Main title at top of the graph
    g.setFont (bold);
    xPos = (frameWidth - g.getFontMetrics ().stringWidth (graphTitle))/2;
    g.drawString (graphTitle, xPos + xOffset, yBorder/2 + yOffset);

    // Sub title, or titles for each trace
    if (subTitle.length () > 0) {
      g.setFont (plain);
      xPos = (frameWidth - g.getFontMetrics ().stringWidth (subTitle))/2;
      yPos = frameHeight - yBorder/2;
      g.drawString (subTitle, xPos + xOffset, yPos + yOffset);
    } // if
    else {
      // Use small font for the plot titles
      g.setFont (small);

      // Draw title for each individual trace (max 4)
      for (int t = 0; t < MAX_TRACE_TITLE; t++) {
        if (traceTitles [t].length () > 0) {
          // Calculate position to draw title
          xPos = t*(frameWidth - 2*xBorder)/MAX_TRACE_TITLE + xBorder;
          yPos = frameHeight - yBorder/2;
          g.setColor (traceTitleColours [t]);
          g.drawString (traceTitles [t], xPos + xOffset, yPos + yOffset);
        } // if
      } // for
    } // else

    // Revert to default font
    g.setFont (plain);
  } // drawAxes (Graphics)

  /**
    Draw and label the horizontal ticks
  */
  private void drawHorizontalTicks (Graphics g) {
    // Find the interval for each tick.. 1, 5, 10, 50, 100, 500 etc
    boolean byTwo = false;
    int tickInterval = 1;
    int pixTick = scaleX (tickInterval) - scaleX (0);

    //$ System.out.println ("First pix = " + pixTick);
    while (pixTick < 20) {
      //$ System.out.println ("tick " + tickInterval + " pix = " + pixTick);
      // Alternately multiply by five and two.
      if (byTwo) {
        tickInterval = tickInterval*2;
      }
      else {
        tickInterval = tickInterval*5;
      }
      byTwo = !byTwo;
      pixTick = scaleX (tickInterval) - scaleX (0);
    }

    // Find the position of the first and last ticks
    int firstTick = ((int) xMin)/tickInterval;
    int lastTick = ((int) xMax)/tickInterval + 1;

    //$ System.out.print ("Interval = " + tickInterval);
    //$ System.out.println (" First = " + firstTick + " Last = " + lastTick);
    // Draw the horizontal ticks
    // for (int tick= firstTick; tick <= lastTick; tick+=tickInterval) {
    for (int tick = firstTick; tick < lastTick; tick++) {
      // Calculate the position of the point and draw the tick
      double tickPoint = tick*tickInterval;
      int xTick = scaleX (tickPoint);
      g.drawLine (xTick + xOffset,
                  yOrigin + yOffset,
                  xTick + xOffset,
                  yOrigin + 5 + yOffset);

      // Label every second tick but not 0.0
      if (((int) tickPoint%(tickInterval*2) == 0) && (tickPoint != 0.0)) {
        g.drawString (Convert.doubleToString (tickPoint, 1, 0),
                      xTick - 10 + xOffset,
                      yOrigin + 15 + yOffset);
      } // if
    } // for
  } // drawHorizontalTicks (Graphics g)

  /**
    Draw and label the vertical ticks
  */
  private void drawVerticalTicks (Graphics g) {
    // Set up the minimum pixels between ticks
    int tickPixels = 20;
    if (printGraph) {
      tickPixels = 10;
    } // if

    // Get the font metrics for the current font
    FontMetrics fm = g.getFontMetrics ();

    // Find the interval for each tick.. 1, 5, 10, 50, 100, 500 etc
    boolean byTwo = false;
    int tickInterval = 1;
    int pixTick = scaleY (0) - scaleY (tickInterval);

    //$ System.out.println ("First pix = " + pixTick);
    while (pixTick < tickPixels) {
      //$ System.out.println ("tick " + tickInterval + " pix = " + pixTick);
      // Alternately multiply by five and two.
      if (byTwo) {
        tickInterval = tickInterval*2;
      }
      else {
        tickInterval = tickInterval*5;
      }
      byTwo = !byTwo;
      pixTick = scaleY (0) - scaleY (tickInterval);
    }

    // Find the position of the first and last ticks
    int firstTick = ((int) yMin)/tickInterval;
    int lastTick = ((int) yMax)/tickInterval + 1;

    //$ System.out.print ("Interval = " + tickInterval);
    //$ System.out.println (" First = " + firstTick + " Last = " + lastTick);
    // Draw the horizontal ticks
    // for (int tick= firstTick; tick <= lastTick; tick+=tickInterval) {
    for (int tick = firstTick; tick < lastTick; tick++) {
      // Calculate the position of the point and draw the tick
      double tickPoint = tick*tickInterval;
      int yTick = scaleY (tickPoint);
      g.drawLine (xOrigin + xOffset,
                  yTick + yOffset,
                  xOrigin - 5 + xOffset,
                  yTick + yOffset);

      // Label every tick but not 0.0
      if (tickPoint != 0.0) {
        String tickString;
        int offset;

        // Determine if engineering notation required
        if (ySpread >= 1E5) {
          DecimalFormat df = new DecimalFormat ("0.0E0");
          tickString = df.format (tickPoint);
        }
        else {
          tickString = Convert.doubleToString (tickPoint, 1, 0);
        }

        //$ System.out.println ("<" + tickString + ">");
        g.drawString (tickString,
                      xOrigin - fm.stringWidth (tickString) - 5 + xOffset,
                      yTick + 5 + yOffset);
      } // if
    } // for
  } // drawVerticalTicks (Graphics g)

  /**
    Method to plot all the points stored in the points vector
  */
  private void plotGraphs (Graphics g) {
    int x1, y1, x2, y2;
    Color plotColour;

    // Set to invalid colour
    plotColour = null;

    // Set the default first point
    x1 = 0;
    y1 = 0;

    // Loop through all the points recorded
    GraphPoint point = points.getFirstPoint ();
    while (point != null) {
      // System.out.println ("Recalling " + point.x + " " + point.y);
      x2 = scaleX (point.x);
      y2 = scaleY (point.y);

      // See if a change of colour required
      if (point.colour != plotColour) {
        g.setColor (point.colour);
        plotColour = point.colour;
      }

      // plot the line and/or point
      if (point.visible) {
        g.drawLine (x1 + xOffset,
                    y1 + yOffset,
                    x2 + xOffset,
                    y2 + yOffset);
      } // if

      // Save coordinates for next time round the loop
      x1 = x2;
      y1 = y2;

      // Get the next point to be plotted
      point = points.getNextPoint ();
    } // while
  } // plotGraphs ()
} // Graph
