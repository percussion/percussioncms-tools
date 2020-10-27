/******************************************************************************
 *
 * [ PSXmlAppDebuggingAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.TracePropDialog;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;
import org.eclipse.ui.IWorkbenchSite;

import javax.swing.*;

/**
 * Shows a Trace Properties dialog.
 * 
 * @author Andriy Palamarchuk
 */
public class PSXmlAppDebuggingAction extends PSXmlAppBaseAction
{
   /**
    * Creates new action.
    * @param site current site. Can be <code>null</code>. In case the
    * application should be provided through
    * {@link #setApplication(com.percussion.E2Designer.OSApplication)}.
    */
   public PSXmlAppDebuggingAction(IWorkbenchSite site)
   {
      super(site);
   }

   // see base
   @Override
   public void run()
   {
      final PSApplication application = loadApplication();
      if (application == null)
      {
         return;
      }
      PSLegacyInitialzer.initializeLegacySystems();
      SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  try
                  {
                     new TracePropDialog(application);
                  }
                  catch (Exception e)
                  {
                     PSDlgUtil.showError(e);
                  }
               }
            });
   }

   /**
    * Returns {@link #XMLAPP_DEBUGGING}
    * @see org.eclipse.jface.action.IAction#getId()
    */
   @Override
   public String getId()
   {
      return XMLAPP_DEBUGGING;
   }
   
   /**
    * The action id.
    */
   public static final String XMLAPP_DEBUGGING = "menuToolsDebugging";
}
