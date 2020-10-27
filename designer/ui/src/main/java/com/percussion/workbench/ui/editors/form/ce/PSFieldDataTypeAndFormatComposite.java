/******************************************************************************
 *
 * [ PSFieldDataTypeAndFormatComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This composite renders the field datatype and data format combo boxes. This
 * is a common composite for both main editor and properties dialog. After
 * creating users of this class needs to call
 * {@link #setDataTypeAndFormat(String, String, boolean)} to set the data. For
 * system or shared field inside local def the controls needs to be disabled by
 * calling {@link #setDataTypeAndFormatEnabled(boolean)}.
 */
public class PSFieldDataTypeAndFormatComposite extends Composite
      implements
         IPSUiConstants, SelectionListener
{
   /**
    * Ctor, creates the controls and call {@link #addLayoutData()} to set the
    * layout.
    * 
    * @param parent The parent composite for this composite.
    * @param style The SWT style for this component
    * @param option A flag to indicate whether this composite is going to be
    *           rendered on main editor or field properties dialog.
    */
   public PSFieldDataTypeAndFormatComposite(Composite parent, int style,
         boolean isMainEditor)
   {
      super(parent, style);
      m_isMainEditor = isMainEditor;
      setLayout(new FormLayout());
      
      m_dataTypeLabel = new Label(this, SWT.WRAP);
      m_dataTypeLabel.setText(PSMessages.getString(
            "PSFieldDataTypeAndFormatComposite.label.datatype"));

      m_dataTypeCombo = new Combo(this, SWT.READ_ONLY);
      m_dataTypeCombo.setItems((String[]) PSUIField.getValidDataTypes()
            .toArray(new String[PSUIField.getValidDataTypes().size()]));
      m_dataTypeCombo.addSelectionListener(this);
      
      m_dataFormatLabel = new Label(this, SWT.WRAP);
      m_dataFormatLabel.setText(PSMessages.getString(
            "PSFieldDataTypeAndFormatComposite.label.storagesize"));

      m_dataFormatCombo = new Combo(this, SWT.NONE);
      m_dataFormatCombo.addSelectionListener(this);
      m_dataFormatCombo.addFocusListener(new FocusAdapter()
      {
         public void focusLost(FocusEvent e)
         {
            handleDataFormatChanges();
         }
      });
      //Add teh layout data
      addLayoutData();
   }

   /**
    * Adds the layout data to the controls based on the place it is being used.
    * Must be called after creating all the controls.
    */
   private void addLayoutData()
   {
      FormData dtLabelFd = new FormData();
      FormData dtComboFd = new FormData();
      FormData dfLabelFd = new FormData();
      FormData dfComboFd = new FormData();
      if (m_isMainEditor)
      {
         dtLabelFd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         dtLabelFd.left = new FormAttachment(0, 0);

         dtComboFd.top = new FormAttachment(m_dataTypeLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         dtComboFd.left = new FormAttachment(m_dataTypeLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         dtComboFd.right = new FormAttachment(50,0);

         dfLabelFd.top = new FormAttachment(m_dataTypeLabel, 0, SWT.TOP);
         dfLabelFd.left = new FormAttachment(m_dataTypeCombo, 0, SWT.RIGHT);
         dfLabelFd.right = new FormAttachment(80,0);
         
         dfComboFd.top = new FormAttachment(m_dataFormatLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         dfComboFd.left = new FormAttachment(m_dataFormatLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         dfComboFd.right = new FormAttachment(100,0);
         m_dataFormatLabel.setAlignment(SWT.RIGHT);
      }
      else
      {
         dtLabelFd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         dtLabelFd.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
         dtLabelFd.left = new FormAttachment(0,10);

         dtComboFd.top = new FormAttachment(m_dataTypeLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         dtComboFd.left = new FormAttachment(m_dataTypeLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         dtComboFd.right = new FormAttachment(100, -10);

         dfLabelFd.top = new FormAttachment(m_dataTypeLabel,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         dfLabelFd.left = new FormAttachment(m_dataTypeLabel, 0, SWT.LEFT);
         dfLabelFd.right = new FormAttachment(m_dataTypeLabel,0,SWT.RIGHT);

         dfComboFd.top = new FormAttachment(m_dataFormatLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         dfComboFd.left = new FormAttachment(m_dataFormatLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         dfComboFd.right = new FormAttachment(m_dataTypeCombo, 0, SWT.RIGHT);
         m_dataTypeLabel.setAlignment(SWT.RIGHT);
         m_dataFormatLabel.setAlignment(SWT.RIGHT);
      }
      m_dataTypeLabel.setLayoutData(dtLabelFd);
      m_dataTypeCombo.setLayoutData(dtComboFd);
      m_dataFormatLabel.setLayoutData(dfLabelFd);
      m_dataFormatCombo.setLayoutData(dfComboFd);
   }
   
   /**
    * Enables or disables the data type and data format combo boxes. For system
    * fields and shared fields in local defs we do not allow data type or format
    * changes.
    * 
    * @param enable If <code>true</code>, data type and format combo boxes
    *           are enabeld otherwise disabled.
    */
   protected void setDataTypeAndFormatEnabled(boolean enable)
   {
      m_dataTypeCombo.setEnabled(enable);
      m_dataFormatCombo.setEnabled(enable);
   }
   

   /**
    * Sets the data type, format values and saved flag.
    * 
    * @param dType The data type of the field. Must not be <code>null</code>.
    *           If it is not valid data type then sets data type and data format
    *           to empty.
    * @param dFormat The data format of the field.
    * @param isSaved The flag to indicate whether the field is saved or not.
    */
   protected void setDataTypeAndFormat(String dType, String dFormat,
         boolean isSaved)
   {
      if (dType == null)
         throw new IllegalArgumentException("dType must not be null");
      m_saved = isSaved;

      if (PSUIField.isValidDataType(dType))
      {
         m_dataTypeCombo.setText(dType);
         String[] dfs = (String[]) PSUIField.getDefinedDataTypeFormats(
               m_dataTypeCombo.getText()).toArray(new String[0]);

         m_dataFormatCombo.setItems(dfs);

         if (dFormat != null && PSUIField.isValidDataTypeFormat(dType, dFormat))
         {
            m_dataFormatCombo.setText(dFormat);
         }
         else
         {
            m_dataFormatCombo.setEnabled(false);
            m_dataFormatCombo.setText("none");
         }

      }
      else
      {
         m_dataTypeCombo.setText(StringUtils.EMPTY);
         m_dataFormatCombo.setText(StringUtils.EMPTY);
      }

      m_dataType = m_dataTypeCombo.getText();
      m_dataFormat = m_dataFormatCombo.getText();
   }
   
   /**
    * Gets the data type. May be empty but never <code>null</code>.
    * @return The data type.
    */
   public String getDataType()
   {
      return m_dataType;
   }
   
   /**
    * Gets the data format to be set on field. May be <code>null</code>.
    * @return The data format.
    */
   public String getDataFormat()
   {
      return m_dataFormat == null || m_dataFormat.equals("none")?
            null : m_dataFormat;
   }
   
   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_dataTypeCombo)
      {
         String dt = m_dataTypeCombo.getText();
         if (dt.equals(m_dataType))
            return;
         // If the field has a data base column associated with it
         // Warn the user about the possible loss of data
         if (m_saved
               && !(MessageDialog.openConfirm(getShell(),PSMessages.getString(
                     "PSFieldDataTypeAndFormatComposite.error.possibledataloss.title"), 
                     PSMessages.getString(
                           "PSFieldDataTypeAndFormatComposite.error.possibledataloss.datatype.message")))) 
         {
            m_dataTypeCombo.select(PSContentEditorDefinition.getItemIndex(
                  m_dataTypeCombo.getItems(), m_dataType));
            return;
         }
         m_dataType = dt;
         resetDataFormat(dt);
      }
      else if (e.getSource() == m_dataFormatCombo)
      {
         handleDataFormatChanges();
      }
   }

   /**
    * Handles the field data format. Checks for the validity of the data format,
    * if not valid and resets the value. If the dataformat equals to none then
    * sets the data format to <code>null</code> If the data format change
    * results in possible data loss, shows a confirm message to proceed further.
    */
   private void handleDataFormatChanges()
   {
      String df = m_dataFormatCombo.getText();
      String errorTitle = PSMessages.getString(
            "PSFieldDataTypeAndFormatComposite.error.invalidstoragesize.title");
      if (StringUtils.isEmpty(df))
      {
         MessageDialog.openError(getShell(),errorTitle,
               PSMessages.getString(
                     "PSFieldDataTypeAndFormatComposite.error.invalidstoragesizeempty.message"));
         m_dataTypeCombo.setText(m_dataFormat);
         m_dataFormatCombo.setFocus();
         return;
      }
      else if (!PSUIField.isValidDataTypeFormat(m_dataType, df))
      {
         MessageDialog.openError(getShell(),errorTitle,PSMessages.getString(
                     "PSFieldDataTypeAndFormatComposite.error.invalidstoragesizenumber.title"));
         m_dataFormatCombo.setText(m_dataFormat);
         m_dataFormatCombo.setFocus();
      }
      else
      {
         if (df.equals(m_dataFormat))
            return;
         if (m_saved && !isFormatChangeSupported(m_dataFormat, df))
         {
            if (!(MessageDialog.openConfirm(getShell(),PSMessages.getString(
                  "PSFieldDataTypeAndFormatComposite.error.possibledataloss.title"),
                  PSMessages.getString(
                        "PSFieldDataTypeAndFormatComposite.error.possibledataloss.dataformat.message"))))
            {
               m_dataFormatCombo.setText(m_dataFormat);
               return;
            }
         }
         m_dataFormat = df;
      }
   }
   
   /**
    * Method to check whether the supplied data type format is supported or not.
    * 
    * @param oldFormat String old format to compare with.
    * @param newFormat String new format to check.
    * @return <code>true</code>, if the format change is supported, otherwise
    *         <code>false</code>.
    */
   private boolean isFormatChangeSupported(String oldFormat, String newFormat)
   {
      // If old and new formats are same it is OK
      if (oldFormat.equals(newFormat))
         return true;
      // If the new format is max then we are coming from low to high it is OK
      if (newFormat.equalsIgnoreCase("max")) 
         return true;
      // If the old format is max then we are going from max to a number it is
      // not OK
      else if (oldFormat.equalsIgnoreCase("max")) 
         return false;
      // Both formats are numbers if old is greater than new it is not OK
      else if (Integer.parseInt(oldFormat) > Integer.parseInt(newFormat))
         return false;

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(SelectionEvent e)
   {
   }

   /**
    * Resets the data format when the data type changes.
    * @param dataType A valid data type is assumed.
    */
   private void resetDataFormat(String dataType)
   {
      // Reset the data type format
      String defFormat = PSUIField.getDefaultFormat(dataType);
      m_dataFormat = defFormat;
      
      String[] dfs = (String[]) PSUIField.getDefinedDataTypeFormats(dataType)
            .toArray(new String[0]);
      m_dataFormatCombo.removeAll();
      if (dfs.length == 0)
      {
         m_dataFormatCombo.setEnabled(false);
      }
      else
      {
         m_dataFormatCombo.setEnabled(true);
         m_dataFormatCombo.setItems(dfs);
      }
      if (defFormat == null)
         m_dataFormatCombo.setText("none"); 
      else
         m_dataFormatCombo.setText(defFormat);
   }
   
   /**
    * Gets the data type combo box control.
    * @return data type combo box. Never <code>null</code>.
    */
   public Combo getDataTypeCombo()
   {
      return m_dataTypeCombo;
   }
   
   /**
    * Gets the data format combo box control. 
    * @return data format combo box. Never <code>null</code>.
    */
   public Combo getDataFormatCombo()
   {
      return m_dataFormatCombo;
   }
   
   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   protected void checkSubclass()
   {
   }

   /**
    * Label for data type combo initialized in ctor never <code>null</code>
    * after that.
    */
   private Label m_dataTypeLabel;

   /**
    * Label for data format combo initialized in ctor never <code>null</code>
    * after that.
    */
   private Label m_dataFormatLabel;

   /**
    * Data type combo box initialized in ctor never <code>null</code> after
    * that.
    */
   private Combo m_dataTypeCombo;

   /**
    * Data format combo box initialized in ctor never <code>null</code> after
    * that.
    */
   private Combo m_dataFormatCombo;

   /**
    * Flag to indicate whether this compiste is placed on main editor or 
    * field properties dialog.
    */
   private boolean m_isMainEditor;

   /**
    * Flag to indicate whether the field is saved or not. Must be reset when the
    * field gets saved. If it is <code>true</code> user is warned about the
    * possible data loss during the data type and format changes.
    */
   private boolean m_saved;
   
   /**
    * The data type of the field. Initialized in
    * {@link #setDataTypeAndFormat(String, String, boolean)} never null after
    * that.
    */
   private String m_dataType;
   
   /**
    * The data format of the field. Initialized in
    * {@link #setDataTypeAndFormat(String, String, boolean)} never null after
    * that.
    */
   private String m_dataFormat;
}
