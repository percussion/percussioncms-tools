/******************************************************************************
 *
 * [ PSUiErrorCodes.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.error.IPSErrorCode;

/**
 * This enumeration contains all error codes for the workbench. Messages keyed
 * by these codes should be placed in the psmessages.properties file. All
 * exceptions should use these codes and extend the
 * {@link com.percussion.client.error.PSClientException} class.
 * 
 * @author paulhoward
 */
public enum PSUiErrorCodes implements IPSErrorCode
{
   /**
    * The operation required a property but one was not supplied. The property
    * must have one of a small set of values.
    * 
    * <table>
    *   <th>
    *      <td>Parameter #</td>
    *      <td>Name</td>
    *      <td>Description</td>
    *   </th>
    *   <tr>
    *      <td>0</td>
    *      <td>propName</td>
    *      <td>The name of the expected property.</td>
    *   </tr>
    *   <tr>
    *      <td>1</td>
    *      <td>foundPropValue</td>
    *      <td>The incorrect value that was found.</td>
    *   </tr>
    *   <tr>
    *      <td>2</td>
    *      <td>allowedValues</td>
    *      <td>A comma seperated list of the allowed values.</td>
    *   </tr>
    * </table>    
    */
   MISSING_PROPERTY_WITH_VALUES(30400),
   
   /**
    * The operation requires a template ancestor node, but one was not found. 
    * 
    * <table>
    *   <th>
    *      <td>Parameter #</td>
    *      <td>Name</td>
    *      <td>Description</td>
    *   </th>
    *   <tr>
    *      <td>0</td>
    *      <td>startingPointPath</td>
    *      <td>The full path of the node where the search began.</td>
    *   </tr>
    *   <tr>
    *      <td>1</td>
    *      <td>objectType</td>
    *      <td>The name of the object type being searched for.</td>
    *   </tr>
    * </table>    
    */
   ANCESTOR_NOT_FOUND(30401);
   
   
   //see interface
   public int getCodeAsInt()
   {
      return m_errorCode;
   }

   //see interface
   public String getCodeAsString()
   {
      return String.valueOf(m_errorCode);
   }
   
   /**
    * Overrides default ctor to store a specified int for each code.
    * @param errorCode
    */
   private PSUiErrorCodes(int errorCode)
   {
      m_errorCode = errorCode;
   }
   
   /**
    * The assigned numeric value (or code) for this error.
    */
   private int m_errorCode;
}
