/******************************************************************************
 *
 * [ PSServerViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.client.PSDeploymentManager;

import java.text.MessageFormat;

/**
 * The handler to handle 'server' view. 
 */
public class PSServerViewHandler extends PSViewHandler
{   
   /**
    * Gets the status label as "name:<name>, port:<port>, version:<version>, 
    * build:<build>".
    * 
    * @return the label, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the data object is not yet set.
    */
   public String getViewLabel()
   {
      if(m_object == null)
         throw new IllegalStateException("The data object is not set");
      
      PSDeploymentServer server = (PSDeploymentServer)m_object;
      PSServerRegistration reg = server.getServerRegistration();
      PSDeploymentManager mgr = server.getDeploymentManager();
      
      if(server.isConnected())
      {
         String[] args = { reg.getServer(), String.valueOf(reg.getPort()), 
            mgr.getConnection().getServerVersion().substring(
                     "Version ".length()),
            mgr.getConnection().getRepositoryInfo().getDatasource()};
         
         return MessageFormat.format(ms_labelConnString, args);
      }
      else
      {
         String[] args = { reg.getServer(), String.valueOf(reg.getPort())};
         return MessageFormat.format(ms_labelNotConnString, args);         
      }
   }

   /**
    * Checks whether the object is supported by this view handler. The object
    * must be an instance of <code>PSDeploymentServer</code>.
    * 
    * @param object the object to check, may be <code>null</code>
    * 
    * @return <code>true</code> if the object is supported, otherwise 
    * <code>false</code>
    */     
   public boolean supportsObject(Object object)
   {
      if(object instanceof PSDeploymentServer)
         return true;
         
      return false;
   }
   
   /**
    * The label string that needs to be displayed in status bar after formatting
    * with dynamic values when the server is connected.
    */
   private static final String ms_labelConnString = 
      PSDeploymentClient.getResources().getString("serverConnView");
   
   /**
    * The label string that needs to be displayed in status bar after formatting
    * with dynamic values when the server is not connected.
    */
   private static final String ms_labelNotConnString = 
      PSDeploymentClient.getResources().getString("serverNotConnView");
}
