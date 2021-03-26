/*[ ActionsCommandPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.util.PSMapPair;


public class PSComparablePair extends PSMapPair implements Comparable
{
   public PSComparablePair(Object key, Object value)
   {
      super(key, value);
   }

   public int compareTo(Object o)
   {
      Object obj = getValue();
      if (obj instanceof String)
      {
         return ((String)getValue()).compareToIgnoreCase(
            (String)((PSComparablePair)o).getValue());
      }
      obj = getKey();
      if (obj instanceof String)
      {
         return ((String)getKey()).compareToIgnoreCase(
            (String)((PSComparablePair)o).getKey());
      }
      return 0;

   }

   /**
    * If a value is present, toString() is called on it and returned, otherwise
    * if a key is present, toString() is called on it and returned, otherwise
    * super.toString() is returned.
    *
    * @return Never <code>null</code>.
    */
   public String toString()
   {
      String label = getValue() != null ? getValue().toString()
            : (getKey() != null ? getKey().toString() : super.toString());
      return label;
   }
}