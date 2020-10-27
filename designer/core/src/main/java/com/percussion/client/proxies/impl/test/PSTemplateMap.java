/******************************************************************************
 *
 * [ PSTemplateMap.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;

import java.util.List;

/**
 * Helper class that encapsulates the template map that is used as the template repository
 * in the test template proxy
 * {@link com.percussion.client.proxies.impl.test.PSTemplateModelProxy}. This is
 * class is required mainly to get a better control on serialization and
 * deserialization using the class
 * {@link com.percussion.xml.serialization.PSObjectSerializer} and has no other
 * use.
 * 
 */
public class PSTemplateMap extends PSRepositoryMap
{  
   
   public PSTemplateMap()
   {
      // 
   }
   
   /**
    * Add a template
    * @param obj
    */
   public void addAssemblyTemplate(PSUiAssemblyTemplate obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj may not be null");
      }
      put(getReference(obj), obj);
   }
   
   /**
    * Get templates
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<PSUiAssemblyTemplate> getAssemblyTemplates()
   {
      return m_values;
   }      

   /* 
    * @see com.percussion.client.proxies.test.impl.PSRepositoryMap#getType()
    */
   @Override
   public PSObjectTypes getType()
   {
      return PSObjectTypes.TEMPLATE;
   }

  
}
