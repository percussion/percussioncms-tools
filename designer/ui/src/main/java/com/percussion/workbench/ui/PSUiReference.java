/******************************************************************************
 *
 * [ PSUiReference.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.model.IPSHomeNodeManager;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.LabelSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to represent 'core' objects by wrapping the
 * {@link IPSReference} provided by the core API. In addition, it can represent
 * other objects that only exist in the UI layer.
 * <p>
 * Instances of this class are generally used to build trees within views and to
 * manage the design objects.
 * <p>
 * Instances can be sorted by the name.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSUiReference implements IAdaptable, Comparable
{
   /**
    * Since root nodes do not have a parent, a property can be set to specify
    * the desired sort order. If this property is found, it must be of type
    * {@link SortOrder}, or it is ignored. All root nodes should be set to the
    * same property. If not found, ascending alpha is assumed.
    */
   public static final String ROOT_SORT_ORDER_PROPNAME = "root_sort_order"; //$NON-NLS-1$
   
   /**
    * What order should the children of this node be sorted in. The view 
    * displaying this object should use this value to determine how to sort.
    * {@link #getChildren()} will provide elements in the correct order. This
    * value is generally needed for adding children after the catalog.
    */
   public enum SortOrder
   {
      ASCENDING,
      DESCENDING,
      
      /**
       * Use the order supplied by {@link #getChildren()}.
       */
      NONE
   }
   
   /**
    * Creates a reference to an object that originates from the core API.
    * 
    * @param parentNode See {@link #getParentNode()} for details. Never 
    * <code>null</code> except for root node.
    * @param source Generally, the UI tree contains data objects such as slots
    * and variants as well as categories and containers. This is a reference
    * returned from the core. Never <code>null</code>.
    * @param childrenSource If this node has children, supply this object to
    * provide those children. This is used to enable lazy loading. The factory
    * will not be called upon until the user expands this node. May be
    * <code>null</code>.
    * @param isReference See {@link #isReference()} for details.
    * @param isFolder See {@link #isFolder()} for details.
    * @param labelSource What to use as text for the label seen by the user.
    * Never <code>null</code>.
    * @param labelOverride If <code>labelSource</code> is <code>LABEL_KEY</code>,
    * then this value must be provided.
    */
   public PSUiReference(PSUiReference parentNode, IPSReference source,
         IPSCatalogFactory childrenSource, boolean isReference,
         boolean isFolder, LabelSource labelSource, String labelOverride)
   {
      if ( null == source)
      {
         throw new IllegalArgumentException("source cannot be null");   //$NON-NLS-1$
      }
      if (null == labelSource)
      {
         throw new IllegalArgumentException("labelSource cannot be null");   //$NON-NLS-1$
      }
      if (labelSource.equals(LabelSource.LABEL_KEY) && StringUtils.isBlank(labelOverride))
      {
         throw new IllegalArgumentException(
               "labelOverride cannot be null or empty if lableSource=LABEL_KEY");   //$NON-NLS-1$
      }
      
      m_ref = source;
      m_name = labelSource.equals(LabelSource.LABEL_KEY) ? labelOverride : null;
      m_description = ""; //$NON-NLS-1$
      m_data = null;
      m_catalogFactory = childrenSource;
      m_isReference = isReference;
      m_isFolder = isFolder;
      m_parentNode = parentNode;
      m_age = System.currentTimeMillis();
      m_labelSource = labelSource;
   }

   /**
    * This ctor provides a way to create references to objects that do not
    * originate from the core API.
    * 
    * @param parentNode See {@link #getParentNode()} for details. Never 
    * <code>null</code> except for root node.
    * 
    * @param name Never <code>null</code> or empty.
    * 
    * @param description May be <code>null</code>.
    * 
    * @param data The object to return when the {@link #getData()} method is
    * called. May be <code>null</code>.
    * 
    * @param catalogFactory May be <code>null</code> if this node has no
    * children.
    * 
    * @param isFolder See {@link #isFolder()} for details.
    */
   public PSUiReference(PSUiReference parentNode, String name,
         String description, Object data, IPSCatalogFactory catalogFactory,
         boolean isFolder)
   {
      if ( null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");   //$NON-NLS-1$
      }
      m_name = name;
      m_ref = null;
      m_age = 0;
      
      m_description = description == null ? "" : description; //$NON-NLS-1$
      m_data = data;
      m_catalogFactory = catalogFactory;
      m_isFolder = isFolder;
      m_parentNode = parentNode;
      m_labelSource = null;
   }

   /**
    * This mechanism provides several classes to enable DnD and outline support
    * (if the node supports it.) The following classes may be adaptable to,
    * depending on the node:
    * <ol>
    * <li>{@link IPSReference}</li>
    * <li>{@link IPropertySource}</li>
    * <ol>
    * 
    * @param adaptTo What do you want this object to adapt to?
    * @return <code>null</code> if this class cannot provide the requested
    *         adaptation, otherwise an compatible with the requested class.
    */
   public Object getAdapter(Class adaptTo)
   {
      if (adaptTo == IPSReference.class)
         return m_ref;
      
      Object o = null;
      //try the handler, if there is one
      if (getHandler() != null)
         o = getHandler().getAdapter(this, adaptTo);
      
      if(o == null && adaptTo == IPropertySource.class)
      {
         o = new UIReferencePropertySource(this);
      }
      
      //finally, try the platform
      if (o == null)
         o = Platform.getAdapterManager().getAdapter(this, adaptTo);
      
      return o;
      
      //todo OK to release -implement getAdapter
//      if (adaptTo.isInstance(IWorkbenchAdapter))
//      {
//         return new IWorkbenchAdapter()
//         {
//            //see interface
//            public Object[] getChildren(Object o)
//            {
//               Object[] results;
//               if (o instanceof PSUiReference)
//               {
//                  PSUiReference ref = (PSUiReference) o;
//                  results = ref.getChildren().toArray();
//               }
//               else
//                  results = new Object[0];
//               return results;
//            }
//
//            //see interface
//            public ImageDescriptor getImageDescriptor(Object object)
//            {
//               return null;
//            }
//
//            //see interface
//            public String getLabel(Object o)
//            {
//               String label;
//               if (o instanceof PSUiReference)
//               {
//                  PSUiReference ref = (PSUiReference) o;
//                  label = ref.getName();
//               }
//               else
//                  label = o.toString();
//               return label;
//            }
//
//            //see interface
//            public Object getParent(Object o)
//            {
//               Object result = null;
//               if (o instanceof PSUiReference)
//               {
//                  PSUiReference ref = (PSUiReference) o;
//                  result = ref.getParentNode();
//               }
//               return result;
//            }
//            
//         };
//      }
   }

   /**
    * See {@link #setObjectHomeInfo(PSObjectType, IPSHomeNodeManager, Map)} for
    * details. Note that multiple nodes may return the same object type. The
    * caller should use {@link #isHomeNode(IPSReference)} to determine if this
    * is the home node for a specific object instance.
    * 
    * @return The design object type that can be placed as a child of this node.
    * May be <code>null</code>.
    */
   public PSObjectType getHomeObjectType()
   {
      return m_homeObjectType;
   }

   /**
    * Set in the
    * {@link #setObjectHomeInfo(PSObjectType, IPSHomeNodeManager, Map)} method.
    * May be <code>null</code>.
    */
   private Map<String,String> m_homeObjectProperties = null;
   
   /**
    * Every design object instance has a node which is the 'default' node for
    * it. The default node becomes the parent of design objects of this type
    * when no other parent has been specified. Defaults to <code>null</code>.
    * 
    * @param type There should be 1 node (or a set of nodes) in the model that
    * are the 'home' node for an object type. This value can be obtained using
    * the {@link #getHomeObjectType()} method.
    * 
    * @param mgr If exactly one node is the home, then no manager is required.
    * Otherwise, a manager must be supplied that can sort each instance of the
    * type into exactly 1 of the nodes.
    * 
    * @param props If <code>mgr</code> is not <code>null</code>, then this
    * value is saved.
    */
   public void setObjectHomeInfo(PSObjectType type, IPSHomeNodeManager mgr,
         Map<String, String> props)
   {
      m_homeObjectType = type;
      m_homeObjectNodeManger = mgr;
      if (mgr != null)
         m_homeObjectProperties = props;
   }

   /**
    * See {@link #setObjectHomeInfo(PSObjectType, IPSHomeNodeManager, Map)}
    * for details.
    * 
    * @param ref The object to check. Never <code>null</code>.
    * 
    * @return <code>true</code> if this node is a home node for the supplied
    * object type, <code>false</code> otherwise.
    */
   public boolean isHomeNode(IPSReference ref)
   {
      if ( null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");   //$NON-NLS-1$
      }
      try
      {
         if (!m_homeObjectType.equals(ref.getObjectType()))
            return false;
         
         if (m_homeObjectNodeManger != null)
         {
            return m_homeObjectNodeManger.isHomeNode(m_homeObjectProperties,
                  PSCoreFactory.getInstance().getModel(
                        ref.getObjectType().getPrimaryType()).load(ref, false,
                        false));
         }
         return true;
      }
      catch (PSModelException e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
      catch (Exception e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * 
    * @param name Property name, case-sensitive.
    * 
    * @param value Any value is allowed.
    */
   public void setProperty(String name, Object value)
   {
      m_props.put(name, value);
   }
   
   /**
    * Remove a property entry previously added w/
    * {@link #setProperty(String, Object)}. If not found, returns w/o error.
    * 
    * @param name The property to remove, case-sensitive.
    */
   public void removeProperty(String name)
   {
      m_props.remove(name);
   }
   
   /**
    * The next time this node catalogs, it will make a query to the server and
    * ignore the cache, if there is one.
    */
   public void refresh()
   {
      m_refresh = true;
   }
   
   /**
    * 
    * @param name
    * @return The value supplied to the {@link #setProperty(String, Object)}
    * method.
    */
   public Object getProperty(String name)
   {
      return m_props.get(name);
   }
   
   public Object clearProperty(String name)
   {
      return m_props.remove(name);
   }
   
   /**
    * A folder is a container of other objects. A node could also be
    * a container, but not a folder if it was expandable, but the children it
    * contained were based on its data (effectively an outline view) rather 
    * than a model catalog. This is the case if {@link #getCatalogFactory()}
    * does not return <code>null</code>.
    * 
    * @return <code>true</code> to indicate this is a folder, <code>false</code>
    * otherwise.
    */
   public boolean isFolder()
   {
      return m_isFolder;
   }
   
   /**
    * The caller may wish to force a recatalog if objects get too old.
    * 
    * @return The number of seconds since this object was retrieved from the
    * server or since it was created if it was not retrieved from the server.
    */
   public long getAge()
   {
      return m_ref == null ? (System.currentTimeMillis() - m_age) / 1000
            : m_ref.getAge();
   }

   /**
    * If this node represents a core object, it is the description of that
    * object. Otherwise, it is the description passed in the ctor.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return m_ref == null ? m_description : m_ref.getDescription();
   }

   /**
    * This method is not meant for client use. See the associated method.
    * Convenience method that calls
    * {@link #getChildren(boolean) getChildren(<code>true</code>)}.
    */
   public List<PSUiReference> getChildren()
      throws PSModelException
   {
      return getChildren(false);
   }
   
   
   /**
    * This method is not meant for client use. Clients should use the
    * {@link com.percussion.workbench.ui.model.PSDesignObjectHierarchy
    * #getChildren(PSUiReference)} method, passing this object as the parameter.
    * <p>
    * todo - OK to release - refactor so this method can be used by clients The
    * possible children for a node can be defined in different ways. The way is
    * determined at construction time by passing a catalog factory. If the
    * catalog factory is not <code>null</code>, it is used to process this
    * method, otherwise, an empty list is returned.
    * 
    * @param useCachedNodes A flag that indicates whether to use nodes obtained
    * the last time this method was called or to rebuild the nodes. If this
    * method has never been called, or {@link #refresh()} has been called since
    * the last call, this flag has no effect.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public List<PSUiReference> getChildren(boolean useCachedNodes)
      throws PSModelException
   {
      if (m_refresh || !useCachedNodes)
      {
         m_lastCatalog = null;
      }
      
      List<PSUiReference> results;
      if (m_lastCatalog != null)
      {
         results = new ArrayList<PSUiReference>();
         results.addAll(m_lastCatalog);
         return results;
      }
      
      IPSCatalog cat = m_catalogFactory == null ? null : m_catalogFactory
            .createCatalog(this); 
      if (cat == null) 
         results = Collections.emptyList();
      else
         results = cat.getEntries(m_refresh);
      m_refresh = false;
      m_catalogExecuted = true;
      m_lastCatalog = new ArrayList<PSUiReference>();
      m_lastCatalog.addAll(results);
      return results;
   }

   /**
    * This method is not meant for client use. Adds the supplied results to the
    * currently cached child list. Maintains proper order of the cache.
    * @param results Never <code>null</code>.
    */
   public void addChildren(List<PSUiReference> results)
   {
      if (null == results)
      {
         throw new IllegalArgumentException("results cannot be null");  
      }
      if (m_lastCatalog != null)
      {
         m_lastCatalog.addAll(results);
         Collections.sort(m_lastCatalog, new Comparator<PSUiReference>()
         {
            /**
             * Compares the display labels.
             * See interface for full description.
             */
            public int compare(PSUiReference node1, PSUiReference node2)
            {
               PSUiReference n1, n2;
               if (getSortOrder() == SortOrder.DESCENDING)
               {
                  n2 = node1;
                  n1 = node2;
               }
               else
               {
                  n1 = node1;
                  n2 = node2;
               }
               return n1.getDisplayLabel().compareToIgnoreCase(
                     n2.getDisplayLabel());
            }
         });
      }
   }

   /**
    * This method is not meant for client use. Clients should use the
    * {@link com.percussion.workbench.ui.model.PSDesignObjectHierarchy
    * #deleteChildren(Collection)} method, passing this object as the parameter.
    * <p>
    * Updates the local cache of children.
    * 
    * @param node Never <code>null</code>. It is not an error if this node is
    * not in the cache.
    */
   public void deleteChild(PSUiReference node)
   {
      if (null == node)
      {
         throw new IllegalArgumentException("node cannot be null");  
      }
      
      if (m_lastCatalog != null)
         m_lastCatalog.remove(node);
   }
   
   /**
    * Unique object identifier across the CMS system. If this node represents a
    * core object, it is the id of that object (which may or may not be
    * <code>null</code> if that object has never been persisted.) Otherwise,
    * it is <code>null</code>.
    * 
    * @return May be <code>null</code>.
    */
   public IPSGuid getId()
   {
      return m_ref == null ? null : m_ref.getId();
   }

   /**
    * This model can be used to manage the data referenced by this object. If
    * this node does not represent a core object, <code>null</code> is
    * returned. In that case, you may retrieve an object using
    * {@link #getData()}.
    * 
    * @return May be <code>null</code>.
    */
   public IPSCmsModel getModel()
   {
      return m_ref == null ? null : m_ref.getParentModel();
   }

   /**
    * If this is a core object, then it is equivalent to calling
    * {@link #getModel() getParentModel.load(getReference,false,false)}.
    * Otherwise, it returns the data object supplied in the ctor, which may be
    * null.
    * 
    * @return May be <code>null</code> if this ref does not represent a core
    * object. To determine the type of a core object, you must look at the doc
    * for {@link com.percussion.client.PSObjectTypes} (there may be other types
    * as well.) To determine the type of a non-core object, you must look at
    * {@link PSUiObjectTypes}.
    * 
    * @throws Exception See
    * {@link IPSCmsModel#load(IPSReference, boolean, boolean)}.
    */
   public Object getData()
      throws Exception
   {
      return m_ref == null ? m_data : m_ref.getParentModel().load(m_ref, false, 
            false);
   }

   /**
    * The display label is what should be shown to the user. 
    * 
    * @return The returned value depends on the label source supplied in
    * the ctor. If no label source was provided in the ctor, the name provided
    * in the ctor is returned.
    */
   public String getDisplayLabel()
   {
      if (getReference() != null)
      {
         if (m_labelSource.equals(LabelSource.LABEL_KEY))
         {
            if (m_name != null)
               return m_name;
            return getName();
         }
         else if (m_labelSource.equals(LabelSource.OBJECT_NAME))
            return getReference().getName();
         else
         {
            assert(m_labelSource.equals(LabelSource.OBJECT_LABEL));
            return getReference().getLabelKey();
         }
      }
      return getName();
   }
   
   /**
    * Returns the label source, For Internal use only.
    *///fixme - OK to release - this needs to be removed and a copy ctor or such provided
   public LabelSource getLabelSource()
   {
      return m_labelSource;
   }
   
   /**
    * The string presented to end users of the product. If this node does not
    * wrap a reference, the name of the node is returned. The text to display
    * to workbench users must be obtained using the {@link #getDisplayLabel()}
    * method.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return m_ref == null ? m_name : m_ref.getLabelKey();
   }
   
   /**
    * If this node represents a core object, it is the name of that object (as
    * specified by the <code>IPSReference</code>.) Otherwise, it is the name
    * passed in the ctor.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_ref == null ? m_name : m_ref.getName();
   }

   /**
    * The sort order is used to order the children of this node. Viewers
    * rendering these nodes should use this value. {@link #getChildren()}
    * always returns nodes already ordered by this setting. Defaults to
    * {@link SortOrder#NONE}
    * 
    * @return The last value set using {@link #setSortOrder(SortOrder)} or the
    * default value if that method has never been called.
    */
   public SortOrder getSortOrder()
   {
      return m_sortOrder;
   }
   
   /**
    * See {@link #getSortOrder()} for details.
    * 
    * @param o Never <code>null</code>.
    */
   public void setSortOrder(SortOrder o)
   {
      m_sortOrder = o;
   }
   
   /**
    * The parent node is the only direct ancestor of this one. May be
    * <code>null</code> if this is the root node.
    * 
    * @return May be <code>null</code>.
    */
   public PSUiReference getParentNode()
   {
      return m_parentNode;
   }

   /**
    * Set the node that has this node as a child.
    * 
    * @param parent May be <code>null</code> for root node. Cannot be self.
    */
   public void setParentNode(PSUiReference parent)
   {
      if (parent.equals(this))
         throw new IllegalArgumentException("Can't set self as parent."); //$NON-NLS-1$
      m_parentNode = parent;
   }
   
   /**
    * This is a convenience method that calls {@link #getReference()}.
    * {@link com.percussion.client.IPSReference#getObjectType()}.
    * 
    * @return If this instance has a reference object, its type is returned,
    * otherwise, <code>null</code> is returned.
    */
   public PSObjectType getObjectType()
   {
      return m_ref == null ? null : m_ref.getObjectType();
   }

   //see Object
   @Override
   public boolean equals(Object o)
   {
      if (o == null)
         return false;
      if (!(o instanceof PSUiReference))
         return false;
      if (o == this)
         return true;
      PSUiReference rhs = (PSUiReference) o;
      
      boolean result = new EqualsBuilder()
         .append(m_catalogFactory, rhs.m_catalogFactory)
         .append(m_data, rhs.m_data)
         .append(m_description, rhs.m_description)
         .append(m_isFolder, rhs.m_isFolder)
         .append(m_isReference, rhs.m_isReference)
         .append(m_name, rhs.m_name)
         .append(m_parentNode, rhs.m_parentNode)
         .append(m_ref, rhs.m_ref)
         .append(m_sortOrder, rhs.m_sortOrder)
         .append(m_homeObjectType, rhs.m_homeObjectType)
         .append(m_homeObjectProperties, rhs.m_homeObjectProperties)
         .append(m_homeObjectNodeManger, rhs.m_homeObjectNodeManger)
         .isEquals();
      return result;
   }

   //see Object
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
         .append(m_catalogFactory)
         .append(m_data)
         .append(m_description)
         .append(m_isFolder)
         .append(m_isReference)
         .append(m_name)
         .append(m_parentNode)
         .append(m_ref)
         .append(m_sortOrder)
         .append(m_homeObjectType)
         .append(m_homeObjectProperties)
         .append(m_homeObjectNodeManger)
         .toHashCode();
   }

   /**
    * Has {@link #getChildren()} been called on this node since it was created?
    * 
    * @return <code>true</code> if yes, <code>false</code> otherwise.
    */
   public boolean isCataloged()
   {
      return m_catalogExecuted ;
   }

   /**
    * See {@link #isReference()} for a description of the property. This is only
    * meant for the hierarchy framework usage, clients should not use this.
    * 
    * @param isRef <code>true</code> to make this node a refernce node,
    * <code>false</code> if an instance node.
    */
   public void setReference(boolean isRef)
   {
      m_isReference = isRef;
   }
   
   /**
    * For objects that appear in multiple places within the hierarchy, one of
    * the instances is the object and all others are a 'reference' to that
    * object.
    * 
    * @return <code>true</code> if this object can appear multiple times
    * within the tree and this instance is not considered the canonical
    * instance.
    */
   public boolean isReference()
   {
      return m_isReference == null ? false : m_isReference.booleanValue();
   }

   /**
    * Only core objects have these.
    * 
    * @return The associated reference passed into the ctor. May be
    * <code>null</code> if this object does not represent an object that came
    * from the core.
    */
   public IPSReference getReference()
   {
      return m_ref;
   }

   /**
    * Builds the fully qualified path of this node within the tree that owns it.
    * This is done by walking ancestors to get all the path parts.
    *  
    * @return Never <code>null</code>.
    */
   public String getPath()
   {
      List<String> pathParts = new ArrayList<String>();
      PSUiReference parent = getParentNode();
      pathParts.add(getDisplayLabel());
      while (parent != null)
      {
         pathParts.add(parent.getDisplayLabel());
         parent = parent.getParentNode();
      }
      StringBuffer path = new StringBuffer();
      Collections.reverse(pathParts);
      for (String part : pathParts)
      {
         path.append("/"); //$NON-NLS-1$
         path.append(part);
      }
      
      return path.toString();
   }

   /**
    * Generally, if you want the children of this node, call
    * {@link #getChildren()}.
    * 
    * @return The factory supplied in the ctor. May be <code>null</code> if
    * there are no children.
    */
   public IPSCatalogFactory getCatalogFactory()
   {
      return m_catalogFactory;
   }

   /**
    * The handler is used to implement object specific behaviors regarding the
    * image for the node and various DnD and copy/paste operations.
    * 
    * @param handler Use <code>null</code> to clear the existing handler.
    */
   public void setHandler(IPSDeclarativeNodeHandler handler)
   {
      if (m_nodeHandler != null)
         m_nodeHandler.setOwningNode(null);
      m_nodeHandler = handler;
      if (handler != null)
         handler.setOwningNode(this);
   }
   
   /**
    * See {@link #setHandler(IPSDeclarativeNodeHandler)} for details.
    * 
    * @return May be <code>null</code>.
    */
   public IPSDeclarativeNodeHandler getHandler()
   {
      return m_nodeHandler;
   }
   
   //see interface
   public int compareTo(Object o)
   {
      PSUiReference other = (PSUiReference) o;
      return getName().compareToIgnoreCase(other.getName());
   }
   
   /**
    * An implementation of <code>IPropertySource</code> for UiReferences
    * that is read-only. Used by the <code>getAdapter()</code> method.
    */
   class UIReferencePropertySource implements IPropertySource
   {

      UIReferencePropertySource(PSUiReference node)
      {
         IPSReference ref = node.getReference();
         addProperty(PSMessages.getString("PSUiReference.property.name.label"), //$NON-NLS-1$
            PSMessages.getString("PSUiReference.property.name.description"), //$NON-NLS-1$
            node.getName());
         addProperty(PSMessages.getString("PSUiReference.property.label.label"), //$NON-NLS-1$
            PSMessages.getString("PSUiReference.property.label.description"), //$NON-NLS-1$
            ref != null ? ref.getLocalLabelKey() : node.getLabel());
         addProperty(PSMessages.getString("PSUiReference.property.description.label"), //$NON-NLS-1$
            PSMessages.getString("PSUiReference.property.description.description"), //$NON-NLS-1$
            ref != null ? ref.getLocalDescription() : node.getDescription());
         
         String idText = "";
         if (node.getId() != null)
         {
            //we use design guid to get a number that includes the type
            PSDesignGuid dguid = new PSDesignGuid(node.getId());
            idText = node.getId() + " (" + dguid.getValue() + ")";
         }
         addProperty(PSMessages.getString("PSUiReference.property.id.label"), //$NON-NLS-1$
            PSMessages.getString("PSUiReference.property.id.description"), //$NON-NLS-1$
            idText);
         addProperty(PSMessages.getString("PSUiReference.property.path.label"), //$NON-NLS-1$
            PSMessages.getString("PSUiReference.property.path.description"), //$NON-NLS-1$
            node.getPath());

         //fixme i18n
         String lockState = "unknown";
         String isPersisted = PSMessages.getString("PSUiReference.true"); //$NON-NLS-1$
         if (ref != null)
         {
            addProperty(PSMessages.getString("PSUiReference.property.type.label"), //$NON-NLS-1$
               PSMessages.getString("PSUiReference.property.type.description"), //$NON-NLS-1$
               StringUtils.defaultString(
                  PSMessages.getString("common.objecttype." +  //$NON-NLS-1$
                     node.getObjectType().toString())));

            lockState = "" + node.getReference().isLocked();
            
            if (!ref.isPersisted())
               isPersisted = PSMessages.getString("PSUiReference.false"); //$NON-NLS-1$
         }
         addProperty(PSMessages.getString("PSUiReference.property.isLocked.label"), //$NON-NLS-1$ 
               PSMessages.getString("PSUiReference.property.isLocked.description"),//$NON-NLS-1$ 
               lockState); //$NON-NLS-1$ 

         addProperty(PSMessages.getString("PSUiReference.property.isPersisted.label"), //$NON-NLS-1$ 
            PSMessages.getString("PSUiReference.property.isPersisted.description"),//$NON-NLS-1$ 
            isPersisted); //$NON-NLS-1$ 

         String nodeTypeValue;
         if (node.isReference())
            nodeTypeValue = PSMessages.getString("PSUiReference.nodeType.reference"); //$NON-NLS-1$
         else if (ref != null)
            nodeTypeValue = PSMessages.getString("PSUiReference.nodeType.instance"); //$NON-NLS-1$
         else
            nodeTypeValue = PSMessages.getString("PSUiReference.nodeType.fixed");
         addProperty(PSMessages.getString("PSUiReference.property.nodeType.label"), //$NON-NLS-1$
            PSMessages.getString("PSUiReference.property.nodeType.description"), //$NON-NLS-1$
            nodeTypeValue);
      }
      
      /**
       * Always returns <code>null</code> for this read-only
       * implementation.
       */
      public Object getEditableValue()
      {
         return null;
      }

      /* 
       * @see org.eclipse.ui.views.properties.IPropertySource#
       * getPropertyDescriptors()
       */
      public IPropertyDescriptor[] getPropertyDescriptors()
      {
         IPropertyDescriptor[] descs = new IPropertyDescriptor[]{};
         return mi_props.keySet().toArray(descs);
      }

      /* 
       * @see org.eclipse.ui.views.properties.IPropertySource#
       * getPropertyValue(java.lang.Object)
       */
      public Object getPropertyValue(Object id)
      {
         if(id == null)
            throw new IllegalArgumentException("id cannot be null"); //$NON-NLS-1$
         for(IPropertyDescriptor desc : mi_props.keySet())
         {
            if(id.equals(desc.getId()))
               return mi_props.get(desc);
         }
         return null;
      }

      /**
       * Always returns <code>false</code> for this
       * implementation
       */
      @SuppressWarnings("unused")
      public boolean isPropertySet(Object id)
      {
         return false;
      }

      /**
       * Does nothing as this is a read-only property source.
       * @throws UnsupportedOperationException Always.
       */
      @SuppressWarnings("unused")
      public void resetPropertyValue(Object id)
      {
         throw new UnsupportedOperationException("read-only property");
      }

      /**
       * Does nothing as this is a read-only property source.
       * @throws UnsupportedOperationException Always.
       */
      @SuppressWarnings("unused")
      public void setPropertyValue(Object id, Object value)
      {
         throw new UnsupportedOperationException("read-only property");
      }
      
      /**
       * Convenience method to help add new properties to this source
       * @param name cannot be <code>null</code> or empty.
       * @param description may be <code>null</code> or empty.
       * @param value may be <code>null</code>
       */
      private void addProperty(String name, String description, Object value)
      {
         if(StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty"); //$NON-NLS-1$
         PropertyDescriptor desc = new PropertyDescriptor(name, name);
         desc.setDescription(description);
         mi_props.put(desc, value);
         
      }
      
      /**
       * Map that contains the property descriptors and property values
       * for this property source
       */
      private Map<IPropertyDescriptor, Object> mi_props = 
         new HashMap<IPropertyDescriptor, Object>();
      
   }

   /**
    * Set in one of the ctors, then never modified. Will be <code>null</code>
    * if this is not a core object.
    */
   private final IPSReference m_ref;

   /**
    * Set in all ctors. Can be <code>null</code>.
    */
   private final IPSCatalogFactory m_catalogFactory;

   /**
    * Only applicable if {@link #m_ref} is <code>null</code>. In this case,
    * it is the name of this non-core object. If <code>m_ref</code> is
    * <code>null</code>, this is never <code>null</code> or empty.
    */
   private final String m_name;

   /**
    * Only applicable if {@link #m_ref} is <code>null</code>. In this case, it
    * is the name of this non-core object. Never <code>null</code>, may be
    * empty.
    */
   private final String m_description;

   /**
    * Only applicable if {@link #m_ref} is <code>null</code>. In this case, it
    * is the data of this non-core object. May be <code>null</code>.
    */
   private final Object m_data;
   
   /**
    * Only applicable if {@link #m_ref} is <code>null</code>. In this case, it
    * contains a timestamp of when the object was created.
    */
   private transient final long m_age;
   
   /**
    * See {@link #isReference()} for details. <code>null</code> until set
    * once, then never <code>null</code>. <code>null</code> is equivalent
    * to <code>false</code>.
    */
   private Boolean m_isReference;

   /**
    * See {@link #isFolder()} for details.
    */
   private final boolean m_isFolder;

   /**
    * See {@link #getParentNode()} for details.
    */
   private PSUiReference m_parentNode;

   /**
    * See {@link #isCataloged()}. Defaults to <code>false</code>.
    */
   private transient boolean m_catalogExecuted = false;

   /**
    * See {@link #setObjectHomeInfo(PSObjectType, IPSHomeNodeManager, Map)} for
    * details.
    */
   private PSObjectType m_homeObjectType;

   /**
    * See {@link #setObjectHomeInfo(PSObjectType, IPSHomeNodeManager, Map)} for
    * details. May be <code>null</code>.
    */
   private IPSHomeNodeManager m_homeObjectNodeManger;
   
   /**
    * Stores the order as set by the {@link #setSortOrder(SortOrder)} method.
    * See {@link #getSortOrder()} for more details.
    */
   private SortOrder m_sortOrder = SortOrder.NONE;
   
   /**
    * See {@link #setHandler(IPSDeclarativeNodeHandler)} for details. 
    */
   private IPSDeclarativeNodeHandler m_nodeHandler;

   /**
    * See {@link #setProperty(String, Object)} for details. Never
    * <code>null</code>.
    */ 
   private Map<String, Object> m_props = new HashMap<String, Object>();

   /**
    * A flag to indicate that the next time catalog is called, it should be 
    * forced to query the server and not deliver from a cache. Set when the
    * {@link #refresh()} method is called. Cleared after a catalog is performed.
    */
   private boolean m_refresh = false;

   /**
    * Where do we get the text for the UI label from? Never <code>null</code>
    * after construction. If there is no reference, then LabelKey is used.
    */
   private final LabelSource m_labelSource;

   /**
    * Either <code>null</code>, or a copy of the nodes returned by the last
    * call to {@link #getChildren(boolean)}. Only modified by that method.
    */
   private List<PSUiReference> m_lastCatalog;
}
