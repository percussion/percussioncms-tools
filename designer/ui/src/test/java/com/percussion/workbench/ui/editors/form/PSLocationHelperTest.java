/******************************************************************************
 *
 * [ PSLocationHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;

public class PSLocationHelperTest extends PSUiTestBase
{
   public void testLoadControlValues()
   {
      final PSLocationHelper helper = new PSLocationHelper();
      helper.initUI(m_shell);
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      // prefix
      {
         template.setLocationPrefix(LOCATION_PREFIX);
         helper.loadControlValues(template);
         assertEquals(LOCATION_PREFIX, helper.getPrefixText().getText());
         
         template.setLocationPrefix(null);
         helper.loadControlValues(template);
         assertTrue(StringUtils.isEmpty(helper.getPrefixText().getText()));
      }

      // suffix
      {
         template.setLocationSuffix(LOCATION_SUFFIX);
         helper.loadControlValues(template);
         assertEquals(LOCATION_SUFFIX, helper.getSuffixText().getText());
         
         template.setLocationSuffix(null);
         helper.loadControlValues(template);
         assertTrue(StringUtils.isEmpty(helper.getSuffixText().getText()));
      }
   }

   public void testUpdateTemplate()
   {
      final PSLocationHelper helper = new PSLocationHelper();
      helper.initUI(m_shell);
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      // prefix
      {
         assertTrue(StringUtils.isEmpty(template.getLocationPrefix()));
         helper.getPrefixText().setText(LOCATION_PREFIX);
         helper.updateTemplate(template);
         assertEquals(LOCATION_PREFIX, template.getLocationPrefix());
         
         helper.getPrefixText().setText("");
         helper.updateTemplate(template);
         assertTrue(StringUtils.isEmpty(template.getLocationPrefix()));
      }
      
      // suffix
      {
         assertTrue(StringUtils.isEmpty(template.getLocationSuffix()));
         helper.getSuffixText().setText(LOCATION_SUFFIX);
         helper.updateTemplate(template);
         assertEquals(LOCATION_SUFFIX, template.getLocationSuffix());
         
         helper.getSuffixText().setText("");
         helper.updateTemplate(template);
         assertTrue(StringUtils.isEmpty(template.getLocationSuffix()));
      }
   }

   // test data
   private final String LOCATION_PREFIX = "Prefix";
   private final String LOCATION_SUFFIX = "Suffix";
}
