/*[ PrintPanelPage.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Vector;

 class PrintPanelPage extends Vector implements Printable
 {
    PrintPanelPage(Component parent)
    {
       super();
      m_parent = parent;
    }
   
    PrintPanelPage(int h, int v, Rectangle size, Component parent)
    {
       super();
      m_ivPage = v;
      m_ihPage = h;
      m_size = size;
      m_parent = parent;
    }

   //this function will split the entire pages into pages that will fit onto the printer
   @SuppressWarnings("unchecked")
   public void appendPrintPages(Book bk)
   {
      PageFormat pf = E2Designer.getApp().getMainFrame().getPageFormat();
      
      //find the total size
      Rectangle m_totalSize = null;
      for(int iComp = 0; iComp < size(); ++iComp)
      {
         if(get(iComp) instanceof PageableAndPrintable && get(iComp) instanceof Component)
         {
            Component uic = (Component)get(iComp);
            if(m_totalSize == null)
            {
               m_totalSize = uic.getBounds();            
            }
            else
               m_totalSize = m_totalSize.union(uic.getBounds());   
         }
      }

      if(m_totalSize == null)
         return;
         
      m_totalSize.width += pf.getImageableX();      
        m_totalSize.height += pf.getImageableY();

      //get the horizontal and vertical pages
      //add to total height so that we can print a title
      
      int ihPages = (m_totalSize.width / (int)pf.getImageableWidth()) + 1;
      int ivPages = (m_totalSize.height / (int)pf.getImageableHeight()) + 1;

      //create the pages
      PrintPanelPage[][] pages = new PrintPanelPage[ihPages][ivPages];
      for(int h = 0; h < ihPages; ++h)
      {
         for(int v = 0; v < ivPages; ++v)
         {
            int x,y,width,height;
            if(h == 0)
               x = 0;
            else
               x = h * (int)pf.getImageableWidth();
                  
            if(v == 0)
               y = 0;
            else
               y = v * (int)pf.getImageableHeight();
            
            width = (h + 1) * (int)pf.getImageableWidth();
            height = (v + 1) * (int)pf.getImageableHeight();
            
            Rectangle bounds = new Rectangle(x, y, width, height);
            PrintPanelPage pg = new PrintPanelPage(h, v, bounds, m_parent);
            pages[h][v] = pg; 
         }
      }
      
      //add the components to the pages
      for(int iComp = 0; iComp < size(); ++iComp)
      {
         if(get(iComp) instanceof PageableAndPrintable && get(iComp) instanceof Component)
         {
            Component uic = (Component)get(iComp);
            //you want the component to span the pages that it is in
            //  Rectangle bounds = uic.getBounds();
            //add the component to the pages it is in
            for(int h = 0; h < ihPages; ++h)
            {
               for(int v = 0; v < ivPages; ++v)
               {
                  pages[h][v].add(uic);
               }
            }
         }
      }                  

      //add the pages to the book
      for(int h = 0; h < ihPages; ++h)
      {
         for(int v = 0; v < ivPages; ++v)
         {
            bk.append((Printable)pages[h][v], pf);
         }
      }
   }
   
    public int print(Graphics g, PageFormat pf, int pageIndex)
              throws PrinterException 
   {
      if(m_parent == null || size() == 0)
         return(Printable.NO_SUCH_PAGE);
      
      g.translate((int)pf.getImageableX(), (int)pf.getImageableY());

      String strTitle = null;
      if(m_parent instanceof JInternalFrame)
      {
         strTitle = ((JInternalFrame)m_parent).getTitle();
      }

      else if(m_parent instanceof JFrame)
      {
         strTitle = ((JFrame)m_parent).getTitle();
      }

      if(strTitle != null)
      {
         strTitle += " - Page H" + String.valueOf(m_ihPage + 1) +
                     " V" + String.valueOf(m_ivPage + 1);
         int iLength = g.getFontMetrics().charsWidth(strTitle.toCharArray(),
                                      0,
                                      strTitle.length());
         
         g.drawString(strTitle, ((int)pf.getImageableWidth() / 2) - (iLength / 2), g.getFontMetrics().getHeight());
      }
      
      for(int iComp = 0; iComp < size(); ++iComp)
      {
         if(get(iComp) instanceof PageableAndPrintable && get(iComp) instanceof Component)
         {
            PageableAndPrintable uic = (PageableAndPrintable)get(iComp);
            uic.setPrintLocation(new Point(m_ihPage, m_ivPage));
            uic.print( g,  pf,  pageIndex);
         }
      }

         return Printable.PAGE_EXISTS;
     }
   
   /**
   * Return the offset, this will enable the object to paint in the top left hand corner
   */
   public Point getOffset()
   {
      return(m_offset);
   }
   
   public Rectangle getSize()
   {
      return(m_size);
   }
   
   private Point m_offset = null;
   private int m_ihPage = 0;
   private int m_ivPage = 0;
   private Rectangle m_size = null;
   private Component m_parent = null;
 }
