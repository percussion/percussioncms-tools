/******************************************************************************
 *
 * [ PSXmlAppImportAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;

/**
 * Performs application import into currently selected application.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlAppImportAction extends PSXmlAppBaseAction
{
   /**
    * Creates new action.
    */
   public PSXmlAppImportAction(IWorkbenchSite site)
   {
      super(site);
   }

   @Override
   public void run()
   {
      final IPSReference ref = getSelectedRef();
      if (ref == null)
      {
         return;
      }

      PSLegacyInitialzer.initializeLegacySystems();
      try
      {
         getMainFrame().validatePlatform(getSite());

         // Andriy: launch separate thread, so file opening dialog
         // during import won't hang UI
         new Thread()
         {
            @Override
            public void run()
            {
               getMainFrame().importApp(ref);
            }
         }.start();
      }
      catch (PartInitException e)
      {
         PSDlgUtil.showError(e);
      }
   }
   
   /**
    * Returns {@link #XMLAPP_IMPORT}
    * @see org.eclipse.jface.action.IAction#getId()
    */
   @Override
   public String getId()
   {
      return XMLAPP_IMPORT;
   }

   /**
    * The action id.
    */
   public static final String XMLAPP_IMPORT = "menuAppImport";
}
