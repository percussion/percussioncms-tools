/******************************************************************************
 *
 * [ PSGenerateStylesheetFromSourceAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.workbench.ui.PSMessages;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Regenerates stylesheet from its source.
 *
 * @author Andriy Palamarchuk
 */
public class PSGenerateStylesheetFromSourceAction extends PSAbstractStylesheetAction
{
   protected PSGenerateStylesheetFromSourceAction()
   {
      super(PSMessages.getString("PSGenerateStylesheetFromSourceAction.name")); //$NON-NLS-1$
   }

   protected boolean checkEnabled()
   {
      return hasStylesheet() && hasStylesheetSource();
   }

   protected void doPerformAction() throws Exception
   {
      getEditor().flushEditors(new NullProgressMonitor());
      getEditor().updateOnSave(getSourceFileURL(), getStylesheetFileURL(), getResultPage());
      
      final Exception[] exceptions = new Exception[] {null};
      getEditor().getDisplay().syncExec(new Runnable()
      {
         public void run()
         {
            try
            {
               getEditor().generateStylesheetForSource(getSourceFileURL());
            }
            catch (Exception e)
            {
               exceptions[0] = e;
            }
         }
      });
      if (exceptions[0] != null)
      {
         throw exceptions[0];
      }
   }

}
