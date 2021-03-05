/*[ PSTransformContext.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.PSItemField;

/**
 * Objects implementinmg this interface will be passed on to item transformers
 * as context information needed to do the transformation.
 */
public class PSTransformContext implements IPSTransformContext
{

   /**
    * Set the item field for this object.
    * 
    * @param itemField The to be set item field object, may not be 
    *    <code>null</code>
    */
   public void setItemField(PSItemField itemField)
   {
      if (itemField == null)
         throw new IllegalArgumentException("itemField may not be null");
         
      m_itemField = itemField;
   }

   /**
    * Get the item field object.
    * 
    * @return The item field object, never <code>null</code>.
    * 
    * @throws IllegalStateException if the item field object has not been set.
    */
   public PSItemField getItemField()
   {
      if (m_itemField == null)
         throw new IllegalStateException("itemField has not been set yet");
      
      return m_itemField;
   }

   /**
    * The item field object, may be <code>null</code> if not set.
    */
   private PSItemField m_itemField = null;
}
