/******************************************************************************
 *
 * [ PSLegacyDnDHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.legacy;

import com.percussion.workbench.ui.PSUiReference;
import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;
import java.util.Collection;

/**
 * Helps DnD operations between Eclipse and Swing parts of the application.
 * Implements a workaround for issue that it seems there is no way to pass
 * objects between two subsystems, only standard system types.
 * Also handles mapping between legacy Swing and modern Eclipse UI model objects.  
 * The Eclipse part of the application places drag data into this class,
 * the Swing part of the application retrieves the data on drop.
 *
 * @author Andriy Palamarchuk
 */
public class PSLegacyDnDHelper
{
   final static Logger ms_log = Logger.getLogger(PSLegacyDnDHelper.class);

   /**
    * Hidden constructor.
    */
   PSLegacyDnDHelper() {}
   
   /**
    * If transfer type corresponds to the transfers returned by
    * {@link #getTransfers()}
    * Provides data for SWT drag, remembers references in
    * {@link #m_transferredData} to provide them later.
    * Should be called between {@link #dragStarted()} and
    * {@link #dragFinished()} calls.
    */
   public Object getDataForDrag(Transfer type, Collection<PSUiReference> nodes)
   {
      if (!m_dragRunning)
      {
         throw new IllegalStateException(
               "Should be called only after call to dragStarted()");
      }
      if (type.equals(TextTransfer.getInstance()))
      {
         rememberDataToTransfer(nodes);
         return generateTransferKey();
      }
      else
      {
         return null;
      }
   }
   
   /**
    * Notifies the helper that transfer is completed
    */
   public void dragFinished()
   {
      m_dragRunning = false;
   }
   
   public void dragStarted()
   {
      m_dragRunning = true;
   }

   /**
    * Saves data to transfer.
    */
   private synchronized void rememberDataToTransfer(
         final Collection<PSUiReference> nodes)
   {
      m_lastTransferId++;
      m_transferredData = nodes;
   }

   /**
    * Encodes data stored in {@link #m_transferredData} to be passed through
    * DnD. 
    */
   private Object generateTransferKey()
   {
      return TRANSFER_ID + ":" + m_lastTransferId;
   }

   /**
    * Called on Eclipse side.
    * Transfers the helper used to pass data through DnD. 
    */
   public Transfer[] getTransfers()
   {
      return new Transfer[] {TextTransfer.getInstance()};
   }

   /**
    * Indicates whether the drop event contains drop from drag generated
    * with the helper data.
    */
   public boolean isRecognizedLegacyDrop(DropTargetEvent event)
         throws UnsupportedFlavorException
   {
      if (!m_dragRunning || !isDataFlavorSupported(event, DATA_FLAVOR))
      {
         return false;
      }
      final Object data;
      try
      {
         data = getTransferable(event).getTransferData(DATA_FLAVOR);
      }
      catch (IOException e)
      {
         // no problem, don't accept then
         return false;
      }
      if (!(data instanceof String))
      {
         return false;
      }
      return isUpToDateDrop(data);
   }

   /**
    * Requests event whether provided data flavor supported.
    */
   private boolean isDataFlavorSupported(DropTargetEvent event,
         final DataFlavor dataFlavor)
   {
      return event instanceof DropTargetDragEvent
            ? ((DropTargetDragEvent) event).isDataFlavorSupported(dataFlavor)
            : ((DropTargetDropEvent) event).isDataFlavorSupported(dataFlavor);
   }

   /**
    * Indicates whether drop encoded in the provided string is valid up-to-date
    * drop.
    */
   private boolean isUpToDateDrop(final Object data)
   {
      if (!(data instanceof String))
      {
         return false;
      }
      final String str = (String) data;
      final String[] parts = str.split(":");
      return parts.length == 2 && parts[0].equals(TRANSFER_ID)
            && parts[1].equals(Long.toString(m_lastTransferId));
   }
   
   /**
    * Returns data encoded by the provided string or <code>null</code> if
    * string specified invalid or out-of-date data.
    */
   Collection<PSUiReference> extractDataFromDrop(final String str)
   {
      if (isUpToDateDrop(str))
      {
         return m_transferredData;
      }
      else
      {
         return null;
      }
   }

   /**
    * Extracts remembered data for the specified event.  
    */
   public Collection<PSUiReference> getDataFor(DropTargetEvent event)
         throws UnsupportedFlavorException, IOException
   {
      if (!isRecognizedLegacyDrop(event))
      {
         throw new IllegalArgumentException("Must be valid legacy drop.");
      }
      final Transferable transferable = getTransferable(event);
      final String id =
            (String) transferable.getTransferData(DATA_FLAVOR);
      return extractDataFromDrop(id);
   }

   /**
    * Extracts transferable from the provided drop event.
    */
   private Transferable getTransferable(DropTargetEvent event)
   {
      return event instanceof DropTargetDragEvent
            ? ((DropTargetDragEvent) event).getTransferable()
            : ((DropTargetDropEvent) event).getTransferable();
   }

   /**
    * The singleton instance.
    */
   public static PSLegacyDnDHelper getInstance()
   {
      return INSTANCE;
   }

   /**
    * Identifier for the transfer to distinguish it among any other transfers.
    */
   private static String TRANSFER_ID = "Rhythmyx Legacy DnD Transfer";
   
   /**
    * The singleton instance.
    */
   private static final PSLegacyDnDHelper INSTANCE = new PSLegacyDnDHelper();
   
   /**
    * Buffer to store transferred data.
    * Populated by each next call to
    * {@link #getDataForDrag(Transfer, Collection)}.
    * Because there is at any time only one drag operation is going on we can
    * have only one place to store data.
    * <code>null</code> means there is no drag operation running
    * (but not vice-versa).
    */
   private Collection<PSUiReference> m_transferredData;
   
   /**
    * Used to make sure up-to-date drag information is used.
    * Identified with value returned by {@link System#currentTimeMillis()}
    * read when the data was stored to {@link #m_transferredData}.
    */
   private long m_lastTransferId;
   
   /**
    * AWT data flavor used to receive the drop.
    */
   public static final DataFlavor DATA_FLAVOR = DataFlavor.stringFlavor;
   
   /**
    * Indicates that drag operation is running.
    */
   private boolean m_dragRunning;
}
