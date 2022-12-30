/******************************************************************************
 *
 * [ PSEditStylesheetAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.error.PSNotLockedException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.workbench.ui.PSMessages;
import org.eclipse.core.runtime.CoreException;

import java.net.URL;

/**
 * An adapter to be able to open a file from  
 * Eclipse editor to edit file.
 *
 * @author Andriy Palamarchuk
 */
public class PSEditStylesheetAction extends PSAbstractStylesheetAction
{
   public PSEditStylesheetAction()
   {
      super(PSMessages.getString("PSEditStylesheetAction.name"));
   }
   
   /**
    * Actually opens the provided stylesheet.
    * @throws CoreException 
    * @throws IllegalStateException 
    */
   @Override
   protected void doPerformAction()
           throws PSServerException, PSAuthorizationException,
           PSAuthenticationFailedException, PSNotLockedException,
           PSIllegalStateException, IllegalStateException, CoreException, PSSystemValidationException, PSValidationException {
      final URL url = getResultPage().getStyleSheet();
      if (getResultPage().isContentLoaded())
      {
         getEditor().registerStreamAsLoadedResource(url,
               getResultPage().clearContent().getContent());
      }
      getEditor().openEditorForResource(url);
   }

   /**
    * Enabled only if has stylesheet.
    */
   @Override
   protected boolean checkEnabled()
   {
      return hasStylesheet();
   }
}
