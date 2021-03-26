/*[ PSContentField.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.ui;

import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.client.PSLightWeightField;
import com.percussion.loader.objectstore.PSFieldProperty;

import java.util.Iterator;


/**
 * This is a container class for a field that is defined in a content type,
 * It contains a <code>PSItemField</code> and possibly its related
 * <code>PSLightWeightField</code>.
 */
public class PSContentField implements Comparable
{

   /**
    * Constructs an instance of this class from the given parameters.
    *
    * @param itemField The field item for a field that is defined in a content
    *    type, it may not be <code>null</code>.
    *
    * @param lwField The light weight field of the above field. It may be
    *    <code>null</code>.
    */
   public PSContentField(PSItemField itemField, PSLightWeightField lwField)
   {
      if (itemField == null)
         throw new IllegalArgumentException("itemField may not be null");

      m_itemField = itemField;
      m_lwField = lwField;
   }

   /**
    * Implements {@link java.util.Comparable#compareTo(Object)}.
    * 
    * @param obj The to be compared object. It must be an instance of
    *    <code>PSContentField</code>.
    * 
    * @return The result of comparing {@link #getFieldName()} of the
    *    current and the <code>obj</code>.
    */
   public int compareTo(Object obj)
   {
      if (!(obj instanceof PSContentField))
         throw new IllegalArgumentException(
            "obj is not an instance of PSContentField");

      PSContentField other = (PSContentField) obj;

      return getFieldName().compareTo(other.getFieldName());
   }
   
   /**
    * Get the (internal) name of the field.
    *
    * @return the field name, never <code>null</code>.
    */
   public String getFieldName()
   {
      return m_itemField.getName();
   }

   /**
    * Indicate whether the field contains a list of pre-defined values.
    *
    * @return <code>true</code> if it does have a list of pre-defined values;
    *    <code>false</code> otherwise.
    */
   public boolean hasChoices()
   {
      if (m_lwField != null)
      {
         Iterator choices = m_lwField.getDisplayChoices().getChoices();
         return (choices == null) ? false : choices.hasNext();
      }
      else
      {
         return false;
      }
   }

   /*
   * Get a list of pre-define values for this field.
   *
   * @return An iterator one or more <code>PSEntry</code>.
    *
    * @throws IllegalStateException if there is no choices.
   */
   public Iterator getChoices()
   {
      if (! hasChoices())
         throw new IllegalStateException(
            "This object does not have choice values");

      return m_lwField.getDisplayChoices().getChoices();
   }

   /**
    * Get the value type of the choices if there are choices for this field.
    *
    * @return one of the <code>PSFieldProperty.VALUE_TYPE_XXX</code>.
    *
    * @throws IllegalStateException if there is no choices.
    */
   public String getChoicesValueType()
   {
      if (! hasChoices())
         throw new IllegalStateException(
            "This object does not have choice values");

      if (m_itemField.getItemFieldMeta().getBackendDataType() == 
          PSItemFieldMeta.DATATYPE_NUMERIC)
      {
         return PSFieldProperty.VALUE_TYPE_NUMBER;
      }
      else
      {
         return PSFieldProperty.VALUE_TYPE_LITERAL;
      }
   }

   /**
    * Returns the field name as the string representation
    * of this object
    *
    * @return field name, never <code>null</code>.
    */
   public String toString()
   {
      return getFieldName();
   }

   /**
    * The item field object for a field that is defined in a content type.
    * It is initialized by the constructor, never <code>null</code> after that.
    */
   private PSItemField m_itemField;

   /**
    * The light weight field object of the above field. It may be
    * <code>null</code>.
    */
   private PSLightWeightField m_lwField;

}