/******************************************************************************
 *
 * [ PSRelTypeGeneralPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;

public class PSRelTypeGeneralPageTest extends PSUiTestBase
{
   public void testBasics()
   {
      new PSRelTypeGeneralPage(m_shell, SWT.NONE, new PSDummyEditor());
   }
   
   public void testLoadControlValues()
   {
      final String label = "Label";
      final String description = "Description";
      final PSRelationshipConfig relType = createRelType();
      relType.setLabel(label);
      relType.setDescription(description);
      
      final PSRelTypeGeneralPage page =
            new PSRelTypeGeneralPage(m_shell, SWT.NONE, new PSDummyEditor());
      page.loadControlValues(relType);
      assertEquals(label, page.m_nameLabelDesc.getLabelText().getText());
      assertEquals(description, page.m_nameLabelDesc.getDescriptionText().getText());
   }

   /**
    * Helper to create new relationship type.
    */
   private PSRelationshipConfig createRelType()
   {
      return new PSRelationshipConfig("name",
            PSRelationshipConfig.RS_TYPE_USER,
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
   }
   
   public void testUpdateRelType()
   {
      
      final PSRelTypeGeneralPage page =
         new PSRelTypeGeneralPage(m_shell, SWT.NONE, new PSDummyEditor());
      final String label = "Label";
      final String description = "Description";
      final PSRelationshipConfig relType = createRelType();
      page.loadControlValues(relType);

      page.m_nameLabelDesc.getLabelText().setText(label);
      page.m_nameLabelDesc.getDescriptionText().setText(description);
      
      page.updateRelType(relType);
      assertEquals(label, relType.getLabel());
      assertEquals(description, relType.getDescription());
   }
}
