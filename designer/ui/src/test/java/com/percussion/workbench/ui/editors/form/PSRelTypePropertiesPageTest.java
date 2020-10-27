/******************************************************************************
 *
 * [ PSRelTypePropertiesPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Andriy Palamarchuk
 */
public class PSRelTypePropertiesPageTest extends PSUiTestBase
{
   public void testListToString()
   {
      final PSRelTypePropertiesPage page = createPage();
      assertEquals("", page.listToString(Collections.<String>emptyList()));
      assertEquals("a",  page.listToString(Collections.singletonList("a")));
      
      final List<String> strings = Arrays.asList(new String[] {"a", "b", "c"});
      assertEquals("a, b, c", page.listToString(strings));
   }

   private PSRelTypePropertiesPage createPage()
   {
      @SuppressWarnings("unused")
      final PSEditorBase editor = new PSEditorBase() {
         @Override
         public boolean isValidReference(IPSReference ref)
         {
            return false;
         }

         @Override
         public void createControl(Composite comp) {}

         public void updateDesignerObject(Object designObject, Object control)
         {}

         public void loadControlValues(Object designObject) {}
         
      };
      final PSRelTypePropertiesPage page =
            new PSRelTypePropertiesPage(m_shell, SWT.NONE, editor);
      return page;
   }

}
