/******************************************************************************
 *
 * [ PSObjectCollator.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.catalogers;

import java.text.CollationKey;
import java.text.Collator;

/**
 * Creates a collator that works on objects that can be sorted by their <code>
 * toString</code> values. It does this by trapping the compare method calls
 * and using toString rather than casting to a string.
**/
public class PSObjectCollator extends Collator
{

   /**
    * Creates the collator.
    *
    * @param delegate The real collator that does the work.
    *
    * @throws IllegalArgumentException if delegate is null
   **/
   public PSObjectCollator( Collator delegate )
   {
      if ( null == delegate )
         throw new IllegalArgumentException();
      m_delegate = delegate;
   }

   @Override
   public int compare( Object obj1, Object obj2 )
   {
      return compare( obj1.toString(), obj2.toString());
   }

   @Override
   public int compare( String s1, String s2 )
   {
      return m_delegate.compare( s1, s2 );
   }

   @Override
   public CollationKey getCollationKey(String source)
   {
      return m_delegate.getCollationKey( source );
   }

   @Override
   public int hashCode()
   {
      return m_delegate.hashCode();
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSObjectCollator))
         return false;
      PSObjectCollator oc = (PSObjectCollator) o;
      return m_delegate.equals(oc.m_delegate);
   }
   
   // storage
   private Collator m_delegate = null;
}
