/*[ ODBCSecurityProviderDialog.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.ListMemberConstraint;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.StringConstraint;
import com.percussion.E2Designer.StringLengthConstraint;
import com.percussion.E2Designer.ValidationConstraint;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSOdbcProvider;
import com.percussion.security.PSSecurityProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * Class to enter the properties for a PSSecurityProviderInstance object that has a type
 * PSSecurityProvider.SP_TYPE_ODBC.
 *
 */
public class ODBCSecurityProviderDialog extends PSDialog
   implements ISecurityProviderEditor
{
   /**
    * Constructor
    *
    *@param parent The owner of this dialog. Should not be null.
    *
    *@throws IllegalArgumentException if inst is <code>null</code> or if
    * c is <code>null</code>
    */
   public ODBCSecurityProviderDialog(   JFrame parent )
   {
      super( parent, sm_res.getString("titleODBC"));

      initDialog();
   }



/* ################ ISecurityProviderEditor interface ################## */

   /**
    * This method can be called after the editor returns to determine if the
    * end user cancelled the editing session or changed the supplied instance,
    * if editing.
    *
    * @return <code>true</code> if the caller had set an instance and the end
    * user modified it or a new instance was created, <code>false</code> otherwise.
    */
   public boolean isInstanceModified()
   {
      return m_bModified;
   }

   /**
    * The editor can function in edit or create mode. This method should be
    * called before the editor is shown the first time to set it into edit mode.
    * When in edit mode, the properties of the supplied instance will be displayed
    * to the user for editing. It can also be used to reset the dialog to its
    * initial state (except for instance names).
    *
    * @param providerInst The provider to edit. If null, a new instance is created,
    * any existing instance is lost. If you want to keep the existing instance,
    * retrieve it before calling this method. This allows the dialog to be used
    * multiple times w/o creating a new one. If called while the dialog is
    * visible, the call is ignored.
    *
    * @return <code>true</code> if the provided instance is used, <code>false
    * </code> otherwise.
    *
    * @throws IllegalArgumentException if providerInst is not the type supported
    * by the editor.
    */
   public boolean setInstance( PSSecurityProviderInstance providerInst )
   {
      if ( isVisible())
         return false;

      if ( null != providerInst &&
         providerInst.getType() != PSSecurityProvider.SP_TYPE_ODBC )
      {
         throw new IllegalArgumentException( "provider type not supported by this dialog" );
      }

      reset();

      m_inst = providerInst;
      if ( null != providerInst )
      {
         m_tfProviderName.setText( providerInst.getName());
         Properties props = providerInst.getProperties();
         String dsn = props.getProperty( PSOdbcProvider.PROPS_SERVER_NAME );
         if ( null != dsn && dsn.trim().length() > 0 )
            m_tfServer.setText( dsn.trim());
      }

      // If there is an instance set, remove its name from the disallowed list
      if ( null != m_existingProviders )
         m_existingProviders.remove( m_inst.getName());

      m_validationInited = false;
      return true;
   }


   /**
    * After editing has completed, the caller can use this method to obtain the
    * newly created or modified provider. If a provider was previously successfully
    * set with the <code>setInstance</code> method, that instance is returned.
    *
    * @return If in edit mode, the edited instance is returned, otherwise a
    * new instance is returned. If the user cancels, null is returned.
    */
   public PSSecurityProviderInstance getInstance()
   {
      return m_inst;
   }

   /**
    * Sets a list of existing security provider names. The editor will not allow the
    * end user to create a new security provider with any name that matches a
    * name on the supplied list, case insensitive. If null, the current list
    * will be discarded. All entries in the list must be non-null, String
    * objects or an exception will be thrown.
    *
    * @param names A list of existing security providers, used to prevent
    * duplicate provider names.
    *
    * @throws IllegalArgumentException If any entry in names is null or is not
    * a String object.
    */
   public void setInstanceNames( Collection names )
   {
      if ( null == names || names.size() == 0 )
      {
         m_existingProviders = null;
         return;
      }

      int size = names.size();
      Iterator iter = names.iterator();
      while ( iter.hasNext())
      {
         Object o = iter.next();
         if ( null == o || !( o instanceof String ))
            throw new IllegalArgumentException(
               "Invalid entry in instance name list" );
      }
      m_existingProviders = names;

      // If there is an instance set, remove its name from the disallowed list
      if ( null != m_inst )
         m_existingProviders.remove( m_inst.getName());

      // we need to force the validation framework to be rebuilt
      m_validationInited = false;
   }


/* ################### Implementation methods ##################### */

   /**
    *   Internal for creating the dialog.
    *
    */
   private void initDialog()
   {
      getContentPane().setLayout(null);
      setSize(389,200);
      center();

      createControls();
      initListeners();
   }
   
   /**
    *   Internal for creating the controls.
    *   
    */
   private void createControls()
   {
      JLabel labelProviderName = new JLabel(sm_res.getString("secProvName"));
      labelProviderName.setBounds(12,23,147,19);
      labelProviderName.setHorizontalAlignment(SwingConstants.RIGHT);
      getContentPane().add(labelProviderName);

      JLabel labelServer = new JLabel(sm_res.getString("serverName"));
      labelServer.setBounds(12,53,147,19);
      labelServer.setHorizontalAlignment(SwingConstants.RIGHT);
      getContentPane().add(labelServer);


      m_tfProviderName = new JTextField();
      m_tfProviderName.setBounds(169,23,193,21);
      getContentPane().add(m_tfProviderName);

      m_tfServer = new JTextField();
      m_tfServer.setBounds(169,53,193,21);
      getContentPane().add(m_tfServer);


      m_buttonOK = new JButton();
      m_buttonOK.setText(sm_res.getString("ok"));
                m_buttonOK.setMnemonic(sm_res.getString("ok.mn").charAt(0));
      m_buttonOK.setBounds(169,130,80,24);
      getContentPane().add(m_buttonOK);

      m_buttonCancel = new JButton();
      m_buttonCancel.setText(sm_res.getString("cancel"));
      m_buttonCancel.setBounds(280,130,80,24);
           m_buttonOK.setMnemonic(sm_res.getString("cancel.mn").charAt(0));
      getContentPane().add(m_buttonCancel);
   }


   /**
    * Clears all editors back to their state when the dialog was first created.
    */
   private void reset()
   {
      m_tfProviderName.setText( null );
      m_tfServer.setText( null );
   }



   /**
    *   Initialize the listeners.
    *   
    */
   private void initListeners()
   {
      m_buttonOK.addActionListener(new ButtonListener());
      m_buttonCancel.addActionListener(new ButtonListener());      
   }


   /**
    *   Handler for OK button clicked event. 
    *   
    */
   private void onOK()
   {
      initValidationFramework();

      if ( !activateValidation())
         return;

      String strProvName = m_tfProviderName.getText().trim();

      Properties props = new Properties();
      props.put( PSOdbcProvider.PROPS_SERVER_NAME, m_tfServer.getText().trim());
      try
      {
         if ( null != m_inst )
         {
            if ( !m_inst.getName().equalsIgnoreCase( strProvName ))
               m_inst.setName(strProvName);
         }
         else
         {
            m_inst = new PSSecurityProviderInstance( strProvName,
               PSSecurityProvider.SP_TYPE_ODBC );
         }
         m_inst.setProperties( props );
      }
      catch(PSIllegalArgumentException e)
      {
         // this shouldn't happen since we validated the name
         e.printStackTrace();
      }

      m_bModified = true;
      setVisible(false);

   }

   /**
    *   Handler for Cancel button clicked event. 
    *   
    */
   public void onCancel()
   {
      m_bModified = false;
      setVisible(false);
   }


   /**
    * Creates the validation framework and sets it in the parent dialog. After
    * setting it, the m_validationInited flag is set to indicate it doesn't
    * need to be done again. If something changes requiring a new framework,
    * just clear this flag and the next time onOk is called, the framework
    * will be recreated.
    * <p>By using the flag, this method can be called multiple times w/o a
    * performance penalty.
    *
    * @throws IllegalArgumentException if the component and constraint arrays
    * don't match. This can only happen if they are both not updated equally
    * (i.e. an implementation flaw).
    */
   private void initValidationFramework()
   {
      if ( m_validationInited )
         return;

      // set up the validation framework
      ArrayList comps = new ArrayList(10);
      ArrayList constraints = new ArrayList(10);
      StringConstraint nonEmpty = new StringConstraint();

      // validate provider name
      comps.add( m_tfProviderName );
      constraints.add( nonEmpty );
      comps.add( m_tfProviderName );
      constraints.add(
         new StringLengthConstraint( PSSecurityProviderInstance.MAX_NAME_LEN ));
      if ( null != m_existingProviders )
      {
         comps.add( m_tfProviderName );
         constraints.add( new ListMemberConstraint( m_existingProviders ));
      }

      // validate DSN
      comps.add( m_tfServer );
      constraints.add( nonEmpty );

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


   /** Inner class to implement the ActionListener interface for handling action events
    * on buttons.
   */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JButton button = (JButton)e.getSource( );
         if(button == m_buttonOK)
            onOK( );
         else if(button == m_buttonCancel)
            onCancel( );
      }
   }


   JTextField m_tfProviderName;
   JTextField m_tfServer;
   JButton m_buttonOK;
   JButton m_buttonCancel;

   private static ResourceBundle sm_res = PSServerAdminApplet.getResources();

   private PSSecurityProviderInstance m_inst = null;
   private boolean m_bModified = false;
   private Collection m_existingProviders = null;

   /**
    * A flag that indicates whether the validation framework needs to be
    * created before activating validation. We use the flag so we will only
    * build the framework if the user presses Ok.
    */
   private boolean m_validationInited = false;


}
