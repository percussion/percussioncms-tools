/******************************************************************************
 *
 * [ PSVariantPropertiesPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * @author Andriy Palamarchuk
 */
public class PSVariantPropertiesPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      assertTrue(StringUtils.isNotBlank(createPage().getName()));
      assertTrue(StringUtils.isNotBlank(createPage().getTitle()));
   }

   private PSVariantPropertiesPage createPage()
   {
      return new PSVariantPropertiesPage();
   }
   
   public void testUpdateDesignerObject()
   {
      final PSVariantPropertiesPage page = createPage();
      final PSVariantWizard wizard = new PSVariantWizard();
      wizard.setContainer(new TestWizardContainer(page));
      page.setWizard(wizard);

      page.createControl(m_shell);
      try
      {
         page.updateDesignerObject(null, new Button(m_shell, SWT.NONE));
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
}
