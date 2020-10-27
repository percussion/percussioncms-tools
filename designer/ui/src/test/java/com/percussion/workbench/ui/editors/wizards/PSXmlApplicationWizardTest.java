/******************************************************************************
 *
 * [ PSXmlApplicationWizardTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.PSUiTestBase;

public class PSXmlApplicationWizardTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSXmlApplicationWizard wizard = new PSXmlApplicationWizard();
      assertEquals(0, wizard.getPageCount());
      wizard.addPages();
      assertEquals(1, wizard.getPageCount());
      
      assertEquals(new PSObjectType(PSObjectTypes.XML_APPLICATION,
            PSObjectTypes.XmlApplicationSubTypes.USER), wizard.getObjectType());
   }
}
