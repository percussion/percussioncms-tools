/******************************************************************************
 *
 * [ PSAddForCTAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.views.dialogs.PSAddDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Standard action for modifying allowed templates of the currently selected 
 * content type.
 */
final class PSAddForCTAction extends PSAddTemplatesAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId() + ".AddForCTAction"; //$NON-NLS-1$

   /**
    * Creates a new action.  Calls parent class constructor.
    *
    * @param shell The shell for any dialogs, never <code>null</code>.
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSAddForCTAction(Shell shell, IViewSite site, 
         ISelectionProvider provider)
   {
      super(shell, site, provider);
   }

   @Override
   @SuppressWarnings("unchecked")
   public void run()
   {
      IPSReference nodeRef = null;
      Map<IPSReference, Collection<IPSReference>> associations = null;
      boolean success = false;
      try
      {
         if (!isEnabled())
            return;
         ISelection sel = m_site.getSelectionProvider().getSelection();
         if (!(sel instanceof IStructuredSelection))
            return;
         
         IStructuredSelection ssel = (IStructuredSelection) sel;
         PSUiReference node = (PSUiReference) ssel.getFirstElement();
         nodeRef = node.getReference();
         
         IPSCmsModel templateModel = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.TEMPLATE);
                  
         List<IPSReference> availItems = 
            new ArrayList<IPSReference>(templateModel.catalog(
                  false,
                  new PSObjectType[]{
                        PSObjectTypeFactory.getType(
                              PSObjectTypes.TEMPLATE,
                              PSObjectTypes.TemplateSubTypes.LOCAL),
                        PSObjectTypeFactory.getType(
                              PSObjectTypes.TEMPLATE,
                              PSObjectTypes.TemplateSubTypes.SHARED)}
                   ));
         List<IPSReference> selectItems = new ArrayList<IPSReference>();
         
         IPSContentTypeModel contentModel = 
            (IPSContentTypeModel) PSCoreFactory.getInstance().getModel(
               PSObjectTypes.CONTENT_TYPE);
         
         associations = contentModel.getTemplateAssociations(null, false, true);
         Collection<IPSReference> templateRefs = associations.get(nodeRef);
         
         if (templateRefs != null)
         {
            for (IPSReference ref : templateRefs)
            {
               selectItems.add(ref);
            }
         }
                  
         m_dlg = new PSAddDialog(
               m_shell, 
               PSMessages.getString("PSAddTemplatesDialog.title"),
               m_availLabel,
               m_selectLabel,
               availItems,
               selectItems, 
               "templates");
         int status = m_dlg.open();
         
         if (status == IDialogConstants.OK_ID)
         {
            selectItems = m_dlg.getSelections();
            associations.put(nodeRef, selectItems);
            contentModel.setTemplateAssociations(associations);  
            success = true;
         }
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
               PSMessages.getString("PSAddTemplatesDialog.title"),
               PSMessages.getString("PSAddTemplatesAction.action.error.title"),
               "",
               e);
      }
      finally
      {
         try
         {
            if ((!success) && associations != null)
            {
               for (IPSReference ref : associations.keySet())
                  PSModelTracker.getInstance().releaseLock(ref);
            }
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin.handleException(
                  PSMessages.getString("PSAddTemplatesDialog.title"),
                  PSMessages.getString("PSAddTemplatesAction.action.error.title"),
                  "",
                  e);
         }

      }
   }

   /* 
    * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(
    * org.eclipse.jface.viewers.IStructuredSelection)
    */
   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      if(selection.size() != 1)
         return false;
      return super.updateSelection(selection);
   }
   
   
   
}
