/*[ JoinArrow.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * This class is used in the join editor to link 2 columns.  It gets created
 * when the user drags and drops between 2 tables. It's container is
 * UIJoinMainFrame.
 */
public class JoinArrow extends JLabel implements PageableAndPrintable
{
   JoinArrow(boolean bLeft, UIJoinConnector connector)
   {
      m_bLeft = bLeft;
      m_connector = connector;
      getHandle();
      setOpaque(false);
   }
   
   public void setLeft(boolean bLeft)
   {
      m_bLeft = bLeft;
      getHandle();
   }
   
   private void getHandle()
   {
      m_handle = m_connector.getLeftHandle();
      if(m_handle != null)
      {
         Point newlocation = SwingUtilities.convertPoint(m_connector,
                                         m_handle.getLocation(),
                                         this);
         m_handle.setLocation(newlocation);
      }
   }
   
   private void paintArrow(Graphics g)
   {
      int iPointNum = 3;
         
      int xPoints[] = {0, 0, 0};
      int yPoints[] = {0, 0, 0};
         
      Point pt1 = null;
      Point pt2 = null;
      Point pt3 = null;
      Dimension size = getSize();
      
      if(m_bLeft)
      {
         pt1 = new Point(0,0);   
         pt2 = new Point(0, size.height);
         pt3 = new Point(size.width, size.height / 2 + 1);
      }
      else
      {
         pt1 = new Point(size.width,0);   
         pt2 = new Point(size.width, size.height);
         pt3 = new Point(0, size.height / 2 + 1);
      }
         
      xPoints[0] = pt1.x;
      xPoints[1] = pt2.x;
      xPoints[2] = pt3.x;
         
      yPoints[0] = pt1.y;
      yPoints[1] = pt2.y;
      yPoints[2] = pt3.y;
         
      g.setColor(Color.black);
      g.fillPolygon(xPoints,yPoints,iPointNum);   
   }      
   
   public void paint(Graphics g)
   {
      paintArrow(g);
      //now paint handle that we covered up
      getHandle();
      if(m_handle != null)
      {
         m_handle.setColor(Color.blue);
         m_handle.paint(g);
      }
   }
   
   /* Implementation of the printing interface
   */
    public int print(Graphics g, PageFormat pf, int pageIndex)
              throws PrinterException 
   {
      Point pt = getLocation();
      Component parent = this;
      while(parent != null && !(parent instanceof UIJoinMainFrame))
      {
         parent = parent.getParent();
      }
      
      if(parent != null && getParent() != null)
         pt = SwingUtilities.convertPoint(getParent(), pt, parent);

      //check for the page
      Point pageLoc = getPrintLocation();
      if(pageLoc != null)
      {
         int iMovex = 0;
         int iMovey = 0;
         if(pageLoc.x > 0)
            iMovex = pageLoc.x * (int)pf.getImageableWidth();
         
         if(pageLoc.y > 0)
            iMovey = pageLoc.y * (int)pf.getImageableHeight();
         
         pt.translate(-iMovex, -iMovey);   
      }

      //paint into an image so that we can relocate it.
      Image offscreen = createImage(getSize().width, getSize().height);
      if(offscreen != null)
      {
         Graphics og = offscreen.getGraphics();
         og.setClip(0, 0, getSize().width, getSize().height);
         og.setColor(Color.white);
         og.fillRect(0, 0, getSize().width, getSize().height);
         paintArrow(og);
         ImageIcon icon = new ImageIcon(offscreen);
         icon.paintIcon(this, g, pt.x, pt.y);
      }
      
      return Printable.PAGE_EXISTS;
   }

   public void setPrintLocation(Point pt)
   {
      m_printLocation = pt;   
   }
   
   public Point getPrintLocation()
   {
      return(m_printLocation);
   }
   

   private boolean m_bLeft = false;
   UIJoinConnector m_connector = null;
   UIConnector.Handle   m_handle = null;
   private Point m_printLocation = null;
}
