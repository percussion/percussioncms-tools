/*[ PSConnectionEditorPanel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.PSPasswordField;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderRemoteAgent;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Element;


/**
 * Loader editor panel for specifying all the properties for the content loading
 * process into Rhythmyx. It contains text fields for entering server name,
 * port, user name and password.
 */
public class PSConnectionEditorPanel extends PSConfigPanel
{
   /**
    * Creates the loader editor panel.
    */
   public PSConnectionEditorPanel()
   {
      init();
   }

   /**
    * Initializes the panel.
    */
   protected void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(0, 10, 10, 5);
      setBorder(emptyBorder);

      PSPropertyPanel bodyPanel = new PSPropertyPanel();
      bodyPanel.setAlignmentX(RIGHT_ALIGNMENT);
      m_server = new UTFixedHeightTextField();
      m_port = new UTFixedHeightTextField();
      m_user = new UTFixedHeightTextField();
      m_pwd = new PSPasswordField();

      JComponent[] j1 = {m_server};
      bodyPanel.addPropertyRow(PSContentLoaderResources.getResourceString(
         ms_res, "textfield.label.server"), j1);

      JComponent[] j2 = {m_port};
      bodyPanel.addPropertyRow(PSContentLoaderResources.getResourceString(
         ms_res, "textfield.label.port"), j2);

      JComponent[] j3 = {m_user};
      bodyPanel.addPropertyRow(PSContentLoaderResources.getResourceString(
         ms_res, "textfield.label.user"), j3);

      JComponent[] j4 = {m_pwd};
      bodyPanel.addPropertyRow(PSContentLoaderResources.getResourceString(
         ms_res, "textfield.label.pwd"), j4);
      add(bodyPanel, BorderLayout.NORTH);
   }

  /**
   * Loads the data for this panel. Error dialog is shown if there is a problem
   * in loading the data.
   *
   * @param configXml, data for this panel, never <code>null</code>.
   *
   * @throws IllegalArgumentException if the supplied argument is not valid.
    */
   public void load(Element configXml)
   {
      if (configXml == null)
         throw new IllegalArgumentException(
            "xml element cannot tbe null");
      try
      {
         m_conDef = new PSConnectionDef(configXml);
         load(m_conDef);
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.unknownnode"), JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Loads the data for this panel. Error dialog is shown if there is a problem
    * in loading the data.
    *
    * @param def, {@link com.percusssion.loader.objectstore.PSLoaderDef} assumed
    * to be not <code>null</code>.
    */
   private void load(PSConnectionDef def) throws PSLoaderException,
      PSUnknownNodeTypeException
   {
      m_server.setText(def.getServerName());
      m_port.setText(def.getPort());
      m_user.setText(def.getUser());
      m_pwd.resetPasswordField(def.getPassword());
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void reset()
   {
      try
      {
         load(m_conDef);
      }
      catch(Exception e) // this is not possible
      {
         e.printStackTrace();
         throw new RuntimeException("Unexpected exception: " + e.toString());
      }
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public boolean validateContent()
   {
      String temp = m_server.getText();
      if (temp == null || temp.length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.missingservername"),
         getResourceString("error.title.missingservername"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      temp = m_port.getText();
      if (temp == null || temp.length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.missingport"),
         getResourceString("error.title.missingport"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      try
      {
         int port = Integer.parseInt(temp);
      }
      catch(NumberFormatException e)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.formaterror"),
         getResourceString("error.title.formaterror"),
         JOptionPane.ERROR_MESSAGE);
         return false;

      }
      temp = m_user.getText();
      if (temp == null || temp.length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.missinguser"),
         getResourceString("error.title.missinguser"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      temp = getEditedPassword();
      if (temp == null || temp.length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.missingpwd"),
         getResourceString("error.title.missingpwd"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if ( isConnectionInfoChanged() )
      {
         if (isValidConnectionInfo())
         {
            updateConnectionInfo();
            fireChangeEvent(new PSConfigurationChangeEvent(this));
         }
         else
         {
            return false;
         }
      }
      
      return true;
   }

   /**
    * Determines if the connection info has been modified, compare with the
    * internal repository of the connection info.
    * 
    * @return <code>true</code> if it has been modified; <code>false</code>
    *    otherwise.
    */
   private boolean isConnectionInfoChanged()
   {
      try
      {
         return (! m_conDef.getPassword().equals(getEditedPassword())) ||
                (! m_conDef.getUser().equals(m_user.getText())) ||
                (! m_conDef.getPort().equals(m_port.getText())) ||
                (! m_conDef.getServerName().equals(m_server.getText()));
      }
      catch (PSLoaderException e) // not possible
      {
         throw new RuntimeException("Unexpected exception: " + e.toString());
      }
   }
   
   /**
    * Get the edited password.
    * 
    * @return The password, it may not empty, but not <code>null</code>.
    */
   private String getEditedPassword()
   {
      return new String(m_pwd.getPassword());
   }
   
   /**
    * Determines if the edited connection information is valid. 
    * 
    * @return <code>true</code> if the connection info is valid; 
    *    <code>false</code> otherwise.
    */
   private boolean isValidConnectionInfo()
   {
      boolean valid = false;
      try
      {
         PSConnectionDef conn = (PSConnectionDef) m_conDef.clone();
         conn.setServerName(m_server.getText());
         conn.setPort(m_port.getText());
         conn.setUser(m_user.getText());
         conn.setPassword(getEditedPassword());

         PSLoaderRemoteAgent remoteAgent = new PSLoaderRemoteAgent(conn);            
         valid = remoteAgent.validateLogin();
      }
      catch (Exception e)
      {
         valid = false;
      }
      
      if (! valid)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.badconnection"),
         getResourceString("error.title.badconnection"),
         JOptionPane.ERROR_MESSAGE);
      }
         
      return valid;
   }

   /**
    * Updates the connection info to the internal repository
    */
   private void updateConnectionInfo()
   {
      try
      {
         PSConnectionInfo conInfo = PSConnectionInfo.getConnectionInfo();
         conInfo.setPassword(getEditedPassword());
         conInfo.setUser(m_user.getText());
         conInfo.setPort(m_port.getText());
         conInfo.setServer(m_server.getText());
   
         m_conDef.setServerName(m_server.getText());
         m_conDef.setPort(m_port.getText());
         m_conDef.setUser(m_user.getText());
         m_conDef.setPassword(getEditedPassword());
      }
      catch (PSLoaderException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Unexpected exception: " + e.toString());
      }
   }
   
   /**
    * Implements the {@link IPSConfigPanel} interface. Updates the {@link
    * PSConnectionInfo} object such that right server credentials are used for
    * cataloging.
    */
   public Element save()
   {
      try
      {
         if (!validateContent())
            return null;
         PSConnectionInfo con = PSConnectionInfo.getConnectionInfo();
         con.setPassword(getEditedPassword());
         con.setPort(m_port.getText());
         con.setServer(m_server.getText());
         con.setUser(m_user.getText());
         m_conDef.setPort(m_port.getText());
         m_conDef.setServerName(m_server.getText());
         m_conDef.setPassword(new String(m_pwd.getPassword()));
         m_conDef.setUser(m_user.getText());
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
      }
      return m_conDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Gets the resource mapping for the supplied key.
    *
    * @param key, may not be <code>null</code>
    * @return mapping corresponding to the key, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   public String getResourceString(String key)
   {
      return PSContentLoaderResources.getResourceString(ms_res, key);
   }

   /**
    * Represents a PSLoaderDef xml object. Initialized in {@link #load(Element)}
    * , never <code>null</code> after that.
    */
   private PSConnectionDef m_conDef;

   /**
    * Field holding the name of the server. Initialized in {@link #init()},
    * never <code>null</code> after that.
    */
   private UTFixedHeightTextField m_server;

   /**
    * Field holding the port# of the server. Initialized in {@link #init()},
    * never <code>null</code> after that.
    */
   private UTFixedHeightTextField m_port;

   /**
    * Field holding the user name. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private UTFixedHeightTextField m_user;

   /**
    * Field holding the password for the user. Initialized in {@link #init()},
    * never <code>null</code> after that.
    */
   private PSPasswordField m_pwd;

   /**
    * Resource bundle for this class. Initialized in the constructor.
    * Never <code>null</code> after that.
    */
   private static ResourceBundle ms_res;
}