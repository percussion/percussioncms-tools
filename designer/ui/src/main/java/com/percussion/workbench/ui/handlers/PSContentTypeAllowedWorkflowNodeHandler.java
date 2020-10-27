/******************************************************************************
 *
 * [ PSContentTypeAllowedWorkflowNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Handler for folders that contain ctype or workflow links.
 * 
 * @author paulhoward
 */
public class PSContentTypeAllowedWorkflowNodeHandler extends
      PSLinkNodeHandler
{
   /**
    * The only ctor, signature required by framework. Parameters passed to 
    * base class.
    */
   public PSContentTypeAllowedWorkflowNodeHandler(Properties props,
         String iconPath, PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsCopy(PSUiReference node)
   {
      return false;
   }

   //see base class method for details
   @SuppressWarnings("unchecked")  //workflow ids
   @Override
   protected Collection<IPSReference> doDeleteAssociations(IPSReference ref,
         Collection<IPSReference> linkedRefs) throws Exception
   {
      return processLinks(ref, linkedRefs, new IProcessor()
      {
         /**
          * Removes the supplied workflow handles from the supplied def if they
          * are present. If one of the supplied handles is the default workflow,
          * it will not be removed and a message will be displayed to the user.
          */
         public void processLink(PSItemDefinition def,
               Collection<IPSReference> wfRefs)
         {
            PSContentEditor ce = def.getContentEditor();
            PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
            //in 6.0 model, there is always an info
            assert(wfInfo != null);
            //ph - doesn't need to be a list, but that's what the object takes
            List<Integer> wfIds = wfInfo.getWorkflowIds();
            if (wfIds == null)
               wfIds = Collections.emptyList();
            int defaultWf = def.getWorkflowId();
            for (Iterator<IPSReference> iter = wfRefs.iterator(); iter
                  .hasNext();)
            {
               //todo - OK for release 
               //  this will have to be fixed when wf ids become guids
               IPSReference wfRef = iter.next();
               int wfId = (int) wfRef.getId().longValue();
               if (wfId == defaultWf)
               {
                  //todo - OK for release - ask user for new default 
                  iter.remove();
                  PSUiUtils.displayWarningMessage(null, 
                     "PSContentTypeAllowedWorkflowNodeHandler.warning.cantDeleteDefaultWf.message");
                  continue;
               }
               else if (wfIds.contains(wfId))
                  wfIds.remove(new Integer(wfId));
            }
         }
      });
   }

   /**
    * @inheritDoc Adds the supplied workflow(s) to the ancestor ctype. If the
    * workflow is already present, it is skipped.
    */
   @SuppressWarnings("unchecked")  //workflow ids
   @Override
   protected Collection<IPSReference> doSaveAssociations(IPSReference ref,
         Collection<IPSReference> linkedRefs) throws Exception
   {
      return processLinks(ref, linkedRefs, new IProcessor()
      {
         /**
          * Adds the supplied workflow handles to the supplied def if they are
          * not already present.
          */
         public void processLink(PSItemDefinition def,
               Collection<IPSReference> wfRefs)
         {
            PSWorkflowInfo wfInfo = def.getContentEditor().getWorkflowInfo();
            //ph - doesn't need to be a list, but that's what the object takes
            List<Integer> wfIds = new ArrayList<Integer>();
            if (wfInfo != null)
               wfIds.addAll(wfInfo.getWorkflowIds());
            int defaultWf = def.getWorkflowId();
            for (IPSReference wfRef : wfRefs)
            {
               //todo - OK for release 
               //  this will have to be fixed when wf ids become guids
               int wfId = (int) wfRef.getId().longValue();
               if (wfId == defaultWf)
                  continue;
               if (!wfIds.contains(wfId))
                  wfIds.add(wfId);
            }
            
            if (!wfIds.isEmpty())
            {
               if (wfInfo == null)
               {
                  wfInfo = new PSWorkflowInfo(
                        PSWorkflowInfo.TYPE_INCLUSIONARY, wfIds);
                  def.getContentEditor().setWorkflowInfo(wfInfo);
               }
               else
                  wfInfo.setValues(wfIds);
            }
         }
      });
   }
   
   /**
    * A simple interface that provides a 'work' method.
    *
    * @author paulhoward
    */
   private interface IProcessor 
   {
      /**
       * See <code>doXXXAssociations</code> for details.
       * 
       * @param workflowLinks If any of the supplied links cannot be processed,
       * they should be removed from the supplied set and a message displayed
       * to the user.
       */
      public void processLink(PSItemDefinition def,
            Collection<IPSReference> workflowLinks);
   }
   
   /**
    * Performs the processing that is generic when adding/deleting links between
    * content types and workflows. The supplied processor performs the op
    * specific work.
    * 
    * @param ref A handle for the object on the other side of the link. Assumed
    * not <code>null</code>.
    * @param linkedRefs Handles for objects on one side of the link. Assumed not
    * <code>null</code>.
    * @param proc A processor that does the work specific to the operation
    * (adding or deleting.) Assumed not <code>null</code>.
    * @return The linkedRefs that were successfully processed. Never
    * <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with server.
    */
   private Collection<IPSReference> processLinks(IPSReference ref,
         Collection<IPSReference> linkedRefs, IProcessor proc)
         throws PSModelException
   {
      if (null == ref)
      {
         throw new IllegalArgumentException("dataRef cannot be null");  
      }
      if (null == linkedRefs || linkedRefs.isEmpty())
         return Collections.emptySet();
      boolean parentIsCType = 
         ref.getObjectType().getPrimaryType() == PSObjectTypes.CONTENT_TYPE;
      IPSCmsModel ctypeModel = getModel(PSObjectTypes.CONTENT_TYPE);
      if (!parentIsCType)
      {
         //this is a special case that we have to prevent because the data is
         // actually saved in the content type object
         StringBuffer openForEdit = new StringBuffer();
         for (Iterator<IPSReference> iter = linkedRefs.iterator(); iter
               .hasNext();)
         {
            IPSReference ctypeRef = iter.next();
            if (ctypeModel.isLockedInThisSession(ctypeRef))
            {
               if (openForEdit.length() > 0)
                  openForEdit.append(", ");
               openForEdit.append(ctypeRef.getName());
               iter.remove();
            }
         }
         if (openForEdit.length() > 0)
         {
            String title = PSMessages.getString(
                  "PSContentTypeAssociateTemplateNodeHandler.warning.openForEdit.title");
            String msg = PSMessages.getString(
                  "PSContentTypeAssociateTemplateNodeHandler.warning.openForEdit.message", 
                  new Object[] {openForEdit});
            MessageDialog.openInformation(PSUiUtils.getShell(), title, msg); 
         }
      }
      
      final String ERR_CONTEXT = "linking ctype to workflow"; 

      Collection<IPSReference> results = new ArrayList<IPSReference>();
      Map<IPSReference, Collection<IPSReference>> ctypeLinks = 
         new HashMap<IPSReference, Collection<IPSReference>>();
      if (parentIsCType)
      {
         ctypeLinks.put(ref, linkedRefs);
         results.addAll(linkedRefs);
      }
      else
      {
         assert(ref.getObjectType().getPrimaryType() == PSObjectTypes.WORKFLOW);
         for (IPSReference linkedRef : linkedRefs)
         {
            //don't use Collections.singletonList, it may be modified later
            Collection<IPSReference> refs = new ArrayList<IPSReference>();
            refs.add(ref);
            ctypeLinks.put(linkedRef, refs);
         }
      }
      
      List<IPSReference> toLoadRefs = new ArrayList<IPSReference>();
      toLoadRefs.addAll(ctypeLinks.keySet());
      Object[] defs;
      try
      {
         defs = ctypeModel.load(toLoadRefs
               .toArray(new IPSReference[toLoadRefs.size()]), true, false);
         if (results.isEmpty())
            results.addAll(toLoadRefs);
      }
      catch (PSMultiOperationException e)
      {
         defs = e.getResults();
         if (results.isEmpty())
         {
            for (int i = 0; i < defs.length; i++)
            {
               if (defs[i] instanceof PSItemDefinition)
               {
                  results.add(toLoadRefs.get(i));
               }
            }
         }
         PSUiUtils.handleExceptionSync(ERR_CONTEXT,
               "PSContentTypeAssociateTemplateNodeHandler.error.load.title",
               "PSContentTypeAssociateTemplateNodeHandler.error.load.message",
               e); 
      }

      List<IPSReference> validRefs = new ArrayList<IPSReference>();
      for (int i = 0; i < defs.length; i++)
      {
         if (!(defs[i] instanceof PSItemDefinition))
            continue;
         validRefs.add(toLoadRefs.get(i));
         
         PSItemDefinition def = (PSItemDefinition) defs[i];
         Collection<IPSReference> linksToProcess = ctypeLinks.get(toLoadRefs
               .get(i));
         proc.processLink(def, linksToProcess);
         // the processor may have removed links that it couldn't process
         if (parentIsCType)
         {
            results.retainAll(linksToProcess);
         }
         else
         {
            if (linksToProcess.isEmpty())
            {
               results.remove(toLoadRefs.get(i));
            }
         }
      }

      try
      {
         ctypeModel.save(validRefs.toArray(new IPSReference[validRefs.size()]), 
               true);
      }
      catch (PSModelException e)
      {
         results.clear();
         releaseLocks(ctypeModel, validRefs);
         PSUiUtils.handleExceptionSync(ERR_CONTEXT, 
            "PSContentTypeAssociateTemplateNodeHandler.error.saveGeneric.title",
            "PSContentTypeAssociateTemplateNodeHandler.error.saveGeneric.message", 
            e);
      }
      catch (PSMultiOperationException e)
      {
         results.clear();
         Object[] errors = e.getResults();
         Collection<IPSReference> errorRefs = new ArrayList<IPSReference>();
         for (int i=0; i < errors.length; i++)
         {
            if (errors[i] instanceof Throwable)
               errorRefs.add(validRefs.get(i));
            else
               results.add(validRefs.get(i));
         }
         if (!errorRefs.isEmpty())
            releaseLocks(ctypeModel, errorRefs);
         PSUiUtils.handleExceptionSync(ERR_CONTEXT, 
            "PSContentTypeAssociateTemplateNodeHandler.error.saveGeneric.title",
            "PSContentTypeAssociateTemplateNodeHandler.error.save.message",
            e);
      }
      return results;
   }
   
   /**
    * Attempts to release the supplied refs from being locked. Any exceptions
    * are ignored.
    * 
    * @param model The model used to lock the objects. Assumed not
    * <code>null</code>.
    * @param refs Handles to be freed. Assumed not <code>null</code>.
    */
   private void releaseLocks(IPSCmsModel model, Collection<IPSReference> refs)
   {
      try
      {
         model.releaseLock(refs.toArray(new IPSReference[refs.size()]));
      }
      catch (PSModelException e1)
      {
         //ignore, nothing else we can do
      }
      catch (PSMultiOperationException e1)
      {
         //ignore, nothing else we can do
      }
   }
}
