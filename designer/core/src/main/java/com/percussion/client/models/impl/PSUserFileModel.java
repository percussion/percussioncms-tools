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
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSUserFileModel;
import com.percussion.client.proxies.impl.PSUserFileHierarchyModelProxy;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.Collection;

/**
 * Straight implementation of the interface.
 * 
 * @author paulhoward
 */
public class PSUserFileModel extends PSCmsModel implements IPSUserFileModel
{
   /**
    * Ctor needed for base class. See
    * {@link PSCmsModel#PSCmsModel(String, String, IPSPrimaryObjectType) base ctor}
    * for details.
    */
   public PSUserFileModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   // see interface
   public Collection<PSHierarchyNode> getDescendentPlaceholders(String treeName)
      throws PSModelException
   {
      PSUserFileHierarchyModelProxy proxy;
      proxy = new PSUserFileHierarchyModelProxy();
      return proxy.getDescendents("/" + treeName + "/*");
   }
}
