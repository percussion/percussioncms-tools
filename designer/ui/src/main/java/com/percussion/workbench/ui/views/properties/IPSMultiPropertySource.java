/******************************************************************************
 *
 * [ IPSMultiPropertySource.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.properties;

import com.percussion.client.PSObjectTypes;
import org.eclipse.ui.views.properties.IPropertySource;

import java.util.Collection;

/**
 * Similar to Eclipse' <code>IPropertySource</code>, except it works on
 * multiple objects simultanously. For use with views that render properties in
 * a table.
 * 
 * @version 6.0
 * @author paulhoward
 */
public interface IPSMultiPropertySource
{

   /**
    * Retrieves identifiers for each property that is currently enabled.
    * Properties are enabled/disabled through preferences settings.
    * 
    * @return Each entry is an <code>IPropertySource</code> that contains the
    * properties for a single object. The
    * <code>IPropertyDescriptor<code>s returned
    * by this source are actually <code>IPSPropertyDescriptor</code>s.
    */
   public Collection<IPropertySource> getProperties();

   /**
    * If this set of properties is of a single type, that type is returned,
    * otherwise, <code>null</code> is returned.
    */
   public PSObjectTypes getObjectType();

}
