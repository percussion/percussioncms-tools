/******************************************************************************
 *
 * [ PSAddSlotsAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.IPSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.utils.guid.IPSGuid;
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
import org.eclipse.ui.IWorkbenchSite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Standard action for modifying contained slots of the currently selected 
 * template.
 */
final class PSAddSlotsAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId() + ".AddSlotsAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    *
    * @param shell The shell for any dialogs, never <code>null</code>.
    * @param site The IViewSite used for finding current selection, never
    * <code>null</code>.
    */
   public PSAddSlotsAction(Shell shell, IViewSite site, 
         ISelectionProvider provider)
   {
      super(PSMessages.getString("PSAddSlotsAction.action.label"), provider);
      if ( null == shell)
      {
         throw new IllegalArgumentException("shell cannot be null");  
      }
      if ( null == site)
      {
         throw new IllegalArgumentException("site cannot be null");  
      }      
      m_shell = shell;
      setToolTipText(PSMessages.getString("PSAddSlotsAction.action.tooltip"));
      setId(PSAddSlotsAction.ID);
      
      m_site = site;
   }

   /**
    * Opens the add slots dialog for the selected template.
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
         
         IPSUiAssemblyTemplate uiTemplate = 
            (IPSUiAssemblyTemplate) PSModelTracker.getInstance().load(
                  nodeRef, true);
         
         IPSCmsModel slotModel = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.SLOT);
         
         String availLabel = PSMessages.getString("PSAddSlotsDialog.available");
         String selectLabel = PSMessages.getString("PSAddSlotsDialog.title");
         List<IPSReference> availItems = 
            new ArrayList<IPSReference>(slotModel.catalog());
         List<IPSReference> selectItems = new ArrayList<IPSReference>();
         
         for (final IPSTemplateSlot slot : uiTemplate.getSlots())
         {
            selectItems.add(slotModel.getReference(slot.getGUID()));
         }
         
         m_dlg = new PSAddDialog(
               m_shell,
               PSMessages.getString("PSAddSlotsDialog.title"),
               availLabel,
               selectLabel,
               availItems,
               selectItems, 
               "slots");
         int status = m_dlg.open();
         
         if (status == IDialogConstants.OK_ID)
         {
            Set<IPSGuid> slots = new HashSet<IPSGuid>();
            for (Object o : m_dlg.getSelections())
            {
               slots.add(((IPSReference) o).getId());
            }
            uiTemplate.setSlotGuids(slots);
            
            PSModelTracker.getInstance().save(nodeRef);
            success = true;
         }
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
               PSMessages.getString("PSAddSlotsDialog.title"),
               PSMessages.getString("PSAddSlotsAction.action.error.title"),
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
                  PSMessages.getString("PSAddSlotsDialog.title"),
                  PSMessages.getString("PSAddSlotsAction.action.error.title"),
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
