/******************************************************************************
 *
 * [ PSExtensionRegistrationWizardTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSUiTestBase;

public class PSExtensionRegistrationWizardTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSExtensionRegistrationWizard wizard = new PSExtensionRegistrationWizard();
      assertEquals(0, wizard.getPageCount());
      wizard.addPages();
      assertEquals(1, wizard.getPageCount());

      assertEquals(PSObjectTypeFactory.getType(PSObjectTypes.EXTENSION), wizard.getObjectType());
      assertTrue(wizard.canFinish());
   }
}
