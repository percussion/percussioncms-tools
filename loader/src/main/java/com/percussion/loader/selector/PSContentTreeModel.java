/*[ PSContentTreeModel.java ]*************************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *****************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.PSLogMessage;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import websphinx.Link;
import websphinx.LinkEvent;
import websphinx.Page;

/**
 * Implemented {@link #IPSContentTree} that represents a crawled data
 * source. The nodes of the tree are {@link #IPSContentTreeNode} which
 * wrapper {@link #PSItemContext} objects accessible via setItemContext()
 * and getItemContext().
 * <p>
 * This tree listens via {@link #IPSCrawlListener}
 * to a particular {@link #PSCrawler} and constructs its data model
 * from the crawled source. This class is the data model contained within
 * {@link #PSWebContentSelector}.
 *
 * @see IPSContentTree
 * @see IPSCotentTreeNode
 * @see PSItemContext
 * @see IPSCrawlListener
 * @see PSWebContentSelector
 *
*/

public class PSContentTreeModel implements IPSContentTree, IPSCrawlListener
{
   /**
    * Default Construction
    */
   public PSContentTreeModel()
   {
   }

   /**
    * IPSContentTree implementation
    *
    */

   // see {@link #IPSContentTree} for description.
   public IPSContentTreeNode getChild(IPSContentTreeNode parent, int nIndex)
   {
      if (parent == null)
         throw new IllegalArgumentException("the parent node cannot be null");

      /**
       * Threshold
       */
      if (!parent.hasChildren())
         return null;

      /**
       * Get the child iterator
       */
      Iterator iter = parent.getChildren();
      int nCount = 0;

      while (iter.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode) iter.next();

         if (nCount++ == nIndex)
            return node;
      }

      return null;
   }

   // see {@link #IPSContentTree} for description.
   public int getChildCount(IPSContentTreeNode parent)
   {
      /**
       * Threshold
       */
      if (parent == null)
         throw new IllegalArgumentException(
            "the parent node cannot be null");

      /**
       * Get the iterator of this nodes children
       */
      Iterator iter = parent.getChildren();
      int nCount = 0;
      while (iter.hasNext())
         nCount++;

      return nCount;
   }

   // see {@link #IPSContentTree} for description.
   public int getIndexOfChild(IPSContentTreeNode parent,
      IPSContentTreeNode child)
   {
      /**
       * Threshold
       */
      if (parent == null)
         throw new IllegalArgumentException(
            "the parent node cannot be null");

      if (child == null)
         throw new IllegalArgumentException(
            "the child node cannot be null");

      /**
       * Threshold
       */
      if (!parent.hasChildren())
         return -1;

      /**
       * Get the child iterator
       */
      Iterator iter = parent.getChildren();
      int nCount = 0;
      IPSContentTreeNode node = null;

      while (iter.hasNext())
      {
         /**
          * Cast the node to a IPSCotentTreeNode
          */
         node = (IPSContentTreeNode) iter.next();
         nCount++;

         if (node == child)
            return nCount;
      }

      return -1;
   }

   // see {@link #IPSContentTree} for description.
   public Iterator getRoots()
      throws IllegalStateException
   {
      if ((m_roots == null) && m_links.isEmpty())
          return null;

      if (m_roots == null)
      {
         m_roots = new Vector();
         Enumeration nodes = m_links.elements();
         String strKey = "";
         IPSContentTreeNode n = null;
         IPSContentTreeNode parent = null;
         Link l = null;
         PSItemContext item = null;

         while (nodes.hasMoreElements())
         {
            // retrieve the Tree Node
            n = (IPSContentTreeNode) nodes.nextElement();

            if (n.isRoot())
            {
               m_roots.addElement(n);
            }
         }
      }

      return m_roots.iterator();
   }

   // see {@link #IPSContentTree} for description.
   public boolean isLeaf(IPSContentTreeNode node)
   {
      return !node.hasChildren();
   }

   // see {@link #IPSContentTree} for description.
   public List getNodes()
   {
      Iterator values = m_links.values().iterator();
      List nodeList = new ArrayList();

      while (values.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode) values.next();
         nodeList.add(node);
      }

      return nodeList;
   }

   /**
    * IPSCrawlListener implementation
    *
    */

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void visited(PSCrawlEvent event)
   {
      if (event == null)
         throw new IllegalArgumentException("event must not be null.");

      PSItemContext itemContext = event.getItemContext();
      // May be null
      PSItemContext parentContext = event.getParentItemContext();

      // If the IPSSelector is passing explicit parents
      // Then do not resolve them when the IPSSelector stops
      if (parentContext != null)
      {
         m_bExplicitParents = true;
      }

      if (itemContext == null)
      {

         return;
      }

      Link l = (Link) itemContext.getDataObject();
      String strErrMsg = " ";
      PSLogMessage msg = null;

      // Informational logging for every node
      msg = new PSLogMessage(
         IPSLogCodes.LOG_SELECTORINFO,
         new Object []
         {
            l.getResourceId(),
            LinkEvent.eventName[l.getStatus()],
            ""
         },
         itemContext);

      Logger.getLogger(PSContentTreeModel.class.getName()).debug(
         msg.getMessage());

      switch (l.getStatus())
      {
         case LinkEvent.QUEUED:
         case LinkEvent.SKIPPED:
         case LinkEvent.RETRIEVING:
            return;

         case LinkEvent.ERROR:

            // Further logging for an encounterd error
            if (event.getException() != null)
            {
               strErrMsg = event.getException().getMessage();

               msg = new PSLogMessage(
                  IPSLogCodes.LOG_SELECTORERROR,
                  new Object []
                  {
                     l.getResourceId(),
                     strErrMsg
                  },
                  itemContext,
                  PSLogMessage.LEVEL_ERROR);

               Logger.getLogger(PSContentTreeModel.class.getName()).debug(
                  msg);

               // Remember these error links
               m_errorLinks.put(l.getResourceId().toLowerCase(), l);
            }

            // Do not continue
            return;

         case LinkEvent.ALREADY_VISITED:
         default:

            // If we've already visited a node but it encountered an error
            // do not load this node.
            String strKey = l.getResourceId().toLowerCase();

            if (m_errorLinks.containsKey(strKey))
               return;

            break;
      }

      addToDependentTree(itemContext, parentContext);
   }

   /**
    * Creates a node from a given item context if it does not exist in the
    * node list. Add the new node into the node list. The parent / child
    * relationship will be resolved during this process.
    *
    * @param itemContext The item context that contains the link to the current
    *    page, assume it is not <code>null<code>.
    *
    * @param parentContext The item context of a parent, it may be
    *    <code>null</code>. This object will be used to get the parent node
    *    if it is <code>null</code>.
    *
    * @throws IllegalArgumentException if {@link #l} is <code>null</code>.
    */
   private synchronized PSContentTreeNode addToDependentTree(
      PSItemContext itemContext, PSItemContext parentContext)
   {
      PSContentTreeNode parentNode = null;
      PSContentTreeNode currNode = null;
      Link currLink = (Link) itemContext.getDataObject();
      String filePath = currLink.getFile();

      if (isADirectory(filePath))
      {
         return null;  // ignore directory
      }

      // get the parent node
      if (parentContext != null)
      {
         Link pl = (Link) parentContext.getDataObject();
         parentNode = findNode(pl);
      }
      else if (currLink.getSource() != null)
      {
         Page page = currLink.getSource();
         Link pl = page.getOrigin();
         page.discardContent();
         if (pl != null)
            parentNode = findNode(pl);
      }

      return addNode(currLink, itemContext, parentNode);
   }

   /**
    * Add a node to the tree model from the given parameters if the specified
    * node does not exist, and add the node to be a child node of the specified
    * parent node (if it is not <code>null</code>).
    *
    * @param currLink The link of the to be added node. It may not be
    *    <code>null</code>
    *
    * @param itemCtx The item context of the to be added node. It may not be
    *    <code>null</code>.
    *
    * @param parentNode The parent node of the to be added node. It may be
    *    <code>null</code>.
    *
    * @return The created or existing node which corresponding to the given
    *    item context, never <code>null</code>.
    */
   PSContentTreeNode addNode(Link currLink, PSItemContext itemCtx,
      PSContentTreeNode parentNode)
   {
      return addNode(currLink, itemCtx, parentNode, true);
   }
   
   /**
    * Just link {@link addNode(Link, PSItemContext, PSContentTreeNode)},
    * except the caller can specify whether to log the added node operation.
    * 
    * @param logAction <code>true</code> if need to log the add node operation.
    */
   private PSContentTreeNode addNode(Link currLink, PSItemContext itemCtx,
      PSContentTreeNode parentNode, boolean logAction)
   {
      if (currLink == null)
         throw new IllegalArgumentException("currLink may not be null");
      if (itemCtx == null)
         throw new IllegalArgumentException("itemCtx may not be null");

      // add current node into the dependency tree
      PSContentTreeNode currNode = findNode(currLink);
      if (currNode == null) // insert a new node if not exist
      {

         if (logAction)
         {
            PSLogMessage   msg = new PSLogMessage(
               IPSLogCodes.LOG_ADDNODE,
               itemCtx.getResourceId(),
               itemCtx);
            Logger.getLogger(PSContentTreeModel.class.getName()).info(msg);
         }

         currNode = new PSContentTreeNode();
         itemCtx.setDataObject(currLink);
         currNode.setItemContext(itemCtx);

         if (parentNode != null)
            parentNode.addChild(currNode);
         m_links.put(currLink.getResourceId(), currNode);
      }
      else
      {
         if (logAction)
         {
            PSLogMessage  msg = new PSLogMessage(
               IPSLogCodes.LOG_UPDATENODE,
               new String []
               {
                  itemCtx.getResourceId(),
                  LinkEvent.eventName[currLink.getStatus()]
               },
               itemCtx
               );
            Logger.getLogger(PSContentTreeModel.class.getName()).info(msg);
         }


         // if current node is already parent of "parentNode",
         // add terminatedNode (of current node) to the "parentNode"
         if (parentNode != null &&  parentNode.isParent(currNode))
         {
            PSContentTreeNode terminatedNode = getTermindatedNode(currLink,
               currNode.getItemContext());
            parentNode.addChild(terminatedNode);
         }
         else if (parentNode != null)
         {
            parentNode.addChild(currNode);
         }
      }

      return currNode;
   }

   /**
    * A new node will added to this tree model if there is no node that
    * contains the specified item context. The new node will contains the
    * specified item context, and it has no parent or child node. There is no 
    * log for this operation.
    *
    * @param itemCtx The item context for the new node. It may not be
    *    <code>null</code>.
    */
   public void addNode(PSItemContext itemCtx)
   {
      if (itemCtx == null)
         throw new IllegalArgumentException("itemCtx may not be null");

      Link currLink = null;
      try 
      {
         currLink = new Link(itemCtx.getResourceId());
      }
      catch (MalformedURLException ex)  // this should never happen
      {
         ex.printStackTrace();
         throw new RuntimeException("Malformed URL, caused by: " + 
            itemCtx.getResourceId());
      }
      addNode(currLink, itemCtx, null, false);
   }

   /**
    * Get a terminated node that is corresponding to a specified link. If
    * such node does not exist, make a new one with a specified item context.
    *
    * @param link The specified link, used for searching the node, assume not
    *    <code>null</code>.
    *
    * @param item The item context object, used to create a new node, assume
    *    not <code>null</code>.
    *
    * @return An existing or a new node, never <code>null</code>.
    */
   private PSContentTreeNode getTermindatedNode(Link link, PSItemContext item)
   {
      PSContentTreeNode node = findTerminatedNode(link);
      if (node == null)
      {
         node = new PSContentTreeNode();
         node.setItemContext(item);
         node.terminateBranch(null);
         m_terminatedLinks.put(link, node);
      }

      return node;
   }

   /**
    * Find a terminated node that is mapped to a specified link object.
    *
    * @param link The search link object, assume not <code>null</code>.
    *
    * @return The searched node if found; otherwise return <code>null</code>.
    */
   private PSContentTreeNode findTerminatedNode(Link link)
   {
      Enumeration keys = m_terminatedLinks.keys();

      while (keys.hasMoreElements())
      {
         Link lCmp = (Link) keys.nextElement();

         if (lCmp.getResourceId().equalsIgnoreCase(
            link.getResourceId()))
            return (PSContentTreeNode) m_links.get(lCmp.getResourceId());
      }

      return null;
   }

   /**
    * @todo send status changes using log events !!!
    */

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void started(PSCrawlEvent event)
   {
      m_roots = null;
      m_links.clear();
      m_errorLinks.clear();
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void stopped(PSCrawlEvent event)
   {
      // Clear out roots
      m_roots = null;

      if (!m_bExplicitParents)
      {
         // Resolves mimetypes
         resolveMimeTypes();
      }
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void cleared(PSCrawlEvent event)
   {
      //Clear our data model
      m_links.clear();

      // Clear error links
      m_errorLinks.clear();

      // Clear out roots
      m_roots = null;

   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void timedOut(PSCrawlEvent event)
   {
   }

   /**
    * see {@link #IPSCrawlListener} for description.
    */
   public void paused(PSCrawlEvent event)
   {
   }

   /**
    * Looks in {#m_links} for a node that corresponds
    * to this link object.
    *
    * @param l {@link #webpshinx.Link} object must not be <code>null<code>.
    *
    * @return {@link #webpshinx.Link} object may be <code>null<code> if
    *    node does not exist.
    *
    * @throws IllegalArgumentException if {@link #l} is <code>null</code>.
    */
   PSContentTreeNode findNode(Link l)
   {
      if (l == null)
         throw new IllegalArgumentException("link must not be null.");

      return (PSContentTreeNode) m_links.get(l.getResourceId());
   }

   /**
    * After a scan is done this method resolves their
    * mime types.
    */
   private void resolveMimeTypes()
   {
      Iterator values = m_links.values().iterator();

      while (values.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode) values.next();
         PSItemContext item = node.getItemContext();

         // Attempt to resolve mimetype
         try
         {
            String strMimeType
               = PSLoaderUtils.getMimeType(item.getResourceId());

            item.setResourceMimeType(strMimeType);
         }
         catch (Exception ignore)
         {}
      }
   }

   /**
    * Tests whether this string is a directory.
    *
    * @param strUrl A string to check.
    *    Never <code>null</code> or empty.
    *
    * @return boolean <code>true</code> is a directory, otherwise
    *    </code>false</code>
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is
    *    <code>null</code>
    */
   private boolean isADirectory(String strUrl)
   {
      if (strUrl == null || strUrl.trim().length() < 1)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      try
      {
         File f = new File(strUrl);

         if (f.exists() && f.isDirectory())
            return true;

      }
      catch (Exception ignored)
      {
      }

      return false;
   }

   /**
    * Debugging method to print the tree list.
    *
    * @param tree The to be printed tree, assume not <code>null</code>.
    * @param treeList The list to be printed, a list over zero or more
    *    <code>IPSContentTreeNode</code> object, assume not <code>null</code>.
    */
   void printNodeList()
   {
      List nodeList = getNodes();

      // print the roots
      Iterator roots = getRoots();
      while (roots.hasNext())
      {
         IPSContentTreeNode root = (IPSContentTreeNode) roots.next();
         PSItemContext item = root.getItemContext();
         System.out.println("printNodeList [ROOT]: " + item.getResourceId());
      }

      // print the generated node list
      Iterator nodes = nodeList.iterator();
      int i=0;
      while (nodes.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode)nodes.next();
         PSItemContext item = node.getItemContext();

         if (node.isBranchTerminated())
            System.out.println("ERROR node [" + i + "] " +
               node.getItemContext().getResourceId());
         else
            System.out.println("printNodeList[" + i  + "] " +
               node.getItemContext().getResourceId());

         // print the parent nodes
         Iterator parents = node.getParents();
         while (parents.hasNext())
         {
            IPSContentTreeNode pnode = (IPSContentTreeNode)parents.next();
            item = pnode.getItemContext();
            System.out.println("                      PARENT = " +
               item.getResourceId());
         }
         // print the children nodes
         Iterator children = node.getChildren();
         while (children.hasNext())
         {
            IPSContentTreeNode cnode = (IPSContentTreeNode)children.next();
            item = cnode.getItemContext();
            System.out.println("                       CHILD = " +
               item.getResourceId());
         }
         i++;
      }
   }

   /**
    * Hashtable consisting a list of <code>Link</code>s (as key) that map to its
    * corresponding <code>IPSContentTreeNode</code> (as value)
    */
   private transient Hashtable m_terminatedLinks = new Hashtable();

   /**
    * Hashtable consisting of (key, value) => (String, IPSContentTreeNode).
    */
   private Hashtable m_links = new Hashtable();

   /**
    * Hashtable consisting of (key, value) => (strUrl, IPSContentTreeNode). for error's
    * encountered while scanning.
    */
   private transient Hashtable m_errorLinks = new Hashtable();

   /**
    * boolean whether or not the IPSSelector we are listening to
    * is explicitly specifying it's parent nodes. If <code>true</code>
    * we do not resolve parent nodes within <code>stopped</code>.
    * Initialized in definition, defaults to <code>false</code>, may
    * be set in <code>visited</code>.
    */
   private boolean m_bExplicitParents = false;

   /**
    * A list of root, init to <code>null</code>, only modified/set by
    * <code>getRoots()</code>.
    */
   private Vector m_roots = null;
}
