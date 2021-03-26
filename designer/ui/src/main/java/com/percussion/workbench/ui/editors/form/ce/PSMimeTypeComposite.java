/******************************************************************************
 *
 * [ PSMimeTypeComposite.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.content.PSContentFactory;
import com.percussion.design.objectstore.PSField;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A common composite to handle the mime type mode and mime type value of
 * fields.
 * 
 * @author bjoginipally
 * 
 */
public class PSMimeTypeComposite extends Composite implements IPSUiConstants,
      SelectionListener
{
   public PSMimeTypeComposite(Composite parent, int style, boolean isMainEditor)
   {
      super(parent, style);
      m_parent = parent;
      m_isMainEditor = isMainEditor;
      setLayout(new FormLayout());

      m_mimeTypeModeLabel = new Label(this, SWT.WRAP);
      m_mimeTypeModeLabel.setText(PSMessages
            .getString("PSMimeTypeComposite.label.mimeTypeMode"));

      m_mimeTypeModeCombo = new Combo(this, SWT.READ_ONLY);
      m_mimeTypeModeCombo.addSelectionListener(this);
      for (PSField.PSMimeTypeModeEnum mode : PSField.PSMimeTypeModeEnum
            .values())
      {
         m_mimeTypeModeCombo.add(mode.getDisplayName());
      }

      m_mimeTypeValueLabel = new Label(this, SWT.WRAP);
      m_mimeTypeValueLabel.setText(PSMessages
            .getString("PSMimeTypeComposite.label.mimeTypeValue"));

      m_mimeTypeValueCombo = new Combo(this, SWT.READ_ONLY);
      m_mimeTypeValueCombo.addSelectionListener(this);
      //Add teh layout data
      addLayoutData();
   }
   
   /**
    * Adds the layout data to the controls based on the place it is being used.
    * Must be called after creating all the controls.
    */
   private void addLayoutData()
   {
      FormData mmLabelFd = new FormData();
      FormData mmComboFd = new FormData();
      FormData mvLabelFd = new FormData();
      FormData mvComboFd = new FormData();
      if (m_isMainEditor)
      {
         mmLabelFd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         mmLabelFd.left = new FormAttachment(0, 0);

         mmComboFd.top = new FormAttachment(m_mimeTypeModeLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         mmComboFd.left = new FormAttachment(m_mimeTypeModeLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         mmComboFd.right = new FormAttachment(50,0);

         mvLabelFd.top = new FormAttachment(m_mimeTypeModeLabel, 0, SWT.TOP);
         mvLabelFd.left = new FormAttachment(m_mimeTypeModeCombo, 0, SWT.RIGHT);
         mvLabelFd.right = new FormAttachment(75,0);
         
         mvComboFd.top = new FormAttachment(m_mimeTypeValueLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         mvComboFd.left = new FormAttachment(m_mimeTypeValueLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         mvComboFd.right = new FormAttachment(100,0);
         m_mimeTypeValueLabel.setAlignment(SWT.RIGHT);
      }
      else
      {
         mmLabelFd.top = new FormAttachment(0,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
         mmLabelFd.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
         mmLabelFd.left = new FormAttachment(0,10);

         mmComboFd.top = new FormAttachment(m_mimeTypeModeLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         mmComboFd.left = new FormAttachment(m_mimeTypeModeLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         mmComboFd.right = new FormAttachment(100, -10);

         mvLabelFd.top = new FormAttachment(m_mimeTypeModeLabel,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         mvLabelFd.left = new FormAttachment(m_mimeTypeModeLabel, 0, SWT.LEFT);
         mvLabelFd.right = new FormAttachment(m_mimeTypeModeLabel,0,SWT.RIGHT);

         mvComboFd.top = new FormAttachment(m_mimeTypeValueLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         mvComboFd.left = new FormAttachment(m_mimeTypeValueLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         mvComboFd.right = new FormAttachment(m_mimeTypeModeCombo, 0, SWT.RIGHT);
         m_mimeTypeModeLabel.setAlignment(SWT.RIGHT);
         m_mimeTypeValueLabel.setAlignment(SWT.RIGHT);
      }
      m_mimeTypeModeLabel.setLayoutData(mmLabelFd);
      m_mimeTypeModeCombo.setLayoutData(mmComboFd);
      m_mimeTypeValueLabel.setLayoutData(mvLabelFd);
      m_mimeTypeValueCombo.setLayoutData(mvComboFd);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(SelectionEvent e)
   {}

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_mimeTypeModeCombo)
      {
         populateTypeValues();
         updateData();
      }
      else if (e.getSource() == m_mimeTypeValueCombo)
      {
         updateData();
      }
   }

   /**
    * Updates the underlying PSField object with the current selections.  This
    * will also dirty the main editor if the parent composite of this
    * composite's parent is an instance of {@link PSFieldPropertiesComposite}.
    */
   private void updateData()
   {
      PSField.PSMimeTypeModeEnum mode = PSField.PSMimeTypeModeEnum
            .getModeEnum(m_mimeTypeModeCombo.getText());
      if (mode != null && !mode.equals(PSField.PSMimeTypeModeEnum.DEFAULT))
      {
         m_field.setMimeType(mode, m_mimeTypeValueCombo.getText());
      }
      else
      {
         m_field.clearMimeTypeProperty();
      }
      
      if (m_parent != null)
      {
         Composite parent = m_parent.getParent();
         if (parent != null && parent instanceof PSFieldPropertiesComposite)
         {
            ((PSFieldPropertiesComposite) parent).getEditor().setDirty();
         }
      }
   }

   /**
    * Convenient method to populate the values of mime type value combo.
    */
   private void populateTypeValues()
   {
      PSField.PSMimeTypeModeEnum mode = PSField.PSMimeTypeModeEnum
            .getModeEnum(m_mimeTypeModeCombo.getText());
      if (mode.equals(PSField.PSMimeTypeModeEnum.FROM_MIMETYPE_FIELD))
      {
         m_mimeTypeValueCombo.setItems(m_fieldNames);
         // Guess the default field
         int index = ArrayUtils.indexOf(m_fieldNames, m_field.getSubmitName()
               + "_type");
         index = index <0?0:index;
         m_mimeTypeValueCombo.select(index);
         m_mimeTypeValueCombo.setEnabled(true);
      }
      else if (mode.equals(PSField.PSMimeTypeModeEnum.FROM_EXT_FIELD))
      {
         m_mimeTypeValueCombo.setItems(m_fieldNames);
         // Guess the default field
         int index = ArrayUtils.indexOf(m_fieldNames, m_field.getSubmitName()
               + "_ext");
         index = index <0?0:index;
         m_mimeTypeValueCombo.select(index);
         m_mimeTypeValueCombo.setEnabled(true);
      }
      else if (mode.equals(PSField.PSMimeTypeModeEnum.FROM_SELECTION))
      {
         m_mimeTypeValueCombo.setItems(m_mimeTypes);
         m_mimeTypeValueCombo.select(ArrayUtils.indexOf(m_mimeTypes,
               DEFAULT_MIME_TYPE_VALUE));
         m_mimeTypeValueCombo.setEnabled(true);
      }
      else
      {
         String[] empty = {};
         m_mimeTypeValueCombo.setItems(empty);
         m_mimeTypeValueCombo.setEnabled(false);
      }
   }

   /**
    * Sets the values of this composite from the field object. Calls the
    * convenient method {@link #setValues(String, String, String[])} to set the
    * values.
    * 
    * @param field The filed object must not be <code>null</code>.
    * @param fieldNames Array of available field names, must not be
    * <code>null</code>.
    */
   public void setValues(PSField field, String[] fieldNames)
   {
      if (field == null)
         throw new IllegalArgumentException("field must not be null");
      if (fieldNames == null)
         throw new IllegalArgumentException("fieldNames must not be null");
      m_field = field;
      m_fieldNames = fieldNames;
      String mimevalue = StringUtils.defaultString(field.getMimeTypeValue());
      PSField.PSMimeTypeModeEnum mode = field.getMimeTypeMode();
      if(mode == null)
      {
         mode = PSField.PSMimeTypeModeEnum.DEFAULT;
      }
      m_mimeTypeModeCombo.select(ArrayUtils.indexOf(m_mimeTypeModeCombo
            .getItems(), mode.getDisplayName()));
      populateTypeValues();
      int index = ArrayUtils.indexOf(m_mimeTypeValueCombo.getItems(), mimevalue);
      index = index==-1 ? 0 : index;
      m_mimeTypeValueCombo.select(index);
   }
   
   /**
    * Gets the mime type mode associtaed with this composite
    * 
    * @return the mime type mode never <code>null</code> or empty.
    */
   public String getMimeTypeMode()
   {
      return "" + m_mimeTypeModeCombo.getSelectionIndex();
   }

   /**
    * Gets the mime type value associtaed with this composite
    * 
    * @return the mime type value never <code>null</code> or empty.
    */
   public String getMimeTypeValue()
   {
      return m_mimeTypeValueCombo.getText();
   }

   /**
    * The object of the PSField whose mime type mode and mime type value are
    * represented by this composite. Initialized in
    * {@link #setValues(PSField, String[])} method.
    */
   private PSField m_field = null;

   /**
    * Variable to hold the array of the supported mimetypes.
    */
   private String[] m_mimeTypes = PSContentFactory.getSupportedMimeTypes();
   
   /**
    * Variable to hold the array of the field names, Initialized in
    * {@link #setValues(PSField, String[])} method.
    */
   private String[] m_fieldNames;

   /**
    * The mimetype mode combo box
    */
   private Combo m_mimeTypeModeCombo;

   /**
    * The mimetype value combo box
    */
   private Combo m_mimeTypeValueCombo;

   /**
    * Label for mimetype mode combo initialized in ctor never <code>null</code>
    * after that.
    */
   private Label m_mimeTypeModeLabel;

   /**
    * Label for mimetype value combo initialized in ctor never <code>null</code>
    * after that.
    */
   private Label m_mimeTypeValueLabel;

   /**
    * Default value for mime type value for from selected mode
    */
   public static final String DEFAULT_MIME_TYPE_VALUE = "text/plain";
   
   /**
    * Default value for mime type mode
    */
   public static final String DEFAULT_MIME_TYPE_MODE = "0";
   
   /**
    * Flag to indicate whether this compiste is placed on main editor or 
    * field properties dialog.
    */
   private boolean m_isMainEditor;

   /**
    * Local reference to the parent composite, initalized in ctor, may be
    * <code>null</code>.
    */
   private Composite m_parent;

}
