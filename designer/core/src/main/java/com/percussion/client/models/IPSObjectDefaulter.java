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

/**
 * This interface is used by the CMS model framework to allow the client to
 * configure a newly created object before a notifications of creation are
 * sent.
 *
 * @author paulhoward
 */
public interface IPSObjectDefaulter
{
   /**
    * Sets desired values onto the supplied object.
    * 
    * @param data Must be of the type expected by the implementation. Never 
    * <code>null</code>.
    */
   void modify(Object data);
}
