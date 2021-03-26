/******************************************************************************
 *
 * [ PSSelectCommunitiesCompositeTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSSelectCommunitiesCompositeTest extends PSUiTestBase
{
   public void testInit() throws PSModelException
   {
      final String NAME0 = "Name0";
      final String NAME1 = "Name1";
      final String NAME2 = "Name2";
      final List<IPSReference> communities = new ArrayList<IPSReference>();
      communities.add(createRef(NAME2));
      communities.add(createRef(NAME0));
      communities.add(createRef(NAME1));
      
      final PSSelectCommunitiesComposite comp =
            new PSSelectCommunitiesComposite(m_shell, SWT.NONE)
      {
         @Override
         List<IPSReference> getCommunities()
         {
            return communities;
         }
      };
      final String[] availableItems =
         comp.m_communitiesControl.getAvailableList().getItems();
      assertTrue(Arrays.equals(new String[] {NAME0, NAME1, NAME2}, availableItems));
   }
}
