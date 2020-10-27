/******************************************************************************
 *
 * [ TestHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.handlers.PSDeclarativeNodeHandler;

import java.util.Properties;

/**
 * Provided for unit test purposes only.
 *
 * @author paulhoward
 */
public class TestHandler extends PSDeclarativeNodeHandler
{

   public TestHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

}
