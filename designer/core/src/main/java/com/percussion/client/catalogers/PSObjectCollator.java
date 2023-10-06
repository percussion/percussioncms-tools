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
      this.delegate = delegate;
   }

   @Override
   public int compare( Object obj1, Object obj2 )
   {
      return compare( obj1.toString(), obj2.toString());
   }

   @Override
   public int compare( String s1, String s2 )
   {
      return delegate.compare( s1, s2 );
   }

   @Override
   public CollationKey getCollationKey(String source)
   {
      return delegate.getCollationKey( source );
   }

   @Override
   public int hashCode()
   {
      return delegate.hashCode();
   }

   @Override
   public boolean equals(Object o)
   {
      if (!(o instanceof PSObjectCollator))
         return false;
      PSObjectCollator oc = (PSObjectCollator) o;
      return delegate.equals(oc.delegate);
   }
   
   // storage
   private final Collator delegate;
}
