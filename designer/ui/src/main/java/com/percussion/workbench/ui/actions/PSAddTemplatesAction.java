/******************************************************************************
 *
 * [ PSAddTemplatesAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.actions;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.views.dialogs.PSAddDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Standard action for modifying allowed templates of the currently selected 
 * slot or content type.
 */
public abstract class PSAddTemplatesAction extends PSBaseSelectionListenerAction
{
   /**
    * The id of this action.
    */
   public static final String ID = PSWorkbenchPlugin.getPluginId() + ".AddTemplatesAction"; //$NON-NLS-1$

   /**
    * Creates a new action.
    *
    * @param shell The shell for any dialogs, never <code>null</code>.
    * @param site The IViewSite used for finding current selection, never
    * <code>null</code>.
    */
   public PSAddTemplatesAction(Shell shell, IViewSite site, 
         ISelectionProvider provider)
   {
      super(PSMessages.getString("PSAddTemplatesAction.action.label"),
            provider);
      if ( null == shell)
      {
         throw new IllegalArgumentException("shell cannot be null");  
      }
      if ( null == site)
      {
         throw new IllegalArgumentException("site cannot be null");  
      }      
            
      m_shell = shell;
      setToolTipText(PSMessages.getString("PSAddTemplatesAction.action.tooltip"));
      setId(PSAddTemplatesAction.ID);
     
      m_site = site;
   }

   /**
    * Opens the add templates dialog for the selected item.
    */
   public abstract void run();
   
   /**
    * The shell in which to show any dialogs.
    */
   protected final Shell m_shell;
   
   /**
    * Used to obtain the selection when the action is run. Never
    * <code>null</code> or modified after ctor.
    */
   protected final IWorkbenchSite m_site;
   
   /**
    * This dialog will be instantiated and opened in the run method
    */
   protected PSAddDialog m_dlg = null;
   
   /**
    * The available templates column label
    */
   protected String m_availLabel = PSMessages.getString(
         "PSAddTemplatesDialog.available");
   
   /**
    * The selected templates column label
    */
   protected String m_selectLabel = PSMessages.getString(
         "PSAddTemplatesDialog.title");
   
}
