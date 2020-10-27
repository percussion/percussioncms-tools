/******************************************************************************
*
* [ PSReferenceComparator.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/

package com.percussion.workbench.ui.util;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.PSMessages;

import java.util.Comparator;

/**
 * A comparator used to sort <code>IPSReference</code> objects.
 */
public class PSReferenceComparator implements Comparator<IPSReference>
{
   /**
    * Ctor for the comparator
    * @param useName flag indicating that the reference names will be
    * compared. If <code>false</code> the labels will be compared.
    * @param ascending if <code>true</code> then sort in ascending order
    * else sort in descending order.
    */
   public PSReferenceComparator(boolean useName, boolean ascending)
   {
     m_useName = useName;
     m_ascending = ascending;
   }
   
   /**
    * Calls {@link #PSReferenceComparator(boolean, boolean) PSReferenceComparator(true, true)}.
    */
   public PSReferenceComparator()
   {
      this(true, true);
   }

   /* 
    * @see java.util.Comparator#compare(T, T)
    */
   public int compare(final IPSReference ref1, final IPSReference ref2)
   {      
      int result = 0;
      if(ref1 == null && ref2 != null)
         result = -1;
      else if(ref1 != null && ref2 == null)
         result = 1;
      else if(ref1 == null && ref2 == null)
         result = 0;
      else
      {
         String text1 = m_useName ? ref1.getName() 
            : PSMessages.getString(ref1.getLabelKey());
         String text2 = m_useName ? ref2.getName() 
            : PSMessages.getString(ref2.getLabelKey());
         
         result = text1.compareToIgnoreCase(text2);
      }
      if(!m_ascending)
         result = -result;
      return result;
   }
   
   private boolean m_useName = true;
   private boolean m_ascending = true;
}
