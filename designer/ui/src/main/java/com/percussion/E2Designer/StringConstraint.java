/******************************************************************************
 *
 * [ StringConstraint.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Constraint for validating String-based component values.
  *
  * @see ValidationConstraint
*/

public class StringConstraint implements ValidationConstraint
{
/** Basic validation for checking whitespace (or empty) or designate using constants.
  *
*/

  public StringConstraint()
  {
     m_invalidChar = null;
  }

/** Constructs a String Constraint object and puts all the specific characters
  * to be validated in a String.
  *
  * @param String
*/

  public StringConstraint(String s)
  {
     m_invalidChar = s;
  }

  /** Constructs a String Constraint object and puts all the specific characters
   * to be validated in a String.
   *
   * @param String the invalid char
   * @param fieldName the field name, may be <code>null</code>
 */

   public StringConstraint(String s, String fieldName)
   {
      m_invalidChar = s;
      m_fieldName   = fieldName;
   }
  
  
  // implementing interface ValidationConstraint
  public String getErrorText()
  {
     if (m_errorMsg[0] == null)
     {
        String fmtMsg = MessageFormat.format(sm_res.getString("emptyField"),
               m_errorMsg);
        if ( m_fieldName != null ) 
           fmtMsg = m_fieldName + fmtMsg;
        return fmtMsg;
     }
     else
        return MessageFormat.format(sm_res.getString("invalidChar") + " {0} " + sm_res.getString("doubleQuotes"), m_errorMsg);
  }

   // implementing interface ValidationConstraint
   public void checkComponent(Object suspect) throws ValidationException
   {
      // initializing m_data
      if (suspect instanceof JTextComponent)
      {
         JTextComponent c = (JTextComponent) suspect;
         if ( null != c.getDocument())
            m_data = c.getText();
         else
            m_data = "";
      }
      else if (suspect instanceof JComboBox)
      {
         Object o = ((JComboBox)suspect).getSelectedItem();
         if ( null == o )
            m_data = "";
         else
            m_data = o.toString();
      }
      else   // this should never happen... so throw ValidationException!
         throw new IllegalArgumentException( "Component null or not text field or combo box" );

      if ( null == m_data )
         m_data = "";
      // begin validation
      if (m_invalidChar != null)
      {
         for (int j = 0; j < m_data.length(); j++)
            for (int i = 0; i < m_invalidChar.length(); i++)
               if (m_data.charAt(j) == m_invalidChar.charAt(i))
               {
                  m_errorMsg[0] = new String(String.valueOf(m_data.charAt(j)));
                  throw new ValidationException();
               }
      }

      // if component is empty

      if ( m_data.trim().length() == 0 )  // validating empty string and "whitespace" string
      {
         m_errorMsg[0] = null;
         throw new ValidationException();
      }
   }

   private String   m_data;
   private String   m_invalidChar;
   private Object[] m_errorMsg = {null};
   
   /**
    * A place holder to prepend the error message. Useful for emptyField error 
    * msg users would never know which of the many empty fields the error is 
    * about.
    */
   private String   m_fieldName = null;

/** A string of invalid characters that is typically not used in the normal
  * identifier convention.
  */
  public static final String NO_SPECIAL_CHAR = " ~!@#$%^&*()+`-=[]{}|;':,.<>/?";
  /** A string of characters that is not accepted as a part of a class name. */
  public static final String CLASS_NAME_CHAR_ONLY = " ~!@#%^&*()+`-=[]{}|;':,<>/?";

   //create static resource bundle object
   static ResourceBundle sm_res = null;
   static
   {
      try
      {
          sm_res = ResourceBundle.getBundle( "com.percussion.E2Designer.ValidationResources",
                                         Locale.getDefault() );
      }catch(MissingResourceException mre)
      {
          System.out.println( mre );
      }
   }
}

 
