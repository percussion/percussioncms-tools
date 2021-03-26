/******************************************************************************
 *
 * [ PSExtensionModelTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import static com.percussion.extension.IPSExtension.VELOCITY_ASSEMBLER;

@Category(IntegrationTest.class)
public class PSExtensionModelTest extends TestCase

{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
   }
   
   public void testGetAssemblerSourceExt() throws PSModelException
   {
      final PSExtensionModel model = createModel();
      
      // does not accept null assembler name
      try
      {
         model.getAssemblerSourceExt(null);
         fail();
      }
      catch (IllegalArgumentException success) {}

      // does not accept blank string as an assembler name
      try
      {
         model.getAssemblerSourceExt(" \t\n");
         fail();
      }
      catch (IllegalArgumentException success) {}

      assertNull(model.getAssemblerSourceExt("UnknownAssembler"));
      assertEquals(".vm", model.getAssemblerSourceExt(VELOCITY_ASSEMBLER));
   }
   
   /**
    * Creates {@link PSExtensionModel}.
    * @return a new model. Never <code>null</code>.
    */
   private PSExtensionModel createModel()
   {
      return new PSExtensionModel("name", "description",
            PSObjectTypes.EXTENSION);
   }

   /**
    * For unit tests session identification.
    */
   private static final String CLIENT_SESSION_ID = "WORKBENCH_UNIT_TEST";
}
