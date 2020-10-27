/******************************************************************************
 *
 * [ PSTemplatePropertiesPageBaseTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class PSTemplatePropertiesPageBaseTest extends PSUiTestBase
{
   public void testBasics()
   {
      new TestImpl(m_shell, SWT.NONE);
   }
   private static class TestImpl extends PSTemplatePropertiesPageBase
   {
      public TestImpl(Composite parent, int style)
      {
         super(parent, style, new PSDummyEditor());
      }

      @Override
      protected String getNamePrefix()
      {
         return "name";
      }
   }
}
