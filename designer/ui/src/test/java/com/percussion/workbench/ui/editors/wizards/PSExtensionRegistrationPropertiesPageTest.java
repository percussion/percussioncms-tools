/******************************************************************************
 *
 * [ PSExtensionRegistrationPropertiesPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.models.IPSExtensionModel.Handlers;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

/**
 * @author Andriy Palamarchuk
 */
public class PSExtensionRegistrationPropertiesPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      assertTrue(StringUtils.isNotBlank(createPage().getName()));
      assertTrue(StringUtils.isNotBlank(createPage().getTitle()));
   }

   private PSExtensionRegistrationPropertiesPage createPage()
   {
      return new PSExtensionRegistrationPropertiesPage();
   }
   
   public void testHandlerSelectionChanges()
   {
      final PSExtensionRegistrationPropertiesPage page = createPage();
      final PSExtensionRegistrationWizard wizard = new PSExtensionRegistrationWizard();
      wizard.setContainer(new TestWizardContainer(page));
      page.setWizard(wizard);
      page.createControl(m_shell);
      
      // initial selection
      assertEquals(0, page.m_handlerCombo.getSelectionIndex());
      assertEquals(Handlers.JAVA, page.getSelectedHandler());
      //assertEquals(Interfaces.values().length, page.m_supportedInterfaces.getItemCount());
      
      // select non-default type
//      final int newIdx = 3;
      //page.m_supportedInterfaces.select(newIdx);
      
      // switch to JavaScript
      page.m_handlerCombo.select(Handlers.JAVASCRIPT.ordinal());
      page.handlerSelectionChanged();
      assertFalse(page.m_supportedInterfaces.isEnabled());
      //assertEquals(0, page.m_supportedInterfaces.getItemCount());
      
      // switch back to Java
      page.m_handlerCombo.select(Handlers.JAVA.ordinal());
      page.handlerSelectionChanged();
      assertTrue(page.m_supportedInterfaces.isEnabled());
      //assertEquals(newIdx, page.m_supportedInterfaces.getSelectionIndex());
   }
   
   public void testUpdateDesignerObject()
   {
      final PSExtensionRegistrationPropertiesPage page = createPage();
      final PSExtensionRegistrationWizard wizard = new PSExtensionRegistrationWizard();
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
   
   public void testFindObjectName()
   {
      final PSExtensionRegistrationPropertiesPage page = createPage();
      final PSExtensionRegistrationWizard wizard = new PSExtensionRegistrationWizard();
      wizard.setContainer(new TestWizardContainer(page));
      page.setWizard(wizard);

      page.createControl(m_shell);
      ((Text) page.m_nameLabelDesc.getNameText()).setText("js1");
      assertTrue(page.findObjectName().endsWith("js1"));
      assertTrue(page.findObjectName().contains("global/percussion/"));
   }
}
