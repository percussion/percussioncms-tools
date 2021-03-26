/*******************************************************************************
 *
 * [ IPSReferenceable.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;

/**
 * A simple interface that allows a design object handle to be associated with
 * an object.
 *
 * @author paulhoward
 */
public interface IPSReferenceable
{
   /**
    * Adds the supplied reference to this object.
    * 
    * @param ref May be <code>null</code> to clear the association.
    */
   public void setReference(IPSReference ref);
   
   /**
    * Returns the handle currently associated with this object.
    * 
    * @return May be <code>null</code> if there isn't a current association set.
    */
   public IPSReference getReference();
}
