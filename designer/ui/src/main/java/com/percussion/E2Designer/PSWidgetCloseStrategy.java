/*******************************************************************************
 *
 * [ PSXmlApplicationEditor.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

/**
 * Performs UI element closing.
 *
 * @author Andriy Palamarchuk
 */
public interface PSWidgetCloseStrategy
{
   
   /**
    * Performs the closing operation.
    * Returns <code>true</code> if the closing operation was successful,
    * <code>false</code> if it was cancelled.
    */
   boolean onClose();

}
