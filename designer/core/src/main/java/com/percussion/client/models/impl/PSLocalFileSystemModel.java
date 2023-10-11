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
import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSLocalFileSystemModel;
import com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyNodeRef;

import java.io.File;

/**
 * Model for {@link PSObjectTypes#LOCAL_FILE} objects.
 *
 * @author Andriy Palamarchuk
 */
public class PSLocalFileSystemModel extends PSCmsModel
      implements IPSLocalFileSystemModel
{

   /**
    * The constructor required by the model contract.
    */
   public PSLocalFileSystemModel(String name, String description,
         IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   // see base
   public File getFile(IPSReference reference)
   {
      if (reference instanceof PSLocalFileSystemHierarchyNodeRef) {
         return ((PSLocalFileSystemHierarchyNodeRef) reference).getFile();
      }else{
         throw new IllegalArgumentException();
      }
   }
}
