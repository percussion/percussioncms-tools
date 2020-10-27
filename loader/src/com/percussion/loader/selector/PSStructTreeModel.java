/**[ PSStructTreeModel.java ]**************************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSConfigurationException;
import com.percussion.loader.PSItemContext;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import websphinx.Link;

/**
 * Implemented {@link #IPSContentTree} that represents a structural data
 * view.
 *    This tree constructs its data model from an existing
 * <code>IPSContentTree</code> dependency view.
*/
public class PSStructTreeModel implements IPSContentTree
{
   /**
    * Default Constructor
    */
   public PSStructTreeModel()
   {
   }

   /**
    * Constructor creates this object given a set of String url's
    * to act as roots.
    *
    * @param roots Iterator over String representing a root url.
    *     Never <code>null</code>. At least one.
    *
    * @throws IllegalArgumentException if <code>roots</code>
    *    is invalid.
    *
    * @throws PSConfigurationException if one of the roots has an invalid
    *    String url.
    */
   public PSStructTreeModel(Iterator roots)
      throws PSConfigurationException
   {
      if (roots == null)
         throw new IllegalArgumentException(
            "roots must not be null");

      setRoots(roots);

      if (m_roots.size() < 1 )
         throw new IllegalArgumentException(
            "must have at least one root");
   }

   /**
    * Convience method to determine, based on the node parameter,
    * which server this data pertains to.
    *
    * @return String the server:port string. Never <code>null</code>
    *    May be empty.
    */
   public String getServer(IPSContentTreeNode aNode)
   {
      String strUrl = getUrlFromNode(aNode);

      try
      {
         URL url = new URL(strUrl);

         String aServer = url.getHost();
         int nPort = url.getPort();

         if (nPort < 1)
            return aServer;

         return aServer + ":" + nPort;
      }
      catch (Exception e)
      {
         return "";
      }
   }

   /**
    * Imports these roots.
    *
    * @param roots An iterator over String objects defining
    *    the root url's that may exist for this scanned content.
    *    Never <code>null</code> and must contain at least one root.
    *
    * @throws IllegalArgumentException if <code>def</code>
    *    is invalid.
    *
    * @throws PSConfigurationException if one of the roots has an invalid
    *    String url.
    */
   private void setRoots(Iterator roots)
      throws PSConfigurationException
   {
      if (roots == null)
         throw new IllegalArgumentException(
            "roots must not be null");

      while (roots.hasNext())
      {
         String strRootVal = (String) roots.next();
         java.net.URL url = null;

         try
         {
            if (!strRootVal.startsWith("file://")
               && !strRootVal.startsWith("http://")
               && !strRootVal.startsWith("https://")
               && !strRootVal.startsWith("ftp://"))
            {
               if (isADirectory(strRootVal))
               {
                  if (!strRootVal.endsWith("/") && !strRootVal.endsWith("\\"))
                     strRootVal += "/";
               }

               url = new java.net.URL(
                  "file", "localhost", "/" + strRootVal);
            }
            else
            {
               if (isADirectory(strRootVal))
                  url = new java.net.URL(strRootVal);
               else
                  url = new java.net.URL(this.getParentUrl(strRootVal));
            }
         }
         catch (Exception e)
         {
            throw new PSConfigurationException(
               IPSLoaderErrors.UNEXPECTED_ERROR,
               new Object []
               {
                  e.getMessage()
               });
         }

         IPSContentTreeNode aRoot = createTreeNode(url.toString());
         // Put it into the root list
         m_roots.addElement(aRoot);
      }
   }

   /**
    * Creates a <code>IPSContentTreeNode</code> based on
    * a String url.
    *
    * @param strUrl String url never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is invalid.
    */
   private IPSContentTreeNode createTreeNode(String strUrl)
   {
      if (strUrl == null || strUrl.trim().length() < 1)
         throw new IllegalArgumentException(
            "strUrl must not null or empty");

      PSContentTreeNode node = new PSContentTreeNode();
      PSItemContext itemC = new PSItemContext(
         strUrl, null, "content/unknown");

      node.setItemContext(itemC);
      return node;
   }

   /**
    * Creates a <code>IPSContentTreeNode</code> based on
    * an existing tree node.
    *
    * @param node IPSContentTreeNode never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>node</code> is invalid.
    */
   private IPSContentTreeNode createTreeNode(IPSContentTreeNode oldNode)
   {
      if (oldNode == null)
         throw new IllegalArgumentException(
            "oldNode must not be null");

      PSContentTreeNode node = new PSContentTreeNode();
      node.setItemContext(oldNode.getItemContext());
      return node;
   }

   /**
    * Processes the existing <code>IPSContentTree</code>, importing
    * the nodes into this model and any associated transformations.
    *
    * @param model IPSContentTree to import. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>model</code> is null.
    */
   public void importDependencyModel(IPSContentTree model)
   {
      if (model == null)
         throw new IllegalArgumentException(
            "model must not be null");

      List nodeList = model.getNodes();
      Iterator nodes = nodeList.iterator();
      while (nodes.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode) nodes.next();

         PSItemContext item = node.getItemContext();
         String strUrl = item.getResourceId();

         m_links.put(strUrl, this.createTreeNode(node));

         // Attempt to add all parentUrl's until we hit a root
         while (strUrl != null && addParentUrl(strUrl))
         {
            strUrl = getParentUrl(strUrl);
         }
      }
      resolveParentNodes();
   }

   /**
    * Method retrieves the parent url of the supplied
    * url.
    *
    * @param url A String, the url. Never <code>null</code> or
    *    empty.
    *
    * @return a String of the parent url. Never <code>null</code> or
    *    empty. If an exception is encountered <code>null</code> is
    *    returned.
    *
    * @throws IllegalArgumentException if <code>url</code> is
    *    invalid
    */
   private String getParentUrl(String url)
   {
      if (url == null || url.trim().length() < 1)
         throw new IllegalArgumentException(
            "url must not be null or empty");

      try
      {
          return Link.getParentURL(new URL(url)).toString();
      }
      catch (Exception e)
      {
         System.out.println("[getParentUrl]  url = " + url +
            ", exception = " + e.toString());

         return null;
      }
   }

   /**
    * Method to add the parent url of <code>strUrl</code> to
    * the hash entries.
    *
    * @param strUrl a String url. Never <code>null</code> or empty.
    *
    * @return boolean <code>true</code> if we added this parent url to
    *    the hash, <code>false</code> otherwise. For example, if this parent
    *    url is above a root it will not be added.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is invalid.
    */
   private boolean addParentUrl(String strUrl)
   {
      if (strUrl == null || strUrl.trim().length() < 1)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      // Get's the parent url from strUrl and
      // creates a node based on it and
      // adds it to the hash if it is below
      // a root.
      String strParent = getParentUrl(strUrl);

      // Check if it IS a root, if so return false.
      // Check if it contains a root as a substring, if so add it, return true
      // If it doesn't contain a root, return false
      if (isAroot(strParent))
         return false;

      if (!containsAroot(strParent))
         return false;

      m_links.put(strParent, this.createTreeNode(strParent));
      return true;
   }

   /**
    * Tests whether this url is a root url specfied by this object.
    *
    * @param strParentUrl a String url. Never <code>null</code> may be empty.
    *
    * @return boolean <code>true</code> if it is a root, <code>false</code>
    *    otherwise.
    *
    * @throws IllegalArgumentException if <code>strParentUrl</code> is
    *    <code>null</code>
    */
   private boolean isAroot(String strParentUrl)
   {
      if (strParentUrl == null)
         throw new IllegalArgumentException(
            "strParentUrl must not be null");

      if (strParentUrl.trim().length() < 1)
         return false;

      for (int i=0; i<m_roots.size(); i++)
      {
         String strRootUrl = getUrlFromNode(
            (PSContentTreeNode) m_roots.elementAt(i));

         if (strParentUrl.equalsIgnoreCase(strRootUrl))
            return true;
      }

      return false;
   }

   /**
    * Tests whether <code>strUrl</code> contains via String
    * substring any root url specifed by this object.
    *
    * @param strUrl String url. Never <code>null</code> or
    *    empty.
    *
    * @return boolean <code>true</code> if <code>strUrl</code>
    *    contains a root within its url, otherwise <code>false</code> if it
    *    doesn't contains any root url string.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is <code>
    *    null</code> or empty.
    */
   private boolean containsAroot(String strUrl)
   {
      if (strUrl == null || strUrl.trim().length() < 1)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      for (int i=0; i<m_roots.size(); i++)
      {
         String strRootUrl = getUrlFromNode(
            (PSContentTreeNode) m_roots.elementAt(i));

         if (strUrl.indexOf(strRootUrl) >= 0)
            return true;
      }

      return false;
   }

   /**
    * Retrieves the resourceId from the PSItemContext of this
    * node. In this case, a url.
    *
    * @param n IPSContentTreeNode. Never <code>null</code>.
    *
    * @return String the resourceId, a url in this case.
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>n</code> is
    *    <code>null</code>.
    */
   private String getUrlFromNode(IPSContentTreeNode n)
   {
      if (n == null)
         throw new IllegalArgumentException(
            "n must not be null");

      PSItemContext itemC = n.getItemContext();

      if (itemC == null)
         return "";

      return itemC.getResourceId();
   }

   /**
    * Method 'hooks up' nodes into parent-child relationships.
    */
   private void resolveParentNodes()
   {
      Enumeration keys = m_links.keys();

      while (keys.hasMoreElements())
      {
         // Find a Parent
         IPSContentTreeNode node = (IPSContentTreeNode)
            m_links.get(keys.nextElement());

         IPSContentTreeNode parent = null;
         String parentUrl = getParentUrl(getUrlFromNode(node));
         if (parentUrl != null)
            parent = findNode(parentUrl);

         if (parent != null)
         {
            // Sets the parent as well
            parent.addChild(node);
         }
      }
   }

   /**
    * Searches for a node from the hash based on a url which is what the
    * hash is keyed upon.
    *
    * @param strUrl a String key to lookup. Never <code>null</code> or
    *    empty.
    *
    * @return IPSContentTreeNode the object of the key in the hash. May be
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is <code>
    *    null</code> or empty.
    */
   private IPSContentTreeNode findNode(String strUrl)
   {
      if (strUrl == null || strUrl.trim().length() < 1)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      IPSContentTreeNode node = null;

      // See if it's a root
      for (int i=0; i<m_roots.size(); i++)
      {
         node = (IPSContentTreeNode) m_roots.elementAt(i);
         String strRootUrl = getUrlFromNode(node);

         if (strRootUrl.equalsIgnoreCase(strUrl))
            return node;
      }

      return (IPSContentTreeNode) m_links.get(strUrl);
   }

   /**
    * Convience method to search for a node in the hash.
    *
    * see {@link #findNode(String strUrl)} for a description.
    */
   private IPSContentTreeNode findNode(IPSContentTreeNode node)
   {
      return (IPSContentTreeNode) m_links.get(getUrlFromNode(node));
   }

   /**
    * IPSContentTree implementation
    *
    */

   /**
    * see {@link #IPSContentTree} for description.
    */
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

   /**
    * see {@link #IPSContentTree} for description.
    */
   public int getChildCount(IPSContentTreeNode parent)
   {
      /**
       * Threshold
       */
      if (parent == null)
         throw new IllegalArgumentException("the parent node cannot be null");

      /**
       * Get the iterator of this nodes children
       */
      Iterator iter = parent.getChildren();
      int nCount = 0;
      while (iter.hasNext())
      {
         nCount++;
         iter.next();
      }

      return nCount;
   }

   /**
    * see {@link #IPSContentTree} for description.
    */
   public int getIndexOfChild(IPSContentTreeNode parent,
      IPSContentTreeNode child)
   {
      /**
       * Threshold
       */
      if (parent == null)
         throw new IllegalArgumentException("the parent node cannot be null");

      if (child == null)
         throw new IllegalArgumentException("the child node cannot be null");

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

   /**
    * see {@link #IPSContentTree} for description.
    */
   public Iterator getRoots()
      throws IllegalStateException
   {
      return m_roots.iterator();
   }

   /**
    * see {@link #IPSContentTree} for description.
    */
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
    * A list of root, initialized in definition, never <code>null</code>
    * may be empty.
    */
   private Vector m_roots = new Vector();

   /**
    * Hash to store the urls
    */
   private transient Hashtable m_links = new Hashtable();
}
