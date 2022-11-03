/*[ PSUdfSet.java ]********************************************************
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted 
 * work including confidential and proprietary information of Percussion.
 *
 * $ID$
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 *
 * $Log: PSUdfSet.java $
 * Revision 1.26  2001/07/25 00:25:53Z  snezanahasanovic
 * Fixed bugs Rx-01-07-0017 and Rx-01-07-0018
 * Revision 1.25  2001/07/12 14:44:12Z  SyamalaKommuru
 * Refresh Context menu is added to user defined functions. 
 * Fix for Bug Rx-01-06-0057
 * Revision 1.24  2001/07/02 12:52:15Z  snezanahasanovic
 * Fixed bug Rx-01-06-0067
 * Revision 1.23  2001/06/13 21:58:13Z  snezanahasanovic
 * Changed access of GLOBAL_EXTENSION_CONTEXT member
 * Revision 1.22  2001/06/12 18:24:21Z  JaySeletz
 * Fix for udf hiding with empty category string
 * Revision 1.21  2001/06/05 13:31:08Z  snezanahasanovic
 * Removed code that was hiding old udfs from the map browser, 
 * it is handled by MapBroser now.
 * Revision 1.20  2001/06/01 14:18:13Z  snezanahasanovic
 * Added code to hide old udfs and exits by the workbench and maper browsers.
 * Revision 1.19  2001/05/03 21:02:33Z  JaySeletz
 * Removed the functionality to enable preinstalled udfs.
 * Revision 1.18  2001/05/01 18:47:12Z  JaySeletz
 * Added sorting of application and global extension lists
 * Revision 1.17  2000/07/17 21:17:49Z  AlexTeng
 * Exposed constant PSUdfSet.GLOBAL_EXTENSION_CONTEXT.
 * 
 * Revision 1.16  2000/07/07 01:38:43Z  AlexTeng
 * Added getAllScriptableUdfs() method to help retrieve scriptable UDFs
 * for all UDF types.
 * 
 * Revision 1.15  2000/07/06 00:35:22Z  paulhoward
 * Now catalogs all extensions not in the 'application' context. 
 * getAllUdfs now returns all udfs, not just scriptable udfs.
 * 
 * Revision 1.14  2000/06/28 08:17:44Z  paulhoward
 * Changes to work w/ the 2.0 extension model.
 * 
 * Revision 1.13  2000/03/02 22:16:35Z  candidorodriguez
 * fixed bug Rx-00-03-0004
 * Revision 1.12  2000/02/24 02:34:39Z  chadloder
 * Fixed bug with getGlobalUdfs() not returning null when it said it would
 * Revision 1.11  2000/02/22 18:33:59  candidorodriguez
 * fixed crash ( was not expecting an null pointer )
 *                   
 * Revision 1.10  2000/02/18 04:40:29Z  paulhoward
 * Add implementation to get global UDFs. Finish conversion to new
 * extension model.
 *                   
 * Revision 1.9  2000/02/10 18:55:16Z  candidorodriguez
 * now uses NEW UDF's
 *
 * Revision 1.8  2000/02/09 17:07:54Z  candidorodriguez
 * now uses new exits
 * Revision 1.7  1999/09/21 20:48:17Z  martingenhart
 * fix bug in save UDF's to application
 * Revision 1.6  1999/08/17 18:35:56  AlexTeng
 * Checks m_listener to see if it exists first before firing ChangeEvent.
 *
 * Revision 1.5  1999/08/16 00:21:08  AlexTeng
 * Added a ChangeListener to monitor m_applicationUdfs.  Whenever
 * a new udfs is added to m_applicationUdfs, a ChangeEvent is fired.
 *
 * Revision 1.4  1999/08/15 21:23:34  AlexTeng
 * Added new method addNewUdfCollection().
 *
 * Revision 1.3  1999/08/13 15:15:35  AlexTeng
 * Updated... Still not finished with "Add/Modify" functionality.
 *
 * Revision 1.2  1999/08/12 00:18:44  AlexTeng
 * Changed sorting logic: must replace the Predefined UDF with the
 * Application UDF if both exists.
 *
 * Revision 1.1  1999/08/10 20:28:54  AlexTeng
 * Initial revision
 *
 ***************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSCollection;
import com.percussion.util.PSStringComparator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/** This object contains all the UDFs needed by the Mapper.  It is holding 4
   * PSCollections of different types of UDFs.
   * <P>
   * <UL>
   * <LI>Predefined UDFs
   * <LI>Application UDFs
   * <LI>Local UDFs
   * <LI>Global UDFs
   * </UL>
*/

public class PSUdfSet
{
//
// CONSTRUCTORS
//
   /**
    * Default constructor.
    */
   public PSUdfSet() { super(); }

   /**
    * Creates a full udf set with predefined udfs and all the udfs that exists
    * within the parameter application.
    *
    * @param app The application that we want to look for its
    * application-specific udfs. This may not be <CODE>null</CODE>.
    */
   public PSUdfSet( OSApplication app )
   {

      if ( app == null )
         throw new IllegalArgumentException(
            "The OSApplication parameter may not be null!" );

      m_app = app;
      m_conn = E2Designer.getDesignerConnection();
   }

   /**
    * Use this constructor to create an exclusive udf set with the provided
    * handler name. This udf set will only contain the extensions of the
    * specified handler.
    *
    * @param strHandlerName We will get all the extensions that belongs to this
    * handler. This may not be <CODE>null</CODE> or empty. NOTE: this name is
    * the display name of the handler.
    *
    * @param connection Admin client needs this connection reference to the
    * server. The workbench will not use it. Will never be <CODE>null</CODE>.
    */
   public PSUdfSet( String strHandlerName, PSDesignerConnection connection )
   {
      if ( strHandlerName == null || strHandlerName.trim().equals("") )
         throw new IllegalArgumentException( "Invalid extension handler name!" );

      m_conn = connection;
   }


//
// PUBLIC METHODS
//
   /**
    * Adds a single udf to the application udf hashmap.
    *
    * @param udf The new UDF definition.
    */
   public void addApplicationUdf( IPSExtensionDef udf)
   {
      // fire ChangeEvent only if m_listener is valid for this context.
      if (null != m_listener)
         m_listener.stateChanged(new ChangeEvent( udf ));

      m_mapApplicationUdfs.put( udf.getRef().getExtensionName(), udf );
      m_udfAddList.put( udf.getRef().getExtensionName(), udf );
   }


   /**
    * Returns an iterator over 0 or more IPSExtensionDef objects that have
    * been added or modified since this object was created.
    */
   public Iterator getSaveList()
   {
      return m_udfAddList.values().iterator();
   }


   /**
    * Removes the UDF based on the name passed in.
    *
    * @param key The name of the UDF to remove.
    */
   public void removeApplicationUdf( Object key )
   {
      Object udf = m_mapApplicationUdfs.remove( key );
      if ( null == udf )
         return;

      m_vDeleteList.add( udf );
      m_udfAddList.remove( key );
   }


   /**
    * Puts back the deleted UDF entries.
    */
   public void undoApplicationUdfRemove()
   {
      for ( int i = 0; i < m_vDeleteList.size(); i++ )
      {
         IPSExtensionDef def = (IPSExtensionDef)m_vDeleteList.get(i);
         m_mapApplicationUdfs.put( def.getRef().getExtensionName(), def );
      }
      // clear undo cache
      m_vDeleteList = new Vector( 5 );
   }

   /**
    * Gets a collection of udfs based on the flag passed in.
    *
    * Assumes the refresh flag for global udfs to false. So It doesn't
    * refresh from server, if global udfs are in cache.
    *
    * @param iUdfFlag Specify which collection of udfs you want.  Use UDF_APP,
    * UDF_GLOBAL, UDF_LOCAL, from <CODE>OSUdfConstants</CODE>
    * as the flags.
    * @return The collection of the udfs specified by the parameter. This will
    * never be <CODE>null</CODE>.
    *
    * @see OSUdfConstants
    */
   public PSCollection getUdfs(int iUdfFlag)
   {
      return getUdfs(iUdfFlag, false);
   }


   /**
    * Gets a collection of udfs based on the flag passed in.
    *
    * @param iUdfFlag Specify which collection of udfs you want.  Use UDF_APP,
    * UDF_GLOBAL, UDF_LOCAL, from <CODE>OSUdfConstants</CODE>
    * as the flags.
    *
    * @param bRefresh Refresh flag for global udfs.
    *        If bRefresh is <CODE>true</CODE>, catalogs from server else
    *        gets from cache. 
    *
    * @return The collection of the udfs specified by the parameter. This will
    * never be <CODE>null</CODE>.
    *
    * @see OSUdfConstants
    */
   public PSCollection getUdfs(int iUdfFlag, boolean bRefresh)
   {
      PSCollection psCol=null;

      switch (iUdfFlag)
      {
         case OSUdfConstants.UDF_APP:
            psCol = getApplicationUdfs();
            break;
         case OSUdfConstants.UDF_GLOBAL:
            psCol = getGlobalUdfs(bRefresh);
            break;
         default:
            throw new IllegalArgumentException("Invalid input param flag!");
      }
      return psCol;
   }

   /**
    * Gets all the scriptabled udfs from the type specified by the param flag.
    *
    * @param iUdfFlag The int flag used to specified which type of scriptable
    * udfs to retrieve. Use <CODE>OSUdfConstants</CODE> constants.
    *
    * @return The collection of scriptable udfs based on the flag. If no
    * scriptable UDFs were found, <CODE>null</CODE> is returned.
    *
    * @see OSUdfConstants
    */
   public PSCollection getScriptableUdfs( int iUdfFlag )
   {
      PSCollection psCol = getUdfs( iUdfFlag );

      PSCollection pSResult = null;
      try
      {
         pSResult= new PSCollection(EXIT_CLASS );
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      for (int i = 0; i < psCol.size(); i++)
      {
         Object obj = psCol.get(i);
         IPSExtensionDef def = (IPSExtensionDef)obj;
         if ( null != def.getInitParameter( "scriptBody" ))
            pSResult.add(obj);
      }
      return ( pSResult );
   }


   /**
    * Gets a list of all the udfs, regardless of the type. If any name clashes
    * (meaning duplicate udfs), there will be a hierarchy for the udf to make it
    * to the list.
    * <OL>
    * <LI>Application Udfs: The application udf of the same name will have the
    * highest precedence into the list.
    * <LI>Server (Global) Udfs: Server udfs are second
    * <LI>Predefined Udfs: Predefined udfs have the least priority.
    * </OL>
    *
    * @return The collection of Udfs, sorted case insensitively by extension
    * name.  Never <code>null</code>.
    */
   public PSCollection getAllUdfs()
   {
      // need to use a sorted map
      Map mapUdfs = new TreeMap(new PSStringComparator(
         PSStringComparator.SORT_CASE_SENSITIVE_ASC));

      addCollectionToMap( getUdfs( OSUdfConstants.UDF_GLOBAL ),
                          mapUdfs );
      addCollectionToMap( getUdfs( OSUdfConstants.UDF_APP ),
                          mapUdfs );

      return ( convertMapToCollection( mapUdfs ) );
   }


   /**
    * Gets a list of all scriptable udfs, regardless of the type. If any name
    * clashes (meaning duplicate udfs), there will be a hierarchy for the udf to
    * make it to the list.
    * <OL>
    * <LI>Application Udfs: The application udf of the same name will have the
    * highest precedence into the list.
    * <LI>Server (Global) Udfs: Server udfs are second
    * <LI>Predefined Udfs: Predefined udfs have the least priority.
    * </OL>
    *
    * @return The collection of Udfs, sorted case insensitively by extension
    * name.  Never <code>null</code>.
    */
   public PSCollection getAllScriptableUdfs()
   {
      // need to use a sorted map
      Map mapUdfs = new TreeMap(new PSStringComparator(
         PSStringComparator.SORT_CASE_SENSITIVE_ASC));

      addCollectionToMap( getScriptableUdfs( OSUdfConstants.UDF_GLOBAL ),
                          mapUdfs );
      addCollectionToMap( getScriptableUdfs( OSUdfConstants.UDF_APP ),
                          mapUdfs );

      return ( convertMapToCollection( mapUdfs ) );
   }


   // TODO[Alex]: keep following code to help provide a method of sorting for
   // the UDFs.
   ////////////////////////////// <KEEP> ///////////////////////////////////////
   /**
    * This method does not create a new <CODE>PSCollection</CODE> to store the
    * sorted UDFs, the parameter is reused and thus alter the object pointed by
    * the parameter!
    */
   /*
   public PSCollection sortUdfs( PSCollection udfCollection )
   {
      IPSExtensionDef[] defArray = new IPSExtensionDef[ udfCollection.size() ];

      for ( int i = 0; i < udfCollection.size(); i++ )
      {
         defArray[i] = (IPSExtensionDef)udfCollection.get( i );
      }

      Comparator comparator = new Comparator()
      {
         public int compare( Object obj1, Object obj2 )
         {
            String name1 = ((IPSExtensionDef)obj1).getName();
            String name2 = ((IPSExtensionDef)obj2).getName();

            for (  )
         }

         // no need to override "equals(Object, Object)" b/c we never need to
         // evaluate 2 equal udf objects.
      };

      PSSortTool.QuickSort( defArray,  );

      for ( int i = 0; i < defArray.length; i++ )
      {
         udfCollection.add( defArray[i] );
      }

      return udfCollection;
   }
   */

/** Uses the smaller collection of UDFs and loops through the bigger collection
   * to get rid of all the duplicates.
   *
   * @param predef The predefined udf collection. The first collection.
   * @param app The application udf collection. The second collection, which has higher precedence in case of name collision.
   * @returns PSCollection The collection of UDFs without duplicates.
*/
/*
   public PSCollection sortUdfs( PSCollection pscollPredefUdf, PSCollection pscollAppUdf )
   {

      if( pscollPredefUdf == null || pscollAppUdf == null )
         return(null);

      // making a deep copy of the parameter PSCollections
      PSCollection pscollPredefCopy = null;
      PSCollection pscollAppCopy = null;

      try {
         pscollPredefCopy = new PSCollection( EXIT_CLASS );
         pscollAppCopy = new PSCollection( EXIT_CLASS );
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      for (int i = 0; i < pscollPredefUdf.size(); i++)
         pscollPredefCopy.add(pscollPredefUdf.get(i));

      for (int i = 0; i < pscollAppUdf.size(); i++)
         pscollAppCopy.add(pscollAppUdf.get(i));

      // disregarding size differences and remove matching UDFs from the 2
      // collections
      for (int i = 0; i < pscollPredefUdf.size(); i++)
      {
         IPSExtensionDef exit = (IPSExtensionDef)pscollPredefUdf.get(i);
         for (int j = 0; j < pscollAppUdf.size(); j++)
         {
            if (compareUdfExit(exit, (IPSExtensionDef)pscollAppUdf.get(j)))
            {
               // Application UDFs takes precedence over predefined UDFs, then
               // remove the application UDF from the appUdf collection.
               pscollPredefCopy.set(i, pscollAppUdf.get(j));
               pscollAppCopy.remove(pscollAppUdf.get(j));
            }
         }
      }

      // adding leftover app udfs to the new list
      for (int i = 0; i < pscollAppCopy.size(); i++)
         pscollPredefCopy.add(pscollAppCopy.get(i));

      return pscollPredefCopy;
   }
*/
  ////////////////////////////// </KEEP> ///////////////////////////////////////


   /**
    * Checks if an UDF exists in the list of used application UDFs.
    *
    * @param strUdfName Pass in the name of the UDF you want to look up. If
    * name is <CODE>null</CODE> or empty, <CODE>false</CODE> will be returned.
    *
    * @return <CODE>true</CODE> if the UDF is enlisted in the used list.
    */
   public boolean isAppUdfUsed( String strUdfName )
   {
      if ( null == strUdfName || strUdfName.length() == 0 )
         return false;

      CatalogReceiver rec = CatalogUdfs.getCatalog( m_app.getName(),
                                                    CatalogUdfs.UDF_APP,
                                                    true );
      Map map = (Map)rec.get( CatalogReceiver.CATALOG_UDF_APP );
      return ( null != map.get( strUdfName ) );
   }


   /**
    * Returns the extension context that should be used when creating a new
    * UDF. If this context is not used, The UDF may not be available after
    * the app is saved. If the constructor that takes an application was used,
    * the context will be an application specific context. If no app is
    * available, it will be a global context.
    *
    * @return A non-<code>null</code>, non-empty string that must be used
    * as the extension context when creating an extension ref.
    */
   public String getExtensionContext()
   {
      return m_app != null ? m_app.getExtensionContext() :
         GLOBAL_EXTENSION_CONTEXT;
   }


   /**
    * Merges more UDFs into the stored application UDFs list.
    *
    * @param mapAppUdfs The map of UDFs you want to merge into the stored
    * application UDF list. If param is <CODE>null</CODE>, method will simply
    * return <CODE>null</CODE> immediately.
    *
    * @return A <CODE>Map</CODE> object that is a COPY of the merged udfs. So
    * the stored application UDFs are not touched.
    */
   public Map mergeAppUdfs( Map mapAppUdfs )
   {
      if ( null == mapAppUdfs )
         return null;

      HashMap map = new HashMap( 10 );
      Iterator iterator = m_mapApplicationUdfs.keySet().iterator();
      while ( iterator.hasNext() )
      {
         String key = (String)iterator.next();
         map.put( key, m_mapApplicationUdfs.get( key ) );
      }

      iterator = mapAppUdfs.keySet().iterator();
      while ( iterator.hasNext() )
      {
         String key = (String)iterator.next();
         map.put( key, mapAppUdfs.get( key ) );
      }

      return map;
   }


   /**
    * Merges more UDFs into the stored application UDFs list.
    *
    * @param appUdfs The collectgion of UDFs you want to merge into the stored
    * application UDF list. If param is <CODE>null</CODE> or empty, method will 
    * return <CODE>null</CODE> immediately.
    *
    * @return A <CODE>Map</CODE> object that is a COPY of the merged udfs. So
    * the stored application UDFs are not touched.
    */
   public Map mergeAppUdfs( PSCollection appUdfs )
   {
      if ( null == appUdfs || appUdfs.size() == 0 )
         return null;

      return mergeAppUdfs( convertCollectionToMap( appUdfs ) );
   }


   /**
    * @return A collection containing all of the UDFs that reside in the
    * extension context returned by the application passed into the ctor. If
    * no app was passed, then an empty collection is returned.
    *
    * @return A collection of 0 or more UDFs that reside under the extension
    * context of the app that created this set, or an empty set if not created
    * by an app.
    */
   PSCollection getApplicationUdfs()
   {
      if ( null != m_app && m_mapApplicationUdfs.size() == 0 )
      {
         String context = m_app.getExtensionContext();
         Vector handlers = CatalogExtensionCatalogHandler.getCatalog( m_conn,
            true );
         Map udfs = null;
         if( handlers != null )
         {
            udfs = new TreeMap(new PSStringComparator(
               PSStringComparator.SORT_CASE_SENSITIVE_ASC));
            int handlerCt = handlers.size();
              for ( int i = 0; i < handlerCt; ++i )
            {
               PSCollection colUdfs = getUdfsFromHandler(
                  ((PSExtensionRef) handlers.get(i)).getExtensionName(),
                  context, true );

               for ( int j = 0; j < colUdfs.size(); j++ )
               {
                  IPSExtensionDef def = (IPSExtensionDef) colUdfs.get( j );
                  udfs.put( def.getRef().getExtensionName(), def );
               }
            }
         }
         if ( udfs.size() > 0 )
            m_mapApplicationUdfs = udfs;
      }
      return ( convertMapToCollection( m_mapApplicationUdfs ) );
   }

   /**
    * Catalog all UDFs from the server and cache them in an instance of this
    * object.
    *
    * @param bRefresh Flag to specify whether to refresh from server or not
    *        If bRefresh is <CODE>true</CODE>, catalogs from server else
    *        gets from cache. If cache is null or empty, then catalogs from server
    * @return The collection of global udfs. May be <CODE>null</CODE>.
    * @todo Should we cache them statically?
   **/
   private PSCollection getGlobalUdfs(boolean bRefresh)
   {
      if ( !bRefresh && null != m_globalUdfs && m_globalUdfs.size() > 0 )
         return m_globalUdfs;

      Map allowedExits = null;

      Vector handlers = CatalogExtensionCatalogHandler.getCatalog( m_conn, true );
      if( handlers != null )
      {
         int handlerCt = handlers.size();
         // use treemap to sort for us by Extension ref name
         allowedExits = new TreeMap(new PSStringComparator(
            PSStringComparator.SORT_CASE_SENSITIVE_ASC));
         String appContext = OSApplication.APP_CONTEXT_PREFIX;
         appContext = appContext.substring( 0, appContext.indexOf( '/' ));
         for ( int i = 0; i < handlerCt; ++i )
         {
            /* Since the server doesn't support wildcards in the context,
               we get all extensions and return those that aren't part of
               the application/ context. */
            PSCollection colUdfs = getUdfsFromHandler(
               ((PSExtensionRef) handlers.get(i)).getExtensionName(), null,
               false);

            for ( int j = 0; j < colUdfs.size(); j++ )
            {
               IPSExtensionDef def = (IPSExtensionDef) colUdfs.get( j );
               String context = def.getRef().getContext();

               int firstSlash = context.indexOf( '/' );

               if ( firstSlash > 0 )
               {
                  String topContext = context.substring( 0, firstSlash );
                  if ( topContext.equals( appContext ))
                     continue;
               }

               /*Since it is possible to have the same udfs names, but different
               contexts, use name, context and category attribs to specify the key*/
               String udfName = def.getRef().getExtensionName();
               String udfContext = def.getRef().getContext();
               String udfCategory = def.getRef().getCategory();
               String udfFullName = udfName + "/" + udfContext + udfCategory;

               allowedExits.put(udfFullName , def ); 
            }
         }
      }

      if (allowedExits != null)
      {
         // now this will be sorted by name
         m_globalUdfs = new PSCollection(allowedExits.values().iterator());
      }

      return ( m_globalUdfs );
   }

   /**
    * Provide an extension handler name, and we will catalog and store all the
    * extensions that belongs to the named extension handler in the passed-in
    * collection.
    *
    * @param handlerName The display name of the extension handler for which we
    * want to get the extensions from. This name may not be <CODE>null</CODE> or
    * empty.
    *
    * @param context The extension context to use. If <code>null</code>, all
    * extensions are returned, otherwise only those under the given context.
    *
    * @param includeEmptyCategories If <code>true</code>, udfs without a
    * categorystring will be included in the results, if <code>false</code>,
    * they will be excluded.
    *
    * @throws IllegalArgumentException If the handler nmae is <code>null</code>
    * or empty.
    */
   private PSCollection getUdfsFromHandler( String handlerName,
      String context, boolean includeEmptyCategories)
   {
      if ( handlerName == null || handlerName.trim().equals("") )
         throw new IllegalArgumentException( "Invalid extension handler name!" );

      PSCollection udfs = null;
      try
      {
         udfs = new PSCollection( EXIT_CLASS );
      }
      catch( ClassNotFoundException e )
      {
         // will never happen; but we'll print stack just in case =)
         e.printStackTrace();
      }

      List<IPSExtensionDef> v = CatalogServerExits.getCatalog( m_conn, handlerName, context,
         "com.percussion.extension.IPSUdfProcessor", true,
         includeEmptyCategories);
      if(udfs != null)
         udfs.addAll(v);

      return udfs;
   }


   /** Adds a <CODE>ChangeListener</CODE> to the <CODE>PSUdfSet</CODE> to
    * monitor the changes of <CODE>m_applicationUdfs</CODE>.
    */
   public void addChangeListener(ChangeListener listener)
   {
      m_listener = listener;
   }

   /** Removes the listener from this PSUdfSet.
    */
   public void removeChangeListener()
   {
      m_listener = null;
   }


   /**
    * Private utility method to convert <CODE>PSCollection</CODE> data into
    * a <CODE>HashMap</CODE>. Prevents duplicate Extensions.
    * This is currently used by the application UDFs.
    *
    * @param col The <CODE>PSCollection</CODE> that we want to convert.
    * @return A non-empty <CODE>HashMap</CODE> equivalent of a
    * non-<CODE>null</CODE> collection parameter. Never <CODE>null</CODE>.
    *
    */
   private HashMap convertCollectionToMap( PSCollection col )
   {
      HashMap map = new HashMap( 10 );
      if ( col != null )
      {
         for ( int i = 0; i < col.size(); i++ )
         {
            IPSExtensionDef def = (IPSExtensionDef)col.get( i );
            map.put( def.getRef().getExtensionName(), def );
         }
      }
      return map;
   }


   /**
    * Private utility method to convert <CODE>PSCollection</CODE> data into
    * the <CODE>Map</CODE> parameter object. Prevents duplicate Extensions.
    *
    * @param col The <CODE>PSCollection</CODE> that we want to convert.
    * @param map The <CODE>Map</CODE> object where we are adding/replacing the
    * collection parameter into. Never <CODE>null</CODE>.
    */
   private void addCollectionToMap( PSCollection col, Map map )
   {
      if ( col != null )
      {
         for ( int i = 0; i < col.size(); i++ )
         {
            IPSExtensionDef def = (IPSExtensionDef)col.get( i );
            String exName = def.getRef().getExtensionName();
            String exContext = def.getRef().getContext();
            String exCategory = def.getRef().getCategory();
            String fullExName = exName + "/" + exContext + exCategory;
            //map.put( def.getRef().getExtensionName(), def );
            map.put( fullExName, def );
         }
      }
   }


   /**
    * Private utility method to convert <CODE>HashMap</CODE> data into
    * a <CODE>PSCollection</CODE>.
    * This is currently used by the application UDFs.
    *
    * @param map The <CODE>Map</CODE> object that we want to convert. May be
    * <CODE>null</CODE>.
    * @return A non-empty <CODE>PSCollection</CODE> equivalent of a
    * non-<CODE>null</CODE> map parameter. Never <CODE>null</CODE>.
    */
   private PSCollection convertMapToCollection( Map map )
   {
      PSCollection col = null;
      try
      {
         col = new PSCollection( EXIT_CLASS );
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      if ( map != null )
      {
         Object[] extensionArray = map.values().toArray();
         for ( int i = 0; i < extensionArray.length; i++ )
         {
            IPSExtensionDef def = (IPSExtensionDef)extensionArray[i];
            col.add( def );
         }
      }
      return col;
   }


//
// MEMBER VARIABLES
//

/** The reference to the applicable application.
*/
   private PSApplication m_app;
/** THe custom designer connection if the default (from E2Designer) is not used.
*/
   private PSDesignerConnection m_conn;

   /**
    * Sorted map of application user defined functions that associates with
    * the current application.  Key is the extension name, value is the
    * PSExtensionDef.  Map is sorted lexicographically, case insensitive.
    */
   private Map m_mapApplicationUdfs = new TreeMap(new PSStringComparator(
      PSStringComparator.SORT_CASE_SENSITIVE_ASC));

   /**
    * A collection of global UDF's, initialized on the first call to 
    * , updated on every further call to.
    */
   private PSCollection m_globalUdfs;
   
/** Listener to notify MapBrowserTree to update its nodes.
*/
   protected ChangeListener m_listener;

   private Vector m_vDeleteList = new Vector(5);

   public static final String EXIT_CLASS =
      "com.percussion.extension.IPSExtensionDef";  

   /**
    * Contains all the UDFs that have been added to or modified in this
    * collection. Never <code>null</code>.
    */
   private HashMap m_udfAddList = new HashMap();

   /**
    * The extension context used for all globally accessible extensions.
    * Must not be <code>null</code> or empty and must end in a trailing slash.
    * It's value must match the context used by the admin client when saving
    * extensions.
    */
   public static final String GLOBAL_EXTENSION_CONTEXT = "global/";

   /**
    * The extension context used for application specific extensions.
    * Must not be <code>null</code>, or empty, and must end in a trailing slash. 
    */
   public static final String APP_EXTENSION_CONTEXT = "application/";
}