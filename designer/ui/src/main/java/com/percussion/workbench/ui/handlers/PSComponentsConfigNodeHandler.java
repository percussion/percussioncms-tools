/******************************************************************************
 *
 * [ PSComponentsConfigNodeHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.editors.form.PSRhythmyxPageEditor;
import com.percussion.workbench.ui.editors.form.PSUrlEditorInput;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Opens components configuration in a web browser browser.
 * Presents read-only inert node.
 *
 * @author Andriy Palamarchuk
 */
public class PSComponentsConfigNodeHandler extends PSIconNodeHandler
{
   /**
    * Constructor required by node handler conventions. 
    */
   public PSComponentsConfigNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }
   
   /**
    * Opens the components url in the browser.
    */
   @SuppressWarnings("unused")
   @Override
   public void handleOpen(IWorkbenchSite site, PSUiReference ref)
   {
      try
      {
         final URL url = new URL(getConnectionInfo().getProtocol(),
               getConnectionInfo().getServer(),
               getConnectionInfo().getPort(),
               COMPONENTS_URL);
         site.getPage().openEditor(new PSUrlEditorInput(url),
               PSRhythmyxPageEditor.ID);
      }
      catch (PartInitException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (MalformedURLException e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Returns <code>true</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsOpen(PSUiReference node)
   {
      return true;
   }

   /**
    * Returns information for current connection.
    */
   private PSConnectionInfo getConnectionInfo()
   {
      return PSCoreFactory.getInstance().getConnectionInfo();
   }

   /**
    * The URL of page to edit components.
    */
   private static final String COMPONENTS_URL =
      "/Rhythmyx/sys_cmpComponents/components.html?" +
      "sys_componentname=sys_compbyname&sys_pagename=sys_compbyname";
}
