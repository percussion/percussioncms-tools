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
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
