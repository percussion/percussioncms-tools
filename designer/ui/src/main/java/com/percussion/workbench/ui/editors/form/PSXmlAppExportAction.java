/******************************************************************************
 *
 * [ PSXmlAppExportAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.ApplicationImportExport;
import com.percussion.E2Designer.OSApplication;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;
import org.eclipse.ui.IWorkbenchSite;

import javax.swing.*;

/**
 * Exports selected application
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlAppExportAction extends PSXmlAppBaseAction
{
   /**
    * Creates new action.
    */
   public PSXmlAppExportAction(IWorkbenchSite site)
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
                     launchExportApplication(application);
                  }
                  catch (Exception e)
                  {
                     PSDlgUtil.showError(e);
                  }
               }
            });
   }

   /**
    * Launches application export in the requred thread.
    */
   private void launchExportApplication(final PSApplication application)
   {
      try
      {
         final Runnable exportTask = new Runnable()
         {
            public void run()
            {
               try
               {
                  exportApplication(application);
               }
               catch (Exception e)
               {
                  PSDlgUtil.showError(e);
               }
            }
         };
         getMainFrame().lockSWTFor(exportTask);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Actually exports application. Should run in the Swing event thread.
    */
   private void exportApplication(final PSApplication application)
         throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      final String userDir = System.getProperty("user.dir");
      final ApplicationImportExport impExp =
            new ApplicationImportExport(
                  new JFileChooser(userDir));
      impExp.exportApplication(new OSApplication(application.toXml()));
   }

   /**
    * Returns {@link #XMLAPP_EXPORT}
    */
   @Override
   public String getId()
   {
      return XMLAPP_EXPORT;
   }

   /**
    * The action id.
    */
   public static final String XMLAPP_EXPORT = "menuAppExport";
}
