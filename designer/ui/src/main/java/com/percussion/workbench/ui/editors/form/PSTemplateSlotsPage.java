/******************************************************************************
 *
 * [ PSTemplateSlotsPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.controls.PSSlotsControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Allows user to select slots for the template.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateSlotsPage extends Composite
{
   public PSTemplateSlotsPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      
      m_slotsControl = createSlotsControl(editor);
   }

   /**
    * Creates and initializes slot selection UI.
    */
   private PSSlotsControl createSlotsControl(PSEditorBase editor)
   {
      final PSSlotsControl slotsControl = new PSSlotsControl(this, SWT.NONE);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100, 0);
      slotsControl.setLayoutData(formData);
      
      editor.registerControl("PSSlotsControl.label.containedSlots",
            slotsControl.getSelectionControl(), null);
      return slotsControl;
   }

   /**
    * Initializes control with template data.
    */
   public void loadControlValues(PSUiAssemblyTemplate template)
   {
      m_slotsControl.loadControlValues(template);
   }

   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      m_slotsControl.updateTemplate(template);
   }
   
   /**
    * @return the slot control, never <code>null</code>.
    */
   protected PSSlotsControl getSlotsControl()
   {
      return m_slotsControl;
   }

   /**
    * Manages the char sets dropdown.
    */
   private PSSlotsControl m_slotsControl;
}
