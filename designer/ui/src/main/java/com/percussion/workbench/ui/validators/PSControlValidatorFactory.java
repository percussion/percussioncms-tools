/******************************************************************************
 *
 * [ PSControlValidatorFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Singleton class can be used to get an instance of a particular control validator.
 * The same validator instance is shared with all the callers.
 */
public class PSControlValidatorFactory
{

   private PSControlValidatorFactory(){}
   
   /**
    * Gets an instance of the singleton factory
    * @return never <code>null</code>.
    */
   public static PSControlValidatorFactory getInstance()
   {
      return ms_instance;
   }
   
   /**
    * @return an instance of a control required validator, 
    * never <code>null</code>.
    */
   public PSControlValueRequiredValidator getRequiredValidator()
   {
      return m_required;
   }
   
   /**
    * @return an instance of a no whitespace in Text control validator,
    * never <code>null</code>.
    */
   public PSControlValueTextWhitespaceValidator getNoWhitespaceValidator()
   {
      return m_whitespace;
   }
   
   /**
    * 
    * @param min the minimum string length of the control's value,
    * if set to -1 then there is no minimum length constraint.
    * @param max the maximum string length of the control's value,
    * if set to -1 then there is no maximum length constraint.
    * @return an instance of a text length validator, never<code>null</code>.
    */
   public PSControlValueTextLengthValidator getLengthValidator(int min, int max)
   {
      for(PSControlValueTextLengthValidator v : m_lengthValidators)
      {
         if(v.getMin() == min && v.getMax() == max)
         {
            return v;
         }
      }
      final PSControlValueTextLengthValidator  val =
            new PSControlValueTextLengthValidator(min, max);
      m_lengthValidators.add(val);
      return val;
   }
   
   /**
    * @return an instance of a numeric Text control validator,
    * never <code>null</code>.
    */
   public PSControlValueTextNumericValidator getNumericValidator()
   {
      return m_numeric;
   }
   
   /**
    * @param model the model passed in by the wizard for the object
    * type being validated.
    * @return a new instance of the duplicate name validator, this
    * is never cached by this factory and will always be a new
    * instance.
    */
   public PSControlValueDuplicateNameValidator getDuplicateNameValidator(
      IPSCmsModel model)
   {
      return new PSControlValueDuplicateNameValidator(model);
   }
   
   /**
    * @return an instance of a content type name validator,
    * never <code>null</code>.
    */
   public PSContentTypeNameValidator getContentTypeNameValidator()
   {
      return m_ctNameValidator;
   }
   
   /**
    * @return an instance of an id Text control validator,
    * never <code>null</code>.
    */
   public PSControlValueTextIdValidator getIdValidator()
   {
      return m_id;
   }

   /**
    * Get the ID validator for a given type.
    * @param type the type of the object that needs to be validated.
    * @return the validator, never <code>null</code>.
    */
   public PSControlValueTextIdValidator getIdValidatorForType(Enum type)
   {
      if (type.equals(PSObjectTypes.CONTENT_TYPE))
      {
         return getContentTypeNameValidator();
      }
      else
      {
         return getIdValidator();
      }
   }
   
   /**
    * Returned validator can be used by Text or Combo controls.
    * @param chars a string containing all characters that are considered
    * illegal. Cannot be <code>null</code> or empty.
    * @return an instance of the illegal character validator for the
    * characters passed in. Never <code>null</code>.
    */
   public PSControlValueTextIllegalCharValidator getIllegalCharValidator(
            String chars)
   {      
      if(StringUtils.isBlank(chars))
         throw new IllegalArgumentException("chars cannot be null or empty.");
      if(!m_illegalCharValidators.containsKey(sortKeyString(chars)))
      {
         m_illegalCharValidators.put(
            sortKeyString(chars),
            new PSControlValueTextIllegalCharValidator(chars));
      }
      return m_illegalCharValidators.get(chars);   
   }
   
   /**
    * Helper method that sorts the list of illegal chars to make
    * a unique key that does not care which order the original
    * char string was presented in. Also eliminates duplicate
    * chars.
    * @param key assumed not <code>null</code>.
    * @return the sorted key, never <code>null</code>.
    */
   private String sortKeyString(String key)
   {
      int count = key.length();
      List<Character> charList = new ArrayList<Character>(key.length());
      for(int i = 0; i < key.length(); i++)
      {
         if(!charList.contains(key.charAt(i)))
         {   
            charList.add(key.charAt(i));
            count++;
         }
      }
      Collections.sort(charList);
      StringBuilder sb = new StringBuilder(count);
      for(Character c : charList)
         sb.append(c);
      return sb.toString();
   }
   
   /**
    * The singleton instance of this factory. Initialized
    * in {@link #getInstance()}, never <code>null</code> after that.
    */
   private static final PSControlValidatorFactory ms_instance =
         new PSControlValidatorFactory();
   
   private final PSControlValueRequiredValidator m_required =
         new PSControlValueRequiredValidator();
   
   private final PSControlValueTextWhitespaceValidator m_whitespace =
         new PSControlValueTextWhitespaceValidator();
   
   private final PSControlValueTextNumericValidator m_numeric =
         new PSControlValueTextNumericValidator();
   
   private final PSContentTypeNameValidator m_ctNameValidator =
         new PSContentTypeNameValidator();
   
   private final PSControlValueTextIdValidator m_id =
         new PSControlValueTextIdValidator();
   
   private List<PSControlValueTextLengthValidator> m_lengthValidators = 
      new ArrayList<PSControlValueTextLengthValidator>();
   
   /**
    * Map that caches illegal character validators. The
    * key used is the string of chars passed in.
    */
   private Map<String, PSControlValueTextIllegalCharValidator> 
      m_illegalCharValidators = 
         new HashMap<String, PSControlValueTextIllegalCharValidator>();
}
