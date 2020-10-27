/*[ ScientificFormat.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;


/**
 * A simple class that has internationalized formatting for scientific
 * notation (e.g. 1.23e27). If a formatted number has more than MAX_DIGITS in
 * its formatted output, the number will be converted to exponential notation.
 * It could be smarter about using scientific notation when place holding zeros
 * are present.
**/
public class ScientificFormat extends DecimalFormat
{
   public ScientificFormat()
   {
      super();
   }

   /**
    * If a formatted number has more than this number of digits, it will be
    * formatted using scientific notation. The value was arbitrarily chosen.
   **/
   public static final int MAX_DIGITS = 18;


   /**
    * Formats the supplied number based on its formatted size. If the number w/o scientific
    * notation is fewer than MAX_DIGITS, then the DecimalFormat will be used to
    * format the number, otherwise it will be displayed in scientific notation.
    * The params are same as the base class.
   **/
   public StringBuffer format(double number, StringBuffer toAppendTo,
                                                      FieldPosition pos)
   {
      StringBuffer out = super.format( number, toAppendTo, pos );
      if ( out.length() > MAX_DIGITS || number < Double.parseDouble( "1e-" + (getMaximumFractionDigits()-1)))
      {
         // clone this object so we get its traits, then reset the formatting pattern
         ScientificFormat sf = (ScientificFormat) clone();
         sf.applyPattern( SCIENTIFIC_PATTERN );
         StringBuffer sciNot = new StringBuffer(MAX_DIGITS);
         out = sf.formatScientific( number, sciNot, pos );
      }
      return  out;
   }


   /**
    * This method is used only to prevent a possible infinite recursion if format
    * was called directly. Certain values of MAX_DIGITS could cause a problem.
   **/
   private StringBuffer formatScientific( double number, StringBuffer toAppendTo,
      FieldPosition pos )
   {
      return super.format( number, toAppendTo, pos );
   }

   /**
    * If the number is in scientific notation, a double is returned. Otherwise,
    * it returns the same value that would have been returned by DecimalFormat.
    *
    * @param value A string representing a number in decimal or scientific format.
    *
    * @return If value is in scientific notation, a double is returned, otherwise
    * the return is the same as if DecimalFormat had been used.
   **/
   public Number parse( String value )
   {
      ParsePosition pos = new ParsePosition(0);
      int chars = value.length();
      Number mantissa = super.parse( value, pos );
      if ( pos.getIndex() < chars )
      {
         // is the next char an 'e' or 'E', indicating a scientific number?
         Number exponent = new Integer(0);
         if ( SCIENTIFIC_NOTATION == value.toUpperCase().charAt( pos.getIndex()))
         {
            pos.setIndex(pos.getIndex()+1);
            boolean bParseInt = isParseIntegerOnly();
            setParseIntegerOnly(true);
            exponent = super.parse( value, pos);
            setParseIntegerOnly( bParseInt );

            if ( null != exponent && !exponent.equals(new Integer(0)) )
            {
               String scientific = mantissa.toString();
               // after converting the mantissa, it may have an exponent, depending on its mag
               int eOffset = scientific.toUpperCase().indexOf( SCIENTIFIC_NOTATION );
               if ( -1 == eOffset )
                  scientific += SCIENTIFIC_NOTATION + exponent.toString();
               else
               {
                  String mant = scientific.substring(0, eOffset );
                  String subExp = scientific.substring( eOffset+1 );
                  scientific = mant + SCIENTIFIC_NOTATION + ( Integer.parseInt(subExp) + exponent.intValue());
               }
               return new Double(scientific);
            }
         }
      }
      return mantissa;
   }

   /**
    * The character used to indicate the start of the exponent (capitalized).
   **/
   private static final char SCIENTIFIC_NOTATION = 'E';

   /**
    * This is the pattern used when formatting numbers for scientific notation.
    * This class could be extended to allow the user of this class to set this
    * pattern.
   **/
   private static final String SCIENTIFIC_PATTERN = "0.#################E0";
}

