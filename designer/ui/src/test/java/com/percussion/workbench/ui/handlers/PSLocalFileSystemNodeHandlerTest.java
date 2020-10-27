/******************************************************************************
 *
 * [ PSLocalFileSystemNodeHandlerTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.workbench.ui.PSUiReference;
import junit.framework.TestCase;
import org.eclipse.swt.dnd.Transfer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class PSLocalFileSystemNodeHandlerTest extends TestCase
{
   private PSLocalFileSystemNodeHandler createHandler()
   {
      return new PSLocalFileSystemNodeHandler(null, null, null);
   }
   
   public void testGetLabelImage()
   {
      // TODO implement
   }

   public void testGetAcceptedTransfers()
   {
      assertTrue(Arrays.equals(new Transfer[0], createHandler().getAcceptedTransfers()));
   }
   
   public void testGetDragHandler()
   {
      assertNotNull(createHandler().getDragHandler());
   }

   public void testGetDropHandler()
   {
      assertNull(createHandler().getDropHandler());
   }

   public void testConfigureForNode()
   {
      // does nothing
      createHandler().configureForNode(null, null);
   }

   public void testHandlePaste()
   {
      try
      {
         createHandler().handlePaste(null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void testHandleDelete()
   {
      try
      {
         PSUiReference r = new PSUiReference(null, "foo", "", null, null, false);
         createHandler().handleDelete(Collections.singleton(r));
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void testEquals()
   {
      assertEquals(createHandler(), createHandler());
      assertEquals(createHandler().hashCode(), createHandler().hashCode());
      assertFalse(createHandler().equals(
            new PSIconNodeHandler(new Properties(), null, null)));
   }
}
