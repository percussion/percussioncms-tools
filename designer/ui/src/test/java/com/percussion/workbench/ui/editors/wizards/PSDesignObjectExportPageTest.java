/*******************************************************************************
 *
 * [ PSDesignObjectExportPageTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import static com.percussion.workbench.ui.PSUiTestBase.CLIENT_SESSION_ID;

/**
 * @author Andriy Palamarchuk
 */
public class PSDesignObjectExportPageTest extends MockObjectTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
   }

   public void testCreate() throws PSModelException
   {
      final Mock mockSelection = new Mock(IStructuredSelection.class);
      final IStructuredSelection selection =
            (IStructuredSelection) mockSelection.proxy();
      
      // null type
      try
      {
         new PSDesignObjectExportPage(null, selection);
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }

      // unrecognized type
      try
      {
         new PSDesignObjectExportPage(TestEnum.VAL1, selection);
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }
      
      // everything is OK
      new PSDesignObjectExportPage(PSObjectTypes.COMMUNITY, selection);

      try
      {
         new PSDesignObjectExportPage(PSObjectTypes.COMMUNITY, null);
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }
      
      mockSelection.verify();
   }

   /**
    * Used for testing.
    */
   private static enum TestEnum
   {
      VAL1,
      VAL2
   }
}
