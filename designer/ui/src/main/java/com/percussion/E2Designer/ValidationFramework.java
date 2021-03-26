/*[ ValidationFramework.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** The generic "validation" framework for checking specified components within
  * a container.  This class is the interface between actual components that needs
  * to be checked and the constraints to be checked against specified by the
  * programmer.
  * <P>
  * ValidationException is thrown to signify an invalid value within a component.
  * A JOptionPane is used to quickly pop up a warning message directing the
  * user to the invalid component.  Then the focus is given to invalid component
  * for user's convenience.
  *
  * @see ValidationException
  * @see ValidationConstraint
*/


public class ValidationFramework
{

/** Constructs a basic framework without component-constraint pairs
  *
*/
  public ValidationFramework()
  {
     m_componentList = null;
     m_constraintList = null;
  }

/** Constructs a basic framework with specified component-constraint pairs
  * If inconsistent pairing of component(object) and constraints are passed in,
  * IllegalArgumentException is throw if the number of components does not match
  * the number of constraints.
  *
  * @see ValidationConstraint
  * @see IllegalArgumentException
*/

  public ValidationFramework(Object[] components, ValidationConstraint[] constraints)
                                         throws IllegalArgumentException
  {
     m_componentList = components;
     m_constraintList = constraints;

     if (m_componentList.length != m_constraintList.length)
        throw new IllegalArgumentException();
  }

/** Reinitializes the framework.
  *
  * @param
  *       components an array of Objects
  *       constraints an array of ValidationConstraint
  * @see ValidationConstraint
*/

   public void setFramework(Object[] components, ValidationConstraint[] constraints)
   {
       m_componentList = components;
       m_constraintList = constraints;
   }

   /**
    * Just like the otherone, except the passed in parent will be used as the parent
    * of the validation error message dialog.
    *
    * @param parent Will be used as the parent of the dialog that displays the
    * validation error.
   **/
   public void setFramework( Window parent, Object[] components, ValidationConstraint[] constraints)
   {
      m_parentWindow = parent;
      setFramework( components, constraints );
   }

   /** Loops through all the components that needs validation and checks them against
    * their appropriate constraints.  Whenever an error occurs, a ValidationException
    * is thrown.  The exception is caught, then a warning dialog is displayed and
    * the component with the incorrect value is highlighted and focused.
    *
    * @see ValidationConstraint
    */
   public boolean checkValidity()
   {
       if ( null == m_componentList || null == m_constraintList )
          return true;
       try
       {
           for (m_counter = 0; m_counter < m_componentList.length; m_counter++)
           {
               m_constraintList[m_counter].checkComponent(m_componentList[m_counter]);
           }
       }
       catch (ValidationException e)
       {
           // if exception occurs, warning dialog pops up
           // If parent wasn't supplied, try to use mainframe
           PSDlgUtil.showErrorDialog(
                 m_constraintList[m_counter].getErrorText(),
                 sm_res.getString("error"));

           if (m_componentList[m_counter] instanceof JTextComponent)
           {
               ((JTextComponent)m_componentList[m_counter]).selectAll();
               ((Component)m_componentList[m_counter]).requestFocus();

               // if the error is at the password field, clear it.
               if (m_componentList[m_counter] instanceof JPasswordField)
                  ((JPasswordField)m_componentList[m_counter]).setText( null );
           }
           else if (m_componentList[m_counter] instanceof JComboBox)
           {
               Component editor = ((JComboBox)
                  m_componentList[m_counter]).getEditor().getEditorComponent();

               if ( editor instanceof JTextComponent )
                  ((JTextComponent) editor).selectAll();

               editor.requestFocus();
           }
           return false; // meaning that validation failed; method cannot go on.
       }
       return true; // meaning that validation passed; method will go on.
   }

   private Object[] m_componentList;
   private ValidationConstraint[] m_constraintList;
   private int m_counter;

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
         mre.printStackTrace();
      }
   }
   private Window m_parentWindow = null;
}


