/******************************************************************************
 *
 * [ IPSXmlApplicationModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.security.PSAuthorizationException;

/**
 * XML application model. Adds functionality to start/stop the application.
 *
 * @author Andriy Palamarchuk
 */
public interface IPSXmlApplicationModel extends IPSCmsModel
{
   /**
    * Starts/stops the application by switching its "enabled" status. 
    * @throws PSAuthorizationException when user does not have authorization
    * to execute the action.
    * @throws PSModelException on failure. 
    */
   void toggleStatus(final IPSReference ref)
         throws PSAuthorizationException, PSModelException;
   
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
   boolean isAppRunningOnServer(IPSReference ref) throws PSModelException;
}
