/******************************************************************************
 *
 * [ PSXmlAppStatusAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

import javax.swing.*;

/**
 * Starts/Stops the application.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlAppStatusAction extends PSXmlAppBaseAction
{
   /**
    * Creates new action.
    */
   public PSXmlAppStatusAction(IWorkbenchSite site)
   {
      super(site);
   }
   
   @Override
   protected boolean updateSelection(
         @SuppressWarnings("unused") IStructuredSelection selection)
   {
      final IPSReference ref = getSelectedRef();
      if (ref == null)
      {
         return false;
      }
      
      final boolean enabled = isAppRunningOnServer(ref);
      configureAction(enabled ? XMLAPP_DISABLE : XMLAPP_ENABLE);
      return true;
   }

   /**
    * Checks if the application is running on server.
    */
   private boolean isAppRunningOnServer(final IPSReference ref)
   {
      try
      {
         return getAppModel().isAppRunningOnServer(ref);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return false;
      }
   }

   // see base
   @Override
   public void run()
   {
      toggleStatus(getSelectedRef());
   }

   /**
    * Asynchroniously toggles status of the application.
    */
   public void toggleStatus(final IPSReference ref)
   {
      SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  try
                  {
                     getAppModel().toggleStatus(ref);
                  }
                  catch (Exception e)
                  {
                     if (e.getCause() instanceof Exception
                           && StringUtils.isNotBlank(
                                 e.getCause().getLocalizedMessage()))
                     {
                        PSDlgUtil.showErrorDialog(
                              e.getCause().getLocalizedMessage(),
                              PSMessages.getString("common.error.title"));
                     }
                     else
                     {
                        PSDlgUtil.showError(e);
                     }
                  }
               }

            });
   }

   /**
    * Returns {@link #XMLAPP_ENABLE}.
    */
   @Override
   public String getId()
   {
      return XMLAPP_ENABLE;
   }

   /**
    * Id of the "enable" resources. Used as action id.
    */
   public static final String XMLAPP_ENABLE = "menuAppEnable";

   /**
    * Id of the "enable" resources. Used as action id.
    */
   public static final String XMLAPP_DISABLE = "menuAppDisable";
}
