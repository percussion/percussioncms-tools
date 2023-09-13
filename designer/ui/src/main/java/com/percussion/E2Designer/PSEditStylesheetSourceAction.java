/******************************************************************************
 *
 * [ PSEditStylesheetSourceAction.java ]
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
import com.percussion.workbench.ui.PSMessages;
import com.percussion.share.service.exception.PSValidationException;
import org.eclipse.core.runtime.CoreException;

import java.net.MalformedURLException;

/**
 * Edits stylesheet source.
 *
 * @author Andriy Palamarchuk
 */
public class PSEditStylesheetSourceAction extends PSAbstractStylesheetAction
{
   public PSEditStylesheetSourceAction()
   {
      super(PSMessages.getString("PSEditStylesheetSourceAction.name")); //$NON-NLS-1$
   }

   @Override
   protected boolean checkEnabled()
   {
      return hasStylesheet() && hasStylesheetSource();
   }

   @Override
   protected void doPerformAction() throws MalformedURLException, PSServerException, PSAuthenticationFailedException, PSIllegalStateException, PSNotLockedException, PSAuthorizationException, CoreException, PSValidationException, PSSystemValidationException, PSValidationException {
      getEditor().openEditorForResource(getSourceFileURL());
      getEditor().updateOnSave(getSourceFileURL(), getStylesheetFileURL(), getResultPage());
   }
}
