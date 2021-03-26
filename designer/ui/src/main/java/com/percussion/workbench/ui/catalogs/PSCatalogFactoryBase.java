/*******************************************************************************
 *
 * [ PSCatalogFactoryBase.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSExtensionModel;
import com.percussion.client.models.IPSTemplateModel;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.IPSCatalogFactory;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefinitionException;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Provides storage and basic access to manage the data supplied in the ctor.
 * The main purpose is to help ensure the derived class has the proper ctor
 * signature required by the framework.
 * <p>
 * No support is provided for a derived class as inner class.
 * 
 * @author paulhoward
 * @version 6.0
 */
public abstract class PSCatalogFactoryBase implements IPSCatalogFactory
{
   /**
    * The only ctor. Derived classes MUST have a matching ctor or they will not
    * be instantiated by the framework.
    * 
    * @param contextProps Various properties defined in the hierarchy def that
    * are inheritable. The derived class must validate the entries. The catalog
    * properties can be obtained using the {@link #getContextProperty(String)}
    * method. May be <code>null</code>. This class takes ownership.
    * 
    * @param proc This is the processor that owns managing creation of the tree.
    * It is used to build next level catalog factories. Never <code>null</code>.
    * 
    * @param type The node for which this factory was created. Never
    * <code>null</code>.
    */
   protected PSCatalogFactoryBase(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      if ( null == proc)
      {
         throw new IllegalArgumentException("proc cannot be null");  
      }
      if ( null == type)
      {
         throw new IllegalArgumentException("type cannot be null");  
      }
      
      m_defProcessor = proc;
      m_catalogDef = type;
      if (contextProps != null)
         m_properties = contextProps;
      else
         m_properties = proc.new InheritedProperties();
   }
   
   //see Object
   @Override
   public boolean equals(Object o)
   {
      if (o == null)
         return false;
      if (!(o instanceof PSCatalogFactoryBase))
         return false;
      if (o == this)
         return true;
      PSCatalogFactoryBase rhs = (PSCatalogFactoryBase) o;
      return new EqualsBuilder()
         .append(m_catalogDef, rhs.m_catalogDef)
         .append(m_defProcessor, rhs.m_defProcessor)
         .append(m_properties, rhs.m_properties)
         .isEquals();
   }

   //see Object
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
         .append(m_catalogDef)
         .append(m_defProcessor)
         .append(m_properties)
         .toHashCode();
   }

   /**
    * Checks if the supplied name is <code>null</code> or empty. If not, it
    * then checks its value against the supplied ones.
    * 
    * @param name May be <code>null</code> or empty, but will cause an
    * IllegalArgumentException to be thrown.
    * 
    * @param validVals If the property has a value and it is not one of these
    * (case-insensitive), an IllegalArgumentException is thrown.
    * 
    * @param blankOk If the property is not present or has a blank value, this
    * flag determines whether an IllegalArgumentException is thrown. If
    * <code>true</code> and the property is empty, no exception is thrown.
    */
   protected void validatePropertyValue(String name, String[] validVals,
         boolean blankOk)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException(
               "property name cannot be null or empty");
      }
      
      String val = getContextProperty(name);
      
      if (!StringUtils.isBlank(val))
      {
         for (String tmpVal : validVals)
         {
            if (val.equalsIgnoreCase(tmpVal))
               return;
         }
      }
      else if (blankOk)
         return;
      
      String msg = 
         "The {0} property is either missing or has an unknown value ({1}).";
      throw new IllegalArgumentException(MessageFormat.format(msg, new Object[] {
            name, val }));         
   }
   
   /**
    * Copy ctor. Useful to derived classes for special cases.
    * 
    * @param source Never <code>null</code>.
    */
   protected PSCatalogFactoryBase(PSCatalogFactoryBase source)
   {
      if ( null == source)
      {
         throw new IllegalArgumentException("source cannot be null");  
      }
      m_defProcessor = source.m_defProcessor;
      m_catalogDef = source.m_catalogDef;
      m_properties = m_defProcessor.new InheritedProperties(source.m_properties);
   }
   
   /**
    * Searches for the named property (case-insensitive) among those supplied in
    * the ctor.
    * 
    * @param name Never <code>null</code> or empty.
    * 
    * @return "" if the named property is not present or its value is
    * <code>null</code>, otherwise, the value of the property.
    */
   protected String getContextProperty(String name)
   {
      if ( null == name)
      {
         throw new IllegalArgumentException("name cannot be null");  
      }
      for (Object o : m_properties.getCatalogProps().keySet())
      {
         if (o.toString().equalsIgnoreCase(name))
            return m_properties.getCatalogProps().getProperty(name, "");
      }
      return "";
   }

   /**
    * Convenience method that calls {@link #getFactory(PSObjectType, Properties)
    * getFactory(objectType, <code>null</code>)}.
    */
   protected IPSCatalogFactory getFactory(PSObjectType objectType)
   {
      return getFactory(objectType, null);
   }

   /**
    * Provides a way for derived classes to get the catalog factory when they
    * create their <code>PSUiReference</code> children.
    * 
    * @param objectType The type of the reference for which this factory is
    * being generated. May be <code>null</code>. This param is used to match
    * a template to the current node.
    * 
    * @param additionalProps If provided, these properties will be included with
    * the properties originally passed when this instance was created. If a
    * name is the same (case-sensitive) as an existing property, it is 
    * overridden by this one.
    * 
    * @return If the children of this node have children, never
    * <code>null</code>, otherwise, may be <code>null</code>.
    */
   protected IPSCatalogFactory getFactory(PSObjectType objectType, 
         Properties additionalProps)
   {
      InheritedProperties processedProps = 
         m_defProcessor.new InheritedProperties(m_properties);
      
      Properties allProps = processedProps.getCatalogProps();
      if (additionalProps != null)
         allProps.putAll(additionalProps);
      return m_defProcessor.createCatalogFactory(objectType,
            m_catalogDef, processedProps);
   }

   /**
    * Provides funcationlity common to catalog factories.
    *
    * @author paulhoward
    */
   protected abstract class BaseCataloger implements IPSCatalog
   {
      /**
       * Only ctor.
       * 
       * @param parent The parent node which will get the children that are
       * cataloged or created. May be <code>null</code>.
       */
      public BaseCataloger(PSUiReference parent)
      {
         m_parent = parent;
      }
      
      /**
       * Creates new nodes for the supplied design objects, delegating to the
       * design object model. If <code>addChildren</code> is called in a
       * derived class, it must properly handle the instance flag by looking
       * at the catalog def supplied in the ctor.
       * 
       * @param refs If <code>null</code> or empty, an empty list is returned.
       * <code>null</code> entries not allowed.  Does not validate that the 
       * supplied refs are valid children for the current parent context.
       * 
       * @return Never <code>null</code>.
       */
      public List<PSUiReference> createNodes(Collection<IPSReference> refs)
      {
         if (refs == null || refs.isEmpty())
            return new ArrayList<PSUiReference>();
         boolean instance = m_catalogDef != null ? !m_catalogDef
               .isIsReference() : true;
         //allow addChildren to validate ref entries
         List<PSUiReference> results = PSDesignObjectHierarchy
            .getInstance().addChildren(getParent(),
                  refs.toArray(new IPSReference[refs.size()]), false, instance);
         return results;
      }
      
      //see base class method for details
      public PSUiReference createEntry(IPSReference childRef)
      {
         try
         {
            return m_defProcessor.createNode(m_parent,
                  childRef, m_properties, m_catalogDef);
         }
         catch (PSHierarchyDefinitionException e)
         {
            // shouldn't happen in general
            throw new RuntimeException(e);
         }
      }

      /**
       * Builds a 'fake' node that represents the supplied error.
       * 
       * @param e May be <code>null</code>.
       * 
       * @return Never <code>null</code>.
       */
      public PSUiReference createErrorNode(Exception e)
      {
         String msg = "<Error occurred>: ";
         if (e != null)
            msg += e.getLocalizedMessage();
         return new PSUiReference(getParent(), msg, "", null, null, false);
      }
      
      /**
       * Catalogs all objects that conform to the <code>catalogFilter</code>
       * constraints provided. These values are further filtered by removing
       * entries that have placeholders somewhere in the tree of the supplied
       * name.
       * 
       * @param treeName Never <code>null</code> or empty.
       * @param force Controls whether cached data can be returned.
       * <code>true</code> indicates to make a server request, even if the
       * data is in the cache.
       * @param modelType Which model to use for the catalog. Never
       * <code>null</code>.
       * @param catalogFilter These are passed to the model's catalog method.
       * May be <code>null</code> if not needed.
       * 
       * @return Never <code>null</code>, may be empty.
       * 
       * @throws PSModelException If any problems communicating with the server.
       */
      public Collection<IPSReference> getUncategorizedRefs(String treeName,
            boolean force, PSObjectTypes modelType, PSObjectType[] catalogFilter)
         throws PSModelException
      {
         //use called class to validate treeName
         if (null == modelType)
         {
            throw new IllegalArgumentException("modelType cannot be null");  
         }
         
         Collection<IPSReference> results = getModel(modelType)
               .catalog(force, catalogFilter);
         if (results.isEmpty())
            return results;
         
         IPSReference ref = results.iterator().next();
         boolean isGuidBased = ref.getId() != null;
         Set<Object> categorizedIds = getCategorizedIdentifiers(isGuidBased,
               treeName);
         for (Iterator<IPSReference> iter = results.iterator(); iter
               .hasNext();)
         {
            IPSReference testRef = iter.next();
            if (categorizedIds.contains(isGuidBased ? testRef.getId() : testRef.getName()))
               iter.remove();
         }
         return results;
      }

      /**
       * Returns set of ids having placeholders anywhere in the tree. The server
       * is always queried to get this list.
       * 
       * @param useGuid If <code>true</code>, then the returned set will contain
       * <code>IPSGuid</code>s, otherwise it will contain <code>String</code>s
       * which are the names of the refs.
       * 
       * @param treeName Never <code>null</code> or empty.
       * 
       * @return set of IPSGuid or String presentations of the requested objects.
       * 
       * @throws PSModelException If any problems communicating with the server.
       */
      private Set<Object> getCategorizedIdentifiers(boolean useGuid,
            String treeName) throws PSModelException
      {
         Set<Object> categorizedIds = new HashSet<Object>();
         for (IPSReference ref : getCategorizedRefs(treeName))
         {
            categorizedIds.add(useGuid ? ref.getId() : ref.getName());
         }
         return categorizedIds;
      }

      /**
       * Returns set of refs having placeholders anywhere in the tree. The 
       * server is always queried.
       *  
       * @param treeName Never <code>null</code> or empty.
       * @return set of string presentations of the categorized ids.
       * @throws PSModelException
       */
      protected Set<IPSReference> getCategorizedRefs(String treeName)
         throws PSModelException
      {
         if (StringUtils.isBlank(treeName))
         {
            throw new IllegalArgumentException(
                  "treeName cannot be null or empty");
         }
         return new HashSet<IPSReference>(PSDesignObjectHierarchy
               .getInstance().getDescendentRefs(treeName));
      }

      /**
       * Builds a cataloger that is equivalent to this one, adding the supplied
       * properties to the new factory.
       * 
       * @param props May be <code>null</code>.
       * 
       * @return Never <code>null</code>.
       */
      protected IPSCatalogFactory getDescendentCataloger(
            Map<String, String> props)
      {
         try
         {
            Constructor ctor = 
               PSCatalogFactoryBase.this.getClass().getConstructor(
                  new Class[] 
                  {
                     InheritedProperties.class,
                     PSHierarchyDefProcessor.class,
                     Catalog.class
                  });
            return (IPSCatalogFactory) ctor.newInstance(
                  new Object[] 
                  {
                     m_properties.inherit(props), 
                     m_defProcessor, 
                     m_catalogDef 
                  });
         }
         catch (SecurityException e)
         {
            // should never happen
            throw new RuntimeException(e);
         }
         catch (NoSuchMethodException e)
         {
            // should never happen because we're constructiong an existing class
            throw new RuntimeException(e);
         }
         catch (IllegalArgumentException e)
         {
            throw new RuntimeException(e);
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            throw new RuntimeException(e.getTargetException());
         }
      }

      /**
       * Simple method to wrap the exception that should never happen. If you
       * need an object specific model, see the other getXXXModel methods.
       * 
       * @param modelType Never <code>null</code>.
       * 
       * @return Never <code>null</code>.
       */
      protected IPSCmsModel getModel(IPSPrimaryObjectType modelType)
      {
         if (null == modelType)
         {
            throw new IllegalArgumentException("modelType cannot be null");  
         }
         try
         {
            return PSCoreFactory.getInstance().getModel(
                  (Enum) modelType);
         }
         catch (PSModelException e)
         {
            // shouldn't happen
            throw new RuntimeException(e);
         }
      }

      /**
       * Simple method to wrap the cast and exception that should never
       * happen.
       * 
       * @return Never <code>null</code>.
       */
      protected IPSContentTypeModel getContentTypeModel()
      {
         return (IPSContentTypeModel) getModel(PSObjectTypes.CONTENT_TYPE);
      }

      /**
       * Simple method to wrap the cast and exception that should never
       * happen.
       * 
       * @return Never <code>null</code>.
       */
      protected IPSUserFileModel getFolderModel()
      {
         return (IPSUserFileModel) getModel(PSObjectTypes.USER_FILE);
      }

      /**
       * Simple method to wrap the cast and exception that should never
       * happen.
       * 
       * @return Never <code>null</code>.
       */
      protected IPSExtensionModel getExtensionModel()
      {
         return (IPSExtensionModel) getModel(PSObjectTypes.EXTENSION);
      }

      /**
       * Simple method to wrap the cast and exception that should never
       * happen.
       * 
       * @return Never <code>null</code>.
       */
      protected IPSTemplateModel getTemplateModel()
      {
         return (IPSTemplateModel) getModel(PSObjectTypes.TEMPLATE);
      }

      /**
       * Walks up the tree, starting at the parent supplied in the ctor,
       * checking each node for a reference whose primary type matches that
       * supplied.
       * 
       * @param primaryType Never <code>null</code>.
       * 
       * @return The found node, or <code>null</code>, if no match is found
       * before reaching the root.
       */
      protected PSUiReference getAncestorNode(PSObjectTypes primaryType)
      {
         if (null == primaryType)
         {
            throw new IllegalArgumentException("primaryType cannot be null");  
         }
         
         PSUiReference curParent = m_parent;
         PSUiReference result = null;
         do
         {
            PSObjectType type = curParent.getObjectType();
            if (type != null && type.getPrimaryType().equals(primaryType))
            {
               result = curParent;
               break;
            }
            curParent = curParent.getParentNode();
         }
         while(curParent != null);
         return result;
      }
      
      /**
       * Returns the node supplied in the ctor.
       * @return May be <code>null</code>.
       */
      protected PSUiReference getParent()
      {
         return m_parent;
      }
      
      /**
       * Creates a new folder with the supplied name using information supplied
       * during creation.
       * 
       * @param name The name of the new folder. Never <code>null</code> or
       * empty.
       * 
       * @param description A brief tooltip for the folder. May be
       * <code>null</code> or empty.
       * 
       * @param data Attached to the generated node. May be <code>null</code>
       * or empty.
       * 
       * @return Never <code>null</code>.
       * 
       * @throws PSHierarchyDefinitionException If declarative data for this
       * level is invalid in some way.
       */
      @SuppressWarnings("unused") //for exception
      protected PSUiReference createFolderNode(String name, String description,
            Object data)
         throws PSHierarchyDefinitionException
      {
         return m_defProcessor.createFolderNode(getParent(), name, description,
               m_properties, m_catalogDef, data);
      }
      
      /**
       * The parent of nodes that will be created by this cataloger. Supplied
       * in ctor, then never changed. May be <code>null</code>.
       */
      final private PSUiReference m_parent;
   }
   
   /**
    * Contains the properties provided in ctor. Created during class
    * construction, then never <code>null</code>.
    */
   private final InheritedProperties m_properties;

   /**
    * This is the processor that manages creation of the tree. Never
    * <code>null</code> after construction.
    */
   private final PSHierarchyDefProcessor m_defProcessor;

   /**
    * This is the node that contained the definition for this factory. It is
    * used by the processor when creating factories for child nodes generated
    * by this factory.
    */
   private final Catalog m_catalogDef;
}
