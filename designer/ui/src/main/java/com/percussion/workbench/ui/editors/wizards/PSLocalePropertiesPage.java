/******************************************************************************
 *
 * [ PSLocalePropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.i18n.PSLocale;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:55 PM
 */
public class PSLocalePropertiesPage extends PSWizardPageBase
      implements
         IPSUiConstants,
         SelectionListener
{

   /**
    * Constructor to create a new locale proeprty page.
    */
   public PSLocalePropertiesPage()
   {
      super(
            PSMessages.getString("PSLocalePropertiesPage.label.new.locale"),
            PSMessages.getString("PSLocalePropertiesPage.label.create.new.locale"), null); //$NON-NLS-1$ //$NON-NLS-2$
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
      final Composite mainComp = new Composite(parent, SWT.NONE);
      mainComp.setLayout(new FormLayout());

      final FormData fd = new FormData();
      fd.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      fd.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      fd.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      mainComp.setLayoutData(fd);

      final Label langNameLabel = new Label(mainComp, SWT.RIGHT);
      final FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      fd1.top = new FormAttachment(0,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      langNameLabel.setLayoutData(fd1);
      langNameLabel.setText(PSMessages
            .getString("PSLocalePropertiesPage.label.language")); //$NON-NLS-1$

      m_langCombo = new Combo(mainComp, SWT.READ_ONLY);
      final FormData fd1a = new FormData();
      fd1a.left = new FormAttachment(langNameLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      fd1a.right = new FormAttachment(100, 0);
      fd1a.top = new FormAttachment(langNameLabel, 0, SWT.TOP);
      m_langCombo.setLayoutData(fd1a);
      m_langCombo.addSelectionListener(this);
      registerControlHelpOnly("PSLocalePropertiesPage.label.language", 
         m_langCombo);

      final Label countryNameLabel = new Label(mainComp, SWT.RIGHT);
      final FormData fd2 = new FormData();
      fd2.left = new FormAttachment(langNameLabel, 0, SWT.LEFT);
      fd2.right = new FormAttachment(langNameLabel, 0, SWT.RIGHT);
      fd2.top = new FormAttachment(langNameLabel, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      countryNameLabel.setLayoutData(fd2);
      countryNameLabel.setText(PSMessages
            .getString("PSLocalePropertiesPage.label.country")); //$NON-NLS-1$

      m_countryCombo = new Combo(mainComp, SWT.READ_ONLY);
      final FormData fd2a = new FormData();
      fd2a.left = new FormAttachment(countryNameLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      fd2a.right = new FormAttachment(100, 0);
      fd2a.top = new FormAttachment(countryNameLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_countryCombo.setLayoutData(fd2a);
      m_countryCombo.addSelectionListener(this);
      registerControlHelpOnly("PSLocalePropertiesPage.label.country",
         m_countryCombo);
      m_nameLabelComp = new PSNameLabelDesc(mainComp, SWT.NONE, "", //$NON-NLS-1$
            WIZARD_LABEL_NUMERATOR, PSNameLabelDesc.SHOW_ALL, this);

      m_nameLabelComp.getNameLabel().setVisible(false);
      m_nameText = (Text) m_nameLabelComp.getNameText();
      m_nameText.setVisible(false);

      final FormData fd3 = new FormData();
      fd3.left = new FormAttachment(0, 0);
      fd3.right = new FormAttachment(100, 0);
      fd3.top = new FormAttachment(countryNameLabel, 0, SWT.BOTTOM);
      m_nameLabelComp.setLayoutData(fd3);

      // Enabled check box
      m_enableChkBox = new Button(mainComp, SWT.CHECK);
      final FormData fd4 = new FormData();
      fd4.left = new FormAttachment(WIZARD_LABEL_NUMERATOR, LABEL_HSPACE_OFFSET);
      fd4.right = new FormAttachment(100, 0);
      fd4.top = new FormAttachment(m_nameLabelComp, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      m_enableChkBox.setLayoutData(fd4);
      m_enableChkBox.setText(PSMessages
            .getString("PSLocalePropertiesPage.label.enabled")); //$NON-NLS-1$

      registerControl("PSLocalePropertiesPage.label.enabled", m_enableChkBox, null);

      // Load the control values
      loadControlValues();

      setControl(mainComp);
   }

   /**
    * A convenient method to load the control values for this wizard.
    * 
    */
   @SuppressWarnings("unchecked")
   private void loadControlValues()
   {
      // Catalog locales
      try
      {
         List<IPSReference> extLocales = PSCoreUtils.catalog(
               PSObjectTypes.LOCALE, true);
         List<String> langStrings = new ArrayList<String>();
         for (IPSReference ref : extLocales)
         {
            langStrings.add(ref.getName());
         }
         Locale[] supLocales = Locale.getAvailableLocales();
         for (Locale loc : supLocales)
         {
            String lngStr = loc.getLanguage();
            String cntStr = loc.getCountry();

            // If country string is not empty build PSLocale name representation
            // of locale
            String langStr = StringUtils.isEmpty(cntStr) ? lngStr : lngStr
                  + "-" + StringUtils.lowerCase(cntStr); //$NON-NLS-1$
            // Check whether it exists
            if (langStrings.contains(langStr))
               continue;

            if (m_langMap.containsKey(loc.getDisplayLanguage()))
            {
               Map<String, Locale> cMap = m_langMap.get(loc
                     .getDisplayLanguage());
               cMap.put(loc.getDisplayCountry(), loc);
            }
            else
            {
               Map<String, Locale> cMap = new HashMap<String, Locale>();
               cMap.put(loc.getDisplayCountry(), loc);
               m_langMap.put(loc.getDisplayLanguage(), cMap);
            }
         }
         String[] langs = m_langMap.keySet().toArray(new String[0]);
         Arrays.sort(langs, String.CASE_INSENSITIVE_ORDER);

         m_langCombo.setItems(langs);
         m_langCombo.select(0);

         String[] cnts = (String[]) m_langMap.get(m_langCombo.getText())
               .keySet().toArray(new String[0]);
         Arrays.sort(cnts, String.CASE_INSENSITIVE_ORDER);

         m_countryCombo.setItems(cnts);
         m_countryCombo.select(0);
         setNameText();
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin
               .handleException(
                     PSMessages
                           .getString("PSLocalePropertiesPage.label.locale.wizard"), PSMessages.getString("PSLocalePropertiesPage.label.locales.catalog"), //$NON-NLS-1$ //$NON-NLS-2$
                     PSMessages
                           .getString("PSLocalePropertiesPage.label.failed.catalog.locales"), e); //$NON-NLS-1$
      }
   }

   /*
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    *      updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSLocale locale = (PSLocale) designObject;
      if (control == m_nameLabelComp.getLabelText())
      {
         locale.setDisplayName(m_nameLabelComp.getLabelText().getText());
      }
      else if (control == m_nameLabelComp.getDescriptionText())
      {
         locale.setDescription(m_nameLabelComp.getDescriptionText().getText());
      }
      else if (control == m_enableChkBox)
      {
         int status = m_enableChkBox.getSelection()
               ? PSLocale.STATUS_ACTIVE
               : PSLocale.STATUS_INACTIVE;
         locale.setStatus(status);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("unchecked")
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_langCombo)
      {
         Map cs = m_langMap.get(m_langCombo.getText());
         String[] cnts = (String[]) cs.keySet().toArray(new String[0]);
         Arrays.sort(cnts, String.CASE_INSENSITIVE_ORDER);
         m_countryCombo.setItems(cnts);
         m_countryCombo.select(0);
         setNameText();
      }
      else if (e.getSource() == m_countryCombo)
      {
         setNameText();
      }
   }

   /**
    * A convenient method to set the name text.
    * 
    */
   private void setNameText()
   {
      Map cs = m_langMap.get(m_langCombo.getText());
      Locale loc = (Locale) cs.get(m_countryCombo.getText());

      String langStr = StringUtils.isEmpty(loc.getCountry()) ? loc
            .getLanguage() : loc.getLanguage()
            + "-" + StringUtils.lowerCase(loc.getCountry()); //$NON-NLS-1$

      m_nameText.setText(langStr);
      String labelText = m_langCombo.getText();
      if (!StringUtils.isEmpty(m_countryCombo.getText()))
         labelText += " " + m_countryCombo.getText(); //$NON-NLS-1$
      m_nameLabelComp.getLabelText().setText(labelText);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(@SuppressWarnings("unused") SelectionEvent e)
   {
      // no-op
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
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label",
            "PSLocalePropertiesPage.label.language",
               "language",
            "PSLocalePropertiesPage.label.country",
               "country",
            "PSLocalePropertiesPage.label.enabled",
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

   /*
    * Controls for this wizard page.
    */
   private Combo m_langCombo;
   private Combo m_countryCombo;
   private Text m_nameText;
   private PSNameLabelDesc m_nameLabelComp;
   private Button m_enableChkBox;

   /*
    * Maps for languages and controls.
    */
   Map<String, Map> m_langMap = new HashMap<String, Map>();
   Map<String, Locale> m_cntMap;
}
