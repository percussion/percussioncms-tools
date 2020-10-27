/******************************************************************************
 *
 * [ PSViewEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.FeatureSet;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

public class PSViewEditor extends PSEditorBase
{
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#isValidReference(
    * com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false;
      return ref.getObjectType().equals(
            PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW,
                  PSObjectTypes.SearchSubTypes.STANDARD))
            || ref.getObjectType().equals(
                  PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW,
                        PSObjectTypes.SearchSubTypes.CUSTOM));
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      if(m_isCustomView)
      {
         m_comp.loadControlValues(designObject);
      }
      else
      {
         m_comp.loadControlValues(designObject);
      }

   }

   @Override
   public void createControl(Composite comp)
   {
      
      if(m_isCustomView)
         m_comp = new PSViewCustomComposite(comp, SWT.NONE, this);
      else
         m_comp = new PSViewStandardComposite(comp, this, m_useExternalSearch);

   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_comp.updateDesignerObject(designObject, control);
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
      m_isCustomView = def.isCustomView();
      m_useExternalSearch = FeatureSet.isFTSearchEnabled() && 
         PSSearch.SEARCH_ENGINE_TYPE_EXTERNAL.equals(def.getProperty(
            PSSearch.PROP_SEARCH_ENGINE_TYPE));
   }   
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {
      String postfix = m_isCustomView ? "custom" : "standard";
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
            "common.customizeButton.label",
               "customize",   
            "common.displayFormat.label",
               "display_format",
            "PSViewParentCategoryComposite.parentCategory.label",
               "parent_category",
            "common.searchCriteria.label",
               "sear_criteria",
            "PSMaxRowsSpinnerComposite.label.max.rows",
               "max_rows_returned",
            "PSViewCustomComposite.customViewUrl.label",
               "custom_view_url", 
            "PSViewCustomComposite.parameters.label",
               "parameters"   
         });
         if(m_isCustomView)
            m_helpHintKeyHelper.addMapping(PSNameLabelDesc.LABEL_TEXT_KEY,
               "custom_view_label");
         else
            m_helpHintKeyHelper.addMapping(PSNameLabelDesc.LABEL_TEXT_KEY,
               "standard_view_label");
            
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Flag indicating that this is a custom view. Set in 
    * {@link #init(IEditorSite, IEditorInput)}
    */
   private boolean m_isCustomView;
   
   private IPSDesignerObjectUpdater m_comp;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   /**
    * <code>true</code> if the search being edited uses the external (FTS) 
    * search engine, <code>false</code> if not, set during 
    * {@link #init(IEditorSite, IEditorInput)}, never modified after that.
    */
   private boolean m_useExternalSearch;

}
