/*[ ValueSelectorCellEditor.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
/**
 * The text field editor panel provides two components: an editable text field
 * and a button which opens the dialog provided in the constructor.
 */
////////////////////////////////////////////////////////////////////////////////
public class ValueSelectorCellEditor extends UTTextFieldCellEditor
{
   /**
   * Construct the cell editor providing a reference to the editor dialog used
   * while the edit button was pressed. The default type will be HTML parameter.
   *
   * @param editor the editor dialog
    */
  //////////////////////////////////////////////////////////////////////////////
   public ValueSelectorCellEditor(ValueSelectorDialog editor)
   {
      this(editor, new DTTextLiteral());
   }

   /**
   * Construct the cell editor providing a reference to the editor dialog used
   * while the edit button was pressed.
   *
   * @param editor the editor dialog
   * @param defaultType the type to create by default (while direct editing)
    */
  //////////////////////////////////////////////////////////////////////////////
   public ValueSelectorCellEditor(ValueSelectorDialog editor,
                                 IDataTypeInfo defaultType)
   {
      super(editor, defaultType);
      m_editor = editor;

      // use the editor to validate the default type against its list of valid
      // types.  if invalid type or null, default it to literal or first valid type
      if ((defaultType == null) ||
      ((defaultType != null) && !m_editor.isValidDataType(defaultType)))
      {
         // this will fix things up
         setDefaultType(defaultType);
      }

      // setup datatypes for createObject() using valid datatypes from our editor
      refreshDataTypes();

      initEditor();
   }

  /**
   * Get the default type for this editor.
   *
   * @return IDataTypeInfo the current default type
   */
  //////////////////////////////////////////////////////////////////////////////
  public IDataTypeInfo getDefaultType()
  {
    return m_defaultType;
  }

   /**
   * Make the editor dialog visible.
    */
  //////////////////////////////////////////////////////////////////////////////
  public void setVisible()
  {
    m_editor.setDefaultType(getDefaultType());
    super.setVisible();
  }

  /**
   * Sets the default type for this editor.  if type is null or not selectable for
   * this editor, it is set to DTTextLiteral or the first valid type
   *
   *@param defaultType the new default type
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setDefaultType(IDataTypeInfo defaultType)
  {
      if ((defaultType == null) ||
      ((defaultType != null) && !m_editor.isValidDataType(defaultType)))
      {
         IDataTypeInfo typeText = new DTTextLiteral();
         if (m_editor.isValidDataType(typeText))
            m_defaultType = typeText;
         else
         {
            Enumeration e = m_editor.getValidDataTypes();
            if (e.hasMoreElements())
            {
               m_defaultType = (IDataTypeInfo)e.nextElement();
            }
         }
       }
       else
         m_defaultType = defaultType;
  }

   /**
   * Initialize the cell editor.
    */
  //////////////////////////////////////////////////////////////////////////////
  public void initEditor()
  {
     // catch OK/Cancel actions from value selector dialog and perform the
    // approptiate action
     if (m_editor != null)
    {
       m_editor.addOkListener(new ActionListener()
       {
          public void actionPerformed(ActionEvent event)
         {
             setValue(m_editor.getObject());
           acceptCellEditing();
         }
       });

       m_editor.addCancelListener(new ActionListener()
       {
          public void actionPerformed(ActionEvent event)
         {
          cancelCellEditing();
         }
       });

      // setup datatypes for createObject() using valid datatypes from our editor
       m_editor.addRefreshListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            refreshDataTypes();
         }
      });


    }

    // perform action for cell browse (...) button
    addEditListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent event)
      {
         // make sure the editor's default type is in sync with ours
         m_editor.setDefaultType(m_defaultType);

          m_editor.setValue(getCellEditorValue());
      }
    });

   // perform action for ENTER on cell text field
    addTextListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent event)
      {
         stopCellEditing();
      }
   });

  }

  /**
   * Refresh the possible datatype values.
   *
   */
   private void refreshDataTypes()
   {
      m_dataTypes.clear();
      Enumeration e = m_editor.getValidDataTypes();
      while (e.hasMoreElements())
      {
         m_dataTypes.add(e.nextElement());
      }
   }

   /**
   *  Returns Value Selector Editor
   *
   *  @return the value selector dialog, might be <code>null</code>.
   */
   protected ValueSelectorDialog getValueEditor()
   {
      return m_editor;
   }
   
   //////////////////////////////////////////////////////////////////////////////
   ValueSelectorDialog m_editor = null;
}
