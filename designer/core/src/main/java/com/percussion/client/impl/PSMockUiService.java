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
package com.percussion.client.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.ui.IPSUiService;
import com.percussion.services.ui.PSUiException;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.ui.data.PSHierarchyNode.NodeType;
import com.percussion.services.ui.data.PSHierarchyNodeProperty;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * This class only implements a single method,
 * {@link #createHierarchyNodeProperty(String, String, IPSGuid)} that is needed
 * by the {@link com.percussion.services.ui.data.PSHierarchyNode} object. It
 * behaves just like the server side implementation for that method. All other
 * methods throw {@link java.lang.UnsupportedOperationException}.
 * 
 * @author paulhoward
 */
public class PSMockUiService implements IPSUiService
{

   public PSMockUiService()
   {
   }

   @SuppressWarnings("unused")
   public PSHierarchyNode createHierarchyNode(String name, IPSGuid parentId,
         NodeType type)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Works just like the real one.
    * 
    * @return Never <code>null</code>.
    */
   public PSHierarchyNodeProperty createHierarchyNodeProperty(String name,
         String value, IPSGuid nodeId)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      if (nodeId == null)
         throw new IllegalArgumentException("nodeId annot be null");
      
      if (!(nodeId.getType() == PSTypeEnum.HIERARCHY_NODE.getOrdinal()))
         throw new IllegalArgumentException(
            "nodeId must be of type hierarchy node");
      
      PSHierarchyNodeProperty property = new PSHierarchyNodeProperty(name, 
         value, nodeId);
      
      return property;
   }

   @SuppressWarnings("unused")
   public List<PSHierarchyNode> findHierarchyNodes(String path, 
      PSHierarchyNode.NodeType type)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   public List<PSHierarchyNode> findHierarchyNodes(String name,
      IPSGuid parentId, PSHierarchyNode.NodeType type)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   public PSHierarchyNode loadHierarchyNode(IPSGuid id) throws PSUiException
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   public void saveHierarchyNode(PSHierarchyNode node)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   public void deleteHierarchyNode(IPSGuid id)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   public void removeChildren(IPSGuid parentId, List<IPSGuid> ids)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
   public void moveChildren(IPSGuid sourceId, IPSGuid targetId,
         List<IPSGuid> ids)
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings("unused")
    public List<PSHierarchyNode> getAllHierarchyNodes()
    {
        throw new UnsupportedOperationException();
    }
    
   @SuppressWarnings("unused")
    public List<PSHierarchyNodeProperty> getAllHierarchyNodesGuidProperties()
    {
        throw new UnsupportedOperationException();
    }
}
