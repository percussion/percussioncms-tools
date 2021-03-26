/******************************************************************************
 *
 * [ IPSinvalidConnectionListener.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client;

import java.util.List;

/**
 * The implementer if this iterface will be notified that the connection used is
 * incorrect and needs to be corrected. The action taken by the implementer
 * typically would be to popup a connetcion dialog box, collect the inofrmation
 * and return appropriately.
 */
public interface IPSinvalidConnectionListener
{
   /**
    * Notifies that the supplied connection has wrong information and requests
    * for correction.
    * 
    * @param wrongConn connection that has incorrect or insufficient
    * information, may be <code>null</code> if the caller does not have
    * connection info yet.
    * @param error error object, typically an exception thrown by server or a
    * string, may be null
    * @param locales a list of valid locales, may be <code>null</code> or
    * empty. If a login attempt fails because the supplied locale is invalid, 
    * then this parameter will be non-null. In that case, the set will contain
    * the valid locales, ordered in ascending alpha order. Generally, the caller
    * will return the supplied connection info with a new locale chosen from 
    * the list.
    * 
    * @return corrected connection information or <code>null</code>. 
    */
   PSConnectionInfo correctConnection(PSConnectionInfo wrongConn, Object error,
         List<String> locales);
}
