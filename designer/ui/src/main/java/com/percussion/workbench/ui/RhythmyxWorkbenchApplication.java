/******************************************************************************
 *
 * [ RhythmyxWorkbenchApplication.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.PSCoreFactory;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import java.io.IOException;

/**
 * This class controls all aspects of the application's execution
 */
public class RhythmyxWorkbenchApplication implements IPlatformRunnable
// took some ideas from IDEApplication 
{

   @SuppressWarnings("unused") //$NON-NLS-1$
   public Object run(Object args) throws Exception
   {
      Display display = PlatformUI.createDisplay();
      final PSCoreFactory factory = PSCoreFactory.getInstance();
      try
      {  
         Shell shell = new Shell(display, SWT.ON_TOP);

         try {
             if (!checkInstanceLocation(shell))
             {
                 Platform.endSplash();
                 return EXIT_OK;
             }
         }
         finally {
            shell.dispose();
         }

         final int returnCode = PlatformUI.createAndRunWorkbench(display,
               new ApplicationWorkbenchAdvisor());
         if (returnCode == PlatformUI.RETURN_RESTART)
         {
            return IPlatformRunnable.EXIT_RESTART;
         }

         return IPlatformRunnable.EXIT_OK;
      }
      finally
      {
         factory.logoff();
         display.dispose();
      }
   }

   /**
    * Validates instance location.
    */
   private boolean checkInstanceLocation(Shell shell)
   {
      final Location location = Platform.getInstanceLocation();
      if (location.isSet())
      {
          try {
              if (location.lock())
              {
                  return true;
              }
              MessageDialog.openError(shell, getMessage("common.error.title"), //$NON-NLS-1$
                      getMessage("RhythmyxWorkbenchApplication.error.workspaceLocked")); //$NON-NLS-1$
          }
          catch (IOException e)
          {
              MessageDialog.openError(shell, getMessage("common.error.title"), //$NON-NLS-1$
                      e.getMessage());                
          }            
          return false;
      }
      return true;
   }

   /**
    * Convenience method to access messages.
    */
   private String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }
}
