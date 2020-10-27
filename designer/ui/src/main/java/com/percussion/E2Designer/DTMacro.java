/*[ DTMacro.java ]*************************************************************
 *
 * COPYRIGHT (c) 2004 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSMacro;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * The data type info used for macros.
 */
public class DTMacro extends AbstractDataTypeInfo
{
   /**
    * Create a new macro for the supplied name.
    * 
    * @param name the name of the macro to be created, not <code>null</code> or
    *    empty.
    * @return the newly created macro, never <code>null</code>.
    */
   public Object create(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");
         
      name = name.trim();
      if (name.length() == 0)
         throw new IllegalArgumentException("name cannot be empty");
      
      Iterator macros = CatalogMacros.getMacros(null, false);
      while (macros.hasNext())
      {
         PSMacro macro = (PSMacro) macros.next();
         if (macro.getName().equalsIgnoreCase(name))
         {
            PSMacro newMacro = new PSMacro(name);
            
            return newMacro;
         }
      }
      
      throw new IllegalArgumentException(
         "no macro definition found for supplied name");
   }

   /**
    * Catalogs all currently defined macros or gets the cache catalog.
    * 
    * @return an enumeration of macro names as <code>String</code> objects.
    *    Never <code>null</code>, may be empty.
    */
   public Enumeration catalog()
   {
      return CatalogMacros.getMacros(false);
   }
}
