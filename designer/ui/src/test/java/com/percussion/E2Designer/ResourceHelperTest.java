/******************************************************************************
 *
 * [ ResourceHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import junit.framework.TestCase;

import static com.percussion.E2Designer.ResourceHelper.getMnemonic;
import static com.percussion.E2Designer.ResourceHelper.getWithMnemonic;

public class ResourceHelperTest extends TestCase
{
   public void testGetMnemonic()
   {
      final PSResources res = createMnemonicResource();
      
      // normal
      assertEquals('a', getMnemonic(res, "id1"));

      // no mnemonic
      assertEquals(0, getMnemonic(res, "id2"));
      assertEquals(0, getMnemonic(res, "Unknown"));
      
      // wrong resource type
      assertEquals(0, getMnemonic(res, "id3"));
   }
   
   public void testGetWithMnemonic()
   {
      final PSResources res = createMnemonicResource();
      
      // normal
      assertEquals("&Abcaaa", getWithMnemonic("Abcaaa", res, "id1"));
      assertEquals("bc&aaa", getWithMnemonic("bcaaa", res, "id1"));
      
      // no mnemonic
      assertEquals("Abc", getWithMnemonic("Abc", res, "id2"));
   }

   /**
    * Creates resources for mnemonics processing tests.
    */
   private PSResources createMnemonicResource()
   {
      return new PSResources()
      {
         @Override
         protected Object[][] getContents()
         {
            return new Object[][]{
                  {"id1", "Text 1"},
                  {"id2", "Text 2"},
                  {"mn_id1", 'a'},
                  {"mn_id3", "sss"}};
         }
      };
   }
}
