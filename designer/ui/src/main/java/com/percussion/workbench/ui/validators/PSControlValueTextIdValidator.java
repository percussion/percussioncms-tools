/******************************************************************************
 *
 * [ PSControlValueTextIdValidator.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.utils.string.PSStringUtils;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes sure the provided value is a valid identifier - should not contain
 * any characters that are identified by {@link #getInvalidChar(String)}. 
 *
 * @author Andriy Palamarchuk
 */
public class PSControlValueTextIdValidator implements IPSControlValueValidator
{
   // see base
   public String validate(PSControlInfo controlInfo)
   {
      if (controlInfo == null)
      {
         throw new IllegalArgumentException("The control cannot be null.");  //$NON-NLS-1$
      }
      if (!(controlInfo.getControl() instanceof Text))
      {
         throw new IllegalArgumentException(
               "The control must be an instance of the Text control.");  //$NON-NLS-1$
      }

      final String text = ((Text) controlInfo.getControl()).getText();
      if (StringUtils.isBlank(text))
      {
         return null;
      }
      final String displayName = controlInfo.getDisplayName();
      
      return validateId(text, displayName);
   }

   /**
    * Validates the given string.
    * @param str the string in question, not <code>null</code> or empty.
    * @return the first invalid character encounters. It may be 
    *    <code>null</code> if the characters in the given string are all valid.
    */
   public Character getInvalidChar(String str)
   {
      return PSStringUtils.validate(str, PSStringUtils.INVALID_NAME_CHARS);
   }

   /**
    * Just like {@link #getInvalidChar(String)}, except this returns a list
    * of all invalid characters from the given string.
    * 
    * @param str the string in question.
    * 
    * @return a list of invalid characters, never <code>null</code>, but may be
    *    empty if there is no invalid characters in the given string.
    */
   private List<Character> getInvalidChars(String str)
   {
      List<Character> illegalChars = new ArrayList<Character>();
      String subStr = str;
      while (!StringUtils.isBlank(subStr))
      {
         Character ch = getInvalidChar(subStr);
         if (ch == null)
            break;
         illegalChars.add(ch);
         int index = subStr.indexOf(ch.charValue());
         if (index == -1 || (index+1) >= subStr.length())
            break;
         
         subStr = subStr.substring(index+1);
      }
      return illegalChars;
   }

   /**
    * Returns an error message if the provided id contains characters that are 
    * identified by {@link #getInvalidChar(String)}.
    * @param id the id to check. Not <code>null</code>, not blank.
    * @param displayName how to name the value.
    * Used in an error message to indicate the associated control name.
    * Not <code>null</code>, not blank.
    * @return the error message or null if the validation succeeds.
    */
   public String validateId(final String id, final String displayName)
   {
      if (StringUtils.isBlank(id))
      {
         throw new IllegalArgumentException("id should be specified");
      }
      if (StringUtils.isBlank(displayName))
      {
         throw new IllegalArgumentException("display name should be specified");
      }
      List<Character> illegalChars = getInvalidChars(id);
      final StringBuffer message = new StringBuffer();
      if (!illegalChars.isEmpty())
      {
         message.append(PSMessages.getString("common.illegalChar.message",
               displayName, 
               "'" + StringUtils.join(illegalChars.iterator(), "', '") + "'"));
      }
      
      validateNameStart(id, message, displayName);

      return message.length() == 0 ? null : message.toString();
   }

   /**
    * Adds an error message to the provided buffer if the provided id starts
    * with a non-letter character.
    * @param id the id to check. Assumed not <code>null</code> or empty.
    * @param message the buffer to append the error message to.
    * Not <code>null</code>.
    * @param displayName how to name the value.
    * Used in an error message to indicate the associated control name.
    * Not <code>null</code>, not blank.
    */
   private void validateNameStart(final String id, final StringBuffer message,
         final String displayName)
   {
      if (!PSStringUtils.validateNameStart(id))
      {
         if (message.length() > 0)
         {
            message.append('\n');
         }
         message.append(PSMessages.getString(
               "PSControlValueTextIdValidator.error.illegalNameStart",
               displayName,
               id.charAt(0)));
      }
   }
}
