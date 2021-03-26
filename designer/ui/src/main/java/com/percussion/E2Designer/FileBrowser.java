/*[ FileBrowser.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.util.PSSortTool;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

/** This dialog allows user to browse through XML, XSL, and other files available
 * in the E2 server.  The FileBrowser functions much like the right side of the
 * Windows Explorer application.  It shows contents (directories and files)
 * only.
 * <P>
 * Currently, this is the first working version WITHOUT cataloging capabilities
 * due to the insufficient support for XML directory structures.  This version
 * also lacks the support for listing multiple drives in the &quot;look in&quot;
 * ComboBox.  Works with version 1.1 of the following files only.
 * <UL>
 * <LI>FileBrowserComboBoxRenderer</LI>
 * <LI>FileBrowserListRenderer</LI>
 * </UL>
 */

public class FileBrowser extends PSDialog implements ItemListener, ActionListener
{
   static String name = "root";
   FileInputStream readIn = null;

   //
   // CONSTRUCTORS
   //

   /** Constructs a FileBrowser object with a Frame object as a parent.  Currently,
    * the Browser only takes the &quot.xml&quot extension. &quot;.xsl&quot; extension
    * can be added easily.
    */
   public FileBrowser( Frame f )
   {
      super( f );

      initializeButtons( );

      m_xmlFactory = new PSXmlDocumentBuilder( );

      //System.out.println("Before adding dirStruct name...");

      // adds the root directory to directory structure vector child directories
      m_dirStruct.addElement( name );   // will be added after double clicking on a directory
      m_dirStructLevel.addElement( new Integer( m_levelCounter ) ); // adding level to match m_dirStruct vector

      //System.out.println("After adding dirStruct name...");

      m_dirStructModel = new FileBrowserComboBoxModel( m_dirStruct, m_dirStructLevel );

      //System.out.println("Before setting comboBox model");

      m_dirStructBox = new JComboBox( m_dirStructModel );

      //System.out.println("Before setting comboBox renderer");

      m_dirStructModel.setSelectedIndex( m_levelCounter );
      m_dirStructBox.setPreferredSize( new Dimension( 200, 20 ) );
      m_dirStructBox.setMaximumSize( m_dirStructBox.getPreferredSize( ) );
      m_dirStructBox.setBorder( new CompoundBorder( new BevelBorder( BevelBorder.LOWERED, Color.white, Color.darkGray ),
            new EmptyBorder( 0,3,0,0 ) ) );

      m_dirStructBox.setEditable( false );
      m_dirStructBox.addItemListener( this );

      //System.out.println("Before setting List data");

      m_currentDirBox = new JList( new DefaultListModel( ) );
      m_currentDirBox.setCellRenderer( new FileBrowserListRenderer( this ) );

      //System.out.println("Before setting XML doc...");

      setXmlDoc( name + XML );

      //System.out.println("After setting XML doc...");

      m_dirStructBox.setRenderer( new FileBrowserComboBoxRenderer( this ) );

      m_currentDirBox.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      m_currentDirBox.addMouseListener( new java.awt.event.MouseAdapter( )
            {
         public void mouseClicked( java.awt.event.MouseEvent e )
         {
            java.awt.Rectangle box;
            int x, y;

            if (((DefaultListModel)m_currentDirBox.getModel( )).getSize( ) != 0)
            {
               box = m_currentDirBox.getCellBounds( 0,
                     ((DefaultListModel)m_currentDirBox.getModel( )).getSize( ) - 1 );

               x = (int)box.getWidth( );
               y = (int)box.getHeight( );
            }
            else
            {
               x = 0;
               y = 0;
            }

            if ((e.getX( ) <= x) && (e.getY( ) <= y))
               if (e.getClickCount( ) == m_clickCountToStart)
               {
                  // setting flag to prevent uneeded action handling by
                  // itemStateChanged.
                  m_isStructBoxAction = false;

                  // gets file name from JList
                  String xmlName = (String)m_currentDirBox.getSelectedValue( );

                  //System.out.println("XML name: " + xmlName);

                  //System.out.println("+++++++++++++++++++");
                  //System.out.println(m_dirStructModel.toString());
                  //System.out.println("+++++++++++++++++++");

                  // if the xml variable does not have a .xml extension...
                  // then it is a directory
                  if (xmlName.lastIndexOf( XML ) == -1)
                  {
                     m_fileNameField.setText( "" );

                     ((DefaultListModel)m_currentDirBox.getModel( )).removeAllElements( );

                     setXmlDoc( xmlName + XML );
                     //m_dirStructBox.setSelectedItem(xmlName);

                     //System.out.println("Before: parent = " + m_parent);

                     // only increment levelCounter if current directory is not
                     // root; and new element has to be a subdir of
                     // a drive ( > level 1 element) in order to be added to StructBox.
                     //System.out.println(" Selected item = " + m_dirStructModel.getSelectedItem().toString());
                     if (m_levelCounter >= 1)
                     {
                        //System.out.println("  In m_levelCounter > 1... " + xmlName + " level = " + Integer.toString(m_levelCounter));
                        //System.out.println("  selected item + 1: " + (m_dirStructModel.getIndexOf(m_dirStructModel.getSelectedItem()) + 1));
                        //System.out.println("  inserting: " + xmlName + "; Level: " + m_levelCounter);

                        m_dirStructModel.insertElementAt( xmlName, m_levelCounter+1, m_dirStructModel.getIndexOf( m_dirStructModel.getSelectedItem( ) ) + 1 );

                        //System.out.println("  finished inserting...");

                        m_parent = (String)m_dirStructModel.getElementAt( m_dirStructModel.getIndexOf( xmlName ) - 1 );
                     }
                     else
                     {
                        //System.out.println("  In m_levelCounter < 1... " + xmlName);
                        m_dirName = xmlName;
                        m_parent = (String)m_dirStructModel.getElementAt( 0 );
                     }

                     //System.out.println("After: parent = " + m_parent);

                     m_parentIndex = m_dirStructModel.getIndexOf( m_parent );

                     // marking index of new sub directory if its direct parent is
                     // a drive (level 1)
                     if (m_dirStructModel.getLevelAt( m_parentIndex ) > 1)
                     {
                        // m_markerBegin == selected sub-directory
                        m_markerEnd = m_dirStructModel.getIndexOf( xmlName );
                     }
                     else if (m_dirStructModel.getLevelAt( m_parentIndex ) == 1)
                  {
                     m_markerBegin = m_dirStructModel.getIndexOf( m_dirName ) + 1;
                     m_markerEnd   = m_markerBegin;
                  }
                  else       // if root
                  {
                     m_markerBegin = -1;
                     m_markerEnd   = -1;
                  }

                  m_dirStructModel.setSelectedItem( xmlName );

                  //System.out.println(" parent level: " + m_dirStructModel.getLevelAt(m_parentIndex));
                  //System.out.println(" MarkerBegin: " + m_markerBegin);
                  //System.out.println(" MarkerEnd: " + m_markerEnd);
                  //System.out.println("=======================================");

                  m_levelCounter++;

                  if (m_parent != null)
                     m_upTreeButton.setEnabled( true );
                  else
                     m_upTreeButton.setEnabled( false );
                  }   // end if (e.xmlName.lastIndexOf...
                  m_isStructBoxAction = true;
               }   // end if (e.getClickCount...
               else
                  if (e.getClickCount( ) == 1)
            {
               String xmlName = (String)m_currentDirBox.getSelectedValue( );
               if (xmlName.lastIndexOf( XML ) != -1)
               {
                  m_fileNameField.setText( xmlName );
               }
            }
         }    // end public void...
      });   // end MouseAdapter code

      // setting up bottom labels
      m_fileName = new JLabel( getResources( ).getString( "fileName" ), SwingConstants.RIGHT );
      m_fileName.setPreferredSize( new Dimension( 180, 20 ) );
      JPanel fName = new JPanel( );
      fName.setLayout( new BoxLayout( fName, BoxLayout.X_AXIS ) );
      fName.add( m_fileName );

      m_fileType = new JLabel( getResources( ).getString( "fileType" ), SwingConstants.RIGHT );
      m_fileType.setPreferredSize( new Dimension( 180, 20 ) );
      JPanel fType = new JPanel( );
      fType.setLayout( new BoxLayout( fType, BoxLayout.X_AXIS ) );
      fType.add( m_fileType );

      JPanel labelPanel = new JPanel( );
      labelPanel.setLayout( new GridLayout( 2,1 ) );
      labelPanel.setBorder( new EmptyBorder( 0,0,0,5 ) );
      ((GridLayout)labelPanel.getLayout( )).setVgap( 4 );
      labelPanel.add( m_fileName );
      labelPanel.add( m_fileType );

      // setting up the bottom TextField and ComboBox
      m_fileNameField = new JTextField( 20 );
      m_fileNameField.setPreferredSize( new Dimension( 200, 20 ) );

      String[] fileTypeArray = new String[2];
      fileTypeArray[0] = "XML files";
      fileTypeArray[1] = "All files";
      m_fileTypeField = new JComboBox( fileTypeArray );
      m_fileTypeField.setPreferredSize( new Dimension( 200, 20 ) );
      m_fileTypeField.setSelectedIndex( 0 );
      m_fileTypeField.setBorder( new BevelBorder( BevelBorder.LOWERED, Color.white, Color.darkGray ) );
      m_fileTypeField.addItemListener( this );

      JPanel fieldPanel = new JPanel( );
      fieldPanel.setLayout( new GridLayout( 2,1 ) );
      ((GridLayout)fieldPanel.getLayout( )).setVgap( 4 );
      fieldPanel.add( m_fileNameField );
      fieldPanel.add( m_fileTypeField );

      // putting together the bottom labels and fields onto one JPanel
      JPanel bottomLeftPanel = new JPanel( );
      bottomLeftPanel.setBorder( new EmptyBorder( 3,0,0,0 ) );
      bottomLeftPanel.setLayout( new BoxLayout( bottomLeftPanel, BoxLayout.X_AXIS ) );
      bottomLeftPanel.add( Box.createHorizontalGlue( ) );
      bottomLeftPanel.add( labelPanel );
      bottomLeftPanel.add( Box.createHorizontalStrut( 3 ) );
      bottomLeftPanel.add( fieldPanel );
      bottomLeftPanel.add( Box.createHorizontalGlue( ) );

      // creating top label
      m_look = new JLabel( getResources( ).getString( "look" ), SwingConstants.RIGHT );
      m_look.setPreferredSize( new Dimension( 100, 10 ) );

      JPanel treeButtonPanel = new JPanel( );
      treeButtonPanel.add( m_upTreeButton );

      JPanel structBoxPanel = new JPanel( );
      structBoxPanel.add( m_dirStructBox );

      // creating top panel with a label, the m_dirStructBox, and the upTreeButton
      JPanel topLeftPanel = new JPanel( );
      /*
      topLeftPanel.setLayout(new BorderLayout());
      topLeftPanel.add(m_look, BorderLayout.WEST);
      topLeftPanel.add(m_dirStructBox, BorderLayout.CENTER);
      topLeftPanel.add(treeButtonPanel, BorderLayout.EAST);
      */
      topLeftPanel.setLayout( new BoxLayout( topLeftPanel, BoxLayout.X_AXIS ) );
      topLeftPanel.add( Box.createHorizontalGlue( ) );
      topLeftPanel.add( m_look );
      topLeftPanel.add( Box.createHorizontalGlue( ) );
      topLeftPanel.add( Box.createHorizontalStrut( 5 ) );
      topLeftPanel.add( Box.createHorizontalGlue( ) );
      topLeftPanel.add( structBoxPanel );//m_dirStructBox);
      topLeftPanel.add( Box.createHorizontalGlue( ) );
      topLeftPanel.add( Box.createHorizontalStrut( 5 ) );
      topLeftPanel.add( Box.createHorizontalGlue( ) );
      topLeftPanel.add( treeButtonPanel );
      topLeftPanel.add( Box.createHorizontalGlue( ) );


      // adding the JList to a JScrollPane
      JScrollPane listPane = new JScrollPane( m_currentDirBox );

      // putting JList on another JPanel to provide an additional border
      JPanel dirBoxPanel = new JPanel( new BorderLayout( ) );
      dirBoxPanel.add( listPane, BorderLayout.CENTER );
      dirBoxPanel.setBorder( new CompoundBorder( new EmptyBorder( 3,0,3,0 ),
            new BevelBorder( BevelBorder.LOWERED ) ) );

      // integrating the whole left side of the the FileBrowser dialog
      JPanel leftPanel = new JPanel( );
      leftPanel.setLayout( new BoxLayout( leftPanel, BoxLayout.Y_AXIS ) );
      leftPanel.add( Box.createVerticalGlue( ) );
      leftPanel.add( topLeftPanel );
      leftPanel.add( listPane );
      leftPanel.add( bottomLeftPanel );
      leftPanel.add( Box.createVerticalGlue( ) );

      // putting the whole dialog together
      JPanel panel = new JPanel( );
      panel.setBorder( new EmptyBorder( 1,3,3,3 ) );
      panel.setLayout( new BorderLayout( ) );
      panel.add( leftPanel, BorderLayout.CENTER );
      panel.add( m_buttonPanel, BorderLayout.EAST );

      getContentPane( ).add( panel );
      setSize( 440, 250 );
      setResizable( false );
   }

   //
   // PUBLIC METHODS
   //

   /** Gets the vector containing the current directory's directories. */
   public Vector getDirList( )
   { return m_dirList; 
   }

   /** Gets the vector containing the current dirctory's files. */
   public Vector getFileList( )
   { return m_fileList; 
   }

   /** Gets the item count where the directories and files separate.  */
   public int getDirFileSeparater( )
   { return m_dirFileSeparater; 
   }


   public FileBrowserComboBoxModel getDirStructModel( )
   { return m_dirStructModel; 
   }


   /** Testing method for FileBrowser.  Gets the file name of an xml file to read
   * and passes an xml document to setListData(Document) to create the JList data
   * vector.
   *
   * @param path a String containing to path to the xml file to be read
   */
   public void setXmlDoc( String fileName )
   {
      Document doc = null;

      File file = new File( "D:" + File.separator + "XMLTest" + File.separator + fileName );
      if (file.exists( ))
      {
         try
         {
            readIn = new FileInputStream( file );

            doc = m_xmlFactory.createXmlDocument( readIn, false );
         }
         catch (Exception e)
         { System.out.println( e.toString( ) ); 
         }

         setListData( doc );
      }
      else
         System.out.println( "File does not exist!" );
   }

   /** Takes an xml document to populate the directory/file vectors by searching
    * for Tables with type &quot;directory&quot; or &quot;file&quot;.  After the
    * vectors are populated, they are sorted alphabetically with PSSortTool.quickSort.
    * Lastly, the 2 sorted vectors are stuffed into the vector that displays the
    * current directory (directories first, then files).
    *
    * @param doc a org.w3c.dom.Document containing the directory and file data
    */
   public void setListData( Document doc )
   {
      m_walker = new PSCatalogResultsWalker( doc );
      m_dirList = new Vector( );
      m_fileList = new Vector( );
      m_currentDirList = new Vector( );

      //System.out.println("Contents of XML file: ");
      while(m_walker.nextResultObject( "Table" ))
      {
         if (m_walker.getResultData( TYPE ).equals( DIRECTORY ))
         {
            //System.out.println(m_walker.getResultData(NAME));
            m_dirList.add( m_walker.getResultData( NAME ) );
         }
         else if (m_walker.getResultData( TYPE ).equals( FILE ))
         {
            //System.out.println(m_walker.getResultData(NAME));
            m_fileList.add( m_walker.getResultData( NAME ) + XML );
         }
      }

      // sorting dirList and fileList alphabetically
      PSSortTool.QuickSort( m_dirList, java.text.Collator.getInstance( ) );
      PSSortTool.QuickSort( m_fileList, java.text.Collator.getInstance( ) );

      //System.out.println("After sorting");


      // directory structure comboBox stuff...
      if (m_isFirstRun == true) // establishing multiple drives...
      {
         //System.out.println("In first run!");

         // adding "Drive" folders to dirStructBox permanently
         for (int i = 0; i < m_dirList.size( ); i++)
         {
            m_dirStructModel.addElement( m_dirList.elementAt( i ), 1 );
         }

         //System.out.println("After loop");
         m_isStructBoxAction = true;
         m_isFirstRun = false;  // this only happens once
      }

      m_currentDirList.addAll( m_dirList );

      //System.out.println("After adding dirList to currentDirList...");

      m_dirFileSeparater = m_currentDirList.size( ); // storing the number directories
      // for icon placement.
      m_currentDirList.addAll( m_fileList );

      for (int i = 0; i < m_currentDirList.size( ); i++)
      {
         //System.out.println("  element: " + m_currentDirList.elementAt(i));
         ((DefaultListModel)m_currentDirBox.getModel( )).addElement( m_currentDirList.elementAt( i ) );
      }
   }

   /** Handles JComboBox item change events. */
   public void itemStateChanged( java.awt.event.ItemEvent e )
   {
      //System.out.println(e.getStateChange() + ": " + (String)e.getItem());
      JComboBox boxRef = (JComboBox)e.getSource( );

      if (boxRef == m_dirStructBox)
      {
         // "reminds" this method not the run when called from actionPerformed
         //if ((boxRef.getSelectedIndex() == m_deleteReminder)) // || (m_deleteReminder == -1))
         if (m_isStructBoxAction == true)
            if (e.getStateChange( ) == java.awt.event.ItemEvent.SELECTED)
            {
               m_fileNameField.setText( "" );
               //System.out.println("Entered itemStateChanged()!");
               //System.out.println("  Source is dirStructBox...");

               int tempMarker = 0;  //  <-----   HERE it is!!!

               // selected any index other than root
               //System.out.println( "selected index = " + Integer.toString(m_dirStructModel.getIndexOf(m_dirStructModel.getSelectedItem())));
               if (m_dirStructModel.getIndexOf( m_dirStructModel.getSelectedItem( ) ) > 0)
               {
                  ((DefaultListModel)m_currentDirBox.getModel( )).removeAllElements( );

                  setXmlDoc( (String)m_dirStructModel.getSelectedItem( ) + XML );

                  // checking the level of the selected index
                  if (m_dirStructModel.getSelectedLevel( ) > 1)
                  {
                     tempMarker = m_dirStructModel.getIndexOf( m_dirStructModel.getSelectedItem( ) ) + 1;
                     //tempMarker = m_markerBegin;
                     m_levelCounter = m_dirStructModel.getSelectedLevel( );
                  }
                  else// if (m_dirStructModel.getSelectedLevel() == 1)
                  {
                     if (m_markerBegin == -1 && m_markerEnd == -1 )// && m_parent == null)
                     {
                        tempMarker = 1; //m_dirStructModel.getSelectedIndex();
                     }
                     else
                        tempMarker = m_markerBegin;

                     m_levelCounter = 1;
                  }

                  // removing children that are out of current tree scope.
                  //int level = m_dirStructModel.getLevelAt(m_dirStructModel.getIndexOf(m_dirName));
                  int counter = 0;

                  if (m_markerBegin != -1 && m_markerEnd != -1)
                  {
                     for (int i = tempMarker; i <= m_markerEnd; i++)
                     {
                        //System.out.println("  Removing item (itemStateChanged): " + m_dirStructModel.getElementAt(tempMarker));
                        m_dirStructModel.removeElementAt( tempMarker );
                        counter++;
                     }

                     m_markerEnd = m_markerEnd - counter;

                     // if a Drive or root folder was selected, remove markers
                     if (tempMarker == m_markerBegin)
                     {
                        m_markerBegin = -1;
                        m_markerEnd   = -1;
                        //==========
                        tempMarker = 1;
                     }
                     else
                        tempMarker = tempMarker - 1;
                     //===========
                  }
               }
               else // at root
               {
                  m_levelCounter = 0;
                  tempMarker = -1;
                  m_dirName = null;
                  ((DefaultListModel)m_currentDirBox.getModel( )).removeAllElements( );

                  setXmlDoc( (String)m_dirStructModel.getSelectedItem( ) + XML );
                  if (m_markerBegin != -1 && m_markerEnd != -1)
                     for (int i = m_markerBegin; i <= m_markerEnd; i++)
                     {
                        //System.out.println("  Removing item (itemStateChanged): " + m_dirStructBox.getItemAt(m_markerBegin));
                        m_dirStructModel.removeElementAt( m_markerBegin );
                     }

                     m_markerBegin = -1;
                     m_markerEnd   = -1;
               }

               m_parentIndex = tempMarker;

               if (m_parentIndex <= 0)
               {
                  m_parent = null;
                  m_upTreeButton.setEnabled( false );
               }
               else
               {
                  m_parent = (String)m_dirStructModel.getElementAt( m_parentIndex - 1 );
                  m_upTreeButton.setEnabled( true );
               }

               //System.out.println("m_parentIndex = " + m_parentIndex );
               //System.out.println("m_parent is: " + m_parent);

               m_dirName = (String)m_dirStructModel.getSelectedItem( );

               //System.out.println(" parent level: " + m_dirStructModel.getLevelAt(m_parentIndex-1));
               //System.out.println(" MarkerBegin: " + m_markerBegin);
               //System.out.println(" MarkerEnd: " + m_markerEnd);
               //System.out.println("=================================================");
            }   
      }
      else if (boxRef == m_fileTypeField)
      {
         // add filter catalog implementation when xmlFileSystem is ready
      }
   }

   /** Handles all the button action events. */
   public void actionPerformed( java.awt.event.ActionEvent e )
   {
      if (e.getActionCommand( ).equals( UP ))
      {
         // setting flag to prevent uneeded action handling by
         // itemStateChanged.
         m_isStructBoxAction = false;

         m_fileNameField.setText( "" );

         ((DefaultListModel)m_currentDirBox.getModel( )).removeAllElements( );

         setXmlDoc( m_parent + XML );

         int iCurrentItem = m_dirStructModel.getIndexOf( m_parent );

         // if the level of the selected item is > 1
         if (1 < m_dirStructModel.getSelectedLevel( ))
         {
            m_dirStructModel.removeElementAt( iCurrentItem + 1 );
            //System.out.println(" removed item (actionPerformed)...");
            //System.out.println(" Selected index: " + m_dirStructModel.getSelectedIndex());

            // setting parent reference; if the current directory is the Drive,
            // its parent is root; else, the parent is simply the cell above it.
            if (m_dirName.equals( (String)m_dirStructModel.getSelectedItem( ) ))
               m_parent = (String)m_dirStructModel.getElementAt( 0 );
            else
               m_parent = (String)m_dirStructModel.getElementAt( iCurrentItem - 1 );

            // moving markerEnd as well when the latest item is removed
            if ( -1 == m_markerBegin || iCurrentItem < m_markerBegin)
            {
               m_markerBegin = -1;
               m_markerEnd   = -1;
            }
            else
               m_markerEnd = iCurrentItem;

            m_levelCounter = m_dirStructModel.getSelectedLevel( );   
         }
         else if (1 == m_dirStructModel.getSelectedLevel( )) // if Drive folder
         {
            m_dirStructModel.setSelectedIndex( 0 );
            m_parent = null;
            m_levelCounter = 0;
            m_markerBegin = -1;
            m_markerEnd   = -1;
         }
         // getting the index of the new parent
         m_parentIndex = m_dirStructModel.getIndexOf( m_parent );

         if (m_parentIndex < 0)
         {
            //m_parent = null;
            m_upTreeButton.setEnabled( false );
         }
         /*
         else
         {
         m_parent = (String)m_dirStructModel.getElementAt(m_parentIndex);
         }
         */
         m_isStructBoxAction = true;

         //System.out.println("m_parent is: " + m_parent);
         //System.out.println(" MarkerBegin: " + m_markerBegin);
         //System.out.println(" MarkerEnd: " + m_markerEnd);
         //System.out.println("----------------------------------------");
      }
      else if (e.getActionCommand( ).equals( SELECT ))
      {
         setVisible( false );
         // set full path to xml file?
      }
      else if (e.getActionCommand( ).equals( CANCEL ))
      {
         setVisible( false );
         dispose( );
         System.exit( 0 );
      }
      else if (e.getActionCommand( ).equals( HELP ))
         onHelp();
   }

   //
   // PRIVATE METHODS
   //

   private void initializeButtons( )
   {
      m_upTreeButton = new JButton( new javax.swing.ImageIcon( "/e2/designer/Classes/com/percussion/E2Designer/images/uptree.gif" ) );
      m_upTreeButton.setActionCommand( UP );
      m_upTreeButton.addActionListener( this );
      m_upTreeButton.setEnabled( false );
      m_upTreeButton.setPreferredSize( new Dimension( 24, 24 ) );


      m_selectButton = new JButton( getResources( ).getString( "select" ) );
      m_selectButton.setActionCommand( SELECT );
      m_selectButton.addActionListener( this );
      m_selectButton.setPreferredSize( new Dimension( 80, 20 ) );

      m_cancelButton = new JButton( getResources( ).getString( "cancel" ) );
      m_cancelButton.setActionCommand( CANCEL );
      m_cancelButton.addActionListener( this );
      m_cancelButton.setPreferredSize( new Dimension( 80, 20 ) );

      m_helpButton   = new JButton( getResources( ).getString( "help" ) );
      m_helpButton.setActionCommand( HELP );
      m_helpButton.addActionListener( this );
      m_helpButton.setPreferredSize( new Dimension( 80, 20 ) );

      JPanel buttons = new JPanel( new GridLayout( 3,1 ) );
      ((GridLayout)buttons.getLayout( )).setVgap( 3 );
      buttons.add( m_selectButton );
      buttons.add( m_cancelButton );
      buttons.add( m_helpButton );

      m_buttonPanel = new JPanel( );
      m_buttonPanel.add( buttons );
      m_buttonPanel.setBorder( new EmptyBorder( 10,0,0,0 ) );
   }

   public static void main( String[] args )
   {
      JFrame f = new JFrame( );

      try
      {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         FileBrowser fb1 = new FileBrowser( f );

         fb1.setVisible( true );
      }
      catch (Exception e)
      { System.out.println( e.toString( ) ); 
      }
   }

   //
   // PRIVATE PROPERTIES
   //

   private JPanel         m_buttonPanel;

   private JButton        m_upTreeButton, m_selectButton, m_cancelButton, m_helpButton;

   private JLabel         m_fileName, m_fileType, m_look;

   private JTextField     m_fileNameField;

   private JComboBox      m_dirStructBox, m_fileTypeField;
   private JList          m_currentDirBox;

   private PSXmlDocumentBuilder m_xmlFactory = null;
   private PSCatalogResultsWalker m_walker = null;

   /** Used to store directory names from xml doc. */
   private Vector m_dirList;

   /** Used to store file names from xml doc. */
   private Vector m_fileList;

   /** Used to store the JList display of the currently openned directory. */
   private Vector m_currentDirList;

   /** Used to store the JComboBox display of the directory structure from root to
   * the current directory.
   */
   private Vector m_dirStruct = new Vector( );

   /** Used to store the indentation level of the JComboBox display of the directory
   * structure from root to the current directory.
   */
   private Vector m_dirStructLevel = new Vector( );

   /** Used to mark the separation between the directory group and the file group
   * within the current directory list.
   */
   private int m_dirFileSeparater  = 0;

   private int m_clickCountToStart = 2;

   private int m_parentIndex;

   /** Always placed at the first child of the drive parent of the sub tree. */
   private int m_markerBegin = -1;

   private int m_markerEnd   = -1;

   private int m_levelCounter = 0;

   private boolean m_isFirstRun = true;

   /** Flag used to distinguish dirStructBox actions from currentDirBox actions.
   * <BR>false = action from currentDirBox.
   * <BR>true = action from dirStructBox.
   */
   private boolean m_isStructBoxAction = false;

   private String m_parent = null;
   private String m_dirName;

   private FileBrowserComboBoxModel m_dirStructModel;

   private final static String SELECT = "select";
   private final static String CANCEL = "cancel";
   private final static String HELP   = "help";
   private final static String UP     = "up";
   private final static String XML    = ".xml";
   private final static String TYPE   = "type";
   private final static String NAME   = "name";

   private final static String FILE   = "file";
   private final static String DIRECTORY = "directory";
}

 
