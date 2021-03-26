/*[ JoinHand.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Vector;

/**
 * This class is used to draw the fingers of the right side of the join
 * when a formula is present
 */
public class JoinHand extends JLabel
{
   JoinHand(boolean bLeft, UIJoinConnector connector, int iWidth)
   {
      m_bLeft = bLeft;
      m_connector = connector;
      m_iWidth = iWidth;
      setOpaque(false);
      
      calcSize();
   }
   
   public void calcSize()
   {
      Vector fingers = m_connector.getFormulaColumns();
      int leftColumn = m_connector.getLeftColumn();
      if (leftColumn >= 0 && leftColumn < m_connector.getLeftJoin().getFrame().getListbox().getModel().getSize())
         fingers.add(m_connector.getLeftJoin().getFrame().getListbox().getModel().getElementAt(leftColumn).toString());
      if (fingers.size() == 0)
         return;
      
      indexes = new int[fingers.size()];
      //find the fingers
      for(int iFinger = 0; iFinger < fingers.size(); ++iFinger)
      {
         for(int iData = 0; iData < m_connector.getLeftJoin().getFrame().getListbox().getModel().getSize(); ++iData)
         {
            if(fingers.get(iFinger).toString().equals(   m_connector.getLeftJoin().getFrame().getListbox().getModel().getElementAt(iData).toString()))
            {
               indexes[iFinger] = iData;               
               break;
            }
         }
      }
      
      Arrays.sort(indexes);
      
      //turn into coords
      for(int index = 0; index < indexes.length; ++index)
      {
         if(indexes[index] < m_connector.getLeftJoin().getFrame().getListbox().getFirstVisibleIndex() && m_connector.getLeftJoin().getFrame().getListbox().getLastVisibleIndex() != -1)
         {
            indexes[index] = 0;
         }
         else if(indexes[index] > m_connector.getLeftJoin().getFrame().getListbox().getLastVisibleIndex() && m_connector.getLeftJoin().getFrame().getListbox().getLastVisibleIndex() != -1)
         {
            indexes[index] = (int)m_connector.getLeftJoin().getFrame().getSize().getHeight() - 3;
         }
         else
         {
            Rectangle rightColumnBounds = SwingUtilities.convertRectangle(m_connector.getLeftJoin().getFrame().getListbox(),
                                                     m_connector.getLeftJoin().getFrame().getListbox().getCellBounds(indexes[index], indexes[index]),
                                                     m_connector.getLeftJoin().getFrame());
            
            indexes[index] = rightColumnBounds.y + (rightColumnBounds.height / 2);
         }
      }
      
      Point location = m_connector.getLeftJoin().getFrame().getLocation();
        if(m_bLeft)
         location.translate( -m_iWidth, 0);
        else
           location.translate( (int)m_connector.getLeftJoin().getFrame().getSize().getWidth(), 0);
      
      setLocation(location);
      setSize(m_iWidth, indexes[indexes.length - 1]);
   }
   
   public void setLeft(boolean bLeft)
   {
      m_bLeft = bLeft;
   }
   
   public void paint(Graphics g)
   {
      if(indexes.length > 0)
      {
         Dimension size = getSize();
         g.setColor(Color.black);
         if(!m_bLeft)
           {
            g.drawLine(size.width - 1, indexes[0],size.width - 1,size.height);
              for(int index = 0; index < indexes.length; ++index)
            {
                 g.drawLine(0, indexes[index] - 1, size.width - 1, indexes[index] - 1);   
            }
         }
           else
         {
              g.drawLine(0,indexes[0],0,size.height);
            for(int index = 0; index < indexes.length; ++index)
              {
               g.drawLine(size.width, indexes[index] - 1, 0, indexes[index] - 1);   
            }
           }
      }
   }
   
   private boolean m_bLeft = false;
   UIJoinConnector m_connector = null;
   int[] indexes;
   int m_iWidth;
}
