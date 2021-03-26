/******************************************************************************
 *
 * [ PSOverrideValidator.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSField;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Base class for validators that check for allowed field overrides
 */
public abstract class PSOverrideValidator
{
   /**
    * Factory method to obtain a validator instance
    * 
    * @param field The field to validate, may not be <code>null</code>, must be
    * of type system or shared.
    * 
    * @return the validator, never <code>null</code>.
    */
   public static PSOverrideValidator getValidator(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");
      
      PSOverrideValidator validator;
      if (field.getType() == PSField.TYPE_SHARED)
      {
         validator = new PSSharedOverrideValidator(field);
      }
      else if (field.getType() == PSField.TYPE_SYSTEM)
      {
         validator = new PSSystemOverrideValidator(field);
      }
      else
      {
         throw new IllegalArgumentException("Invalid field type " + 
            field.getType() + " for field: " + field.getSubmitName());
      }
      
      return validator;
   }
   
   /**
    * Construct the validator, not for public use, instead call factory method
    * {@link #getValidator(PSField)}
    * 
    * @param field The field to validate, may not be <code>null</code>.
    */
   protected PSOverrideValidator(PSField field)
   {
      if (field == null)
         throw new IllegalArgumentException("field may not be null");
      m_field = field;
   }
   
   /**
    * Validates if validation rules may be added to the field supplied during 
    * construction, displaying any necessary messages to the user.
    * 
    * @return <code>true</code> if validation succeeds, <code>false</code> if
    * not.  Note that messages may be displayed even if <code>true</code> is
    * returned. 
    * 
    * @throws Exception If there are any unexpected errors.
    */
   public abstract boolean validate() throws Exception;
   
   /**
    * Get the field supplied during ctor
    * 
    * @return The field, never <code>null</code>.
    */
   public PSField getField()
   {
      return m_field;
   }
   

   /**
    * Convenience method that calls 
    * {@link #isValid(PSField, boolean) isValid(sourceField, false}.
    */
   protected boolean isValid(PSField sourceField)
   {
      return isValid(sourceField, false);
   }

   /**
    * Checks for allowed validation rules overrides, displays any errors or
    * warnings to the end user.
    * 
    * @param sourceField The source field to check to see if validation rules
    * can be overriden, may not be <code>null</code>.
    * 
    * @param multiple <code>true</code> if there are multiple possible 
    * source fields that match the field supplied during construction, 
    * <code>false</code> otherwise.  Supply <code>true</code> for the case
    * where mulitple shared fields match by name, in which case a less 
    * definitive warning is displayed. 
    * 
    * @return <code>true</code> if the validation rules of the source field
    * may be overriden, <code>false</code> if not.
    */
   protected boolean isValid(PSField sourceField, boolean multiple)
   {
      if (sourceField.hasValidationRules())
      {
         // cannot override, display error
         String key = multiple ? "possibleInvalidFieldValidationOverride" : 
            "invalidFieldValidationOverride";
         String msg = PSMessages.getString(
            "PSOverrideValidator.error.message." + key, new Object[] {
            sourceField.getType(), sourceField.getSubmitName()});
         
         displayError(msg);
         
         return false;
      }
      
      return true;
   }      
   
   /**
    * Displays the supplied invalid override message to the end user.
    * 
    * @param msg The message, assumed not <code>null</code> or empty.
    */
   private void displayError(String msg)
   {
      String title = PSMessages.getString(
         "PSOverrideValidator.error.title.invalidFieldValidationOverride");
         
      MessageDialog.openError(PSUiUtils.getShell(), title, msg);
   }
   
   /**
    * The field supplied during construction, never <code>null</code> or 
    * modified after that.
    */
   private PSField m_field;
}