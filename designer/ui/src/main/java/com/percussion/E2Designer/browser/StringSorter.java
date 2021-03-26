/*[ StringSorter.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;


import java.text.Collator;

public class StringSorter
{


   static public String [] sortCaseInsensitiveDefaultLocale(String [] s)
   {
       // Compare the strings using the default locale
      Collator c = Collator.getInstance();
      c.setStrength(Collator.SECONDARY);     // for case insensitive comparison
      for ( int i = 0; i < s.length; i++)
      {
         int min = i;
         for (int j=i; j < s.length; j++)
         {
            if (c.compare(s[j], s[min]) < 0)
               min =j;
         }
         String sTemp;
         sTemp = s[i];
         s[i] = s[min];
         s[min] = sTemp;
      }
    return s;
   }
}

   
