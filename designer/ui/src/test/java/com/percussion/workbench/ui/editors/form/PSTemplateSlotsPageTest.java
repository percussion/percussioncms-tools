package com.percussion.workbench.ui.editors.form;

import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;

public class PSTemplateSlotsPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      new PSTemplateSlotsPage(m_shell, SWT.NONE, new PSDummyEditor());
   }
}
