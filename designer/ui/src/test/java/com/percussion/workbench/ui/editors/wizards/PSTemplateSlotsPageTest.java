/******************************************************************************
 *
 * [ PSTemplateSlotsPageTest ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSModelException;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

public class PSTemplateSlotsPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      assertTrue(StringUtils.isNotBlank(createPage().getTitle()));
   }
   
   public void testUpdateDesignerObject() throws PSModelException
   {
      final PSTemplateSlotsPage page = new PSTemplateSlotsPage();
      page.createControl(m_shell);

      // unknown control
      try
      {
         page.updateDesignerObject(null, new Button(m_shell, SWT.PUSH));
         fail();
      }
      catch (IllegalArgumentException success) {};
   }

   private PSTemplateSlotsPage createPage()
   {
      final PSTemplateSlotsPage page = new PSTemplateSlotsPage();
      page.createControl(m_shell);
      return page;
   }
}
