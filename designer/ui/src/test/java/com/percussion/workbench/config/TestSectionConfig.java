/******************************************************************************
 *
 * [ TestSectionConfig.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Config for JUnit test purpose. Configuration scope is not included in the
 * test.
 */
public class TestSectionConfig extends PSSectionConfig
{
   private List<String> values = new ArrayList<String>();

   public TestSectionConfig()
   {
   }

   public Iterator<String> getValues()
   {
      return values.iterator();
   }

   public void addValue(String value)
   {
      this.values.add(value);
   }

   static final String KEY = "com.percussion.workbench.config.testConfigKey";
}
