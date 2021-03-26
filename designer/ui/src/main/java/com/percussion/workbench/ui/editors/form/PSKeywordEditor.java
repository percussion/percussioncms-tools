/******************************************************************************
 *
 * [ PSKeywordEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.PSKeywordCommonComposite;
import com.percussion.workbench.ui.editors.common.PSKeywordCommonComposite.Choice;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a single paned UI for modifying a keyword design object. 
 * @author erikserating
 */
public class PSKeywordEditor extends PSEditorBase
{

   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return ref.getObjectType().equals(
         new PSObjectType(PSObjectTypes.KEYWORD));
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * loadControlValues()
    */
   public void loadControlValues(Object designObject)
   {
      PSKeyword keyword = (PSKeyword)designObject;
      
      
      // Set the title text to the label name
      ((Label)m_comp.getNameControl()).setText(
         StringUtils.defaultString(keyword.getName()));      
      
      // set description
      m_comp.getDescriptionControl().setText(
         StringUtils.defaultString(keyword.getDescription()));
      
      // set choices table
      List<PSKeywordChoice> kChoices = keyword.getChoices();
      List<Choice> choices = new ArrayList<Choice>();
      for(PSKeywordChoice kc : kChoices)
      {
         choices.add(m_comp.new Choice(kc));
      }
      m_comp.getChoicesTable().setValues(choices);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite comp)
   {
      m_comp = new PSKeywordCommonComposite(comp, SWT.NONE, this);
      registerControl(
         PSNameLabelDesc.DESC_TEXT_KEY,
         m_comp.getDescriptionControl(),
         null);
      registerControl(
         "PSKeywordCommonComposite.label.choices",
         m_comp.getChoicesTable(),
         null); 
      registerControlHelpOnly("PSKeywordCommonComposite.label.value",
         m_comp.getValueControl());

   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked")   
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSKeyword keyword = (PSKeyword)designObject;
      
      // update description
      if(control == m_comp.getDescriptionControl())
      {
         keyword.setDescription(m_comp.getDescriptionControl().getText());
      }
      // update choices
      if(control == m_comp.getChoicesTable())
      {
         List<Choice> values = 
            (List<Choice>)m_comp.getChoicesTable().getValues();
         List<PSKeywordChoice> choices = new ArrayList(keyword.getChoices());
         choices.clear();         
         int seq = 1;
         for(Choice choice : values)
         {
            PSKeywordChoice kc = choice.toKeywordChoice();
            kc.setSequence(seq++);
            choices.add(kc);
         }
         keyword.setChoices(choices);
      }

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
            "PSNameLabelDesc.label.description",
               "description",
            "PSKeywordCommonComposite.label.value",
               "value_(box)",
            "PSKeywordCommonComposite.column.name.label",
               "label",
            "PSKeywordCommonComposite.column.name.value",
               "value_(table)",
            "PSKeywordCommonComposite.column.name.desc",
               "description"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;      
   
   /**
    * Common keyword composite, initialized in {@link #createControl(Composite)}
    * , never <code>null</code> after that.
    */
   private PSKeywordCommonComposite m_comp;

}
