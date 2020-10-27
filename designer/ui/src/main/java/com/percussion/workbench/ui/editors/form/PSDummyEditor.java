/******************************************************************************
 *
 * [ PSDummyEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import org.eclipse.swt.widgets.Composite;

/**
 * This implementation does nothing. Is used in tests.
 *
 * @author Andriy Palamarchuk
 */
public class PSDummyEditor extends PSEditorBase
{
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return false;
   }

   @Override
   public void createControl(Composite comp)
   {
      // does nothing
   }

   public void updateDesignerObject(Object designObject, Object control)
   {
      // does nothing
   }

   public void loadControlValues(Object designObject)
   {
      // does nothing
   }

   @Override
   public void registerControl(String displayName, Object control, IPSControlValueValidator[] validators, int page)
   {
      // does nothing
   }

   @Override
   public void registerControl(String displayName, Object control, IPSControlValueValidator[] validators)
   {
      // does nothing
   }
}
