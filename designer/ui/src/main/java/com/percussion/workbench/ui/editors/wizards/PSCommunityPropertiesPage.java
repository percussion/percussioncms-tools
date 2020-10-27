/******************************************************************************
 *
 * [ PSCommunityPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:43:45 PM
 */
public class PSCommunityPropertiesPage extends PSWizardPageBase
   implements IPSUiConstants
{

   
   public PSCommunityPropertiesPage()
   {
      super(PSMessages.getString(
         "PSCommunityPropertiesPage.label.new.community"), //$NON-NLS-1$
         PSMessages.getString(
            "PSCommunityPropertiesPage.label.create.community"), //$NON-NLS-1$
         null);  
   }

   /**
    * @param parent
    */
   public void createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      // This composite takes care of registering its own controls            
      m_commonComp = new PSNameLabelDesc(comp, SWT.NONE, 
         PSMessages.getString("PSCommunityEditor.label.community"), //$NON-NLS-1$
         WIZARD_LABEL_NUMERATOR,
         PSNameLabelDesc.SHOW_NAME |
         PSNameLabelDesc.SHOW_DESC, this);
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      PSControlInfo cInfo = m_controlInfo.get(m_commonComp.getNameText());
      cInfo.addValidator(vFactory.getIllegalCharValidator("&"));
      final FormData formData = new FormData();      
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(formData);
            
      m_assignedRoles = new PSSlushBucket(
         comp, SWT.NONE, PSMessages.getString(
            "PSCommunityEditor.label.available.roles"),  //$NON-NLS-1$
         PSMessages.getString(
            "PSCommunityEditor.label.assigned.roles"),  //$NON-NLS-1$ 
         new PSReferenceLabelProvider());
      m_assignedRoles.setValues(getRoles(), null);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(m_commonComp, 35, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 0);
      formData_1.height = 220;
      m_assignedRoles.setLayoutData(formData_1);
      registerControl(PSMessages.getString(
         "PSCommunityEditor.label.assigned.roles"), //$NON-NLS-1$
         m_assignedRoles,
         null);
      
      setControl(comp);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSCommunity community = (PSCommunity)designObject;
      
      if(control == m_commonComp.getNameText())
      {
         community.setName(((Text)m_commonComp.getNameText()).getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         community.setDescription(m_commonComp.getDescriptionText().getText());
      }
      else if(control == m_assignedRoles)
      {
         Collection<IPSGuid> roles = new ArrayList<IPSGuid>();
         for(IPSReference ref : (List<IPSReference>)m_assignedRoles.getSelections())
         {
            roles.add(ref.getId());
         }
         community.setRoleAssociations(roles);        
      }
   }
   
   /**
    * @return list of <code>IPSReference</code> objects that represent all
    * available roles. Never <code>null</code>, may be empty.
    */
   private List<IPSReference> getRoles()
   {
      List<IPSReference> roles = new ArrayList<IPSReference>();
      try
      {
        roles.addAll(PSCoreUtils.catalog(PSObjectTypes.ROLE, false));
      }
      catch (PSModelException e)
      {         
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      return roles;
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
            PSNameLabelDesc.NAME_TEXT_KEY,
               "community_name",
            "PSCommunityEditor.label.assigned.roles",
               "assigned_roles"
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
    * Controls
    */
   private PSSlushBucket m_assignedRoles;
   private PSNameLabelDesc m_commonComp;

}
