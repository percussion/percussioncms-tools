/******************************************************************************
 *
 * [ PSContentEditorTableHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCEDataTypeInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSErrorCodes;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.catalogers.PSCatalogDatabaseTables;
import com.percussion.client.catalogers.PSSqlCataloger;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.IPSCETableColumnActions;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.cms.IPSConstants;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcPrimaryKey;
import com.percussion.tablefactory.PSJdbcTableComponent;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.util.PSCollection;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class to handle the table and column creation for the content type
 * editors.
 */
public class PSContentEditorTableHandler
{
   /**
    * Constructor, initializes the parent field set, parent mapper and 
    * container locator. In case of shared defs, these are from shared group.
    * 
    * @param parentSet PSFieldSet object of the local def or shared group.
    * Cannot be <code>null</code>.
    * @param parentMapper PSDisplayMapper of local def or shared group.
    * Cannot be <code>null</code>.
    * @param parentLocator PSContainerLocator of local def or shared group
    * Cannot be <code>null</code>
    * @param editorType
    * @param actions the object that implements the 
    * <code>IPSCETableColumnActions</code> interface that makes column
    * actions available. Cannot be <code>null</code>.
    * 
    * @throws PSUninitializedConnectionException
    */
   public PSContentEditorTableHandler(PSFieldSet parentSet,
         PSDisplayMapper parentMapper, PSContainerLocator parentLocator, int editorType,
         IPSCETableColumnActions actions)
         throws PSUninitializedConnectionException {
      if(parentSet == null)
         throw new IllegalArgumentException("parentSet cannot be null.");
      if(parentMapper == null)
         throw new IllegalArgumentException("parentMapper cannot be null.");
      if(parentLocator == null)
         throw new IllegalArgumentException("parentLocator cannot be null.");
      if(actions == null)
         throw new IllegalArgumentException("actions cannot be null.");
      m_parentFieldSet = parentSet;
      m_parentMapper = parentMapper;
      m_parentLocator = parentLocator;
      m_editorType = editorType;
      m_objectStore = PSProxyUtils.getObjectStore();
      m_columnActions = actions;
   }

   /**
    * Identifies the new tables and columns used in the mapper and creates them.
    * If the fieldset's isNameChanged property is <code>false</code> or if
    * there are no local field mappings in parent (main) editor, it displays a
    * warning message and does not continue with create process. Displays an
    * error message when an exception happens. Displays a warning message if the
    * server fails to create or alter tables for this editor.
    * 
    * @throws IOException
    * @throws PSJdbcTableFactoryException
    * @throws PSServerException
    * @throws PSAuthenticationFailedException
    * @throws PSAuthorizationException
    * @throws PSModelException
    */
   public void createAlterTablesForEditor() throws PSAuthorizationException,
         PSAuthenticationFailedException, PSServerException,
         PSJdbcTableFactoryException, IOException, PSModelException
   {
      StringBuffer errorMessage = new StringBuffer();
      /*
       * Map of table name as key and set name as value to hold the relationship
       * between table and fieldset to update the fieldset after creating or
       * altering the table is successful.
       */
      Map<String, String> setTableNameMap = new HashMap<String, String>();

      /*
       * Map of table name as key and map of field name and column name as value
       * to update the new fields of the fieldset with correct column names
       * after altering the table.
       */
      Map tableColumnInfo = new HashMap();

      // if none of fields of parent field set are mapped, we don't
      // create tables
      if (m_editorType == EDITOR_TYPE_LOCAL
            && !doesMappingsExist(m_parentFieldSet, m_parentMapper))
      {
         //No local mappings no tables needs to be created return
         return;
      }
      /*
       * Checks all fieldsets(parent and children) whether they have new local
       * fields that were not specified with proper datatype and format and pops
       * up a warning message.
       */
      List fieldsetNames = getFieldSetsWithInvalidFields(m_parentFieldSet);
      if (!fieldsetNames.isEmpty())
      {
         String msg = "The mappers {0} contain fields which need"
               + " to be created but do not have a datatype and "
               + "format specified. The columns corresponding to "
               + "these fields will not be created and thus the "
               + "application will not initialize properly if you "
               + "attempt to start it.";
         errorMessage.append(MessageFormat.format(msg, new Object[]
         {fieldsetNames.toString()}));
      }

      final PSJdbcTableSchemaCollection tableDef =
            createTableSchemas(m_parentLocator, m_parentFieldSet,
            m_parentMapper, setTableNameMap, tableColumnInfo);

      if (tableDef.size() != 0)
      {
         PSTableLocator tableLocator;
         try
         {
            tableLocator = getSystemTableLocator();
         }
         catch (Exception e)
         {
            throw new PSServerException(e);
         }
         Document doc = m_objectStore.saveTableDefinitions(tableLocator,
               tableDef);

         PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
         Element element = null;
         while ((element = tree.getNextElement(true)) != null)
         {
            String tableName = element.getAttribute(XATTR_TABLENAME);
            String create = element.getAttribute(XATTR_CREATE);
            boolean isCreate = false;
            if (create != null && create.equals(XATTR_VALUE_YES))
               isCreate = true;

            /*
             * In case of success of each table processing, update the fieldset
             * with table or column information. In case of error log the error
             * message. If the error is for altering the table, update the
             * fieldset with new columns created before alter process failed.
             * The alter process may fail after adding some of new columns.
             */
            if (element.getTagName().equals(XELEM_SUCCESS))
            {
               if (tableName != null)
               {
                  setNewFieldLocators(setTableNameMap.get(tableName),
                        (Map) tableColumnInfo.get(tableName), tableName);
               }
               else
                  throw new RuntimeException("missing table name attribute");
            }
            else if (element.getTagName().equals(XELEM_ERROR))
            {
               errorMessage.append(tree.getElementData(element).trim());
               errorMessage.append("\n");
               
               if (!isCreate && tableName != null)
               {
                  String setName = setTableNameMap.get(tableName);
                  if(StringUtils.isNotBlank(setName))
                     setNewFieldLocators(m_parentLocator, setName, (Map) tableColumnInfo.get(tableName),
                        tableName);
               }
            }

         }
      }

      if (StringUtils.isNotBlank(errorMessage.toString()))
      {
         throw new PSModelException(PSErrorCodes.RAW, errorMessage.toString());
      }
   }

   /**
    * Prepares the list of new and modified tables referred by passed in
    * fieldset and it's child fieldsets. Creates table schemas for the new
    * fieldsets which do not have reference to backend table. If the fieldset
    * has reference to the table, then it checks for any new fields added to the
    * set and creates schema for altering the table by adding new columns
    * corresponding to the new fields.
    * 
    * @param locator a container locator; assumed not <code>null</code>.
    * @param set A set to create or modify the associated table for, assumed not
    *           to be <code>null</code>
    * 
    * @param setTableNameMap map of table name as key and set name as value to
    *           hold the relationship between table and fieldset to update
    *           fieldset after creating or altering of table is successful,
    *           assumed not to be <code>null</code>, may be empty.
    * 
    * @param tableColumnInfo map of table name as key and map of field name
    *           (key) and column name(value) as value to update the new fields
    *           of the fieldset with correct column names, after creation of new
    *           columns of table is successful, assumed not to be
    *           <code>null</code>, may be empty.
    * 
    * @return a new PSJdbcTableSchemaCollection that contains each table to
    *         create or alter. Never <code>null</code>, but may be empty.
    * 
    * @throws IllegalStateException if table name of the field set is
    *            <code>null</code>
    * @throws PSJdbcTableFactoryException if table factory reports this error
    * @throws IOException if table factory reports this error
    * @throws PSAuthorizationException if authorization exception happens while
    *            cataloging columns of the table.
    * @throws PSAuthenticationFailedException if authentication exception
    *            happens while cataloging columns of the table.
    * @throws PSServerException if any exception happens while cataloging
    *            columns of the table.
    */
   private PSJdbcTableSchemaCollection createTableSchemas(
         PSContainerLocator locator, PSFieldSet set, PSDisplayMapper mapper,
         Map<String, String> setTableNameMap, Map tableColumnInfo)
         throws PSJdbcTableFactoryException, IOException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSServerException
   {
      PSJdbcTableSchemaCollection tableSchemas = new PSJdbcTableSchemaCollection();

      // If mappings exist for this set, only then create or alter tables
      if (doesMappingsExist(set, mapper))
      {
         PSJdbcTableSchema table = null;
         if (StringUtils.isBlank(getTableAlias(set))) // new fieldset, so create
         // new table
         {
            table = createNewTableSchema(set, locator, mapper, setTableNameMap,
                  tableColumnInfo);
         }
         else
         {
            // add any new columns to existing table
            table = createAlterTableSchema(set, locator, mapper,
                  setTableNameMap, tableColumnInfo);
         }

         if (table != null)
            tableSchemas.add(table);
      }

      // recurse into all child field sets
      Iterator allData = set.getAll();
      while (allData.hasNext())
      {
         Object obj = allData.next();
         if (obj instanceof PSFieldSet)
         {
            PSFieldSet chSet = (PSFieldSet) obj;
            if (!(chSet.getType() == PSFieldSet.TYPE_COMPLEX_CHILD || chSet
               .getType() == PSFieldSet.TYPE_SIMPLE_CHILD))
               continue;
            PSDisplayMapper chMapper = mapper.getMapping(chSet.getName()).getDisplayMapper();
            tableSchemas.addAll(createTableSchemas(locator, chSet,
                  chMapper, setTableNameMap, tableColumnInfo));
         }
      }

      return tableSchemas;
   }

   /**
    * Sets table references in the locator by going through the field sets in
    * mapper.
    */
   public void setTableReferences()
   {
      final List<PSTableRef> tableRefs = new ArrayList<PSTableRef>();
      Map backEndTables = m_parentLocator.getBackEndTables();
      getTableNames(m_parentFieldSet, tableRefs, backEndTables);
      if (tableRefs.size() > 0)
      {
         Iterator iter = m_parentLocator.getTableSets();
         while (iter.hasNext())
         {
            PSTableSet tableset = (PSTableSet) iter.next();
            tableset.setTableRefs(new PSCollection(tableRefs.iterator()));
         }
      }
   }

   /**
    * Gets all tables referred by this field set and in all it's child fields
    * and fieldsets. It determines the table name in case of new fieldset.
    * Please see {@link #getTableName(PSFieldSet, Map)} for new table names
    * convention.
    * 
    * @param fieldset the field set from which to get all tablenames, assumed
    * not to be <code>null</code>.
    * @param tableRefs the list of table names, assumed not <code>null</code>.
    * All tables referred are added to this list.
    * @throws IllegalStateException if no backend table is found in the supplied
    * fieldset.
    */
   private void getTableNames(PSFieldSet fieldset, List<PSTableRef> tableRefs,
         Map backEndTables)
   {
      // new field set
      String aliasName = getTableAlias(fieldset);

      if (aliasName == null)
      {
         String tableName = getTableName(fieldset, backEndTables);
         tableRefs.add(new PSTableRef(tableName, tableName));
      }
      else
      {
         PSBackEndTable table = (PSBackEndTable) backEndTables.get(aliasName
               .toLowerCase());
         if (table == null)
         {
            Object[] params =
            {aliasName};
            throw new IllegalStateException(MessageFormat.format(
                  "The table with alias <{0}> is "
                        + "not found in table references list.", params));
         }

         tableRefs.add(new PSTableRef(table.getTable(), table.getAlias()));
      }

      Iterator fields = fieldset.getAll();
      while (fields.hasNext())
      {
         Object test = fields.next();

         if (test instanceof PSField)
         {
            PSField field = (PSField) test;
            IPSBackEndMapping locator = field.getLocator();
            if (locator instanceof PSExtensionCall)
            {
               PSExtensionCall exit = (PSExtensionCall) locator;
               Iterator parameters = exit.getParameters().iterator();
               while (parameters.hasNext())
               {
                  PSExtensionParamValue parameter = (PSExtensionParamValue) 
                     parameters.next();
                  IPSReplacementValue value = parameter.getValue();
                  if (value instanceof PSBackEndColumn)
                  {
                     PSBackEndColumn column = (PSBackEndColumn) value;
                     String tableAlias = column.getTable().getAlias();
                     tableRefs.add(new PSTableRef(tableAlias, tableAlias));
                  }
               }
            }
         }
         else if (test instanceof PSFieldSet)
         {
            PSFieldSet fieldSet = (PSFieldSet) test;
            if (!fieldSet.isSharedFieldSet())
               getTableNames(fieldSet, tableRefs, backEndTables);
         }
      }
   }

   /**
    * This method is useful to update the column information to the new fields
    * of existing fieldset (referring to an existing table) in case of altering
    * fails for any reason. As alter may fail after creating some of new columns
    * we have to update the fields to which the columns are created.
    * 
    * @param locator the locator of backend tables, assumed not to be
    *           <code>null</code>
    * 
    * @param setName the name of the set referring to the supplied table name,
    *           assumed not to be <code>null</code> or empty.
    * 
    * @param fieldColumnMap map of field name as key and column name as value to
    *           update the new fields of the fieldset with correct column names,
    *           assumed not to be <code>null</code>.
    * 
    * @param tableName the table to which the columns are added, assumed not to
    *           be <code>null</code> or empty.
    * 
    * @throws IllegalStateException if any of the supplied parameters do not
    *            have required information.
    * 
    * @throws IOException if an IO error occurs
    * @throws PSAuthorizationException if the current designer doesn't have
    *            design access
    * @throws PSAuthenticationFailedException if the current designer is unable
    *            to authenticate.
    * @throws PSServerException if the server can't be contacted for cataloging.
    */
   private void setNewFieldLocators(PSContainerLocator locator, String setName,
         Map fieldColumnMap, String tableName) throws IOException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSServerException
   {
      PSFieldSet set = getMatchingFieldSet(setName);
      if (set == null)
         throw new IllegalStateException("fieldset not found for the set name:"
               + setName);

      String aliasName = getTableAlias(set);
      if (aliasName == null)
      {
         Object[] params =
         {set.getName()};
         String msg = "The table name/alias corresponding to the fieldset <{0}>" +
                " is not found either to create a new table or to add new " +
                "columns for the new fields added to existing editor";
         throw new IllegalStateException(MessageFormat.format(msg, params));
      }

      PSBackEndTable backEndTable = (PSBackEndTable) locator.getBackEndTables()
            .get(aliasName.toLowerCase());

      if (backEndTable == null)
      {
         Object[] params =
         {aliasName};
         String msg = "The table with alias <{0}> is not found "
               + "in table references list.";
         throw new IllegalStateException(MessageFormat.format(msg, params));
      }

      PSSqlCataloger columnCatalog = new PSSqlCataloger(backEndTable
            .getDataSource(), tableName.toUpperCase());
      PSDesignerConnection conn = PSCoreFactory.getInstance()
            .getDesignerConnection();
      columnCatalog.setConnectionInfo(conn);
      List<String> columns = columnCatalog.getCatalog();

      // If the column is created for the field, set it's locator with
      // backend column.
      Iterator fieldColumns = fieldColumnMap.entrySet().iterator();
      while (fieldColumns.hasNext())
      {
         Map.Entry entry = (Map.Entry) fieldColumns.next();
         String fieldName = (String) entry.getKey();
         String columnName = (String) entry.getValue();

         Iterator cols = columns.iterator();
         while (cols.hasNext())
         {
            if (columnName.equalsIgnoreCase((String) cols.next()))
            {
               Iterator fields = set.getAll();
               while (fields.hasNext())
               {
                  Object field = fields.next();
                  if (field instanceof PSField)
                  {
                     PSField psfield = (PSField) field;
                     if (psfield.getLocator() == null
                           && psfield.getSubmitName().equals(fieldName))
                     {
                        psfield.setLocator(createBackEndColumn(backEndTable,
                              columnName));
                        break;
                     }
                  }
               }
               break;
            }
         }
      }
   }

   /**
    * Alters and removes field columns and creates new columns for the 
    * new fields of the field set passed in, if there are any new fields
    * mapped in the mapper. Updates <code>setTableNameMap</code>
    * and <code>tableColumnInfo</code> to hold the relationship between
    * table and fieldset and fieldnames and columnnames. This information
    * is used after altering the table is successful to refresh the fieldset
    * with new columns.
    * 
    * @param set a set that might contain new fields, assumed not to be
    *           <code>null</code>.
    * 
    * @param locator a container locator, used to determine the jdbc driver;
    *           assumed not <code>null</code>
    * 
    * @param mapper the mapper which contains the mappings of fields of the
    *           supplied fieldset, assumed not to be <code>null</code> or
    *           empty.
    * 
    * @param setTableNameMap map of table name as key and set name as value to
    *           hold the relationship between table and fieldset to update the
    *           fields of fieldset after creation of new columns of table is
    *           successful, assumed not to be <code>null</code>, may be
    *           empty.
    * 
    * @param tableColumnInfo map of table name as key and map of field name
    *           (key) and column name(value) as value to update the new fields
    *           of the fieldset with correct column names, after creation of new
    *           columns of table is successful, assumed not to be
    *           <code>null</code>, may be empty.
    * 
    * @return the new PSJdbcTableSchema table definition or <code>null</code>
    *         if the set contains no new columns.
    * 
    * @throws IllegalStateException if table alias or back-end-table could not
    *            be found for the table alias for the supplied fieldset.
    * @throws PSJdbcTableFactoryException if table factory reports this error
    * @throws IOException if table factory reports this error
    */
   @SuppressWarnings("unchecked")
   private PSJdbcTableSchema createAlterTableSchema(PSFieldSet set,
         PSContainerLocator locator, PSDisplayMapper mapper,
         Map setTableNameMap, Map tableColumnInfo)
         throws PSJdbcTableFactoryException, IOException,
         PSAuthorizationException, PSAuthenticationFailedException,
         PSServerException
   {
      String aliasName = getTableAlias(set);
      if (aliasName == null)
      {
         Object[] params =
         {set.getName()};
         String msg = "The table name/alias corresponding to the fieldset <{0}>" +
         " is not found either to create a new table or to add new " +
         "columns for the new fields added to existing editor";
         
         throw new IllegalStateException(
               MessageFormat
                     .format(msg, params));
      }

      PSBackEndTable backEndTable = (PSBackEndTable) locator.getBackEndTables()
            .get(aliasName.toLowerCase());

      if (backEndTable == null)
      {
         Object[] params =
         {aliasName};
         String msg = "The table with alias <{0}> is not found "
               + "in table references list.";

         throw new IllegalStateException(MessageFormat.format(msg, params));
      }

      PSConnectionDetail connDet = m_objectStore
            .getConnectionDetail(backEndTable.getDataSource());
      String driver = connDet.getDriver();

      // get the table name from the back-end-table
      String tableName = backEndTable.getTable().toUpperCase();
      PSSqlCataloger columnCatalog = new PSSqlCataloger(backEndTable
            .getDataSource(), tableName);
      PSDesignerConnection conn = PSCoreFactory.getInstance()
            .getDesignerConnection();
      columnCatalog.setConnectionInfo(conn);

      PSJdbcDataTypeMap dataTypeMap = null;
      try
      {
         dataTypeMap = new PSJdbcDataTypeMap(null, driver, null);
      }
      catch (SAXException e)
      {
         // this will only happen with a configuration error (corrupt file)
         throw new RuntimeException(e.getMessage());
      }
      List columns = columnCatalog.getCatalog();
      Map fieldColumnNames = new HashMap();

      Iterator mappings = mapper.iterator();
      List modifiedColumns = new ArrayList();
      Map<PSField, Integer> columnActions = m_columnActions.getColumnActions();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldRef = mapping.getFieldRef();
         Object object = set.get(fieldRef);
         if (object instanceof PSField)
         {
            PSField field = (PSField) object;
            
            Integer action = columnActions.get(field);
            boolean isNewColumn = field.getLocator() == null; 
            boolean isModification =  action != null && 
                  action.intValue() == IPSCETableColumnActions.COLUMN_ACTION_ALTER;
            // Adds it to the new column list only if the field is not a system 
            // field and has valid data type and format to create a new column.
            if (!field.isSystemField() && (isNewColumn || isModification)
                  && hasValidDataTypeAndFormat(field))
            {
               
               String columnName;
               if (isModification)
               {
                  PSBackEndColumn col = (PSBackEndColumn) field.getLocator();
                  columnName = col.getColumn().toUpperCase();
               }
               else
               {
                  columnName = getUniqueName(columns, field.getSubmitName())
                        .toUpperCase();
               }
               
               if(isNewColumn)
               {
                  columns.add(columnName);
                  fieldColumnNames.put(field.getSubmitName(), columnName);
               }

               modifiedColumns.add(createColumn(dataTypeMap, columnName, field,
                     PSJdbcTableComponent.ACTION_REPLACE));
            }
         }
            
      }

      /*
       * Only Complex and Simple Child field sets supports ordering data. Create
       * 'SORTRANK' column if the user wants to support ordering and the table
       * does not have this column.
       */
      if (set.getType() == PSFieldSet.TYPE_COMPLEX_CHILD
            || set.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
      {
         if (set.isSequencingSupported())
         {
            List sortColumn = new ArrayList();
            sortColumn.add(getChildSortColumn());

            if (!containsAll(columns, sortColumn))
            {
               modifiedColumns.add(new PSJdbcColumnDef(dataTypeMap,
                     getChildSortColumn(), PSJdbcTableComponent.ACTION_CREATE,
                     Types.INTEGER, null, true, null));
            }
         }
      }
      
      // Handle column removals     
      // Add to removal list if appropriate
      List<PSField> removeActions = new ArrayList<PSField>();
      for (PSField field : columnActions.keySet())
      {
         //Check whether the field belongs to this table or child table.
         PSBackEndColumn colLocator = (PSBackEndColumn)field.getLocator();
         if(colLocator == null)
            continue; // Skip if no locator
         String tableAlias = colLocator.getTable().getAlias();
         if(!aliasName.equalsIgnoreCase(tableAlias))
            continue;
         Integer action = columnActions.get(field);
         if (action != null
               && action.intValue() == IPSCETableColumnActions.COLUMN_ACTION_DELETE
               && !field.isSystemField())
         {
            PSBackEndColumn col = (PSBackEndColumn) field.getLocator();
            modifiedColumns.add(createColumn(dataTypeMap, col.getColumn()
                  .toUpperCase(), field, PSJdbcTableComponent.ACTION_DELETE));
            removeActions.add(field);
            
         }
      }
      for(PSField field : removeActions)
         m_columnActions.removeColumnAction(field);

      // if there were new fields, the table associated with this set has
      // been modifed, so add it to the list of the new and modifed tables
      PSJdbcTableSchema table = null;
      if (modifiedColumns.size() != 0)
      {
         table = new PSJdbcTableSchema(tableName, modifiedColumns.iterator());
         table.setAlter(true);
         // must setCreate in addition to setAlter, or it won't work
         table.setCreate(false);
         tableColumnInfo.put(tableName, fieldColumnNames);
         setTableNameMap.put(tableName, set.getName());
      }      
      
     
      return table;
   }

   /**
    * Checks whether all values in <code>list2</code> are present in
    * <code>list1</code>. Assumes the objects in lists are Strings and check
    * is case insensitive. <br>
    * If <code>list2</code> is empty it always returns <code>true</code>.
    * If <code>list1</code> is empty and <code>list2</code> is not empty it
    * always returns <code>false</code>
    * 
    * @param list1 the list in which to check, may not be <code>null</code>,
    *           may be empty.
    * @param list2 the list which to check, may not be <code>null</code>, may
    *           be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @return <code>true</code> if list1 contains all values of list2
    *         otherwise <code>false</code>
    */
   public static boolean containsAll(List list1, List list2)
   {
      if (list1 == null)
         throw new IllegalArgumentException("list1 can not be null");

      if (list2 == null)
         throw new IllegalArgumentException("list2 can not be null");

      boolean contains = true;

      Iterator list2Values = list2.iterator();

      while (list2Values.hasNext())
      {
         String list2Value = (String) list2Values.next();

         boolean exists = false;
         Iterator list1Values = list1.iterator();
         while (list1Values.hasNext())
         {
            String list1Value = (String) list1Values.next();
            if (list1Value.equalsIgnoreCase(list2Value))
            {
               exists = true;
               break;
            }
         }

         if (!exists)
         {
            contains = false;
            break;
         }
      }

      return contains;
   }

   /**
    * Gets the sort column name of child table used in content editor.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public static String getChildSortColumn()
   {
      return IPSConstants.CHILD_SORT_KEY;
   }

   /**
    * Checks the supplied name for uniqueness among the supplied list of names.
    * If it is not unique, appends a sequentially incremented number to the
    * supplied name and checks for uniqueness until it finds a unique name. The
    * found unique name is returned. Check is case insensitive. <br>
    * For example if the supplied name is 'foo' if 'foo' is not found, it
    * returns 'foo' otherwise checks for 'foo1', if not found returns 'foo1'
    * otherwise checks for 'foo2'. This is continued until it finds unique name.
    * 
    * @param names the list of names to be checked, may not be <code>null</code>
    * @param name the name to check, can not be <code>null</code> or empty.
    * 
    * @return the unique name, never <code>null</code> or empty.
    */
   private String getUniqueName(List names, String name)
   {
      if (names == null)
         throw new IllegalArgumentException("names can not be null");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty");

      String checkName = name;
      int i = 1;

      while (!isUniqueName(names, checkName))
      {
         checkName = name + i++;
      }

      return checkName;
   }

   /**
    * Checks whether the <code>checkName</code> is unique among the list of
    * names supplied ignoring the case. If the list is empty, returns the name
    * as unique.
    * 
    * @param names the list of names to be checked, may not be <code>null</code>
    * @param checkName the name to be checked, may not be <code>null</code> or
    *           empty.
    * 
    * @return <code>true</code> if the <code>checkName</code> does not exist
    *         in the list of supplied names, otherwise <code>false</code>
    */
   private boolean isUniqueName(List names, String checkName)
   {
      if (names == null)
         throw new IllegalArgumentException("names can not be null");

      if (checkName == null || checkName.trim().length() == 0)
         throw new IllegalArgumentException(
               "checkName can not be null or empty");

      boolean isUnique = true;

      if (!names.isEmpty())
      {
         Iterator iter = names.iterator();
         while (iter.hasNext())
         {
            String name = (String) iter.next();
            if (checkName.equalsIgnoreCase(name))
            {
               isUnique = false;
               break;
            }
         }
      }

      return isUnique;
   }

   /**
    * Creates a table definition for new fieldset. Only those fields which are
    * used in the mappings and have a valid datatype and format are added.
    * Updates <code>setTableNameMap</code> and <code>tableColumnInfo</code>
    * to hold the relationship between table and fieldset and field names and
    * column names.
    * 
    * @param osFieldSet a new field set, assumed not to be <code>null</code>
    * 
    * @param locator a container locator, used to determine the jdbc; assumed
    *           not <code>null</code>
    * 
    * @param mapper the mapper which contains the mappings of fields of the
    *           supplied fieldset, assumed not to be <code>null</code> or
    *           empty.
    * 
    * @param setTableNameMap map of table name as key and set name as value to
    *           hold the relationship between table and fieldset to update the
    *           fieldset after creation of table is successful, assumed not to
    *           be <code>null</code>
    * 
    * @param tableColumnInfo map of table name as key and map of field name
    *           (key) and column name(value) as value to update the fields of
    *           the fieldset with backend locators, after creation of table is
    *           successful, assumed not to be <code>null</code>, may be
    *           empty.
    * 
    * @throws IllegalStateException if table name of the field set is
    *            <code>null</code>
    * @throws PSJdbcTableFactoryException if the table factory reports an error
    * @return the new PSJdbcTableSchema table definition; may be
    *         <code>null</code> if it does not find any local fields to which
    *         it has to create columns.
    * @throws PSAuthenticationFailedException
    * @throws PSAuthorizationException
    * @throws PSServerException
    */
   @SuppressWarnings("unchecked")
   private PSJdbcTableSchema createNewTableSchema(PSFieldSet osFieldSet,
         PSContainerLocator locator, PSDisplayMapper mapper,
         Map<String, String> setTableNameMap, Map tableColumnInfo)
         throws PSJdbcTableFactoryException, IOException, PSServerException,
         PSAuthorizationException, PSAuthenticationFailedException
   {
      int setType = osFieldSet.getType();
      String newName = getTableName(osFieldSet, locator.getBackEndTables());
      if (setType == PSFieldSet.TYPE_PARENT)
      {
         m_parentTableOldName = newName;
      }
      //Check whether a table exists with this name already
      //If exists get a unique name and set that name on the container locator 
      String newUniqueName = getUniqueTableName(newName,setType);
      if(!newName.equalsIgnoreCase(newUniqueName))
      {
         Iterator iter = locator.getTableSets();
         while(iter.hasNext())
         {
            PSTableSet tset = (PSTableSet) iter.next();
            Iterator iter1 = tset.getTableRefs();
            while (iter1.hasNext())
            {
               PSTableRef tref = (PSTableRef) iter1.next();
               if (tref.getName().equalsIgnoreCase(newName))
               {
                  tref.setAlias(newUniqueName);
                  tref.setName(newUniqueName);
               }
            }
         }
         newName = newUniqueName;
      }
      if (setType == PSFieldSet.TYPE_PARENT)
      {
         m_parentTableNewName = newName;
      }
      // since this is going to be a new table we need to create a
      // back-end-table
      PSBackEndTable backEndTable = getBackEndTable(newName);

      List sysColumns = getSystemColumns(setType);

      PSJdbcDataTypeMap dataTypeMap = null;
      String driver = null;
      // [todo: find better solution here]
      PSTableLocator tableLocator = null;
      try
      {
         tableLocator = getSystemTableLocator();
      }
      catch (Exception e)
      {
         // this will only happen with a configuration error (corrupt file)
         throw new RuntimeException(e.getMessage());
      }

      if (null != tableLocator.getCredentials())
      {
         PSConnectionDetail connDet = m_objectStore
               .getConnectionDetail(tableLocator.getCredentials()
                     .getDataSource());
         driver = connDet.getDriver();
      }
      try
      {
         dataTypeMap = new PSJdbcDataTypeMap(null, driver, null);
      }
      catch (SAXException e)
      {
         // this will only happen with a configuration error (corrupt file)
         throw new RuntimeException(e.getMessage());
      }

      // First, add system columns and set that as primary key
      final List<PSJdbcColumnDef> newColumns = new ArrayList<PSJdbcColumnDef>();
      final List<String> pkColumns = new ArrayList<String>();
      for (int i = 0; i < sysColumns.size(); i++)
      {
         // assume system columns are non-null INTEGERs, used as the primary key
         newColumns.add(new PSJdbcColumnDef(dataTypeMap, (String) sysColumns
               .get(i), PSJdbcTableComponent.ACTION_CREATE, Types.INTEGER,
               null, false, null));
         pkColumns.add((String) sysColumns.get(i));
      }
      PSJdbcPrimaryKey primaryKey = new PSJdbcPrimaryKey(pkColumns.iterator(),
            PSJdbcTableComponent.ACTION_CREATE);

      // Second, add any optional system columns
      if (setType == PSFieldSet.TYPE_COMPLEX_CHILD
            || setType == PSFieldSet.TYPE_SIMPLE_CHILD)
      {
         if (osFieldSet.isSequencingSupported())
            newColumns.add(new PSJdbcColumnDef(dataTypeMap,
                  IPSConstants.CHILD_SORT_KEY,
                  PSJdbcTableComponent.ACTION_CREATE, Types.INTEGER, null,
                  true, null));
      }

      // Third, add columns to represent the local fields used in mappings
      Iterator mappings = mapper.iterator();
      final Map<String, String> fieldColumnNames = new HashMap<String, String>();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
         String fieldRef = mapping.getFieldRef();
         if (fieldRef.equals(osFieldSet.getName())
               && osFieldSet.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
         {
            //handle simple child in shared def
            PSDisplayMapper childMapper = mapping.getDisplayMapper();
            assert(childMapper != null);
            PSDisplayMapping childMapping = (PSDisplayMapping) childMapper.get(0);
            assert(childMapping != null);
            fieldRef = childMapping.getFieldRef();
         }
         Object object = osFieldSet.get(fieldRef);
         if (object instanceof PSField)
         {
            PSField newField = (PSField) object;

            // Adds it to the new column list only if the field is either new local
            // field or shared field when it is shared field editor 
            // and has valid data type and format to create a new column.
            if ((newField.isLocalField() || (m_editorType == EDITOR_TYPE_SHARED 
                  && newField.isSharedField()))
                  && hasValidDataTypeAndFormat(newField))
            {
               fieldColumnNames.put(newField.getSubmitName(), newField
                     .getSubmitName().toUpperCase());
               newColumns.add(createColumn(dataTypeMap, newField
                     .getSubmitName().toUpperCase(), newField,
                     PSJdbcTableComponent.ACTION_CREATE));
            }
         }
      }

      // get the table name from the back-end-table
      String tableName = backEndTable.getTable();
      PSJdbcTableSchema newTable = null;
      // create a new table definition
      if (!fieldColumnNames.isEmpty())
      {
         newTable = new PSJdbcTableSchema(tableName, newColumns.iterator());

         // Simple Child Table will have multiple rows for same CONTENTID and
         // REVISIONID. So we should not set primary key on this.
         if (osFieldSet.getType() != PSFieldSet.TYPE_SIMPLE_CHILD)
            newTable.setPrimaryKey(primaryKey);
         tableColumnInfo.put(tableName, fieldColumnNames);
         setTableNameMap.put(tableName, osFieldSet.getName());
      }
      return newTable;
   }

   /**
    * Checks whether a table exists with the supplied name in the database
    * represented by CMS repository datasource, if yes then returns the table
    * name by appending X at the end, where X is a number that makes the table
    * unique. Otherwise returns the same name. As per the convention the child
    * tables start with parent table names. If we have already modified the
    * parent table name replaces the old parent table name in the child table
    * with the new parent table name before checking for database table unique
    * ness.
    * 
    * @param tableName assumed not <code>null</code> or empty.
    * @param setType Field set type for which we need the new name.
    * @return String unique name of the table. Never <code>null</code>.
    */
   private String getUniqueTableName(String tableName, int setType)
   {
      // Make sure child field sets start with parent table name
      if (!(setType == PSFieldSet.TYPE_PARENT
            || StringUtils.isBlank(m_parentTableNewName)
            || StringUtils.isBlank(m_parentTableOldName)))
      {
         if (tableName.startsWith(m_parentTableOldName + "_"))
         {
            tableName = StringUtils.replace(tableName, m_parentTableOldName,
                  m_parentTableNewName, 1);
         }
      }
      // Convert any "." in tableName to an "_"
      tableName = StringUtils.replaceChars(tableName, '.', '_');
      
      // Look up the table names of all existing tables
      List<String> tables = PSCatalogDatabaseTables.getCatalog(null, "TABLE",
            true);
      // Convert the table names to uppercase
      List<String> ucaseTables = new ArrayList<String>();
      for(String table: tables)
         ucaseTables.add(StringUtils.upperCase(table));
      // Scan the list for desired table name and if found, make it unique.
      String tName = StringUtils.upperCase(tableName);
      int counter = 1;
      while (ucaseTables.contains(tName))
      {
         tName = tableName + Integer.toString(counter++);
      }
      return tName;
   }
    
   /**
    * Gets the list of minimum required system columns for content editor table
    * used for <code>type</code> of fieldset.
    * 
    * @return list of system column names, never <code>null</code> and empty.
    */
   private List getSystemColumns(int type)
   {
      checkFieldSetType(type);

      if (type == PSFieldSet.TYPE_COMPLEX_CHILD)
         return ms_sysComplexChildColumns;
      else
         return ms_sysColumns;
   }

   /**
    * Checks the supplied type with one of allowed types of
    * <code>PSFieldSet</code>.
    * 
    * @param type the type of fieldset, must be one of valid types of
    *           <code>PSFieldSet</code>
    * 
    * @throws IllegalArgumentException if it is not one of allowed types of
    *            <code>PSFieldSet</code>.
    */
   private void checkFieldSetType(int type)
   {
      if (!PSFieldSet.isValidType(type))
      {
         throw new IllegalArgumentException(
               "type is invalid, it must be one of following values"
                     + PSFieldSet.TYPE_PARENT + ","
                     + PSFieldSet.TYPE_SIMPLE_CHILD + ","
                     + PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD + ","
                     + PSFieldSet.TYPE_COMPLEX_CHILD);
      }
   }

   /**
    * Gets table locator used in system table definition. The table location is
    * same right now for all table sets in container locator of system table
    * definition. So takes the table location of first table set for system
    * table location.
    * 
    * @return the table locator, never <code>null</code>
    * @throws Exception
    */
   private PSTableLocator getSystemTableLocator() throws Exception
   {
      PSTableLocator sysTableLocator = null;

      PSContentEditorSystemDef definition = getSystemDef();
      PSContainerLocator sysContainerLocator = definition.getContainerLocator();

      // get table set from system container locator
      Iterator sysTableIter = sysContainerLocator.getTableSets();
      while (sysTableIter.hasNext())
      {
         PSTableSet set = (PSTableSet) sysTableIter.next();
         sysTableLocator = set.getTableLocation();
         break;
      }

      return sysTableLocator;
   }

   /**
    * Get the system def object may be <code>null</code>.
    * 
    * @throws Exception In case of error.
    */
   private PSContentEditorSystemDef getSystemDef() throws Exception
   {
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG);
      List<IPSReference> sysref = (List<IPSReference>) model.catalog(true);
      if (sysref.isEmpty())
         return null;
      PSContentEditorSystemDef systemDef = (PSContentEditorSystemDef) model
            .load(sysref.get(0), false, false);
      return systemDef;
   }

   /**
    * Creates a new PSJdbcColumnDef column based on the provided OSField's name,
    * data type, and format. Can set the action flag to delete the column
    * in the database.
    * 
    * @param dataTypeMap The object containing the jdbc to native mappings,
    *           which will be passed to the PSJdbcColumnDef constructor. Assumed
    *           not <code>null</code>.
    * @param columnName the name of column to create, assumed not to be
    *           <code>null</code> or empty.
    * @param field provides the data type, and format for the new column.
    *           Assumed not <code>null</code>.
    * @param action The table factory action that needs to be set on the column. 
    * 
    * @return a new <code>PSJdbcColumnDef</code> object that accepts nulls,
    *         has ACTION_CREATE, and whose name, type, and size have been set
    *         based on the specified field.
    */
   private PSJdbcColumnDef createColumn(PSJdbcDataTypeMap dataTypeMap,
         String columnName, PSField field, int action)
   {
      PSJdbcColumnDef newColumn;

      // determine the datatype and size
      StringBuffer params = new StringBuffer(5);
      int jdbcDataType = getJdbcDataType(field, params);
      String strSize = (params.length() != 0 ? params.toString() : null);
      newColumn = new PSJdbcColumnDef(dataTypeMap, columnName,
            action, jdbcDataType, strSize, true,
            null);

      return newColumn;
   }

   /**
    * Determines the correct JDBC datatype and params for this field based on
    * datatype and format. This method must be maintained as new datatypes and
    * format options are specified in the static intializer.
    * 
    * @param params The buffer to which the params value is appended. May not be
    *           <code>null</code> and must be empty. The params value is the
    *           value supplied along with the datatype when creating a column,
    *           for example in the statement:
    *           <p>
    *           <code>CREATE TABLE Foo (myCol VARCHAR(255) NOT NULL)</code>
    *           <p>
    *           "255" is the params value. If no params value is to be supplied,
    *           the buffer is left empty.
    * 
    * @return One of the values from {@link java.sql.Types}.
    * 
    * @throws IllegalArgumentException if params is <code>null</code> or not
    *            empty.
    * @throws IllegalStateException if this field does not have a datatype
    *            defined.
    */
   private int getJdbcDataType(PSField field, StringBuffer params)
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      if (params.length() > 0)
         throw new IllegalArgumentException("params must be empty");

      int jdbcDataType = Types.NULL;
      int size = -1;
      String contentDataType = field.getDataType();
      String format = field.getDataFormat();

      // if format is not "max", it's a number
      if (format != null)
      {
         if (!format.equalsIgnoreCase(MAX_FORMAT))
         {
            try
            {
               size = Integer.parseInt(format);
               format = null;
            }
            catch (NumberFormatException e)
            {
               throw new IllegalStateException("Invalid format \"" + format
                     + "\" for datatype \"" + contentDataType + "\"");
            }
         }
      }

      if (contentDataType.equals(PSField.DT_BINARY))
      {
         /**
          * Driver with smallest varbinary is Oracle with the max RAW being
          * 2000.
          */
         jdbcDataType = Types.BLOB;
         if (size != -1)
         {
            if (size <= 2000)
               jdbcDataType = Types.VARBINARY;
            else
            {
               // leave as blob and clear the size
               size = -1;
            }
         }
      }
      else if (contentDataType.equals(PSField.DT_TEXT))
      {
         /**
          * Driver with smallest varchar is Oracle with the max VARCHAR2 being
          * 4000.
          */
         jdbcDataType = Types.CLOB;
         if (size != -1)
         {
            if (size <= 4000)
               jdbcDataType = Types.VARCHAR;
            else
               size = -1; // leave as clob and clear the size
         }
      }
      else if (contentDataType.equals(PSField.DT_INTEGER))
         jdbcDataType = Types.INTEGER;
      else if (contentDataType.equals(PSField.DT_FLOAT))
         jdbcDataType = Types.FLOAT;
      else if (contentDataType.equals(PSField.DT_DATE))
         jdbcDataType = Types.DATE;
      else if (contentDataType.equals(PSField.DT_TIME))
         jdbcDataType = Types.TIME;
      else if (contentDataType.equals(PSField.DT_DATETIME))
         jdbcDataType = Types.TIMESTAMP;
      else if (contentDataType.equals(PSField.DT_BOOLEAN))
         jdbcDataType = Types.BIT;
      else
         throw new IllegalStateException("Unknown datatype: " + contentDataType);

      if (size != -1)
         params.append(String.valueOf(size));

      return jdbcDataType;
   }

   /**
    * Gets table name for the field set from fieldset name. This should be
    * called to determine the table name of the new fieldset. <br>
    * The following table describes the convention for table names. <table
    * border=1>
    * <tr>
    * <th>case</th>
    * <th>table name </th>
    * </tr>
    * <tr>
    * <td>parent</td>
    * <td>name of set</td>
    * </tr>
    * <tr>
    * <td>child</td>
    * <td>table name of parent field set + "_" + name of child field set</td>
    * </tr>
    * </table> The maximum number of allowed characters for table name is 128.
    * If name is exceeding this limit it truncates the name to 128 characters.
    * If the name is combination of two names, make sures both names get equal
    * parts.
    * 
    * @param fieldset the field set to get tablename, may not be
    *           <code>null</code>.
    * @param backEndTables map of backend tables referred in this content
    *           editor, with lowercased table alias name as key and
    *           <code>PSBackEndTable</code> object as value, may not be
    *           <code>null</code>
    * 
    * @return the table name in upper case, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if parent fieldset of this content editor is
    *            not new and the table referred by this fieldset is not found.
    */
   private String getTableName(PSFieldSet fieldset, Map backEndTables)
   {
      if (fieldset == null)
         throw new IllegalArgumentException("fieldset can not be null");

      if (backEndTables == null)
         throw new IllegalArgumentException("backEndTables can not be null");

      int type = fieldset.getType();
      /*
       * case , table name parent, name of set child, table name of parent field
       * set + "_" + name of child field set
       */
      String tableName = null;
      if (type == PSFieldSet.TYPE_PARENT)
         tableName = fieldset.getName();
      else
      {
         String parentTableAlias = getTableAlias(m_parentFieldSet);
         String parentTableName = null;

         /*
          * If alias is not present for parent fieldset, then even the parent
          * fieldset is also new fieldset, so table name will be name of
          * fieldset. Otherwise get table name from table alias.
          */
         if (parentTableAlias == null)
            parentTableName = m_parentFieldSet.getName();
         else
         {
            PSBackEndTable table = (PSBackEndTable) backEndTables
                  .get(parentTableAlias.toLowerCase());
            if (table == null)
            {
               Object[] params =
               {parentTableAlias};
               String msg = "The table with alias <{0}> is not found "
                     + "in table references list.";
               throw new IllegalStateException(MessageFormat
                     .format(msg, params));
            }
            parentTableName = table.getTable();
         }

         if (parentTableName.length() >= MAX_TABLE_NAME_LEN / 2 - 1)
            parentTableName = parentTableName.substring(0,
                  MAX_TABLE_NAME_LEN / 2);

         String fieldSetName = fieldset.getName();
         if (fieldSetName.length() >= MAX_TABLE_NAME_LEN / 2 - 1)
            fieldSetName = fieldSetName.substring(0, MAX_TABLE_NAME_LEN / 2);
         tableName = parentTableName + "_" + fieldSetName;
      }

      if (tableName.length() > MAX_TABLE_NAME_LEN)
         tableName = tableName.substring(0, MAX_TABLE_NAME_LEN);

      return tableName.toUpperCase();
   }

   /**
    * Sets table alias for the passed in field set and all of it's children
    * field sets.
    * 
    * @param set the field set, assumed not to be <code>null</code>
    */
   private String getTableAlias(PSFieldSet set)
   {
      Iterator fields = set.getAll();

      String tableAlias = null;
      while (fields.hasNext())
      {
         Object field = fields.next();
         if (field instanceof PSFieldSet)
            continue;
         else
         {
            PSField osfield = (PSField) field;
            if (m_editorType == EDITOR_TYPE_LOCAL && osfield.isLocalField())
            {
               /*
                * Get table alias for this field set from existing fields(fields
                * which have backend mapping.
                */
               if (osfield.getLocator() instanceof PSBackEndColumn)
               {
                  tableAlias = ((PSBackEndColumn) osfield.getLocator())
                        .getTable().getAlias();
                  break;
               }
            }
            else if(m_editorType == EDITOR_TYPE_SHARED && osfield.isSharedField())
            {
               /*
                * Get table alias for this field set from existing fields(fields
                * which have backend mapping.
                */
               if (osfield.getLocator() instanceof PSBackEndColumn)
               {
                  tableAlias = ((PSBackEndColumn) osfield.getLocator())
                        .getTable().getAlias();
                  break;
               }
            }
         }
      }
      return tableAlias;
   }

   /**
    * Checks recursively the supplied fieldset and its children for new local
    * fields that are not specified with proper datatype and format and gets all
    * fieldset names with those invalid fields.
    * 
    * @param set the fieldset to check, assumed not to be <code>null</code>
    * 
    * @return the list of the names of fieldsets with invalid fields, never
    *         <code>null</code>, may be empty.
    */
   private List<String> getFieldSetsWithInvalidFields(PSFieldSet set)
   {
      List<String> fieldsetNames = new ArrayList<String>();
      if (hasInvalidDatatypeAndFormatFields(set))
         fieldsetNames.add(set.getName());

      Iterator sets = set.getAll();
      while (sets.hasNext())
      {
         Object object = sets.next();
         if (object instanceof PSFieldSet
               && !((PSFieldSet) object).isSharedFieldSet())
         {
            fieldsetNames
                  .addAll(getFieldSetsWithInvalidFields((PSFieldSet) object));
         }
      }
      return fieldsetNames;
   }

   /**
    * Checks whether this fieldset has any new local fields with invalid
    * datatype and format.
    * 
    * @return <code>true</code> if any of the new local fields were not
    *         specified with proper datatype and format, otherwise
    *         <code>false</code>
    */
   private boolean hasInvalidDatatypeAndFormatFields(PSFieldSet set)
   {
      Iterator fields = set.getAll();

      while (fields.hasNext())
      {
         Object obj = fields.next();
         if (obj instanceof PSField)
         {
            PSField field = (PSField) obj;
            if (field.isLocalField() && field.getLocator() == null)
            {
               if (!hasValidDataTypeAndFormat(field))
                  return true;
            }
         }
      }

      return false;
   }

   /**
    * Checks whether this field has valid data type and format. If field's data
    * type is valid and the format is supported for the data type, it checks for
    * the validity of the field's format and returns the result.
    * 
    * @return <code>true</code> if the data type and format are valid.
    */
   private boolean hasValidDataTypeAndFormat(PSField field)
   {
      boolean isValid = false;

      String dataType = field.getDataType();
      if (dataType != null && dataType.trim().length() != 0
            && isValidDataType(dataType))
      {
         String format = field.getDataFormat();
         if (supportsFormat(dataType))
         {
            if (format != null && format.trim().length() != 0)
            {
               isValid = isValidDataTypeFormat(dataType, format);
            }
         }
         else
            isValid = true;
      }

      return isValid;
   }

   /**
    * Determines if specified format is valid for specified dataType.
    * 
    * @param dataType The dataType to check formats for, may not be <code>null
    * </code>,
    * and must be a valid datatype (see {@link #isValidDataType(String)}).
    * 
    * @param format The format to check. May not be <code>null</code> or
    * empty.
    * 
    * @return <code>true</code> if the specified format is valid, <code>false
    * </code>
    * if not.
    * 
    * @throws IllegalArgumentException if dataType is <code>null</code>,
    * invalid, or if format is <code>null</code> or empty.
    */
   private boolean isValidDataTypeFormat(String dataType, String format)
   {
      if (dataType == null || !isValidDataType(dataType))
         throw new IllegalArgumentException("dataType is invalid:" + dataType);

      if (format == null || format.trim().length() == 0)
         throw new IllegalArgumentException("format may not be null or empty");

      PSCEDataTypeInfo dtInfo = ms_dtInfoMap.get(dataType);
      return dtInfo.isValid(format);
   }

   /**
    * Determines if specified dataType supports a format.
    * 
    * @param dataType The dataType to check, may not be <code>null</code>,
    * and must be a valid datatype (see {@link #isValidDataType(String)}).
    * 
    * @return <code>true</code> if the specified datatype supports specifying
    * a format, <code>false</code> if not.
    * 
    * @throws IllegalArgumentException if dataType is <code>null</code> or
    * invalid.
    */
   private boolean supportsFormat(String dataType)
   {
      if (dataType == null || !isValidDataType(dataType))
         throw new IllegalArgumentException("dataType is invalid:" + dataType);

      PSCEDataTypeInfo dtInfo = ms_dtInfoMap.get(dataType);
      return dtInfo.supportsFormat();
   }

   /**
    * Determines if specified dataType is valid.
    * 
    * @param dataType The dataType to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the specified dataType is valid,
    *         <code>false
    * </code> if not.
    * 
    * @throws IllegalArgumentException if dataType is <code>null</code>.
    */
   private boolean isValidDataType(String dataType)
   {
      if (dataType == null)
         throw new IllegalArgumentException("dataType may not be null");

      return ms_dtInfoMap.containsKey(dataType);
   }

   /**
    * Sets the locator for the fields(which are new and have proper datatype and
    * format to create columns) of the fieldset referring to the supplied table
    * name. This should be called after creation or altering the table is
    * successful. Sets the table alias to the fieldset if this is called after
    * creation of new table.
    * 
    * @param setName the name of the set referring to the supplied table name,
    *           assumed not to be <code>null</code> or empty.
    * 
    * @param fieldColumnMap map of field name as key and column name as value to
    *           update the new fields of the fieldset with correct column names,
    *           assumed not to be <code>null</code>.
    * 
    * @param tableName the table to which the columns are added, assumed not to
    *           be <code>null</code> or empty.
    * 
    * @throws IllegalStateException if any of the supplied parameters do not
    *            have required information.
    */
   private void setNewFieldLocators(String setName, Map fieldColumnMap,
         String tableName)
   {
      PSFieldSet set = getMatchingFieldSet(setName);
      if (set == null)
         throw new IllegalStateException("fieldset not found for the set name:"
               + setName);

      Iterator fields = set.getAll();
      while (fields.hasNext())
      {
         Object field = fields.next();
         if (field instanceof PSField)
         {
            PSField psfield = (PSField) field;
            if (m_editorType == EDITOR_TYPE_LOCAL)
            {
               if (psfield.isLocalField() && psfield.getLocator() == null)
               {
                  String columnName = (String) fieldColumnMap.get(psfield
                        .getSubmitName());
                  if (columnName != null)
                  {
                     psfield.setLocator(createBackEndColumn(
                           getBackEndTable(tableName), columnName));
                  }
               }
            }
            else if(m_editorType == EDITOR_TYPE_SHARED)
            {
               if (psfield.isSharedField() && psfield.getLocator() == null)
               {
                  String columnName = (String) fieldColumnMap.get(psfield
                        .getSubmitName());
                  if (columnName != null)
                  {
                     psfield.setLocator(createBackEndColumn(
                           getBackEndTable(tableName), columnName));
                  }
               }
               
            }
         }
      }
   }

   /**
    * Gets the fieldset with supplied name from field sets of the content
    * editor.
    * 
    * @param name the name to check, assumed not to be <code>null</code>
    * 
    * @return the matching fieldset, may be <code>null</code> if it didn't
    *         find a set with supplied name.
    */
   private PSFieldSet getMatchingFieldSet(String name)
   {
      return getMatchingFieldSet(m_parentFieldSet, name);
   }

   /**
    * Checks whether the mappings exists in the corresponding mapper for any of
    * the fields of the supplied fieldset.
    * 
    * @param set the set to check, assumed not to be <code>null</code>
    * 
    * @return <code>true</code> if mappings exist, otherwise
    *         <code>false</code>
    */
   private boolean doesMappingsExist(PSFieldSet set, PSDisplayMapper mapper)
   {
      boolean mappingsExist = false;
      if (!(mapper == null || mapper.isEmpty()))
      {
         Iterator mappings = mapper.iterator();
         while (mappings.hasNext())
         {
            PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();
            String fieldRef = mapping.getFieldRef();
            Object object = set.get(fieldRef);
            if (object instanceof PSField)
            {
               PSField fld = (PSField) object;
               if (fld.isLocalField() || fld.isSharedField())
               {
                  mappingsExist = true;
                  break;
               }
            }
            else if (object == null)
            {
               //handle case of simple children in shared defs
               if (fieldRef.equals(set.getName()) 
                     && set.getType() == PSFieldSet.TYPE_SIMPLE_CHILD)
               {
                  mappingsExist = true;
                  break;
               }
            }
         }
      }

      return mappingsExist;
   }

   /**
    * Checks the supplied fieldset and all it's children with supplied name and
    * returns the matching fieldset. The check is case sensitive.
    * 
    * @param set the fieldset to check, may not be <code>null</code>
    * @param name the name to check, may not be <code>null</code> or empty.
    * 
    * @return the matching fieldset, may be <code>null</code> if it didn't
    *         find a set with supplied name.
    */
   private PSFieldSet getMatchingFieldSet(PSFieldSet set, String name)
   {
      if (set == null)
         throw new IllegalArgumentException("set can not be null");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name can not be null or empty");

      PSFieldSet matchingSet = null;

      if (set.getName().equals(name))
         matchingSet = set;
      else
      {
         Iterator sets = set.getAll();
         while (sets.hasNext())
         {
            Object fieldset = sets.next();
            if (fieldset instanceof PSFieldSet)
            {
               matchingSet = getMatchingFieldSet((PSFieldSet) fieldset, name);
               if (matchingSet != null)
                  break;
            }
         }
      }

      return matchingSet;
   }

   /**
    * Creates the back-end-column for the field name passed in.
    * 
    * @param beTable a back end table to create the back-end-column in; assumed
    *           not <code>null</code>
    * @param fieldName a field name to create back-end-column for; assumed not
    *           <code>null</code>
    * @return a new <code>PSBackEndColumn</code> object
    */
   private PSBackEndColumn createBackEndColumn(PSBackEndTable beTable,
         String fieldName)
   {
      try
      {
         return new PSBackEndColumn(beTable, fieldName.toUpperCase());
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   /**
    * Creates and returns the back end table for the table to be created, which
    * alias name was passed in.
    * 
    * @param aliasName an alias name the back-end-table will be created for can
    *           not be <code>null</code>
    * @return the back-end-table, will not be <code>null</code>
    * @see PSBackEndTable
    */
   private PSBackEndTable getBackEndTable(String aliasName)
   {
      try
      {
         PSBackEndTable backEndTable = new PSBackEndTable(aliasName);
         backEndTable.setTable(aliasName.toUpperCase());

         return backEndTable;

      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   /**
    * Parent field set object. Initialized in ctor, never <code>null</code>
    * after that.
    */
   private PSFieldSet m_parentFieldSet;

   /**
    * Parent display mapper object. Initialized in ctor, never <code>null</code>
    * after that.
    */
   private PSDisplayMapper m_parentMapper;
   
   /**
    * Parent container locator. Initialized in ctor, never <code>null</code>
    * after that.
    */
   private PSContainerLocator m_parentLocator;

   /**
    * Member variable to hold the type of the editor we are acting.
    * Should one of EDITOR_TYPE_LOCAL, EDITOR_TYPE_SHARED.
    */
   private int m_editorType;

   /**
    * Constant denoting the type of the editor local def.
    */
   public static final int EDITOR_TYPE_LOCAL = 1;

   /**
    * Constant denoting the type of the editor shared def.
    */
   public static final int EDITOR_TYPE_SHARED = 2;

   /**
    * Member variable to hold the PSObjectStore object. Initialized in the
    * constructor and never <code>null</code> after that.
    */
   private PSObjectStore m_objectStore;
   
   /**
    * Object that implements the content editor table columns actions
    * interface.
    */
   private IPSCETableColumnActions m_columnActions;

   /**
    * Maximum allowed length of table name.
    */
   private static int MAX_TABLE_NAME_LEN = 128;

   /**
    * Tag name of 'success' element in table definitions save results document.
    */
   private static final String XELEM_SUCCESS = "success";

   /**
    * Tag name of 'error' element in table definitions save results document.
    */
   private static final String XELEM_ERROR = "error";

   /**
    * Name of table name attribute for either 'success' or 'error' elements in
    * table definitions save results document.
    */
   private static final String XATTR_TABLENAME = "tableName";

   /**
    * Name of create flag attribute for either 'success' or 'error' elements in
    * table definitions save results document.
    */
   private static final String XATTR_CREATE = "create";

   /**
    * Constant to indicate 'Yes' value.
    */
   private static final String XATTR_VALUE_YES = "y";

   /**
    * The list of minimum system columns required for any content editor table.
    * The entries will be added first time {@link #getSystemColumns } is called
    * and never changed after that.
    */
   private static List<String> ms_sysColumns;

   /**
    * The list of minimum system columns required for any complex child content
    * editor table. The entries will be added first time {@link
    * #getSystemColumns } is called for complex child type and never changed
    * after that.
    */
   private static List<String> ms_sysComplexChildColumns;

   static
   {
      ms_sysColumns = new ArrayList<String>();
      ms_sysColumns.add(IPSConstants.ITEM_PKEY_CONTENTID);
      ms_sysColumns.add(IPSConstants.ITEM_PKEY_REVISIONID);

      ms_sysComplexChildColumns = new ArrayList<String>();
      ms_sysComplexChildColumns.addAll(ms_sysColumns);
      ms_sysComplexChildColumns.add(IPSConstants.CHILD_ITEM_PKEY);
   }

   /**
    * Map of info for each datatype. Never <code>null</code> or empty,
    * immutable. Must be maintained to contain an entry for each valid datatype.
    * The key is the datatype name as a String, and the value is a
    * {@link PSCEDataTypeInfo} object, never <code>null</code>. Entries are
    * sorted by the key value lexicographically.
    */
   private static Map<String, PSCEDataTypeInfo> ms_dtInfoMap;

   /**
    * Constant to indicate the "max" format.
    */
   private static final String MAX_FORMAT = "max";
   
   /**
    * Parent table new name, will be set while creating new tables. See
    * {@link #createNewTableSchema(PSFieldSet, PSContainerLocator, PSDisplayMapper, Map, Map)},
    * may be same as old table name and will be <code>null</code> if not set.
    */
   private String m_parentTableNewName;

   /**
    * Parent table old name, will be set while creating new tables. See
    * {@link #createNewTableSchema(PSFieldSet, PSContainerLocator, PSDisplayMapper, Map, Map)},
    * may be same as new table name and will be <code>null</code> if not set. 
    */
   private String m_parentTableOldName;
   
   static
   {
      /*
       * create format map and add all valid types and their format info. Any
       * type that supports formatting must supply a default format. Predefined
       * values should be added so that the first value is the one that should
       * be offered as the default choice if presented to a user (i.e. as
       * choices in a combo box.
       */
      ms_dtInfoMap = new HashMap<String, PSCEDataTypeInfo>();

      // for now "max" is the only predefined format available
      List<String> maxFormat = new ArrayList<String>(1);
      maxFormat.add(MAX_FORMAT);

      // setup text type, add it with "max" in format list and 50 as default
      PSCEDataTypeInfo text = new PSCEDataTypeInfo();
      text.setFormats(maxFormat.iterator());
      text.setDefaultFormat("50");
      ms_dtInfoMap.put(PSField.DT_TEXT, text);

      // setup binary type, add it with "max" in format list and as default
      PSCEDataTypeInfo bin = new PSCEDataTypeInfo();
      bin.setFormats(maxFormat.iterator());
      bin.setDefaultFormat(MAX_FORMAT);
      ms_dtInfoMap.put(PSField.DT_BINARY, bin);

      PSCEDataTypeInfo none = new PSCEDataTypeInfo();
      none.setSupportsNumeric(false);

      ms_dtInfoMap.put(PSField.DT_INTEGER, none);
      ms_dtInfoMap.put(PSField.DT_FLOAT, none);
      ms_dtInfoMap.put(PSField.DT_DATE, none);
      ms_dtInfoMap.put(PSField.DT_TIME, none);
      ms_dtInfoMap.put(PSField.DT_DATETIME, none);
      ms_dtInfoMap.put(PSField.DT_BOOLEAN, none);
   }
}
