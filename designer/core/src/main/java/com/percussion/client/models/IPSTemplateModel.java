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

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;

import java.util.Set;

/**
 * Assembly template model.
 *
 * @author Andriy Palamarchuk
 */
public interface IPSTemplateModel extends IPSCmsModel
{
   /**
    * Returns the content types associated with the template.
    * @param templateRef the template to return content types for.
    * Never <code>null</code>. 
    * @param force whether to force data loading.
    * @return collection of the content types. Is empty if no associated content
    * types are found. 
    */
    Set<IPSReference> getContentTypes(
         final IPSReference templateRef, final boolean force)
         throws PSModelException;
}
