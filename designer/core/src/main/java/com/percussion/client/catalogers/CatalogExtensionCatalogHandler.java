/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.client.catalogers;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.catalog.exit.PSExtensionHandlerCatalogHandler;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionRef;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * returns the name of the installed exit handlers
 */
public class CatalogExtensionCatalogHandler
{
   private CatalogExtensionCatalogHandler()
   {

   }

   public static Vector<PSExtensionRef> getCatalog(PSDesignerConnection connection,
      boolean forceCatalog)
   {
      return getCatalog(connection, false, forceCatalog);
   }

   /**
    * The first time this method is called, it queries the server for a list of
    * all installed extension handlers. The list is cached and returned on
    * successive calls that don't have the forceCatalog flag set.
    * 
    * @param connection A valid connection to a Rx server.
    * 
    * @param scriptableOnly If <code>true</code>, only handlers that are
    * scriptable are returned. Otherwise, all handlers are returned.
    * 
    * @param forceCatalog if <code>true</code> re-catalog the data, else use
    * cached data
    * 
    * @return A collection containing 0 or more names of the installed
    * extensions as PSExtensionRef instances.
    */
   public static Vector<PSExtensionRef> getCatalog(PSDesignerConnection connection,
      boolean scriptableOnly, boolean forceCatalog)
   {
      String key = scriptableOnly ? "s" : "ns";
      Vector<PSExtensionRef> catalog = m_data.get(key);
      if (null == catalog)
         catalog = new Vector<>(5);

      if (catalog.isEmpty() || forceCatalog)
      {
         try
         {
            catalog.clear();
            IPSExtensionDef[] defs = PSExtensionHandlerCatalogHandler
               .getCatalog(new PSCataloger(connection));

            for (IPSExtensionDef def : defs) {
               String scriptableValue = def
                       .getInitParameter(IPSExtensionDef.INIT_PARAM_SCRIPTABLE);
               if (!scriptableOnly
                       || (null != scriptableValue && scriptableValue
                       .equalsIgnoreCase("yes"))) {
                  catalog.add(def.getRef());
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      m_data.put(key, catalog);
      return (catalog);
   }

   /**
    * checks if the extension is installed on the server
    * 
    * @param connection the server to query
    * 
    * @param key the name to search for
    * 
    * @return <code> true </code> if extension is installed <code> false </code>
    * if not
    * 
    */

   public static boolean isCatalogHandlerInstalled(
      PSDesignerConnection connection, String key)
   {
      boolean bFound = false;
      Vector<PSExtensionRef> vc = getCatalog(connection, false);
         for (PSExtensionRef psExtensionRef : vc) {
            if (psExtensionRef.getExtensionName().equals(key)) {
               bFound = true;
               break;
            }
         }
      return bFound;
   }

   /**
    * The name of the standard extension handler for Java extensions.
    */
   public static final String JAVA_EXTENSION_HANDLER_NAME = "Java";

   /**
    * The name of the standard extension handler that processes JavaScript
    * extensions.
    */
   public static final String JAVA_SCRIPT_EXTENSION_HANDLER_NAME = "JavaScript";

   private static final Map<String, Vector<PSExtensionRef>> m_data =
      new HashMap<>();
}
