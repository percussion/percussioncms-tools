/******************************************************************************
 *
 * [ PSXmlApplicationPropertiesPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Text;

public class PSXmlApplicationPropertiesPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      PSXmlApplicationPropertiesPage page = new PSXmlApplicationPropertiesPage();
      assertTrue(StringUtils.isNotBlank(page.getName()));
      assertTrue(StringUtils.isNotBlank(page.getTitle()));
   }

   public void testUpdateDesignerObject()
   {
      final PSXmlApplicationPropertiesPage page = createPage();
      page.createControl(m_shell);
      final PSApplication application = new PSApplication(NAME1) {};
      assertFalse(NAME1.equals(NAME2));
      
      // name
      ((Text)page.m_nameDescComposite.getNameText()).setText(NAME2);
      page.updateDesignerObject(application, page.m_nameDescComposite.getNameText());
      assertEquals(NAME2, application.getName());
      
      // description
      page.m_nameDescComposite.getDescriptionText().setText(DESCRIPTION);
      page.updateDesignerObject(application,
            page.m_nameDescComposite.getDescriptionText());
      assertEquals(DESCRIPTION, application.getDescription());
      
      // anything else
      try
      {
         page.updateDesignerObject(application,
               page.m_nameDescComposite.getLabelText());
         fail();
      }
      catch (IllegalArgumentException success) {}
   }

   /**
    * Page creation helper. 
    */
   private PSXmlApplicationPropertiesPage createPage()
   {
      final PSXmlApplicationPropertiesPage page =
         new PSXmlApplicationPropertiesPage();
      final PSXmlApplicationWizard wizard = new PSXmlApplicationWizard();
      wizard.setContainer(new TestWizardContainer(page));
      
      page.setWizard(wizard);
      return page;
   }

   /**
    * Sample name.
    */
   private static String NAME1 = "Name 1";

   /**
    * One more sample name.
    */
   private static String NAME2 = "Name 2";

   /**
    * Sample description.
    */
   private static String DESCRIPTION = "Description !";
}
