/******************************************************************************
 *
 * [ PSUiApplication.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Overrides {@link PSApplication} to extend it with a list of application
 * files associated with this application.
 */
public class PSUiApplication extends PSApplication
{
   /**
    * See {@link PSApplication#PSApplication(java.lang.String)}.
    */
   public PSUiApplication(String name)
   {
      super(name);
   }

   /**
    * See {@link PSApplication#PSApplication(Document)}.
    */
   public PSUiApplication(Document sourceDoc) 
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      super(sourceDoc);
   }
   
   /**
    * Get the list of application files associated with this application.
    * 
    * @return a list with application files associated with this 
    *    application, never <code>null</code>, may be empty. Changes to the 
    *    returned list will not affect this class.
    */
   public Collection<PSApplicationFile> getApplicationFiles()
   {
      return new ArrayList<PSApplicationFile>(m_applicationFiles);
   }
   
   /**
    * Set a new list of application files associated with this application.
    * 
    * @param applicationFiles the new list of files to be associated with
    *    this application, may be <code>null</code> or empty. Changes to the 
    *    supplied list will not affect this class.
    */
   public void setApplicationFiles(
      Collection<PSApplicationFile> applicationFiles)
   {
      m_applicationFiles.clear();
      if (applicationFiles != null)
         m_applicationFiles.addAll(applicationFiles);
   }

   /**
    * A list of application files, never <code>null</code>, may be empty. All
    * application files specified will either be created or overridden if the 
    * application is saved.
    */
   private Collection<PSApplicationFile> m_applicationFiles = 
      new ArrayList<PSApplicationFile>();
}

