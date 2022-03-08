/******************************************************************************
 *
 * [ PSRepositoryViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import java.text.MessageFormat;

/**
 * The handler to handle 'repository' view. 
 */
public class PSRepositoryViewHandler extends PSViewHandler
{   
   /**
    * Gets the status label as "driver:<driver>, server:<server>, 
    * database:<database>, origin:<origin>".
    * 
    * @return the label, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the data object is not yet set.
    */
   public String getViewLabel()
   {
      if(m_object == null)
         throw new IllegalStateException("The data object is not set");
      
      OSDbmsInfo dbmsInfo = (OSDbmsInfo)m_object;
      
      String[] args = { dbmsInfo.getDriver(), dbmsInfo.getServer(), 
         dbmsInfo.getDatabase(), dbmsInfo.getOrigin() };
         
      return MessageFormat.format(ms_labelString, args);
   }

   /**
    * Checks whether the object is supported by this view handler. The object
    * must be an instance of <code>OSDbmsInfo</code>.
    * 
    * @param object the object to check, may be <code>null</code>
    * 
    * @return <code>true</code> if the object is supported, otherwise 
    * <code>false</code>
    */     
   public boolean supportsObject(Object object)
   {
      if(object instanceof OSDbmsInfo)
         return true;
         
      return false;
   }
   
   /**
    * The label string that needs to be displayed in status bar after formatting
    * with dynamic values.
    */
   private static final String ms_labelString = 
      PSDeploymentClient.getResources().getString("repositoryView");
}
