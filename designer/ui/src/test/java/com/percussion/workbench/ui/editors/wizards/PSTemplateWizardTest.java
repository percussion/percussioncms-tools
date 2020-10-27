/******************************************************************************
 *
 * [ PSTemplateWizardTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSUiTestBase;

public class PSTemplateWizardTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSTemplateWizard wizard = new PSTemplateWizard();
      assertEquals(0, wizard.getPageCount());
      wizard.addPages();
      assertEquals(6, wizard.getPageCount());

      assertEquals(PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
            PSObjectTypes.TemplateSubTypes.SHARED), wizard.getObjectType());
      assertFalse(wizard.canFinish());
   }
}
