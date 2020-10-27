/******************************************************************************
 *
 * [ PSLinkNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSDropHandler;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This handler is a base class for handlers that manage links between objects.
 * It provides a handler that only allows DROP_LINK. For drop and paste, it
 * calls the abstract method
 * {@link #doSaveAssociations(IPSReference, Collection)} and for deletes it
 * calls {@link #doDeleteAssociations(IPSReference, Collection)}, both of which
 * must be implemented by derived classes.
 * 
 * @author paulhoward
 */
public abstract class PSLinkNodeHandler extends
      PSDeclarativeNodeHandler
{
   /**
    * The only ctor, signature required by framework. Parameters passed to 
    * base class.
    */
   public PSLinkNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * Only allows DROP_LINK feedback. The
    * {@link #performDrop(PSUiReference, int, Object)} method calls the outer
    * class' {@link #saveAssociations(PSUiReference, Collection)}.
    * <p>
    * This drop handler is returned by this class' {@link #getDropHandler()}.
    * Derived classes can use it if they need to modify the behavior.
    */
   protected class LinkDropHandler extends NodeDropHandler
   {
      /**
       * @inheritDoc
       * Only linking is allowed when adding relationship.
       */
      @Override
      public int getValidDndOperation(int desiredOp)
      {
         return desiredOp == DND.DROP_NONE ? DND.DROP_NONE : DND.DROP_LINK;
      }

      /**
       * @inheritDoc
       * Adds the dropped templates as links.
       */
      @Override
      @SuppressWarnings("unchecked")
      public boolean performDrop(PSUiReference target, int op, Object data)
      {
         assert(op == DND.DROP_LINK);
         if (!(data instanceof Collection))
            return false;
         m_nodes = (Collection<PSUiReference>) data;
         Collection<IPSReference> refs = new ArrayList<IPSReference>();
         for (PSUiReference node : m_nodes)
         {
            refs.add(node.getReference());
         }
         boolean success = saveAssociations(target, refs);
         m_nodes = null;
         return success;
      }
   }

   /**
    * Returns the nodes that were passed into the
    * {@link LinkDropHandler#performDrop(PSUiReference, int, Object)} method.
    * 
    * @return <code>null</code> except during the processing of the
    * {@link #saveAssociations(PSUiReference, Collection)} and
    * {@link #doSaveAssociations(IPSReference, Collection)} methods.
    */
   protected Collection<PSUiReference> getNodes()
   {
      return m_nodes;
   }
   
   /**
    * @inheritDoc
    * 
    * Overridden to return a {@link LinkDropHandler}.
    */
   @Override
   public IPSDropHandler getDropHandler()
   {
      return new LinkDropHandler();
   }

   @Override
   public boolean supportsCopy(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      return isProcessableNode(node);
   }

   /**
    * If the node wraps a design object and the supplied node's associated
    * design object has a transfer type that matches one of those returned by
    * {@link #getAcceptedTransfers()}, then <code>true</code> is returned.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if this handler can process the standard
    * operations with the supplied target node, <code>false</code> otherwise.
    */
   private boolean isProcessableNode(PSUiReference node)
   {
      IPSReference ref = node.getReference();
      if (ref == null)
         return false;
      
      Transfer suppliedTransferType = getTransfer(ref.getObjectType());
      for (Transfer t : getAcceptedTransfers())
      {
         if (t.equals(suppliedTransferType))
            return true;
      }
      return false;
   }

   /**
    * 
    * @return <code>true</code> if this handler knows how to de-link the
    * supplied node from the managed design object type, <code>false</code>
    * otherwise.
    */
   @Override
   public boolean supportsDelete(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      return isProcessableNode(node);
   }

   /**
    * Overridden to support dropping/pasting related templates.
    */
   @Override
   @SuppressWarnings("unchecked")
   public void handlePaste(final PSUiReference parent,
         final Map<Transfer, Object> cbData)
   {
      Collection<IPSReference> refs = new ArrayList<IPSReference>();
      for (Transfer t : cbData.keySet())
      {
         for (PSUiReference node : (Collection<PSUiReference>) cbData.get(t))
         {
            refs.add(node.getReference());
         }
      }
      saveAssociations(parent, refs);
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>true</code>;
    */
   @SuppressWarnings("unused")
   @Override
   protected boolean supportsDeleteAssociation(PSUiReference node)
   {
      return true;
   }
   
   @Override
   public void handleDelete(final Collection<PSUiReference> nodes)
   {
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            if (nodes == null || nodes.isEmpty())
               return;
      
            try
            {
               //group associated refs together by parent
               Map<IPSReference, Collection<IPSReference>> categorizedRefs = 
                  new HashMap<IPSReference, Collection<IPSReference>>();
               for (PSUiReference node : nodes)
               {
                  PSUiReference parent = node.getParentNode();
                  PSUiReference ancestor = getAncestor(parent, true);
                  if (ancestor == null)
                     ancestor = getAncestor(parent, false);
                  IPSReference instanceRef = ancestor.getReference();
         
                  IPSCmsModel model = PSCoreFactory.getInstance().getModel(
                        instanceRef.getObjectType().getPrimaryType());
                  if (checkIfEditing(model, instanceRef))
                     return;
                  
                  Collection<IPSReference> refs = categorizedRefs.get(instanceRef);
                  if (refs == null)
                  {
                     refs = new ArrayList<IPSReference>();
                     categorizedRefs.put(instanceRef, refs);
                  }
                  refs.add(node.getReference());
               }
               for (IPSReference parent : categorizedRefs.keySet())
               {
                  Collection<IPSReference> deletedRefs = doDeleteAssociations(
                        parent, categorizedRefs.get(parent));
                  Iterator<PSUiReference> iter = nodes.iterator();
                  while (iter.hasNext())
                  {
                     if (!deletedRefs.contains(iter.next().getReference()))
                        iter.remove();
                  }
               }
               PSDesignObjectHierarchy.getInstance().deleteChildren(nodes);
               //the design model is notified directly, so we don't have to
            }
            catch (Exception e)
            {
               String title = PSMessages
                     .getString("PSLinkNodeHandler.error.deleteLink.title");
               Long[] args = 
               {
                  new Long(nodes.size()), 
                  new Long(e.getCause() == null ? 1 : 2)
               };
               String msg = PSMessages.getString(
                     "PSLinkNodeHandler.error.deleteLink.message", (Object[]) args);
               new PSErrorDialog(PSUiUtils.getShell(), title, msg, e).open();
            }
         }
      });
   }

   /**
    * First finds the ancestor node that wraps a 'real' instance of a design
    * object. Then checks if that object is currently open for editing. If it
    * is, a message is displayed to the user that the operation cannot proceed.
    * Otherwise calls {@link #doSaveAssociations(IPSReference, Collection)},
    * catching exceptions and displaying an error message to the user.
    * <p>
    * This method performs its work inside a busy indicator.
    * 
    * @return <code>true</code> if successful, <code>false</code> otherwise.
    * If <code>false</code>, an error has been displayed to the user.
    */
   protected boolean saveAssociations(final PSUiReference parentFolder, 
         final Collection<IPSReference> refs)
   {
      final boolean[] resultTransfer = new boolean[1];
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            try
            {
               PSUiReference ancestor = getAncestor(parentFolder, true);
               if (ancestor == null)
                  ancestor = getAncestor(parentFolder, false);
               IPSReference instanceRef = ancestor.getReference();
               
               IPSCmsModel model = PSCoreFactory.getInstance().getModel(
                     instanceRef.getObjectType().getPrimaryType());
               if (checkIfEditing(model, instanceRef))
               {
                  resultTransfer[0] = false;
                  return;
               }
               Collection<IPSReference> filteredRefs = filterReferences(
                     parentFolder, refs);
               
               Collection<IPSReference> unpersistedRefs = 
                  new ArrayList<IPSReference>();
               StringBuffer nameBuf = new StringBuffer();
               for (IPSReference ref : refs)
               {
                  if (!ref.isPersisted())
                  {
                     unpersistedRefs.add(ref);
                     if (nameBuf.length() > 0)
                        nameBuf.append(", ");
                     nameBuf.append(ref.getName());
                  }
               }
               if (!unpersistedRefs.isEmpty())
               {
                  filteredRefs.removeAll(unpersistedRefs);
                  String title = PSMessages.getString(
                        "PSLinkNodeHandler.error.unpersistedChildLinks.title");
                  Object[] args = { nameBuf.toString() };
                  String msg = PSMessages.getString(
                     "PSLinkNodeHandler.error.unpersistedChildLinks.message",
                     args);
                  MessageDialog.openWarning(PSUiUtils.getShell(), title, msg);
               }
               
               doSaveAssociations(instanceRef, filteredRefs);
               resultTransfer[0] = true;
            }
            catch (Exception e)
            {
               String title = PSMessages
                     .getString("PSLinkNodeHandler.error.saveLink.title");
               Long[] args = 
               {
                  new Long(refs.size()), 
                  new Long(e.getCause() == null ? 1 : 2)
               };
               String msg = PSMessages.getString(
                     "PSLinkNodeHandler.error.saveLink.message", (Object[]) args);
               new PSErrorDialog(PSUiUtils.getShell(), title, msg, e).open();
            }
         }
      });
      return resultTransfer[0];
   }

   /**
    * Creates a new collection and transfers refs from the supplied collection
    * to the new one if its type is accepted by this handler and it is not
    * already a child of the supplied folder.
    * 
    * @param target The node that is to receive the refs. Assumed not
    * <code>null</code>.
    * 
    * @param refs Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If any problems getting the children of target.
    */
   private Collection<IPSReference> filterReferences(PSUiReference target,
         Collection<IPSReference> refs)
         throws PSModelException
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();
      Collection<Transfer> acceptedTypes = new ArrayList<Transfer>();
      acceptedTypes.addAll(Arrays.asList(getAcceptedTransfers()));
      for (IPSReference ref : refs)
      {
         if (acceptedTypes.contains(getTransfer(ref.getObjectType())))
         {
            results.add(ref);
         }
      }
      List<PSUiReference> targetChildren = PSDesignObjectHierarchy
            .getInstance().getChildren(target, true);
      Collection<IPSReference> dupes = new ArrayList<IPSReference>();
      for (PSUiReference node : targetChildren)
      {
         IPSReference ref = node.getReference();
         if (ref != null && results.contains(ref))
            dupes.add(ref);
      }
      results.removeAll(dupes);
      return results;
   }

   /**
    * If <code>ref</code> is being edited, a message is shown to the user 
    * saying the operation is dis-allowed. 
    * 
    * @param model the model that <code>ref</code> came from, not
    *    <code>null</code>.
    * @param ref the design object to check, not <code>null</code>.
    * @return <code>true</code> if ref is currently being edited in this
    *    session, <code>false</code> otherwise. If <code>true</code> is 
    *    returned, the operation should be cancelled.
    */
   protected boolean checkIfEditing(IPSCmsModel model, IPSReference ref)
   {
      if (model == null)
         throw new IllegalArgumentException("model cannot be null");
      
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");
      
      if (model.isLockedInThisSession(ref))
      {
         String title = PSMessages.getString(
            "PSLinkNodeHandler.error.openForEdit.title");
         String message = PSMessages.getString(
            "PSLinkNodeHandler.error.openForEdit.message");
         MessageDialog.openWarning(PSUiUtils.getShell(), title, message);

         return true;
      }
      
      return false;
   }
   
   /**
    * Persists the supplied refs as links to the the design object that is
    * supplied. This method should NOT perform any updates to the model as that
    * is handled by this class.
    * <p>
    * This method will not be called if the data object is currently open for
    * editing.
    * <p>
    * This method is called from inside a busy indicator.
    * 
    * @param dataRef The handle to the object to which the associations should
    * be added. Never <code>null</code>.
    * 
    * @param associatedRefs Handles to the design objects that are to be linked.
    * It is not an error if an association already exists. If <code>null</code>
    * or empty, returns immediately. Caller takes ownership and may change it
    * without affecting this object.
    * 
    * @return The references that should be added as the children. Generally,
    * this is just the set of associated refs passed in. Never <code>null</code>.
    * 
    * @throws Exception If the links could not be made for any reason.
    */
   protected abstract Collection<IPSReference> doSaveAssociations(
         IPSReference dataRef, Collection<IPSReference> associatedRefs)
      throws Exception;
   
   /**
    * Permanently removes the supplied refs linked to the the design object that
    * is supplied. This method should NOT perform any updates to the model as
    * that is handled by this class.
    * <p>
    * This method will not be called if the data object is currently open for
    * editing.
    * <p>
    * This method is called from inside a busy indicator.
    * 
    * @param dataRef The handle to the object from which the associations should
    * be removed. Never <code>null</code>.
    * 
    * @param associatedRefs Handles to the design objects that are to be
    * de-linked. It is not an error if an association is not present. If
    * <code>null</code> or empty, returns immediately. Caller takes ownership.
    * Generally, this set is used for the return value.
    * 
    * @return The refs that were deleted, which may be less than the supplied
    * <code>associatedRefs</code>. Never <code>null</code>.
    * 
    * @throws Exception If the links could not be made for any reason.
    */
   protected abstract Collection<IPSReference> doDeleteAssociations(
         IPSReference dataRef, Collection<IPSReference> associatedRefs)
      throws Exception;
   

   @Override
   public boolean isAcceptedType(IPSReference ref)
   {
      // base class validation must succeed and reference must have been
      // persisted
      return (super.isAcceptedType(ref) && ref.isPersisted());
   }

   /**
    * See {@link #getNodes()} for details.
    */
   private Collection<PSUiReference> m_nodes;
}
