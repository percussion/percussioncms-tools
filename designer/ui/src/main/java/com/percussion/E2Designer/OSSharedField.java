/******************************************************************************
 *
 * [ OSSharedField.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;

/**
 * The class to represent the shared field which holds the information of the
 * group to which this field belongs to.
 */
public class OSSharedField extends OSField
{

   /**
    * The constructor for creating the new shared field object with it's group
    * information and the type of field set it belongs to.
    *
    * @param group the shared field group, may not be <code>null</code>
    * @param type the type of field set, must be one of
    * <code>TYPE_SIMPLE_CHILD</code>,
    * <code>TYPE_MULTI_PROPERTY_SIMPLE_CHILD</code> and
    * <code>TYPE_COMPLEX_CHILD</code> values.
    * @param field a valid field, must not be <code>null</code>
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public OSSharedField(PSSharedFieldGroup group, int type, PSField field)
   {
      super(field);

      if(group == null)
         throw new IllegalArgumentException("group can not be null");

      if(! (type == PSFieldSet.TYPE_COMPLEX_CHILD ||
         type == PSFieldSet.TYPE_SIMPLE_CHILD ||
         type == PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD) )
      {
         throw new IllegalArgumentException(
            "type is invalid, it must be one of following values" +
            PSFieldSet.TYPE_SIMPLE_CHILD + "," +
            PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD + "," +
            PSFieldSet.TYPE_COMPLEX_CHILD);
      }

      m_group = group;

      m_groupType = type;

   }

   /**
    * Gets the shared group to which this field belongs to.
    *
    * @return the group, never <code>null</code>
    */
   public PSSharedFieldGroup getGroup()
   {
      return m_group;
   }

   /**
    * Gets the type of shared group, basically the type of field set that
    * this field belongs to.
    *
    * @return the type, will be one of <code>TYPE_SIMPLE_CHILD</code>,
    * <code>TYPE_MULTI_PROPERTY_SIMPLE_CHILD</code> and
    * <code>TYPE_COMPLEX_CHILD</code> values.
    */
   public int getGroupType()
   {
      return m_groupType;
   }
   /**
    * Get the tyoe of field set that this field belongs to.
    * For OSSharedField field set type is the same as group type
    * @return type, will be one of <code>TYPE_SIMPLE_CHILD</code>,
    * <code>TYPE_MULTI_PROPERTY_SIMPLE_CHILD</code> and
    * <code>TYPE_COMPLEX_CHILD</code> values.
    */
   public int getFieldSetType()
   {
      return m_groupType;
   }

   /**
    * @return the group name appended with "/" and field name in case of
    * simple child field, the field name in case of complex child.
    */
   public String toString()
   {
      String superSubmitName = super.getSubmitName();

      if(m_groupType == PSFieldSet.TYPE_COMPLEX_CHILD)
         return superSubmitName;
      else {
         if (superSubmitName.indexOf("/") < 0)
            return m_group.getName() + "/" + superSubmitName;
         else
            return superSubmitName;
      }
   }

   /**
    * The shared group to which the fieldset of this field belongs
    * to, initialized in constructor and never changed after that.
    */
   private PSSharedFieldGroup m_group;

   /**
    * The type of the field set that this field belongs to, initialized in
    * constructor and never changed after that. The group type is field set type.
    */
   private int m_groupType;
}
