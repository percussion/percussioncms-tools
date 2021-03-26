/******************************************************************************
 *
 * [ PSSecurityAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.security.PSAclDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import java.util.List;

/**
 * Standard action for editing the ACLs for the currently selected object. Only
 * single object support is currently available. 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 */
class PSSecurityAction extends PSBaseSelectionListenerAction
{
   
   Logger log = LogManager.getLogger(PSSecurityAction.class);
   
   /**
    * The id of this action.
    */
   public static final String ID = PlatformUI.PLUGIN_ID + ".SecurityAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSSecurityAction(ISelectionProvider provider)
   {
      super(PSMessages.getString("PSSecurityAction.action.label"), provider); //$NON-NLS-1$
      setToolTipText(PSMessages.getString("PSSecurityAction.action.tooltip")); //$NON-NLS-1$
      setId(PSSecurityAction.ID);
   }

   /**
    * The <code>CopyAction</code> implementation of this method defined on
    * <code>IAction</code> copies the selected resources to the clipboard.
    */
   @Override
   @SuppressWarnings("unchecked")
   // using 3rd party code not 1.5 compliant
   public void run()
   {
      if (!isEnabled())
         return;
      List<Object> objects = getStructuredSelection().toList();
      if (objects.size() == 0)
         return;
      IPSReference ref = null;
      for (Object object : objects)
      {
         if (object instanceof PSUiReference)
         {
            ref = ((PSUiReference) object).getReference();
            break;
         }
      }
      if (objects.size() == 0)
      {
         // Error/Warning???
         return;
      }
      if (PSCoreFactory.getInstance().isLocalMode())
      {
         MessageDialog.openError(PSUiUtils.getShell(), "Error", //$NON-NLS-1$
            "This action is not functional in test mode"); //$NON-NLS-1$
      }
      else
      {
         try
         {
            PSAclDialog dlg = new PSAclDialog(PSUiUtils.getShell(), ref);
            dlg.open();
         }
         catch (Exception e)
         {
            log.error("Error opening ACL Dialog",e);
         }
      }
   }

   /**
    * The <code>CopyAction</code> implementation of this
    * <code>SelectionListenerAction</code> method enables this action if one
    * or more resources of compatible types are selected.
    */
   @Override
   @SuppressWarnings("unchecked")
   // using 3rd party code not 1.5 compliant
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if (!super.updateSelection(selection))
         return false;

      if (selection.size() == 0)
         return false;

      //todo - OK for release - add support for multiple object ACL editing
      if (selection.size() != 1)
         return false;

      List<Object> selectedObjs = selection.toList();

      for (Object o : selectedObjs)
      {
         if ((o instanceof PSUiReference))
         {
            PSUiReference node = (PSUiReference)o;
            if (node.getReference() == null)
               return false;
            IPSDeclarativeNodeHandler handler = node.getHandler();
            if (handler == null || !handler.supportsSecurity(node))
               return false;
         }
      }

      return true;
   }
}
