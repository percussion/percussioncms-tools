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

import com.percussion.client.PSErrorCodes;
import com.percussion.client.error.IPSErrorCode;
import com.percussion.client.error.PSClientException;

/**
 * This exception is used when a problem associated with locking a secure-able
 * object for single access fails. More specifically, the following two
 * scenarios may generate it:
 * <ol>
 * <li>An attempt is made to perform an operation that requires the caller to
 * acquire the lock, but it is already locked by someone else.</li>
 * <li>An attempt is made to perform an operation on an object that requires a
 * lock but the caller did not own the lock.</li>
 * <ol>
 * <p>
 * The class is immutable.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSLockException extends PSClientException
{
   /**
    * Convenience constructor that calls {@link #PSLockException(String, 
    * String, String, String) PSLockException(operation, objectType, 
    * objectName, null)}.
    */
   public PSLockException(String operation, String objectType, 
         String objectName)
   {
      this(operation, objectType, objectName, null);
   }
   
   /**
    * Convenience constructor that calls {@link #PSLockException(String, 
    * String, String, String, Throwable) PSLockException(operation, objectType, 
    * objectName, who, null)}.
    */
   public PSLockException(String operation, String objectType, 
         String objectName, String who)
   {
      this(operation, objectType, objectName, who, null);
   }
   
   /**
    * Ctor to use when the reason was the object was locked by someone else
    * so the operation couldn't proceed.
    * 
    * @param operation a string representation of the operation that failed
    *    because the object was locked by someone else. If
    *    <code>null</code> or empty, {@code "<unknown operation>"} will be used.
    * @param objectType a string representation of the object type that failed
    *    the operation because it was locked by someone else. If
    *    <code>null</code> or empty, {@code "<unknown type>"} will be used.
    * @param objectName the internal name of the object. If <code>null</code>
    *    or empty, {@code "<unknown name>"} will be used.
    * @param who the login id of the subject that currently owns the lock. If
    *    <code>null</code> or empty, {@code "<unknown user>"} will be used.
    * @param cause the underlying exception which cause this exception to be 
    *    thrown, may be <code>null</code>.
    */
   public PSLockException(String operation, String objectType, 
      String objectName, String who, Throwable cause)
   {
      super(cause);
      
      buildMessage(operation, objectType, objectName, who, who != null);
   }
   
   /**
    * Validates all the arguments and calls the base class to do the work.
    *  
    * @param operation See the 4 param
    * {@link #PSLockException(String, String, String, String) ctor} for
    * description.
    * 
    * @param objectType See the 4 param
    * {@link #PSLockException(String, String, String, String) ctor} for
    * description.
    * 
    * @param objectName See the 3 param
    * {@link #PSLockException(String, String, String, String) ctor} for
    * description.
    * 
    * @param who See the 4 param
    * {@link #PSLockException(String, String, String, String) ctor} for
    * description. Only used if <code>locked</code> is <code>true</code>.
    * 
    * @param locked Used to select the message. If <code>true</code>, the
    * message indicating that the operation failed because the object was locked
    * by someone else is used, otherwise the message indicating object wasn't
    * locked but needed to be.
    */
   private void buildMessage(String operation, String objectType, 
         String objectName, String who, boolean locked)
   {
      String[] args = new String[locked ? 4 : 3];

      int argIndex = 0;
      if (operation == null || operation.trim().length() == 0)
         args[argIndex] = "<unknown operation>";
      else
         args[argIndex] = operation.trim();

      argIndex++;
      if (objectType == null || objectType.trim().length() == 0)
         args[argIndex] = "<unknown type>";
      else
         args[argIndex] = objectType.trim();

      argIndex++;
      if (objectName == null || objectName.trim().length() == 0)
         args[argIndex] = "<unknown name>";
      else
         args[argIndex] = objectName.trim();
      
      IPSErrorCode errorCode;
      if (locked)
      {
         argIndex++;
         if (who == null || who.trim().length() == 0)
            args[argIndex] = "<unknown user>";
         else
            args[argIndex] = who.trim();
         errorCode = PSErrorCodes.SECURED_OBJECT_ALREADY_LOCKED;
      }
      else
         errorCode = PSErrorCodes.SECURED_OBJECT_NOT_LOCKED;
      super.initDetails(errorCode, args);
   }
   
   /**
    * Support serialization.
    */
   private static final long serialVersionUID = 1L;
}
