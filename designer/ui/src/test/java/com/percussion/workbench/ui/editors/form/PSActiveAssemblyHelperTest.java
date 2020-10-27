/******************************************************************************
 *
 * [ PSActiveAssemblyHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.AAType;
import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.editors.form.PSActiveAssemblyHelper.AATypeChoice;

public class PSActiveAssemblyHelperTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSActiveAssemblyHelper helper = new PSActiveAssemblyHelper();
      helper.initRadio(m_shell);
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      
      assertEquals(AATypeChoice.values().length, AAType.values().length);
      template.setActiveAssemblyType(AAType.Normal);
      helper.loadControlValues(template);
      assertEquals(AATypeChoice.NORMAL.ordinal(), helper.getRadio().getSelectedIndex());
      
      template.setActiveAssemblyType(AAType.AutoIndex);
      helper.loadControlValues(template);
      assertEquals(AATypeChoice.AUTOINDEX.ordinal(), helper.getRadio().getSelectedIndex());
      
      template.setActiveAssemblyType(AAType.NonHtml);
      helper.loadControlValues(template);
      assertEquals(AATypeChoice.NONHTML.ordinal(), helper.getRadio().getSelectedIndex());
   }
   
   public void testUpdateTemplate()
   {
      final PSActiveAssemblyHelper helper = new PSActiveAssemblyHelper();
      helper.initRadio(m_shell);
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      
      template.setActiveAssemblyType(AAType.Normal);
      helper.loadControlValues(template);
      helper.getRadio().setSelection(AATypeChoice.AUTOINDEX.ordinal());
      helper.updateTemplate(template);
      assertEquals(AAType.AutoIndex, template.getActiveAssemblyType());
   }
}
