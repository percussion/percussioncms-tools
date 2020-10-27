/*[ NewSecurityProviderTypeDialog.java ]***************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.PSComboBox;
import com.percussion.E2Designer.PSDialog;
import com.percussion.E2Designer.SecurityProviderMetaData;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;



/**
 * Class to create a new PSSecurityProviderInstance object.
 *
 */
public class NewSecurityProviderTypeDialog extends PSDialog
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor to create a new PSSecurityProviderInstance object.
    *
    * @param parent The parent frame (can be null)
    * @param existingProviders List of existing security provider instance
    *    names, may not be <code>null</code> can be empty.
    * @param groupProviders List of <code>IPSGroupProviderInstance</code>
    *    objects, may not be <code>null</code> can be empty.
    * @param config the server configuration, not <code>null</code>.
    */
   public NewSecurityProviderTypeDialog(JFrame parent, List existingProviders, 
      Collection groupProviders, PSServerConfiguration config)
   {
      super(parent, sm_res.getString("titleNewSec"));

      if(existingProviders == null)
         throw new IllegalArgumentException(
            "existingProviders can not be null");

      if(groupProviders == null)
         throw new IllegalArgumentException(
            "groupProviders can not be null");
            
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");

      m_existingProviders = existingProviders;
      m_config = config;
      initDialog();
   }

   private void initDialog()
   {
      getContentPane().setLayout(null);
      setSize(350,150);
      center();

      createControls();
      initControls();
      initListeners();
   }

   /**
    *   Internal for creating the controls.
    *   
    */
   private void createControls()
   {
      m_labelSelect = new JLabel(sm_res.getString("selectType"));
      m_labelSelect.setBounds(17,21,204,24);
      getContentPane().add(m_labelSelect);

      m_comboProvider = new PSComboBox();
      m_comboProvider.setBounds(15,54,260,20);
      getContentPane().add(m_comboProvider);

      m_buttonNext = new JButton();
      m_buttonNext.setText(sm_res.getString("ok"));

                m_buttonNext.setMnemonic((sm_res.getString("ok.mn")).charAt(0));
                m_buttonNext.setSize(new Dimension(80,24));
      m_buttonNext.setBounds(100,85,80,24);
      getContentPane().add(m_buttonNext);

      m_buttonCancel = new JButton();
      m_buttonCancel.setText(sm_res.getString("cancel"));
                m_buttonCancel.setMnemonic((sm_res.getString("cancel.mn")).charAt(0));
                m_buttonCancel.setSize(new Dimension(80,24));
      m_buttonCancel.setBounds(195,85,80,24);
      getContentPane().add(m_buttonCancel);
      
   }

   /**
    *   Internal for initializing the fields of the dialog. 
    *   
    */
   private void initControls()
   {
      // add the types supported to the combo box
      String[] providers = SecurityProviderMetaData.getInstance().
         getSecurityProvidersByDisplayName(true);
         
      for (int i=0; i<providers.length; ++i)
         m_comboProvider.addItem(providers[i]);
   }

   /**
    *   Initialize the listeners.
    */
   private void initListeners()
   {
      m_buttonNext.addActionListener(new ButtonListener());
      m_buttonCancel.addActionListener(new ButtonListener());
   }


   /** Inner class to implement the ActionListener interface for handling action events
    * on buttons.
   */
   class ButtonListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e )
      {
         JButton button = (JButton)e.getSource( );
         if(button == m_buttonNext)
            onNext( );
         else if(button == m_buttonCancel)
            onCancel( );
      }
   }


   /**
    *   Handler for Next >> button clicked event.
    *
    */
   private void onNext()
   {
      setVisible( false );
      String strProvider = (String)m_comboProvider.getSelectedItem();

      ISecurityProviderEditor editor =
         SecurityProviderEditorFactory.getSecurityProviderEditor(
            SecurityProviderMetaData.getInstance().getIdForDisplayName(
            strProvider), (JFrame) getParent(), m_config);
      editor.setInstanceNames( m_existingProviders );

      editor.setVisible( true );
      m_bInstanceCreated = editor.isInstanceModified();
      if ( m_bInstanceCreated )
         m_inst = editor.getInstance();

      editor.dispose();
   }

   /**
    *   Handler for Cancel button clicked event.
    *
    */
   public void onCancel()
   {
      m_bInstanceCreated = false;
      setVisible(false);
   }

   /**
    *@return <code>true</code> if a new instance was created. 
    *   
    */
   public boolean isInstanceCreated()
   {
      return m_bInstanceCreated;
   }

   /**
    *Returns the newly created instance of the security provider
    *@return the new instance that was created 
    *   
    */
   public PSSecurityProviderInstance getProviderInstance()
   {
      return m_inst;
   }


   JLabel         m_labelSelect;
   PSComboBox   m_comboProvider;
   JButton         m_buttonNext;
   JButton         m_buttonCancel;


   private static ResourceBundle sm_res = PSServerAdminApplet.getResources();
   private boolean m_bInstanceCreated = false;

   private PSSecurityProviderInstance m_inst = null;

   /**
    * The list of exisitng security provider instance names, used to check for
    * duplicate names while creating new instance, initialized in constructor
    * and never <code>null</code> or modified after that. May be empty.
    */
   private List m_existingProviders = null;

   /**
    * The server configuration, initialized while constructed, never 
    * <code>null</code> after that.
    */
   private PSServerConfiguration m_config = null;
}
