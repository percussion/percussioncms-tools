/*[ BECredentialCellEditor.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import com.percussion.EditableListBox.EditableListBoxBrowseEditor;
import com.percussion.EditableListBox.EditableListBoxCellNameHelper;

import javax.swing.*;

/** The editor that BECredentialPanel uses for table cell editing. 
*/

public class BECredentialCellEditor extends EditableListBoxBrowseEditor
{
//
// CONSTRUCTORS
//
  public BECredentialCellEditor(JTextField field, JDialog ref)
  {
    super(field, ref);
  }

  public BECredentialCellEditor(JComboBox box, JDialog ref)
  {
    super(box, ref);
  }

//
// PUBLIC METHODS
//

/** Overrides the parent method.  Sets the edited text as alias name.
*/
  public boolean stopCellEditing()
  {
    Object test = getCellEditorValue();

    if (test instanceof EditableListBoxCellNameHelper)
    {
      System.out.println("This is an EditableListBoxCellNameHelper!");
    }

    if (test instanceof String)
    {
      System.out.println("This is just a String...");
    }

    boolean stopped = m_delegate.stopCellEditing();

    if (stopped)
    {
      fireEditingStopped();
    }

    return stopped;
  }
}

