/*******************************************************************************
 * $Id: OSDataMapper.java 1.25 2002/01/18 21:29:33Z jamesschultz Exp $
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * Version Labels  : $Name: $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: OSDataMapper.java $
 * Revision 1.25  2002/01/18 21:29:33Z  jamesschultz
 * extract method registerExtension while updating to reflect method
 * name change in OSExtensionCall
 * Revision 1.24  2001/07/02 13:45:26Z  snezanahasanovic
 * Changed code to have mappings regardless of case sensitivity.
 * Revision 1.23  2000/06/28 00:19:28Z  AlexTeng
 * Added code to support new UDF and extension model.
 *
 * Revision 1.22  2000/03/15 22:15:40Z  AlexTeng
 * Added code to a problem that removes all non-back end column
 * mappings. Now, all non-back end column mappings are preserved
 * by default.
 *
 * Revision 1.21  2000/03/06 20:29:21Z  AlexTeng
 * Added new parameter in guessMapping(OSBackendDatatank,
 * OSPageDatatank, boolean, <new>boolean</new>)
 * I have separated the conditional branching of "removal of mappings"
 * and the "guessing of mapping" to different flags instead of relying
 * on the size of the mapper and a single "updateMapping" flag. I believe
 * this gives more control to the guessing functionality of the mapper.
 * MapperPropertyDialog's change is reflected on the param change.
 *
 * Revision 1.20  2000/02/29 19:40:48Z  AlexTeng
 * Fixed a possible NullPointerException where the BackendTank is
 * removed after some mappings were set but just before the user saves
 * the application. Added some code to continue the search for tables
 * if the table associated with the current mapping is null.
 *
 * Revision 1.19  2000/02/27 18:50:34Z  candidorodriguez
 * fixed bug Rx-00-02-0004
 * Revision 1.18  2000/02/09 17:06:44Z  candidorodriguez
 * now uses new exits
 * Revision 1.17  2000/02/04 20:32:41Z  AlexTeng
 * Removed all ClassNotFoundException from constructing a new
 * OSDataMapper object.
 *
 * Revision 1.16  2000/01/14 18:48:20Z  AlexTeng
 * Added implementation for HTML Parameter implementation.
 *
 * Revision 1.15  1999/11/22 21:16:28Z  AlexTeng
 * Added new constructor to assist the new IOSPipe interface
 * implementation in OSQueryPipe and OSUpdatePipe objects.
 *
 * Revision 1.14  1999/09/21 18:22:12Z  martingenhart
 * avoid invalid indices in guessMapping
 * Revision 1.13  1999/09/13 15:28:04  martingenhart
 * do not add new mapping while guessing if mappigs already exist, only
 * validate the existing ones
 * Revision 1.12  1999/08/14 19:26:46  martingenhart
 * several bugfixes, mapper changes to support CGI, etc.
 * cache all catalogs
 * Revision 1.11  1999/08/04 14:26:32  martingenhart
 * made OSLoadSaveHelper static, added debug logging
 * Revision 1.10  1999/08/02 17:13:22  paulhoward
 * When guessing mappings, don't do anything unless both tanks
 * are present.
 *
 * Revision 1.9  1999/07/30 13:33:02Z  martingenhart
 * keep existing mappings if they are still valid while guessing
 * Revision 1.8  1999/07/15 16:24:51  martingenhart
 * guess mappings on tank drops, fixed mapper bugs
 * Revision 1.7  1999/06/24 22:27:38  martingenhart
 * added PSApplication to th eIPersist interface
 * Revision 1.6  1999/06/18 02:16:47  AlexTeng
 * Changed getUniqueId() method in class Util to static.
 * Updated this source file to reflect the change.
 *
 * Revision 1.5  1999/06/03 21:38:27  martingenhart
 * added mapper browsers, fixed save/load
 * Revision 1.4  1999/05/18 22:58:35  martingenhart
 * fixes for load/save application
 * Revision 1.3  1999/05/13 23:32:18  martingenhart
 * fixes for load application
 * Revision 1.2  1999/05/10 21:18:33  martingenhart
 * implemented load/save functionality
 * Revision 1.1  1999/04/28 21:48:36  martingenhart
 * Initial revision
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.util.PSCollection;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;


/**
 * A wrapper around the PSDataMapper object that provides all data and its
 * access for the UIFigure objects.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSDataMapper extends PSDataMapper implements IGuiLink,
                                                          IPersist,
                                                          IDataCataloger
{
  /**
   * Default constructor.
   */
   public OSDataMapper()
   {
        super();
   }

  /**
   * Copy constructor.
   *
   * @param source the source object.
   */
   public OSDataMapper( PSDataMapper source )
   {
      super();
      super.copyFrom( source );
   }

  /**
   * Copy constructor.
   *
   * @param   source   the source object.
   */
   public OSDataMapper(OSDataMapper source)
   {
        super();
        copyFrom(source);
   }

  /**
   * Copy the passed object into this object.
   *
   * @param   source   the source object.
   */
   public void copyFrom(OSDataMapper source)
   {
      super.copyFrom((PSDataMapper) source);
   }


  //////////////////////////////////////////////////////////////////////////////
  // Implementation of IDataCataloger
   public void catalogData(ObjectType iObjType, CatalogReceiver container)
   {
      if (iObjType.equals(ObjectType.HTML_PARAM))
      {
         catalogHtmlParam(container);
      }
      else if (iObjType.equals(ObjectType.UDF))
      {
         catalogUdfs(container);
      }
   }


   /**
    * Performs local cataloging of html parameters.
    *
    * @param container The <CODE>CatalogReceiver</CODE> object to store the
    * cataloged results.
    *
    * @see CatalogReceiver
    */
   private void catalogHtmlParam( CatalogReceiver container )
   {
      // retrieve all mappings
      for (int i = 0; i < size(); i++)
      {
         PSDataMapping mapping = (PSDataMapping)get(i);

         IPSBackEndMapping bMapping = mapping.getBackEndMapping();
         IPSDocumentMapping dMapping = mapping.getDocumentMapping();

         if ( bMapping instanceof PSHtmlParameter )
            container.add( ((PSHtmlParameter)bMapping).getName() );

         if ( dMapping instanceof PSHtmlParameter )
            container.add( ((PSHtmlParameter)dMapping).getName() );

         // get conditionals if they exist
         PSCollection conditionals = mapping.getConditionals();
         if ( (null != conditionals) && (!conditionals.isEmpty()) )
         {
            for ( int j = 0; j < conditionals.size(); j++ )
            {
               PSConditional cond = (PSConditional)conditionals.get(j);
               IPSReplacementValue param = cond.getVariable();
               if ( param instanceof PSHtmlParameter )
                  container.add( ((PSHtmlParameter)param).getName() );
               param = cond.getValue();
               if ( param instanceof PSHtmlParameter )
                  container.add( ((PSHtmlParameter)param).getName() );
            }
         }
      }
   }


   /**
    * Performs local cataloging of UDfs.
    *
    * @param container The <CODE>CatalogReceiver</CODE> object to store the
    * cataloged results. This will never be <CODE>null</CODE>.
    *
    * @see CatalogReceiver
    */
   private void catalogUdfs( CatalogReceiver container )
   {
      Map mapAppUdfs = (Map)container.get( CatalogReceiver.CATALOG_UDF_APP );
      Map mapServerUdfs = (Map)container.get( CatalogReceiver.CATALOG_UDF_SERVER );

      // need to check if MapperPropertyDialog is open (if editor != null); if
      // it is, catalog through the Dialog, not the data object (OSDataMapper)
      UIFigure fig = getFigure();
      IEditor editor = null;
      if ( null != fig )
         editor = fig.getEditor();

      if ( null == fig || null == editor )
      {
         // get udfs from mapper
         for (int i = 0; i < size(); i++)
         {
            PSDataMapping mapping = (PSDataMapping) get( i );
            OSExtensionCall call = null;
            if (mapping.getBackEndMapping() instanceof OSExtensionCall)
               call = (OSExtensionCall) mapping.getBackEndMapping();
            else if (mapping.getDocumentMapping() instanceof OSExtensionCall)
               call = (OSExtensionCall) mapping.getDocumentMapping();

            if (call != null)
               registerExtension( call, mapAppUdfs, mapServerUdfs );
         }
      }
      else
      {
         /////////////////////////// editor catalog ////////////////////////////
         MapperTableModel table =
               (MapperTableModel)((MapperPropertyDialog)editor).getMapperTable();
         for (int i=0; i<table.getRowCount(); i++)
         {
            // omit empty or incomplete rows
            if (table.getValueAt(i, MapperTableModel.XML).toString().equals("") ||
                  table.getValueAt(i, MapperTableModel.BACKEND).toString().equals(""))
                 continue;

            OSExtensionCall call = null;
            if (table.getValueAt(i, MapperTableModel.BACKEND) instanceof
                  OSExtensionCall)
            {
               call = (OSExtensionCall) table.getValueAt( i,
                  MapperTableModel.BACKEND );
            }
            else if (table.getValueAt(i, MapperTableModel.XML) instanceof
                  OSExtensionCall)
            {
               call = (OSExtensionCall) table.getValueAt( i,
                  MapperTableModel.XML );
            }

            if (call != null)
               registerExtension( call, mapAppUdfs, mapServerUdfs );
         }
      }
      container.put( CatalogReceiver.CATALOG_UDF_APP, mapAppUdfs );
      container.put( CatalogReceiver.CATALOG_UDF_SERVER, mapServerUdfs );
      editor = null;
   }


   /**
    * Adds the extension definition referrenced by the supplied extension call
    * to one of the supplied Maps based on its extension type, using the
    * extension name as a key.
    *
    * @param call extension to be registered; assumed not <code>null</code>
    * @param mapAppUdfs receives the call if extension type is
    * <code>UDF_APP</code>; assumed not <code>null</code>
    * @param mapServerUdfs receives the call if extension type is
    * <code>UDF_GLOBAL</code>; assumed not <code>null</code>
    */
   private void registerExtension(OSExtensionCall call, Map mapAppUdfs,
                                  Map mapServerUdfs)
   {
      if (mapAppUdfs != null && call.isApplicationUdf())
      {
         mapAppUdfs.put(call.getExtensionRef().getExtensionName(),
            call.getExtensionDef());
      }
      else if (mapServerUdfs != null && call.isGlobalUdf())
      {
         mapServerUdfs.put( call.getExtensionRef().getExtensionName(),
            call.getExtensionDef() );
      }
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSPipe))
      {
         String [] astrParams =
         {
            "PSPipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         PSPipe pipe = (PSPipe) store;
         copyFrom(pipe.getDataMapper());

         // restore GUI information
         OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);
         return true;
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }

      return false;
    }


   /**
    * Returns the local reference that the mapper data object retains from the
    * application.
    *
    * @return The UDF set that contains all the different UDFs.
    */
   public PSUdfSet getUdfSet()
   {
      return m_udfSet;
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.
    * E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSPipe))
      {
         String [] astrParams =
         {
            "PSPipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this mapper into the provided pipe
         PSPipe pipe = (PSPipe) store;
         pipe.setDataMapper(this);

         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }
   }

  /*
   * This function does a new guess for the provided dataset. If just one tank is
   * attached, the mapping is cleared only. If both tanks are attached, a new
   * guess will be made and the result is stored into the provided dataset.
   *
   * @param dataset the dataset to do the guess for
   */
  //////////////////////////////////////////////////////////////////////////////
   public void guessMapping(OSDataset dataset)
   {
      if (dataset != null)
      {
         if (dataset.getPageDataTank() instanceof OSPageDatatank &&
             dataset.getPipe().getBackEndDataTank() instanceof OSBackendDatatank)
         {
            OSPageDatatank page = (OSPageDatatank) dataset.getPageDataTank();
            OSBackendDatatank backend = (OSBackendDatatank) dataset.getPipe().getBackEndDataTank();
            if (null == page || null == backend)
               return;

            boolean doGuess = false;
            if ( size() == 0 )
               doGuess = true;

            guessMapping(backend, page, true, doGuess);
         }
      }
   }

  /*
   * This function does a new guess for the provided dataset. If just one tank is
   * attached, the mapping is cleared only. If both tanks are attached, a new
   * guess will be made and the result is stored into the provided dataset.
   *
   * @param backend the backend datatank
   * @param page the page datatank
   * @param doRemove <CODE>true</CODE> if want to loop through existing mappings
   * that are not valid, meaning the backend entry does not match the page
   * entry.
   * @param doGuess <CODE>true</CODE> if mappings should be guessed
   */
  //////////////////////////////////////////////////////////////////////////////
   public void guessMapping(OSBackendDatatank backend, OSPageDatatank page,
                            boolean doRemove , boolean doGuess)
   {
      Vector pageFields = new Vector(page.getColumns());
      Vector backendFields = new Vector(backend.getColumns());

      // see if we have all the data
      if( pageFields.size() == 0 || backendFields.size() == 0 )
      {
         return; // nothing to do
      }

      if (doRemove)
      {
         // first loop through existing mappings and remove all which are not
         // valid anymore
         for (int i=0, n=size(); i<n; i++)
         {
            boolean removeMapping = true;
            PSDataMapping mapping = (PSDataMapping) get(i);
            String xmlField = mapping.getXmlField();

            System.out.println( " xmlField: "+xmlField );

            IPSBackEndMapping backendMapping = mapping.getBackEndMapping();
            for (int j=0, m=pageFields.size(); j<m; j++)
            {
                 String pageField = (String) pageFields.elementAt(j);
               if (pageField.equalsIgnoreCase(xmlField))
               {
                  // found an XML match, now search for the backend
                  for (int k=0, q=backendFields.size(); k<q; k++)
                  {
                      String backendField = (String) backendFields.elementAt(k);
                     if (backendMapping instanceof PSBackEndColumn)
                     {
                        PSBackEndColumn backendColumn = (PSBackEndColumn) backendMapping;
                        int index = backendField.indexOf(".");
                        String tableName = "";
                          String colName = "";
                          if (index != -1)
                        {
                             tableName = backendField.substring(0, index);
                           colName = backendField.substring(index + 1 ,
                                                            backendField.length());
                        }

                        // just in case the table (BackendTank) was removed right before
                        // the mapper was saved with mapping data
                        if ( null == backendColumn.getTable() ||
                             null == backendColumn.getTable().getTable() )
                           continue;

                        if (backendColumn.getTable().getTable().equals(tableName) &&
                            backendColumn.getColumn().equals(colName))
                        {
                           // the current mapping is still valid
                           removeMapping = false;

                           // remove the contents of this mapping from XML and
                           // backend columns, so they will not be guessed again
                           pageFields.removeElementAt(j);
                           j--;
                           m--;
                           backendFields.removeElementAt(k);
                           k--;
                           q--;
                           break;
                        }
                     }

                     // break since we already know whether or not to keep this
                     // mapping
                     if (!removeMapping)
                        break;
                  }
               }
            }
            if (removeMapping)
            {
               remove(mapping);
               i--;
               n--;
            }
         }
      }

      if (doGuess)
      {
         // now guess all remaining entries
         for (int i=0, n=pageFields.size(); i<n; i++)
         {
              String pageField = (String) pageFields.elementAt(i);
              pageField = pageField.substring(pageField.lastIndexOf("/")+1,
                                            pageField.length());
            for (int j=0, m=backendFields.size(); j<m; j++)
            {
               String backendField = (String) backendFields.elementAt(j);
                 backendField = backendField.substring(backendField.lastIndexOf(".")+1,
                                                     backendField.length());
               if (backendField.equalsIgnoreCase(pageField))
                 {
                  // found a match, add it to the mapper
                  pageField = (String) pageFields.elementAt(i);
                   backendField = (String) backendFields.elementAt(j);
                  int index = backendField.indexOf(".");
                    String tableName = "";
                    String colName = "";
                  if (index != -1)
                   {
                       tableName = backendField.substring(0, index);
                       colName = backendField.substring(index + 1,
                                                      backendField.length());
                  }

                  try
                  {
                     PSBackEndTable table = null;
                     PSCollection tables = backend.getTables();
                     for (int k=0; k<tables.size(); k++)
                     {
                        PSBackEndTable test = (PSBackEndTable) tables.get(k);
                        if (test.getTable().equals(tableName))
                        {
                           table = test;
                           break;
                        }
                     }

                     // this should never happen, but keep going
                     if (table == null)
                        continue;

                     PSBackEndColumn column = new PSBackEndColumn(table,
                                                                  colName);

                     PSDataMapping mapping = new PSDataMapping(pageField,
                                                               column);
                       this.add(mapping);
                  }
                  catch (IllegalArgumentException e)
                  {
                     e.printStackTrace();
                  }
                  break;
               }
            }
         }
      }
   }

  /**
    * Responsible for removing any mis-matched BackEnd column references from
    * the mapper. This method compares the column names in the BackEndDataTank
    * and the column names of the mappings within the mapper.
    *
    * @param enumColumns All BackEndColumn mappings will be cleared if an empty
    * enumeration is passed in. If it is <CODE>null</CODE>, exception is thrown.
    * If parameter contains no columns all mappings contain PSBackEndColumn will
    * be removed.
    * @return <CODE>true</CODE> if any entries were kept at the end of this
    * process. <CODE>false</CODE> is returned if no entries remains.
    * @throw If the parameter is <CODE>null</CODE>, an IllegalArgumentException
    * will be thrown.
    */
   public boolean removeMismatchedMappings( Enumeration enumColumns )
   {
      if ( null == enumColumns )
         throw new IllegalArgumentException("enumColumns parameter cannot be null!");

      // storing all the columns in the hashmap for optimization; stored as
      // { columnName(regardless of table name), actual PSBackEndColumn object }
      HashMap mapMapperCols = new HashMap(5);
      for ( int i = 0; i < this.size(); i++ )
      {
         PSDataMapping mapping = (PSDataMapping)this.get(i);
         IPSBackEndMapping beMapping = mapping.getBackEndMapping();
         if ( beMapping instanceof PSBackEndColumn )
         {
            PSBackEndColumn column = (PSBackEndColumn)beMapping;
            mapMapperCols.put( column.getColumn(), mapping );
         }
      }

      while ( enumColumns.hasMoreElements() )
      {
         PSBackEndColumn beColumn = (PSBackEndColumn)enumColumns.nextElement();
         String sColumnName = beColumn.getColumn();
         //ystem.out.print( sColumnName+" ?= " );

         PSDataMapping mapping = (PSDataMapping)mapMapperCols.get( sColumnName );

         // remove the mappings that we do not need to remove; thus ending up
         // with a hashmap with a list of mappings to be removed from "this"
         // datamapper
         if ( null != mapping )
         {
            //System.out.println("keeping entry: "+sColumnName);
            mapMapperCols.remove( sColumnName );
         }
      }

      Vector vRemovalList = new Vector(5);
      for ( int i = 0; i < this.size(); i++ )
      {
         PSDataMapping mapping = (PSDataMapping)this.get(i);
         IPSBackEndMapping beMapping = mapping.getBackEndMapping();
         if ( (beMapping instanceof PSBackEndColumn) )
         {
            PSBackEndColumn column = (PSBackEndColumn)beMapping;
            String columnName = column.getColumn();

            Object columnObj = mapMapperCols.get( columnName );
            if ( null != columnObj )
               vRemovalList.add( mapping ); // add mapping reference from mappings
                                            // from mapper; thus correct reference
         }
      }

      for ( int i = 0; i < vRemovalList.size(); i++ )
      {
         this.remove( vRemovalList.get( i ) );
      }

      return ( 0 != this.size() );
   }


   /////////////////////////////////////////////////////////////////////////////
   // implementations for IGuiLink
   public void setFigure(UIFigure fig) { m_owner = fig; }
   public void release() { m_owner = null; }
   public UIFigure getFigure() { return m_owner; }

  //////////////////////////////////////////////////////////////////////////////
  // member used explicitly withing th eIGuiLink interface
   private UIFigure m_owner = null;

   private PSUdfSet m_udfSet = null;
}
