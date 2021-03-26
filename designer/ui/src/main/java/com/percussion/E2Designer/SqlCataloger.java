/*[ SqlCataloger.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.catalog.PSCatalogResultsWalker;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;

import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

/**
 * Supports cataloging the different levels of grouping including:
 * <ul>
 * <li>datasources</li>
 * <li>tables</li>
 * <li>columns</li>
 * </ul>
 * The constructor used determines the type of cataloging that will be performed.
 * For all constructors, the driver name cannot be null or empty, all other
 * params can optionally be null or empty. <p>
 * To use the class, call the constructor for the desired cataloging, then call
 * getCatalog() to actually perform the catalog, passing in one of the allowed
 * element types described in the corresponding constructor. <p>
 * This class should not be used until the mainframe has been initialized.
**/
public class SqlCataloger
{
   /**
    * Creates a cataloger for datasources. The only element data type supported 
    * is "name"
    */
   public SqlCataloger()
   {
      m_properties = initProperties( "Datasource", null, null, false );
   }

   /**
    * Creates a cataloger for db object types. The only element data type
    * supported is "type".
    * 
    * @param bTableTypes This is only used to differentiate the signature from
    * the owners ctor. The value is ignored.
    */
   public SqlCataloger( String datasource, boolean bTableTypes )
   {
      m_properties = initProperties( "TableTypes", datasource, null, true );
      m_reqType = "TableType";
   }

   /**
    * Creates a cataloger for tables. The only element datatype supported is 
    * "name".
    */
   public SqlCataloger( String datasource)
   {
      m_properties = initProperties( "Table", datasource, null, true );
   }

   /**
    * Creates a cataloger for columns. The only element data type supported is 
    * "name".
    */
   public SqlCataloger( String datasource, String table )
   {
      m_properties = initProperties( "Column", datasource, table, true );
   }

   /**
    * Creates a cataloger for the specified request type. The element data type
    * supported is dependent on the request type.
    */
   public SqlCataloger( String reqType, String datasource, String table )
   {
      m_properties = initProperties( reqType, datasource, table, true );
   }


   /**
    * Allows the caller to override the defaults used for the Rx connection
    * and the DBMS credentials. By default, the connection is taken from the
    * E2Designer object and the credentials are taken from the browser.
    * This method allows classes in other packages to use this class by
    * specifying their own connection information.
    * <p>This method should be called just after construction before any of
    * the cataloging methods are called. Credentials are not required to catalog
    * drivers and servers.
    *
    * @param conn A valid connection to the Rx server. If null, then reverts
    * to default behavior.
    */
   public void setConnectionInfo( PSDesignerConnection conn )
   {
      m_connection = conn;
   }

   /**
    * Initializes the properties file based on the supplied params. If a param is
    * null, it is not included in the properties object, otherwise it is. If the
    * flag bRequireCred is <code>true</code>, then a userId and pw are also added
    * to the properties object if the designer had specified one for the supplied
    * driver/server pair.
   **/
   private Properties initProperties( String reqType, String datasource, 
      String table, boolean bRequireCred )
   {
      Properties properties = new Properties();
      properties.put("RequestCategory", "data"    );
      properties.put("RequestType",     reqType  );
      if ( null != datasource  )
         properties.put("Datasource",      datasource    );
      if ( null != table && table.trim().length() > 0 )
         properties.put("TableName",       table     );
      return properties;
   }

   /**
    * Creates a vector and fills it with the names of the cataloged objects as
    * specified by the constructor. The element types supported are specified
    * in the description of each constructor.
    *
    * @param elementDataType The name of the element in the xml document that contains
    * the catalog data. The supported types for each catalog type are specified in
    * the ctor descriptor. If a name is supplied that is not supported, the behavior
    * is unspecified.
    *
    * @param bSort If you want the list sorted in ascending order, set this flag
    * to <code>true</code>, otherwise the list is returned unsorted.
    *
    * @return A vector that contains the cataloged items, which may be empty but
    * will not be null.
    *
    * @throws PSServerException if the server can't be contacted
    * @throws PSAuthorizationException if the current designer doesn't have design access
    * @throws IOException if an IO error occurs
   **/
   public Vector getCatalog( String elementDataType, boolean bSort )
         throws IOException,
             PSAuthorizationException,
             PSAuthenticationFailedException,
             PSServerException
   {
      Vector catalog = new Vector();
      PSCatalogResultsWalker names = getWalker();
      String reqType = null == m_reqType ? (String) m_properties.get(REQUEST_TYPE) : m_reqType;
      while ( names.nextResultObject( reqType ))
      {
         String name = names.getResultData(elementDataType);
         if ( name.length() > 0 )
            catalog.addElement( name );
      }
      if ( bSort && catalog.size() > 1 )
      {
         Collator c = Collator.getInstance();
         c.setStrength( Collator.PRIMARY );
         ObjectCollator oc = new ObjectCollator( c );
         Collections.sort( catalog, oc );
      }
      return catalog;
   }

   /**
    * Simple version of its overloaded brother, using "name" for the element
    * data type and sorting the list.
   **/
   public Vector getCatalog()
         throws IOException,
             PSAuthorizationException,
             PSAuthenticationFailedException,
             PSServerException
   {
      return getCatalog( "name", true );
   }

   /**
    * If you want to process the catalog document yourself, (perhaps to get
    * multiple element types in a single pass) this method returns a catalog
    * walker that can be used to do this. Typically getCatalog() will be called
    * instead.
    *
    * @return A catalog walker that can be used to read the catalog document and
    * extract a list of the cataloged objects.
    *
    * @throws PSServerException if the server can't be contacted
    * @throws PSAuthorizationException if the current designer doesn't have design access
    * @throws IOException if an IO error occurs
    *
    * @see #getCatalog
   **/
   public PSCatalogResultsWalker getWalker()
         throws IOException,
             PSAuthorizationException,
             PSAuthenticationFailedException,
             PSServerException
   {
      try
      {
         PSCataloger cataloger   = new PSCataloger( null == m_connection ?
            E2Designer.getDesignerConnection() : m_connection );

         Document doc = cataloger.catalog(m_properties);
         PSCatalogResultsWalker names   = new PSCatalogResultsWalker(doc);
         return names;
      }
      catch ( IllegalArgumentException e )
      {
         // this should not be thrown in this context
         e.printStackTrace();
         // don't return an invalid walker
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Pops up a dialog w/ an appropriate message for the supplied exception.
    * Users of this class can call this handler if they want to show a message
    * rather than dealing with it themselves. These exceptions are not handled
    * directly in the methods above in case this class is used in a non-interactive
    * mode.
   **/
   public static void handleException( IOException e )
   {
      showExceptionMessage( "CatalogerIOException", e.getLocalizedMessage());
   }

   public static void handleException( PSAuthorizationException e )
   {
      showExceptionMessage( "CatalogerAuthException", e.getLocalizedMessage());
   }

   public static void handleException(PSAuthenticationFailedException e)
   {
      showExceptionMessage("CatalogerAuthException", e.getLocalizedMessage());
   }

   public static void handleException( PSServerException e )
   {
      showExceptionMessage( "CatalogerServerException", e.getLocalizedMessage());
   }

   /**
    * Pops up a message dialog w/ an OK button. The message text is obtained
    * from the main resource bundle using the supplied message key.
    */
   private static void showExceptionMessage(String messageKey,
      String exceptionText)
   {
      Object[] astrParams =
      {exceptionText};

      PSDlgUtil.showErrorDialog(
         MessageFormat.format(E2Designer.getResources().getString(messageKey),
            astrParams),
         E2Designer.getResources().getString("CatalogerExceptionTitle"));
   }

   private static final String REQUEST_TYPE = "RequestType";

   /* Properties used for cataloging. */
   private Properties m_properties = null;

   /* This is used to specify a non-standard request type. If this is null, the
      request type is taken from the properties object. */
   private String m_reqType = null;

   private PSDesignerConnection m_connection = null;
}
