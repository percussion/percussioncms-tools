/******************************************************************************
 *
 * [ PSExtensionPropertyCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.catalogs.property;

import com.percussion.client.PSObjectTypes;
import com.percussion.workbench.ui.views.properties.IPSMultiPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

import java.util.Collection;
import java.util.List;

/**
 * Provides a set of properties that would be of interest to the implementer.
 * 
 * @version 1.0
 * @created 03-Sep-2005 4:43:51 PM
 */
public class PSExtensionPropertyCataloger implements IPSMultiPropertySource
{

   public PSExtensionPropertyCataloger()
   {

   }

   /**
    * Retrieves identifiers for each property that is currently enabled.
    * Properties are enabled/disabled through preferences settings.
    * 
    * @return Each entry identifies a specific
    *         property supported by the associated object.
    */
   public Collection<String> getPropertyKeys()
   {
      return null;
   }

   /**
    * @param references A list of objects for which you want the value specified
    *           by the key.
    * @param key A list of one or more of the values obtained from the {@link
    *           #getPropertyKeys()} method. Never <code>null</code> or empty.
    * @return Each entry is a list of values (in the same order as the
    *         corresponding keys) for the object that is in the corresponding
    *         location in the supplied references list. If a property key or a
    *         reference in the supplied lists are not of the correct type, an
    *         empty string is returned for its position.
    */
   public List getPropertyValues(List references, List key)
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

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.views.properties.IPSMultiPropertySource#getProperties()
    */
   public Collection<IPropertySource> getProperties()
   {
      // XXX Auto-generated method stub
      return null;
   }

}
