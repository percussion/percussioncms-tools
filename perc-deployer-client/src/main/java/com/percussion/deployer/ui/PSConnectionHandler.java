/******************************************************************************
 *
 * [ PSConnectionHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;


import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSLockedException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSResources;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;

/**
 * The class to handle making a server connection and handle the server
 * repository information changes.
 */
public class PSConnectionHandler
{
   /**
    * Constructs the handler with the servers tree that holds all registered
    * servers and their repository information.
    *
    * @param tree the servers tree, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if tree is <code>null</code>
    */
   public PSConnectionHandler(PSServersTree tree)
   {
      if(tree == null)
         throw new IllegalArgumentException("tree may not be null.");

      m_tree = tree;
   }

    /**
     * Makes a connection to the specified server using the supplied parameters
     * and override lock, see {@link PSDeploymentServerConnection}, as <code>
     * false</code>, if {@link PSLockedException} is thrown then a dialog box
     * is presented asking the user to override the lock, if still lock cannot
     * be acquired an error dialog with error message is displayed.
     *
     * @param owner the dialog/frame making the request to connect, used to
     * display wait cursor, assumed not to be <code>null</code>
     * @param server the server to which to connect, assumed not to be <code>
     * null</code> or empty.
     * @param port the port on which server is listening, assumed  > 0.
     * @param userid the user id to connect to server, assumed not to be <code>
     * null</code> or empty.
     * @param password The password to connect to, may be <code>null</code>
     * or empty.
     * @param isEncrypted if <code>true</code> assumes the supplied password is
     * encrypted, otherwise as clear text.
     * @param useSSL <code>true</code> to use 'https' protocol to connect to the
     * server, <code>false</code> to use default 'http' protocol.
     *
     * @return the connection, may be <code>null</code> if an exception happens
     * making connection
     */
   private PSDeploymentServerConnection getConnection(Window owner,
      String server, int port, String userid, String password,
      boolean isEncrypted, boolean useSSL)
   {
      PSDeploymentServerConnection connection = null;
      owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      String protocol = useSSL ? "https" : "http";
      try
      {
         connection = new PSDeploymentServerConnection(protocol, server, port, 
            userid, password, isEncrypted, false, IPSDeployConstants.OperatingMode.MultiServerManager);
      }
      catch(PSLockedException e)
      {
         //display confirmation dialog for user to override the lock.
         PSResources res = PSDeploymentClient.getResources();
         String title = res.getString("lockErrTitle");
         String msg= res.getString("overrideLock");
         msg = e.getLocalizedMessage() + msg;
         int option = JOptionPane.showConfirmDialog(owner,
            ErrorDialogs.cropErrorMessage(msg), res.getString("errorTitle"),
               JOptionPane.OK_CANCEL_OPTION);
         if(option == JOptionPane.OK_OPTION)
         {
            try
            {
               connection = new PSDeploymentServerConnection(protocol, server, 
                  port, userid, password, isEncrypted, true, IPSDeployConstants.OperatingMode.MultiServerManager);
            }
            catch(Exception ex)
            {
               PSDeploymentClient.getErrorDialog().showError(ex, false, null);
            }
         }
      }
      catch(Exception e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false, null);
      }
      owner.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      return connection;
   }

   /**
    * Makes a connection to the specified server using the supplied parameters.
    * Displays an error dialog if an exception happens in the process. Displays
    * the <code>PSRepositoryAliasDialog</code> if the repository used by this
    * server is not set with an alias.
    *
    * @param parent the parent dialog of the dialogs that need to be shown here,
    * may not be <code>null</code>
    * @param server the server to which to connect, may not be <code>null</code>
    * or empty.
    * @param port the port on which server is listening, must be > 0.
    * @param userid the user id to connect to server, may not be <code>null
    * </code> or empty.
    * @param password The password to connect to, may be <code>null</code>
    * or empty.
    * @param isEncrypted if <code>true</code> assumes the supplied password is
    * encrypted, otherwise as clear text.
    * @param saveCredential if <code>true</code> the credentials will be saved
    * with the server registration, otherwise not.
    * @param useSSL <code>true</code> to use 'https' protocol to connect to the
    * server, <code>false</code> to use default 'http' protocol.
    * 
    * @return the connected deployment server, may be <code>null</code> if an
    * exception happens making connection or user hit 'Cancel' in the alias
    * dialog.
    */
   public PSDeploymentServer registerServer( Dialog parent, String server,
      int port, String userid, String password, boolean isEncrypted,
      boolean saveCredential, boolean useSSL) throws PSDeployException
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null.");

      if(server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty.");

      if(port <= 0)
         throw new IllegalArgumentException("port must be > 0.");

      if(userid == null || userid.trim().length() == 0)
         throw new IllegalArgumentException("userid may not be null or empty.");

      PSDeploymentServerConnection connection = getConnection(parent,
            server, port, userid, password, false, useSSL);

      if(connection == null)
         return null;

      PSDbmsInfo connRepository = connection.getRepositoryInfo();
      OSDbmsInfo repositoryInfo = m_tree.getMatchingRepository(connRepository);
      if(repositoryInfo == null) //no matching repository
      {
         String repositoryAlias = getRepositoryIdentifier(connRepository);
         String dataSource      = connRepository.getDatasource();
         PSRepositoryAliasDialog dlg = new PSRepositoryAliasDialog(parent,
            dataSource, repositoryAlias, m_tree.getExistingRepositoryAliases());
         dlg.setVisible(true);
         if(!dlg.isOk())
         {
            connection.disconnect();
            return null;
         }

         repositoryInfo = new OSDbmsInfo(dlg.getAlias(), connRepository);
      }

      PSServerRegistration registration = new PSServerRegistration(
         server, port, userid, connection.getPassword(true),
         saveCredential, useSSL);

      PSDeploymentServer depServer =
         new PSDeploymentServer(registration, repositoryInfo);
      depServer.createDeploymentManager(connection);
      depServer.setServerLicensed(connection.isServerLicensed());

      m_tree.addServerNode(repositoryInfo, depServer);

      // save the changes
      PSDeploymentClient.saveServers(m_tree.getRegisteredServerMap());

      return depServer;
   }

   /**
    * Makes a connection to the server with the supplied parameters and displays
    * a detailed confirmation dialog to user to continue with the process if the
    * server repository information has changed. If user wants to continue,
    * checks to see if the current repository information is set with the alias
    * or not. If not displays <code>RepositoryAliasDialog</code> to enter the
    * alias name for the repository. Upon pressing OK in this dialog moves the
    * server node to appropriate repository node.
    *
    * @param parent the parent window of the dialogs that need to be shown here,
    * may not be <code>null</code> and must be instance of <code>Dialog</code>
    * or <code>Frame</code>
    * @param server the server that is being edited, may not be
    * <code>null</code>
    * @param port the port on which server is listening, must be > 0.
    * @param userid the user id to connect to server, may not be <code>null
    * </code> or empty.
    * @param password The password to connect to, may be <code>null</code>
    * or empty.
    * @param isEncrypted if <code>true</code> assumes the supplied password is
    * encrypted, otherwise as clear text.
    * @param saveCredential if <code>true</code> the credentials will be saved
    * with the server registration, otherwise not.
    * @param useSSL <code>true</code> to use 'https' protocol to connect to the
    * server, <code>false</code> to use default 'http' protocol.
    *
    * @return <code>false</code> if an exception happens making connection or
    * user hit 'Cancel' in the dialogs displayed in this method, otherwise
    * <code>true</code>
    */
   public boolean connectAndUpdateServerRegistration(Window parent,
      PSDeploymentServer server, int port,
      String userid, String password, boolean isEncrypted,
      boolean saveCredential, boolean useSSL)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null.");

      if(!(parent instanceof Frame) && !(parent instanceof Dialog))
         throw new IllegalArgumentException(
         "parent must be an instance of Dialog or Frame");

       if(server == null )
         throw new IllegalArgumentException("server may not be null");

      if(port <= 0)
         throw new IllegalArgumentException("port must be > 0.");

      if(userid == null || userid.trim().length() == 0)
         throw new IllegalArgumentException("userid may not be null or empty.");

      PSDeploymentServerConnection connection = getConnection(parent,
            server.getServerRegistration().getServer(), port, userid, password,
            isEncrypted, useSSL);

      if(connection == null)
         return false;

      PSDbmsInfo connRepository = connection.getRepositoryInfo();
      if(!server.getRepositoryInfo().isSameDb(connRepository))
      {
         PSResources res = PSDeploymentClient.getResources();

         String title = res.getString("repositoryInfoChangeTitle");
         String message = res.getString("repositoryInfoChangeConfirm");

         String origRep = getRepositoryIdentifier(server.getRepositoryInfo());
         String newRep = getRepositoryIdentifier(connRepository);
         message = MessageFormat.format(message, new String[]{origRep, newRep});

         //display confirmation dialog and others
         int option = JOptionPane.showConfirmDialog(parent,
            ErrorDialogs.cropErrorMessage(message), title,
               JOptionPane.OK_CANCEL_OPTION);

         if(option == JOptionPane.CANCEL_OPTION)
         {
            try
            {
               connection.disconnect();
            }
            catch(PSDeployException e)
            {
               PSDeploymentClient.getErrorDialog().showError(e, false, null);
            }
            return false;
         }

         String repositoryAlias;
         OSDbmsInfo repositoryInfo =
            m_tree.getMatchingRepository(connRepository);
         if(repositoryInfo == null) //no matching repository
         {
            List existAliases = m_tree.getExistingRepositoryAliases();
            PSRepositoryAliasDialog dlg;
            if(parent instanceof Dialog)
            {
               dlg = new PSRepositoryAliasDialog((Dialog)parent,
                     server.getRepositoryInfo().getDatasource(), newRep, existAliases);
            }
            else
            { 
               dlg = new PSRepositoryAliasDialog((Frame)parent,
                     server.getRepositoryInfo().getDatasource(), newRep, existAliases);
            }

            dlg.setVisible(true);

            if(!dlg.isOk())
            {
               try
               {
                  connection.disconnect();
               }
               catch(PSDeployException e)
               {
                  PSDeploymentClient.getErrorDialog().showError(e, false, null);
               }
               return false;
            }

            repositoryInfo = new OSDbmsInfo(dlg.getAlias(), connRepository);
         }

         m_tree.moveServerNode(server.getRepositoryInfo(),
            repositoryInfo, server);
         server.setRepositoryInfo(repositoryInfo);
      }

      server.getServerRegistration().setPort(port);
      server.getServerRegistration().setCredentials(userid,
         connection.getPassword(true));
      server.getServerRegistration().setSaveCredential(saveCredential);
      server.getServerRegistration().setUseSSL(useSSL);
      server.createDeploymentManager(connection);
      server.setServerLicensed(connection.isServerLicensed());

      // save the changes
      PSDeploymentClient.saveServers(m_tree.getRegisteredServerMap());
      
      return true;
   }

   /**
    * Makes a connection to the server if the login credentials are saved with
    * this server, otherwise prompts with a dialog to provide connection
    * details.  If server is already connected,
    * method simply returns.
    *
    * @param server the server that need to be connected, may not be <code>null
    * </code>.
    * 
    * @return <code>true</code> if a connected is successfully made to the 
    * server or if the server is already connected, <code>false</code> 
    * otherwise.
    *
    * @throws IllegalArgumentException if server is <code>null</code>
    */
   public boolean connectToServer(PSDeploymentServer server)
   {
      if(server == null)
         throw new IllegalArgumentException("server may not be null.");
      
      if (!server.isConnected())
      {
         PSServerRegistration registration = server.getServerRegistration();
         if(registration.isSaveCredentials())
         {
            connectAndUpdateServerRegistration(PSDeploymentClient.getFrame(),
               server, registration.getPort(),
               registration.getUserName(), registration.getPassword(), true, true, 
               registration.isUseSSL());
         }
         else
         {
            PSServerRegistrationDialog dlg = new PSServerRegistrationDialog(
               PSDeploymentClient.getFrame(), server, this, true, null);
            dlg.setVisible(true);
         }
      }

      
      return server.isConnected();
   }

   /**
    * Gets the repository identifier string from this repository. This returns
    * a string in the format "driver:server:database:origin".
    *
    * @param repository, the repository to which the identity string need to be
    * returned, assumed not to be <code>null</code>
    *
    * @return the repository string, never <code>null</code> or empty.
    */
   private String getRepositoryIdentifier(PSDbmsInfo repository)
   {
      return repository.getDriver() + ":" +
            repository.getServer() + ":" + repository.getDatabase() +
            ":" + repository.getOrigin();
   }

   /**
    * The tree to which the server node to be added in case of new server
    * registration or move the server node to some other repository node in case
    * of editing a server registration or connecting to a server as the
    * repository information might have changed. Intialized in the constructor
    * and never <code>null</code> after that.
    */
   private PSServersTree m_tree;
}
