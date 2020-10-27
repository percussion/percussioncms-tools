/******************************************************************************
 *
 * [ PSControlValueTextIdValidatorTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.utils.string.PSStringUtils;
import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

/**
 * @author Andriy Palamarchuk
 */
public class PSControlValueTextIdValidatorTest extends PSUiTestBase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      m_text = new Text(m_shell, SWT.NONE);
      m_controlInfo = new PSControlInfo(m_text, "name", null, null);
   }

   public void testVaildateNull()
   {
      try
      {
         createValidator().validate(null);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   public void testValidateWrongControlType()
   {
      m_controlInfo = new PSControlInfo(
         new Button(m_shell, SWT.NONE), "name", null, null);
      try
      {
         createValidator().validate(m_controlInfo);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   public void testValidateBlankValue()
   {
      m_text.setText("");
      assertNull(createValidator().validate(m_controlInfo));

      m_text.setText("   ");
      assertNull(createValidator().validate(m_controlInfo));
   }

   public void testValidate()
   {
      // normal
      m_text.setText("abc");
      assertNull(createValidator().validate(m_controlInfo));

      // contains number, underscore
      m_text.setText("ab2c_");
      assertNull(createValidator().validate(m_controlInfo));

      // contains number, underscore, period
      m_text.setText("ab_2c.def");
      assertNull(createValidator().validate(m_controlInfo));

      m_text.setText("ab_,$()_-'.def");
      assertNull(createValidator().validate(m_controlInfo));

      // invalid characters
      for (final char ch : "*+\"".toCharArray())
      {
         assertValidatorReturnsError("abc" + ch);
      }

      // invalid characters
      for (final char ch : PSStringUtils.INVALID_NAME_CHARS
            .toCharArray())
      {
         assertValidatorReturnsError("abc" + ch);
      }


      // starts with a number
      assertValidatorReturnsError("2abc");

      // starts with an underscore
      assertValidatorReturnsError("_abc");
   }

   public void testContentTypeValidate()
   {
      // normal
      m_text.setText("abc");
      assertNull(createCtValidator().validate(m_controlInfo));

      // contains number, underscore
      m_text.setText("ab2c_");
      assertNull(createCtValidator().validate(m_controlInfo));

      // invalid characters
      for (final char ch : "*+\"".toCharArray())
      {
         assertCtValidatorReturnsError("abc" + ch);
      }

      // invalid characters
      for (final char ch : PSStringUtils.INVALID_NAME_CHARS
            .toCharArray())
      {
         assertCtValidatorReturnsError("abc" + ch);
      }


      // starts with a number
      assertCtValidatorReturnsError("2abc");

      // starts with an underscore
      assertCtValidatorReturnsError("_abc");
   }

   /**
    * Tests only parameters validation.
    * The main logic is tested in {@link #testValidate()}.
    */
   public void testValidateId()
   {
      m_controlInfo = new PSControlInfo(
            new Button(m_shell, SWT.NONE), "name", null, null);
      
      // correct params
      assertNull(createValidator().validateId("abc", "name"));
      
      // wrong params
      assertValidateIdFails(null, "name");
      assertValidateIdFails("", "name");
      assertValidateIdFails(" \t\n", "name");
      assertValidateIdFails("id", null);
      assertValidateIdFails("id", "");
      assertValidateIdFails("id", " \t\n");
   }

   /**
    * Makes sure that <code>validateId</code> method fails with
    * {@link IllegalArgumentException} for the provided parameters.
    * @param id id to pass to the <code>validateId</code> method.
    * @param name name to pass to the <code>validateId</code> method.
    */
  
   private void assertValidateIdFails(String id, String name)
   {
      try
      {
         createValidator().validateId(id, name);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   /**
    * Raises an assertion if validator returns an error on the provided string.
    * @param value the string to test against the validator.
    * Can be null.
    */
   private void assertValidatorReturnsError(final String value)
   {
      m_text.setText(value);
      assertNotBlank(createValidator().validate(m_controlInfo));
   }

   /**
    * Raises an assertion if the Content Type name validator returns 
    * an error on the provided string.
    * @param value the string to test against the validator.
    * Can be null.
    */
   private void assertCtValidatorReturnsError(final String value)
   {
      m_text.setText(value);
      assertNotBlank(createCtValidator().validate(m_controlInfo));
   }

   /**
    * Creates a validator.
    */
   private PSControlValueTextIdValidator createValidator()
   {
      return new PSControlValueTextIdValidator();
   }

   /**
    * Creates a Content Type validator.
    */
   private PSControlValueTextIdValidator createCtValidator()
   {
      return new PSContentTypeNameValidator();
   }


   /**
    * Text control to validate.
    */
   private Text m_text;
   
   /**
    * Control info passed to validator, pointing to {@link #m_text}.
    */
   private PSControlInfo m_controlInfo;
}
