/******************************************************************************
 *
 * [ PSSearchEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.FeatureSet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a single paned UI for modifying a search design object. The editor's
 * appearance will vary depending on whether a custom or standard type search
 * is being edited.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSSearchEditor extends PSEditorBase
   implements IPSUiConstants
{

   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false;
      return ref.getObjectType().equals(
            PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH,
                  PSObjectTypes.SearchSubTypes.STANDARD))
            || ref.getObjectType().equals(
                  PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH,
                        PSObjectTypes.SearchSubTypes.CUSTOM));
   }

   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSSearch def = (PSSearch)designObject;
      
      // Load common controls
      
      // set name
      ((Label)m_commonComp.getNameText()).setText(def.getInternalName());
      // set label
      m_commonComp.getLabelText().setText(def.getDisplayName());
      // set description
      m_commonComp.getDescriptionText().setText(def.getDescription());

      m_resultsComp.loadControlValues(designObject);
      
      if(m_isCustomSearch)
      {
         m_customUrlText.setText(StringUtils.defaultString(def.getUrl()));         
      }
      else
      {
         if(m_useExternalSearch)
         {
            m_queryComp.loadControlValues(designObject);

            // set display mode
            String displayMode = def.getProperty(PSSearch.PROP_SEARCH_MODE);
            m_displayModeCombo.select(displayMode == 
               PSSearch.SEARCH_MODE_ADVANCED ? 1 : 0);
         }
         
         // set user customizable
         String uCust = def.getProperty(PSSearch.PROP_USER_CUSTOMIZABLE);
         m_UserCustomizableButton.setSelection(uCust != null && 
            uCust.equalsIgnoreCase(PSSearch.BOOL_YES));
      }
      
      // Set search query panel
      m_queryEditor.loadControlValues(def);
   }

   @Override
   public void createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();
      String namePrefix = m_isCustomSearch 
         ? PSMessages.getString("PSSearchEditor.customSearch.label") //$NON-NLS-1$
            : PSMessages.getString("PSSearchEditor.standardSearch.label");  //$NON-NLS-1$
      m_commonComp = 
         new PSNameLabelDesc(comp, SWT.NONE,
            namePrefix, 0,  
            PSNameLabelDesc.LAYOUT_SIDE |
            PSNameLabelDesc.SHOW_ALL |
            PSNameLabelDesc.NAME_READ_ONLY |
            PSNameLabelDesc.LABEL_USES_NAME_PREFIX);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.left = new FormAttachment(0,0);
      m_commonComp.setLayoutData(formData);
            
      registerControl(
         m_commonComp.LABEL_TEXT_KEY, 
         m_commonComp.getLabelText(),
         new IPSControlValueValidator[]{required});
      registerControl(
         m_commonComp.DESC_TEXT_KEY,
         m_commonComp.getDescriptionText(), 
         null);
      
      Composite leftComp = m_isCustomSearch 
      ? createCustomLeftComposite(comp) 
         : createStandardLeftComposite(comp);
      final FormData formData_left = new FormData();      
      formData_left.top = new FormAttachment(m_commonComp, 10, SWT.BOTTOM);
      formData_left.left = new FormAttachment(0, 0);
      formData_left.right = new FormAttachment(50, -20);
      leftComp.setLayoutData(formData_left);
            
      m_searchCriteriaLabel = new Label(comp, SWT.NONE);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_commonComp, 10, SWT.BOTTOM);
      formData_4.left = new FormAttachment(50, 0);
      m_searchCriteriaLabel.setLayoutData(formData_4);
      m_searchCriteriaLabel.setText(
         PSMessages.getString(
            "common.searchCriteria.label")); //$NON-NLS-1$
      
      // Search criteria panel
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSSearch search = (PSSearch)m_data;
      m_queryEditor = new PSSearchFieldEditorComposite(
         comp, search, factory.getRemoteRequester(),
         PSEditorUtil.getCEFieldCatalog(false), this);      
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_searchCriteriaLabel, 0, SWT.BOTTOM); 
      formData_5.left = new FormAttachment(m_searchCriteriaLabel, 0, SWT.LEFT);
      formData_5.right = new FormAttachment(100, 0);
      formData_5.bottom = new FormAttachment(100, -5);
      m_queryEditor.setLayoutData(formData_5);
      
      final Button customButton = new Button(comp, SWT.PUSH);
      customButton.setText(PSMessages.getString(
         "common.customizeButton.label")); //$NON-NLS-1$
      final FormData formData_6 = new FormData();
      formData_6.right = new FormAttachment(m_queryEditor, -5, SWT.LEFT);
      formData_6.bottom = new FormAttachment(m_queryEditor, 0, SWT.BOTTOM);
      customButton.setLayoutData(formData_6);
      customButton.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(
               @SuppressWarnings("unused") SelectionEvent e)
            {               
               m_queryEditor.onCustomize();
            }
            
         });
      
      comp.setTabList(new Control[]{
         m_commonComp,
         leftComp,
         customButton
         
      });
   }
   
   /**
    * Creates the composite containing controls for a custom search.
    * 
    * @param parent The parent composite, assumed not <code>null</code>.
    * 
    * @return The composite, never <code>null</code>.
    */
   private Composite createCustomLeftComposite(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();
      
      m_customUrlLabel = new Label(comp, SWT.NONE);
      final FormData formData_10 = new FormData();      
      formData_10.top = new FormAttachment(0, 0);
      formData_10.left = new FormAttachment(0, 0);
      m_customUrlLabel.setLayoutData(formData_10);
      m_customUrlLabel.setText(PSMessages.getString(
         "PSSearchEditor.customSearchUrl.label"));  //$NON-NLS-1$
      
      m_customUrlText = new Text(comp, SWT.BORDER);
      final FormData formData_11 = new FormData();
      formData_11.top = new FormAttachment(m_customUrlLabel, 0, SWT.BOTTOM);
      formData_11.left = new FormAttachment(0, 0);
      formData_11.right = new FormAttachment(100, 0);
      m_customUrlText.setLayoutData(formData_11);
      registerControl(
         "PSSearchEditor.customSearchUrl.label",
         m_customUrlText,
         new IPSControlValueValidator[]{required});
      
      m_resultsComp = new PSSearchResultsComposite(comp, SWT.NONE, this);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_customUrlText, 10, SWT.BOTTOM);
      formData_3.right = new FormAttachment(m_customUrlText, 0, SWT.RIGHT);
      formData_3.left = new FormAttachment(0, 0);
      m_resultsComp.setLayoutData(formData_3);
      
      return comp;
   }
   
   /**
    * Creates the composite containing controls for a standard search.
    * 
    * @param parent The parent composite, assumed not <code>null</code>.
    * 
    * @return The composite, never <code>null</code>.
    */
   private Composite createStandardLeftComposite(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      if(m_useExternalSearch)
      {
         m_queryComp = new PSSearchQueryComposite(comp, SWT.NONE, this);
         final FormData formDataQC = new FormData();
         formDataQC.top = new FormAttachment(0, 0);
         formDataQC.left = new FormAttachment(0, 0);
         formDataQC.right = new FormAttachment(100, 0);
         m_queryComp.setLayoutData(formDataQC);
      }
      
      m_resultsComp = new PSSearchResultsComposite(comp, SWT.NONE, this);
      final FormData formData_7 = new FormData();
      if(m_useExternalSearch)
      {
         formData_7.top = new FormAttachment(m_queryComp, 5, SWT.BOTTOM);
      }
      else
      {
         formData_7.top = new FormAttachment(0, 0);
      }
      formData_7.right = new FormAttachment(100, 0);
      formData_7.left = new FormAttachment(0, 0);
      m_resultsComp.setLayoutData(formData_7);

      if (m_useExternalSearch)
      {
         m_displayModeLabel = new Label(comp, SWT.WRAP);
         final FormData formData_10 = new FormData();      
         formData_10.top = new FormAttachment(m_resultsComp,
            COMBO_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
            SWT.BOTTOM);
         formData_10.left = new FormAttachment(0, 0);
         m_displayModeLabel.setLayoutData(formData_10);
         m_displayModeLabel.setText(
            PSMessages.getString("PSSearchEditor.displayMode.label")); //$NON-NLS-1$
         
         m_displayModeComboViewer = new ComboViewer(comp, SWT.READ_ONLY);
         m_displayModeComboViewer.setContentProvider(
            new PSDefaultContentProvider());
         m_displayModeCombo = m_displayModeComboViewer.getCombo();
         final FormData formData_11 = new FormData();
         formData_11.right = new FormAttachment(100, 0);
         formData_11.top = new FormAttachment(m_displayModeLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         formData_11.left = new FormAttachment(
            m_displayModeLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         m_displayModeCombo.setLayoutData(formData_11);
         List<String> modes = new ArrayList<String>();
         modes.add(PSMessages.getString(
            "PSSearchEditor.displayMode.simple.choice")); //$NON-NLS-1$
         modes.add(PSMessages.getString(
            "PSSearchEditor.displayMode.advanced.choice")); //$NON-NLS-1$
         m_displayModeComboViewer.setInput(modes);
         m_displayModeCombo.select(0);// default
         registerControl(
            "PSSearchEditor.displayMode.label",
            m_displayModeCombo,
            null);
      }


      m_UserCustomizableButton = new Button(comp, SWT.CHECK);
      final FormData formData_12 = new FormData();
      if (m_useExternalSearch)
      {
         formData_12.top = new FormAttachment(m_displayModeCombo, 10, 
            SWT.BOTTOM);
      }
      else
      {
         formData_12.top = new FormAttachment(m_resultsComp, 10, 
            SWT.BOTTOM);
      }
      
      formData_12.left = new FormAttachment(0, 0);
      m_UserCustomizableButton.setLayoutData(formData_12);
      m_UserCustomizableButton.setText(
         PSMessages.getString("PSSearchEditor.userCustomize.label")); //$NON-NLS-1$
      registerControl(
         "PSSearchEditor.userCustomize.label",
         m_UserCustomizableButton,
         null);
      
      return comp;
   }


   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
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
      else if(control == m_customUrlText)
      {
         def.setUrl(m_customUrlText.getText());
      }
      else if(control == m_displayModeCombo)
      {
         int idx = m_displayModeCombo.getSelectionIndex();
         String mode = idx == 1 
            ? PSSearch.SEARCH_MODE_ADVANCED 
               : PSSearch.SEARCH_MODE_SIMPLE;
         setSearchProperty(def, PSSearch.PROP_SEARCH_MODE, mode);
      }
      else if(control == m_UserCustomizableButton)
      {
         String uCust = m_UserCustomizableButton.getSelection()
            ? PSSearch.BOOL_YES : PSSearch.BOOL_NO;
         setSearchProperty(def, PSSearch.PROP_USER_CUSTOMIZABLE, uCust);
      }
      else if(control == m_queryEditor)
      {
         m_queryEditor.updateDesignerObject(designObject, control);
      }
      else
      {
         if (m_useExternalSearch)
         {
            m_queryComp.updateDesignerObject(designObject, control);
         }
         
         m_resultsComp.updateDesignerObject(designObject, control);
      }
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#init(
    * org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
    */
   @Override
   public void init(IEditorSite site, IEditorInput input) throws PartInitException
   {
      super.init(site, input);
      PSSearch def = (PSSearch)m_data;
      m_isCustomSearch = def.isCustomSearch();
      m_useExternalSearch = FeatureSet.isFTSearchEnabled() && 
         PSSearch.SEARCH_ENGINE_TYPE_EXTERNAL.equals(def.getProperty(
            PSSearch.PROP_SEARCH_ENGINE_TYPE));
   }
   
   /**
    * Catalogs the list of available display format references.
    *  
    * @return A list of all available display formats, never <code>null</code>.
    */
   public static List<IPSReference> getDisplayFormats()
   {
     
      List<IPSReference> displayFormats = new ArrayList<IPSReference>();
      try
      {
         displayFormats.addAll(
            PSCoreUtils.catalog(PSObjectTypes.UI_DISPLAY_FORMAT, false));
         
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString(
               "PSSearchEditor.error.catalogingDisplayFormats"),   //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return displayFormats;
   } 
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {
      String fts = m_useExternalSearch ? "_fulltext" : "";
      String postfix = m_isCustomSearch ? "custom" : "standard";
      if(postfix.equals("standard"))
         postfix += fts;
      return super.getHelpKey(control) + "_" + postfix;
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description",
            "common.searchCriteria.label",
               "search_criteria",
            "common.customizeButton.label",
               "customize",            
            "PSSearchEditor.customSearchUrl.label",
               "custom_search_url",
            "PSMaxRowsSpinnerComposite.label.max.rows",
               "max_rows_returned",
            "common.displayFormat.label",
               "display_format",
            "PSSearchQueryComposite.searchFor.label",
               "search_for",
            "PSSearchQueryComposite.mode.label",
               "mode",            
            "PSSearchQueryComposite.filterWith.label",
               "filter_with",
            "PSSearchQueryComposite.expansion.label",
               "expansion",
            "PSSearchEditor.displayMode.label",
               "initial_display_mode",
            "PSSearchEditor.userCustomize.label",
               "user_customizable"
               
         });
         if(m_isCustomSearch)
            m_helpHintKeyHelper.addMapping(PSNameLabelDesc.LABEL_TEXT_KEY,
               "custom_search_label");
         else
            m_helpHintKeyHelper.addMapping(PSNameLabelDesc.LABEL_TEXT_KEY,
            "standard_search_label");
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }   
   
   /**
    * A convenience method to set a search property. Will remove
    * the property if the value is <code>null</code> or empty.
    * @param name assumed not <code>null</code> or empty.
    * @param value may be <code>null</code> or empty.
    */
   static void setSearchProperty(PSSearch def, String name, String value)
   {      
      if(StringUtils.isBlank(value))
      {
         def.removeProperty(name, null);
      }
      else
      {
         def.setProperty(name, value);
      }
   }
   
   /**
    * <code>true</code> if editing is a custom search, <code>false</code> if 
    * editing a standard search.  Set during 
    * {@link #init(IEditorSite, IEditorInput)}, not modified otherwise.
    */
   protected boolean m_isCustomSearch;
   
   /**
    * <code>true</code> if the search being edited uses the external (FTS) 
    * search engine, <code>false</code> if not, set during 
    * {@link #init(IEditorSite, IEditorInput)}, never modified after that.
    */
   private boolean m_useExternalSearch;
   
   // Controls
   private PSNameLabelDesc m_commonComp;
   private Label m_searchCriteriaLabel;
   private Label m_customUrlLabel;
   private Text m_customUrlText;   
   private Button m_UserCustomizableButton;
   private Combo m_displayModeCombo;
   private ComboViewer m_displayModeComboViewer;
   private Label m_displayModeLabel;   

   private PSSearchFieldEditorComposite m_queryEditor;
   
   /**
    * Composite containing controls for editing the external (FTS) search 
    * values, intialized during {@link #createControl(Composite)}, will be
    * <code>null</code> if {@link #m_useExternalSearch} is <code>false</code>.
    */
   private PSSearchQueryComposite m_queryComp = null;

   /**
    * Composite containing controls for editing the search result settings,
    * intialized during {@link #createControl(Composite)}, never 
    * <code>null</code> or modified after that.
    */
   private PSSearchResultsComposite m_resultsComp;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
}
