/******************************************************************************
 *
 * [ PSDeclarativeNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.client.objectstore.PSUiItemDefinition;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.dnd.PSTransferFactory;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler.HandlerOptions.HandlerOptionSet;
import com.percussion.workbench.ui.model.IPSDropHandler;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * This class implements default behaviors that are object independent.
 *
 * @author paulhoward
 */
public class PSDeclarativeNodeHandler implements
      IPSDeclarativeNodeHandler
{
   /**
    * If this handler allows USER_FILE folders and a non-empty property by this
    * name is found in the props, its value is used to sub-type the folder
    * <code>Transfer</code>. This prevents dropping of folders from different
    * trees onto this folder tree.
    */
   public static final String FOLDER_ROOT_PROPNAME = "userPathRootName";

   /**
    * Ctor required by framework.
    * 
    * @param props May be <code>null</code>. Accessible by derived classes
    * using {@link #getProperty(String)}. If there is a property of the form
    * <i>primaryType</i>SubType, where <i>primaryType</i> is the name of the
    * {@link com.percussion.client.IPSPrimaryObjectType} enum, then the value of
    * this property will be used as the transfer sub-type for this type.
    * <p>
    * See {@link #FOLDER_ROOT_PROPNAME} for details of another specific property
    * that affects the behavior of this class.
    * 
    * @param iconPath May be <code>null</code> or empty. If so, the default
    * behavior of this class is to provide no image.
    * 
    * @param allowedTypes These are used to calculate the default types that
    * will be accepted from a drop action (i.e. will be returned by the
    * {@link #getAcceptedTransfers()} method. May be <code>null</code> or
    * empty.
    */
   public PSDeclarativeNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      if (StringUtils.isEmpty(iconPath))
         iconPath = null;
      m_iconPath = iconPath;
      
      m_props = new Properties();
      if (props != null)
      {
         Enumeration e = props.propertyNames();
         while (e.hasMoreElements())
         {
            String name = e.nextElement().toString();
            m_props.setProperty(name.toLowerCase(), props.getProperty(name));
         }
      }
      
      if (allowedTypes != null)
      {
         m_acceptedTypes = new Transfer[allowedTypes.length];
         int i = 0;
         for (PSObjectType type : allowedTypes)
         {
            m_acceptedTypes[i++] = PSTransferFactory.getInstance()
                  .getTransfer(type, getTransferSubType(type)); 
         }
      }
      else
         m_acceptedTypes = new Transfer[0];
   }

   /**
    * Two handlers are considered equal if they are instances of the same class
    * as determined by comparing the class names.
    */
   @Override
   public boolean equals(Object other)
   {
      return getClass().getName().equals(other.getClass().getName());
   }
   
   //see Object
   @Override
   public int hashCode()
   {
      return getClass().getName().hashCode();
   }
   
   /**
    * @inheritDoc
    * The default implementation doesn't know how to adapt anything.
    * 
    * @return Always <code>null</code>.
    * @see org.eclipse.core.runtime.IAdapterFactory
    */
   @SuppressWarnings("unused")
   public Object getAdapter(Object node, Class adaptTo)
   {
      return null;
   }

   /**
    * @inheritDoc
    * The default implementation doesn't know how to adapt anything.
    * 
    * @return Always an empty list.
    * @see org.eclipse.core.runtime.IAdapterFactory
    */
   public Class[] getAdapterList()
   {
      return new Class[0];
   }
   
   //see interface
   public Image getLabelImage(PSUiReference node)
   {
      Image icon = null;
      if (m_iconPath == null)
      {
         IPSReference ref = node.getReference();
         if ( ref != null && isFileType(ref.getObjectType()))
         {
            ImageDescriptor d = PlatformUI.getWorkbench().getEditorRegistry()
                  .getImageDescriptor(ref.getName());
            //fixme - register it
            icon = d.createImage(true);
            return icon;
         }
         return PSUiUtils.getSharedImage(node.isFolder()
               ? ISharedImages.IMG_OBJ_FOLDER
               : ISharedImages.IMG_OBJ_FILE);
      }
      return PSUiUtils.getImage(m_iconPath);
   }
   
   /**
    * Checks if the supplied type is based on file content.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the type has file-based content,
    * <code>false</code> otherwise.
    */
   private boolean isFileType(PSObjectType type)
   {
      //todo - OK for release - should this be part of the PSObjectTypes?
      if (type.getSecondaryType() != null &&
            type.getSecondaryType().equals(PSObjectTypes.FileSubTypes.FILE))
      {
         return true;
      }
      
      IPSPrimaryObjectType primary = (IPSPrimaryObjectType) type.getPrimaryType();
      if (primary.equals(PSObjectTypes.CONFIGURATION_FILE)
            || primary.equals(PSObjectTypes.CONTENT_EDITOR_CONTROLS)
            || primary.equals(PSObjectTypes.LEGACY_CONFIGURATION))
      {
         return true;
      }
      
      return false;
   }

   /**
    * @inheritDoc
    * Default behavior is to not accept any type of object.
    * 
    * @return Never <code>null</code>, always empty.
    */
   public Transfer[] getAcceptedTransfers()
   {
      return m_acceptedTypes.clone();
   }

   /**
    * @inheritDoc The default implementation calls
    * <code>PSTransferFactory.getInstance().getTransfer(type,
    * getTransferSubType(type))</code>.
    */
   public Transfer getTransfer(PSObjectType type)
   {
      //verify the type is known to this handler
      return PSTransferFactory.getInstance().getTransfer(type,
            getTransferSubType(type));
   }

   /**
    * The behavior depends on the supplied type. See the description of
    * <code>props</code> in the ctor to see how different properties affect
    * the behavior of this method.
    * 
    * @return May be <code>null</code>, never empty.
    */
   private String getTransferSubType(PSObjectType type)
   {
      if (type.getPrimaryType().equals(PSObjectTypes.USER_FILE))
      {
         String folderRoot = getProperty(FOLDER_ROOT_PROPNAME);
         if (StringUtils.isBlank(folderRoot))
         {
            folderRoot = null;
         }
         return folderRoot;
      }
      String objectSubType = getProperty(type.getPrimaryType().name()
            + "subType");
      if (StringUtils.isBlank(objectSubType))
         return null;
      return objectSubType;
   }

   /**
    * Adds the supplied transfer to the list of existing transfers.
    * @param t Never <code>null</code>.
    */
   protected void addAcceptedTransfer(Transfer t)
   {
      if (null == t)
      {
         throw new IllegalArgumentException("t cannot be null");  
      }
      Transfer[] tmp = m_acceptedTypes;
      m_acceptedTypes = new Transfer[m_acceptedTypes.length+1];
      System.arraycopy(tmp, 0, m_acceptedTypes, 0, tmp.length);
      m_acceptedTypes[tmp.length] = t;
   }
   
   /**
    * @inheritDoc
    * 
    * If you override this method and replace or add additional transfers, you
    * must supply a derived instance of {@link NodeDragHandler} that overrides
    * the <code>dragSetData</code> method and override
    * {@link #handleCopy(Collection)} to manage any transfer type(s) you added.
    * Other types can be passed to the base class for processing.
    */
   public Transfer[] getSourceTransfers(Collection<PSUiReference> nodes)
   {
      if (nodes == null || nodes.isEmpty())
         return new Transfer[0];
      
      Set<Transfer> transfers = new HashSet<Transfer>();
      for (PSUiReference node : nodes)
      {
         assert(node.getObjectType() != null);
         transfers.add(getTransfer(node.getObjectType()));
      }
      return transfers.toArray(new Transfer[transfers.size()]);
   }

   /**
    * @inheritDoc
    * The default implementation does nothing with the value.
    */
   @SuppressWarnings("unused")
   public void setOwningNode(PSUiReference owner)
   {}

   /**
    * @inheritDoc
    * 
    * Default implementation does nothing.
    */
   @SuppressWarnings("unused")
   public void handleOpen(IWorkbenchSite site, PSUiReference ref) {}

   /**
    * The default implementation of a drag handler for the declarative 
    * hierarchy model.
    *
    * @author paulhoward
    */
   protected class NodeDragHandler extends DragSourceAdapter 
   {
      // see interface
      @SuppressWarnings("unused")
      @Override
      public void dragFinished(DragSourceEvent event)
      {
         m_dragSelection.clear();
         ms_logger.debug("Drag finished");
      }

      /**
       * The default behavior builds an array of all {@link IPSReference}
       * objects found in the current selection when the
       * {@link #dragStart(DragSourceEvent)} method is called and sets that on
       * the supplied event's <code>data</code> property as
       * <code>IPSReference[]</code>.
       * <p>
       * If no Transfer can be found for the dataType of this event, a message
       * is logged and the method returns.
       */
      @Override
      public void dragSetData(DragSourceEvent event)
      {
         Transfer t = PSTransferFactory.getInstance().getTransfer(
               event.dataType);
         if (t == null)
         {
            PSWorkbenchPlugin.getDefault().log(
                  "Unexpected data type during DnD: "
                        + event.dataType.getClass().getName());
            return;
         }
         Collection<PSUiReference> data = new ArrayList<PSUiReference>();
         data.addAll(m_dragSelection);
         event.data = data;
      }

      /**
       * Gets the current selection from the workbench selection service and
       * gets the <code>IPSReference</code>s from all the selected nodes, 
       * saving them into a local collection for possible later use. 
       */
      @Override
      public void dragStart(DragSourceEvent event)
      {
         /* even though some of these methods are doc'd that they can return 
          * null, that shouldn't happen because of the current operation, so
          * we don't need to check
          */
         ISelection sel = PlatformUI.getWorkbench()
               .getActiveWorkbenchWindow().getActivePage().getSelection();
         event.doit = false;
         if (!(sel instanceof IStructuredSelection))
         {
            //shouldn't happen
            return;
         }
         IStructuredSelection ssel = (IStructuredSelection) sel;
         Collection<PSUiReference> nodes = new ArrayList<PSUiReference>();
         for (Object o : ssel.toList())
         {
            if (!(o instanceof PSUiReference))
            {
               PSWorkbenchPlugin.getDefault().log(
                     "Unexpected node type in selection set: "
                           + o.getClass().getName());
               return;
            }
            PSUiReference node = (PSUiReference) o;
            if (node.getReference() == null)
            {
               PSWorkbenchPlugin.getDefault().log(
                     "Design object ref is null in node: "
                           + node.getName());
               return;
            }
            nodes.add(node);
         }
         if (!canStartDrag(nodes))
            return;
         m_dragSelection.addAll(nodes);
         event.doit = true;
      }

      /**
       * Derived classes can override this method to prevent a drag from starting
       * based on the selection.
       * 
       * @param nodes Will never be <code>null</code>.
       * 
       * @return The default implementation always returns <code>true</code>.
       */
      protected boolean canStartDrag(
            @SuppressWarnings("unused") Collection<PSUiReference> nodes)
      {
         return true;
      }

      /**
       * Two instances are considered equal if they both are instances of this
       * class.
       */
      @Override
      public boolean equals(Object other)
      {
         if (other == this)
            return true;
         if (other == null)
            return false;
         return other instanceof NodeDragHandler;
      }

      /**
       * Matches <code>equals</code> method.
       */
      @Override
      public int hashCode()
      {
         return getClass().hashCode();
      }
      
      /**
       * The set of nodes that were selected when the drag started.
       * 
       * @return The set owned by this class. Do not modify it.
       */
      protected Collection<PSUiReference> getSelection()
      {
         return m_dragSelection;
      }
      
      /**
       * Filled in when <code>dragStart</code> is called by querying the
       * selection service and getting the design object wrapper nodes from the
       * selected nodes. It is cleared by the <code>dragFinished</code>
       * method. Never <code>null</code>.
       */
      private Collection<PSUiReference> m_dragSelection = 
         new ArrayList<PSUiReference>();
   }
   
   /**
    * The default implementation of a drop handler for the declarative 
    * hierarchy model.
    *
    * @author paulhoward
    */
   protected class NodeDropHandler implements IPSDropHandler
   {
      /**
       * @inheritDoc 
       * Checks for accepted transfers and if none are found,
       * <code>DND.DROP_NONE</code> is returned. Otherwise, if the desired op
       * is <code>DND.DROP_LINK</code>, <code>DND.DROP_MOVE</code> is
       * returned. Otherwise, the input is returned.
       * 
       * @return One of the <code>DND.DROP_xxx</code> values.
       */
      public int getValidDndOperation(int desiredOp)
      {
         if (getAcceptedTransfers().length == 0)
            return DND.DROP_NONE;
         if (desiredOp == DND.DROP_LINK)
            return DND.DROP_COPY;
         return desiredOp;
      }

      /**
       * See
       * {@link IPSDropHandler#validateDrop(PSUiReference, int, TransferData) 
       * interface} for description.
       * 
       * @return The default implementation always returns <code>true</code>.
       */
      @SuppressWarnings("unused")
      public boolean validateDrop(PSUiReference target, int operation,
            TransferData transferType)
      {
         return true;
      }

      /**
       * Supports copy or move. 
       */
      @SuppressWarnings("unchecked")
      public boolean performDrop(PSUiReference target, int op, Object data)
      {
         ms_logger.debug("Drop performed");
         if (!(data instanceof Collection))
            return false;
         Collection<PSUiReference> nodes = (Collection<PSUiReference>) data;
         if (op == DND.DROP_COPY)
         {
            if (isHierarchicalOperation(target, nodes))
            {
               processHierarchicalObjectPaste(target, nodes);
            }
            else
            {
               processFolderPaste(target, nodes);
            }
            /* Whether to return true or false on partial success is a question,
             * but I decided to always return true.
             */
            return true;
         }
         
         // they want a move
         try
         {
            PSDesignObjectHierarchy model = PSDesignObjectHierarchy.getInstance();
            List<PSUiReference> toMove = new ArrayList<PSUiReference>();
            toMove.addAll((Collection<PSUiReference>) data);
            if (isCrossModelMove(target, toMove))
            {
               assert(isHierarchicalOperation(target, toMove));
               processHierarchicalObjectPaste(target, toMove);
               handleDelete(toMove);
            }
            else
               model.move(target, toMove);            
         }
         catch (PSModelException e)
         {
            PSUiUtils.handleExceptionSync("DnD Drop", null, null, e);
            return false;
         }
         catch (PSMultiOperationException e)
         {
            PSUiUtils.handleExceptionSync("DnD Drop", null, null, e);
            return false;
         }
         catch (UnsupportedOperationException e)
         {
            PSUiUtils.handleExceptionSync("DnD Drop", null, null, e);
            return false;
         }
         return true;
      }

      /**
       * Looks at the object types of the target and children. Assumes all 
       * children come from the same model.
       * 
       * @param target Assumed not <code>null</code>.
       * 
       * @param toMove Assumed not <code>null</code>.
       * 
       * @return <code>true</code> If the children and target come from
       * different trees, <code>false</code> otherwise.
       */
      private boolean isCrossModelMove(PSUiReference target,
            Collection<PSUiReference> toMove)
      {
         if (toMove.isEmpty())
            return false;
         if (target.getReference() == null)
            return false;
         
         Enum secondary = target.getObjectType().getSecondaryType();
         if (secondary != null
               && secondary
                     .equals(PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER))
         {
            return false;
         }
         return !target.getObjectType().getPrimaryType().equals(
               toMove.iterator().next().getObjectType().getPrimaryType());
      }

      /**
       * Two instances are considered equal if they both are instances of this
       * class.
       */
      @Override
      public boolean equals(Object other)
      {
         if (other == this)
            return true;
         if (other == null)
            return false;
         return other instanceof NodeDropHandler;
      }

      /**
       * Matches <code>equals</code> method.
       */
      @Override
      public int hashCode()
      {
         return getClass().hashCode();
      }
   }

   /**
    * @inheritDoc
    * Default behavior returns a {@link NodeDragHandler}.
    * 
    * @return Never <code>null</code>.
    */
   public DragSourceListener getDragHandler()
   {
      return new NodeDragHandler();
   }

   /**
    * @inheritDoc Default behavior returns a {@link NodeDropHandler}.
    * 
    * @return If {@link #getAcceptedTransfers()} returns an array length greater
    * than 0 and the PASTEABLE option is enabled, then a valid handler is
    * returned, otherwise, <code>null</code> is returned.
    */
   public IPSDropHandler getDropHandler()
   {
      return (m_options.isOptionSet(HandlerOptions.PASTEABLE) 
         && getAcceptedTransfers().length > 0) ? new NodeDropHandler() : null;
   }

   //see interface for details
   public boolean isAcceptedType(IPSReference ref)
   {
      Transfer test = getTransfer(ref.getObjectType());
      for (Transfer t : getAcceptedTransfers())
      {
         if (t.equals(test))
            return true;
      }
      return false;
   }

   //see interface
   public HandlerOptionSet getConfiguration()
   {
      return HandlerOptions.COPYABLE.new HandlerOptionSet(m_options);
   }
   
   // see interface
   public void configure(HandlerOptionSet options)
   {
      m_options = options;
   }

   /**
    * Checks the supplied option against the list of supported options.
    * 
    * @param o Never <code>null</code>.
    * @return <code>true</code> if it is enabled, <code>false</code> otherwise.
    */
   protected boolean isOptionEnabled(HandlerOptions o)
   {
      if (null == o)
      {
         throw new IllegalArgumentException("o cannot be null");  
      }
      return m_options.isOptionSet(o);
   }
   
   /**
    * @inheritDoc
    * 
    * @return <code>true</code> if configured for it, else <code>false</code>.
    */
   @SuppressWarnings("unused")
   public boolean supportsSecurity(PSUiReference node)
   {
      return isOptionEnabled(HandlerOptions.SECURABLE);
   }

   /**
    * @inheritDoc
    * 
    * @return <code>true</code> if this handler has a drag handler (determined
    * by calling the {@link #getDragHandler()} method) and that option is
    * enabled, <code>false</code> otherwise. This behavior generally means
    * derived classes do not need to override this method.
    */
   @SuppressWarnings("unused")
   public boolean supportsCopy(PSUiReference node)
   {
      return isOptionEnabled(HandlerOptions.COPYABLE)
            && (getDragHandler() != null);
   }

   /**
    * @inheritDoc
    * @param node Not used by this class.
    * @return <code>true</code> if the option is enabled, <code>false</code>
    * otherwise.
    */
   @SuppressWarnings("unused")
   public boolean supportsDelete(PSUiReference node)
   {
      return isOptionEnabled(HandlerOptions.DELETABLE);
   }

   /**
    * @inheritDoc
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   public boolean supportsOpen(PSUiReference node)
   {
      return false;
   }

   /**
    * Walks the branch of which the supplied node is a member until a node that
    * wraps a 'non reference' design object is found. The supplied node is
    * checked as well.
    * 
    * @param startNode Start search at this node,inclusive. Never
    * <code>null</code>.
    * 
    * @param type If provided, only a reference that matches this type will be
    * returned.
    * 
    * @return The closest matching ancestor (or the supplied node), or
    * <code>null</code> if not found.
    */
   public PSUiReference getReferenceAncestor(PSUiReference startNode,
         PSObjectType type)
   {
      if (null == startNode)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      
      PSUiReference ancestor = startNode;
      while (ancestor != null)
      {
         if (ancestor.getObjectType() != null && ancestor.isReference())
         {
            if (type != null && ancestor.getObjectType().equals(type))
               break;
         }
         ancestor = ancestor.getParentNode();
      }
      return ancestor;
      
   }
   
   /**
    * Walks the branch of which the supplied node is a member until a node that
    * wraps a design object is found. The supplied node is checked first.
    * 
    * @param node Never <code>null</code>.
    * 
    * @param instance If <code>true</code>, find the closest ancestor that is
    * an instance of the design object, otherwise, find the closest ancestor
    * that is a reference.
    * 
    * @return The closest ancestor that is a design object and whose
    * <code>isReference()</code> method returns <code>!instance</code>. May be
    * <code>null</code> if not found or may be the supplied node.
    */
   public PSUiReference getAncestor(PSUiReference node, boolean instance)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      
      PSUiReference ancestor = node;
      while (ancestor != null)
      {
         if (ancestor.getObjectType() != null
               && (instance ? !ancestor.isReference() : ancestor.isReference()))
         {
            break;
         }
         ancestor = ancestor.getParentNode();
      }
      return ancestor;
   }
   
   /**
    * This is available for derived classes to override. When
    * {@link #supportsDelete(PSUiReference)} is called, if the node is a
    * 'referenced design object', then that code searches for an ancestor that
    * is not a referenced object and if found, calls this method on that
    * handler, supplying the original node.
    * 
    * @param node Never <code>null</code>.
    * 
    * @return <code>true</code> the the parent's handler can process a delete
    * request, <code>false</code> otherwise. The default implementation always
    * returns <code>false</code>.
    */
   protected boolean supportsDeleteAssociation(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      return false;
   }
   
   /**
    * @inheritDoc
    * 
    * @return <code>true</code> if this handler has a drop handler (determined
    * by calling the {@link #getDropHandler()} method) and the option is
    * enabled, <code>false</code> otherwise. This behavior generally means
    * derived classes do not need to override this method.
    */
   @SuppressWarnings("unused")
   public boolean supportsPaste(PSUiReference node)
   {
      return (getDropHandler() != null)
            && isOptionEnabled(HandlerOptions.PASTEABLE);
   }

   /**
    * @inheritDoc
    * 
    * @return If the supplied node has a design object reference that is not
    * read-only, and the option is enabled, <code>true</code> is returned,
    * otherwise, <code>false</code> is returned.
    */
   public boolean supportsRename(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      boolean result = true;
      if (node.getReference() == null || node.getReference().isReadOnly()
            || !isOptionEnabled(HandlerOptions.RENAMABLE))
      {
         result = false;
      }
      return result;
   }

   //see interface
   public Map<Transfer, Object> handleCopy(Collection<PSUiReference> nodes)
   {
      Collection<PSUiReference> objectNodes = new ArrayList<PSUiReference>();
      Collection<PSUiReference> folderNodes = new ArrayList<PSUiReference>();
      boolean containsFolder = false;
      boolean differentParents = false;
      boolean differentTypes = false;
      PSUiReference parent = null;
      Enum primaryType = null;
      PSObjectType folderType = null;
      for (PSUiReference node : nodes)
      {
         IPSReference ref = node.getReference();
         if (ref == null)
            continue;
         if (node.isFolder())
         {
            containsFolder = true;
            folderNodes.add(node);
            if (folderType == null)
               folderType = ref.getObjectType();
            else if (!folderType.equals(ref.getObjectType()))
            {
               differentTypes = true;
               break;
            }
         }
         else
         {
            objectNodes.add(node);
         
            if (primaryType == null)
               primaryType = ref.getObjectType().getPrimaryType();
            else if (!primaryType.equals(ref.getObjectType().getPrimaryType()))
            {
               differentTypes = true;
               break;
            }
         }
         if (parent == null)
            parent = node.getParentNode();
         if (parent != node.getParentNode())
            differentParents = true;
      }
      
      //these conditions should never happen, just being safe
      if (differentParents && containsFolder)
      {
         PSWorkbenchPlugin.getDefault().log(
               "Selection contained folders and all selected items didn't have the same parent.");
         return null;
      }
      if (differentTypes)
      {
         PSWorkbenchPlugin.getDefault().log(
               "Selection contained objects of different primary types, which is not supported.");
         return null;
      }

      Map<Transfer, Object> results = new HashMap<Transfer, Object>();
      Collection<String> transferPaths = new ArrayList<String>();
      if (objectNodes.size() > 0)
      {
         //there may be multiple secondary types present
         //group the nodes by secondary type
         Map<PSObjectType, Collection<PSUiReference>> groupedNodes = 
            new HashMap<PSObjectType, Collection<PSUiReference>>();
         for (PSUiReference node : objectNodes)
         {
            transferPaths.add(node.getPath());
            PSObjectType type = node.getObjectType();
            Collection<PSUiReference> nodeGroup = groupedNodes.get(type);
            if (nodeGroup == null)
            {
               nodeGroup = new ArrayList<PSUiReference>();
               groupedNodes.put(type, nodeGroup);
            }
            nodeGroup.add(node);
         }
         for (PSObjectType type : groupedNodes.keySet())
         {
            results.put(getTransfer(type), groupedNodes.get(type));
         }
      }
      if (folderNodes.size() > 0)
      {
         results.put(getTransfer(folderType), folderNodes);
         for (PSUiReference node : folderNodes)
         {
            transferPaths.add(node.getPath());
         }
      }
      if (!transferPaths.isEmpty())
      {
         results.put(TextTransfer.getInstance(), transferPaths.toString());
      }
      return results;
   }
   
   /**
    * @inheritDoc
    * The default behavior is to create copies of the supplied data, wrap them
    * in a new node and add them to the supplied parent. If any of the data
    * objects are {@link com.percussion.client.PSObjectTypes#USER_FILE folders},
    * they are copied recursively.
    * <p>
    * Note that this default behavior is inconsistent w/ the default behavior of
    * the {@link #getAcceptedTransfers()} method.
    */
   public void handlePaste(PSUiReference parent, 
         Map<Transfer, Object> cbData)
   {
      for (Map.Entry<Transfer,Object> entry : cbData.entrySet())
      {
         @SuppressWarnings("unchecked")
         Collection<PSUiReference> nodes = (Collection<PSUiReference>) entry.getValue();
         if (isHierarchicalOperation(parent, nodes))
         {
            processHierarchicalObjectPaste(parent, nodes);
         }
         else
         {
            processFolderPaste(parent, nodes);
         }
      }
   }

   /**
    * @inheritDoc
    * The default behavior is to permanently delete the design objects
    * associated with the supplied nodes.
    */
   public void handleDelete(Collection<PSUiReference> toDelete)
   {
      doHandleDelete(toDelete);
   }
   
   /**
    * Does all the work required by {@link #handleDelete(Collection)}.
    * See that method for param details.
    */
   protected void doHandleDelete(Collection<PSUiReference> toDelete)
   {
      try
      {
         IPSCmsModel model = null;
         Map<Enum, List<PSUiReference>> nodesByModel = 
            new HashMap<Enum, List<PSUiReference>>();
         //sort by model type
         for (PSUiReference node : toDelete)
         {
            ms_logger.debug("Deleting: " + node.getName());
            Enum primary = node.getObjectType().getPrimaryType();
            List<PSUiReference> c = nodesByModel.get(primary);
            if (c == null)
            {
               c = new ArrayList<PSUiReference>();
               nodesByModel.put(primary, c);
            }
            
            c.add(node);
         }
         
         for (Enum type : nodesByModel.keySet())
         {
            model = PSCoreFactory.getInstance().getModel(type);
            List<PSUiReference> nodes = nodesByModel.get(type);
            if (type == PSObjectTypes.USER_FILE)
            {
               //need to delete all objects that are children of these folders
               Collection<IPSReference> objRefs = getDescendentObjectRefs(
                     nodes, null);
               if (!objRefs.isEmpty())
               {
                  IPSCmsModel objModel = objRefs.iterator().next()
                        .getParentModel();
                  objModel.delete(objRefs.toArray(new IPSReference[objRefs
                        .size()]));
               }
            }
            IPSReference[] refs = new IPSReference[nodes.size()];
            for (int i=0; i<nodes.size(); i++)
               refs[i] = nodes.get(i).getReference();
            model.delete(refs);
         }
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
      catch (PSMultiOperationException e)
      {
         PSWorkbenchPlugin.handleException("Object Delete", null, null, e);
      }
   }

   /**
    * Catalogs every supplied folder recursively (depth first) and puts
    * references to all non-folder design objects found as descendents.
    * 
    * @param folderNodes Assumed to be all USER_FILE folders. Assumed not
    * <code>null</code>. If empty, returns immediately.
    * 
    * @param results The first call should pass in <code>null</code>.
    * Otherwise, the found refs are added to this set and it is returned.
    * 
    * @return Never <code>null</code>, may be empty. All entries will have
    * the same primary type.
    * 
    * @throws PSModelException If problems communicating with the server or all
    * the objects don't have the same type.
    */
   private Collection<IPSReference> getDescendentObjectRefs(
         Collection<PSUiReference> folderNodes, Collection<IPSReference> results)
      throws PSModelException
   {
      if (results == null)
         results = new ArrayList<IPSReference>();
      if (folderNodes.isEmpty())
         return results;

      PSDesignObjectHierarchy viewModel = PSDesignObjectHierarchy.getInstance();
      for (PSUiReference folder : folderNodes)
      {
         Collection<PSUiReference> children = viewModel.getChildren(folder);
         Collection<PSUiReference> subFolders = new ArrayList<PSUiReference>();
         Enum primary = null;
         for (PSUiReference child : children)
         {
            if (primary == null)
               primary = child.getObjectType().getPrimaryType();
            if (child.getObjectType().getPrimaryType().equals(
                  PSObjectTypes.USER_FILE))
            {
               subFolders.add(child);
            }
            else
            {
               assert(child.getReference() != null);
               results.add(child.getReference());
            }
         }
         if (!subFolders.isEmpty())
            getDescendentObjectRefs(subFolders, results);
      }
      return results;
   }

   /**
    * @inheritDoc
    * The default behavior of this method is to do nothing.
    */
   @SuppressWarnings("unused")
   public void configureForNode(PSUiReference parent, IPSReference ref)
   {}

   /**
    * Returns one of the properties supplied in the ctor, if found, otherwise, ""
    * is returned.
    * 
    * @param name Never <code>null</code> or empty. The name is
    * case-insensitive.
    * 
    * @return The value of the property that matches the supplied name,
    * otherwise the empty string.
    */
   protected String getProperty(String name)
   {
      if (StringUtils.isEmpty(name))
      {
         throw new IllegalArgumentException("name cannot be null or empty");  
      }
      return m_props.getProperty(name.toLowerCase(), "");
   }

   /**
    * Analyzes the supplied data to determine if this operation is taking place
    * with design objects that have a hierarchical model or a flat model.
    * USER_FILE objects are considered to be a flat model.
    * 
    * @param target Assumed not <code>null</code>.
    * @param nodes Assumed not <code>null</code>.
    * 
    * @return <code>true</code> If either the target (if supplied) or one of
    * the non-<code>null</code> nodes is a non-USER_FILE hierarchy,
    * <code>false</code> otherwise.
    */
   private boolean isHierarchicalOperation(PSUiReference target,
         Collection<PSUiReference> nodes)
   {
      IPSReference targetRef = target.getReference();
      boolean isHierarchical = false;
      if (targetRef != null)
      {
         isHierarchical = targetRef.getParentModel().isHierarchyModel();
         if (isHierarchical)
         {
            return !targetRef.getObjectType().getPrimaryType().equals(
                  PSObjectTypes.USER_FILE);
         }
         return false;
      }
      
      //have to check all children
      for (PSUiReference node : nodes)
      {
         IPSReference ref = node.getReference();
         if (ref != null)
         {
            isHierarchical = ref.getParentModel().isHierarchyModel();
            if (isHierarchical)
            {
               return !ref.getObjectType().getPrimaryType().equals(
                     PSObjectTypes.USER_FILE);
            }
            return false;
         }
      }
      return false;
   }

   /**
    * Performs the necessary steps to copy the supplied nodes to the supplied
    * target in a hierarchical model. The children are processed recursively.
    * 
    * @param target Assumed not <code>null</code>.
    * @param nodes Assumed not <code>null</code> and no <code>null</code> 
    * entries.
    */
   private void processHierarchicalObjectPaste(PSUiReference target,
         Collection<PSUiReference> nodes)
   {
      try
      {
         IPSHierarchyNodeRef targetRef = (IPSHierarchyNodeRef) target.getReference();
         IPSCmsModel model;
         IPSHierarchyManager mgr; 
         if (targetRef == null)
         {
            //pasting to root - not supported for all models
            String treeName = PSDesignObjectHierarchy.getInstance()
                  .getTreeName(target);
            model = PSCoreFactory.getInstance().getHierarchyModel(treeName);
            mgr = model.getHierarchyManager(treeName);
         }
         else
         {
            model = targetRef.getParentModel();
            mgr = model.getHierarchyManager(targetRef);
         }
         
         if (model == null)
            throw new RuntimeException("Model not found");
  
         removeDescendentChildren(nodes);
         
         //check if dropping onto self
         for (PSUiReference node : nodes)
         {
            if (target == node)
            {
               String msg = MessageFormat.format(PSMessages.getString(
                     "PSDeclarativeNodeHandler.dropOnSelf.message"), 
                     target.getPath(), node.getDisplayLabel());
               String title = PSMessages.getString(
                     "PSDeclarativeNodeHandler.dropOnSelf.title");      
               MessageDialog.openError(PSUiUtils.getShell(), title, msg);
               return;
            }
         }
         
         doProcessHierarchicalObjectPaste(mgr, target, nodes, null);
      }
      catch (PSModelException e)
      {
         PSUiUtils.handleExceptionSync("Paste Hier", null, null, e);
      }
      catch (PSMultiOperationException e)
      {
         PSUiUtils.handleExceptionSync("Paste Hier", null, null, e);
      }
      catch (Exception e)
      {
         PSUiUtils.handleExceptionSync("Paste Hier", null, null, e);
      }
   }
   
   /**
    * Processes all nodes in the supplied list, removing any that are
    * descendents of any others.
    * 
    * @param nodes Assumed not <code>null</code>. This list may be modified
    * when this method has returned.
    */
   protected static void removeDescendentChildren(
         Collection<PSUiReference> nodes)
   {
      Collection<PSUiReference> testNodes = new ArrayList<PSUiReference>();
      testNodes.addAll(nodes);
      for (PSUiReference testNode : testNodes)
      {
         if (!testNode.isFolder())
            continue;
         String testPath = testNode.getPath().toLowerCase();
         for (Iterator<PSUiReference> iter = nodes.iterator(); iter.hasNext();)
         {
            String path = iter.next().getPath().toLowerCase();
            if (path.length() > testPath.length() && path.startsWith(testPath)
                  && path.indexOf("/", testPath.length()) > 0)
            {
               iter.remove();
            }
         }
      }
   }
   
   /**
    * For each child, a check is made to determine if an object with the same
    * name is already present. If so, an appropriate message is displayed to the
    * user asking what action to take. Otherwise, the files/folders are copied
    * until an error occurs or it finishes. Stops after the first error.
    * 
    * @param mgr Assumed not <code>null</code>.
    * 
    * @param target The node that will receive the clones. Assumed not
    * <code>null</code>.
    * 
    * @param children The nodes to clone. Assumed not <code>null</code>.
    * 
    * @param replaceFiles If an entry is already present by the same name, and
    * if this value is <code>null</code>, a new name will be created for the
    * clone if it is being pasted to the same folder. Otherwise, this flag
    * controls whether the user is asked to overwrite a file.
    * 
    * @throws PSModelException If any problems communicating with the server or
    * any issues during deletes/creates.
    */
   protected void doProcessHierarchicalObjectPaste(IPSHierarchyManager mgr,
         PSUiReference target, Collection<PSUiReference> children,
         Boolean replaceFiles)
      throws PSModelException, PSMultiOperationException, Exception
   {
      IPSHierarchyNodeRef targetRef = (IPSHierarchyNodeRef) target.getReference();
      Collection<IPSHierarchyNodeRef> targetChildrenRefs = mgr
            .getChildren((IPSHierarchyNodeRef) target.getReference());
      for (PSUiReference node : children)
      {
         boolean skipNode = false;
         boolean folderCollision = false;
         boolean generateName = false;
         IPSHierarchyNodeRef existingRef = null;
         for (IPSHierarchyNodeRef ref : targetChildrenRefs)
         {
            if (ref.getName().equalsIgnoreCase(node.getReference().getName()))
            {
               if (node.isFolder())
               {
                  String msg = MessageFormat.format(PSMessages.getString(
                        "PSDeclarativeNodeHandler.folderExistsWarning.message"), 
                        target.getPath(), node.getDisplayLabel());
                  String title = PSMessages.getString(
                        "PSDeclarativeNodeHandler.folderExistsWarning.title");      
                  int choice = queryUser(title, msg);
                  if (choice == 1)
                  {
                     skipNode = true;
                  }
                  else if (choice == 2)
                  {
                     return;
                  }

                  folderCollision = true;
               }
               else if (!(node.getParentNode() != null && node.getParentNode()
                     .equals(target) && replaceFiles == null)
                     || (replaceFiles != null && !replaceFiles))
               {
                  String msg = MessageFormat.format(PSMessages.getString(
                        "PSDeclarativeNodeHandler.fileExistsWarning.message"),
                        target.getPath(), node.getDisplayLabel());
                  String title = PSMessages.getString(
                        "PSDeclarativeNodeHandler.fileExistsWarning.title");
                  int choice = queryUser(title, msg);
                  if (choice == 1)
                  {
                     skipNode = true;
                  }
                  else if (choice == 2)
                  {
                     return;
                  }
               }
               else if (node.getParentNode() != null && node.getParentNode()
                     .equals(target))
               {
                  generateName = true;
               }
               if (!skipNode)
               {
                  existingRef = ref;
                  break;
               }
            }
         }
         if (skipNode)
            continue;
         
         if (existingRef != null && !folderCollision && !generateName)
         {
            try
            {
               //delete it first
               mgr.removeChildren(Collections.singletonList(existingRef));
            }
            catch (PSMultiOperationException e)
            {
               PSUiUtils.handleExceptionSync("Paste Hier", null, null, e);
            }
         }

         PSDesignObjectHierarchy viewModel = PSDesignObjectHierarchy
               .getInstance();
         if (node.isFolder())
         {
            PSUiReference folderNode = node;
            if (!folderCollision)
            {
               folderNode = viewModel.createFolder(target, node.getName());
            }
            else
            {
               Collection<PSUiReference> n = viewModel.getNodes(existingRef);
               folderNode = n.iterator().next();
            }
            List<PSUiReference> folderChildren = PSDesignObjectHierarchy
                  .getInstance().getChildren(node);
            doProcessHierarchicalObjectPaste(mgr, folderNode, folderChildren, 
                  folderCollision);
         }
         else
         {
            IPSHierarchyNodeRef copy = null;
            String name = node.getName();
            int nameIndex = 0;
            do
            {
               try
               {
                  copy = cloneChildren(mgr, targetRef, node, name);
               }
               catch (PSMultiOperationException e)
               {
                  if (generateName
                        && e.getResults()[0] instanceof PSDuplicateNameException
                        && nameIndex < 40) //arbitrarily stop after 40 attempts
                  {
                     String pattern = "Copy of {0}";
                     if (nameIndex > 0)
                        pattern = "Copy ({1}) of {0}";
                     name = MessageFormat.format(pattern, node.getName(),
                           nameIndex);
                     nameIndex++;
                  }
                  else
                     throw e;
               }
            }
            while (copy == null); 
            viewModel.addChildren(target, new IPSReference[] {copy});
         }
      }
   }   

   /**
    * This method is provided so derived classes can easily extend the support
    * of this class to handle cross-model pastes. After this method returns, the
    * cloned object should be persisted.
    * 
    * @param mgr The manager of the target. Never <code>null</code>.
    * 
    * @param targetRef The node that will be the new parent. May be
    * <code>null</code> to add children to root.
    * 
    * @param node The child being cloned. Never <code>null</code>.
    * 
    * @param name The desired name of the clone. Supply <code>null</code> or
    * empty to use the existing name.
    * 
    * @return Never <code>null</code>.
    * @throws PSMultiOperationException
    */
   protected IPSHierarchyNodeRef cloneChildren(IPSHierarchyManager mgr,
         IPSHierarchyNodeRef targetRef, PSUiReference node, String name)
      throws Exception
   {
      if (null == mgr)
      {
         throw new IllegalArgumentException("mgr cannot be null");  
      }
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      if (targetRef.getObjectType().getPrimaryType().equals(
            node.getObjectType().getPrimaryType()))
      {
         return mgr.cloneChildren(targetRef,
               new IPSHierarchyNodeRef[] { (IPSHierarchyNodeRef) node
               .getReference() }, new String[] { name })[0];
      }

      List<String> names = new ArrayList<String>();
      if (StringUtils.isBlank(name))
         names.add(node.getName());
      else
         names.add(name);
      
      IPSPrimaryObjectType primary = (IPSPrimaryObjectType) targetRef
            .getObjectType().getPrimaryType(); 
      PSObjectType newType = node.isFolder() ? PSObjectTypeFactory.getType(
            primary, PSObjectTypes.FileSubTypes.FOLDER) : PSObjectTypeFactory
            .getType(primary, PSObjectTypes.FileSubTypes.FILE);
      IPSHierarchyNodeRef clone = mgr.createChildren(targetRef, newType,
            names)[0];

      if (node.isFolder())
         return clone;
      
      IPSCmsModel srcModel = node.getReference().getParentModel();
      PSMimeContentAdapter srcData = (PSMimeContentAdapter) srcModel.load(
            node.getReference(), false, false);
      
      IPSCmsModel tgtModel = targetRef.getParentModel();
      PSMimeContentAdapter tgtData = (PSMimeContentAdapter) tgtModel.load(
            clone, true, false);
      
      tgtData.setContent(srcData.getContent());
      tgtData.setCharacterEncoding(srcData.getCharacterEncoding());
      tgtData.setMimeType(srcData.getMimeType());
      tgtModel.save(clone, true);
      return clone;
   }

   /**
    * Pops up a question dialog w/ Proceed, Skip and Cancel buttons. 
    * 
    * @return A value between 0 and 2, inclusive. 0 means proceed was chosen,
    * 1 means skip and 2 means cancel.
    */
   private int queryUser(String title, String msg)
   {
      MessageDialog dlg = new MessageDialog(PSUiUtils.getShell(), 
            title, null, msg, MessageDialog.QUESTION, 
            new String[] { 
               IDialogConstants.PROCEED_LABEL,
               IDialogConstants.SKIP_LABEL, 
               IDialogConstants.CANCEL_LABEL }, 1);
      return dlg.open();
   }
   
   /**
    * Handles the pasting of objects into the tree by creating copies of them.
    * If any errors occur, a message is displayed to the user.
    * 
    * @param parent The node to which the objects should be added. If
    * <code>null</code>, the objects are added to their 'home' node.
    * 
    * @param objectRefs Handles to the objects that are being pasted. Assumed
    * not <code>null</code>.
    * 
    * @return <code>true</code> if all objects successfully created and added
    * to <code>parent</code>, <code>false</code> otherwise. In this case, a
    * message has already been displayed to the user showing the problems.
    */
   private boolean processObjectPaste(PSUiReference parent,
      Collection<IPSReference> objectRefs)
   {
      if (objectRefs.isEmpty())
         return true;
      IPSReference[] refs = null;
      Map<Throwable, IPSReference> errors = new HashMap<Throwable, IPSReference>();
      List<IPSReference> possibleClones = new ArrayList<IPSReference>();
      try
      {
         List<String> names = new ArrayList<String>();
         possibleClones.addAll(objectRefs);

         possibleClones = getCloneNames(possibleClones, names);
         assert (possibleClones != null);
         assert (names.size() == 0 || names.size() == possibleClones.size());
         if (possibleClones.size() > 0)
            refs = createShowBusy(parent, objectRefs, names);
         else
            refs = new IPSReference[0];
      }
      catch (PSMultiOperationException e)
      {
         Collection<IPSReference> results = new ArrayList<IPSReference>();
         int i = 0;
         for (Object o : e.getResults())
         {
            if (o instanceof Throwable)
               errors.put((Throwable) o, possibleClones.get(i));
            else
               results.add((IPSReference) o);
            i++;
         }
         refs = results.toArray(new IPSReference[results.size()]);
      }
      catch (PSModelException e)
      {
         PSUiUtils.handleExceptionSync("Pasting object", null, null, e);
         return false;
      }
      
      // BusyIndicator that was set for this job, looses the cursor when any
      // paint occurs ( in CTypes: getCloneNames() posts a dialog that needs painting.
      // set busy cursor manually on the shells while performing save and reset them
      Display display = Display.getCurrent();
      Cursor cursor = new Cursor(display, SWT.CURSOR_WAIT);
      Shell[] shells = display.getShells();
      for (int i = 0; i < shells.length; i++)
         shells[i].setCursor(cursor);

      for (IPSReference ref : refs)
      {
         try
         {
            PSModelTracker.getInstance().save(ref, true, true);
         }
         catch (Exception e)
         {
            errors.put(e, ref);
         }
      }
      // unset the shell's busy cursor
      for (int i = 0; i < shells.length; i++)
         shells[i].setCursor(null);
      

      if (errors.size() > 0)
      {
         List<Throwable> ex = new ArrayList<Throwable>();
         List<IPSReference> detailRefs = new ArrayList<IPSReference>();
         for (Entry<Throwable, IPSReference> entry : errors.entrySet())
         {
            ex.add(entry.getKey());
            detailRefs.add(entry.getValue());
         }
         PSUiUtils.handleExceptionSync("Pasting object", null, null,
            new PSMultiOperationException(ex.toArray(), detailRefs.toArray()));
      }
      return errors.size() == 0;
   }

   /**
    * Executes the create action and shows the busy indicator during the entire
    * operation. This blocks any further user input until the creation is 
    * complete.
    *  
    * @param parent the node to which the objects should be added. If
    *    <code>null</code>, the objects are added to their 'home' node.
    * @param objectRefs handles to the objects that are being created. Assumed
    *    not <code>null</code>.
    * @param names a list with the new names for the objects created, assumed 
    *    not <code>null</code>.
    * @return the references to the new created objects, never 
    *    <code>null</code>.
    * @throws PSMultiOperationException for multi operation errors.
    * @throws PSModelException for all but multi operation errors.
    */
   private IPSReference[] createShowBusy(final PSUiReference parent,
      final Collection<IPSReference> objectRefs, final List<String> names) 
      throws PSMultiOperationException, PSModelException
   {
      final IPSReference[][] results = new IPSReference[1][];
      final Exception[] exceptions = new Exception[1];

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            try
            {
               results[0] = PSModelTracker.getInstance().create(
                  objectRefs.toArray(new IPSReference[objectRefs.size()]),
                  names.toArray(new String[objectRefs.size()]), parent);
               
               /* If a content type is open for editing, the copy of the type
                * has field entries that should not be present in a persisted
                * object. We have to remove those. Running the demerger does not
                * impact types that are not open for editing.
                * This isn't a great fix, but we are close to release and fixing
                * this correctly would be a much bigger change.
                * @todo - ph -I think the 'right' fix would be to prevent the 
                * ctype editor from screwing up the object in the first place.  
                * It should keep its own copy of the merged field set.
                */
               IPSCmsModel ctypeModel = PSCoreFactory.getInstance().getModel(
                     PSObjectTypes.CONTENT_TYPE);
               for (IPSReference ref : results[0])
               {
                  if (ref.getObjectType() == PSObjectTypeFactory
                        .getType(PSObjectTypes.CONTENT_TYPE))
                  {
                     try
                     {
                        PSUiItemDefinition def = (PSUiItemDefinition) ctypeModel
                              .load(ref, false, false);
                        PSContentEditorDefinition.demergeFields(def);
                     }
                     catch (Exception e)
                     {
                        //this should never happen as the object is in local mem
                        throw new PSModelException(e);
                     }
                  }
               }
               exceptions[0] = null;
            }
            catch (PSMultiOperationException e)
            {
               exceptions[0] = e;
            }
            catch (PSModelException e)
            {
               exceptions[0] = e;
            }
         }
      });
      
      if (exceptions[0] instanceof PSMultiOperationException)
         throw (PSMultiOperationException) exceptions[0];
      else if (exceptions[0] instanceof PSModelException)
         throw (PSModelException) exceptions[0];
      
      return results[0];
   }
   
   /**
    * Provided for derived classes to query the user for a name when an object
    * is cloned. The returned value is supplied to the
    * {@link IPSCmsModel#create(IPSReference[], String[])} method.
    * 
    * @param clones The list of possible clones. 0 or more of these should be
    * returned. Only the returned ones will be cloned. The supplied list can be
    * modified and returned.
    * 
    * @param names There should be no entries, or an entry for each value in the
    * returned list. The entries may be <code>null</code> to use the default
    * naming scheme for the clone at the corresponding index.
    *  
    * @return The default impl returns the supplied <code>clones</code> and
    * doesn't add any entries to <code>names</code>.
    */
   protected List<IPSReference> getCloneNames(List<IPSReference> clones,
         @SuppressWarnings("unused") List<String> names)
   {
      return clones;
   }

   /**
    * Handles the pasting of a USER_FILE folder into the tree by copying them
    * recursively. Any objects found in the tree are copied as well. It is
    * assumed that all validity checks have already been performed.
    * 
    * @param parent Becomes the parent of the copies made of the supplied nodes.
    * Assumed not <code>null</code>.
    * 
    * @param nodes The objects and folders to paste. Folders are processed
    * recursively. Assumed not <code>null</code> and that all entries have the
    * same parent.
    */
   private void processFolderPaste(final PSUiReference parent,
      final Collection<PSUiReference> nodes)
   {
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            try
            {
               IPSHierarchyNodeRef curParentRef = 
                  (IPSHierarchyNodeRef) parent.getReference();
               IPSCmsModel folderModel = PSCoreFactory.getInstance().getModel(
                  PSObjectTypes.USER_FILE);
               List<String> names = new ArrayList<String>();
               Collection<IPSReference> objectRefs = 
                  new ArrayList<IPSReference>();
               List<PSUiReference> folderNodes = 
                  new ArrayList<PSUiReference>();
               for (PSUiReference node : nodes)
               {
                  if (node.getObjectType().equals(
                     PSObjectTypeFactory.getType(PSObjectTypes.USER_FILE,
                     PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER)))
                  {
                     folderNodes.add(node);
                  }
                  else
                  {
                     objectRefs.add(node.getReference());
                  }
               }
               
               processObjectPaste(parent, objectRefs);
               
               if (!folderNodes.isEmpty())
               {
                  IPSHierarchyManager folderMgr = null;
                  for (PSUiReference folderNode : folderNodes)
                  {
                     if (folderMgr == null)
                     {
                        folderMgr = folderModel.getHierarchyManager(
                           folderNode.getReference());
                     }
                     
                     names.add(folderNode.getName());
                  }

                  IPSHierarchyNodeRef newFolders[] = folderMgr.createChildren(
                     curParentRef, PSObjectTypeFactory.getType(
                        PSObjectTypes.USER_FILE,
                        PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER),
                        names);
                  PSDesignObjectHierarchy mainModel = 
                     PSDesignObjectHierarchy.getInstance();
                  List<PSUiReference> createdNodes = mainModel.addChildren(
                     parent, newFolders);
                  for (int i = 0; i < createdNodes.size(); i++)
                  {
                     List<PSUiReference> children = mainModel.getChildren(
                        folderNodes.get(i));
                     processFolderPaste(createdNodes.get(i), children);
                  }
               }
            }
            catch (PSModelException e)
            {
               // should never happen
               throw new RuntimeException(e);
            }
            catch (PSMultiOperationException e)
            {
               PSUiUtils.handleExceptionSync("Folder paste", null, null, e);
            }
         }
      });
   }
   
   /**
    * Retrieves the requested model, converting the possible exception to a
    * runtime exception.
    * 
    * @param type Never <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   static public IPSCmsModel getModel(PSObjectTypes type)
   {
      if (null == type)
      {
         throw new IllegalArgumentException("type cannot be null");  
      }
      try
      {
         return PSCoreFactory.getInstance().getModel(type);
      }
      catch (PSModelException e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * This is the resource path of the icon for this node. It is used by the
    * {@link #getLabelImage(PSUiReference)} method. May be <code>null</code>, never empty.
    */
   private final String m_iconPath;

   /**
    * Supplied in ctor. Never <code>null</code> or members changed after
    * construction. The names are all lower-cased for case-insensitive compares.
    */
   private final Properties m_props;

   /**
    * The types that will be accepted during a drop. Never <code>null</code>,
    * may be empty. Can be extended by calling the
    * {@link #addAcceptedTransfer(Transfer)} method.
    */
   private Transfer[] m_acceptedTypes;

   /**
    * Flags that control the behavior of this handler. Defaults to all options
    * enabled. Changed by the {@link #configure(HandlerOptionSet)} method only.
    */
   private HandlerOptionSet m_options = HandlerOptions.getAllOptions();
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger =
      LogManager.getLogger(PSDeclarativeNodeHandler.class);
}
