/*[ DTNumericLiteral.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSNumericLiteral;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class DTNumericLiteral extends AbstractDataTypeInfo
{
   /**
    * Creates a PSNumericLiteral object, using the supplied strValue as the name.
    */
   public Object create( String strValue )
   {
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException();
      Number val = null;
      DecimalFormat nf = null;
      try
      {
         val = new BigDecimal( strValue );
         nf = new DecimalFormat();
      }
      catch ( NumberFormatException e )
      {
            nf = new ScientificFormat();
            nf.setParseIntegerOnly(false);
            nf.setMaximumFractionDigits(40);
            try
            {
               val = null;
               val = nf.parse( strValue );
            }
            catch ( ParseException pe )
            {
               Object [] params =
               {
                  new Integer( pe.getErrorOffset()),
                  NumberFormat.getNumberInstance().format(0),
                  ((DecimalFormat) nf).toLocalizedPattern()
               };
               String errorMsg;
               if ( nf instanceof DecimalFormat )
                  errorMsg = MessageFormat.format(
                     E2Designer.getResources().getString( "CantParseNumber" ), params );
               else
                  /* the 3rd element in the params array will be ignored here */
                  errorMsg = MessageFormat.format(
                     E2Designer.getResources().getString( "CantParseNumberNoFormat" ),
                        params );
               throw new IllegalArgumentException( errorMsg );
            }
         }

      try
      {
         return new PSNumericLiteral( val, nf );
      }
      catch ( IllegalArgumentException e )
      {
         throw new IllegalArgumentException( e.getLocalizedMessage());
      }
   }

/**
 * Goes through the strValue and removes all the &quot;thousands&quot; (it may
 * be &apos;,&apos; or &apos;.&apos;) delimiters.
 *
 * @param strValue The String value of the number.
 * @param cDelimiter The thousands delimiter.
 *
 * @returns String The String value of the number without the delimiters.
 */
   private String removeDelimiters(String strValue, char cDelimiter)
   {
      String strCopy = new String(strValue);
      String front = null;
      String end = null;
      int count = 0;

      while ( -1 != strCopy.lastIndexOf( cDelimiter ) )
      {
         count = strCopy.lastIndexOf( cDelimiter );
         front = strCopy.substring( 0, count );
         end = strCopy.substring( count+1, strCopy.length() );
         strCopy = front + end;
      }

      return strCopy;
   }
}


