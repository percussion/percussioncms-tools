/******************************************************************************
 *
 * [ PSRelTypeCategoryHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;

/**
 * @author Andriy Palamarchuk
 */
public class PSRelTypeCategoryHelperTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSRelTypeCategoryHelper helper = new PSRelTypeCategoryHelper();
      helper.setCombo(new Combo(m_shell, SWT.NONE));
      
      // unknown category
      {
         final String UNKNOWN_CATEGORY = "Unknown Category";
         final PSRelationshipConfig relType = new PSRelationshipConfig("name",
                  PSRelationshipConfig.RS_TYPE_USER,
                  PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY)
                  {
                     @Override
                     public String getCategory()
                     {
                        return UNKNOWN_CATEGORY;
                     }
                  };
         try
         {
            helper.loadControlValues(relType);
            fail();
         }
         catch (IllegalArgumentException success)
         {
            assertTrue(success.getMessage().contains(UNKNOWN_CATEGORY));
         }
      }

      // no category
      {
         final PSRelationshipConfig relType = new PSRelationshipConfig("name",
               PSRelationshipConfig.RS_TYPE_USER,
               PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY)
               {
                  @Override
                  public String getCategory()
                  {
                     return null;
                  }
               };
         assertTrue(StringUtils.isBlank(relType.getCategory()));
         helper.loadControlValues(relType);
         assertTrue(StringUtils.isBlank(relType.getCategory()));
         final int selection = helper.getCombo().getSelectionIndex();
         assertEquals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY,
               PSRelationshipConfig.CATEGORY_ENUM[selection].getValue());
      }

      // a category
      {
         final PSRelationshipConfig relType = createRelType();
         relType.setCategory(PSRelationshipConfig.CATEGORY_FOLDER);
         helper.loadControlValues(relType);
         final int selection = helper.getCombo().getSelectionIndex();
         assertEquals(PSRelationshipConfig.CATEGORY_FOLDER,
               PSRelationshipConfig.CATEGORY_ENUM[selection].getValue());

         // system type
         relType.setType(PSRelationshipConfig.RS_TYPE_SYSTEM);
         helper.loadControlValues(relType);
         assertFalse(helper.getCombo().isEnabled());

         // user type
         relType.setType(PSRelationshipConfig.RS_TYPE_USER);
         helper.loadControlValues(relType);
         assertTrue(helper.getCombo().isEnabled());
      }
   }

   public void testUpdateRelType()
   {
      final PSRelationshipConfig relType = createRelType();
      relType.setCategory(PSRelationshipConfig.CATEGORY_FOLDER);
      
      final PSRelTypeCategoryHelper helper = new PSRelTypeCategoryHelper();
      helper.setCombo(new Combo(m_shell, SWT.NONE));
      helper.loadControlValues(relType);

      helper.getCombo().select(
            helper.CATEGORIES.indexOf(PSRelationshipConfig.CATEGORY_PROMOTABLE));
      helper.updateRelType(relType);
      assertEquals(PSRelationshipConfig.CATEGORY_PROMOTABLE,
            relType.getCategory());
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
}
