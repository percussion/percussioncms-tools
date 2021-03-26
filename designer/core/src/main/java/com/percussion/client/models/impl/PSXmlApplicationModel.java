/******************************************************************************
 *
 * [ PSXmlApplicationModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSXmlApplicationModel;
import com.percussion.client.proxies.impl.PSXmlApplicationModelProxy;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.security.PSAuthorizationException;

/**
 * XML application model. Adds functionality to start/stop the application.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationModel extends PSCmsModel
      implements IPSXmlApplicationModel
{
   /**
    * The constructor.
    */
   public PSXmlApplicationModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }
   
   /**
    * Starts/stops the application by switching its "enabled" status. 
    */
   public void toggleStatus(final IPSReference ref)
         throws PSAuthorizationException, PSModelException
   {
      if (ref == null)
      {
         throw new IllegalArgumentException("ref cannot be null");
      }
      
      getAppProxy().toggleStatus(ref,
            (PSApplication) getDataFromCache(ref, false));
   }

   /**
    * Convenience method to get application model proxy.
    */
   private PSXmlApplicationModelProxy getAppProxy()
   {
      return (PSXmlApplicationModelProxy) getProxy();
   }
   
   /**
    * Gets the application summaries from the server and returns
    * <code>true</code> if the
    * application is running on the server. (Checks for the isActive property
    * on the returned enumeration.)
    * @throws PSModelException if data retrieval from the server fails
    *
    *@returns <code>true</code> if the application is running
    *(has been started) on the server.
    */
   public boolean isAppRunningOnServer(IPSReference ref) throws PSModelException
   {
      return getAppProxy().isAppRunningOnServer(ref);
   }
}
