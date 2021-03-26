/******************************************************************************
 *
 * [ PSTemplateGeneralPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.extension.IPSExtension;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

public class PSTemplateGeneralPageTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSTemplateGeneralPage page =
            new PSTemplateGeneralPage(m_shell, SWT.NONE, new PSDummyEditor());
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setPublishWhen(PublishWhen.Never);
      template.setMimeType(IPSMimeContentTypes.MIME_TYPE_TEXT_HTML);
      template.setCharset("ascii");

      // name, label, desc
      {
         final String NAME = "Name !"; 
         final String LABEL = "Label !"; 
         final String DESC = "Description !!!";
         template.setName(NAME);
         template.setLabel(LABEL);
         template.setDescription(DESC);
         template.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);

         page.loadControlValues(template);
         
         assertEquals(NAME, ((Label) page.m_nameLabelDesc.getNameText()).getText());
         assertEquals(LABEL, page.m_nameLabelDesc.getLabelText().getText());
         assertEquals(DESC, page.m_nameLabelDesc.getDescriptionText().getText());
         
         template.setDescription(null);
         
         page.loadControlValues(template);
         
         assertTrue(StringUtils.isEmpty(page.m_nameLabelDesc.getDescriptionText().getText()));
      }
   }

   public void testUpdateTemplate()
   {
      final PSTemplateGeneralPage page =
            new PSTemplateGeneralPage(m_shell, SWT.NONE, new PSDummyEditor());
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setPublishWhen(PublishWhen.Never);
      template.setMimeType(IPSMimeContentTypes.MIME_TYPE_TEXT_XML);
      template.setCharset("ascii");
      template.setAssembler(IPSExtension.VELOCITY_ASSEMBLER);


      // name, label, desc
      {
         final String NAME = "Name !"; 
         final String LABEL = "Label !"; 
         final String DESC = "Description !!!";
         template.setName(NAME);
         template.setLabel(LABEL);
         template.setDescription(DESC);

         page.loadControlValues(template);
         
         final String LABEL2 = "Label2 !"; 
         final String DESC2 = "Description2 !!!";
         page.m_nameLabelDesc.getLabelText().setText(LABEL2);
         page.m_nameLabelDesc.getDescriptionText().setText(DESC2);
         page.updateTemplate(template);
         page.m_nameLabelDesc.update();
         
         assertEquals(LABEL2, template.getLabel());
         assertEquals(DESC2, template.getDescription());
         
         page.m_nameLabelDesc.getLabelText().setText("");
         page.m_nameLabelDesc.getDescriptionText().setText("");
         page.updateTemplate(template);
         
         assertEquals(NAME, template.getLabel());
         assertEquals("", template.getDescription());
      }
   }
}
