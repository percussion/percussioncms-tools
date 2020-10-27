/*******************************************************************************
 *
 * [ PSUserConfigHelper.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.client;

import java.util.Vector;

/**
 * Helper class for {@link com.percussion.client.PSUserConfig}
 */
public class PSUserConfigHelper
{
   public PSUserConfigHelper()
   {

   }

   /**
    * Adds the escape character specified in the PSUserConfig to the passed in
    * string if the string contains delimiters.
    * 
    * @returns the string with the escape chars inserted before delimiter.
    * 
    * For example if the delimiter is ";" and the escape char is '^' and if the
    * passed in string is "Garbage;in" the returned string will be "Garbage^;in"
    * 
    * @see PSUserConfig#DELIMITER
    * @see PSUserConfig#ESCAPE_CHAR
    * 
    */
   static public String addEscapeChars(String str)
   {
      if (str.equals(""))
         return str;

      int index = str.indexOf(PSUserConfig.DELIMITER);
      if (index < 0)
         return str;

      StringBuffer buf = new StringBuffer(str);
      int lastIndex = -1;

      while (true)
      {
         str = buf.toString();
         index = str.indexOf(PSUserConfig.DELIMITER, lastIndex);
         if (index < 0)
            break;
         buf.insert(index, PSUserConfig.ESCAPE_CHAR);
         int length = buf.length();
         if (length - 1 > index + 1)
            lastIndex = index + 2;
         else
            break;
      }

      return buf.toString();
   }

   /**
    * Parses the passed in string based on the escape chars and the delimiters
    * and returns the vector of strings. The returned tokens have their escape
    * chars removed.
    * 
    */
   @SuppressWarnings("unchecked")
   public Vector getStringTokens(String str)
   {
      Vector v = new Vector();
      int length = str.length();
      if (str.equals("") || length == 0)
         return v;

      String firstString = str;
      String lastString = str;
      int index = lastString.indexOf(PSUserConfig.DELIMITER);
      if (index < 0)
      {
         v.add(str);
         return v;
      }

      while (true)
      {
         index = lastString.indexOf(PSUserConfig.DELIMITER);
         // System.out.println("In getStringTokens - lastString = "
         // +lastString);
         if (index > 0)
         {
            StringBuffer buf = new StringBuffer(lastString);
            firstString = getFirstToken(buf);
            lastString = buf.toString();
            v.add(firstString);

            // System.out.println("First token = "+ firstString);
            // System.out.println("lastString = "+ lastString);
            if (lastString.equals(""))
               break;

         }
         else
         {
            if (lastString.length() > 0)
               v.add(lastString);
            return v;
         }

      }

      return v;

   }

   /**
    * Parses and returns the first String token from the passed in string
    * buffer. The parsing removes the escape chars in the token if any.
    * 
    */
   private String getFirstToken(StringBuffer buf)
   {
      String firstString = "";
      boolean bEscapeChar = false;

      while (true)
      {
         int index = buf.toString().indexOf(PSUserConfig.DELIMITER);
         int length = buf.toString().length();
         if (index > 0)
         {
            char chr = buf.toString().charAt(index - 1);
            if ((String.valueOf(chr)).equals(String
               .valueOf(PSUserConfig.ESCAPE_CHAR)))
            {
               bEscapeChar = true;
               // remove escape char
               // System.out.println("..in getFirstToken...lastString
               // ="+buf.toString());
               // System.out.println("..Removing escape char at index
               // ="+(index-1));
               firstString = firstString
                  + buf.toString().substring(0, index - 1)
                  + PSUserConfig.DELIMITER;
               // System.out.println("..firstString = "+firstString);
               if (length - 1 >= index + 1)
               {
                  buf.delete(0, index + 1);
               }
               else
               {
                  length = buf.length();
                  if (length > 0)
                     buf.delete(0, length);
               }
               // System.out.println("..buf.toString() = "+buf.toString());
            }
            else
            {
               if (!buf.toString().equals(""))
                  firstString = firstString
                     + buf.toString().substring(0, index);
               // System.out.println(".... Index = "+index+" firstString =
               // "+firstString);
               if (length - 1 > index + 1)
               {
                  buf.delete(0, index + 1);
                  // System.out.println("Set buf.toString() to....
                  // "+buf.toString());
               }
               else
               {
                  length = buf.length();
                  if (length > 0)
                     buf.delete(0, length);
               }
               return firstString;
            }
         }
         else if (index == 0)
         {
            buf.delete(0, 1);
            // System.out.println("Index = 0, ..buf.toString() =
            // "+buf.toString());
            return firstString;
         }
         else
         {
            if (bEscapeChar)
            {
               length = buf.length();
               firstString = firstString + buf.toString();
               buf.delete(0, length);
               return firstString;
            }

            return buf.toString();
         }
      } // end while
   }

}
