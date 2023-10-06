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

import java.util.Collection;

/**
 * Adds some special cataloging functionality.
 * 
 * @author ram
 */
public interface IPSWorkflowModel extends IPSCmsModel
{
   /**
    * Get the roles that are members fo the supplied workflow reference.
    * 
    * @param wfRef workflow reference whose member roles are sought, may be
    * <code>null</code> in which case roles from all workflows are returned.
    * @return set of roles from the supplied or all workflow(s), never
    * <code>null</code> mat be empty.
    * @throws PSModelException
    */
   Collection<IPSReference> getWorkflowRoles(IPSReference wfRef)
      throws PSModelException;
}
