/******************************************************************************
 *
 * [ PSExtensionJavascriptComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.PSModelException;
import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PSExtensionJavascriptComposite extends Composite
   implements
      IPSDesignerObjectUpdater,
      IPSUiConstants
{

   
   
   public PSExtensionJavascriptComposite(Composite parent, PSEditorBase editor)
   {
      super(parent, SWT.NONE);
      setLayout(new FormLayout());
      m_editor = editor;
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      m_nameLabelDesc = new PSNameLabelDesc(this, SWT.NONE, PSMessages.getString(
         "PSExtensionJavascriptComposite.name.prefix") , 0,  //$NON-NLS-1$
          PSNameLabelDesc.SHOW_NAME |
          PSNameLabelDesc.SHOW_DESC |
          PSNameLabelDesc.NAME_READ_ONLY |
          PSNameLabelDesc.LAYOUT_SIDE
          , editor);
      m_nameLabelDesc.getDescriptionText().setTextLimit(Text.LIMIT);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.left = new FormAttachment(0, 0);
      m_nameLabelDesc.setLayoutData(formData);

      m_versionLabel = new Label(this, SWT.RIGHT);
      final FormData formData_1 = new FormData();      
      formData_1.top = new FormAttachment(m_nameLabelDesc,
         10 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.DEFAULT);
      formData_1.left = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(ONE_FIFTH, 0);
      m_versionLabel.setLayoutData(formData_1);
      m_versionLabel.setText(PSMessages.getString(
         "PSExtensionJavascriptComposite.version.label")); //$NON-NLS-1$

      m_versionText = new Text(this, SWT.BORDER);
      m_versionText.addModifyListener(new ModifyListener() {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
         {
            m_versionText.setToolTipText(m_versionText.getText());
         }
      });
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(50, -25);
      formData_2.top = new FormAttachment(m_versionLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_2.left = new FormAttachment(m_versionLabel, 5, SWT.RIGHT);
      m_versionText.setLayoutData(formData_2);
      editor.registerControl(
         "PSExtensionJavascriptComposite.version.label", //$NON-NLS-1$
         m_versionText,
         null
         );

      m_contextLabel = new Label(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_versionLabel, 0, SWT.TOP);
      formData_3.left = new FormAttachment(50, 25);
      m_contextLabel.setLayoutData(formData_3);
      m_contextLabel.setText(PSMessages.getString(
         "PSExtensionJavascriptComposite.context.label")); //$NON-NLS-1$

      m_contextComboViewer = new ComboViewer(this, SWT.BORDER);
      m_contextComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_contextCombo = m_contextComboViewer.getCombo();
      m_contextCombo.addSelectionListener(new SelectionAdapter() {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         @Override
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
         {
            int index = m_contextCombo.getSelectionIndex();
            m_contextCombo.setToolTipText(index == -1 ? "" : m_contextCombo.getItem(index)); //$NON-NLS-1$
         }
      });
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(m_versionText, 0, SWT.TOP);
      formData_4.left = new FormAttachment(m_contextLabel, 5, SWT.RIGHT);
      m_contextCombo.setLayoutData(formData_4);
      editor.registerControl(
         "PSExtensionJavascriptComposite.context.label", //$NON-NLS-1$
         m_contextCombo,
         new IPSControlValueValidator[]{vFactory.getRequiredValidator()}
         );
      
      m_parametersLabel = new Label(this, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_versionText, 10, SWT.BOTTOM);
      formData_5.left = new FormAttachment(0, 0);
      m_parametersLabel.setLayoutData(formData_5);
      m_parametersLabel.setText(PSMessages.getString(
         "PSExtensionJavascriptComposite.params.label")); //$NON-NLS-1$
      if(m_editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      
      m_paramsTable = ((PSExtensionEditor)m_editor).createParamsTable(
         this, false);
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(m_parametersLabel, 0, SWT.BOTTOM);
      formData_6.left = new FormAttachment(0, 0);
      formData_6.right = new FormAttachment(100, 0);
      formData_6.height = 70;
      m_paramsTable.setLayoutData(formData_6);
      editor.registerControl(
         "PSExtensionJavascriptComposite.params.label", //$NON-NLS-1$
         m_paramsTable,
         null
         );     
      

      m_bodyLabel = new Label(this, SWT.LEFT);
      final FormData formData_7 = new FormData();
      formData_7.left = new FormAttachment(m_paramsTable, 0, SWT.LEFT);      
      formData_7.top = new FormAttachment(m_paramsTable,
         15 , SWT.BOTTOM);      
      m_bodyLabel.setLayoutData(formData_7);
      m_bodyLabel.setText(PSMessages.getString(
         "PSExtensionJavascriptComposite.body.label")); //$NON-NLS-1$

      m_bodyText = new Text(this, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
      final FormData formData_8 = new FormData();
      formData_8.height = 120;
      formData_8.right = new FormAttachment(100, 0);
      formData_8.top = new FormAttachment(m_bodyLabel,
         0, SWT.BOTTOM);
      formData_8.left = new FormAttachment(m_bodyLabel, 0, SWT.LEFT);
      m_bodyText.setLayoutData(formData_8);
      editor.registerControl(
         "PSExtensionJavascriptComposite.body.label", //$NON-NLS-1$
         m_bodyText,
         new IPSControlValueValidator[]{vFactory.getRequiredValidator()}
         );
      
      
      try
      {
         loadControlChoices();
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);         
      }
      
   }   
   

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSExtensionDef def = (PSExtensionDef) designObject;
      if(control == m_nameLabelDesc.getDescriptionText())
      {
         def.setInitParameter(IPSExtensionDef.INIT_PARAM_DESCRIPTION, 
            m_nameLabelDesc.getDescriptionText().getText());
      }
      else if(control == m_versionText)
      {
         def.setInitParameter(IPSExtensionDef.INIT_PARAM_JAVASCRIPT_VERSION, 
            m_versionText.getText());
      }
      else if(control == m_contextCombo)
      {
         PSExtensionRef ref = def.getRef();
         def.setExtensionRef(new PSExtensionRef(
            ref.getHandlerName(),
            m_contextCombo.getText(),
            ref.getExtensionName()));
      }
      else if(control == m_paramsTable)
      {
         List<String[]> values = 
            (List<String[]>)m_paramsTable.getValues();
         List<PSExtensionParamDef> params = 
            new ArrayList<PSExtensionParamDef>();
         for(String[] param : values)
         {
            PSExtensionParamDef p = 
               new PSExtensionParamDef(param[0], param[1]);
            p.setDescription(param[2]);
            params.add(p);
         }
         def.setRuntimeParameters(params.iterator());
      }
      else if(control == m_bodyText)
      {
         def.setInitParameter(IPSExtensionDef.INIT_PARAM_SCRIPT_BODY,
            m_bodyText.getText());
      }      
      
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void loadControlValues(Object designObject)
   {
      final PSExtensionDef def = (PSExtensionDef)designObject;
      // Set name 
      ((Label)m_nameLabelDesc.getNameText()).setText(
         StringUtils.defaultString(def.getRef().getExtensionName()));
      // Set Description      
      m_nameLabelDesc.getDescriptionText().setText(
         StringUtils.defaultString(def.getInitParameter(
            IPSExtensionDef.INIT_PARAM_DESCRIPTION)));
      // Set class name
       m_versionText.setText(
          StringUtils.defaultString(def.getInitParameter(
          IPSExtensionDef.INIT_PARAM_JAVASCRIPT_VERSION)));
       // Set Context
       StructuredSelection selection = 
          new StructuredSelection(def.getRef().getContext());
       m_contextComboViewer.setSelection(selection);
       
       //Set params table values
       Set<String> extraTypes = new HashSet<String>();
       List<String[]> rows = new ArrayList<String[]>();
       Iterator<String> paramNames = 
          (Iterator<String>)def.getRuntimeParameterNames();
       while(paramNames.hasNext())
       {
          String name = paramNames.next();
          IPSExtensionParamDef param = def.getRuntimeParameter(name);
          String[] row = new String[]{
             param.getName(),
             param.getDataType(),
             param.getDescription()
          };
          rows.add(row);
          if(!ArrayUtils.contains(PSExtensionEditor.ms_javascript_types, row[1]))
          {
             extraTypes.add(row[1]);
          }
       }
       // Add extra types to cell editor
       if(!extraTypes.isEmpty())
       {          
          List<String> types = new ArrayList<String>();
          CollectionUtils.addAll(types, PSExtensionEditor.ms_javascript_types);
          types.addAll(extraTypes);
          PSComboBoxCellEditor cEditor = 
             (PSComboBoxCellEditor)m_paramsTable.getCellEditor(1);
          cEditor.setItems(types.toArray(new String[]{}));
       }
       m_paramsTable.setValues(rows);
       
       // Set script body
       m_bodyText.setText(
          StringUtils.defaultString(def.getInitParameter(
          IPSExtensionDef.INIT_PARAM_SCRIPT_BODY)));
       
      
   }
   
   /**
    * Loads the choices into the various controls that have choices
    * such as Combo's and lists
    */
   private void loadControlChoices() throws PSModelException
   {
     try
     {
        // load context control choices
        m_contextComboViewer.setInput(PSExtensionEditor.loadContexts());             
        
     }
     catch(Exception e)
     {
        throw new PSModelException(e);
     }
      
   }
   
   
   // Controls
   private Label m_parametersLabel;
   private Combo m_contextCombo;
   private ComboViewer m_contextComboViewer;
   private Label m_contextLabel;
   private Text m_versionText;
   private Label m_versionLabel;
   private PSNameLabelDesc m_nameLabelDesc;
   private PSSortableTable m_paramsTable;   
   private Text m_bodyText;
   private Label m_bodyLabel;
   
   PSEditorBase m_editor;

}
