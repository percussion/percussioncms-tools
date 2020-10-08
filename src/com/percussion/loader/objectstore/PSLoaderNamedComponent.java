/*[ PSLoaderNamedComponent.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;



/**
 * As a sub group of <code>PSLoaderComponent</code> classes, the derived
 * classes has to implement set and get name methods.
 */
public abstract class PSLoaderNamedComponent extends PSLoaderComponent
{
   /**
    * Set the component name from the supplied name.
    *
    * @param name The to be set name. It may not be <code>null</code>.
    */
   public abstract void setName(String name);

   /**
    * Get the component name.
    *
    * @return the name of the component. It may not be <code>null</code>.
    */
   public abstract String getName();
}
