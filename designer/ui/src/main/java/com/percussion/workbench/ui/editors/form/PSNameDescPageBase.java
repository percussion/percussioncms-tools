/******************************************************************************
 *
 * [ PSNameDescPageBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.EDITOR_TOP_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_TENTH;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;

/**
 * Basec class for name/desc editor pages.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSNameDescPageBase extends Composite
{
   public PSNameDescPageBase(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style);
      m_editor = editor;
      setLayout(new FormLayout());
      
      m_nameLabelDesc = createNameLabelDesc(this, editor);
      
      m_splitPane = createSplitPane(this, m_nameLabelDesc);
      m_splitPane.setLayout(new FormLayout());
      
      // left column
      m_leftPane = createLeftPane(m_splitPane);
      m_leftPane.setLayout(new FormLayout());

      // right column
      m_rightPane = createRightPane(m_splitPane);
      m_rightPane.setLayout(new FormLayout());
   }

   /**
    * Creates a pane containing all the controls of the page except
    * name, label and description control.
    */
   private Composite createSplitPane(final Composite container,
         final Control previousControl)
   {
      final Composite pane = new Composite(container, SWT.NONE);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(previousControl, 0, SWT.LEFT);
      formData.right = new FormAttachment(previousControl, 0, SWT.RIGHT);
      formData.top = new FormAttachment(previousControl, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      pane.setLayoutData(formData);
      return pane;
   }

   /**
    * Creates left part of the split pane.
    */
   private Composite createLeftPane(final Composite splitPane)
   {
      final Composite leftPane = new Composite(splitPane, SWT.NONE);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      final FormData labelFormData =
            (FormData) m_nameLabelDesc.getLabelText().getLayoutData();
      formData.right = labelFormData.right;
      formData.top = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100, 0);
      leftPane.setLayoutData(formData);
      return leftPane;
   }

   /**
    * Creates right part of the split pane.
    */
   private Composite createRightPane(final Composite splitPane)
   {
      final Composite rightPane = new Composite(splitPane, SWT.NONE);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(50, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.bottom = new FormAttachment(100, 0);
      rightPane.setLayoutData(formData);
      return rightPane;
   }

   /**
    * Creates and lays out the name label description control.
    */
   private PSNameLabelDesc createNameLabelDesc(final Composite container,
         PSEditorBase editor)
   {
      final PSNameLabelDesc control =
            new PSNameLabelDesc(container, SWT.NONE,
                  getNamePrefix(), 0,
                  PSNameLabelDesc.SHOW_ALL
                  | PSNameLabelDesc.NAME_READ_ONLY
                  | PSNameLabelDesc.LAYOUT_SIDE
                  | PSNameLabelDesc.LABEL_USES_NAME_PREFIX,
                  editor);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, EDITOR_TOP_OFFSET);
      control.setLayoutData(formData);
      return control;
   }

   /**
    * Creates label in the top left corner of the container.
    */
   protected Label createTopLabel(final Composite container, String labelText)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(labelText + ':');
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.top =
            new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      label.setLayoutData(formData);
      return label;
   }

   /**
    * Creates a combo with a label in the top left corner of the container.
    */
   protected Combo createTopComboWithLabel(Composite container, Label label)
   {
      final Combo combo = new Combo(container, SWT.READ_ONLY);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(label, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData.top = new FormAttachment(0);
      formData.right = new FormAttachment(100 - ONE_TENTH);
      combo.setLayoutData(formData);
      m_editor.registerControl(label.getText(), combo, null);
      return combo;
   }

   /**
    * Name prefix for the name label
    * (passed as a parameter to {@link PSNameLabelDesc}.
    */
   protected abstract String getNamePrefix();

   /**
    * Generates form data to attach a control under the provided control.
    * @param verticalOffset offset from the upper control.
    */
   protected FormData attachToUpperFormData(Control previousControl, int verticalOffset)
   {
      final FormData formData = new FormData();
      formData.left = new FormAttachment(previousControl, 0, SWT.LEFT);
      formData.right = new FormAttachment(previousControl, 0, SWT.RIGHT);
      formData.top = new FormAttachment(previousControl, verticalOffset, SWT.BOTTOM);
      return formData;
   }

   /**
    * Convenience method to get string resource.
    */
   protected static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * Name, label control.
    */
   final PSNameLabelDesc m_nameLabelDesc;
   
   /**
    * Pane containing all the controls of the page except
    * name, label and description control.
    */
   final protected Composite m_splitPane;
   
   /**
    * Left part of the split pane
    */
   final protected Composite m_leftPane;

   /**
    * Right part of the split pane
    */
   final protected Composite m_rightPane;
   
   /**
    * Editor associated with the page.
    */
   protected final PSEditorBase m_editor;
}
