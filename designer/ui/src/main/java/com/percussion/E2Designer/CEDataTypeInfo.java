/*[ CEDataTypeInfo.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to contain information about a particular content editor datatype.
 */
public class CEDataTypeInfo
{
   /**
    * @return <code>true</code> if numeric values are valid formats,
    * <code>false</code> if not.  Returns <code>true</code> by default unless
    * {@link #setSupportsNumeric(boolean)} has been called to modify this value.
    */
   public boolean supportsNumeric()
   {
      return m_numeric;
   }

   /**
    * Sets whether or not this type supports a numeric format value.
    *
    * @param supportsNumeric  <code>true</code> if numeric values are valid
    * formats, <code>false</code> if not.
    */
   public void setSupportsNumeric(boolean supportsNumeric)
   {
      m_numeric = supportsNumeric;
   }

   /**
    * Returns the list of predefined formats.
    *
    * @return An iterator over zero or more formats as Strings, never <code>null
    * </code>, may be empty if none are defined.  Will be empty by default
    * unless {@link #setFormats(Iterator)} has been called to modify this value.
    */
   public Iterator getFormats()
   {
      return m_formatList.iterator();
   }

   /**
    * Sets the list of predefined formats.
    *
    * @param formats An Iterator over one or more formats as Strings, never
    * <code>null</code>, may be empty if none are defined.  The entries in the
    * list may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if formats is <code>null</code>, or if
    * if it contains a <code>null</code> or empty value.
    */
   public void setFormats(Iterator formats)
   {
      if (formats == null)
         throw new IllegalArgumentException("formats may not be null");

      m_formatList.clear();
      while (formats.hasNext())
      {
         String format = (String)formats.next();
         if (format == null || format.trim().length() == 0)
            throw new IllegalArgumentException(
               "formats may not contain null or empty values");
         m_formatList.add(format);
      }
   }

   /**
    * Determines if the specified format is valid.
    *
    * @param format The format to check, may not be <code>null</code>.
    *
    * @return <code>true</code> if the format matches a predefined format
    * or if numeric are supported and it represents a numeric value.  <code>
    * false</code> otherwise.  Match is case-sensitive.
    *
    * @throws IllegalArgumentException if format is <code>null</code>.
    */
   public boolean isValid(String format)
   {
      if (format == null)
         throw new IllegalArgumentException("format may not be null");

      boolean isValid = false;
      if (m_formatList.contains(format))
         isValid = true;
      else if (supportsNumeric())
      {
         try
         {
            int test = Integer.parseInt(format);
            // format has to be greater than 1 
            if(test >=1) 
               isValid = true;
         }
         catch(NumberFormatException e)
         {
            // leave result as false
         }
      }

      return isValid;
   }

   /**
    * Sets the default format.
    *
    * @param format The format, may be <code>null</code>.  Setting
    * this value does not automatically enable formats.  If not <code>null
    * </code>, must be a supported format.
    *
    * @throws IllegalArgumentException if format is invalid or if formats are
    * not supported.
    */
   public void setDefaultFormat(String format)
   {
      if (format != null)
      {
         if (!supportsFormat())
            throw new IllegalArgumentException("formats not supported");

         if (!isValid(format))
            throw new IllegalArgumentException("invalid format");
      }

      m_defaultFormat = format;
   }

   /**
    * Gets the default format.
    *
    * @return The deault format, <code>null</code> if formats not supported,
    * or a default has not been defined, otherwise a valid format.
    */
   public String getDefaultFormat()
   {
      return m_defaultFormat;
   }

   /**
    * Determines if specifying a format is valid.
    *
    * @return <code>true</code> if the datatype supports a format.  <code>
    * false</code> otherwise.
    */
   public boolean supportsFormat()
   {
      return m_numeric || m_formatList.size() != 0;
   }

   /**
    * List of predefined formats, never <code>null</code>, may be empty.
    */
   private List m_formatList = new ArrayList();

   /**
    * The default format for this datatype.  Intialized to <code>null</code>,
    * may be modified by a call to {@link #setDefaultFormat(String)
    * setDefaultFormat}.
    */
   private String m_defaultFormat = null;

   /**
    * <code>true</code> if numeric values are valid formats, <code>false
    * </code> if not.  <code>true</code> by default.
    */
   private boolean m_numeric = true;
}

