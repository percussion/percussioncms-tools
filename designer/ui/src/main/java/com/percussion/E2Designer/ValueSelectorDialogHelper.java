/******************************************************************************
 *
 * [ ValueSelectorDialogHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/* Helper class to assist in creation of the Vector of DataTypes required for
 * the ValueSelectorDialog constructor.
 *
 * User will need to call the getDataTypes() method to get the Vector that can
 * be used for the ValueSelectorDialog.
 *
 * @see ValueSelectorDialog
 */
public class ValueSelectorDialogHelper
{
   //TODO - ph: the ctors that take OS objects should be re-implemented in terms
   // of the ctors that take vectors (and the unneeded methods removed)
   /**
    * Constructor for the Helper class.
    *
    * @param backendTank   if there is a BackendDatatank attached. Can be null.
    * @param pageTank      if there is a PageDatatank attached. Can be null.
    */
   public ValueSelectorDialogHelper(
      OSBackendDatatank backendTank,
      OSPageDatatank pageTank)
   {
      // first initialize all types but columns/fields
      init(null);
      initDatatanks(backendTank, pageTank);
   }

   /**
    * Constructor for the Helper class.
    *
    * @param backendTank   if there is a BackendDatatank attached. Can be null.
    * @param pageTank      if there is a PageDatatank attached. Can be null.
    * @param type        for what should we initialize 
    * (ValueSelectorDialogHelper.MAPPER)
    */
   public ValueSelectorDialogHelper(
      OSBackendDatatank backendTank,
      OSPageDatatank pageTank,
      int type)
   {
      // first initialize all types but columns/fields
      if (type == MAPPER_SOURCE_BACKEND)
         initMapperSourceForBackend();
      else if (type == MAPPER_SOURCE_XML)
         initMapperSourceForXml();
      else if (type == MAPPER_TARGET)
         initMapperTarget();
      else
         throw new IllegalArgumentException();

      initDatatanks(backendTank, pageTank);
   }

   /**
    * Constructor for the Helper class.
    *
    * @param backendTanks a vector of backend tanks, never <code>null</code>,
    * may be empty.
    * @param pageTanks a vector of page tanks, never <code>null</code>, may
    * be empty.
    */
   public ValueSelectorDialogHelper(Vector backendTanks, Vector pageTanks)
   {
      if (backendTanks == null || pageTanks == null)
         throw new IllegalArgumentException();

      init(null);
      initDatatanks(backendTanks, pageTanks);
   }

   /**
    * Constructor for the Helper class.
    *
    * @param backendTanks a vector of backend tanks, never <code>null</code>,
    * may be empty.
    * @param pageTanks a vector of page tanks, never <code>null</code>, may be
    * empty.
    */
   public ValueSelectorDialogHelper(
      Vector backendTanks,
      Vector pageTanks,
      int type)
   {
      if (backendTanks == null || pageTanks == null)
         throw new IllegalArgumentException();

      // first initialize all types but columns/fields
      if (type == MAPPER_SOURCE_BACKEND)
         initMapperSourceForBackend();
      else if (type == MAPPER_SOURCE_XML)
         initMapperSourceForXml();
      else if (type == MAPPER_TARGET)
         initMapperTarget();
      else
         throw new IllegalArgumentException();

      initDatatanks(backendTanks, pageTanks);
   }

   /**
    * Constructor for the Helper class.  Enables mapping for all DTDataTypes.
    */
   public ValueSelectorDialogHelper()
   {
      initMapperSourceForAll();
   }

   /**
    * Standard helper initialization.
    * @param list a list of the strings that represents classes names, which 
    * will be used to instantiate <CODE>Class</CODE> objects associated with 
    * those classes or interfaces.
    * It can be <CODE>null</CODE>, and if so a default list will be used instead
    */
   private void init(List list)
   {
      m_vDataTypes = new Vector<IDataTypeInfo>();

      if (list == null)
         list = ms_lClasses;

      String className = "";
      try
      {
         for (int i = 0; i < list.size(); i++)
         {
            className = (String) list.get(i);
            Class newClass = Class.forName(className);
            //add a new instance of the class to the vector of data types
            m_vDataTypes.add((IDataTypeInfo) newClass.newInstance());
         }
      }
      catch (Exception e)
      {
         //if this happens, it is probably a coding bug, so don't need to i18n
         String msg =
            "Failed to load class '"
               + className
               + "' for value selector: "
               + e.getClass().getName()
               + ": "
               + e.getLocalizedMessage();
         throw new RuntimeException(msg);
      }
   }

   /**
    * Initialize the helper for mapper backend source side.
    */
   private void initMapperSourceForBackend()
   {
      init(null);

   }

   /**
    * Initialize the helper for mapper XML source side.
    */
   private void initMapperSourceForXml()
   {
      init(null);
   }

   /**
    * Initialization helper for mapping of all possible datatype sources.  
    * This method must contain all possible DTDataType sources.  It cannot 
    * however initialize a DTBackendColumn, as this requires a PSBackendTank 
    * that contains at least one table.
    */
   private void initMapperSourceForAll()
   {
      init(null);

      // add the DTXMLField
      Vector v = new Vector();
      DTXMLField xmlField = new DTXMLField(v);
      m_vDataTypes.add(xmlField);
   }

   /**
    * Initialize the helper for mapper target side.
    */
   private void initMapperTarget()
   {
      m_vDataTypes = new Vector<IDataTypeInfo>();
   }
   /**
    * Initialize the datatanks.
    *
    * @param dataTank      if there is a BackendDatatank attached. Can be null.
    * @param pageTank      if there is a PageDatatank attached. Can be null.
    */
   private void initDatatanks(
      OSBackendDatatank dataTank,
      OSPageDatatank pageTank)
   {
      // add the DTBackendColumn
      if (dataTank != null)
      {
         DTBackendColumn dtColumn = new DTBackendColumn(dataTank);
         m_vDataTypes.add(dtColumn);
      }

      // add the DTXMLField elements
      if (pageTank != null)
      {
         Vector v = pageTank.getColumns();
         DTXMLField xmlField = new DTXMLField(v);
         m_vDataTypes.add(xmlField);
      }
   }

   /**
    * Initialize the datatanks.
    *
    * @param backendTanks a vector of backend tanks
    * @param pageTanks a vector of page tanks
    */
   private void initDatatanks(Vector backendTanks, Vector pageTanks)
   {
      // add the DTBackendColumn
      if (backendTanks.size() != 0)
      {
         DTBackendColumn dtColumn = new DTBackendColumn(backendTanks);
         m_vDataTypes.add(dtColumn);
      }

      // add the DTXMLField elements
      if (pageTanks.size() != 0)
      {
         Vector<String> xmlFields = new Vector<String>();
         for (int i = 0, n = pageTanks.size(); i < n; i++)
         {
            OSPageDatatank pageTank = (OSPageDatatank) pageTanks.get(i);
            boolean exists = false;
            String file = pageTank.getSchemaSource().getFile();
            for (int j = 0; j < i; j++)
            {
               OSPageDatatank tank = (OSPageDatatank) pageTanks.get(j);
               if (file.equals(tank.getSchemaSource().getFile()))
               {
                  exists = true;
                  break;
               }
            }

            if (!exists)
               xmlFields.addAll(pageTank.getColumns());
         }
         DTXMLField xmlField = new DTXMLField(xmlFields);
         m_vDataTypes.add(xmlField);
      }
   }

   /**
    * @returns the Vector that can be used for the ValueSelectorDialog 
    * constructor.
    *
    * @see ValueSelectorDialog
    */
   public Vector<IDataTypeInfo> getDataTypes()
   {
      return m_vDataTypes;
   }

   /////////////////////////////////////////////////////////////////////////////
   private Vector<IDataTypeInfo> m_vDataTypes;

  /**
   * A default list of the strings that represent classes names, which will be
   * instantiated in {@link #init(List)}
   */
  final static private List<String> ms_lClasses = new ArrayList<String>();
  static
  {
      ms_lClasses.add("com.percussion.E2Designer.DTCgiVariable");
      ms_lClasses.add("com.percussion.E2Designer.DTCookie");
      ms_lClasses.add("com.percussion.E2Designer.DTDateLiteral");
      ms_lClasses.add("com.percussion.E2Designer.DTNumericLiteral");
      ms_lClasses.add("com.percussion.E2Designer.DTSingleHtmlParameter");
      ms_lClasses.add("com.percussion.E2Designer.DTHtmlParameter");
      ms_lClasses.add("com.percussion.E2Designer.DTTextLiteral");
      ms_lClasses.add("com.percussion.E2Designer.DTUserContext");
      ms_lClasses.add("com.percussion.E2Designer.DTContentItemStatus");  
      ms_lClasses.add("com.percussion.E2Designer.DTContentItemData");
      ms_lClasses.add("com.percussion.E2Designer.DTRelationshipProperty");                
      ms_lClasses.add(
         "com.percussion.E2Designer.DTOriginatingRelationshipProperty");
      ms_lClasses.add("com.percussion.E2Designer.DTMacro");
  }

   // initialzation for ...
   public static int MAPPER_SOURCE_BACKEND = 1;
   public static int MAPPER_SOURCE_XML = 2;
   public static int MAPPER_TARGET = 3;
}
