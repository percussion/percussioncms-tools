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

import java.util.HashMap;


/**
 * This is a simple version of a class by the same name in the Rx server. It
 * provides a mapping between Java encoding names and 'standard' encoding names
 * (which are names written for encoding specs in DTD, HTML and other files). It
 * also contains a couple of useful constants.
**/
public class PSCharSets
{

   public final static String DEFAULT_INPUT_ENCODING = System.getProperty( "file.encoding" );
   public final static String DEFAULT_OUTPUT_ENCODING = DEFAULT_INPUT_ENCODING;

   /**
    * @return The Java name of the character encoding used internally by the
    * splitter.
   **/
   static public String getInternalEncoding()
   {
      return "UTF8";
   }

   /**
    * Searches the alias mapping table for a java name that matches the one
    * supplied (ignoring case). If one is found and it has an associated standard
    * name, that name is returned. Otherwise, javaName is returned.
   **/
   static public String getStdName( String javaName )
   {
      if ( null == javaName )
         return null;

      String stdName = (String) alias.get( javaName.toLowerCase());
      if ( null == stdName || 0 == stdName.length())
         stdName = javaName;
      return stdName;
   }
   /* Contains mappings between Java encoding names and standard name. If the
      standard name is not known, an empty string is placed with that key. */
   private static HashMap<String, String> alias = new HashMap<String, String>(140);

   static
   {
      /* This list contains every known Java encoding type as of JDK1.2.2. */
      alias.put( "ascii" , "US-ASCII" );
      alias.put( "big5" , "BIG5" );
      alias.put( "cp037" , "CP037" );
      alias.put( "cp1006" , "" );
      alias.put( "cp1025" , "" );
      alias.put( "cp1026" , "" );
      alias.put( "cp1046" , "" );
      alias.put( "cp1097" , "" );
      alias.put( "cp1098" , "" );
      alias.put( "cp1112" , "" );
      alias.put( "cp1122" , "" );
      alias.put( "cp1123" , "" );
      alias.put( "cp1124" , "" );
      alias.put( "cp1140" , "" );
      alias.put( "cp1141" , "" );
      alias.put( "cp1142" , "" );
      alias.put( "cp1143" , "" );
      alias.put( "cp1144" , "" );
      alias.put( "cp1145" , "" );
      alias.put( "cp1146" , "" );
      alias.put( "cp1147" , "" );
      alias.put( "cp1148" , "" );
      alias.put( "cp1149" , "" );
      alias.put( "cp1250" , "" );
      alias.put( "cp1251" , "" );
      alias.put( "cp1252" , "ISO-8859-1" );
      alias.put( "cp1253" , "WINDOWS-1253" );
      alias.put( "cp1254" , "ISO-8859-9" );
      alias.put( "cp1255" , "" );
      alias.put( "cp1256" , "" );
      alias.put( "cp1257" , "" );
      alias.put( "cp1258" , "" );
      alias.put( "cp1381" , "" );
      alias.put( "cp1383" , "" );
      alias.put( "cp273" , "" );
      alias.put( "cp277" , "" );
      alias.put( "cp278" , "" );
      alias.put( "cp280" , "" );
      alias.put( "cp284" , "" );
      alias.put( "cp285" , "" );
      alias.put( "cp297" , "" );
      alias.put( "cp33722" , "" );
      alias.put( "cp420" , "" );
      alias.put( "cp424" , "" );
      alias.put( "cp437" , "" );
      alias.put( "cp500" , "" );
      alias.put( "cp737" , "" );
      alias.put( "cp775" , "" );
      alias.put( "cp838" , "" );
      alias.put( "cp850" , "" );
      alias.put( "cp852" , "" );
      alias.put( "cp855" , "" );
      alias.put( "cp856" , "" );
      alias.put( "cp857" , "" );
      alias.put( "cp858" , "" );
      alias.put( "cp860" , "" );
      alias.put( "cp861" , "" );
      alias.put( "cp862" , "" );
      alias.put( "cp863" , "" );
      alias.put( "cp864" , "" );
      alias.put( "cp865" , "" );
      alias.put( "cp866" , "" );
      alias.put( "cp868" , "" );
      alias.put( "cp869" , "" );
      alias.put( "cp870" , "" );
      alias.put( "cp871" , "" );
      alias.put( "cp874" , "" );
      alias.put( "cp875" , "" );
      alias.put( "cp918" , "" );
      alias.put( "cp921" , "" );
      alias.put( "cp922" , "" );
      alias.put( "cp930" , "" );
      alias.put( "cp933" , "" );
      alias.put( "cp935" , "" );
      alias.put( "cp937" , "" );
      alias.put( "cp939" , "" );
      alias.put( "cp942" , "" );
      alias.put( "cp942c" , "" );
      alias.put( "cp943" , "" );
      alias.put( "cp943c" , "" );
      alias.put( "cp948" , "" );
      alias.put( "cp949" , "" );
      alias.put( "cp949c" , "" );
      alias.put( "cp950" , "" );
      alias.put( "cp964" , "" );
      alias.put( "cp970" , "" );
      alias.put( "dbcs_ascii" , "" );
      alias.put( "dbcs_ebcdic" , "" );
      alias.put( "doublebyte" , "" );
      alias.put( "euc" , "" );
      alias.put( "euc_cn" , "GB2312" );
      alias.put( "euc_jp" , "EUC-JP" );
      alias.put( "euc_kr" , "EUC-KR" );
      alias.put( "euc_tw" , "" );
      alias.put( "gbk" , "" );
      alias.put( "iso2022" , "" );
      alias.put( "iso2022cn" , "" );
      alias.put( "iso2022jp" , "ISO-2022-JP" );
      alias.put( "iso2022kr" , "ISO-2022-KR" );
      alias.put( "iso8859_1" , "ISO-8859-1" );
      alias.put( "iso8859_2" , "ISO-8859-2" );
      alias.put( "iso8859_3" , "ISO-8859-3" );
      alias.put( "iso8859_4" , "ISO-8859-4" );
      alias.put( "iso8859_5" , "ISO-8859-5" );
      alias.put( "iso8859_6" , "ISO-8859-6" );
      alias.put( "iso8859_7" , "ISO-8859-7" );
      alias.put( "iso8859_8" , "ISO-8859-8" );
      alias.put( "iso8859_9" , "ISO-8859-9" );
      alias.put( "jis0201" , "" );
      alias.put( "jis0208" , "" );
      alias.put( "jis0212" , "" );
      alias.put( "johab" , "" );
      alias.put( "koi8_r" , "KOI8_R" );
      alias.put( "macarabic" , "" );
      alias.put( "maccentraleurope" , "" );
      alias.put( "maccroatian" , "" );
      alias.put( "maccyrillic" , "" );
      alias.put( "macdingbat" , "" );
      alias.put( "macgreek" , "" );
      alias.put( "machebrew" , "" );
      alias.put( "maciceland" , "" );
      alias.put( "macroman" , "" );
      alias.put( "macromania" , "" );
      alias.put( "macsymbol" , "" );
      alias.put( "macthai" , "" );
      alias.put( "macturkish" , "" );
      alias.put( "macukraine" , "" );
      alias.put( "ms874" , "" );
      alias.put( "ms932" , "" );
      alias.put( "ms932db" , "" );
      alias.put( "ms936" , "" );
      alias.put( "ms949" , "" );
      alias.put( "ms950" , "" );
      alias.put( "singlebyte" , "" );
      alias.put( "sjis" , "SHIFT_JIS" );
      alias.put( "tis620" , "" );
      alias.put( "unicode" , "" );
      alias.put( "unicodebig" , "" );
      alias.put( "unicodelittle" , "" );
      alias.put( "utf8", "UTF-8" );
   }
}

