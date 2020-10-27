/******************************************************************************
 *
 * [ PSSharedOverrideValidator.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Validator for shared fields, see base class.
 */
public class PSSharedOverrideValidator extends PSOverrideValidator
{
   /**
    * See base class for details.
    * 
    * @param field The field, must be of type {@link PSField#TYPE_SHARED}.
    */
   protected PSSharedOverrideValidator(PSField field)
   {
      super(field);
      
      if (field.getType() != PSField.TYPE_SHARED)
         throw new IllegalArgumentException("invalid type");
   }

   @Override
   public boolean validate() throws Exception
   {
      // can't handle duplicate names in different groups, so save multiple
      List<PSField> sharedFields = new ArrayList<PSField>();
      PSContentEditorSharedDef sharedDef = 
         PSContentEditorDefinition.getSharedDef();
      if(sharedDef == null)
         return false;
      Iterator iter = sharedDef.getFieldGroups();
      String fieldName = getField().getSubmitName();
      while(iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)iter.next();
         PSFieldSet set = group.getFieldSet();
         PSField sharedField = set.getFieldByName(fieldName);
         if (sharedField != null)
         {
            sharedFields.add(sharedField);
         }
      }
      
      if (sharedFields.isEmpty())
      {
         System.out.println("Failed to locate source of field: " + 
            fieldName);
         return false;
      }
      
      if (sharedFields.size() == 1)
         return isValid(sharedFields.get(0));

      for (PSField field : sharedFields)
      {
         if (!isValid(field, true))
            break;
      }
      
      return true;
   }  
}