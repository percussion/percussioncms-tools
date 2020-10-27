/*[ UICIdentifier.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


/**
 * This class contains the collection of identifiers that are required to 
 * uniquely identify a connectable component across all compoent factories.
 */
public class UICIdentifier
{
   /**
    * Creates a new identifier for a UIConnectable object.
    *
    * @param strFactory the class name of the factory that created the object
    *
    * @param ID the id of the object that is unique within the factory
    *
    * @throws IllegalArgumentException if strFactory is null or empty or ID is
    * INVALID_ID
    *
    * @see Util#INVALID_ID
    */
   public UICIdentifier(String strFactory, int ID)
   {
      if ((null == strFactory) || (0 == strFactory.trim().length()) 
         || (Util.INVALID_ID == ID))
      {
         throw new IllegalArgumentException();
      }
      m_strFactory = strFactory;
      m_ID = ID;
   }

   /**
    * @returns the class name of the factory used to create this object
    */
   public String getFactoryName()
   {
      return(m_strFactory);
   }

   /**
    * @returns the unique identifier for this object. The identifier is unique
    * across all objects created by the associated factory.
    */
   public int getID()
   {
      return(m_ID);
   }

   // private storage
   private String m_strFactory = null;
   private int m_ID = Util.INVALID_ID;
}   
