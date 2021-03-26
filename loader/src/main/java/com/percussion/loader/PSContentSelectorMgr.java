/*[ PSContentSelectorMgr.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.objectstore.PSErrorHandlingDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFieldTransformationsDef;
import com.percussion.loader.objectstore.PSItemTransformationsDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class manages the content selecting operation.
 */
public class PSContentSelectorMgr extends PSProcessMgr
{
   /**
    * Constructs a <code>PSContentSelectorMgr</code> object from a loader
    * descriptor
    *
    * @param ld The loader descriptor, may not <code>null</code>.
    * @param contentTree The previously processed content tree, may be
    *    <code>null</code> if not exists.
    *
    * @throws IllegalArgumentException If <code>ld</code> is <code>null</code>.
    */
   public PSContentSelectorMgr(PSLoaderDescriptor ld,
      PSContentStatus prevStatus) throws PSLoaderException
   {
      super(ld);

      if ( prevStatus != null )
      {
         m_prevContentTree = prevStatus.getContentTree();
         m_prevDescriptor = prevStatus.getDescriptor();
      }

      initLog4j();
   }

   /**
    * Initialize log4j according to the specification in the descriptor
    */
   private void initLog4j()
   {
      // TODO: implementing it after the info is available
   }

   /**
    * Retrieve and process the content tree according to the content descriptor
    */
   public void run()
   {
      boolean emailOnFailure = false;
      boolean attachLog = false;
      try
      {
         // notify starting the manager operation
         operationStarted();

         // scanning the content to create a content tree
         addProgressListenersToSelector();

         processStarted(PSStatusEvent.PROCESS_SCANNING);
         m_contentTree = m_selector.scan();

         if (! isAborting() )
            processCompleted(PSStatusEvent.PROCESS_SCANNING);

         removeProgressListenersFromSelector();

         // mark the content tree
         if (! isAborting())
            markContentTree();

         // notify the manager operation status
         if (isAborting())
            operationAborted();
         else
            operationCompleted();
         PSErrorHandlingDef errDef = m_loaderDesc.getErrorHandlingDef();
         emailOnFailure = errDef.getEmailOnError();
         attachLog = errDef.getEmail().getAttachLogs();
      }
      catch (Exception e)
      {
         // unexpected error
         setRunException(e);
         operationAborted();
      }

      if (isAborted() && emailOnFailure)
      {
         if (attachLog)
            sendFailedEmail("\n\n" + getLog());
         else
            sendFailedEmail("");
      }
   }

   /**
    * Mark the content tree, which is the result of scanning process of the
    * selector. Mark each node as modified, excluded, ...etc.
    * <p>If previous scaned tree is provided, it will operate in 'delta' mode,
    * meaning it will check each node to determine if the source has changed
    * since the last scan (meaning the node must have some sort of checksum).
    * The node will be marked appropriately (updated or unchanged). Nodes in
    * the tree that don't exist in the source any more will be removed. New
    * nodes will be added (and marked as inserted).
    *
    * @throws java.io.IOExcetion if error occurs during retrieving data.
    *
    * @throws PSLoaderException if any other error occurs.
    */
   private void markContentTree() throws PSLoaderException, java.io.IOException
   {
      List nodeList = getListFromTree(m_contentTree);
      int totalCount = nodeList.size();

      if (totalCount <= 0)
         return;  // there is nothing to process

      // start to mark the tree
      processStarted(PSStatusEvent.PROCESS_MARKING_TREE);

      Map prevTreeIdMap = getResourceIdMap(m_prevContentTree);
      int count = 0;

      // set status for each node.
      Iterator nodes = nodeList.iterator();
      while (nodes.hasNext())
      {
         if (isAborting())
            break;

         IPSContentTreeNode node = (IPSContentTreeNode) nodes.next();
         PSItemContext item = node.getItemContext();

         count++;
         //fireProgressEvent((count * 100) / totalCount);

         item.setLastScan(new java.util.Date());
         item.setContentTreeNode(node);

         if (isExtractableItem(item))
         {
            IPSContentTreeNode prevNode;
            prevNode =
               (IPSContentTreeNode) prevTreeIdMap.get(item.getResourceId());

            setItemFromPreviousNode(node, prevNode);
         }
         else
         {
            item.setStatus(PSItemContext.STATUS_EXCLUDED);
         }
      }

      if (isAborting())
         processAborted(PSStatusEvent.PROCESS_MARKING_TREE);
      else
         processCompleted(PSStatusEvent.PROCESS_MARKING_TREE);

   }

   /**
    * Set the item context from a node that exists in the previous scanned tree
    *
    * @param node The to be set node object, assume it is interested by
    *    one of the extractors, not <code>null</code>
    *
    * @param prevNode The node from previous tree, which has the same resource
    *    id as the <code>item</code> does. It may be <code>null</code> if the
    *    same resource id not exist in previous tree.
    *
    * @throws java.io.IOException if error occurs during retrieve data.
    * @throws PSLoaderException if any error occurs.
    */
   private void setItemFromPreviousNode(IPSContentTreeNode node,
      IPSContentTreeNode prevNode) throws PSLoaderException, java.io.IOException
   {
      PSItemContext item = node.getItemContext();

      if (prevNode == null) // a new node, not exist in previous tree
      {
         item.setStatus(PSItemContext.STATUS_NEW);
      }
      else                 // same node exists in previous scanned tree
      {
         PSItemContext prevItem = prevNode.getItemContext();
         boolean prevExcluded =
            prevItem.getStatus().equals(PSItemContext.STATUS_EXCLUDED);

         if (prevExcluded)
         {
            // User may have manualy mark the node to EXCLUDE
            // in previous session. So we'll keep it the same as before.
            item.setStatus(PSItemContext.STATUS_EXCLUDED);
         }
         else
         {
            // copy info from previous node if exist
            if (prevItem.getLocator() != null)
               item.setLocator(prevItem.getLocator());

            if (prevItem.getLastLoad() != null)
               item.setLastLoad(prevItem.getLastLoad());
               
            setStatusFromExtractorDef(item, prevItem);
         }
      }
   }

   /**
    * Comparing global item transformers between the previous and the current
    * ones.
    *
    * @return <code>true</code> if they are the same; otherwise,
    *    <code>false</code>.
    */
   private boolean compareGlobalItemTransformers()
   {
      PSItemTransformationsDef prevTrans, currTrans;

      prevTrans = m_prevDescriptor.getItemTransDef();
      currTrans = m_loaderDesc.getItemTransDef();

      if ( currTrans == null && prevTrans == null)
      {
         return true; // no both null
      }
      else if (currTrans == null || prevTrans == null)
      {
         return false; // one of them is null
      }
      else
      {
         return currTrans.equals(prevTrans);
      }
   }

   /**
    * Comparing global field transformers between the previous and the current
    * ones.
    *
    * @return <code>true</code> if they are the same; otherwise,
    *    <code>false</code>.
    */
   private boolean compareGlobalFieldTransformers()
   {
      // get the global transformers for both prev and current
      PSFieldTransformationsDef prevFieldTrans, currFieldTrans;

      prevFieldTrans = m_prevDescriptor.getFieldTransDef();
      currFieldTrans = m_loaderDesc.getFieldTransDef();

      if ( currFieldTrans == null && prevFieldTrans == null)
      {
         return true; // no both null
      }
      else if (currFieldTrans == null || prevFieldTrans == null)
      {
         return false; // one of them is null
      }
      else
      {
         return currFieldTrans.equals(prevFieldTrans);
      }
   }

   /**
    * Set the current item status and extractor definition according to the
    * previous item context.
    *
    * @param item The current item, assuem not <code>null</code>.
    *
    * @param prevItem The previous scanned item, assume not <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void setStatusFromExtractorDef(PSItemContext item,
      PSItemContext prevItem)
      throws PSLoaderException
   {
      PSExtractorDef currDef = item.getExtractorDef();
      PSExtractorDef prevDef = prevItem.getExtractorDef();

      // if current guess is not the same as the previous one
      // set to the previous extractor if found a match one
      if (! currDef.equals(prevDef))
      {
         PSExtractorDef findDef = null;
         try
         {
            IPSItemExtractor extractor = null;
            String prevName = prevDef.getName();
            extractor = (IPSItemExtractor) m_staticExtractors.get(prevName);
            if (extractor == null)
               extractor = (IPSItemExtractor) m_itemExtractors.get(prevName);
            if (extractor != null)
               findDef = new PSExtractorDef(extractor.getConfigure());
         }
         catch (PSUnknownNodeTypeException e)
         {
            throw new PSLoaderException(e);
         }
         if (findDef != null)
            item.setExtractorDef(findDef);
      }

      if ((prevItem.getChecksum() != -1) &&
          (item.getChecksum() != -1) &&
          (prevItem.getChecksum() == item.getChecksum()))
      {
         item.setStatus(prevItem.getStatus());
      }
      else
      {
         if (item.getLocator() != null)
            item.setStatus(PSItemContext.STATUS_MODIFIED);
      }
   }

   /**
    * Get the extractor with the specified name.
    *
    * @param name The extractor name, assume not <code>null</code> or empty.
    *
    * @return The extractor definition with the specified name. It may be
    *    <code>null</code> if cannot find such extractor.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private PSExtractorDef findExtractorDef(String name)
      throws PSLoaderException
   {
      PSExtractorDef findDef = null;

      IPSItemExtractor extractor = null;

      try
      {
         extractor = (IPSItemExtractor) m_staticExtractors.get(name);
         if (extractor == null)
            extractor = (IPSItemExtractor) m_itemExtractors.get(name);
         if (extractor != null)
            findDef = new PSExtractorDef(extractor.getConfigure());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSLoaderException(e);
      }

      return findDef;
   }

   /**
    * Determines whether a given item is interested by one of the extractors
    * that is in both static and item list of extractors.
    *
    * @param item The item that will be interrogated by the list of extractors.
    *
    * @return <code>true</code> if find one extractor that is interested the
    *    <code>item</code>; <code>false</code> otherwise.
    */
   private boolean isExtractableItem(PSItemContext item)
   {
      boolean result = false;
      IPSItemExtractor extractor;
      extractor = getExtractorForItem(m_staticExtractors.values().iterator(),
         item);

      if (extractor == null)
         extractor = getExtractorForItem(m_itemExtractors.values().iterator(),
            item);

      if (extractor != null)
      {
         PSExtractorDef extractorDef;
         try
         {
            extractorDef = new PSExtractorDef(extractor.getConfigure());
            item.setExtractorDef(extractorDef);
            result = true;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return result;
   }

   /**
    * Get the mapping of the resource-id and its related tree node.
    *
    * @param tree The tree that will be mapped, it may be <code>null</code>.
    *
    * @return A map with the resource-id as the key (in <code>String</code>)
    *    and its related tree node as the value
    *    (in <code>IPSContentTreeNode</code>). It will never <code>null</code>,
    *    but may be empty.
    */
   private Map getResourceIdMap(IPSContentTree tree)
   {
      Map mapId = new HashMap();

      if ( tree != null )
      {
         List nodeList = getListFromTree(tree);
         Iterator nodes = nodeList.iterator();
         while (nodes.hasNext())
         {
            IPSContentTreeNode node = (IPSContentTreeNode) nodes.next();
            PSItemContext item = node.getItemContext();
            mapId.put(item.getResourceId(), node);
         }
      }

      return mapId;
   }

   /**
    * Adds all progress listeners to the content selector object.
    *
    * @throws IllegalStateException if the content selector has not been set.
    */
   protected void addProgressListenersToSelector()
   {
      if (m_selector == null)
         throw new IllegalArgumentException("m_selector has not been set");

      Iterator progressListener = m_progressListeners.iterator();
      while (progressListener.hasNext())
      {
         IPSProgressListener listener;
         listener = (IPSProgressListener) progressListener.next();
         m_selector.addProgressListener(listener);
      }
   }

   /**
    * Removes all progress listeners from the content selector object.
    *
    * @throws IllegalStateException if the content selector has not been set.
    */
   protected void removeProgressListenersFromSelector()
   {
      if (m_selector == null)
         throw new IllegalArgumentException("m_selector has not been set");

      Iterator progressListener = m_progressListeners.iterator();
      while (progressListener.hasNext())
      {
         IPSProgressListener listener;
         listener = (IPSProgressListener) progressListener.next();
         m_selector.removeProgressListener(listener);
      }
   }

   /**
    * Get the content tree which is processed by this object.
    *
    * @return The content tree, may be <code>null</code>
    *
    */
   public IPSContentTree getContentTree()
   {
      return m_contentTree;
   }

   /**
    * Aborting current process
    */
   public void abort()
   {
      if (! isAborting())
      {
         super.abort();
         m_selector.abort();
      }
   }


   /**
    * The current content tree has been processes by this object.
    * It may be <code>null</code> if has not been created by the selector.
    */
   private IPSContentTree m_contentTree = null;

   /**
    * The previous content tree, it was processed by another selector manager
    * with the same descriptor, may be <code>null</code> if there is no
    * history content tree available.
    */
   private IPSContentTree m_prevContentTree = null;

   /**
    * The previous / last descriptor that was used for the content loader
    * operation. Initialized by the constructor, may be <code>null</code> if
    * no exist
    */
   private PSLoaderDescriptor m_prevDescriptor = null;

}
