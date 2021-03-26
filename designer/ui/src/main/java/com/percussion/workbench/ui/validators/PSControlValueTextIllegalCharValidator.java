/******************************************************************************
 *
 * [ PSControlValueTextIllegalCharValidator.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import java.util.HashSet;
import java.util.Set;

/**
 * Validation to check for illegal characters existing in a text
 * or combo control. The illegal characters are passed into the ctor as
 * a string.
 */
public class PSControlValueTextIllegalCharValidator
         implements
            IPSControlValueValidator
{

   /**
    * The ctor
    * @param chars a string containing any character that is
    * considered illegal. Cannot be <code>null</code> or empty.
    */
   PSControlValueTextIllegalCharValidator(String chars)
   {
      if(StringUtils.isBlank(chars))
         throw new IllegalArgumentException("chars cannot be null or empty."); //$NON-NLS-1$
      m_illegalChars = new HashSet<Character>();
      for(int i = 0; i < chars.length(); i++)
      {
         m_illegalChars.add(chars.charAt(i));
      }
   }
   
   /* @see com.percussion.workbench.ui.validators.IPSControlValueValidator#
    * validate(com.percussion.workbench.ui.util.PSControlInfo)
    */
   public String validate(PSControlInfo controlInfo)
   {
      if(controlInfo == null)
         throw new IllegalArgumentException("The control cannot be null.");  //$NON-NLS-1$
      Control control = controlInfo.getControl();
      if(!(control instanceof Text || control instanceof Combo))
         throw new IllegalArgumentException(
            "The control must be an instance of the Text or Combo control.");  //$NON-NLS-1$
      String value = control instanceof Combo 
         ? ((Combo)control).getText() 
         : ((Text)control).getText();
      boolean found = false;
      for(char c : m_illegalChars)
      {
         if(value.indexOf(c) != -1)
         {   
            found = true;
            break;
         }
      }
      if(found)
      {
         StringBuilder sb = new StringBuilder();
         for(char c : m_illegalChars)
         {
            sb.append(' ');
            if(c == ' ')
            {
              sb.append(PSMessages.getString(
                 "PSControlValueTextIllegalCharValidator.space.token"));   //$NON-NLS-1$
            }
            else
            {
               sb.append(c);
            }
         }
         Object[] args = new Object[]{controlInfo.getDisplayName(),
                  sb.toString()};
         return PSMessages.getString("common.illegalChar.message", args);       //$NON-NLS-1$
      }
      
      return null;
   }
   
   /**
    * Set of all illegal characters. Initialized in ctor,
    * never <code>null</code> after that.
    */
   private Set<Character> m_illegalChars;

}
