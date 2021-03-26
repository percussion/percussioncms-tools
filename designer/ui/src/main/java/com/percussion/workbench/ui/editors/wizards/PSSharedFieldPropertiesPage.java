/******************************************************************************
 *
 * [ PSSharedFieldPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PSSharedFieldPropertiesPage extends PSWizardPageBase
      implements
         IPSUiConstants
{

   /**
    * Contructor for creating the Shared field wizard properties page.
    */
   public PSSharedFieldPropertiesPage() {
      super(PSMessages.getString("PSSharedFieldPropertiesPage.page.name"), //$NON-NLS-1$
            PSMessages.getString("PSSharedFieldPropertiesPage.page.title"), null); //$NON-NLS-1$
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object,
    *      java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSContentEditorSharedDef shDef = (PSContentEditorSharedDef) designObject;
      if (control == m_groupNameText)
      {
         PSSharedFieldGroup group = new PSSharedFieldGroup(m_groupNameText
               .getText(), m_fileNameText.getText());
         PSFieldSet set = new PSFieldSet(m_groupNameText.getText());
         set.setType(PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
         set.setRepeatability(PSFieldSet.REPEATABILITY_ONE_OR_MORE);
         set.setSequencingSupported(false);
         group.setFieldSet(set);
         PSDisplayMapper mapper = new PSDisplayMapper(m_groupNameText.getText());
         PSUIDefinition uiDef = new PSUIDefinition(mapper);
         group.setUIDefinition(uiDef);
         try
         {
            PSTableLocator tloc = PSContentEditorDefinition
                  .getSystemTableLocator();
            PSTableSet tset = new PSTableSet(tloc, new PSTableRef("CT_SH_" //$NON-NLS-1$
                  + StringUtils.upperCase(m_groupNameText.getText())));
            PSCollection tableSets = new PSCollection(PSTableSet.class);
            tableSets.add(tset);
            PSContainerLocator loc = new PSContainerLocator(tableSets);
            group.setLocator(loc);
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin.handleException("Shared Def Wizard", //$NON-NLS-1$
                  PSMessages.getString("PSSharedFieldPropertiesPage.error.title.systemtablelocator"), //$NON-NLS-1$
                  PSMessages.getString("PSSharedFieldPropertiesPage.error.message.systemtablelocator"), //$NON-NLS-1$
                  e);
         }
         PSCollection groupColl = new PSCollection(PSSharedFieldGroup.class);
         groupColl.add(group);
         shDef.setFieldGroups(groupColl);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   @SuppressWarnings("unchecked")
   public void createControl(Composite parent)
   {
      final Composite mainComp = new Composite(parent, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      PSControlValidatorFactory vFactory = PSControlValidatorFactory
            .getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();

      final FormData fd = new FormData();
      fd.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      fd.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      fd.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      mainComp.setLayoutData(fd);

      final Label fileName = new Label(mainComp, SWT.RIGHT);
      final FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      fd1.top = new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      fileName.setLayoutData(fd1);
      fileName.setText(PSMessages.getString(
         "PSSharedFieldPropertiesPage.label.filename")); //$NON-NLS-1$

      m_fileNameText = new Text(mainComp, SWT.BORDER);
      final FormData fd2 = new FormData();
      fd2.left = new FormAttachment(fileName, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      fd2.right = new FormAttachment(100, 0);
      fd2.top = new FormAttachment(fileName,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_fileNameText.setLayoutData(fd2);

      registerControl("PSSharedFieldPropertiesPage.label.filename", m_fileNameText,
            new IPSControlValueValidator[]
            {required,new SharedDefFilenameValidator()}, PSControlInfo.TYPE_NAME);

      final Label groupName = new Label(mainComp, SWT.RIGHT);
      final FormData fd3 = new FormData();
      fd3.left = new FormAttachment(0, 0);
      fd3.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      fd3.top = new FormAttachment(fileName, LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      groupName.setLayoutData(fd3);
      groupName.setText(PSMessages.getString(
         "PSSharedFieldPropertiesPage.label.groupname")); //$NON-NLS-1$

      m_groupNameText = new Text(mainComp, SWT.BORDER);
      final FormData fd4 = new FormData();
      fd4.left = new FormAttachment(groupName, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      fd4.right = new FormAttachment(100, 0);
      fd4.top = new FormAttachment(groupName,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      m_groupNameText.setLayoutData(fd4);

      registerControl("PSSharedFieldPropertiesPage.label.groupname", m_groupNameText,
            new IPSControlValueValidator[]
            {required});
      
      //Build the existing file names
      m_fileNames = new HashSet();
      try
      {
         PSContentEditorSharedDef shDef = PSContentEditorDefinition.getSharedDef();
         Iterator iter = shDef.getFieldGroups();
         while(iter.hasNext())
         {
            PSSharedFieldGroup gr = (PSSharedFieldGroup) iter.next();
            m_fileNames.add(StringUtils.lowerCase(gr.getFilename()));
         }
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException("Shared Def Wizard", //$NON-NLS-1$
               PSMessages.getString("PSSharedFieldPropertiesPage.error.title.shareddefcatalog"), //$NON-NLS-1$
               PSMessages.getString("PSSharedFieldPropertiesPage.error.message.shareddefcatalog"), //$NON-NLS-1$
               e);
      }
      
      setControl(mainComp);
   }

   /**
    * Conveneient inner class to validate the shared def file names.
    * 
    */
   private class SharedDefFilenameValidator implements IPSControlValueValidator
   {
      /*
       * (non-Javadoc)
       * 
       * @see com.percussion.workbench.ui.validators.IPSControlValueValidator#validate(com.percussion.workbench.ui.util.PSControlInfo)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public String validate(PSControlInfo controlInfo)
      {
         if (controlInfo == null)
         {
            throw new IllegalArgumentException("The control cannot be null."); //$NON-NLS-1$
         }
         if (!(controlInfo.getControl() instanceof Text))
         {
            throw new IllegalArgumentException(
                  "The control must be an instance of the Text control."); //$NON-NLS-1$
         }
         String text = ((Text) controlInfo.getControl()).getText();
         if (StringUtils.isBlank(text))
         {
            return null;
         }
         text = StringUtils.lowerCase(StringUtils.trim(text));
         String invalidCharsMsg = PSMessages.getString("PSSharedFieldPropertiesPage.error.message.invalidchars"); //$NON-NLS-1$
         String invalidChars = "\\ / * ? \" < > |"; //$NON-NLS-1$
         if (text.contains("\\") || text.contains("/") || text.contains(":") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
               || text.contains("*") || text.contains("?") //$NON-NLS-1$ //$NON-NLS-2$
               || text.contains("\"") || text.contains("<") //$NON-NLS-1$ //$NON-NLS-2$
               || text.contains(">") || text.contains("|")) //$NON-NLS-1$ //$NON-NLS-2$
            return invalidCharsMsg + invalidChars;
         // Check whether a file already exists with this name
         if (!text.endsWith(PSSharedDefFileWizard.XML_EXTENSION))
            text += PSSharedDefFileWizard.XML_EXTENSION;
         String dupFileMsg = PSMessages.getString("PSSharedFieldPropertiesPage.error.message.duplicatefilenames"); //$NON-NLS-1$
         if (m_fileNames.contains(text))
            return dupFileMsg;
         return null;
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
            "PSSharedFieldPropertiesPage.label.filename",
               "shared_field_definition_file_name",
            "PSSharedFieldPropertiesPage.label.groupname",
               "first_group_name"
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
    * @return  gets the file name Text control.
    */
   public Text getFileNameText()
   {
      return m_fileNameText;
   }
   
   /**
    * Filename text control
    */
   private Text m_fileNameText;

   /**
    * Group name text control
    */
   private Text m_groupNameText;
   
   /**
    * A member variable to hold the existing filenames.
    */
   private Set<String> m_fileNames;
   
}
