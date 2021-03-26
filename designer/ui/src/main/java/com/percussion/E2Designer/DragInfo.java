/******************************************************************************
 *
 * [ DragInfo.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * This class contains information needed during a drag/drop operation.   It is 
 * used by UIFigureFrame to determine if a dragging figure can connect to a
 * figure in the frame, and where to place the figure when it is dropped.
 */
public class DragInfo
{
   /**
    * @param figure a reference to the figure that is being dragged. This can
    * be used to determine if the figure is trying to drop onto itself.
    *
    * @param ptOffset the offset of the cursor from the upper left corner of
    * the component when the drag was initiated. This is used to correctly
    * position the component after the drop.
    *
    * @param ID a unique identifier for the component, used to determine if
    * the dragging component can connect to the component under the cursor. If
    * multiple, unconnected objects are being dragged, null should be passed
    * in for this param.
    *
    * @param strServerName the name of the E2 server that contains the application
    * where this component originated from. It may be needed if we allow dragging
    * between instances of the program opened on different E2 servers (or to 
    * prevent such dragging).
    */
   public DragInfo(UIConnectableFigure figure, Point ptOffset, 
         UICIdentifier ID, String strServerName, Properties props)
   {
      this(figure, ptOffset, ID, strServerName, props, null);      
   }

   /**
    * @param figure a reference to the figure that is being dragged. This can
    * be used to determine if the figure is trying to drop onto itself.
    *
    * @param ptOffset the offset of the cursor from the upper left corner of
    * the component when the drag was initiated. This is used to correctly
    * position the component after the drop.
    *
    * @param ID a unique identifier for the component, used to determine if
    * the dragging component can connect to the component under the cursor. If
    * multiple, unconnected objects are being dragged, null should be passed
    * in for this param.
    *
    * @param strServerName the name of the E2 server that contains the application
    * where this component originated from. It may be needed if we allow dragging
    * between instances of the program opened on different E2 servers (or to 
    * prevent such dragging).
    *
    * @param dragImage the image to be displayed during drag and drop
    */
   public DragInfo(UIConnectableFigure figure, Point ptOffset, 
         UICIdentifier ID, String strServerName, Properties props, ImageIcon dragImage)
   {
      m_figure = figure;
      m_ptOffset = ptOffset;
      m_ID = ID;
      m_strServer = strServerName;
      m_dragImage = dragImage;
      m_figureProperties = props;
   }
   
   /**
   * @returns the figure properties.  These are used by UIFigureFrame to decide
   * when to allow dragging
   */
   public Properties getFigureProperties()
   {
      return(m_figureProperties);
   }

   /**
    * @returns the reference for this object before it started dragging
    */
   public UIConnectableFigure getOriginalRef()
   {
      return(m_figure);
   } 

   /**
    * @returns the offset of the cursor from the upper left corner of the
    * component at the point in time the drag was initiated.
    */
   public Point getOffset()
   {
      return(m_ptOffset);
      
   }

   /**
    * @returns the name of the E2 server that contains the application that 
    * this figure originated from. 
    */
   public String getServerName()
   {
      return(m_strServer);
      
   }

   /**
    * @returns the unique ID for the component that is being dragged. If multiple
    * components are being dragged and they are contained by a single component,
    * the ID of the container is returned. If multiple, seperate components are
    * being dragged, null is returned.
    */
   public UICIdentifier getID()
   {
      return m_ID;
   }
   
   /**
   * @returns the image used during drag and drop
   */
   public ImageIcon getDragImage()
   {
      return(m_dragImage);
   }

   // private storage
   private UIConnectableFigure m_figure = null;
   private Point m_ptOffset = new Point(0, 0);
   private UICIdentifier m_ID = null;
   private String m_strServer = new String();
   private ImageIcon m_dragImage = null;
   final private Properties m_figureProperties;
}
