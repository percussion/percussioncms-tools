/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * The deployable element : TEMPLATE for packaging
 * @author vamsinukala
 *
 */
public class PSTemplateDependencyHandler extends PSElementDependencyHandler
{

   /**
    * Construct the dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSTemplateDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   // see base class
   protected PSDependencyHandler getChildHandler()
   {
      if (m_childHandler == null)
         m_childHandler = getDependencyHandler(
            PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);

      return m_childHandler;
   }

   // see base class
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;   
   }
   
   
   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = "sys_Templates";

   
   /**
    * The content list definition handler, initialized by
    * <code>getChildHandler()</code> if it is <code>null</code>, will never
    * be <code>null</code> after that.
    */
   private PSDependencyHandler m_childHandler = null;

   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   static
   {
      ms_childTypes.add(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
   }
}