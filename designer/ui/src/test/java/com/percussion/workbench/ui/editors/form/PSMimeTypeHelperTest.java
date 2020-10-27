/******************************************************************************
 *
 * [ PSMimeTypeHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.content.IPSMimeContentTypes;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PSMimeTypeHelperTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSMimeTypeHelper helper = new PSMimeTypeHelper();
      helper.setCombo(new Combo(m_shell, SWT.NONE));
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setMimeType(IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN);
      helper.loadControlValues(template);
      assertEquals(IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN,
            helper.m_mimeTypes.get(helper.getCombo().getSelectionIndex()));
      
      template.setMimeType(IPSMimeContentTypes.MIME_TYPE_TEXT_XML);
      helper.loadControlValues(template);
      assertEquals(IPSMimeContentTypes.MIME_TYPE_TEXT_XML,
            helper.m_mimeTypes.get(helper.getCombo().getSelectionIndex()));
      
      // combo parameters
      assertEquals(helper.getCombo().getItemCount(), helper.m_mimeTypes.size());
      assertTrue(helper.m_mimeTypes.size() > 0);
      {
         final List<String> sortedTypes = new ArrayList<String>(helper.m_mimeTypes);
         Collections.sort(sortedTypes);
         assertEquals(sortedTypes, helper.m_mimeTypes);
      }

      // unknown type
      {
         final String UNKNOWN_MIME_TYPE = "Unknown Mime Type";
         template.setMimeType(UNKNOWN_MIME_TYPE);
         helper.loadControlValues(template);
         assertEquals(UNKNOWN_MIME_TYPE,
               helper.m_mimeTypes.get(helper.getCombo().getSelectionIndex()));
      }
   }
   
   public void testUpdateTemplate()
   {
      final PSMimeTypeHelper helper = new PSMimeTypeHelper();
      helper.setCombo(new Combo(m_shell, SWT.NONE));
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      
      template.setMimeType(IPSMimeContentTypes.MIME_TYPE_TEXT_PLAIN);
      helper.loadControlValues(template);
      helper.getCombo().select(
            helper.m_mimeTypes.indexOf(IPSMimeContentTypes.MIME_TYPE_TEXT_XML));
      helper.updateTemplate(template);
      assertEquals(IPSMimeContentTypes.MIME_TYPE_TEXT_XML, template.getMimeType());
   }
}
