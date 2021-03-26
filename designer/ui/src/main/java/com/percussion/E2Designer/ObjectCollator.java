/*[ ObjectCollator.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.text.CollationKey;
import java.text.Collator;

/**
 * Creates a collator that works on objects that can be sorted by their <code>
 * toString</code> values. It does this by trapping the compare method calls
 * and using toString rather than casting to a string.
**/
public class ObjectCollator extends Collator
{

   /**
    * Creates the collator.
    *
    * @param delegate The real collator that does the work.
    *
    * @throws IllegalArgumentException if delegate is null
   **/
   public ObjectCollator( Collator delegate )
   {
      if ( null == delegate )
         throw new IllegalArgumentException();
      m_delegate = delegate;
   }

   public int compare( Object obj1, Object obj2 )
   {
      return compare( obj1.toString(), obj2.toString());
   }

   public int compare( String s1, String s2 )
   {
      return m_delegate.compare( s1, s2 );
   }

   public CollationKey getCollationKey(String source)
   {
      return m_delegate.getCollationKey( source );
   }

   public int hashCode()
   {
      return m_delegate.hashCode();
   }

   // storage
   private Collator m_delegate = null;
}
