/******************************************************************************
*
* [ PSLockHelper.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;

import java.util.HashSet;
import java.util.Set;

/**
 * A little helper class to implement locking for the
 * test proxy classes. This is not meant to be used for
 * real locking.
 * @author ErikSerating
 */
public class PSLockHelper
{

   /**
    * Get the lock for the specified key
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if we got the lock. If
    * <code>false</code> then someone else already has
    * the lock.
    */
   public boolean getLock(String key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      if(hasLock(key))
         return false;
      m_locks.add(key);
      return true;
   }
   
   /**
    * Get the lock for the specified key
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if we got the lock. If
    * <code>false</code> then someone else already has
    * the lock.
    */
   public boolean getLock(Long key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      return getLock(key.toString());
   }
   
   /**
    * Get the lock for the specified key
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if we got the lock. If
    * <code>false</code> then someone else already has
    * the lock.
    */
   public boolean getLock(IPSReference key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      return getLock(key.toString());
   }
   
   /**
    * Release the lock for the specified key
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if we released the lock. If
    * <code>false</code> then there was no lock to release.
    */
   public boolean releaseLock(Long key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
     return releaseLock(key.toString());
   }
   
   /**
    * Release the lock for the specified key
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if we released the lock. If
    * <code>false</code> then there was no lock to release.
    */
   public boolean releaseLock(IPSReference key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");      
      return releaseLock(key.toString());
   }
   
   /**
    * Release the lock for the specified key
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if we released the lock. If
    * <code>false</code> then there was no lock to release.
    */
   public boolean releaseLock(String key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      if(!hasLock(key))
         return false;
      m_locks.remove(key);
      return true;
   }
   
   /**
    * Indicates if the specified key has an associated
    * lock.
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if the key has a lock.
    */
   public boolean hasLock(String key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      return m_locks.contains(key);
   }
   
   /**
    * Indicates if the specified key has an associated
    * lock.
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if the key has a lock.
    */
   public boolean hasLock(Long key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      return hasLock(key.toString());
   }
   
   /**
    * Indicates if the specified key has an associated
    * lock.
    * @param key the key to which the lock will apply
    *, cannot be <code>null</code>.
    * @return <code>true</code> if the key has a lock.
    */
   public boolean hasLock(IPSReference key)
   {
      if(key == null)
         throw new IllegalArgumentException("key cannot be null.");
      return hasLock(key.toString());
   }
   
   /**
    * Release all of the locks.
    */
   public void releaseAll()
   {
      m_locks.clear();
   }
     
   /**
    * Set containing all the lock keys
    */
   private Set<String> m_locks = new HashSet<String>();

}
