/******************************************************************************
 *
 * [ PSLegacyDnDHelperTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.legacy;

import com.percussion.workbench.ui.PSUiReference;
import junit.framework.TestCase;
import org.eclipse.swt.dnd.TextTransfer;

import java.util.ArrayList;
import java.util.List;

public class PSLegacyDnDHelperTest extends TestCase
{
   public void testBasics()
   {
      assertNotNull(PSLegacyDnDHelper.getInstance());
      
      final PSLegacyDnDHelper helper = new PSLegacyDnDHelper();
      assertTrue(helper.getTransfers().length > 0);
   }
   
   public void testGetData()
   {
      final PSLegacyDnDHelper helper = new PSLegacyDnDHelper();
      helper.dragStarted();
      
      final List<PSUiReference> nodes1 = new ArrayList<PSUiReference>();
      final String data1 =
         (String) helper.getDataForDrag(TextTransfer.getInstance(), nodes1);
      assertSame(nodes1, helper.extractDataFromDrop(data1));
      
      final List<PSUiReference> nodes2 = new ArrayList<PSUiReference>();
      final String data2 =
         (String) helper.getDataForDrag(TextTransfer.getInstance(), nodes2);
      assertSame(nodes2, helper.extractDataFromDrop(data2));
      assertNull(helper.extractDataFromDrop(data1));
   }
}
