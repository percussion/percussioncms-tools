/******************************************************************************
 *
 * [ PSPublishWhenHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.editors.form.PSPublishWhenHelper.PublishWhenChoice;

public class PSPublishWhenHelperTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSPublishWhenHelper helper = new PSPublishWhenHelper();
      helper.initRadio(m_shell);
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      
      assertEquals(PublishWhen.values().length - 1, PublishWhenChoice.values().length);
      template.setPublishWhen(PublishWhen.Always);
      helper.loadControlValues(template);
      assertEquals(PublishWhenChoice.ALWAYS.ordinal(), helper.getRadio().getSelectedIndex());
      
      template.setPublishWhen(PublishWhen.Default);
      helper.loadControlValues(template);
      assertEquals(PublishWhenChoice.DEFAULT.ordinal(), helper.getRadio().getSelectedIndex());
      
      template.setPublishWhen(PublishWhen.Never);
      helper.loadControlValues(template);
      assertEquals(PublishWhenChoice.NEVER.ordinal(), helper.getRadio().getSelectedIndex());
      
      // unspecified is treated as always 
      template.setPublishWhen(PublishWhen.Unspecified);
      helper.loadControlValues(template);
      assertEquals(PublishWhenChoice.ALWAYS.ordinal(), helper.getRadio().getSelectedIndex());
   }
   
   public void testUpdateTemplate()
   {
      final PSPublishWhenHelper helper = new PSPublishWhenHelper();
      helper.initRadio(m_shell);
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      
      template.setPublishWhen(PublishWhen.Always);
      helper.loadControlValues(template);
      helper.getRadio().setSelection(PublishWhen.Default.ordinal());
      helper.updateTemplate(template);
      assertEquals(PublishWhen.Default, template.getPublishWhen());
   }
}
