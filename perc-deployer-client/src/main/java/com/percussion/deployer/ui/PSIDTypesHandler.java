/******************************************************************************
 *
 * [ PSIDTypesHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.error.PSDeployException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Handles client side ID Type guessing
 */
public class PSIDTypesHandler
{
   /**
    * Initializes the context identifier to dependency type mappings.
    * 
    * @param server The server to use to catalog dependency types, may not
    * be <code>null</code> and must be connected.
    * 
    * @throws PSDeployException If there are any errors.
    */
   public PSIDTypesHandler(PSDeploymentServer server) throws PSDeployException
   {
      if(server == null)
         throw new IllegalArgumentException("server may not be null.");
      
      if(!server.isConnected())
         throw new IllegalArgumentException("server must be connected.");
      
      m_typeMap = new HashMap();
      PSCatalogResultSet types = server.getLiteralIDTypes();
      Iterator literalTypes = types.getResults();
      while(literalTypes.hasNext())
      {
         PSCatalogResult result = (PSCatalogResult)literalTypes.next();
         
         String trueType = result.getID();
         
         // don't guess types with parents - these require manual selection
         if (server.getParentType(trueType) != null)
            continue;
         
         String compType = trueType.toLowerCase();
         
         //strip off "def" suffix         
         if (compType.endsWith(TYPE_SUFFIX))
         {
            compType = compType.substring(0, compType.length() - 
               TYPE_SUFFIX.length());
         }
         
         // put all recognized patterns in the map
         m_typeMap.put(compType, trueType);
         m_typeMap.put(compType + NAME_SUFFIX, trueType);
         m_typeMap.put(NAME_PREFIX + compType, trueType);
         m_typeMap.put(NAME_PREFIX + compType + NAME_SUFFIX, trueType);
      }      
   }
   
   /**
    * Performs a guess for all of the supplied ID type mappings, updating the
    * type of each mapping for which a guess can be made.  
    * 
    * @param types An iterator over zero or more {@link PSApplicationIDTypes}
    * objects, may not be <code>null</code>.
    */
   public void guessIdTypes(Iterator types)
   {
      if (types == null)
         throw new IllegalArgumentException("types may not be null");
      
      while (types.hasNext())
      {
         PSApplicationIDTypes idTypes = (PSApplicationIDTypes) types.next();
         Iterator mappings = idTypes.getAllMappings(true);
         while (mappings.hasNext())
         {
            PSApplicationIDTypeMapping mapping = 
               (PSApplicationIDTypeMapping) mappings.next();
            guessType(mapping);
         }
      }
   }

   /**
    * Guesses the supplied mapping, updating the type if possible.
    * 
    * @param mapping The mapping to guess, assumed not <code>null</code>.
    */
   private void guessType(PSApplicationIDTypeMapping mapping)
   {
      String name = mapping.getContext().getIdentifier();
      String type = name == null ? null : 
         (String) m_typeMap.get(name.toLowerCase());
      
      if (type != null)
         mapping.setType(type);
      else if (mapping.getValue().equals("0"))
         mapping.setType(mapping.TYPE_NONE);
   }
   
   /**
    * Map of context value identifiers to dependency types, both as 
    * <code>String</code> objects.  Never <code>null</code> or modified
    * after construction.
    */
   private Map m_typeMap;

   /**
    * Constant for the "def" suffix on a dependency type.
    */
   private static String TYPE_SUFFIX = "def";
   
   /**
    * Constant for the "id" suffix on a context value identifier
    */
   private static String NAME_SUFFIX = "id";
   
   /**
    * Constant for the "sys_" prefix on a context value identifier
    */   
   private static String NAME_PREFIX = "sys_";
}

