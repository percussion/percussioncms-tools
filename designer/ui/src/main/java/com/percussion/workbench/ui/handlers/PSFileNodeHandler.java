/******************************************************************************
 *
 * [ PSFileNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.dnd.PSReferenceTransfer;
import com.percussion.workbench.ui.model.IPSDropHandler;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * This class adds IFile and IResource support.
 * todo - OK for release - We decided not to add this functionality right now,
 * but are leaving it because we may want to add it in the future
 *
 * @author paulhoward
 */
public class PSFileNodeHandler extends PSDeclarativeNodeHandler
{
   /**
    * Ctor required by framework. Adds <code>FileTransfer</code> as an 
    * allowed type if any allowed types are passed in.
    * 
    * @param props Not used.
    * 
    * @param iconPath Not used
    */
   public PSFileNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
      if (allowedTypes.length > 0)
         addAcceptedTransfer(FileTransfer.getInstance());
   }

   /**
    * Extends base class by preventing links (changed to copy) and handling
    * file drops. 
    *
    * @author paulhoward
    */
   private class FileDropHandler extends NodeDropHandler
   {
      //see base class method for details
      @Override
      public int getValidDndOperation(int desiredOp)
      {
         return desiredOp == DND.DROP_LINK ? DND.DROP_COPY : desiredOp;
      }

      //see base class method for details
      @Override
      public boolean performDrop(PSUiReference target, int op, Object data)
      {
         if (data instanceof Collection)
            return super.performDrop(target, op, data);

         //else a file transfer
         if (op != DND.DROP_COPY || op != DND.DROP_MOVE)
            return false;
         
         assert(data instanceof String[]);
         String[] paths = (String[]) data;
         for (String p : paths)
         {
            System.out.println("Copied: " + p);
         }
         //todo - OK for release - implement
         
         return true;
      }
   }
   
   /**
    * Extends the base class to handle setting the data for file transfers.
    *
    * @author paulhoward
    */
   protected class FileDragHandler extends NodeDragHandler
   {
      /**
       * Overridden to set the file transfer data. Other types are delegated to
       * base class.
       */
      @Override
      public void dragSetData(DragSourceEvent event)
      {
         if (!FileTransfer.getInstance().isSupportedType(event.dataType))
         {
            super.dragSetData(event);
            return;
         }
         
         //handle setting file data
         // todo - OK for release - implement
         //gets called each time the cursor enters an object that accepts the drop type
         System.out.println("Set data for file transfer");
         event.data = new String[] {"c:\\folder\file.txt"};
      }

      /**
       * Overridden to delete the source files/folders if content was moved to
       * local file system.
       */
      @Override
      public void dragFinished(DragSourceEvent event)
      {
         // todo - OK for release - implement
         //delete the resources on the server and delete the temp files 
         super.dragFinished(event);
      }
   }
   
   //see base class method for details
   @Override
   public DragSourceListener getDragHandler()
   {
      if (ms_dragHandler == null)
         ms_dragHandler = new FileDragHandler(); 
      return ms_dragHandler;
   }

   //see base class method for details
   @Override
   public IPSDropHandler getDropHandler()
   {
      if (ms_dropHandler == null)
         ms_dropHandler = new FileDropHandler(); 
      return ms_dropHandler;
   }

   /**
    * Adds file transfer type to all other supported types.
    */
   @Override
   public Transfer[] getSourceTransfers(Collection<PSUiReference> nodes)
   {
      Transfer[] t = super.getSourceTransfers(nodes);
      Transfer[] all = new Transfer[t.length+1];
      System.arraycopy(t, 0, all, 0, t.length);
      all[t.length] = FileTransfer.getInstance();
      return all;
   }

   //see base class method for details
   @Override
   public Image getLabelImage(PSUiReference node)
   {
      // todo - OK for release - get icon based on file ext
      return super.getLabelImage(node);
   }

   //see base class method for details
   @Override
   public Map<Transfer, Object> handleCopy(Collection<PSUiReference> nodes)
   {
      Map<Transfer, Object> results = super.handleCopy(nodes);
      //todo - OK for release - add for file type
      Transfer t = FileTransfer.getInstance();
      String[] paths = {"c:\\folder1", "c:\\folder2\file1.txt"};
      results.put(t, paths);
      return results;
   }

   //see base class method for details
   @Override
   public void handlePaste(PSUiReference parent, Map<Transfer, Object> cbData)
   {
      Map<Transfer, Object> refData = new HashMap<Transfer, Object>();
      Object fileData = null;
      //filter out the file type
      for (Iterator<Transfer> iter = cbData.keySet().iterator(); iter.hasNext();)
      {
         Transfer t = iter.next();
         if (t instanceof PSReferenceTransfer)
            refData.put(t, cbData.get(t));
         else if (t instanceof FileTransfer)
            fileData = cbData.get(t);
         else
            assert(false);
      }
      super.handlePaste(parent, refData);
      
      if (!refData.isEmpty())
         return;  //the file type is identical to the ref type
      assert(fileData != null);
      //paste copy from file system
      // todo - OK for release - implement
      for (String s : (String[]) fileData)
      {
         System.out.println("Copied: " + s);
      }
   }

   /**
    * The handler that manages the drop operations. Created lazily by the
    * {@link #getDropHandler()} method, then never modified.
    */
   private static IPSDropHandler ms_dropHandler;

   /**
    * The handler that manages the drag operations. Created lazily by the
    * {@link #getDragHandler()} method, then never modified.
    */
   private static DragSourceListener ms_dragHandler;
}
