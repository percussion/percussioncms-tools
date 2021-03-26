/*[ PipeFigureFactory.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.util.PSCollection;

import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;


/**
 * Factory for creating the UI objects that are used for visually creating
 * a pipe (or dataset).
 */
public class PipeFigureFactory extends FigureFactory
{
   /**
    * Unique identifiers for each figure. The actual value is not important,
    * except it cannot be Util.INVALID_ID.
    */
   public static final int CONTROL_PANEL_ID = Util.FIRST_VALID_ID + 1;
   public static final int PIPE_ID = Util.FIRST_VALID_ID + 2;
   public static final int ENCRYPTOR_ID = Util.FIRST_VALID_ID + 3;
   public static final int TRANSACTION_MGR_ID = Util.FIRST_VALID_ID + 4;
   public static final int RESULT_PAGER_ID = Util.FIRST_VALID_ID + 5;
  //////////////// removed for transplant ////////////////////////
   //public static final int FORMULA_ID = Util.FIRST_VALID_ID + 6;
  //////////////// removed for transplant ////////////////////////
   public static final int MAPPER_ID = Util.FIRST_VALID_ID + 7;
   public static final int SYNCHRONIZER_ID = Util.FIRST_VALID_ID + 8;
   public static final int SELECTOR_ID = Util.FIRST_VALID_ID + 9;
   public static final int PAGE_DATATANK_ID = Util.FIRST_VALID_ID + 10;
   public static final int BACKEND_DATATANK_ID = Util.FIRST_VALID_ID + 11;

    // Types
    public static final String INSERTABLE = "Insertable";

   // constructors
   /**
    * Creates a new pipe figure factory. This method should only be
    * used by the FigureFactoryManager. All users of the class should obtain
    * an instance from the FigureFactoryManager.
    * <p>
    * Fills up the base class' vector with the names and categories of the 
    * objects that can be added to the process view window.
    *
    * @see FigureFactoryManager
    */
   PipeFigureFactory( )
   {
    String fullname = getClass().getName();
    CLASS_NAME = fullname.substring( fullname.lastIndexOf( "." ) + 1);
  
      String strFigureName = BACKEND_DATATANK;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE,
            BACKEND_DATATANK_ID ));

      strFigureName = PAGE_DATATANK;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE,
            PAGE_DATATANK_ID ));

    strFigureName = QUERY_PIPE;
      m_FigureList.add(new FigureInfo(strFigureName, strFigureName,
            PIPE_ID ));

      strFigureName = UPDATE_PIPE;
      m_FigureList.add(new FigureInfo(strFigureName, strFigureName, 
            PIPE_ID ));

      strFigureName = ENCRYPTOR;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE, 
            ENCRYPTOR_ID ));

      strFigureName = TRANSACTION_MGR;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE, 
            TRANSACTION_MGR_ID ));

      strFigureName = RESULT_PAGER;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE, 
            RESULT_PAGER_ID ));
    //////////////// removed for transplant ////////////////////////
    /*
      strFigureName = FORMULA;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE,
            FORMULA_ID ));
    */
    //////////////// removed for transplant ////////////////////////
      strFigureName = MAPPER;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE, 
            MAPPER_ID ));

      strFigureName = SYNCHRONIZER;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE, 
            SYNCHRONIZER_ID ));

      strFigureName = SELECTOR;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE,
            SELECTOR_ID ));

/*      strFigureName = CONTROL_PANEL;
      m_FigureList.add(new FigureInfo(strFigureName, INSERTABLE,
            CONTROL_PANEL_ID ));
*/
   }


   /**
    * The internal names for the insert actions. The e2 resource bundle should
    * have entries matching this name.
    */
   public static final String CONTROL_PANEL = "ControlPanel";
   public static final String QUERY_PIPE = "QueryPipe";
   public static final String UPDATE_PIPE = "UpdatePipe";
   public static final String ENCRYPTOR = "Encryptor";
   public static final String TRANSACTION_MGR = "TransactionMgr";
   public static final String RESULT_PAGER = "ResultPager";
  //////////////// removed for transplant ////////////////////////
   //public static final String FORMULA = "Formula";
  //////////////// removed for transplant ////////////////////////
   public static final String MAPPER = "Mapper";
   public static final String SYNCHRONIZER = "Synchronizer";
   public static final String SELECTOR = "Selector";
   public static final String PAGE_DATATANK = "PageDatatank";
   public static final String BACKEND_DATATANK = "BEDatatank";


   /**
    * The file names of all property editor dialogs
    */
   private static final String PIPE_PROPERTY_EDITOR = "DatasetPropertyDialog";
   private static final String PAGE_TANK_PROPERTY_EDITOR = "PageTankPropertyDialog";
   private static final String MAPPER_PROPERTY_EDITOR = "MapperPropertyDialog";
   private static final String ENCRYPTOR_PROPERTY_EDITOR = "EncryptorPropertyDialog";
   private static final String SELECTOR_PROPERTY_EDITOR = "SelectorPropertyDialog";
   private static final String DATA_SYNCHRONIZER_PROPERTY_EDITOR = "DataSynchronizerPropertyDialog";
   //private static final String JAVA_EXITS_PROPERTY_EDITOR = "JavaExitsPropertyDialog";
   private static final String TRANSACTION_MGR_PROPERTY_EDITOR = "TransactionManagerPropertyDialog";
   private static final String RESULT_PAGER_PROPERTY_EDITOR = "ResultPagerPropertyDialog";
      // the following two are public because they are set in UIConnectableFigureBEDatatank
   public static final String QUERY_PIPE_DATATANK_PROPERTY_EDITOR = "UIJoinMainFrame";
   public static final String UPDATE_PIPE_DATATANK_PROPERTY_EDITOR = "MultiTableDatatankPropertyDialog";

   /**
    * If strType is a valid type, creates the figure associated with the type
    * and returns it. The type should be obtained from getFigureCategories and
    * getFigureTypes.
    *
    * @param strType name of the type of object to create. The string is case
    * insensitive.
    *
    * @throws IllegalArgumentException if strType is null or empty
    *
    * @throws UnsupportedOperationException if strType is not an available type
    *
    * @throws FigureCreationException if any errors occur while creating the figure.
    * The text of the exception is the detail message of the original exception.
    *
    * @see FigureFactory
    */
   @Override
   public UIConnectableFigure createFigure(String strType)
         throws FigureCreationException
   {
      if ( null == strType )
      {
         Object[] astrParams =
         {
            strType
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "NullArgFormat" ), astrParams ));
      }
      if ( 0 == strType.trim().length())
      {
         Object[] astrParams =
         {
            strType
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "EmptyStringErr" ), astrParams ));
      }

      // get the figure info from the vector
      FigureInfo fi = getElement( strType );
      if ( null == fi )
      {
         Object[] astrParams =
         {
            strType
         };
         throw new UnsupportedOperationException(MessageFormat.format(
               E2Designer.getResources().getString("UnsupportedFigure"),
               astrParams ));
      }

      UIConnectableFigure figure = null;
      try
      {
         // key for image used for connection points that connect to center of attachee
         String strConnectionPointIcon = "RigidConnPtFrontIcon";

         if (strType.equals( QUERY_PIPE ))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSQueryPipe(), PIPE_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);

            // add connection points
            Dimension figureSize = figure.getBaseImageSize();
            UIRigidConnectionPoint cp = null;

            cp = new UIRigidConnectionPoint(
                  "RigidConnPtLeftSideIcon",
                  figure, new Point(0, figureSize.height/2),
                  UIConnectionPoint.POS_LEFT,
            UIConnectionPoint.CP_ID_BACKEND_TANK);
            // add allowed connections
            cp.addAllowedId(BACKEND_DATATANK_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(52, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_SELECTOR);
            // add allowed connections
            cp.addAllowedId(SELECTOR_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(119, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_RESULT_PAGER);
            // add allowed connections
            cp.addAllowedId(RESULT_PAGER_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(186, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_MAPPER);
            // add allowed connections
            cp.addAllowedId(MAPPER_ID);
            figure.addConnectionPoint(cp);

        /////////////////// removed for transplant ///////////////////////////////
        /*
            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(235, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_FORMULA);
            // add allowed connections
            cp.addAllowedId(FORMULA_ID);
            figure.addConnectionPoint(cp);
        */
        /////////////////// removed for transplant ///////////////////////////////

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(274, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_ENCRYPTOR);
            // add allowed connections
            cp.addAllowedId(ENCRYPTOR_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  "RigidConnPtRightSideIcon",
                  figure, new Point( figureSize.width-1, figureSize.height/2),
                  UIConnectionPoint.POS_RIGHT,
            UIConnectionPoint.CP_ID_PAGE_TANK);
            // add allowed connections
            cp.addAllowedId(PAGE_DATATANK_ID);
            figure.addConnectionPoint(cp);


         }
         else if (strType.equals( UPDATE_PIPE ))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSUpdatePipe(), PIPE_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
               
            // add connection points
            Dimension figureSize = figure.getBaseImageSize();
            
            UIRigidConnectionPoint cp = new UIRigidConnectionPoint(
                  "RigidConnPtLeftSideIcon",
                  figure, new Point( 0, figureSize.height/2),
                  UIConnectionPoint.POS_LEFT,
            UIConnectionPoint.CP_ID_PAGE_TANK);
            // add allowed connections
            cp.addAllowedId(PAGE_DATATANK_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(30, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_ENCRYPTOR);
            // add allowed connections
            cp.addAllowedId(ENCRYPTOR_ID);
            figure.addConnectionPoint(cp);

        /////////////////// removed for transplant ///////////////////////////////
        /*
            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(69, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_FORMULA);
            // add allowed connections
            cp.addAllowedId(FORMULA_ID);
            figure.addConnectionPoint(cp);
        */
        /////////////////// removed for transplant ///////////////////////////////

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(118, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_MAPPER);
            // add allowed connections
            cp.addAllowedId(MAPPER_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(185, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_SYNCHRONIZER);
            // add allowed connections
            cp.addAllowedId(SYNCHRONIZER_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(252, figureSize.height/2),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_TRANSACTION_MGR);
            // add allowed connections
            cp.addAllowedId(TRANSACTION_MGR_ID);
            figure.addConnectionPoint(cp);

            cp = new UIRigidConnectionPoint(
                  "RigidConnPtRightSideIcon",
                  figure, new Point(figureSize.width-1, figureSize.height/2),
                  UIConnectionPoint.POS_RIGHT,
            UIConnectionPoint.CP_ID_BACKEND_TANK);
            // add allowed connections
            cp.addAllowedId(BACKEND_DATATANK_ID);
            figure.addConnectionPoint(cp);
         }
         else if ( strType.equals( BACKEND_DATATANK ))
         {
            figure = new UIConnectableFigureBEDatatank( fi.getName(), 
               new OSBackendDatatank(), fi.getId(), CLASS_NAME );
            // setting figure label information
            figure.setLabelHelper(new IFigureLabelHelper()
            {
               /**
                * Initially we default to the first table in the datatank
                * as the label text.
               **/
               public String getLabelText(Object data)
               {
                  String label = null;
                  try
                  {
                     OSBackendDatatank tank = (OSBackendDatatank) data;
                     PSCollection tables = tank.getTables();
                     PSBackEndTable table = null;
                     if ( null != tables && 0 < tables.size() )
                        table = (PSBackEndTable) tables.get(0);
                     if ( null != table )
                        label = table.getAlias();
                     else
                        label = E2Designer.getResources().getString(
                           "NoTables");
                  }
                  catch ( ClassCastException e )
                  {
                     // passed in wrong object, return empty string
                  }
                  return label;
               }

               public String getToolTipText(Object data)
               {
                  return new String("This is a backend datatank");
               }
            });
         }
         else if ( strType.equals( PAGE_DATATANK ))
         {
            figure = new UIConnectableFigure( fi.getName(),
               new OSPageDatatank(), PAGE_TANK_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
            // setting figure label information
            figure.setLabelHelper(new IFigureLabelHelper()
            {
               /**
                * Initially we default to the first table in the datatank
                * as the label text.
               **/
               public String getLabelText(Object data)
               {
                  String label = null;
                  try
                  {
                     OSPageDatatank tank = (OSPageDatatank) data;
                     if ( null != tank )
                     {
                        URL dtd = tank.getSchemaSource();
                        if ( null != dtd )
                           label = dtd.getFile();
                        else
                           label = E2Designer.getResources().getString(
                              "NoDTD");
                     }
                  }
                  catch ( ClassCastException e )
                  {
                     // passed in wrong object, return empty string
                  }
                  return label;
               }

               public String getToolTipText(Object data)
               {
                  return new String("This is a page datatank");
               }
            });
            figure.getMenuActions().add(PSEditDtdAction.class.getName());
         }
         else if ( strType.equals( ENCRYPTOR ))
         {
            figure = new UIConnectableFigure( fi.getName(), 
               new OSDataEncryptor(), ENCRYPTOR_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
         }
         else if ( strType.equals( TRANSACTION_MGR ))
         {
            figure = new UIConnectableFigure( fi.getName(),
               new OSTransaction(), TRANSACTION_MGR_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
         }
         else if ( strType.equals( RESULT_PAGER ))
         {
            figure = new UIConnectableFigure( fi.getName(), 
               new OSResultPager(), RESULT_PAGER_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
         }
         else if ( strType.equals( MAPPER ))
         {
            figure = new UIConnectableFigure( fi.getName(),
               new OSDataMapper(), MAPPER_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
         }
         else if ( strType.equals( SYNCHRONIZER ))
         {
            figure = new UIConnectableFigure( fi.getName(), 
               new OSDataSynchronizer(), DATA_SYNCHRONIZER_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
         }
         else if ( strType.equals( SELECTOR ))
         {
            figure = new UIConnectableFigure( fi.getName(), 
               new OSDataSelector(), SELECTOR_PROPERTY_EDITOR, fi.getId(), CLASS_NAME);
         }

      /////////////////// removed for transplant ///////////////////////////////
      /*
         else if ( strType.equals( FORMULA ))
         {
            figure = new UIConnectableFigure( fi.getName(),
               new OSExitCallSet(), JAVA_EXITS_PROPERTY_EDITOR, fi.getId(), CLASS_NAME );
         }
      */
      /////////////////// removed for transplant ///////////////////////////////

         else if ( strType.equals( CONTROL_PANEL ))
         {
            figure = new UIConnectableFigure( fi.getName(), 
               null, null, fi.getId(), CLASS_NAME);
               
        /////////////////// removed for transplant ///////////////////////////////
        /*
            // positions obtained from bitmap
            UIConnectionPoint cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point( 18, 32 ),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_FORMULA);
            // add allowed connections
            cp.addAllowedId(FORMULA_ID);
            figure.addConnectionPoint(cp);
        */
        /////////////////// removed for transplant ///////////////////////////////

            UIConnectionPoint cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point(47, 32 ),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_ENCRYPTOR);
            // add allowed connections
            cp.addAllowedId(ENCRYPTOR_ID);
            figure.addConnectionPoint(cp);
            

            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point( 76, 32 ),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_RESULT_PAGER);
            // add allowed connections
            cp.addAllowedId(RESULT_PAGER_ID);
            figure.addConnectionPoint(cp);
            
            cp = new UIRigidConnectionPoint(
                  strConnectionPointIcon,
                  figure, new Point( 107, 32 ),
                  UIConnectionPoint.POS_CENTER,
            UIConnectionPoint.CP_ID_TRANSACTION_MGR);
            // add allowed connections
            cp.addAllowedId(TRANSACTION_MGR_ID);
            figure.addConnectionPoint(cp);
         }
         else
         {
            Debug.assertTrue( false, E2Designer.getResources(), "MissingFigureType", null ); 
         }
      } 
      catch ( Exception e )
      {
         e.printStackTrace();
         throw new FigureCreationException( e.getLocalizedMessage());
      }
      figure.setType(strType);
      return figure;
   }

   // variables
   private String CLASS_NAME = null;   // calculated from class obj
}

