/*[ PasswordConstraint.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Handles all password confirmation validation.  Receives a confirmation String
 * to match the password passed in as a Component.
 *
 */
public class PasswordConstraint implements ValidationConstraint
{
//
// CONSTRUCTORS
//

   /**
    *Constructs a basic PasswordConstraint object that handles Strings.
    */

   public PasswordConstraint(String confirmString)
   {
      m_confirm = confirmString;
   }

   public PasswordConstraint(String confirmString, boolean requirePassword)
   {
      m_confirm = confirmString;
      m_requirePassword = requirePassword;
   }

   /**
    * Creates a constraint using the supplied component as the source for the
    * confirmation password.
    *
    * @param confirm A valid control.
    *
    * @param requirePassword If <code>true</code>, validation will fail if the
    * password field is empty.
    */
   public PasswordConstraint( JPasswordField confirm, boolean requirePassword )
   {
      m_confirmControl = confirm;
      m_requirePassword = requirePassword;
   }

//
// PUBLIC METHODS
//

   /**
    * @return true = password is required; false = password is not required.
    */
   public boolean isPasswordRequired()
   {
      return m_requirePassword;
   }

   /** @param requirePassword Sets the validationFramework to check for empty
    * password fields.
    */
   public void setPasswordRequired(boolean requirePassword)
   {
      m_requirePassword = requirePassword;
   }

   /** Returns the error text.
    */

   // implementing definition from interface ValidationConstraint
   public String getErrorText()
   {
      if (m_errorFlag == 0)
         return sm_res.getString("passwordnotmatch");
      else  // m_errorFlag == 1
         return sm_res.getString("requirepassword");
   }

   /** It simply checks if password field matches the confirmation field.
    */

   // implementing definition from interface ValidationConstraint
   public void checkComponent(Object suspect) throws ValidationException
   {
      if (suspect instanceof JPasswordField)
         m_password = new String(((JPasswordField)suspect).getPassword());

      if ( null != m_confirmControl )
      {
         m_confirm = new String( m_confirmControl.getPassword());
      }

      ValidationException e = null;
      if (!m_password.equals(m_confirm))
      {
         m_errorFlag = 0;
         e = new ValidationException();
      }

      if (m_password.equals("") && m_confirm.equals("") && m_requirePassword == true)
      {
         m_errorFlag = 1;
         e = new ValidationException();
      }
      if ( null != e )
      {
         // clear the confirm pw field
         if ( null != m_confirmControl )
            m_confirmControl.setText( null );
         throw e;
      }
   }      // End method checkComponent()

//
// MEMBER VARIABLES
//

   private String  m_password = null;
   private String  m_confirm = null;
   private boolean m_requirePassword = false;
   private int     m_errorFlag = 0;

   /**
    * Either m_confirm or this control will be non-null, but not both. If this
    * control is valid, the confirm password is set dynamically each time
    * the validation is activated.
    */
   private JPasswordField m_confirmControl = null;

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


