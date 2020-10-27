/******************************************************************************
 *
 * [ IPSObjectDefaulter.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models;

/**
 * This interface is used by the CMS model framework to allow the client to
 * configure a newly created object before an notifications of creation are 
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
   public void modify(Object data);
}
