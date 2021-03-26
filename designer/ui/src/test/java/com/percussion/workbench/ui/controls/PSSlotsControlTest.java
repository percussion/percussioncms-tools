/******************************************************************************
 *
 * [ PSSlotsControlTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.jmock.cglib.Mock;

import java.util.HashSet;
import java.util.Set;

public class PSSlotsControlTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSSlotsControl control = new PSSlotsControl(m_shell, SWT.NONE);
      assertTrue(StringUtils.isNotBlank(control.getAvailableLabelText()));
      assertTrue(StringUtils.isNotBlank(control.getSelectedLabelText()));
   }
   
   public void testUpdateDesignerObject() throws PSModelException
   {
      final IPSReference ref1 = createRef("Name1");
      final IPSReference ref2 = createRef("Name2");
      final Set<IPSReference> refs = new HashSet<IPSReference>();
      refs.add(ref1);
      refs.add(ref2);
      
      final PSSlotsControl page = new PSSlotsControl(m_shell, SWT.NONE)
      {
         @Override
         protected Set<IPSReference> getSelections()
         {
            return refs;
         }
      };
      final Mock mockSlotModel = new Mock(IPSCmsModel.class);
      page.m_slotModel = (IPSCmsModel) mockSlotModel.proxy();
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();

      // slots
      template.addSlot(createSlot("Slot1"));
      template.addSlot(createSlot("Slot2"));
      
      assertEquals(2, template.getSlots().size());

      mockSlotModel.expects(once()).method("load")
         .with(ANYTHING, eq(false), eq(false))
         .will(returnValue(new Object[] {createSlot(ref2.getName())}));
      page.updateTemplate(template);
      assertEquals(1, template.getSlots().size());
      assertEquals(ref2.getName(), template.getSlots().iterator().next().getName());
      
      // exception while specifying slots
      {
         final RuntimeException e = new RuntimeException();
         mockSlotModel.expects(once()).method("load")
               .with(ANYTHING, eq(false), eq(false))
               .will(throwException(e));
         try
         {
            page.updateTemplate(template);
            fail();
         }
         catch (RuntimeException thrown)
         {
            assertSame(thrown, e);
         }
      }
      
      mockSlotModel.verify();
   }

   private IPSTemplateSlot createSlot(String name)
   {
      final PSTemplateSlot slot = new PSTemplateSlot();
      slot.setName(name);
      return slot; 
   }
}
