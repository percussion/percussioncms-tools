/******************************************************************************
 *
 * [ PSObjectInfoExtractor.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.proxies.PSObjectFactory;

/**
 * This singleton class knows how to get certain, basic properties from any
 * design object.
 *
 * @author paulhoward
 */
public class PSObjectInfoExtractor
{
   /**
    * The internal name of the supplied object.
    * 
    * @param o Never <code>null</code>. If the object is not correct for the
    * type, a <code>ClassCastException</code> will be thrown.
    * 
    * @param type What the supplied data is supposed to be. Never
    * <code>null</code>.
    * 
    * @return Never <code>null</code> or empty.
    */
   public static String getName(Object o, IPSPrimaryObjectType type)
   {
      //let getReference() validate contract
      return getReference(o, type).getName();
   }

   /**
    * The end-user name of the supplied object. If there isn't one, the name is
    * returned.
    * 
    * @param o Never <code>null</code>. If the object is not correct for the
    * type, a <code>ClassCastException</code> will be thrown.
    * 
    * @param type What the supplied data is supposed to be. Never
    * <code>null</code>.
    * 
    * @return Never <code>null</code> or empty.
    */
   public static String getLabelKey(Object o, IPSPrimaryObjectType type)
   {
      //let getReference() validate contract
      return getReference(o, type).getLabelKey();
   }
   
   /**
    * The description of the supplied object.
    * 
    * @param o Never <code>null</code>. If the object is not correct for the
    * type, a <code>ClassCastException</code> will be thrown.
    * 
    * @param type What the supplied data is supposed to be. Never
    * <code>null</code>.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public static String getDescription(Object o, IPSPrimaryObjectType type)
   {
      //let getReference() validate contract
      return getReference(o, type).getDescription();
   }
   
   /**
    * The client object type of the supplied object.
    * 
    * @param o Never <code>null</code>. If the object is not correct for the
    * type, a <code>ClassCastException</code> will be thrown.
    * 
    * @param type What the supplied data is supposed to be. Never
    * <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   public static PSObjectType getObjectType(Object o, IPSPrimaryObjectType type)
   {
      //let getReference() validate contract
      return getReference(o, type).getObjectType();
   }
   
   /**
    * Create a reference for the supplied object. This reference is for local
    * use only. It should not be returned to any caller.
    * 
    * @param o If <code>null</code>, IAE thrown. If unknown, ClassCastException
    * thrown.
    * 
    * @return Never <code>null</code>.
    */
   private static IPSReference getReference(Object o, IPSPrimaryObjectType type)
   {
      if (null == o)
      {
         throw new IllegalArgumentException("object cannot be null");  
      }
      if (type == null)
      {
         throw new IllegalArgumentException("type cannot be null");  
      }

      return PSObjectFactory.objectToReference(o, type, false);
   }
   
   /**
    * Private to implement singleton.
    */
   private PSObjectInfoExtractor()
   {}
}
