/******************************************************************************
 *
 * [ PSActionMenuGeneralCommonComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionProperties;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class PSActionMenuGeneralCommonComposite extends Composite
   implements IPSUiConstants, IPSDesignerObjectUpdater
{
   
   /**
    * Create the composite
    * @param parent
    * @param style
    * @param editor
    */
   public PSActionMenuGeneralCommonComposite(
      Composite parent, int style, PSEditorBase editor, int page)
   {
      super(parent, style);
      
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null.");
      
      setLayout(new FormLayout());

      //Composite comp = new Composite(this, SWT.NONE);
      
      m_acceleratorLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      m_acceleratorLabel.setLayoutData(formData);
      m_acceleratorLabel.setText(PSMessages.getString(
         "PSMenuActionGeneralCommonComposite.accelerator.label")); //$NON-NLS-1$

      m_acceleratorText = new Text(this, SWT.BORDER);
      m_acceleratorText.setTextLimit(255);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(30, 0);
      formData_1.top = new FormAttachment(m_acceleratorLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_acceleratorLabel, 0, SWT.LEFT);
      m_acceleratorText.setLayoutData(formData_1);
      editor.registerControl(
         "PSMenuActionGeneralCommonComposite.accelerator.label",
         m_acceleratorText,
         null,
         page);

      m_mnemonicLabel = new Label(this, SWT.NONE);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_acceleratorLabel, 0, SWT.TOP);
      formData_2.left = new FormAttachment(m_acceleratorText, 20, SWT.RIGHT);
      m_mnemonicLabel.setLayoutData(formData_2);
      m_mnemonicLabel.setText(PSMessages.getString(
         "PSMenuActionGeneralCommonComposite.mnemonic.label")); //$NON-NLS-1$

      m_mnemonicText = new Text(this, SWT.BORDER);
      m_mnemonicText.setTextLimit(1);
      final FormData formData_3 = new FormData();
      formData_3.width = 15;
      formData_3.top = new FormAttachment(m_mnemonicLabel, 0, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_mnemonicLabel, 0, SWT.LEFT);
      m_mnemonicText.setLayoutData(formData_3);
      editor.registerControl(
         "PSMenuActionGeneralCommonComposite.mnemonic.label",
         m_mnemonicText,
         null,
         page);
      
      m_tooltipLabel = new Label(this, SWT.NONE);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_mnemonicLabel, 0, SWT.TOP);
      formData_4.left = new FormAttachment(50, 0);
      m_tooltipLabel.setLayoutData(formData_4);
      m_tooltipLabel.setText(PSMessages.getString(
         "PSMenuActionGeneralCommonComposite.tooltip.label")); //$NON-NLS-1$

      m_tooltipText = new Text(this, SWT.BORDER);
      m_tooltipText.setTextLimit(255);
      final FormData formData_5 = new FormData();
      formData_5.bottom = new FormAttachment(m_mnemonicText, 0, SWT.BOTTOM);
      formData_5.right = new FormAttachment(100, 0);
      formData_5.top = new FormAttachment(m_tooltipLabel, 0, SWT.BOTTOM);
      formData_5.left = new FormAttachment(m_tooltipLabel, 0, SWT.LEFT);
      m_tooltipText.setLayoutData(formData_5);      
      editor.registerControl(
         "PSMenuActionGeneralCommonComposite.tooltip.label",
         m_tooltipText,
         null,
         page);
      
      m_sortRankLabel = new Label(this, SWT.NONE);
      m_sortRankLabel.setText(PSMessages.getString(
      "PSMenuActionGeneralCommonComposite.sortrank.label")); //$NON-NLS-1$
      m_sortRankSpinner = new Spinner(this, SWT.BORDER);
      m_sortRankSpinner.setMinimum(1);
      m_sortRankSpinner.setMaximum(999);
      editor.registerControl(
         "PSMenuActionGeneralCommonComposite.sortrank.label",
         m_sortRankSpinner,
         null,
         page);
      
      m_iconPathLabel = new Label(this, SWT.NONE);
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_tooltipText, 10, SWT.BOTTOM);
      formData_8.left = new FormAttachment(m_tooltipText, 0, SWT.LEFT);
      m_iconPathLabel.setLayoutData(formData_8);
      m_iconPathLabel.setText(PSMessages.getString(
         "PSMenuActionGeneralCommonComposite.iconPath.label")); //$NON-NLS-1$

      m_iconPathText = new Text(this, SWT.BORDER);
      m_iconPathText.setTextLimit(255);
      final FormData formData_9 = new FormData();
      formData_9.right = new FormAttachment(100, 0);
      formData_9.top = new FormAttachment(m_iconPathLabel, 0, SWT.BOTTOM);
      formData_9.left = new FormAttachment(m_iconPathLabel, 0, SWT.LEFT);
      m_iconPathText.setLayoutData(formData_9);
      editor.registerControl(
         "PSMenuActionGeneralCommonComposite.iconPath.label",
         m_iconPathText,
         null,
         page);
      
     // Layout sort rank label and spinner
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(m_iconPathText,
          LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_6.left = new FormAttachment(m_acceleratorText, 0, SWT.LEFT);
      m_sortRankLabel.setLayoutData(formData_6);
      

      
      final FormData formData_7 = new FormData();
      formData_7.bottom = new FormAttachment(m_iconPathText, 0, SWT.BOTTOM);
      formData_7.left = new FormAttachment(m_sortRankLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_sortRankSpinner.setLayoutData(formData_7);
      
      //
   }   
     
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSAction action = (PSAction)designObject;
      
      if(control == m_acceleratorText)
      {
         setActionProperty(
            action, PSAction.PROP_ACCEL_KEY, m_acceleratorText.getText());
      }
      else if(control == m_mnemonicText)
      {
         setActionProperty(
            action, PSAction.PROP_MNEM_KEY, m_mnemonicText.getText());
      }
      else if(control == m_tooltipText)
      {
         setActionProperty(
            action, PSAction.PROP_SHORT_DESC, m_tooltipText.getText());
      }
      else if(control == m_sortRankSpinner)
      {
         action.setSortRank(m_sortRankSpinner.getSelection());
      }
      else if(control == m_iconPathText)
      {
         setActionProperty(
            action, PSAction.PROP_SMALL_ICON, m_iconPathText.getText());
      }
      
   }
   
   /**
    * Loads the intial control values.
    * @param designObject the <code>PSAction</code> object that contains
    * the values, cannot be <code>null</code>.
    */
   public void loadControlValues(Object designObject)
   {
      PSAction action = (PSAction)designObject;
      if(action == null)
         throw new IllegalArgumentException("action cannot be null.");
      PSActionProperties props = action.getProperties();
      
      // set accelerator
      m_acceleratorText.setText(
         StringUtils.defaultString(
            props.getProperty(PSAction.PROP_ACCEL_KEY)));
      // set mnemonic
      m_mnemonicText.setText(
         StringUtils.defaultString(
            props.getProperty(PSAction.PROP_MNEM_KEY)));
      // set tootip
      m_tooltipText.setText(
         StringUtils.defaultString(
            props.getProperty(PSAction.PROP_SHORT_DESC)));
      // set sort rank
      m_sortRankSpinner.setSelection(action.getSortRank());
      // set icon path
      m_iconPathText.setText(
         StringUtils.defaultString(
            props.getProperty(PSAction.PROP_SMALL_ICON)));
   }
   
   /**
    * Helper method to set an action property. If the passed in value is
    * <code>null</code> or empty then the property is removed from the
    * action.
    * @param action the action to add the property to, cannot be 
    * <code>null</code>.
    * @param key the property key, cannot be <code>null</code> or
    * empty.
    * @param value the value to be set, may be <code>null</code> or
    * empty.
    */
   public static void setActionProperty(PSAction action, String key, String value)
   {
      if(action == null)
         throw new IllegalArgumentException("action cannot be null.");
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      PSActionProperties props = action.getProperties();
      if(StringUtils.isBlank(value))
      {
         if(props.getProperty(key) != null)
         {
            props.removeProperty(key);
         }
      }
      else
      {
         props.setProperty(key, value);
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
   
   //Controls
   private Text m_iconPathText;
   private Label m_iconPathLabel;
   private Spinner m_sortRankSpinner;
   private Label m_sortRankLabel;
   private Text m_tooltipText;
   private Label m_tooltipLabel;
   private Text m_mnemonicText;
   private Label m_mnemonicLabel;
   private Text m_acceleratorText;
   private Label m_acceleratorLabel;
   

}
