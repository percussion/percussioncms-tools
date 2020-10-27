/******************************************************************************
*
* [ PSActionMenuCommandComposite.java ]
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
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionParameters;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSUrlParamTableComposite;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Command composite for the action menu
 */
public class PSActionMenuCommandComposite extends Composite
   implements IPSDesignerObjectUpdater
{

   
   /**
    * Create the composite
    * @param parent
    * @param style
    * @param editor
    * @param page
    */
   public PSActionMenuCommandComposite(
      Composite parent, int style, PSEditorBase editor, int page)
   {
      super(parent, style);
      setLayout(new FormLayout());
      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      m_editor = editor;

      m_commandGroupLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 3);
      formData.left = new FormAttachment(0, 50);
      m_commandGroupLabel.setLayoutData(formData);
      m_commandGroupLabel.setText(PSMessages.getString(
         "PSActionMenuCommandComposite.commandGroup.label")); //$NON-NLS-1$

      m_generateButton = new Button(this, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.height = 19;
      formData_1.top = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(100, -50);
      m_generateButton.setLayoutData(formData_1);
      m_generateButton.setText(PSMessages.getString(
         "PSActionMenuCommandComposite.generate.label")); //$NON-NLS-1$
      m_generateButton.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               onGenerate();
            }
            
         });
       
      m_commandGroupCombo = new Combo(this, SWT.READ_ONLY);
      final FormData formData_2 = new FormData();
      formData_2.left = new FormAttachment(m_commandGroupLabel, 5, SWT.RIGHT);
      formData_2.top = new FormAttachment(m_commandGroupLabel, -3, SWT.TOP);
      formData_2.right = new FormAttachment(m_generateButton, -5, SWT.LEFT);
      m_commandGroupCombo.setLayoutData(formData_2);
      m_commandGroupCombo.setItems(new String[]
         {
            "Content Assembler", //$NON-NLS-1$
            "Content Editor", //$NON-NLS-1$
            "Relationship Handler" //$NON-NLS-1$
         });
      m_commandGroupCombo.select(0);
      editor.registerControlHelpOnly(
         "PSActionMenuCommandComposite.commandGroup.label",
         m_commandGroupCombo);

      m_urlLabel = new Label(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_commandGroupCombo, 15, SWT.BOTTOM);
      formData_3.left = new FormAttachment(m_commandGroupLabel, 0, SWT.LEFT);
      m_urlLabel.setLayoutData(formData_3);
      m_urlLabel.setText(PSMessages.getString(
         "PSActionMenuCommandComposite.url.label")); //$NON-NLS-1$

      m_urlText = new Text(this, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
      m_urlText.setTextLimit(4000);
      final FormData formData_4 = new FormData();
      formData_4.height = 50;
      formData_4.right = new FormAttachment(m_generateButton, 0, SWT.RIGHT);
      formData_4.top = new FormAttachment(m_urlLabel, 0, SWT.BOTTOM);
      formData_4.left = new FormAttachment(m_urlLabel, 0, SWT.LEFT);
      m_urlText.setLayoutData(formData_4);
      editor.registerControl(
         "PSActionMenuCommandComposite.url.label",
         m_urlText,
         new IPSControlValueValidator[]{
            PSControlValidatorFactory.getInstance().getRequiredValidator()},
         page);

      m_parametersLabel = new Label(this, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.right = new FormAttachment(m_commandGroupCombo, -5, SWT.LEFT);
      formData_5.top = new FormAttachment(m_urlText, 35, SWT.BOTTOM);
      formData_5.left = new FormAttachment(m_urlText, 0, SWT.LEFT);
      m_parametersLabel.setLayoutData(formData_5);
      m_parametersLabel.setText(PSMessages.getString(
         "PSActionMenuCommandComposite.parameters.label"));       //$NON-NLS-1$
      
      m_paramsTable = new PSUrlParamTableComposite(this, getContextParams());
      final FormData formData_6 = new FormData();
      formData_6.left = new FormAttachment(m_parametersLabel, 0, SWT.LEFT);
      formData_6.right = new FormAttachment(m_urlText, 0, SWT.RIGHT);
      formData_6.top = new FormAttachment(m_parametersLabel, 0, SWT.BOTTOM);
      formData_6.height = 150;
      m_paramsTable.setLayoutData(formData_6);
      editor.registerControl(
         "PSActionMenuCommandComposite.parameters.label",
         m_paramsTable,
         null,
         page);
      
      
      
      //
   }
   
   /**
    * Action executed when the generate button is pressed.
    * It replaces the current values of the url and params with
    * the selected example.
    */
   private void onGenerate()
   {
      String example = null;
      switch(m_commandGroupCombo.getSelectionIndex())
      {
         case 0:
            example = CONTENT_ASSEMBLER_EXAMPLE;
            break;
         case 1:
            example = CONTENT_EDITOR_EXAMPLE;            
            break;
         case 2:
            example = RELATIONSHIP_HANDLER_EXAMPLE;
            break;
      }
      String[] temp = example.split("\\?");       //$NON-NLS-1$
      m_paramsTable.setValue(example);
      m_urlText.setText(temp[0]);
   }   
     
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSAction action = (PSAction)designObject;
      if(control == m_urlText || control == m_paramsTable)
      {
         action.setURL(m_urlText.getText());
         PSActionParameters params = action.getParameters();
         Iterator<PSPair<String, String>> it = m_paramsTable.getParamPairs();
         params.clear();
         while(it.hasNext())
         {
            PSPair<String, String> pair = it.next();
            params.add(
               new PSActionParameter(pair.getFirst(), pair.getSecond()));
         }
         
      }
   }
   
   /**
    * Loads the intial control values.
    */
   public void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
      PSAction action = (PSAction)m_editor.m_data;
      if(!StringUtils.isBlank(action.getURL()))
      {
         m_urlText.setText(action.getURL());
         PSActionParameters params = action.getParameters();
         Iterator it = params.iterator();
         StringBuilder sb = new StringBuilder();
         //Build a dummy url with parameters (other than those in the url) so 
         //that params table can render these. 
         String sep = "dummy?";
         while(it.hasNext())
         {
            PSActionParameter param = (PSActionParameter)it.next();
            sb.append(sep);
            sep = "&";
            sb.append(param.getName());
            sb.append("=");
            sb.append(param.getValue());
         }
         m_paramsTable.setValue(sb.toString());
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
   
   /**
    * Helper method to retrieve all of the context params 
    * @return never <code>null</code>, may be empty.
    */
   private java.util.List<String> getContextParams()
   {      
      java.util.List<String> results = new ArrayList<String>();
      try
      {
         java.util.List<IPSReference> refs = 
            PSCoreUtils.catalog(PSObjectTypes.UI_ACTION_MENU_MISC, false);
         for(IPSReference ref : refs)
         {
            if(ref.getObjectType().getSecondaryType() ==
               PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXT_PARAMETERS)
            {
               results.add(ref.getName());
            }
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSActionMenuCommandComposite.catalogContextParams.msg"),  //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      
      return results;
   }
   
   //Controls
   private Label m_parametersLabel;
   private Text m_urlText;
   private Label m_urlLabel;
   private Combo m_commandGroupCombo;   
   private Button m_generateButton;
   private Label m_commandGroupLabel;
   private PSUrlParamTableComposite m_paramsTable;
   
   private PSEditorBase m_editor;
   
   /**
    * The content editor example url.
    */
   private static final String CONTENT_EDITOR_EXAMPLE =
      "../rx_ceArticle/article.html" + //$NON-NLS-1$
      "?sys_command=edit&$sys_contentid=$sys_contentid&sys_pageid=0&sys_view=sys_All" + //$NON-NLS-1$
      "&sys_revision=$sys_revision"; //$NON-NLS-1$

   /**
    * The content assembler example url.
    */
   private static final String CONTENT_ASSEMBLER_EXAMPLE =
      "../casArticle/casArticle.html" + //$NON-NLS-1$
      "?sys_contentid=$sys_contentid&sys_variantid=$sys_variantid" + //$NON-NLS-1$
      "&sys_context=$sys_context&sys_revision=$sys_revision" + //$NON-NLS-1$
      "&sys_authtype=$sys_authtype"; //$NON-NLS-1$

   /**
    * The relationship handler example url.
    */
   private static final String RELATIONSHIP_HANDLER_EXAMPLE =
      "../rx_ceArticle/article.html" + //$NON-NLS-1$
      "?sys_command=relate/create&sys_contentid=$sys_contentid" + //$NON-NLS-1$
      "&sys_revision=$sys_revision&sys_dependentid=$sys_dependentid"; //$NON-NLS-1$

   

}
