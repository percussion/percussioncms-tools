/*[ UIParameterPanelTest.java ]******************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSTextLiteral;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;
import java.util.Vector;

/**
 * @author dougrand
 *
 * Test UIParameterPanel
 */
public class UTParameterPanelTest extends TestCase
{

   /**
    * @param arg0
    */
   public UTParameterPanelTest(String arg0)
   {
      super(arg0);
      // XXX Auto-generated constructor stub
   }
   
   public static TestSuite suite()
   {
      return new TestSuite(UTParameterPanelTest.class);
   }
   
   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      
      m_parent = new JDialog();
   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      m_parent.dispose();
   }   
   
   public void testParameterHandling() throws Exception
   {
      Vector types = new Vector();
      DTTextLiteral defaultvalue = new DTTextLiteral();
      types.add(defaultvalue);
      ValueSelectorDialog dialog = new ValueSelectorDialog(m_parent, types, defaultvalue);
      ValueSelectorCellEditor ce = new ValueSelectorCellEditor(dialog);
      UTParameterPanel panel = new UTParameterPanel(ce, false);
      
      UTParameterPanelTableModel model = panel.getTableModel();
      model.appendRow();
      model.appendRow();
      
      panel.setParameterAt("foo", new PSTextLiteral("bar"), "no desc", 0);
      panel.setParameterAt("bar", null, "no desc", 1);
      
      PSExtensionParamValue vals[] = panel.getParameters();
      
      assertTrue(vals.length == 2);
      
      // Check for no null values
      for (int i = 0; i < vals.length; i++)
      {
         assertTrue(vals[i] != null);
      }
      
      // Check values
      IPSReplacementValue val0 = vals[0].getValue();
      IPSReplacementValue val1 = vals[1].getValue();
      
      assertTrue(val0.getValueText().equals("bar"));
      assertTrue(val1.getValueText().length() == 0);
   }

   /**
    * Dialog for use in test only
    */
   JDialog m_parent = null;
}
