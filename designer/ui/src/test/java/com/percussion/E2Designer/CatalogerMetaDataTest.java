/******************************************************************************
 *
 * [ CatalogerMetaData.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import junit.framework.TestCase;

public class CatalogerMetaDataTest extends TestCase
{
   public void testEquals()
   {
      final String fullName = "Full Name";
      final String name = "name";
      final String type = "type";

      final CatalogerMetaData data = new CatalogerMetaData(name, type, fullName);
      assertFalse(data.equals(null));
      assertFalse(data.equals(new Object()));
      
      assertTrue(data.equals(new CatalogerMetaData(name, type, fullName)));
      assertEquals(data.hashCode(),
            new CatalogerMetaData(name, type, fullName).hashCode());
      
      assertFalse(data.equals(new CatalogerMetaData("other", type, fullName)));
      assertFalse(data.equals(new CatalogerMetaData(name, "other", fullName)));
      assertFalse(data.equals(new CatalogerMetaData(name, type, "other")));
   }

}
