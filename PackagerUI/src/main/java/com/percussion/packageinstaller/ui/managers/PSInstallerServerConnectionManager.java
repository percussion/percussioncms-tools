/******************************************************************************
 *
 * [ PSInstallerServerConnectionManager.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packageinstaller.ui.managers;

import com.percussion.packager.ui.managers.PSServerConnectionManager;

/**
 * A subclassed version of PSServerConnectionManager to change file
 * where registrations are persisted.
 *
 */
public class PSInstallerServerConnectionManager
   extends PSServerConnectionManager
{ 
   
   
   
   /**
    * @param filepath
    */
   public PSInstallerServerConnectionManager(String filepath)
   {
      super(filepath);
   }  

   /**
    * Returns the singleton instance of the server connection manager.
    * @return
    */
   public static PSInstallerServerConnectionManager getInstance()
   {
      if(ms_theinstance == null)
      {
         ms_theinstance = new PSInstallerServerConnectionManager(
                  "InstallerServerRegistration.xml");
      }
      return ms_theinstance;
   }
   
   /**
    * Singleton instance of the server connection manager.
    * Initialized in {@link #getInstance()}, never 
    * <code>null</code> after that.
    */
   protected static PSInstallerServerConnectionManager ms_theinstance;  
   
}
