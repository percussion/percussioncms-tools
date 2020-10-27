/******************************************************************************
 *
 * [ PSUiConfigManagerTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.config;

import junit.framework.TestCase;
import org.apache.log4j.PropertyConfigurator;

/**
 * 
 */
public class PSUiConfigManagerTest extends TestCase
{
   /**
    * Constructor for PSUiConfigManagerTest.
    * 
    * @param name
    */
   public PSUiConfigManagerTest(String name)
   {
      super(name);
   }

   /*
    * (non-Javadoc)
    * 
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      PropertyConfigurator.configure("log4j.properties");
   }

   /*
    * Test method for the methods:
    * 'com.percussion.workbench.config.PSUiConfigManager.getSectionConfig(String)'
    * 'com.percussion.workbench.config.PSUiConfigManager.saveSectionConfig(PSSectionConfig)'
    * 'com.percussion.workbench.config.PSUiConfigManager.removeSectionConfig(PSSectionConfig)'
    */
   public void testAll()
   {
      // create a test config object and persist first
      TestSectionConfig cfg = new TestSectionConfig();
      cfg.setKey(TestSectionConfig.KEY);
      for (int i = 0; i < 10; i++)
         cfg.addValue("value" + i);
      PSUiConfigManager.getInstance().saveSectionConfig(cfg);
      // Test get method
      Object obj = PSUiConfigManager.getInstance().getSectionConfig(
         TestSectionConfig.KEY);
      assertTrue(obj != null && obj instanceof TestSectionConfig);
      // Test Save method
      cfg = (TestSectionConfig) obj;
      cfg.addValue("newValue");
      PSUiConfigManager.getInstance().saveSectionConfig(cfg);
      obj = PSUiConfigManager.getInstance().getSectionConfig(
         TestSectionConfig.KEY);
      assertTrue(obj != null && obj instanceof TestSectionConfig);
      TestSectionConfig cfgNew = (TestSectionConfig) obj;
      assertTrue(!cfg.equals(cfgNew));
      // Test Remove method
      PSUiConfigManager.getInstance().removeSectionConfig(cfgNew);
      obj = PSUiConfigManager.getInstance().getSectionConfig(
         TestSectionConfig.KEY);
      assertTrue(obj == null);
   }

}
