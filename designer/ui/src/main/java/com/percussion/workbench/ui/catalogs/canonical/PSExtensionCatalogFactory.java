/*******************************************************************************
 *
 * [ PSExtensionCatalogFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.catalogs.canonical;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.models.IPSExtensionModel.Interfaces;
import com.percussion.extension.PSExtensionDef;
import com.percussion.workbench.ui.IPSCatalog;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.catalogs.PSCatalogFactoryBase;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor;
import com.percussion.workbench.ui.views.hierarchy.PSHierarchyDefProcessor.InheritedProperties;
import com.percussion.workbench.ui.views.hierarchy.xmlbind.Catalog;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Creates a cataloger that can retrieve extensions - providing them in a 
 * hierarchical structure. The hierarchy is totally constructed by this class.
 * The declarative def (if there was one) is ignored.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSExtensionCatalogFactory extends PSCatalogFactoryBase
{
   /**
    * See {@link PSCatalogFactoryBase#PSCatalogFactoryBase(InheritedProperties, 
    * PSHierarchyDefProcessor, Catalog)} for further details.
    * 
    * @param contextProps One property is required, {@link #INTERFACE_PROPNAME},
    * which is used to filter the extensions. Its value may be 'javascript' to
    * get all javascript extensions, or any Java interface name that is
    * supported.
    */
   public PSExtensionCatalogFactory(InheritedProperties contextProps,
         PSHierarchyDefProcessor proc, Catalog type)
   {
      super(contextProps, proc, type);
      if (StringUtils.isBlank(getContextProperty(INTERFACE_PROPNAME)))
      {
         throw new IllegalArgumentException(INTERFACE_PROPNAME
               + " property must be supplied");
      }
   }   
   
   // see interface
   public IPSCatalog createCatalog(PSUiReference parent)
   {
      return new BaseCataloger(parent)
      {
         //see interface for details
         public List<PSUiReference> getEntries(boolean force)
            throws PSModelException
         {
            final String interfaceStr = getContextProperty(INTERFACE_PROPNAME);
            try
            {
               Collection<IPSReference> resultRefs;
               if (interfaceStr.equalsIgnoreCase("javascript"))
                  resultRefs = getJavaScriptExtensions(force);
               else
                  resultRefs = getInterfaceExtensions(interfaceStr, force);
               return createNodes(resultRefs);
            }
            catch (PSMultiOperationException e)
            {
               return Collections.singletonList(createErrorNode(e));
            }
            catch (InterfaceParsingException e)
            {
               return Collections.singletonList(createErrorNode(e));
            }
         }

         /**
          * Gets all extensions whose handler type is JavaScript (case
          * insensitive.)
          * 
          * @param force Passed through to the model when cataloging. If
          * <code>true</code>, then the request will go to the server,
          * bypassing the cache.
          * 
          * @return Never <code>null</code>, may be empty.
          * 
          * @throws PSModelException If any problems communicating w/ the
          * server.
          * 
          * @throws PSMultiOperationException If any problems communicating w/
          * the server.
          */
         private List<IPSReference> getJavaScriptExtensions(boolean force)
            throws PSModelException, PSMultiOperationException
         {
            final Set<IPSReference> categorizedRefs = getCategorizedRefs(
                  getTreeName());
            final List<IPSReference> refs = 
               getExtensionRefs(new ExtensionFilter()
            {
               public boolean accept(PSExtensionDef def, IPSReference ref)
               {
                  return !categorizedRefs.contains(ref)
                        && def.getRef().getHandlerName().equalsIgnoreCase(
                        "javascript");
               }
            }, force);
            
            return refs;
         }

         /**
          * Returns extensions implementing the specified interface.
          * These values are further filtered by removing
          * entries that have placeholders somewhere in the current tree. 
          * @param force Controls whether cached data can be returned.
          * <code>true</code> indicates to make a server request, even if the
          * data is in the cache.
          * @throws InterfaceParsingException if interface parsing failed.
          */
         private List<IPSReference> getInterfaceExtensions(
               final String interfaceName, boolean force)
            throws PSModelException, PSMultiOperationException,
            InterfaceParsingException
         {
            validateInterfaceName(interfaceName);
            final Set<IPSReference> categorizedRefs = getCategorizedRefs(
                  getTreeName());
            final List<IPSReference> refs = 
               getExtensionRefs(new ExtensionFilter()
            {
               public boolean accept(PSExtensionDef def, IPSReference ref)
               {
                  if (interfaceName.equals(JEXL_INTERFACE))
                  {
                     /**
                      * Filter out velocity tools extensions since they aren't
                      * handled by the extensions manager
                      */
                     if (def.getRef().getContext().contains("velocity"))
                     {
                        return false;
                     }
                  }
                  return !categorizedRefs.contains(ref)
                        && hasMatchingInterface(def, interfaceName);
               }
            }, force);
            
            return refs;
         }

         /**
          * Returns map of extensions filtered by the specified filter.
          */
         private List<IPSReference> getExtensionRefs(
               ExtensionFilter filter, boolean force) throws PSModelException,
               PSMultiOperationException
         {
            boolean showDeprecated = PSWorkbenchPlugin.getDefault().getPreferences()
                  .isShowDeprecatedFunctionality();
            final Collection<IPSReference> refs = getExtensionModel().catalog(
                  force);
            final Object[] data = getExtensionModel().load(
                  refs.toArray(new IPSReference[refs.size()]), false, false);
            int i = 0;
            final List<IPSReference> resultRefs = new ArrayList<IPSReference>();
            for (IPSReference ref : refs)
            {
               final PSExtensionDef def = (PSExtensionDef) data[i++];
               if (!showDeprecated && def.isDeprecated())
               {
                  continue;
               }
               if (filter.accept(def, ref))
               {
                  resultRefs.add(ref);
               }
            }
            return resultRefs;
         }
      };
   }

   /**
    * Makes sure the provided string presents valid interface name.
    * @param interfaceName interface name to validate.
    * @throws InterfaceParsingException is thrown if defined that interfaceName
    * is invalid.
    */
   private void validateInterfaceName(final String interfaceName)
         throws InterfaceParsingException
   {
      if (Interfaces.findByClassName(interfaceName) == null)
      {
         throw new InterfaceParsingException("Unrecognized interface "
               + interfaceName);
      }
   }

   /**
    * Walks all interfaces defined for the def and checks if any of
    * them match the supplied type.
    * 
    * @param def Assumed not <code>null</code>.
    * @param interfaceName interface the returned extensions should implement.
    * @return <code>true</code> if the specified extension implements any
    * interfaces.
    */
   private boolean hasMatchingInterface(final PSExtensionDef def, 
         final String interfaceName)
   {
      final Iterator<String> iter = def.getInterfaces();
      while (iter.hasNext())
      {
         if (iter.next().equals(interfaceName))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Looks up the tree name using the property name "userPathRootName".
    * 
    * @return Never <code>null</code> or empty.
    * Throws {@link IllegalArgumentException} if no property is present.
    */
   private String getTreeName()
   {
      final String treeName = getContextProperty(USER_PATH_ROOTNAME_PROPNAME);
      if (StringUtils.isBlank(treeName))
      {
         throw new IllegalArgumentException("Extension tree name is not specified!");
      }
      return treeName;
   }

   /**
    * Thrown during parsing.
    *
    * @author Andriy Palamarchuk
    */
   private class InterfaceParsingException extends Exception
   {
      /**
       * Needed for serializable class.
       */
      private static final long serialVersionUID = 1L;

      public InterfaceParsingException(String msg)
      {
         super(msg);
      }
   }

   /**
    * Simple filter mechanism.
    *
    * @author paulhoward
    */
   private interface ExtensionFilter
   {
      /**
       * Determines if the supplied def should be excluded from the results.
       * 
       * @param def Assumed not <code>null</code>.
       * @param ref Assumed not <code>null</code>.
       * 
       * @return <code>true</code> if the def meets criteria, <code>false</code>
       * otherwise.
       */
      public boolean accept(PSExtensionDef def, IPSReference ref);
   }

   /**
    * Name of the property specifying interface the returned extensions should implement.
    * Optional.
    * If specified only extensions of these interfaces will be returned.
    */
   private static String INTERFACE_PROPNAME = "extensionSubType";

   /**
    * The name of the property that controls whether to get templates that don't
    * appear under some other node or content types that have local templates.
    */
    private static String USER_PATH_ROOTNAME_PROPNAME =  "userPathRootName";
    
    /**
     * The name of the jexl method interface marker
     */
    private static final String   JEXL_INTERFACE =
       "com.percussion.extension.IPSJexlExpression";
}
