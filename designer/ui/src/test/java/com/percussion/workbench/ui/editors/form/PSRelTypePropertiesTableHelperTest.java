/******************************************************************************
 *
 * [ PSRelTypePropertiesTableHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.design.objectstore.PSProperty;
import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;

import static com.percussion.workbench.ui.editors.form.PSRelTypePropertiesTableHelper.BLANK_NAME_VAL;

/**
 * @author Andriy Palamarchuk
 */
public class PSRelTypePropertiesTableHelperTest extends PSUiTestBase
{
   public void testGetTableOptions()
   {
      {
         final PSRelTypePropertiesTableHelper helper = createSystem();
         assertNot0(helper.getTableOptions() & PSSortableTable.SURPRESS_MANUAL_SORT);
         assert0(helper.getTableOptions() & PSSortableTable.DELETE_ALLOWED);
         assert0(helper.getTableOptions() & PSSortableTable.INSERT_ALLOWED);
         assert0(helper.getTableOptions() & PSSortableTable.SHOW_DELETE);
      }

      {
         final PSRelTypePropertiesTableHelper helper = createUser();
         assertNot0(helper.getTableOptions() & PSSortableTable.SURPRESS_MANUAL_SORT);
         assertNot0(helper.getTableOptions() & PSSortableTable.DELETE_ALLOWED);
         assertNot0(helper.getTableOptions() & PSSortableTable.INSERT_ALLOWED);
         assertNot0(helper.getTableOptions() & PSSortableTable.SHOW_DELETE);
      }
   }
   
   public void testGetPropertyName()
   {
      final String[] nameContainer = new String[1];
      
      final PSProperty property = new PSProperty("Not checked")
      {
         @Override
         public String getName()
         {
            return nameContainer[0];
         }

         @Override
         public void setName(String name)
         {
            nameContainer[0] = name;
         }
      };

      checkGeneralSetGetPropertyNameHandling(createUser(), property);
      checkGeneralSetGetPropertyNameHandling(createSystem(), property);
      
      checkSystemProvidesReadableName("rs_useownerrevision");
      checkSystemProvidesReadableName("rs_usedependentrevision");
      checkSystemProvidesReadableName("rs_useserverid");
      checkSystemProvidesReadableName("rs_islocaldependency");
      checkSystemProvidesReadableName("rs_skippromotion");
   }

   /**
    * Checks that system, but not user helper transfers the provided predefined 
    * property name to human-readable one.
    */
   private void checkSystemProvidesReadableName(final String name)
   {
      {
         final PSProperty property = new PSProperty(name); 
         assertFalse(StringUtils.equals(
               name, createSystem().getPropertyName(property)));
         assertEquals(name, createUser().getPropertyName(property));
      }
      {
         final String name1 = "random name";
         final PSProperty property = new PSProperty(name1); 
         assertEquals(name1, createSystem().getPropertyName(property));
         assertEquals(name1, createUser().getPropertyName(property));
      }
   }

   private void checkGeneralSetGetPropertyNameHandling(final PSRelTypePropertiesTableHelper helper, final PSProperty property)
   {
      // just a string
      final String name1 = "name";
      helper.setPropertyName(property, name1);
      assertEquals(name1, createUser().getPropertyName(property));
      
      // null
      helper.setPropertyName(property, null);
      assertEquals("", createUser().getPropertyName(property));
      assertEquals(BLANK_NAME_VAL, property.getName());

      // blank
      helper.setPropertyName(property, " \n\t");
      assertEquals("", createUser().getPropertyName(property));
      assertEquals(BLANK_NAME_VAL, property.getName());
   }
   
   private void assert0(int i)
   {
      assertEquals(0, i);
   }

   private void assertNot0(int i)
   {
      assertTrue(i != 0);
   }
   /**
    * Creates system properties helper.
    */
   private PSRelTypePropertiesTableHelper createSystem()
   {
      return new PSRelTypePropertiesTableHelper(true, false);
   }

   /**
    * Creates user properties helper.
    */
   private PSRelTypePropertiesTableHelper createUser()
   {
      return new PSRelTypePropertiesTableHelper(false, false);
   }
}
