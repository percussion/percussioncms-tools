/******************************************************************************
 *
 * [ JavaExitsPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBox;
import com.percussion.EditableListBox.EditableListBoxEditor;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * This class is for editing the properties of the Formula object that is of JavaExit type.
 */
public class JavaExitsPropertyDialog extends PSEditorDialog
{

   /**
    * Constructs <code>JavaExitsPropertyDialog</code> with given parent and
    * exit call set.
    *
    * @param parent the parent frame, may be <code>null</code>
    * @param c the exit call set, may not be <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if <code>c</code> is <code>null</code>
    */
   public JavaExitsPropertyDialog(Window parent, OSExitCallSet c)
   {
      super(parent);
      setLocationRelativeTo(parent);

      if (c == null)
         throw new IllegalArgumentException("the exit call set can not be null");

      m_collection = c;
      initDialog();
   }

   /**
    * Constructs <code>JavaExitsPropertyDialog</code> with given parent and
    * exit call set. Initializes the dialog with data. This should be called
    * to use dialog independently instead of calling {@link #onEdit}. Should
    * call <code>setVisible(true)</code> after constructing to display the
    * dialog.
    *
    * @param parent the parent frame, may be <code>null</code>
    * @param c the exit call set, may not be <code>null</code>, may be empty.
    * @param modifySet if <code>true</code>, allows to add or delete the
    * exits in the passed in exit call set and modify the exits, otherwise
    * allows to modify the exits in given set only.
    *
    * @throws IllegalArgumentException if <code>c</code> is <code>null</code>
    */
   public JavaExitsPropertyDialog(
           Window parent,
      OSExitCallSet c,
      boolean modifySet)
   {
      this(parent, c);

      initUI(modifySet, null);
   }

   /**
    * Initializes the UI State.
    *
    * @param modifySet if <code>true</code>, allows to add or delete the
    * exits in the passed in exit call set and modify the exits, otherwise
    * allows to modify the exits in given set only.
    * @param title of the dialog, if <code>null</code> default title for this
    * dialog remains, otherwise this will be set as title for the dialog and the
    * list box that shows the exits, assumed not empty.
    */
   private void initUI(boolean modifySet, String title)
   {
      createEditableListBox();

      if (title != null)
      {
         m_editableListBox.setTitle(title);
         setTitle(title);
      }
      initListeners();

      initData();
      //Disable add and delete buttons in the list box if we don't want
      //to add exits to or delete exits from the supplied call set.
      if (!modifySet)
      {
         m_editableListBox.getLeftButton().setEnabled(false);
         m_editableListBox.getRightButton().setEnabled(false);
      }
      //by default select the first exit.
      if (m_editableListBox.getList().getRowCount() > 0)
      {
         m_editableListBox.getList().getSelectionModel().setSelectionInterval(
            0,
            0);
      }
   }

   /**
    * Constructs <code>JavaExitsPropertyDialog</code> with given parent and
    * exit call set. Initializes the dialog with data. This should be called
    * to use dialog independently instead of calling {@link #onEdit}. Should
    * call <code>setVisible(true)</code> after constructing to display the
    * dialog.
    *
    * @param parent the parent dialog, may be <code>null</code>
    * @param c the exit call set, may not be <code>null</code>, may be empty.
    * @param modifySet if <code>true</code>, allows to add or delete the
    * exits in the passed in exit call set and modify the exits, otherwise
    * allows to modify the exits in given set only.
    * @param title the title of the dialog, may be <code>null</code>, can not
    * be empty.
    *
    * @throws IllegalArgumentException if <code>c</code> is <code>null</code> or
    * title is empty.
    */
   public JavaExitsPropertyDialog(
      JDialog parent,
      OSExitCallSet c,
      boolean modifySet,
      String title)
   {
      super(parent);

      if (c == null)
         throw new IllegalArgumentException("the exit call set can not be null");

      if (title != null && title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be empty.");

      if (parent != null)
         setLocationRelativeTo(parent);

      m_collection = c;
      initDialog();
      initUI(modifySet, title);
   }

   //see interface for description.
   public boolean onEdit(UIFigure figure, final Object data)
   {
      if (figure == null)
      {
         throw new IllegalArgumentException("Passed in UIFigure object is null");
      }
      if (!(figure.getData() instanceof OSExitCallSet))
      {
         throw new IllegalArgumentException("UIFigure's getData did not return an OSExitCallSet object");
      }

      if (figure instanceof UIConnectableFigure)
      {
         UIConnectableFigure connection = (UIConnectableFigure) figure;
         if (!connection.isAttached())
         {
            JOptionPane.showMessageDialog(
               this,
               E2Designer.getResources().getString("mustBeAttached"),
               E2Designer.getResources().getString("OpErrorTitle"),
               JOptionPane.OK_OPTION);
            return (false);
         }
      }

      m_collection = (OSExitCallSet) figure.getData();

      // find and initialize the member m_osPageDatatank with the
      // OSPageDatatank that this Formula object is attached to
      try
      {
         UTPipeNavigator navigator = new UTPipeNavigator();
         UIFigure figPageDatatank = navigator.getPageTank(figure);
         if (figPageDatatank != null)
         {
            m_osPageDatatank = (OSPageDatatank) figPageDatatank.getData();
            if (m_osPageDatatank == null)
               throw new IllegalArgumentException("OSPageDatatank object is null");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      createEditableListBox();
      initListeners();

      initData();

      setVisible(true);

      return m_bModified;
   }

   /**
    * Initializes all controls with data and displays warning message if there
    * is mismatch in parameters after the exit is created.
    */
   private void initData()
   {
      m_vData = new ArrayList<>();
      for (Object o : m_collection) {
         OSExtensionCall exitCall = (OSExtensionCall) o;

         // create the JavaExitsPropertyDialogData objects for
         // each item in the collection
         JavaExitsPropertyDialogData exitData =
                 new JavaExitsPropertyDialogData(exitCall);

         // Has the extension param list changed since the exit was created?
         if (exitData.hasParamValueMismatch()) {
            // display warning message
            String msg =
                    MessageFormat.format(
                            E2Designer.getResources().getString("ParameterMismatch"),
                            new Object[]{exitData.getName()});
            JOptionPane.showMessageDialog(
                    this,
                    Util.cropErrorMessage(msg),
                    E2Designer.getResources().getString("JavaExits"),
                    JOptionPane.WARNING_MESSAGE);
         }
         m_vData.add(exitData);
      }
      m_vServerExits = getServerExits(m_collection);

      initDialogFields();
   }

   /** Handles the ok buttons action. Overrides PSDialog onOk() method
    * implemenation.
    */
   @Override
   public void onOk()
   {
      int iLimit = m_editableListBox.getItemCount();
      if (iLimit > 0)
      {
         boolean bSaved = saveCollection();
         if (!bSaved)
            return;
            
         m_okExit = true;
         dispose();
      }
      else
      {
         JOptionPane.showMessageDialog(
            this,
            Util.cropErrorMessage(
               E2Designer.getResources().getString("InvalidEmptyExits")),
            E2Designer.getResources().getString("JavaExits"),
            JOptionPane.ERROR_MESSAGE);
      }
   }
   
   /**
    * Returns whether this dialog was exited through the OK button or not.
    * 
    * @return <code>true</code> if exited through OK button, <code>false</code>
    *    otherwise.
    */
   public boolean wasOkExit()
   {
      return m_okExit;
   }

   /**
    * Initializes the dialog by creating the controls of the dialog and initializes listeners.
    */
   private void initDialog()
   {
      getContentPane().setLayout(null);
      setSize(450, 575);
      setAutoRequestFocus(true);
      createControls();
      createParameterPanel();
      createCommandPanel();

      center();

   }

   /**
    *   Creates the dialog controls
    */
   private void createControls()
   {

      jPanelListBox = new JPanel();
      jPanelListBox.setLayout(new BorderLayout());
      jPanelListBox.setBounds(10, 10, 272, 160);
      getContentPane().add(jPanelListBox);

      jPanelMove = new JPanel();
      jPanelMove.setLayout(null);
      jPanelMove.setBounds(288, 35, 35, 55);
      getContentPane().add(jPanelMove);

      jButtonMoveUp =
         new JButton(
            new ImageIcon(
               getClass().getResource(
                  getResources().getString("gif_upButton"))));
      jButtonMoveUp.setBounds(0, 0, 20, 15);
      jPanelMove.add(jButtonMoveUp);

      jButtonMoveDown =
         new JButton(
            new ImageIcon(
               getClass().getResource(
                  getResources().getString("gif_downButton"))));
      jButtonMoveDown.setBounds(0, 35, 20, 15);
      jPanelMove.add(jButtonMoveDown);

      jLabelMove = new JLabel();
      jLabelMove.setText(getResources().getString("move"));
      jLabelMove.setBounds(0, 15, 36, 16);
      jPanelMove.add(jLabelMove);

      jPanelCommand = new JPanel(new BorderLayout());
      jPanelCommand.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      jPanelCommand.setBounds(75, 505, 440, 595);      
      getContentPane().add(jPanelCommand);

      jPanelParameter = new JPanel();
      jPanelParameter.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      jPanelParameter.setBounds(10, 175, 420, 340);
      getContentPane().add(jPanelParameter);
   }

   /**
    *   Creates the command panel with OK, Cancel and help buttons.
    *
    */
   private void createCommandPanel()
   {
      m_commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            JavaExitsPropertyDialog.this.onOk();
         }
      };

      getRootPane().setDefaultButton(m_commandPanel.getOkButton());
      jPanelCommand.add(m_commandPanel, BorderLayout.EAST);

   }

   /**
    *   Attempts to save the collection of JavaExits to the passed in collection in the Dialog constructor.
    *
    *@returns true if save attempt was successful, else returns false. A message is displayed in case
    * save is unsuccessful.
    */
   private boolean saveCollection()
   {
      try
      {
         int curIndex =
            m_editableListBox.getSelectionModel().getMinSelectionIndex();
         if (curIndex >= 0)
         {
            saveParameterPanelData(curIndex);
         }

         String type = m_collection.getType();
         m_collection.clear();
         int iLimit = m_editableListBox.getItemCount();
         for (int i = 0; i < iLimit; i++)
         {
            JavaExitsPropertyDialogData data =
               (JavaExitsPropertyDialogData) m_editableListBox.getRowValue(i);
            if(data==null)
               continue;
            PSExtensionParamValue[] values = data.getParamValues();
            // replace any null values with an empty text literal as a
            // placeholder
            for (int j = 0; j < values.length; j++)
            {
               if (values[j] == null)
                  values[j] = new PSExtensionParamValue(new PSTextLiteral(""));
            }

            OSExtensionCall call = new OSExtensionCall(data.getExit(), values);
            m_collection.add(call, type);
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
      return true;
   }

   /**
    *   Creates the parameter panel with the table of parameter values
    *
    */
   private void createParameterPanel()
   {
      // create value selector dialog cell editor
      ValueSelectorDialogHelper h =
         new ValueSelectorDialogHelper(null, m_osPageDatatank);
      ValueSelectorDialog d =
         new ValueSelectorDialog(this, h.getDataTypes(), null);
      ValueSelectorCellEditor editor = new ValueSelectorCellEditor(d);
      m_parameterPanel = new UTParameterPanel(editor, false);
      jPanelParameter.add(m_parameterPanel);

   }

   /**
    *   Creates the List with JavaExits. The list olds JavaExitPropertyDialogData objects.
    *
    *@see JavaExitsPropertyDialogData
    */
   private void createEditableListBox()
   {
      Object[] comboBoxData = getComboBoxData();
      Object[] data = {
      };

      // TODOph: change to editable list box when have time to debug it
      m_editableListBox =
         new EditableListBox(
            getResources().getString("JavaExits"),
            this,
            data,
            comboBoxData,
            EditableListBox.DROPDOWNLIST,
            EditableListBox.INSERTBUTTON);
      m_editableListBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      (
         (EditableListBoxEditor) m_editableListBox
            .getCellEditor())
            .setClickCountToStart(
         Integer.MAX_VALUE);
      // disabling editing
      m_editableListBox.getRightButton().addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_bMove = true;
            ListSelectionModel sm = m_editableListBox.getSelectionModel();
            int curIndex = sm.getMinSelectionIndex();
            m_editableListBox.deleteRows();

            int remainingItems = m_editableListBox.getItemCount();
            if (remainingItems > 0)
            {
               if (curIndex >= remainingItems)
                  curIndex = remainingItems - 1;
               sm.setSelectionInterval(curIndex, curIndex);
               updateParameterPanel();
            }
            else
            {
               // no more items in list, so clear panel and reset index
               m_parameterPanel.clear();
               m_curIndex = -1;
            }
            m_bMove = false;

         }
      });

      jPanelListBox.add(m_editableListBox);

   }

   /**
    *   gets the data for the combobox editor of the editable list box. If
    * necessary, the server exits are cataloged. Once created, the list is
    * cached. Successive calls return the cached list.
    *
    * @return array of JavaExitPropertyDialogData objects, one for each exit
    * that was cataloged, or <code>null</code> if no exits were cataloged
    */
   private JavaExitsPropertyDialogData[] getComboBoxData()
   {
      if (null != m_catalogedExitData)
         return m_catalogedExitData;

      JavaExitsPropertyDialogData[] comboBoxData = null;

      //get the server exits
      if (m_vServerExits == null)
         m_vServerExits = getServerExits(m_collection);
      // it can return null now

      if (m_vServerExits != null)
      {
         int iComboSize = m_vServerExits.size();
         if (iComboSize > 0)
         {
            comboBoxData = new JavaExitsPropertyDialogData[iComboSize];
            for (int i = 0; i < iComboSize; i++)
            {
               IPSExtensionDef exit = (IPSExtensionDef) m_vServerExits.get(i);
               comboBoxData[i] = new JavaExitsPropertyDialogData(exit);
            }
         }
      }
      m_catalogedExitData = comboBoxData;
      return comboBoxData;
   }

   /**
    *   Initializes the list in the editable list box
    *
    */
   private void initEditableListBox()
   {
      for (int i = 0; i < m_vData.size(); i++)
      {
         JavaExitsPropertyDialogData dialogData =
             m_vData.get(i);
         m_editableListBox.addRowValue(dialogData);
      }
   }

   /**
    *   initializes the listeners
    *
    */
   private void initListeners()
   {
      ButtonListener buttonListener = new ButtonListener();

      ListItemListener listListener = new ListItemListener();
      ListFocusListener listFocusListener = new ListFocusListener();
      
      m_editableListBox.getSelectionModel().addListSelectionListener(
         listListener);
      m_editableListBox.getList().addFocusListener(listFocusListener);

      jButtonMoveUp.addActionListener(buttonListener);
      jButtonMoveDown.addActionListener(buttonListener);
      (
         (EditableListBoxEditor) m_editableListBox
            .getCellEditor())
            .addCellEditorListener(
         new EditorListener());

   }

   /** Inner class to implement CellEditorListener interface for handling the
      * editing stopped event.
   */
   class EditorListener implements CellEditorListener
   {
      public void editingStopped(ChangeEvent e)
      {
         //         System.out.println("Editing stopped");
         updateParameterPanel();
      }
      public void editingCanceled(ChangeEvent e)
      {
         //         System.out.println("Editing cancelled");
      }

   }

   /**
    * Inner class implements ListSelectionListener interface.
    */
   class ListItemListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         //      System.out.println("In ListItemListener");
         onListSelectionChanged();
      }
   }
   
   /**
    * This class tracks the focus on the list widget so the description
    * will be taken down if the focus moves from the list.    
    */
   class ListFocusListener implements FocusListener
   {
      /* (non-Javadoc)
       * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
       */
      public void focusGained(FocusEvent arg0)
      {
         onListSelectionChanged();
      }

      /* (non-Javadoc)
       * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
       */
      public void focusLost(FocusEvent arg0)
      {
         // Remove current text
         m_parameterPanel.setDescriptionText(null);
      }      
   }

   /**
    * Inner class implements ActionListener interface.
    */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         JButton button = (JButton) e.getSource();
         if (button == jButtonMoveUp)
            onMoveUp();
         else if (button == jButtonMoveDown)
            onMoveDown();
      }
   }
   
   /**
    *   Handler for list selection change event to update the parameter table.
    */
   private void onListSelectionChanged()
   {
      if (m_bMove)
         return;

      int prevIndex = m_curIndex;
      m_curIndex = m_editableListBox.getSelectionModel().getMinSelectionIndex();
      if (m_curIndex < 0 || m_curIndex >= m_editableListBox.getItemCount())
         return;
      //      System.out.println("Previous selected index = "+prevIndex);
      //      System.out.println("Current selected index = "+m_curIndex);

      saveParameterPanelData(prevIndex);
      updateParameterPanel();
   }

   /**
    *   saves the table data in the JavaExit at the specified index.
    *
    * @param index The index of the exit which is to receive the parameter
    * values. If not with the range of allowed values ( 0 to # of exits),
    * nothing is done.
    *
    */
   private void saveParameterPanelData(int index)
   {
      if (index < 0 || index >= m_editableListBox.getItemCount())
         return;
      Object o = m_editableListBox.getRowValue(index);

      JavaExitsPropertyDialogData data = (JavaExitsPropertyDialogData) o;
      if (data == null)
         return;
      //set the parameter Values
      PSExtensionParamValue[] params = m_parameterPanel.getParameters();
      data.setParamValues(params);
   }

   /**
    * Gets the object at the current index in the editable list box. If this object
    * is a string, it is converted to a JavaExitsPropertyDialogData object and
    * put in the editable list at the current position, replacing the string. If
    * the string doesn't name a valid exit, a message box is displayed to the user
    * and the row at the current index is removed.
    *
    * @return <code>true</code> if new exit object was added to the editable list
    * box at the current index, <code>false</code> if the current index is < 0 or
    * the specified exit can't be found
    */
   private boolean addServerExitToDialogData()
   {
      int curIndex =
         m_editableListBox.getSelectionModel().getMinSelectionIndex();
      if (curIndex < 0)
         return false;
      Object o = m_editableListBox.getRowValue(curIndex);
      if (o instanceof String)
      {
         // find a call by this name, then make a copy of it w/ empty param values
         String strName = (String) o;
         JavaExitsPropertyDialogData data = getData(strName);
         if (null == data)
         {
            IPSExtensionDef exit = getServerExit(strName);
            if (null == exit)
            {
               JOptionPane.showMessageDialog(
                  this,
                  getResources().getString("InvalidExit"),
                  getResources().getString("InvalidExitTitle"),
                  JOptionPane.OK_OPTION);
               m_editableListBox.removeItemAt(curIndex);
               Component editor =
                  ((JComboBox) m_editableListBox.getCellEditorComponent())
                     .getEditor()
                     .getEditorComponent();
               if (null != editor && editor instanceof JTextComponent)
                  // TODOph: This doesn't work for some reason and I've run out of time to debug it
                   ((JTextComponent) editor).setText(null); // clear field

               return false;
            }
            data = new JavaExitsPropertyDialogData(exit);
         }
         else
            data = new JavaExitsPropertyDialogData(data.getExit());
         m_editableListBox.setRowValue(data, curIndex);
      }
      return true;
   }

   /**
    *   handler for move up event
    *
    */
   private void onMoveUp()
   {
      int i = m_editableListBox.getSelectionModel().getMinSelectionIndex();

      if (i <= 0)
         return;

      Object o = m_editableListBox.getRowValue(i);
      m_editableListBox.removeItemAt(i);
      m_editableListBox.insertRowValue(i - 1, o);
      m_bMove = true;
      m_editableListBox.getSelectionModel().setSelectionInterval(i - 1, i - 1);
      m_curIndex = i - 1;
      m_bMove = false;
   }

   /**
    *   handler for move down event
    *
    */
   private void onMoveDown()
   {
      //      System.out.println("Move Down");
      int i = m_editableListBox.getSelectionModel().getMinSelectionIndex();
      //      System.out.println("Selected index ="+i);
      int items = m_editableListBox.getItemCount();
      if (i < 0 || i >= items - 1)
         return;
      Object o = m_editableListBox.getRowValue(i);
      m_editableListBox.removeItemAt(i);
      m_editableListBox.insertRowValue(i + 1, o);
      m_bMove = true;
      m_editableListBox.getSelectionModel().setSelectionInterval(i + 1, i + 1);
      m_curIndex = i + 1;
      m_bMove = false;

   }

   /**
    *initializes the dialog fields
    *
    */
   private void initDialogFields()
   {
      initEditableListBox();
      //      System.out.println("itemcount for list box ="+m_editableListBox.getItemCount());
      m_tableModel = m_parameterPanel.getTableModel();

      ValueSelectorDialogHelper vsHelper =
         new ValueSelectorDialogHelper(null, m_osPageDatatank);
      Vector vDataTypes = vsHelper.getDataTypes();
      m_parameterPanel.setDataTypesForValueSelector(vDataTypes);

      updateParameterPanel();
   }

   /**
    *   Updates the parameter panel with data for the current selection in editable list box
    *
    */
   private void updateParameterPanel()
   {
      m_curIndex = m_editableListBox.getSelectionModel().getMinSelectionIndex();
      if (m_curIndex < 0)
         return;

      if (m_tableModel == null)
         return;

      while (m_tableModel.getRowCount() > 0)
      {
         m_tableModel.deleteRow(0);
      }

      Object o = m_editableListBox.getRowValue(m_curIndex);

      if (o instanceof String)
      {
         if (!addServerExitToDialogData())
            return;
      }

      JavaExitsPropertyDialogData dialogData =
         (JavaExitsPropertyDialogData) m_editableListBox.getRowValue(
            m_curIndex);
      if (dialogData == null)
         return;
      m_parameterDefs = dialogData.getParamDefCollection();

      // Fill in description in the panel
      IPSExtensionDef exit = dialogData.getExit();
      String desc = exit.getInitParameter("com.percussion.user.description");
      m_parameterPanel.setDescriptionText(desc);

      // fill in the parameters for the current selection
      int size;
      if (m_parameterDefs != null)
      {
         PSExtensionParamValue[] parameterVals;
         parameterVals = dialogData.getParamValues();
         size = m_parameterDefs.length;
         for (int i = 0; i < size; i++)
         {
            IPSExtensionParamDef par = m_parameterDefs[i];

            IPSReplacementValue value = null;
            if (parameterVals[i] != null)
               value = parameterVals[i].getValue();
            // replace any null values with an empty text literal as a
            // placeholder
            if (value == null)
               value = new PSTextLiteral("");

            m_parameterPanel.appendParameter(
               par.getName(),
               value,
               par.getDescription());
         }
      }
      // append empty rows
      while (m_parameterPanel.getRowCount() < 24)
         m_parameterPanel.appendParameter("", "", "");

   }

   /**
    *   gets the Java exits on the server by catalogging, returning all exits
    * allowed by the supplied call set.
    *
    * @param callSet If not null, the returned exits will be limited to those
    * types allowed by the supplied call set. If <code>null</code>, no exits
    * are returned.
    *
    * @return All cataloged exits that are allowed by the call set (if not null),
    * or all exits otherwise.
    *
    */
   public ArrayList getServerExits(OSExitCallSet callSet)
   {
      // CLR added to fix problem adding all EXITS
      ArrayList<IPSExtensionDef> allowedExits = null;
      // CLR added to fix problem adding all EXITS
      if (callSet != null) 
      {

         List<IPSExtensionDef> allExits =
            CatalogServerExits.getCatalog(
               null,
               CatalogExtensionCatalogHandler.JAVA_EXTENSION_HANDLER_NAME,
               false);

         if (allExits != null)
         {
            allowedExits = new ArrayList<IPSExtensionDef>();
            int size = allExits.size();
            for (int i = 0; i < size; i++)
            {
               IPSExtensionDef def = (IPSExtensionDef) allExits.get(i);
               if (def != null && callSet.isAllowed(def))
                  allowedExits.add(def);
            }
         }
      }
      return allowedExits;
   }

   /**
    *
    * Returns the JavaExitsPropertyDialogData object stored in the m_vData vector that has the passed in name.
    *   Returns null if the passed in name is null or empty or if the data for the specified exit name is not found.
    */
   private JavaExitsPropertyDialogData getData(String strExitName)
   {
       if (strExitName == null || strExitName.trim().equals(""))
         return null;
      if (m_vData == null || m_vData.isEmpty())
         return null;
      for (int i = 0; i < m_vData.size(); i++)
      {
         JavaExitsPropertyDialogData data = m_vData.get(i);
         String strName = data.getName();
         if (strName.equals(strExitName))
         {
            return data;
         }
      }
      return null;
   }

   /**
    *
    * Returns the object stored in the m_vServerExits vector that has the passed in name.
    *   Returns null if the passed in name is null or empty.
    */
   private IPSExtensionDef getServerExit(String strExitName)
   {
       if (strExitName == null || strExitName.trim().equals(""))
         return null;
      if (m_vServerExits == null || m_vServerExits.isEmpty())
         return null;
      for (Object m_vServerExit : m_vServerExits) {
         IPSExtensionDef exit = (IPSExtensionDef) m_vServerExit;
         String strName = exit.getRef().getExtensionName();
         if (strName.equals(strExitName)) {
            return exit;
         }
      }
      return null;
   }

   /**
    * Flag to indicate whether this dialog was exited via OK button or not.
    * Initialized to <code>false</code>, only changed in onOk.
    * after that.
    */
   private boolean m_okExit = false;
   
   private boolean m_bMove = false;
   private OSExitCallSet m_collection = null;
   private ArrayList m_vServerExits = null;

   // contains IPSExtensionDef objects in the Combo box of editable list box
   private ArrayList<JavaExitsPropertyDialogData> m_vData;
   // contains vector of JavaExitsPropertyDialogData objects

   /**
    * Contains a data object for each exit that was cataloged. For use in the
    * exit drop list. It is set once, then never modified again. May be <code>
    * null</code>.
    */
   private JavaExitsPropertyDialogData[] m_catalogedExitData = null;

   /**
    * the current parameter definitions
    */
   private IPSExtensionParamDef[] m_parameterDefs = null;

   private OSPageDatatank m_osPageDatatank = null;

   private EditableListBox m_editableListBox = null;
   /**
    * The index of the item in the editable list box that was most recently
    * processed, or -1 if the last processing was to delete the last row.
    */
   private int m_curIndex = -1;

   private UTStandardCommandPanel m_commandPanel = null;
   private UTParameterPanel m_parameterPanel = null;
   private UTParameterPanelTableModel m_tableModel = null;

   private boolean m_bModified = false;

   //{{DECLARE_CONTROLS
   JPanel jPanelListBox;
   JPanel jPanelMove;
   JButton jButtonMoveUp;
   JButton jButtonMoveDown;
   JLabel jLabelMove;
   JPanel jPanelCommand;
   JPanel jPanelParameter;

}
