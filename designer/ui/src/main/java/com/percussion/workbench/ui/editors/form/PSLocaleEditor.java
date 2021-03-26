/******************************************************************************
 *
 * [ PSLocaleEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.i18n.PSLocale;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.Locale;

/**
 * Editor for editing PSLocale objects in workbench. 
 */
public class PSLocaleEditor extends PSEditorBase implements IPSUiConstants
{

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false; // Should never happen
      if(ref.getObjectType().getPrimaryType() == PSObjectTypes.LOCALE)
         return true;
      return false;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite comp)
   {
      m_locale = (PSLocale)m_data;
      Locale loc = getJavaLocale(m_locale);
      String country = loc==null?PSMessages.getString("PSLocaleEditor.label.unknown"):loc.getDisplayCountry(); //$NON-NLS-1$
      String language = loc==null?PSMessages.getString("PSLocaleEditor.label.unknown"):loc.getDisplayLanguage(); //$NON-NLS-1$

      final Composite mainComp = new Composite(comp, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      mainComp.setSize(720,420); 

      //Display the country and language
      final Label langLabel = new Label(mainComp,SWT.NONE);
      final FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(40, 0);
      fd1.top = new FormAttachment(0, 0);
      langLabel.setFont(JFaceResources.getBannerFont());
      langLabel.setLayoutData(fd1);
      langLabel.setText(PSMessages.getString("PSLocaleEditor.label.language")+language); //$NON-NLS-1$
      Label countryLabel = null;
      if (!StringUtils.isBlank(country))
      {
         countryLabel = new Label(mainComp, SWT.NONE);
         countryLabel.setFont(JFaceResources.getBannerFont());
         final FormData fd2 = new FormData();
         fd2.left = new FormAttachment(40, 0);
         fd2.right = new FormAttachment(100, 0);
         fd2.top = new FormAttachment(langLabel, 0,
               SWT.TOP);
         countryLabel.setLayoutData(fd2);
         countryLabel.setText(PSMessages.getString("PSLocaleEditor.label.country") + country); //$NON-NLS-1$
      }
      Label separator = new Label(mainComp, SWT.SEPARATOR | SWT.HORIZONTAL);
      final FormData formDataSep = new FormData();
      formDataSep.top = new FormAttachment(langLabel, 5, SWT.BOTTOM);
      formDataSep.right = new FormAttachment(100, 0);
      formDataSep.left = new FormAttachment(0, 0);
      separator.setLayoutData(formDataSep);

      // Add the common composite that contains label, description
      // fields
      m_commonComp = new PSNameLabelDesc(mainComp, SWT.NONE, PSMessages.getString("PSLocaleEditor.label.locale"), //$NON-NLS-1$
            EDITOR_LABEL_NUMERATOR, PSNameLabelDesc.SHOW_LABEL | PSNameLabelDesc.SHOW_DESC
                  | PSNameLabelDesc.LAYOUT_SIDE, this);
      // Layout all the controls
      final FormData fd3 = new FormData();
      fd3.left = new FormAttachment(0, 0);
      fd3.right = new FormAttachment(100, 0);
      fd3.top = new FormAttachment(separator, BUTTON_VSPACE_OFFSET,SWT.BOTTOM);
      m_commonComp.setLayoutData(fd3);
      // Enabled check box
      m_enableChkBox = new Button(mainComp, SWT.CHECK);
      final FormData fd4 = new FormData();
      fd4.left = new FormAttachment(0, 0);
      fd4.right = new FormAttachment(50, 0);
      fd4.top = new FormAttachment(m_commonComp, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      m_enableChkBox.setLayoutData(fd4);
      m_enableChkBox.setText(PSMessages.getString("PSLocaleEditor.label.enabled")); //$NON-NLS-1$
      
      //Register controls
      registerControl("PSLocaleEditor.label.enabled" ,m_enableChkBox, null);
      
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSLocale locale = (PSLocale) designObject;
      if(control == m_commonComp.getLabelText())
      {
         locale.setDisplayName(m_commonComp.getLabelText().getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         locale.setDescription(m_commonComp.getDescriptionText().getText());
      }
      else if(control == m_enableChkBox)
      {
         
         int status = m_enableChkBox.getSelection()
               ? PSLocale.STATUS_ACTIVE
               : PSLocale.STATUS_INACTIVE;
         locale.setStatus(status);
      }
   }

   
   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSLocale loc = (PSLocale) designObject;
      m_commonComp.getLabelText().setText(loc.getLabel());
      m_commonComp.getDescriptionText().setText(
            StringUtils.defaultString(loc.getDescription()));
      boolean enable = loc.getStatus()==PSLocale.STATUS_ACTIVE?true:false;      
      m_enableChkBox.setSelection(enable);
   }

   /**
    * A convenient method to find the Java Locale object corresponding to
    * the supplied PSLocale object.
    * @param psLoc Object of PSLocale for which the Java Locale needs to be found.
    * @return Locale object corresponding to the PSLocale object or
    * <code>null</code> if could not find or supplied PSLocale object is 
    * <code>null</code>.
    */
   private Locale getJavaLocale(PSLocale psLoc)
   {
      if(psLoc == null)
         return null;
      String[] strSplit = psLoc.getLanguageString().split("-"); //$NON-NLS-1$
      String langStr = strSplit[0];
      String cntStr = strSplit.length >= 2 ? strSplit[1] : ""; //$NON-NLS-1$
      Locale[] locales = Locale.getAvailableLocales();
      for (Locale loc : locales)
      {
         if (loc.getLanguage().equalsIgnoreCase(langStr)
               && loc.getCountry().equalsIgnoreCase(cntStr))
            return loc;
      }
      return null;
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
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label",
            "PSLocaleEditor.label.enabled",
               "enabled"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   // Controls for this editor
   private PSNameLabelDesc m_commonComp;
   private PSLocale m_locale;
   private Button m_enableChkBox;
   

}
