/* *****************************************************************************
 *
 * [ ValueSelectorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSFunctionCall;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Dialog for selecting values for conditionals, user defined functions and exits.
 *
 */
public class ValueSelectorDialog extends PSDialogAPI
   implements IPSCellEditorDialog
{
   /**
      * @version  1.1 1999/5/11
      * Constructor that takes the parent frame (can be null) and a Vector of all variable types
      * supported in the current context.
      *
      * @param  frame the parent frame (can be null)
      * @param  vIDataType a Vector of all variable types that are supported in the calling
      * context. The calling class creates objects that implement the IDataTypeInfo interface
      * and are appropriate to the current context. The Value Selector dialog then uses these
      * values to query the end user for a new value, creating the matching PS<DataType> object
      * to return. Objects implementing this interface generally use DT<DataType>
      * as their class name. Examples of the PS<DataType> are PSBackEndColumn, PSCookie,
      * PSCgiVariable, etc.
      * @param defaultType the default type to start from for empty values.  if null, DTTextLiteral
      * wil be used.
      *
      *@throws IllegalArgumentException if the passed in vector is null or empty or
      * if the vector does not contain objects that implement the IDataTypeInfo interface
      * or if the default type is not found in the vector.
      *
      *@see IDataTypeInfo
      *@see AbstractDataTypeInfo
      *
      */
   public ValueSelectorDialog(Frame frame, Vector vIDataType,
                             IDataTypeInfo defaultType) throws IllegalArgumentException

   {
      super(frame);
      init(vIDataType, defaultType);
   }

  /**
      * Constructor that takes the parent dialog (can be null) and a Vector of all variable types
      * supported in the current context.
      *
      * @param  dialog the parent dialog (can be null)
      * @param  vIDataType a Vector of all variable types that are supported in the calling
      * context. The calling class creates objects that implement the IDataTypeInfo interface
      * and are appropriate to the current context. The Value Selector dialog then uses these
      * values to query the end user for a new value, creating the matching PS<DataType> object
      * to return. Objects implementing this interface generally use DT<DataType>
      * as their class name. Examples of the PS<DataType> are PSBackEndColumn, PSCookie,
      * PSCgiVariable, etc.
      * @param defaultType the default type to start from for empty values.  if null, DTTextLiteral
      * will be used.
      *
      *@throws IllegalArgumentException if the passed in vector is <code>null</code> or empty or
      * if the vector does not contain objects that implement the IDataTypeInfo interface
      * or if the default type is not found in the vector.
      *
      *@see IDataTypeInfo
      *@see AbstractDataTypeInfo
      *
      */
   public ValueSelectorDialog(Dialog dialog, Vector vIDataType,
                             IDataTypeInfo defaultType) throws IllegalArgumentException
   {
      super(dialog);
      init(vIDataType, defaultType);
   }

  /**
   *   validate the constructor parameters and initialize field variables.
   *
   * @param  vIDataType a Vector of all variable types that are supported in the calling
   * context.
   *
   * @param defaultType the default type to start from for empty values
   *
   * @throws IllegalArgumentException if the passed in vector is <code>null</code> or empty or
   * if the vector does not contain objects that implement the IDataTypeInfo interface
   * or if the default type is not found in the vector.
   */
    private void init(Vector vIDataType, IDataTypeInfo defaultType)
      throws IllegalArgumentException
    {
      if(vIDataType == null || vIDataType.size() <= 0)
         throw new IllegalArgumentException("Passed in vector of IDataTypeInfo is null or empty");

      for(int i=0; i<vIDataType.size(); i++)
      {
         if(!(vIDataType.get(i) instanceof IDataTypeInfo))
            throw new IllegalArgumentException("The passed in vector does not contain objects that Implement IDataTypeInfo");
      }

      m_vIDataType = vIDataType;

      if (defaultType == null)
         m_defaultType = new DTTextLiteral();
      else if (!isValidDataType(defaultType))
      {
         throw new IllegalArgumentException("The passed in vector does not contain an object matching the default data type");
      }

      m_defaultType = defaultType;
      initDialog();

    }

   /**
    *   *   Add a new OK listener.
   *
    * @ listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addOkListener(ActionListener listener)
  {
   m_okListeners.addElement(listener);
  }

   /**
    *   *   Remove the provided OK listener.
   *
    * @ listener the listener to be removed
    */
   //////////////////////////////////////////////////////////////////////////////
   public void removeOkListener(ActionListener listener)
  {
   m_okListeners.removeElement(listener);
  }

  /**
   * Inform all OK listeners that the OK button was pressed.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireOk()
  {
    ActionEvent event = new ActionEvent(this, 0, "OK");
    for (int i=0; i<m_okListeners.size(); i++)
      ((ActionListener) m_okListeners.elementAt(i)).actionPerformed(event);
  }

   /**
    *   *   Add a new Cancel listener.
   *
    * @param listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addCancelListener(ActionListener listener)
  {
   m_cancelListeners.addElement(listener);
  }

   /**
    *   *   Add a new Refresh listener. Notifies the ActionListener when the refresh
    * method is called
    *
    * @param listener the new listener
    */
   //////////////////////////////////////////////////////////////////////////////
   public void addRefreshListener(ActionListener listener)
  {
   m_refreshListeners.addElement(listener);
  }

   /**
    *   *   Remove the provided Cancel listener.
   *
    * @param listener the listener to be removed
    */
   //////////////////////////////////////////////////////////////////////////////
   public void removeCancelListener(ActionListener listener)
  {
   m_cancelListeners.removeElement(listener);
  }

  /**
   * Inform all Cancel listeners that the Cancel button was pressed.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireCancel()
  {
    ActionEvent event = new ActionEvent(this, 0, "Cancel");
    for (int i=0; i<m_cancelListeners.size(); i++)
      ((ActionListener) m_cancelListeners.elementAt(i)).actionPerformed(event);
  }

  /**
   * Inform all Refresh listeners that the datatypes have been refreshed.
   */
   //////////////////////////////////////////////////////////////////////////////
  protected void fireRefresh()
  {
    ActionEvent event = new ActionEvent(this, 0, "Refresh");
    for (int i=0; i<m_refreshListeners.size(); i++)
      ((ActionListener) m_refreshListeners.elementAt(i)).actionPerformed(event);
  }

   /**
    * Internal for creating the dialog.
    */
   private void initDialog()
   {
      createControls();
      
      initListeners();
      
      initControls();
      //Do not pack()!!! Give some size instead
      setSize(new Dimension(2*COMMON_CONTROL_WIDTH, 3*COMMON_CONTROL_WIDTH/2));
      pack();
      center();
      setResizable(true);
   }

   /**
    * Internal for creating the controls.
    */
   private void createControls()
   {
      
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
      PSPropertyPanel propsPanel = new PSPropertyPanel();
      
      propsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
      m_comboDataType = new PSComboBox();
      
      Dimension dim1 = new Dimension(COMMON_CONTROL_WIDTH, 20);
      m_comboDataType.setMinimumSize(dim1);
      m_comboDataType.setPreferredSize(dim1);     
      
      propsPanel.addPropertyRow( getResources().getString("TYPE"), 
                                 new JComponent[] {m_comboDataType}, 
                                 m_comboDataType, 
                                 getResources().getString("TYPE.mn").charAt(0), 
                                 null);
      
      m_textFieldValue = new JTextField();
      
      Dimension dim2 = new Dimension(COMMON_CONTROL_WIDTH, 20);
      m_comboDataType.setPreferredSize(dim2);
      m_comboDataType.setMinimumSize(dim2);
      
      m_textFieldValue.setPreferredSize(dim2);
      m_textFieldValue.setMinimumSize(dim2);
      
      propsPanel.addPropertyRow( getResources().getString("VALUE"), 
                                 new JComponent[] {m_textFieldValue}, 
                                 m_textFieldValue, 
                                 getResources().getString("VALUE.mn").charAt(0), 
                                 null);
      m_listChoices = new JList();
      m_listChoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                        
      propsPanel.addPropertyRow( getResources().getString("CHOICES"), 
                                 new JComponent[] {m_listChoices}, 
                                 m_listChoices, 
                                 getResources().getString("CHOICES.mn").charAt(0), 
                                 null);

      JPanel jPanelCommand = createCommandPanel();            
      mainPanel.add(propsPanel, BorderLayout.CENTER);      
      mainPanel.add(jPanelCommand, BorderLayout.SOUTH);
      
      setContentPane(mainPanel);
   }

   /**
    *
    * Create the command panel
    */
   private JPanel createCommandPanel()
   {
      m_utCommandPanel = new 
                     UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
        public void onOk()
        {
            ValueSelectorDialog.this.onOk();
        }

        public void onCancel()
        {
         ValueSelectorDialog.this.onCancel();
        }
      };

      /* this next line causes a Memory Leak for some reason, but not
       * in all dialogs where this same thing is done.  Don't know why.
       * Removing it fixes the leak, but the OK button is not longer
       * the default button
       */
      //getRootPane().setDefaultButton(m_utCommandPanel.getOkButton());
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(m_utCommandPanel, BorderLayout.EAST);
      return panel;
   }



   /**
    * Initialize the listeners.
    *
    */
   private void initListeners()
   {

    ItemChangeListener icl =new ItemChangeListener();
    m_comboDataType.addItemListener(icl);

      ListItemChangeListener l = new ListItemChangeListener();
      m_listChoices.addListSelectionListener(l);

    m_textFieldValue.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        m_utCommandPanel.getOkButton().doClick();
      }
    });
   }

   /**
    *   *   Internal for initializin the fields of the dialog. Also sets the value field editable
    * or non-editable.
    *
    */
   private void initControls()
   {
      for(int i=0; i<m_vIDataType.size(); i++)
      {
         String strName = ((IDataTypeInfo)m_vIDataType.get(i)).getDisplayName();
         addNameToComboBox(strName);
      }
      if(m_comboDataType.getItemCount() > 0)
      {
         m_comboDataType.setSelectedIndex(0);
         String strName = (String)m_comboDataType.getSelectedItem();
         setValueFieldEditable(strName);
      }
   }

   /**
    *   *   Sets the Value field in the dialog editable or non editable. Uses the method in
    * IDataTypeInfo to determine if the field should be set editable or not. If the
    * object allows uncataloged values, the field is set editable.
    *@see IDataTypeInfo
    *
    */
   private void setValueFieldEditable(String strName)
   {
      IDataTypeInfo data = (IDataTypeInfo)this.get(strName);
      m_textFieldValue.setEditable(data.allowUncatalogedValues());
   }

   /**
    *Handler for change in selected item in the combo box.
    *
    */
   private void onDataTypeComboItemChanged()
   {
      String strName = (String)m_comboDataType.getSelectedItem();
      if(strName == null || strName.equals(""))
         return;

    if (strName.startsWith("DT") ||
        strName.startsWith("PS"))
    {
      // convert datatype to display string
      String classKey = strName.substring(2, strName.length());
      strName = E2Designer.getResources().getString(E2Designer.getResources().getString(classKey));
    }

      IDataTypeInfo data = (IDataTypeInfo)this.get(strName);

      Enumeration e = data.catalog();
      TreeSet sortedSet = new TreeSet();
      while(e.hasMoreElements())
         sortedSet.add(e.nextElement());
      // Pre-populate the Html params if an Html data type
      if(data instanceof DTHtmlParameter 
         || data instanceof DTSingleHtmlParameter)
      {
         for(int i = 0; i < ms_prePopulateHtmlParamValues.length; i++)
            sortedSet.add(ms_prePopulateHtmlParamValues[i]);
      }
      
      Vector v = new Vector(sortedSet);
      m_listChoices.setListData(v);
      m_comboDataType.setSelectedItem(strName);
      setValueFieldEditable(strName);
   }


   /**
    *Handler for change in selected item in the Choice list.
    *
    */
   private void onListSelectionChanged()
   {
      String strValue = (String)m_listChoices.getSelectedValue();
    if (strValue != null)
        m_textFieldValue.setText(strValue);
   }

   /**
    *
    * Returns the object stored in the m_vIDataType vector that has the passed in name.
    *   *   Returns null if the passed in name is null or empty.
    */
   private Object get(String strName)
   {
//      System.out.println("Getting IDataType object that has the display name "+strName);
      if(strName == null || strName.trim().equals(""))
         return null;
      if(m_vIDataType == null || m_vIDataType.size()<= 0)
         return null;
      for(int i=0; i<m_vIDataType.size(); i++)
      {
         IDataTypeInfo data = (IDataTypeInfo)m_vIDataType.get(i);
         String strDataName = data.getDisplayName();
         if(strDataName.equals(strName))
         {
            return data;
         }
      }
      return null;
   }


   /**
    *
    * Creates the object for external use when the user clicks OK.
    */
   private void createObject() throws IllegalArgumentException
   {
      String strName = (String)m_comboDataType.getSelectedItem();
      IDataTypeInfo data = (IDataTypeInfo)this.get(strName);
      String strValue = m_textFieldValue.getText();

      if ( null != strValue && strValue.trim().length() > 0 )
         m_Object = data.create(strValue);
      else
         m_Object = null;
   }


   /**
    * Parse the provided value into its components (type, value) and return the
   * value.
   *
   * @param value the value in the format type/value
    */
   private String parseValue(String value)
   {
   int index = value.indexOf("/");
    if (index >= 0)
      return value.substring(index+1, value.length());

    return value;
   }

   private void setDialogInvisible()
   {
      this.setVisible(false);
   }

   /**
    *
    * Get the object created when the user clicked OK. This method should be called
    * to get the object created by the Value Selector Dialog. Will return null
    * if the object could not be created.
    *
    */
   public Object getObject()
   {
      return m_Object;
   }


   /**
    * @return An object that contains the state of the dialog or <code>null
    * </code> if the dialog was cancelled.
    * @see #getObject
    */
   public Object getData()
   {
      return getObject();
   }


   /**
    * Sets the object that will be edited by this dialog.
    *
    * @param model object to be edited, not <code>null</code> and must be valid
    *
    * @throws IllegalArgumentException if <code>model</code> is not a valid
    * object for this dialog.
    * @see #setValue
    */
   public void setData(Object model)
   {
      if (!myIsValidModel( model ))
         throw new IllegalArgumentException( "Cannot provide a invalid model" );

      m_Object = null; // clear in case dialog is reused
      setValue( model );
   }


   /**
    * Determines if <code>model</code> is an object that can be edited by
    * this dialog.  An object is editable if it is an instance of either
    * <code>IDataTypeInfo</code> or <code>IPSReplacementValue</code> and if
    * its class has been registered in the designer resources.
    *
    * @param model object to check for editability.  If <code>null</code>,
    * this method will return <code>false</code>.
    *
    * @return <code>true</code> if the object is editable by this dialog;
    * <code>false</code> otherwise.
    */
   public boolean isValidModel(Object model)
   {
      return myIsValidModel( model );
   }


   /**
    * Determines if <code>model</code> is an object that can be edited by
    * this dialog.  An object is editable if it is an instance of either
    * <code>IDataTypeInfo</code> or <code>IPSReplacementValue</code> and if
    * its class has been registered in the designer resources.
    * This method is used by <code>setData</code> because it cannot be overriden.
    * (If <code>setData</code> called <code>isValidModel</code>, then if
    * a derived class called <code>super.setData</code>, the super class might
    * use the derived class' <code>isValidModel</code> instead -- which would
    * cause a false result if the derived class had different requirements than
    * the super class.)
    *
    * @param model object to check for editability.  If <code>null</code>,
    * this method will return <code>false</code>.
    *
    * @return <code>true</code> if the object is editable by this dialog;
    * <code>false</code> otherwise.
    */
   private boolean myIsValidModel(Object model)
   {
      try
      {
         if (model instanceof IDataTypeInfo)
         {
            String type = model.getClass().toString();
            type = type.substring( type.lastIndexOf( '.' ) + 1 );
            String classKey = E2Designer.getResources().getString( type );
            // classKey is never null; getString throws MissingResourceException
            return true;
         }
         else if (model instanceof PSFunctionCall)
         {
            return true;
         }
         else if (model instanceof IPSReplacementValue)
         {
            String type = ((IPSReplacementValue) model).getValueType();
            String classKey = E2Designer.getResources().getString( type );
            // classKey is never null; getString throws MissingResourceException
            return true;
         }
      } catch (MissingResourceException e)
      {
         // can't edit if can't find the classKey from the resources
         return false;
      }

      return false;
   }


   // see interface for details
   public void reset()
   {
      m_Object = null;
      m_textFieldValue.setText( null );
      if (m_comboDataType.getItemCount() > 0)
      {
         m_comboDataType.setSelectedIndex( 0 );
         String strName = (String) m_comboDataType.getSelectedItem();
         setValueFieldEditable( strName );
      }
      m_listChoices.clearSelection();
   }


   /**
    * Initialize the "value" object with the provided data.
    *
    * @param value the value text
    */
   public void setValue(Object value)
   {
      if (value instanceof PSFunctionCall)
      {
         // the current value in the cell is a function call, but the user
         // selected the "Single Value" popup menu item. This dialog box
         // cannot be used to edit function call. Leaving this as a no-op
         // behaves as if the user is editing a new cell.
      }
      else
      {
         if (value instanceof IPSReplacementValue)
            m_textFieldValue.setText(
               ((IPSReplacementValue) value).getValueText());
         else
            m_textFieldValue.setText( parseValue( value.toString() ) );

         updateDataTypeCombo( value );
      }
   }


/** Handles ok button action. Overrides PSDialog onOk() method implementation.
*/
  public void onOk()
  {
    //create the object for the selected type
    String strValue = m_textFieldValue.getText();
    if(strValue == null || (strValue.trim()).equals(""))
    {
      JOptionPane.showMessageDialog(this, getResources().getString( "ENTER_VALUE" ));
      return;
    }
    try
    {
        createObject();
    }
    catch(IllegalArgumentException e)
      {
         String errorMsg = e.getLocalizedMessage();
         if ( 0 == errorMsg.trim().length())
            errorMsg = E2Designer.getResources().getString("CantConvertInput");
         JOptionPane.showMessageDialog( this,
                                                       errorMsg,
                                                       E2Designer.getResources().getString("InputErrorTitle"),
                                                       JOptionPane.ERROR_MESSAGE );
         return;
      }
    fireOk();
    setDialogInvisible();
  }


/** Handles cancel button action. Overrides PSDialog onCancel() method
  * implementation.
*/
  public void onCancel()
  {
    fireCancel();
    dispose();
  }


   /**
    * Sets the selected item of the data type combo box based on the type of
    * the object supplied as <code>value</code>.
    *
    * @param value the value that is an instance of IDataTypeInfo
    * If <code>null</code>, this method does nothing.
    */
   private void updateDataTypeCombo(Object value)
   {
      if (value == null)
         return;

      if (value instanceof IDataTypeInfo)
      {
         String type = value.getClass().toString();
         type = type.substring( type.lastIndexOf( '.' ) + 1 );
         String classKey = E2Designer.getResources().getString( type );
         m_comboDataType.setSelectedItem( classKey );
      }
      else if (value instanceof IPSReplacementValue)
      {
         String type = ((IPSReplacementValue) value).getValueType();
         String classKey = E2Designer.getResources().getString( type );
         String displayName = E2Designer.getResources().getString(classKey);
         
         m_comboDataType.setSelectedItem(displayName);
      }
      else
      {
         // set the default if it exists
         if (m_defaultType != null)
         {
            String type =
               ((AbstractDataTypeInfo) m_defaultType).getClass().toString();
            type = type.substring( type.lastIndexOf( '.' ) + 1 );
            String classKey = E2Designer.getResources().getString( type );
            m_comboDataType.setSelectedItem( classKey );
         }
      }
   }

   /**
    * Sets the value selectors default data type. This will be used only for
    * undefined fields.
    *
    * @param defaultType the new default type.  if null, or not valid for this
    * dialog, DTTextLiteral will be used.  If DTTextLiteral is not valid, then the first
    * valid datatype will be used.
    */
  //////////////////////////////////////////////////////////////////////////////
   public void setDefaultType(IDataTypeInfo defaultType)
   {

      if ((defaultType == null) || (!isValidDataType(defaultType)))
         {
            IDataTypeInfo textType = new DTTextLiteral();
            if (isValidDataType(textType))
               m_defaultType = textType;
            else
               m_defaultType = (IDataTypeInfo)m_vIDataType.get(0);
         }
      else
         m_defaultType = defaultType;

      // if the DataTypeInfo in combobox did not change, update its available
      // values
      if ( defaultType.getDisplayName().equals( E2Designer.getResources().getString("DTHtmlParameter") ) &&
         defaultType.getDisplayName().equals( m_defaultType.getDisplayName() ))
         onDataTypeComboItemChanged();
  }

   /**
    * returns a list of datatype that are valid for setting values on this
    * dialog
    *
    * @return list of valid datatypes for this dialog
    *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Enumeration getValidDataTypes()
   {
      return m_vIDataType.elements();
   }

   /**
    *
    * Refresh the type vector.
   *
    * @param value the value text
    */
   public void refresh(Vector vIDataType)
   {
      m_vIDataType = vIDataType;

    // remove all elements and then initialize them again with the new settings
    m_comboDataType.removeAllItems();
    initControls();
    fireRefresh();
   }


   /**
    *
    * Inserts a the Data type Name to the list of entries in the combobox.
    * The name is inserted in an alphabetically sorted order.
    */
   private void addNameToComboBox(String strName)
   {
      int iCount = m_comboDataType.getItemCount();

      // sort while adding
    Collator c = Collator.getInstance();
    c.setStrength(Collator.SECONDARY);     // for case insensitive comparison
    int i=0;
    if (iCount > 0)        //then we need to find the insert index
    {
      for ( i=0 ; i < iCount ; i++)
      {
        if( c.compare(strName, (String)m_comboDataType.getItemAt(i)) < 0 )
        {
          break;
        }
         }
      }
      if (i >= iCount)
         m_comboDataType.addItem(strName);
      else
         m_comboDataType.insertItemAt(strName, i);
   }

   /**
    *   *   Validates that the datatype is one that is selectable for the
    * current instance.
    *
    * @param dt the datatype to validate
    *
    * @return true if valid, false if not.
    *
    */
   public boolean isValidDataType(IDataTypeInfo dt)
   {
      boolean match = false;
      String strName = dt.getDisplayName();

      for(int i=0; i<m_vIDataType.size(); i++)
      {
         if( ((IDataTypeInfo)m_vIDataType.get(i)).getDisplayName().equals(strName) )
         {
            match = true;
            break;
         }
      }
      return match;
   }

  /** Inner class to implement ListSelectionListener for handling the List selection changes.
   */
  class ListItemChangeListener implements ListSelectionListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      onListSelectionChanged();
    }
  }



  /**
    * Inner class to implement ItemListener interface for handling item change events for combo boxes.
    */
  class ItemChangeListener implements ItemListener
   {
      public void itemStateChanged( ItemEvent e )
      {
//      System.out.println("In ItemChangeListener");
      PSComboBox combo = (PSComboBox)e.getSource( );
         if (combo == m_comboDataType)
      {
        if (e.getStateChange()== ItemEvent.SELECTED)
               onDataTypeComboItemChanged();
      }
      }
   }


   //storage
   private UTStandardCommandPanel m_utCommandPanel = null;
   private Vector m_vIDataType = null;
   private Object m_Object = null;

   protected transient Vector m_okListeners = new Vector();
   protected transient Vector m_cancelListeners = new Vector();
   protected transient Vector m_refreshListeners = new Vector();

   private IDataTypeInfo m_defaultType = null;

   //{{DECLARE_CONTROLS
   private PSComboBox  m_comboDataType;
   private JTextField  m_textFieldValue;
   
   private JList m_listChoices;
   
   /**
    * The common controls, i.e. the textfield, scrollpane and and such take their 
    * size from this value, and other sizes are calculated from this value
    */
   private static int COMMON_CONTROL_WIDTH = 240;
   //}}
   
   /**
    * Values that will be pre-populated in the choice field for
    * HTML param types.
    */
   private static String[] ms_prePopulateHtmlParamValues = 
      {
      "sys_authtype",
      "sys_contentid",
      "sys_contenttypeid",
      "sys_context",
      "sys_communityid",
      "sys_folderid",
      "sys_revision",
      "sys_siteid",
      "sys_slotid",
      "sys_slotname",
      "sys_stateid",
      "sys_variantid",
      "sys_workflowid"
      };
}
