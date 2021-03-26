/******************************************************************************
 *
 * [ PSUiObjectTypes.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSObjectType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the object types that may be used with
 * {@link com.percussion.workbench.ui.PSUiReference PSUiReference}. They
 * represent objects that are created in memory, but never persisted to the
 * server.
 * 
 * @author paulhoward
 * @version 6.0
 */
public enum PSUiObjectTypes implements IPSPrimaryObjectType
{
   placeholder;
   
   //see interface for details
   public boolean isFileType()
   {
      return false;
   }
   
// see interface for details
   public boolean supportsAcls()
   {
      return false;
   }

   //see interface for details
   public boolean hasSubTypes()
   {
      return getSubTypeValues().length > 0;
   }

   //see interface for details
   public Set<PSObjectType> getTypes()
   {
      Set<PSObjectType> results = new HashSet<PSObjectType>();
      if (hasSubTypes())
      {
         for (Enum subType : getSubTypeValues())
         {
            results.add(new PSObjectType(this, subType));
         }
      }
      else
         results.add(new PSObjectType(this, null));
      return Collections.unmodifiableSet(results);
   }

   //see interface for details
   public boolean isAllowedType(Enum subType)
   {
      for (Enum existingType : getSubTypeValues())
      {
         if (existingType == subType)
            return true;
      }
      return false;
   }

   /**
    * Default behavior is to return an empty array. Types with sub-types must
    * override this method and return all the sub type values. All other
    * methods in the interface are implemented based on this method.
    *  
    * @return Never <code>null</code>, default implementation is always empty.
    */
   protected Enum[] getSubTypeValues()
   {
      return new Enum[0];
   }
}
