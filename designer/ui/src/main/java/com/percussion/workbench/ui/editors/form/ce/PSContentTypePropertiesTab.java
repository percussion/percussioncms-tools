/******************************************************************************
 *
 * [ PSContentTypePropertiesTab.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSContentTypeEditor;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValueRequiredValidator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the properties tab for content editor.
 */
public class PSContentTypePropertiesTab extends Composite
      implements
         IPSUiConstants,
         SelectionListener,
         ISelectionChangedListener,
         ICheckStateListener,
         IPSDesignerObjectUpdater
{

   /**
    * Creates the controls for thsi page.
    * @param parent The parent composite of this composite.
    * @param style SWT style for this tab
    * @param editor the base editor for this tab.
    */
   public PSContentTypePropertiesTab(Composite parent, int style,
         PSEditorBase editor) {
      super(parent, style);
      m_editor = (PSContentTypeEditor) editor;
      setLayout(new FormLayout());
      // Add the comon composite that contains name, label, description
      // fields
      m_commonComp = new PSNameLabelDesc(this, SWT.NONE, PSMessages.getString("PSContentTypePropertiesTab.label.contenttype"), //$NON-NLS-1$
            EDITOR_LABEL_NUMERATOR, PSNameLabelDesc.SHOW_ALL
                  | PSNameLabelDesc.NAME_READ_ONLY
                  | PSNameLabelDesc.LAYOUT_SIDE, editor);
      // Layout all the controls
      final FormData formData_1 = new FormData();
      formData_1.left = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(formData_1);

      // Available Workflows
      final Label awLabel = new Label(this, SWT.NONE);
      awLabel.setText(PSMessages.getString("PSContentTypePropertiesTab.label.allowedworkflows")); //$NON-NLS-1$
      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(m_commonComp, 0, SWT.LEFT);
      formData_2.right = new FormAttachment(40, 0);
      formData_2.top = new FormAttachment(m_commonComp, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      awLabel.setLayoutData(formData_2);

      m_workflowsTableViewer = CheckboxTableViewer.newCheckList(this,
            SWT.BORDER);
      m_workflowsTable = m_workflowsTableViewer.getTable();
      final FormData formData = new FormData();
      formData.right = new FormAttachment(40, 0);
      formData.top = new FormAttachment(awLabel, 0, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      formData.height = DESCRIPTION_FIELD_HEIGHT;
      m_workflowsTable.setLayoutData(formData);
      m_workflowsTableViewer.setContentProvider(new PSDefaultContentProvider());
      m_workflowsTableViewer.setLabelProvider(new WFLabelProvider());
      editor.registerControl(awLabel.getText(),m_workflowsTable,null);
      
      m_workflowsTable.addSelectionListener(this);
      m_workflowsTableViewer.addCheckStateListener(this);
      m_allButton = new Button(this, SWT.NONE);
      m_allButton.setText(PSMessages.getString("PSContentTypePropertiesTab.label.allbutton")); //$NON-NLS-1$
      final FormData formData_0 = new FormData();
      formData_0.top = new FormAttachment(m_workflowsTable, 0, SWT.TOP);
      formData_0.left = new FormAttachment(m_workflowsTable,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_0.width = 50;
      formData_0.height = 21;
      m_allButton.setLayoutData(formData_0);
      m_allButton.addSelectionListener(this);
      editor.registerControl(m_allButton.getText(),m_allButton,null);

      m_noneButton = new Button(this, SWT.NONE);
      m_noneButton.setText(PSMessages.getString("PSContentTypePropertiesTab.label.nonebutton")); //$NON-NLS-1$
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_allButton, BUTTON_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_allButton, 0, SWT.LEFT);
      formData_3.width = 50;
      formData_3.height = 21;
      m_noneButton.setLayoutData(formData_3);
      m_noneButton.addSelectionListener(this);
      editor.registerControl(m_noneButton.getText(),m_noneButton,null);

      final Label defualtWorkflowLabel = new Label(this, SWT.RIGHT);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_workflowsTable, 0, SWT.TOP);
      formData_4.left = new FormAttachment(50, LABEL_HSPACE_OFFSET);
      defualtWorkflowLabel.setLayoutData(formData_4);
      defualtWorkflowLabel.setText(PSMessages
            .getString("PSContentTypeWorkflowPage.label.defaultworkflow")); //$NON-NLS-1$

      m_defaultWfCombo = new ComboViewer(this, SWT.BORDER | SWT.READ_ONLY);
      final Combo combo = m_defaultWfCombo.getCombo();
      final FormData formData_5 = new FormData();
      formData_5.right = new FormAttachment(100, 0);
      formData_5.top = new FormAttachment(defualtWorkflowLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_5.left = new FormAttachment(defualtWorkflowLabel,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      combo.setLayoutData(formData_5);
      m_defaultWfCombo.setContentProvider(new PSDefaultContentProvider());
      m_defaultWfCombo.setLabelProvider(new PSReferenceLabelProvider());
      m_defaultWfCombo.addSelectionChangedListener(this);
      IPSControlValueValidator required = new PSControlValueRequiredValidator();

      editor.registerControl(defualtWorkflowLabel.getText(), 
            combo, new IPSControlValueValidator[]
            {required});

      m_enableSearchingForButton = createCheckButton(editor, defualtWorkflowLabel, 
         "PSContentTypePropertiesTab.label.enablesearching");      
      m_producesResourceButton = createCheckButton(editor, m_enableSearchingForButton, 
         "PSContentTypePropertiesTab.label.producesResource");
      
      // Content type icon
      final Label ctiLabel = new Label(this, SWT.NONE);
      ctiLabel.setText(PSMessages
            .getString("PSContentTypePropertiesTab.label.contenttypeicon")); //$NON-NLS-1$
      final FormData ctiLabelFd = new FormData();
      ctiLabelFd.left = new FormAttachment(m_producesResourceButton, 0,
            SWT.LEFT);
      ctiLabelFd.right = new FormAttachment(100, 0);
      ctiLabelFd.top = new FormAttachment(m_producesResourceButton,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      ctiLabel.setLayoutData(ctiLabelFd);
      
      m_iconSourceCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
      final FormData m_iconSourceFd = new FormData();
      m_iconSourceFd.left = new FormAttachment(ctiLabel, 0,
            SWT.LEFT);
      m_iconSourceFd.right = new FormAttachment(65, 0);
      m_iconSourceFd.top = new FormAttachment(ctiLabel,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      m_iconSourceCombo.setLayoutData(m_iconSourceFd);
      m_iconSourceCombo.setItems(m_iconSrcData);
      m_iconSourceCombo.addSelectionListener(this);
      editor.registerControl(ctiLabel.getText(), m_iconSourceCombo, null);
      
      m_iconExtFieldsCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
      final FormData m_iconExtFieldsFd = new FormData();
      m_iconExtFieldsFd.left = new FormAttachment(m_iconSourceCombo, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      m_iconExtFieldsFd.right = new FormAttachment(100, 0);
      m_iconExtFieldsFd.top = new FormAttachment(ctiLabel,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      m_iconExtFieldsCombo.setLayoutData(m_iconExtFieldsFd);
      
      m_iconExtFieldsCombo.setItems(getExtFieldNames());
      m_iconExtFieldsCombo.addSelectionListener(this);
      editor.registerControl(ctiLabel.getText(), m_iconExtFieldsCombo, null);
      
      m_iconFileButton = new Button(this, SWT.NONE);
      final FormData m_iconFileButtonFd = new FormData();
      m_iconFileButtonFd.width=20;
      m_iconFileButtonFd.right = new FormAttachment(100, 0);
      m_iconFileButtonFd.top = new FormAttachment(ctiLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      m_iconFileButton.setLayoutData(m_iconFileButtonFd);
      m_iconFileButton.setText("...");
      m_iconFileButton.addSelectionListener(this);
      
      m_iconFileText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
      final FormData m_iconFileTextFd = new FormData();
      m_iconFileTextFd.left = new FormAttachment(m_iconSourceCombo,
            LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_iconFileTextFd.right = new FormAttachment(m_iconFileButton,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.LEFT);
      m_iconFileTextFd.top = new FormAttachment(ctiLabel,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      m_iconFileText.setLayoutData(m_iconFileTextFd);
      editor.registerControl(ctiLabel.getText(), m_iconFileText, null);

      //Transforms and validations
      final Label transformLabel = new Label(this, SWT.WRAP);
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_workflowsTable, LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      formData_7.left = new FormAttachment(0, 0);
      transformLabel.setLayoutData(formData_7);
      transformLabel.setText(PSMessages.getString("PSContentTypePropertiesTab.label.transformsvalidations")); //$NON-NLS-1$

      final Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formData_8 = new FormData();
      formData_8.right = new FormAttachment(100, 0);
      formData_8.top = new FormAttachment(transformLabel,0,SWT.CENTER);
      formData_8.left = new FormAttachment(transformLabel,LABEL_HSPACE_OFFSET,SWT.RIGHT);
      label.setLayoutData(formData_8);
      
      m_transforms = new PSTransformValidationComposite(this,SWT.NONE,editor);
      final FormData formData_9 = new FormData();
      formData_9.top = new FormAttachment(transformLabel, LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      formData_9.left = new FormAttachment(0, 0);
      formData_9.right = new FormAttachment(100, 0);
      
      m_transforms.setLayoutData(formData_9);
      
   }

   /**
    * Creates a check box button below the specified control and within the specified "editor".
    *  
    * @param editor the editor to register the created button, assumed not <code>null</code>.
    * @param controlAbove the control that is right above the created button, assumed not <code>null</code>.
    * @param labelKey the key of the label for the created button. assumed not empty or <code>null</code>.
    * 
    * @return the created button, never <code>null</code>.
    */
   private Button createCheckButton(PSEditorBase editor, Control controlAbove, String labelKey) 
   {
      Button button = new Button(this, SWT.CHECK);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(controlAbove,
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      formData.left = new FormAttachment(controlAbove, 0, SWT.LEFT);
      button.setLayoutData(formData);
      button.setText(PSMessages.getString(labelKey));
      editor.registerControl(button.getText(), button, null);

      return button;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   @Override
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   @Override
   protected void checkSubclass()
   {
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_allButton)
      {
         m_workflowsTableViewer.setAllChecked(true);
      }
      else if (e.getSource() == m_noneButton)
      {
         m_workflowsTableViewer.setAllChecked(false);
         //Reselect default workflow as it is required.
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         if (index != -1)
         {
            String msg = PSMessages.getString("PSContentTypePropertiesTab.error.message.selectiondefaultworkflow") //$NON-NLS-1$
                  + PSMessages.getString("PSContentTypePropertiesTab.error.message.defaultworkflowempty"); //$NON-NLS-1$
            MessageDialog.openInformation(getShell(), PSMessages.getString("PSContentTypePropertiesTab.error.title.defaultworkflow"),msg); //$NON-NLS-1$
            IPSReference selDef = (IPSReference) m_defaultWfCombo
            .getElementAt(index);
            m_workflowsTableViewer.setChecked(selDef,true);
         }
      }
      else if(e.getSource() == m_iconSourceCombo)
      {
         handleIconValueControlVisibility();
      }
      else if(e.getSource() == m_iconFileButton)
      {
         launchFileDialog();
      }
   }

   /**
    * Helper method to handle the icon value controls visibility. Hides and
    * shows the fields combobox or file name text control based on the icon
    * source.
    */
   private void handleIconValueControlVisibility()
   {
      //Hide value controls and show them based on selection
      m_iconExtFieldsCombo.setVisible(false);
      m_iconFileButton.setVisible(false);
      m_iconFileText.setVisible(false);
      String sel = ""+m_iconSourceCombo.getSelectionIndex();
      if(sel.equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
      {
         m_iconFileButton.setVisible(true);
         m_iconFileText.setVisible(true);
         // Automatically launch file dialog if the specified file is empty.
         if (StringUtils.isBlank(m_iconFileText.getText()))
         {
            launchFileDialog();
            // If it is still empty set the source to none.
            if (StringUtils.isBlank(m_iconFileText.getText()))
            {
               m_iconSourceCombo.select(Integer
                     .parseInt(PSContentEditor.ICON_SOURCE_NONE));
               m_iconFileButton.setVisible(false);
               m_iconFileText.setVisible(false);
            }
         }
      }
      else if(sel.equals(PSContentEditor.ICON_SOURCE_FROMFILEEXT))
      {
         m_iconExtFieldsCombo.setVisible(true);
         String fld = m_iconExtFieldsCombo.getText();
         String[] fns = getExtFieldNames();
         m_iconExtFieldsCombo.setItems(fns);
         int index = ArrayUtils.indexOf(fns, fld);
         index = index<0?0:index;
         m_iconExtFieldsCombo.select(index);
      }
      
   }
   
   /**
    * Launches file dialog for selection of icon files for content types.
    * Filters the files with extensions "*.gif", "*.jpg", "*.jpeg", "*.png".
    * If the selected file is invalid then launches the dialog again.
    * Sets icon file text with the selected file path.
    */
   private void launchFileDialog()
   {
      String file = null;
      do
      {
         final FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
         dlg.setFilterExtensions(m_filterExts);
         dlg.setText(PSMessages
               .getString("PSContentTypePropertiesTab.label.contenttypeicon")); //$NON-NLS-1$
         file = dlg.open();
      }while(launchAgain(file));
      if(file!=null)
         m_iconFileText.setText(file);
   }
   
   /**
    * Helper method to decide whether the file dialog needs to be launched
    * again, based on the selected file.
    * 
    * <pre>
    *    Returns false in the following cases.
    *    Supplied file is blank or
    *    Supplied file matches with the current selection or
    *    If a file already exists with this name and user selects yes to 
    *    override it.
    * </pre>
    * <pre>
    *    Returns true in the following cases.
    *    Supplied file is invalid or
    *    Supplied file extension is not in allowed list or
    *    If a file already exists with this name and user selects no to 
    *    override it.
    * </pre>
    * 
    * @param file Absolute path of the uploaded file may be <code>null</code>
    *           or empty.
    * @return <code>true</code> or <code>false</code> based on the condition
    *         as per the above description.
    */
   private boolean launchAgain(String file)
   {
      if (StringUtils.isBlank(file))
         return false;
      if (m_iconFileText.getText().equals(file))
         return false;
      File f = new File(file);
      // It must be a a valid File
      if (f == null || f.isDirectory() || !f.exists())
      {
         String colDelTitle = PSMessages
               .getString("PSContentTypePropertiesTab.warn.title.invalidiconfile"); //$NON-NLS-1$
         String colDelMsg = PSMessages
               .getString("PSContentTypePropertiesTab.warn.message.invalidiconfile"); //$NON-NLS-1$
         MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_ERROR
               | SWT.OK);
         msgBox.setMessage(colDelMsg);
         msgBox.setText(colDelTitle);
         msgBox.open();
         return true;
      }
      // File must have a valid extension
      int index = file.lastIndexOf(".");
      String[] allowedExts = {".gif", ".jpg", ".jpeg", ".png"};
      if (index == -1
            || !ArrayUtils.contains(allowedExts, file.substring(index)))
      {
         String colDelTitle = PSMessages
               .getString("PSContentTypePropertiesTab.warn.title.invalidiconfileextension"); //$NON-NLS-1$
         String colDelMsg = PSMessages
               .getString("PSContentTypePropertiesTab.warn.message.invalidiconfileextension"); //$NON-NLS-1$
         MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_ERROR
               | SWT.OK);
         msgBox.setMessage(colDelMsg);
         msgBox.setText(colDelTitle);
         msgBox.open();
            return true;
      }
      String[] fns = PSCoreUtils.getContentTypeIconFileNames();
      if (!m_iconFileText.getText().equals(f.getName())
            && ArrayUtils.contains(fns, f.getName()))
      {
         String colDelTitle = PSMessages
               .getString("PSContentTypePropertiesTab.warn.title.confirmduplicatefile"); //$NON-NLS-1$
         String colDelMsg = PSMessages
               .getString("PSContentTypePropertiesTab.warn.message.confirmduplicatefile"); //$NON-NLS-1$
         MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_QUESTION
               | SWT.YES | SWT.NO);
         msgBox.setMessage(colDelMsg);
         msgBox.setText(colDelTitle);
         int userResponse = msgBox.open();
         return userResponse == SWT.NO;
      }
      return false;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(@SuppressWarnings("unused") SelectionEvent e)
   {
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
    */
   public void selectionChanged(SelectionChangedEvent event)
   {
      if (event.getSource() == m_defaultWfCombo)
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         IPSReference selDef = (IPSReference) m_defaultWfCombo
               .getElementAt(index);
         m_workflowsTableViewer.setAllGrayed(false);
         m_workflowsTableViewer.setChecked(selDef, true);
         m_workflowsTableViewer.setGrayed(selDef, true);
      }
   }
   
   /*
    *  (non-Javadoc)
    * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
    */
   public void checkStateChanged(CheckStateChangedEvent event)
   {
      if (event.getSource() == m_workflowsTableViewer
            && event.getChecked() == false)
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         IPSReference selDef = (IPSReference) m_defaultWfCombo
               .getElementAt(index);
         if (selDef != null && selDef.equals(event.getElement()))
         {
            m_workflowsTableViewer.setChecked(selDef, true);
         }
      }
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      // Cast the design object
      PSItemDefinition itemDef = (PSItemDefinition)designObject;
      PSContentEditor ceditor = itemDef.getContentEditor();      
      if(control == m_commonComp.getLabelText())
      {
         itemDef.setLabel(m_commonComp.getLabelText().getText().trim());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         itemDef.setDescription( 
            m_commonComp.getDescriptionText().getText().trim());
      }
      else if(control == m_defaultWfCombo.getCombo())
      {
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         if(index == -1)
         {
            ceditor.setWorkflowId(-1);
            return;
         }
         IPSReference selDef = (IPSReference) m_defaultWfCombo
               .getElementAt(index);
         ceditor.setWorkflowId((int)selDef.getId().longValue());
         //The workflow info needs to be updated after setting the workflowid
         updateWorkflowInfo(ceditor);
      }
      else if(control == m_workflowsTable)
      {
         updateWorkflowInfo(ceditor);
      }
      else if(control == m_enableSearchingForButton)
      {
         PSContentEditorPipe pipe = (PSContentEditorPipe)ceditor.getPipe();
         PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
         fieldSet.setUserSearchable(m_enableSearchingForButton.getSelection());
      }
      else if(control == m_producesResourceButton)
      {
         ceditor.producesResource(m_producesResourceButton.getSelection());
      }
      else if(control == m_noneButton)
      {
         updateWorkflowInfo(ceditor);
         int index = m_defaultWfCombo.getCombo().getSelectionIndex();
         if(index == -1)
            ceditor.setWorkflowId(-1);
      }
      else if(control == m_allButton)
      {
         updateWorkflowInfo(ceditor);
      }
      else if (control == m_iconSourceCombo || control == m_iconFileText
            || control == m_iconExtFieldsCombo)
      {
         updateIconInfo(ceditor);
      }
      m_transforms.updateDesignerObject(designObject,control);
   }

   /**
    * Helper method to set the icon control values. If the source is
    * <code>PSContentEditor.ICON_SOURCE_FROMFILEEXT</code> and the field does not
    * exist then warns the user and resets the source to
    * <code>PSContentEditor.ICON_SOURCE_NONE</code>
    */
   public void setIconControlValues()
   {
      PSItemDefinition itemDef = (PSItemDefinition) m_editor
      .getDesignerObject();
      PSContentEditor ceditor = itemDef.getContentEditor();
      String iconSrc = ceditor.getIconSource();
      String value = ceditor.getIconValue();
      m_iconSourceCombo.select(Integer.parseInt(iconSrc));

      String[] fn = getExtFieldNames();
      
      if (iconSrc.equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
      {
         m_iconFileText.setText(value);
      }
      else if (iconSrc.equals(
            PSContentEditor.ICON_SOURCE_FROMFILEEXT))
      {
         m_iconExtFieldsCombo.setItems(fn);
         if(StringUtils.isBlank(value))
         {
            value = fn[0];
         }
         if(!ArrayUtils.contains(fn, value))
         {
            String msg = MessageFormat
            .format(
                  PSMessages
                        .getString("PSContentTypePropertiesTab.warn.msg.missingextfieldmsg"),
                        value);
            String title = PSMessages
            .getString("PSContentTypePropertiesTab.warn.msg.missingextfieldtitle");
            MessageDialog.openInformation(getShell(), title, msg);
            ceditor.setContentTypeIcon("0", null);
            m_iconSourceCombo.select(0);
         }
         m_iconExtFieldsCombo.select(ArrayUtils.indexOf(fn, value));
      }
      handleIconValueControlVisibility();
   }
   
   /**
    * Convenient method to return the field names to use for
    * extension field. 
    * @return String array of field names.
    */
   private String[] getExtFieldNames()
   {
      PSItemDefinition itemDef = (PSItemDefinition) m_editor
      .getDesignerObject();
      List<String> fnList = itemDef.getSingleDimensionParentTextFieldNames();
      return fnList.toArray(new String[fnList.size()]);
   }
   /**
    * Helper method to update the contenttype icon info.
    * @param ceditor Object of PSContentEditor assumed not <code>null</code>.
    */
   private void updateIconInfo(PSContentEditor ceditor)
   {
      String src = ""+m_iconSourceCombo.getSelectionIndex();
      String val = null;
      if(src.equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
      {
         val = m_iconFileText.getText();
      }
      else if(src.equals(PSContentEditor.ICON_SOURCE_FROMFILEEXT))
      {
         val = m_iconExtFieldsCombo.getText();
      }
      if(StringUtils.isBlank(val))
         src = PSContentEditor.ICON_SOURCE_NONE;
      ceditor.setContentTypeIcon(src, val);
   }

   /**
    * A conveneint method to update the workflows info
    * @param ceditor Object of PSContentEditor.
    */
   private void updateWorkflowInfo(PSContentEditor ceditor)
   {
      Object[] wfs = m_workflowsTableViewer.getCheckedElements();
      List<Integer> wflist = new ArrayList<Integer>();
      for(int i=0;i<wfs.length;i++)
      {
         wflist.add(new Integer((int)((IPSReference)wfs[i]).getId().longValue()));
      }
      PSWorkflowInfo wfinfo = ceditor.getWorkflowInfo();
      if(wfinfo == null)
         wfinfo = new PSWorkflowInfo(PSWorkflowInfo.TYPE_INCLUSIONARY,new ArrayList());
      wfinfo.setValues(wflist);
      ceditor.setWorkflowInfo(wfinfo);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * loadControlValues()
    */
   public void loadControlValues(Object designObject)
   {
      PSItemDefinition itemDef = (PSItemDefinition)designObject;
      PSContentEditor ceditor = itemDef.getContentEditor();
      //load name, label and description
      ((Label)m_commonComp.getNameText()).setText(itemDef.getName());
      m_commonComp.getLabelText().setText(itemDef.getLabel());
      m_commonComp.getDescriptionText().setText(itemDef.getDescription());
      //Set search checkbox
      PSContentEditorPipe pipe = (PSContentEditorPipe)ceditor.getPipe();
      PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
      m_enableSearchingForButton.setSelection(fieldSet.isUserSearchable());
      //Set "Produces Resource"
      m_producesResourceButton.setSelection(ceditor.doesProduceResource());
      //Load workflow lists
      List<IPSReference> defWfList = new ArrayList<IPSReference>();
      defWfList.addAll(getCommunityWorkflows(itemDef.getGUID()));
      m_workflowsTableViewer.setContentProvider(new PSDefaultContentProvider());
      m_workflowsTableViewer.setLabelProvider(new WFLabelProvider());
      m_workflowsTableViewer.setInput(defWfList);
      m_defaultWfCombo.setInput(defWfList);

      //get workflow info
      PSWorkflowInfo winfo = ceditor.getWorkflowInfo();
      int defwf = ceditor.getWorkflowId();
      
      //If there is no workflow info means all workflows are allowed
      if(winfo == null)
      {
         m_workflowsTableViewer.setAllChecked(true);
      }
      else
      {
         for(int i=0;i<defWfList.size();i++)
         {
            IPSReference wf = defWfList.get(i);
            if(winfo.getWorkflowIds().contains(new Integer((int)wf.getId().longValue())))
            {
               m_workflowsTableViewer.setChecked(wf,true);
            }
         }
      }
      //Find the index of default workflow and select it in Default Workflow combo
      int dindex = -1;
      for(int i=0;i<defWfList.size();i++)
      {
         IPSReference wf = defWfList.get(i);
         if(((int)wf.getId().longValue()) == defwf)
         {
            dindex = i;
            m_workflowsTableViewer.setChecked(wf,true);
            m_workflowsTableViewer.setGrayed(wf, true);
            break;
         }
      }
      m_defaultWfCombo.getCombo().select(dindex);
      if(dindex == -1)
      {
         String msg = PSMessages.getString("PSContentTypePropertiesTab.error.message.previousinvalidwf") //$NON-NLS-1$
            + PSMessages.getString("PSContentTypePropertiesTab.error.message.dwtofirst"); //$NON-NLS-1$
         MessageDialog.openInformation(getShell(),PSMessages.getString("PSContentTypePropertiesTab.error.title.invaliddw"),msg); //$NON-NLS-1$
      }
      m_transforms.loadControlValues(designObject);
      //set content type icon source and values
      setIconControlValues();
   }
   
   /**
    * Gets the list of communities for the given contenttype guid.
    * @return list of IPSReference of workflow objects. May be 
    */
   private List<IPSReference> getCommunityWorkflows(IPSGuid ctGuid)
   {
      List<IPSReference> wfs = new ArrayList<IPSReference>();
      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.CONTENT_TYPE);
         IPSReference ref = model.getReference(ctGuid);
         List<IPSReference> comms = PSSecurityUtils.getVisibleCommunities(ref);
         if (!comms.isEmpty())
            wfs.addAll(PSSecurityUtils.getObjectsByCommunityVisibility(comms,
                  PSTypeEnum.WORKFLOW));
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException("Workflow Catalog", //$NON-NLS-1$
               PSMessages.getString("PSContentTypePropertiesTab.error.title.wfcatalog"), //$NON-NLS-1$
               PSMessages.getString("PSContentTypePropertiesTab.error.message.wfcatalog"), e); //$NON-NLS-1$
      }
      return wfs;

   }   

   /**
    * The label provider class for the workflow tree viewer
    */
   class WFLabelProvider implements ILabelProvider
   {

      @SuppressWarnings("unused")
      public Image getImage(Object element)
      {
         return null;
      }

      public String getText(Object element)
      {
         if(!(element instanceof IPSReference))
            throw new IllegalArgumentException("IPSReference was expected."); //$NON-NLS-1$
         if(element == null)
            return ""; //$NON-NLS-1$
         return ((IPSReference)element).getName();
      }

      @SuppressWarnings("unused")
      public void addListener(ILabelProviderListener listener)
      {
      }

      public void dispose()
      {
      }

      @SuppressWarnings("unused")
      public boolean isLabelProperty(Object element, String property)
      {
         return false;
      }

      @SuppressWarnings("unused")
      public void removeListener(ILabelProviderListener listener)
      {
      }
      
   }
   
   /**
    * String array of icon source strings. 
    */
   private String[] m_iconSrcData =
      {"None", "Specified", "File Extension Field"};

   /**
    * String array of supported file extensions for content type icons.
    */
   private String[] m_filterExts =
      {"*.gif", "*.jpg", "*.jpeg", "*.png"};

   private PSContentTypeEditor m_editor;
   // Controls
   private PSNameLabelDesc m_commonComp;
   private CheckboxTableViewer m_workflowsTableViewer;
   private Table m_workflowsTable;
   private Button m_allButton;
   private Button m_noneButton;
   private ComboViewer m_defaultWfCombo;
   private Button m_enableSearchingForButton;
   private Button m_producesResourceButton;
   private PSTransformValidationComposite m_transforms;
   private Combo m_iconSourceCombo;
   private Combo m_iconExtFieldsCombo;
   private Text m_iconFileText;
   private Button m_iconFileButton;
}
