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

import com.percussion.client.PSModelChangedEvent;

/**
 * Classes that want to register for notifications for changes in a model must
 * implement this interface and pass it as the listener when registering.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public interface IPSModelListener
{
   /**
    * Whenever a significant event occurs (after successful complete,) this
    * method will be called on registered listeners.
    *  
    * @param event Identifies who caused the event and what the event was. 
    * Never <code>null</code>.
    */
   void modelChanged(PSModelChangedEvent event);
}
