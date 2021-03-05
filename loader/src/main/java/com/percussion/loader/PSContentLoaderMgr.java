/* *****************************************************************************
 *
 * [ PSContentLoaderMgr.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.objectstore.PSErrorHandlingDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFieldTransformationDef;
import com.percussion.loader.objectstore.PSFieldTransformationsDef;
import com.percussion.loader.objectstore.PSItemTransformationsDef;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSTransformationDef;
import com.percussion.loader.objectstore.PSTransitionDef;
import com.percussion.loader.objectstore.PSWorkflowDef;
import com.percussion.util.IPSHtmlParameters;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class manages the content loading operation.
 */
public class PSContentLoaderMgr extends PSProcessMgr
{
   /**
    * Constructs a <code>PSContentLoaderMgr</code> object from the specified
    * parameters. All the nodes of the content tree will be processed unless
    * called {@link #setUploadedNodes(List)}.
    *
    * @param ld The loader descriptor, may not <code>null</code>.
    * 
    * @param contentTree The content tree which contains the uploaded nodes,
    *    may not <code>null</code>.
    *    
    * @param errorOccuredInSelectMgr <code>true</code> if error has occurred in
    *    selector manager, the previous operation.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   public PSContentLoaderMgr(PSLoaderDescriptor desc,
      IPSContentTree contentTree, boolean errorOccuredInSelectMgr)
      throws PSLoaderException
   {
      super(desc);

      if (contentTree == null)
         throw new IllegalArgumentException("contentTree may not be null");
         
      m_contentTree = contentTree;

      if (errorOccuredInSelectMgr) // remember error has occurred,
         setErrorOccured();        // used to send email at the end if needed

      PSLoaderDef loaderDef = desc.getLoaderDef();
      PSConnectionDef connection = desc.getConnectionDef();
      m_remoteAgent = new PSLoaderRemoteAgent(connection);

      if (m_loader instanceof PSContentLoader)
      {
         PSContentLoader loader = (PSContentLoader) m_loader;
         loader.setConnectionDef(m_loaderDesc.getConnectionDef());
      }
   }

   /**
    * Set the uploaded tree nodes, which should be from the content
    * tree of the constructor.
    * 
    * @param uploadNodes A list of {@link IPSContentTreeNode} objects. It may
    *    not be <code>null</code> or empty.
    */
   public void setUploadedNodes(List uploadNodes)
   {
      if (uploadNodes == null || uploadNodes.isEmpty())
         throw new IllegalArgumentException(
            "uploadNodes may not be null or empty");
            
      m_uploadNodes = uploadNodes;
   }
 
   /**
    * Set the specified upload method.
    * 
    * @param uploadMethod The to be set method, it must be one of the
    *    <code>UPLOAD_AS_XXX</code> constants.
    */
   public void setUploadMethod(int uploadMethod)
   {
      if ((uploadMethod != UPLOAD_AS_NEEDED) &&
          (uploadMethod != UPLOAD_AS_MODIFIED) &&
          (uploadMethod != UPLOAD_AS_NEW))
      {
         throw new IllegalArgumentException(
            "uploadMethod must be one of the UPLOAD_AS_XXX constants");
      }
      m_uploadMethod = uploadMethod;
   }
     
   /**
    * Process the content tree according to the content descriptor
    */
   public void run()
   {
      boolean emailOnSuccess = false;
      boolean emailOnError = false;
      boolean attachLog = false;
      try
      {
         // notify starting the manager operation
         operationStarted();

         if (m_uploadNodes == null)
            m_uploadNodes = getListFromTree(m_contentTree);

         
         processNodeList(m_uploadNodes, PSStatusEvent.PROCESS_LOADING_CONTENTS,
            true);

         // notify the manager operation status
         if (isAborting())
         {
            operationAborted();
         }
         else
         {
            operationCompleted();
            saveStatus();
         }

         PSErrorHandlingDef errDef = m_loaderDesc.getErrorHandlingDef();
         emailOnSuccess = errDef.getEmailOnSuccess();
         emailOnError = errDef.getEmailOnError();
         attachLog = errDef.getEmail().getAttachLogs();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         // unexpected error
         setRunException(e);
         operationAborted();
      }

      if (isAborted() && emailOnError)
      {
         if (attachLog)
            sendFailedEmail("\n\n" + getLog());
         else
            sendFailedEmail("");
      }
      else if ( hasErrorOccured() && emailOnError)
      {
         if (attachLog)
            sendErrorEmail("\n\n" + getLog());
         else
            sendErrorEmail("");
      }
      else if (emailOnSuccess)
      {
         if (attachLog)
            sendSuccessfulEmail("\n\n" + getLog());
         else
            sendSuccessfulEmail("");
      }
   }

   /**
    * Save current uploaded tree.
    *
    * @throws PSLoaderException if an error occurs during saving the tree
    */
   private void saveStatus()
      throws PSLoaderException
   {
      PSLoaderRepositoryHandler repositoryHandler;
      repositoryHandler = new PSLoaderRepositoryHandler(m_loaderDesc.getPath());
      IPSContentTree tree = getContentTree();
      PSContentStatus status = new PSContentStatus(tree, m_loaderDesc);
      repositoryHandler.saveStatus(status);
   }

   /**
    * Process a list of nodes according to the specified process-id
    *
    * @param nodeList A list of <code>IPSContentTreeNode</code> objects,
    *    the node list that will be uploaded.
    *
    * @param processId The process id, assume it is one of the
    *    PSStatusEvent.PROCESS_XXX values.
    *
    * @param modifyStatus <code>true</code> if the item status can be modified
    *    according to current upload method.
    * 
    * @throws PSLoaderException is any error occurs.
    */
   private void processNodeList(List nodeList, int processId, 
      boolean modifyStatus)
      throws PSLoaderException
   {
      if (isAborting())
         return; // do nothing if the process has been aborted

      processStarted(processId);

      Iterator nodes = nodeList.iterator();
      int totalCount = nodeList.size();
      int count = 0;

      while (nodes.hasNext() && (! isAborting()))
      {
         IPSContentTreeNode node = (IPSContentTreeNode)nodes.next();
         PSItemContext item = node.getItemContext();

         // check whether we should abort - checking here
         // allows the ui quicker response to a stop event.
         if (isAborting())
            operationAborted();

         if (modifyStatus)
            modifyItemStatusAsNeeded(item);

         if (item.getStatus().equals(PSItemContext.STATUS_NEW) ||
             item.getStatus().equals(PSItemContext.STATUS_MODIFIED))
         {
            try
            {
               loadContent(node);
               item.setStatus(PSItemContext.STATUS_UNCHANGED);
               item.setLastLoad(new java.util.Date());
            }
            catch (Exception e)
            {
               item.setStatus(PSItemContext.STATUS_ERROR);
               setRunException(e);

               // log the error
               String[] args = {item.getResourceId(), e.toString()};
               PSLogMessage   msg = new PSLogMessage(
                     IPSLogCodes.ERROR_PROCESS_NODE,
                     args, item, PSLogMessage.LEVEL_ERROR);
               Logger.getLogger(this.getClass()).error(msg);
            }
         }

         count++;
         fireProgressEvent((count * 100) / totalCount);
      }

      if (!isAborting())
         processCompleted(processId);
   }

   /**
    * Modify the status based on the current upload method for specified item,
    * do nothing if the item is excluded.
    * 
    * @param item The to be modified item context, assume not <code>null</code>.
    */
   private void  modifyItemStatusAsNeeded(PSItemContext item)
   {        
      if (! item.getStatus().equals(PSItemContext.STATUS_EXCLUDED))
      {
         if (m_uploadMethod == UPLOAD_AS_NEW)
         {
            item.setStatus(PSItemContext.STATUS_NEW);
         }
         else if (m_uploadMethod == UPLOAD_AS_MODIFIED)
         {
            if (item.getLocator() != null)
               item.setStatus(PSItemContext.STATUS_MODIFIED);
            else
               item.setStatus(PSItemContext.STATUS_NEW);
         }
      }
   }
         
   /**
    * Load the content in a given tree node. 
    *
    * @param node The node which contains uploaded content. Assume the status
    *    of the node is <code>STATUS_NEW</code> or <code>STATUS_MODIFIED</code>.
    *
    * @throws PSLoaderException if any error occurs.
    */
   private void loadContent(IPSContentTreeNode node)
      throws PSLoaderException
   {
      PSItemContext itemCtx = node.getItemContext();
      PSExtractorDef extractorDef = itemCtx.getExtractorDef();

      // log the action
      PSLogMessage   msg = new PSLogMessage(
            IPSLogCodes.UPLOAD_NODE,
            itemCtx.getResourceId(),
            itemCtx);
      Logger.getLogger(this.getClass().getName()).info(msg);

      // do the uploading
      if (extractorDef.isStaticType())
      {
         itemCtx = loadStaticContent(node);
      }
      else
      {
         itemCtx = loadItemContent(node);
      }
      
      itemCtx.clearCachedContent();

      // set the processed item back to the node, which may or may not be
      // the original item of the node.
      node.setItemContext(itemCtx);
   }

   /**
    * Uploads the static content of the supplied node.
    * 
    * @param node The to be uploaded node, which contains the uploaded content,
    *    assume not <code>null</code> and its status is either 
    *    <code>STATUS_NEW</code> or <code>STATUS_MODIFIED</code>.
    * 
    * @return item context which contains the uploaded content. It may or may
    *    not be the same as the one in the <code>node</code>.
    * 
    * @throws PSLoaderException
    */
   private PSItemContext loadStaticContent(IPSContentTreeNode node)
      throws PSLoaderException
   {
      PSItemContext itemCtx = node.getItemContext();
      PSExtractorDef extractorDef = itemCtx.getExtractorDef();
      IPSItemExtractor extractor = (IPSItemExtractor) m_staticExtractors.get(
         extractorDef.getName());

      itemCtx = extractItem(extractor, node);
      m_loader.loadStaticItem(itemCtx);
      
      return itemCtx;
   }
   
   /**
    * Uploads the item content of the supplied node.
    * 
    * @param node The to be uploaded node, which contains the uploaded content,
    *    assume not <code>null</code> and its status is either 
    *    <code>STATUS_NEW</code> or <code>STATUS_MODIFIED</code>.
    * 
    * @return item context which contains the uploaded content. It may or may
    *    not be the same as the one in the <code>node</code>.
    * 
    * @throws PSLoaderException if an error occurs
    */
   private PSItemContext loadItemContent(IPSContentTreeNode node)
      throws PSLoaderException
   {
      PSItemContext itemCtx = node.getItemContext();
      PSExtractorDef extractorDef = itemCtx.getExtractorDef();
      IPSItemExtractor extractor = (IPSItemExtractor) m_itemExtractors.get(
         extractorDef.getName());

      // set community for the connection info from the item if needed
      String communityID =  extractorDef.getFieldValue(
         IPSHtmlParameters.SYS_COMMUNITYID);
      if (communityID != null && communityID.trim().length() != 0)
         m_remoteAgent.setCommunity(communityID);
      
      // perform pre-update transitions if there is any
      if (itemCtx.getStatus().equals(PSItemContext.STATUS_MODIFIED))
      {
         performTransitions(itemCtx, PSWorkflowDef.TRANS_PREUPDATE);
      }
      
      // update the content
      setClientItem(itemCtx); // set the PSClientItem to be processed below
      itemCtx = extractItem(extractor, node);

      itemCtx = itemTransformation(itemCtx);
      m_loader.loadContentItem(itemCtx);
      
      // perform transitions for new or updated item
      if (itemCtx.getStatus().equals(PSItemContext.STATUS_MODIFIED))
      {
         performTransitions(itemCtx, PSWorkflowDef.TRANS_POSTUPDATE);
      }
      else
      {
         performTransitions(itemCtx, PSWorkflowDef.TRANS_INSERT);
      }
      
      return itemCtx;
   }
   
   
   /**
    * Set the <code>PSClientItem</code> for an item context.
    *
    * @param itemContext The to be set item context, assume it is not a
    *    static item and it is not <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void setClientItem(PSItemContext itemContext)
      throws PSLoaderException
   {
      PSExtractorDef def = itemContext.getExtractorDef();
      String contentTypeId = def.getContentTypeName();

      PSClientItem clientItem;
      PSLocator locator = itemContext.getLocator();

      // invalid locator (id, revision == 0, -1) may created by preview loader
      try
      {
      
         if ( (locator == null) || (locator.getId() == 0) ||
              (locator.getRevision() == -1) )
            clientItem = m_remoteAgent.newItemDefault(contentTypeId);
         else
            clientItem = m_remoteAgent.openItem(locator, true, true);

      }
      catch(PSRemoteException re)
      {
         throw new PSLoaderException(re);
      }
      itemContext.setItem(clientItem);
   }

   /**
    * Do the item transformation through the tranformer defined within the
    * extractor of the item and the global transformers if exists.
    *
    * @param item The item context that contains the to be transformed item
    *    and the item specific transformer definitions, assume not
    *    <code>null</code>.
    *
    * @return The item context object that contains the (transformed) item,
    *    never <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private PSItemContext itemTransformation(PSItemContext item)
      throws PSLoaderException
   {
      // do specific transformation first
      PSExtractorDef extractorDef = item.getExtractorDef();
      fieldTransformation(extractorDef.getFieldTransformations(), item);
      item = itemTransformation(extractorDef.getItemTransformations(), item);

      // do the global transformation
      PSFieldTransformationsDef fieldTrans = m_loaderDesc.getFieldTransDef();
      if (fieldTrans != null)
         fieldTransformation(fieldTrans.getTransformations(), item);

      PSItemTransformationsDef itemTrans = m_loaderDesc.getItemTransDef();
      if (itemTrans != null)
         item = itemTransformation(itemTrans.getTransformations(), item);

      return item;
   }

   /**
    * Perform field transformations for a given item from a list of
    * field transformation definitions.
    *
    * @param fieldTransDef An iterator of zero or more
    *    <code>PSFieldTransformationDef</code> objects, assume not
    *    <code>null</code>, but may be empty.
    * @param item The to be transformed item, assume <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void fieldTransformation(Iterator fieldTransDef,
      PSItemContext itemContext) throws PSLoaderException
   {
      PSTransformContext transContext = new PSTransformContext();

      while (fieldTransDef.hasNext())
      {
         PSFieldTransformationDef def;
         def = (PSFieldTransformationDef) fieldTransDef.next();

         PSClientItem item = itemContext.getItem();
         PSItemField itemField = item.getFieldByName(def.getTargetField());
         if (itemField != null)
         {
            transContext.setItemField(itemField);

            String name = def.getName();
            IPSFieldTransformer fieldTrans;
            fieldTrans = (IPSFieldTransformer) m_transMap.get(name);
            Object[] params = (Object[]) m_transParamsMap.get(name);

            fieldTrans.transform(params, transContext);
         }
         else
         {
            // TODO: warning, field not found
         }
      }
   }


   /**
    * For a given item, do the item transformation from a list of
    * transformation definitions.
    *
    * @param itemTransDef List of transformation definitions, assume not
    *    <code>null</code>, but may be empty.
    * @param item The to be transformed item, assume <code>null</code>.
    *
    * @return The transformed item. It may be the original item if the
    *    list of definition is empty.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private PSItemContext itemTransformation(Iterator itemTransDef,
      PSItemContext item) throws PSLoaderException
   {
      while (itemTransDef.hasNext())
      {
         PSTransformationDef def = (PSTransformationDef) itemTransDef.next();
         String name = def.getName();
         IPSItemTransformer itemTrans;
         itemTrans = (IPSItemTransformer) m_transMap.get(name);
         Object[] params = (Object[]) m_transParamsMap.get(name);

         item = itemTrans.transform(params, item);
      }
      return item;
   }

   /**
    * Extract an item by a given extractor from a node.
    *
    * @param extractor The object used to extract the item, assume it can
    *    extract an item from the <code>node</code>, and it is not
    *    <code>null</code>.
    *
    * @param node The node that will be used to extract item from, assume not
    *    <code>null</code>.
    *
    * @return The extracted item, never <code>null</code>.
    *
    * @throws PSLoaderException if error occurs.
    */
   private PSItemContext extractItem(IPSItemExtractor extractor,
      IPSContentTreeNode node)  throws PSLoaderException
   {
      PSItemContext[] items;
      InputStream in = null;
      PSItemContext item = node.getItemContext();

      try
      {
         in = m_selector.retrieve(node);
         items = extractor.extractItems(item, in);
      }
      catch (java.io.IOException e)
      {
         throw new PSLoaderException(
            IPSLoaderErrors.UNEXPECTED_ERROR, e.toString());
      }
      finally
      {
         if (in != null)
         try { in.close(); } catch (Exception ex) {}
      }

      if (items.length > 1)
      {
         throw new PSLoaderException(
            IPSLoaderErrors.EXTRACTED_TOO_MANY_ITEMS,
               extractor.getClass().toString());
      }

      return items[0];
   }

   /**
    * Performs transitions to the item in the supplied node. Do nothing
    * for preview loader.
    *
    * @param node The node that contains the item that may apply actions to,
    *    assume not <code>null</code>, and the item is not static.
    *
    * @param transSet Transition set, assume it is one of the 
    *    <code>PSWorkflowDef.TRANS_XXX</code> values.
    * 
    * @throws PSLoaderException if an error is encountered while applying
    *    actions to <code>node</code>
    */
   private void performTransitions(PSItemContext item, int transSet) 
      throws PSLoaderException
   {
      // no transition performed for preview loader
      if (m_loader instanceof PSContentLoaderPreview)
         return;
      
      PSExtractorDef extractorDef = item.getExtractorDef();
      PSWorkflowDef workflow = extractorDef.getWorkflowDef();
      Iterator trans = null;
      if (workflow != null)
      {
         trans = workflow.getTransitions(transSet).getComponents();
         if (! trans.hasNext())
            trans = null;
      }

      // perform the workflow transitions if needed
      if (trans != null)
      {
         // log the action
         PSLogMessage   msg = new PSLogMessage(
               IPSLogCodes.PERFORM_ACTIONS_4_NODE,
               item.getResourceId(),
               item);
         Logger.getLogger(this.getClass().getName()).info(msg);

         while (trans.hasNext())
         {
            // Using the internal name, "trigger", of the transitions, 
            // which are unique for a given workflow.
            PSTransitionDef tran = (PSTransitionDef)trans.next();
            String transId = tran.getTrigger();
            boolean success = false;
   
            try
            {
               success = m_remoteAgent.transitionItem(item.getLocator(), 
                  transId);
            }
            catch (PSRemoteException ex)
            {
               throw new PSLoaderException(ex);
            }
            if (! success)
            {
               Object[] args = {transId, item.getResourceId()};
               throw new PSLoaderException(
                  IPSLoaderErrors.FAILED_WF_TRANSITION, args);
            }
         }
      }
   }

   /**
    * Get the content tree which is processed by this object.
    *
    * @return The content tree, never <code>null</code>
    *
    * @throws IllegalStateException if the content tree has not been created.
    */
   public IPSContentTree getContentTree()
   {
      if (m_contentTree == null)
         throw new IllegalArgumentException(
            "m_contentTree has not been created");

      return m_contentTree;
   }

   /**
    * The current content tree has been processes by this object.
    * It may be <code>null</code> if has not been created by the selector.
    */
   private IPSContentTree m_contentTree = null;

   /**
    * The content data handler, initialized by the constructor, never
    * <code>null</code> after that.
    */
   private PSLoaderRemoteAgent m_remoteAgent;

   /**
    * A list of uploaded <code>IPSContentTreeNode</code>, which are the
    * nodes from <code>m_contentTree</code>.
    */
   private List m_uploadNodes = null;
   
   /**
    * The upload method. Default to <code>UPLOAD_AS_NEEDED</code>, it will
    * always be one of the <code>UPLOAD_AS_XXX</code> constants.
    */
   private int m_uploadMethod = UPLOAD_AS_NEEDED;
   
   /**
    * Upload the items for new or modified items only. Do nothing for unchanged
    * items.
    */
   public final static int UPLOAD_AS_NEEDED = 1;
   
   /**
    * Upload the items as they are newly scanned items.
    */
   public final static int UPLOAD_AS_NEW = 2;
   
   /**
    * Upload the items as if they have been modified.
    */
   public final static int UPLOAD_AS_MODIFIED = 3;
   
}
