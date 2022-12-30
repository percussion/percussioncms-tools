/******************************************************************************
 *
 * [ WebpagePropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;


import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSReplacementValueFactory;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSXmlField;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.util.PSResourceLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;



/**
 * The result webpage editor.
 */
////////////////////////////////////////////////////////////////////////////////
public class WebpagePropertyDialog extends PSEditorDialog
{
   /**
    * Constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public WebpagePropertyDialog()
   {
      super();
      setResizable( true );
      initDropLists();
      initUI();
      initListeners();
      setSize( 450, 550 );
      pack();
      center();
   }

   public WebpagePropertyDialog(Window parent)
   {
      super(parent);
      setResizable( true );
      initDropLists();
      initUI();
      initListeners();
      setSize( 450, 550 );
      pack();
      center();
   }

   /**
    * Create the view panel.
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createViewPanel()
   {
      Box left = Box.createVerticalBox();

      Box row = Box.createHorizontalBox();
      row.add( Box.createHorizontalGlue());
      String labelStr = getResources().getString("root");
      char mn = getResources().getString("mn_root").charAt(0);
      UTFixedLabel rootLabel = 
            new UTFixedLabel(labelStr,JLabel.RIGHT);
      rootLabel.setDisplayedMnemonic(mn);
      rootLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      row.add( rootLabel);
      left.add( row );

      row = Box.createHorizontalBox();
      row.add( Box.createHorizontalGlue());
      labelStr = getResources().getString("requestor");
      mn       = getResources().getString("mn_requestor").charAt(0);
      UTFixedLabel reqLabel = new UTFixedLabel( labelStr, JLabel.RIGHT );
      reqLabel.setDisplayedMnemonic(mn);
      reqLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      
      row.add( reqLabel);
      left.add( Box.createVerticalStrut(3));
      left.add( Box.createVerticalGlue());
      left.add( row );

      row = Box.createHorizontalBox();
      row.add( Box.createHorizontalGlue());
      row.add( new UTMnemonicLabel(getResources(), "stylesheet", m_stylesheet));
      left.add( Box.createVerticalStrut(3));
      left.add( Box.createVerticalGlue());
      left.add( row );      
      
      row = Box.createHorizontalBox();
      row.add( Box.createHorizontalGlue());
      labelStr = getResources().getString("namespaceCleanup");
      mn       = getResources().getString("mn_namespaceCleanup").charAt(0);
      JLabel namespaceLabel = new UTFixedLabel( labelStr, JLabel.RIGHT );
      namespaceLabel.setDisplayedMnemonic(mn);
      namespaceLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      
      row.add( namespaceLabel);
      
      left.add( Box.createVerticalStrut(3));
      left.add( Box.createVerticalGlue());
      left.add( row );
      

      Box right = Box.createVerticalBox();
      m_root.setEnabled(false);
      m_root.setBackground(Color.lightGray);
      right.add( m_root );
      rootLabel.setLabelFor(m_root);

      m_requestor.setEnabled(false);
      m_requestor.setBackground(Color.lightGray);
      right.add( Box.createVerticalStrut(3));
      right.add( Box.createVerticalGlue());
      right.add(m_requestor);
      reqLabel.setLabelFor(m_root);
      
      m_stylesheet.setEditable(true);
      m_stylesheet.addItem( ms_defaultSS );
      right.add( Box.createVerticalStrut(3));
      right.add( Box.createVerticalGlue());
      row = Box.createHorizontalBox();
      /* If we don't put the cb in a horizontal box, it won't fill all available
         width. */
      row.add( m_stylesheet );
      right.add( row );
      
      m_namespaceCleanup = new JCheckBox();
      namespaceLabel.setLabelFor(m_namespaceCleanup);
      row = Box.createHorizontalBox();      
      row.add(m_namespaceCleanup);
      row.add(Box.createHorizontalGlue());
      
      right.add(row);

      JPanel center = new JPanel();
      center.setLayout( new BoxLayout( center, BoxLayout.X_AXIS ));
      center.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      center.add( left );
      center.add( Box.createHorizontalStrut(5));
      center.add( right );

      JPanel panel = new JPanel( new BorderLayout());
      panel.add( center, BorderLayout.CENTER );
      return panel;
   }

   /**
    * Create command panel.
    *
    * @return   JPanel      the guess command panel
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createCommandPanel()
   {
      m_commandPanel = new UTStandardCommandPanel(
                                 this, "", SwingConstants.HORIZONTAL)
      {
         // implement onOk action
         public void onOk()
         {
            WebpagePropertyDialog.this.onOk();
         }
      };
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(m_commandPanel, BorderLayout.EAST);
      return m_commandPanel;
   }

   /**
    * Create the conditional table.
    */
   //////////////////////////////////////////////////////////////////////////////
   private JPanel createTableView()
   {
      ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
            (OSBackendDatatank) null, null);
      m_variableDialog = new ValueSelectorDialog(this, h.getDataTypes(),
         null);
      m_variableEditor = new ValueSelectorCellEditor(m_variableDialog,
         null);

      m_valueDialog = new ValueSelectorDialog(this, h.getDataTypes(),
         null);
      m_valueEditor = new ValueSelectorCellEditor(m_valueDialog,
         null);

      // define the cell editors
      m_table.getColumn( ConditionalTableModel.VARIABLE_COL_NAME )
         .setCellEditor(m_variableEditor);
      m_table.getColumn( ConditionalTableModel.VARIABLE_COL_NAME )
         .setPreferredWidth(150);
      DefaultCellEditor opEditor = new DefaultCellEditor(
         new UTOperatorComboBox());
      opEditor.setClickCountToStart(1);
      m_table.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setCellEditor(opEditor);
      m_table.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setPreferredWidth(50);
      m_table.getColumn( ConditionalTableModel.VALUE_COL_NAME )
         .setCellEditor(m_valueEditor);
      m_table.getColumn( ConditionalTableModel.VALUE_COL_NAME )
         .setPreferredWidth(150);
      m_table.getColumn( ConditionalTableModel.BOOL_COL_NAME )
         .setCellEditor(new DefaultCellEditor(new UTBooleanComboBox()));
      m_table.getColumn( ConditionalTableModel.BOOL_COL_NAME )
         .setPreferredWidth(40);

      // set operator cell renderer
      m_table.getColumn( ConditionalTableModel.OPERATOR_COL_NAME )
         .setCellRenderer(new UTOperatorComboBoxRenderer());

      JScrollPane pane = new JScrollPane(m_table);
      pane.setPreferredSize(new Dimension(380, 200));

      Box top = Box.createHorizontalBox();
      m_includeExt = new JCheckBox( getResources().getString("ExtCheckLabel"));
      m_includeExt.setMnemonic(
                        getResources().getString("mn_ExtCheckLabel").charAt(0));
      top.add( m_includeExt );
      top.add( m_extText );
      top.add( Box.createHorizontalStrut(5));
      String extLabel = getResources().getString("ExtensionsDescription");
      m_extLabel = new JLabel( MessageFormat.format( extLabel,
         new Object [] { EXTENSION_DELIMITER } ));
      top.add( m_extLabel );
      top.add( Box.createHorizontalGlue());

      JPanel panel = new JPanel( new BorderLayout());
      String title = getResources().getString("TablePanelGroupTitle");
      panel.setBorder( BorderFactory.createTitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED), title ));
      panel.add( top, BorderLayout.NORTH );
      panel.add( pane, BorderLayout.CENTER );
      return panel;
   }


   /**
    * Creates the panel that contains the controls to set the content-type and
    * the character encoding of the type.
    */
   private JPanel createContentTypePanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ));
      String title = getResources().getString("ContentTypeGroupTitle");
      panel.setBorder( BorderFactory.createTitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED), title ));

      // create the radio buttons, they are all in the same group
      String labelStr = getResources().getString("DefaultRadioLabel");
      char mn = getResources().getString("mn_DefaultRadioLabel").charAt(0);
      m_defaultRadio = new JRadioButton(labelStr);
      m_defaultRadio.setMnemonic(mn);
      m_userSpecifiedRadio = new JRadioButton( getResources().getString("UserSpecifedRadioLabel"));
      m_userSpecifiedRadio.setMnemonic(
            getResources().getString("mn_UserSpecifedRadioLabel").charAt(0));
      m_xmlRadio = new JRadioButton( getResources().getString("XmlRadioLabel"));
      m_xmlRadio.setMnemonic(
            getResources().getString("mn_XmlRadioLabel").charAt(0));

      // make the buttons into a group
      ButtonGroup contentType = new ButtonGroup();
      contentType.add( m_defaultRadio );
      contentType.add( m_userSpecifiedRadio );
      contentType.add( m_xmlRadio );

      Box row = Box.createHorizontalBox();
      row.add( m_defaultRadio );
      row.add( Box.createHorizontalGlue());
      panel.add( row );

      row = Box.createHorizontalBox();
      row.add( m_userSpecifiedRadio );
      row.add( m_userSpecifiedList );
      panel.add( row );

      row = Box.createHorizontalBox();
      row.add( m_xmlRadio );
      row.add( m_xmlElement );
      panel.add( row );

      // separate the char encoding from the mime type a bit
      panel.add( Box.createVerticalStrut(15));

      row = Box.createHorizontalBox();
      labelStr = getResources().getString("CharEncodingLabel");
      mn = getResources().getString("mn_CharEncodingLabel").charAt(0);
      JLabel charEncLabel = new JLabel(labelStr);
      charEncLabel.setDisplayedMnemonic(mn);
      charEncLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      charEncLabel.setLabelFor(m_charEncoding);
      row.add(charEncLabel);
      row.add( Box.createHorizontalStrut(8));
      row.add( m_charEncoding );
      panel.add( row );

      return panel;
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
            // put this at the top of the list
            cb.addItem(USE_DEFAULT_CHAR_ENCODING);
            for (final Object charsetName : PSResourceLoader.loadCharacterSets())
            {
               cb.addItem(charsetName);
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
    * Checks if the requested combo box is cached, returning the cached cb if
    * found, otherwise, a new cb is created and its drop list is filled from a
    * file that lists the charsets. The list is sorted in ascending alpha
    * order. If the properties file containing the charsets cannot be found,
    * an empty string is placed in the first position and an error message
    * in the second position of the drop list.
    *
    * @return The combo box w/ a pre-filled drop list.
    */
   private UTFixedHeightComboBox getMimeTypeComboBox()
   {
      UTFixedHeightComboBox cb =
         (UTFixedHeightComboBox) Util.getCachedObject( MIMETYPE_CB_CACHE_KEY );
      if ( null == cb )
      {
         cb = new UTFixedHeightComboBox();
         String errMsgKey = null;
         try
         {
            for (final String mimeType : PSResourceLoader.loadMimeTypes())
            {
               cb.addItem(mimeType);
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
         Util.cacheObject( MIMETYPE_CB_CACHE_KEY, cb, true );
      }
      return cb;
   }

   /**
    * Initialize the dialogs GUI elements with its data.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   private void initUI()
   {
      JPanel mainPanel = new JPanel(new BorderLayout());
      
      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
      topPanel.add( createViewPanel(), BorderLayout.NORTH );
      topPanel.add( createTableView(), BorderLayout.CENTER );
      topPanel.add( createContentTypePanel(), BorderLayout.SOUTH );

      // create bottom panel
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(createCommandPanel(), BorderLayout.EAST );
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      mainPanel.add(topPanel, BorderLayout.NORTH);
      mainPanel.add(bottomPanel, BorderLayout.SOUTH);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(mainPanel);
      this.pack();
   }

   /**
    * Takes all the XML fields in all of the tanks in the supplied vector and
    * adds them to the <code>m_xmlElement</code> drop list. The list is sorted
    * in ascending dictionary order. The tanks are de-duped by looking at
    * their schema source files.
    *
    * @param pageTanks A vector containing OSPageDatatank objects. If <code>
    * null</code> or empty, nothing is added to the drop list.
    */
   @SuppressWarnings("unchecked")
   private void fillXmlDropList( Vector<OSPageDatatank> pageTanks )
   {
      if ( null != pageTanks && pageTanks.size() > 0)
      {
         Vector xmlFields = new Vector();
         Map<String, OSPageDatatank> uniqueTanks = new HashMap<String, OSPageDatatank>();
         // dedupe the list
         for (final OSPageDatatank pageTank : pageTanks)
         {
            uniqueTanks.put( pageTank.getSchemaSource().getFile(), pageTank );
         }

         // get all the elements into a single list
         for (final OSPageDatatank pageTank : uniqueTanks.values())
         {
            xmlFields.addAll(pageTank.getColumns());
         }

         // sort it
         Collections.sort( xmlFields, Collator.getInstance());

         // now add them to the list
         for (final Object field : xmlFields)
         {
            m_xmlElement.addItem(field);
         }
      }
   }

   /**
    * Obtains the necessary data and fills all combo drop lists that have
    * a set of pre-defined data. This should be done first as it may create
    * certain controls.
    */
   private void initDropLists()
   {
      // set up the charset drop list
      m_charEncoding = getCharsetComboBox();

      // get loaded MIME types
      m_userSpecifiedList = getMimeTypeComboBox();
   }

   /**
    * Handles key released event.
    */
   public void addKeyEnterHandler()
   {
      addKeyListener(new KeyAdapter()
      {
         public void keyReleased (KeyEvent e)
         {
            if(e.getKeyCode () ==  e.VK_ENTER)
               if(m_table.isEditing())
                  m_table.getCellEditor().stopCellEditing();
         }
      });
   }

   /**
    * Adds listeners to all controls that affect the graying of another
    * control.
    */
   private void initListeners()
   {
      m_includeExt.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            System.out.println( "check box action" );
            boolean enabled = ((JCheckBox) evt.getSource()).isSelected();
            m_extText.setEnabled( enabled );
            m_extText.setBackground( enabled ? Color.white : DISABLED_COLOR );
            m_extLabel.setEnabled( enabled );
         }
      });

      m_userSpecifiedRadio.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            System.out.println( "user spec radio action" );
            m_userSpecifiedList.setEnabled( true );
            m_xmlElement.setEnabled( false );
         }
      });

      m_xmlRadio.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            System.out.println( "xml radio action" );
            m_userSpecifiedList.setEnabled( false );
            m_xmlElement.setEnabled( true );
         }
      });

      m_defaultRadio.addActionListener( new ActionListener()
      {
         public void actionPerformed( ActionEvent evt )
         {
            m_userSpecifiedList.setEnabled( false );
            m_xmlElement.setEnabled( false );
         }
      });
   }


   /**
    * Checks all controls that might possibly be grayed and grays them out
    * if they should be. This method should be only be called when the dialog
    * is first created or reset. After that, listeners added to the controls
    * will take care of graying.
    *
    * @see #initListeners
    */
   private void initGraying()
   {
      boolean extEnabled = m_includeExt.isSelected();
      m_extText.setEnabled( extEnabled );
      if ( !extEnabled )
         m_extText.setBackground( DISABLED_COLOR );
      m_extLabel.setEnabled( extEnabled );

      m_userSpecifiedList.setEnabled( m_userSpecifiedRadio.isSelected());
      m_xmlElement.setEnabled( m_xmlRadio.isSelected());
   }


   /**
    * Creates the validation framework and sets it in the parent dialog. After
    * setting it, the m_validationInited flag is set to indicate it doesn't
    * need to be done again. If something changes requiring a new framework,
    * just clear this flag and the next time onOk is called, the framework
    * will be recreated.
    * <p>
    * By using the flag, this method can be called multiple times, saving the
    * caller from having to track changes (with no performance penalty).
    */
   private void initValidationFramework()
   {
      if ( m_validationInited )
         return;

      // set up the validation framework
      List<JComponent> comps = new ArrayList<JComponent>(10);
      List<ConditionalValidator> constraints = new ArrayList<ConditionalValidator>(10);
      StringConstraint nonEmpty = new StringConstraint();

      ConditionalValidator cv = new ConditionalValidator( m_includeExt, nonEmpty );
      comps.add( m_extText );
      constraints.add( cv );

      cv = new ConditionalValidator( m_xmlRadio, nonEmpty );
      comps.add( m_xmlElement );
      constraints.add( cv );

      // do an 'assert'
      if ( comps.size() != constraints.size())
         throw new IllegalArgumentException( "validation array size mismatch" );

      Component [] c = new Component[comps.size()];
      comps.toArray( c );
      ValidationConstraint [] v = new ValidationConstraint[constraints.size()];
      constraints.toArray( v );
      setValidationFramework( c, v );

      m_validationInited = true;
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
   public boolean onEdit(UIFigure figure, final Object data)
   {
      try
      {
          if (data instanceof PSApplication)
            m_app = (PSApplication) data;
          else
            throw new IllegalArgumentException("PSApplication expected!");

           m_root.setText(m_app.getRequestRoot());
         if (figure.getData() instanceof OSResultPage)
         {
            m_webpage = (OSResultPage) figure.getData();
            UTAppNavigator nav = new UTAppNavigator();
            Vector datasets = nav.getSourceDatasets((UIConnectableFigure) figure);
            Vector backendTanks = new Vector();
            Vector<OSPageDatatank> pageTanks = new Vector<OSPageDatatank>();
            OSDataset dataset = null;

            if (datasets != null)
            {
               for (int i=0, n=datasets.size(); i<n; i++)
               {
                  UIConnectableFigure uic = (UIConnectableFigure) datasets.get(i);
                  dataset = (OSDataset) uic.getData();
                  // fixed bug (Rx-00-01-0009): making "XML Element" available again
                  // in ValueSelectorDialog. See *rev1.20*
                  if (dataset.getPageDataTank() != null)
                  {
                     OSPageDatatank pageTank = (OSPageDatatank) dataset.getPageDataTank();
                     pageTanks.add(pageTank);
                  }
                  ///////////////////// (Rx-00-01-0009)
               }
            }

            if (datasets == null || datasets.size() == 0)
               m_requestor.setText(getResources().getString("none"));
            else if (datasets.size() == 1)
               m_requestor.setText(dataset.getRequestor().getRequestPage());
            else
               m_requestor.setText(getResources().getString("multiple"));

            m_stylesheet.removeAllItems();
            m_stylesheet.addItem( ms_defaultSS );
            if (m_webpage.getStyleSheet() != null)
            {
               m_stylesheet.addItem(m_webpage.getStyleSheet().toString());
               m_stylesheet.setSelectedItem(m_webpage.getStyleSheet().toString());
            }
            
            m_namespaceCleanup.setSelected(m_webpage.allowNamespaceCleanup());

            // fill in the drop list for the XML fields
            fillXmlDropList( pageTanks );

            m_defaultRadio.setSelected( true );
            Collection c = m_webpage.getExtensions();

            m_includeExt.setSelected( c.size() != 0);
            m_extText.setText( buildExtensionString( c ));

            IPSReplacementValue mimeType = m_webpage.getMimeType();
            if ( null == mimeType )
               m_defaultRadio.setSelected( true );
            else if ( mimeType instanceof PSXmlField )
            {
               m_xmlRadio.setSelected( true );
               m_xmlElement.setSelectedItem( mimeType.getValueDisplayText());
            }
            else
            {
               m_userSpecifiedRadio.setSelected( true );
               m_userSpecifiedList.setSelectedItem(
                  mimeType.getValueDisplayText());
            }

            String encoding = m_webpage.getCharacterEncoding();
            if ( null == encoding )
               encoding = USE_DEFAULT_CHAR_ENCODING;
            m_charEncoding.setSelectedItem( encoding );

            // refresh the value selector dialog
            ValueSelectorDialogHelper h = new ValueSelectorDialogHelper(
               backendTanks, pageTanks);
            Vector types = h.getDataTypes();
              m_variableDialog.refresh(types);
            m_valueDialog.refresh(types);

            // remove existing rows first
            m_conditionals = m_webpage.getConditionals();
            while (m_model.getRowCount() > 0)
               m_model.deleteRow(0);

            m_model.loadFromConditionals(m_conditionals);
            m_model.appendRow(24 - m_table.getRowCount());

            initGraying();
            this.setVisible(true);
         }
         else
            throw new IllegalArgumentException("OSResultPage expected!");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return m_modified;
   }


   public void onOk()
   {
      initValidationFramework();
      if ( !activateValidation())
         return;

      // validate the xml element field format
      IPSReplacementValue mimeType = null;
      if ( m_xmlRadio.isSelected())
      {
         try
         {
            /* We support a hidden feature here. The user could enter any
               'PSX...' type value here. The only documented thing to enter is
               an xml element. */
            String element = (String) m_xmlElement.getSelectedItem();
            element = element.trim();
            mimeType = PSReplacementValueFactory
               .getReplacementValueFromXmlFieldName( element );
         }
         catch ( IllegalArgumentException e )
         {
            String msg = getResources().getString( "InvalidElementFormat" );
            String title = getResources().getString( "ValidationErrorTitle" );
            JOptionPane.showMessageDialog(this, title,
               MessageFormat.format( msg, new Object[] { e.getLocalizedMessage() }),
               JOptionPane.ERROR_MESSAGE );
            return;
         }
      }

      String urlString = m_stylesheet.getSelectedItem().toString();
      boolean bDefault = 0 == urlString.trim().length()
            || urlString.equals( ms_defaultSS );
      if ( bDefault )
      {
         m_webpage.setStyleSheet(null);
      }
      else
      {
         URL url = null;
         try
         {
            // add XSL extension if none was provided
            if (urlString.lastIndexOf(".") == -1)
               urlString += Util.XSL_FILE_EXTENSION;

            url = new URL(urlString);
         }
         catch (MalformedURLException e)
         {
            try
            {
               // try again adding the protocol automatically
               urlString = Util.createURLString(urlString, Util.XSL_FILE_EXTENSION);
               url = new URL(urlString);
            }
            catch (MalformedURLException e2)
            {
               Object[] params =
               {
                  urlString,
               };

               // its still not possible to create the URL, inform the user and return
               JOptionPane.showMessageDialog(this,
                                                   MessageFormat.format(E2Designer.getResources().getString("InvalidURL"), params),
                              E2Designer.getResources().getString("OpErrorTitle"),
                              JOptionPane.WARNING_MESSAGE);
               return;
            }
         }
         m_webpage.setStyleSheet(url);
      }
      
      m_webpage.setAllowNamespaceCleanup(m_namespaceCleanup.isSelected());

      /* End the cell editing mode before validation. If the user presses the
         OK button before Enter, the contents of the editor will be recognized. */
      if ( m_table.isEditing())
         m_table.getCellEditor().stopCellEditing();

      ConditionalValidationError error = m_model.validate();
      if ( null != error )
      {
         JOptionPane.showMessageDialog(this, error.getErrorText(),
               getResources().getString( "ValidationErrorTitle" ), JOptionPane.WARNING_MESSAGE);
         m_table.getSelectionModel().clearSelection();
         m_table.editCellAt( error.getErrorRow(), error.getErrorCol());
         m_table.getEditorComponent().requestFocus();
         return;
      }
      try
      {
         m_webpage.setConditionals( m_model.saveToConditionals(m_conditionals));
      }
      catch ( ClassNotFoundException e )
      {
         PSDlgUtil.showError( e );
      }
      catch ( IllegalArgumentException iae )
      {
         PSDlgUtil.showError( iae );
      }

      if ( m_includeExt.isSelected())
         m_webpage.setExtensions(
            tokenizeExtensions((String) m_extText.getText()));
      else
         m_webpage.setExtensions( null );

      if ( m_userSpecifiedRadio.isSelected())
      {
         String mime = (String) m_userSpecifiedList.getSelectedItem();
         mimeType = new PSTextLiteral( mime.trim());
      }
      // for default and xml, the mime type was set while validating
      m_webpage.setMimeType( mimeType );

      String encoding = (String) m_charEncoding.getSelectedItem();
      if ( null != encoding )
      {
         if ( encoding.equals( USE_DEFAULT_CHAR_ENCODING ))
            encoding = null;
         else
            encoding = encoding.trim();
      }
      m_webpage.setCharacterEncoding( encoding );

      dispose();
   }



   /**
    * Tokenizes the list of strings in the supplied string, using <code>
    * EXTENSIONS_DELIMITER</code> as the delimiter.
    *
    * @param ext A concatenation of strings using a delimiter. May be <code>
    * null</code>.
    *
    * @return A collection containing 1 or more tokenized values. <code>null
    * </code> if ext is <code>null</code> or empty.
    */
   private Collection tokenizeExtensions( String ext )
   {
      if ( null == ext || ext.trim().length() == 0 )
         return null;

      List<String> l = new ArrayList<String>();
      StringTokenizer toker = new StringTokenizer( ext, EXTENSION_DELIMITER );
      while ( toker.hasMoreTokens())
         l.add( toker.nextToken().trim());
      return l;
   }


   /**
    * Takes all of the entries in c and appends them in ascending dictionary
    * order, separated by semi-colons.
    *
    * @param c A collection of 0 or more String objects. May be <code>null</code>.
    *
    * @return A string containing all the elements in c separated by the
    * <code>EXTENSION_DELIMITER</code> delimiter. If the collection is empty
    * or <code>null</code>, the empty string is returned.
    */
   @SuppressWarnings("unchecked")
   private String buildExtensionString( Collection c )
   {
      if ( null == c || c.size() == 0 )
         return "";

      ArrayList<Object> extensions = new ArrayList<Object>();
      extensions.addAll( c );
      Collections.sort( extensions, Collator.getInstance());
      Iterator iter = extensions.iterator();
      // arbitrarily chosen length
      StringBuffer buf = new StringBuffer( 200 );
      boolean first = true;
      while ( iter.hasNext())
      {
         if ( !first )
            buf.append( EXTENSION_DELIMITER );
         buf.append((String) iter.next());
         first = false;
      }

      return buf.toString();
   }

   //////////////////////////////////////////////////////////////////////////////
  /**
   * the selector data
   */
  private OSResultPage m_webpage = null;
  private PSApplication m_app = null;
  private PSCollection m_conditionals = null;
  /**
   * this flag will be set if any data within this dialog was modified
   */
  private boolean m_modified = false;
   /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
   /**
   * the application root display field
   */
  private UTFixedHeightTextField m_root = new UTFixedHeightTextField();
   /**
   * the requestor URL display field
   */
  private UTFixedHeightTextField m_requestor = new UTFixedHeightTextField();
   /**
   * the requestor URL display field
   */
  private UTFixedHeightComboBox m_stylesheet = new UTFixedHeightComboBox();
  /**
   * the conditional table
   */
  ConditionalTableModel m_model = new ConditionalTableModel();
  UTJTable m_table = new UTJTable(m_model);
  /**
   * The table cell editors
   */
  ValueSelectorCellEditor m_variableEditor = null;
  ValueSelectorCellEditor m_valueEditor = null;
  /**
   * The dialogs associated with the ConditionalCellEditor.
   */
  ValueSelectorDialog m_variableDialog = null;
  ValueSelectorDialog m_valueDialog = null;

   /**
    * The control used to specify whether extensions should be used with the
    * other conditions. If checked, the user must specify at least 1 extension
    * in the m_extText field.
    */
   private JCheckBox m_includeExt;
   
   /**
    * Control used to specify if namespace cleanup is allowed for this
    * result page. If checked then namespace cleanup will be performed
    * after transformation.
    */
   private JCheckBox m_namespaceCleanup;
   
   /**
    * If <code>m_includedExt</code> is checked, this field must contain the
    * extensions to include, semi-colon separated.
    */
   private UTFixedHeightTextField m_extText = new UTFixedHeightTextField();
   /**
    * The label for <code>m_extText</code>, kept locally so it can be grayed
    * out.
    */
   private JLabel m_extLabel = null;

   /**
    * One of the radio button used to select how the MIME type is to be
    * specified. If checked, the type specified in the attached resource is
    * used.
    */
   private JRadioButton m_defaultRadio;
   /**
    * One of the radio button used to select how the MIME type is to be
    * specified. If checked, the type entered/selected in <code>
    * m_userSpecifiedList</code> will be used.
    */
   private JRadioButton m_userSpecifiedRadio;
   /**
    * One of the radio button used to select how the MIME type is to be
    * specified. If checked, the type specified in the element specified in
    * the <code>m_xmlElement</code> control.
    */
   private JRadioButton m_xmlRadio;

   /**
    * If the <code>m_userSpecifiedRadio</code> button is checked, this field
    * contains the MIME type to use for this web page.
    */
   private UTFixedHeightComboBox m_userSpecifiedList = new UTFixedHeightComboBox();
   /**
    * If the <code>m_xmlRadio</code> button is checked, this field contains
    * the MIME type to use for this web page.
    */
   private UTFixedHeightComboBox m_xmlElement = new UTFixedHeightComboBox();

   /**
    * Contains the text <code>USE_DEFAULT_CHAR_ENCODING</code> or a user
    * specified IANA character encoding name. If the default text is chosen,
    * this means use the char encoding specified in the attached resource.
    */
   private UTFixedHeightComboBox m_charEncoding = null;

   /**
    * A flag that indicates whether the validation framework needs to be
    * created before activating validation. We use the flag so we will only
    * build the framework if the user presses Ok.
    */
   private boolean m_validationInited = false;

   /**
    * The default value for <code>m_charEncoding</code>. If chosen, it means
    * the encoding specified in the resource will be used.
    */
   private static final String USE_DEFAULT_CHAR_ENCODING =
      "Use resource's character encoding";

   /* This is used as the default name to display when the stylesheet name
      is empty. */
   private static String ms_defaultSS = OSResultPage.getDefaultStylesheetName();

   /**
    * This is the key used to cache the charset combo box in the global
    * cache. We want to pick a name that is globally unique. We cache this
    * guy so we don't have to read a file, sort and fill the drop list each
    * time this dialog is instantiated/class loaded.
    */
   private static final String CHARSET_CB_CACHE_KEY = "Webpage_charsets_cb";

   /**
    * This is the key used to cache the mime type combo box in the global
    * cache. We want to pick a name that is globally unique. We cache this
    * guy so we don't have to read a file, sort and fill the drop list each
    * time this dialog is instantiated/class loaded.
    */
   private static final String MIMETYPE_CB_CACHE_KEY = "Webpage_mimetypes_cb";

   /**
    * The single character delimiter (as a String object) to use between
    * extensions in <code>m_extText</code>. It should be a character that
    * can't appear in filenames.
    */
   private static final String EXTENSION_DELIMITER = ";";

   /**
    * This is the color of the background of the editor when it is disabled,
    * so it is clear that it is disabled. Leaving the background white makes
    * it difficult to see that the editor is disabled. Must not be <code>null
    * </code>.
    */
   private static final Color DISABLED_COLOR = Color.lightGray;
}
