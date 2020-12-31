/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * This class extends the tab components and provides the implementation
 * for the drag and drop functionality.
 */
public class DnDTab extends TabComponent implements DropTargetListener,
                                                    ChangeListener
{
   /**
    * Construct the DnD tab and initialize it accoring to the provided 
    * parameters.
    *
    * @param title the tab title displayed for the user.
    * @param id this string is used as an identifier among multiple tab's. 
    *    The owner of this tab component is responsible to provid a unique id.
    * @param toggleAction the toggle action command string for this tab.
    * @param hideable is this tab hidable.
    * @param saveable is this tabs file saveable.
    * @param loadable is this tabs file loadable.
    */
   public DnDTab(String title, String id, String toggleAction, 
                 boolean hideable, boolean saveable, boolean loadable)
   {
      super(title, id, toggleAction, hideable, saveable, loadable);
      
      initTab();
   }
   
   /**
    * Set the drop target for this tab. This also creates and initializes the 
    * undo/redo manager.
    */
   public void initTab()
   {
      // Create the undo manager and actions
      m_manager = new MonitorableUndoManager();
      m_manager.addChangeListener(this);
      getDocument().addUndoableEditListener(m_manager);

      m_dropTarget = new DropTarget(this, 
                                    DnDConstants.ACTION_COPY_OR_MOVE,   
                                    this, true, null);
      
      // we handle the CTRL-V key ourself, so remove it from the keymap
      KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_V, 
                                             KeyEvent.CTRL_MASK, true);
      this.getKeymap().removeKeyStrokeBinding(key);
      key = KeyStroke.getKeyStroke(KeyEvent.VK_V, 
                                   KeyEvent.CTRL_MASK, false);
      this.getKeymap().removeKeyStrokeBinding(key);
   }
   
   /**
    * Initiate an undo action from the menu for example.
    *
    * @param event the action event
    */
   public void undo(ActionEvent event)
   {
      if (canUndo())
      {
         try
         {
            m_manager.undo();
         }
         catch (CannotUndoException e)
         {
            Toolkit.getDefaultToolkit().beep();
         }
      }
   }
   
   /**
    * Returns the status whether or not this tab has somethings to undo.
    *
    * @return <code>true</code> if there is something to undo, 
    *    <code>false</code> otherwise.
    */
   public boolean canUndo()
   {
      return m_manager.canUndo();
   }
   
   /**
    * Initiate an redo action from the menu for example.
    *
    * @param event the action event
    */
   public void redo(ActionEvent event)
   {
      if (canRedo())
      {
         try
         {
            m_manager.redo();
         }
         catch (CannotRedoException e)
         {
            Toolkit.getDefaultToolkit().beep();
         }
      }
   }
   
   /**
    * Returns the status whether or not this tab has somethings to redo.
    *
    * @return <code>true</code> if there is something to redo, 
    *    <code>false</code> otherwise.
    */
   public boolean canRedo()
   {
      return m_manager.canRedo();
   }

   /**
    * Implementation for the DropTargetListener interface.
    */
   public void dragEnter(DropTargetDragEvent event)
   {
      // Get the type of object being transferred and determine
      // whether it is appropriate.
      checkTransferType(event);

      // Accept or reject the drag.
      acceptOrRejectDrag(event);
   }

   /**
    * Implementation for the DropTargetListener interface.
    */
   public void dragExit(DropTargetEvent event)
   {
      // no-op
   }

   /**
    * Implementation for the DropTargetListener interface.
    */
   public void dragOver(DropTargetDragEvent event)
   {
      // Accept or reject the drag
      acceptOrRejectDrag(event);
   }

   /**
    * Implementation for the DropTargetListener interface.
    */
   public void dropActionChanged(DropTargetDragEvent event)
   {
      // Accept or reject the drag
      acceptOrRejectDrag(event);
   }

   /**
    * Implementation for the DropTargetListener interface.
    */
   public void drop(DropTargetDropEvent event)
   {
      // Check the drop action
      if ((event.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0)
      {
         // Accept the drop and get the transfer data
         event.acceptDrop(event.getDropAction());
         Transferable transferable = event.getTransferable();

         try
         {
            boolean result = false;

            if (m_draggingFile)
            {
               result = dropFile(transferable);
            }
            else 
            {
               result = dropContent(transferable, event);
            }

            event.dropComplete(result);
         } 
         catch (Exception e)
         {
            event.dropComplete(false);
         }
      }
      else
      {
         event.rejectDrop();
      }
   }

   /**
    * This function decides whether or not to reject the provided drag event.
    *
    * @param event the drag event.
    */
   protected boolean acceptOrRejectDrag(DropTargetDragEvent event)
   {
      int dropAction = event.getDropAction();
      int sourceActions = event.getSourceActions();
      boolean acceptedDrag = false;

      // Reject if the object being transferred
      // or the operations available are not acceptable
      if (!m_acceptableType || 
          (sourceActions & DnDConstants.ACTION_COPY_OR_MOVE) == 0)
      {
         event.rejectDrag();
      }
      else if (m_draggingFile  && (!this.isEditable() || !this.isLoadable()))
      {
         // Can't drag text to a non loadable or read-only JTextArea
         event.rejectDrag();
      }
      else if ((dropAction & DnDConstants.ACTION_COPY_OR_MOVE) == 0)
      {
         // Not offering copy or move - suggest a copy
         event.acceptDrag(DnDConstants.ACTION_COPY);
         acceptedDrag = true;
      }
      else
      {
         // Offering an acceptable operation: accept
         event.acceptDrag(dropAction);
         acceptedDrag = true;
      }

      return acceptedDrag;
   }

   /**
    * This function checks the transfer type and sets the local status 
    * according to the result.
    *
    * @param event the event to check.
    */
   protected void checkTransferType(DropTargetDragEvent event)
   {
      // Accept a list of files, or data content that
      // amounts to plain text or a Unicode text string
      m_acceptableType = false;
      m_draggingFile = false;

      if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
      {
         m_acceptableType = true;
         m_draggingFile = true;
      }
      else if (event.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor()) ||
               event.isDataFlavorSupported(DataFlavor.stringFlavor))
      {
         m_acceptableType = true;
      }
   }

   /**
    * This function handles the drop of a new file and updates the this editor
    * accordingly.
    *
    * @param transferable the new file to transfer.
    * @throws IOException for any input/output function failure.
    * @throws UnsupportedFlavorException if the falvor of the transferable 
    *    is not supported.
    */
   protected boolean dropFile(Transferable transferable) throws IOException, 
                                                                UnsupportedFlavorException
   {
      Object transfer = transferable.getTransferData(DataFlavor.javaFileListFlavor);
      if (transfer instanceof List)
      {
         List fileList = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);

         File transferFile = (File) fileList.get(0);
         FileInputStream is = new FileInputStream(transferFile);
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         int read;
         byte[] buf = new byte[1024];
         while ((read = is.read(buf)) >= 0)
         {
            os.write(buf, 0, read);
            if (read < buf.length)
               break;
         }
         os.flush();
         this.setText(os.toString());
         this.setFileName(transferFile.getAbsolutePath());
         MainFrame mainFrame = getMainframe();
         if (mainFrame != null)
            mainFrame.setTitle();

         //  new file loaded so forget all previous edits
         discardAllEdits();

         return true;
      }
      
      return false;
   }
   
   /**
    * Get the mainframe.
    *
    * @return the mainframe if available, null otherwise.
    */
   private MainFrame getMainframe()
   {
      Container parent = this.getParent();
      while (parent != null)
      {
         if (parent instanceof MainFrame)
            return (MainFrame) parent;

         parent = parent.getParent();
      }
      
      return null;
   }
   
   /**
    * Discard all edits for the undo/redoo manager.
    */
   public void discardAllEdits()
   {
      m_manager.discardAllEdits();
   }

   /**
    * 
    */
   // This method handles a drop with data content
   protected boolean dropContent(Transferable transferable, 
                                 DropTargetDropEvent event)
   {
      if (!this.isEditable())
      {
         // Can't drop content on a read-only text control
         return false;
      }

      try
      {
         // Check for a match with the current content type
         DataFlavor[] flavors = event.getCurrentDataFlavors();

         DataFlavor selectedFlavor = null;

         // Look for either plain text or a String.
         for (int i = 0; i < flavors.length; i++)
         {
            DataFlavor flavor = flavors[i];
            if (flavor.equals(DataFlavor.getTextPlainUnicodeFlavor()) ||
                flavor.equals(DataFlavor.stringFlavor))
            {
               selectedFlavor = flavor;
               break;
            }
         }

         if (selectedFlavor == null)
         {
            // No compatible flavor - should never happen
            return false;
         }

         // Get the transferable and then obtain the data
         Object data = transferable.getTransferData(selectedFlavor);

         String insertData = null;
         if (data instanceof InputStream)
         {
            // Plain text flavor
            String charSet = selectedFlavor.getParameter("charset");
            InputStream is = (InputStream) data;
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            try
            {
               insertData = new String(bytes, charSet);
            }
            catch (UnsupportedEncodingException e)
            {
               // Use the platform default encoding
               insertData = new String(bytes);
            }
         }
         else if (data instanceof String)
         {
            // String flavor
            insertData = (String)data;
         }

         if (insertData != null)
         {
            int selectionStart = this.getCaretPosition();
            replaceSelection(insertData);
            select(selectionStart,   selectionStart + insertData.length());
            return true;
         }
         return false;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   /**
    * Implementation for the change listener.
    */
   public void stateChanged(ChangeEvent event)
   {
      MainFrame mainFrame = getMainframe();
      if (mainFrame != null)
         mainFrame.updateMenu();
   }

   /**
    * The drop target for this object.
    */
   protected DropTarget m_dropTarget = null;
   /**
    * Indicator if the transferable type is acceptable for this object.
    */
   protected boolean m_acceptableType = false;
   /**
    * Is <code>true</code> if we drag an entire file, 
    * <code>false</code> otherwise.
    */
   protected boolean m_draggingFile = false;
   /**
    * The undo manager which controls the undo/redo functionality for this
    * editor.
    */
   private MonitorableUndoManager m_manager = null;
}
