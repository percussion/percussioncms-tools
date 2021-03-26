/*[ UTEditorComponent.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.UTComponents;

import javax.swing.*;
import java.awt.event.ActionListener;


/**
 * The abstract class for all the editor components which should work with
 * {@link UTCellEditor } which can be used as table cell editor.
 */
public abstract class UTEditorComponent extends JPanel
   implements IEditorComponent
{

   //see interface for description
   public abstract JComponent getEditorComponent();

   //see interface for description
   public abstract void addActionListener(ActionListener l);
}
