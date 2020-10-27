/******************************************************************************
 *
 * [ ListMemberConstraint.java ]
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Constraint for validating String-based component values.
 * @see ValidationConstraint
 */
public class ListMemberConstraint implements ValidationConstraint
{
   //create static resource bundle object
   static ResourceBundle sm_res = null;
   static
   {
      try
      {
          sm_res = ResourceBundle.getBundle(
                               "com.percussion.E2Designer.ValidationResources",
                               Locale.getDefault() );
      }
      catch(MissingResourceException mre)
      {
         System.out.println( mre );
      }
   }

                      
   /**
    * Default ctor for this type of constraint.
    *
    * @param existing A list of names against which to validate. The control&apos;s
    * text cannot appear in this list. The objects in the list are converted to
    * Strings using toString(). null entries are ignored.
    *
    * @param caseSensitive If <code>true</code>, the check is performed using
    * case sensitive comparison.
    *
    */
   public ListMemberConstraint( Collection existing, boolean caseSensitive )
   {
      if ( null == existing)
      {
         existing = new ArrayList();
      }
      m_caseSensitive = caseSensitive;
      m_existingElems = existing;
   }

   /**
    * Default ctor for this type of constraint.
    *
    * @param existing A list of names against which to validate. The control&apos;s
    * text cannot appear in this list. The objects in the list are converted to
    * Strings using toString(). null entries are ignored.
    *
    * @param errMsg the default error msg for this control/component may be 
    *        <code>null</code>
    * case sensitive comparison.
    *
    */
   public ListMemberConstraint( Collection existing, String errMsg )
   {
      if ( null == existing)
      {
         existing = new ArrayList();
      }
      m_caseSensitive = false;
      m_existingElems = existing;
      m_errorMsg      = errMsg;
   }

   /**
    * A convenience method. Equivalent to <code>this( existing, false )</code>.
    * See {@link #ListMemberConstraint( Collection, boolean) this} for a
    * description.
    */
   public ListMemberConstraint( Collection existing )
   {
      this( existing, false );
   }


/* ################ ValidationConstraint interface impl ################## */

   /**
    * @return The error msg to display when validation fails.
    */
   public String getErrorText()
   {
      StringBuffer buf = new StringBuffer( 400 );

      // Error checking should never happen if the collection of existing
      // names is null or empty, but take care anyway.
      if (!(null == m_existingElems  || m_existingElems.size() < 1))
      {
         Iterator iter = m_existingElems.iterator();
         boolean first = true;
         while ( iter.hasNext())
         {
            if ( !first )
               buf.append( ", " );
            buf.append( iter.next().toString());
            first = false;
         }
      }

      String [] args = new String[]
      {
         buf.toString()
      };

      if ( null == m_errorMsg )
      {
         String pattern = null;

         try
         {
            if ( null != sm_res )
               pattern = sm_res.getString( "uniqueListConstraintError" );
         }
         catch ( MissingResourceException ex )
         { /* We should throw this, but I don't want to change the
               interface at this point. */ }
         if ( null == pattern )
            pattern = "missing resources for ListMemberConstraint";
         // todo
         pattern = "Entry not unique. Matched one of the following: {0}";
         // If the originator has overridden the error message, don't over-write
         // it again, with the above static message. 
         if ( m_errorMsg == null )
            m_errorMsg = MessageFormat.format( pattern, args );
      }
      else
      {
         m_errorMsg = MessageFormat.format( m_errorMsg, args );
      }
      return m_errorMsg;
   }

   /**
    * Validates the supplied component by checking its text against the list
    * of names that were supplied with the constructor. If any matches are
    * found, the validation fails and an exception is thrown.
    *
    * @param suspect A JTextComponent or JComboBox to check.
    *
    * @throws ValidationException if the text from the component is found
    * on the disallowed list.
    */
   public void checkComponent( Object suspect )
      throws ValidationException
   {

      // If the collection of elements is null or empty, just return
      if (null == m_existingElems  || m_existingElems.size() < 1)
      {
         return;
      }

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
      {
         if ( ((JComboBox)suspect).getSelectedIndex() < 0 )
            text = "";
         else
            text = new String(((JComboBox)suspect).getSelectedItem().toString());
         if ( text.trim().length() < 1 )
         {
            // By setting this error message, it will override the static error 
            // message that ValidationException creates to the user.
            m_errorMsg = "Combo box does not have proper selection";
            throw new ValidationException();
         }
      }
      else   // this should never happen... so throw ValidationException!
         throw new IllegalArgumentException(
                             "Component null or not text field or combo box" );

      // begin validation
      Iterator iter = m_existingElems.iterator();
      while ( iter.hasNext())
      {
         boolean found = false;
         String existing = iter.next().toString();
         if ( m_caseSensitive )
            found = existing.equals( text );
         else
            found = existing.equalsIgnoreCase( text );
         if ( found )
            throw new ValidationException();
      }
   }

   /**
    * A list of 1 or more elements that contain the names that the component
    * attached to this constraint can't use.
    */
   private Collection m_existingElems;

   /**
    * Flag indicating whether the comparisons between the value and the list
    * are case sensitive or not. If <code>true</code>, then the comparisons
    * are case sensitive.
    */
   private boolean m_caseSensitive;

   /**
    * null until the first time {@link #getErrorText() getErrorText} is
    * called. Then it caches the dynamically created message for subsequent
    * calls.
    */
   private String m_errorMsg = null;
}


