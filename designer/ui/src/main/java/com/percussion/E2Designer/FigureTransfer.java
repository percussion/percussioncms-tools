/******************************************************************************
 *
 * [ FigureTransfer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

/**
 * This class is a wrapper class for the UIConnectableFigure objects when they
 * need to be transfered via DnD. CopyableFigureTransfer should be
 * used for the clipboard.
 */
public class FigureTransfer implements Transferable, ClipboardOwner
{

   public static final String sUICOBJ_FLAVOR_TYPE = 
      "application/x-java-serialized-object; "
      + "class=com.percussion.E2Designer.UIConnectableFigure";
   public static final String sUICOBJ_FLAVOR_NAME = "ConnectableFigure";

   public static final String sUICCOL_FLAVOR_TYPE = 
      "application/x-java-serialized-object; "
      + "class=com.percussion.util.PSCollection";
   public static final String sUICCOL_FLAVOR_NAME = "ConnectableFigureCollection";

   public static final String sDESCRIPTOR_FLAVOR_TYPE = 
      "application/x-java-serialized-object; "
      + "class=com.percussion.E2Designer.DragInfo";
   public static final String sDESCRIPTOR_FLAVOR_NAME = "DragInfo";

   // constructors
   /**
    *   Creates a new transfer object.
    */
   public FigureTransfer( UIConnectableFigure uic, DragInfo info )
   {
      this(uic, info, null);                  
   }

   public FigureTransfer( Vector uiccollection )
   {
      this(null,null,uiccollection);   
   }
   
   public FigureTransfer( UIConnectableFigure uic, DragInfo info, Vector uiccollection )
   {
      int i=0;
      // Basic UIConnectable object
      m_aFlavors[i++] = new DataFlavor(sUICOBJ_FLAVOR_TYPE, sUICOBJ_FLAVOR_NAME);
      // A descriptor object for the dragging object
      m_aFlavors[i++] = new DataFlavor(sDESCRIPTOR_FLAVOR_TYPE, 
         sDESCRIPTOR_FLAVOR_NAME);
      //a collection of UIConnectable objects
      m_aFlavors[i++] = new DataFlavor(sUICCOL_FLAVOR_TYPE, 
         sUICCOL_FLAVOR_NAME);
      Debug.assertTrue(i <= m_aFlavors.length, E2Designer.getResources(),
            "FlavorArrayTooSmall", null);
      m_uic = uic;
      m_info = info;
      m_uiccollection = uiccollection;
   }

   // Transferable implementation
   public DataFlavor[] getTransferDataFlavors( )
   {
      return m_aFlavors;
   }

   public boolean isDataFlavorSupported( DataFlavor flavor )
   {
      boolean bFound = false;
      for (int i=0; i < m_aFlavors.length && !bFound; i++)
      {
         if (m_aFlavors[i].equals(flavor))
            bFound = true;
      }
      return(bFound);
      
   }

   public Object getTransferData( DataFlavor flavor )
         throws   UnsupportedFlavorException,
               IOException
   {
      if (m_aFlavors[0].equals(flavor))
         return m_uic;
      else if (m_aFlavors[1].equals(flavor))
         return m_info;
      else if(m_aFlavors[2].equals(flavor))
         return m_uiccollection;   
      else
         throw new UnsupportedFlavorException(flavor);
   }

   public void lostOwnership(Clipboard c, Transferable t)
   {
      m_uic = null;   
      m_info = null;
      m_uiccollection = null;
   }
   
   // protected storage
   protected DataFlavor [] m_aFlavors = new DataFlavor[3];
   protected UIConnectableFigure m_uic;
   protected DragInfo m_info;
   protected Vector m_uiccollection;
}

