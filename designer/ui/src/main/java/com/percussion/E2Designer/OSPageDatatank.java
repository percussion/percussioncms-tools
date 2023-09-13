/******************************************************************************
 *
 * [ OSPageDatatank.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSContentFactory;
import com.percussion.error.PSCatalogException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSCollection;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSDtdBuilder;
import com.percussion.xml.PSDtdGenerator;
import com.percussion.xml.PSDtdTree;
import com.percussion.xml.PSDtdTreeMergeManager;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import com.percussion.share.service.exception.PSValidationException;

////////////////////////////////////////////////////////////////////////////////
public class OSPageDatatank extends PSPageDataTank implements IGuiLink, 
   IPersist, ICustomDropSourceData, IAuxFilesTransfer, IDataCataloger
{
   /**
    * Supported document types. These types are used to indicate the original
    * source of data for the DTD in this tank. <p/>
    */
   public static final int SRC_TYPE_UNKNOWN = -1;
   public static final int SRC_TYPE_HTML = 0;
   public static final int SRC_TYPE_XML = 1;
   public static final int SRC_TYPE_DTD = 2;

   /**
    * This means the DTD was automatically created from a set of tables in
    * an OSBackendDatatank.
   **/
   public static final int SRC_TYPE_BACKEND = 3;

    /* TODO: If an object is dragged from the browser (such as HTML file) which
    * can create different objects based on user choice, create a new object
    * and then create the chosen object when known. Then the XSL type can be
    * removed.
    */

   /**
    * A page tank that exists in the application can never have a type of XSL
    * (i.e it's a total hack). This is used when the user drags an HTML file
    * then chooses to create a web page (XSL). In this case, the tank is only
    * temporary.
    *
   **/
   public static final int SRC_TYPE_XSL = 4;
   /** DTD generated for an Update, contains HTML form params as DTD */
   public static final int SRC_TYPE_FORM = 5;


   /**
    * @throws MalformedURLException this exception should never be thrown
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSPageDatatank() throws MalformedURLException
   {
      super(new URL("file:unspecified"));
   }

   /**
    * Creates a new object, copying all its property values from the supplied
    * tank.
    *
    * @param tank a valid page datatank
    *
    * @throws NullPointerException if tank is null
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSPageDatatank(PSPageDataTank tank)
   {
      super(tank.getSchemaSource());
      copyFrom(tank);
   }

   /**
    * Creates a new object, copying all its property values from the supplied
    * tank.
    *
    * @param tank a valid page datatank
    *
    * @throws NullPointerException if tank is null
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSPageDatatank(OSPageDatatank tank)
   {
      super(tank.getSchemaSource());
      copyFrom(tank);
   }

   @Override
   public void setSchemaSource( URL dtd )
   {
      super.setSchemaSource( dtd );
      if ( null != m_owner )
         m_owner.invalidateLabel();
   }

   /**
    * Auto create a page datatank from the provided backend datatank. The first
    * table will be the master table, which is used for the root element and the
    * file name. A DTD of the following form will be created:
    *    <!-- AUTO CREATED by Percussion Rhythmyx -->
    *    <!ELEMENT table_name (col1, col2, ..., t1, t2, ...)>
    *      <!ELEMENT col1 (#PCDATA)>
    *      <!ELEMENT col2 (#PCDATA)>
    *      ...
    *      <!ELEMENT t1 (col11, col12, ...)>
    *        <!ELEMENT col11 (#PCDATA)>
    *        <!ELEMENT col12 (#PCDATA)>
    *        ...
    *      <!ELEMENT t2 (col21, col22, ...)>
    *        ...
    * Additional tables may be added later, using the add table function. The
    * created file will be stored to the server.
    *
    * @param backendTank the backend datatank from which we create th epage datatank
    * @throws MalformedURLException this exception should never be thrown
    */
   //////////////////////////////////////////////////////////////////////////////
   public void autoCreatePageDatatank(OSBackendDatatank backendTank)
                                              throws MalformedURLException
   {
      try
      {
         setSchemaSource(new URL("file:" + getFileName(backendTank)));
         m_originalSrcType = SRC_TYPE_BACKEND;
         InputStream stream = autoCreateDTD(backendTank);

         String filename = getFileName(backendTank);
         String doctype = Util.stripPath( filename, "" );
         setDtdTree( new PSDtdTree( stream, doctype, PSCharSets.rxJavaEnc()));

         // all automatic created page tanks will be read only
         setSchemaSourceReadOnly();
      }
      catch ( PSCatalogException e )
      {
         e.printStackTrace();
         // ignore phtodo
      }
   }

   /**
    * Populates this page tank from the supplied dtd builder using the specified
    * file base name.
    * 
    * @param dtdBuilder Specifies the DTD defintion, may not be 
    * <code>null</code>.
    * @param fileBase The base filename, without the extension, may not be
    * <code>null</code> or empty.
    * 
    * @throws MalformedURLException if a URL cannot be created using the 
    * supplied file name.
    */
   public void autoCreatePageDatatank(PSDtdBuilder dtdBuilder, String fileBase)
    throws MalformedURLException
   {
      if (dtdBuilder == null)
         throw new IllegalArgumentException("dtdBuilder may not be null");
      
      if (fileBase == null || fileBase.trim().length() == 0)
         throw new IllegalArgumentException(
            "fileBase may not be null or empty");
      
      try
      {
         setSchemaSource(new URL("file:" + fileBase + DEFAULT_DTD_EXT));
         m_originalSrcType = SRC_TYPE_DTD;
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         dtdBuilder.write(out);         
         InputStream stream = new ByteArrayInputStream(out.toByteArray());

         setDtdTree(new PSDtdTree(stream, fileBase, PSCharSets.rxJavaEnc()));

         // all automatic created page tanks will be read only
         setSchemaSourceReadOnly();
      }
      catch (PSCatalogException e)
      {
         // this could only be from a bug in the PSDtdBuilder.write() method
         e.printStackTrace();
      }
      catch (IOException e)
      {
         // will never happen with bytearray streams
         e.printStackTrace();
      }
   }
   

   /**
    * To set the object document type based on the file extension.
    *
    * @param fileName the file name including its file type (the file extension).
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setDocumentType(String fileName)
   {
      String strTemp = fileName.toLowerCase();
   //Any file that is splitable is treated as HTML
      if ( Util.isSplitable( strTemp ))
         m_originalSrcType = SRC_TYPE_HTML;
      else if (strTemp.endsWith(".xsl"))
         m_originalSrcType = SRC_TYPE_XSL;
      else if (strTemp.endsWith(".xml"))
         m_originalSrcType = SRC_TYPE_XML;
      else if (strTemp.endsWith( DEFAULT_DTD_EXT ))
         m_originalSrcType = SRC_TYPE_DTD;
      else
         m_originalSrcType = SRC_TYPE_UNKNOWN;
   }

   /**
    * Get the document type.
    *
    * @return int the document type (OSPageDatatank.TYPE_HTM, ...)
    */
   //////////////////////////////////////////////////////////////////////////////
   public int getDocumentType()
   {
      return m_originalSrcType;
   }

   /**
    * To know if the object is BACKEND type
    *
    * @return boolean
    */
   //////////////////////////////////////////////////////////////////////////////
   private boolean isBACKEND()
   {
      return m_originalSrcType == SRC_TYPE_BACKEND;
   }

   /**
    * To know if the object is HTML type
    *
    * @return boolean
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean isHTML()      // phtodo convert to isOriginallyHTML() ?
   {
      return m_originalSrcType == SRC_TYPE_HTML || m_originalSrcType == SRC_TYPE_FORM;
   }

   /**
    * To know if the object is XML type
    *
    * @return boolean
    */
   //////////////////////////////////////////////////////////////////////////////
   private boolean isXML()
   {
      return m_originalSrcType == SRC_TYPE_XML;
   }

   /**
    * To know if the object is DTD type
    *
    * @return boolean
    */
   //////////////////////////////////////////////////////////////////////////////
   private boolean isDTD()
   {
      return m_originalSrcType == SRC_TYPE_DTD;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param tank a valid OSPageTank. If null, a IllegalArgumentException is
    * thrown.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void copyFrom(OSPageDatatank tank)
   {
      copyFrom((PSPageDataTank) tank);
      m_columns = tank.m_columns;
      m_bSourceIsPrepared = tank.m_bSourceIsPrepared;
      m_originalSrcType = tank.getDocumentType();
      m_filePath = tank.m_filePath;
      m_tempFilePath = tank.m_tempFilePath;
      m_readOnly = tank.m_readOnly;
      m_originalSrcType = tank.m_originalSrcType;
      m_app = tank.m_app;
      setDtdTree( tank.m_originalDtdTree );
      m_tree = tank.m_tree;
   }


   public String getBaseFileName()
   {
      String fileBaseName = new String("");

      File file = new File(getFilePath());
      fileBaseName = file.getName();
      if (isBACKEND())
      {
         fileBaseName = fileBaseName.substring(0, 
            fileBaseName.toLowerCase().lastIndexOf( DEFAULT_DTD_EXT ));
      }
      else if (isDTD())
      {
         fileBaseName = fileBaseName.substring(0, 
            fileBaseName.toLowerCase().lastIndexOf( DEFAULT_DTD_EXT ));
      }
      else
      {
         fileBaseName = Util.stripPath( fileBaseName );
      }

      return(fileBaseName);
   }

/**
    * Catalog all columns and return them as a vector. If any errors occur, a
    * message is displayed to the user.
    *
    * @return The cataloged fields, or an empty vector if an error occurs.
    *
    */
   //////////////////////////////////////////////////////////////////////////////
   public Vector getColumns()
   {
      if ( m_columns == null || 0 == m_columns.size() || isMergeDirty())
      {
      m_columns = new Vector<String>();
      // TODO: this is a kludge so that we don't invoke this dtd tree
      // logic when our parent window is not open
      PSDtdTree tree = getDtdTree();
      if ( tree != null )
         {
        final List list = tree.getCatalog("/", "@");
        if( list != null )
        {
           for (final Object str : list)
           {
              m_columns.add((String) str);
           }
        }
      }
    }
    return null == m_columns ? new Vector(0) : m_columns;
   }

   /**
    * Set the schema source read only.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setSchemaSourceReadOnly()
   {
      m_readOnly = true;
   }

   /**
    * Returns the info wether or not this page was autocreated from a backend
    * tank.
    *
    * @return boolean the backend autocreation status
   **/
   //////////////////////////////////////////////////////////////////////////////
   public boolean isBackendAutocreated()
   {
      //return m_backendAutoCreated;
      return SRC_TYPE_BACKEND == m_originalSrcType;
   }

   /**
    * Returns the read only status of the schema source.
    *
    * @return boolean the read only status
   **/
   //////////////////////////////////////////////////////////////////////////////
   public boolean isSchemaSourceReadOnly()
   {
        return m_readOnly;
   }

   /**
    * This returns the filename used while creating the DTD from a backend datatank.
    * Its the name of the master (first) table found in the provided tank. The
    * database name is prepended. The base name can be used as a DTD root element.
    *
    * @param backendTank the backend datatank
    *
    * @return The file name, which has a basename that is XML compliant.
    */
   //////////////////////////////////////////////////////////////////////////////
   private String getFileName(OSBackendDatatank backendTank)
   {
      String fileName = getFileBaseName(backendTank);
      // fix it up so it is a valid XML name
      fileName = Util.makeXmlName( fileName );

      fileName += DEFAULT_DTD_EXT;

      return fileName;
   }

   /**
    * This returns the filename used while creating the DTD from a backend
    * datatank without the file extension.
    * Its the name of the master (first) table found in the provided tank. The
    * database name is prepended.
    *
    * @param backendTank the backend datatank
    * @return String the file name
   **/
   //////////////////////////////////////////////////////////////////////////////
   private String getFileBaseName(OSBackendDatatank backendTank)
   {
      String fileName = new String("");
      PSCollection tables = backendTank.getTables();
      if (!tables.isEmpty())
      {
         PSBackEndTable table = (PSBackEndTable) tables.get(0);
         fileName = table.getTable();
         String ds = table.getDataSource();
         if (!StringUtils.isBlank(ds))
            fileName = table.getDataSource() + "_" + fileName;
      }

      return fileName;
   }

   /**
    * Autocreate a page datatank using the provided backend datatank. The root
    * element name can be obtained by calling getFileName(backendTank).
    *
    * @param backendTank the backend datatank
    *
    * @return The DTD as a stream, encoded using the standard encoding
    * (PSCharSets.rxJavaEnc()).
    */
   //////////////////////////////////////////////////////////////////////////////
   @SuppressWarnings("unchecked")
   private InputStream autoCreateDTD(OSBackendDatatank backendTank)
   {
      /* TODOph: Enhance this, taking into acct primary/foreign key constraints,
         whether columns allow null, etc. A second, smarter dtd could be generated
         after the mapper has been fully defined. Add a button to the mapper to allow
         the user to generate the dtd and/or ask them on save. */
      try
      {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         String header = "<?xml version='1.0' encoding='"
            + PSCharSets.rxStdEnc() + "'?>";
         out.write(header.getBytes(PSCharSets.rxJavaEnc()));
         final byte[] newLineBytes = NEW_LINE.getBytes(PSCharSets.rxJavaEnc());
         out.write(newLineBytes);

         String line = new String("<!-- AUTO CREATED by Percussion Rhythmyx -->");
         out.write(line.getBytes(PSCharSets.rxJavaEnc()));
         out.write(newLineBytes);

         PSCollection joins = backendTank.getJoins();
         PSCollection tables = backendTank.getTables();
         String rootElement = getFileBaseName(backendTank);

         Vector<String> masterTableAliases = new Vector<>(5);

         Vector joinInfo = createJoinMap(joins, masterTableAliases);
         if ( null == joinInfo )
         {
            // circular reference in joins found, notify user
            PSDlgUtil.showErrorDialog(
                  E2Designer.getResources().getString("CircJoinsNoSupport"),
                  E2Designer.getResources().getString("CircJoinsNoSupportTitle"));

            // fake info for a single table
            masterTableAliases.add(((OSBackendTable) tables.get(0)).getAlias());
            joinInfo = new Vector(1);
            Vector mapping = new Vector(2);
            mapping.add(((OSBackendTable) tables.get(0)).getAlias());
            mapping.add(new String [0]);
            joinInfo.add(mapping);
         }
         if ( null == joins || joins.isEmpty())
            masterTableAliases.add(((OSBackendTable) tables.get(0)).getAlias());

         // create the first entry
         int masterTableCt = masterTableAliases.size();
         String [] children = new String[masterTableCt];
         for ( int i = 0; i < masterTableCt; ++i )
            children[i] =
               Util.makeXmlName( masterTableAliases.get(i)) + "Set";

         Map<String, String[]> dtd = new HashMap();
         dtd.put( Util.makeXmlName( rootElement ), children );

         // create entry for each master table
         for (String masterTableAlias : masterTableAliases)
            addElements(dtd, (String) masterTableAlias, joinInfo, tables);

         // write out hash map
         for (String key : dtd.keySet()) {
            children = (String[]) dtd.get(key);
            String element = createElement(key, children);
            out.write(element.getBytes(PSCharSets.rxJavaEnc()));
            out.write(newLineBytes);
         }

         out.flush();
         return new ByteArrayInputStream( out.toByteArray());
      }
      catch ( IllegalArgumentException | IOException iae )
      {
         PSDlgUtil.showError(iae);
      }
      return null;   // phtodo throw??
   }


   /**
    * This method is used to recursively build a DTD from a set of tables. It
    * performs the following steps:
    * <UL>
    * <LI>Look up the name of the element by using tableName as a key into nameMap</LI>
    * <LI></LI>
    * <LI>For each column in the table, a child is added to the element</LI>
    * <LI>For each table that is joined to this one, add a child element named <tableMapName>Set</LI>
    * <LI>Add an element for each child set added</LI>
    * <LI>Call this method for the set's child element, (which is a table)</LI>
    * </UL>
    * This method is called on the table that appears in the join info after the
    *
    * @param dtd The stream to which all newly created elements will be added.
    *
    * @param tableAlias The alias name of the table for which we are adding elements.
    *
    * @param joinInfo A vector of vectors. Each vector contains 2 objects, a
    * table alias (String) and a String array. The string array contains a list of table
    * aliases that are joined to the table specified by the first object (on the
    * RHS of the join). If there are no joined tables, the string array will be
    * of length 0.
    *
    * @param tables A list of all tables that are being used to create the DTD.
   **/
   private void addElements(Map<String, String[]> dtd, String tableAlias,
         Vector joinInfo, PSCollection tables )
   {
      // create the 'set' element
      String [] children = new String[1];
      String tableAliasElementName = Util.makeXmlName( tableAlias );
      children[0] = tableAliasElementName + "*";
      dtd.put( tableAliasElementName + "Set", children );

      // find the table we're adding next
      int tableCt = tables.size();
      OSBackendTable table = null;
      for ( int i = 0; i < tableCt; ++i )
      {
         table = (OSBackendTable) tables.get(i);
         if ( table.getAlias().equals( tableAlias ))
            break;
      }
      if ( null == table )
         throw new IllegalArgumentException( "Table alias not found in tables list "
               + tableAlias + ")." );

      Vector columns = table.getColumns();

      int joinInfoCt = joinInfo.size();
      String [] joinedTables = new String[0];
      for ( int i = 0; i < joinInfoCt; ++i )
      {
         Vector v = (Vector) joinInfo.get(i);
         String alias = (String) v.get(0);
         if ( alias.equals( tableAlias ))
         {
            joinedTables = (String[]) v.get(1);
            break;
         }
      }

      // add the cols & joined tables as sets to the list of children for this element
      int columnCt = columns.size();
      children = new String[columnCt + joinedTables.length];
      int childIndex = 0;
      for ( int i = 0; i < columnCt; ++i )
      {
         /* bug fix Rx-00-01-0005. When a column name has characters that are not
            allowed in xml element names (e.g. spaces), an error would occur.
            Fixed by converting bad character to underscore. */
         String col = Util.makeXmlName((String) columns.get(i));
         dtd.put( col, null );
         children[childIndex++] = col;
      }
      for ( int i = 0; i < joinedTables.length; ++i )
         children[childIndex++] = Util.makeXmlName(joinedTables[i]) + "Set";

      dtd.put( tableAliasElementName, children );

      for ( int i = 0; i < joinedTables.length; ++i )
         addElements( dtd, joinedTables[i], joinInfo, tables );
   }


   /**
    * Creates a string containing a single element line. The form of the line
    * depends on the children array. If there is at least 1 child, an element of
    * the form: <p>
    *   <!ELEMENT elementName ( children[0], ..., children[n-1] )> <p>
    * is produced. If no children or children is null, then an element of the form:<p>
    * <!ELEMENT elementName (#PCDATA)><p>
    * is producde.
    *
    * @param elementName The name of the element to be created. Must be a valid
    * XML element identifier.
    *
    * @param children An array of children names. Each child name must include
    * any desired attributes indicating allowed quantities. The child elements
    * are added in the order they appear in the array. Each child must be a
    * valid XML element identifier.
    *
    * @return A string containing the newly created element.
   **/
   private String createElement( String elementName, String [] children )
   {
      String element = "<!ELEMENT " + elementName + " (";
      if ( null == children || 0 == children.length )
         element += "#PCDATA";
      else
      {
         for ( int i = 0; i < children.length; ++i )
         {
            element += children[i];
            if ( i < children.length-1 )
               element += ", ";
         }
      }
      element += ")>";
      return element;
   }

   /**
    * Analyzes the supplied joins and tables, creating a vector that contains a
    * table alias and an array of table aliases that are joined to that
    * table. Each of these vectors is stored in the main vector in a specific
    * order. All 'master' tables are listed first in ascending alpha
    * order. A master table is a table that only appears on the left hand side of
    * a join. The entries in each array are also sorted in ascending alpha order.<p>
    * After the master tables have been added, the tables listed with each master
    * table are added and all tables they are joined to as an array. This
    * continues recursively until all tables have been added to the vector.<p>
    * Each table that appears in a join appears exactly once as the first object
    * in the vector list. Each String array contains a unique list of tables as
    * well. <p>
    * If a circular reference is discovered, null is returned and masterTables
    * is not modified.<p>
    * If joins is null or empty, an empty vector is returned and masterTables is
    * not modified.
    *
    * @param joins A list of PSBackEndJoin objects for a set of joined tables.
    *
    * @param masterTables This is an output param. The vector is filled with the
    * names of all tables that only appear on the LHS of the join. The vector is
    * cleared before adding the new tables. If a vector is returned, there
    * will be at least 1 entry added to masterTables.
    *
    * @return A vector of vectors. Each vector contains 2 objects, a table alias
    * String and a String array containing the aliases of the tables joined to
    * the aforementioned table. If a circular reference is discovered, null is
    * returned.
   **/
   private Vector createJoinMap(PSCollection joins, Vector<String> masterTables)
   {
      Vector<Object> map = new Vector<>(joins.size()*2);
      if ( null == joins || joins.isEmpty())
         return map;

      final Set<String> rightTableAliases = new HashSet<>();

      // scan joins and pull out all the tables on the RHS, making a unique list
      int joinCt = joins.size();
      for (Object o : joins) {
         PSBackEndJoin join = (PSBackEndJoin) o;
         rightTableAliases.add(join.getRightColumn().getTable().getAlias());
      }

      // used for sorting below
      Collator c = Collator.getInstance();
      c.setStrength( Collator.PRIMARY );
      ObjectCollator oc = new ObjectCollator( c );

      /* walk the joins checking if each left table appears on the RHS of any
         join and whether it has already been added to the list */
      final Set<String> masterTablesSet = new HashSet<>();
      for (Object o : joins) {

         PSBackEndJoin join = (PSBackEndJoin) o;
         String alias = join.getLeftColumn().getTable().getAlias();
         // does it appear on RHS ?
         boolean bAppearsRHS = rightTableAliases.contains(alias);
         if (!bAppearsRHS) {
            masterTablesSet.add(alias);
         }

         // has this alias been added to the map yet?
         int mapCt = map.size();
         boolean bFound = false;
         for (int j = 0; j < mapCt && !bFound; ++j) {
            Vector v = (Vector) map.get(j);
            String mappedAlias = (String) v.get(0);
            if (alias.equals(mappedAlias)) {
               bFound = true;
               break;
            }
         }
         if (!bFound) {
            Vector<Object> v = new Vector<>(2);
            v.add(alias);
            // make list of joined tables
            Vector<String> joinedChildren = new Vector<>(5);
            for (int j = 0; j < joinCt; ++j) {
               PSBackEndJoin join2 = (PSBackEndJoin) joins.get(j);
               String leftAlias = join2.getLeftColumn().getTable().getAlias();
               if (leftAlias.equals(alias))
                  joinedChildren.add(join2.getRightColumn().getTable().getAlias());
            }
            if (joinedChildren.size() > 1)
               Collections.sort(joinedChildren, oc);

            String[] children = new String[joinedChildren.size()];
            for (int j = 0; j < children.length; ++j)
               children[j] = (String) joinedChildren.get(j);
            v.add(children);
            map.add(v);
         }
      }

      // check for recursion w/in the join set
      boolean bCircular = false;
      Set<String> joined = new HashSet<String>();
      int mapCt = map.size();
      for ( int i = 0; i < mapCt && !bCircular; ++i )
      {
         joined.clear();
         bCircular = checkJoinForCircularRef( map, 0, joined );
      }
      if ( !bCircular )
      {
         // copy master tables hash map to the supplied vector
         Iterator<String> tables = masterTablesSet.iterator();
         masterTables.clear();
         while ( tables.hasNext())
            masterTables.add( tables.next());
         if ( masterTables.size() > 1 )
               Collections.sort( masterTables, oc );
      }
      else
         System.out.println( "Found circular ref" );

      //todo sort map
      return bCircular ? null : map;
   }

   /**
    * Checks the join at the supplied index, following, following all possible
    * paths as indicated by the joinMap. <p>
    * The joinMap contains the map generated by createJoinMap. For the entry found
    * at the indicated index, we look at all possible joined tables, and for each
    * one, if it is found on the LHS, we recurse back into this method, using the
    * new index, first adding the table name to the joined map. The joined map
    * keeps track of what tables have already been visited.
    *
    * @param joinMap A map created by the createJoinMap method.
    *
    * @param index The index of the joined table to check, starting at 0.
    *
    * @param joined A map of all the tables that have been visited so far. If
    * a joined table is found in this map, a circular reference has been found.
    *
    * @return <code>true</code> if a circular reference is found between the
    * tables by following the joins specified in the joinMap, <code>false</code>
    * otherwise.
   **/
   private boolean checkJoinForCircularRef( Vector joinMap, int index,
         Set<String> joined )
   {
      Vector joinMapping = (Vector) joinMap.get(index);
      String leftTable = (String) joinMapping.get(0);
      String [] joinedTo = (String[]) joinMapping.get(1);

      joined.add(leftTable);
      int joinMappings = joinMap.size();
      boolean bCircular = false;
      for ( int i = 0; i < joinedTo.length && !bCircular; ++i )
      {
         // see if the current joined to table appears on the left side anywhere
         String left = null;
         int j = 0;
         for ( ; j < joinMappings; ++j )
         {
            if ( j != index )
            {
               Vector mapping = (Vector) joinMap.get(j);
               left = (String) mapping.get(0);
               if ( left.equals(joinedTo[i]))
                  break;
            }
         }
         if ( j < joinMappings )
         {
            // repeat the process for these guys
            if ( joined.contains(left))
               bCircular = true;
            else
            {
               bCircular = checkJoinForCircularRef(joinMap, j,
                     new HashSet<String>(joined));
            }
         }
      }
      return bCircular;
   }


   /*
    * Create an XML document using the file path. If successful, the created
    * document is stored in m_dtdDoc, so it can be saved in saveToServer.
    *
    * @return boolean true if successful, false otherwise
    */
   private boolean createXmlDocument()
   {
      String filePath = getFilePath();
      try
      {
         // create the XML document from the local file system and save it
         // set others to null
         File droppedFile = new File(filePath);
         IPSMimeContent content = PSContentFactory.loadXmlFile(droppedFile);
         Reader in = PSContentFactory.getReader(content);
         Document xmlDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         setDtdTree( PSDtdGenerator.generate( xmlDoc ));
         return true;
      }
      catch (FileNotFoundException e)
      {
         final Object[] astrParams =
         {
            filePath,
            e.toString()
         };
         PSDlgUtil.showErrorDialog(
               MessageFormat.format(
                     E2Designer.getResources().getString("FileNotFound"), astrParams),
               E2Designer.getResources().getString("OpErrorTitle"));
      }
      catch (SAXException e)
      {
         final Object[] astrParams =
         {
            filePath,
            e.toString()
         };
         PSDlgUtil.showErrorDialog(
               MessageFormat.format(
                     E2Designer.getResources().getString("InvalidXmlDocument"), astrParams),
               E2Designer.getResources().getString("OpErrorTitle"));
      }
      catch (IOException e)
      {
         // this should never happen
         e.printStackTrace();
      }
      catch ( IllegalArgumentException e )
      {
         PSDlgUtil.showError(e);
      }
      return false;
   }

   /*
    * Reads a DTD from the file found at the supplied path. If any errors occur,
    * a message is displayed to the end user regarding the error. The base name
    * of the file must match the root element name in the DTD.
    *
    *
    * @param filePath the location of the DTD file on the local disk
    *
    * @param rootName the root element name of the DTD (not currently used)
    *
    * @return boolean <code>true</code> if successful, <code>false</code> otherwise
    */
   private boolean createFromDtd( String filePath /*, String rootName*/ )
   {
      boolean isSuccessful = false;
      try
      {
         setDtdTree( new PSDtdTree( new URL( "file", null, -1, filePath )));
         isSuccessful = true;
      }
      catch ( PSCatalogException e )
      {
         PSDlgUtil.showError(e, false, E2Designer.getResources().getString("ExceptionTitle"));
      }
      catch ( MalformedURLException mue )
      {
         // This should not happen unless the filePath is invalid
         PSDlgUtil.showError(mue, false, E2Designer.getResources().getString("ExceptionTitle"));
      }
      return isSuccessful;
   }


   /**
    * If this object has a DTD, it is added to the CatalogReceiver. This class
    * has 2 DTDs, an original DTD that was set on the object and a merged DTD
    * that is the result of merging this DTD w/ DTDs from ResultPages attached
    * to the dataset that owns this tank. The original DTD is returned by this
    * method (the original DTD has not been modified in any way by the end user).
    *
    * @param iObjType The type of data that the catalog wants.
    *
    * @param objContainer A CatalogReceiver object, that stores a list of 
     * PSDtdTree objects.
    *
    * @see IDataCataloger
    */
   public void catalogData(ObjectType iObjType, CatalogReceiver objContainer)
   {
      if (iObjType.equals(ObjectType.XML_DTD) && null != m_originalDtdTree )
         objContainer.add( m_originalDtdTree );
   }


   //////////////////////////////////////////////////////////////////////////////
   // IGuiLink interface implementation
   public void setFigure(UIFigure fig)
   {
      m_owner = fig;
   }

   public void release()
   {
      m_owner = null;
      releasePurgableTempFile();
   }

   public UIFigure getFigure()
   {
      return m_owner;
   }

   //////////////////////////////////////////////////////////////////////////////
   // IPersist interface implementation
   public boolean load(PSApplication psApp, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object[] astrParams = {"OSDataset"};
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      OSApplication app = null;
      if (psApp instanceof OSApplication)
         app = (OSApplication) psApp;
      else
      {
         throw new IllegalArgumentException("Expected OS Application");
      }

      try
      {
         if (store instanceof OSDataset)
         {
            // restore the data part
            OSDataset dataset = (OSDataset) store;
            this.copyFrom((OSPageDatatank) dataset.getPageDataTank());
         }
         /*
          * save app reference for possible use during dnd. We must do this
          * after the copyFrom, because the copyFrom doesn't transfer the app.
          */
         m_app = app;

         // restore GUI information
         OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);

         int currentId = this.getId();
         if (config.getProperty(KEY_SCHEMA_SOURCE_READONLY + currentId) != null)
         {
            m_readOnly = Boolean.valueOf(config.getProperty(
               KEY_SCHEMA_SOURCE_READONLY + currentId));
         }
         
         m_filePath = config.getProperty(KEY_PAGE_DATATANK_FILE_PATH
            + currentId);
         
         if (config.getProperty(KEY_ORIG_SRC_TYPE + currentId) != null)
         {
            m_originalSrcType = new Integer(config.getProperty(
               KEY_ORIG_SRC_TYPE + currentId)).intValue();
         }
         else
         {
            // try to guess based on extension, we aren't guaranteed that it is
            // correct
            String fileSchema = getSchemaSource().getFile();
            if (fileSchema.endsWith(DEFAULT_FORM_EXT))
            {
               m_originalSrcType = SRC_TYPE_FORM;
            }
            else if (null != m_filePath)
            {
               if (Util.isHtml(m_filePath))
                  m_originalSrcType = SRC_TYPE_HTML;
               else if (m_filePath.endsWith(DEFAULT_DTD_EXT))
                  m_originalSrcType = SRC_TYPE_DTD;
               else
                  m_originalSrcType = SRC_TYPE_XML;
            }
            else
               m_originalSrcType = SRC_TYPE_BACKEND;
         }
         
         
         // load the merged dtd first
         String mergedFileName = getSchemaSource().getFile();
         File mergedDtdName = new File(mergedFileName);
         try
         {
            String baseName = Util.stripPath(mergedFileName, "");
            InputStream origDtdStream = m_app.loadAppFile(mergedDtdName);

            /*
             * It's not documented, but the PSDtdTree requires that the input
             * stream be resetable if the dtd is a pure dtd as opposed to a dtd
             * inside an xml document. Byte array streams are resetable. If the
             * input stream is from a file, it won't be resettable (I don't
             * understand why not.)
             */
            if (!origDtdStream.markSupported())
            {
               byte[] b = new byte[origDtdStream.available()];
               origDtdStream.read(b);
               origDtdStream = new java.io.ByteArrayInputStream(b);
            }

            /*
             * The base name is only needed if the content is a true DTD. If
             * not, the doctype is not needed.
             */
            m_tree = new PSDtdTree(origDtdStream, baseName, 
               PSCharSets.rxJavaEnc());
         }
         catch (PSServerException se)
         {

            PSDlgUtil.showErrorDialog(
               E2Designer.getResources().getString("MissingDtdFile"), 
               E2Designer.getResources().getString("MissingDtdFileTitle"));
            return false;
         }
         catch (PSCatalogException ce)
         {
            // AT -> another bad way to doing things... however, this
            // reconstruction of the tree is needed for updateResources. Reason:
            // if the .frm file was created from Rx1.0, the .frm will be an XML
            // document without a valid dtd. This causes problems when
            // constructing a PSDtdTree since it needs a valid DTD file or a
            // valid
            // XML DOCTYPE. As of Rx1.1, .frm files will always be wrapped in a
            // valid XML DOCTYPE. Thus, wrap the .frm in an XML DOCTYPE here.
            if (SRC_TYPE_FORM == m_originalSrcType)
            {
               // load appFile again from server
               Document frmDoc = PSXmlDocumentBuilder.createXmlDocument(
                  m_app.loadAppFile(mergedDtdName), false);
               m_tree = PSDtdGenerator.generate(frmDoc);
            }
            else
            {
               ce.printStackTrace();
            }
         }

         /*
          * loading m_treeOriginal from <name>.<ext> file on the server. If
          * this was created from a form, there will be no original file, only
          * the merged version (which is the same as the original because form
          * tanks are never merged.
          */
         if (SRC_TYPE_FORM != m_originalSrcType)
         {
            try
            {
               String origFileName = Util.stripPath(mergedFileName,
                  LOCAL_DTD_EXT);
               String baseName = Util.stripPath(origFileName, "");

               InputStream origDtdStream = null;
               origDtdStream = m_app.loadAppFile(new File(origFileName));
               /*
                * It's not documented, but the PSDtdTree requires that the input
                * stream be resetable if the dtd is a pure dtd as opposed to a
                * dtd inside an xml document. Byte array streams are resetable.
                * If the input stream is from a file, it won't be resettable (I
                * don't understand why not.)
                */
               if (!origDtdStream.markSupported())
               {
                  byte[] b = new byte[origDtdStream.available()];
                  origDtdStream.read(b);
                  origDtdStream = new java.io.ByteArrayInputStream(b);
               }

               /*
                * The base name is only needed if the content is a true DTD. If
                * not, the doctype is not needed.
                */
               m_originalDtdTree = new PSDtdTree(origDtdStream, baseName,
                  PSCharSets.rxJavaEnc());
            }
            catch (FileNotFoundException e)
            { /*
                * ignore, this may be a 1.0 app, the original tree will be set
                * to the merged tree below
                */
            }
            catch (PSServerException se)
            { /*
                * ignore, this may be a 1.0 app, the original tree will be set
                * to the merged tree below
                */
            }

         }
         
         return true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      catch ( Error err )
      {
         err.printStackTrace();
      }
      catch ( Throwable t )
      {
         t.printStackTrace();
      }
     finally
     {
       // phTodo: create generic notification framework for data changes
       if ( null != m_owner )
         m_owner.invalidateLabel();
       if ( null == m_originalDtdTree)
          m_originalDtdTree = m_tree;
     }

     return false;
   }

   //////////////////////////////////////////////////////////////////////////////
   // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object[] astrParams =
         {
            "OSDataset or OSContentEditor"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this page tank into the provided dataset
         if(store instanceof OSDataset)
         {
            OSDataset dataset = (OSDataset) store;
            dataset.setPageDataTank(/*(PSPageDataTank)*/ this);
            m_dataset = (UIConnectableFigure) dataset.getFigure();
         }

         if (app != null && app instanceof OSApplication)
         {
            saveToServer((OSApplication) app);
            m_dataset = null;
         }

         // save app reference for possible use during dnd
         m_app = (OSApplication) app;
         // then store all keys with the new ID created.
         int newId = this.getId();
         config.setProperty(KEY_SCHEMA_SOURCE_READONLY + newId, Boolean.toString(m_readOnly));
         if (m_filePath != null)
            config.setProperty(KEY_PAGE_DATATANK_FILE_PATH + newId, m_filePath);
         config.setProperty( KEY_ORIG_SRC_TYPE + newId, new Integer( m_originalSrcType ).toString());

         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, newId, config, m_owner);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData interface implementation
   public boolean prepareSourceForDrop(DropAction action, String filePath, String rootName)
   {
      // just do this once
      if ( m_bSourceIsPrepared || null == filePath )
         return true;

      if (rootName == null)
      {
         rootName = Util.stripPath( filePath, "" );
      }

      // indicate this has been prepared
      m_bSourceIsPrepared = true;

      // set dropped page objects to read only
      setSchemaSourceReadOnly();

      switch (action)
      {
         case UPDATE:
         case QUERY:
         {
            boolean isSuccessful = false;
            if ( isDTD())
               isSuccessful = createFromDtd( filePath/*, rootName*/ );
            else if (isXML())
               isSuccessful = createXmlDocument();
           // else if (isHTML())
              // isSuccessful = createFromHtml(action, filePath);//This is change in 5.4
            return isSuccessful;
         }
         case STATIC:
            if (isHTML())
               return true;
            return false;

         case XSL:
            return true;
      }

      // this should never happen
      return false;
   }

   /**
    * Saves the merged DTD and the original DTD to the server. If tank was
    * created for an Update, only the merged DTD is saved (which is the original
    * DTD because no merging is done for Update tanks).
    * It also saves the source file, if it has not been saved as a part of the
    * object.
    * @todo Change ICustomDropSourceData interface to use OSApplication
    */
   private boolean saveToServer(OSApplication app)
   {
      if ( app == null )
      {
         throw new IllegalArgumentException( "app must not be null" );
      }

      try
      {
         InputStream is = null;

         PSDtdTree tree = getDtdTree( m_dataset );

         if ( null == tree )
            return true;   // nothing to save

         File file = new File(getSchemaSource().getFile());

         String baseName = Util.stripPath(  file.getName(), "" );

         boolean wrapInXml = !tree.getRoot().getElement().getName().equals( baseName );

         String str=tree.toDTD( wrapInXml );

         is = new ByteArrayInputStream(str.getBytes());

         app.saveAppFile( file, is );
         //System.out.println("Merged DTD: App file is " + file.toString());

         tree.setTreeDirty(false);
         //OSLoadSaveHelper.logSaveApplicationFile(dtdFile.getFileName());

         ///////// saving original dtd file to server as an <name>.<ext> file////////
         if ( null != m_originalDtdTree && SRC_TYPE_FORM != m_originalSrcType )
         {
            is = new ByteArrayInputStream(m_originalDtdTree.toDTD(
               wrapInXml).getBytes());

            file = new File( baseName + LOCAL_DTD_EXT );
            //System.out.println("Original DTD: App file is " + file.toString());
            app.saveAppFile( file, is );
            //OSLoadSaveHelper.logSaveApplicationFile(dtdFile.getFileName());
         }
      }
      catch (Exception e)
      {
         // todoph Catch all exceptions individually
         e.printStackTrace();
         return false;
      }
      //Now save the source file
      return saveSourceFile(app);
   }

   /**
    * Interface method unimplemented.
    */
   public void cleanup(OSApplication app)
   {
      // delete the dtd or xml source file
      File file = new File(getSchemaSource().getFile());
      app.removeAppFile(file);
      
      // delete the "local" or backup dtd if we have one
      if (m_originalDtdTree != null && m_originalSrcType != SRC_TYPE_FORM)
      {
         String baseName = Util.stripPath( file.getName(), "");
         file = new File( baseName + LOCAL_DTD_EXT );
         app.removeAppFile(file);
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
   public String getFilePath()
   {
      return m_filePath;
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
   public void setFilePath(String filePath)
   {
      m_filePath = filePath;
      setTempFilePath(filePath);
   }

   //////////////////////////////////////////////////////////////////////////////
   // ICustomDropSourceData
   public void setUsedLocalFileSystem()
   {
      // no - op  since now we check to see if m_tempFilePath or m_xmlDoc or
      // m_dtdDoc to be not null
      // see isUsingLocalFileSystem method
   }


   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public String getServerFileName()
   {
      return getSchemaSource().getFile();
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public void setTempFilePath( String path )
   {
      //Needed for saving the source file
      m_tempFilePath = path;
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public String getTempFilePath()
   {
      return m_tempFilePath;
   }

   //////////////////////////////////////////////////////////////////////////////
   // IAuxFilesTransfer
   public boolean hasFiles()
   {
      return (m_filePath != null);
   }

   public boolean isUsingDTD()
   {
      return SRC_TYPE_FORM != m_originalSrcType;
   }

   public PSDtdTree getTree()
   {
      return ( getDtdTree() );
   }

   /**
    * Saves the original source file that was dragged and dropped onto the
    * workbench. The source file will be saved in <rxroot>/<appRoot>/Src dir.
    * If a source file is a part of the object created when  the
    * source file was dropped onto the workbench (Eg: Objects created by UPDATE
    * and STATIC actions) then the source file will not be saved here,
    * since it is already saved in <rxroot>/<appRoot>.
    * @param app an application which this file belongs to, it is never
    * <code>null</code>
    * @return <CODE>true</CODE> if the file was saved successfully, if an
    * exception is caught during this process <CODE>false</CODE> is returned.
    */
   private boolean saveSourceFile(OSApplication app)
   {
      try
      {
         /*If this source file is not xml file or html file do not save it,
          already saved as an application file*/
         if(!isXML() && !isHTML())
            return true;

         File tempFile = null;

         /*If this file has been saved as a part of the object, do not save it
         in the application's source dir*/
         if(m_tempFilePath != null)
            tempFile = new File(this.m_tempFilePath);
         else
            return true;

         final IPSMimeContent content = isHTML()
               ? PSContentFactory.loadHtmlFile(tempFile)
               : PSContentFactory.loadXmlFile(tempFile);  
         final InputStream source = content.getContent();
         String fileName = Util.stripPath(getFilePath());
         String newFilePath = SRC_DIR + File.separatorChar + fileName;
         File droppedFile = new File (newFilePath);
         app.saveAppFile(droppedFile,source);

         if(!(getTempFilePath().equals(getFilePath())))
         {
            if(tempFile != null)
                  tempFile.delete();
         }
         setTempFilePath(null);

      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
      return true;
   }

   /**
    * Cleans up the temporary file if there is one.
    */
   protected void finalize() throws Throwable
   {
      if ( null != m_tempFilePath && !m_tempFilePath.equals(m_filePath))
      {
         File f = new File( m_tempFilePath );
         f.delete();
         m_tempFilePath = null;
      }
      super.finalize();
   }

   //////////////////////////////////////////////////////////////////////////////
   // Serializable interface optional implementation
   /**
    * Overide the serialization method to create and write the AuxFilesTransferHelper
    * object, if it is necessary. It is necessary if there is data in a file to
    * transfer.
   **/
   private  void writeObject(java.io.ObjectOutputStream stream)
       throws IOException
   {
      stream.defaultWriteObject();
      if( hasFiles())
      {
         AuxFilesTransferHelper auxFilesHelper = null;
         if ( null != m_tempFilePath )
            auxFilesHelper = new AuxFilesTransferHelper( m_tempFilePath );
         else if ( null != m_app && null != m_filePath )
            auxFilesHelper = new AuxFilesTransferHelper( m_app );
         if ( null != auxFilesHelper )
            stream.writeObject(auxFilesHelper);
      }

   }

    /**
    * Overide the serialization method to read the AuxFilesTransferHelper
    * object.
   **/
   private  void readObject(java.io.ObjectInputStream stream)
       throws IOException, ClassNotFoundException
   {
      stream.defaultReadObject();
      if ( hasFiles())
      {
         AuxFilesTransferHelper auxFilesHelper = (AuxFilesTransferHelper)stream.readObject();
         releasePurgableTempFile();
         m_tempFile = auxFilesHelper.getFile(this);
         setTempFilePath(m_tempFile.getPath());
      }
   }


   /**
    * The local variables that store the DTD trees should only be accessed via
    * these 2 methods (getDtdTree()). This method sets the original DTD and
    * resets the merged tree so it will be calculated the next time it is needed.
    * The column catalog cache is also cleared.
    *
    * @param dtd The dtd that is to be owned by this tank. Any former dtd is
    * lost.
   **/
   private void setDtdTree( PSDtdTree dtd )
   {
      m_originalDtdTree = dtd;
      m_tree = null;
      m_columns = null;
   }


   /**
    * Calls the parameterized version with a null parameter.
    *
    * @see #getDtdTree( UIConnectableFigure )
   **/
   private PSDtdTree getDtdTree()
   {
      //System.out.println( "  USED OSPageDatatank.getDtdTree()!!!!!" );
      return getDtdTree( null );
   }

   /**
    * Checks if the current merged tree is up to date, performing any necessary
    * merge, then returns the result. The merge is performed by scanning all
    * Web pages attached to the output of the dataset that owns this tank.
    *
    * @param dataset The dataset that owns this tank. If it is null, an attempt
    * will be made to get the owning dataset through the owning GUI figure. If
    * there is no GUI figure, or the owning dataset cannot be obtained, the merged
    * tree is set to equal the original tree and returned.
    *
    * @return The local reference to the DTD 'tree', which may be null if there
    * is no original tree.
   **/
   private PSDtdTree getDtdTree( UIConnectableFigure dataset )
   {
      if ( null == m_originalDtdTree )
         return null;

      try
      {
         if ( null == dataset )
         {
            UIFigureFrame frame = Util.getOwnerFrameOf( this.getFigure() );

            // TODO: this is a kludge
            if ( null != frame )
               dataset = (UIConnectableFigure) ((UIPipeFrame)frame).getDataset().getFigure();
         }
      }
      catch ( IllegalArgumentException e )
      { /* ignore, the code below handles a null dataset */ }

      try
      {
         if ( null == dataset && null == getFigure())
         {
            m_tree = m_originalDtdTree;
         }
         else if ( isMergeDirty())
         {
            /* Keep a copy of the original so we can restore repeat attributes
               after merging the trees. We do this so user modified repeat attributes
               are not lost after a merge. */
            PSDtdTree userModTree = m_tree;
            Vector dtdList = CatalogDtds.getCatalog( dataset, true );
            m_tree = (PSDtdTree)m_originalDtdTree.clone();
            PSDtdTreeMergeManager merger = new PSDtdTreeMergeManager( m_tree );
            Enumeration dtdEnum = dtdList.elements();
            while ( dtdEnum.hasMoreElements() )
            {
               PSDtdTree t = (PSDtdTree)dtdEnum.nextElement();
               merger.mergeDtdTree( t );
            }

            // restore any user modified repeat attributes
            if ( null != userModTree )
            {
               m_tree = merger.updateTreeForUserMod( m_tree, userModTree );
            }
         }
      }
      catch ( CloneNotSupportedException e )
      {
         // shouldn't be thrown unless PSDtdTree stops supporting it
         System.out.println( "Unexpected exception during clone of DTD tree" );
      }
      return m_tree;
   }

   /**
    * @return <code>true</code> if any DTDs that the merged dtd depend on have
    * changed since the last merge, <code>false</code> otherwise
   **/
   private boolean isMergeDirty()
   {
      boolean bRet=true;
      if(  SRC_TYPE_FORM != m_originalSrcType )
      {
         // process here
      }
      else
      {
         bRet=false;   // is an frm form!
      }
      // phtodo implement
      return (bRet);
   }

   /**
    * Release the purgable temp file if it is not <code>null</code>.
    */
   private void releasePurgableTempFile()
   {
      if (m_tempFile != null)
      {
         m_tempFile.release();
         m_tempFile = null;
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the owner of this data object
    */
   private UIFigure m_owner;

   private static final String KEY_SCHEMA_SOURCE_READONLY = new String("schemaSourceReadOnly");
   /**
    * cache for the cataloged fileds
    */
   private Vector<String> m_columns;
   /**
    * status flag which indicates wether or not the source preparation from the
    * local file system has been done
    */
   private boolean m_bSourceIsPrepared;
   /**
    * The merged DTDs. This is set to null if the original changes to indicate
    * that a new merge needs to be done.
    */
   private PSDtdTree m_tree;
   /**
    * The original dtd tree that is created when the pagetank is first created.
    * It will only change if the pagetank itself is updated with a new DTD.
    */
   private PSDtdTree m_originalDtdTree = null;
   /**
    * the read only flag, if true, the schema source is read only
    */
   private boolean m_readOnly = false;
   /*
    * the file path from the local file system. we need this to save the original
    * path while dropping a file from the browser. If this tank was not created
    * from a file on the local system, it is null.
    */
   private String m_filePath = null;
   /**
    * the file path from the local file system of the temporary file.
    */
   // no need to serialize, it will be replaced if deserialized
   transient  private String m_tempFilePath = null;

  /**
   * The reference to the temp file object. This is to prevent G.C.'ed while it
   * is in use. It is <code>null</code> if has not set.
   * No need to be serialized, used for runtime only.
   */
  transient private PSPurgableTempFile m_tempFile = null;

   /*
    * The type of the source used to originally create this tank.
    */
   private int m_originalSrcType = SRC_TYPE_UNKNOWN;

   /**
    * We keep a reference to the app around in case we are serialized and
    * we need to copy an application file that is currently saved on the server.
    * The application name is serialized (thru a helper), then on deserialization,
    * the helper reads the data from the original app and creates a temp file.
    * This is only used if we aren't currently owned by a uic figure.
   **/
   transient private OSApplication m_app = null;

   /**
    * This is only used as a 'hidden' parameter when save() calls saveToServer().
    * At all other times, it is null. When used, it is the dataset that owns this
    * tank.
   **/
   transient private UIConnectableFigure m_dataset = null;

   private static final String NEW_LINE = "\r\n";

   /**
    * A unique (within the app objects) extension for DTDs that are saved for
    * later use by this object, but not the server. The ext must be unique from
    * extensions used by other objects saving DTDs. The extension includes
    * the prefix dot ('.').
   **/
   private static final String LOCAL_DTD_EXT = ".pdt";
   /** The canonical DTD extension identifier. */
   private static final String DEFAULT_DTD_EXT = ".dtd";
   /** The canonical extension when the source is an HTML form for updating. */
   private static final String DEFAULT_FORM_EXT = ".frm";

   // user property keys
   private static final String KEY_PAGE_DATATANK_FILE_PATH = "pageDatatankFilePath";
   private static final String KEY_ORIG_SRC_TYPE = "pageDatatankSrcType";
}

