/*******************************************************************************
 *
 * [ IPSHomeNodeManager.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.model;

import com.percussion.client.PSModelException;

import java.util.Map;

/**
 * This interface is used as part of the declarative hierarchy model. Within
 * the model, either exactly 1 node, or a set of nodes can be declared to be
 * the 'home' node for a type of design object. If more than 1 node is 
 * considered the home node, this class is used to determine which node
 * within the set is 'the' home node for any specific instance (generally based
 * on some property in the object.)
 * <p>
 * Instances of this class must have a no-parameter ctor.
 *
 * @author paulhoward
 */
public interface IPSHomeNodeManager
{
   /**
    * Using the supplied properties, generally obtained from the declarative
    * def of a home node, this method will compare them to the data object to
    * determine if they match.
    * 
    * @param props What properties (if any) are required depends on the
    * implementation.
    * 
    * @param data Never <code>null</code>. Must be of the correct class
    * needed by this manager.
    * 
    * @return <code>true</code> if the supplied data matches the supplied 
    * properties according to this manager, <code>false</code> otherwise.
    * 
    * @throws ClassCastException if <code>data</code> not of the correct type.
    */
   public boolean isHomeNode(Map<String,String> props, Object data);
   
   /**
    * This method is used to change the value of 1 or more properties in the
    * supplied object so that the supplied data can be considered a child of the
    * home node that has the supplied properties. After this method returns, if
    * the supplied data is passed to {@link #isHomeNode(Map, Object)},
    * it would return <code>true</code>.
    * 
    * @param props What properties (if any) are required depends on the
    * implementation.
    * 
    * @param data Never <code>null</code>. Must be of the correct class
    * needed by this manager.
    * 
    * @throws PSModelException If the conversion cannot be done because of 
    * missing properties.
    * 
    * @throws ClassCastException if <code>data</code> not of the correct type.
    */
   public void modifyForHomeNode(Map<String,String> props, Object data) 
      throws PSModelException;
}
