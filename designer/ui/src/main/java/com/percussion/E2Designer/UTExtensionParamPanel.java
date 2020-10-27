/*[ UTExtensionParamPanel.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A table that allows users to manipulate extension parameters.
 */
public class UTExtensionParamPanel extends JPanel
{
   /**
    * Constructor.
    *
    * @param def The extension def; params will be gotten from here.
    * <CODE>null</CODE> is okay, creates an empty table.
    *
    * @param paramTypes An array of strings that contains the various types of
    * parameters. <CODE>null</CODE> is okay, lists no types.
    *
    * @param bIsTypeEditable <CODE>true</CODE> if the type combobox should be
    * user editable. <CODE>false</CODE> if not.
    *
    * @param bWantTitle <CODE>true</CODE> if you want a "Parameters" title on
    * top of the table.
    */
   public UTExtensionParamPanel( IPSExtensionDef def, String[] paramTypes,
                                 boolean bIsTypeEditable, boolean bWantTitle )
   {
      super();
      initPanel( paramTypes, bIsTypeEditable, bWantTitle );
      initData( def );
   }


   /**
    * Constructor.
    *
    * @param paramTypes An array of strings that contains the various types of
    * parameters. <CODE>null</CODE> is okay, lists no types.
    *
    * @param isTypeEditable <CODE>true</CODE> if the type combobox should be
    * user editable. <CODE>false</CODE> if not.
    *
    * @param bWantTitle <CODE>true</CODE> if you want a "Parameters" title on
    * top of the table.
    */
   public UTExtensionParamPanel( String[] paramTypes, boolean isTypeEditable,
                                 boolean bWantTitle )
   {
      this( null, paramTypes, isTypeEditable, bWantTitle );
   }


   /**
    * Stops the cell editor edit mode. It automatically sets the data that is in
    * edit into the table cell.
    */
   public void stopCellEditing()
   {
      TableCellEditor editor = m_jTableParams.getCellEditor();

      if ( null != editor )
         editor.stopCellEditing();
   }


   /**
    * Retrieves all of the function parameter information from the ui component
    * and builds parameter definitions for each parameter.
    *
    * @return A map containing 1 or more PSExtensionParamDef objects as the
    * value and the param name as the corresponding key. If there are no
    * params, <code>null</code> is returned.
    */
   public Iterator getRuntimeParams()
   {
      int iRowCount = m_jTableParams.getRowCount();
      TableColumn nameColumn = m_jTableParams.getColumn( PARAM_NAME );
      int iName = nameColumn.getModelIndex();
      TableColumn typeColumn = m_jTableParams.getColumn( PARAM_TYPE );
      int iType = typeColumn.getModelIndex();
      TableColumn descriptionColumn = m_jTableParams.getColumn( PARAM_DESC );
      int iDesc = descriptionColumn.getModelIndex();

      List<PSExtensionParamDef> list = new ArrayList<PSExtensionParamDef>();
      for(int i=0; i<iRowCount; i++)
      {
         String strName = (String)m_jTableParams.getValueAt(i, iName);
         String strType = (String)m_jTableParams.getValueAt(i, iType);
         String strDesc = (String)m_jTableParams.getValueAt(i, iDesc);
         if(strName == null || 0 == strName.trim().length()) //skip this row
            continue;
         if(strType == null || 0 == strType.trim().length()) //skip this row
            continue;

         PSExtensionParamDef exitParamDef= null;
         try
         {
            exitParamDef = new PSExtensionParamDef( strName.trim(),
                                                    strType.trim() );
            exitParamDef.setDescription( strDesc );
         }
         catch(IllegalArgumentException e)
         {
            PSDlgUtil.showError(e, false, ms_res.getString("error"));
         }

         list.add( exitParamDef );
      }//end for loop

      return list.listIterator();
   }


   /**
    * Updates the parameters table with the entries from the passed in
    * <CODE>IPSExtensionDef</CODE> object.
    *
    * @param extDef The extension def.
    *
    * @see IPSExtensionDef
    */
   public void updateParamsTable( IPSExtensionDef extDef )
   {
      updateParamsTable( extDef, null, m_cbTypeEditor.isEditable() );
   }

   /**
    * Updates the parameters table with the entries from the passed in
    * <CODE>IPSExtensionDef</CODE> object.
    *
    * @param extDef The extension def.
    *
    * @param paramTypes An array of parameter types. These types will be made
    * available in the type column of the param table. <CODE>null</CODE> will
    * list nothing for the available param types.
    *
    * @param isTypeEditable <CODE>true</CODE> if the user is allowed to enter
    * their own type.
    *
    * @see IPSExtensionDef
    */
   public void updateParamsTable(IPSExtensionDef extDef,
                                 String[] paramTypes,
                                 boolean isTypeEditable )
   {
      if( extDef != null )
      {
         int iRowCount = m_jTableParams.getRowCount();
         if(iRowCount > 0)
         {
            while(m_jTableParams.getRowCount() > 0)
               ((DefaultTableModel)m_jTableParams.getModel()).removeRow(0);
         }

         Iterator rtParams = extDef.getRuntimeParameterNames();
         while ( rtParams.hasNext())
         {
            IPSExtensionParamDef paramDef =
               extDef.getRuntimeParameter((String) rtParams.next());
            if(paramDef == null)
               throw new IllegalArgumentException(
                               "Null PSExtensionParamDef object in collection");

            Object [] data = {paramDef.getName(), paramDef.getDataType(),
               paramDef.getDescription()};
            ((DefaultTableModel)m_jTableParams.getModel()).addRow(data);
         }
         addEmptyRowsToTable();
      }

      if ( null != paramTypes && 0 < paramTypes.length )
      {
         int iTypeCount = m_cbTypeEditor.getItemCount();
         for ( int i = iTypeCount - 1; 0 <= i; i-- )
            m_cbTypeEditor.removeItemAt( i );

         iTypeCount = paramTypes.length;
         for ( int i = 0; i < iTypeCount; i++ )
            m_cbTypeEditor.addItem( paramTypes[i] );
      }

      m_cbTypeEditor.setEditable( isTypeEditable );
   }


   /**
    * Validate the data in the parameter table. Skips a whole row if the name
    * and/or the type is an empty string. The data in the description column is
    * ignored.
    *
    * @return <CODE>false</CODE> if any of the names in the table contains
    * invalid characters. <CODE>true</CODE> if the names are valid.
    */
   public boolean validateParamTableData()
   {
      //if ( null == m_jTableParams )
      //   return true;

      int iRowCount = m_jTableParams.getRowCount();
      TableColumn nameColumn = m_jTableParams.getColumn( PARAM_NAME );
      int iName = nameColumn.getModelIndex();
      TableColumn typeColumn = m_jTableParams.getColumn( PARAM_TYPE );
      int iType = typeColumn.getModelIndex();

      HashMap<String, String> map = new HashMap<String, String>(10); // used to check for dupes
      for(int i=0; i<iRowCount; i++)
      {
         String strName = (String)m_jTableParams.getValueAt(i, iName);
         String strType = (String)m_jTableParams.getValueAt(i, iType);
         if(strName == null || strName.trim().equals("")) //skip this row
            continue;
         if(strType == null || strType.trim().equals("")) //skip this row
            continue;

         boolean bValidName = isValidParamName(strName);
         if(!bValidName)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString( "PARAMETER_NAME" )+" "+"'"+strName+ "'"+" "+
               ms_res.getString( "NOT_VALID_NAME" )+"\n"+
               ms_res.getString( "FIRST_CHAR" )+"\n"+
               ms_res.getString( "SUBSEQUENT_CHARS" ),
               ms_res.getString( "error" ));
            m_jTableParams.setRowSelectionInterval(i,i);
            m_jTableParams.setColumnSelectionInterval(iName, iName);
            return false;
         }

         if ( null != map.get( strName ) )
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString( "PARAMETER_NAME" )+" "+"'"+strName+"'"+" "+
               ms_res.getString( "DUPE" )+"\n"+ ms_res.getString( "UNDUPE" ),
               ms_res.getString( "error" ));
            m_jTableParams.setRowSelectionInterval(i,i);
            m_jTableParams.setColumnSelectionInterval(iName, iName);
            return false;
         }
         map.put( strName, strName );

      }//end for loop

      return true;
   }


   /**
    * @return <CODE>true</CODE> if the table has no data.
    */
   public boolean isParamTableEmpty()
   {
      //   any unsaved param defs?
      int iRowCount = m_jTableParams.getRowCount();
      TableColumn nameColumn = m_jTableParams.getColumn(PARAM_NAME);
      int iName = nameColumn.getModelIndex();
      TableColumn typeColumn = m_jTableParams.getColumn(PARAM_TYPE);
      int iType = typeColumn.getModelIndex();
      TableColumn descriptionColumn = m_jTableParams.getColumn(PARAM_DESC);
      int iDesc = descriptionColumn.getModelIndex();

      int i = 0;
      for(; i<iRowCount; i++)
      {
         String strName = (String)m_jTableParams.getValueAt(i, iName);
         String strType = (String)m_jTableParams.getValueAt(i, iType);
         String strDesc = (String)m_jTableParams.getValueAt(i, iDesc);
         if( !(strName == null || 0 == strName.trim().length())
              || !(strType == null || 0 == strType.trim().length())
              || !(strDesc == null || 0 == strDesc.trim().length()))
         {
            break;
         }
      }
      return ( i < iRowCount );
   }


   /**
    * Is this a valid parameter name?
    *
    * @param   name         the name to validate
    *
    * @return               <code>true</code> if the parameter name follows
    *                      the syntax for JavaScript,
    *                      <code>false</code> otherwise
    */
   private boolean isValidParamName(String name)
   {
      if (name == null)
         return false;

      int strLen = name.length();
      if (strLen == 0)
         return false;

      /* First character must be '_' , '$' or an alphabet
       * For all JavaScript valid characters refer to ecmascript spec
       */
      if(!Character.isJavaIdentifierStart(name.charAt(0)))
         return false;

      /* Subsequent chars may be '_', '$',an alphabet, or a digit
       * For all JavaScript valid characters refer to ecmascript spec
       */
      for (int i = 1; i < strLen; i++)
      {
         if(!Character.isJavaIdentifierPart(name.charAt(i)))
            return false;
      }

      return true;
   }


   /**
    * Initializes the param table with the param data from an extension def.
    *
    * @param def The extension def. <CODE>null</CODE> will be ignored.
    */
   private void initData( IPSExtensionDef def )
   {
      updateParamsTable( def );
      addEmptyRowsToTable();
   }


   /**
    * @return The <CODE>JPanel</CODE> containing a parameters table for this
    * extension.
    *
    * @param paramTypes An array of parameter types. These types will be made
    * available in the type column of the param table. <CODE>null</CODE> will
    * list nothing for the available param types.
    *
    * @param isTypeEditable <CODE>true</CODE> if the user is allowed to enter
    * their own type.
    *
    * @param bWantTitle <CODE>true</CODE> if you want a "Parameters" title on
    * top of the table.
    */
   private void initPanel( String[] paramTypes, boolean isTypeEditable,
                           boolean bWantTitle )
   {
      String [] header = { PARAM_NAME,
                           PARAM_TYPE,
                           PARAM_DESC };

      // create the table
      m_jTableParams = new UTJTable(0,3);
      DefaultTableModel dtm;
      dtm =  (DefaultTableModel)m_jTableParams.getModel();
      dtm.setColumnIdentifiers(header);

      // set table selection and reordering
      m_jTableParams.getTableHeader().setReorderingAllowed(false);
      m_jTableParams.setSelectionMode(
                                    DefaultListSelectionModel.SINGLE_SELECTION);
      m_jTableParams.setColumnSelectionAllowed(false);
      TableRowSelectionListener lsl = new TableRowSelectionListener();
      m_jTableParams.getSelectionModel().addListSelectionListener( lsl );
      this.addEmptyRowsToTable();

      TableColumn nameColumn = m_jTableParams.getColumn( PARAM_NAME );
      nameColumn.setPreferredWidth(80);

      TableColumn descColumn = m_jTableParams.getColumn( PARAM_DESC );

      TableColumn typeColumn = m_jTableParams.getColumn( PARAM_TYPE );
      typeColumn.setPreferredWidth(80);
      //Create a text field
      m_textFieldEditor = new JTextField();
      TypeCellEditor textEditor = new TypeCellEditor(m_textFieldEditor);
      textEditor.addCellEditorListener(textEditor);

      //Use the text field as the editor in the 'Name' and 'Description' columns.
      nameColumn.setCellEditor(textEditor);
      descColumn.setCellEditor(textEditor);

      // Create a combo box for the Type column
      m_cbTypeEditor = new JComboBox();
      m_cbTypeEditor.setEditable( isTypeEditable );
      if ( null != paramTypes && 0 < paramTypes.length )
      {
         for ( int i = 0; i < paramTypes.length; i++ )
            m_cbTypeEditor.addItem( paramTypes[i] );
      }

      TypeCellEditor editor = new TypeCellEditor(m_cbTypeEditor);
      editor.addCellEditorListener( editor );
      // Use the combo box as the editor in the "Type" column.
      typeColumn.setCellEditor( editor );

      // Set a tooltip for the type column renderer.
      DefaultTableCellRenderer typeColumnRenderer =
                                                 new DefaultTableCellRenderer();
      typeColumnRenderer.setToolTipText(ms_res.getString("tooltipClickForType"));
      typeColumn.setCellRenderer(typeColumnRenderer);

      JScrollPane jScrollPaneTable = new JScrollPane();
      jScrollPaneTable.getViewport().add(m_jTableParams);
      jScrollPaneTable.setPreferredSize( new Dimension( 350, 90 ) );

      setLayout( new BorderLayout() );

      if ( bWantTitle )
      {
         JLabel label = new JLabel( ms_res.getString("param") );
         JPanel labelPanel = new JPanel();
         labelPanel.setLayout( new BoxLayout( labelPanel, BoxLayout.X_AXIS ) );
         labelPanel.add( Box.createHorizontalStrut( 7 ) );
         labelPanel.add( label );
         labelPanel.add( Box.createHorizontalGlue() );

         add( labelPanel, BorderLayout.NORTH );
      }
      add( jScrollPaneTable, BorderLayout.CENTER );
   }


   /**
    * adds an additional row if last row is selected.
    */
   private void rowSelectionChanged()
   {
      int i = m_jTableParams.getSelectedRow();

      if (i < 0)
         m_jTableParams.clearSelection();
      else
      {
         if (i == (m_jTableParams.getRowCount() - 1)) // last row selected
            addEmptyRowsToTable();
         m_jTableParams.setRowSelectionInterval(i, i);
      }
   }


   /**
    * Adds an additional row to the parameters table.
    */
   private void addEmptyRowsToTable()
   {
      //add two rows
      Object [] data ={"","",""};
      for (int i=0; i<2; i++)
         ((DefaultTableModel)m_jTableParams.getModel()).addRow(data);
   }


  /**
   * Implements {@link ListSelectionListener} for handling the param table
   * selection changes.
   */
   private class TableRowSelectionListener implements ListSelectionListener
   {
      public void valueChanged( ListSelectionEvent e )
      {
         rowSelectionChanged();
      }
   }


   /** Constructs the cell editors for the table params' columns.
    *
    */
   private class TypeCellEditor extends DefaultCellEditor
                                implements CellEditorListener
   {
      /**Constructs a TypeCellEditor that uses a combo box.
       *
       */
      public TypeCellEditor( JComboBox box )
      {
         super( box );
      }

      /**Constructs a TypeCellEditor that uses a text field.
       *
       */
      public TypeCellEditor(JTextField field)
      {
         super (field);
      }

      /** Asks the editor if it can start editing using anEvent.
       *  If anEvent is a MouseEvent and a count of clicks is greater or equal
       *  to a default count of clicks returns <CODE>true</CODE>, otherwise
       *  <CODE>false</CODE>.
       */
      public boolean isCellEditable(EventObject anEvent)
      {
         if(anEvent instanceof MouseEvent)
         {
            MouseEvent event = (MouseEvent)anEvent;
            if(event.getClickCount() >= this.getClickCountToStart())
               return true;
         }

         return false;
      }

      public void editingCanceled( ChangeEvent evt )
      {
         //System.out.println( "canceled" );
      }

      public void editingStopped( ChangeEvent evt )
      {
         //System.out.println( "stopped" );

         /*
         DefaultCellEditor ed = (DefaultCellEditor)evt.getSource();

         JComboBox box = (JComboBox)ed.getComponent();
         String obj = (String)box.getEditor().getItem();
         if ( !obj.equals( "" ) )
         {
            if ( null == m_map.get( obj ) )
               box.addItem( obj );

            m_map.put( obj, obj );
         }
         */
      }

      //private HashMap m_map = new HashMap( 10 );
   }


   /** A JTable that displays the parameter for the chosen extension. */
   private UTJTable m_jTableParams = null;

   /** The param type table cell editor. */
   private JComboBox m_cbTypeEditor = null;

   /**The params 'Name' and 'Description' table cell editor. Gets initialized in
    * {@link #initPanel(String[], boolean, boolean) initPanel()}
    */
   private JTextField m_textFieldEditor = null;

   /** A package that contains all the display strings.
    */
   private static ResourceBundle ms_res = null;
   static {
      try
      {
         ms_res = ResourceBundle.getBundle(
               "com.percussion.E2Designer.UTExtensionParamPanelResources",
               Locale.getDefault() );
      }
      catch ( MissingResourceException e )
      {
         e.printStackTrace();
      }
   }

   private static final String PARAM_NAME = ms_res.getString( "paramName" );
   private static final String PARAM_TYPE = ms_res.getString( "paramType" );
   private static final String PARAM_DESC = ms_res.getString( "paramDesc" );

   /** A convenience String array for constructing java script extension
    * parameter types.
    * */
   public static final String[] PARAM_TYPE_JAVASCRIPT = {
                  "String", "Boolean", "Number", "Date" };
}
