/*[ DTDateLiteral.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSDateLiteral;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DTDateLiteral extends AbstractDataTypeInfo
{
   /**
    * Creates a PSDateLiteral object, using the supplied strValue as the name.
    *
    * @param strValue must be in a format that is parsable by
    * java.util.DateFormat.parse.
    *
    * @see java.text.DateFormat#parse
    */
   public Object create( String strValue )
   {
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException();

      int formats [] =
      {
         DateFormat.FULL,
         DateFormat.LONG,
         DateFormat.MEDIUM,
         DateFormat.SHORT
      };
      /* This is the index into the above array for the format to use when displaying
         dates. It will be passed to the returned PSLiteralDate object. */
      int formatIndex = 1;

      DateFormat [] formatters = new DateFormat[formats.length];
      Date date = null;
      int i = 0;
      String formatPatterns = "";
      // at what position did the parsing fail, used in error msg
      int errorOffset = 0;
      for ( ; null == date && i < formats.length; ++i )
      {
         try
         {
            formatters[i] = DateFormat.getDateTimeInstance( formats[i], formats[i] );
            formatters[i].setLenient( true );
            if ( formatters[i] instanceof SimpleDateFormat )
               System.out.println( ((SimpleDateFormat) formatters[i]).toLocalizedPattern());
            date = formatters[i].parse( strValue );
         }
         catch ( ParseException pe )
         {
            // ignore this and try next format
            // Buildup an error message while we go to return if we can't parse it
            if ( formatters[i] instanceof SimpleDateFormat )
               formatPatterns += "    " + ((SimpleDateFormat) formatters[i]).toLocalizedPattern() + "\n";
         }
      }
      if ( null == date )
      {
         // try again, this time just using date
         for ( i = 0; null == date && i < formats.length; ++i )
         {
            try
            {
               formatters[i] = DateFormat.getDateInstance( formats[i] );
               formatters[i].setLenient( true );
               if ( formatters[i] instanceof SimpleDateFormat )
                  System.out.println( ((SimpleDateFormat) formatters[i]).toLocalizedPattern());
               date = formatters[i].parse( strValue );
            }
            catch ( ParseException pe )
            {
               // ignore this and try next format
            }
         }
      }
      if ( null == date )
      {
         // try again, this time just using date
         for ( i = 0; null == date && i < formats.length; ++i )
         {
            try
            {
               formatters[i] = DateFormat.getTimeInstance( formats[i] );
               formatters[i].setLenient( true );
               if ( formatters[i] instanceof SimpleDateFormat )
                  System.out.println( ((SimpleDateFormat) formatters[i]).toLocalizedPattern());
               date = formatters[i].parse( strValue );
            }
            catch ( ParseException pe )
            {
               // ignore this and try next format
               if ( i == (formats.length-1))
                  errorOffset = pe.getErrorOffset();
            }
         }
      }

      if ( null != date )
      {
         PSDateLiteral literal = null;
         try
         {
            SimpleDateFormat sdf = null;
            /* Usually the formatter is a SimpleDateFormat object, but it is not
               guaranteed to be. */
            if ( !(formatters[formatIndex] instanceof SimpleDateFormat ))
               sdf = new SimpleDateFormat();
            else
               sdf = (SimpleDateFormat) formatters[formatIndex];
            literal = new PSDateLiteral( date, sdf );
         }
         catch ( IllegalArgumentException e)
         {
            throw new IllegalArgumentException( e.getLocalizedMessage());
         }
         return literal;
      }
      else
      {
         Object [] params =
         {
            new Integer( errorOffset ),
            NumberFormat.getNumberInstance().format(0),
            formatPatterns
         };
         String errorMsg = MessageFormat.format(
            E2Designer.getResources().getString( "CantParseDate" ), params );
         throw new IllegalArgumentException( errorMsg );
      }
   }
}
