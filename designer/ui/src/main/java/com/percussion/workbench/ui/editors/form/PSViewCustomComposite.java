/******************************************************************************
 *
 * [ PSViewCustomComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSUrlParamTableComposite;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

public class PSViewCustomComposite extends Composite
   implements IPSDesignerObjectUpdater, IPSUiConstants
{

   
   /**
    * Create the composite
    * @param parent
    * @param style
    * @param editor
    */
   public PSViewCustomComposite(
      Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      if(editor == null)
         throw new IllegalArgumentException("Editor cannot be null."); //$NON-NLS-1$
      m_editor = editor;
      createControl();  
   }
   
   private void createControl()
   {
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();
      // This composite takes care of registering its own controls
      m_commonComp = 
         new PSNameLabelDesc(this, SWT.NONE, 
            PSMessages.getString("PSViewCustomComposite.customView.label"), 0,  //$NON-NLS-1$
            PSNameLabelDesc.LAYOUT_SIDE |
            PSNameLabelDesc.SHOW_ALL |
            PSNameLabelDesc.NAME_READ_ONLY |
            PSNameLabelDesc.LABEL_USES_NAME_PREFIX, m_editor);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.left = new FormAttachment(0,0);
      m_commonComp.setLayoutData(formData);
      
      m_DisplayFormatLabel = new Label(this, SWT.NONE);
      final FormData formData_1 = new FormData();      
      formData_1.top = new FormAttachment(m_commonComp, 35, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 0);
      m_DisplayFormatLabel.setLayoutData(formData_1);
      m_DisplayFormatLabel.setText(
         PSMessages.getString("common.displayFormat.label")); //$NON-NLS-1$

      m_displayFormatComboViewer = new ComboViewer(this, SWT.READ_ONLY);
      m_displayFormatComboViewer.setContentProvider(
         new PSDefaultContentProvider());
      m_displayFormatComboViewer.setLabelProvider(
         new PSReferenceLabelProvider());
      m_displayFormatCombo = m_displayFormatComboViewer.getCombo();
      final FormData formData_2 = new FormData();     
      formData_2.right = new FormAttachment(50, -20);
      formData_2.top = new FormAttachment(m_DisplayFormatLabel, 0, SWT.BOTTOM);
      formData_2.left = new FormAttachment(m_DisplayFormatLabel, 0, SWT.LEFT);
      m_displayFormatCombo.setLayoutData(formData_2);
      
      m_editor.registerControl(
         "common.displayFormat.label",
         m_displayFormatCombo,
         new IPSControlValueValidator[]{required});      
      
      m_parentCatComp = new PSViewParentCategoryComposite(this, m_editor, true);
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_displayFormatCombo, 5, SWT.BOTTOM);
      formData_7.left = new FormAttachment(m_displayFormatCombo, 0, SWT.LEFT);
      formData_7.right = new FormAttachment(m_displayFormatCombo, 0, SWT.RIGHT);
      m_parentCatComp.setLayoutData(formData_7);
      
      m_UrlLabel = new Label(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_DisplayFormatLabel, 0, SWT.TOP);
      formData_3.left = new FormAttachment(50, 0);
      m_UrlLabel.setLayoutData(formData_3);
      m_UrlLabel.setText(PSMessages.getString(
         "PSViewCustomComposite.customViewUrl.label")); //$NON-NLS-1$

      m_UrlText = new Text(this, SWT.BORDER | SWT.WRAP);
      m_UrlText.setTextLimit(255);
      final FormData formData_4 = new FormData();
      formData_4.height = DESCRIPTION_FIELD_HEIGHT;
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(m_UrlLabel, 0, SWT.BOTTOM);
      formData_4.left = new FormAttachment(m_UrlLabel, 0, SWT.LEFT);
      m_UrlText.setLayoutData(formData_4);      
      m_editor.registerControl(
         "PSViewCustomComposite.customViewUrl.label",
         m_UrlText,
         new IPSControlValueValidator[]{required});

      m_ParametersLabel = new Label(this, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_UrlText, 5, SWT.BOTTOM);
      formData_5.left = new FormAttachment(m_UrlText, 0, SWT.LEFT);
      m_ParametersLabel.setLayoutData(formData_5);
      m_ParametersLabel.setText(PSMessages.getString(
         "PSViewCustomComposite.parameters.label")); //$NON-NLS-1$
      
      m_paramTable = new PSUrlParamTableComposite(this, null);
      m_editor.registerControl(
         "PSViewCustomComposite.parameters.label",
         m_paramTable,
         null);
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(m_ParametersLabel, 0, SWT.BOTTOM);
      formData_6.left = new FormAttachment(m_ParametersLabel, 0, SWT.LEFT);
      formData_6.right = new FormAttachment(100, 0);
      formData_6.height = 120;
      m_paramTable.setLayoutData(formData_6);
      
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
   
  
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSSearch def = (PSSearch)designObject;
      
      if(control == m_commonComp.getLabelText())
      {
         def.setDisplayName(m_commonComp.getLabelText().getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         def.setDescription(m_commonComp.getDescriptionText().getText());
      }
      else if(control == m_displayFormatCombo)
      {
         int selection = m_displayFormatCombo.getSelectionIndex();
         IPSReference ref = 
            (IPSReference)m_displayFormatComboViewer.getElementAt(selection);
         int id = (int)ref.getId().longValue();
         def.setDisplayFormatId(String.valueOf(id));         
      }
      else if(control == m_UrlText || control == m_paramTable)
      {
         String url = m_UrlText.getText();
         if(StringUtils.isBlank(url))
            url = PSSearch.URL_PLACEHOLDER;
         String query = m_paramTable.getValue();
         if(!StringUtils.isBlank(query))
            url += "?" + query; //$NON-NLS-1$
         def.setUrl(url);
      }
      else
      {
         m_parentCatComp.updateDesignerObject(designObject, control);
      }
      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSSearch def = (PSSearch)designObject;
      
      // Set name/label/description
      ((Label)m_commonComp.getNameText()).setText(def.getInternalName());
      m_commonComp.getLabelText().setText(def.getDisplayName());
      m_commonComp.getDescriptionText().setText(def.getDescription());
      
      // Set display format
      m_displayFormatComboViewer.setInput(getDisplayFormats());
      m_displayFormatCombo.select(0); //default to first selection
      if(!StringUtils.isBlank(def.getDisplayFormatId()))
      {
         int id = Integer.parseInt(def.getDisplayFormatId());         
         int selection = 
            PSUiUtils.getReferenceIndexById(getDisplayFormats(), id);
         m_displayFormatCombo.select(selection);
      }
      
      //load parent category control
      m_parentCatComp.loadControlValues(designObject);
      
      // Set custom view url and parameters
      String url = def.getUrl();     
      String query = url;
      if(url.indexOf('?') >= 0)
      {
         query = url.substring(0, url.indexOf('?'));
         m_paramTable.setValue(url);
      }
      if (query.equals(PSSearch.URL_PLACEHOLDER))
         query = "";
      m_UrlText.setText(query);
   }   
   
   /**
    * @return list of all available display formats
    */
   private List<IPSReference> getDisplayFormats()
   {
      List<IPSReference> displayFormats = new ArrayList<IPSReference>();
      try
      {
         displayFormats.addAll(
            PSCoreUtils.catalog(PSObjectTypes.UI_DISPLAY_FORMAT, false));         
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      return displayFormats;
   }   
   
      
   
   private PSEditorBase m_editor;  
   private Label m_ParametersLabel;
   private Text m_UrlText;
   private Label m_UrlLabel;
   private Combo m_displayFormatCombo;
   private ComboViewer m_displayFormatComboViewer;   
   private Label m_DisplayFormatLabel;
   private PSNameLabelDesc m_commonComp;
   private PSUrlParamTableComposite m_paramTable;
   
   /**
    * Composite that contains the parent categories drop down
    * and functionality. Initialized during construction, never
    * <code>null</code> or modified after that.
    */
   private PSViewParentCategoryComposite m_parentCatComp;
   

}
