/******************************************************************************
 *
 * [ PSNodeReferenceDecorator.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.decorators;

import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * This decorator places a small arrow on the design object nodes that represent
 * references to the design object rather than the actual design object.
 *
 * @author paulhoward
 */
public class PSNodeReferenceDecorator extends LabelProvider implements
      ILightweightLabelDecorator
{
   //see interface
   public void decorate(Object element, IDecoration decoration)
   {
      if (ms_missing || !(element instanceof PSUiReference))
         return;
      
      PSUiReference node = (PSUiReference) element;
      if (!node.isReference())
         return;
      
      String ICON_PATH = "icons/decorators/nodeRef.gif";
      ImageDescriptor desc = JFaceResources.getImageRegistry().getDescriptor(
            ICON_PATH);
      if (desc == null)
      {
         desc = PSWorkbenchPlugin.getImageDescriptor(ICON_PATH);
         if (desc == null)
         {
            ms_missing = true;
            PSWorkbenchPlugin.getDefault().log(
                  "Couldn't find image for node reference decorator: "
                        + ICON_PATH);
            return;
         }
         JFaceResources.getImageRegistry().put(ICON_PATH, desc);
      }
      decoration.addOverlay(desc);
   }

   /**
    * A flag to indicate we couldn't find the image file so we don't keep trying
    * hundreds of times. Defaults to <code>false</code>.
    */
   private static boolean ms_missing = false;
}
