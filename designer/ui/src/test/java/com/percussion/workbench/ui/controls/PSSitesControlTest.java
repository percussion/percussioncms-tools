/******************************************************************************
 *
 * [ PSSitesControlTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;

import java.util.HashSet;
import java.util.Set;

public class PSSitesControlTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSSitesControl control = new PSSitesControl(m_shell, SWT.NONE);
      assertTrue(StringUtils.isNotBlank(control.getAvailableLabelText()));
      assertTrue(StringUtils.isNotBlank(control.getSelectedLabelText()));
   }
   
   public void testUpdateTemplate() throws PSModelException
   {
      final IPSReference siteRef1 = createRef("Site 1");
      final IPSReference siteRef2 = createRef("Site 2");

      final Set<IPSReference> siteRefs = new HashSet<IPSReference>();
      siteRefs.add(siteRef1);
      siteRefs.add(siteRef2);
      
      final PSSitesControl control = new PSSitesControl(m_shell, SWT.NONE)
      {
         @Override
         protected Set<IPSReference> getSelections()
         {
            return siteRefs;
         }
      };

      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      assertTrue(template.getSites().isEmpty());
      control.updateTemplate(template);
      assertEquals(siteRefs, template.getSites());
   }
}
