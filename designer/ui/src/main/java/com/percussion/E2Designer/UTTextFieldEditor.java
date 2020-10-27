/*[ UTTextFieldEditor.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * The text field editor panel provides two components: an editable text field
 * and a button which opens the dialog provided in the constructor.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTTextFieldEditor extends JPanel
{
   /**
   * Construct the default panel
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTTextFieldEditor()
   {
   initPanel();
   }

   /**
   * Construct the panel, assigning an editor, which is brought up while the
   * edit button was pressed.
   *
   * @param editor the editore dialog
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTTextFieldEditor(PSDialog editor)
   {
      m_editor = editor;
      initPanel();
   }

   public UTTextFieldEditor(PSDialog editor, IDataTypeInfo defaultType)
   {
      m_editor = editor;
      initPanel();
      m_defaultType = defaultType;
   }


   /**
    * This component is mainly a text field, so any focus requests to the base panel
    * should be directed to the text editor.
   **/
   public void requestFocus()
   {
      m_text.requestFocus();
   }

   /**
   * Construct the panel, assigning an editor, which is brought up while the
   * edit button was pressed.
   *
   * @param editor the editore dialog
    */
  //////////////////////////////////////////////////////////////////////////////
   public UTTextFieldEditor(JFileChooser chooser)
   {
   m_chooser = chooser;
   initPanel();
   }

   /**
   * Set editor value.
   *
   * @param value the new value to set
   * @return <code>true</code> if value is set
   * @throws IllegalArgumentException If param is null.
   */
   public boolean setValue(Object value)
   {
      if(value == null)
         throw new IllegalArgumentException("Value to set cannot be null");

      boolean bCreated = true;

      String strValue;
      if (m_defaultType != null && value instanceof String)
      {
         strValue = (String)value;
         try
         {
            m_value = m_defaultType.create(strValue);
            if (m_value instanceof IPSReplacementValue)
               strValue = ((IPSReplacementValue)m_value).getValueText();
            else
               strValue = m_value.toString();            
         }
         catch ( IllegalArgumentException e )
         {
            PSDlgUtil.showError(e, false,
                  E2Designer.getResources().getString("ConversionFailedTitle"));
            bCreated = false;
         }
      }
      else
      {
         if (value instanceof IPSReplacementValue)
            strValue = ((IPSReplacementValue)value).getValueText();
         else
            strValue = value.toString();
         m_value = value;
      }

      if ( bCreated )
         m_text.setText(strValue);

      return bCreated;
   }

   /**
   * Set editor value and display text.
   *
   * @param value the new value to set
   *    assumed not <code>null</code>.
   * @param displayText Text to be set in the editor
   *    assumed not <code>null</code>, might be empty.
   */
   /////////////////////////////////////////////////////////////////////////////
   protected void setValue(Object value, String displayText)
   {
      m_text.setText(displayText);
      m_value = value;
   }

   /**
   * Get editor value.
   *
   * @return Object the editor value
   */
  //////////////////////////////////////////////////////////////////////////////
  public Object getValue()
  {
      return m_value;
  }

   /**
   * Get text field value
   *
   * @return String the text field value
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getText()
  {
      return m_text.getText();
  }
   /**
   * Get the display status (returns true if in display mode, false if in edit
   * mode).
   *
   * @return boolean the current display status
   */
  //////////////////////////////////////////////////////////////////////////////
  public boolean isDisplayMode()
  {
      return m_isDisplayMode;
  }

   /**
   * Sets the panel into display mode.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setDisplayMode()
  {
   removeAll();
   add(m_displayPanel);
   m_isDisplayMode = true;
   invalidate();
   repaint();
  }

   /**
   * Sets the panel into edit mode.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setEditMode()
  {
   removeAll();
   add(m_editPanel);
   m_text.select(0, m_text.getText().length());
   m_text.grabFocus();
   m_isDisplayMode = false;

   invalidate();
   repaint();
  }

   /**
   * Toggles the panel view from display mode to edit mode and vice versa.
   */
  //////////////////////////////////////////////////////////////////////////////
  public void toggleDisplayMode()
  {
   removeAll();
   if (isDisplayMode())
   {
     add(m_editPanel);
     m_isDisplayMode = false;
   }
   else
   {
     add(m_displayPanel);
     m_isDisplayMode = true;
    }

    invalidate();
    repaint();
  }

   /**
   * Add a key listener to the text field.
   *
   * @param listener the key listener
   */
  //////////////////////////////////////////////////////////////////////////////
  public void addTextKeyListener(KeyListener listener)
  {
   m_text.addKeyListener(listener);
  }

  /**
  * Add a focus listener to the text field
  *
  * @param listener the focus listener
  */
  //////////////////////////////////////////////////////////////////////////////
  public void addTextFocusListener(FocusListener listener)
  {
   m_text.addFocusListener(listener);
  }

  /**
  * Add a compenent listener to the text field
  *
  * @param listener the compent listener
  */
  //////////////////////////////////////////////////////////////////////////////
  public void addTextComponentListener(ComponentListener listener)
  {
   m_text.addComponentListener(listener);
  }


   /**
   * Add an action listener to the text field.
   *
   * @param listener the action listener
   */
  //////////////////////////////////////////////////////////////////////////////
  public void addTextListener(ActionListener listener)
  {
   m_text.addActionListener(listener);
  }

  //force an event on the text field
  public void fireTextActionPerformed(String text)
  {
   m_text.setActionCommand(text);
   m_text.postActionEvent();
   /* if we don't clear the command, it will be sent the next time the ENTER key
      is pressed */
   m_text.setActionCommand(null);
  }
   /**
   * Add an action listener to the edit button.
   *
   * @param listener the action listener
   */
  //////////////////////////////////////////////////////////////////////////////
  public void addEditListener(ActionListener listener)
  {
   m_edit.addActionListener(listener);
  }

   /**
   * Initialize the panels.
    */
  //////////////////////////////////////////////////////////////////////////////
  protected void initPanel()
  {
   // create the panels
    m_displayPanel = createDisplayPanel();
    m_editPanel = createEditPanel();

    // add the display panel by default
   setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(m_displayPanel);
    m_isDisplayMode = true;
  }

   /**
   * Create and initialize the display panel. This will be displayed if the cell
   * is not in edit-mode.
   *
   * return JPanel the display panel
    */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createDisplayPanel()
  {
   JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
    p1.add(m_text);

    return p1;
  }

   /**
   * Make the editor dialog visible.
    */
  //////////////////////////////////////////////////////////////////////////////
  public void setVisible()
  {
    m_editor.setVisible(true);
  }

   /**
   * Create and initialize the edit panel. This will be displayed if the cell
   * is in edit-mode.
   *
   * return JPanel the edit panel
    */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createEditPanel()
  {
   m_edit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        if (m_editor != null)
        {
          setVisible();
        }
        else if (m_chooser != null)
        {
          m_chooser.showOpenDialog(E2Designer.getApp().getMainFrame());
        }
      }
    });

   JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
    p1.add(m_text);
      p1.add(m_edit);

    return p1;
  }

  /**
  *creates a new object based on the original object
  *
  *@param repValue new replacement text for object
  *
  *@param refObject the object to use as base to create the new object
  *
  *@param dataTypes the Vector of default datatypes, 
  *
  *@param parent the object to use as base to create the new object
  *
  *@return <code> null </code> if object type was not found, else
  *a new object based on original object
  *
  */
  public static Object createObject(String repValue,Object refObject,
      Vector dataTypes, @SuppressWarnings("unused") Component parent)
  {
     Object retObject=null;

     // if our datatypes is empty, lazily construct
     // a ValueSelectorDialogHelper, this contains all possible
     // IPSReplacementValue(s).  This may have already been done.
     if (dataTypes == null || dataTypes.isEmpty())
     {
        ValueSelectorDialogHelper helper = new ValueSelectorDialogHelper();

        // get the IPSReplacementValue list
        dataTypes = helper.getDataTypes();
      }

     // cast the old object
     if( refObject instanceof IPSReplacementValue )
     {
        IPSReplacementValue oldData=(IPSReplacementValue)refObject;
        if( oldData != null )
        {
           // get the object type
           String strname=oldData.getValueType();
          // and get the class text
          String classKey = E2Designer.getResources().getString(strname);
         // get the display name, this is the same name that we use
         // to create the object
         if (classKey.startsWith("DT") || classKey.startsWith("PS"))
         {
           // convert datatype to display string
           String key = classKey.substring(2, classKey.length());
           classKey = E2Designer.getResources().getString(E2Designer.getResources().getString(key));
         }
         if(classKey == null || classKey.trim().length() < 1)
            return null;
         // now walk trought the list and get the proper object type
         Object item = null;
         for(int i=0; i<dataTypes.size(); i++)
         {
            // get the object
            Object obj = dataTypes.get(i);
            if(!(obj instanceof IDataTypeInfo))
               continue;
            IDataTypeInfo data = (IDataTypeInfo)obj;
            // it matches?
            if(data.getDisplayName().equals(classKey))
            {
               item = data; // yes return the object
            }
         }

         if( item != null && null != repValue && repValue.trim().length() > 0 )
         {
            // cast to IDataTypeInfo
            IDataTypeInfo data=(IDataTypeInfo)item;
            // construct it
            try
            {
               retObject = data.create(repValue);
            }
            catch(IllegalArgumentException e)
            {
               String errorMsg = e.getLocalizedMessage();
               if ( 0 == errorMsg.trim().length())
               {
                  errorMsg =
                     E2Designer.getResources().getString("CantConvertInput");
               }
               PSDlgUtil.showErrorDialog(errorMsg,
                     E2Designer.getResources().getString("InputErrorTitle"));
               retObject = null ;
            }
         }
       }
     }
      // return the new object or null
      return(retObject);
  }

   //////////////////////////////////////////////////////////////////////////////
  /**
   * the value storage
   */
  protected Object m_value = new Object();

  /**
   * the text field
   */
  protected JTextField m_text = new JTextField("");
  /**
   * the edit button
   */
  protected UTFixedButton m_edit = new UTFixedButton((new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_Browser")))), new Dimension(20, 500));
  /**
   * the editor dialog
   */
  protected PSDialog m_editor = null;
  /**
   * the editor dialog
   */
  protected JFileChooser m_chooser = null;
  /**
   * the panel display status: true for display mode, false for edit mode
   */
  protected boolean m_isDisplayMode = true;
  /**
   * the display panel
   */
  protected JPanel m_displayPanel = null;
  /**
   * the edit panel
   */
  protected JPanel m_editPanel = null;
  protected IDataTypeInfo m_defaultType = new DTTextLiteral();
}
