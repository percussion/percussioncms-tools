/******************************************************************************
 *
 * [ PSTemplateVariantPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;

public class PSTemplateVariantPageTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setName("name");
      template.setAssemblyUrl("url");

      final PSTemplateVariantPage page =
            new PSTemplateVariantPage(m_shell, SWT.NONE, new PSDummyEditor());
      
      
      // stylesheet
      template.setStyleSheetPath(null);
      page.loadControlValues(template);
      assertEquals("", page.m_stylesheetText.getText());
      
      final String STYLESHEET = "Stylesheet !";
      template.setStyleSheetPath(STYLESHEET);
      page.loadControlValues(template);
      assertEquals(STYLESHEET, page.m_stylesheetText.getText());
      
      // url
      final String URL = "Url !";
      template.setAssemblyUrl(URL);
      page.loadControlValues(template);
      assertEquals(URL, page.m_urlText.getText());
   }

   public void testUpdateTemplate()
   {
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setAssemblyUrl("url");
      template.setName("name");
      template.setOutputFormat(OutputFormat.Snippet);

      final PSTemplateVariantPage page =
            new PSTemplateVariantPage(m_shell, SWT.NONE, new PSDummyEditor());
      page.loadControlValues(template);
      
      // stylesheet
      page.m_stylesheetText.setText("");
      page.updateTemplate(template);
      assertNull(template.getStyleSheetPath());
      
      final String STYLESHEET = "Stylesheet !";
      page.m_stylesheetText.setText(STYLESHEET);
      page.updateTemplate(template);
      assertEquals(STYLESHEET, template.getStyleSheetPath());
      
      // url
      final String URL = "Url !";
      page.m_urlText.setText(URL);
      page.updateTemplate(template);
      assertEquals(URL, template.getAssemblyUrl());
   }
}
