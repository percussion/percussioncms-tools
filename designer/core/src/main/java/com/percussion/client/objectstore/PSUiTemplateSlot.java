/*******************************************************************************
 *
 * [ PSUiTemplateSlot.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;
import com.percussion.services.assembly.data.PSTemplateSlot;

import java.io.Serializable;

/**
 * This class adds serializable capability.
 *
 * @author paulhoward
 */
public class PSUiTemplateSlot extends PSTemplateSlot implements Serializable,
   IPSReferenceable, Cloneable
{
   @Override
   public Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }

   //see interface
   public void setReference(IPSReference ref)
   {
      if ( null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      m_ref = ref;
   }

   //see interface
   public IPSReference getReference()
   {
      return m_ref;
   }

   //todo
   private IPSReference m_ref;

   /**
    * Unique id for serialization.
    */
   private static final long serialVersionUID = 1L;
}
