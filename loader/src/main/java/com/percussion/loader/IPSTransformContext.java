/*[ IPSTransformContext.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.PSItemField;

/**
 * Objects implementinmg this interface will be passed on to item transformers
 * as context information needed to do the transformation. 
 */
public interface IPSTransformContext
{
   public PSItemField getItemField();
}
