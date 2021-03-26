/******************************************************************************
 *
 * [ OSDataset.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.util.PSCollection;

import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.Vector;

/**
 * This class converts all the sub-objects of PSDataSet into their OS...
 * counterparts.
 */
public class OSDataset extends PSDataSet implements IGuiLink,
                                                    IPersist,
                                                    IDataCataloger
{
   /**
    */
   public OSDataset()
   {
      super( "Dataset" );

      initName();
      // making HtmlOutputEnabled enabled on default.
      OSRequestor requestor = new OSRequestor();
      setRequestor(requestor);
   }

   /**
    */
   public OSDataset( String strName )
   {
        super( strName );

      initName();
      // making HtmlOutputEnabled enabled on default.
      OSRequestor requestor = new OSRequestor();
      setRequestor(requestor);
   }

   /**
    * DataSetTypes, used w/ <code>getType</code>
    */
   public static final int DST_QUERY = 1;
   public static final int DST_UPDATE = 2;
   // derived classes should make their values between 3 and 99, inclusive
   // the type cannot be determined
   public static final int DST_UNKNOWN = 100;

   /**
    * @return one of the DST_... types that indicates the type of the supplied dataset
    * If the dataset type cannot currently be determined, DST_UNKNOWN is
    * returned. The most derived class that the caller is interested should be
    * used to determine the type.
   **/
   public static int getType(PSDataSet ds)
   {
      int type = DST_UNKNOWN;
      PSPipe pipe = ds.getPipe();
      if (null != pipe)
      {
         if (pipe instanceof PSUpdatePipe)
            type = DST_UPDATE;
         else
            type = DST_QUERY;
      }
      return type;
   }

   /**
    * Checks the object currently in the dataset and converts it to OSRequestor
    * if it is currently a PSRequestor.
   **/
   @Override
   public PSRequestor getRequestor()
   {
      PSRequestor req = super.getRequestor();
      try
      {
         if ( req != null && !(req instanceof OSRequestor ))
         {
            setRequestor(new OSRequestor(req));
         }
      }
      catch ( IllegalArgumentException e )
      {
         // ignored, this should never happen in this situation
      }
      return req;
   }


   /**
    * Creates a new object that takes all of its properties from the supplied
    * dataset.
    *
    * @param ds a valid dataset
    *
    * @throws NullPointerException  if ds is null
    *
    */
   public OSDataset( PSDataSet ds )
   {
      super( ds.getName());

      copyFrom( ds,null,null );

      // making HtmlOutputEnabled enabled on default.
      OSRequestor requestor = new OSRequestor(getRequestor());
      setRequestor(requestor);
   }


   /**
    * Creates a new object that takes all of its properties from the supplied
    * dataset.
    *
    * @param ds a valid dataset
    *
    * @throws NullPointerException  if ds is null
    *
    */
   public OSDataset( OSDataset ds )
   {
      super( ds.getName());

      copyFrom( ds,null,null );

      // making HtmlOutputEnabled enabled on default.
      OSRequestor requestor = new OSRequestor(getRequestor());
      setRequestor(requestor);
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param ds a valid OSDataSet. If null, an IllegalArgumentException is
    * thrown.
    */
   public void copyFrom( OSDataset ds )
   {
      if ( null == ds )
         throw new IllegalArgumentException();

      copyFrom(ds,null,null );
   }


   /**
    * We override the base class' copy because we want to create OS... equivalents
    * for all PS... objects in the dataset. For any object reference, we are doing
    * a deep copy.
    *
     * @todo [Paul] This code should not know about exits. Exits are standalone objects
     * that should be restored by the same class restoring other app window
     * objects. Because of the way it's done, I think there are probably some
     * bugs here w/ certain copy/paste operations.
    */
   public void copyFrom( PSDataSet ds,PSApplication app,Properties config)
   {
      if ( null == ds )
         /* this could be handled by the base class, but this makes us
         consistent w/ the constructors */
         throw new IllegalArgumentException();
      
      // Remember requestor.
      // On the next step this requestor is replaced
      // by a requestor from ds and some UI-specific data is lost.
      // Instead copy ds.getRequestor() data to the current requestor
      // using req.copyFrom(). This will keep the UI-specific data while
      // replacing base requestor data.
      OSRequestor req = (OSRequestor) getRequestor();

      /* Get all non-reference fields */
      super.copyFrom( ds );

      if (ds.getDataEncryptor() != null)
          setDataEncryptor(new OSDataEncryptor(ds.getDataEncryptor()));

      if (ds.getPipe() != null)
      {
          if (ds.getPipe() instanceof PSQueryPipe)
            setPipe(new OSQueryPipe((PSQueryPipe) ds.getPipe()));
         else if (ds.getPipe() instanceof PSUpdatePipe)
         {
            setPipe(new OSUpdatePipe((PSUpdatePipe) ds.getPipe()));
         }
         else
         {
            throw new IllegalArgumentException("Unrecognized class for pipe "
                  + ds.getPipe().toString());
         }

         PSPipe pPipe=ds.getPipe();
         // if we are loading
         if( app != null && config != null )
         {
            PSExtensionCallSet pResult=pPipe.getResultDataExtensions();
            PSExtensionCallSet pInput=pPipe.getInputDataExtensions();
            // set the owner
            OSExitCallSet exits = new OSExitCallSet();
            exits.setFigure(this.getFigure());
            // if both sets are loaded
            if( pResult != null && pInput != null )
            {
               PSExtensionCallSet pSaved=null;
               // save the input
               pSaved=pInput;
               // reset
               pPipe.setInputDataExtensions(null);
               // make a new set
               OSExitCallSet  result=new OSExitCallSet();
               // and load it
               result.load(app,pPipe,config);
               // recreate the exit
               recreateExit( result, AppFigureFactory.POST_JAVA_EXIT );

               pPipe.setInputDataExtensions(pSaved);

               // save the result
               pSaved=pResult;
               // reset the result
               pPipe.setResultDataExtensions(null);
               // load it
               exits.load(app,pPipe,config);
               // restore the result
               pPipe.setResultDataExtensions(pSaved);
               // and restore it
               recreateExit( exits, AppFigureFactory.PRE_JAVA_EXIT );
            }
            else
            {
               // single exit
               String strType=new String();

               // load the exit
               exits.load(app,pPipe,config);

               if(pResult != null )
               {
                  strType=AppFigureFactory.POST_JAVA_EXIT;
               }
               else if( pInput != null )
               {
                  strType=AppFigureFactory.PRE_JAVA_EXIT;
               }

               if ( null != strType && strType.length() > 0 )
               {
                  // recreate the object
                  recreateExit( exits, strType );
               }
            }
         }
      }

      if (ds.getPageDataTank() != null)
         setPageDataTank(new OSPageDatatank(ds.getPageDataTank()));

      if(ds.getRequestor() != null)
      {
         if (req == null)
         {
            req = new OSRequestor((OSRequestor) getRequestor());
         }
         req.copyFrom(ds.getRequestor());
         setRequestor(req);
      }

      // result pages are managed in the application
      if (ds.getResultPager() != null)
         setResultPager(new OSResultPager(ds.getResultPager()));
   }


   /**
    * (Re)creates the exit on the connector
    *
    * @param result the exit set to be used to create the exit, may not be
    * <code>null</code>.
    *
    * @param strType the connector to attach to, may not be <code>null</code> or 
    * empty, and must be a valid figure type.
    */
   public  void recreateExit( OSExitCallSet  result, String strType )
   {
      if (result == null)
         throw new IllegalArgumentException("result may not be null");
      
      if (strType == null || strType.trim().length() == 0)
         throw new IllegalArgumentException("strType may not be null or empty");
      
      UIConnectableFigure fig=null;
      // get the figure manager
      FigureFactory fact=FigureFactoryManager.getFactoryManager().getFactory(
            "com.percussion.E2Designer.AppFigureFactory" );
      try
      {
         // create the figure
         fig=fact.createFigure(strType);
         // set the set
         fig.setData(result);
         // set the connector name
         String name=new String();

         if(strType.equals(AppFigureFactory.PRE_JAVA_EXIT) )
         {
            name="RigidConnPtPreIcon";
         }
         else if(  strType.equals(AppFigureFactory.POST_JAVA_EXIT))
         {
            name="RigidConnPtPostIcon";
         }
         if( name.length() >0 )
         {
            // attach it
            reattachFigure(name,fig);
         }
         // add the figure
         this.getFigure().add(fig);

      }
      catch ( FigureCreationException e )
      {
         System.out.println("Could not create UIConnectableFigure");
      }
  }

  /**
   * search trough all the connectors on the figure for the requested connector.
   *
   * @param connectorName the connector to attach.
   *
   * @param toBeAdded the figure to attach to the connnector.
   */
   private void reattachFigure( String connectorName,
                                UIConnectableFigure toBeAdded)
   {
      // find the connector
      int limit=this.getFigure().getComponentCount();
      if( limit > 0   )
      {
         Component child=null;
         for(int count=0;count<limit;count++)
         {
            // get the child
            child=this.getFigure().getComponent(count);

            // is a rigid connector
            if( child  instanceof  UIRigidConnectionPoint )
            {
               UIRigidConnectionPoint connector=(UIRigidConnectionPoint)child;
               String componentName=connector.getName();
               if( componentName.equals(connectorName) )
               {
                  toBeAdded.createDynamicConnection(connector);
                  break;
               }
            }
         }
      }
   }


   /**
    * Implementation of IDataCataloger.
    * @see IDataCataloger
    */
   public void catalogData(ObjectType iObjType, CatalogReceiver container)
   {
      if (iObjType.equals(ObjectType.HTML_PARAM))
      {
         catalogHtmlParam(iObjType, container);
      }
      else if(iObjType.equals(ObjectType.UDF))
      {
         catalogUdfs(iObjType,container);
      }
   }


   /**
    * Performs the action of cataloging for HTML Parameters.
    *
    * @param container A storage <CODE>Map</CODE> derivation object for catalog.
    */
   private void catalogHtmlParam(ObjectType iObjType, CatalogReceiver container )
   {
      // get all the HTML parameter data from the attached objects; data from:
      // 1) mapper
      // 2) selector
      // 3) all attached result pages (on either side)
      // 4) requestor
      // 5) all attached static webpages

      IOSPipe osPipe = (IOSPipe)getPipe();
      UTPipeNavigator pNavigator = new UTPipeNavigator();
      UIFigure pipeFig = null;
      IDataCataloger catalogPerformer = null;

      // get RequestTypeHtmlParamName from this application if this dataset
      // is an update dataset
      if (getFigure().getName().equals(AppFigureFactory.UPDATE_DATASET))
      {
         UIFigureFrame frame = Util.getOwnerFrameOf( this.getFigure() );
         catalogPerformer = (IDataCataloger)frame.getData();
         catalogPerformer.catalogData(ObjectType.HTML_PARAM, container);
      }

      // check if the UIPipeFrame of this dataset object is open; if it is,
      // there may be inconsistancies from the pipe objects.
      boolean isEditorOpen = false;
      if ( !(getFigure().getName().equals(AppFigureFactory.BINARY_RESOURCE)) &&
           null != getFigure().getEditor() )
      {
         Vector allFigs = ((UIPipeFrame)getFigure().getEditor()).getAll();
         isEditorOpen = true;
         for ( int i = 0; i < allFigs.size(); i++ )
         {
            UIFigure fig = (UIFigure)allFigs.get(i);
            if ( fig.getId() == PipeFigureFactory.PIPE_ID )
            {
               pipeFig = fig;
               break;
            }
         }
      }

      // get cataloger data from Requestor
      OSRequestor requestor = (OSRequestor)getRequestor();
      if ( null != requestor )
         requestor.catalogData(iObjType, container);

      // get cataloger data from all AppView objects attached to this dataset
      // ie: ResultPage and ExternalInterface objects
      UTAppNavigator appNav = new UTAppNavigator();
      Vector vFigures = appNav.getAllAttachingFigures( (UIConnectableFigure)getFigure() );
      for ( int i = 0; i < vFigures.size(); i++ )
      {
         UIConnectableFigure figure = (UIConnectableFigure)vFigures.get(i);
         // Ignore all datasets attaching to this dataset; this prevents
         // an infinite loop problem.
         if ( !(figure.getName().equals(AppFigureFactory.DATASET)) &&
              !(figure.getName().equals(AppFigureFactory.QUERY_DATASET)) &&
              !(figure.getName().equals(AppFigureFactory.UPDATE_DATASET)) )
         {
            Object figureData = figure.getData();
            if ( figureData instanceof IDataCataloger )
               ((IDataCataloger)figureData).catalogData( iObjType, container );
         }
      }
      //OSDataSelector dataSelector = null;
      // Get cataloger data from DataSelector
      if ( !isEditorOpen )
         catalogPerformer = (IDataCataloger)osPipe.getDataSelector();
      else
      {
         UIFigure selectorFig = pNavigator.getSelector( pipeFig );
         if ( null != selectorFig )
            catalogPerformer = (IDataCataloger)selectorFig.getData();
      }
      if ( null != catalogPerformer )
         catalogPerformer.catalogData( iObjType, container );

      // get cataloger data from DataMapper
      catalogPerformer = null;
      if ( !isEditorOpen )
      {
         if ( osPipe instanceof OSQueryPipe )
            catalogPerformer = (IDataCataloger)((OSQueryPipe)osPipe).getDataMapper();
         else //
            catalogPerformer = (IDataCataloger)((OSUpdatePipe)osPipe).getDataMapper();
      }
      else
      {
         UIFigure mapperFig = pNavigator.getMapper( pipeFig );
         if ( null != mapperFig )
            catalogPerformer = (IDataCataloger)mapperFig.getData();
      }
      if ( null != catalogPerformer )
         catalogPerformer.catalogData( iObjType, container );
   }


   /**
    * Performs the action of cataloging for UDFs.
    *
    * @param container A storage <CODE>Map</CODE> derivation object for catalog.
    */
   private void catalogUdfs(ObjectType iObjType, CatalogReceiver container )
   {
      Component editor = (Component)getFigure().getEditor();

      if ( editor != null )
      {
         // go through UIPipeFrame for objects
         UIPipeFrame pipeFrame = (UIPipeFrame)editor;
         UTPipeNavigator nav = new UTPipeNavigator();
         Vector figList = nav.getAllFiguresAttached( nav.getPipe( pipeFrame.getPipe() ) );
         System.out.println( "figList: "+figList.size() );
         for ( int j = 0; j < figList.size(); j++ )
         {
            UIFigure figure = (UIFigure)figList.get(j);
            Object data = figure.getData();
            if ( data instanceof IDataCataloger )
               ((IDataCataloger)data).catalogData( iObjType, container );
         }
      }
      else
      {
         // TODO [Alex]: there is no quick method to gather all data objects
         // within OSDataset. If we create one, this code here will not need to
         // be maintained.

         // go through dataset for objects
         editor = null;
         PSPipe pipe = getPipe();

         if ( null != pipe.getBackEndDataTank()
              && ( pipe.getBackEndDataTank() instanceof IDataCataloger ) )
         {
            ((IDataCataloger)pipe.getBackEndDataTank()).catalogData( iObjType,
                                                                container );
         }
         if ( null != pipe.getDataMapper()
             &&  ( pipe.getDataMapper() instanceof IDataCataloger ))
         {
            ((IDataCataloger)pipe.getDataMapper()).catalogData( iObjType,
                                                                container );
         }
      }
   }


   /*************
      IGuiLink interface implementation
   *************/
   public void setFigure( UIFigure fig )
   {
      m_owner = fig;
   }

   public void release()
   {
      m_owner = null;
   }

   public UIFigure getFigure()
   {
      return m_owner;
   }

   // implementation

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSDataSet))
      {
         Object[] astrParams =
         {
            "PSDataSet"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
          PSDataSet dataset = (PSDataSet) store;

         copyFrom(dataset,app,config);

         // restore GUI information
           if (m_owner != null)
              m_owner.invalidateLabel();

         OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);

         /* load page tank now - Todo [Paul] This is wrong. base classes should not
            know about derived classes */
         if (!(this instanceof OSBinaryDataset))
         {
            OSPageDatatank pageTank = new OSPageDatatank();
            pageTank.load(app, this, config);
            setPageDataTank(pageTank);
         }  

         return true;
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }

      return false;
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
      if (!(this instanceof OSBinaryDataset) &&
         getPageDataTank() instanceof OSPageDatatank)
      {
         ((OSPageDatatank) getPageDataTank()).cleanup(app);
      }
      
      
      
   }

   //////////////////////////////////////////////////////////////////////////////
   // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSApplication))
      {
         Object[] astrParams =
         {
            "PSApplication"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         /* save page datatank first - Todo [Paul] This is wrong. base classes
            should not know about derived classes */
         if (!(this instanceof OSBinaryDataset) &&
            getPageDataTank() instanceof OSPageDatatank)
         ((OSPageDatatank) getPageDataTank()).save(app, this, config);

         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save the dataset into given store
         PSApplication application = (PSApplication) store;
         PSCollection datasets = application.getDataSets();
         if (datasets == null)
            datasets = new PSCollection("com.percussion.design.objectstore.PSDataSet");

         datasets.remove(this);
         datasets.add(this);
           application.setDataSets(datasets);

         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (IllegalArgumentException e)
      {
          e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
          e.printStackTrace();
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }
   }

   /** Attaches a unique number after the name of this object.
    */
   private void initName()
   {
      setName(getName() + new Integer(Util.getUniqueId()).toString());
   }


   /**
    * This function will default the requestors name to the page tanks name only
    * if the requestor has no name yet
    */
   public void defaultRequestorName()
   {
      try
      {
         PSPageDataTank pagetank = getPageDataTank();
         PSRequestor requestor = getRequestor();
         if(pagetank != null)
         {
            if(requestor == null)
               requestor = new OSRequestor();

            if(requestor.getRequestPage() == null ||
               requestor.getRequestPage().length() == 0)
            {
               if(pagetank.getSchemaSource() != null)
               {
                  URL urlsource = pagetank.getSchemaSource();
                  String requestPage = Util.stripPath( urlsource.getFile(), "" );
                  requestor.setRequestPage(requestPage);
                  setRequestor(requestor);
               }
            }
         }
      }
      catch (IllegalArgumentException e)
      {
          e.printStackTrace();
      }
   }

   // private storage
   private UIFigure m_owner;
}

