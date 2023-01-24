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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package com.percussion.client;

/**
 * Exception thrown when logon to server fails for any reason. All the ctors in
 * this somply invoke the base class version.
 */
public class PSLogonException extends Exception
{
   private static final long serialVersionUID = 1L;

   /**
    * @see Exception#Exception()
    */
   public PSLogonException()
   {
      super();
   }

   /**
    * @see Exception#Exception(java.lang.String)
    */
   public PSLogonException(String message)
   {
      super(message);
   }

   /**
    * @see Exception#Exception(java.lang.String, java.lang.Throwable)
    */
   public PSLogonException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * @see Exception#Exception(java.lang.Throwable)
    */
   public PSLogonException(Throwable cause)
   {
      super(cause);
   }
}
