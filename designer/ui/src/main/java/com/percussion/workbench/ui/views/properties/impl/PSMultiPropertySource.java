/******************************************************************************
 *
 * [ PSMultiPropertySource.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.properties.impl;

import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.views.properties.IPSMultiPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

import java.util.Collection;

/**
 * Scans all child nodes, recursively, to find all occurrences of
 * IPropertySource. Retrieves properties from these and presents them as a table
 * of objects (rows) with properties based on the object type. The object type,
 * name, description and path are shown in the table.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSMultiPropertySource implements IPSMultiPropertySource
{

   public PSMultiPropertySource()
   {

   }

   /**
    * @param propertySources Never <code>null</code>. May be empty if there
    * are no properties available for this object.
    */
   public PSMultiPropertySource(Collection<IPropertySource> propertySources)
   {

   }

   /**
    * Retrieves identifiers for each property that is currently enabled.
    * Properties are enabled/disabled through preferences settings.
    * 
    * @return Each entry contains the properties for a single object. The
    * <code>IPropertyDescriptor<code>s returned
    * by this source are actually <code>IPSPropertyDescriptor</code>s.
    */
   public Collection<IPropertySource> getProperties()
   {
      return null;
   }

   /**
    * If this set of properties is of a single type, that type is returned,
    * otherwise, the ?? value is returned.
    */
   public PSObjectTypes getObjectType()
   {
      return null;
   }

}
