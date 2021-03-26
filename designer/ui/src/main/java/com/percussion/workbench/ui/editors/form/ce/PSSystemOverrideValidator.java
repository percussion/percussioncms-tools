/******************************************************************************
 *
 * [ PSSystemOverrideValidator.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;

/**
 * Validator for system fields, see base class.
 */
public class PSSystemOverrideValidator extends PSOverrideValidator
{
   /**
    * See base class for details.
    * 
    * @param field The field, must be of type {@link PSField#TYPE_SYSTEM}.
    */
   protected PSSystemOverrideValidator(PSField field)
   {
      super(field);

      if (field.getType() != PSField.TYPE_SYSTEM)
         throw new IllegalArgumentException("invalid type");
   }
   
   @Override      
   public boolean validate() throws Exception
   {
      PSField sourceField = null;
      PSFieldSet set = PSContentEditorDefinition.getSystemDef().getFieldSet();
      PSField field = getField();
      sourceField = set.getFieldByName(getField().getSubmitName());
      if (sourceField == null)
      {
         System.out.println("Failed to locate source of field: " + 
            field.getSubmitName());
         return false;
      }
      
      return isValid(sourceField);
   }
}