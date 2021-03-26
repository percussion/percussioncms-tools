/******************************************************************************
 *
 * [ PSCommunityEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The community editor
 */
public class PSCommunityEditor extends PSEditorBase
{

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#isValidReference(
    * com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false; // Should never happen
      if(ref.getObjectType().getPrimaryType() == PSObjectTypes.COMMUNITY)
         return true;
      return false;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#loadControlValues()
    */
   public void loadControlValues(Object designObject)
   {
      PSCommunity community = (PSCommunity)designObject;
      
      // Set name
      ((Label)m_commonComp.getNameText()).setText(community.getName());
      
      // Set description
      String desc = community.getDescription();
      if (desc == null)
         desc = StringUtils.EMPTY;
      m_commonComp.getDescriptionText().setText(desc);
      
      // Set assigned roles
      List<IPSReference> roles = getRoles();
      List<IPSReference> assignedRoles = new ArrayList<IPSReference>();
      for(IPSGuid guid : community.getRoleAssociations())
      {
         IPSReference ref = PSUiUtils.getReferenceByGuid(roles, guid);
         if(ref != null)
            assignedRoles.add(ref);
      }
      m_assignedRoles.setValues(roles, assignedRoles);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#createControl(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      //This composite takes care of registering its own controls    
      m_commonComp = new PSNameLabelDesc(comp, SWT.NONE, 
         PSMessages.getString("PSCommunityEditor.label.community"), //$NON-NLS-1$
         0,
         PSNameLabelDesc.SHOW_NAME |
         PSNameLabelDesc.SHOW_DESC |
         PSNameLabelDesc.NAME_READ_ONLY |
         PSNameLabelDesc.LAYOUT_SIDE, this);
      final FormData formData = new FormData();      
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(formData);      
      
      m_assignedRoles = new PSSlushBucket(
         comp, SWT.NONE, PSMessages.getString(
            "PSCommunityEditor.label.available.roles"), //$NON-NLS-1$
            PSMessages.getString("PSCommunityEditor.label.assigned.roles"),  //$NON-NLS-1$
         new PSReferenceLabelProvider());
      
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(95, 0);
      formData_1.top = new FormAttachment(m_commonComp, 35, SWT.BOTTOM);
      formData_1.left = new FormAttachment(5, 0);
      formData_1.height = 220;
      m_assignedRoles.setLayoutData(formData_1);
      registerControl(PSMessages.getString(
         "PSCommunityEditor.label.assigned.roles"), //$NON-NLS-1$
         m_assignedRoles,
         null);

   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSCommunity community = (PSCommunity)designObject;
      
      if(control == m_commonComp.getDescriptionText())
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
            "PSNameLabelDesc.label.description",
               "description"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /** 
    * Controls
    */
   private PSSlushBucket m_assignedRoles;
   private PSNameLabelDesc m_commonComp;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

}
