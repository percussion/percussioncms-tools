/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;

/**
 * This class provides a collection of static utilitiy functions used
 * everywhere.
 */
public class Util
{
   /**
    * Do not instantiate this class.
    */
   private Util()
   {
   }

   /**
    * Replace the argument markers in the provided string with the provided
    * argument strings. {0} is replaced with arg[0] and so on.
    *
    * @param target string to dress with the provided arguments
    * @param args the arguments
    * @return the dressed string
    */
   public static String dress(String target, String[] args)
   {
      String strReturn = "";
      int begin = 0;
      int end = target.length();
      for (int i=0; i<args.length; i++)
      {
         String marker = "{";
         marker += i;
         marker += "}";

         end = target.indexOf(marker);
         strReturn += target.substring(begin, end);
         strReturn += args[i];
         begin = target.indexOf(marker) + marker.length();
      }
      strReturn += target.substring(begin, target.length());

      return strReturn;
   }

   /**
    * Get the root from the provided file. This will be the file name without
    * its extension. If the file is invalid we return the default root name.
    *
    * @param source the file to get the root for.
    * @return the root name for the provided file.
    */
   public static String getRootName(File source)
   {
      if (source == null || !source.isFile())
         return "root";

      String root = source.getName();

      // remove file extension
      int pos = root.lastIndexOf(".");
      if (pos != -1)
         root = root.substring(0, pos);

      return makeXmlName(root);
   }

   /**
    * Makes a valid XML name and returns it.
    *
    * @param str the string to make valid.
    * @return the valid XML name.
    */
   public static String makeXmlName(String str)
   {
      StringBuffer buf = new StringBuffer(str.length());

      if (str.length() == 0)
      {
         buf.append("root");
         return buf.toString();
      }

      char c = str.charAt(0);
      if (!(Character.isLetter(c) || '_' == c /* || ':' == c */))
      {
         buf.append('_');
      }
      else
      {
         buf.append(c);
      }

      for (int i = 1; i < str.length(); i++)
      {
         c = str.charAt(i);
         if (!(
            Character.isLetter(c)
            || Character.isDigit(c) || '.' == c || '-' == c || '_' == c /* || ':' == c */))
         {
            buf.append('_');
         }
         else
         {
            if (0x20DD <= c && c <= 0x20E0)
            {
               buf.append('_');
            }
            else
            {
               buf.append(c);
            }
         }
      }

      return buf.toString();
   }

   /**
    * Returns DocumentBuilder object for parsing XML documents
    * @return DocumentBuilder object for parsing XML documents. Never
    * <code>null</code>.
    * @deprecated Use <code>PSXmlDocumentBuilder#getDocumentBuilder(false)</code>
    * instead.
    */
   static public DocumentBuilder getDocumentBuilder()
   {
      try
      {
         return PSXmlDocumentBuilder.getDocumentBuilder(false);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.getMessage());
      }
   }

   /**
    * Splits a string on a specified delimiter character
    * and returns the resulting string elements in
    * a List collection object.
    *
    * @param str the string to be split. Never <code>null</code>.
    * @param delim the delimiter to split on
    * @returns List of string elements Never <code>null</code>,
    *  but can be empty.
    */
   public static List splitString(String str, char delim)
   {
      List<String> tokens = new ArrayList<String>();
      if(str != null)
      {
         StringTokenizer st = new StringTokenizer(str,String.valueOf(delim));
         while(st.hasMoreTokens())
         {
            tokens.add(st.nextToken());
         }      }
      return tokens;

   }

}
