/******************************************************************************
 *
 * [ PSServerConnectionManager.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.managers;

import com.percussion.conn.PSServerException;
import com.percussion.deployer.catalog.PSCataloger;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.packager.ui.PSCredentialsDialog;
import com.percussion.packager.ui.PSPackagerClient;
import com.percussion.packager.ui.PSResourceUtils;
import com.percussion.packager.ui.PSUiUtils;
import com.percussion.packager.ui.data.PSServerRegistration;
import com.percussion.packager.ui.data.PSServerRegistrations;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.guitools.ErrorDialogs;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Singleton class that handles persistence of the server registrations
 * and creation and access to deployer server connection objects.
 */
public class PSServerConnectionManager
{
   
   /**
    * Protected Ctor to inhibit instantiation.
    */
   protected PSServerConnectionManager(String filepath)
   {
      loadServerRegistrations(filepath);
   }
   
   /**
    * Returns the singleton instance of the server connection manager.
    * @return
    */
   public static PSServerConnectionManager getInstance()
   {
      if(ms_instance == null)
      {
         ms_instance = new PSServerConnectionManager("ServerRegistrations.xml");
      }
      return ms_instance;
   }
   
   /**
    * Return all server registrations found on the local machine.
    * @return list of all server registrations.
    */
   public PSServerRegistrations getServers()
   {
      PSServerRegistrations servers = new PSServerRegistrations();
      for(PSServerRegistration sr : m_servers)
      {
         servers.add(sr);
      }
      return servers;
   }
   
   /**
    * Get the server registration marked as default.
    * @return default server registration or <code>null</code>
    * if not found.
    */
   public PSServerRegistration getDefaultServer()
   {
      for(PSServerRegistration sr : m_servers)
      {
         if(sr.isDefault())
            return sr;
      }
      return null;
   }
   
   /**
    * Find a server registration by its host/port string ie myhost:8000.
    * @param hostport the hot port string, cannot be <code>null</code>
    * or empty.
    * @return server registration or <code>null</code>
    * if not found.
    */
   public PSServerRegistration getServerByHostPortString(String hostport)
   {
      if(StringUtils.isBlank(hostport))
         throw new IllegalArgumentException("hostport cannot be null or empty.");
      for(PSServerRegistration sr : m_servers)
      {
         if(sr.toString().equals(hostport))
            return sr;
      }
      return null;
   }
   
   /**
    * Saves the server registration to the file system.
    * @param server the server registration, cannot be
    * <code>null</code>.
    */
   public void saveServerRegistration(PSServerRegistration server)
   {
      m_servers.remove(server);
      if(server.isDefault())
         clearAllDefaultFlags();
      m_servers.add(server);
      saveServerRegistrations();
   }
   
   /**
    * Deletes the server registration from the file system.
    * @param server the server registration, cannot be
    * <code>null</code>.
    */
   public void deleteServerRegistration(PSServerRegistration server)
   {
      m_servers.remove(server);
      if(m_recentConnections.contains(server.toString()))
         m_recentConnections.remove(server.toString());      
      saveServerRegistrations();
   }
   
   /**
    * Removes any server registration not in the recent connections list.
    */
   public void deleteAllNonRecentConnections()
   {
      List<PSServerRegistration> deleteList = 
         new ArrayList<PSServerRegistration>();
      for(PSServerRegistration server : m_servers)
      {
         if(!m_recentConnections.contains(server.toString()))
            deleteList.add(server);
      }
      if(!deleteList.isEmpty())
      {
         for(PSServerRegistration s : deleteList)
            m_servers.remove(s);
         saveServerRegistrations();
      }
   }
   
   /**
    * Gets currently initialized deployment server connection.
    * 
    * @return the deployment server connection, maybe <code>null</code>
    * if never initialized.
    */
   public PSDeploymentServerConnection getConnection()
   {
      return m_conn;
   }
   
   /**
    * Disconnect the current connection if one exists.
    * This should be used instead of calling the connections
    * disconnect method directly so that server manager 
    * registered listeners will be notified;
    * @throws PSDeployException 
    */
   public void disconnect() throws PSDeployException
   {
      if(m_conn != null)
      {
         PSServerRegistration server = getCurrentServerInfo();
         String hostport = server.toString();
         if(!server.isSaveCredentials())
         {
            server.clearCredentials();
         }
         m_conn.disconnect();
         m_currentServer = null;
         m_deploymentManager = null;
         m_cataloger = null;
         fireServerConnectionEvent(hostport, false);
      }
   }
   
   /**
    * Retrieves server registration for currently initialized connection.
    * @return server registration, may be <code>null</code> if no
    * connection initialized.
    */
   public PSServerRegistration getCurrentServerInfo()
   {
      return m_currentServer;
   }
   
   /**
    * Initializes the specified server by doing the following steps.
    * <pre>
    * 1. check for existing connection and disconnect
    * 2. create new deployment server connection
    * 3. Registers server in recent connections
    * </pre>
    * @param server the server registration cannot be <code>null</code>
    * @return <code>true</code> if cancelled by user. 
    * @throws PSDeployException 
    * @throws PSServerException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationFailedException 
    */
   public boolean initConnection(PSServerRegistration server) 
      throws PSAuthenticationFailedException, PSAuthorizationException,
      PSServerException, PSDeployException
   {
        return initConnection(server, true);  
   }   
   
   /**
    * Initializes the specified server by doing the following steps.
    * <pre>
    * 1. check for existing connection and disconnect
    * 2. create new deployment server connection
    * 3. Registers server in recent connections if register is set to 
    * <code>true</code>
    * </pre>
    * @param server the server registration cannot be <code>null</code>
    * @param register Registers server in recent connections if register is set 
    * to <code>true</code>
    * @return <code>true</code> if cancelled by user. 
    * @throws PSDeployException 
    * @throws PSServerException 
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationFailedException 
    */
   public boolean initConnection(PSServerRegistration server, boolean register) 
      throws PSAuthenticationFailedException, PSAuthorizationException,
      PSServerException, PSDeployException
   {
      if(server == null)
         throw new IllegalArgumentException("server cannot be null.");
      if(m_conn != null && m_conn.isConnected())
      {         
            disconnect();
      }
      String user = server.getUserName();
      String pass = server.getPassword();
      
      if(StringUtils.isBlank(user))
      {
         //Ask for creds
         PSCredentialsDialog dialog = 
            new PSCredentialsDialog(PSPackagerClient.getFrame(),
               server.getServer() + ":" + server.getPort());
         dialog.setVisible(true);
         if(dialog.isOk())
         {
            user = dialog.getUsername();
            pass = dialog.getPassword();
         }
         else
         {
            return true;
         }
      }

      try {
         m_conn = new PSDeploymentServerConnection(
                 server.isUseSSL() ? "https" : "http",
                 server.getServer(),
                 server.getPort(),
                 user,
                 pass,
                 false,
                 true);

         m_currentServer = server;
         m_deploymentManager = new PSDeploymentManager(m_conn);
         m_cataloger = m_deploymentManager.getCataloger();
         fireServerConnectionEvent(server.toString(), true);
         if (register) {
            addRecentConnection(server);
         }
      }catch(PSAuthenticationFailedException e){
         JOptionPane.showMessageDialog(PSPackagerClient.getFrame(), PSResourceUtils.getCommonResourceString("invalidCredentials"));
         server.clearCredentials();
         return this.initConnection(server);
      }catch(Exception npe){
         PSUiUtils.showStackTraceDialog(npe,"ERROR","Connection to Server Failed. Invalid Server Configurations/Credentials");
         System.out.println("Connection to Server Failed Due to: " + npe.getMessage());
         server.clearCredentials();
         return this.initConnection(server);
      }
        return false;
      
   }  
   
   /**
    * @return the deploymentManager will be <code>null</code> if
    * no connection exists.
    */
   public PSDeploymentManager getDeploymentManager()
   {
      return m_deploymentManager;
   }

   /**
    * @return the cataloger will be <code>null</code> if
    * no connection exists.
    */
   public PSCataloger getCataloger()
   {
      return m_cataloger;
   }

   /**
    * Helper method to create title string.
    * @return the title string, never <code>null</code>.
    */
   public String getConnectionTitleString(String prefix)
   {
      StringBuilder sb = new StringBuilder();
      if(prefix != null)
      {
         sb.append(prefix);
         sb.append(" --- ");
      }
      if(m_conn != null && m_conn.isConnected())
      {
         sb.append(m_currentServer.getServer());
         sb.append(":");
         sb.append(m_currentServer.getPort());
         sb.append(" [");
         sb.append(m_conn.getServerVersion());
         sb.append(" - ");
         sb.append(m_conn.getRepositoryInfo().getDbmsIdentifier());
         sb.append("]");
      }
      else
      {
         sb.append("(Not Connected)");
      }
      return sb.toString();
      
   }
   
   /**
    * Returns all unique recent connection host:port strings
    * from latest to oldest
    * @return list of recent conns, never <code>null</code>,
    * may be empty.
    */
   public List<String> getRecentConnections()
   {
      List<String> recent = new ArrayList<String>();
      if(m_recentConnections != null)
      {
         for(String r : m_recentConnections)
         {
            if(StringUtils.isNotBlank(r))
               recent.add(0, r);
         }
      }
      return recent;
   }
   
   /**
    * Adds a server connection listener to this manager.
    * @param listener cannot be <code>null</code>.
    */
   public void addServerConnectionListener(
      IPSServerConnectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_listeners.contains(listener))
         m_listeners.add(listener);
   }
   
   /**
    * Removes a server connection listener to this manager.
    * @param listener cannot be <code>null</code>.
    */
   public void removeServerConnectionListener(
      IPSServerConnectionListener listener)
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_listeners.contains(listener))
         m_listeners.remove(listener);
   }
   
   /**
    * Fire a connection event off and notify all registered
    * listeners.
    * @param hostport assumed not <code>null</code> or empty.
    * @param isConnectionEvent flag indicating if this is a
    * connection event type.
    */
   private void fireServerConnectionEvent(
      String hostport, boolean isConnectionEvent)
   {
      for(IPSServerConnectionListener listener : m_listeners)
      {
         if(isConnectionEvent)
         {
            listener.connected(hostport);
         }
         else
         {
            listener.disconnected(hostport);
         }
      }
   }
   
   /**
    * Adding a recent connection removing an existing entry
    * from the list with the same hostport. Also maintains the
    * MAX_RECENT_CONNECTIONS size by removing the oldest entry before
    * adding a new one. Persists to file system.
    * @param server
    */
   private void addRecentConnection(PSServerRegistration server)
   {
      String hostport = server.toString();
      if(m_recentConnections.contains(hostport))
      {
         m_recentConnections.remove(hostport);
      }
      else
      {
         if(m_recentConnections.size() == MAX_RECENT_CONNECTIONS)
            m_recentConnections.remove(0);         
      }
      m_recentConnections.add(hostport);
      saveServerRegistrations();
   }
   
   /**
    * Helper method to clear all default flags for existing
    * server registrations.
    */
   private void clearAllDefaultFlags()
   {
      for(PSServerRegistration sr : m_servers)
      {
         sr.setIsDefault(false);
      }
   }
   
   /**
    * Utility method to load server registrations from the file system.
    */
   protected void loadServerRegistrations(String filepath)
   {
      m_registerFile = filepath;
      m_servers = new ArrayList<PSServerRegistration>();
      File regFile = new File(IPSDeployConstants.CLIENT_DIR,
         m_registerFile);
      if(regFile.exists())
      {
         FileInputStream in = null;
         try
         {
            in = new FileInputStream(regFile);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            PSServerRegistrations servers = 
               new PSServerRegistrations(doc.getDocumentElement());
            Iterator<PSServerRegistration> it = servers.getServers();
            while(it.hasNext())
            {
               m_servers.add(it.next());
            }
            m_recentConnections = servers.getRecentConnections();
            
         }
         catch(IOException e)
         {
            System.out.println(
               "Error reading the server registrations file <" +
               regFile.getAbsolutePath() + ">");
            ErrorDialogs.showErrorMessage(null, MessageFormat.format(
               PSResourceUtils.getCommonResourceString("ioReadError"),
               new String[]{regFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);
         }
         catch(SAXException e)
         {
            System.out.println(
               "Exception parsing the xml file to load the server registrations"
               );
            System.out.println(e.getLocalizedMessage());

            ErrorDialogs.showErrorMessage(null,MessageFormat.format(
               PSResourceUtils.getCommonResourceString("ioParseError"),
               new String[]{regFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);
         }
         catch(Exception e)
         {
            System.out.println(
               "Exception loading server registrations defined in the file. "
               + e.getLocalizedMessage());

            ErrorDialogs.showErrorMessage(null, MessageFormat.format(
               PSResourceUtils.getCommonResourceString("ioDocError"),
               new String[]{regFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);

         }         
         finally
         {
            try {
               if(in != null)
                  in.close();
            } catch(IOException ie){}
         }

      }
      
   }
   
   /**
    * Utility method to save all server registrations to the file system.
    */
   private void saveServerRegistrations()
   {
      File dir = new File(IPSDeployConstants.CLIENT_DIR);
      File regFile = new File(dir, m_registerFile);
      if(regFile.exists())
      {
         regFile.delete();
      }
      else if(!dir.exists() || !dir.isDirectory())
      {         
         new File(IPSDeployConstants.CLIENT_DIR).mkdirs();
      }
      PSServerRegistrations servers = new PSServerRegistrations();
      servers.setRecentConnections(m_recentConnections);
      for(PSServerRegistration sr : m_servers)
      {
         servers.add(sr);
      }
      FileOutputStream out = null;
      try
      {
         out = new FileOutputStream(regFile);
         Document doc =  PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.write(servers.toXml(doc), out);
      }
      catch (IOException e)
      {
         System.out.println("Error saving to the server registrations file <"
            + regFile.getAbsolutePath() + ">");
         ErrorDialogs.showErrorMessage(null, MessageFormat.format(
            PSResourceUtils.getCommonResourceString("ioWriteError"),
            new String[]{
               regFile.getAbsolutePath(),
               e.getLocalizedMessage()}), null);
      }
      finally
      {
         try {
            if(out != null)
               out.close();
         } catch(IOException ie){}
      }
   }
   
   /**
    * Server registration list. Initialized in
    * {@link #loadServerRegistrations(String)}. Never <code>null</code> after that.
    */
   protected List<PSServerRegistration> m_servers;
   
   /**
    * List of the last 5 recently connected server registrations
    * by host:port string.
    */
   protected List<String> m_recentConnections = new ArrayList<String>();
   
   /**
    * The currently initialized deployment server connection.
    * Initialized in {@link #initConnection(PSServerRegistration)}.
    * Can be retrieved by calling {@link #getConnection()}.
    */
   protected PSDeploymentServerConnection m_conn;
   
   /**
    * The currently initialized server registration.
    * Set in {@link #initConnection(PSServerRegistration)}.
    * May be <code>null</code>.
    */
   protected PSServerRegistration m_currentServer;
   
   /**
    * The deployment manager that will be used to create requests to a server.
    * Created in {@link #initConnection(PSServerRegistration)}
    * and set to <code>null</code> in {@link #disconnect()}.
    */
   protected PSDeploymentManager m_deploymentManager = null;

   /**
    * The cataloger used to catalog the data from the server.
    * Created in {@link #initConnection(PSServerRegistration)}
    * and set to <code>null</code> in {@link #disconnect()}.
    */
   protected PSCataloger m_cataloger = null;
   
   /**
    * List of all registered serve connection listeners.
    * Never <code>null</code>, may be empty.
    */
   protected List<IPSServerConnectionListener> m_listeners = 
      new ArrayList<IPSServerConnectionListener>();
      
   /**
    * Singleton instance of the server connection manager.
    * Initialized in {@link #getInstance()}, never 
    * <code>null</code> after that.
    */
   protected static PSServerConnectionManager ms_instance;   
      
   /**
    * The name of the server registration xml file.
    */
   private String m_registerFile;
   
   /**
    * Maximum number of recent connections stored.
    */
   protected static final int MAX_RECENT_CONNECTIONS = 5;
   
}
