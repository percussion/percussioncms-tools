/*******************************************************************************
 *
 * [ PSLockHelper.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class to find whether an object specified by
 * {@link com.percussion.client.IPSReference} is if:
 * <p>
 * <ol>
 * <li>locked at all</li>
 * <li>locked to logged in user in the current session</li>
 * <li>locked to other</li>
 * <li>locked to logged in user in the a different session</li>
 * </ol>
 * <p>
 */
public class PSLockHelper
{
   /**
    * Default ctor with package access to prevent instantiating outside.
    */
   PSLockHelper(String clientSessionId, String userName)
   {
      if (clientSessionId == null || clientSessionId.length() == 0)
      {
         throw new IllegalArgumentException(
            "clientSessionId must not be null or empty");
      }
      if (userName == null || userName.length() == 0)
      {
         throw new IllegalArgumentException(
            "userName must not be null or empty");
      }
      m_clientSessionid = clientSessionId;
      m_loggedUserName = userName;
   }

   /**
    * Is the object with supplied reference locked by the current user or any
    * one?
    * 
    * @param ref object reference, must not be <code>null</code>.
    * @return <code>false</code> if not locked to any one including logged in
    * user, <code>true</code> otherwise.
    */
   public boolean isLocked(IPSReference ref)
   {
      return !StringUtils.isEmpty(ref.getLockSessionId());
   }

   /**
    * Is the object with supplied reference locked by the current user in the
    * current session?
    * 
    * @param ref object reference, must not be <code>null</code>.
    * @return <code>true</code> if locked to logged in user in the current
    * client session, <code>flase</code> otherwise.
    */
   public boolean isLockedToMe(IPSReference ref)
   {
      String sessionid = ref.getLockSessionId();
      if (StringUtils.isEmpty(sessionid))
         return false;

      if (!m_clientSessionid.equals(sessionid))
         return false;

      // Should not encounter this case at all since existence of sessionid
      // guarantees non-empty user name.
      String locker = ref.getLockUserName();
      if (StringUtils.isEmpty(locker))
         return false;
      //

      if (!m_loggedUserName.equals(locker))
         return false;

      return true;
   }

   /**
    * Is the object with supplied reference locked by the current user in a
    * different session?
    * 
    * @param ref object reference, must not be <code>null</code>.
    * @return <code>true</code> if locked to logged in user in a different
    * client session, <code>flase</code> otherwise.
    */
   public boolean isLockedToMeElsewhere(IPSReference ref)
   {
      String sessionid = ref.getLockSessionId();
      if (StringUtils.isEmpty(sessionid))
         return false;

      // Should not encounter this case at all since existence of sessionid
      // guarantees non-empty user name.
      String locker = ref.getLockUserName();
      if (StringUtils.isEmpty(locker))
         return false;
      //

      if (!m_loggedUserName.equals(locker))
         return false;

      if (m_clientSessionid.equals(sessionid))
         return false;

      return true;
   }

   /**
    * Is the object with supplied reference locked by user other than logged in
    * user?
    * 
    * @param ref object reference, must not be <code>null</code>.
    * @return <code>true</code> if locked to someone else, <code>flase</code>
    * otherwise.
    */
   public boolean isLockedToOther(IPSReference ref)
   {
      String sessionid = ref.getLockSessionId();
      if (StringUtils.isEmpty(sessionid))
         return false;

      String locker = ref.getLockUserName();
      if (StringUtils.isEmpty(locker) || !m_loggedUserName.equals(locker))
         return true;

      return false;
   }

   /**
    * Client sessionid set in the ctor, never <code>null</code> after that.
    * This is used to find if the object is locked for this user in this
    * session.
    */
   private String m_clientSessionid = null;

   /**
    * Currently logged in user name set in the ctor, never <code>null</code>
    * after that. This is used to find if the object is locked for this user in
    * this session.
    */
   private String m_loggedUserName = null;
}
