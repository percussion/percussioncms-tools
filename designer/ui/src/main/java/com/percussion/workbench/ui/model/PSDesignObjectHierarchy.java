/******************************************************************************
 *
 * [ PSDesignObjectHierarchy.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.client.IPSCoreListener;
import com.percussion.client.IPSHierarchyNodeRef;
import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSLogonStateChangedEvent;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.error.PSClientException;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.dnd.PSReferenceTransfer;
import com.percussion.workbench.ui.model.IPSHierarchyChangeListener.HierarchyChangeType;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.views.PSAssemblyDesignView;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefinitionException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static com.percussion.workbench.ui.model.IPSHierarchyChangeListener.HierarchyChangeType.MODEL_DEACTIVATED;

/**
 * This is the model that manages the hierarchical representation of the CMS
 * design objects within the workbench. It is a declarative model, so may change
 * from session to session. It does not change declaratively during a workbench
 * session.
 * <p>
 * The hierarchy is composed of named 'tree fragments'. A tree fragment is
 * effectively a sub-directory off of the root directory. Users of this class
 * can focus on a single fragment, or the whole tree. The tree fragment name is
 * called a 'path' in methods that use it and should be given in the form
 * '/fragmentName'. Each fragment is defined declaratively via an xml file.
 * <p>
 * This is a singleton implementation.
 * 
 * @author paulhoward
 */
public class PSDesignObjectHierarchy implements IPSDesignObjectChangeListener,
      IPSModelListener
{
   /**
    * Implementation of the singleton pattern.
    * 
    * @return The one and only instance. Never <code>null</code>.
    */
   synchronized public static PSDesignObjectHierarchy getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSDesignObjectHierarchy();
      return ms_instance;
   }

   /**
    * Every time a design object is created, deleted or modified, all registered
    * listeners will be notified.
    * 
    * @param l Never <code>null</code>. If already registered for the
    * specified root, the supplied one replaces the existing one.
    * 
    * @param treeName If provided (non-<code>null</code>), only changes
    * within this section of the hierarchy are sent to this listener. If
    * <code>null</code> or empty, this listener gets all changes. Must be the
    * name of a root node (case-insensitive) as returned by
    * {@link #getRootNames()}. If you register for all changes, then any
    * listeners previously registered for specific roots are removed.
    */
   public void addListener(IPSHierarchyChangeListener l, String treeName)
   {
      if (null == l)
      {
         throw new IllegalArgumentException("listener cannot be null");
      }
      if (!StringUtils.isEmpty(treeName) && !isValidRootName(treeName))
      {
         throw new IllegalArgumentException(treeName
               + " is not a valid root name");
      }

      Set<IPSHierarchyChangeListener> listeners = getListeners(treeName);
      listeners.add(l);

      // if registering for all, remove any specific listeners
      if (StringUtils.isEmpty(treeName))
      {
         for (String name : getRootNames())
         {
            getListeners(name).remove(l);
         }
      }
   }

   /**
    * Performs a case-insensitive check against all known root nodes.
    * 
    * @param treeName The name to check. May be <code>null</code> or empty.
    * 
    * @return If the supplied name matches the name of a root node returned by
    * {@link #getRootNames()}, case-insensitive, <code>true</code> is
    * returned, otherwise <code>false</code>.
    */
   private boolean isValidRootName(String treeName)
   {
      if (StringUtils.isEmpty(treeName))
         return false;

      for (String name : getRootNames())
      {
         if (name.equalsIgnoreCase(treeName))
            return true;
      }
      return false;
   }

   /**
    * Removes the supplied listener previously registered with
    * {@link #addListener(IPSHierarchyChangeListener, String)}. If l is not
    * currently registered, no action is taken.
    * <p>
    * See that method for description of params.
    */
   public void removeListener(IPSHierarchyChangeListener l, String rootName)
   {
      if (null == l)
      {
         throw new IllegalArgumentException("listener cannot be null");
      }
      if (!StringUtils.isEmpty(rootName) && !isValidRootName(rootName))
      {
         throw new IllegalArgumentException(rootName
               + " is not a valid root name");
      }

      Set<IPSHierarchyChangeListener> listeners = getListeners(rootName);
      listeners.remove(l);
   }

   /**
    * Builds a path that is unique among all trees managed by this model. This
    * may be useful if you need to convert a node to a string a back again. This
    * path can be passed to {@link #getNodes(String[])}.
    * 
    * @param node Never <code>null</code>.
    * 
    * @return Never <code>null</code> or empty. Will be of the form
    * <pre>
    *    //treename/node.getPath()
    * </pre>
    */
   public String getFullyQualifiedPath(PSUiReference node)
   {
      PSUiReference ancestor = node;
      while (ancestor.getParentNode() != null)
         ancestor = ancestor.getParentNode();
      String treeName = ancestor.getProperty(ROOTNAME_PROPNAME).toString();
      assert(!StringUtils.isBlank(treeName));
      
      return "//" + treeName + node.getPath(); 
   }
   
   /**
    * Catalogs the root children of the requested tree.
    * 
    * @param rootName One of the values returned by {@link #getRootNames()}. If
    * it isn't, a node w/ an error message is returned.
    * 
    * @return The root children of this tree fragment. May be empty, never
    * <code>null</code>.
    * 
    * @throws PSModelException If any problems getting the root children.
    */
   public List<PSUiReference> getChildren(String rootName)
      throws PSModelException
   {
      if (StringUtils.isBlank(rootName))
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      List<PSUiReference> results;
      if (!m_connected)
      {
         PSUiReference node = new PSUiReference(
               null,
               PSMessages.getString("PSDesignObjectHierarchy.disconnected.label"),
               PSMessages.getString("PSDesignObjectHierarchy.disconnected.tooltip"),
               null, null, false);
         results = Collections.singletonList(node);
         return results;
      }
      
      try
      {
         PSHierarchyDefProcessor proc = getProcessor(rootName);
         if (null == proc)
         {
            results = new ArrayList<PSUiReference>();
            // don't i18n because it should never happen except during dev
            results.add(new PSUiReference(null,
                  "<Error Occurred>: invalid tree name: " + rootName,
                  "No definition registered by this name.", null, null,
                  false));
            return results;
         }
         results = proc.getChildren(null);
         for (PSUiReference node : results)
         {
            node.setProperty(ROOTNAME_PROPNAME, rootName);
         }
      }
      catch (PSHierarchyDefinitionException e)
      {
         results = new ArrayList<PSUiReference>();
         // don't i18n because it should never happen except during dev
         String msg = "<Error Occurred>: invalid definition: " + rootName; 
         results.add(new PSUiReference(null, msg, e.getLocalizedMessage(),
               null, null, false));
         ms_logger.error(msg, e);
         return results;
      }
      processCatalog(null, rootName, results);
      return results;
   }

   /**
    * Performs local book-keeping. Home node management and caching is done on
    * the supplied results.
    * 
    * @param parent The parent of the results. May be <code>null</code> for the
    * root node.
    * 
    * @param treeName The tree fragment that the results came from. Assumed not
    * <code>null</code> or empty.
    * 
    * @param results Assumed not <code>null</code>.
    */
   private void processCatalog(PSUiReference parent, String treeName,
         List<PSUiReference> results)
   {
      if (results.size() > 0)
      {
         for (PSUiReference node : results)
         {
            maybeRegisterListener(node.getReference());
            PSObjectType type = node.getHomeObjectType();
            if (type != null)
            {
               Collection<PSUiReference> existing = getHomeNodes(type);
               if (!existing.contains(node))
               {
                  existing.add(node);
                  ms_logger.info("Added home node: " + node.getDisplayLabel());
               }
            }
         }
         Cache c = getCache(treeName);
         c.addNodes(parent, results, true);
      }

   }

   /**
    * Convenience method that calls
    * {@link #getNodes(String[]) getNodes(new String[] &#125;path&#125;).get(0)}.
    */
   public PSUiReference getNode(String path) throws PSModelException
   {
      return getNodes(new String[] { path }).get(0);
   }

   /**
    * Gets all nodes in all trees that wrap the supplied design object. Only
    * nodes in the cache are considered.
    * 
    * @param wrappedRef Never <code>null</code>.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public Collection<PSUiReference> getNodes(IPSReference wrappedRef)
   {
      if (null == wrappedRef)
      {
         throw new IllegalArgumentException("localRef cannot be null");  
      }
      
      Collection<Cache> caches = new ArrayList<Cache>();
      for (String n : getRootNames())
      {
         caches.add(getCache(n));
      }
      
      Collection<PSUiReference> results = new ArrayList<PSUiReference>();
      for (Cache c : caches)
      {
         results.addAll(c.getNodes(wrappedRef));
      }
      
      return results;
   }

   /**
    * Returns the node(s) that match the supplied path. If the node has been
    * previously cataloged, it is returned from cache, otherwise, queries to the
    * server are made as needed.
    * 
    * @param paths Never <code>null</code>. Each entry must conform to the
    * following format:
    * 
    * <pre>
    *      [//treename]path
    *   where 
    *      //treename is an optional prefix that limits the scope of the search
    *      path is required and of the form /a/b/c
    * </pre>
    * 
    * Path parts are matched case-insensitively and may use forward or back
    * slashes for seperators.
    * 
    * @return A non-<code>null</code> list that contains either the matching
    * node, or <code>null</code> if there isn't such a path in the tree
    * currently for each supplied path.
    * 
    * @throws PSModelException If cataloging must be performed and it fails.
    */
   public List<PSUiReference> getNodes(String[] paths) throws PSModelException
   {
      List<PSUiReference> results = new ArrayList<PSUiReference>();
      for (String path : paths)
      {
         path = path.replace('\\', '/');
         int startIndex = path.startsWith("//") ? 2 : 0;
         int pos = path.indexOf("/", startIndex);
         if (pos < 0)
         {
            results.add(null);
            continue;
         }

         String treeName = null;
         String nodePath = path;
         if (startIndex == 2)
         {
            treeName = path.substring(2, pos);
            nodePath = path.substring(treeName.length() + 2);
         }
         results.add(doGetNode(treeName, nodePath));
      }
      return results;
   }

   /**
    * Walks the cache and sends it to System.out pretty-printed.
    */
   public void dumpCache()
   {
      for (String treeName : m_fragmentCache.keySet())
      {
         System.out.println("Dumping cache for tree: " + treeName);
         Cache c = m_fragmentCache.get(treeName);
         c.dumpCache();
      }
   }

   /**
    * Attempts to find a node such that node.getPath() will return the supplied
    * nodePath (case-insensitive.)
    * 
    * @param treeName Either <code>null</code> or a valid tree name.
    * 
    * @param nodePath Assumed not <code>null</code> and that all separators
    * have been normalized to "/".
    * 
    * @return A valid node if found, or <code>null</code>. Nodes within the
    * path that were not previously cataloged are automatically cataloged in an
    * attempt to find a match.
    * 
    * @throws PSModelException If any problems communicating w/ the server.
    */
   private PSUiReference doGetNode(String treeName, String nodePath)
      throws PSModelException
   {
      Collection<Cache> caches = new ArrayList<Cache>();
      if (treeName != null)
         caches.add(getCache(treeName));
      else
      {
         for (String n : getRootNames())
         {
            caches.add(getCache(n));
         }
      }

      nodePath = nodePath.toLowerCase();

      PSUiReference curNode = null;
      Cache theCache = null;
      for (Cache c : caches)
      {
         if (c.getRootNodes().isEmpty())
         {
            String rootName = treeName;
            // this will rarely happen, if ever
            if (treeName == null)
            {
               for (String name : m_fragmentCache.keySet())
               {
                  if (m_fragmentCache.get(name).equals(c))
                  {
                     rootName = name;
                     break;
                  }
               }
            }
            getChildren(rootName);
         }
         for (PSUiReference node : c.getRootNodes())
         {
            if (nodePath.startsWith(node.getPath().toLowerCase()))
            {
               theCache = c;
               curNode = node;
               break;
            }
            if (curNode != null)
               break;
         }
         if (curNode != null)
            break;
      }

      if (curNode == null)
         return null;

      StringTokenizer toker = new StringTokenizer(nodePath, "/");
      toker.nextToken(); // remove the first part, which we processed above
      while (toker.hasMoreElements() && curNode != null)
      {
         String part = toker.nextToken();
         if (part.trim().length() == 0)
         {
            // malformed path
            curNode = null;
            break;
         }
         PSUiReference[] result = new PSUiReference[1];
         boolean success = theCache.getChildNode(curNode, part, result);
         if (success)
            curNode = result[0];
         else
         {
            // if not found, attempt to catalog
            getChildren(curNode);
            success = theCache.getChildNode(curNode, part, result);
            assert (success);
            curNode = result[0];
         }
      }

      return curNode;
   }

   /**
    * Convenience method that calls {@link #getChildren(PSUiReference, boolean)
    * getChildren(parent, <code>false</code>)}.
    */
   public List<PSUiReference> getChildren(PSUiReference parent)
      throws PSModelException
   {
      return getChildren(parent, false);
   }

   
   /**
    * Catalogs the children for the supplied node.
    * 
    * @param parent A node previously returned by this method or by
    * {@link #getChildren(String)}. Never <code>null</code>.
    * 
    * @param useCache A flag to control whether the cached children should be
    * returned versus rebuilding the set. This is provided for catalogers that
    * may need to know what nodes are present to determine if a new reference
    * should be added or an existing node removed.
    * 
    * @return Never <code>null</code>, may be empty.
    * 
    * @throws PSModelException If any problems communicating with server.
    */
   public List<PSUiReference> getChildren(PSUiReference parent, boolean useCache)
      throws PSModelException
   {
      if (parent == null)
      {
         throw new IllegalArgumentException("Call getChildren(String) first.");
      }
      if (!m_connected)
      {
         return Collections.emptyList();
      }

      if (useCache && parent.isCataloged())
         return parent.getChildren(true);
      
      List<PSUiReference> results;
      try
      {
         PSHierarchyDefProcessor proc = getProcessor(parent);
         results = proc.getChildren(parent);
      }
      catch (PSHierarchyDefinitionException e)
      {
         results = new ArrayList<PSUiReference>();
         // don't i18n because it should never happen except during dev
         results.add(new PSUiReference(null,
               "<Error Occurred>: invalid definition: " + parent == null ? ""
                     : getRootKey(parent), e.getLocalizedMessage(), null,
               null, false));
         return results;
      }

      fixupPlaceholders(results);
      processCatalog(parent, getRootKey(parent), results);
      return results;
   }

   /**
    * Looks through all results for placeholder type nodes. For each one found,
    * the placeholder is replaced by the linked design object.
    * 
    * @param nodes Assumed not <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating w/ the server
    */
   private void fixupPlaceholders(List<PSUiReference> nodes)
      throws PSModelException
   {
      try
      {
         int index = 0;
         for (PSUiReference node : nodes)
         {
            PSObjectType type = node.getObjectType();
            if (type != null && type.equals(PLACEHOLDER))
            {
               IPSCmsModel userFileModel = PSCoreFactory.getInstance()
                     .getModel(PSObjectTypes.USER_FILE);
               PSHierarchyNode placeholder = (PSHierarchyNode) userFileModel
                     .load(node.getReference(), false, false);

               final String id = placeholder.getProperty(PLACEHOLDER_GUID_PROPNAME);
               final String name = placeholder.getProperty(PLACEHOLDER_NAME_PROPNAME);
               final String objectType =
                     placeholder.getProperty(PLACEHOLDER_OBJECT_TYPE_PROPNAME);

               if ((StringUtils.isBlank(id) && StringUtils.isBlank(name))
                     || StringUtils.isBlank(objectType))
               {
                  deletePlaceholder(userFileModel, node.getReference());
               }
               final IPSReference ref =
                     getObjectRefFromPlaceholderData(id, name, objectType);
               if (ref == null)
               {
                  // skip it, but don't delete it - it could be referencing a
                  // node that is not visible to this user
                  String msg = "Couldn''t find object for placeholder: {0}:{1}."
                     + " Either this is an orphan node or you cannot see the referenced object.";
                  final String identifier = id + name;
                  ms_logger.info(MessageFormat.format(msg,
                        new Object[] {objectType, identifier}));
                  continue;
               }

               InheritedProperties inheritedProps = (InheritedProperties) node
                     .getProperty(PSHierarchyDefProcessor.INHERITED_PROPS_PROPNAME);
               PSUiReference replacementNode = getProcessor(node).createNode(
                     node.getParentNode(), ref, inheritedProps, null);
               replacementNode.setProperty(PLACEHOLDER_PROPNAME, node
                     .getReference());
               nodes.set(index, replacementNode);
            }
            index++;
         }
      }
      catch (Exception e)
      {
         if (e instanceof PSModelException)
            throw (PSModelException) e;
         if (e instanceof PSClientException)
         {
            throw new PSModelException((PSClientException) e);
         }
         throw new PSModelException(e);
      }
   }

   /**
    * Attempts to permanently delete the object with the supplied handle,
    * logging the attempt. Returns whether successful or not.
    * 
    * @param userFileModel Assumed not <code>null</code>.
    * 
    * @param handle Assumed not <code>null</code>.
    */
   private void deletePlaceholder(IPSCmsModel userFileModel,
         IPSReference handle)
   {
      try
      {
         PSWorkbenchPlugin.getDefault().log(
               "Attempting to delete invalid placeholder named "
                     + handle.getName() + " with id "
                     + handle.getId().toString());
         IPSHierarchyManager mgr = userFileModel
               .getHierarchyManager(handle);
         mgr.removeChildren(Collections
               .singletonList((IPSHierarchyNodeRef) handle));
      }
      catch (Exception e)
      {
         //handle multi and model exceptions here
         if (e instanceof RuntimeException)
            throw (RuntimeException) e;
         PSWorkbenchPlugin.getDefault().log(
               "Failed to delete invalid placeholder: "
                     + e.getLocalizedMessage());
      }
   }

   /**
    * Gets the list of all known home nodes for the supplied type.
    * 
    * @return Never <code>null</code>, may be empty. The returned list may be
    * modified to add objects to the home nodes cache.
    */
   private Collection<PSUiReference> getHomeNodes(PSObjectType type)
   {
      Collection<PSUiReference> nodes = m_homeNodes.get(type);
      if (nodes == null)
      {
         nodes = new ArrayList<PSUiReference>();
         m_homeNodes.put(type, nodes);
      }
      return nodes;
   }

   /**
    * Convenience method that calls
    * {@link #getProcessor(String) getProcessor(getRootKey(node))}.
    */
   private PSHierarchyDefProcessor getProcessor(PSUiReference node)
   {
      return getProcessor(getRootKey(node));
   }

   /**
    * Get the def processor that was registered for the supplied root name.
    * 
    * @param rootName Assumed not <code>null</code> or empty.
    * 
    * @return Never <code>null</code>.
    */
   private PSHierarchyDefProcessor getProcessor(String rootName)
   {
      return m_treeParts.get(rootName.toLowerCase());
   }

   /**
    * Get the name of the root that identifies the tree fragment that contains
    * the supplied <code>node</code>. This is used as the lookup key into the
    * {@link #m_treeParts} and {@link #m_fragmentCache} maps.
    * <p>
    * This is the value stored in the {@link #ROOTNAME_PROPNAME} property.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getRootKey(PSUiReference node)
   {
      PSUiReference parent = node;
      while (parent.getParentNode() != null)
      {
         parent = parent.getParentNode();
      }
      return parent.getProperty(ROOTNAME_PROPNAME).toString();
   }

   /**
    * Get the name of the tree fragment to which the supplied <code>node</code>
    * belongs.
    * <p>
    * This is the value stored in the
    * {@link PSHierarchyDefProcessor#TREE_PROPNAME} property.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getUserFileKey(PSUiReference node)
   {
      String key = null;
      PSUiReference parent = node;
      do
      {
         Object o = parent.getProperty(PSHierarchyDefProcessor.TREE_PROPNAME);
         if (o != null)
         {
            key = o.toString();
            if (!StringUtils.isBlank(key))
               break;
         }
         parent = parent.getParentNode();
      }
      while (parent != null);
      
      if (key == null)
      {
         throw new IllegalStateException(
               "Missing TREE_PROPNAME in ancestor stack: " + node.getPath());
      }
      return key; 
   }

   /**
    * This map contains all known tree fragments. The key is the normalized root
    * name (lower-cased). The value is the cache object used to store links
    * between design object references and UI references (nodes.) Never
    * <code>null</code>, may be empty.
    */
   private Map<String, Cache> m_fragmentCache = new HashMap<String, Cache>();

   /**
    * Convenience method that calls
    * {@link #getCache(String) getCache(getRootKey(<code>node</code>))}.
    */
   private Cache getCache(PSUiReference node)
   {
      return getCache(getRootKey(node));
   }

   /**
    * 
    * @param rootName Assumed to be a valid root.
    * 
    * @return Never <code>null</code>.
    */
   private Cache getCache(String rootName)
   {
      Cache c = m_fragmentCache.get(rootName.toLowerCase());
      if (c == null)
      {
         c = new Cache();
         m_fragmentCache.put(rootName.toLowerCase(), c);
      }
      return c;
   }

   /**
    * @inheritDoc Manages the cache and notifies all listeners of deletion and
    * modification (including rename) events for flat and hierarchical models.
    * <p>
    * The {@link #addChildren(PSUiReference, IPSReference[])} method manages the
    * cache and notification for flat model creation events because it allows
    * supplying a parent node.
    */
   public void modelChanged(PSModelChangedEvent event)
   {
      boolean hierarchy = false;
      IPSHierarchyChangeListener.HierarchyChangeType type = null;
      if (event.getEventType() == PSModelChangedEvent.ModelEvents.DELETED)
         type = IPSHierarchyChangeListener.HierarchyChangeType.NODE_DELETED;
      else if ((event.getEventType() == PSModelChangedEvent.ModelEvents.MODIFIED)
            || (event.getEventType() == PSModelChangedEvent.ModelEvents.RENAMED))
      {
         type = IPSHierarchyChangeListener.HierarchyChangeType.NODE_MODIFIED;
      }
      else if ((event.getEventType() == PSModelChangedEvent.ModelEvents.CREATED))
      {
         type = IPSHierarchyChangeListener.HierarchyChangeType.NODE_CREATED;
      }
      else if ((event.getEventType() == PSModelChangedEvent.ModelEvents.SAVED))
      {
         //save any unsaved placeholders
         IPSCmsModel fileModel = getUserFileModel();
         List<IPSHierarchyNodeRef> placeholders = 
            new ArrayList<IPSHierarchyNodeRef>();
         for (IPSReference ref : event.getSource())
         {
            for (String rootName : getRootNames())
            {
               Cache c = getCache(rootName);
               Collection<PSUiReference> results = c.getNodes(ref);
               for (PSUiReference node : results)
               {
                  IPSHierarchyNodeRef placeholder = (IPSHierarchyNodeRef) node
                        .getProperty(PLACEHOLDER_PROPNAME);
                  if (placeholder != null && !placeholder.isPersisted())
                  {
                     placeholders.add(placeholder);
                  }
               }
            }
         }
         try
         {
            fileModel.save(placeholders.toArray(new IPSReference[placeholders
                  .size()]), true);
         }
         catch (PSMultiOperationException e)
         {
            handlePlaceholderException(e);
         }
         catch (PSModelException e)
         {
            handlePlaceholderException(e);
         }
         return;
      }
//      else if (event.getEventType()
//                  == PSModelChangedEvent.ModelEvents.CHILDREN_REMOVED)
//      {
//         type = IPSHierarchyChangeListener.HierarchyChangeType.NODE_DELETED;
//         hierarchy = true;
//      }
      else
         // don't care about other events
         return;

      Collection<PSPair<String, Collection<PSUiReference>>> results = 
         new ArrayList<PSPair<String, Collection<PSUiReference>>>();
      Collection<PSUiReference> allResults = new ArrayList<PSUiReference>();
      if (hierarchy)
      {
         for (IPSHierarchyNodeRef child : event.getChildren())
         {
            for (String rootName : getRootNames())
            {
               Cache c = getCache(rootName);
               Collection<PSUiReference> partialResults = c.removeNodes(child);
               if (partialResults != null && partialResults.size() > 0)
               {
                  results.add(new PSPair<String, Collection<PSUiReference>>(
                        rootName, partialResults));
                  allResults.addAll(partialResults);
               }
            }
         }
      }
      else
      {
         if (type == IPSHierarchyChangeListener.HierarchyChangeType.NODE_CREATED)
         {
            
         }
         else
         {
            for (IPSReference ref : event.getSource())
            {
               for (String rootName : getRootNames())
               {
                  Cache c = getCache(rootName);
                  Collection<PSUiReference> partialResults = c.getNodes(ref);
                  if (partialResults.size() > 0)
                  {
                     Collection<PSUiReference> modifiedNodes = null;
                     for (PSPair<String, Collection<PSUiReference>> p : results)
                     {
                        if (p.getFirst().equals(rootName))
                        {
                           modifiedNodes = p.getSecond();
                           break;
                        }
                     }
                     if (modifiedNodes == null)
                     {
                        results.add(new PSPair<String, Collection<PSUiReference>>(
                              rootName, partialResults));
                     }
                     else
                        modifiedNodes.addAll(partialResults);
                     allResults.addAll(partialResults);
      
                     // cleanup cache
                     if (type == 
                        IPSHierarchyChangeListener.HierarchyChangeType.NODE_DELETED)
                     {
                        Collection<PSUiReference> deletedNodes = c.removeNodes(ref);
                        List<IPSHierarchyNodeRef> toDelete = 
                           new ArrayList<IPSHierarchyNodeRef>();
                        for (PSUiReference node : deletedNodes)
                        {
                           IPSHierarchyNodeRef placeholder = (IPSHierarchyNodeRef) 
                                 node.getProperty(PLACEHOLDER_PROPNAME);
                           if (placeholder != null)
                              toDelete.add(placeholder);
                        }
                        if (!toDelete.isEmpty())
                        {
                           try
                           {
                              IPSReference toDeleteRef = toDelete.get(0);
                              IPSCmsModel model = PSCoreFactory.getInstance()
                                    .getModel(PSObjectTypes.USER_FILE);
                              IPSHierarchyManager mgr = model
                                    .getHierarchyManager(toDeleteRef);
                              mgr.removeChildren(toDelete);
                           }
                           catch (PSModelException e)
                           {
                              // should never happen
                              throw new RuntimeException(e);
                           }
                           catch (PSMultiOperationException e)
                           {
                              //not much we can do, just log it; they will be skipped 
                              // when processed later
                              PSWorkbenchPlugin.getDefault().log(
                                 "Failed to delete placeholders for deleted objects.",
                                 e);
                           }
                        }
                     }
                     else if (event.getEventType() 
                           == PSModelChangedEvent.ModelEvents.RENAMED)
                     {
                        
                     }
                  }
               }
            }
         }
      }
      
      if (!results.isEmpty())
      {
         // send results to each listener according to their registration
         for (PSPair<String, Collection<PSUiReference>> entry : results)
         {
            Collection<IPSHierarchyChangeListener> listeners = 
               getListeners(entry.getFirst());
            for (IPSHierarchyChangeListener l : listeners)
            {
               l.changeOccurred(type, entry.getSecond().toArray(
                     new PSUiReference[entry.getSecond().size()]), null);
            }
         }
      }
      if (!allResults.isEmpty())
      {
         for (IPSHierarchyChangeListener l : getListeners(null))
         {
            l.changeOccurred(type, allResults
                  .toArray(new PSUiReference[allResults.size()]), null);
         }
      }
   }

   /**
    * Calls the plugin to display a message to the user, sending the results
    * in the supplied exception. The message is appropriate if a placeholder
    * failed to save.
    * 
    * @param e Assumed not <code>null</code>.
    */
   private void handlePlaceholderException(PSMultiOperationException e)
   {
      PSWorkbenchPlugin.handleException(
            "Placeholder save",
            PSMessages.getString(
                  "PSWorkbenchPlugin.error.multiException.title"),
            PSMessages.getString(
                  "PSDesignObjectHierarchy.error.placeholderSaveFailed"),
            e.getResults());
   }

   /**
    * Calls the plugin to display a message to the user, sending the results
    * in the supplied exception. The message is appropriate if a placeholder
    * failed to save.
    * 
    * @param e Assumed not <code>null</code>.
    */
   private void handlePlaceholderException(PSModelException e)
   {
      PSWorkbenchPlugin.handleException(
            "Placeholder save",
            PSMessages.getString("common.operationFailed.title"),
            PSMessages.getString("common.operationFailed.message"),
            e);
   }

   /**
    * Retrieves the model for the USER_FILE object type. A small convenience
    * method.
    * 
    * @return Never <code>null</code>.
    */
   private IPSCmsModel getUserFileModel()
   {
      try
      {
         return PSCoreFactory.getInstance().getModel(PSObjectTypes.USER_FILE);
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Gets the set of listeners associated with the supplied name, creating one
    * if there isn't one (which is then added to the cache.)
    * 
    * @param rootName May be <code>null</code> (or empty, which is treated the
    * same). The name is normalized by lower-casing it.
    * 
    * @return The actual set from the cache. Any modifications to this will
    * affect the cache of listeners. Never <code>null</code>, may be empty.
    */
   private Set<IPSHierarchyChangeListener> getListeners(String rootName)
   {
      // normalize the name
      if (StringUtils.isEmpty(rootName))
         rootName = null;
      else
         rootName = rootName.toLowerCase();

      Set<IPSHierarchyChangeListener> listeners = m_uiListeners.get(rootName);
      if (listeners == null)
      {
         listeners = new HashSet<IPSHierarchyChangeListener>();
         m_uiListeners.put(rootName, listeners);
      }
      return listeners;
   }

   /**
    * Returns the names of all registered roots.
    * 
    * @return Never <code>null</code>, could be empty.
    */
   private Collection<String> getRootNames()
   {
      Collection<String> results = new ArrayList<String>();
      for (String entry : m_treeParts.keySet())
      {
         results.add(entry);
      }
      return results;
   }

   /**
    * Should be called if a node is removed that is not associated with an
    * instance of a design object. Nodes that are associated with design objects
    * will be automatically removed when the associated object is deleted.
    * 
    * @param nodes If <code>null</code> or empty, returns immediately.
    */
   public void deleteChildren(Collection<PSUiReference> nodes)
   {
      if (nodes == null || nodes.isEmpty())
         return;

      //todo (OK for release) add support for nodes in multiple trees
      for (PSUiReference node : nodes)
      {
         Cache cache = getCache(node);
         cache.removeNode(node);
         PSUiReference parent = node.getParentNode();
         if (parent != null)
         {
            parent.deleteChild(node);
         }
      }
      List<PSUiReference> orderedNodes = new ArrayList<PSUiReference>();
      orderedNodes.addAll(nodes);
      notifyListeners(
            getRootKey(nodes.iterator().next()),
            IPSHierarchyChangeListener.HierarchyChangeType.NODE_DELETED,
            orderedNodes);
      
   }
   
   //see IPSDesignObjectChangeListener
   public List<PSUiReference> addChildren(PSUiReference suppliedParent,
         IPSReference[] suppliedChildren)
   {
      return addChildren(suppliedParent, suppliedChildren, true);
   }

   /**
    * Convenience method that calls
    * {@link #addChildren(PSUiReference, IPSReference[], boolean, boolean) 
    * addChildren(suppliedParent, suppliedChildren, validatePath, true)}.
    */
   public List<PSUiReference> addChildren(PSUiReference suppliedParent,
         IPSReference[] suppliedChildren, boolean validatePath)
   {
      return addChildren(suppliedParent, suppliedChildren, validatePath, true);
   }

   /**
    * Version of {@link #addChildren(PSUiReference, IPSReference[])} that
    * optionally allows you to skip child path validation - used when loading
    * children where you know the path is valid and are adding references to
    * objects whose instances are not valid for the current context. Only the 
    * additional flag parameter is documented.
    * 
    * @param validatePath <code>true</code> to validate that the children are
    * valid for the supplied parent, <code>false</code> to skip it.
    * 
    * @param instance A flag the determines whether the generated nodes are
    * instances or refs, as identified by the {@link PSUiReference#isReference()}
    * method.
    */
   public List<PSUiReference> addChildren(PSUiReference suppliedParent,
         IPSReference[] suppliedChildren, boolean validatePath, boolean instance)
   {
      if (suppliedChildren.length == 0)
         return Collections.emptyList();
      PSUiUtils.isValidArray(suppliedChildren, true);
      
      Map<PSUiReference, Collection<IPSReference>> sortedChildren = 
         new HashMap<PSUiReference, Collection<IPSReference>>();

      if (suppliedParent != null && validatePath)
      {
         //check if it is the correct type
         IPSDeclarativeNodeHandler handler = suppliedParent.getHandler();
         if (handler == null)
            suppliedParent = null;
         else
         {
            for (IPSReference ref : suppliedChildren)
            {
               if (!handler.isAcceptedType(ref))
               {
                  suppliedParent = null;
                  break;
               }
            }
         }
      }
      
      if (suppliedParent == null)
      {
         PSObjectType type = suppliedChildren[0].getObjectType();
         for (IPSReference ref : suppliedChildren)
         {
            Collection<PSUiReference> homeNodes = getHomeNodes(type);
            for (PSUiReference uiRef : homeNodes)
            {
               if (uiRef.isHomeNode(ref))
               {
                  Collection<IPSReference> refs = sortedChildren.get(uiRef);
                  if (refs == null)
                  {
                     refs = new ArrayList<IPSReference>();
                     sortedChildren.put(uiRef, refs);
                  }
                  refs.add(ref);
               }
            }
         }
      }
      else
      {
         sortedChildren.put(suppliedParent, Arrays.asList(suppliedChildren));
      }

      List<PSUiReference> results = new ArrayList<PSUiReference>();
      for (Map.Entry<PSUiReference, Collection<IPSReference>> entry 
            : sortedChildren.entrySet())
      {
         PSUiReference parent = entry.getKey();
         Collection<IPSReference> children = entry.getValue();
         IPSCatalog catalog = parent.getCatalogFactory().createCatalog(parent);
         for (IPSReference ref : children)
         {
            PSUiReference node = catalog.createEntry(ref);
            node.setReference(!instance);
            results.add(node);
            maybeRegisterListener(ref);
         }

         if (!results.isEmpty())
         {
            if (parent.getObjectType() != null 
                  && parent.getObjectType().getPrimaryType().equals(
                        PSObjectTypes.USER_FILE))
            {
               List<PSUiReference> nonFolders = 
                  new ArrayList<PSUiReference>();
               for (PSUiReference node : results)
               {
                  if (!node.getObjectType().equals(WORKBENCH_FOLDER))
                     nonFolders.add(node);
               }
               try
               {
                  createPlaceholders(parent, nonFolders);
               }
               catch (PSMultiOperationException e)
               {
                  handlePlaceholderException(e);
               }
            }

            Cache c = getCache(parent);
            try
            {
               c.addNodes(parent, results, false);
               parent.addChildren(results);
               notifyListeners(
                     getRootKey(parent),
                     IPSHierarchyChangeListener.HierarchyChangeType.NODE_CREATED,
                     results);
            }
            catch (IllegalStateException e)
            {
               /*
                * ignore, this means the parent to which they wanted to add
                * children does not exist, so clear the results
                */
               results.clear();
            }
         }
      }

      if (ms_logger.isDebugEnabled())
      {
         String pattern = "Added {0} children to {1}.";
         ms_logger.debug(MessageFormat.format(pattern, results.size(), 
            (suppliedParent == null ? "no parent" : suppliedParent.getPath())));
      }
      return results;
   }

   /**
    * Convenience method that calls
    * {@link #notifyListeners(String, 
    * IPSHierarchyChangeListener.HierarchyChangeType, List, List)
    * notifyListeners(treeName, type, affectedNodes, null)}.
    */
   private void notifyListeners(String treeName,
         IPSHierarchyChangeListener.HierarchyChangeType type,
         List<PSUiReference> affectedNodes)
   {
      assert(type != HierarchyChangeType.NODE_MOVED);
      notifyListeners(treeName, type, affectedNodes, null);
   }
   
   /**
    * Sends a message to certain registered listeners as filtered by the
    * supplied <code>treeName</code>.
    * 
    * @param treeName The tree within which the events occurred. It is used to
    * notify listeners interested in specific events.
    * 
    * @param type The message to send.
    * 
    * @param affectedNodes The nodes that had children added or removed. If
    * <code>null</code> or empty, returns immediately w/o sending any
    * notifications.
    * 
    * @param sourceParents The parent of the affected nodes before the move
    * occurred. Should be <code>null</code> unless the type is
    * {@link HierarchyChangeType#NODE_MOVED}, in which case it should not be
    * <code>null</code>. <code>null</code> entries are ok.
    */
   private void notifyListeners(String treeName,
         IPSHierarchyChangeListener.HierarchyChangeType type,
         List<PSUiReference> affectedNodes, List<PSUiReference> sourceParents)
   {
      if (affectedNodes == null || affectedNodes.isEmpty())
         return;

      for (IPSHierarchyChangeListener l : getListeners(treeName))
      {
         PSUiReference[] sourceParentsArray = sourceParents == null ? null :
            sourceParents.toArray(new PSUiReference[sourceParents.size()]);
         l.changeOccurred(type, 
               affectedNodes.toArray(new PSUiReference[affectedNodes.size()]), 
               sourceParentsArray);
      }

      for (IPSHierarchyChangeListener l : getListeners(null))
      {
         PSUiReference[] sourceParentsArray = sourceParents == null ? null :
            sourceParents.toArray(new PSUiReference[sourceParents.size()]);
         l.changeOccurred(type, 
               affectedNodes.toArray(new PSUiReference[affectedNodes.size()]),
               sourceParentsArray);
      }
   }

//   /**
//    * Creates a node, but does not add it to the cache.
//    * 
//    * @param parent The node that will become the parent of the new node. Never
//    * <code>null</code>.
//    * 
//    * @param ref Never <code>null</code>.
//    * 
//    * @param inheritedProps Properties aggregated from all ancestors. May be
//    * <code>null</code>.
//    * 
//    * @return The newly created wrapper, never <code>null</code>.
//    * 
//    * @throws PSHierarchyDefinitionException If there is no InstanceTree 
//    * definition for the supplied object type or it is malformed.
//    */
//   public PSUiReference createNode(PSUiReference parent, IPSReference ref,
//         InheritedProperties inheritedProps)
//      throws PSHierarchyDefinitionException
//   {
//      if (null == parent)
//      {
//         throw new IllegalArgumentException("parent cannot be null");  
//      }
//      if (null == ref)
//      {
//         throw new IllegalArgumentException("ref cannot be null");  
//      }
//      
//      PSHierarchyDefProcessor proc = getProcessor(parent);
//      return proc.createNode(parent, ref, inheritedProps, null);
//   }

   /**
    * Checks if the supplied node is part of a hierarchical model other than
    * USER_FILE.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the node has a file sub-type that is folder,
    * <code>false</code> otherwise. 
    */
   public static boolean isHierarchyModel(PSUiReference node)
   {
      PSObjectType type = node.getObjectType();
      if (type != null && type.getSecondaryType() != null)
      {
         if (type.getSecondaryType().equals(PSObjectTypes.FileSubTypes.FOLDER))
            return true;
      }
      return false;
   }
   
   /**
    * Checks if the supplied node accepts USER_FILE folders as children.
    * 
    * @param node Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if this node accepts USER_FILE type folders,
    * <code>false</code> otherwise.
    */
   public static boolean isUserFileHierarchy(PSUiReference node)
   {
      IPSDeclarativeNodeHandler handler = node.getHandler();
      if (handler == null)
         return false;
      
      for (Transfer t : handler.getAcceptedTransfers())
      {
         if ((t instanceof PSReferenceTransfer) 
               && ((PSReferenceTransfer)t).getPrimaryType().equals(
                     PSObjectTypes.USER_FILE))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Creates a new folder as a child of <code>parent</code> using a default
    * name. The name is of the form 'New Folder (n)', where (n) is only included
    * if needed to make the name unique and n is a number starting at 1.
    *  
    * @param parent Never <code>null</code>.
    * @param name If not <code>null</code> or empty, this name is used for the
    * new folder instead of what was described above.
    * 
    * @return The new folder node. Never <code>null</code>.
    * 
    * @throws Exception If any problems communicating w/ the server or creating
    * the folder.
    */
   public PSUiReference createFolder(PSUiReference parent, String name) 
      throws Exception
   {
      if (null == parent)
      {
         throw new IllegalArgumentException("parent cannot be null");  
      }
      IPSCmsModel model;
      IPSHierarchyManager mgr;
      PSObjectType newType;
      if (isUserFileHierarchy(parent))
      {
         model = PSCoreFactory.getInstance().getModel(
               WORKBENCH_FOLDER.getPrimaryType());
         mgr = model.getHierarchyManager(getUserFileKey(parent));
         newType = WORKBENCH_FOLDER;
      }
      else
      {
         mgr = getHierarchyManager(parent);
         newType = parent.getObjectType();
      }

      return createFileOrFolder(parent, newType, name, mgr);
   }

   /**
    * Finds the correct manager used to manage the tree to which the reference
    * in the supplied node belongs.
    * 
    * @param node Assumed not <code>null</code>.
    * @return Never <code>null</code>.
    */
   private IPSHierarchyManager getHierarchyManager(PSUiReference node)
   {
      IPSCmsModel model;
      IPSHierarchyManager mgr;
      assert(isHierarchyModel(node));
      IPSHierarchyNodeRef targetRef = 
         (IPSHierarchyNodeRef) node.getReference();
      if (targetRef == null)
      {
         //pasting to root - not supported for all models
         String treeName = getTreeName(node);
         model = PSCoreFactory.getInstance().getHierarchyModel(treeName);
         mgr = model.getHierarchyManager(treeName);
      }
      else
      {
         model = targetRef.getParentModel();
         mgr = model.getHierarchyManager(targetRef);
      }
      return mgr;
   }
   
   /**
    * Add a new file type object as a child of the supplied parent. The primary
    * type of the child will match the parent and the secondary type will be
    * {@link PSObjectTypes.FileSubTypes#FILE}. The file is not persisted, it
    * only exists in memory until it is saved using the model. 
    * 
    * @param parent Never <code>null</code>.
    * 
    * @param name Never <code>null</code> or empty. If it matches an existing
    * name, a {@link PSDuplicateNameException} is thrown.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws Exception If an object by the same name already exists under this
    * parent (regardless of type), a PSDuplicateNameException is thrown. If any
    * server problems, an exception is thrown.
    */
   public PSUiReference createFile(PSUiReference parent, String name)
      throws Exception
   {
      PSObjectType type = PSObjectTypeFactory
      .getType((IPSPrimaryObjectType) parent.getObjectType()
            .getPrimaryType(), PSObjectTypes.FileSubTypes.FILE);

      return createFileOrFolder(parent, type, name, getHierarchyManager(parent));      
   }
   
   /**
    * Performs the work of creating a file or folder in a hierarchical model.
    * 
    * @param parent The parent of the generated object. Assumed not
    * <code>null</code>.
    * @param type The type of the generated object. Assumed not
    * <code>null</code>.
    * @param name The name of the generated object. Never <code>null</code> or
    * empty.
    * @return Never <code>null</code>.
    * 
    * @throws Exception If an object by the same name already exists under this
    * parent (regardless of type), a PSDuplicateNameException is thrown. If any
    * server problems, an exception is thrown.
    */
   private PSUiReference createFileOrFolder(PSUiReference parent,
         PSObjectType type, String name, IPSHierarchyManager mgr)
      throws Exception
   {
      List<PSUiReference> results = null;
      
      boolean done = false;
      int suffix = 0;
      IPSHierarchyNodeRef parentRef = (IPSHierarchyNodeRef) parent
            .getReference();
      List<String> names = new ArrayList<String>();
      Collection<IPSHierarchyNodeRef> curChildren = mgr.getChildren(parentRef);
      while (!done)
      {
         try
         {
            names.clear();
            if (StringUtils.isBlank(name))
               names.add(createUniqueFolderName(curChildren, suffix));
            else
               names.add(name);
            results = addChildren(parent, mgr
                  .createChildren(parentRef, type, names));
            done = true;
         }
         catch (PSMultiOperationException e)
         {
            if (e.getResults()[0] instanceof PSDuplicateNameException
                  && StringUtils.isBlank(name))
            {
               suffix++;
            }
            else
            {
               throw (Exception) e.getResults()[0];
            }
         }
      }
      return results.get(0);
   }

   /**
    * Find a name that does not exist within the supplied set. The name will be
    * of the form:
    * <ol>
    * <li>New Folder</li>
    * <li>New Folder (n)</li>
    * <ol>
    * Where n is a number starting at 1 to make a unique name.
    * 
    * @param existing Assumed not <code>null</code>.

    * @param count The first time in should be 0.
    * 
    * @return A name that is unique among all the supplied names,
    * case-insensitive.
    */
   private String createUniqueFolderName(
         Collection<IPSHierarchyNodeRef> existing, int count)
   {
      String name = "";
      if (count == 0)
         name = "New Folder";
      else if (count > 0)
         name = "New Folder (" + count + ")";
      
      for (IPSHierarchyNodeRef childRef : existing)
      {
         if (childRef.getName().equalsIgnoreCase(name))
         {
            name = createUniqueFolderName(existing, count+1);
            break;
         }
      }
      return name;
   }
   
   
   /**
    * The supplied nodes will be of folder type or leaf type. If folder, they
    * may be a USER_FILE:WORKBENCH_FOLDER or a folder type in a hierarchical
    * model.
    * <p>
    * The target is either a folder that matches the folder types in the
    * <code>nodes</code> set, or a home node for the leaves.
    * <p>
    * If the nodes are already children of <code>target</code>, they are
    * skipped. 
    * <p>
    * Cross-tree moves are not supported. The caller must do this herself.
    * 
    * @param target Never <code>null</code>.
    * 
    * @param nodes The nodes to be moved. If <code>null</code> or empty,
    * returns immediately. It is a <code>List</code> in case errors occur. The
    * exception may contain partial success. No <code>null</code> entries
    * allowed.
    * 
    * @throws PSModelException Any problems that affect the entire operation and
    * don't allow individual nodes to succeed.
    * 
    * @throws PSMultiOperationException If problems w/ individual nodes. The
    * results array will contain either <code>null</code> for successfully
    * moved nodes, or an exception in the corresponding location.
    */
   public void move(PSUiReference target, List<PSUiReference> nodes)
      throws PSModelException, PSMultiOperationException
   {
      if (nodes == null || nodes.size() == 0)
         return;

      PSUiUtils.isValidCollection(nodes, true);
      
      if (null == target)
      {
         throw new IllegalArgumentException("target cannot be null");
      }

      // first, we need to figure out what we're dealing with
      boolean isUserFileType = false;
      boolean isToUserFile = false;
      if (target.getReference() != null)
      {
         if (target.getObjectType().equals(WORKBENCH_FOLDER))
         {
            isUserFileType = true;
            isToUserFile = true;
         }
      }
      else
      {
         // check all children
         for (PSUiReference node : nodes)
         {
            if (node.getObjectType().equals(WORKBENCH_FOLDER)
                  || ((node.getReference() != null) 
                  && !(node.getReference() instanceof IPSHierarchyNodeRef)))
            {
               isUserFileType = true;
               break;
            }
         }
      }

      // check if moving to self
      for (PSUiReference node : nodes)
      {
         if (node.getParentNode().equals(target))
         {
            ms_logger.debug("Moved to self - skipping");
            return;
         }
      }

      String sourceTreeName = null;
      PSUiReference sourceParent = null;
      for (PSUiReference node : nodes)
      {
         // todo (OK for release) don't assume that all children in same tree &&
         // have same parent
         if (sourceTreeName == null)
         {
            sourceParent = node.getParentNode();
            sourceTreeName = getRootKey(node);
         }
         else
         {
            if (!sourceTreeName.equals(getRootKey(node))
                  || !sourceParent.equals(node.getParentNode()))
            {
               throw new UnsupportedOperationException(
                     "A selection set with nodes from multiple parents is not supported.");
            }
         }
      }
      PSMultiOperationException ex = null;
      List<PSUiReference> successfulNodes = null;
      if (isUserFileType)
      {
         // either the children or the target or both are USER_FILE based
         List<IPSHierarchyNodeRef> toMoveRefs = 
            new ArrayList<IPSHierarchyNodeRef>();
         List<PSUiReference> toClearNodes = new ArrayList<PSUiReference>();
         List<PSUiReference> toRemove = new ArrayList<PSUiReference>();
         List<PSUiReference> toAdd = new ArrayList<PSUiReference>();
         for (PSUiReference node : nodes)
         {
            if (node.isFolder())
            {
               toMoveRefs.add((IPSHierarchyNodeRef) node.getReference());
            }
            else
            {
               IPSHierarchyNodeRef placeholderRef = (IPSHierarchyNodeRef) node
                     .getProperty(PLACEHOLDER_PROPNAME);
               if (isToUserFile)
               {
                  if (placeholderRef != null)
                  {
                     toMoveRefs.add(placeholderRef);
                     toClearNodes.add(node);
                  }
                  else
                     toAdd.add(node);
               }
               else
               {
                  if (placeholderRef != null)
                     toRemove.add(node);
                  else
                  {
                     toMoveRefs.add(placeholderRef);
                     toClearNodes.add(node);
                  }
               }
            }
         }

         IPSCmsModel fileModel = null;
         IPSHierarchyManager mgr = null;
         IPSHierarchyNodeRef targetRef = null;
         if (target.getReference() != null)
         {
            targetRef = (IPSHierarchyNodeRef) target.getReference();
            mgr = targetRef.getManager();
            fileModel = targetRef.getParentModel();
         }
         else if (!toRemove.isEmpty())
         {
            IPSHierarchyNodeRef placeholderRef = ((IPSHierarchyNodeRef) toRemove
                  .get(0).getProperty(PLACEHOLDER_PROPNAME));
            mgr = placeholderRef.getManager();
            fileModel = placeholderRef.getParentModel();
         }
         else if (!toMoveRefs.isEmpty())
         {
            mgr = toMoveRefs.get(0).getManager();
            fileModel = toMoveRefs.get(0).getParentModel();
         }
         else
         {
            IPSReference ref = toAdd.get(0).getReference();
            fileModel = PSCoreFactory.getInstance().getModel(ref);
            mgr = fileModel.getHierarchyManager(ref);
         }

         mgr.moveChildren(toMoveRefs, targetRef);
         Cache c = getCache(sourceTreeName);
         for (PSUiReference node : toClearNodes)
         {
            c.removeNode(node);
         }
         
         if (toAdd.size() > 0)
         {
            //some objects moved from home node to user-file folder
            createPlaceholders(target, toAdd);
            for (PSUiReference node : toAdd)
            {
               c.removeNode(node);
            }
         }

         if (toRemove.size() > 0)
         {
            //some objects moved from user-file folder to home node
            List<IPSHierarchyNodeRef> placeholderRefs = 
               new ArrayList<IPSHierarchyNodeRef>();
            for (PSUiReference node : toRemove)
            {
               placeholderRefs.add((IPSHierarchyNodeRef) node
                     .getProperty(PLACEHOLDER_PROPNAME));
               node.removeProperty(PLACEHOLDER_PROPNAME);
               c.removeNode(node);
            }
            mgr.removeChildren(placeholderRefs);
         }
         
         for (PSUiReference node : nodes)
         {
            node.setParentNode(target);
         }
      }
      else
      {
         // hierarchy model move
         IPSReference targetRef = target.getReference();
         IPSCmsModel targetModel;
         if (targetRef != null)
            targetModel = PSCoreFactory.getInstance().getModel(targetRef);
         else
            targetModel = PSCoreFactory.getInstance().getModel(sourceTreeName);
   
         IPSHierarchyManager mgr = targetModel.getHierarchyManager(targetRef);
         if (mgr == null)
         {
            throw new UnsupportedOperationException(
                  "Target must have a hierarchy manager: " + target.getPath());
         }
         
         // validate: all children must have a mgr
         for (PSUiReference node : nodes)
         {
            if (node.getReference() == null)
            {
               throw new UnsupportedOperationException(
                     "Non-design-object nodes can't be moved.");
            }
            IPSReference ref = node.getReference();
            IPSCmsModel model = ref.getParentModel();
            if (!model.isHierarchyModel())
            {
               throw new UnsupportedOperationException(
                  "Can't move mixture of hierarchy and non-hierarchy based objects.");
            }
         }
         
         // todo (OK for release) cross tree support?
         // use mgr to move the objects
         List<IPSHierarchyNodeRef> childRefs = 
            new ArrayList<IPSHierarchyNodeRef>();
         for (PSUiReference node : nodes)
         {
            childRefs.add((IPSHierarchyNodeRef) node.getReference());
         }
         
         try
         {
            mgr.moveChildren(childRefs, (IPSHierarchyNodeRef) targetRef);
         }
         catch (PSMultiOperationException e)
         {
            ex = e;
         }
         
         successfulNodes = getSuccessfulNodes(ex, nodes);
         
         for (PSUiReference node : successfulNodes)
         {
            node.setParentNode(target);
         }         
      }
      
      if (successfulNodes == null)
         successfulNodes = nodes;
      // the cache and notification for moves are managed by modelChanged()
      getCache(sourceTreeName).moveNodes(sourceParent, successfulNodes, target);
      List<PSUiReference> sourceParents = new ArrayList<PSUiReference>();
      for (int i = 0; i < successfulNodes.size(); i++)
         sourceParents.add(sourceParent);
      notifyListeners(sourceTreeName,
         IPSHierarchyChangeListener.HierarchyChangeType.NODE_MOVED,
         successfulNodes, sourceParents);
      if (ex != null)
      {
         throw ex;
      }
      return;
   }

   /**
    * If the exception is not <code>null</code>, then the valid nodes in its
    * results array are returned, otherwise the <code>defaultNodes</code> is
    * returned directly.
    * 
    * @param defaultNodes No assumptions made.
    * 
    * @param ex May be <code>null</code>.
    * 
    * @return If ex is not <code>null</code>, then never <code>null</code>,
    * otherwise <code>defaultNodes</code>.
    */
   private List<PSUiReference> getSuccessfulNodes(
         PSMultiOperationException ex, List<PSUiReference> defaultNodes)
   {
      if (ex == null)
         return defaultNodes;
      
      List<PSUiReference> results = new ArrayList<PSUiReference>();
      Object[] exResults = ex.getResults(); 
      for (int i = 0; i < exResults.length; i++)
      {
         if (!(exResults[i] instanceof Throwable))
            results.add(defaultNodes.get(i));
      }
      return results;
   }
   
   /**
    * Saves reference data to the placeholder.
    */
   private void initPlaceholderFromRef(PSHierarchyNode placeholder, final IPSReference ref)
   {
      if (ref.getId() == null)
      {
         placeholder.addProperty(PLACEHOLDER_NAME_PROPNAME,
               String.valueOf(ref.getName()));
      }
      else
      {
         placeholder.addProperty(PLACEHOLDER_GUID_PROPNAME,
               ref.getId().toString());
      }
      placeholder.addProperty(PLACEHOLDER_OBJECT_TYPE_PROPNAME,
            ref.getObjectType().toSerial());
   }

   /**
    * Builds the placeholders and sets them as a property of the supplied 
    * object nodes. The placeholder is persisted if the corresponding object
    * is persisted.
    * 
    * @param parent The parent of the object nodes. Assumed not <code>null</code>.
    * 
    * @param objectNodes The design objects that are being added to a USER_FILE
    * folder. Assumed that only design objects are in this list.
    * 
    * @throws PSMultiOperationException If any problems creating/saving the
    * placeholders.
    */
   private void createPlaceholders(PSUiReference parent, 
         List<PSUiReference> objectNodes)
      throws PSMultiOperationException
   {
      try
      {
         List<String> names = new ArrayList<String>();
         for (PSUiReference node : objectNodes)
         {
            names.add(node.getDisplayLabel().replaceAll(
                  "[|\\\\/:?\\*\"><!-]", "_"));
         }
         IPSCmsModel fileModel = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.USER_FILE);
         IPSHierarchyManager mgr = fileModel.getHierarchyManager(parent
               .getReference());
         IPSHierarchyNodeRef[] children = mgr.createChildren(
               (IPSHierarchyNodeRef) parent.getReference(), PLACEHOLDER, names);

         Object[] placeholders = fileModel.load(children, true, false);

         int i = 0;
         List<IPSHierarchyNodeRef> persistedOnes = 
            new ArrayList<IPSHierarchyNodeRef>();
         for (Object o : placeholders)
         {
            PSHierarchyNode placeholder = (PSHierarchyNode) o;
            IPSReference linkedRef = objectNodes.get(i).getReference();
            assert !linkedRef.getObjectType().getPrimaryType()
                  .equals(PSObjectTypes.USER_FILE);
            initPlaceholderFromRef(placeholder, linkedRef);
            objectNodes.get(i).setProperty(PLACEHOLDER_PROPNAME, children[i]);
            if (objectNodes.get(i).getReference().isPersisted())
               persistedOnes.add(children[i]);
            i++;
         }
         
         fileModel.save(persistedOnes
               .toArray(new IPSHierarchyNodeRef[persistedOnes.size()]), true);
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }

   /**
    * For each child of the supplied node, a new catalog is performed. The new
    * data is compared against existing children and properties and updated as
    * needed. This process is done recursively. New children are added as if
    * they had been created locally and children that are no longer in the model
    * are removed as if the user had removed them.
    * 
    * @param node Never <code>null</code>.
    */
   public void refresh(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");
      }

      node.refresh();
      getCache(node).flush(node);
      flushHomeNodeCache(node.getPath());
      notifyListeners(
            getRootKey(node),
            IPSHierarchyChangeListener.HierarchyChangeType.NODE_CHILDREN_MODIFIED,
            Collections.singletonList(node));
   }

   /**
    * Processes all current home nodes. Any whose path is a descendent of the  
    * supplied path are removed from the cache.
    * 
    * @param path Assumed never <code>null</code> or empty.
    */
   private void flushHomeNodeCache(String path)
   {
      assert StringUtils.isNotBlank(path);
      for (Collection<PSUiReference> nodes : m_homeNodes.values())
      {
         Iterator<PSUiReference> nodeIter = nodes.iterator();
         while (nodeIter.hasNext())
         {
            String homePath = nodeIter.next().getPath();
            if (homePath.startsWith(path) && homePath.length() > path.length())
               nodeIter.remove();
         }
      }
   }

   /**
    * Scans the tree for all non-folder entries. All known entries are
    * returned, whether they have been cataloged during this session or not.
    * They are not processed thru the cache. The server is always queried.
    * 
    * @param treeName Never <code>null</code> or empty. Must be one of the
    * values returned by {@link #getRootNames()}.
    * 
    * @return References for all non-folder objects in the requested tree,
    * never <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    */
   public Collection<IPSReference> getDescendentRefs(String treeName)
      throws PSModelException
   {
      IPSUserFileModel folderModel = (IPSUserFileModel) PSCoreFactory
            .getInstance().getModel(PSObjectTypes.USER_FILE);
      Collection<PSHierarchyNode> placeholders = folderModel
            .getDescendentPlaceholders(treeName);

      Collection<IPSReference> results = new ArrayList<IPSReference>(); 
      for (PSHierarchyNode placeholderData : placeholders)
      {
         final String id = placeholderData.getProperty(PLACEHOLDER_GUID_PROPNAME);
         final String name = placeholderData.getProperty(PLACEHOLDER_NAME_PROPNAME);
         final String objectType = placeholderData
               .getProperty(PLACEHOLDER_OBJECT_TYPE_PROPNAME);
         if ((StringUtils.isBlank(id) && StringUtils.isBlank(name))
               || StringUtils.isBlank(objectType))
         {
            // ignore invalid placeholders
            continue;
         }
         
         final IPSReference ref =
               getObjectRefFromPlaceholderData(id, name, objectType);
         if (ref == null)
         {
            /* skip it, it could be referencing a node that is not visible to 
             * this user
             */ 
            continue;
         }
         results.add(ref);
      }
      return results;
   }

   /**
    * Gets the tree to which the supplied node is a member.
    * 
    * @param node Never <code>null</code>.
    * 
    * @return The name of the encompassing tree. Never <code>null</code>.
    */
   public String getTreeName(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      return getRootKey(node);
   }

   /**
    * Should be called when the model is no longer needed. Removes self as
    * listener from various classes.
    */
   public void dispose()
   {
      PSModelTracker.getInstance().removeListener(this);
      for (PSObjectTypes type : m_registeredTypes)
      {
         try
         {
            PSCoreFactory.getInstance().getModel(type).removeListener(this);
         }
         catch (PSModelException e)
         {
            // should not happen since we already retrieved it once
            throw new RuntimeException("Model not found");
         }
      }
   }

   /**
    * Checks if a model listener has been added for the type of the supplied
    * reference. If it has, nothing is done. Otherwise, a listener is added for
    * delete and modify events.
    * 
    * @param ref If <code>null</code>, returns immediately.
    */
   private void maybeRegisterListener(IPSReference ref)
   {
      if (null == ref)
         return;
      if (ref.getObjectType() != null
            && !m_registeredTypes.contains(ref.getObjectType()
                  .getPrimaryType()))
      {
         ref.getParentModel().addListener(
               this,
               PSModelChangedEvent.ModelEvents.DELETED.getFlag()
                     | PSModelChangedEvent.ModelEvents.MODIFIED.getFlag()
                     | PSModelChangedEvent.ModelEvents.RENAMED.getFlag()
                     | PSModelChangedEvent.ModelEvents.CHILDREN_ADDED
                           .getFlag()
                     | PSModelChangedEvent.ModelEvents.CHILDREN_REMOVED
                           .getFlag()
                     | PSModelChangedEvent.ModelEvents.CREATED.getFlag()
                     | PSModelChangedEvent.ModelEvents.SAVED.getFlag());
         m_registeredTypes.add((PSObjectTypes) ref.getObjectType()
               .getPrimaryType());
      }
   }
   
   /**
    * This is provided for unit testing only. Not for any other use.
    */
   synchronized static void resetInstance()
   {
      ms_instance = null;
   }

   /**
    * Performs all steps necessary when logging into a server.
    */
   private void loginInit()
   {
      try
      {
         if (m_connected)
            return;
         
         PSCoreFactory.getInstance().
            getModel(PSObjectTypes.USER_FILE).addListener(
            PSDesignObjectHierarchy.this,
            PSModelChangedEvent.ModelEvents.DELETED.getFlag()
               | PSModelChangedEvent.ModelEvents.MODIFIED.getFlag()
               | PSModelChangedEvent.ModelEvents.RENAMED.getFlag()
               | PSModelChangedEvent.ModelEvents.CHILDREN_ADDED
                     .getFlag()
               | PSModelChangedEvent.ModelEvents.CHILDREN_REMOVED
                     .getFlag()
               | PSModelChangedEvent.ModelEvents.CREATED.getFlag()
               | PSModelChangedEvent.ModelEvents.SAVED.getFlag());
         m_registeredTypes.add(PSObjectTypes.USER_FILE);
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }

      m_connected = true;
      //tell all listeners
      for (Set<IPSHierarchyChangeListener> listeners : m_uiListeners
            .values())
      {
         for (IPSHierarchyChangeListener listener : listeners)
         {
            listener.changeOccurred(
               IPSHierarchyChangeListener.HierarchyChangeType.MODEL_ACTIVATED,
               null, null);
         }
      }
   }
   
   /**
    * Private to implement singleton pattern. Loads all known tree fragment
    * definition files and registers this instance as a listener on the
    * PSModelTracker and with the USER_FILE model.
    */
   private PSDesignObjectHierarchy()
   {
      //register for logon/logoff events
      PSCoreFactory.getInstance().addListener(new IPSCoreListener()
      {
         //see interface
         public void logonStateChanged(PSLogonStateChangedEvent event)
         {
            if (event.getEventType() 
                  == PSLogonStateChangedEvent.LogonStateEvents.LOGOFF)
            {
               ms_logger.debug("Processing logoff event.");
               if (!m_connected)
                  return;
               m_connected = false;
               
               PSCoreFactory factory = PSCoreFactory.getInstance();
               for (PSObjectTypes type : m_registeredTypes)
               {
                  try
                  {
                     IPSCmsModel model = factory.getModel(type);
                     model.removeListener(PSDesignObjectHierarchy.this);
                  }
                  catch (PSModelException e)
                  {
                     //ignore, nothing we can do and we're disconnecting
                  }
               }
               
               final Display display =
                    PSWorkbenchPlugin.getDefault().getWorkbench().getDisplay();
               display.asyncExec(new Runnable()
               {
                  public void run()
                  {
                     for (Set<IPSHierarchyChangeListener> listeners :
                            m_uiListeners.values())
                     {
                        for (IPSHierarchyChangeListener listener : listeners)
                        {
                           listener.changeOccurred(
                                 MODEL_DEACTIVATED, null, null);
                        }
                     }
                     m_registeredTypes.clear();
                     m_fragmentCache.clear();
                  }
               });
            }
            else
            {
               assert (event.getEventType() 
                     == PSLogonStateChangedEvent.LogonStateEvents.LOGON);
               ms_logger.debug("Processing logon event.");
               loginInit();
            }
         }
      });

      //did a login occur before we were registered?
      if (PSCoreFactory.getInstance().getConnectionInfo() != null
            && !m_connected)
      {
         loginInit();
      }
      
      PSModelTracker.getInstance().addListener(this);
      // todo (OK for release)
      //   make more flexible, let processor create root node
      // [name][description][def resource name]
      String[][] defs = {
            { "Assembly", "", "assembly_viewHierarchyDef.xml" },
            { "Content", "", "content_viewHierarchyDef.xml" },
            { "Community", "Community Visibility", 
               "community_viewHierarchyDef.xml" },
            { "DatabaseExplorer", "Database Explorer",
                  "databaseExplorer_viewHierarchyDef.xml" }, 
            { "LocalFileExplorer", "Local File System Explorer",
                  "localFileExplorer_viewHierarchyDef.xml" },
            { "Security", "", "security_viewHierarchyDef.xml" },
            { "UIElements", "", "uielements_viewHierarchyDef.xml" },
            { "System", "", "system_viewHierarchyDef.xml" }, 
            { "XmlServer", "", "xmlserver_viewHierarchyDef.xml" }, 
            { "widget-content", "", "widget_content_viewHierarchyDef.xml" },
            { "widget-system", "", "widget_system_viewHierarchyDef.xml" }, 
            { "widget-xmlserver", "", "widget_xmlserver_viewHierarchyDef.xml" }, 
      };

      for (String[] def : defs)
      {
         try
         {
            Reader reader = new InputStreamReader(PSAssemblyDesignView.class
                  .getResourceAsStream(def[2]), "utf-8");
            m_treeParts.put(def[0].toLowerCase(), new PSHierarchyDefProcessor(
                  def[0], reader));
         }
         catch (UnsupportedEncodingException e)
         {
            // should never happen as java supports utf-8 by default
            throw new RuntimeException(e);
         }
         catch (IOException e)
         {
            String msg = PSMessages.getString(
                  "PSDesignObjectHierarchy.error.init.loadDefinitionFailed", 
                  (Object[]) new String[] {def[2]});
            String title = 
               PSMessages.getString("PSDesignObjectHierarchy.error.title");
            PSWorkbenchPlugin.handleException("PSDesignObjectHierarchy ctor",
                  title, msg, e);
         }
      }
   }

   /**
    * Constructs reference to the object placeholder stands for from
    * placeholder data.
    *
    * @param id object id. Can be <code>null</code> or empty if name is specified.
    * @param name object name. Can be <code>null</code> or empty if id is specified.
    * @param objectType the reference object type serialized string.
    * @return reference to the object placeholder stands for.
    */
   private IPSReference getObjectRefFromPlaceholderData(final String id,
         final String name, final String objectType) throws PSModelException
   {
      IPSCmsModel linkedModel = PSCoreFactory.getInstance().getModel(
            new PSObjectType(objectType).getPrimaryType());
      assert StringUtils.isBlank(id) ^ StringUtils.isBlank(name)
            : "Id (" + id + ") and name (" + name 
            + ") are specified simultaniously but should be mutually exclusive"; 
      return StringUtils.isBlank(name)
            ? (IPSReference) linkedModel.getReference(new PSGuid(id))
            : (IPSReference) linkedModel.getReference(name);
   }

   /**
    * A simple class to hide how the unique id is determined for an object. If
    * the id is present, it is used, otherwise the name is used.
    * 
    * @author paulhoward
    */
   private class ReferenceId
   {
      /**
       * The only ctor. If the id is present, it becomes the key for this
       * instance. Otherwise, if the ref is from a hierarchical model, the path
       * is used as the key, else, the name is used as the key.
       * 
       * @param ref Assumed not <code>null</code>.
       */
      public ReferenceId(IPSReference ref)
      {
         m_id = ref.getId();
         if (m_id == null)
         {
            if (ref.getParentModel().isHierarchyModel())
               m_name = ((IPSHierarchyNodeRef) ref).getPath();
            else
               m_name = ref.getName();
         }
         else
            m_name = null;
      }

      /**
       * The equality is based on which property is used as the id.
       */
      @Override
      public boolean equals(Object o)
      {
         if (o == null)
            return false;
         if (!(o instanceof ReferenceId))
            return false;
         if (o == this)
            return true;
         ReferenceId rhs = (ReferenceId) o;
         if (m_id != null)
            return m_id.equals(rhs.m_id);
         return m_name.equalsIgnoreCase(rhs.m_name);
      }

      /**
       * The hashcode is based on which property is used as the id.
       */
      @Override
      public int hashCode()
      {
         if (m_id != null)
            return m_id.hashCode();
         return m_name.hashCode();
      }

      /**
       * Will be <code>null</code> if {@link #m_name} is present, otherwise,
       * not <code>null</code>.
       */
      private final IPSGuid m_id;

      /**
       * Will be <code>null</code> if {@link #m_id} is present, otherwise, not
       * <code>null</code> or empty.
       */
      private final String m_name;
   }

   /**
    * This cache stores a map-like structure, where the 'key' is an
    * {@link IPSReference} and the value is a collection of
    * {@link PSUiReference}s. This collection can contain at most 1 uiRef that
    * returns <code>false</code> when its {@link PSUiReference#isReference()}
    * method is called and 0 or more instances when said method returns
    * <code>true</code>.
    * <p>
    * A real map is not used because hash codes for a reference can change over
    * time and we may fail to find a match. Therefore, we always use the
    * <code>equals</code> method in this virtual map.
    * 
    * @author paulhoward
    */
   private class Cache
   {
      /**
       * Scans the cache looking for all ui refs that wrap the supplied design
       * object ref.
       * 
       * @param wrapped Assumed not <code>null</code>.
       * 
       * @return Never <code>null</code>, may be empty if there are none. If
       * ui refs are returned, at most, 1 of the entries is 'the' node, while
       * any additional entries are references to that node. See class
       * description for more details. The nodes generally do not have the same
       * parent.
       * <p>
       * The returned set is owned by this class and should be treated read-only
       * by callers.
       */
      public List<PSUiReference> getNodes(IPSReference wrapped)
      {
         List<PSUiReference> results = m_cache.get(new ReferenceId(wrapped));
         if (results == null)
            results = new ArrayList<PSUiReference>();
         return results;
      }

      /**
       * Sets the children list associated with the cache entry for the supplied
       * node to <code>null</code>.
       * 
       * @param node Never <code>null</code>.
       */
      public void flush(PSUiReference node)
      {
         if (null == node)
         {
            throw new IllegalArgumentException("node cannot be null");  
         }
         Map<String, Pair> map = getContainingMap(node);
         Pair p = map.get(makeHierarchyMapKey(node));
         p.m_children = null;
      }

      /**
       * Pretty-prints the contents of this cache's hierarchical part, indenting
       * each level of the tree 3 spaces and grouping folders/files and sorting
       * in ascending alpha order.
       */
      public void dumpCache()
      {
         List<PSUiReference> rootNodes = new ArrayList<PSUiReference>();
         rootNodes.addAll(getRootNodes());
         sort(rootNodes);
         for (PSUiReference node : rootNodes)
         {
            System.out.println(node.getDisplayLabel());
            @SuppressWarnings("unchecked")
            Map<String, Pair>[] childMap = new Map[1];
            if (!getChildMap(node, childMap))
               dumpLevel(childMap[0], 1);
         }
      }
      
      /**
       * Processes the children for the {@link #dumpCache()} method,
       * recursively.
       * 
       * @param children Assumed not <code>null</code>.
       * 
       * @param depth Controls how many spaces to indent when printing. 3*depth
       * spaces will be printed.
       */
      private void dumpLevel(Map<String, Pair> children, int depth)
      {
         List<PSUiReference> folders = new ArrayList<PSUiReference>();
         List<PSUiReference> files = new ArrayList<PSUiReference>();
         for (String childName : children.keySet())
         {
            Pair p = children.get(childName);
            if (p.m_node.isFolder())
               folders.add(p.m_node);
            else
               files.add(p.m_node);
         }

         sort(folders);
         sort(files);

         for (PSUiReference folderNode : folders)
         {
            for (int i = 0; i < depth * 3; i++)
               System.out.print(" ");
            System.out.println("(F) " + folderNode.getDisplayLabel());

            @SuppressWarnings("unchecked")
            Map<String, Pair>[] childMap = new Map[1];

            Pair p = children.get(makeHierarchyMapKey(folderNode));
            if (!getChildMap(p.m_node, childMap))
            {
               dumpLevel(childMap[0], depth + 1);
            }
         }

         for (PSUiReference fileNode : files)
         {
            for (int i = 0; i < depth * 3; i++)
               System.out.print(" ");
            System.out.println(fileNode.getName());

            @SuppressWarnings("unchecked")
            Map<String, Pair>[] childMap = new Map[1];

            Pair p = children.get(makeHierarchyMapKey(fileNode));
            if (!getChildMap(p.m_node, childMap))
            {
               dumpLevel(childMap[0], depth + 1);
            }
         }
      }

      /**
       * Sorts nodes ascending based on name.
       * 
       * @param nodes Assumed not <code>null</code>.
       */
      private void sort(List<PSUiReference> nodes)
      {
         Collections.sort(nodes, new Comparator<PSUiReference>()
         {
            /**
             * Sorts ascending based on the name.
             * @param r1 Assumed not <code>null</code>.
             * @param r2 Assumed not <code>null</code>.
             * @return See interface.
             */
            public int compare(PSUiReference r1, PSUiReference r2)
            {
               return r1.getDisplayLabel().toLowerCase().compareTo(
                     r2.getDisplayLabel().toLowerCase());
            }
         });
      }

      /**
       * Finds a node in the cache that has the supplied parent and name.
       * 
       * @param parent The parent to look for. Assumed to be present in the
       * cache because it was returned by a previous call to this method.
       * 
       * @param partName The name of the child. Assumed not <code>null</code>. 
       * The compare is done case-insensitive.
       * 
       * @param result The child is returned as the first element of this array.
       * Assumed not <code>null</code>. The returned value is either the
       * child, or <code>null</code>. To determine if further cataloging may
       * find it, see the return boolean.
       * 
       * @return <code>true</code> if the child was or should have been
       * present in the cache because the parent has been previously cataloged,
       * <code>false</code> otherwise.
       */
      public boolean getChildNode(PSUiReference parent, String partName,
            PSUiReference[] result)
      {
         result[0] = null;
         @SuppressWarnings("unchecked")
         Map<String, Pair>[] map = new Map[1];
         boolean createdMap = getChildMap(parent, map);
         if (createdMap)
            return false;
         result[0] = null;
         for (Pair p : map[0].values())
         {
            if (p.m_node.getDisplayLabel().equalsIgnoreCase(partName))
            {
               result[0] = p.m_node;
               break;
            }
         }
         return true;
      }

      /**
       * Gets the cached nodes (those previously added with
       * {@link #addNodes(PSUiReference, Collection, boolean)} and not removed
       * with {@link #removeNodes(IPSReference)} whose parent is
       * <code>null</code>.
       * 
       * @return The currently known roots. Never <code>null</code>. The
       * returned collection is owned by this class and should be treated
       * read-only by callers.
       */
      public Collection<PSUiReference> getRootNodes()
      {
         Collection<PSUiReference> results = new ArrayList<PSUiReference>();
         for (Pair p : m_hierarchyCache.values())
         {
            results.add(p.m_node);
         }
         return results;
      }

      /**
       * Adds the supplied nodes to appropriate caches managed by this object.
       * If the node wraps a design object, it is added to the cache that tracks
       * all nodes by their wrapped handle. It is also added to the hierarchy
       * cache that tracks all nodes.
       * 
       * @param parent The parent of the supplied nodes. May be
       * <code>null</code>.
       * 
       * @param nodes The nodes to add. Assumed not <code>null</code>.
       * 
       * @param clearFirst Clear this parent's child cache before adding the new
       * nodes. Should be <code>true</code> if these nodes resulted from a
       * catalog, <code>false</code> otherwise.
       */
      public void addNodes(PSUiReference parent, Collection<PSUiReference> nodes,
            boolean clearFirst)
      {
         addDesignNodes(nodes);

         Map<String, Pair> hierarchyCacheMap;
         if (parent == null)
         {
            hierarchyCacheMap = m_hierarchyCache;
         }
         else
         {
            @SuppressWarnings("unchecked")
            Map<String, Pair>[] tmp = new Map[1];
            getChildMap(parent, tmp);
            hierarchyCacheMap = tmp[0];
         }

         if (clearFirst)
            hierarchyCacheMap.clear();
         for (PSUiReference node : nodes)
         {
            Pair p = new Pair(node, null);
            hierarchyCacheMap.put(makeHierarchyMapKey(node), p);
         }
      }

      /**
       * Creates a key that is either the name of the node (if there is no
       * guid) or a string representation of the guid. This allows the cache
       * to be independent of design object name changes as much as possible.
       * 
       * @param node Assumed not <code>null</code>.
       * 
       * @return Never <code>null</code> or empty.
       */
      private String makeHierarchyMapKey(PSUiReference node)
      {
         IPSGuid id = node.getId();
         return id == null ? node.getDisplayLabel().toLowerCase() : id
               .toString();
      }
      
      /**
       * If the supplied nodes do not have an {@link IPSReference} child, they
       * are skipped. Otherwise, the node wrapper is cached with its reference.
       * A check is made to see if a matching node is already in the cache and
       * if found, it is replaced.
       * 
       * @param nodes Assumed to be from same parent.
       */
      private void addDesignNodes(Collection<PSUiReference> nodes)
      {
         for (PSUiReference node : nodes)
         {
            IPSReference ref = node.getReference();
            if (ref == null)
               continue;
            List<PSUiReference> values = m_cache.get(new ReferenceId(ref));
            if (values == null)
            {
               values = new ArrayList<PSUiReference>();
               m_cache.put(new ReferenceId(ref), values);
            }
            values.remove(node);
            values.add(node);
         }
      }

      /**
       * Remove the supplied node from the hierarchy cache. Generally, this is
       * used to remove nodes that represent references to objects rather than
       * the object itself.
       * 
       * @param node Assumed not <code>null</code>.
       */
      public void removeNode(PSUiReference node)
      {
         Map<String, Pair> map = getContainingMap(node);
         map.remove(makeHierarchyMapKey(node));
      }
      
      /**
       * Deletes all nodes in the cache that have the supplied ref as a
       * property.
       * 
       * @param wrapped Any value allowed, but only valid values will have an
       * effect.
       * 
       * @return The nodes that were actually deleted, or <code>null</code> if
       * this cache doesn't contain any.
       */
      public Collection<PSUiReference> removeNodes(IPSReference wrapped)
      {
         List<PSUiReference> nodes = m_cache.remove(new ReferenceId(wrapped));
         if (nodes != null)
         {
            for (PSUiReference node : nodes)
            {
               Map<String, Pair> map = getContainingMap(node);
               map.remove(makeHierarchyMapKey(node));
            }
         }
         return nodes;
      }
      
      /**
       * Modifies the cache so it represents the changes described by the
       * parameters and changes the parent of the children to match the new
       * node.
       * 
       * @param sourceParent The parent of the children before they were moved.
       * May be <code>null</code> if the root node.
       * 
       * @param children Those who were moved. Never <code>null</code>.
       * 
       * @param targetParent The parent of the children after they were moved.
       * May be <code>null</code> if the root node.
       */
      public void moveNodes(PSUiReference sourceParent,
            Collection<PSUiReference> children, PSUiReference targetParent)
      {
         @SuppressWarnings("unchecked")
         Map<String, Pair>[] sourceCacheMap = new Map[1];
         getChildMap(sourceParent, sourceCacheMap);
         @SuppressWarnings("unchecked")
         Map<String, Pair>[] targetCacheMap = new Map[1];
         getChildMap(targetParent, targetCacheMap);

         for (PSUiReference child : children)
         {
            String key = makeHierarchyMapKey(child);
            Pair p = sourceCacheMap[0].remove(key);
            assert (p != null);
            targetCacheMap[0].put(key, p);
            child.setParentNode(targetParent);
         }
      }

      /**
       * Gets the map containing the children of the supplied parent, creating
       * it if it doesn't already exist.
       * 
       * @param parent May be <code>null</code> to indicate the root.
       * 
       * @param result Should be an array of length 1. The map is returned as
       * the first entry.
       * 
       * @return <code>true</code> if the map was found in the cache,
       * <code>false</code> if one was created.
       */
      private boolean getChildMap(PSUiReference parent,
            Map<String, Pair>[] result)
      {
         boolean createdMap = false;
         Map<String, Pair> containingMap = getContainingMap(parent);
         assert containingMap != null;
         Map<String, Pair> childMap = 
            containingMap.get(makeHierarchyMapKey(parent)).m_children;
         if (childMap == null)
         {
            childMap = new HashMap<String, Pair>();
            createdMap = true;
            containingMap.get(makeHierarchyMapKey(parent)).m_children = childMap;
         }
         result[0] = childMap;
         return createdMap;
      }

      /**
       * Gets the map that contains the supplied node as one of its children.
       * More specifically, <code>map.get(node.getName().toLowerCase())</code>
       * will return the node, where map is the returned value.
       * 
       * @param node Assumed not <code>null</code>.
       * 
       * @return The actual map from the cache. Changes to this value will
       * affect the cache. Never <code>null</code>.
       * 
       * @throws IllegalStateException If the map is not found.
       */
      private Map<String, Pair> getContainingMap(PSUiReference node)
      {
         Map<String, Pair> result = null;
         if (node.getParentNode() != null)
         {
            result = getContainingMap(node.getParentNode());
            
            String mapKey = makeHierarchyMapKey(node.getParentNode());
            Pair mapValue = result.get(mapKey);
            if (mapValue != null)
               result = mapValue.m_children;
         }
         else
         {
            result = m_hierarchyCache;
         }
         if (null == result)
         {
            throw new IllegalStateException(
                  "Couldn't find containing map for " + node.getPath());
         }
         return result;
      }

      /**
       * This map contains the links between the design object references and
       * the UI references that contain them. There may be multiple UI refs for
       * each design ref, so they are grouped in a set. Never <code>null</code>,
       * may be empty.
       */
      private final Map<ReferenceId, List<PSUiReference>> m_cache = 
         new HashMap<ReferenceId, List<PSUiReference>>();

      /**
       * Very simple class that groups 2 pieces of information.
       */
      private class Pair
      {
         public Pair(PSUiReference node, Map<String, Pair> children)
         {
            m_node = node;
            m_children = children;
         }

         PSUiReference m_node;

         Map<String, Pair> m_children;
      }

      /**
       * Contains all nodes in the tree, arranged hierarchically. For each
       * level, the key is the value returned by the
       * {@link #makeHierarchyMapKey(PSUiReference)}, and the value is the node
       * itself and all children. If the node hasn't been cataloged (or isn't a
       * container), the child entry will be <code>null</code>. Never
       * <code>null</code>.
       */
      private Map<String, Pair> m_hierarchyCache = new HashMap<String, Pair>();
   }

   /**
    * This is the name of the property stored in any {@link PSUiReference} node
    * that is managed with a placeholder object.
    */
   private static final String PLACEHOLDER_PROPNAME = "placeholder_ref";

   /**
    * This is the name of the property stored in any {@link PSUiReference} node
    * at the root of the visible tree.
    */
   private static final String ROOTNAME_PROPNAME = "rootName";

   /**
    * The one and only. Set in {@link #getInstance()}, then never modified or
    * <code>null</code>.
    */
   private static PSDesignObjectHierarchy ms_instance;

   /**
    * Contains all object types that this tree contains that have been
    * registered with listeners. The listener is used to modify the tree when
    * other code makes changes to the model. May be empty, never
    * <code>null</code>. Once a type is registered, it is not removed until
    * this object is disposed or a logoff event is received.
    */
   private final Set<PSObjectTypes> m_registeredTypes = 
      new HashSet<PSObjectTypes>();

   /**
    * Home nodes are the place that a design object of some type will be placed
    * if no other location is given. Each design object instance has exactly 1
    * home node, but any given type may have multiple ones. We store them here
    * as we find them during processing of the defs so they are easily found.
    */
   // todo (OK for release) should this be moved to the processor?
   private Map<PSObjectType, Collection<PSUiReference>> m_homeNodes = 
      new HashMap<PSObjectType, Collection<PSUiReference>>();

   /**
    * Maintains the set of all known design object change listeners. Never
    * <code>null</code>, may be empty.
    */
   private final Map<String, Set<IPSHierarchyChangeListener>> m_uiListeners = 
      new HashMap<String, Set<IPSHierarchyChangeListener>>();

   /**
    * This map maintains the set of processors for all known tree fragments. The
    * key is the root node of the fragment and the value is the processor that
    * knows how to build the tree.
    */
   private Map<String, PSHierarchyDefProcessor> m_treeParts = 
      new HashMap<String, PSHierarchyDefProcessor>();

   /**
    * A flag to track whether we are connected. It is toggled by the core 
    * listener we registered in the ctor.
    */
   private boolean m_connected = false;
   
   /**
    * This is the type for a <code>USER_FILE</code> placeholder type. We keep
    * this around to save on typing. Never <code>null</code>.
    */
   private PSObjectType PLACEHOLDER = PSObjectTypeFactory.getType(
         PSObjectTypes.USER_FILE, PSObjectTypes.UserFileSubTypes.PLACEHOLDER);

   /**
    * This is the type for a <code>USER_FILE</code> folder type. We keep this
    * around to save on typing. It is purely a
    * convenience. Never <code>null</code>.
    */
   private PSObjectType WORKBENCH_FOLDER = PSObjectTypeFactory.getType(
         PSObjectTypes.USER_FILE,
         PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER);

   /**
    * The name of the property stored in a placeholder node that contains the 
    * guid of the design object for which it is holding a place. The value is
    * the string representation of the guid.
    */
   private static final String PLACEHOLDER_GUID_PROPNAME = "guid";

   /**
    * The name of the property stored in a placeholder node that contains the 
    * name of the design object for which it is holding a place.
    * It is mutually exclusive with the {@link #PLACEHOLDER_GUID_PROPNAME} property.
    * Name is used instead of guids if guids are not available for this object type. 
    */
   private static final String PLACEHOLDER_NAME_PROPNAME = "name";

   /**
    * The name of the property stored in a placeholder node that contains the 
    * object type of the design object for which it is holding a place. The 
    * value is the serialized form of the {@link PSObjectType} for the linked
    * object.
    */
   private static final String PLACEHOLDER_OBJECT_TYPE_PROPNAME = "objectType";
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Log ms_logger = LogFactory
         .getLog(PSDesignObjectHierarchy.class);
}
