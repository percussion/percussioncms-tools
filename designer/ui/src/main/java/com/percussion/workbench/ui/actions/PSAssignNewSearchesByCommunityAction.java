/******************************************************************************
 *
 * [ PSAssignNewSearchesByCommunityAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.E2Designer.browser.PSConfigureCommunityNewSearchesDialog;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Standard action for assigning new searches by community.
 */
final class PSAssignNewSearchesByCommunityAction extends
      PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId()
         + ".AssignNewSearchesByCommunity"; //$NON-NLS-1$

   /**
    * Creates a new action.
    * 
    * @param provider Supplied to super ctor. Never <code>null</code>.
    */
   public PSAssignNewSearchesByCommunityAction(ISelectionProvider provider)
   {
      super(PSMessages.getString(
            "PSAssignNewSearchesByCommunity.title.assignnewsearches"), provider); //$NON-NLS-1$

      setToolTipText(PSMessages.getString(
            "PSAssignNewSearchesByCommunity.tooltiptext.assignnewcommunity")); //$NON-NLS-1$
      setId(PSAssignNewSearchesByCommunityAction.ID);
   }

   /**
    * Opens the configure new searches for communities dialog.
    */
   @Override
   public void run()
   {
      try
      {
         if (!isEnabled())
            return;
         if (!canEdit())
            return;
         final IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.UI_SEARCH);
         final Collection<IPSReference> searchRefs = model.catalog();
         Object[] objs = model.load(searchRefs.toArray(new IPSReference[0]),
               true, false);
         final PSSearchCollection searches = new PSSearchCollection();
         for (Object obj : objs)
            searches.add((PSSearch) obj);

         Shell shell = PSUiUtils.getShell();
         final AwtSwtModalDialogBridge bridge = new AwtSwtModalDialogBridge(
               shell);
         final Display display = shell.getDisplay();
         SwingUtilities.invokeLater(new Runnable()
         {
            @SuppressWarnings("synthetic-access")//$NON-NLS-1$
            public void run()
            {
               try
               {
                  m_dlg = new PSConfigureCommunityNewSearchesDialog(
                        (Frame) null, searches);
                  bridge.registerModalSwingDialog(m_dlg);
                  m_dlg.center();
                  m_dlg.setVisible(true);
                  if (m_dlg.isOk())
                  {
                     display.syncExec(new Runnable()
                     {
                        public void run()
                        {
                           try
                           {
                              model.save(searchRefs
                                    .toArray(new IPSReference[0]), true);
                           }
                           catch (Exception e)
                           {
                              if (e instanceof RuntimeException)
                                 throw (RuntimeException) e;

                              String title = PSMessages.getString(
                                    "PSAssignNewSearchesByCommunity.error.title.failedtoassignnewsearches"); //$NON-NLS-1$
                              String msg = PSMessages.getString(
                                    "PSAssignNewSearchesByCommunity.error.message.searchessave"); //$NON-NLS-1$
                              PSWorkbenchPlugin.handleException(
                                    "Assign new searches by community action", //$NON-NLS-1$
                                    title, msg, e);
                           }
                        }
                     });
                  }
               }
               finally
               {
                  try
                  {
                     model.releaseLock(searchRefs
                           .toArray(new IPSReference[searchRefs.size()]));
                  }
                  catch (PSModelException e)
                  {
                     PSWorkbenchPlugin.getDefault().log(
                           "Failed to release locks on searches.", e); //$NON-NLS-1$
                  }
                  catch (PSMultiOperationException e)
                  {
                     PSWorkbenchPlugin.getDefault().log(
                           "Failed to release locks on searches.", e); //$NON-NLS-1$
                  }
               }
            }
         });

      }
      /*
       * @todo ph: need to catch PSMultiOperationException and release the locks
       * that were actually obtained. This could happen if we had a ref cached
       * that didn't show the lock, then someone else locked it. canEdit() 
       * would pass, but the lock attempt would fail
       */
      catch (Exception e)
      {
         String title = PSMessages.getString(
               "PSAssignNewSearchesByCommunity.error.title.failedtoassignnewsearches"); //$NON-NLS-1$
         String msg = PSMessages.getString(
               "PSAssignNewSearchesByCommunity.error.message.searchesload"); //$NON-NLS-1$
         PSWorkbenchPlugin.handleException(
               "Assign new searches by community action", title, msg, e); //$NON-NLS-1$
      }
   }

   /**
    * Checks if all searches are unlocked.
    * 
    * @return <code>true</code> if they are all unlocked, <code>false</code>
    * otherwise. If <code>false</code> is returned, a message has already been
    * displayed to the user.
    */
   private boolean canEdit()
   {
      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.UI_SEARCH);
         Collection<IPSReference> searchRefs = model.catalog();
         for (IPSReference ref : searchRefs)
         {
            if (!StringUtils.isEmpty(ref.getLockUserName()))
            {
               String title = PSMessages.getString(
                     "common.warning.operationUnavailable.title"); //$NON-NLS-1$
               String msg = PSMessages.getString(
                     "PSAssignNewSearchesByCommunityAction.cantEdit.message"); //$NON-NLS-1$
               MessageDialog.openInformation(PSUiUtils.getShell(), title, msg);
               return false;
            }
         }
         return true;
      }
      catch (PSModelException e)
      {
         String titleKey = "common.error.title"; //$NON-NLS-1$
         String msgKey = 
            "PSAssignNewSearchesByCommunityAction.error.catalog.message"; //$NON-NLS-1$
         PSUiUtils.handleExceptionSync("Assign new searches", titleKey, //$NON-NLS-1$
               msgKey, e);
      }
      return false;
   }

   @Override
   protected boolean updateSelection(IStructuredSelection selection)
   {
      return super.updateSelection(selection);
   }

   /**
    * This dialog will be instantiated and opened in the run method
    */
   private PSConfigureCommunityNewSearchesDialog m_dlg = null;
}
