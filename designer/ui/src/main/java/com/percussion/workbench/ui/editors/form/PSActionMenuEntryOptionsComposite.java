/******************************************************************************
*
* [ PSActionMenuEntryOptionsComposite.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionProperties;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSActionMenuGeneralCommonComposite;
import com.percussion.workbench.ui.editors.dialog.PSStyleEditorDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

public class PSActionMenuEntryOptionsComposite extends Composite
   implements IPSDesignerObjectUpdater
{

   
   /**
    * Create the composite
    * @param parent
    * @param style
    * @param  editor , cannot be <code>null</code>
    * @param page index that this control appears on.
    */
   public PSActionMenuEntryOptionsComposite(
      Composite parent, int style, PSEditorBase editor, int page)
   {
      super(parent, style);
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      setLayout(new FormLayout());
      m_editor = editor;

      m_optionsLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      m_optionsLabel.setLayoutData(formData);
      m_optionsLabel.setText(PSMessages.getString("PSActionMenuEntryOptionsComposite.options.label")); //$NON-NLS-1$

      m_separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formData_1 = new FormData();
      formData_1.top = new FormAttachment(m_optionsLabel, 5, SWT.CENTER);
      formData_1.right = new FormAttachment(100, 0);
      formData_1.left = new FormAttachment(m_optionsLabel, 5, SWT.RIGHT);
      m_separator.setLayoutData(formData_1);

      m_supportMultiselectButton = new Button(this, SWT.CHECK);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_optionsLabel, 5, SWT.BOTTOM);
      formData_2.left = new FormAttachment(m_optionsLabel, 20, SWT.LEFT);
      m_supportMultiselectButton.setLayoutData(formData_2);
      m_supportMultiselectButton.setText(PSMessages.getString(
         "PSActionMenuEntryOptionsComposite.supportMultiSelect.label")); //$NON-NLS-1$
      editor.registerControl(
         "PSActionMenuEntryOptionsComposite.supportMultiSelect.label",
         m_supportMultiselectButton,
         null,
         page);

      m_refreshHintLabel = new Label(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_supportMultiselectButton, 0, SWT.TOP);
      formData_3.left = new FormAttachment(m_supportMultiselectButton, 50, SWT.DEFAULT);
      m_refreshHintLabel.setLayoutData(formData_3);
      m_refreshHintLabel.setText(PSMessages.getString(
         "PSActionMenuEntryOptionsComposite.refreshHint.label")); //$NON-NLS-1$

      m_refreshHintComboViewer = new ComboViewer(this, SWT.READ_ONLY);
      m_refreshHintComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_refreshHintCombo = m_refreshHintComboViewer.getCombo();
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(m_refreshHintLabel, -3, SWT.TOP);
      formData_4.left = new FormAttachment(m_refreshHintLabel, 5, SWT.RIGHT);
      m_refreshHintCombo.setLayoutData(formData_4);
      List<String> hints = new ArrayList<String>(4);
      hints.add(PSMessages.getString("PSActionMenuEntryOptionsComposite.hints.none.choice")); //$NON-NLS-1$
      hints.add(PSMessages.getString("PSActionMenuEntryOptionsComposite.hints.selected.choice")); //$NON-NLS-1$
      hints.add(PSMessages.getString("PSActionMenuEntryOptionsComposite.hints.parent.choice")); //$NON-NLS-1$
      hints.add(PSMessages.getString("PSActionMenuEntryOptionsComposite.hints.root.choice")); //$NON-NLS-1$
      m_refreshHintComboViewer.setInput(hints);
      m_refreshHintCombo.select(0);
      editor.registerControl(
         "PSActionMenuEntryOptionsComposite.refreshHint.label",
         m_refreshHintCombo,
         null,
         page);

      m_launchNewWindowButton = new Button(this, SWT.CHECK);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_supportMultiselectButton, 15, SWT.BOTTOM);
      formData_5.left = new FormAttachment(m_supportMultiselectButton, 0, SWT.LEFT);
      m_launchNewWindowButton.setLayoutData(formData_5);
      m_launchNewWindowButton.setText(PSMessages.getString(
         "PSActionMenuEntryOptionsComposite.launchNewWindow.label")); //$NON-NLS-1$
      m_launchNewWindowButton.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               setStyleControlsEnabled();
            }
            
         });
      editor.registerControl(
         "PSActionMenuEntryOptionsComposite.launchNewWindow.label",
         m_launchNewWindowButton,
         null,
         page);
      
      m_separator2 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formData_6 = new FormData();
      formData_6.right = new FormAttachment(80, 0);
      formData_6.top = new FormAttachment(m_launchNewWindowButton, 0, SWT.CENTER);
      formData_6.left = new FormAttachment(m_launchNewWindowButton, 5, SWT.RIGHT);
      m_separator2.setLayoutData(formData_6);

      m_targetLabel = new Label(this, SWT.NONE);
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_launchNewWindowButton, 5, SWT.BOTTOM);
      formData_7.left = new FormAttachment(0, 45);
      m_targetLabel.setLayoutData(formData_7);
      m_targetLabel.setText(PSMessages.getString(
         "PSActionMenuEntryOptionsComposite.target.label")); //$NON-NLS-1$

      m_targetText = new Text(this, SWT.BORDER);
      m_targetText.setTextLimit(255);
      final FormData formData_8 = new FormData();
      formData_8.right = new FormAttachment(m_separator2, 0, SWT.RIGHT);
      formData_8.top = new FormAttachment(m_launchNewWindowButton, 5, SWT.BOTTOM);
      formData_8.left = new FormAttachment(m_targetLabel, 20, SWT.DEFAULT);
      m_targetText.setLayoutData(formData_8);
      editor.registerControl(
         "PSActionMenuEntryOptionsComposite.target.label",
         m_targetText,
         null,
         page);

      m_styleLabel = new Label(this, SWT.NONE);
      final FormData formData_9 = new FormData();
      formData_9.top = new FormAttachment(m_targetText, 8, SWT.DEFAULT);
      formData_9.left = new FormAttachment(m_targetLabel, 0, SWT.LEFT);
      m_styleLabel.setLayoutData(formData_9);
      m_styleLabel.setText(PSMessages.getString(
         "PSActionMenuEntryOptionsComposite.style.label")); //$NON-NLS-1$

      m_styleButton = new Button(this, SWT.NONE);
      final FormData formData_10 = new FormData();
      formData_10.height = 20;
      formData_10.width = 20;
      formData_10.right = new FormAttachment(m_targetText, 0, SWT.RIGHT);
      formData_10.top = new FormAttachment(m_styleLabel, -3, SWT.TOP);
      m_styleButton.setLayoutData(formData_10);
      m_styleButton.setText("..."); //$NON-NLS-1$
      m_styleButton.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
              onStyleButtonPressed();
            }
            
         });

      m_styleText = new Text(this, SWT.BORDER);
      m_styleText.setTextLimit(255);
      final FormData formData_11 = new FormData();
      formData_11.right = new FormAttachment(m_styleButton, -5, SWT.LEFT);
      formData_11.top = new FormAttachment(m_styleLabel, -3, SWT.TOP);
      formData_11.left = new FormAttachment(m_targetText, 0, SWT.LEFT);
      m_styleText.setLayoutData(formData_11);
      editor.registerControl(
         "PSActionMenuEntryOptionsComposite.style.label",
         m_styleText,
         null,
         page);
      
      // Set tab order
      Control[] controls = new Control[]
         {
            m_supportMultiselectButton,
            m_refreshHintCombo,
            m_launchNewWindowButton,
            m_targetText,
            m_styleText,
            m_styleButton
         };
      setTabList(controls);
      //
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSAction action = (PSAction)designObject;
      PSActionProperties props = action.getProperties();
      
      if(control == m_supportMultiselectButton)
      {
         String bool = 
            m_supportMultiselectButton.getSelection() ? "yes" : "no"; //$NON-NLS-1$ //$NON-NLS-2$
         props.setProperty(PSAction.PROP_MUTLI_SELECT, bool);
      }
      else if(control == m_refreshHintCombo)
      {
         props.setProperty(PSAction.PROP_REFRESH_HINT,
            ms_refreshHints[m_refreshHintCombo.getSelectionIndex()]);
      }
      else if(control == m_launchNewWindowButton)
      {
         String bool = 
            m_launchNewWindowButton.getSelection() ? "yes" : "no"; //$NON-NLS-1$ //$NON-NLS-2$
         props.setProperty(PSAction.PROP_LAUNCH_NEW_WND, bool);
      }
      else if(control == m_styleText)
      {
         // Property only exists if value not null or empty and
         // the launch new window button is checked.
         String text = 
            m_launchNewWindowButton.getSelection() 
            ? m_styleText.getText() : ""; //$NON-NLS-1$
         PSActionMenuGeneralCommonComposite.setActionProperty(
            action, PSAction.PROP_TARGET_STYLE, text);   
      }
      else if(control == m_targetText)
      {
         // Property only exists if value not null or empty and
         // the launch new window button is checked.
         String text = 
            m_launchNewWindowButton.getSelection() 
            ? m_targetText.getText() : ""; //$NON-NLS-1$
         PSActionMenuGeneralCommonComposite.setActionProperty(
            action, PSAction.PROP_TARGET, text);
      }
   }
   
   /**
    * Loads intial control values from the designer object
    */
   public void loadControlValues(Object designObject)
   {
      PSAction action = (PSAction)designObject;
      PSActionProperties props = action.getProperties();
      
      // set multi select
      m_supportMultiselectButton.setSelection(
         props.getProperty(PSAction.PROP_MUTLI_SELECT, "no") //$NON-NLS-1$
         .toLowerCase().equals("yes")); //$NON-NLS-1$
      // set refresh hint
      String hint = props.getProperty(PSAction.PROP_REFRESH_HINT, "None"); //$NON-NLS-1$
      for(int i = 0; i < ms_refreshHints.length; i++)
      {
         if(ms_refreshHints.equals(hint))
         {
            m_refreshHintCombo.select(i);
            break;
         }
      }
      // set launch new window
      m_launchNewWindowButton.setSelection(
         props.getProperty(PSAction.PROP_LAUNCH_NEW_WND, "no") //$NON-NLS-1$
         .toLowerCase().equals("yes")); //$NON-NLS-1$
      // set style
      m_styleText.setText(props.getProperty(PSAction.PROP_TARGET_STYLE, "")); //$NON-NLS-1$
      // set target
      m_targetText.setText(props.getProperty(PSAction.PROP_TARGET, "")); //$NON-NLS-1$
      
      setStyleControlsEnabled();
   }
   
   /**
    * Sets the style and target controls as enabled if
    * the launch new window button is selected else it
    * disables all of these controls.
    */
   private void setStyleControlsEnabled()
   {
      if(m_launchNewWindowButton.getSelection())
      {
         m_styleLabel.setEnabled(true);
         m_styleButton.setEnabled(true);
         m_styleText.setEnabled(true);
         m_targetLabel.setEnabled(true);
         m_targetText.setEnabled(true);
      }
      else
      {
         m_styleLabel.setEnabled(false);
         m_styleButton.setEnabled(false);
         m_styleText.setEnabled(false);
         m_targetLabel.setEnabled(false);
         m_targetText.setEnabled(false);
      }
   }
   
   /**
    * Launches the style dialog when the button is pressed
    */
   private void onStyleButtonPressed()
   {
      String[] args = new String[]{m_targetText.getText(),
         m_styleText.getText()};
      PSStyleEditorDialog dialog = 
         new PSStyleEditorDialog(getShell(), args);
      dialog.setBlockOnOpen(true);
      
      int status = dialog.open();
      if(status == Dialog.OK)
      {
         m_targetText.setText(args[0]);
         m_styleText.setText(args[1]);         
      }
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   private Text m_styleText;
   private Button m_styleButton;
   private Label m_styleLabel;
   private Text m_targetText;
   private Label m_targetLabel;
   private Label m_separator2;
   private Button m_launchNewWindowButton;
   private Combo m_refreshHintCombo;
   private ComboViewer m_refreshHintComboViewer;
   private Label m_refreshHintLabel;
   private Button m_supportMultiselectButton;
   private Label m_separator;
   private Label m_optionsLabel;
   
   @SuppressWarnings("unused")
   private PSEditorBase m_editor;
   
   /**
    * Refresh hint values array
    */
   private static String[] ms_refreshHints = new String[]
     {
        "None", //$NON-NLS-1$
        "Selected", //$NON-NLS-1$
        "Parent", //$NON-NLS-1$
        "Root" //$NON-NLS-1$
     };

   

}
