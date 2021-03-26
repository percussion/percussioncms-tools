/******************************************************************************
 *
 * [ PSTemplateWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.models.IPSExtensionModel;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.apache.commons.lang.StringUtils;

/**
 * Wizard creating new template.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSTemplateWizard()
   {
      super(PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
            PSObjectTypes.TemplateSubTypes.SHARED));
   }

   
   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSTemplateWizard.title")); //$NON-NLS-1$
      addPage(new PSTemplateTypePage());      
      addPage(new PSTemplateOutputPage());
      addPage(createPropertiesPage());
      addPage(new PSTemplateTdSettingsPage());
      addPage(new PSTemplateSlotsPage());
      addPage(new PSTemplateContentTypesPage());
   }
    
   /**
    * Creates template properties page.
    */
   private PSTemplatePropertiesPage createPropertiesPage()
   {
      final PSTemplatePropertiesPage propertiesPage = new PSTemplatePropertiesPage();
      propertiesPage.setPageComplete(false);
      return propertiesPage;
   }
   
   /**
    * Default value for the extension model used on this page.
    * <code>null</code> in case of error accessing the model.
    * Reports any errors accessing the model to the user. 
    */
   IPSExtensionModel initializeExtensionModel()
   {
      try
      {
         return (IPSExtensionModel) getCoreFactory().getModel(
               PSObjectTypes.EXTENSION);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
         return null;
      }
   }

   /**
    * The singleton core factory instance. Never <code>null</code>.
    */
   private PSCoreFactory getCoreFactory()
   {
      return PSCoreFactory.getInstance();
   }

   /**
    * Indicates whether it is necessary to skip next Output Format page.
    */
   public boolean skipOutputFormatPage()
   {
      return getTypePage().skipOutputFormatPage();
   }
   
   /**
    * Indicates whether it is necessary to skip next Content Types page.
    */
   public boolean skipContentTypesPage()
   {
      return getTypePage().skipContentTypesPage();
   }
   
   /**
    * Indicates whether the template would have source
    * with the currently selected assembler.
    */
   public boolean hasSource()
   {
      // we skip output page only for templates with source
      if (skipOutputFormatPage())
      {
         return true;
      }
      // just in case
      if (getSelectedSubtype().equals(TemplateSubTypes.VARIANT))
      {
         return false;
      }
      try
      {
         final String fileExt = m_extensionModel.getAssemblerSourceExt(
               getOutputPage().getSelectedAssembler());
         return StringUtils.isNotBlank(fileExt);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
         return false;
      }
   }
   
   /**
    * Indicates that selected template type is for DB
    * publishing.
    * @return <code>true</code> if DB pub type selected.
    */
   public boolean isDbPubType()
   {     
      return getTypePage().isDatabaseTypeSelected();
   }

   /**
    * Returns {@link PSTemplateTypePage} of the wizard. 
    */
   private PSTemplateTypePage getTypePage()
   {
      return (PSTemplateTypePage) getPages()[TYPE_PAGE_IDX];
   }
   
   /**
    * Returns {@link PSTemplateOutputPage} of the wizard.
    * Never <code>null</code>. 
    */
   private PSTemplateOutputPage getOutputPage()
   {
      return (PSTemplateOutputPage) getPages()[OUTPUT_PAGE_IDX];
   }
   
   @Override
   protected PSObjectType getObjectTypeForRef()
   {
      return new PSObjectType(PSObjectTypes.TEMPLATE, getSelectedSubtype());
   }

   /**
    * Returns subtype, currently selected by the user. Never <code>null</code>.
    */
   private TemplateSubTypes getSelectedSubtype()
   {
      return getTypePage().getTypeRadioSelection().getSubType();
   }

   /**
    * Index of the type page in the wizard.
    */
   private static final int TYPE_PAGE_IDX = 0;

   /**
    * Index of the output page in the wizard.
    */
   private static final int OUTPUT_PAGE_IDX = 1;
   
   /**
    * Model used to catalog assemblers.
    * Not changed after initialization, except in unit test.
    * Never <code>null</code>.
    */
   IPSExtensionModel m_extensionModel = initializeExtensionModel();
}
