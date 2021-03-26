/******************************************************************************
 *
 * [ PSRelTypeEffectsExecutionContextDialogTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.workbench.ui.PSUiTestBase;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static com.percussion.relationship.IPSExecutionContext.VALIDATION_MAX;
import static com.percussion.relationship.IPSExecutionContext.VALIDATION_MIN;
import static com.percussion.workbench.ui.editors.form.PSRelTypeEffectsExecutionContextDialog.executionContextToLabel;

/**
 * @author Andriy Palamarchuk
 */
public class PSRelTypeEffectsExecutionContextDialogTest extends PSUiTestBase
{
   public void testExecutionContextToLabel()
   {
      final Set<String> labels = new HashSet<String>();
      for (int i = VALIDATION_MIN; i <= VALIDATION_MAX; i++)
      {
         final String label = executionContextToLabel(i);
         assertTrue(StringUtils.isNotBlank(label));
         assertTrue(label.length() < 50);
         assertFalse(labels.contains(label));
         labels.add(label);
      }
   }
   
   public void testExecutionContextToLabel_NonExistintId()
   {
      assertExecutionContextToLabelFails(-10);
      assertExecutionContextToLabelFails(VALIDATION_MIN - 1);
      assertExecutionContextToLabelFails(VALIDATION_MAX + 1);
   }

   /**
    * Insures that
    * {@link PSRelTypeEffectsExecutionContextDialog#executionContextToLabel(int)}
    * fails with provided id. 
    */
   private void assertExecutionContextToLabelFails(final int id)
   {
      try
      {
         executionContextToLabel(id);
         fail();
      }
      catch (IllegalArgumentException success) {}
   }
}
