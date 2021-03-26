/******************************************************************************
 *
 * [ PSProxyUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies;

import com.percussion.client.IPSReference;
import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.error.PSClientException;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.PSLockException;
import com.percussion.client.proxies.IPSCmsModelProxy.METHOD;
import com.percussion.client.webservices.PSWebServicesConnection;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.assemblydesign.AssemblyDesignSOAPStub;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.faults.Error;
import com.percussion.webservices.faults.PSError;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCallError;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCallResult;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSErrorsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallError;
import com.percussion.webservices.faults.PSErrorsFaultServiceCallSuccess;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.rhythmyx.SystemLocator;
import com.percussion.webservices.rhythmyx.UiLocator;
import com.percussion.webservices.rhythmyxdesign.AssemblyDesignLocator;
import com.percussion.webservices.rhythmyxdesign.ContentDesignLocator;
import com.percussion.webservices.rhythmyxdesign.SecurityDesignLocator;
import com.percussion.webservices.rhythmyxdesign.SystemDesignLocator;
import com.percussion.webservices.rhythmyxdesign.UiDesignLocator;
import com.percussion.webservices.securitydesign.SecurityDesignSOAPStub;
import com.percussion.webservices.system.SystemSOAPStub;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import com.percussion.webservices.ui.UiSOAPStub;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.rpc.ServiceException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class that has static methods useful in proxies.
 */
public class PSProxyUtils
{
   /**
    * Convenience method that calls {@link #processAndThrowException(int, 
    * Throwable, Logger) processAndThrowException(-1, ex, log)}.
    */
   public static void processAndThrowException(Throwable ex, Logger log)
      throws PSModelException
   {
      try
      {
         processAndThrowException(-1, ex, log);
      }
      catch (PSMultiOperationException e)
      {
         // this will never happen
      }
   }

   /**
    * Logs the exception and throws an appropriately wrapped exception based on
    * the supplied exception or reference count. All webservice faults will
    * be converted into client exceptions.
    * 
    * @param refCount the number of references that were passed to the caller. 
    *    If this value is 1, the supplied exception is wrapped in a 
    *    <code>PSMultiOperationException</code> exception and thrown. Otherwise
    *    it is wrapped in a <code>PSModelException</code> and then thrown.
    * @param ex the exception that actually occurred, not <code>null</code>.
    * @param log the logger used to log the exception, not <code>null</code>.
    * @throws PSMultiOperationException if the supplidd exception is of type
    *    <code>PSMultiOperationException</code>, its results are processed to
    *    convert all webservice faults into client exceptions. Otherwise if
    *    the supplied reference count is 1 the provided exception is 
    *    wrapped into a <code>PSMultiOperationException</code> with webservice
    *    faults converted to client exceptions.
    * @throws PSModelException for all other situations, webservice faults
    *    converted to client exceptions.
    */
   public static void processAndThrowException(int refCount, Throwable ex, 
      Logger log) throws PSMultiOperationException, PSModelException
   {
      if (null == ex)
         throw new IllegalArgumentException("ex cannot be null");
      
      if (log == null)
         throw new IllegalArgumentException("log cannot be null");

      logError(log, ex);
      if (ex instanceof PSMultiOperationException)
      {
         Object[] results = ((PSMultiOperationException) ex).getResults();
         Object[] processedResults = new Object[results.length];
         for (int i = 0; i < results.length; i++)
            processedResults[i] = convertFault(results[i]);
         
         throw new PSMultiOperationException(processedResults,
               ((PSMultiOperationException) ex).getDetails());
      }
      if (refCount == 1)
      {
         Object[] results = new Object[1];
         results[0] = convertFault(ex);
         throw new PSMultiOperationException(results);
      }
      Throwable t = (Throwable) convertFault(ex);
      if (t instanceof PSModelException)
         throw (PSModelException) t;
      throw new PSModelException(t);
   }
   
   /**
    * If the supplied source is any type of webservice fault, it is converted
    * into a client exception and returned. Otherwise the source is returned 
    * as supplied.
    * 
    * @param source the source which may need to be converted, assumed not 
    *    <code>null</code>.
    * @return the converted fault or the source as it was provided, never 
    *    <code>null</code>.
    */
   private static Object convertFault(Object source)
   {
      if (source instanceof PSError)
         return PSProxyUtils.convertFault((PSError) source);
      else if (source instanceof PSLockFault)
      {
         return PSProxyUtils.convertFault((PSLockFault) source, 
            null, null, null);
      }
      else if (source instanceof Error)
      {
         return PSProxyUtils.convertFault((Error) source);
      }
      else if (source instanceof AxisFault)
      {
         PSModelException e = PSProxyUtils.convertFault((AxisFault) source);
         if (e != null)
            return e;
      }
      
      return source;
   }

   /**
    * Logs the supplied exception to the specifid logger.
    * 
    * @param log the logger to which to log the exception, not 
    *    <code>null</code>.
    * @param ex the exception to log, not <code>null</code>.
    */
   public static void logError(Logger log, Throwable ex)
   {
      if (log == null)
         throw new IllegalArgumentException("log cannot be null");

      if (null == ex)
         throw new IllegalArgumentException("ex cannot be null");  

      String msg = "";
      if (ex instanceof AxisFault)
      {
         msg = ((AxisFault) ex).toString();
         log.error(msg, ex);
      }
      else if (ex instanceof PSMultiOperationException)
      {
         Object[] results = ((PSMultiOperationException) ex).getResults();
         int count = 0;
         for (Object o : results)
         {
            if (o instanceof Throwable)
               count++;
         }
         
         log.error(MessageFormat.format("Multi-operation exception occurred: " +
            "{0} successes, {1} failures. Stack traces follow",
            results.length - count, count));
         for (Object o : results)
         {
            if (o instanceof Throwable)
            {
               if (o instanceof AxisFault)
               {
                  msg = ((AxisFault) o).toString();
                  log.error(msg, (AxisFault) o);
               }
               else
                  log.error(o);
            }
         }
      }
      else
         log.error(ex);
   }

   /**
    * Converts the supplied source object to the specified type.
    * 
    * @see PSTransformerFactory
    * @see com.percussion.webservices.transformation.converter.PSConverter
    * 
    * @param type the class type to which to convert the supplied source, must
    * not be <code>null</code>.
    * @param source the object to convert, bust not be <code>null</code>.
    * @return the transformed object, never <code>null</code>. todo - OK for
    * release - the throws declaration should be removed, but a lot of files
    * must be touched and I wasn't able to track down exactly how it works, so I
    * left it for now
    */
   @SuppressWarnings("unused")
   public static Object convert(Class type, Object source)
      throws PSTransformationException
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
      }
      if (source == null)
         throw new IllegalArgumentException("source cannot be null"); //$NON-NLS-1$

      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      Converter converter = factory.getConverter(type);
      return converter.convert(type, source);
   }

   /**
    * Set the rhythmyx session as header to the supplied binding. Clears all
    * existing headers before the new sessionheader is set.
    * 
    * @param binding the binding to which to add the rhythmyx session as header,
    * not <code>null</code>.
    */
   public static void setSessionHeader(Stub binding)
   {
      if (binding == null)
         throw new IllegalArgumentException("binding cannot be null");

      String session = PSCoreFactory.getInstance().getWebServicesConnection()
         .getLoginInfo().getSessionId();
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      binding.clearHeaders();
      binding.setHeader("urn:www.percussion.com/6.0.0/common", "session",
         session);
   }

   /**
    * Helper method to get the design version of the supplied id.
    * 
    * @param guid
    * @return designer long value of the guid.
    */
   public static long getDesignId(long guid)
   {
      return new PSDesignGuid(guid).getValue();
   }

   /**
    * Helper method to get the design version of the supplied guid.
    * 
    * @param guid
    * @return designer long value of the guid.
    */
   public static long getDesignId(IPSGuid guid)
   {
      return new PSDesignGuid(guid).getValue();
   }

   /**
    * Build and return assembly design service soap stub. The server connection
    * info is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return assembly design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public AssemblyDesignSOAPStub getAssemblyDesignStub()
      throws MalformedURLException, ServiceException
   {
      AssemblyDesignSOAPStub binding = (AssemblyDesignSOAPStub) new AssemblyDesignLocator()
         .getassemblyDesignSOAP(makeUrl(ASSEMBLY_DESIGN_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Build and return content design service soap stub. The server connection
    * info is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return content design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public ContentDesignSOAPStub getContentDesignStub()
      throws MalformedURLException, ServiceException
   {
      ContentDesignSOAPStub binding = (ContentDesignSOAPStub) new ContentDesignLocator()
         .getcontentDesignSOAP(makeUrl(CONTENT_DESIGN_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Build and return security design service soap stub. The server connection
    * info is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return security design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public SecurityDesignSOAPStub getSecurityDesignStub()
      throws MalformedURLException, ServiceException
   {
      SecurityDesignSOAPStub binding = (SecurityDesignSOAPStub) new SecurityDesignLocator()
         .getsecurityDesignSOAP(makeUrl(SECURITY_DESIGN_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Build and return system design service soap stub. The server connection
    * info is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return system design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public SystemDesignSOAPStub getSystemDesignStub()
      throws MalformedURLException, ServiceException
   {
      SystemDesignSOAPStub binding = (SystemDesignSOAPStub) new SystemDesignLocator()
         .getsystemDesignSOAP(makeUrl(SYSTEM_DESIGN_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Build and return system public service soap stub. The server connection
    * info is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return system design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public SystemSOAPStub getSystemPublicStub()
      throws MalformedURLException, ServiceException
   {
      SystemSOAPStub binding = (SystemSOAPStub) new SystemLocator()
         .getsystemSOAP(makeUrl(SYSTEM_PUBLIC_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Build and return ui design service soap stub. The server connection info
    * is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return ui design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public UiDesignSOAPStub getUiDesignStub()
      throws MalformedURLException, ServiceException
   {
      UiDesignSOAPStub binding = (UiDesignSOAPStub) new UiDesignLocator()
         .getuiDesignSOAP(makeUrl(UI_DESIGN_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Build and return ui design service soap stub. The server connection info
    * is taken from the connection info available
    * {@link PSCoreFactory#getConnectionInfo()} and the URL string is hardcoded.
    * 
    * @return ui design stub, never <code>null</code>
    * @throws MalformedURLException
    * @throws ServiceException
    */
   static public UiSOAPStub getUiStub() throws MalformedURLException,
      ServiceException
   {
      UiSOAPStub binding = (UiSOAPStub) new UiLocator()
         .getuiSOAP(makeUrl(UI_URL));
      setSessionHeader(binding);
      return binding;

   }

   /**
    * Make a {@link URL} with the supplied URL string and the connection info
    * obtained by {@link PSCoreFactory#getConnectionInfo()}.
    * 
    * @param urlString url string to make the server URL, assumed not
    * <code>null</code>.
    * @return URL object built, never <code>null</code>.
    * @throws MalformedURLException
    */
   static private URL makeUrl(String urlString) throws MalformedURLException
   {
      /*
       * Do not use PSCoreFactory.getInstance().getConnectionInfo() since it can
       * return invalid connection if not loggedon yet and it will not force to
       * logon.
       */
      PSWebServicesConnection wConn = PSCoreFactory.getInstance()
         .getWebServicesConnection();
      if(wConn==null)
         throw new PSUninitializedConnectionException("Please logon to server");

      PSConnectionInfo cInfo = wConn.getConnectionInfo();

      return new URL(cInfo.getProtocol(), cInfo.getServer(), cInfo.getPort(),
         urlString);
   }

   /**
    * Convenience method that calls
    * {@link #getObjectStore(boolean) getObjectStore(<code>false</code>)}.
    */
   static public PSObjectStore getObjectStore()
      throws PSUninitializedConnectionException
   {
      return getObjectStore(false);
   }

   /**
    * Get the object store. Will create one if not created yet.
    * 
    * @param flushCache If <code>true</code>, then the existing store is
    * discarded, if there is one. Otherwise, it may be returned from cache.
    * @return reference to only objectstore object, may be <code>null</code>.
    * @throws PSUninitializedConnectionException
    */
   static public PSObjectStore getObjectStore(boolean flushCache)
      throws PSUninitializedConnectionException
   {
      if (flushCache)
         m_objectStore = null;
      if (m_objectStore == null)
         m_objectStore = createObjectStore();

      return m_objectStore;
   }
   
   /**
    * Flushes the current object store to make sure a new one will be created
    * with the next call to (@link #getObjectStore()}.
    */
   static public void flushObjectStore()
   {
      m_objectStore = null;
   }

   /**
    * Create the object store. Create a local one for test mode and a remote one
    * otherwise.
    * 
    * @return never <code>null</code>.
    * 
    * @throws PSUninitializedConnectionException if creation of the remote
    * objectstore is attempted before the designer connection is initialized.
    */
   static private PSObjectStore createObjectStore()
      throws PSUninitializedConnectionException
   {
      if (PSCoreFactory.getInstance().isLocalMode())
      {
         return new PSLocalObjectStore();
      }
      if (PSCoreFactory.getInstance().getDesignerConnection() == null)
      {
         throw new PSUninitializedConnectionException(
            "Designer connection is not initialized yet"); //$NON-NLS-1$
      }
      return new PSObjectStore(PSCoreFactory.getInstance()
         .getDesignerConnection());
   }

   /**
    * Create a reference for content editor system definition object with hard
    * coded parameters.
    * 
    * @return reference to the only object, never <code>null</code>.
    */
   public static IPSReference getSystemDefReference()
   {

      PSReference ref = (PSReference) PSCoreUtils.createReference(
         SYSTEM_DEF_OBJECT_NAME, SYSTEM_DEF_OBJECT_NAME,
         SYSTEM_DEF_OBJECT_NAME, PSObjectTypeFactory
            .getType(PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG),
         new PSDesignGuid(PSTypeEnum.CONFIGURATION,
            PSContentEditorSystemDef.SYSTEM_DEF_ID));
      ref.setPersisted();
      //system def doesn't support acl, so fake it w/ full access
      ref.setPermissions(new int[] { PSPermissions.READ.getOrdinal(),
            PSPermissions.UPDATE.getOrdinal(),
            PSPermissions.DELETE.getOrdinal(),
            PSPermissions.OWNER.getOrdinal() });
      return ref;
   }

   /**
    * Helper method to copy permissions from
    * {@link PSObjectSummary summary objects} to {@link IPSReference reference}
    * objects. It does not assume one-to-one correspondence between each
    * reference in the collection and summary in the array however it assumed
    * every reference will have a corresponding summary (by id). If not found
    * the permissions are set to an empty array.
    * 
    * @param targetRefs must not be <code>null</code>. Each entry in the
    * collection is assumed to be castable to {@link PSReference}.
    * @param summaries must not be <code>null</code>.
    */
   static public void copyPermissions(List<IPSReference> targetRefs,
      PSObjectSummary[] summaries)
   {
      Map<Long, PSObjectSummary> actionMap = new HashMap<Long, PSObjectSummary>(
         summaries.length);
      for (PSObjectSummary summary : summaries)
         actionMap.put(summary.getId(), summary);
      for (IPSReference iRef : targetRefs)
      {
         PSReference ref = (PSReference) iRef;
         PSObjectSummary summary = actionMap.get(((PSDesignGuid) iRef.getId())
            .getValue());
         int[] permValues = new int[0];
         if (summary != null)
         {
            permValues = summary.getPermissions();
         }
         ref.setPermissions(permValues);
      }
   }

   /**
    * Helper method to set lockinfo for the supplied references.
    * 
    * @param refs reference array, must not be <code>null</code>.
    * @param empty <code>true</code> to clear lock info, <code>false</code>
    * to set it on each of the supplied refs.
    */
   static public void setLockInfo(IPSReference[] refs, boolean empty)
   {
      if (refs == null)
      {
         throw new IllegalArgumentException("refs must not be null");
      }
      if (refs.length == 0)
         return;
      String session = null;
      String userName = null;
      if (!empty)
      {
         session = PSCoreFactory.getInstance().getClientSessionId();
         userName = PSCoreFactory.getInstance().getConnectionInfo().getUserid();
      }
      for (IPSReference reference : refs)
      {
         ((PSReference) reference).setLockSessionId(session);
         ((PSReference) reference).setLockUserName(userName);
      }
   }

   /**
    * Hardcoded name for the system definition object.
    */
   private static final String SYSTEM_DEF_OBJECT_NAME = "contentEditorSystemDef";

   /**
    * Reference to the the object store. Initially <code>null</code> and
    * initialized upon first access. See {@link #getObjectStore()} for more
    * details.
    */
   private static PSObjectStore m_objectStore = null;

   /**
    * SOAP url for assembly design services.
    */
   public static final String ASSEMBLY_DESIGN_URL = "/Rhythmyx/designwebservices/assemblyDesignSOAP"; //$NON-NLS-1$

   /**
    * SOAP url for content design services.
    */
   public static final String CONTENT_DESIGN_URL = "/Rhythmyx/designwebservices/contentDesignSOAP"; //$NON-NLS-1$

   /**
    * SOAP url for security design services.
    */
   public static final String SECURITY_DESIGN_URL = "/Rhythmyx/designwebservices/securityDesignSOAP"; //$NON-NLS-1$

   /**
    * SOAP url for system design services.
    */
   public static final String SYSTEM_DESIGN_URL = "/Rhythmyx/webservices/systemDesignSOAP"; //$NON-NLS-1$

   /**
    * SOAP url for system public services.
    */
   public static final String SYSTEM_PUBLIC_URL = "/Rhythmyx/webservices/systemSOAP"; //$NON-NLS-1$

   /**
    * SOAP url for content design services.
    */
   public static final String UI_DESIGN_URL = "/Rhythmyx/designwebservices/uiDesignSOAP"; //$NON-NLS-1$

   /**
    * SOAP url for content public services.
    */
   public static final String UI_URL = "/Rhythmyx/webservices/uiSOAP"; //$NON-NLS-1$

   /**
    * Extracts multi-operation exception from the {@link PSErrorResultsFault}.
    * 
    * @param refs array of references used for two purposes, namely to commpare
    * the number of service calls in the error fault with number of references
    * and to set persisted flag to <code>true</code> for each successful save
    * (applicable to save only). Must not be <code>null</code> or empty.
    * @param fault axis fault that can be only {@link PSErrorResultsFault}.
    * @param method method enumeration used to set persisted flag as explained
    * above. Currently this is ignored if it is not {@link METHOD#SAVE}
    * @return {@link PSMultiOperationException} with results and errors set if
    * the target exception in the supplied exception is
    * {@link PSErrorResultsFault}
    * @throws RuntimeException if the reference array size does not match with
    * the number of service calls in the error fault.
    */
   static public Throwable extractMultiOperationException(IPSReference[] refs,
      METHOD method, PSErrorResultsFault fault) throws RuntimeException
   {
      Throwable ex;
      PSErrorResultsFaultServiceCall[] sCalls = fault.getServiceCall();
      assert refs.length == sCalls.length;
      if (refs.length != sCalls.length)
      {
         throw new RuntimeException("Something is seriously wrong: "
            + "The number fo objects submitted and number "
            + "of objects.erros returned does not match");
      }
      
      Map<Long, Object> callMap = new HashMap<Long, Object>(sCalls.length);
      for (int i = 0; i < sCalls.length; i++)
      {
         PSErrorResultsFaultServiceCall sCall = sCalls[i];
         IPSReference ref = refs[i];
         
         if (sCall.getError() != null)
         {
            PSErrorResultsFaultServiceCallError error = sCall.getError();
            if (error.getPSError() == null)
            {
               callMap.put(error.getId(), 
                  convertFault(error.getPSLockFault(), method.name(), 
                     ref.getObjectType().toString(), ref.getName()));
            }
            else
            {
               callMap.put(error.getId(), 
                  convertFault(error.getPSError()));
            }
         }
         else
         {
            PSErrorResultsFaultServiceCallResult success = sCall.getResult();
            if (method != METHOD.SAVE && method != METHOD.DELETE)
               callMap.put(success.getId(), success.getId());
         }
      }
      // Put the results back in the proper order so they match up
      // with the refs passed in
      Object[] res = new Object[sCalls.length];
      for(int i = 0; i < refs.length; i++)
      {
         res[i] = callMap.get(new PSDesignGuid(refs[i].getId()).getValue());
         if(res[i] instanceof PSModelException)
            ((PSModelException)res[i]).setDetail(refs[i]);
      }
      ex = new PSMultiOperationException(res, refs);
      return ex;
   }

   /**
    * Extracts multi-operation exception from the {@link PSErrorsFault}.
    * 
    * @param refs array of references used for two purposes, namely to compare
    * the number of service calls in the error fault with number of references
    * and to set persisted flag to <code>true</code> for each successful save
    * (applicable to save only). May be <code>null</code> or empty. If
    * <code>null</code> comparison and setting persisted flag is not done.
    * @param fault axis fault that can be only {@link PSErrorsFault}.
    * @param method method enumeration used to set persisted flag as explained
    * above. Currently this is ignored if it is not {@link METHOD#SAVE}
    * @return {@link PSMultiOperationException} with results and errors set if
    * the target exception in the supplied exception is {@link PSErrorsFault}
    * @throws RuntimeException if the reference array size does not match with
    * the number of service calls in the error fault.
    */
   static public Throwable extractMultiOperationException(IPSReference[] refs,
      METHOD method, PSErrorsFault fault) throws RuntimeException
   {
      Throwable ex;
      PSErrorsFaultServiceCall[] sCalls = fault.getServiceCall();
      if (refs != null)
      {
         assert refs.length == sCalls.length;
         if (refs.length != sCalls.length)
         {
            throw new RuntimeException("Something is seriously wrong: "
               + "The number fo objects submitted and number "
               + "of objects.erros returned does not match");
         }
      }
      
      Map<Long, Object> callMap = new HashMap<Long, Object>(sCalls.length);
      for (PSErrorsFaultServiceCall sCall : sCalls)
      {         
         if (sCall.getSuccess() == null)
         {
            PSErrorsFaultServiceCallError error = sCall.getError();
            if (error.getPSError() == null)
            {
               callMap.put(error.getId(), 
                  convertFault(error.getPSLockFault()));
            }
            else
            {
               callMap.put(error.getId(), 
                  convertFault(error.getPSError()));
            }
         }
         else
         {
            PSErrorsFaultServiceCallSuccess success = sCall.getSuccess();
            if (method != METHOD.SAVE && method != METHOD.DELETE)
               callMap.put(success.getId(), success.getId());
         }
      }
      // Put the results back in the proper order so they match up
      // with the refs passed in
      Object[] res = new Object[sCalls.length];
      for(int i = 0; i < refs.length; i++)
      {
         res[i] = callMap.get(new PSDesignGuid(refs[i].getId()).getValue());
         if(res[i] instanceof PSModelException)
            ((PSModelException)res[i]).setDetail(refs[i]);
      }
      ex = new PSMultiOperationException(res, refs);
      return ex;
   }

   /**
    * Extracts multi-operation exception from the
    * {@link InvocationTargetException}. If the target exception wrapped in the
    * supplied exception is not {@link PSErrorsFault}, returns the target
    * exception itself.
    * 
    * @see #extractMultiOperationException(IPSReference[], METHOD, PSErrorsFault)
    */
   static public Throwable extractMultiOperationException(IPSReference[] refs,
      InvocationTargetException e, METHOD method) throws RuntimeException
   {
      Throwable ex = e.getTargetException();
      
      if (ex instanceof PSErrorsFault)
      {
         PSErrorsFault fault = (PSErrorsFault) ex;
         ex = extractMultiOperationException(refs, method, fault);
      }
      else if (ex instanceof PSErrorResultsFault)
      {
         PSErrorResultsFault fault = (PSErrorResultsFault) ex;
         ex = extractMultiOperationException(refs, method, fault);
      }

      return ex;
   }

   /**
    * Converts the supplied error results fault into a single exception. The 
    * fault is walked until the first error is found which will then be 
    * connverted into a <code>PSClientException</code>.
    * 
    * @param fault the fault to convert, never <code>null</code>.
    * @return the first error found in the supplied fault converted into a
    *    <code>PSClientException</code>, never <code>null</code>.
    */
   static public PSClientException createClientException(
      PSErrorResultsFault fault, String operation, String type, String name)
   {
      if (null == fault)
         throw new IllegalArgumentException("fault cannot be null");  

      PSClientException ex = null;
      for (PSErrorResultsFaultServiceCall call : fault.getServiceCall())
      {
         if (call.getError() != null)
         {
            PSErrorResultsFaultServiceCallError error = call.getError();
            if (error.getPSLockFault() != null)
            {
               ex = convertFault(error.getPSLockFault(), operation, 
                  type, name);
            }
            else if (error.getPSError() != null)
            {
               ex = convertFault(error.getPSError());
            }
            else
               ex = new PSModelException(fault);
            
            break;
         }
      }
      
      return ex;
   }

   /**
    * Extracts the relevant info from the supplied fault and converts it into 
    * a model exception.
    *  
    * @param fault the fault to convert, not <code>null</code>.
    * @return the fault converted into a model exception, never 
    *    <code>null</code>.
    */
   static public PSModelException convertFault(PSError fault)
   {
      if (fault == null)
         throw new IllegalArgumentException("fault cannot be null");  

      /*
       * Override the axis fault so that we are able to show the original error
       * message as returned from the server.
       */
      PSError faultOverride = new PSError(fault.getCode(), 
         fault.getErrorMessage(), fault.getStack())
      {
         @Override
         public String getMessage()
         {
            return getErrorMessage();
         }
         
         @Override
         public String getLocalizedMessage()
         {
            return getErrorMessage();
         }
      };

      PSModelException e = new PSModelException(PSErrorCodes.RAW, 
         new Object[] { fault.getErrorMessage() }, faultOverride);
      e.setStackTrace(fault.getStackTrace());
      
      return e;
   }

   /**
    * Extracts the relevant info from the supplied fault and converts it into 
    * a model exception.
    *  
    * @param fault the fault to convert, not <code>null</code>.
    * @return the fault converted into a model exception, never 
    *    <code>null</code>.
    */
   static public PSModelException convertFault(Error fault)
   {
      if (fault == null)
         throw new IllegalArgumentException("fault cannot be null");  

      /*
       * Override the axis fault so that we are able to show the original error
       * message as returned from the server.
       */
      Error faultOverride = new Error(fault.getCode(), 
         fault.getErrorMessage())
      {
         @Override
         public String getMessage()
         {
            return getErrorMessage();
         }
         
         @Override
         public String getLocalizedMessage()
         {
            return getErrorMessage();
         }
      };

      PSModelException e = new PSModelException(PSErrorCodes.RAW, 
         new Object[] { fault.getErrorMessage() }, faultOverride);
      e.setStackTrace(fault.getStackTrace());
      
      return e;
   }

   /**
    * Converts the supplied axis fault into a model exception.
    * 
    * @param fault the axis fault to convert, not <code>null</code>.
    * @return the model exception, may be <code>null</code> if no conversion is
    *    supported for the cause of the supplied fault.
    */
   static public PSModelException convertFault(AxisFault fault)
   {
      if (fault == null)
         throw new IllegalArgumentException("fault cannot be null");
      
      PSModelException e = null;
      Throwable cause = fault.detail;
      if (cause instanceof ConnectException)
      {
         e = new PSModelException(PSErrorCodes.NO_CONNECTION, 
            new Object[] { cause.getLocalizedMessage() }, cause);
         e.setStackTrace(fault.getStackTrace());
      }

      return e;
   }

   /**
    * Extracts the relevant info from the supplied lock fault and converts it 
    * into a lock exception.
    * 
    * @param fault the lock fault to convert, not <code>null</code>.
    * @param operation the name of the operation which cause the fault, 
    *    may be <code>null</code> or empty.
    * @param objectType the type of the object that failed, may be 
    *    <code>null</code> or empty.
    * @param objectName teh name of the object that failed, may be 
    *    <code>null</code> or empty.
    * @return the lock fault converted into a lock exception, never 
    *    <code>null</code>.
    */
   static public PSLockException convertFault(PSLockFault fault, 
      String operation, String objectType, String objectName)
   {
      /*
       * Override the axis fault so that we are able to show the original error
       * message as returned from the server.
       */
      PSLockFault faultOverride = new PSLockFault(fault.getCode(), 
         fault.getErrorMessage(), fault.getStack(), fault.getLocker(), 
         fault.getRemainigTime())
      {
         @Override
         public String getMessage()
         {
            return getErrorMessage();
         }
         
         @Override
         public String getLocalizedMessage()
         {
            return getErrorMessage();
         }
      };
         
      PSLockException e = new PSLockException(operation, objectType, objectName, 
         fault.getLocker(), faultOverride);
      e.setStackTrace(fault.getStackTrace());
      
      return e;
   }
}
