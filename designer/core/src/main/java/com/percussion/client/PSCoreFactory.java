/******************************************************************************
 *
 * [ PSCoreFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.PSLogonStateChangedEvent.LogonStateEvents;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.proxies.IPSCmsModelProxy;
import com.percussion.client.proxies.IPSHierarchyModelProxy;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.client.webservices.PSWebServicesConnection;
import com.percussion.conn.IPSConnectionErrors;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.error.PSErrorManagerDefaultImpl;
import com.percussion.error.PSException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSProperties;
import com.percussion.util.PSRemoteRequester;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.faults.Error;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;
import com.percussion.xml.serialization.PSObjectSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class provides models and proxies for design objects. The map of object
 * types to classes is hard-coded in this class for now. In the future, this
 * could be moved to the plugin.xml registry when (if) we expose this stuff. But
 * the class is designed for extensibility.
 * <p>
 * Models are instances of {@link IPSCmsModel} while proxies are instances of
 * {@link IPSCmsModelProxy}.
 * <p>
 * A model is not loaded until it is requested the first time. Then a single
 * copy is kept in memory until the program terminates. The same behavior
 * applies to proxies.
 * <p>
 * This class implements the singleton pattern. Use the {@link #getInstance()}
 * method to obtain the single instance.
 * <p>
 * The client can opererate in 3 modes:
 * <ol>
 * <li>Local - this is used for unit testing code and for running the client
 * w/o a running server. Test data is provided that emulates a simple system.</li>
 * <li>Remote with test creds - this is used mainly for unit testing web
 * services and such</li>
 * <li>Remote with user creds - this is normal behavior</li>
 * <ol>
 * The state can be determined by 2 flags: {@link #isLocalMode()} and
 * {@link #isTestConnectionInfo}. These 2 flags and the connection info is
 * stored in the 'conn_rxserver.properties' file (located in
 * com/percussion/testing package.) If the file cannot be found, the system
 * defaults to the normal behavior (remote with user creds.)
 * 
 * @author paulhoward
 * @version 6.0
 * @see IPSCmsModel
 */
public class PSCoreFactory
{
   /**
    * Obtains the single instance of the factory.
    * 
    * @return Never <code>null</code>.
    */
   synchronized public static PSCoreFactory getInstance()
   {
      if (ms_instance == null)
      {
         ms_instance = new PSCoreFactory();
      //   ms_instance.initSpringContext();

         // Initialize the singleton with the class registry from this package
         PSObjectSerializer.getInstance().registerBeanClasses(
            PSCoreFactory.class);
         
         //check if the test pkg is visible
         try
         {
            Class clazz = 
               Class.forName("com.percussion.testing.IPSCustomJunitTest");
            InputStream is = clazz.getResourceAsStream(
                  "conn_rxserver.properties");
            if (null != is)
            {
               Properties props = new Properties();
               props.load(is);
               ms_instance.m_useTestProxies = props.getProperty(
                     "useLocalData", "").equalsIgnoreCase("true");
               ms_instance.m_useTestConnectionInfo = props.getProperty(
                     "useTestCreds", "").equalsIgnoreCase("true");
               if (ms_instance.m_useTestConnectionInfo)
               {
                  ms_instance.m_testConnectionInfo = new PSConnectionInfo(
                        "Test", props.getProperty("hostName"), Integer
                              .parseInt(props.getProperty("port")), props
                              .getProperty("loginId"), props
                              .getProperty("loginPw"), props.getProperty(
                              "useSSL", "").equalsIgnoreCase("true"), props
                              .getProperty("locale"));
               }
            }
         }
         catch (ClassNotFoundException ignore)
         {
            //don't load the test info
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
         
         
         // build all maps
         for (ModelInfo info : ms_modelInfo)
         {
            ms_instance.m_modelNameToType.put(info.m_primaryType.name(),
                  info.m_primaryType);
         }
      }
      return ms_instance;
   }

   /**
    * One way to retrieve the model for a design object. This might be used if
    * some declarative structure needed to specify design objects.
    * 
    * @param objectType A string representation of one of the enumerations that
    * this factory knows about. Never <code>null</code> or empty. If the
    * supplied value cannot be converted to an enum value using
    * {@link Enum#valueOf(Class, String)}, an exception will be thrown.
    * 
    * @return Never <code>null</code>.
    * 
    * @throws PSModelException If no model by the requested type is registered.
    */
   public IPSCmsModel getModel(String objectType) throws PSModelException
   {
      Enum type = m_modelNameToType.get(objectType);
      if (type == null)
      {
         throw new PSModelException(PSErrorCodes.UNKOWN_MODEL, new Object[]
         {objectType});
      }
      return getModel(type);
   }

   /**
    * Checks the supplied object type against all registered types.
    * 
    * @param objectType Never <code>null</code>.
    * 
    * @return <code>true</code> if a model could be obtained by calling
    *         {@link #getModel(Enum) getModel(objectType)}, <code>false</code>
    *         otherwise.
    */
   public boolean isSupportedType(Enum objectType)
   {
      try
      {
         return findModel(objectType, false) != null;
      }
      catch (PSModelException e)
      {
         // this will never happen because we pass false to findModel
         throw new RuntimeException("never happen"); //$NON-NLS-1$
      }
   }

   /**
    * Convenience method that calls
    * {@link #getModel(Enum) getModel(ref.getObjectType().getPrimaryType())}.
    * 
    * @param ref Never <code>null</code>.
    */
   synchronized public IPSCmsModel getModel(IPSReference ref)
         throws PSModelException
   {
      if (null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");
      }

      return getModel(ref.getObjectType().getPrimaryType());
   }

   /**
    * Walks all known models, checking the supplied name against the names 
    * known to each model until a match is found or there are no more models.
    * 
    * @param treeName Never <code>null</code> or empty. Compare done 
    * case-insensitive.
    * 
    * @return A matching model, or <code>null</code> if not found.
    */
   public IPSCmsModel getHierarchyModel(String treeName)
   {
      for (IPSCmsModel model : m_models.values())
      {
         if (model.isHierarchyModel())
         {
            for (String name : model.getHierarchyTreeNames())
            {
               if (name.equalsIgnoreCase(treeName))
                  return model;
            }
         }
      }
      return null;
   }

   /**
    * Checks all registered models until it finds the one that supports the
    * supplied type and then returns it. If a type cannot be found, an exception
    * is thrown. Use {@link #isSupportedType(Enum)} to determine if this method
    * will return a model.
    * 
    * @param objectType Never <code>null</code>.
    * 
    * @return The model registered for the supplied type. Never
    *         <code>null</code>.
    * 
    * @throws PSModelException If no model by the requested type is registered.
    */
   synchronized public IPSCmsModel getModel(Enum objectType)
         throws PSModelException
   {
      IPSCmsModel model = m_models.get(objectType);
      if (null == model)
      {
         ModelInfo info = findModel(objectType, true);
         try
         {
            String className = info.m_modelClassName;
            if (!className.equals(ModelInfo.CMS_MODEL_CLASSNAME))
               className = getHandlerClassName(className);
            Class clazz = Class.forName(className);
            Constructor ctor = clazz.getConstructor(new Class[] {
                  String.class, String.class, IPSPrimaryObjectType.class });
            model = (IPSCmsModel) ctor.newInstance(new Object[]
                  {info.m_name, info.m_description,
                  (IPSPrimaryObjectType) objectType});
         }
         catch (Exception e)
         {
            Object[] errorArgs = new Object[4];
            errorArgs[0] = info.m_name;
            errorArgs[1] = info.m_modelClassName;
            if (e instanceof InvocationTargetException)
            {
               Throwable cause = 
                  ((InvocationTargetException)e).getTargetException();
               errorArgs[2] = cause.getClass().getName();
               errorArgs[3] = cause.getLocalizedMessage();
            }
            else
            {
               errorArgs[2] = e.getClass().getName();
               errorArgs[3] = e.getLocalizedMessage();
            }

            throw new PSModelException(PSErrorCodes.INVALID_MODEL_REGISTRATION,
                  errorArgs);
         }

         for (IPSModelListener listener : m_listeners.keySet())
         {
            int notifications = m_listeners.get(listener).intValue();
            if (notifications != 0)
               model.addListener(listener, notifications);
         }
         m_models.put(objectType, model);
      }
      return model;
   }

   /**
    * A convenience method that calls
    * {@link IPSCmsModel#flush(IPSReference) flush(<code>null</code>)} on all
    * models that have been loaded into memory. See that method for details.
    */
   public void flushAll()
   {
      for (IPSCmsModel model : m_models.values())
      {
         model.flush(null);
      }
   }

   /**
    * Get CMS model proxy for the given design object type.
    * <p>
    * This method is not meant to be used by clients of the core. It is public
    * as a side effect of the implementation.
    * 
    * @param objectType Object type for which the CMS model proxy is being
    *           requested, must not be <code>null</code>.
    * 
    * @return the CMS model proxy, or <code>null</code> if one is not
    *         configured or applicable.
    * 
    * @throws PSModelException If the class registered for this model cannot be
    *            loaded.
    */
   public IPSCmsModelProxy getCmsModelProxy(Enum objectType)
         throws PSModelException
   {
      IPSCmsModelProxy proxy = m_proxies.get(objectType);
      if (null == proxy)
      {
         ModelInfo info = findModel(objectType, true);

         proxy = (IPSCmsModelProxy) loadClass(info.m_name,
               getHandlerClassName(info.m_proxyClassName), IPSCmsModelProxy.class);
         m_proxies.put(objectType, proxy);
      }
      return proxy;
   }

   /**
    * Get hierarchical model proxy for the given design object type.
    * <p>
    * This method is not meant to be used by clients of the core. It is public
    * as a side effect of the implementation.
    * 
    * @param objectType Object type for which the hierarchical model proxy is
    *           being requested, must not be <code>null</code>.
    * 
    * @return the CMS model proxy or <code<null</code> if one is not
    *         configured or applicable.
    * 
    * @throws PSModelException If the class registered for this model cannot be
    *            loaded.
    */
   public IPSHierarchyModelProxy getHierarchyModelProxy(Enum objectType)
         throws PSModelException
   {
      IPSHierarchyModelProxy proxy = m_hierarchyProxies.get(objectType);
      if (null == proxy)
      {
         ModelInfo info = findModel(objectType, true);
         if (info.m_hierarchyProxyClassName == null)
            return null;
         proxy = (IPSHierarchyModelProxy) loadClass(info.m_name,
               getHandlerClassName(info.m_hierarchyProxyClassName),
               IPSHierarchyModelProxy.class);
         m_hierarchyProxies.put(objectType, proxy);
      }
      return proxy;
   }

   /**
    * If the factory has been flagged to return test implementations of the
    * proxies, the supplied classname will be modified by adding a 'test' pkg
    * after the 'impl' package.
    * 
    * @param className Assumed not <code>null</code> or empty and that it has
    *           an 'impl' package.
    * 
    * @return What was supplied, possibly with an additional package part.
    */
   private String getHandlerClassName(String className)
   {
      String result;
      if (m_useTestProxies)
      {
         int pos = className.lastIndexOf("."); //$NON-NLS-1$
         result = className.substring(0, pos) + ".test" //$NON-NLS-1$
               + className.substring(pos);
      }
      else
         result = className;
      return result;
   }

   /**
    * Loads a class by name, mapping any exception into 1.
    * 
    * @param modelName Used only for error messages.
    * 
    * @param name The fully qualified name of the class to load. Assumed not
    *           <code>null</code> or empty.
    * @param requiredType If not <code>null</code>, the returned object is
    *           guaranteed to be castable to this type or an exception is
    *           thrown.
    * @return Never <code>null</code>.
    */
   private Object loadClass(String modelName, String name, Class requiredType)
         throws PSModelException
   {
      try
      {
         Class clazz = Class.forName(name);
         Object o = clazz.newInstance();
         if (requiredType != null)
            requiredType.cast(o);
         return o;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         // since there are so many and we do the same for all, catch them all
         Object[] errorArgs = new Object[4];
         errorArgs[0] = name;
         errorArgs[1] = "Loading model: " + modelName;

         throw new PSModelException(PSErrorCodes.LOAD_CLASS_FAILED,
               errorArgs, e);
      }
   }

   /**
    * Logon to server with supplied user credentials and server information.
    * Create a web services connection keeps an instance of it. If the
    * connection was made earlier to this, the user will be logged off and
    * logged in again.
    * <p>
    * After successful connection, a notification is sent to all registered
    * clients.
    * 
    * @param connectionInfo Connection information with server and user details
    * to logon. If <code>null</code> and the factory is configured to use
    * test credentials, those credentials will be used. Otherwise, a message
    * will be sent to the registered listener, giving them an opportunity to
    * supply credentials.
    */
   public void logon(PSConnectionInfo connectionInfo)
   {
      m_connecting = true;
      try
      {
         doLogon(connectionInfo);
      }
      finally
      {
         m_connecting = false;
         // to make possible other threads to react on end of connecting
         Thread.yield();
      }
   }

   /**
    * {@link #logon(PSConnectionInfo)} implementation, allowing that method
    * to deal with {@link #isConnecting()}.
    * @see #logon(PSConnectionInfo) 
    */
   private void doLogon(PSConnectionInfo connectionInfo)
   {
      if (m_useTestProxies)
      {
         ms_log.info("Will not logon for test proxies");
         return;
      }
      // Logoff if already logged in
      logoff();
      // Now logon
      Exception ex = null;
      do
      {
         if (connectionInfo == null)
         {
            connectionInfo = m_useTestConnectionInfo
                  ? getConnectionInfo()
                  : informInvalidConnectionListener(connectionInfo, null);   
         }
         //user did not want continue, return;
         if (connectionInfo == null)
            return;
         //Try connecting with updated connection info
         ex = null;
         // Create the connections and requester
         try
         {
            createDesignerConnection(connectionInfo);
            m_userConfig = PSUserConfig.createConfig(PSProxyUtils
               .getObjectStore(true));
            createWebServicesConnection(connectionInfo);
            createRemoteRequester(connectionInfo);
         }
         catch (PSServerException e)
         {
            ex = e;
         }
         catch (PSAuthorizationException e)
         {
            ex = e;
         }
         catch (PSAuthenticationFailedException e)
         {
            ex = e;
         }
         catch (PSNotAuthenticatedFault e)
         {
            ex = e;
         }
         catch (PSContractViolationFault e)
         {
            ex = e;
         }
         catch (RemoteException e)
         {
            ex = e;
         }
         catch (ServiceException e)
         {
            ex = e;
         }
         catch (PSModelException e)
         {
            ex = e;
         }

         if (ex != null)
         {
            connectionInfo = informInvalidConnectionListener(connectionInfo, ex);
         }
      }
      while (ex != null && connectionInfo != null);
      
      if(connectionInfo == null)
      {
         throw new PSUninitializedConnectionException(
            "Connection to server is cancelled");
      }
      
      m_connectionInfo  = connectionInfo;
      
      m_lockHelper = new PSLockHelper(m_clientSessionId, m_connectionInfo
         .getUserid());
      
      informListeners(PSLogonStateChangedEvent.LogonStateEvents.LOGON);
      
      startLockExtender();
   }
   
   /**
    * Reconnect with webservices. This does nothing if we never connected
    * before this call.
    * 
    * @throws Exception for any error.
    */
   public void reconnect() throws Exception
   {
      PSConnectionInfo connection = getConnectionInfo();
      if (connection != null && m_webServicesConnection != null)
      {
         m_webServicesConnection.login();
      }
   }

   /**
    * Starts the lock extendertask. Stops if already running and restarts.
    */
   private void startLockExtender()
   {
      //Stop if already running
      if(m_lockExtender!=null)
      {
         m_lockExtender.cancel();
         m_timer.purge();
      }
      m_lockExtender = new LockExtender();
      // Server lock time is 30 minutes which is 1800000 ms. We take 25 minutes
      // as initial delay and period after.
      int interval = 1500000;
      m_timer.schedule(m_lockExtender, interval, interval);
   }

   /**
    * Get the error message from the fault
    * 
    * @param error the error object from which to try to get the fault
    *    message, may be <code>null</code> in which case <code>null</code>
    *    is returned.
    * @return the error message may be <code>null</code> or empty.
    */
   public String getFaultMessage(Object error)
   {
      if (error == null)
         return null;

      if (error instanceof Error)
      {
         Error err = (Error) error;
         return err.getErrorMessage();
      }else if(error instanceof PSServerException){
         PSServerException exc = (PSServerException) error;
         PSException.setErrorManager(new PSErrorManagerDefaultImpl());
         if(IPSConnectionErrors.UNAUTHORIZED == exc.getErrorCode()){
            return "Authentication failed. Invalid User Name and/or Password.";
         }else{
            return null;
         }
      }
      String msg = ((Throwable) error).toString();
      if (msg.trim().length() > 0)
         return msg;
      return null;
   }

   /**
    * Helper method to parse the error message looking for specific token. The
    * INVALID_LOCALE fault will be piggy backed with a list of valid locales.
    * @param error the fault generated due to INVALID_LOCALE fault, never
    *           <code>null</code>
    * @return list of valid locales, may be <code>null</code>
    */
   private List<String> extractLocalesFromFault(Object error)
   {
      String errMsg = null;
      errMsg = getFaultMessage(error);
      
      if (StringUtils.isBlank(errMsg))
         return null;
      if ( error instanceof PSContractViolationFault &&
           errMsg.contains(PS_VALID_LOCALES))
      {
         String locStr = StringUtils.substringBetween(errMsg, "<", ">").trim();
         String[] lArray = StringUtils.splitByWholeSeparator(locStr, null);
         if (lArray != null)
            return Arrays.asList(lArray);
      }
      return null;
   }

   

   /**
    * Inform the registered listener about a wrong web services connection and
    * associated exception to get an updated connection info in return.
    * 
    * @param connectionInfo connection info object used to logon to server and
    * failed, assumed not <code>null</code>.
    * @param ex Exception probably thrown by server when attempted to logon,
    * assumed not <code>null</code>.
    * @return the corrected connection information object, may be
    * <code>null</code>.
    */
   private PSConnectionInfo informInvalidConnectionListener(
      PSConnectionInfo connectionInfo, Exception ex)
   {
      m_connecting = false;
      if (m_useTestConnectionInfo)
      {
         ms_log.error("Connection could not be established with "
            + "test credentials. Correct the test connection "
            + "information and try again.");
         return null;
      }

      if (m_InvalidConnectionListener == null)
      {
         ms_log.error("An Invalid Connection listener "
            + "must be registered with core fatcory");
         return null;
      }
      
      return m_InvalidConnectionListener.correctConnection(connectionInfo, ex,
            extractLocalesFromFault(ex));
   }

   /**
    * Indicates if the core factory is operating in test mode. Test mode
    * means there are NO server calls and all data is provided from local
    * repositories (files) and/or hard-coded (for read-only data.)
    * 
    * @return <code>true</code> if all data is coming from the local
    * environment.
    */
   public boolean isLocalMode()
   {
      return m_useTestProxies;
   }
   
   /**
    * This flag indicates whether the connection info object was initialized
    * using test credentials. This could later be overridden by a call to 
    * 
    * @return <code>true</code> means that connection info from a file will be
    * used if none has been supplied.
    */
   public boolean isTestConnectionInfo()
   {
      return m_useTestConnectionInfo;
   }
   
   /**
    * Clears the locally cached instances of any models and proxies. This is
    * only provided for testing purposes, not to be used by clients.
    */
   public void clearModelProxyCache()
   {
      m_models.clear();
      m_proxies.clear();
   }

   /**
    * Ceates a new webservices connection by logging on to the server with details
    * in the connection info object supplied.
    * 
    * @param connectionInfo connection information required to logon to a
    * server, assume dnot <code>null</code>.
    * 
    * @see PSWebServicesConnection#PSWebServicesConnection(PSConnectionInfo)
    * 
    * @throws ServiceException
    * @throws RemoteException
    * @throws PSContractViolationFault
    * @throws PSNotAuthenticatedFault
    */
   private void createWebServicesConnection(PSConnectionInfo connectionInfo)
      throws PSNotAuthenticatedFault, PSContractViolationFault,
      RemoteException, ServiceException
   {
      m_webServicesConnection = new PSWebServicesConnection(connectionInfo);
   }

   /**
    * Creates a designer connection
    * 
    * @param connectionInfo connection information to create the connection,
    * assuemd not <code>null</code>.
    * 
    * @throws PSAuthorizationException in case user specified in the connection
    * info does not have adeqaute permission to logon to the server.
    * @throws PSAuthenticationFailedException in case the credentials are not
    * correct.
    * @throws PSServerException in case of any other error while creating the
    * connection.
    * @throws PSModelException If the server version is not compatible with the
    * client version.
    */
   private void createDesignerConnection(PSConnectionInfo connectionInfo)
      throws PSServerException, PSAuthorizationException,
      PSAuthenticationFailedException, PSModelException
   {
      PSProperties connProps = new PSProperties();
      connProps.setProperty(PSDesignerConnection.PROPERTY_HOST, connectionInfo
         .getServer());
      connProps.setProperty(PSDesignerConnection.PROPERTY_LOGIN_ID,
         connectionInfo.getUserid());
      connProps.setProperty(PSDesignerConnection.PROPERTY_LOGIN_PW,
         connectionInfo.getClearTextPassword());
      connProps.setProperty(PSDesignerConnection.PROPERTY_PORT, connectionInfo
         .getPort()
         + StringUtils.EMPTY);

      /**
       * use the locale information from the connection 
       */
      connProps.setProperty(PSDesignerConnection.PROPERTY_LOCALE,
            connectionInfo.getLocale());

      if (connectionInfo.isUseSsl())
         connProps.setProperty(PSDesignerConnection.PROPERTY_PROTOCOL, "https");
      m_designerConnection = new PSDesignerConnection(connProps);
      
      PSFormatVersion serverVersion =
         m_designerConnection.getServerVersion();
      PSFormatVersion clientVersion =
         new PSFormatVersion("com.percussion.util");
//      Removing to allow cougar 1.0.0 connections
//      if (serverVersion == null ||
//         !m_designerConnection.checkVersionCompatibility(clientVersion))
//      {
//         throw new PSModelException(PSErrorCodes.SERVER_VERSION_INVALID, 
//            serverVersion == null ? "unknown" : 
//               serverVersion.getVersionString());
//      }
   }

   /**
    * Creates a remote requester object
    * 
    * @param connectionInfo connection information to create the connection,
    * assuemd not <code>null</code>.
    */
   private void createRemoteRequester(PSConnectionInfo connectionInfo)
   {
      PSProperties connProps = new PSProperties();
      connProps.setProperty(PSDesignerConnection.PROPERTY_HOST, connectionInfo
         .getServer());
      connProps.setProperty(PSDesignerConnection.PROPERTY_LOGIN_ID,
         connectionInfo.getUserid());
      connProps.setProperty(PSDesignerConnection.PROPERTY_LOGIN_PW,
         connectionInfo.getClearTextPassword());
      connProps.setProperty(PSDesignerConnection.PROPERTY_PORT, connectionInfo
         .getPort()
         + StringUtils.EMPTY);

      connProps.setProperty(PSDesignerConnection.PROPERTY_LOCALE,
         PSI18nUtils.DEFAULT_LANG);

      connProps.setProperty("useSSL", connectionInfo.isUseSsl() ? Boolean.TRUE //$NON-NLS-1$
         .toString() : Boolean.FALSE.toString());

      m_remoteRequester = new PSRemoteRequester(connProps);
      m_remoteRequester.setRequestTimeout(connectionInfo.getTimeout());
   }

   /**
    * Logs off the currect user and closes current session with a proper
    * cleanup. {@link #flushAll()} is called to flush the cache.
    * <p>
    * A notification is sent to all registered clients.
    */
   public void logoff()
   {
      try
      {
         if (m_designerConnection != null)
         {
            if (m_userConfig != null)
            {
               m_userConfig.flush();
               m_userConfig = null;
            }
            m_designerConnection.close();
         }
      }
      catch (Exception e)
      {
         // do nothing
      }
      
      /*
       * Make it null even during an exception. Must also flush the current
       * object store to make sure a new one gets created with the new 
       * connection.
       */ 
      m_designerConnection = null;
      PSProxyUtils.flushObjectStore();
      m_remoteRequester = null;

      if (m_webServicesConnection != null)
      {
         m_webServicesConnection.close();
         m_webServicesConnection = null;
      }
      m_connectionInfo = null;
      
      flushAll();
      
      /*
       * Inform listeners about the logon state change. This must be the last
       * step because listeners may clean up data still needed with previous 
       * steps.
       */
      informListeners(PSLogonStateChangedEvent.LogonStateEvents.LOGOFF);
   }
   
   /**
    * If we're doing a test, then there will be fake connection information
    * present. If so, inform the logon listeners.
    */
   public void finishTestingSetup()
   {
      if (m_testConnectionInfo != null)
      {
         informListeners(LogonStateEvents.LOGON);
      }
   }

   /**
    * Inform all registered listeners the appropriate logon state change based
    * on the supplied event type.
    * 
    * @param eType event type, must be one of type
    * {@link PSLogonStateChangedEvent.LogonStateEvents}. 
    */
   private void informListeners(PSLogonStateChangedEvent.LogonStateEvents eType)
   {
      PSLogonStateChangedEvent event = new PSLogonStateChangedEvent(eType);
      for (IPSCoreListener listener : 
         new HashSet<IPSCoreListener>(m_coreListeners))
      {
         listener.logonStateChanged(event);
      }
   }

   /**
    * Access method for designer connection that was created and authenticated
    * during logon request.This is used with proxies that provide services for
    * legacy objects.
    * <p>
    * <code>null</code> until {@link #logon(PSConnectionInfo)} called and
    * authentication was successful. A call to {@link #logoff()} will set it to
    * <code>null</code> again.
    * <p>
    * If {@link #isLocalMode()} returns <code>true</code>, then this method 
    * will always return <code>null</code>. In that case, the client should
    * supply their own 'test' data to simulate the behavior of making a request
    * to the server.
    * 
    * @return may be <code>null</code>
    */
   public PSDesignerConnection getDesignerConnection()
   {
      if(isLocalMode())
         return null;
      if (m_designerConnection == null)
         logon(null);
      return m_designerConnection;
   }
   
   /**
    * Indicates whether the core factory is connected to a server.
    */
   public boolean isConnected()
   {
      return isLocalMode()
            || (m_remoteRequester != null && m_designerConnection != null);
   }

   /**
    * For every model that is currently loaded, its
    * {@link IPSCmsModel#getLockedRefs()} method is called and the results are
    * aggregated and returned.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public Collection<IPSReference> getLockedRefs()
   {
      Collection<IPSReference> results = new ArrayList<IPSReference>();
      for (IPSCmsModel model : m_models.values())
      {
         results.addAll(model.getLockedRefs());
      }
      return results;
   }

   /**
    * Access method for the remote requester that was created and authenticated
    * during logon request.This is used with components that require a remote
    * requester
    * <p>
    * <code>null</code> until {@link #logon(PSConnectionInfo)} called and
    * authentication was successful. A call to {@link #logoff()} will set it to
    * <code>null</code> again.
    * <p>
    * If {@link #isLocalMode()} returns <code>true</code>, then this method 
    * will always return <code>null</code>. In that case, the client should
    * supply their own 'test' data to simulate the behavior of making a request
    * to the server.
    * 
    * @return may be <code>null</code>
    */
   public PSRemoteRequester getRemoteRequester()
   {
      if (m_remoteRequester == null)
         logon(null);
      return m_remoteRequester;
   }

   /**
    * Get the web services connection that was created and authenticated during
    * logon. This is used with the proxies.
    * <p>
    * <code>null</code> until {@link #logon(PSConnectionInfo)} called, then
    * not changed until {@link #logoff()} called, at which time it is set to
    * <code>null</code>.
    * <p>
    * 
    * @return <code>null</code> if {@link #logon(PSConnectionInfo)} was never
    * called or successful.
    */
   public PSWebServicesConnection getWebServicesConnection()
   {
      if (m_webServicesConnection == null)
         logon(null);
      return m_webServicesConnection;
   }

   /**
    * Return reference to the connection info object used to logon to server
    * successfully. Returns test connection info in test mode.
    * <p>
    * This object is set after every successful logon to server. Reset to
    * <code>null</code> during logoff.
    * 
    * @return last used connection info object, may be <code>null</code> if
    * not logged on to server successfully.
    */
   public PSConnectionInfo getConnectionInfo()
   {
      if (m_useTestConnectionInfo)
         return m_testConnectionInfo;
      return m_connectionInfo;
   }

   /**
    * Register the supplied listener to receive notification for certain events
    * from all models. If any loaded model fires an event, it is sent to all
    * listeners. If a caller is only interested in a single model, they should
    * register with just that model and not the factory.
    * <p>
    * The listener can be later removed by calling
    * {@link #removeListener(IPSModelListener) removeListener}.
    * 
    * @param listener If the listener is not already registered, they will be
    * added, otherwise no action is taken. If <code>null</code>, returns
    * immediately.
    * 
    * @param notifications This value must be supplied to control for which
    * events you get notified. See {@link PSModelChangedEvent} for details.
    */
   synchronized public void addListener(IPSModelListener listener,
         int notifications)
   {
      if (null == listener || notifications == 0)
         return;

      m_listeners.put(listener, notifications);
      /*
       * add to all loaded models, as models are loaded, all registered
       * listeners will be added to them as well
       */
      Iterator<IPSCmsModel> models = m_models.values().iterator();
      while (models.hasNext())
      {
         models.next().addListener(listener, notifications);
      }
   }

   /**
    * Register the supplied listener to receive notification for Logon/logoff
    * events. An event is fired each time the core subsystem is requested to
    * login or logoff from a server. This does not fire events for each physical
    * request, just each time {@link #logon(PSConnectionInfo) logon} or
    * {@link #logoff()} is called and executed successfully.
    * <p>
    * The listener can be later removed by calling
    * {@link #removeListener(IPSCoreListener) removeListener}.
    * 
    * @param listener If the listener is not already registered, they will be
    * added, otherwise no action is taken. If <code>null</code>, returns
    * immediately.
    */
   synchronized public void addListener(IPSCoreListener listener)
   {
      if (null == listener)
         return;

      m_coreListeners.add(listener);
   }

   /**
    * Removes a listener previously registered with
    * {@link #addListener(IPSModelListener, int) addListener}.
    * 
    * @param listener If the listener is currently registered, it is removed.
    * If <code>null</code>, returns immediately.
    * 
    * @return <code>true</code> if the listener had been registered,
    * <code>false</code> otherwise.
    */
   synchronized public boolean removeListener(IPSModelListener listener)
   {
      if (null == listener)
         return false;

      Integer notifications = m_listeners.remove(listener);
      if (notifications == null)
         return false;

      if (notifications.intValue() != 0)
      {
         Iterator<IPSCmsModel> models = m_models.values().iterator();
         while (models.hasNext())
         {
            models.next().removeListener(listener);
         }
      }
      return true;
   }

   /**
    * Removes a listener previously registered with
    * {@link #addListener(IPSCoreListener) addListener}.
    * 
    * @param listener If the listener is currently registered, it is removed. If
    * <code>null</code>, returns immediately.
    * 
    * @return <code>true</code> if the listener had been registered,
    * <code>false</code> otherwise.
    */
   synchronized public boolean removeListener(IPSCoreListener listener)
   {
      if (null == listener)
         return false;

      return m_coreListeners.remove(listener);
   }
   
   /**
    * Initialize the workbench spring context
    */
   private void initSpringContext()
   {
      try
      {
         PSBaseServiceLocator.initCtx(new String[] { getSpringConfig()
               .getAbsolutePath() });
      }
      catch (PSMissingBeanConfigurationException e)
      {
         throw new RuntimeException(e);
      }
      catch(BeanDefinitionStoreException e)
      {
         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (Throwable ignore)
      {
         ms_log.warn("Not running plugin, Spring context will not be loaded.");
      }
   }

   /**
    * Attempts to find the workbench spring configuration file and return a
    * handle to it. It checks in the following places, stopping when it finds a 
    * file named workbench-beans.xml.
    * <ol>
    *    <li>.</li>
    *    <li>./springConfigs</li>
    *    <li>in this plugin's directory</li>
    * <ol>
    *
    * 
    * @return Never <code>null</code>.
    * 
    * @throws IOException
    */
   private File getSpringConfig() throws IOException
   {
      //first, look in the current directory, support for unit tests
      {
         final File file = new File("./workbench-beans.xml");
         ms_log.info("Looking for spring configuration: "
               + file.getAbsolutePath());
         if (file.exists())
         {
            return file;
         }
      }
      
      //second, look in the springConfigs subdirectory
      // (expected to be used by Unit tests)
      {
         final File file = new File("./springConfigs/workbench-beans.xml");
         ms_log.info("Looking for spring configuration: "
               + file.getAbsolutePath());
         if (file.exists())
         {
            return file;
         }
      }

      //next try the plugin
      ms_log.info("Looking for spring configuration in the plugin.");
      final Path path = new Path("springConfigs");
      final URL url = Platform.find(
         Platform.getBundle("com.percussion.client"), path);
      final URL localUrl = Platform.asLocalURL(url);
      return  new File(localUrl.getFile(), "workbench-beans.xml");
   }

   /**
    * Register the invalid connection listener. The listener registered here is
    * notified if logon fails because of bad connetcion. Replaces the previously
    * set one.
    * 
    * @param listener listener to register, must not be <code>null</code>.
    */
   synchronized public void setInvalidConnectionListener(
      IPSinvalidConnectionListener listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("listener must not be null"); //$NON-NLS-1$
      }
      m_InvalidConnectionListener = listener;
   }

   /**
    * See {@link #setInvalidConnectionListener(IPSinvalidConnectionListener)}.
    * 
    * @return The last value supplied to the referenced setter, or
    * <code>null</code> if it has never been called.
    */
   public IPSinvalidConnectionListener getInvalidConnectionListener()
   {
      return m_InvalidConnectionListener;
   }

   /**
    * Scan the set of model infos and return the one whose type matches the
    * requested type.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @param throwEx If <code>true</code> and an info is not found, an
    * exception is thrown.
    * 
    * @return The matching info, if found. Otherwise, if <code>throwEx</code>
    * is code>true</code>, an exception is thrown, else, <code>null</code>
    * is returned.
    * 
    * @throws PSModelException If no model by the requested type is registered.
    */
   private ModelInfo findModel(Enum type, boolean throwEx)
         throws PSModelException
   {
      Iterator<ModelInfo> models = ms_modelInfo.iterator();
      while (models.hasNext())
      {
         ModelInfo info = models.next();
         if (info.m_primaryType == type)
            return info;
      }
      if (throwEx)
         throw new PSModelException(PSErrorCodes.UNKOWN_MODEL, new Object[]
         {type.toString()});
      return null;
   }

   /**
    * Made private to implement singleton pattern.
    */
   private PSCoreFactory() {
   }

   /**
    * Get the user configuration object that was constructed during logon.
    * 
    * @return user config object never <code>null</code> if logged in
    * successfully.
    */
   public PSUserConfig getUserConfig()
   {
      return m_userConfig;
   }

   /**
    * The single instance of this class. <code>null</code> until
    * {@link #getInstance()} called the first time, then never <code>null
    * </code>.
    */
   private static PSCoreFactory ms_instance;

   /**
    * Contains all loaded models. The key is the type of the object while the
    * value is the associated model. Models are loaded lazily. Never
    * <code>null</code>.
    */
   private Map<Enum, IPSCmsModel> m_models = new HashMap<Enum, IPSCmsModel>();

   /**
    * Initialized at construction. Each key is the pre-defined name of the model
    * and the value is the primary type it manages.
    */
   private Map<String, Enum> m_modelNameToType = new HashMap<String, Enum>();

   /**
    * Contains all loaded proxies. The key is the primary type and the value is
    * the proxy class instance for that type. Lazily populated as needed. Never
    * <code>null</code>.
    */
   private Map<Enum, IPSCmsModelProxy> m_proxies = new HashMap<Enum, IPSCmsModelProxy>();

   /**
    * Contains all loaded hierarchy proxies. The key is the primary type and the
    * value is the proxy class instance for that type. Lazily populated as
    * needed. Never <code>null</code>.
    */
   private Map<Enum, IPSHierarchyModelProxy> m_hierarchyProxies = 
      new HashMap<Enum, IPSHierarchyModelProxy>();

   /**
    * Stores all the notification listeners for logon/logoff events. 
    * <p>
    * Never <code>null</code>, may be empty.
    */
   private Set<IPSCoreListener> m_coreListeners = new HashSet<IPSCoreListener>();

   /**
    * Stores all the notifications desired by the listener for model events. We
    * need to keep these as each listener must be added to the models as they
    * are lazily loaded.
    * <p>
    * The value is the set of notification flags from the
    * {@link PSModelChangedEvent} class.
    * <p>
    * Never <code>null</code>, may be empty.
    */
   private Map<IPSModelListener, Integer> m_listeners = 
      new HashMap<IPSModelListener, Integer>();

   /**
    * Stores the only invalid connection listener to report that the connection
    * failed because of invalid connection info. If the system is using test
    * credentials and those are bad to establish a connection, system will log
    * an error message to that extent and stop proceeding further.
    * <p>
    * Normally not <code>null</code> sinve the plugin always registers as
    * listener. But it is possible that main program might not have registered a
    * listener in which case it is <code>null</code>. For example UI less
    * application for example, framework test app may not register since they
    * have no way correcting the credentials in the fly. Such programs must make
    * sure that they supply correct credentials in the first place.
    */
   private IPSinvalidConnectionListener m_InvalidConnectionListener = null;

   /**
    * This flag controls how the {@link #getCmsModelProxy(Enum)} class works. If
    * <code>true</code>, then the test version of the proxy is used,
    * otherwise, the standard version is used.
    */
   private boolean m_useTestProxies = false;

   /**
    * A small container for basic information that defines a model. All members
    * are public for direct access. A ctor is supplied to save setting multiple
    * fields and to enforce 1 small rule for descriptions.
    * 
    * @author paulhoward
    */
   private static class ModelInfo
   {
      /**
       * The official name of the model. This name can be used to retrieve the
       * model. Never <code>null</code> or empty after construction.
       */
      public String m_name;

      /**
       * An optional message describing what the model does. Never
       * <code>null</code>, may be empty.
       */
      public String m_description = StringUtils.EMPTY;

      /**
       * The primary type of the model. Each model has a primary type and 0 or
       * more sub-types. Never <code>null</code> after construction.
       * <p>
       * This object implements the {@link IPSPrimaryObjectType} interface.
       */
      public Enum m_primaryType;

      /**
       * The class that implements the {@link IPSCmsModelProxy} interface. Never
       * <code>null</code> or empty after construction.
       */
      public String m_proxyClassName;

      /**
       * The class that implements the {@link IPSHierarchyModelProxy} interface.
       * Never <code>null</code> or empty after construction.
       */
      public String m_hierarchyProxyClassName;

      /**
       * The class that implements the {@link IPSCmsModel} interface. Defaults
       * to PSCmsModel and never <code>null</code> or empty after
       * construction.
       */
      public String m_modelClassName = CMS_MODEL_CLASSNAME;
      
      /**
       * The default Model class name
       */
      public static final String CMS_MODEL_CLASSNAME = 
         "com.percussion.client.models.impl.PSCmsModel"; //$NON-NLS-1$
      /**
       * Simple ctor to ease construction.
       * 
       * @param name A unique name (among all models) for this model. Never
       *           <code>null</code> or empty.
       * @param desc May be <code>null</code> or empty.
       * @param type The primary type supported by this model. Assumed not
       *           <code>null</code>. Must implement the
       *           {@link IPSPrimaryObjectType} interface.
       * @param proxyClassName Never <code>null</code> or empty. No attempt is
       *           made to load the class until first requested.
       * @param hierarchyProxyClassName May be <code>null</code>, never
       *           empty. No attempt is made to load the class until first
       *           requested.
       */
      public ModelInfo(String name, String desc, Enum type,
            String proxyClassName, String hierarchyProxyClassName,
            String modelClassName) {
         if (null == name || name.trim().length() == 0)
         {
            throw new IllegalArgumentException("name cannot be null or empty");
         }
         if (null == proxyClassName || proxyClassName.trim().length() == 0)
         {
            throw new IllegalArgumentException(
                  "proxyClassName cannot be null or empty");
         }
         if (null != hierarchyProxyClassName
               && hierarchyProxyClassName.trim().length() == 0)
         {
            throw new IllegalArgumentException(
                  "hierarchyProxyClassName cannot be null or empty");
         }
         if (!(type instanceof IPSPrimaryObjectType))
         {
            throw new IllegalArgumentException(
                  "type must implement IPSPrimaryObjectType interface");
         }

         m_name = name;
         if (desc == null)
            desc = "";
         m_description = desc;
         m_primaryType = type;
         m_proxyClassName = proxyClassName;
         m_hierarchyProxyClassName = hierarchyProxyClassName;
         if (!StringUtils.isBlank(modelClassName))
            m_modelClassName = modelClassName;
      }
   }  
   
   /**
    * Get the client Session Id string. This is used while logging in to the
    * server which uses this as a key for locking and unlocking the design
    * objects. Set by the client before logging on to the server.
    * 
    * @return client sessionid, never <code>null</code> or empty. If never set,
    * a default value is used.
    */
   public String getClientSessionId()
   {
      return m_clientSessionId;
   }

   /**
    * Set the client sessionid
    * 
    * @param clientSessionId session id to set, may be <code>null</code>. If 
    * <code>null</code>, the default value is used.
    * @see #getClientSessionId()
    */
   public void setClientSessionId(String clientSessionId)
   {
      if (StringUtils.isBlank(clientSessionId))
         m_clientSessionId = DEFAULT_SESSION_ID;
      else
         m_clientSessionId = clientSessionId;
   }
   
   /**
    * Easy access method to get the currect user as {@link Principal}. Throws
    * runtime exception if no user logged in yet.
    * 
    * @return currently logged-in user as principal, never <code>null</code>
    */
   public IPSTypedPrincipal getUserPrincipal()
   {
      if (getConnectionInfo() == null)
      {
         throw new RuntimeException(
            "No user logged in yet. System cannot function");
      }
      return new PSTypedPrincipal(getConnectionInfo().getUserid(),
         PrincipalTypes.USER);
   }
   
   /**
    * @return lock helper that wolud have initilized just after a successful log
    * on to the server. May be <code>null</code> if user never logged on to
    * server successfully.
    */
   public PSLockHelper getLockHelper()
   {
      return m_lockHelper;
   }

   /**
    * Returns <code>true</code> when the code is busy trying to connect to
    * a server.
    */
   public boolean isConnecting()
   {
      return m_connecting;
   }

   /**
    * Contains all of the information for the models. This information is used
    * to build the maps. In the future, this information could be moved to the
    * plugin. xml to provide a more flexible way of enhancing the product.
    * <p>
    * Initialized in a static block.
    */
   private static Set<ModelInfo> ms_modelInfo = new HashSet<ModelInfo>();

   static
   {
      // initialize all models - may move to declarative model someday
      ms_modelInfo.add(new ModelInfo(
            "Auto translations model",
            "Manages all translation configurations as a set.",
            PSObjectTypes.AUTO_TRANSLATION_SET,
            "com.percussion.client.proxies.impl.PSAutoTranslationSetModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo("Content type model",
            "Manages content type definitions.",
            PSObjectTypes.CONTENT_TYPE,
            "com.percussion.client.proxies.impl.PSContentTypeModelProxy",
            null,
            "com.percussion.client.models.impl.PSContentTypeModel"));

      ms_modelInfo.add(new ModelInfo("Site model",
            "Manages sites.",
            PSObjectTypes.SITE,
            "com.percussion.client.proxies.impl.PSSiteModelProxy",
            null,
            null));

      ms_modelInfo.add(new ModelInfo(
            "Resource files model",
            "Manages files and folders visible via URL requests that are"
                  + " not associated with other resources such as querys.",
            PSObjectTypes.RESOURCE_FILE,
            "com.percussion.client.proxies.impl.PSResourceFileModelProxy",
            "com.percussion.client.proxies.impl.PSResourceFileHierarchyModelProxy", 
            null));

      ms_modelInfo.add(new ModelInfo("Locale model",
            "Manages locale definitions.", PSObjectTypes.LOCALE,
            "com.percussion.client.proxies.impl.PSLocaleModelProxy", 
            null, null));

      ms_modelInfo.add(new ModelInfo("Keyword category model",
            "Manages keyword categories and entries.", PSObjectTypes.KEYWORD,
            "com.percussion.client.proxies.impl.PSKeywordModelProxy", 
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "Shared fields model",
            "Manages all content type shared fields and the files that contain"
                  + " them.",
            PSObjectTypes.SHARED_FIELDS,
            "com.percussion.client.proxies.impl.PSSharedFieldsModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "Content types system definitions model",
            "Manages the ContentEditorSystemDef file",
            PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG,
            "com.percussion.client.proxies.impl.PSContentTypeSystemConfigModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo("Template model",
            "Manages v6 templates and v5 variants.", PSObjectTypes.TEMPLATE,
            "com.percussion.client.proxies.impl.PSTemplateModelProxy",
            null,
            "com.percussion.client.models.impl.PSTemplateModel"));

      ms_modelInfo.add(new ModelInfo("Workflow names model",
            "Only provides cataloging of workflows. No CRUD operations are"
                  + " supported.", PSObjectTypes.WORKFLOW,
            "com.percussion.client.proxies.impl.PSWorkflowModelProxy", 
            null, "com.percussion.client.models.impl.PSWorkflowModel"));

      ms_modelInfo.add(new ModelInfo("Slot model", "Manages template slots.",
            PSObjectTypes.SLOT,
            "com.percussion.client.proxies.impl.PSSlotModelProxy", 
            null, 
            null));

      ms_modelInfo.add(new ModelInfo("Community model",
            "Manages community objects, but not community visibility.",
            PSObjectTypes.COMMUNITY,
            "com.percussion.client.proxies.impl.PSCommunityModelProxy", 
            null, "com.percussion.client.models.impl.PSCommunityModel"));

      ms_modelInfo.add(new ModelInfo("Role model",
            "Provides a role catalog only. No other operations are supported.",
            PSObjectTypes.ROLE,
            "com.percussion.client.proxies.impl.PSRoleModelProxy", 
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "UI action menus model",
            "Manages the menus used mainly by the CX.",
            PSObjectTypes.UI_ACTION_MENU,
            "com.percussion.client.proxies.impl.PSUiMenuActionModelProxy",
            null, null));
      
      ms_modelInfo.add(new ModelInfo(
            "UI action menus misc model",
            "Catalog only model for retrieving misc items needed by the action menu gui",
            PSObjectTypes.UI_ACTION_MENU_MISC,
            "com.percussion.client.proxies.impl.PSUiMenuActionMiscModelProxy",
            null,null));

      ms_modelInfo.add(new ModelInfo("UI views model",
            "Manages the views used mainly by the CX.", PSObjectTypes.UI_VIEW,
            "com.percussion.client.proxies.impl.PSUiViewModelProxy", 
            null, null));

      ms_modelInfo.add(new ModelInfo("UI searches model",
            "Manages the searches used mainly by the CX.",
            PSObjectTypes.UI_SEARCH,
            "com.percussion.client.proxies.impl.PSUiSearchModelProxy", 
            null, null));

      ms_modelInfo.add(new ModelInfo("UI display formats model",
            "Manages the display formats used mainly by the CX.",
            PSObjectTypes.UI_DISPLAY_FORMAT,
            "com.percussion.client.proxies.impl.PSUiDisplayFormatModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "Content editor controls model",
            "Manages the file that contains the control definitions used by"
                  + " the content editor.",
            PSObjectTypes.CONTENT_EDITOR_CONTROLS,
            "com.percussion.client.proxies.impl.PSContentEditorControlModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "Relationship types model",
            "Manages the relationship configurations that control item linkage.",
            PSObjectTypes.RELATIONSHIP_TYPE,
            "com.percussion.client.proxies.impl.PSRelationshipTypeModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "Configuration files model",
            "Manages server configuration files which don't have a custom editor.",
            PSObjectTypes.CONFIGURATION_FILE,
            "com.percussion.client.proxies.impl.PSConfigurationFileModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo("Xml applications model",
            "Manages old-style xml applications.",
            PSObjectTypes.XML_APPLICATION,
            "com.percussion.client.proxies.impl.PSXmlApplicationModelProxy",
            null,
            "com.percussion.client.models.impl.PSXmlApplicationModel"));

      ms_modelInfo.add(new ModelInfo(
            "Xml application files model",
            "Manages all files and folders associated with an old-style"
                  + " application.",
            PSObjectTypes.XML_APPLICATION_FILE,
            "com.percussion.client.proxies.impl.PSXmlApplicationFileModelProxy",
            "com.percussion.client.proxies.impl.PSXmlApplicationFileHierarchyModelProxy", 
            null));

      ms_modelInfo.add(new ModelInfo(
            "Legacy configurations model",
            "Manages pre-v6 server configurations that are expected to become"
                  + " obsolete in the near future.",
            PSObjectTypes.LEGACY_CONFIGURATION,
            "com.percussion.client.proxies.impl.PSLegacyConfigurationModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "User files model",
            "Manages hierarchical objects that model a simple file system.",
            PSObjectTypes.USER_FILE,
            "com.percussion.client.proxies.impl.PSUserFileModelProxy",
            "com.percussion.client.proxies.impl.PSUserFileHierarchyModelProxy", 
            "com.percussion.client.models.impl.PSUserFileModel"));

      ms_modelInfo.add(new ModelInfo("Local files model",
            "Manages files as seen through the local file system.",
            PSObjectTypes.LOCAL_FILE,
            "com.percussion.client.proxies.impl.PSLocalFileSystemModelProxy", 
            "com.percussion.client.proxies.impl.PSLocalFileSystemHierarchyModelProxy", 
            "com.percussion.client.models.impl.PSLocalFileSystemModel"));

      ms_modelInfo.add(new ModelInfo("Database model",
            "Provides access to content of data sources.",
            PSObjectTypes.DB_TYPE,
            "com.percussion.client.proxies.impl.PSDatabaseTypeModelProxy", 
            "com.percussion.client.proxies.impl.PSDatabaseTypeHierarchyModelProxy",
            null));

      ms_modelInfo.add(new ModelInfo("Shared properties model",
            "Manages name/value pairs that are full-fledged design objects.",
            PSObjectTypes.SHARED_PROPERTY,
            "com.percussion.client.proxies.impl.PSSharedPropertyModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo("Extension definition model",
            "Manages Rhythmyx extensions.", PSObjectTypes.EXTENSION,
            "com.percussion.client.proxies.impl.PSExtensionModelProxy", 
            null,
            "com.percussion.client.models.impl.PSExtensionModel"));

      ms_modelInfo.add(new ModelInfo(
            "Content Editor Control definition model",
            "Manages Rhythmyx Content Editor controls.",
            PSObjectTypes.CONTENT_EDITOR_CONTROLS,
            "com.percussion.client.proxies.impl.PSContentEditorControlModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
            "Item Filter model",
            "Manages filters generally used for publishing.",
            PSObjectTypes.ITEM_FILTER,
            "com.percussion.client.proxies.impl.PSItemFilterModelProxy",
            null, null));

      ms_modelInfo.add(new ModelInfo(
         "Database cataloging model",
         "Catalogs datasources, categories and tables",
         PSObjectTypes.DB_TYPE,
         "com.percussion.client.proxies.impl.PSDatabaseTypeModelProxy",
         "com.percussion.client.proxies.impl.PSDatabaseTypeHierarchyModelProxy",
         null));

      ms_modelInfo.add(new ModelInfo("Item Filter model",
         "Manages item filters.", PSObjectTypes.ITEM_FILTER,
         "com.percussion.client.proxies.impl.PSItemFilterModelProxy", null,
         null));
   }

   /**
    * Reference to the web services connection. See
    * {@link #getWebServicesConnection()} fro more details.
    */
   private PSWebServicesConnection m_webServicesConnection = null;

   /**
    * Reference to the designer connection. See {@link #getDesignerConnection()}
    * fro more details.
    */
   private PSDesignerConnection m_designerConnection = null;

   /**
    * Reference to the remote requester. See {@link #getRemoteRequester()} for
    * more details.
    */
   private PSRemoteRequester m_remoteRequester;
   
   /**
    * See {@link #getConnectionInfo()}
    */
   private PSConnectionInfo m_connectionInfo = null;
   
   /**
    * @see #getUserConfig()
    */
   private PSUserConfig m_userConfig = null;

   /**
    * Never <code>null</code>.
    */
   private static final Logger ms_log = LogManager.getLogger(PSCoreFactory.class);

   /**
    * This flag indicates whether this factory was configured with test
    * credentials.
    */
   private boolean m_useTestConnectionInfo;

   /**
    * Test Connection info used to connect to server while running tests.
    * Initialized in the {@link #getInstance()}.
    */
   private PSConnectionInfo m_testConnectionInfo = null;

   private static final String DEFAULT_SESSION_ID = "default"; 
   
   /**
    * @see #getClientSessionId()
    * @see #setClientSessionId(String)
    */
   private String m_clientSessionId = "";
   
   /**
    * Instance of lock helper. This is expected to be instantiated every time
    * user logs on to server successfully.
    */
   private PSLockHelper m_lockHelper = null;
   
   /**
    * Lockextender timer task. Restarts during every logon.
    * @see #startLockExtender() 
    */
   private LockExtender m_lockExtender = null;
   
   /**
    * Timer for scheduled tasks. Right now only lock extender task is using
    * this.
    */
   private Timer m_timer = new Timer();

   /**
    * A constant (token) for parsing an error message. This is specifically
    * thrown by webservices during login() request at which time, the specified
    * locale was inactive. The generated soap fault piggy backs with valid
    * locales. ConnectionMgr can rethrow the login for user to select from the
    * list of valid locales
    */
   private static final String PS_VALID_LOCALES = "Valid locales:";

   /**
    * This class prevents objects unlocked because lock timeout. Makes an extend
    * lock request to the server for the objects well before locks time out.
    */
   public class LockExtender extends TimerTask
   {
      /**
       * Implement the interface method to do the core job.
       * 
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run()
      {
         SystemDesignSOAPStub binding;
         Exception ex = null;
         try
         {
            binding = PSProxyUtils.getSystemDesignStub();
            com.percussion.webservices.common.PSObjectSummary[] summaries = binding
               .getLockedSummaries();
            List<Long> objIds = new ArrayList<Long>();
            for (PSObjectSummary summary : summaries)
            {
               if (summary != null)
                  objIds.add(summary.getId());
            }
            long[] ids = new long[objIds.size()];
            for (int i = 0; i < objIds.size(); i++)
               ids[i] = objIds.get(i);
            if (ids.length > 0)
               binding.extendLocks(ids);

         }
         catch (MalformedURLException e)
         {
            ex = e;
         }
         catch (ServiceException e)
         {
            ex = e;
         }
         catch (PSContractViolationFault e)
         {
            ex = e;
         }
         catch (RemoteException e)
         {
            ex = e;
         }
         if (ex != null)
         {
            ms_log.error(ex);
         }
      }
   }
   
   /**
    * @see #isConnecting()
    */
   private boolean m_connecting;
}
