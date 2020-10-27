/******************************************************************************
 *
 * [ PSContentTypeChildTabComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class PSContentTypeChildTabComposite extends Composite
{

   public PSContentTypeChildTabComposite(Composite parent, int style) 
   {
      super(parent, style);
      setLayout(new FormLayout());
      //
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }

}
