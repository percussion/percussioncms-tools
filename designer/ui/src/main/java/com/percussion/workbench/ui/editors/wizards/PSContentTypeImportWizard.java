/*******************************************************************************
 *
 * [ PSContentTypeImportWizard.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.security.acl.NotOwnerException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Imports content types from files.
 * 
 * @author Andriy Palamarchuk
 */
public class PSContentTypeImportWizard extends PSImportWizardBase
{
   // see base class
   @Override
   protected void fromXml(Object o, String xml)
         throws IOException, SAXException, PSUnknownNodeTypeException
   {
      final PSItemDefinition itemDef = (PSItemDefinition) o;
      final PSContentEditor editor = itemDef.getContentEditor();
      final int id = editor.getId();
      final long contentType = editor.getContentType();

      final Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new StringReader(xml), false);
      editor.fromXml(doc.getDocumentElement(), null, null);
      editor.setId(id);
      editor.setContentType(contentType);
   }
   
   /**
    * Makes sure content type has privileges to see the workflows.
    * @inheritDoc
    */
   @Override
   protected void updateAccessPrivileges(Object o)
      throws PSModelException, NotOwnerException
   {
      super.updateAccessPrivileges(o);
      final PSItemDefinition itemDef = (PSItemDefinition) o;
      final Set<IPSReference> newCommunities =
            findCommunitiesForWorkflows(itemDef);
      addCommunitiesToContentType(getItemDefRef(itemDef), newCommunities);
   }



   /**
    * Gives the specified communities access to the content type.
    * 
    * @param ref the content type reference to add communities access to.
    * Assumed not <code>null</code>.
    * 
    * @param newCommunities the communities to access to. Assumed not
    * <code>null</code>.
    * 
    * @throws PSModelException on model failure.
    * @throws NotOwnerException if the caller principal is not an owner of this
    * ACL.
    */
   private void addCommunitiesToContentType(final IPSReference ref,
         final Set<IPSReference> newCommunities)
         throws PSModelException, NotOwnerException
   {
      final IPSAcl acl = (IPSAcl) getModel().loadAcl(ref, true);
      for (final IPSReference communityRef : newCommunities)
      {
         final IPSAclEntry aclEntry =
            acl.createEntry(communityRef.getName(), PrincipalTypes.COMMUNITY);
         aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
         acl.addEntry(acl.getFirstOwner(), aclEntry);
      }
      getModel().saveAcl(ref, true);
   }

   /**
    * Finds all the communities the editor workflows are visible to.
    * @param itemDef the content type containing the wokflows.
    * Assumed not <code>null</code>.
    * @return communities the editor workflows are visible to.
    * Not <code>null</code>.
    * @throws PSModelException on model failure.
    */
   private Set<IPSReference> findCommunitiesForWorkflows(
         final PSItemDefinition itemDef) throws PSModelException
   {
      final List<IPSReference> communities =
            PSSecurityUtils.getVisibleCommunities(getItemDefRef(itemDef));
      // communities to add
      final Set<IPSReference> newCommunities = new HashSet<IPSReference>();
      
      final Set<IPSReference> visibleWorkflows = communities.isEmpty()
         ? Collections.<IPSReference>emptySet() : PSSecurityUtils
            .getObjectsByCommunityVisibility(communities, PSTypeEnum.WORKFLOW);

      // add *all* workflow communities for *all* inaccessible workflows
      final PSContentEditor editor = itemDef.getContentEditor();
      for (final Object workflowIdObj : 
         editor.getWorkflowInfo().getWorkflowIds())
      {
         final int workflowId = (Integer) workflowIdObj;
         final IPSReference workflowRef = getWorkflowModel().getReference(
               new PSDesignGuid(PSTypeEnum.WORKFLOW, workflowId));
         if (!visibleWorkflows.contains(workflowRef))
         {
            final List<IPSReference> workflowCommunities =
                  PSSecurityUtils.getVisibleCommunities(workflowRef);
            newCommunities.addAll(workflowCommunities);
         }
      }
      
      newCommunities.removeAll(communities);
      return newCommunities;
   }

   /**
    * This implementation returns hardcoded value for object type.
    */
   @Override
   protected PSObjectType findObjectType(
         @SuppressWarnings("unused") String xml)
   {
      return new PSObjectType(PSObjectTypes.CONTENT_TYPE);
   }

   /**
    * Not used because this class overloads {@link #findObjectType(String)}.
    */
   @Override
   protected Object createTemp()
   {
      throw new AssertionError("Not used");
   }

   /**
    * Generates a ref for the provided item definition.
    * @param itemDef the object to generate ref for.
    * Assumed not <code>null</code>.
    * @return the reference for the provided object. Not <code>null</code>.
    * @throws PSModelException on model failure.
    */
   private IPSReference getItemDefRef(final PSItemDefinition itemDef) 
      throws PSModelException
   {
      return getModel().getReference(itemDef.getGUID());
   }

   /**
    * Gets workflow mode.
    * @return workflow model. Never <code>null</code>.
    * @throws PSModelException if model loading fails.
    */
   private IPSCmsModel getWorkflowModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(PSObjectTypes.WORKFLOW);
   }

   // see base class
   @Override
   protected String getMessagePrefix()
   {
      return "PSContentTypeImportWizard.";
   }

   // see base class
   @Override
   protected String getPageImage()
   {
      return "contentType.gif";
   }

   // see base class
   @Override
   protected String getFileExtension()
   {
      return CONTENT_TYPE_SUFFIX;
   }

   // see base class
   @Override
   protected PSObjectTypes getPrimaryType()
   {
      return PSObjectTypes.CONTENT_TYPE;
   }

   /**
    * Content type file extension.
    */
   public static final String CONTENT_TYPE_SUFFIX = ".contentType";
}
