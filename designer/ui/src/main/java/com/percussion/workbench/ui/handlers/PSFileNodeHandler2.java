package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>Temporary class to extract common functionality for
 * {@link PSLocalFileSystemNodeHandler} and {@link PSConfigNodeHandler}.
 * To be merged with {@link PSFileNodeHandler}.
 *
 * <p>Note, see {@link PSLocalFileSystemNodeHandler} how to extract file-specific
 * images.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSFileNodeHandler2 extends PSDeclarativeNodeHandler
{
   /**
    * Required by framework.
    */
   public PSFileNodeHandler2(Properties props, String iconPath, PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   //see base class method for details
   @Override
   public Map<Transfer, Object> handleCopy(Collection<PSUiReference> nodes)
   {
      Map<Transfer, Object> results = new HashMap<Transfer, Object>();
      for (Transfer t : getCustomTransfers())
      {
         results.put(t, getData(t, nodes));
      }
      return results;
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
   public Transfer[] getSourceTransfers(Collection<PSUiReference> nodes)
   {
      //Make sure getDragHandler is consistent w/ the types here
      Transfer[] baseTransfers = super.getSourceTransfers(nodes);
      Transfer[] transfers = new Transfer[baseTransfers.length
            + getCustomTransfers().length];
      System.arraycopy(baseTransfers, 0, transfers, 0, baseTransfers.length);
      System.arraycopy(getCustomTransfers(), 0, transfers, baseTransfers.length,
            getCustomTransfers().length);
      return transfers;
   }

   /**
    * Builds an object for the supplied type that can be used for clipboard/dnd
    * operations.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @param nodes Assumed not <code>null</code> or empty.
    * 
    * @return The generated object. Never <code>null</code>.
    */
   private Object getData(Transfer type, Collection<PSUiReference> nodes)
   {
      assert(type != null);
      assert(nodes != null && !nodes.isEmpty());

      Object result = null;
      if (type.equals(FileTransfer.getInstance()))
      {
         String[] paths = new String[nodes.size()];
         int i = 0;
         for (PSUiReference node : nodes)
         {
            paths[i++] = getFileFromRef(node).getAbsolutePath();
         }
         result = paths;
      }
      else
      {
         throw new IllegalStateException("Unknown transfer type: "
               + type.getClass().getName());
      }
      return result;      
   }

   /**
    * Transfers custom to this handler.
    */
   protected Transfer[] getCustomTransfers()
   {
      return new Transfer[] {FileTransfer.getInstance()};
   }
   
   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @Override
   @SuppressWarnings("unused")
   public boolean supportsDelete(PSUiReference node)
   {
      return false;
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsPaste(PSUiReference node)
   {
      return false;
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsRename(PSUiReference node)
   {
      return false;
   }

   /**
    * Handles file Drag and Drop.
    */
   protected class FileDragHandler extends NodeDragHandler
   {
      //see base class method for details
      @Override
      public void dragFinished(DragSourceEvent event)
      {
         if (event.detail == DND.DROP_MOVE
               || event.detail == DND.DROP_TARGET_MOVE)
         {
            for (final PSUiReference node : getSelectionParentNodes())
            {
               PSDesignObjectHierarchy.getInstance().refresh(node);
            }
         }
         super.dragFinished(event);
         //fixme delete the temp file created in dragSetData
      }

      /**
       * Retrieves set of parent nodes of the selected nodes.
       */
      private Set<PSUiReference> getSelectionParentNodes()
      {
         final Set<PSUiReference> parentNodes = new HashSet<PSUiReference>();
         for (final PSUiReference node : getSelection())
         {
            if (node.getParentNode() != null)
            {
               parentNodes.add(node.getParentNode());
            }
         }
         return parentNodes;
      }
      
      //see base class method for details
      @Override
      public void dragSetData(DragSourceEvent event)
      {
         Transfer customTransfer = null;
         for (Transfer t : getCustomTransfers())
         {
            if (t.isSupportedType(event.dataType))
            {
               customTransfer = t;
            }
         }
         if (customTransfer != null)
         {
            //this is one we added
            event.data = getData(customTransfer, getSelection());
         }
         else
         {
            super.dragSetData(event);
         }
      }
      
   }

   /**
    * Extracts file name from the provided reference.
    */
   protected abstract File getFileFromRef(final PSUiReference ref);

   /**
    * The handler that manages the drag operations. Created lazily by the
    * {@link #getDragHandler()} method, then never modified.
    */
   private static DragSourceListener ms_dragHandler;
}
