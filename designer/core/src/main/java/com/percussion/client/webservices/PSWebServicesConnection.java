/******************************************************************************
 *
 * [ PSWebServicesConnection.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.webservices;

import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.rhythmyx.SecurityLocator;
import com.percussion.webservices.security.LoginRequest;
import com.percussion.webservices.security.LoginResponse;
import com.percussion.webservices.security.LogoutRequest;
import com.percussion.webservices.security.RefreshSessionRequest;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.security.data.PSLogin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Logs on to the server during construction. Stores the authentications details
 * thereafter and uses to make web service requests Thread safe.
 * 
 * TODO Implementation depends on Martin'd web services design.
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSWebServicesConnection
{
   /**
    * Ctor taking the details of the user and server to connect to.
    * 
    * @param connectionInfo Server and User connction details. Must not be
    * <code>null</code>
    * @throws ServiceException
    * @throws RemoteException
    * @throws PSContractViolationFault
    * @throws PSNotAuthenticatedFault
    */
   public PSWebServicesConnection(PSConnectionInfo connectionInfo)
      throws ServiceException, PSNotAuthenticatedFault,
      PSContractViolationFault, RemoteException
   {
      if (connectionInfo == null)
      {
         throw new IllegalArgumentException("connectionInfo must not be null"); //$NON-NLS-1$
      }
      m_connectionInfo = connectionInfo;
      URL servicePortAddress = null;
      try
      {
         servicePortAddress = new URL(m_connectionInfo.getProtocol(),
            m_connectionInfo.getServer(), m_connectionInfo.getPort(),
            DEFAULT_SERVICE_PORT);
      }
      catch (MalformedURLException e)
      {
         ms_log.error(e);
      }
      m_securityBinding = (SecuritySOAPStub) new SecurityLocator()
         .getsecuritySOAP(servicePortAddress);
      //convert timeout to milliseconds
      m_securityBinding.setTimeout(m_connectionInfo.getTimeout() * 1000);
      login();

      // interval is 90% of the server session timeout
      long sessionTimeOut = m_loginInfo.getSessionTimeout() * 9 / 10;
      if(sessionTimeOut < 1)
      {
         ms_log.warn("Server returned session timeout period of 0 ms");
         ms_log.warn("Assuming a default session timeout period of 30 minutes");
         sessionTimeOut = 1800000;
      }
      m_timer = new Timer();
      m_timer.schedule(new SessionKeeper(), sessionTimeOut, sessionTimeOut);
   }

   /**
    * Helper routine to make a login request to the server and revise the login
    * info. It assumes the service port and connection info are already
    * initialized.
    * 
    * @throws RemoteException
    * @throws PSNotAuthenticatedFault
    * @throws PSContractViolationFault
    */
   public void login() throws RemoteException,
      PSNotAuthenticatedFault, PSContractViolationFault
   {
      LoginRequest logRequest = new LoginRequest(m_connectionInfo.getUserid(),
         m_connectionInfo.getClearTextPassword(), PSCoreFactory.getInstance()
            .getClientSessionId(), null/* community? */,
            m_connectionInfo.getLocale());

      //convert time from seconds to milliseconds
      m_securityBinding.setTimeout(m_connectionInfo.getTimeout()*1000);
      LoginResponse resp = m_securityBinding.login(logRequest);
      m_loginInfo = resp.getPSLogin();
   }

   /**
    * Get the login info that contains all the information regardin user session
    * with server.
    * 
    * @return login info object, may be <code>null</code> if the user is not
    * currently connected to the server.
    */
   public PSLogin getLoginInfo()
   {
      return m_loginInfo;
   }

   /**
    * Get the service prt address to communicate to server.
    * 
    * @return never <code>null</code>.
    */
   public URL getServicePortAddress()
   {
      return m_servicePortAddress;
   }

   /**
    * Get the login info that contains all the information to logon to the
    * server.
    * 
    * @return connection info object, may be <code>null</code> if the user is
    * not currently connected to the server.
    */
   public PSConnectionInfo getConnectionInfo()
   {
      return m_connectionInfo;
   }

   /**
    * Closes the session with server and does required cleansup. The object
    * becomes unusable after calling this until it is reconstructed.
    */
   public void close()
   {
      try
      {
         m_securityBinding
            .logout(new LogoutRequest(m_loginInfo.getSessionId()));
      }
      // catch (PSInvalidSessionFault e)
      // catch (PSContractViolationFault e)
      // catch (RemoteException e)
      catch (Exception e)
      {
         ms_log.warn(e);
      }
      m_timer.cancel();
      m_timer = null;
      m_loginInfo = null;
   }

   /**
    * This class prevents session timing out. Makes a dummy request to the server
    * well before session times out so that session timeout period is reset on
    * the server. For example, if the session timeout period for the server is
    * 30 minutes. The dummy request is made say every 25 minutes to seep the
    * sesion alive for as long as the client is up. This runs in a separate
    * timer thread.
    */
   public class SessionKeeper extends TimerTask
   {
      /**
       * Implement the interface method to do the core job which is to make a
       * web service request to server to refresh the session for the current
       * session.
       * 
       * @see java.lang.Runnable#run()
       */
      @Override
      synchronized public void run()
      {
         try
         {
            m_securityBinding.refreshSession(new RefreshSessionRequest(
               m_loginInfo.getSessionId()));
         }
         // catch (ServiceException e)
         // catch (PSNotAuthenticatedFault e)
         // catch (PSContractViolationFault e)
         // catch (RemoteException e)
         catch (Exception e)
         {
            ms_log.error(e);
         }
      }
   }

   /**
    * Connection info used to logon to the server. Initialized in the ctor.
    * Never <code>null</code> after that.
    */
   private PSConnectionInfo m_connectionInfo = null;

   /**
    * Login info that was returned from server during logon. Never
    * <code>null</code> after successful logon.
    */
   private PSLogin m_loginInfo = null;

   private URL m_servicePortAddress = null;

   /**
    * Port binding object for the security services, initialized during logon,
    * never <code>null</code> after that.
    */
   private SecuritySOAPStub m_securityBinding = null;

   /**
    * Timer thread to make a light weight web services request to server to keep
    * the session alive. Initialized in the ctor. Never <code>null</code>
    * after that until {@link #close()} is called.
    */
   private Timer m_timer = null;

   /**
    * Default web services port on the server.
    */
   private static final String DEFAULT_SERVICE_PORT = "/Rhythmyx/webservices/securitySOAP"; //$NON-NLS-1$

   /**
    * Logger instance for this class.
    */
   private static Log ms_log = LogFactory.getLog(PSWebServicesConnection.class);
}
