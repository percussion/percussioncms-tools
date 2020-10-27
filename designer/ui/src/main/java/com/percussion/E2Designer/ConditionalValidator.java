/*[ ConditionalValidator.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;

/**
 * An object that applies a real validation object based on a button state.
 * If the button supplied in the ctor is selected, the supplied validator
 * will be applied, otherwise, the component passes validation.
 * <p>
 * This is useful for controls that are grayed. For example, if you have a
 * group of radio buttons that have associated text boxes, you only care
 * about the selected one. But by using this class, you can set up the
 * constraints once and not have to modify them based on the selected radio
 * button.
 */
public class ConditionalValidator implements ValidationConstraint
{
   /**
    * The standard constructor.
    *
    * @param button The state of the button at the time of validation
    * determines whether validation is performed.
    *
    * @param delegate The constraint to use if the button is selected.
    */
   public ConditionalValidator( AbstractButton button, ValidationConstraint
      delegate )
   {
      if ( null == button )
         throw new IllegalArgumentException( "button can't be null" );
      if ( null == delegate )
         throw new IllegalArgumentException( "delegate validator can't be null" );

      m_button = button;
      m_delegate = delegate;
   }


   /**
    * When a validation constraint is thrown, call this method to get a
    * textual representation of the failure.
    *
    * @return An error message describing the validation failure in generic
    * terms.
    */
   public String getErrorText()
   {
      return m_delegate.getErrorText();
   }

   /**
    * If the button set in the ctor is selected, the delegate validator is
    * activated. Otherwise, the component passes.
    *
    * @param suspect The component that is being validated.
    *
    * @throws ValidationException If the suspect does not pass the validation
    * test of the delegate validator.
    */
   public void checkComponent( Object suspect )
      throws ValidationException
   {
      if ( m_button.isSelected())
         m_delegate.checkComponent( suspect );
   }


   /**
    * The state of this button when <code>checkComponent</code> is called
    * determines if the validation of <code>m_delegate</code> is performed.
    */
   private AbstractButton m_button;
   /**
    * The real validation constraint. Activated if <code>m_button</code> is
    * selected when validation begins.
    */
   private ValidationConstraint m_delegate;
}
