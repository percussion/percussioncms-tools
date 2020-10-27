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

import com.percussion.workbench.ui.PSMessages;

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
   protected void doPerformAction() throws Exception
   {
      getEditor().openEditorForResource(getSourceFileURL());
      getEditor().updateOnSave(getSourceFileURL(), getStylesheetFileURL(), getResultPage());
   }
}
