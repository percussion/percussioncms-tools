/******************************************************************************
 *
 * [ PSAbstractStylesheetAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * Abstract class for actions dealing with {@link OSResultPage} stylesheets.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSAbstractStylesheetAction extends PSFigureAbstractAction
{
   final static Logger ms_log = Logger.getLogger(PSAbstractStylesheetAction.class);

   // see base class
   protected PSAbstractStylesheetAction(String name)
   {
      super(name);
   }

   /**
    * Indicates whether has a stylesheet. 
    */
   public boolean hasStylesheet()
   {
      if (getFigure().getData() instanceof OSResultPage)
      {
         return getStylesheetFileURL() != null;
      }
      return false;
   }

   /**
    * Generates the stylesheet source file URL.
    */
   protected URL getSourceFileURL() throws MalformedURLException
   {
      return new URL("file:" + getSourceFileName());  //$NON-NLS-1$
   }

   /**
    * Returns figure data as {@link OSResultPage}.
    */
   protected OSResultPage getResultPage()
   {
      return (OSResultPage) getFigure().getData();
   }
   
   /**
    * Actually performs action on the provided webpage.
    * Note, webpage must have the stylesheet.
    */
   public final void actionPerformed(ActionEvent e)
   {
      if (getFigure().getData() instanceof OSResultPage)
      {
         assert hasStylesheet();
         try
         {
            doPerformAction();
         }
         catch (Exception ex)
         {
            PSDlgUtil.showError(ex);
         }
      }
   }
   
   @Override
   public void setFigure(UIFigure figure)
   {
      super.setFigure(figure);
      setEnabled(checkEnabled());
   }

   /**
    * Returns <code>true</code> if current page has stylesheet source.
    */
   protected boolean hasStylesheetSource()
   {
      if (!hasStylesheet())
      {
         return false;
      }
      if (getFigure().getData() instanceof OSResultPage)
      {
         try
         {
            return applicationContainsStylesheetSource();
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            ms_log.warn("Could not retrieve source file information", e); //$NON-NLS-1$
            return false;
         }
      }
      return false;
   }

   /**
    * Returns <code>true</code> if the application contains stylesheet source file.
    */
   private boolean applicationContainsStylesheetSource() throws PSServerException, PSAuthorizationException, PSAuthenticationFailedException
   {
      final List<String> files = getObjectStore().getApplicationFiles(
            getApplication().getRequestRoot());
      for (final String fileName : files)
      {
         if (new File(fileName).equals(new File(getSourceFileName())))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Name of the current stylesheet.
    */
   protected String getStylesheetFileName()
   {
      return getStylesheetFileURL().getFile();
   }

   /**
    * URL of the current stylesheet.
    */
   protected URL getStylesheetFileURL()
   {
      return getResultPage().getStyleSheet();
   }

   /**
    * Generates source file name for the current stylesheet.
    */
   protected String getSourceFileName()
   {
      return ICustomDropSourceData.SRC_DIR + "/"                  //$NON-NLS-1$
            + Util.stripPath(getStylesheetFileName(), ".html");   //$NON-NLS-1$
   }

   /**
    * Is called to determine whether the action should be enabled.
    * Subclasses must overwrite it. 
    */
   protected abstract boolean checkEnabled();

   /**
    * Perform actual processing.
    */
   protected abstract void doPerformAction() throws Exception;
}
