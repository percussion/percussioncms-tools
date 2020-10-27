/******************************************************************************
 *
 * [ PSExtensionJavaComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSExtensionModel.Interfaces;
import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSRemoteConsole;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSMuttBoxControl;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSJexlExtensionContextFilter;
import com.percussion.workbench.ui.editors.dialog.PSExtensionDataDialog;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PSExtensionJavaComposite extends Composite
   implements
      IPSDesignerObjectUpdater,
      IPSUiConstants
{

   
   public PSExtensionJavaComposite(Composite parent, PSEditorBase editor)
   {
      super(parent, SWT.NONE);
      setLayout(new FormLayout());
      m_editor = editor;
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      m_nameLabelDesc = new PSNameLabelDesc(this, SWT.NONE, 
         PSMessages.getString("PSExtensionJavaComposite.name.prefix.label") , 0,  //$NON-NLS-1$
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

      m_classNameLabel = new Label(this, SWT.NONE);
      final FormData formData_1 = new FormData();      
      formData_1.top = new FormAttachment(m_nameLabelDesc,
         10 + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.DEFAULT);
      formData_1.left = new FormAttachment(0, 20);
      m_classNameLabel.setLayoutData(formData_1);
      m_classNameLabel.setText(PSMessages.getString(
         "PSExtensionJavaComposite.classNames.label")); //$NON-NLS-1$

      m_classNameText = new Text(this, SWT.BORDER);
      m_classNameText.addModifyListener(new ModifyListener() {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void modifyText(@SuppressWarnings("unused") ModifyEvent e) //$NON-NLS-1$
         {
            m_classNameText.setToolTipText(m_classNameText.getText());
         }
      });
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(50, -25);
      formData_2.top = new FormAttachment(m_classNameLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_2.left = new FormAttachment(m_classNameLabel, 5, SWT.RIGHT);
      m_classNameText.setLayoutData(formData_2);
      editor.registerControl(
         "PSExtensionJavaComposite.classNames.label", //$NON-NLS-1$
         m_classNameText,
         new IPSControlValueValidator[]{vFactory.getRequiredValidator(),
            vFactory.getNoWhitespaceValidator()}
         );

      m_contextLabel = new Label(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_classNameLabel, 0, SWT.TOP);
      formData_3.left = new FormAttachment(50, 25);
      m_contextLabel.setLayoutData(formData_3);
      m_contextLabel.setText(PSMessages.getString(
         "PSExtensionJavaComposite.context.label")); //$NON-NLS-1$

      m_contextComboViewer = new ComboViewer(this, SWT.BORDER);
      m_contextComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_contextComboViewer.addFilter(m_contextFilter);
      m_contextCombo = m_contextComboViewer.getCombo();
      m_contextCombo.addSelectionListener(new SelectionAdapter() {
         @Override
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
         {
            setContextControlToolTip();
         }
      });
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(m_classNameText, 0, SWT.TOP);
      formData_4.left = new FormAttachment(m_contextLabel, 5, SWT.RIGHT);
      m_contextCombo.setLayoutData(formData_4);
      editor.registerControl(
         "PSExtensionJavaComposite.context.label", //$NON-NLS-1$
         m_contextCombo,
         new IPSControlValueValidator[]{vFactory.getRequiredValidator()}
         );
      
      m_parametersLabel = new Label(this, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_classNameText, 10, SWT.BOTTOM);
      formData_5.left = new FormAttachment(0, 0);
      m_parametersLabel.setLayoutData(formData_5);
      m_parametersLabel.setText(PSMessages.getString(
         "PSExtensionJavaComposite.params.label")); //$NON-NLS-1$
      if(m_editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      
      m_paramsTable = ((PSExtensionEditor)m_editor).createParamsTable(
         this, true);
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(m_parametersLabel, 0, SWT.BOTTOM);
      formData_6.left = new FormAttachment(0, 0);
      formData_6.right = new FormAttachment(100, 0);
      formData_6.height = 70;
      m_paramsTable.setLayoutData(formData_6);
      editor.registerControl(
         "PSExtensionJavaComposite.params.label", //$NON-NLS-1$
         m_paramsTable,
         null
         );
      
      m_supportedInterfaces = new InterfacesMuttBox(
         this, PSMessages.getString(
            "PSExtensionJavaComposite.supportedInterfaces.label"), //$NON-NLS-1$
            PSMuttBoxControl.TYPE_DROP); //$NON-NLS-1$
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_paramsTable, 10, SWT.BOTTOM);
      formData_7.left = new FormAttachment(0, 0);
      formData_7.right = new FormAttachment(100, 0);
      formData_7.height = 85;
      m_supportedInterfaces.setLayoutData(formData_7);
      editor.registerControl(
         "PSExtensionJavaComposite.supportedInterfaces.label", //$NON-NLS-1$
         m_supportedInterfaces,
         new IPSControlValueValidator[]{vFactory.getRequiredValidator()}
         );
      
      m_supportedInterfaces.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               PSMuttBoxControl control = (PSMuttBoxControl)e.getSource();
               PSExtensionEditor.handleInterfaceSelection(
                  control.getSelections(),
                  m_contextComboViewer, m_contextFilter);
               setContextControlToolTip();
            }
            
         });
      
      m_requiredFiles = new PSMuttBoxControl(
         this, PSMessages.getString(
            "PSExtensionJavaComposite.requiredFiles.label"), //$NON-NLS-1$
            PSMuttBoxControl.TYPE_FILE_CHOOSER); //$NON-NLS-1$
      m_requiredFiles.setFileExtFilter(
         new String[]{"*.class","*.jar","*.zip"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_supportedInterfaces, 10, SWT.BOTTOM);
      formData_8.left = new FormAttachment(0, 0);
      formData_8.right = new FormAttachment(50, -25);
      formData_8.height = 70;
      m_requiredFiles.setLayoutData(formData_8);
      editor.registerControl(
         "PSExtensionJavaComposite.requiredFiles.label", //$NON-NLS-1$
         m_requiredFiles,
         null
         );
      
      m_requiredApps = new PSMuttBoxControl(
         this, PSMessages.getString(
            "PSExtensionJavaComposite.requiredApps.label"), //$NON-NLS-1$
            PSMuttBoxControl.TYPE_DROP); //$NON-NLS-1$
      final FormData formData_9 = new FormData();
      formData_9.top = new FormAttachment(m_requiredFiles, 0, SWT.TOP);
      formData_9.left = new FormAttachment(50, 0);
      formData_9.right = new FormAttachment(100, 0);
      formData_9.height = 70;
      m_requiredApps.setLayoutData(formData_9);
      editor.registerControl(
         "PSExtensionJavaComposite.requiredApps.label", //$NON-NLS-1$
         m_requiredApps,
         null
         );
      
      m_extDataButton = new Button(this, SWT.PUSH);
      m_extDataButton.setText(PSMessages.getString("PSExtensionJavaComposite.extensionData.label")); //$NON-NLS-1$
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(m_requiredApps, 10, SWT.BOTTOM);
      formData_10.right = new FormAttachment(m_requiredApps, 0, SWT.RIGHT);
      m_extDataButton.setLayoutData(formData_10);
      
      m_extDataButton.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
            {
               IPSExtensionDef def = (IPSExtensionDef)m_editor.getDesignerObject();
               PSExtensionDataDialog dialog = 
                  new PSExtensionDataDialog(getShell(), def);
               int status = dialog.open();
               if(status == Dialog.OK)
               {
                  m_editor.setDirty();
               }
            }
            
         });
      
      try
      {
         loadControlChoices();
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);         
      }
      
      Control[] controlList = new Control[]
         {
            m_nameLabelDesc,
            m_classNameText,
            m_contextCombo,
            m_paramsTable,            
            m_supportedInterfaces,
            m_requiredFiles,
            m_requiredApps,
            m_extDataButton          
         };
      setTabList(controlList);
      
   }
   
   /**
    * Sets context control tooltip to be the value of the current
    * context selection.
    */
   protected void setContextControlToolTip()
   {
      int index = m_contextCombo.getSelectionIndex();
      m_contextCombo.setToolTipText(index == -1 ? "" :  //$NON-NLS-1$
         m_contextCombo.getItem(index)); //$NON-NLS-1$
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
      else if(control == m_classNameText)
      {
         def.setInitParameter(IPSExtensionDef.INIT_PARAM_CLASSNAME, 
            m_classNameText.getText());
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
      else if(control == m_supportedInterfaces)
      {
         def.setInterfaces(m_supportedInterfaces.getSelections());
      }      
      else if(control == m_requiredFiles)
      {
         try
         {
            List<URL> urls = new ArrayList<URL>();        
            for(String path : (List<String>)m_requiredFiles.getSelections())
               urls.add(new File(path).toURL());
            def.setSuppliedResources(urls.iterator());
         }
         catch (MalformedURLException e)
         {
            PSWorkbenchPlugin.handleException(null, null, null, e);            
         }
      }
      else if(control == m_requiredApps)
      {
         def.setRequiredApplications(m_requiredApps.getSelections().iterator());
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
      // Clear resources
      def.setSuppliedResources(new ArrayList().iterator());
      // Set name 
      ((Label)m_nameLabelDesc.getNameText()).setText(
         StringUtils.defaultString(def.getRef().getExtensionName()));
      // Set Description      
      m_nameLabelDesc.getDescriptionText().setText(
         StringUtils.defaultString(def.getInitParameter(
            IPSExtensionDef.INIT_PARAM_DESCRIPTION)));
      // Set class name
       m_classNameText.setText(
          StringUtils.defaultString(def.getInitParameter(
          IPSExtensionDef.INIT_PARAM_CLASSNAME)));
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
          if(!ArrayUtils.contains(PSExtensionEditor.ms_java_types, row[1]))
          {
             extraTypes.add(row[1]);
          }
       }
       // Add extra types to cell editor
       if(!extraTypes.isEmpty())
       {          
          List<String> types = new ArrayList<String>();
          CollectionUtils.addAll(types, PSExtensionEditor.ms_java_types);
          types.addAll(extraTypes);
          PSComboBoxCellEditor cEditor = 
             (PSComboBoxCellEditor)m_paramsTable.getCellEditor(1);
          cEditor.setItems(types.toArray(new String[]{}));
       }
       m_paramsTable.setValues(rows);
       
       // Set supported interfaces
       List<String> interfaces = new ArrayList<String>();
       Iterator it = def.getInterfaces();
       while(it.hasNext())
          interfaces.add((String)it.next());
       m_supportedInterfaces.setSelections(interfaces);
       if(interfaces.contains("com.percussion.extension.IPSJexlExpression")) //$NON-NLS-1$
       {
          m_contextFilter.setApplyFiltering(true);
       }
       else
       {
          m_contextFilter.setApplyFiltering(false);
       }
       m_contextComboViewer.refresh();
       
             
       // Set Required apps
       List<String> apps = new ArrayList<String>();
       it = def.getRequiredApplications();
       while(it.hasNext())
          apps.add((String)it.next());
       m_requiredApps.setSelections(apps);
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
        
        // load supported interfaces choices
        m_supportedInterfaces.setChoices(Interfaces.getClassNames());
        
        // load required apps choices
        m_requiredApps.setChoices(loadAppNames());
        
     }
     catch(Exception e)
     {
        throw new PSModelException(e);
     }
      
   }
   
   /**
    * Loads the list of application names from the server.
    * 
    * @return A list of appnames as <code>String</code> objects, never 
    * <code>null</code>, may be empty if the request to the server 
    * fails, in which case a message is printed to the console.
    */
   private List<String> loadAppNames()
   {
      List<String> appNames = new ArrayList<String>();
      try
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         PSRemoteConsole console = 
            new PSRemoteConsole(factory.getDesignerConnection());
         Document doc = console.execute("show applications"); //$NON-NLS-1$
         PSXmlTreeWalker   walker   = new PSXmlTreeWalker(doc);
         if (walker.getNextElement("Applications", true, true) != null) //$NON-NLS-1$
         {
            while (walker.getNextElement("Application", true, true) != null) //$NON-NLS-1$
            {
               String name = walker.getElementData("name", false); //$NON-NLS-1$
               if (name != null && name.trim().length() > 0)
               {
                  appNames.add(name);
               }
            }
         }         
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      
      return appNames;
   }
   
   /**
    * Override mutt box to extend functionality on delete action
    */
   class InterfacesMuttBox extends PSMuttBoxControl
   {

      public InterfacesMuttBox(Composite parent, String labelText, int type)
      {
         super(parent, labelText, type);
      }

      /* 
       * @see com.percussion.workbench.ui.controls.PSMuttBoxControl#preDelete()
       */
      @SuppressWarnings("unchecked") //$NON-NLS-1$
      @Override
      protected boolean preDelete()
      {
         List<String> removeList = new ArrayList<String>(); 
         PSExtensionDef def = (PSExtensionDef)m_editor.m_data;
         StringBuilder sb = new StringBuilder();
         for(String param : PSExtensionDataDialog.ms_assembly_params)
         {
            if(def.getInitParameter(param) != null)
            {
               removeList.add(param);
               sb.append(param);
               sb.append("\n"); //$NON-NLS-1$
            }
         }
         if(removeList.isEmpty())
            return true;
         List<String> selections = (List<String>)getSelections();
         for(String current : selections)
         {
            if(current.equals(PSExtensionDataDialog.ASSEMBLY_INTERFACE))
            {
               Object[] args = new Object[]{sb.toString()};
               String msg = 
                  PSMessages.getString("PSExtensionDataDialog.removingParams.message", args); //$NON-NLS-1$
               boolean bOK = MessageDialog.openQuestion(
                  getShell(),
                  PSMessages.getString("PSExtensionDataDialog.removingParams.title"), msg); //$NON-NLS-1$
               if(bOK)
               {
                  for(String key : removeList)
                  {
                     def.setInitParameter(key, null);
                  }
                  return true;
               }
               return false;
            }
         }
         return true;
      }
      
      
      
   }
   
   private Label m_parametersLabel;
   private Combo m_contextCombo;
   private ComboViewer m_contextComboViewer;
   private Label m_contextLabel;
   private Text m_classNameText;
   private Label m_classNameLabel;
   private PSNameLabelDesc m_nameLabelDesc;
   private PSSortableTable m_paramsTable;
   private PSMuttBoxControl m_supportedInterfaces;
   private PSMuttBoxControl m_requiredFiles;
   private PSMuttBoxControl m_requiredApps;
   private Button m_extDataButton;
   
   /** 
    * Filter used to restrict context combo to values allowed for
    * Jexl expression extensions.
    */
   PSJexlExtensionContextFilter m_contextFilter = 
      new PSJexlExtensionContextFilter();
   
   PSEditorBase m_editor;

}
