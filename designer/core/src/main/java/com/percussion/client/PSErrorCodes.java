/******************************************************************************
 *
 * [ PSErrorCodes.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.error.IPSErrorCode;

/**
 * This enumeration contains all error codes for the client core framework.
 * Messages keyed by these codes should be placed in the
 * ErrorMessages.properties file. All exceptions should use these codes and
 * extend the {@link com.percussion.client.error.PSClientException} class.
 * 
 * @author paulhoward
 */
public enum PSErrorCodes implements IPSErrorCode
{
   /**
    * This code is mainly for testing purposes. It just displays the first
    * parameter with no other text.
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>message</td>
    * <td>The text to display.</td>
    * </tr>
    * </table>
    */
   RAW(30000),

   /**
    * An attempt to lock an object that is locked by someone else.
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>operation</td>
    * <td>The name of the operation that was attempted. e.g. 'load'</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>object type</td>
    * <td>The string representation of the type (one of the
    * <code>PSObjectTypes</code>.)</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>object name</td>
    * <td>The internal name of the object.</td>
    * </tr>
    * <tr>
    * <td>3</td>
    * <td>who</td>
    * <td>The name of the subject that owns the locke.</td>
    * </tr>
    * </table>
    */
   SECURED_OBJECT_ALREADY_LOCKED(30001),

   /**
    * An attempt to perform an operation on an object that requires it to be
    * locked by the caller. The parameters are the same as
    * {@link #SECURED_OBJECT_ALREADY_LOCKED}, except there is no 'who'
    * parameter.
    */
   SECURED_OBJECT_NOT_LOCKED(30002),

   /**
    * An attempt to register an error message bundle failed because the
    * requested error code range overlapped with an already registered range.
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>minCode</td>
    * <td>minimum error code of existing, conflicting range</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>maxCode</td>
    * <td>maximum error code of existing, conflicting range</td>
    * </tr>
    * </table>
    */
   OVERLAPPING_ERROR_CODE_RANGE(30003),

   /**
    * Some part of the information supplied with a model registration is
    * incorrect. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfModel</td>
    * <td>Which one.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>className</td>
    * <td>Which class failed to load.</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>exceptionName</td>
    * <td>May make the error reason more clear.</td>
    * </tr>
    * <tr>
    * <td>3</td>
    * <td>reason</td>
    * <td>Why it failed.</td>
    * </tr>
    * </table>
    */
   INVALID_MODEL_REGISTRATION(30005),

   /**
    * Some part of the information supplied with a model registration is
    * incorrect. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfModel</td>
    * <td>What's it called.</td>
    * </tr>
    * </table>
    */
   UNKOWN_MODEL(30006),

   /**
    * Children are expected to have the same parent but have different parents.
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>parent1Name</td>
    * <td>Name of expected parent.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>parent2Name</td>
    * <td>Name the other parent, which is unexpected.</td>
    * </tr>
    * </table>
    */
   NOT_SAME_PARENT(30007),

   /**
    * Attempt to move a node to node which is a child of the node being moved
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>childName</td>
    * <td>Name of the node being moved.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>parentName</td>
    * <td>Name the target parent node.</td>
    * </tr>
    * </table>
    */
   CANT_MOVE_NODE_UNDER_ITSELF(30008),

   /**
    * Tree name does not exist for the name passed 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>treeName</td>
    * <td>Name of the tree being requested.</td>
    * </tr>
    * </table>
    */
   TREE_NAME_NOT_FOUND(30009),

   /**
    * An attempt to load a class by name failed for some reason. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>class name</td>
    * <td>Fully qualified name.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>context</td>
    * <td>Short message identifying why the class is being loaded.</td>
    * </tr>
    * </table>
    */
   LOAD_CLASS_FAILED(30010),
   
   /**
    * Object with given name already exists. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfObject</td>
    * <td>What's it called.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>objectType</td>
    * <td></td>
    * </tr>
    * </table>
    */
   DUPLICATE_NAME(30011),
   
   /**
    * Object can't be saved. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfObject</td>
    * <td>Object which was attempted to save.</td>
    * </tr>
    * </table>
    */
   SAVE_FAILED(30012),
   
   /**
    * Object can't be deleted. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfObject</td>
    * <td>Object which was attempted to delete.</td>
    * </tr>
    * </table>
    */
   DELETE_FAILED(30013),
   
   /**
    * Object can't be moved. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfObject</td>
    * <td>Object which was attempted to move.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>targetObject</td>
    * <td>Object to move to.</td>
    * </tr>
    * </table>
    */
   MOVE_FAILED(30014),
   
   /**
    * Client and server versions are not compatible. 
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>serverVersion</td>
    * <td>The version of the server that is not supported.</td>
    * </tr>
    * </table>
    */
   SERVER_VERSION_INVALID(30015),

   /**
    * The current operation can't be accomplished while the object being
    * operated upon is locked.
    * 
    * <table>
    * <th>
    * <td>Parameter #</td>
    * <td>Name</td>
    * <td>Description</td>
    * </th>
    * <tr>
    * <td>0</td>
    * <td>nameOfObject</td>
    * <td>The name of the object on which the operation was attempted, e.g.
    * move.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>opType</td>
    * <td>The operation that was attempted.</td>
    * </tr>
    * </table>
    */
   OPERATION_NOT_ALLOWED_WHILE_LOCKED(30016),

   /**
    * A connection to the server was refused because the server or the network
    * went down.
    * 
    * <table>
    * <th>
    *    <td>Parameter #</td>
    *    <td>Name</td>
    *    <td>Description</td>
    * </th>
    * <tr>
    *    <td>0</td>
    *    <td>cause</td>
    *    <td>The underlying error message.</td>
    * </tr>
    * </table>
    */
   NO_CONNECTION(30017),

   /**
    * An unknown webservice error occurred. Use this message for axis faults
    * which are not specifically converted.
    * 
    * <table>
    * <th>
    *    <td>Parameter #</td>
    *    <td>Name</td>
    *    <td>Description</td>
    * </th>
    * <tr>
    *    <td>0</td>
    *    <td>cause</td>
    *    <td>The underlying error message.</td>
    * </tr>
    * </table>
    */
   UNKNOWN_AXIS_FAULT(30018),

   /**
    * Can't access templates with the provided ids. This indicates either
    * data inconsistency, or the user does not have required permissions.
    *  
    * <table>
    * <th>
    *    <td>Parameter #</td>
    *    <td>Name</td>
    *    <td>Description</td>
    * </th>
    * <tr>
    *    <td>0</td>
    *    <td>ids</td>
    *    <td>Comma-separated list of the template ids, where each id is
    *    in format "<code>host-type-uuid (long value of the guid)</code>".</td>
    * </tr>
    * </table>
    */
   INACCESSIBLE_TEMPLATES(30019),

   /**
    * Can't access content types with the provided ids. This indicates either
    * data inconsistency, or the user does not have required permissions.
    *  
    * <table>
    * <th>
    *    <td>Parameter #</td>
    *    <td>Name</td>
    *    <td>Description</td>
    * </th>
    * <tr>
    *    <td>0</td>
    *    <td>cause</td>
    *    <td>Comma-separated list of the content type ids, where each id is
    *    in format "<code>host-type-uuid (long value of the guid)</code>".</td>
    * </tr>
    * </table>
    */
   INACCESSIBLE_CONTENT_TYPES(30020);
   
   
   // see interface for details
   public int getCodeAsInt()
   {
      return m_errorCode;
   }

   public String getCodeAsString()
   {
      return String.valueOf(m_errorCode);
   }

   private PSErrorCodes(int errorCode)
   {
      m_errorCode = errorCode;
   }

   /**
    * The assigned numeric value (or code) for this error.
    */
   private int m_errorCode;
}
