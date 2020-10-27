/******************************************************************************
 *
 * [ CatalogMacros.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.macro.PSMacroCatalogHandler;
import com.percussion.design.objectstore.PSMacro;
import com.percussion.design.objectstore.PSMacroDefinition;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Catalogs all macros available from the server.
 */
public class CatalogMacros implements Serializable
{
   /**
    * Do not construct new instances. Use the static function.
    */
   private CatalogMacros()
   {
   }
   
   /**
    * Get an enumeration over all known macro names.
    * 
    * @param forceCatalog <code>true</code> to force a new catalog request,
    *    <code>false</code> to use the cached catalog.
    * @return an enumeration over all macro names, never <code>null</code>, may
    *    be eempty.
    */
   public static Enumeration getMacros(boolean forceCatalog)
   {
      getMacros(null, forceCatalog);
      
      Vector<String> macroNames = new Vector<String>(ms_macros.keySet());
      return macroNames.elements();
   }

   /**
    * Returns an iterator over a list of macros.
    *
    * @param connection the connection to the server, if <code>null</code> will
    *    obtain the connection using the 
    *    <code>E2Designer.getApp().getMainFrame().getDesignerConnection()</code>
    *    method.
    * @param forceCatalog if <code>true</code> then ignores the cached data
    *    and performs a fresh catalog (the cached data is updated with the 
    *    result of this catalog), otherwise uses the cached data if possible. 
    * @return an iterator over a list of macros, never <code>null</code>,
    *    may be empty
    */
   public static Iterator getMacros(PSDesignerConnection connection, 
      boolean forceCatalog)
   {
      try
      {
         if (connection == null)
         {
            connection = E2Designer.getDesignerConnection();
         }

         if (ms_macros == null || forceCatalog)
         {
            ms_macros = new HashMap<String, PSMacro>();

            Iterator macros = PSMacroCatalogHandler.getMacros(
               new PSCataloger(connection)).iterator();
            while (macros.hasNext())
            {
               PSMacroDefinition macro = (PSMacroDefinition) macros.next();
               
               ms_macros.put(macro.getName(), new PSMacro(macro.getName()));
            }
         }
      }
      catch (Exception e)
      {
         handleException(e);
      }

      return ms_macros.values().iterator();
   }

   /**
    * A common method to print message to user and call stack to log. The method
    * returns after the user dismisses the dialog.
    *
    * @param e the exception or error that occurred, assumed not
    *    <code>null</code>.
    */
   private static void handleException(Exception e)
   {
      PSDlgUtil.showErrorDialog(
         e.getLocalizedMessage(),
         E2Designer.getResources().getString("CatalogerExceptionTitle"));
   }

   /**
    * Map to cache the cataloged macros, the macro name as <code>String</code> 
    * is used for the key while the value is the <code>PSMacro</code> 
    * object. Initialized on the first call to 
    * {@link getMacros(PSDesignerConnection, boolean)}, updated on each call
    * to {@link getMacros(PSDesignerConnection, true)}.
    */
   private static Map<String, PSMacro> ms_macros;
}
