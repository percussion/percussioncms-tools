/******************************************************************************
 *
 * [ PSAddWorkflowsAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.views.dialogs.PSAddDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Standard action for modifying allowed workflows of the currently selected 
 * content type.
 */
final class PSAddWorkflowsAction extends BaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId() + ".AddWorkflowsAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    *
    * @param shell The shell for any dialogs, never <code>null</code>.
    * @param site The IViewSite used for finding current selection, never
    * <code>null</code>.
    */
   public PSAddWorkflowsAction(Shell shell, IViewSite site)
   {
      super(PSMessages.getString("PSAddWorkflowsAction.action.label"));
      if ( null == shell)
      {
         throw new IllegalArgumentException("shell cannot be null");  
      }
      if ( null == site)
      {
         throw new IllegalArgumentException("site cannot be null");  
      }      
      m_shell = shell;
      setToolTipText(PSMessages.getString("PSAddWorkflowsAction.action.tooltip"));
      setId(PSAddWorkflowsAction.ID);
      m_site = site;
   }

   /**
    * Opens the add workflows dialog for the selected content type.
    */
   @Override
   @SuppressWarnings("unchecked")
   public void run()
   {
      boolean success = false;
      IPSReference nodeRef = null;
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
         
         String availLabel = PSMessages.getString("PSAddWorkflowsDialog.available");
         String selectLabel = PSMessages.getString("PSAddWorkflowsDialog.title");
         
         Set<IPSReference> availItemsSet = new HashSet<IPSReference>();
         List<IPSReference> communities =
            PSSecurityUtils.getVisibleCommunities(nodeRef);
         if (!communities.isEmpty())
         {
            availItemsSet = PSSecurityUtils.getObjectsByCommunityVisibility(
                  communities,
                  PSTypeEnum.WORKFLOW);
         }
         List<IPSReference> availItems = new ArrayList<IPSReference>();
         for (IPSReference ref : availItemsSet)
            availItems.add(ref);
         
         List<IPSReference> selectItems = new ArrayList<IPSReference>();
         
         PSItemDefinition contentType = 
            (PSItemDefinition) PSModelTracker.getInstance().load(nodeRef, true);
         
         Collection<IPSReference> wfRefs = new ArrayList<IPSReference>();
         PSWorkflowInfo workflowInfo = contentType.getContentEditor()
               .getWorkflowInfo();

         if (workflowInfo != null)
         {
            List<Integer> wfIds = workflowInfo.getWorkflowIds();
            IPSCmsModel wfModel = 
               PSCoreFactory.getInstance().getModel(PSObjectTypes.WORKFLOW);
            for (IPSReference wfRef : wfModel.catalog(false))
            {
               if (wfIds.contains((int)wfRef.getId().longValue()))
                  wfRefs.add(wfRef);
            }
         }
         
         String defaultWorkflow = "";
         int defaultWorkflowId = contentType.getWorkflowId();
         
         for (IPSReference wfRef : wfRefs)
         {
            if ((int)wfRef.getId().longValue() == defaultWorkflowId)
               defaultWorkflow = wfRef.getName();
            selectItems.add(wfRef);
         }
         
         m_dlg = new PSAddDialog(
               m_shell,
               PSMessages.getString("PSAddWorkflowsDialog.title"),
               availLabel,
               selectLabel,
               availItems,
               selectItems, 
               "workflows");
         
         boolean defaultSelected = false;
         int status = m_dlg.open();
                  
         while (status == IDialogConstants.OK_ID)
         {
            List<IPSReference> selections = m_dlg.getSelections();
            List<Integer> wfIds = new ArrayList<Integer>();
            
            for (IPSReference selection : selections)
            {
               int id = (int)selection.getId().longValue();
               
               if (id == defaultWorkflowId)
                  defaultSelected = true;
               
               wfIds.add(id);
            }
            
            if (selections.size() == 0 || !defaultSelected)
            {
               MessageDialog.openWarning(m_shell,
                     PSMessages.getString(
                        "common.error.defaultWorkflowRemoved.title"), //$NON-NLS-1$
                     PSMessages.getString(
                        "common.error.defaultWorkflowRemoved.message",
                        new Object[]{defaultWorkflow})); //$NON-NLS-1$
               status = m_dlg.open();
            }
            else
            {
               if (workflowInfo == null)
                  workflowInfo = new PSWorkflowInfo(
                        PSWorkflowInfo.TYPE_INCLUSIONARY, wfIds);
               else
                  workflowInfo.setValues(wfIds);
            
               PSModelTracker.getInstance().save(nodeRef);
               success = true;
               break;
            }
         }
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
               PSMessages.getString("PSAddWorkflowsDialog.title"),
               PSMessages.getString("PSAddWorkflowsAction.action.error.title"),
               "",
               e);
      }
      finally
      {
         try
         {
            if (!success)
               PSModelTracker.getInstance().releaseLock(nodeRef);
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin.handleException(
                  PSMessages.getString("PSAddWorkflowsDialog.title"),
                  PSMessages.getString("PSAddWorkflowsAction.action.error.title"),
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

   /**
    * The shell in which to show any dialogs.
    */
   private final Shell m_shell;
   
   /**
    * Used to obtain the selection when the action is run. Never
    * <code>null</code> or modified after ctor.
    */
   private final IWorkbenchSite m_site;
   
   /**
    * This dialog will be instantiated and opened in the run method
    */
   private PSAddDialog m_dlg = null;
   
}
