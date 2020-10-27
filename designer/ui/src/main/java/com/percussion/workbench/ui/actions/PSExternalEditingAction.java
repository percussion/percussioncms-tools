/******************************************************************************
 *
 * [ PSExternalEditingAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSMultiOperationException;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSFileEditorTracker;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the base class for actions that operate on content being edited in 
 * an external editor.
 *
 * @author paulhoward
 */
public abstract class PSExternalEditingAction extends
      PSBaseSelectionListenerAction
{
   /**
    * Required by base class.
    * @param name Supplied to base class ctor.
    * @param provider Supplied to base class ctor.
    */
   public PSExternalEditingAction(String name, ISelectionProvider provider)
   {
      super(name, provider);
   }

   /**
    * For every {@link PSUiReference} object in the selection, the
    * <code>refresh</code> method is called on the design object model.
    */
   @Override
   public void run()
   {
      if (!isEnabled())
         return;
      List sel = getStructuredSelection().toList();
      Collection<PSUiReference> toProcess = new ArrayList<PSUiReference>();
      for (Object o : sel)
      {
         if (!(o instanceof PSUiReference))
            continue;
         PSUiReference node = (PSUiReference) o;
         toProcess.add(node);
      }
      
      if (!queryProceed())
         return;
      Collection<Exception> errors = new ArrayList<Exception>();
      Collection<Object> details = new ArrayList<Object>();
      for (PSUiReference node : toProcess)
      {
         try
         {
            processRef(node.getReference());
         }
         catch (Exception e)
         {
            errors.add(e);
            details.add(node.getReference());
         }
      }
      
      if (!errors.isEmpty())
      {
         String title = PSMessages
               .getString("PSExternalEditorAction.error.actionFailed.title");
         String msg = PSMessages
               .getString("PSExternalEditorAction.error.actionFailed.message");
         PSUiUtils.handleExceptionSync("Processing external file",
               title, msg, new PSMultiOperationException(errors.toArray(),
                  details.toArray()));
      }
   }

   /**
    * Called by this class during run processing on each appropriate node.
    * 
    * @param ref Never <code>null</code>.
    * 
    * @throws Exception If any problems performing the action.
    */
   protected abstract void processRef(IPSReference ref)
      throws Exception;
   
   /**
    * Called before any nodes are processed. The derived class can override to
    * ask the user if they want to proceed.
    * 
    * @return <code>true</code> to continue with the operation,
    * <code>false</code> to cancel the operation.
    */
   protected boolean queryProceed()
   {
      return true;
   }
   
   /**
    * Checks that every element in the selection is a {@link PSUiReference} and
    * whether it is open in an external editor.
    * 
    * @return <code>true</code> If there is at least 1 element and the 
    * aforementioned check is valided, otherwise <code>false</code>.
    */
   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if (!super.updateSelection(selection)
            || !PSCoreFactory.getInstance().isConnected()
            || selection.isEmpty())
      {
         return false;
      }

      List<PSPair<IPSReference, IFile>> currentlyOpen = PSFileEditorTracker
            .getInstance().getRegisteredReferences(false);

      if (currentlyOpen.isEmpty())
         return false;
      
      for (Object o : selection.toList())
      {
         if (!(o instanceof PSUiReference))
         {
            return false;
         }
         PSUiReference node = (PSUiReference) o;
         boolean found = false;
         for (PSPair<IPSReference, IFile> p : currentlyOpen)
         {
            IPSReference ref = node.getReference();
            if (ref != null && ref.equals(p.getFirst()))
               found = true;
         }
         if (!found)
            return false;
      }
      
      return true;
   }
}
