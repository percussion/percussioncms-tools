/******************************************************************************
 *
 * [ PSVariantWizardTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.workbench.ui.PSUiTestBase;

/**
 * @author Andriy Palamarchuk
 */
public class PSVariantWizardTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSVariantWizard wizard = new PSVariantWizard();
      assertEquals(0, wizard.getPageCount());
      wizard.addPages();
      assertEquals(1, wizard.getPageCount());

      assertEquals(PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
            TemplateSubTypes.VARIANT), wizard.getObjectType());
      assertTrue(wizard.canFinish());
   }
}
