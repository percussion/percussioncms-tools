/******************************************************************************
 *
 * [ PSSlotWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.workbench.ui.PSMessages;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:44:01 PM
 */
public class PSSlotWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSSlotWizard()
   {
      super(new PSObjectType(PSObjectTypes.SLOT));
   }

   /* 
    * @see org.eclipse.jface.wizard.IWizard#addPages()
    */
   @Override
   public void addPages()
   {
      setWindowTitle(PSMessages.getString("PSSlotWizard.title")); //$NON-NLS-1$
      PSSlotPropertiesPage page = new PSSlotPropertiesPage();
      addPage(page);
   }

   /**
    * Overriden to add default relation type value to slot object
    * @see com.percussion.workbench.ui.editors.wizards.PSWizardBase#
    * postObjectCreationOperations(java.lang.Object)
    */
   @Override
   protected void postObjectCreationOperations(Object data)
   {
      PSTemplateSlot slot = (PSTemplateSlot)data;
      // Default relationship name to ActiveAssembly      
      slot.setRelationshipName(
            PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
   }   
   
   
}
