/******************************************************************************
 *
 * [ PSSearchQueryComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.PSCoreFactory;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.search.PSCommonSearchUtils;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Contains the controls used to edit properties specific to an external search
 * or view.
 */
public class PSSearchQueryComposite extends Composite
   implements
      IPSDesignerObjectUpdater,
      IPSUiConstants
{
   /**
    * Construct the composite, creates the layout and controls.
    * 
    * @param parent The parent composite, may not be <code>null</code>.
    * @param style The style to construct, see 
    * {@link Composite#Composite(Composite, int)} for details.
    * @param editor The parent editor, may not be <code>null</code>.
    */
   public PSSearchQueryComposite(Composite parent, int style, 
      PSEditorBase editor)
   {
      super(parent, style);

      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      
      setLayout(new FormLayout());
      
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      vFactory.getRequiredValidator();
      
      Label searchForLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      searchForLabel.setLayoutData(formData);
      searchForLabel.setText(
         PSMessages.getString("PSSearchQueryComposite.searchFor.label")); //$NON-NLS-1$
      
      m_searchForText = new Text(this, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
      m_searchForText.setTextLimit(100);
      final FormData formData_1 = new FormData();
      formData_1.height = 60;
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(searchForLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 0);
      m_searchForText.setLayoutData(formData_1);
      editor.registerControl(
         "PSSearchQueryComposite.searchFor.label",
         m_searchForText,
         new IPSControlValueValidator[]{
            vFactory.getLengthValidator(-1, 100)});
      
      m_synonymExpansionButton = new Button(this, SWT.CHECK);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_searchForText, 10, 
            SWT.BOTTOM);
      formData_2.left = new FormAttachment(0, 0);
      m_synonymExpansionButton.setLayoutData(formData_2);
      m_synonymExpansionButton.setText(
         PSMessages.getString("PSSearchQueryComposite.synonymExpansion.label")); //$NON-NLS-1$
      editor.registerControl(
         "PSSearchQueryComposite.synonymExpansion.label",
         m_synonymExpansionButton,
         null);
      
      setTabList(new Control[]{
         m_searchForText,
         m_synonymExpansionButton});        
   }
   
   /* (non-Javadoc)
    * @see IPSDesignerObjectUpdater#updateDesignerObject(Object, Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSSearch def = (PSSearch)designObject;

      if(control == m_searchForText)
      {         
         PSSearchEditor.setSearchProperty(def, PSSearch.PROP_FULLTEXTQUERY,
            m_searchForText.getText());
      }
      else if(control == m_synonymExpansionButton)
      {
         PSSearchEditor.setSearchProperty(def, 
               PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
               m_synonymExpansionButton.getSelection() ?
                     PSCommonSearchUtils.BOOL_YES :
                        PSCommonSearchUtils.BOOL_NO);
      }
   }

   /* (non-Javadoc)
    * @see IPSDesignerObjectUpdater#loadControlValues(Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSSearch def = (PSSearch)designObject;

      // set search for
      m_searchForText.setText(StringUtils.defaultString(
         def.getProperty(PSSearch.PROP_FULLTEXTQUERY)));
      
      // set synonym expansion
      boolean synExp = false;
      if (!def.hasProperty(PSCommonSearchUtils.PROP_SYNONYM_EXPANSION))
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         PSObjectStore os = new PSObjectStore(factory.getDesignerConnection());
         try
         {
            synExp = 
               os.getServerConfiguration().getSearchConfig().
                  isSynonymExpansionRequired();
         }
         catch (Exception e)
         {
            PSUiUtils.handleExceptionSync("Loading server configuration", 
                  null, null, e);
         }
      }
      else
      {
         synExp = def.doesPropertyHaveValue(
               PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
               PSCommonSearchUtils.BOOL_YES);
      }
      
      m_synonymExpansionButton.setSelection(synExp);
   }

   /**
    * Control for the search for text, never <code>null</code> or modified
    * after construction.
    */
   private Text m_searchForText;
   
   /**
    * Control for the expand query with synonyms setting, never
    * <code>null</code> or modified after contruction.
    */
   private Button m_synonymExpansionButton;
}

