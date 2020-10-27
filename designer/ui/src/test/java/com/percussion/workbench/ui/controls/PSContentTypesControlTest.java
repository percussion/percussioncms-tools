/******************************************************************************
 *
 * [ PSContentTypesControlTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.jmock.cglib.CGLIBCoreMock;

import java.util.HashSet;
import java.util.Set;

public class PSContentTypesControlTest extends PSUiTestBase
{
   public void testBasics()
   {
      final PSContentTypesControl control = new PSContentTypesControl(m_shell, SWT.NONE);
      assertTrue(StringUtils.isNotBlank(control.getAvailableLabelText()));
      assertTrue(StringUtils.isNotBlank(control.getSelectedLabelText()));
   }
   
   protected IPSReference createContentTypeRef(final String name) throws PSModelException
   {
      final PSReference ref = new PSReference();
      ref.setName(name);
      ref.setObjectType(PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE));
      return ref;
   }

   public void testUpdateDesignerObject() throws PSModelException
   {
      final IPSReference ref1 = createContentTypeRef("ContentType1");
      final IPSReference ref2 = createContentTypeRef("ContentType2");
      final Set<IPSReference> selectedContentTypes = new HashSet<IPSReference>();
      selectedContentTypes.add(ref1);
      selectedContentTypes.add(ref2);

      final PSContentTypesControl page = new PSContentTypesControl(m_shell,
            SWT.NONE)
      {
         @Override
         protected Set<IPSReference> getSelections()
         {
            return selectedContentTypes;
         }
      };
      final CGLIBCoreMock mockContentTypeModel = new CGLIBCoreMock(IPSContentTypeModel.class);
      page.m_contentTypeModel = (IPSContentTypeModel) mockContentTypeModel
            .proxy();
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();

      // content types
      page.updateTemplate(template);
      assertEquals(new HashSet<IPSReference>(selectedContentTypes),
            template.getNewContentTypes());
      
      mockContentTypeModel.verify();
   }

}
