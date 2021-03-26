/******************************************************************************
 *
 * [ PSUserConnectionSetTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.workbench.connections;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.workbench.config.PSUiConfigManager;
import junit.framework.TestCase;

/**
 * JUnit test impl for
 * {@link com.percussion.workbench.connections.PSUserConnectionSet} class.
 */
public class PSUserConnectionSetTest extends TestCase
{
   /**
    * @param name
    */
   public PSUserConnectionSetTest(String name)
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
      PSUserConnectionSet list = PSUiConfigManager.getInstance()
         .getUserConnections();
      if (list == null || !list.getConnections().hasNext())
      {
         list = new PSUserConnectionSet();
         boolean useSsl = true;
         for (int i = 0; i < 5; i++)
         {
            PSUserConnection conn = new PSUserConnection("Connection " + i,
               "Server " + i, 9992, "userid " + i, "demo " + i,
               useSsl = !useSsl, PSI18nUtils.DEFAULT_LANG);
            list.addConnection(conn);
         }
         PSUiConfigManager.getInstance().saveSectionConfig(list);
      }
   }

   /**
    * Test addinig a new connection.
    * 
    * @throws Exception
    */
   public void testAddNew() throws Exception
   {
      PSUserConnectionSet list = PSUiConfigManager.getInstance()
         .getUserConnections();
      PSUserConnection conn = new PSUserConnection("Connection New",
            "Server New", 9992, "userid New", "demonew", true,
            PSI18nUtils.DEFAULT_LANG);
      list.addConnection(conn);
      PSUiConfigManager.getInstance().saveSectionConfig(list);
   }

   /**
    * Test modifying an existing connection.
    * 
    * @throws Exception
    */
   public void testModify() throws Exception
   {
      PSUserConnectionSet list = PSUiConfigManager.getInstance()
         .getUserConnections();
      PSUserConnection conn = list.getConnections().next();
      conn.setName("TestTest");
      PSUiConfigManager.getInstance().saveSectionConfig(list);
   }

   /**
    * Test removing an existing connection.
    * 
    * @throws Exception
    */
   public void testRemove() throws Exception
   {
      PSUserConnectionSet list = PSUiConfigManager.getInstance()
         .getUserConnections();
      PSUserConnection conn = list.getConnections().next();
      list.remove(conn);
      PSUiConfigManager.getInstance().saveSectionConfig(list);
   }
}
