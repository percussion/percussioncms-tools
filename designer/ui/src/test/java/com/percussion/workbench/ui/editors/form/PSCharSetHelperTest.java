/******************************************************************************
 *
 * [ PSCharSetHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.util.PSIgnoreCaseStringComparator;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PSCharSetHelperTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSCharSetHelper helper = new PSCharSetHelper();
      helper.setCombo(new Combo(m_shell, SWT.NONE));
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setCharset(BIG5_CHARSET);
      helper.loadControlValues(template);
      assertEquals(BIG5_CHARSET,
            helper.m_charSets.get(helper.getCombo().getSelectionIndex()));
      
      template.setCharset(ASCII_CHARSET);
      helper.loadControlValues(template);
      assertEquals(ASCII_CHARSET,
            helper.m_charSets.get(helper.getCombo().getSelectionIndex()));

      // general combo check
      assertEquals(helper.getCombo().getItemCount(), helper.m_charSets.size());
      {
         final List<String> sortedCharSets = new ArrayList<String>(helper.m_charSets);
         Collections.sort(sortedCharSets, new PSIgnoreCaseStringComparator());
         assertEquals(sortedCharSets, helper.m_charSets);
      }

      {
         final String UNKNOWN_CHAR_SET = "Unknown Char Set";
         template.setCharset(UNKNOWN_CHAR_SET);
         helper.loadControlValues(template);
         assertEquals(UNKNOWN_CHAR_SET,
               helper.m_charSets.get(helper.getCombo().getSelectionIndex()));
      }
   }

   public void testUpdateTemplate()
   {
      final PSCharSetHelper helper = new PSCharSetHelper();
      helper.setCombo(new Combo(m_shell, SWT.NONE));
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      // character set
      template.setCharset(BIG5_CHARSET);
      helper.loadControlValues(template);
      helper.getCombo().select(helper.m_charSets.indexOf(ASCII_CHARSET));
      helper.updateTemplate(template);
      assertEquals(ASCII_CHARSET, template.getCharset());
   }

   /**
    * Name for the ASCII charset.
    */
   private static final String ASCII_CHARSET = "US-ASCII";
   
   /**
    * Name of the Big5 charset.
    */
   private static final String BIG5_CHARSET = "Big5";
}
