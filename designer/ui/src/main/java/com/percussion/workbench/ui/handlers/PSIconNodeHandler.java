/******************************************************************************
 *
 * [ PSIconNodeHandler.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.IPSDropHandler;

import java.util.Properties;

/**
 * Ignores all declarative settings, disabling all <code>supportsXXX</code>
 * functionality as well as any specified allowed types (supplied in ctor.)
 * 
 * @author paulhoward
 */
public class PSIconNodeHandler extends PSDeclarativeNodeHandler
{
   /**
    * Required by framework. See base for params.
    */
   public PSIconNodeHandler(Properties props, String iconPath,
         PSObjectType[] allowedTypes)
   {
      super(props, iconPath, allowedTypes);
   }

   /**
    * Must be overridden because the allowedTypes passed into the ctor may not
    * be empty.
    */
   @Override
   public IPSDropHandler getDropHandler()
   {
      return null;
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsCopy(PSUiReference node)
   {
      return false;
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsDelete(PSUiReference node)
   {
      return false;
   }

   /**
    * Returns <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   protected boolean supportsDeleteAssociation(PSUiReference node)
   {
      return false;
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsPaste(PSUiReference node)
   {
      return false;
   }

   /**
    * @inheritDoc
    * 
    * @return Always <code>false</code>.
    */
   @SuppressWarnings("unused")
   @Override
   public boolean supportsRename(PSUiReference ref)
   {
      return false;
   }
}
