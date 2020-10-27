/*[ StringLengthConstraint.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Constraint for validating String-based component values.
 * @see ValidationConstraint
 */
public class StringLengthConstraint implements ValidationConstraint
{

/** A string of invalid characters that is typically not used in the normal
  * identifier convention.
  *
*/

  public static final String NO_SPECIAL_CHAR = " ~!@#$%^&*()+`-=[]{}|;':,.<>/?";

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


/** Basic validation for checking whitespace (or empty) or designate using constants.
  *
*/

   public StringLengthConstraint( int maxLen )
   {
      if ( maxLen < 1 )
         throw new IllegalArgumentException( "Maximum length must be greater than 0" );
      m_maxLen = maxLen;
   }

   // implementing interface ValidationConstraint
   public String getErrorText()
   {
      return "String exceeded max length (" + m_maxLen + ")";
   }

   // implementing interface ValidationConstraint
   public void checkComponent(Object suspect) throws ValidationException
   {
      String text = null;
      // initializing m_data
      if (suspect instanceof JTextComponent)
      {
         JTextComponent tf = (JTextComponent)suspect;
         if ( null != tf.getDocument())
            text = tf.getText();
         else
            text = "";
      }
      else if (suspect instanceof JComboBox)
         text = new String(((JComboBox)suspect).getSelectedItem().toString());
      else   // this should never happen... so throw ValidationException!
         throw new IllegalArgumentException( "Component null or not text field or combo box" );

      // begin validation
      if ( text.length() > m_maxLen )
         throw new ValidationException();
   }

   private int m_maxLen;
}


