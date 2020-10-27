/*[ PSMapPair.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.util;

/**
 * A simple implementation of the Map.Entry class which is not available
 * publicly. It forms an immutable pairing of 2 objects, one of which may
 * be <code>null</code>. Useful for storing param/value pairings.
 */
public class PSMapPair
{
   /**
    * The only constructor.
    *
    * @param key A non-<code>null</code> object.
    *
    * @param value The other half of the pair. May be <code>null</code>.
    *
    * @throws IllegalArgumentException if key is <code>null</code>.
    */
   public PSMapPair( Object key, Object value )
   {
      if ( null == key )
         throw new IllegalArgumentException( "key cannot be null" );
      m_key = key;
      m_value = value;
   }


   /**
    * Accessor for left half of pair.
    *
    * @return the key set in the constructor. Never <code>null</code>.
    */
   public Object getKey()
   {
      return m_key;
   }


   /**
    * Accessor for right half of pair.
    *
    * @return The value set in the constructor. May be <code>null</code>.
    */
   public Object getValue()
   {
      return m_value;
   }

   /**
    * Mutator for left half of pair.
    *
    * @param A non-<code>null</code> object.
    */
   public void setKey(Object key)
   {
      if (key == null)
         throw new IllegalArgumentException( "key cannot be null" );
      m_key = key;
   }


   /**
    * Mutator for right half of pair.
    *
    * @param value, may be <code>null</code>.
    */
   public void setValue(Object value)
   {
      m_value = value;
   }

   /**
    * The key part of the pairing. Never <code>null</code> after construction.
    */
   private Object m_key;

   /**
    * The value part of the pairing. May be <code>null</code>.
    */
   private Object m_value;
}

