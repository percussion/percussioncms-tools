/******************************************************************************
 *
 * [ PSIDTypesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The dialog used to edit id types with a single panel. Should be used when 
 * editing id types for a server or when editing id types for a single 
 * deployable element (package) with its dependencies or a single dependency.
 */
public class PSIDTypesDialog extends PSDialog
{
   /**
    * Constructs this object and calls <code>initDialog</code> to initialize the
    * dialog framework. The caller has to call <code>setVisible</code> to make 
    * this dialog visible.
    * 
    * @param parent the parent dialog of this dialog, may be <code>null</code>
    * @param server the deployment server on which the application id
    * types need to be saved, may not be <code>null</code> and must be connected.
    * @param identifier the identifier for which the id types are being edited,
    * may not be <code>null</code> or empty, used in title of the dialog.
    * @param idTypesList The list of dependency id types, may not be <code>null
    * </code> or empty.
    * @param validate if <code>true</code> validates that user identified type
    * for all literal ids that are not identified, otherwise not.
    * @param incompleteOnly if <code>true</code> displays only incomplete 
    * id type mappings, otherwise all.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if an error happens cataloging types.
    */
   public PSIDTypesDialog(Dialog parent, PSDeploymentServer server, 
      String identifier, Iterator idTypesList, boolean validate, 
      boolean incompleteOnly) throws PSDeployException
   {
      super(parent);
      validateAndInitializeDialog(server, identifier, idTypesList, validate,
         incompleteOnly);
   }
   
   /**
    * Constructs this object and calls <code>initDialog</code> to initialize the
    * dialog framework. The caller has to call <code>setVisible</code> to make 
    * this dialog visible.
    * 
    * @param parent the parent dialog of this dialog, may be <code>null</code>
    * @param server the deployment server on which the application id
    * types need to be saved, may not be <code>null</code> and must be connected.
    * @param identifier the identifier for which the id types are being edited,
    * may not be <code>null</code> or empty, used in title of the dialog.
    * @param idTypesList The list of dependency id types, may not be <code>null
    * </code> or empty.
    * @param validate if <code>true</code> validates that user identified type
    * for all literal ids that are not identified in <code>onOk()</code>, 
    * otherwise not.
    * @param incompleteOnly if <code>true</code> displays only incomplete 
    * id type mappings, otherwise all.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if an error happens cataloging types.
    */
   public PSIDTypesDialog(Frame parent, PSDeploymentServer server, 
      String identifier, Iterator idTypesList, boolean validate, 
      boolean incompleteOnly) throws PSDeployException
   {
      super(parent);
      validateAndInitializeDialog(server, identifier, idTypesList, validate,
         incompleteOnly);
   }
   
   
   /**
    * Worker method for constructing and initializing the dialog framework. 
    * Validates that all parameters are valid to construct the dialog.
    * 
    * @param server the deployment server on which the application id
    * types need to be saved, may not be <code>null</code> and must be connected.
    * @param identifier the identifier for which the id types are being edited,
    * may not be <code>null</code> or empty, used in title of the dialog.
    * @param idTypesList The list of dependency id types, may not be <code>null
    * </code> or empty.
    * @param validate if <code>true</code> validates that user identified type
    * for all literal ids that are not identified, otherwise not.
    * @param incompleteOnly if <code>true</code> displays only incomplete 
    * id type mappings, otherwise all.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if an error happens cataloging types.
    */
   private void validateAndInitializeDialog(PSDeploymentServer server, 
      String identifier, Iterator idTypesList, boolean validate, 
      boolean incompleteOnly) throws PSDeployException
   {
      if(server == null)
         throw new IllegalArgumentException("server may not be null.");
      
      if(!server.isConnected())
         throw new IllegalArgumentException("server must be connected.");      
   
      if(idTypesList == null || !idTypesList.hasNext())
         throw new IllegalArgumentException("idTypes may not be null or empty");
           
      if(identifier == null || identifier.trim().length() == 0)
         throw new IllegalArgumentException(
            "identifier may not be null or empty");
         
      m_server = server;
      m_idTypes = new ArrayList();
      while(idTypesList.hasNext())
         m_idTypes.add(idTypesList.next());
      m_isValidate = validate;
      m_isIncompleteOnly = incompleteOnly;
      initDialog(identifier);
   }   

   /**
    * Initializes the dialog.
    *
    * @param identifier The title to use, assumed not <code>null</code> or 
    * empty. 
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void initDialog(String identifier) throws PSDeployException
   {
      JPanel panel = new JPanel(new BorderLayout(10, 20));
      getContentPane().add(panel);
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle( MessageFormat.format(getResourceString("title"), 
         new String[]{identifier}) );
      
      JPanel descPanel = new JPanel();
      descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
      int steps = 5;
      try 
      {
         steps = Integer.parseInt(getResourceString("descStepCount"));
      }
      catch (NumberFormatException ex) 
      {
         //uses the default  
      }
      
      for (int i = 1; i <= steps; i++) 
      {
         descPanel.add(new JLabel(getResourceString("descStep" + i), 
            SwingConstants.LEFT));
      }
      panel.add(descPanel, BorderLayout.NORTH);
      
      m_idTypesPanel = new PSIDTypesPanel(m_idTypes.iterator(), 
         m_server.getLiteralIDTypes(), m_server, m_isIncompleteOnly);
      panel.add(m_idTypesPanel, BorderLayout.CENTER);         

      JButton guessButton = new UTFixedButton(getResourceString("guessAll"));
      guessButton.setMnemonic(getResourceString("guessAll.mn").charAt(0));
      guessButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            onGuessAll();            
         }});
      
      JPanel commandPanel = new JPanel(new BorderLayout());      
      JPanel guessPanel  = new JPanel(new BorderLayout());
      guessPanel.setBorder(new EmptyBorder(0,0,5,5));
      JPanel cmdPanel  = new JPanel(new BorderLayout());
      
      guessPanel.add(guessButton, BorderLayout.SOUTH);
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true), 
                   BorderLayout.EAST);
      commandPanel.add(guessPanel, BorderLayout.WEST);
      commandPanel.add(cmdPanel, BorderLayout.EAST);

      panel.add(commandPanel, BorderLayout.SOUTH);
         
      pack();      
      center();    
      setResizable(true);
   }

   /**
    * Attempts to guess all undefined ID types 
    */
   protected void onGuessAll()
   {
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         PSIDTypesHandler handler = new PSIDTypesHandler(m_server);
         handler.guessIdTypes(m_idTypes.iterator());
         m_idTypesPanel.mappingsChanged();
      }
      catch (PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
            getResourceString("error"));
      }
      finally
      {
         setCursor(Cursor.getDefaultCursor());
      }
   }

   /**
    * Validates that all literal ids identified if dialog is constructed in 
    * validate mode. Saves the ID Types and disposes the dialog.
    */
   public void onOk()
   {
      m_idTypesPanel.stopEditing();
      if(m_isValidate)
      {
         if(!m_idTypesPanel.validateMappings())
         {
            ErrorDialogs.showErrorMessage(this, 
               getResourceString("unIdentifiedIds"), 
               getResourceString("saveError"));
            return;
         }
      }
      try {
         m_server.getDeploymentManager().saveIdTypes(m_idTypes.iterator());
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(), 
            getResourceString("saveError"));
         return;
      }
      
      super.onOk();
   }

   /**
    * The deployment server on which the id types need to be saved, initialized 
    * in the constructor and never <code>null</code> or modified after that.
    */
   private PSDeploymentServer m_server;
   
   /**
    * The list of <code>PSApplicationIDTypes</code> that need to be identified,
    * initialized in the constructor and the list is never <code>null</code> and
    * modified after that. The type will be set as user sets the type. 
    */
   private List m_idTypes;
   
   /**
    * The flag to indicate whether to validate that all id types are identified
    * or not when user exits the dialog by clicking OK. <code>true</code> 
    * signifies validation and vice versa. Initialized in the constructor and
    * never modified after that.
    */
   private boolean m_isValidate;
   
   /**
    * The flag to indicate whether to show only mappings with undefined type or
    * to show all, <code>true</code> defines to show only incomplete mappings 
    * and vice versa. Initialized in the constructor and never modified after 
    * that.
    */
   private boolean m_isIncompleteOnly;
   
   /**
    * The panel to provide ui to edit id types, initialized in <code>
    * initDialog()</code> and never <code>null</code> or modified after that.
    */
   private PSIDTypesPanel m_idTypesPanel;
}
