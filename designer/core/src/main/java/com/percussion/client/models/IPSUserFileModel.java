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
package com.percussion.client.models;

import com.percussion.client.PSModelException;
import com.percussion.services.ui.data.PSHierarchyNode;

import java.util.Collection;

/**
 * A custom extension is provided for USER_FILE to give extra cataloging 
 * functionality.
 *
 * @author paulhoward
 */
public interface IPSUserFileModel extends IPSCmsModel
{
   /**
    * Gets the data object for all placeholders that exist in the requested 
    * tree.
    *  
    * @param treeName Never <code>null</code> or empty.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSModelException If any problems communicating with the server.
    * It may wrap a PSMultiOperationException.
    */
 Collection<PSHierarchyNode> getDescendentPlaceholders(
         String treeName)
      throws PSModelException;
}
