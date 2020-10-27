/******************************************************************************
 *
 * [ PSControlValueFileExistsValidatorTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;

public class PSControlValueFileExistsValidatorTest extends PSUiTestBase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      m_text = new Text(m_shell, SWT.NONE);
      m_controlInfo = new PSControlInfo(m_text, "name", null, null);
   }

   public void testValidate() throws IOException
   {
      final File file = File.createTempFile("pref", "tmp");
      assertTrue(file.exists());
      try
      {
         assertNull(createValidator().validate(m_controlInfo));
      }
      finally
      {
         file.delete();
      }
   }

   public void testVaildate_null()
   {
      try
      {
         createValidator().validate(null);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
   
   public void testValidate_wrongControlType()
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

   public void testValidate_blankValue()
   {
      m_text.setText("");
      assertNull(createValidator().validate(m_controlInfo));

      m_text.setText("   ");
      assertNull(createValidator().validate(m_controlInfo));
   }
   
   public void testValidate_directory()
   {
      assertValidatorReturnsError(".");
   }

   public void testValidate_nonExistingFile()
   {
      assertValidatorReturnsError("Non Existing File");
   }
   
   /**
    * Raises an assertion if validator returns an error on the provided file name.
    */
   private void assertValidatorReturnsError(final String fileName)
   {
      m_text.setText(new File(fileName).getAbsolutePath());
      assertNotBlank(createValidator().validate(m_controlInfo));
   }
   
   

   /**
    * Creates new validator.
    */
   private PSControlValueFileExistsValidator createValidator()
   {
      return new PSControlValueFileExistsValidator();
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
