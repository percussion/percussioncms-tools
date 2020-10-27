/******************************************************************************
 *
 * [ PSTemplatePropertiesPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;

public class PSTemplatePropertiesPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      assertTrue(StringUtils.isNotBlank(createPage().getName()));
      assertTrue(StringUtils.isNotBlank(createPage().getTitle()));
      assertEmptyText(createPage().getNameText());
      assertEmptyText(createPage().m_nameDesc.getLabelText());
      assertEmptyText(createPage().m_nameDesc.getDescriptionText());
      assertEmptyText(createPage().m_sourceText);
   }

   private void assertEmptyText(final Text text)
   {
      assertTrue(StringUtils.isEmpty(text.getText()));
   }
   
   /**
    * Helper page creator.
    */
   private PSTemplatePropertiesPage createPage()
   {
      final PSTemplatePropertiesPage page = new PSTemplatePropertiesPage();
      final PSTemplateWizard wizard = new PSTemplateWizard();
      wizard.setContainer(new TestWizardContainer(page));
      page.setWizard(wizard);
      page.createControl(m_shell);
      return page;
   }
   
   public void testUpdateDesignerObject() throws IOException
   {
      final PSTemplatePropertiesPage page = createPage();
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();

      // unknown control
      try
      {
         page.updateDesignerObject(template, new Button(m_shell, SWT.PUSH));
         fail();
      }
      catch (IllegalArgumentException success) {};
      
      {
         final String NAME = "Name!";
         page.getNameText().setText(NAME);
         page.updateDesignerObject(template, page.getNameText());
         assertEquals(NAME, template.getName());
      }
      {
         final String LABEL = "Label!";
         page.m_nameDesc.getLabelText().setText(LABEL);
         page.updateDesignerObject(template, page.m_nameDesc.getLabelText());
         assertEquals(LABEL, template.getLabel());
      }
      {
         final String DESCRIPTION = "Description!";
         page.m_nameDesc.getDescriptionText().setText(DESCRIPTION);
         page.updateDesignerObject(template, page.m_nameDesc.getDescriptionText());
         assertEquals(DESCRIPTION, template.getDescription());
      }
      // no source file
      {
         template.setTemplate("Template");
         page.m_sourceText.setText("");
         page.updateDesignerObject(template, page.m_sourceText);
         assertNull(template.getTemplate());
      }
      // source file provided
      {
         final String SOURCE = "Source Text!";
         final File file = File.createTempFile("src", "tmp");
         try
         {
            FileUtils.writeStringToFile(file, SOURCE, "UTF-8");
            page.m_sourceText.setText(file.getAbsolutePath());
            page.updateDesignerObject(template, page.m_sourceText);
         }
         finally
         {
            file.delete();
         }
         assertEquals(SOURCE, template.getTemplate());
      }
      {
         // Andriy: it would not hurt to have more detailed test here,
         // but underlying functionality does not exist yet
         page.updateDesignerObject(template, page.m_communitiesControl.getSlushControl());
      }
   }
}
