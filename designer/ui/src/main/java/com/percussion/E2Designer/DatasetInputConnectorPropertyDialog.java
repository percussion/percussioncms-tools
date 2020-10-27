/******************************************************************************
 *
 * [ DatasetInputConnectorPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSReplacementValueFactory;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSXmlField;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;
import com.percussion.util.PSSortTool;
import com.percussion.workbench.ui.util.PSResourceLoader;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The dataset input connector property dialog.
 */
////////////////////////////////////////////////////////////////////////////////
public class DatasetInputConnectorPropertyDialog extends PSEditorDialog
                                                 implements KeyListener,
                                                            CellEditorListener,
                                                            ClipboardOwner
{
   /**
   * Construct the default dataset input connector property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public DatasetInputConnectorPropertyDialog()
   {
        super();
        initDialog();
   }

   /**
   * Construct the default dataset input connector property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public DatasetInputConnectorPropertyDialog(JFrame parent, OSRequestor requestor)
   {
        super(parent);
      this.setLocationRelativeTo(parent);

      m_requestor = requestor;
        initDialog();
   }

   /**
    * Create the parameter selection table view.
    *
    * @return   JScrollPane      the table view, a scrollable pane
    */
   //////////////////////////////////////////////////////////////////////////////
   private JScrollPane createParameterTableView()
   {
      // create value selector dialogs
      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
            (OSBackendDatatank) null, null);
      m_parameterVariableEditor = new ValueSelectorDialog(this,
                                                        h.getDataTypes(),
                                                        null);
      m_parameterValueEditor = new ValueSelectorDialog(this,
                                                     h.getDataTypes(),
                                                     null);

      // define back-end column editor as combo box editor
      ValueSelectorCellEditor nameEditor = new ValueSelectorCellEditor(
         m_parameterVariableEditor, null);
      nameEditor.addCellEditorListener(this);
      m_parameterView.getColumn(
         ConditionalTableModel.VARIABLE_COL_NAME ).setCellEditor(nameEditor);

      DefaultCellEditor operatorEditor = new DefaultCellEditor(
         new UTOperatorComboBox());
      operatorEditor.addCellEditorListener(this);
      m_parameterView.getColumn(
         ConditionalTableModel.OPERATOR_COL_NAME ).setCellEditor(operatorEditor);

      // set operator cell renderer
      m_parameterView.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setCellRenderer(new UTOperatorComboBoxRenderer());

      ValueSelectorCellEditor valueEditor =
         new ValueSelectorCellEditor(m_parameterValueEditor, null);
      valueEditor.addCellEditorListener(this);
      m_parameterView.getColumn( ConditionalTableModel.VALUE_COL_NAME )
         .setCellEditor(valueEditor);

      // define the cell renderers
      UTTextFieldCellRenderer renderer = new UTTextFieldCellRenderer();
      m_parameterView.setDefaultRenderer(m_parameterView.getColumn(
         ConditionalTableModel.VALUE_COL_NAME ).getClass(), renderer);

      // set preferred column sizes
      m_parameterView.getColumn( ConditionalTableModel.VARIABLE_COL_NAME )
         .setPreferredWidth(150);
      m_parameterView.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setPreferredWidth(40);
      m_parameterView.getColumn( ConditionalTableModel.VALUE_COL_NAME )
         .setPreferredWidth(170);

      /* TODOph: What I want is to send a message when the entire table looses focus.
         However, this sends a message every time any editor in this table looses
         focus. Don't have time to figure it out now. */
      m_parameterView.addFocusListener( m_urlChanged );

      // create the scroll pane and add all its contents
      JScrollPane pane = new JScrollPane(m_parameterView);
      pane.setPreferredSize(PARAMETER_TABLE_SIZE);

      return pane;
   }

   /**
    * Create the parameter validation table view.
    *
    * @return   JScrollPane      the table view, a scrollable pane
    */
   //////////////////////////////////////////////////////////////////////////////
   private JScrollPane createValidationTableView()
   {
      // create value selector dialogs
      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
            (OSBackendDatatank) null, null);
      m_validationVariableEditor = new ValueSelectorDialog(this,
                                           h.getDataTypes(),
                                           null);
      m_validationValueEditor = new ValueSelectorDialog(this,
                                         h.getDataTypes(),
                                         null);
      // define special column editors
      ValueSelectorCellEditor nameEditor = new ValueSelectorCellEditor(
         m_validationVariableEditor, null);
      m_validationView.getColumn( ConditionalTableModel.VARIABLE_COL_NAME )
         .setCellEditor(nameEditor);

      DefaultCellEditor operatorEditor =
         new DefaultCellEditor(new UTOperatorComboBox());
      m_validationView.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setCellEditor(operatorEditor);

      // set operator cell renderer
      m_validationView.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setCellRenderer(new UTOperatorComboBoxRenderer());

      ValueSelectorCellEditor valueEditor =
         new ValueSelectorCellEditor(m_validationValueEditor, null);
      m_validationView.getColumn( ConditionalTableModel.VALUE_COL_NAME )
         .setCellEditor(valueEditor);

      DefaultCellEditor boolEditor =
         new DefaultCellEditor(new UTBooleanComboBox());
      m_validationView.getColumn( ConditionalTableModel.BOOL_COL_NAME )
         .setCellEditor(boolEditor);

      // set preferred column sizes
      m_validationView.getColumn( ConditionalTableModel.VARIABLE_COL_NAME )
         .setPreferredWidth(150);
      m_validationView.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setPreferredWidth(50);
      m_validationView.getColumn( ConditionalTableModel.VALUE_COL_NAME )
         .setPreferredWidth(150);
      m_validationView.getColumn( ConditionalTableModel.BOOL_COL_NAME )
         .setPreferredWidth(20);
      m_validationView.addFocusListener( m_urlChanged );

      // create the scroll pane and add all its contents
      JScrollPane pane = new JScrollPane(m_validationView);
      pane.setPreferredSize(VALIDATION_TABLE_SIZE);

      return pane;
  }

   /**
    * Create application panel
    *
    * @return   JPanel   the panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createApplicationPanel()
   {
      // read only
      m_application.setEditable(false);
      m_application.setBackground(Color.lightGray);

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panel.add(new UTMnemonicLabel(getResources(), "root", m_application));
      panel.add(m_application);

      return panel;
   }

   /**
    * A small class used to notify the sample URL that it needs to update
    * itself. Use the single instance m_urlChanged.
    */
   private class URLUpdater extends FocusAdapter
   {
      @Override
      @SuppressWarnings("unused")
      public void focusLost(FocusEvent e)
      {
         onUrlChanged();
      }
   }

   private URLUpdater m_urlChanged = new URLUpdater();

   /**
    * Create request URL panel
    *
    * @return   JPanel   the panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createRequestURLPanel()
   {
      m_requestURL.setEditable(true);
      m_requestURL.addFocusListener( m_urlChanged );

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panel.add(new UTMnemonicLabel(getResources(), "requestURL", m_requestURL));
      panel.add(m_requestURL);

      return panel;
   }

   /**
    * Create the sample URL field
    *
    * @return   JPanel   the panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createSampleURLPanel()
   {
      m_copy.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent event)
         {
            onCopy();
         }
      });
      m_copy.setPreferredSize(new Dimension(130, 30));
      m_copy.setMnemonic(getResources().getString("mn_copy").charAt(0));
      m_sampleURL.setLineWrap(true);
      m_sampleURL.setEnabled(false);
      JScrollPane pane = new JScrollPane(m_sampleURL);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder( createGroupBorder( getResources().getString("sampleURL")));
      panel.add(m_copy, BorderLayout.WEST);
      panel.add(pane, BorderLayout.CENTER);

      return panel;
   }

   /**
    * Create the view panel
    *
    * @return   JPanel      the view panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createViewPanel()
   {
      JPanel p1 = new JPanel(new BorderLayout());
      p1.setBorder(new EmptyBorder(5, 5, 5, 5));
      p1.add(createApplicationPanel(), "North");
      p1.add(createRequestURLPanel(), "South");

      m_combinedPanel = new JPanel(new BorderLayout());
      m_combinedPanel.add( p1, BorderLayout.NORTH );
      m_combinedPanel.add( createContentTypePanel(), BorderLayout.CENTER );

      JPanel p2 = new JPanel(new BorderLayout());
      p2.setBorder(
         createGroupBorder( getResources().getString( "parameterSelection" )));
      p2.add(createParameterTableView(), BorderLayout.CENTER );

      m_valPanel = new JPanel(new BorderLayout());
      m_valPanel.setBorder(
         createGroupBorder( getResources().getString( "parameterValidation" )));
      m_valPanel.add(createValidationTableView(), BorderLayout.CENTER );

      m_viewPanel = new JPanel();
      m_viewPanel.setLayout( new BoxLayout( m_viewPanel, BoxLayout.Y_AXIS ));
      m_viewPanel.add( m_combinedPanel );
      m_viewPanel.add( p2 );
      m_viewPanel.add(m_valPanel);
      m_viewPanel.add( createSampleURLPanel());

      return m_viewPanel;
   }


   /**
    * Creates a panel that contains controls for viewing/editing the content
    * type info, such as charset and mime type. The preferred height for this
    * panel is ~100 pixels.
    */
   private JPanel createContentTypePanel()
   {
      m_contentPanel = new JPanel();
      m_contentPanel.setLayout( new BoxLayout( m_contentPanel, BoxLayout.Y_AXIS ));
      m_contentPanel.setBorder(
         createGroupBorder( getResources().getString( "ContentTypeGroupTitle" )));

      // Get the combo boxes that contain lists.
      m_defaultCharset = getCharsetComboBox();

      // char encoding
      Box row = createCharEncodingBox();
      m_contentPanel.add( row );

      // content type / extension map table
      row = Box.createHorizontalBox();
      JScrollPane scroller = new JScrollPane( m_mimeExtMap );
      /* We need to set a preferred height or it will be too large. */
      scroller.setPreferredSize( new Dimension( 80, 80 ));
      row.add( scroller );
      m_contentPanel.add( Box.createVerticalStrut(5));
      m_contentPanel.add( row );

      // initialize the map table UI
      DefaultTableModel model = (DefaultTableModel) m_mimeExtMap.getModel();
      Vector<String> colNames = new Vector<String>(2);
      colNames.add( getExtColName() );
      colNames.add( getMimeColName() );
      model.setColumnIdentifiers( colNames );

      TableColumn extCol = m_mimeExtMap.getColumn( getExtColName() );
      final JComboBox extEditor = getExtensionComboBox();
      extEditor.setEditable( true );
      extCol.setCellEditor( new DefaultCellEditor( extEditor ));
      /* extensions tend to be short, so make the column narrower (it would
         default to half the available width */
      extCol.setPreferredWidth(10);

      final JComboBox mimeEditor = getMimeComboBox();
      mimeEditor.setEditable( true );
      m_mimeExtMap.getColumn( getMimeColName() ).setCellEditor(
         new DefaultCellEditor( mimeEditor ));

      return m_contentPanel;
   }

   private String getMimeColName()
   {
      return getResources().getString( "MimeMapMimeColHeader" );
   }

   private String getExtColName()
   {
      return getResources().getString( "MimeMapExtColHeader" );
   }

   /**Creates a character encoding box
    * @return row a newly created encoding box, it can not be null
    */
   private Box createCharEncodingBox()
   {
      Box row = Box.createHorizontalBox();
      row.add( new UTMnemonicLabel(getResources(), "DefaultCharsetLabel",
         m_defaultCharset ));
      row.add( Box.createHorizontalStrut(5));
      row.add( m_defaultCharset );
      row.add( Box.createHorizontalGlue());
      return row;
   }

   /**
    * Checks if the requested combo box is cached, returning the cached cb if
    * found, otherwise, a new cb is created and its drop list is filled from a
    * file that lists the charsets. The list is sorted in ascending alpha
    * order. If the properties file containing the charsets cannot be found,
    * an empty string is placed in the first position and an error message
    * in the second position of the drop list.
    *
    * @return The combo box w/ a pre-filled drop list.
    */
   private UTFixedHeightComboBox getCharsetComboBox()
   {
      UTFixedHeightComboBox cb =
         (UTFixedHeightComboBox) Util.getCachedObject( CHARSET_CB_CACHE_KEY );
      if ( null == cb )
      {
         cb = new UTFixedHeightComboBox();
         String errMsgKey = null;
         try
         {
            for (final Object charSetName : PSResourceLoader.loadCharacterSets())
            {
               cb.addItem(charSetName);
            }
         }
         catch ( FileNotFoundException e )
         {
            errMsgKey = "PropertiesFileMissing";
         }
         catch ( IOException e )
         {
            errMsgKey = "PropertiesFileLoadFailed";
         }

         if ( null != errMsgKey )
         {
            cb.addItem( "" );
            cb.addItem( getResources().getString( errMsgKey ));
         }
         Util.cacheObject( CHARSET_CB_CACHE_KEY, cb, true );
      }
      return cb;
   }

   /**
    * Creates 2 combo boxes, one for mime types and one for extensions. The
    * drop lists for each cb is filled from the contents of a properties file.
    * The keys of this property file are placed in the extensions cb and the
    * values are placed in the mime type cb. The list is sorted in ascending
    * alpha order. If the properties file containing the list cannot be found,
    * an empty string is placed in the first position and an error message
    * in the second position of the drop list. Both boxes are then placed in
    * the global object cache.
    *
    * @see #getMimeComboBox
    * @see #getExtensionComboBox
    * @see #EXTENSION_CB_CACHE_KEY
    * @see #MIMETYPE_CB_CACHE_KEY
    */
   private void loadMimeExtComboBoxes()
   {
      JComboBox extCb = new JComboBox()
      {
         @Override
         public void addFocusListener(FocusListener l)
         {
            //System.out.println( "adding listener = " + l );
            super.addFocusListener( l );
         }

         @Override
         public void removeFocusListener(FocusListener l)
         {
            //System.out.println( "removing listener = " + l );
            super.removeFocusListener( l );
         }

      };
      JComboBox mimeCb = new JComboBox();
      String errMsgKey = null;
      try
      {
         Properties props = PSResourceLoader.loadMimeExtMap();

         // Save the map so we can auto-guess the mime when the user enters an ext.
         Util.cacheObject( MIMEMAP_CACHE_KEY, props, true );

         Object [] extNames = props.keySet().toArray();
         Object [] mimeTypes = props.values().toArray();
         Collator coll = Collator.getInstance();
         coll.setStrength( Collator.PRIMARY );
         PSSortTool.QuickSort( extNames, coll );
         PSSortTool.QuickSort( mimeTypes, coll );
         for ( int i = 0; i < extNames.length; ++i )
         {
            extCb.addItem( extNames[i] );
            /* check for dupes, since the list is in sorted order, we can
               just check the current against the previous */
            if ( i > 0 && mimeTypes[i-1].equals(mimeTypes[i]))
               continue;
            mimeCb.addItem( mimeTypes[i] );
         }
      }
      catch ( FileNotFoundException e )
      {
         errMsgKey = "PropertiesFileMissing";
      }
      catch ( IOException e )
      {
         errMsgKey = "PropertiesFileLoadFailed";
      }

      if ( null != errMsgKey )
      {
         extCb.addItem( "" );
         extCb.addItem( getResources().getString( errMsgKey ));
         mimeCb.addItem( "" );
         mimeCb.addItem( getResources().getString( errMsgKey ));
      }

      /* Add a listener that auto-guesses the mime type when an extension is
         chosen. This only happens if the mime cell is empty.
         NOTE: We can't use class instance variables because this editor is
         used by many instances. That's why we walk the component hierarchy
         to find the table rather than using the table that is stored in an
         instance of this class. */
      extCb.getEditor().getEditorComponent().addFocusListener( new FocusAdapter()
      {
         @Override
         public void focusLost(FocusEvent evt)
         {
            System.out.println( "focus lost" );
            JTextComponent src = (JTextComponent) evt.getSource();
            String ext = src.getText();

            Component c = src.getParent();
            while ( null != c && !(c instanceof UTJTable ))
               c = c.getParent();
            if ( null == c )
               return;
            UTJTable table = (UTJTable) c;
            int row = table.getEditingRow();

            if ( null != ext && ext.trim().length() > 0 )
            {
               Properties mimeMap =
                  (Properties) Util.getCachedObject( MIMEMAP_CACHE_KEY );
               if ( null != mimeMap )
               {
                  String mimeType = mimeMap.getProperty( ext );
                  // don't overwrite a value that's already there
                  String current = (String) table.getValueAt( row, 1 );
                  if ( null == current || current.trim().length() == 0 )
                     table.setValueAt( mimeType, row, 1);
               }
            }
            else
               // clear the mime type
               table.setValueAt( "", row, 1 );
         }
      });

      Util.cacheObject( EXTENSION_CB_CACHE_KEY, extCb, true );
      Util.cacheObject( MIMETYPE_CB_CACHE_KEY, mimeCb, true );
   }


   /**
    * Checks if the requested combo box is cached, returning the cached cb if
    * found, otherwise, a new cb is created and its drop list is filled from a
    * file that lists the mimetypes. The list is sorted in ascending alpha
    * order. If the properties file containing the list cannot be found,
    * an empty string is placed in the first position and an error message
    * in the second position of the drop list.
    *
    * @return The combo box w/ a pre-filled drop list.
    *
    * @see #loadMimeExtComboBoxes
    */
   private JComboBox getMimeComboBox()
   {
      JComboBox cb = (JComboBox) Util.getCachedObject( MIMETYPE_CB_CACHE_KEY );
      if ( null == cb )
      {
      //System.out.println( "loading mime cb for mime cb " );
         loadMimeExtComboBoxes();
         cb = (JComboBox) Util.getCachedObject( MIMETYPE_CB_CACHE_KEY );
      }
      return cb;
   }


   /**
    * Checks if the requested combo box is cached, returning the cached cb if
    * found, otherwise, a new cb is created and its drop list is filled from a
    * file that lists the file extensions. The list is sorted in ascending alpha
    * order. If the properties file containing the list cannot be found,
    * an empty string is placed in the first position and an error message
    * in the second position of the drop list.
    * <p>
    *
    * @return The combo box w/ a pre-filled drop list.
    *
    * @see #loadMimeExtComboBoxes
    */
   private JComboBox getExtensionComboBox()
   {
      JComboBox cb = (JComboBox) Util.getCachedObject( EXTENSION_CB_CACHE_KEY );
      if ( null == cb )
      {
      //System.out.println( "loading mime cb for ext cb " );
         loadMimeExtComboBoxes();
         cb = (JComboBox) Util.getCachedObject( EXTENSION_CB_CACHE_KEY );
      }
      return cb;
   }



   /**
    * Create the command panel.
    *
    * @return   JPanel      the command panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
         @Override
         public void onOk()
         {
            DatasetInputConnectorPropertyDialog.this.onOk();
         }
      };

      JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      panel.add(m_commandPanel);
      return panel;
   }

   /**
    * Initialize the dialogs GUI elements with its data.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private void initDialog()
   {
      setResizable( true );

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(createViewPanel(), "Center");
      panel.add(createCommandPanel(), "South");

      // set the default button
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(panel);
      this.setSize(DIALOG_SIZE);

      // initialize validation constraints
      m_validatedComponents[0] = m_requestURL;
      m_validationConstraints[0] = new StringConstraint();
      setValidationFramework(m_validatedComponents, m_validationConstraints);

   }

   /**
    * Added for testing reasons only.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private ResourceBundle m_res = null;
   @Override
   protected ResourceBundle getResources()
   {
      try
      {
         if (m_res == null)
            m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                             Locale.getDefault() );
      }
      catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return m_res;
   }

   private static final String DEFAULT_REQUEST_EXT = ".html";

   /**
    * Builds a sample URL of the following format:
    * http://<server IP addr>[:port]/<server root>/<app root>/<req page>[?...].
    * The items in brackets are 'optional'. The port will be included we one
    * was supplied when we logged in. The parameter separator will be included
    * if there are any validation or selection parameters in the dialog.
    *
   **/
   private void onUrlChanged()
   {
      System.out.println( "Changing sample URL" );
      String req = m_requestURL.getText();
      if ( req.lastIndexOf(".") < 0 )
         req = req + DEFAULT_REQUEST_EXT;
      String sample = "http://" + m_server + m_port + "/" + m_serverRoot
                     + "/" + m_application.getText() + "/" + req;
      if ( m_parameterTable.hasData() || m_validationTable.hasData())
         sample = sample + "?" + PARAMETER_ELLIPSIS;
      m_sampleURL.setText(sample);
   }

   /**
    * This string is appended to the sample URL if there are any conditionals
    * in either table.
    */
   private static final String PARAMETER_ELLIPSIS = "...";

   /**
    * Copy the sample URL to the clipboard.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private void onCopy()
   {
      String sample = m_sampleURL.getText();
      // strip trailling "..."
      int offset = sample.lastIndexOf( PARAMETER_ELLIPSIS );
      if ( offset > 0 )
         sample = sample.substring( 0, offset );

      StringSelection transfer = new StringSelection( sample );
      m_clipboard.setContents(transfer, this);
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementations for ClipboardOwner
   @SuppressWarnings("unused")
   public void lostOwnership(Clipboard clipboard, Transferable contents) { }

   //////////////////////////////////////////////////////////////////////////////
   // implementations for CellEditorListener
   @SuppressWarnings("unused")
   public void keyPressed(KeyEvent e) { }
   @SuppressWarnings("unused")
   public void keyReleased(KeyEvent e) { /*onUrlChanged();*/ }
   @SuppressWarnings("unused")
   public void keyTyped(KeyEvent e) { }

   //////////////////////////////////////////////////////////////////////////////
   // implementations for CellEditorListener
   @SuppressWarnings("unused")
   public void editingCanceled(ChangeEvent e) { /*onUrlChanged();*/ }
   @SuppressWarnings("unused")
   public void editingStopped(ChangeEvent e) { /*onUrlChanged();*/ }

   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
   public boolean onEdit(UIFigure figure, final Object data)
   {
//      System.out.println( "onEdit: table = " + m_mimeExtMap );
      try
      {
         // get some of the params to build our sample URL and save them for later
         UIMainFrame mf = E2Designer.getApp().getMainFrame();
         Properties props = mf.getLoginProperties();
         String server = props.getProperty( PSDesignerConnection.PROPERTY_HOST );
         m_server = InetAddress.getByName( server ).getHostAddress();
         // if we can't get the IP address, use the name
         if ( null == m_server || 0 == m_server.trim().length())
            m_server = server;
         String port = props.getProperty( PSDesignerConnection.PROPERTY_PORT );
         if ( 0 != port.length())
            m_port = ":" + port;
         try
         {
            m_serverRoot = mf.getObjectStore().getServerConfiguration().getRequestRoot();
         }
         catch ( PSAuthorizationException e )
         { /* ignore */ }
         catch ( PSServerException e )
         { /* ignore */ }

         if (data instanceof PSApplication)
            m_app = (PSApplication) data;
         else
            throw new IllegalArgumentException("PSApplication expected!");

         if (figure instanceof UIFlexibleConnectionPoint)
         {
            UIFlexibleConnectionPoint cp = (UIFlexibleConnectionPoint) figure;
            UIConnectableFigure fig = cp.getOwner();
            if (fig.getData() instanceof OSDataset)
            {
               m_dataset = (OSDataset) fig.getData();
            }

            m_datasetFigure = fig;
            // setting figure into memory
            Util.setFigure( fig );

         }
         else
            throw new IllegalArgumentException("UIFlexibleConnectionPoint expected!");

         if (figure.getData() instanceof OSRequestor)
         {
            UIConnectableFigure fig = ((UIFlexibleConnectionPoint)figure).getOwner();

            /* Don't use the requestor from the connection point, that is just a
               placeholder */
            if(fig.getData() instanceof OSDataset)
            m_requestor = (OSRequestor) m_dataset.getRequestor();

            m_pageDatatank = m_requestor.getPageDatatank();
            ValueSelectorDialogHelper h =
               new ValueSelectorDialogHelper(null, m_pageDatatank);
            m_parameterVariableEditor.refresh(h.getDataTypes());
            m_parameterVariableEditor.setDefaultType(
                  new DTSingleHtmlParameter());
            m_parameterValueEditor.refresh(h.getDataTypes());
            m_parameterValueEditor.setDefaultType(new DTTextLiteral());
            m_validationVariableEditor.refresh(h.getDataTypes());
            m_validationVariableEditor.setDefaultType(
                  new DTSingleHtmlParameter());
            m_validationValueEditor.refresh(h.getDataTypes());
            m_validationValueEditor.setDefaultType(new DTTextLiteral());

            m_application.setText(m_app.getRequestRoot());

            m_requestURL.setText(m_requestor.getRequestPage());
            String charset = m_requestor.getCharacterEncoding();
            m_defaultCharset.setSelectedItem( charset );

            HashMap mimeMap = m_requestor.getMimeProperties();
            Iterator iter = null;
            if ( null != mimeMap )
               iter = mimeMap.keySet().iterator();
            if ( null != iter )
            {
               for ( int i = 0; iter.hasNext(); ++i)
               {
                  String ext = (String) iter.next();
                  m_mimeExtMap.setValueAt( ext, i, 0 );
                  IPSReplacementValue mimeType =
                     (IPSReplacementValue) mimeMap.get( ext );
                  m_mimeExtMap.setValueAt( mimeType.getValueDisplayText(), i, 1 );
               }
            }

            m_parameterTable.loadFromConditionals(m_requestor.getSelectionCriteria());
            if (!m_parameterTable.hasEmptyRow())
               m_parameterTable.appendRow(12);

            m_validationTable.loadFromConditionals(m_requestor.getValidationRules());
            if (!m_validationTable.hasEmptyRow())
               m_validationTable.appendRow(12);

            this.onUrlChanged();
            this.center();

            /*if a figure is a content editor dataset change the request properties
             dialog*/
            if(m_bIsContentEditor)
            {
               //a validation panel is not needed for a content editor
               m_viewPanel.remove(m_valPanel);

               /*Since we allow only .hml and .xml extensions we do not need
               entire m_combinedPanel */
               m_combinedPanel.remove(m_contentPanel);

               // char encoding is needed for a content editor
               m_combinedPanel.add(createCharEncodingBox(), "Center");

               //now set a new size for a request properties dialog
               this.setSize(DIALOG_SIZE_CE);
               this.center();

            }
            this.setVisible(true);
         }
         else
            throw new IllegalArgumentException("OSRequestor expected!");
      }
      catch (Exception e)
      {
         PSDlgUtil.showError
            (e, true, E2Designer.getResources().getString( "OpErrorTitle" ));
      }

      return m_modified;
   }


   @Override
   public void onOk()
   {
      /* End the cell editing mode before validation. If the user presses the
         OK button before Enter, the contents of the editor will be recognized. */
      if ( m_parameterView.isEditing())
         m_parameterView.getCellEditor().stopCellEditing();
      if ( m_validationView.isEditing())
         m_validationView.getCellEditor().stopCellEditing();
      if ( m_mimeExtMap.isEditing())
         m_mimeExtMap.getCellEditor().stopCellEditing();

      if (activateValidation())
      {
         try
         {
            // do conditionals first, since they have their own validation
            ConditionalValidationError error = m_parameterTable.validate();
            if ( null != error )
            {
               JOptionPane.showMessageDialog(this, error.getErrorText(),
                     getResources().getString( "ValidationErrorTitle" ),
                     JOptionPane.WARNING_MESSAGE);
               m_parameterView.getSelectionModel().clearSelection();
               m_parameterView.editCellAt( error.getErrorRow(), error.getErrorCol());
               m_parameterView.getEditorComponent().requestFocus();
               return;
            }

            error = m_validationTable.validate();
            if ( null != error )
            {
            JOptionPane.showMessageDialog(this, error.getErrorText(),
               getResources().getString( "ValidationErrorTitle" ),
               JOptionPane.WARNING_MESSAGE);
               m_validationView.getSelectionModel().clearSelection();
               m_validationView.editCellAt( error.getErrorRow(),
                  error.getErrorCol());
               m_validationView.getEditorComponent().requestFocus();
               return;
            }
            m_requestor.setSelectionCriteria(
               m_parameterTable.saveToConditionals(
               m_requestor.getSelectionCriteria()));

            m_requestor.setValidationRules(
               m_validationTable.saveToConditionals(
               m_requestor.getValidationRules()));

            m_modified = true;
            m_requestor.setRequestPage(m_requestURL.getText());

            // todo check param semantics
            String charset = (String) m_defaultCharset.getSelectedItem();
            if ( null == charset )
               charset = "";
            else
               charset = charset.trim();
            m_requestor.setCharacterEncoding( charset );
            // walk entire table in case they left any blank rows between valid rows
            HashMap<String, IPSReplacementValue> mimeMap = new HashMap<String, IPSReplacementValue>();
            int rows = m_mimeExtMap.getRowCount();
            boolean hasDupes = false;
            for ( int i = 0; i < rows; ++i )
            {
               String ext = (String) m_mimeExtMap.getValueAt( i, 0 );
               if ( null != ext && ext.trim().length() > 0 )
               {
                  String mimeText = (String) m_mimeExtMap.getValueAt( i, 1 );
                  if ( null != mimeText && mimeText.trim().length() > 0 )
                  {
                     try
                     {
                        // the server only allows lower case for extension name
                        ext = ext.toLowerCase();
                        if ( mimeMap.containsKey( ext ))
                        {
                           hasDupes = true;
                           continue;
                        }
                        mimeText = mimeText.trim();
                        /* Since XML elements and mime types look the same,
                           use the factory to check the type. It will return
                           PSXmlField if no other type is recognized. In this
                           case we then convert it to text literal. We won't
                           support XML elements in this context. */
                        IPSReplacementValue mimeValue = PSReplacementValueFactory
                           .getReplacementValueFromXmlFieldName( mimeText );
                        if ( mimeValue instanceof PSXmlField )
                           mimeValue = new PSTextLiteral( mimeText );
                        mimeMap.put( ext, mimeValue );
                     }
                     catch ( IllegalArgumentException e )
                     {
                        String msg = getResources().getString( "InvalidElementFormat" );
                        String title = getResources().getString( "ValidationErrorTitle" );
                        JOptionPane.showMessageDialog(this, title,
                           MessageFormat.format( msg,
                           new Object[] { mimeText, e.getLocalizedMessage() }),
                           JOptionPane.ERROR_MESSAGE );
                     }
                  }
               }
            }

            if ( hasDupes )
            {
               // show warning msg to user
               String title = getResources().getString( "DupesWarningTitle" );
               String msg = getResources().getString( "DupesWarning" );
               JOptionPane.showMessageDialog(this, title, msg,
                  JOptionPane.WARNING_MESSAGE );
            }

            m_requestor.setMimeProperties( mimeMap );

            if (m_dataset != null)
            {
               PSCollection datasets = m_app.getDataSets();
               if (datasets == null)
                  datasets = new PSCollection(PSDataSet.class.getName());

               // replace the dataset with the updated one from this editor
               for (int i=0; i<datasets.size(); i++)
               {
                  PSDataSet dataset = (PSDataSet) datasets.get(i);
                  if (dataset.getName().equals(m_dataset.getName()))
                  {
                     datasets.remove(dataset);
                     break;
                  }
               }
               datasets.add(m_dataset);
            }

            m_requestor.setInternalName(m_requestURL.getText());

            /* get the label of the dataset figure and updates the label with
               the new URL. Ideally we shouldn't have to do this here, it would be
               automatically done via a notification from the data object to the
               UI object. However, the dataset's label is based not on the dataset
               object, but on the requestor object, so we have this special case. */
            m_datasetFigure.invalidateLabel();

         }
         catch (ClassNotFoundException e)
         {
            PSDlgUtil.showError(e);
         }
         catch (IllegalArgumentException e)
         {
            JOptionPane.showMessageDialog(this, Util.cropErrorMessage(e.getLocalizedMessage()),
                              E2Designer.getResources().getString("OpErrorTitle"),
                              JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
         }

         dispose();
      }
   }


   //////////////////////////////////////////////////////////////////////////////
   /**
   * the dataset input connector
   */
   PSApplication m_app = null;
   OSDataset m_dataset = null;

   OSRequestor m_requestor = null;
   OSPageDatatank m_pageDatatank = null;
   UIFigure m_datasetFigure = null;

   /** A boolean to determine if the dataset is a content editor or not*/
   boolean m_bIsContentEditor = false;

   /**A panel that contains controls for parameter validation.
    * Gets initialized @link createViewPanel().
    */
   JPanel m_valPanel = null;

   /** A main view panel. Gets initialized in @link createViewPanel()*/
   JPanel m_viewPanel = null;

   /**A panel that contains controls for viewing/editing the application name,
    * a requstor name and m_combinedPanel.  Gets initialized in
    * }@link createViewPanel()}.
    */
   JPanel m_combinedPanel = null;

   /**A panel that contains controls for viewing/editing the content type info,
    * such as charset and mime type. Gets initialized in @link createContentTypePanel().
    */
   JPanel m_contentPanel = null;

   ValueSelectorDialog m_parameterVariableEditor = null;
   ValueSelectorDialog m_parameterValueEditor = null;
   ValueSelectorDialog m_validationVariableEditor = null;
   ValueSelectorDialog m_validationValueEditor = null;

   /**
    * the parameter selection table
    */
   private final static Dimension PARAMETER_TABLE_SIZE = new Dimension(500, 100);
   ConditionalTableModel m_parameterTable = new ConditionalTableModel(false);
   UTJTable m_parameterView =  new UTJTable(m_parameterTable);
   /**
    * the parameter validation table
    */
   private final static Dimension VALIDATION_TABLE_SIZE = new Dimension(500, 100);
   ConditionalTableModel m_validationTable = new ConditionalTableModel();
   UTJTable m_validationView =  new UTJTable(m_validationTable);
   /**
    * the application
    */
   private UTFixedTextField m_application = new UTFixedTextField("", TEXT_FIELD_SIZE);
   /**
    * the request URL
    */
   private UTFixedTextField m_requestURL = new UTFixedTextField("", TEXT_FIELD_SIZE);
   /**
    * the copy button
    */
   private UTFixedButton m_copy = new UTFixedButton(getResources().getString("copy"));
   /**
    * the sample URL
    */
   private JTextArea m_sampleURL = new JTextArea("", 3, 50);
   /**
    * the system clipboard
    */
   private Clipboard m_clipboard = getToolkit().getSystemClipboard();
     /**
    * the standard command panel
    */
   private UTStandardCommandPanel m_commandPanel = null;
   /**
    * this flag will be set if any data within this dialog was modified
    */
   private boolean m_modified = false;
     /**
    * the text field size
    */
   private final static Dimension TEXT_FIELD_SIZE = new Dimension(370, 20);
   /**
    * the dialog size
    */
   private final static Dimension DIALOG_SIZE = new Dimension(570, 630);

   /**
    * the dialog size for a content editor
    */

    private final static Dimension DIALOG_SIZE_CE = new Dimension(570, 380);

   /**
    * The name of the E2 server machine, in IP format, if available. It is
    * initialized in onEdit.
    */
   private String m_server = null;
   /**
    * The port that was used when logging into E2, or the empty string if
    * no port was specified. It is initialized in onEdit();
    */
   private String m_port = "";

   /**
    * The server root for the E2 server we are connected to, or a placeholder
    * if the real root can't be obtained. It is initialized in onEdit.
    */
   private String m_serverRoot = null;

   /**
    * Contains the default char set used to set character encoding in the
    * HTTP Content-Type variable. Does not have to be specified.
    */
   private UTFixedHeightComboBox m_defaultCharset = new UTFixedHeightComboBox();

   /**
    * This table contains the default extension to mime type mappings for this
    * resource. Column 1 is the extension (w/o leading period). Col2 is the
    * mime type. It is valid for the table to be empty. 50 rows was arbitrarily
    * chosen as large enough to contain the most mappings ever expected.
    */
   private UTJTable m_mimeExtMap = new UTJTable(50, 2)
   {
      @Override
      public boolean editCellAt(int row, int col)
      {
         return super.editCellAt( row, col );
      }

      @Override
      public boolean editCellAt(int row, int c, java.util.EventObject e)
      {
         return super.editCellAt( row, c, e );
      }

      @Override
      public void setEditingRow(int r)
      {
         super.setEditingRow(r);
      }

      @Override
      public void setCellEditor(javax.swing.table.TableCellEditor e)
      {
         super.setCellEditor(e);
      }

      @Override
      public boolean isEditing()
      {
         return super.isEditing();
      }
   };


   /**
    * This is the key used to cache the extensions combo box in the global
    * cache. We want to pick a name that is globally unique. We cache this
    * guy so we don't have to read a file, sort and fill the drop list each
    * time this dialog is instantiated/class loaded.
    */
   private static final String EXTENSION_CB_CACHE_KEY = "DatasetIC_extensions_cb";

   /**
    * This is the key used to cache the mime type combo box in the global
    * cache. We want to pick a name that is globally unique. We cache this
    * guy so we don't have to read a file, sort and fill the drop list each
    * time this dialog is instantiated/class loaded.
    */
   private static final String MIMETYPE_CB_CACHE_KEY = "DatasetIC_mimetypes_cb";

   /**
    * This is the key used to cache the charset combo box in the global
    * cache. We want to pick a name that is globally unique. We cache this
    * guy so we don't have to read a file, sort and fill the drop list each
    * time this dialog is instantiated/class loaded.
    */
   private static final String CHARSET_CB_CACHE_KEY = "DatasetIC_charsets_cb";

   /**
    * This is the key used to cache the extension to mimetype map. We use the
    * map to auto-guess a mime type when the user selects an extension.
    * We want to pick a name that is globally unique. We cache this
    * guy so we don't have to read a file more than once.
    */
   private static final String MIMEMAP_CACHE_KEY = "DatasetIC_mimemap";

   /**
    * the validation framework variables
    */
   //////////////////////////////////////////////////////////////////////////////
   private final Component m_validatedComponents[] = new Component[1];
   private final ValidationConstraint m_validationConstraints[] =
      new ValidationConstraint[1];
}
