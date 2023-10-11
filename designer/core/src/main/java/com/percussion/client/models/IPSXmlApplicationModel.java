/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    *@return <code>true</code> if the application is running
    *(has been started) on the server.
    */
   boolean isAppRunningOnServer(IPSReference ref) throws PSModelException;
}
