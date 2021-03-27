/******************************************************************************
 *
 * [ OSApplication.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;


import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.*;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionDefFactory;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * The GUI wrapper object for the server PSApplication object. Additional GUI
 * only data will be attached upon construction of this object.
 */

public class OSApplication extends PSApplication implements IDataCataloger
{
   /**
    * A valid existing PSApplication is required before construction.
    *
    * @param appXml The DOM document object that is basically the PSApplication
    * in its XML form. Use PSApplication&apos;s toXml() method to get this
    * Document object.
    *
    * @throws PSUnknownDocTypeException Pass thrown exceptions through.
    * @throws PSUnknownNodeTypeException Pass thrown exceptions through.
    */
   public OSApplication( Document appXml ) throws PSUnknownDocTypeException,
                                                  PSUnknownNodeTypeException
   {
      super( appXml );
      m_udfSet = new PSUdfSet( this );
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementation of IDataCataloger
   /**
    * Catalogs HTML params locally.
    *
    * @see IDataCataloger
    */
   public void catalogData(ObjectType iObjType, CatalogReceiver container )
   {
      if (ObjectType.HTML_PARAM.equals(iObjType ))
         container.add(this.getRequestTypeHtmlParamName());
   }


   /**
    * All objects that have files associated with the application should use
    * this method to save the content. The data will be written to the location
    * currently set on this application, which could be the local file system
    * (if exporting) or the server. If the file is to be saved on the server,
    * it is not actually written until {@link #saveAppFiles(PSObjectStore)
    * saveAppFiles} is called. At that point, all files that have been saved
    * (and not subsequently loaded) will be written to the server.
    *
    * @param file The relative filename of the object. The name must be relative,
    * if it is not, only the actual filename will be used, the path will be discarded.
    * Currently, saving to the server does not support any directory parts in the
    * path. If null, throws an exception.
    *
    * @param dataSrc The data to write to the supplied filename. The data in the
    * stream MUST be encoded using PSCharSets.rxJavaEnc(). If it is not, the
    * contents may be interpreted incorrectly when the file is subsequently
    * loaded. The stream is closed after this method has successfully written
    * all data to the destination. If null, throws an exception.
    *
    * @throws IOException If any failures occur while writing the file.
    *
    * @throws FileNotFoundException If the file exists but is a directory rather
    * than a regular file, does not exist but cannot be created, or cannot be
    * opened for any other reason
   **/
   public void saveAppFile( File file, InputStream dataSrc )
         throws IOException, FileNotFoundException
   {
      if ( null == file || null == dataSrc )
         throw new IllegalArgumentException( "At least one of the input params is null." );

      final String filename = file.getName();
      if ( 0 == filename.length())
         throw new IllegalArgumentException( "Empty file specified." );

      final int sepIndex = filename.indexOf( File.pathSeparatorChar );
      if ( sepIndex >= 0 )
      {
         file = new File( filename.substring( sepIndex+1 ));
      }
      if ( null != getAppFileLocation() )
      {
         try(OutputStream out = new FileOutputStream(
                 new File(getAppFileLocation().getPath() + File.pathSeparator + filename)))
         {
            IOUtils.copy(dataSrc, out);
            dataSrc.close();
         }
      }
      else
      {
         try
         {
            PSApplicationFile appFile = new PSApplicationFile(dataSrc, file);
            m_appFiles.put( file.getName(), appFile );
         }
         catch ( IllegalArgumentException e )
         { /* ignore, we know the file object is not null */ }
      }
   }

   /**
    * Put the specified file in the list of files to save. This is to 
    * facilitate the cleanup mechanism not to delete server files  
    * @param file file object to add to the list of files to save, must not 
    * be <code>null</code>.
    */
   public void saveAppFile(File file)
   {
      if (null == file)
         throw new IllegalArgumentException("file must not be null.");

      if (0 == file.getName().length())
         throw new IllegalArgumentException("file name must not be empty.");

      final int sepIndex = file.getName().indexOf(File.pathSeparatorChar);
      if (sepIndex >= 0)
         file = new File(file.getName().substring(sepIndex + 1));

      if (m_appFiles.get(file.getName()) == null)
      {
         m_appFiles.put(file.getName(), new PSApplicationFile(file));
      }
   }

   /**
    * Add to the list of files to be deleted from the server. The actual 
    * removal of the file does not happen in this method. 
    * {@link #removeAppFiles(PSObjectStore)} must be called 
    * for actual removal. 
    * @param file the file object to be added to delete list, must not be 
    * <code>null</code>.
    */
   public void removeAppFile(File file)
   {
      if(file == null)
         throw new IllegalArgumentException("file must not be null");
         
      m_appFilesToRemove.put( file.getName(), new PSApplicationFile( file ) );
   }

   /**
    * Delete the files marked for deletion from the server. Each file 
    * to delete must have been added by 
    * {@link #removeAppFile(File)}. A server exception with 
    * originating exception being {@link PSNotFoundException} indicating 
    * the file does not already exist to delete will be ignored by the 
    * method.
    *
    * @param os The object store to save the files to. Must not be 
    * <code>null</code>.
    *
    * @throws PSServerException If the server is not responding.
    *
    * @throws PSAuthorizationException If the user does not have update access
    * on the application.
    *
    * @throws PSAuthenticationFailedException If the user couldn't be 
    * authenticated on the server.
    *
    * @throws PSNotLockedException If a lock is not currently held (the timeout
    * already expired or getApplication was not used to lock the application).
    *
    * @throws PSValidationException If a validation error is encountered.
    *
    */
   public void removeAppFiles(PSObjectStore os) throws PSServerException,
      PSAuthorizationException, PSAuthenticationFailedException,
      PSNotLockedException, PSValidationException
   {
      if (null == os)
         throw new IllegalArgumentException("Param is null");

      // get current app files so we don't try to delete something that hasn't
      // already been saved
      Vector curFiles = Util.getApplicationFiles(getRequestRoot());      
      
      Iterator iter = m_appFilesToRemove.values().iterator();
      while (iter.hasNext())
      {
         PSApplicationFile appFile = (PSApplicationFile) iter.next();
         try
         {
            String appFileName = appFile.getFileName().getName();
            if (!m_appFiles.containsKey(appFileName) && 
               curFiles.contains(appFileName))
            {
               os.removeApplicationFile(this, appFile, true);
            }
         }
         catch (PSServerException | PSSystemValidationException e)
         {
            //TODO: Handle the error, add logging
            e.printStackTrace();
         }
      }
      m_appFilesToRemove.clear();
   }

   /**
    * Similar to {@link #saveAppFile(File, InputStream) saveAppFile}, but loads/
    * imports the file. <p/>
    * If any files have been set using {@link #addAppFile(PSApplicationFile)
    * addAppFile} or {@link #saveAppFile(File,InputStream) saveAppFile}, and one
    * of those files matches the requested file, the <code>InputStream</code>
    * that was originally saved will be returned. When a matching file is found
    * in the local list and returned, it is removed from the local list.
    *
    * @param file The relative filename of the object. The name must be relative,
    * if it is not, only the actual filename will be used, the path will be discarded.
    * Currently, no directory parts in the path are supported. If null, an
    * exception is thrown.
    *
    * @return A stream that contains the contents of the file of the supplied name.
    * The characters in the stream are encoded using PSCharSets.rxJavaEnc(). The
    * caller takes ownership and is responsible for closing the stream.
    *
    * @throws IOException If any failures occur while reading the file.
    *
    * @throws FileNotFoundException If the file does not exist, is a directory
    * rather than a regular file, or for some other reason cannot be opened for reading.
    *
    * @throws PSServerException If the server is not responding.
    *
    * @throws PSAuthorizationException If the user does not have update access
    * on the application.
    *
    * @throws PSAuthenticationFailedException If the user couldn't be authenticated
    * on the server.
    *
    * @throws PSNotLockedException If a lock is not currently held (the timeout
    * already expired or getApplication was not used to lock the application).
    *
    * @throws PSValidationException If a validation error is encountered.
   **/
   public InputStream loadAppFile( File file )
         throws IOException,
            FileNotFoundException,
            PSServerException,
            PSAuthorizationException,
            PSAuthenticationFailedException,
            PSNotLockedException,
            PSValidationException
   {
      if ( null == file )
         throw new IllegalArgumentException( "Input param is null" );
      // validate the filename
      String filename = file.getName();
      int sepIndex = filename.indexOf( File.pathSeparatorChar );
      if ( sepIndex >= 0 )
      {
         file = new File( filename.substring( sepIndex+1 ));
      }
      InputStream input = null;
      if (null != getAppFileLocation())
      {
         File localFile = new File(getAppFileLocation().getPath()
            + File.separator + filename );
         input = new FileInputStream( localFile );
         if ( !input.markSupported())
         {
            // read this into a byte stream so we have mark support on the stream
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            IOUtils.copy(input, os);
            input = new ByteArrayInputStream( os.toByteArray());
         }
      }
      else
      {
         try
         {
            // check if we have it local, if not load it from the server
            PSApplicationFile appFile = m_appFiles.get(filename);
            if ( null != appFile )
            {
               input = appFile.getContent().getContent();
               m_appFiles.remove( appFile );
               if ( m_appFiles.size() == 0 )
                  m_appFileLocation = null;
            }
            else
            {
               appFile = getObjectStore().loadApplicationFile(this,
                     new PSApplicationFile(file));
               input = appFile.getContent().getContent();
            }
         }
         catch ( IllegalArgumentException |PSIllegalStateException e )
         {  /* ignore, we know the stream has never been retrieved since we just loaded */ } catch (PSSystemValidationException e) {
            e.printStackTrace();
         }
      }
      return input;
   }

   /**
    * The directory on the local file system to save the application files. If
    * the files are to be loaded, the directory must exist. If the files are to
    * be saved and the directory doesn't exist, it and any missing parent directories
    * will be created. No validation is done on the supplied dir until an attempt
    * is made to load or save an application file. By default, files are loaded/saved
    * from/to the server.<p/>
    * When setting the location during an import or export, save the returned
    * location and restore it when finished.<p/>
    * See {@link #getAppFileLocation() getAppFileLocation} for more details.
    *
    * @param dir The directory that specifies the location to load/save application
    * files. If null, files are saved to the server.
    *
    * @return The previous location.
    *
    * @see #getAppFileLocation
   **/
   public File setAppFileLocation( File dir )
   {
      File origLocation = m_appFileLocation;
      m_appFileLocation = dir;
      return origLocation;
   }


   /**
    * Each application can load/save its associated application files either on
    * the local file system or on the Rx server, which is useful for regular
    * application load/save and app import/export. The associated {@link
    * #loadAppFile(File) loadAppFile } and {@link #saveAppFile(File, InputStream)
    * saveAppFile } are used to allow the location of the files to be transparent
    * to application objects. By default, files are loaded/saved from/to the server.
    *
    * @return The current location. If not null, it is a directory on the local
    * system (which may not exist yet). If null, the location is the Rx server
    * with the application file.
    *
    * @see #setAppFileLocation(File)
   **/
   public File getAppFileLocation()
   {
      return m_appFileLocation;
   }


   /**
    * Retrieves a reference to the application UDF set. This is a collection
    * of different types of UDFs. The application places its UDFs in the
    * set before returning it. The returned object should not be modified by
    * the caller and it should not be cached.
    *
    * @return The reference of the UDF set. It will never be <CODE>null</CODE>.
    */
   public PSUdfSet getUdfSet()
   {
      return m_udfSet;
   }


   /**
    * This method saves the supplied file locally so it can be written to the
    * server when the application is saved. Typically, this will be used when
    * importing applications so app files can be saved (each object associated
    * with the app file thinks that it is already on the server when an app is
    * imported). If an application object tries to load a file that matches the
    * name of a file added using this method, the contents of that file object
    * are returned and the file is removed from this list. This allows an application
    * to be imported, then some of the app files 'loaded' by the application
    * objects. All remaining app files will be written to the server on application
    * save.
    *
    * @param file The file that is to be saved to the server with this application.
    * The content must be set before passing to this method. If null, the method returns
    * immediately.
   **/
   public void addAppFile( PSApplicationFile file )
   {
      if ( null == file )
         return;
      m_appFiles.put( file.getFileName().getName(), file );
   }

   /**
    * If there are any files that have been added via {@link #addAppFile(
    * PSApplicationFile) addAppFile}, they will be written to the supplied
    * object store. After a file is successfully saved, it is removed from
    * the vector. Therefore, if an exception occurs in the middle of saving
    * several files, this method could be called again to finish saving the
    * rest of the files of the reason for the exception was removed. This
    * application must be saved via {@link PSObjectStore#saveApplication(
    * PSApplication, boolean, boolean, boolean) saveApplication} before this
    * method is called or an exception will be thrown (the underlying object
    * store call doesn't document which one).
    *
    * @param store The object store to save the files to. If null, throws an
    * exception.
    *
    * @throws PSServerException If the server is not responding.
    *
    * @throws PSAuthorizationException If the user does not have update access
    * on the application.
    *
    * @throws PSAuthenticationFailedException If the user couldn't be authenticated
    * on the server.
    *
    * @throws PSNotLockedException If a lock is not currently held (the timeout
    * already expired or getApplication was not used to lock the application).
    *
    * @throws PSValidationException If a validation error is encountered.
    *
    * @todo The app file load/save import/export still needs more work. The biggest
    * problem is that once certain files are saved for the app, they are never
    * loaded again, so saving an app twice w/ no changes results in different
    * behavior. Not sure how to address this w/o affecting the efficieny.
    * Also, we need to delay loading of application files until they are actually
    * needed (e.g. dtd) so we don't do a lot of unnecessary work.
   **/
   public void saveAppFiles(PSObjectStore store)
           throws PSServerException, PSAuthorizationException,
           PSAuthenticationFailedException, PSNotLockedException,
           PSValidationException, PSSystemValidationException {
      if (store == null)
      {
         throw new IllegalArgumentException( "Param is null" );
      }

      for (final PSApplicationFile appFile : m_appFiles.values())
      {
         if (!appFile.isNull())
         {
            store.saveApplicationFile(this, appFile, true, false);
         }
      }
      m_appFiles.clear();
      m_appFileLocation = null;
   }


   /**
    * Saves all UDF definitions that have changed since the application was opened. The
    * UDFs are saved to the Rx server extension mgr using an extension context
    * unique to this application. This mechanism only allows saving UDFs that
    * have no additional resources (i.e. scriptable UDFs).
    *
    * See {@link PSObjectStore#saveExtension(IPSExtensionDefFactory,
    * IPSExtensionDef, Iterator, boolean) saveExtension} for a description of
    * the exceptions.
    */
   public void saveUdfs()
           throws PSServerException,
           PSAuthorizationException,
           PSAuthenticationFailedException,
           PSNotLockedException,
           PSValidationException,
           PSExtensionException,
           PSNotFoundException, PSSystemValidationException {
      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      Iterator udfs = m_udfSet.getSaveList();
      while ( udfs.hasNext())
      {
         IPSExtensionDef def = (IPSExtensionDef) udfs.next();
         getObjectStore().saveExtension( factory, def, PSIteratorUtils.emptyIterator(),
             true );
         udfs.remove();
      }
   }

   /**
    * Returns global object store
    */
   private PSObjectStore getObjectStore()
   {
      return E2Designer.getApp().getMainFrame().getObjectStore();
   }


   /**
    * Overrides the base class to auto-create the context if it hasn't been
    * set yet.
    *
    * @return A non-<code>null</code>, non-empty string that can be used
    * as the extension context when creating an extension ref.
    */
   @Override
   public String getExtensionContext()
   {
      String context = super.getExtensionContext();
      if ( null == context )
      {
         context = createExtensionContext();
         super.setExtensionContext( context );
      }
      return context;
   }

   /**
    * Returns the UDF definition that is named by the supplied reference. The
    * local set is checked first. If not found there, the passed in store is
    * checked. If not found, an exception is thrown.
    *
    * @param ref The name of UDF to return. If <code>null</code>, an exception
    * is thrown.
    *
    * @return A valid definition for the supplied ref.
    *
    * @throws PSNotFoundException if a UDF that has the supplied ref is not
    * contained in this app.
    */
   public IPSExtensionDef getUdf( PSExtensionRef ref )
      throws PSNotFoundException
   {
      IPSExtensionDef def = null;
      PSCollection udfs = m_udfSet.getUdfs( OSUdfConstants.UDF_APP );
      int size = udfs.size();
      for ( int i = 0; i < size && null == def; ++i )
      {
         IPSExtensionDef udf = (IPSExtensionDef)udfs.get(i);
         if ( udf.getRef().equals( ref ))
            def = udf;
      }
      if ( null == def )
         throw new PSNotFoundException( 0,
            ref.toString() + " not found in " + this.getName() + "." );

      return def;
   }

   /**
    * Similar to {@link #saveUdfs() saveUdfs()}, but rather than writing the
    * changed extensions to the server, all extensions are serialized and
    * written to the supplied file. If there are no UDFs, no file is created.
    * This is useful when exporting an application.
    *
    * @param destination The file to store the UDFs. This file must be valid
    * and it must not exist.
    *
    * @throws IOException If any problems occur during the file creation or
    * saving.
    */
   public void saveUdfs( File destination )
      throws IOException
   {
      if ( null == destination )
         throw new IllegalArgumentException( "Target file must be supplied." );
      else if ( destination.isDirectory() || destination.exists())
         throw new IllegalArgumentException(
            "Supplied target is directory or existing file." );

      if ( m_udfSet.getUdfs( OSUdfConstants.UDF_APP ).size() == 0 )
         return;

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot( doc, APP_EXT_ROOT );

      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      PSCollection colAppUdfs = m_udfSet.getUdfs( OSUdfConstants.UDF_APP );
      for ( int i = 0; i < colAppUdfs.size(); i++ )
      {
         IPSExtensionDef def = (IPSExtensionDef)colAppUdfs.get( i );
         factory.toXml( root, def );
      }

      FileOutputStream out = null;
      try
      {
         out = new FileOutputStream( destination );
         PSXmlDocumentBuilder.write( doc, out );
      }
      finally
      {
         out.close();
      }
   }


   /**
    * Similar to {@link #saveUdfs(File) saveUdfs(File)}, but rather than
    * saving the UDFs, to a file, they are read from a file. All UDFs found
    * in the file are added to the UDF set for this app and treated as new
    * UDFs (meaning they will be saved to the extension mgr when the app is
    * saved). This is useful for importing an application.
    *
    * @param source The file to read the UDFs form. This file must exist.
    *
    * @throws PSExtensionException if any of the extension definitions are
    * malformed
    */
   public void loadUdfs( File source )
      throws PSExtensionException
   {
      if ( null == source )
         throw new IllegalArgumentException( "Source file must be supplied." );
      else if ( !source.exists())
         throw new IllegalArgumentException(
            "Source file does not exists." );

      PSExtensionDefFactory factory = new PSExtensionDefFactory();

      FileInputStream in = null;
      Document doc = null;
      try
      {
         in = new FileInputStream( source );
         doc = PSXmlDocumentBuilder.createXmlDocument( in, false );
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
      finally
      {
         try { in.close(); }
         catch( IOException ioe ) { /* ignore */ }
      }

      // create a walker so we can iterate over subelements
      final int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker walker = new PSXmlTreeWalker( doc );
      // search for first Extension element
      Element elExtension = walker.getNextElement("Extension", firstFlag);
      if (elExtension != null)
      {
         for ( ; null != elExtension;
               elExtension = walker.getNextElement( "Extension", nextFlag ) )
         {
            m_udfSet.addApplicationUdf( factory.fromXml( elExtension ) );
         }
      }
   }


   /**
    * Adds the supplied extension def to the app's modified extension list.
    * All extensions on this list will get written to the server when the
    * application is saved. If a UDF exists that has the same name as the
    * supplied one, it will be overwritten. The context of the def will
    * be replaced with the application context before it is saved.
    *
    * @param udf An valid extension that implements the
    * com.percussion.extension.IPSUdfProcessor interface.
    */
   public void addUdf( IPSExtensionDef udf )
   {
      if ( null == udf || !udf.implementsInterface(
         "com.percussion.extension.IPSUdfProcessor" ))
      {
         throw new IllegalArgumentException( "extension def null or unsupported interface" );
      }
   }


   /**
    * Creates a globally unique identifier that can be used as the context
    * for application specific extensions. Once created, it should be saved
    * to the application. The id is generated by taking the local server name,
    * appending the current date/time as a long, then base64 encoding the
    * result. The name is base64 encoded to make sure the result is
    * a valid Java identifier. This generated name is prepended with the
    * constant APP_CONTEXT_PREFIX. This identifier is not 100% guaranteed to
    * be unique. It is possible to have dupes. But the probability is so small
    * that we don't worry about it. Plus, we are changing the extension
    * security model in the future so this may go away.
    *
    * @return A non-<code>null</code>, non-empty identifier that is nearly
    * guaranteed to be unique across all applications.
    *
    * @see PSApplication#setExtensionContext
    */
   private String createExtensionContext()
   {
      // the name must start with a valid java identifier start
      String host = "_";
      try
      {
         StringBuffer hostName =
            new StringBuffer(InetAddress.getLocalHost().getHostName());
         int len = hostName.length();
         for (int i=0; i<len; i++)
         {
            char c = hostName.charAt(i);
            if (!Character.isJavaIdentifierPart(c))
               hostName.setCharAt(i, '_');
         }

         host += hostName.toString();
      }
      catch ( UnknownHostException e )
      {
         host += "unknown";
      }

      Date currentDate = new Date();
      String uniquePart = host + "_" + currentDate.getTime();
      String context = APP_CONTEXT_PREFIX
         + uniquePart + "/";
      return context;
   }


   // storage

   /**
    * Where application files are loaded/saved. If not null, it is a directory
    * on the local system.
    * If <code>null</code>, files are loaded/saved from/to the server.
    * By default files are saved to the server.
    */
   private File m_appFileLocation;

   /**
    * Any files that are to be saved as part of the application are kept here.
    * Typically, these are files that were read when an app was imported. This
    * object is never null. The key is the complete, relative filename and the
    * value is a <code>PSApplicationFile</code> object. 
    * Never <code>null</code>.
    */
   private final Map<String, PSApplicationFile> m_appFiles =
         new HashMap<String, PSApplicationFile>();

   /**
    * Any files that are to be removed from the server are kept here. 
    * Typically, these are the files associated with external resources that 
    * are removed from the UI. This object is never <code>null</code>. The key 
    * is the complete, relative filename and the value is a 
    * <code>PSApplicationFile</code> object. Never <code>null</code>.
    */
   private Map<String, PSApplicationFile> m_appFilesToRemove =
         new HashMap<String, PSApplicationFile>();

   /**
    * Each application will keep track of its own set UDFs. All objects within
    * this application will get a reference to this UDF set to manipulate the
    * UDFs. Although it is declared here as <CODE>null</CODE>, within the
    * constructor of <CODE>OSApplication</CODE> will instantiate this object.
    */
   private PSUdfSet m_udfSet = null;

   /**
    * This is the beginning of the context used to store application specific
    * extensions. It must not be <code>null</code> or empty and it must have
    * a trailing forward slash. It is combined with a generated id to create
    * a globally unique identifier.
    * <p>
    * This is made public so catalogers can exclude application specific
    * extensions. App specific extensions should be obtained directly from
    * this class.
    */
   public static final String APP_CONTEXT_PREFIX = "application/";

   /**
    * The name of the root element of the XML document used to export
    * application UDFs.
    */
   public static final String APP_EXT_ROOT = "Extensions";
}
