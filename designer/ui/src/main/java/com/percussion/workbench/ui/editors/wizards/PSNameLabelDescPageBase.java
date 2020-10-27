/******************************************************************************
 *
 * [ PSNameLabelDescPageBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_FORTH;
import static com.percussion.workbench.ui.IPSUiConstants.WIZARD_TOP_OFFSET;

/**
 * Base for wizard pages showing name, label, description component at the top
 * of the page.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSNameLabelDescPageBase extends PSWizardPageBase
{

   /**
    * Just forwards to the parent constructor.
    */
   public PSNameLabelDescPageBase(String pageName, String title,
      ImageDescriptor image)
   {
      super(pageName, title, image);
   }

   public final void createControl(Composite parent)
   {
      final Composite container = initContainer(parent);
      
      m_nameLabelDesc = createNameLabelDesc(container);
      fillUpContainer(container);
   }

   /**
    * Completes control creation by filling up rest of the page container.
    * The provided container is already padded between borders. 
    */
   protected abstract void fillUpContainer(Composite container);

   /**
    * Creates and lays out container for all the controls, inserts it into the
    * page.
    * The returned composite is already intended on all the borders.
    */
   private Composite initContainer(final Composite parent)
   {
      final Composite pageContainer = new Composite(parent, SWT.NULL);
      pageContainer.setLayout(new FormLayout());
      setControl(pageContainer);

      final Composite container = new Composite(pageContainer, SWT.NULL);
      container.setLayout(new FormLayout());

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100);
      container.setLayoutData(formData);
      return container;
   }

   /**
    * Creates and lays out the name label description control.
    */
   private PSNameLabelDesc createNameLabelDesc(final Composite container)
   {
      final PSNameLabelDesc control =
            new PSNameLabelDesc(container, SWT.NONE,
                  getNamePrefix(), LABEL_NUM, //$NON-NLS-1$
                  getNameDescOptions(), this);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      control.setLayoutData(formData);
      return control;
   }

   /**
    * Creates a label aligned in the same way as name/desc labels.
    * Helper method for descendants to layout labels.
    * @param container the label parent control.
    * @param labelText text to assign to label (except the semicolumn).
    * @param topAttachment how to attach top of the control.
    */
   protected Label createAlignedLabel(final Composite container,
         final String labelText, final FormAttachment topAttachment)
   {
      final Label label = new Label(container, SWT.RIGHT);
      label.setText(labelText + ':');
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(LABEL_NUM);
      formData.top = topAttachment;
      label.setLayoutData(formData);
      return label;
   }
   /**
    * Options passed to the {@link PSNameLabelDesc} control during creation.
    * Default implementation returns {@link PSNameLabelDesc#SHOW_ALL} option. 
    */
   protected int getNameDescOptions()
   {
      return PSNameLabelDesc.SHOW_ALL;
   }
   
   /**
    * Name prefix for the name/description control.
    */
   protected abstract String getNamePrefix();

   /**
    * Label numerator for the page labels.
    */
   protected static final int LABEL_NUM = ONE_FORTH;

   /**
    * Name, label, description control.
    */
   protected PSNameLabelDesc m_nameLabelDesc;
}
