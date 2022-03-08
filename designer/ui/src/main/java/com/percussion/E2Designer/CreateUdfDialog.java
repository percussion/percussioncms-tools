/*[ CreateUdfDialog.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSCollection;
import com.percussion.utils.collections.PSIteratorUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

/** 
 *
 * Class for creating or editing user defined functions.
 */
public class CreateUdfDialog extends PSDialog
{
   /**
    *
    * Construct the CreateUdfDialog with a collection of IPSExtensionDef objects.
    *
    * @param parent the parent JDialog. Can be <code>null</code>, but should not
    * be <CODE>null</CODE> due to a focus problem when application loses topmost
    * z-order.
    * @param c a PSUdfSet object. Can NOT be <code>null</code>.
    *
    * @param handlerName The name of the handler to use when creating a new
    * UDF. If <code>null</code> or empty, a list box will be shown in the
    * dialog to allow the user to select the handler.
    */
   public CreateUdfDialog(JDialog parent, PSUdfSet c, String handlerName )
   {
      this( parent, c, handlerName, null );
   }

   public CreateUdfDialog(JDialog parent, PSUdfSet c, IConnectionSource connSrc )
   {       
      this(parent, c, null, connSrc);
   }

   private CreateUdfDialog(JDialog parent, PSUdfSet c, String handlerName,
      IConnectionSource connSrc )
   {
      super( parent );
      m_connectionSource = connSrc;
      m_handlerName = handlerName;
      initDialog();
      initData( c );
   }
   /**
    *
    * Construct the CreateUdfDialog with a collection of IPSExtensionDef objects.
    *
    * @param parent the parent JFrame. Can be <code>null</code>, but should not
    * be <CODE>null</CODE> due to a focus problem when application loses topmost
    * z-order.
    * @param c a PSUdfSet object. Can NOT be <code>null</code>.
    *
    * @param handlerName The name of the handler to use when creating a new
    * UDF. If <code>null</code> or empty, a list box will be shown in the
    * dialog to allow the user to select the handler.
    * @param context a vector of current contexts, can not be <code>null</code>
    * @param categoreies a vector of current categories, can not be <code>null</code>
    * @param jScrExits a vector of all JavaScript extensions, needed for
    * populating a function name combo box when an UDF is created or edited from
    * the admin client. Guaranteed by the calling method not to be <CODE>null</CODE> 
    * @throws IllegalStateException If no scriptable handlers can be
    * cataloged. Before throwing this, a message is displayed to the user.
    */
   public CreateUdfDialog(JFrame parent, PSUdfSet c, String handlerName,
      Vector context, Vector categories, Vector jScrExits )
   {
      this( parent, c, handlerName, null, context, categories,jScrExits  );

   }

   public CreateUdfDialog(JFrame parent, PSUdfSet c, IConnectionSource connSrc )
   {
      this(parent, c, null, connSrc);
   }


   private CreateUdfDialog(JFrame parent, PSUdfSet c, String handlerName,
      IConnectionSource connSrc )
   {
      super( parent );
      m_connectionSource = connSrc;
      m_handlerName = handlerName;

      initDialog();
      initData( c );
   }

  /**
    *
    * Construct the CreateUdfDialog with a collection of IPSExtensionDef objects.
    * @param parent the parent JFrame. Can be <code>null</code>, but should not
    * be <CODE>null</CODE> due to a focus problem when application loses topmost
    * z-order.
    * @param c a PSUdfSet object. Can NOT be <code>null</code>.
    * @param handlerName The name of the handler to use when creating a new
    * UDF. If <code>null</code> or empty, a list box will be shown in the
    * dialog to allow the user to select the handler.
    * @param context a vector of current contexts, assumed not to be <code>null</code>
    * @param categoreies a vector of current categories, assumed not to be <code>null</code>
    * @param jScrExits a vector of all JavaScript extensions, used for populating
    * a function name combo box only when creating or editing a UDF from the
    * admin client.  Assumed not to be <code>null</code>
    * @throws IllegalStateException If no scriptable handlers can be
    * cataloged. Before throwing this, a message is displayed to the user.
    */
   private CreateUdfDialog(JFrame parent, PSUdfSet c, String handlerName,
      IConnectionSource connSrc, Vector context, Vector categories, Vector jScrExits )
   {
      super( parent ); 
      m_connectionSource = connSrc;
      m_handlerName = handlerName;
      setComboBoxes(null, context, categories);     

      m_allExits = jScrExits;

      initDialog();
      initData( c );
      
   }

   /**
    * This form of the constructor can be used when only a single UDF is being
    * edited. No handler list will be visible and the name field will not be
    * editable.
    *
    * @param parent The owner of this dialog. Should not be <code>null</code>
    * or z-order problems may occur.
    *
    * @param def The def that is being edited. May not be <code>null</code>.
    * @param context a vector of current UDFs context can not be <code>null</code>.
    * @param category a vector of current UDFs' categories can not be <code>null</code>.
    * @param os an object store, can be <code>null</code>
    * @param udfs a vector of all udfs, can be <code>null</code>
    */
   public CreateUdfDialog( JFrame parent, IPSExtensionDef def, Vector context,
      Vector category, PSObjectStore os, Vector udfs) 
   {
      super( parent );
      m_isEdit = true;
      m_def = def;
      m_handlerName = def.getRef().getHandlerName();

      if(os != null)
         m_objectStore = os;

      if(udfs != null)
         m_allExits = udfs;

      if(context != null && category != null)
         setComboBoxes(def, context, category);

      initDialog();
      initData( def );
   }

   /**
    * Prepares this dialog for display. Sets up the selection combo box to match
    * the name of the extension definition passed in.
    *
    * @param strDefName The extension definition name that we want to match, can
    * not be <code>null</code>.
    */
   public void onEdit( String strDefName )
   {
      if ( null == strDefName )
         throw new IllegalArgumentException( "Parameter may not be null!" );

      int iCount = m_copyCombo.getItemCount();
      for ( int i = 0; i < iCount; i++ )
      {
         UIExtensionDef item = (UIExtensionDef)m_copyCombo.getItemAt( i );
         String extensionName = item.getExtension().getRef().getExtensionName();
         String context = item.getExtension().getRef().getContext();
         
         if ( extensionName.equals( strDefName ) &&
            context.startsWith("application/"))
         {
            m_copyCombo.setSelectedIndex( i );
            m_functionName.setText(strDefName);
            break;
         }
      }
   }


  /**
   * @returns the PSCollection of newly created or modified udfs.
   */
   public PSCollection getNewUdfs()
   {
      return m_newUdfs;
   }


   /**
    * Initializes the dialog by creating the controls of the dialog and
    * initializes listeners.
    */
   private void initDialog()
   {
      createControls();
      pack();
      center();
   }

   /**
    * Populates the dialog controls with the data of the passed in extension.
    * @param def an extension that the dialog will be populated for, assumed
    * not to be <code>null</code>.
    */
   private void initData( IPSExtensionDef def )
   {
      try
      {
         m_vAllExits = new PSCollection(
            "com.percussion.extension.IPSExtensionDef" );
         m_newUdfs = new PSCollection(
            "com.percussion.extension.IPSExtensionDef");
      }
      catch ( ClassNotFoundException e )
      {
         // ignore, we've hard-coded the name
         e.printStackTrace();
      }
      m_vAllExits.add( def );
      m_copyCombo.addItem(new UIExtensionDef(def));
      // add listeners before setting name
      initListeners();
      m_functionName.setText(def.getRef().getExtensionName());
      // this should force the dialog to be populated
      m_copyCombo.setSelectedIndex(0);
      onCopyComboItemChanged();
   }


   /**
    * Initializes the dialog controls with the data.
    * @param c The set of UDFs which we want to base our dialog, can not be
    * <code>null</code>
    */
   private void initData( PSUdfSet c )
   {
      if(c == null)
         throw new IllegalArgumentException("Passed in PSUdfSet can not be null.");

      m_udfSet = c;

      /* Cannot use getAllUdfs() <-- does not make sense...
       use getUdfs() and get according to the context needed */
      m_vAllExits = c.getAllScriptableUdfs();
      try
      {
         m_newUdfs = new PSCollection("com.percussion.extension.IPSExtensionDef");
      }
      catch (ClassNotFoundException e)
      {
         // will never get here.
         e.printStackTrace();
      }
      // add listeners after all data has been added
      initControls( c );
      initListeners();
   }

   /**
    * Creates the controls for the dialog.
    */
   private void createControls()
   {
      // create left panel
      jLabelName = new JLabel( getResources().getString("FN_NAME") );
      jLabelName.setHorizontalAlignment(SwingConstants.RIGHT);

      m_contextLabel = new JLabel( getResources().getString("context"));
      m_contextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      m_categoryLabel = new JLabel( getResources().getString("category"));

      jLabelParams = new JLabel( getResources().getString("PARAMS") );
      jLabelParams.setHorizontalAlignment(SwingConstants.RIGHT);

      jLabelBody = new JLabel( getResources().getString("BODY") );
      jLabelBody.setHorizontalAlignment(SwingConstants.RIGHT);

      jLabelSummary = new JLabel( getResources().getString("SUMMARY") );
      jLabelSummary.setHorizontalAlignment(SwingConstants.RIGHT);

      JPanel leftPanel = new JPanel();
      leftPanel.setLayout( new BoxLayout( leftPanel, BoxLayout.Y_AXIS ) );
      leftPanel.add( Box.createVerticalStrut( 4 ) );
      leftPanel.add( jLabelName );

      if(m_cbContext != null && m_cbCategory != null)
      { 
         leftPanel.add( Box.createVerticalStrut( 10 ) );
         leftPanel.add( m_contextLabel); 
      }

      leftPanel.add( Box.createVerticalStrut( 12 ) );
      leftPanel.add( jLabelParams );
      leftPanel.add( Box.createVerticalStrut( 73 ) );
      leftPanel.add( jLabelBody );
      leftPanel.add( Box.createVerticalStrut( 130 ) );
      leftPanel.add( jLabelSummary );
      leftPanel.add( Box.createVerticalGlue() );

      // create right panel
      m_paramPanel = new UTExtensionParamPanel( null, null, true, false ) ;
      JPanel topPanel = new JPanel();
      topPanel.setLayout( new BoxLayout( topPanel, BoxLayout.Y_AXIS ) );
      topPanel.add( createFnNamePanel() );

      if(m_cbContext != null && m_cbCategory != null)
      {
        topPanel.add( Box.createVerticalStrut( 6 ) );
        topPanel.add(createPanelCB());

      }

      topPanel.add( Box.createVerticalStrut( 5 ) );
      topPanel.add( m_paramPanel );

      JPanel topPanel_command = new JPanel();
      topPanel_command.setLayout( new BoxLayout( topPanel_command,
                                                 BoxLayout.X_AXIS ) );
      topPanel_command.add( topPanel );
      topPanel_command.add( Box.createHorizontalStrut( 5 ) );
      topPanel_command.add( Box.createHorizontalGlue() );
      topPanel_command.setBorder( new EmptyBorder( 3, 0, 0, 3 ) );

      JScrollPane jScrollPaneBody = new JScrollPane();
      jTextAreaBody = new JTextArea( 8, 20);
      jTextAreaBody.setLineWrap(true);
      jTextAreaBody.setWrapStyleWord(true);
      jScrollPaneBody.getViewport().add(jTextAreaBody);

      JScrollPane jScrollPaneSummary = new JScrollPane();
      jTextAreaSummary = new JTextArea( 5, 20 );
      jTextAreaSummary.setLineWrap(true);
      jTextAreaSummary.setWrapStyleWord(true);
      jScrollPaneSummary.getViewport().add(jTextAreaSummary);

      JPanel rightPanel = new JPanel();
      rightPanel.setLayout( new BoxLayout( rightPanel, BoxLayout.Y_AXIS ) );
      rightPanel.add( topPanel_command );
      rightPanel.add( Box.createVerticalStrut( 5 ) );
      rightPanel.add( jScrollPaneBody );
      rightPanel.add( Box.createVerticalStrut( 5 ) );
      rightPanel.add( jScrollPaneSummary );

      rightPanel.setBorder( new EmptyBorder( 0, 3, 0, 0 ) );

      JPanel panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
      panel.add( leftPanel );
      panel.add( rightPanel );
      panel.setBorder( new EmptyBorder( 3, 3, 3, 3 ) );

      JPanel morePanel = new JPanel( new BorderLayout() );
      morePanel.add( panel, BorderLayout.CENTER );

      JPanel name = createUdfCopyPanel();
      
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(name,BorderLayout.NORTH);
      bottomPanel.add(createCommandPanel(), BorderLayout.SOUTH);
      morePanel.add( bottomPanel, BorderLayout.SOUTH );

      getContentPane().add( morePanel );
   }

   /**
    * Creates the panel for displaying the function name and its version
    */
   private JPanel createFnNamePanel()
   {
      JPanel panel = new JPanel();

      m_functionName = new UTFixedTextField("",180,20);
      m_functionName.setEditable(true);

      jLabelVersion = new JLabel( getResources().getString( "VERSION" ) );
      m_versionField = new UTFixedTextField( "", 120, 20 );

      panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
      panel.add( m_functionName );
      panel.add( Box.createHorizontalStrut( 15 ));
      panel.add( jLabelVersion );
      panel.add( Box.createHorizontalStrut( 3 ));
      panel.add( m_versionField );
      panel.add( Box.createHorizontalGlue() );

      return panel;
   }

   /**
    * Creates the panel which will hold combo boxes for displaying the
    * categories and context
    * @return panel a panel that holds category and context combo boxes
    */
   private JPanel createPanelCB()
     {

      JPanel panel = new JPanel();
      panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
      panel.add( m_cbContext );
      panel.add( Box.createHorizontalStrut( 10 ));
      panel.add( m_categoryLabel);
      panel.add( Box.createHorizontalStrut( 3 ));
      panel.add( m_cbCategory );
      panel.add( Box.createHorizontalGlue() );

      return panel;
   }

   /**
    * Create the panel for displaying the function names and handler names
    */
   private JPanel createUdfCopyPanel()
   {
      JPanel panel = new JPanel();

      JLabel copyLabel =
         new JLabel(getResources().getString("copyLabel"));
      copyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

      panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );

      int horizontalStrut = 27;

      //handler combo box will be visible only from the workbench
      if (null == m_handlerName)
      {
         JLabel handlerLabel =
            new JLabel (getResources().getString("HandlerNameLabel"));
         handlerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         panel.add(handlerLabel);
         panel.add(Box.createHorizontalStrut( 12 ));
         panel.add( m_handlerNameField );
         panel.add( Box.createHorizontalStrut( 25 ));

         horizontalStrut = 3;
      }

      panel.add( copyLabel );
      panel.add( Box.createHorizontalStrut( horizontalStrut ));

      panel.add( m_copyCombo );
      panel.add( Box.createHorizontalGlue() );
      panel.setBorder( new EmptyBorder( 3, 3, 5, 0 ) );
      return panel;
   }


   /**
    * Inner class to implement the ActionListener interface for handling action
    * events on buttons.
    */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JButton button = (JButton)e.getSource();
         if(button == jButtonOk)
            onOk();
         else if(button == jButtonCancel)
            onCancel();
         else if(button == jButtonHelp)
            onHelp();
      }
   }

   /**
    * Create the command panel
    */
   private JPanel createCommandPanel()
   {
      JPanel commandPanel = new JPanel();
      commandPanel.setLayout(new BorderLayout());

      // create ok/cancel buttons
      jButtonOk = new UTFixedButton(getResources().getString("OK"));
           jButtonOk.setMnemonic((getResources().getString( "OK.mn" )).charAt(0));
      jButtonOk.setDefaultCapable(true);
      jButtonCancel = new UTFixedButton(getResources().getString("CANCEL"));
      jButtonCancel.setMnemonic(
                (getResources().getString( "CANCEL.mn" )).charAt(0));
      // create Help button
      jButtonHelp = new UTFixedButton(getResources().getString("HELP"));
           jButtonHelp.setMnemonic(
                 (getResources().getString( "HELP.mn" )).charAt(0));
           JPanel cmdPanel = new JPanel(new FlowLayout());
           cmdPanel.add(jButtonOk, 0);
           cmdPanel.add(jButtonCancel, 1);
           cmdPanel.add(jButtonHelp, 2);

      commandPanel.add(cmdPanel, BorderLayout.EAST);
      getRootPane().setDefaultButton(jButtonOk);
      return commandPanel;
   }

   /**
    * Handler for Add button clicked event. First the data in the parameter table and the Body of
    * the function is validated. On successful validation, adds the currently selected user
    * defined function in the combo box to the collection of IPSExtensionDef objects passed
    * in to the dialog constructor.
    *
    * If a user defined function with this name already exists in the collection, the user is prompted
    * if he wants to replace the existing entry. If the user clicks yes, then the entry is replaced.
    *
    * Note: originally, this method had an Add button rather than an OK button.
    * However, users were having problems with this concept given the layout
    * of this dialog, so it was removed.
    */
/*
   private boolean onAdd()
   {
      boolean status = addUdf();
      addFunctionNameToComboBox(strFnName); // will add only if name does not exist
      return status;
   }
*/
   /**
    * Worker method for the OK button action handler. Tries to add the new udf
    * to the list of new udfs. If succesful, the dialog is then closed, if not
    * a message was displayed to the user and the dialog remains visible.
   **/
   public void onOk()
   {
      if (addUdf())
         close();
   }

   /**
    * Worker method for the Cancel button action handler. Checks for unsaved data
    * and asks the user if they really want to exit if it finds any, otherwise
    * it just exits. The user can cancel the exit if there is unsaved data.
    */
   public void onCancel()
   {
      boolean bUnsavedData = false;
      /* do they have unsaved data? Get the name from the text field, see if
      it exists in the UDF vector. If it does, compare it against that
      entry. If there is no name, check the body field and params. If
      either have data, throw up the warning message. */
      String strFnName = m_functionName.getText();

      String body = ((String)jTextAreaBody.getText());
      String description = jTextAreaSummary.getText();

      if(strFnName != null && strFnName.trim().length() > 0 )
      {
         // see if it is an existing one
         IPSExtensionDef existingExit = getUdfExitFromAllExits(strFnName);
         IPSExtensionDef newExit = null;
         if ( null != existingExit && null != body && 0 != body.length() )
         {
            String strContext = null;
            if ( null != m_udfSet )
               strContext = m_udfSet.getExtensionContext();
            else
               strContext = existingExit.getRef().getContext();

            PSExtensionRef ref = new PSExtensionRef(
               getHandlerName(), strContext, strFnName );

            newExit = new PSExtensionDef( ref,
               PSIteratorUtils.iterator(
                  "com.percussion.extension.IPSUdfProcessor" ),
               PSIteratorUtils.emptyIterator(),
               getUdfParams(), m_paramPanel.getRuntimeParams());
         }
         if ( null != existingExit && !existingExit.equals( newExit ))
         bUnsavedData = true;
      }
      else if ( body.length() > 0 )
         bUnsavedData = true;
      else
      {
         //   any unsaved param defs?
         bUnsavedData = m_paramPanel.isParamTableEmpty();
      }

      if ( !bUnsavedData ||
           (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                                       getResources().getString( "LOSE_WORK" ),
                                       getResources().getString( "WARNING" ),
                                       JOptionPane.YES_NO_OPTION)))
      {
         close();
      }
   }


   /**
    * Called to create a new exit from the dialog contents and save it to the
    * udf collection. First, field validation is performed and appropriate
    * messages are displayed to the user. If validation is successful, the method
    * checks to see if the user is replacing an existing exit or creating a new
    * one. In either case, the data from the fields is copied into the exit
    * object and the object is stored in the new udfs collection.
    *
    * @returns <code>true</code if successful, <code>false</code> otherwise
   **/
   private boolean addUdf()
   {
      String strFnName = m_functionName.getText();
      String context = null;
      String category = null;

      if(m_cbContext != null && m_cbCategory != null)
      {
         category = (String)m_cbCategory.getSelectedItem();
         context = (String)m_cbContext.getSelectedItem();

         if(!validateContext(context))
            return false;
      }

      //Validate the function name
      if(!validateFnName(strFnName))
         return false;

      String strBody = ((String)jTextAreaBody.getText()).trim();
      if(strBody == null || strBody.equals(""))
      {
         JOptionPane.showMessageDialog(this, getResources().getString(
            "ENTER_BODY" ));
         return false;
      }

      m_paramPanel.stopCellEditing();
      boolean bValid = m_paramPanel.validateParamTableData();

      if(!bValid)
         return false;

      // now that validation has been done, save data into existing exit or
      //create a new one and add to collection
      IPSExtensionDef exit = null;

      /*get a vector of all UDFs with the same name as specified in the
      function name combo box, if there is any*/
      Vector exits = getUdfsWithTheSameName(strFnName, m_isEdit);

      //add a new udf
      if(!m_isEdit)
      {
         if(! canAddUdf(exits))
            return false;
      }

      else if(exits != null && exits.size() != 0)
      {
         if(m_def.getRef().getExtensionName().equals(strFnName))
         {
            if(!canAddUdf (exits))
               return false;
         }
      }

      /* if we are renaming the UDF, or editing the context*/
      if(m_isEdit && ((! m_def.getRef().getExtensionName().equals(strFnName))
         || (!m_def.getRef().getContext().equals(context))))
      {
         if(! m_def.getRef().getExtensionName().equals(strFnName))
         {
            //get all the udfs with the same name as the specifed
            Vector newUdf = getUdfsWithTheSameName(strFnName, false);
            //act like we are adding a new one
            if( ! canAddUdf(newUdf))
               return false;
         }
         //now delete Udf's ref , before recreating it.
         if(m_def != null)
            deleteUdf(m_def);
      }

      /*Now build the UDF*/
      String strContext = null;
      //Set a context for this UDF
      if(context != null)
        strContext = context;
      else
         strContext = PSUdfSet.GLOBAL_EXTENSION_CONTEXT;

      if ( null != m_udfSet && context == null)
         strContext = m_udfSet.getExtensionContext();

      PSExtensionRef ref = null;

      if(category != null && context != null)
         ref = new PSExtensionRef(category, getHandlerName(),strContext, strFnName );

      else
      ref = new PSExtensionRef(
         getHandlerName(), strContext, strFnName );

      exit = new PSExtensionDef( ref,
         PSIteratorUtils.iterator( "com.percussion.extension.IPSUdfProcessor" ),
            PSIteratorUtils.emptyIterator(),
            getUdfParams(), m_paramPanel.getRuntimeParams());
         m_vAllExits.add(exit);

      if (null != exit)
         m_newUdfs.add(exit);

      return true;
   }

   /**
    * Checks if the new UDF being added through the admin client has
    * the same name and category as an existing one. If a match is
    * found a warning message box pops-up.
    * @param exits, a vector of udfs that have the same name as the one being
    * specified, if none exist it is <CODE>null</CODE>
    * @return <CODE>true</CODE> if a new udf can be added meaning, the specified name
    * is the unique name, or although warned the user wants to save the new udf
    */
    private boolean canAddUdf(Vector exits)
    {
      boolean canBeAdded = true;

      String strFnName = m_functionName.getText();
      
      String udfName = null;
      if(exits == null)
      {
         IPSExtensionDef exit = getUdfExitFromAllExits(strFnName);
         if(exit != null)
            udfName = exit.getRef().getExtensionName();
         if(udfName != null && udfName.equals(strFnName))
            if ((JOptionPane.YES_OPTION !=   JOptionPane.showConfirmDialog(this,
               getResources().getString( "CONFIRM_REPLACE" ),
               getResources().getString( "WARNING" ),JOptionPane.YES_NO_OPTION)))
            return false; // user does not want to save data
         else
            return canBeAdded;
      }

      String category = null;
      if(m_cbCategory != null)
         category = (String)m_cbCategory.getSelectedItem();

      if(exits != null && exits.size() != 0)
      {
         boolean found = false;
         for(int i = 0; i < exits.size() && found == false; i++)
         {
            IPSExtensionDef exDef = (IPSExtensionDef)exits.get(i);
            String name = exDef.getRef().getExtensionName();
            String exCategory = exDef.getRef().getCategory();

            if(name.equals(strFnName) && category != null &&
               exCategory.equals(category))
            {
               found = true;
               /*Warn the user if the name and the category are the ones that already
               exist.  This will cause having multiple UDFs with the same
               names and the same categories*/
               if ((JOptionPane.YES_OPTION !=   JOptionPane.showConfirmDialog(this,
                  getResources().getString( "EXT_SAME" ),
                  getResources().getString( "WARNING" ),JOptionPane.YES_NO_OPTION)))
               return false;// user does not want to save data
            }
         }
      }
      return canBeAdded;
    }


   /**
    * Returns <CODE>true</CODE> if and only if the given
    * context is a well-formed context, if not it pops up a message box
    * telling the user what the restriction on the context are.
    * A well-formed context is defined as a sequence of valid context names
    * separated by forward slashes. When a udf is specified from the admin 
    * client a word application or application/ is a reserved word
    * and the context can not start with either of them.
    * @param context the context to check for validity. If
    * <CODE>null</CODE>, <CODE>false</CODE> will be returned.
    * @return <CODE>true</CODE> if the given context is
    * a well-formed context.
    */
   private boolean validateContext(String context)
   {
      boolean isContext = true;

      if(!PSExtensionRef.isValidContext(context) || (!context.endsWith("/")) ||
         (context.startsWith(PSUdfSet.APP_EXTENSION_CONTEXT)))
      {
         String message = getResources().getString("CONTEXT_ERR")+
            getResources().getString("FIRST_CHAR")+
            getResources().getString("CONTEXT_CHAR");

         JOptionPane.showMessageDialog(this,message,
            getResources().getString("INVALID_CONTEXT"),JOptionPane.ERROR_MESSAGE);
         isContext = false;
      }
      return isContext;
   }

   /**
    * Validates the specified function name.
    * If the name is not a valid function name, it pops-up a message box
    * describing what characters are to be used when creating a function name.
    * @param strFnName a function name to check for validity, can be <CODE>null</CODE>
    * @return <CODE>true</CODE> if the specified name is a valid
    * function name, otherwise <CODE>false</CODE>
    */
   private boolean validateFnName(String strFnName)
   {
      boolean isFnName = true;
      if(strFnName == null || (strFnName.trim()).equals(""))
      {
         JOptionPane.showMessageDialog(this, getResources().getString(
            "ENTER_FN_NAME" ));
         return false;
      }

      /* Check for a valid JavaScript character that a function name
       * starts with. Rhythmyx supports letters, '_', and '$'
       * For all valid JavaScript characters refer to ECMA script specification
       */
      if(!Character.isJavaIdentifierStart(strFnName.charAt(0)))
      {
         showError(strFnName);
         return false;
      }
      
      /* Check for a valid JavaScript subsequent characters
       * Rhythmyx supports letters, digits, '_', and '$'
       * For all valid JavaScript characters refer to ECMA script specification
       */
      for(int i=1;i<strFnName.length();i++)
      {
         if(!Character.isJavaIdentifierPart(strFnName.charAt(i)))
         {
            showError(strFnName);
            return false;
         }
      }
      return isFnName;
   }

   /**
    * Deletes the specified udf
    * @param udf an UDF to be deleted, it can be  <CODE>null</CODE>
    */
    private void deleteUdf(IPSExtensionDef def)
    {
      if(m_isEdit && m_objectStore != null && def != null)
      {
         try
         {
            m_objectStore.removeExtension(def.getRef());
         }
         catch(Exception e)
         {
            PSDlgUtil.showErrorDialog(e.getLocalizedMessage(),
               E2Designer.getResources().getString( "ServerErr" ));
         }
      }
    }

   /**
    * Extracts the UDF properties from the controls. Assumes body text is non-
    * <code>null</code> and not empty.
    *
    * @return A properties object that contains mandatory and optional params.
    * Never <code>null</code>.
    */
   private Properties getUdfParams()
   {
      Properties initParams = new Properties();
      initParams.setProperty( "scriptBody", jTextAreaBody.getText());
      String description = jTextAreaSummary.getText();
      if ( null != description )
         initParams.setProperty( IPSExtensionDef.INIT_PARAM_DESCRIPTION,
            description );
      String version = m_versionField.getText();
      if ( null != version && version.trim().length() > 0 )
         initParams.setProperty( "version", version.trim());
      initParams.setProperty( IPSExtensionDef.INIT_PARAM_REENTRANT, "yes" );
      return initParams;
   }

   /**
    * If the local copy of the handler name is <code>null</code>, the name is
    * retrieved from the ui.
    *
    * @return A non-empty string. Never <code>null</code>.
    */
   private String getHandlerName()
   {
      return null == m_handlerName ?
         (String) m_handlerNameField.getSelectedItem()
         : m_handlerName;
   }


   /**
    * Handler for Close button clicked event. Just close the dialog and free system resources.
    *
    */
   private void close()
   {
      setVisible(false); // hide the Frame
      dispose();        // free the resources
   }


   /**
    * @param def The extension def that we will get the list of param types
    * from. An <CODE>IllegalArgumentException</CODE> will be thrown if
    * <CODE>null</CODE>.
    */
   private String[] getParamTypes( IPSExtensionDef def )
   {
      if ( null == def )
         throw new IllegalArgumentException( "Parameter may not be null!" );

      Map mParamTypes = new HashMap(10);
      Iterator itParams = def.getRuntimeParameterNames();
      while( itParams.hasNext() )
      {
         String strParamName = (String)itParams.next();
         IPSExtensionParamDef param = def.getRuntimeParameter( strParamName );
         mParamTypes.put( param.getDataType(), null );
      }

      String[] retArray = new String[mParamTypes.size()];
      Iterator itTypes = mParamTypes.keySet().iterator();
      int i = 0;
      while ( itTypes.hasNext() )
      {
         retArray[i] = (String)itTypes.next();
         i++;
      }
      return retArray;
   }

   /**
    * Sets context and category comboboxes
    * @param def
    * @param context a vector of current UDFs context
    * @param categories a vector of current UDFs categories
    */
   private void setComboBoxes(IPSExtensionDef def, Vector context,
      Vector categories)
   {
      String defaultContext = "user/";
      String defaultCategory = "user";
      m_cbCategory = new UTFixedComboBox(categories, 120,20);
      m_cbContext = new UTFixedComboBox (context, 180,20);
      m_cbCategory.setEditable(true);
      m_cbContext.setEditable(true);

      if(null != def)
      {
         m_cbCategory.setSelectedItem(def.getRef().getCategory());
         m_cbContext.setSelectedItem(def.getRef().getContext());
      }
      //if setting a new UDF have defaults for category and context selected
      else
      {
         m_cbCategory.setSelectedItem(defaultCategory);
         m_cbContext.setSelectedItem(defaultContext);
      }
   }

   
   /**
    *
    * Inserts a function name to the list of entries in the combobox.
    * The name is inserted in an alphabetically sorted order.
    * @param def an UDF to be inserted, assumed not to be <code>null</code>
    */
   private void addFunctionNameToComboBox(UIExtensionDef def)
   {
      String strFnName = def.toString();
      //check to see if item already exists
      int iCount = m_copyCombo.getItemCount();

      // sort while adding
      Collator c = Collator.getInstance();
      c.setStrength(Collator.SECONDARY);     // for case insensitive comparison
      int i=0;
      if (iCount > 0)        //then we need to find the insert index
      {
         for ( i=0 ; i < iCount ; i++)
         {
            UIExtensionDef extDef =
               (UIExtensionDef)m_copyCombo.getItemAt(i);

            String defName = extDef.toString();
            if( c.compare( strFnName,defName) < 0 )
               break;
         }
      }
      if (i >= iCount)
         m_copyCombo.addItem(def);
      else
         m_copyCombo.insertItemAt(def, i);
   }

   /**
    * Initializes the dialog controls with the data.
    *
    * @param c The set of UDFs which we want to base our dialog.
    */
   private void initControls( PSUdfSet c )
   {
      List list = null;
      //from the admin client
      if(m_allExits != null)
         list = (List)m_allExits;

      //from the wb, only not deprecated
      else
         list = (List)m_vAllExits;
      for (int i = 0; i < list.size(); i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef)list.get(i);
         addFunctionNameToComboBox(new UIExtensionDef(exit));
      }

      m_copyCombo.setSelectedIndex(-1);

      /* catalog the handler names, if needed. If we can't get any, we can't
         continue */
      if ( null == m_handlerName )
      {
         // user must use an existing handler
         m_handlerNameField.setEditable( false );

         // catalog for ExtensionHandlers
         Vector defs = CatalogExtensionCatalogHandler.getCatalog(
            m_connectionSource.getDesignerConnection(false), true, false );
         Iterator iter = defs.iterator();
         while ( iter.hasNext())
            m_handlerNameField.addItem(
               ((PSExtensionRef) iter.next()).getExtensionName());

         if ( defs.size() == 0 )
         {
            String msg = getResources().getString( "HandlerNotFoundError" );
            JOptionPane.showMessageDialog( this, msg,
               getResources().getString("ExceptionDialogTitle"),
               JOptionPane.ERROR_MESSAGE );
            throw new IllegalStateException( msg );
         }
      }


      String[] typeList = null;
      boolean isTypeEditable = true;
      String strHandlerName = getHandlerName();
      if ( null != strHandlerName && strHandlerName.equals( JAVASCRIPT ) )
      {
         typeList = UTExtensionParamPanel.PARAM_TYPE_JAVASCRIPT;
         isTypeEditable = false;
      }
            
      m_paramPanel.updateParamsTable( null, typeList, isTypeEditable );
   }

   /**
    * Initializes the listeners.
    */
   private void initListeners()
   {
      ItemChangeListener icl = new ItemChangeListener();
      m_copyCombo.addItemListener(icl);

      ButtonListener bl = new ButtonListener();
      jButtonOk.addActionListener(bl);
      jButtonCancel.addActionListener(bl);
      jButtonHelp.addActionListener(bl);
   }

   /**
    * Inner class as a wrapper class for <code>IPSExtensionDef</code>. Will be
    * used when an UDF are inserted into combo box that holds UDFs.
    */
   private class UIExtensionDef
   {
      /**
       * A constructor that wraps  extension def
       * @param def an extension, can not to be <code>null</code>
       */
      public UIExtensionDef(IPSExtensionDef def)
      {
         if (def == null)
            throw new IllegalArgumentException("UDF may not be null!");

         m_udf = def;
      }

      /**
       * Needed to be overriden for UI purpose, so an extension can be inserted
       * into combo box and still get its full name to be displayed
       * @return a full UDF name, never <code>null</code>.
       */
      public String toString()
      {
         String context = m_udf.getRef().getContext();
         if (context.startsWith("application"))
            context = "application/";
         String defName = context + m_udf.getRef().getExtensionName();
            
         return defName;
      }

      /**
       * Gets the UDF.
       * @return an UDF, never <code>null</code>.
       */
      public IPSExtensionDef getExtension()
      {
         return m_udf;
      }

      /**
       * Gets the context of the extension.
       * @return context of the extension, can not be <code>null</code>
       */
      public String getContext()
      {
         return m_udf.getRef().getContext();
      }

      /**
       * An UDF, gets initialized in the constructor
       */
      private IPSExtensionDef m_udf = null;
   }


   /**
    * Inner class to implement ItemListener interface for handling item change
    * events for combo boxes.
    */
   class ItemChangeListener implements ItemListener
   {
      public void itemStateChanged( ItemEvent e )
      {
         UTFixedComboBox combo = (UTFixedComboBox)e.getSource( );
         if (combo == m_copyCombo)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
               if(e.getItem() instanceof UIExtensionDef)
                  onCopyComboItemChanged();
            }
         }
      }
   }

   /**
    * Handler for item selection change in the copy combo box.
    */
   private void onCopyComboItemChanged()
   {
      UIExtensionDef uiDef = (UIExtensionDef) m_copyCombo.getSelectedItem();
      IPSExtensionDef def = uiDef.getExtension();

      String[] typeList = null;
      boolean isTypeEditable = false;
      String strHandlerName = getHandlerName();
      if ( null != strHandlerName  )
      {
         if ( strHandlerName.equals( JAVASCRIPT ) )
         {
            typeList = UTExtensionParamPanel.PARAM_TYPE_JAVASCRIPT;
            isTypeEditable = false;
         }
         else
         {
            typeList = getParamTypes( def );
            isTypeEditable = true;
         }
      }

      m_paramPanel.updateParamsTable( def, typeList, isTypeEditable );
      updateBody( def );
      updateVersion( def );
      updateSummary( def );

      m_copyCombo.setToolTipText(def.getRef().getContext() +
         def.getRef().getExtensionName());
   }


   /**
    * Updates the function body with the body from the passed in IPSExtensionDef
    * object.
    */
   private void updateBody(IPSExtensionDef udfExit)
   {
      if(udfExit == null)
         return;

      if ( udfExit instanceof IPSExtensionDef )
      {
         jTextAreaBody.setText(((IPSExtensionDef)udfExit).getInitParameter(
                                                                "scriptBody" ));
      }
   }

   /**
    *
    * Updates the function description/summary with the text from the passed in
    * IPSExtensionDef object.
    */
   private void updateSummary(IPSExtensionDef udfExit)
   {
      if(udfExit == null)
         return;

      jTextAreaSummary.setText( udfExit.getInitParameter(
         IPSExtensionDef.INIT_PARAM_DESCRIPTION ));

   }

   /**
    *
    * Updates the version with the version text from the passed in
    * IPSExtensionDef object.
    */
   private void updateVersion(IPSExtensionDef udfExit)
   {
      if(udfExit == null)
         return;

      m_versionField.setText( udfExit.getInitParameter( "version" ));
   }


   /**
    * Returns the IPSExtensionDef object that has the passed in function name in
    * the vector of IPSExtensionDef objects m_vAllExits. Returns null if the
    * name does not exist.
    */
   private IPSExtensionDef getUdfExitFromAllExits(String strFnName)
   {
      if(strFnName == null)
         return null;
      if(m_vAllExits.size()<= 0)
         return null;
      for(int i=0; i<m_vAllExits.size(); i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef)m_vAllExits.get(i);
         if( exit != null )
         {
              String strUdfName = exit.getRef().getExtensionName();

              if(strUdfName.equals(strFnName))
              {
               return exit;
            }
         }
      }
      return null;
   }

     /**
    * Returns the vector of IPSExtensionDef objects that have the passed in
    * function name in the vector of IPSExtensionDef objects m_allExits.
    * Returns null if there are not udfs with that name.
    * @param strName a name to search for, it can be <CODE>null<CODE>
    * @param isEdit <CODE>true</CODE> if the udf is being edited, otherwise
    * <CODE>false</CODE>
    * @return a vector of the udfs with the same name as the specified udf name
    * if any, if not <CODE>null</CODE>
    */
   private Vector getUdfsWithTheSameName(String strName, boolean isEdit)
   {
      if(strName == null)
         return null;

      if(m_allExits == null)
         return null;

      Vector<IPSExtensionDef> exits = new Vector<IPSExtensionDef>();

      for(int i=0; i<m_allExits.size(); i++)
      {

         IPSExtensionDef exit = (IPSExtensionDef)m_allExits.get(i);
         if( exit != null )
         {
              String strUdfName = exit.getRef().getExtensionName();
              if(strUdfName.equals(strName))
            {
               if(!isEdit)
                    exits.add(exit);
               else
               {
                  //do not inculde udf that is being edited
                  if(!m_def.getRef().equals(exit) &&
                     (!m_def.getRef().getCategory().equals(exit.getRef().
                     getCategory())))
                        exits.add(exit);
               }
            }
         }
      }
      return exits;
   }

   /**
    * Displays an error message box with a detailed message
    * @param strName if gets here not an empty string nor <CODE>null</CODE>
    */
   private void showError(String strName)
   {
     JOptionPane.showMessageDialog(this,"'"+strName+ "'"+" "+
      getResources().getString( "NOT_VALID_NAME" )+"\n"+
      getResources().getString("FIRST_CHAR")+"\n"+
      getResources().getString("SUBSEQUENT_CHARS"),
      getResources().getString("ERROR_FN"),JOptionPane.ERROR_MESSAGE);
   }

   /**
    * Temporary for testing.
    */
   /*
   static public PSCollection getDummyCollectionForTest()
   {
      PSCollection coll = null;
      PSCollection collExitParamDef = null;

      try
      {
         coll = new PSCollection("com.percussion.design.objectstore.IPSExtensionDef");
         IPSExtensionDef ex = new IPSExtensionDef("com.percussion.extension.PSJavaScriptUdfExitHandler", "test", "Body of Javascript function");
         collExitParamDef = new PSCollection("com.percussion.design.objectstore.PSExtensionParamDef");
         PSExtensionParamDef param = new PSExtensionParamDef("Param1", "var");
         param.setDescription("temporary for test");
         collExitParamDef.add(param);
         ex.setParamDefs(collExitParamDef);
         coll.add(ex );
         ex= new IPSExtensionDef("com.percussion.extension.PSJavaScriptUdfExitHandler", "fn_2", "descr 2");
         coll.add(ex);
         collExitParamDef = new PSCollection("com.percussion.design.objectstore.PSExtensionParamDef");
         param = new PSExtensionParamDef("strName", "String");
         param.setDescription("string for test");
         collExitParamDef.add(param);
         ex.setParamDefs(collExitParamDef);

      }
      catch(Exception clEx)
      {
         clEx.printStackTrace();
      }
      return coll;
   }
   */

   /**
    * For testing.
    */
   /*
   static public void main(String args[])
   {
   final JFrame frame = new JFrame("Test CreateUdfDialog");
      frame.addWindowListener(new BasicWindowMonitor());

      try
   {
     String strLnFClass = UIManager.getSystemLookAndFeelClassName();
         LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
            CreateUdfDialog dialog = new CreateUdfDialog(frame, getDummyCollectionForTest());
         dialog.setLocationRelativeTo(frame);
               dialog.setVisible(true);
            }
         });

         frame.setSize(640, 480);
         frame.setVisible(true);

    }
    catch (Exception e)
    { System.out.println(e); }

   }
   */


   /**
    * A combo box that contains current categories initialize/modified in
    * setComboBoxes method called only when adding/modifing UDFs from the admin
    */
   private UTFixedComboBox m_cbCategory = null;
   /**
    * A combo box that contains current context initialize/modified in
    * setComboBoxes method called only when adding/modifing UDFs from the admin
    */
   private UTFixedComboBox m_cbContext = null;

   /**
    * Label for m_cbContext combo box initialized in createControls method
    */
   private JLabel m_contextLabel = null;

   /**
    * Label for m_cbCategory combo box initialized in createControls method
    */
   private JLabel m_categoryLabel = null;

   /**
    * The collection of all exits that the user can access. It contains all the
    * udfs in a list. If a duplicate udf of the same name exists (this can only
    * happen from different types of udfs), a hierarchy scheme is used to
    * determine which one is kept. (1 being the highest priority)
    * <OL>
    * <LI>Application Udfs</LI>
    * <LI>Global (server) Udfs</LI>
    * <LI>Predefined Udfs</LI>
    * </OL>
    */
   private PSCollection m_vAllExits = null;

  /**
   * The Set of different referenced UDFs (Predefined, Application).
   */
  private PSUdfSet m_udfSet = null;

  /**
   * Storage for newly created or modified udfs. This will be used in
   * conjunction with the PSUdfSet for new udf notification. These new udfs will
   * be used by the owner of this dialog to add to the PSUdfSet.
   */
  private PSCollection m_newUdfs = null;

   /**
    * Either <code>null</code> or a valid handler name. If a handler name is
    * passed to the ctor, it is saved here. If not, a combo box is shown in
    * the ui listing all of the scriptable handlers. Do not access directly.
    * Call <code>getHandlerName</code>, which will use this or the edit
    * control, as appropriate.
    */
   private String m_handlerName = null;

   /**
    * The edit control for the handler name. Only visible if the caller
    * doesn't supply a handler in the ctor.
    */
   private UTFixedComboBox m_handlerNameField = new UTFixedComboBox(180,20);

   /**
    * The edit control for UDFs.  
    */
    private UTFixedComboBox m_copyCombo = new UTFixedComboBox(212,20);

   /**
    * A source for a PSDesignerConnection, used for cataloging. If <code>null
    * </code>, cataloging is disabled.
    */
   private IConnectionSource m_connectionSource = null;

   /**
    * A boolean to indicate whether or not the Create Udf Dialog is in edit mode,
    * Another possible mode is adding mode.
    * Gets set to <CODE>true</CODE> in the constructor called from the admin client
    * if an UDF is being edited
    */
   private boolean m_isEdit = false;

   /**
    * An extension being edited.  Gets initialized in the constructor 
    */
   private IPSExtensionDef m_def = null;

   /**
    * An object store.  If an UDF's context is being edited that UDF has to be
    * deleted and a new one constructed.  Gets initialized in the constructor
    */
   private PSObjectStore m_objectStore = null;

   /**
    * A vector of all JavaScript extensions.  Gets initialized in the constructor
    */
   private Vector m_allExits = null;

   //{{DECLARE_CONTROLS
   private JButton jButtonOk;
   private JButton jButtonCancel;
   private JButton jButtonHelp;
   private JLabel jLabelName;
   private UTFixedTextField m_functionName;
   private JLabel jLabelParams;
   private JTextArea jTextAreaBody;
   private JLabel jLabelBody;
   private JLabel jLabelVersion;
   private UTFixedTextField m_versionField;
   private JLabel jLabelSummary;
   private JTextArea jTextAreaSummary;
   private UTExtensionParamPanel m_paramPanel;
   //}}

   private static final String JAVASCRIPT = "JavaScript";
}
