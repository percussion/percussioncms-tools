/******************************************************************************
 *
 * [ PSTemplateSitesPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;

public class PSTemplateSitesPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      new PSTemplateSitesPage(m_shell, SWT.NONE, new PSDummyEditor());
   }
}
