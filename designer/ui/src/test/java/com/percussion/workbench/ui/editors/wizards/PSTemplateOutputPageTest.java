/******************************************************************************
 *
 * [ PSTemplateOutputPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

public class PSTemplateOutputPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      assertTrue(StringUtils.isNotBlank(createPage().getName()));
      assertTrue(StringUtils.isNotBlank(createPage().getTitle()));
   }
   
   /**
    * Convenience method to create new page.
    */
   private PSTemplateOutputPage createPage()
   {
      final PSTemplateOutputPage page = new PSTemplateOutputPage();
      page.createControl(m_shell);
      return page;
   }
   
   public void testUpdateDesignerObject()
   {
      final boolean skip[] = new boolean[] {false};
      
      final PSTemplateOutputPage page = new PSTemplateOutputPage()
      {
         @Override
         boolean skipOutputFormatPage()
         {
            return skip[0];
         }
      };
      page.createControl(m_shell);

      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      assertEquals(TemplateType.Shared, template.getTemplateType());

      // unknown control
      try
      {
         page.updateDesignerObject(template, new Button(m_shell, SWT.PUSH));
         fail();
      }
      catch (IllegalArgumentException success) {}
      
      // unknown control when page must be skipped
      skip[0] = true;
      page.updateDesignerObject(template, new Button(m_shell, SWT.PUSH));
   }
}
