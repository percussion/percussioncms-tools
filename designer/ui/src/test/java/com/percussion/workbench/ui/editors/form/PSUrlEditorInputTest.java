/******************************************************************************
 *
 * [ PSUrlEditorInputTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import junit.framework.TestCase;

import java.net.MalformedURLException;
import java.net.URL;

public class PSUrlEditorInputTest extends TestCase
{
   public void testBasics() throws MalformedURLException
   {
      final URL url = new URL(URL_STR);
      final PSUrlEditorInput input = new PSUrlEditorInput(url);
      assertFalse(input.exists());
      assertNull(input.getPersistable());
      assertEquals("/bb/search", input.getName());
      assertEquals(URL_STR, input.getToolTipText());
      assertEquals(url, input.getAdapter(URL.class));
   }
   
   public void testEqualsHash() throws MalformedURLException
   {
      final URL url1 = new URL(URL_STR);
      final URL url2 = new URL(URL_STR + "2");
      final PSUrlEditorInput input = new PSUrlEditorInput(url1);
      assertEquals(input, input);
      assertEquals(input, new PSUrlEditorInput(url1));
      assertEquals(input.hashCode(), new PSUrlEditorInput(url1).hashCode());
      assertFalse(input.equals(new PSUrlEditorInput(url2)));
   }
   
   /**
    * A sample url.
    */
   private static final String URL_STR =
         "http://www.google.com/bb/search?q=something&btnG=Google+Search";
}
