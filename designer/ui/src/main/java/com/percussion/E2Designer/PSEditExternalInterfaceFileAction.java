/******************************************************************************
 *
 * [ PSEditExternalInterfaceFileAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.workbench.ui.PSMessages;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Edits external interface file.
 *
 * @see OSExternalInterface
 * @author Andriy Palamarchuk
 */
public class PSEditExternalInterfaceFileAction extends PSFigureAbstractAction
{
   private final static Logger ms_log = LogManager
         .getLogger(PSEditExternalInterfaceFileAction.class);

   public PSEditExternalInterfaceFileAction()
   {
      super(PSMessages.getString("PSEditExternalInterfaceFileAction.name"));
   }

   public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
   {
      if (getFigure().getData() instanceof OSExternalInterface)
      {
         try
         {
            getEditor().openEditorForResource(getFileUrl());
         }
         catch (Exception ex)
         {
            PSDlgUtil.showError(ex);
         }
      }
   }

   /**
    * The data file url.
    */
   private URL getFileUrl() throws IOException, PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException
   {
      return new URL("file:" + getApplicationFileName());
   }

   /**
    * Returns file name of the corresponding application file.
    */
   @SuppressWarnings("unused") //IOException
   private String getApplicationFileName() throws IOException, PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException
   {
      final OSExternalInterface ei = (OSExternalInterface) getFigure().getData();
      final String eiFileName = new File(ei.getFilePath()).getPath();
      final List<String> files = getObjectStore().getApplicationFiles(
            getApplication().getRequestRoot());
      for (final String fileName : files)
      {
         if (eiFileName.endsWith(new File(fileName).getPath()))
         {
            return fileName;
         }
      }
      return null;
   }
   
   @Override
   public void setFigure(UIFigure figure)
   {
      super.setFigure(figure);
      setEnabled(hasApplicationFile());
   }

   private boolean hasApplicationFile()
   {
      try
      {
         return getFigure().getData() instanceof OSExternalInterface
               && getApplicationFileName() != null;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         ms_log.warn("Could not retrieve file information", e); //$NON-NLS-1$
         return false;
      }
   }
}
