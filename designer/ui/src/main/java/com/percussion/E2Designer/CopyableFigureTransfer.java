/*[ CopyableFigureTransfer.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

/**
 * This class is a wrapper class for the UIConnectableFigure objects when
 * they need to be transfered via the clipboard.
 */
public class CopyableFigureTransfer extends FigureTransfer
{
   // constructors
   /**
    *   Creates a new transfer object.
    */
   public CopyableFigureTransfer( UIConnectableFigure uic, DragInfo info )
      throws java.io.IOException
   {
      this(uic, info, null);
   }

   public CopyableFigureTransfer( Vector uiccollection )
      throws java.io.IOException
   {
      this(null, null, uiccollection);   
   }
   
   public CopyableFigureTransfer( UIConnectableFigure uic, DragInfo info, Vector uiccollection )
      throws java.io.IOException
   {
      super(uic, info, uiccollection);

      /* we serialize all the parts up front. This may seem inefficient,
       * but we do it to make sure we have the state of the object at the
       * time it was copied. If we do a lazy copy, the user can go off and
       * edit it, etc. and we'll paste the modified version, not the
       * copied one.
       */

      if (m_uic != null)
         m_uicSerialized = getSerializedByteArray(m_uic);

      if (m_info != null)
         m_infoSerialized = getSerializedByteArray(m_info);

      if (m_uiccollection != null)
         m_uicCollectionSerialized = getSerializedByteArray(m_uiccollection);
   }

   public Object getTransferData( DataFlavor flavor )
         throws   UnsupportedFlavorException,
               IOException
   {
      if (m_aFlavors[0].equals(flavor))
         return m_uicSerialized;
      else if (m_aFlavors[1].equals(flavor))
         return m_infoSerialized;
      else if(m_aFlavors[2].equals(flavor))
         return m_uicCollectionSerialized;
      else
         throw new UnsupportedFlavorException(flavor);
   }

   protected byte[] getSerializedByteArray(Object o)
      throws java.io.IOException
   {
      java.io.ByteArrayOutputStream bOut
         = new java.io.ByteArrayOutputStream();
      java.io.ObjectOutputStream objOut
         = new java.io.ObjectOutputStream(bOut);
      objOut.writeObject(o);
      objOut.close();

      byte[] ret = bOut.toByteArray();
      bOut.close();

      return ret;
   }

   protected byte[]      m_uicSerialized = null;
   protected byte[]      m_infoSerialized = null;
   protected byte[]      m_uicCollectionSerialized = null;
}

