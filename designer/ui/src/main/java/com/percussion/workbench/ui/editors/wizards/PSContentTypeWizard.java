/******************************************************************************
 *
 * [ PSContentTypeWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;


import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Wizard for creating Content Type objects.
 */
public class PSContentTypeWizard extends PSWizardBase
{
   /**
    * Override the implicit ctor to provide the type to the super.
    */
   public PSContentTypeWizard()
   {
      super(new PSObjectType(PSObjectTypes.CONTENT_TYPE));
   }
   
   @Override
   public void addPages()
   {
      setWindowTitle("New Content Type");
      m_propPage = new PSContentTypePropertiesPage();
      addPage(m_propPage);
      m_wfPage = new PSContentTypeWorkflowPage();
      addPage(m_wfPage);
   }
   
   @Override
   protected void loadAclAndCommunityControl()
   {
      super.loadAclAndCommunityControl();
      if(m_wfPage != null)
      {
         List<IPSReference> wfList = m_wfPage.getCommunityWorkflows();
         m_wfPage.updateWorkflows(wfList,new ArrayList<IPSReference>(),null);
      }
      
   }
   
   /**
    * Content type workflow selection wizard page.
    */
   private PSContentTypeWorkflowPage m_wfPage;
   
   /**
    * Content type properties selection wizard page
    */
   private PSContentTypePropertiesPage m_propPage;
}
