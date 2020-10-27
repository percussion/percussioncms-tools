/******************************************************************************
 *
 * [ AppFigureFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.error.PSIllegalArgumentException;

import java.awt.*;
import java.text.MessageFormat;

/**
 * Factory for creating the UI objects that are used for visually creating
 * an application.
 */
public class AppFigureFactory extends FigureFactory
{
   /**
    * Unique identifiers for each figure. The actual value is not important,
    * except it must be >= Util.FIRST_VALID_ID.
    */
   public static final int RESULT_PAGE_ID = Util.FIRST_VALID_ID + 1;
   public static final int DIRECTED_CONNECTION_ID = Util.FIRST_VALID_ID + 2;
   public static final int DATASET_ID = Util.FIRST_VALID_ID + 3;
   public static final int EXTERNAL_INTERFACE_ID = Util.FIRST_VALID_ID + 4;
   public static final int STYLESHEET_SELECTOR_ID = Util.FIRST_VALID_ID + 7;
   public static final int NOTIFIER_ID = Util.FIRST_VALID_ID + 8;
   public static final int BINARY_RESOURCE_ID = Util.FIRST_VALID_ID + 9;
   public static final int SECURITY_ID = Util.FIRST_VALID_ID + 10;
   public static final int REQUESTOR_ID = Util.FIRST_VALID_ID + 11;
   public static final int APPLICATION_FILE_ID = Util.FIRST_VALID_ID + 12;
   public static final int DRAG_APPLICATION_FILE_ID = Util.FIRST_VALID_ID + 13;
   public static final int APPLICATION_ICON_ID = Util.FIRST_VALID_ID + 14;
   ///////////////////////// needed for transplant /////////////////////////////
   public static final int JAVA_EXIT_ID = Util.FIRST_VALID_ID + 15;
   public static final int PRE_JAVA_EXIT_ID = Util.FIRST_VALID_ID + 16;
   public static final int POST_JAVA_EXIT_ID = Util.FIRST_VALID_ID + 17;
   ///////////////////////// needed for transplant /////////////////////////////

  /** A Figure representing an Xsl file from the local file system */
  public static final int XSL_FILE_ID = Util.FIRST_VALID_ID + 18;

   public static final String INSERTABLE = "Insertable";

   /**
    * The internal names for the insert actions. The e2 resource bundle should
    * have entries matching this name.
    */
   public static final String RESULT_PAGE = "ResultPage";
   public static final String DIRECTED_CONNECTION = "DirectedConnection";

    /* This is not for general use, it should only be used when the type of
       dataset is not known ahead of time (e.g. when loading) */
    public static final String DATASET = "Dataset";
    // QUERY and UPDATE DATASETs created w/ empty pipes of the appropriate type
   public static final String QUERY_DATASET = "QueryDataset";
   public static final String UPDATE_DATASET = "UpdateDataset";

   public static final String EXTERNAL_INTERFACE = "ExtInterface";
   public static final String STYLESHEET_SELECTOR = "StylesheetSelector";
   public static final String NOTIFIER = "Notifier";
   public static final String BINARY_RESOURCE = "BinaryResource";
   public static final String SECURITY = "AppSecurity";
   public static final String REQUESTOR = "Requestor";
   public static final String APPLICATION_FILE = "ApplicationFile";
  public static final String APPLICATION_ICON = "ApplicationIcon";
  ///////////////////////// needed for transplant /////////////////////////////
  // Mutable figures//
  public static final String JAVA_EXIT = "JavaExit";
  public static final String PRE_JAVA_EXIT = "PreJavaExit";
  public static final String POST_JAVA_EXIT = "PostJavaExit";

  public static final String PRE_PROCESS_CP = "RigidConnPtPreIcon";
  public static final String POST_PROCESS_CP = "RigidConnPtPostIcon";
  ///////////////////////// needed for transplant /////////////////////////////

  /** A Figure representing an Xsl file from the local file system */
  public static final String XSL_FILE = "XslFile";

   // constructors
   /**
    * Creates a new application figure factory. This method should only be
    * used by the FigureFactoryManager. All users of the class should obtain
    * an instance from the FigureFactoryManager.
    * <p>
    * Fills up the base class' vector with the names and categories of the
    * objects that can be added to the process view window.
    *
    * @see FigureFactoryManager
    */
   AppFigureFactory( )
   {
      String fullname = getClass().getName();
      CLASS_NAME = fullname.substring( fullname.lastIndexOf( "." ) + 1);

      m_FigureList.add(new FigureInfo(RESULT_PAGE, INSERTABLE,
            RESULT_PAGE_ID ));

      m_FigureList.add(new FigureInfo(DIRECTED_CONNECTION, INSERTABLE,
            DIRECTED_CONNECTION_ID ));

      m_FigureList.add(new FigureInfo(DATASET, DATASET,
            DATASET_ID ));

      m_FigureList.add(new FigureInfo(QUERY_DATASET, INSERTABLE,
            DATASET_ID ));

      m_FigureList.add(new FigureInfo(UPDATE_DATASET, INSERTABLE,
            DATASET_ID ));

      m_FigureList.add(new FigureInfo(EXTERNAL_INTERFACE, EXTERNAL_INTERFACE,
            EXTERNAL_INTERFACE_ID ));

      m_FigureList.add(new FigureInfo(APPLICATION_FILE, APPLICATION_FILE,
            APPLICATION_FILE_ID ));

      m_FigureList.add(new FigureInfo(NOTIFIER, NOTIFIER,
            NOTIFIER_ID ));

      m_FigureList.add(new FigureInfo(SECURITY, SECURITY,
            SECURITY_ID ));

      m_FigureList.add(new FigureInfo(APPLICATION_ICON, APPLICATION_ICON,
            APPLICATION_ICON_ID ));

      m_FigureList.add(new FigureInfo(BINARY_RESOURCE, INSERTABLE,
        BINARY_RESOURCE_ID ));

      m_FigureList.add(new FigureInfo(JAVA_EXIT, JAVA_EXIT,
       JAVA_EXIT_ID ));

      m_FigureList.add(new FigureInfo(PRE_JAVA_EXIT, PRE_JAVA_EXIT,
        PRE_JAVA_EXIT_ID ));

      m_FigureList.add(new FigureInfo(POST_JAVA_EXIT, POST_JAVA_EXIT,
        POST_JAVA_EXIT_ID ));

      m_FigureList.add(new FigureInfo(XSL_FILE, XSL_FILE,
        XSL_FILE_ID ));
   }

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
    public UIConnectableFigure createFigure(String strType)
      throws FigureCreationException
   {
      if ( null == strType )
      {
         Object [] astrParams =
         {
            strType
         };
         throw new IllegalArgumentException( MessageFormat.format(
            E2Designer.getResources().getString( "NullArgFormat" ), astrParams ));
      }
      if ( 0 == strType.trim().length())
      {
         Object [] astrParams =
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
         Object [] astrParams =
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
         // key for image used for connection points
         String strConnectionPointInputIcon = "FlexConnPtInputIcon";
         String strConnectionPointOutputIcon = "FlexConnPtOutputIcon";

         if (strType.equals( RESULT_PAGE ))
         {
            figure = new UIConnectableFigure( fi.getName(),
               new OSResultPage(), "WebpagePropertyDialog", fi.getId(), CLASS_NAME);
               
            // add connection points
            Dimension figureSize = figure.getBaseImageSize();

            // creating connection points
            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                  strConnectionPointOutputIcon,
                  figure, new Point(figureSize.width-1, figureSize.height/2),
                  UIConnectionPoint.POS_RIGHT,
                  UIConnectionPoint.CP_ID_RIGHT);

               // add allowed connections
               cp.addAllowedId(DIRECTED_CONNECTION_ID);
               cp.addAllowedId(BINARY_RESOURCE_ID);
               cp.addAllowedId(DATASET_ID);
               figure.addConnectionPoint(cp);
            }
            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                     strConnectionPointInputIcon,
                     figure, new Point(0, figureSize.height/2),
                     UIConnectionPoint.POS_LEFT,
                     UIConnectionPoint.CP_ID_LEFT);
                  // add allowed connections
                  cp.addAllowedId(DIRECTED_CONNECTION_ID);
                  cp.addAllowedId(DATASET_ID);
                  cp.setAsInput( true );
                  figure.addConnectionPoint(cp);
            }

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);
            
            // setting figure label information
            figure.setLabelHelper(new IFigureLabelHelper()
            {
               public String getLabelText(Object data)
               {
                  return ((OSResultPage)data).getStylesheetForDisplay();
               }

               public String getToolTipText(Object data)
               {
                  return null;
               }
            });
            figure.getMenuActions().add(PSEditStylesheetAction.class.getName());
            figure.getMenuActions().add(PSEditStylesheetSourceAction.class.getName());
            figure.getMenuActions().add(PSGenerateStylesheetFromSourceAction.class.getName());
         }
         else if (strType.equals(DIRECTED_CONNECTION))
         {
            UIConnector conn = new UIConnector(fi.getName(), null, null,
               fi.getId(), CLASS_NAME);
            figure = conn;
         }
         else if (strType.equals(QUERY_DATASET) ||
            strType.equals(UPDATE_DATASET) ||
            strType.equals(DATASET))
         {
            // add an empty pipe of the correct type
            OSDataset ds = new OSDataset();
            PipeFigureFactory f = (PipeFigureFactory)
            FigureFactoryManager.getFactoryManager().getFactory(
               "com.percussion.E2Designer.PipeFigureFactory");

            if (strType.equals(QUERY_DATASET))
            ds.setPipe(
                  (OSQueryPipe) f.createFigure(PipeFigureFactory.QUERY_PIPE).getData());
            else if ( strType.equals(UPDATE_DATASET))
               ds.setPipe(
                     (OSUpdatePipe) f.createFigure(PipeFigureFactory.UPDATE_PIPE).getData());

            figure = new ResizableObject(fi.getName(), ds, "UIPipeFrame",
                  fi.getId(), CLASS_NAME);

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);

            // add connection points (1 input connector)
            Dimension figureSize = figure.getBaseImageSize();

            // setting figure label information
            figure.setLabelHelper(new IFigureLabelHelper()
            {
             public String getLabelText(Object data)
                  {
                     String label = null;
                     try
                     {
                        OSRequestor req = (OSRequestor) ((OSDataset) data).getRequestor();
                        label = req.getRequestPage();

                        if (null == label || 0 == label.trim().length())
                 {
                   // display only
                   label = E2Designer.getResources().getString("Dummy");
                          return label;
                 }
                 else
                 {
                   req.setInternalName(label);
                   return req.getInternalName();
                 }
                     }
                     catch (ClassCastException e)
                     {
                        // passed in wrong object, return empty string
                 return null;
                     }
                  }

             public String getToolTipText(Object data)
             {
               return null;
             }
            });

            /* The data object passed into this method is a placeholder so it
               can be edited. The real OSRequestor object should be obtained from the dataset
               that this connector is attached to (this can be done using
               the owner of the cp).
               TODOph: remove requestor from cp totally */
            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                     strConnectionPointInputIcon,
                     figure, new Point( 0, figureSize.height/2),
                     UIConnectionPoint.POS_LEFT,
                     UIConnectionPoint.CP_ID_LEFT,
                     new OSRequestor());
               // add allowed connections
               cp.addAllowedId(DIRECTED_CONNECTION_ID);
               cp.addAllowedId(RESULT_PAGE_ID);
               cp.addAllowedId(EXTERNAL_INTERFACE_ID);
                   cp.addAllowedId(DATASET_ID);
               cp.setAsInput( true );
               cp.setPopupName(E2Designer.getResources().getString("requestorProperties"));
               cp.setEditorName("DatasetInputConnectorPropertyDialog");
               figure.addConnectionPoint(cp);
               ((OSRequestor) ds.getRequestor()).setFigure(cp);

               //add the requestor editing object
               figure.setLabelEditor(cp);
            }

            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                     strConnectionPointOutputIcon,
                     figure, new Point(figureSize.width, figureSize.height/2),
                     UIConnectionPoint.POS_RIGHT,
                  UIConnectionPoint.CP_ID_RIGHT);
               // add allowed connections
               cp.addAllowedId(DIRECTED_CONNECTION_ID);
               cp.addAllowedId(RESULT_PAGE_ID);
                 cp.addAllowedId(BINARY_RESOURCE_ID);
                   cp.addAllowedId(DATASET_ID);
               //only allow one dataset attached to another dataset at a time
               cp.addAttacheeConstraints(new IDQuantityConstraint(cp, DATASET_ID, 1));
               //only allow one ID at a time attached
               cp.addAttacheeConstraints(new IDConsistencyConstraint(cp));

               figure.addConnectionPoint(cp);
            }
            // *** adding new Java Exit RigidConnectionPoints ***

            // First, adding "Pre-processing exit" ConnectionPoint to the  dataset
            UIRigidConnectionPoint rcp = new UIRigidConnectionPoint(
                     PRE_PROCESS_CP,
                     figure, new Point(5, figureSize.height/2),
                     UIConnectionPoint.POS_CENTER,
               UIConnectionPoint.CP_ID_JAVA_EXIT);
            // add allowed connections
            rcp.addAllowedId(JAVA_EXIT_ID);
            rcp.addAllowedId(PRE_JAVA_EXIT_ID);
            //only allow one java exit to a dataset connector at a time
            figure.addConnectionPoint(rcp);

            int p=figureSize.width/4*4-4;

            // Second, adding "Post-processing exit" ConnectionPoint to the  dataset
            rcp = new UIRigidConnectionPoint(
                     POST_PROCESS_CP,
                     figure, new Point(p, figureSize.height/2),
                     UIConnectionPoint.POS_CENTER,
               UIConnectionPoint.CP_ID_JAVA_EXIT);
            // add allowed connections
            rcp.addAllowedId(JAVA_EXIT_ID);
            rcp.addAllowedId(POST_JAVA_EXIT_ID);

            //only allow one java exit to a dataset connector at a time
            figure.addConnectionPoint(rcp);

            // add auxilliary editor for table cache flushing
            figure.addAuxEditor("flushTables",
               "com.percussion.E2Designer.FlushTableMetaDataDialog");
         }
         else if (strType.equals( EXTERNAL_INTERFACE ))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSExternalInterface(), null, fi.getId(), CLASS_NAME);

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);

            Dimension figureSize = figure.getBaseImageSize();
            final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                  strConnectionPointOutputIcon,
                  figure, new Point(figureSize.width-1, figureSize.height/2),
                  UIConnectionPoint.POS_RIGHT,
               UIConnectionPoint.CP_ID_RIGHT);
            // add allowed connections
            cp.addAllowedId(DIRECTED_CONNECTION_ID);
            cp.addAllowedId(DATASET_ID);
            figure.addConnectionPoint(cp);

            // setting figure label information
            figure.setLabelHelper(new IFigureLabelHelper()
            {
              public String getLabelText(Object data)
              {
               return ((OSExternalInterface)data).getLabelText();
              }

              public String getToolTipText(Object data)
              {
               return null;
              }
            });
            figure.getMenuActions().add(PSEditExternalInterfaceFileAction.class.getName());
         }
         else if(strType.equals(APPLICATION_FILE))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSApplicationFile(), null, fi.getId(), CLASS_NAME);

              // setting figure label information
              figure.setLabelHelper(new IFigureLabelHelper()
              {
                public String getLabelText(Object data)
                {
                  return ((OSApplicationFile)data).getInternalName();
                }

                public String getToolTipText(Object data)
                {
                  return null;
                }
              });

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);
         }
         else if(strType.equals(SECURITY))
         {
            figure = new UIConnectableFigure(fi.getName(),
               null, "AppSecDialog", fi.getId(), CLASS_NAME);

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);
         }
         else if(strType.equals( APPLICATION_ICON ))
         {
            figure = new UIConnectableFigure(fi.getName(),
               null, "AppPropDialog", fi.getId(), CLASS_NAME);

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);
         }
         else if (strType.equals( STYLESHEET_SELECTOR ))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSResultPage(), null, fi.getId(), CLASS_NAME);

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);

            Dimension figureSize = figure.getBaseImageSize();
            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                     strConnectionPointOutputIcon,
                     figure, new Point(0, figureSize.height/2),
                     UIConnectionPoint.POS_RIGHT,
                  UIConnectionPoint.CP_ID_RIGHT);
               // add allowed connections
               cp.addAllowedId(DIRECTED_CONNECTION_ID);
               cp.addAllowedId(DATASET_ID);
               cp.setAsInput( true );
               figure.addConnectionPoint(cp);
            }
            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                     strConnectionPointOutputIcon,
                     figure, new Point(figureSize.width/2, figureSize.height-1),
                     UIConnectionPoint.POS_BOTTOM,
                  UIConnectionPoint.CP_ID_BOTTOM);
               // add allowed connections
               cp.addAllowedId(DIRECTED_CONNECTION_ID);
               cp.addAllowedId(RESULT_PAGE_ID);
               figure.addConnectionPoint(cp);
            }
            {
               final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                     strConnectionPointOutputIcon,
                     figure, new Point(figureSize.width-1, figureSize.height/2),
                     UIConnectionPoint.POS_RIGHT,
                  UIConnectionPoint.CP_ID_RIGHT_1);
               // add allowed connections
               cp.addAllowedId(DIRECTED_CONNECTION_ID);
               cp.addAllowedId(RESULT_PAGE_ID);
               figure.addConnectionPoint(cp);
            }
         }
         else if (strType.equals( NOTIFIER ))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSNotifier(), "NotifierPropertyDialog", fi.getId(), CLASS_NAME)

               {
                  public boolean isOn()
                  {
                     if(getData() != null &&
                        getData() instanceof OSNotifier)
                        {
                        OSNotifier data = (OSNotifier)getData();
                        return data.getRecipients() != null
                              && data.getRecipients().size() > 0;
                     }

                     return false;
                  }
               };

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);
         }
         else if (strType.equals(BINARY_RESOURCE))
         {
            figure = new UIConnectableFigure(fi.getName(),
               new OSBinaryDataset(), "BinaryResourceDialog", fi.getId(),
               CLASS_NAME);

            // we do not want this object to attach to anyone
            figure.setCanBeAttached(false);

            // add connection points (1 input connector)
            Dimension figureSize = figure.getBaseImageSize();

            /* The data object passed into this method is a placeholder so it
               can be edited. The real OSRequestor object should be obtained from the dataset
               that this connector is attached too (this can be done using
               the owner of the cp).
               TODOph: remove requestor from cp totally */
            final UIFlexibleConnectionPoint cp = new UIFlexibleConnectionPoint(
                  strConnectionPointInputIcon,
                  figure, new Point( 0, figureSize.height/2),
                  UIConnectionPoint.POS_LEFT,
                  UIConnectionPoint.CP_ID_LEFT,
                  new OSNonTextRequestor());
            // add allowed connections
            cp.addAllowedId(DIRECTED_CONNECTION_ID);
            cp.addAllowedId(DATASET_ID);
            cp.addAllowedId(RESULT_PAGE_ID);
            cp.setAsInput( true );
            cp.setPopupName(E2Designer.getResources().getString("requestorProperties"));
            cp.setEditorName("DatasetInputConnectorPropertyDialog");
            figure.addConnectionPoint(cp);

            figure.setLabelHelper(new IFigureLabelHelper()
            {
               // this should be identical to the query/update dataset
               public String getLabelText(Object data)
               {
                  String label = null;
                  try
                  {
                     OSRequestor req = (OSRequestor) ((OSDataset) data).getRequestor();
                     label = req.getRequestPage();
                     if (null == label || 0 == label.trim().length())
                        label = E2Designer.getResources().getString("Dummy");
                     return label;
                  }
                  catch ( ClassCastException e )
                  {
                     // passed in wrong object, return empty string
                  }
                  return label;
               }

               public String getToolTipText(Object data)
               {
                  return null;
               }
            });

            //add the requestor editing object
            figure.setLabelEditor(cp);

            // add auxilliary editor for table cache flushing
            figure.addAuxEditor("flushTables",
               "com.percussion.E2Designer.FlushTableMetaDataDialog");

         }

         else if (strType.equals(JAVA_EXIT))
         {
           figure = new UIConnectableFigure(fi.getName(),
                  new OSExitCallSet(), "JavaExitsPropertyDialog", fi.getId(),
                  CLASS_NAME);
         }

         // allows the creation of specific exits
         else if (strType.equals(PRE_JAVA_EXIT))
         {
           String name=fi.getName();

           figure = new UIConnectableFigure(name,new OSExitCallSet(),
                 "JavaExitsPropertyDialog", fi.getId(), CLASS_NAME);
         }
         else if (strType.equals(POST_JAVA_EXIT))
         {

           String name=fi.getName();

           figure = new UIConnectableFigure(name,new OSExitCallSet(),
                 "JavaExitsPropertyDialog", fi.getId(), CLASS_NAME);
         }
         else if (strType.equals( XSL_FILE ))
         {
           figure = new UIConnectableFigure(fi.getName(),
                  new OSFile(), null, fi.getId(), CLASS_NAME);
         }
         else
         {
            Debug.assertTrue( false, E2Designer.getResources(),
               "MissingFigureType", null );
         }

      }
      catch ( PSIllegalArgumentException e )
      {
         PSDlgUtil.showError(e, true, E2Designer.getResources().getString("OpErrorTitle"));
      }
      figure.setType(strType);
      return figure;
   }

   // variables
   private final String CLASS_NAME; // calculate from class obj
}

