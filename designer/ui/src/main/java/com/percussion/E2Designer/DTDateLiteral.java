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
import org.apache.commons.lang3.time.FastDateFormat;
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

      FastDateFormat [] formatters = new FastDateFormat[formats.length];
      Date date = null;
      int i = 0;
      String formatPatterns = "";
      // at what position did the parsing fail, used in error msg
      int errorOffset = 0;
      for ( ; null == date && i < formats.length; ++i )
      {
         try
         {
            formatters[i] = FastDateFormat.getDateTimeInstance( formats[i], formats[i] );
            if ( formatters[i] !=null )
               System.out.println( ( formatters[i]).getPattern());
            date = formatters[i].parse( strValue );
         }
         catch ( ParseException pe )
         {
            // ignore this and try next format
            // Buildup an error message while we go to return if we can't parse it
            if ( formatters[i] instanceof FastDateFormat )
               formatPatterns += "    " + (formatters[i]).getPattern() + "\n";
         }
      }
      if ( null == date )
      {
         // try again, this time just using date
         for ( i = 0; null == date && i < formats.length; ++i )
         {
            try
            {
               formatters[i] = FastDateFormat.getDateInstance( formats[i] );
               if ( formatters[i] !=null ) {
                  System.out.println((formatters[i]).getPattern());
                  date = formatters[i].parse(strValue);
               }
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
               formatters[i] = FastDateFormat.getTimeInstance( formats[i] );

               if ( formatters[i] !=null ) {
                  System.out.println(( formatters[i]).getPattern());
                  date = formatters[i].parse(strValue);
               }
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
            FastDateFormat sdf = null;
            /* Usually the formatter is a SimpleDateFormat object, but it is not
               guaranteed to be. */
            if ( !(formatters[formatIndex] instanceof FastDateFormat ))
               sdf = FastDateFormat.getInstance();
            else
               sdf =  formatters[formatIndex];
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
                 errorOffset,
            NumberFormat.getNumberInstance().format(0),
            formatPatterns
         };
         String errorMsg = MessageFormat.format(
            E2Designer.getResources().getString( "CantParseDate" ), params );
         throw new IllegalArgumentException( errorMsg );
      }
   }
}
