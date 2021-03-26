/*******************************************************************************
 *
 * [ IPSErrorCode.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client.error;

public interface IPSErrorCode
{
   /**
    * Returns an integer representation of the error code.
    * @return A value >= 0.
    */
   public int getCodeAsInt();
   
   public String getCodeAsString();
}
