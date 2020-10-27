/******************************************************************************
 *
 * [ PSContentTypeAssociateTemplateNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.PSLockException;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.editors.dialog.PSConvertTemplatesDialog;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Handler for the folder that contains the templates (both shared and local)
 * that the content type ancestor uses.
 * 
 * @author paulhoward
 */
public class PSContentTypeAssociateTemplateNodeHandler extends
      PSLinkNodeHandler
{
   /**
    * The only ctor, signature required by framework. Parameters passed to 
    * base class.
    */
   public PSContentTypeAssociateTemplateNodeHandler(Properties props,
         String iconPath, PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   //see base class method for details
   @Override
   protected Collection<IPSReference> doDeleteAssociations(
         IPSReference ctypeRef, Collection<IPSReference> templateRefs)
      throws Exception
   {
      if (null == ctypeRef)
      {
         throw new IllegalArgumentException("ctypeRef cannot be null");  
      }
      if (null == templateRefs || templateRefs.isEmpty())
         return Collections.emptySet();
      
      IPSContentTypeModel ctypeModel = 
         (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
      Map<IPSReference, Collection<IPSReference>> associations = ctypeModel
         .getTemplateAssociations(Collections.singletonList(ctypeRef), false, 
               true);
      Collection<IPSReference> assocTemplateRefs = associations
            .get(ctypeRef);
      if (assocTemplateRefs == null)
         assocTemplateRefs = Collections.emptySet();
         //continue processing to remove nodes that don't exist anymore
      
      for (Iterator<IPSReference> iter = assocTemplateRefs
            .iterator(); iter.hasNext();)
      {
         if (templateRefs.contains(iter.next()))
         {
            iter.remove();
         }
      }
      ctypeModel.setTemplateAssociations(associations);
      return templateRefs;
   }

   /**
    * @inheritDoc
    * Adds the dropped templates as being allowed to render the ancestor content
    * type. If one or more of the templates are local, the user is asked what
    * they want to do:
    * <ol>
    *    <li>Move the template from the other ctype to this one</li>
    *    <li>Convert it to shared and add here</li>
    * <ol>
    */
   @Override
   protected Collection<IPSReference> doSaveAssociations(IPSReference ctypeRef,
         Collection<IPSReference> templateRefs) throws Exception
   {
      if (null == ctypeRef)
      {
         throw new IllegalArgumentException("ctypeRef cannot be null");  
      }
      if (null == templateRefs || templateRefs.isEmpty())
         return Collections.emptyList();
      
      if (processContentTypeTemplateAssociations(templateRefs, 
            Collections.singleton(ctypeRef)))
      {  
         return templateRefs;
      }
      return Collections.emptyList();
   }

   /**
    * Handles the following cases of linking content types to templates. This is
    * designed to support DnD between templates and ctypes nodes, so not every
    * case is possible. Only the possible ones are covered here. The first entry
    * indicates the number and type of elements in the supplied
    * <code>templateRefs</code> set and the 2nd entry indicates the number of
    * elements in the supplied <code>contentTypeRefs</code> set. (+ means 'or
    * more')
    * <p>
    * Note - in terms of the DnD operation, the target of the drop is always
    * the type w/ a single element.
    * <p> 
    * <ol>
    *    <li>1 shared template, 1+ ctypes - add the links</li>
    *    <li>2+ shared templates, 1 ctype - add the links</li>
    *    <li>1+ local template, 1 ctype - ask user what to do: move or convert;
    *    if convert, change the template to shared and add the 2nd link, if 
    *    move, remove existing link and add new</li>
    *    <li>1 local template, 1+ ctype - ask user what to do: convert or 
    *    cancel</li>
    *    <li>combo of local/shared, 1 ctype - break into groups and treat as 
    *    already noted</li>
    * <ol>
    * 
    * @param templateRefs Never <code>null</code> or empty.
    * 
    * @param ctypeRefs Never <code>null</code> or empty.
    * 
    * @return <code>true</code> if successful, <code>false</code> if the
    * operation was cancelled.
    * 
    * @throws PSMultiOperationException If persisting the links fails.
    * @throws PSModelException If a generic problem not associated with the data
    * occurs.
    * @throws PSLockException If the links cannot be locked for editing.
    */
   static boolean processContentTypeTemplateAssociations(
         Collection<IPSReference> templateRefs,
         Collection<IPSReference> ctypeRefs)
      throws PSMultiOperationException, PSModelException, PSLockException
   {
      if (templateRefs.size() > 1 && ctypeRefs.size() > 1)
      {
         Log log = LogFactory
               .getLog(PSContentTypeAssociateTemplateNodeHandler.class);
         log.warn(
            "Unexpected params: either the templates or ctypes must have at most a single object.");
         return false;
      }
      
      IPSContentTypeModel model = 
         (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
      Map<IPSReference, Collection<IPSReference>> associations = null;
      Collection<IPSReference> localTemplates = getFilteredTemplates(
            templateRefs, PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
                  PSObjectTypes.TemplateSubTypes.LOCAL));
      if (!localTemplates.isEmpty())
      {
         //ask user what to do and respond accordingly
         if (localTemplates.size() == 1 && ctypeRefs.size() > 1)
         {
            //must convert or cancel, move not available
            if (!MessageDialog.openQuestion(PSUiUtils.getShell(), 
               PSMessages.getString(
                  "PSContentTypeAssociateTemplateNodeHandler.askToConvert.title"), 
               PSMessages.getString(
                  "PSContentTypeAssociateTemplateNodeHandler.askToConvert.message")))
            {
               return false;
            }
            boolean convertedSuccessfully = 
               convertTemplatesToShared(localTemplates);
            if (!convertedSuccessfully)
            {
               return false;
            }
         }
         else
         {
            LocalTemplateProcessChoice userChoice = 
               getUserFeedback(localTemplates);
            if (userChoice == LocalTemplateProcessChoice.CONVERT)
            {
               boolean convertedSuccessfully = 
                  convertTemplatesToShared(localTemplates);
               if (!convertedSuccessfully)
               {
                  return false;
               }
            }
            else if (userChoice == LocalTemplateProcessChoice.MOVE)
            {
               //remove the previous association, add new one below
               associations = model.getTemplateAssociations(null, false, true);
               for (IPSReference tmpRef : associations.keySet())
               {
                  Collection<IPSReference> assocTemplateRefs = associations
                        .get(tmpRef);
                  for (Iterator<IPSReference> iter = assocTemplateRefs
                        .iterator(); iter.hasNext();)
                  {
                     if (localTemplates.contains(iter.next()))
                     {
                        iter.remove();
                     }
                  }
               }
               //local templates can exist w/o a link
               
               //remove any nodes that are linked since there can only be one
               PSDesignObjectHierarchy viewModel = 
                  PSDesignObjectHierarchy.getInstance();
               for (IPSReference localRef : localTemplates)
               {
                  Collection<PSUiReference> wrapperNodes = 
                     viewModel.getNodes(localRef);
                  for (PSUiReference node : wrapperNodes)
                  {
                     if (node.isReference())
                     {
                        PSUiReference parent = node.getParentNode();
                        String nodeChildType = 
                           (String) parent.getProperty("childType");
                        if (StringUtils.equals(nodeChildType, 
                              "templateLinkedToContentType"))
                        {
                           viewModel.deleteChildren(
                                 Collections.singletonList(node));
                        }
                     }
                  }
               }
            }
            else
            {
               assert(userChoice == LocalTemplateProcessChoice.CANCEL);
               return false;
            }
         }
      }

      if (associations == null)
      {
         associations = model.getTemplateAssociations(ctypeRefs, false, true);
      }
      
      for (IPSReference ctypeRef : ctypeRefs)
      {
         Collection<IPSReference> curTemplateRefs = associations.get(ctypeRef);
         if (curTemplateRefs == null)
         {
            curTemplateRefs = new ArrayList<IPSReference>();
            associations.put(ctypeRef, curTemplateRefs);
         }
         
         for (IPSReference templateRef : templateRefs)
         {
            assert (templateRef.getObjectType().getPrimaryType()
                  .equals(PSObjectTypes.TEMPLATE));
            
            if (!curTemplateRefs.contains(templateRef)
                  && templateRef.isPersisted())
            {
               curTemplateRefs.add(templateRef);
            }
         }
      }
      model.setTemplateAssociations(associations);
      return true;
   }

   /**
    * Changes all the supplied templates from local to shared. If a template is
    * currently open for edit, the user is notified and the entire operation is
    * cancelled.
    * 
    * @param localTemplates Assumed not <code>null</code> or empty.
    * 
    * @return <code>false</code> if any of the supplied templates are currently
    * open for editing, <code>true</code> if the conversion completes.
    * 
    * @throws PSModelException Under certain conditions if the template cannot
    * be retrieved or saved, such as the server can't be reached.
    * 
    * @throws PSMultiOperationException Under all other conditions if the
    * template cannot be retrieved or saved.
    */
   static boolean convertTemplatesToShared(
         Collection<IPSReference> localTemplates)
      throws PSModelException, PSMultiOperationException
   {
      IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);
      //check if any of the local templates is open for editing
      for (IPSReference ref : localTemplates)
      {
         if (templateModel.isLockedInThisSession(ref))
         {
            String title = PSMessages.getString(
                  "PSContentTypeAssociateTemplateNodeHandler.localTemplateOpenForEdit.info.title");
            String msg = PSMessages.getString(
                  "PSContentTypeAssociateTemplateNodeHandler.localTemplateOpenForEdit.info.message",
                  new Object[] {ref.getName()});
            MessageDialog.openInformation(PSUiUtils.getShell(), title, msg);
            return false;
         }
      }
      
      //modify the templates, add association below
      IPSReference[] refs = localTemplates
            .toArray(new IPSReference[localTemplates.size()]);
      //we don't use the model tracked because the object cannot be open 
      //  for editing
      Object[] objs = templateModel.load(refs, true, false);
      for (Object o : objs)
      {
         IPSAssemblyTemplate templateData = (IPSAssemblyTemplate) o;
         templateData
               .setTemplateType(IPSAssemblyTemplate.TemplateType.Shared);
      }
      templateModel.save(refs, true);
      
      /* modify the declarative views model:
       * 1. Refresh the template instance under the content type node
       * 2. Move the template node from type-specific to shared node
       */
      PSDesignObjectHierarchy viewModel = PSDesignObjectHierarchy.getInstance();
      for (IPSReference localRef : localTemplates)
      {
         Collection<PSUiReference> wrapperNodes = 
            viewModel.getNodes(localRef);
         for (PSUiReference node : wrapperNodes)
         {
            if (!node.isReference())
            {
               viewModel.refresh(node.getParentNode());
            }
         }
      }
      
      //todo - OK for release - remove dependency on node name 
      PSUiReference templateRoot = viewModel.getNode("/Templates");
      if (templateRoot.isCataloged())
      {
         viewModel.refresh(templateRoot);
      }
      return true;
   }
   
   /**
    * Determines which of the supplied refs are for LOCAL templates and returns
    * those in a new set.
    * 
    * @param templateRefs Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   static private Collection<IPSReference> getFilteredTemplates(
         Collection<IPSReference> templateRefs, PSObjectType filter) 
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();
      for (IPSReference templateRef : templateRefs)
      {
         if (templateRef.getObjectType().equals(filter))
            results.add(templateRef);
      }
      return results;
   }

   /**
    * Pops up a dialog asking user a question and allowing several responses.
    * 
    * @param localTemplates The templates in question. Assumed not
    * <code>null</code> or empty.
    * 
    * @return Never <code>null</code>.
    */
   static LocalTemplateProcessChoice getUserFeedback(
         Collection<IPSReference> localTemplates)
   {
      StringBuffer buf = new StringBuffer();
      for (IPSReference ref : localTemplates)
      {
         if (buf.length() > 0)
            buf.append(", ");
         buf.append(ref.getName());
      }
      PSConvertTemplatesDialog dialog = 
         new PSConvertTemplatesDialog(PSUiUtils.getShell(), buf.toString());
      dialog.open();
      return dialog.getValue();
   }

   /**
    * Transfer mechanism between UI method {@link #getUserFeedback(Collection)}
    * and processing method
    * {@link #doSaveAssociations(IPSReference, Collection)}.
    * 
    * @author paulhoward
    */
   public enum LocalTemplateProcessChoice
   {
      /**
       * Convert the local templates to shared templates and add a link to each
       * one.
       */
      CONVERT,
      
      /**
       * Move the type specific template ownership from the previous ctype to
       * this one.
       */
      MOVE,
      
      /**
       * Stop the operation.
       */
      CANCEL
   }
}
