/******************************************************************************
 *
 * [ PSContentTypePropertyFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.properties.impl.factories;

import com.percussion.client.IPSReference;
import com.percussion.workbench.ui.views.properties.IPSPropertySourceFactory;
import org.eclipse.ui.views.properties.IPropertySource;

import java.util.Collection;

/**
 * Maintains a staic registry for classes that know what properties to display
 * and how to retrieve them from the corresponding object.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSContentTypePropertyFactory implements IPSPropertySourceFactory
{

   public PSContentTypePropertyFactory()
   {

   }

   /**
    * @param references Never <code>null</code>, may be empty.
    * 
    * @return An {@link IPropertySource} for each supplied object. Never
    * <code>null</code>, may be empty if no objects are provided.
    */
   public Collection<IPropertySource> getPropertySource(
         Collection<IPSReference> references)
   {
      return null;
   }

}
