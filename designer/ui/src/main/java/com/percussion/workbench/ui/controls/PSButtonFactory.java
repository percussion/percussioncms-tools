/******************************************************************************
*
* [ PSButtonFactory.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.controls;


import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Factory class that allows the easy creation of some common
 * imaged buttons. The buttons take care of disposing their own
 * images.
 */
public class PSButtonFactory
{

   private PSButtonFactory()
   {
      
   }
   
   /**
    * Creates a button with a green plus sign image
    * @return the button, never <code>null</code>.
    */
   public static Button createAddButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_ADD);
   }
   
   /**
    * Creates a button with a pencil image.
    * @return the button, never <code>null</code>.
    */
   public static Button createEditButton(Composite parent)
   {
      return createImagedPushButton(parent, IPSUiConstants.IMAGE_EDIT);
   }
   
   /**
    * Creates a button with a red X image
    * @return the button, never <code>null</code>.
    */
   public static Button createDeleteButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_DELETE);
   }
   
   /**
    * Creates a button with an up arrow image
    * @return the button, never <code>null</code>.
    */
   public static Button createUpButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_TRIANGLE_UP);
   }
   
   /**
    * Creates a button with an down arrow image
    * @return the button, never <code>null</code>.
    */
   public static Button createDownButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_TRIANGLE_DOWN);
   }
   
   /**
    * Creates a button with an left arrow image
    * @return the button, never <code>null</code>.
    */
   public static Button createLeftButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_TRIANGLE_LEFT);
   }
   
   /**
    * Creates a button with an right arrow image
    * @return the button, never <code>null</code>.
    */
   public static Button createRightButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_TRIANGLE_RIGHT);
   }
   
   /**
    * Creates a button with an left double arrow image
    * @return the button, never <code>null</code>.
    */
   public static Button createDoubleLeftButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_TRIANGLE_DOUBLE_LEFT);
   }
   
   /**
    * Creates a button with an left double arrow image
    * @return the button, never <code>null</code>.
    */
   public static Button createDoubleRightButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_TRIANGLE_DOUBLE_RIGHT);
   }
   
   /**
    * Creates a button with a back navigation image, 
    * a yellow left pointing arrow.
    * @return the button, never <code>null</code>.
    */
   public static Button createBackButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_NAV_BACK);
   }
   
   /**
    * Creates a button with a forward navigation image, 
    * a yellow right pointing arrow.
    * @return the button, never <code>null</code>.
    */
   public static Button createForwardButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_NAV_FORWARD);
   }
   
   /**
    * Creates a button with a green plus sign image
    * @return the button, never <code>null</code>.
    */
   public static Button createAddChildButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_ADD_CHILD);
   }
   
   /**
    * Creates a button with claendar image
    * @return the button, never <code>null</code>.
    */
   public static Button createCalendarButton(Composite parent)
   {
      return createImagedPushButton(parent,
         IPSUiConstants.IMAGE_CALENDAR);
   }

   /**
    * Creates the imaged button, the button disposes the image when it
    * is disposed
    * @param parent assumed not <code>null</code>.
    * @param image assumed not <code>null</code>.
    * @return the newly created button, never <code>null</code>.
    */
   private static Button createImagedPushButton(Composite parent, String image)
   {
      Button button = new Button(parent, SWT.PUSH);
      ImageDescriptor desc = PSUiUtils.getImageDescriptorFromIconsFolder(image);
      if(desc == null)
         throw new IllegalArgumentException(
            "image must be pointing to an existing image.");
      final Image theImage = desc.createImage();
      button.setImage(theImage);
      //Add a dispose listener to auto dispose the image
      button.addDisposeListener(new DisposeListener()
         {
            @SuppressWarnings("unused")
            public void widgetDisposed(DisposeEvent e)
            {
               theImage.dispose();               
            }
         
         });
      return button;
   }

}
