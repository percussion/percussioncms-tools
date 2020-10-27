/******************************************************************************
 *
 * [ PSContentEditorControlModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#CONTENT_EDITOR_CONTROLS}. Uses base class
 * implementation whenever possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSContentEditorControlModelProxy extends
   com.percussion.client.proxies.impl.PSContentEditorControlModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#CONTENT_EDITOR_CONTROLS} and for primary type.
    */
   public PSContentEditorControlModelProxy()
   {
      super();
   }
}
