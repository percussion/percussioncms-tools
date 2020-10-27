/******************************************************************************
 *
 * [ PSDummyMultiPageEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import org.eclipse.swt.widgets.Control;

/**
 * Does nothing. Used for tests.
 *
 * @author Andriy Palamarchuk
 */
@SuppressWarnings("unused") //for all parameters
public class PSDummyMultiPageEditor extends PSMultiPageEditorBase
{
   @Override
   protected void createPages()
   {
      // does nothing
   }

   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return false;
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

   @Override
   public void unregisterControl(Control control)
   {
      // does nothing
   }
}
