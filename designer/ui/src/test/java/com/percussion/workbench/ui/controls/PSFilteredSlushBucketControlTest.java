/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSFilteredSlushBucketControlTest extends PSUiTestBase
{
   public void testBasics()
   {
      new TestImpl(m_shell, SWT.NONE);
   }
   
   public void testInitializeAvailableItems() throws PSModelException
   {
      final String NAME0 = "Name0";
      final String NAME1 = "Name1";
      final String NAME2 = "Name2";
      final String NAME3 = "Name3";
      final String NAME4 = "Name4";
      final List<IPSReference> refs = new ArrayList<IPSReference>();
      refs.add(createRef(NAME4));
      refs.add(createRef(NAME2));
      refs.add(createRef(NAME0));
      refs.add(createRef(NAME1));
      refs.add(createRef(NAME3));
      
      final List<IPSReference> selectedRefs = new ArrayList<IPSReference>();
      selectedRefs.add(createRef(NAME3));
      selectedRefs.add(createRef(NAME2));
      
      final TestImpl control = new TestImpl(m_shell, SWT.NONE)
      {
        @Override
        protected List<IPSReference> createAvailableItems(final Object designObject)
        {
           return refs;
        }

        @Override
        protected List<IPSReference> createOriginallySelectedItems(final Object designObject)
        {
           return selectedRefs;
        }
      };
      control.loadControlValues(null);
      
      final String[] availableItems =
         control.m_selectionControl.getAvailableList().getItems();
      assertTrue(Arrays.equals(new String[] {NAME0, NAME1, NAME4}, availableItems));

      final String[] selectedItems =
         control.m_selectionControl.getSelectedList().getItems();
      assertTrue(Arrays.equals(new String[] {NAME2, NAME3}, selectedItems));

      assertEquals(refs.size(), availableItems.length + selectedItems.length);
   }

   private static class TestImpl extends PSFilteredSlushBucketControl
   {
      public TestImpl(Composite parent, int style)
      {
         super(parent, style);
      }

      @Override
      public String getSelectedLabelText()
      {
         return "Selected";
      }

      @Override
      protected String getAvailableLabelText()
      {
         return "Available";
      }

      @Override
      protected List<IPSReference> createAvailableItems(final Object designObject)
      {
         return new ArrayList<IPSReference>();
      }

      @Override
      protected List<IPSReference> createOriginallySelectedItems(final Object designObject)
      {
         return new ArrayList<IPSReference>();
      }
   }
}
