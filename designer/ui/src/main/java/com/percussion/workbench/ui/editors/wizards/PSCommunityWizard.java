/******************************************************************************
 *
 * [ PSCommunityWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:46 PM
 */
public class PSCommunityWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSCommunityWizard()
   {
      super(new PSObjectType(PSObjectTypes.COMMUNITY));
   }
   
   @Override
   public void addPages()
   {
       setWindowTitle("New community");
       addPage(new PSCommunityPropertiesPage());
   }
}
