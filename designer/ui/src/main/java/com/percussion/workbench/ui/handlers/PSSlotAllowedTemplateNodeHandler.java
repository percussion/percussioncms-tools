/******************************************************************************
 *
 * [ PSSlotAllowedTemplateNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSTemplateModel;
import com.percussion.client.models.PSLockException;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.security.PSPermissions;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.editors.dialog.PSIncludedContentTypesDialog;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Handler for the node that contains the templates that the slot ancestor is
 * associated with. It expects a tree structure like the following:
 * <pre>
 * ....
 *    - slot instance
 *       - Allowed Content Types (folder)
 *          - ctype instance
 *             -Allowed Templates (folder)
 *                - template instance
 * </pre>
 * If a template is dropped on the 'Allowed Content Types' folder, a dialog is
 * raised allowing the user to choose content types. If dropped on the 
 * 'Allowed Templates' folder, they are added using the ancestor content type.
 * (The actual node names are irrelevant.)
 * 
 * @author paulhoward
 */
public class PSSlotAllowedTemplateNodeHandler extends PSLinkNodeHandler
{
   /**
    * The only ctor, signature required by framework. Parameters passed to 
    * base class.
    */
   public PSSlotAllowedTemplateNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * @inheritDoc
    * 
    * @return <code>true</code> if the node wraps a design object, otherwise
    * false.
    */
   @Override
   public boolean supportsDelete(PSUiReference node)
   {
      if (node.getObjectType() == null)
         return false;
      
      IPSPrimaryObjectType t = (IPSPrimaryObjectType) node.getObjectType()
            .getPrimaryType();
      assert (t.equals(PSObjectTypes.TEMPLATE) 
            || t.equals(PSObjectTypes.CONTENT_TYPE));
      return true;
   }
   
   /**
    * This is overridden because we can't use the standard 
    * {@link #doDeleteAssociations(IPSReference, Collection)} method. Instead 
    * we call the {@link #doDeleteAssociations(PSPair, Collection)} which 
    * takes a pair of references including the slot and content type by which
    * to match up the deletes.
    */
   @Override
   public void handleDelete(final Collection<PSUiReference> nodes)
   {
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
      {
         public void run()
         {
            if (nodes == null || nodes.isEmpty())
               return;
      
            try
            {
               // group associated refs together by parent pair
               Map<PSPair, Collection<IPSReference>> categorizedRefs = 
                  new HashMap<PSPair, Collection<IPSReference>>();
               for (PSUiReference node : nodes)
               {
                  PSUiReference parent = node.getParentNode();
                  /*
                   * Get the slot from which to remove the template 
                   * association, must never be <code>null</code>.
                   */
                  PSUiReference slot = getAncestor(parent, true);
                  assert(slot != null);
                  IPSReference slotRef = slot.getReference();
                  /*
                   * Get the content type from which to remove the template
                   * association, may be <code>null</code> if the content
                   * type node was deleted (meaning all template associations 
                   * for that content type will be removed).
                   */
                  PSUiReference contentType = getAncestor(parent, false);
                  IPSReference contentTypeRef = contentType == null ? 
                     null : contentType.getReference();
         
                  // make sure the slot is not currently edited
                  IPSCmsModel model = PSCoreFactory.getInstance().getModel(
                     slotRef.getObjectType().getPrimaryType());
                  if (checkIfEditing(model, slotRef))
                     return;
                  
                  PSPair<IPSReference, IPSReference> refPair = 
                     new PSPair<IPSReference, IPSReference>(slotRef, 
                        contentTypeRef);
                  Collection<IPSReference> refs = categorizedRefs.get(refPair);
                  if (refs == null)
                  {
                     refs = new ArrayList<IPSReference>();
                     categorizedRefs.put(refPair, refs);
                  }
                  refs.add(node.getReference());
               }
               
               for (PSPair<IPSReference, IPSReference> refPair : 
                  categorizedRefs.keySet())
               {
                  Collection<IPSReference> deletedRefs = doDeleteAssociations(
                     refPair, categorizedRefs.get(refPair));
                  Iterator<PSUiReference> iter = nodes.iterator();
                  while (iter.hasNext())
                  {
                     if (!deletedRefs.contains(iter.next().getReference()))
                        iter.remove();
                  }
               }
            }
            catch (Exception e)
            {
               String title = PSMessages.getString(
                  "PSLinkNodeHandler.error.deleteLink.title");
               Long[] args = 
               {
                  new Long(nodes.size()), 
                  new Long(e.getCause() == null ? 1 : 2)
               };
               String msg = PSMessages.getString(
                  "PSLinkNodeHandler.error.deleteLink.message", 
                  (Object[]) args);
               new PSErrorDialog(PSUiUtils.getShell(), title, msg, e).open();
            }
         }
      });
   }

   //overridden to save target of drop/paste for use by other methods
   @Override
   protected boolean saveAssociations(PSUiReference targetNode,
         Collection<IPSReference> refs)
   {
      m_targetNode = targetNode;
      return super.saveAssociations(targetNode, refs);
   }

   //see base class
   @Override
   protected Collection<IPSReference> doSaveAssociations(IPSReference slotRef, 
         Collection<IPSReference> templateRefs) throws Exception
   {
      if (null == slotRef)
         throw new IllegalArgumentException("slotRef cannot be null");  

      if (null == templateRefs || templateRefs.isEmpty())
         return Collections.emptyList();
      
      IPSCmsModel slotModel = getModel(PSObjectTypes.SLOT);
      boolean success = false;
      try
      {
         IPSTemplateSlot slotData = (IPSTemplateSlot) slotModel.load(
               slotRef, true, false);
         Collection<PSPair<IPSGuid, IPSGuid>> associations = 
            slotData.getSlotAssociations();
         Collection<PSPair<IPSGuid, IPSGuid>> newAssociations = 
            getContentTypeMappings(templateRefs);
         
         //merge the existing w/ the new
         for (Iterator<PSPair<IPSGuid, IPSGuid>> iter = 
            newAssociations.iterator(); iter.hasNext();)
         {
            boolean found = false;
            PSPair<IPSGuid, IPSGuid> toAdd = iter.next();
            for (PSPair<IPSGuid, IPSGuid> existing : associations)
            {
               if (existing.getFirst().equals(toAdd.getFirst()) 
                     && existing.getSecond().equals(toAdd.getSecond()))
               {
                  found = true;
                  break;
               }
            }
            if (found)
               iter.remove();
         }

         associations.addAll(newAssociations);
         slotData.setSlotAssociations(associations);
         slotModel.save(slotRef, true);
         success = true;
         updateContentTypeTemplateAssociations(newAssociations);
         if (getReferenceAncestor(m_targetNode,
               PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE)) == null)
         {
            Collection<IPSReference> results = new ArrayList<IPSReference>();
            IPSCmsModel ctypeModel = getModel(PSObjectTypes.CONTENT_TYPE);
            for (PSPair<IPSGuid, IPSGuid> p : newAssociations)
            {
               results.add(ctypeModel.getReference(p.getFirst()));
            }
            return results;
         }
         return templateRefs;
      }
      finally
      {
         if (!success)
            slotModel.releaseLock(slotRef);
      }
   }
   
   /**
    * Each content type/template pair in the supplied set is checked against the
    * known associations and if it is not found, it is added.
    * 
    * @param pairs For each pair, the first should be the guid of a content
    * type, the 2nd the guid of a template. Assumed not <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    * @throws PSLockException If the contenttype/template set is already locked.
    * @throws PSMultiOperationException if any errors while setting associations.
    */
   private void updateContentTypeTemplateAssociations(
         Collection<PSPair<IPSGuid, IPSGuid>> pairs)
      throws PSModelException, PSLockException, PSMultiOperationException
   {
      IPSContentTypeModel ctypeModel = 
         (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
      Map<IPSReference, Collection<IPSReference>> associations = 
         ctypeModel.getTemplateAssociations(getUpdatableContentTypes(), false, 
            true);
      IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);
      
      for (PSPair<IPSGuid, IPSGuid> p : pairs)
      {
         IPSGuid ctypeGuid = p.getFirst();
         IPSGuid templateGuid = p.getSecond();
         IPSReference ctypeRef = ctypeModel.getReference(ctypeGuid);
         Collection<IPSReference> templateRefs = associations.get(ctypeRef);
         if (templateRefs == null)
         {
            templateRefs = new ArrayList<IPSReference>();
            associations.put(ctypeRef, templateRefs);
         }
         IPSReference templateRef = templateModel.getReference(templateGuid);
         if (!templateRefs.contains(templateRef))
            templateRefs.add(templateRef);
      }
      ctypeModel.setTemplateAssociations(associations);
   }

   /**
    * Not used, see {@link #doDeleteAssociations(PSPair, Collection)} instead.
    */
   @Override
   @SuppressWarnings("unused")
   protected Collection<IPSReference> doDeleteAssociations(IPSReference slotRef,
      Collection<IPSReference> refs) throws Exception
   {
      throw new UnsupportedOperationException("Not supported, " +
         "see signature doDeleteAssociations(PSPair, Collection) instead.");
   }

   /**
    * Deletes the allowed template associations from the specified slot 
    * references. For selected template nodes this will only remove associations
    * matching slot and content type supplied through the reference pair. For 
    * selected content type nodes this will remove all associations matching 
    * that content type.
    * 
    * @param refPair if specific templates were selected for delete, the 
    *    supplied reference pair must contain the slot reference as the first 
    *    entry and the content type reference as the second entry to match up 
    *    the template associations that need to be removed. If content types 
    *    were selected for delete, the supplied reference pair will contain 
    *    the slot reference as first entry, the second entry will be 
    *    <code>null</code> to remove all template associations for a specific
    *    content type, assumed not <code>null</code>.
    * @param refs references the templates to be removeed from the specified 
    *    slots, may be <code>null</code> or empty in which case this returns 
    *    immediately, associations that do not exist will be ignored.
    * @return the refs for which this deleted the template associations, always
    *    the same as passed in, never <code>null</code>.
    * @throws Exception for any error deleting the requested slot template 
    *    associations.
    */
   private Collection<IPSReference> doDeleteAssociations(
      PSPair<IPSReference, IPSReference> refPair,
      Collection<IPSReference> refs) throws Exception
   {
      if (refs == null || refs.isEmpty())
         return Collections.emptySet();
      
      IPSReference slotRef = refPair.getFirst();
      IPSReference contentTypeRef = refPair.getSecond();
      
      IPSCmsModel slotModel = getModel(PSObjectTypes.SLOT);
      boolean success = false;
      try
      {
         IPSTemplateSlot slotData = (IPSTemplateSlot) slotModel.load(
            slotRef, true, false);
         Collection<PSPair<IPSGuid, IPSGuid>> associations = 
            slotData.getSlotAssociations();
         
         for (Iterator<PSPair<IPSGuid, IPSGuid>> iter = 
            associations.iterator(); iter.hasNext();)
         {
            PSPair<IPSGuid, IPSGuid> existingPair = iter.next();
            
            for (IPSReference ref : refs)
            {
               if (ref.getObjectType().equals(
                  PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE)))
               {
                  if (ref.getId().equals(existingPair.getFirst()))
                     iter.remove();
               }
               else
               {
                  assert(ref.getObjectType().getPrimaryType().equals(
                     PSObjectTypes.TEMPLATE));
                  if (ref.getId().equals(existingPair.getSecond()) && 
                     contentTypeRef.getId().equals(existingPair.getFirst()))
                  {
                     iter.remove();
                  }
               }
            }
         }
         
         slotData.setSlotAssociations(associations);
         slotModel.save(slotRef, true);
         success = true;
         return refs;
      }
      finally
      {
         if (!success)
            slotModel.releaseLock(slotRef);
      }
   }

   /**
    * Checks if there is ancestor node that wraps a content type ref. If so,
    * that content type is used as the pairing for each template. Otherwise,
    * pops up a dialog asking the user to choose a content type for each
    * supplied template.
    * 
    * @param templates The templates that are being added. Assumed not
    *    <code>null</code>.
    * 
    * @return The new template/content type pairings, never <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    * @throws PSLockException If the contenttype/template set is already locked.
    */
   private Collection<PSPair<IPSGuid, IPSGuid>> getContentTypeMappings(
      Collection<IPSReference> templates)
      throws PSModelException, PSLockException
   {
      Collection<PSPair<IPSGuid, IPSGuid>> results = 
         new ArrayList<PSPair<IPSGuid, IPSGuid>>();
      
      PSUiReference ctypeAncestorNode = getReferenceAncestor(m_targetNode,
         PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE));
      
      if (ctypeAncestorNode != null)
      {
         assert (ctypeAncestorNode.getObjectType().getPrimaryType().equals(
            PSObjectTypes.CONTENT_TYPE));
         IPSReference ctypeRef = ctypeAncestorNode.getReference();
         for (IPSReference templateRef : templates)
         {
            results.add(new PSPair<IPSGuid, IPSGuid>(ctypeRef.getId(),
               templateRef.getId()));
         }
      }
      else
      {
         Collection<IPSReference> variantTemplates = 
            new ArrayList<IPSReference>();
         Collection<IPSReference> localTemplates = 
            new ArrayList<IPSReference>();
         Collection<IPSReference> otherTemplates = 
            new ArrayList<IPSReference>();
         for (IPSReference template : templates)
         {
            if (template.getObjectType().getSecondaryType().equals(
               TemplateSubTypes.VARIANT))
               variantTemplates.add(template);
            else if (template.getObjectType().getSecondaryType().equals(
               TemplateSubTypes.LOCAL))
               localTemplates.add(template);
            else
               otherTemplates.add(template);
         }
         
         IPSContentTypeModel ctypeModel = 
            (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
         IPSTemplateModel templateModel = 
            (IPSTemplateModel) getModel(PSObjectTypes.TEMPLATE);
         
         /*
          * Variant templates always have exact one contenttype associated. Use
          * that to instead of asking the user.
          */
         for (IPSReference variantTemplate : variantTemplates)
         {
            Set<IPSReference> contentTypes = templateModel.getContentTypes(
               variantTemplate, false);
            for (IPSReference contentType : contentTypes)
               results.add(new PSPair<IPSGuid, IPSGuid>(
                  contentType.getId(), variantTemplate.getId()));
         }
         
         /*
          * Locale templates may have zero or one contenttype associated. If
          * one is associated, use that instead of asking the user. If none is
          * associated, this does nothing.
          */
         for (IPSReference localTemplate : localTemplates)
         {
            Set<IPSReference> contentTypes = templateModel.getContentTypes(
               localTemplate, false);
            for (IPSReference contentType : contentTypes)
               results.add(new PSPair<IPSGuid, IPSGuid>(
                  contentType.getId(), localTemplate.getId()));
         }
         
         /*
          * All template types but variant and local may have one or more 
          * contenttypes associated. Ask the user which ones to use.
          */
         if (!otherTemplates.isEmpty())
         {
            Collection<IPSReference> ctypes = getUpdatableContentTypes();
            
            Map<IPSReference, Collection<IPSReference>> associations = 
               ctypeModel.getTemplateAssociations(ctypes, false, true);
            
            PSIncludedContentTypesDialog dialog = 
               new PSIncludedContentTypesDialog(PSUiUtils.getShell(), 
                  templates, ctypes, associations);
            int status = dialog.open();
            if (status == Dialog.OK)
               results.addAll(dialog.getValues());
         }
      }

      return results;
   }
   
   /**
    * Get all usable content types which allow updates. Usable
    * content types are filtered from all content types used for internal 
    * system implementations.
    * 
    * @return a collection with all usable content types that allow the update 
    *    permission, never <code>null</code>, may be empty.
    * @throws PSModelException for any error looking up the usable content 
    *    types.
    */
   private Collection<IPSReference> getUpdatableContentTypes() 
      throws PSModelException
   {
      IPSContentTypeModel ctypeModel = (IPSContentTypeModel) getModel(
         PSObjectTypes.CONTENT_TYPE);

      Collection<IPSReference> ctypes = 
         ctypeModel.getUseableContentTypes(false);
      Collection<IPSReference> updatableCtypes = 
         new ArrayList<IPSReference>();
      for (IPSReference ctype : ctypes)
      {
         if (PSSecurityUtils.hasPermission(ctype, PSPermissions.UPDATE))
            updatableCtypes.add(ctype);
      }
      
      return updatableCtypes;
   }

   /**
    * The node that received the drop. Set in
    * {@link #saveAssociations(PSUiReference, Collection)}.
    */
   private PSUiReference m_targetNode;
}
