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
public interface IPSCommunityModel extends IPSCmsModel
{
   /**
    * Get the roles that are members fo the supplied community reference.
    * 
    * @param commRef community reference whose member roles are sought, may be
    * <code>null</code> in which case roles from all communities are returned.
    * @return set of roles from the supplied or all communities, never
    * <code>null</code> mat be empty.
    * @throws PSModelException
    */
   Collection<IPSReference> getCommunityRoles(IPSReference commRef)
      throws PSModelException;
}
