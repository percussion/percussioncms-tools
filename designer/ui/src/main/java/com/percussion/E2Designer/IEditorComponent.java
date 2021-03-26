/*[ IEditorComponent.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * This interface defines methods which are required for using any
 * object which extends <code>JComponent</code> as editor component in
 * {@link UTCellEditor} which is used as table cell editor.
 */
public interface IEditorComponent
{
   /**
    * Returns the basic editor component in an editor. For example it can be any
    * of the following controls, 'JTextField', 'JComboBox', 'JCheckBox', 'JList'.
    *
    * @return the editor component, may not be <code>null</code>
    */
   public JComponent getEditorComponent();

   /**
    * Add listener to the component on whose action the cell editing to be
    * stopped.
    *
    * @param l the action listener, may not be <code>null</code>
    */
   public void addActionListener(ActionListener l);
}
