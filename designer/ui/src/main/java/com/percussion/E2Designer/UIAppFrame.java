/******************************************************************************
 *
 * [ UIAppFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.E2Designer.browser.BrowserFrame;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSDocumentMapping;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSVersionConflictException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.editors.form.PSXmlAppDebuggingAction;
import com.percussion.workbench.ui.editors.form.PSXmlAppExportAction;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditorActionBarContributor;
import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;

/**
 * This class implements a visual editor for E2 applications
 */
public class UIAppFrame extends UIFigureFrame
{
   // constructors
   /**
    * @param data a valid application
    *
    * @param store A valid Rx object store, used for other information needed
    * to create the GUI application
    *
    * @param readOnly the application was opened read only
   *
   * @throws NullPointerException if data is null
   */
   public UIAppFrame(OSApplication data, PSObjectStore store, boolean readOnly,
         final PSXmlApplicationEditor xmlApplicationEditor)
   {
      super(data.getName(), new Dimension(2000, 2000), xmlApplicationEditor);

      if ( null == store )
         throw new IllegalArgumentException( "object store can't be null" );
      m_readOnly = readOnly;

      this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

      m_timerListener = new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if(e.getSource() == m_timer)
            {
               m_timer.stop();
            }
            else if(e.getSource() == m_lockTimer)
            {
               extendApplicationLock();
               m_lockTimer.restart();
            }
         }
      };
      m_timer = new Timer(2 * (int) DateUtils.MILLIS_PER_SECOND, m_timerListener);
      m_lockTimer = new Timer(
            LOCK_EXTEND_MINS * (int) DateUtils.MILLIS_PER_MINUTE,
            m_timerListener);
      m_lockTimer.start();

      try
      {
         setApp(data);
         convertObjects(getApp(), store);

         m_ActionList = createInsertActions().toArray(new IAction[0]);

         // add custom drop actions
         //addActionListener(new AutoCreateResultPage());
         addActionListener(new DroppedPageDatatank());
         addActionListener(new DroppedBackendDatatank());
         // TODO: temp fix; side effect from bug (GBOD-4B5NNP) in AutoCreateDataset
         addActionListener(new AutoCreateDataset());
         addActionListener(new DroppedXslFile());
         // Bug Rx-00-03-0023: This must be added last
         addActionListener(new AutoCreateApplicationFile());

         // load the application
         load();
      }
      catch (MissingResourceException e)
      {
         Object [] astrParams =
         {
            e.getLocalizedMessage(),
            e.getKey()
         };
         E2Designer.FatalError( MessageFormat.format( getResources().getString( "MissingResourceExceptionFormat" ),
               astrParams ) );
      }
   }

   /**
    * Creates actions for "Insert" menu.
    */
   private Vector<IAction> createInsertActions()
   {
      final Vector<IAction> v = new Vector<>();
      final Enumeration eFigures =
         getFigureFactory().getFigureTypes(AppFigureFactory.INSERTABLE);
      while (eFigures.hasMoreElements( ))
      {
         final String strFigureName = (String) eFigures.nextElement( );
         final String id = getInsertIdFromFigureName(strFigureName);
         final IAction action = new Action(
               getActionName(id),
               ResourceHelper.getIcon2(getResources(), id))
               {
                  @Override
                  public void run()
                  {
                     final Runnable r = new Runnable()
                     {
                        public void run()
                        {
                           add(strFigureName);
                        }
                     };
                     SwingUtilities.invokeLater(r);
                  }
               };
         action.setId(id);
         action.setAccelerator(ResourceHelper.getAccelKey2(getResources(), id));
         action.setToolTipText(
               ResourceHelper.getToolTipText(getResources(), id));
         v.add(action);
      }
      return v;
   }

   @Override
   public List<Action> getDynamicActionListeners()
   {
      final List<Action> actions = super.getDynamicActionListeners();
      
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     saveApp(false, false);
                  }
               });
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_APP_SAVE);
         actions.add(action);
      }
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     saveAsApp(false, false);
                  }
               });
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_APP_SAVEAS);
         actions.add(action);
      }
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     viewAppProperties();
                  }
               });
            }
         };
         action.setId(PSXmlApplicationEditorActionBarContributor.MENU_APP_PROPERTIES);
         actions.add(action);
      }
      {
         final PSXmlAppDebuggingAction action = new PSXmlAppDebuggingAction(null);
         action.setApplication(getApp());
         action.setId(PSXmlAppDebuggingAction.XMLAPP_DEBUGGING);
         actions.add(action);
      }
      {
         final Action action = new Action()
         {
            @Override
            public void run()
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     exportApplication(getApp());
                  }
               });
            }
         };
         action.setId(PSXmlAppExportAction.XMLAPP_EXPORT);
         actions.add(action);
      }
      return actions;
   }

   /**
    *    Saves and exports the application
    */
   public void exportApplication(final PSApplication app)
   {
      try
      {
         final Runnable exportTask = new Runnable()
         {
            public void run()
            {
               doExportApplication(app);
            }
         };
         getMainFrame().lockSWTFor(exportTask);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
   }

   private void doExportApplication(PSApplication app)
   {
      ApplicationImportExport impExp = new ApplicationImportExport(
            new JFileChooser(System.getProperty("user.dir")));
      if (!prepareAppForSave(app))
      {
         return;
      }

      if (saveComponents(false))
      {
         /* N.B. TODOph
            After adding the new framework for saving app files, the behavior
            of import changed slightly. The old behavior saved all app files
            to the server and built the application in memory. It then exports
            the in-memory app and the files loaded from the server. After the
            mods, the app files were no longer written to the server by
            save components. Making this call here restores that behavior.
            However, this is bug-prone. First, it doesn't work unless the
            app has been saved at least once. Second the app files on the
            server and the application on the server can become out of sync.
            Don't have time to fix this now, and no-one has reported it yet
            so it gets delayed until later.
         */
         try
         {
            getApp().saveAppFiles(getObjectStore());
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         // todo: save UDFs
         impExp.exportApplication(getApp());
      }
   }

   /**
    * Saves the dataset into the application before we close the frame.
    *
    * @return boolean   returns true if the window should be closed, false
    * otherwise
    */
   //////////////////////////////////////////////////////////////////////////////
   public boolean onClose()
   {
      if ((m_timer == null) || m_timer.isRunning())
      {
         return false;
      }

      try
      {
         // should we release the lock and close the window?
         final boolean bReturn = saveApp(true, false);
         if (bReturn)
         {
            releaseApplicationLock();
            closeWindow();
         }
         else
         {
            m_timer.start();
         }

         return bReturn;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return false;
   }

   /**
    * Releases application lock.
    */
   public void releaseApplicationLock()
   {
      try
      {
         doReleaseLock();
         
         if (null != m_lockTimer)
         {
            m_lockTimer.stop();
            m_lockTimer = null;
         }

         if (m_timer != null)
         {
            m_timer.stop();
            m_timer.removeActionListener(m_timerListener);
            m_timer = null;
            m_timerListener = null;
         }
      }
      catch (PSServerException e)
      {
         PSDlgUtil.showErrorDialog(e.getLocalizedMessage(),
               getResources().getString("ServerConnErr"));
      }
      catch (PSAuthorizationException e)
      {
         PSDlgUtil.showErrorDialog(e.getLocalizedMessage(),
               getResources().getString("AuthErr"));
      }
      catch (PSLockedException e)
      {
         PSDlgUtil.showErrorDialog(e.getLocalizedMessage(),
               getResources().getString("LockErr"));
      }
      catch (PSAuthenticationFailedException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Calls backend lock release.
    */
   private void doReleaseLock() throws PSModelException, Exception, PSServerException, PSAuthorizationException, PSAuthenticationFailedException, PSLockedException
   {
      if (getApplicationRef() != null)
      {
         getAppModel().releaseLock(getApplicationRef());
      }
      if (getApplicationRef() == null || getApplicationRef().isPersisted())
      {
         getObjectStore().releaseApplicationLock(getApp());
      }
   }

   /**
    * Convenience method to access application model.
    */
   private IPSCmsModel getAppModel() throws PSModelException
   {
      return PSCoreFactory.getInstance().getModel(PSObjectTypes.XML_APPLICATION);
   }

   /**
    * Convenience method to access application reference.
    */
   private PSReference getApplicationRef()
   {
      return getXmlApplicationEditor().getApplicationRef();
   }

   /**
    * Returns current object store.
    */
   private PSObjectStore getObjectStore()
   {
      return E2Designer.getApp().getMainFrame().getObjectStore();
   }

   /**
    * Closes the window without saving data.
    */
   @Override
   public void closeWindow()
   {
      if (isClosed())
         return;
      super.closeWindow();
   }


   // Properties
   @Override
   public boolean hasPersistableData()
   {
      return true;
   }

   @Override
   public Object getData()
   {
      return getApp();
   }

   @Override
   public void onEdit()
   {
      viewAppProperties( );
   }

   /**
    * Returns <code>true</code> if the menu id is for the insert menu.
    */
   @Override
   public boolean hasActionItems( String strMenuId )
   {
      if (strMenuId.equals( PSXmlApplicationEditorActionBarContributor.MENU_INSERT ))
         return( true );
      else
         return super.hasActionItems( strMenuId );
   }


   /**
    * Returns an array of action items for the insert menu.
    *
    * @param strMenuId The internal name of the main menu item.
    */
   @Override
   public IAction[] getActionItems(String strMenuId)
   {
      if (strMenuId.equals(PSXmlApplicationEditorActionBarContributor.MENU_INSERT))
      {
         // verify consistency
         Debug.assertTrue(hasActionItems(strMenuId), getResources(),
               "DynActionsImplErr", null);
         return m_ActionList;
      }
      else
      {
         return super.getActionItems(strMenuId);
      }
   }


   // Operations
   /**
    * Returns the figure factory for this class. The exception that could occur
    * is a design time error, so it is allowed to propagate.
    */
   @Override
   protected FigureFactory getFigureFactory( )
   {
      return FigureFactoryManager.getFactoryManager( ).getFactory( sFIGURE_FACTORY );
   }


   /**
    * Forces connector lines to always be on top.
    */
   @Override
   protected Integer getLayer( UIConnectableFigure uic )
   {
      /* The absolute layer numbers are not important. Only thing that matters is
      that UIConnector layer is larger # than other figures. */
      return uic instanceof UIConnector ? 100 : 10;
   }




   /**
    * Saves the application to the object store. If the application has never
    * been saved before, saveAsApp()is called, otherwise the app is saved
    * without user intervention.
    *
    * @param close true if we should also close the frame after saving
    * @param validate true if validation is enabled while saving
    * @return boolean Returns <CODE>true</CODE> if application is saved,
    * <CODE>false</CODE> if the application was not saved.
    */
   public boolean saveApp(boolean close, boolean validate)
   {
      if(!checkExits())
      {
         return false;
      }

      if (isNewApplication())
      {
         return saveAsApp(close, true);
      }
      
      if (m_readOnly)
      {
         return saveAsApp(close, false);
      }

      if ( !isNewApplication() || !m_readOnly )
      {
         return save(close, validate, false);
      }
      else
      {
         return false;
      }
   }

   /**
    * This method does all the work of load an application from the server.
    */
   @SuppressWarnings("deprecation")
   private void load()
   {
      Cursor restoreCursor = E2Designer.getApp().getMainFrame().getCursor();
      Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
      E2Designer.getApp().getMainFrame().setCursor(waitCursor);

      try
      {
         // get application configuration and initialize the default positions
         Properties config = getApp().getUserProperties();
         if (null == config)
         {
            // create a new one if not found
            config = new Properties();
            getApp().setUserProperties(config);
         }
         OSLoadSaveHelper.initDefault(config);

         /*load contents for left application pane
         parse the application into its pieces and restore it to the right
         application pane*/
         FigureFactory factory = getFigureFactory();
         UIConnectableFigure uic;


         PSCollection datasets = getApp().getDataSets();
         HashMap<Integer, UIConnectableFigure> mapResultPages =
               new HashMap<>();
         Vector<UIConnectableFigure> vectorDatasets = new Vector<>();
         if (datasets != null)
         {
            for (Object o : datasets) {
               PSDataSet dataset = (PSDataSet) o;
               if (null != dataset) {
                  int type = OSBinaryDataset.getType(dataset);

                  if (type == OSBinaryDataset.DST_BINARY) {
                     uic = factory.createFigure(AppFigureFactory.BINARY_RESOURCE);

                     if (((IPersist) uic.getData()).load(getApp(), dataset, config)) {
                        add(uic);
                        OSLoadSaveHelper.nextAttached(config, uic);
                     }
                  } else if (type != OSBinaryDataset.DST_BINARY) {
                     if (OSDataset.DST_UNKNOWN == type) {
                        /* Datasets must have a type, so turn it into a query pipe */
                        dataset.setPipe(new OSQueryPipe());
                        type = OSDataset.DST_QUERY;
                     }
                     uic = factory.createFigure(OSDataset.DST_QUERY == type
                             ? AppFigureFactory.QUERY_DATASET
                             : AppFigureFactory.UPDATE_DATASET);

                     if (((IPersist) uic.getData()).load(getApp(), dataset, config)) {
                        add(uic);
                        OSLoadSaveHelper.nextAttached(config, uic);
                        vectorDatasets.addElement(uic);
                     }

                     // load result pages
                     PSResultPageSet resultPageSet = dataset.getOutputResultPages();
                     if (resultPageSet != null) {
                        PSCollection resultPages = resultPageSet.getResultPages();
                        if (resultPages != null) {
                           for (Object page : resultPages) {
                              PSResultPage resultPage = (PSResultPage) page;
                              Integer resultPageId = resultPage.getId();

                              UIConnectableFigure webpage;
                              if (mapResultPages.containsKey(resultPageId))
                                 webpage = mapResultPages.get(resultPageId);
                              else
                                 webpage = factory.createFigure(AppFigureFactory.RESULT_PAGE);

                              if (((IPersist) webpage.getData()).load(getApp(), resultPage, config)) {
                                 // add the result page
                                 if (!mapResultPages.containsKey(resultPageId)) {
                                    add(webpage);
                                    OSLoadSaveHelper.nextAttached(config, webpage);

                                    // add the just loaded result page to the map
                                    if (config.getProperty(UIAppFrame.SAVED_FROM_E2) != null ||
                                            config.getProperty(UIAppFrame.SAVED_FROM_DESIGNER) != null)
                                       mapResultPages.put(resultPageId, webpage);
                                 }

                                 // create and add connector
                                 UIConnector connector = (UIConnector) factory.createFigure(AppFigureFactory.DIRECTED_CONNECTION);
                                 add(connector);

                                 // attach connector between dataset and result page
                                 UIConnectionPoint cp = uic.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
                                 connector.createDynamicConnectionProgrammatic(cp, false);
                                 cp = webpage.getConnectionPoint(UIConnectionPoint.CP_ID_LEFT);
                                 connector.createDynamicConnectionProgrammatic(cp, true);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         // connect each result link to its target dataset
         // WARNING: each DataSet can only have 1 outputRequestLink, so
         // additional connections from a DataSet will be lost after save/load!
           Component[] comps = m_drawingPane.getComponents();
         for (Component comp : comps) {
            UIConnectableFigure dataset = (UIConnectableFigure) comp;
            if (dataset.getId() == AppFigureFactory.DATASET_ID) {
               OSDataset data = (OSDataset) dataset.getData();
               if (data.getOutputRequestLink() != null) {
                  PSRequestLink requestLink = data.getOutputRequestLink();
                  PSCollection cRequestLinks = null;
                  try {
                     cRequestLinks = new PSCollection("com.percussion.design.objectstore.PSRequestLink");
                  } catch (ClassNotFoundException e) {
                     e.printStackTrace();
                  }

                  cRequestLinks.add(requestLink);

                  // find our dataset
                  for (Component component : comps) {
                     UIConnectableFigure fig = (UIConnectableFigure) component;
                     if (fig.getId() == AppFigureFactory.DATASET_ID ||
                             fig.getId() == AppFigureFactory.BINARY_RESOURCE_ID) {
                        OSDataset inputdataset = (OSDataset) fig.getData();

                        if (requestLink.getTargetDataSet().equals(inputdataset.getName())) {
                           UIConnector connector = (UIConnector) factory.createFigure(AppFigureFactory.DIRECTED_CONNECTION);
                           add(connector);

                           // attach connector between dataset and result page
                           UIConnectionPoint cp = dataset.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
                           connector.createDynamicConnectionProgrammatic(cp, false);
                           cp = fig.getConnectionPoint(UIConnectionPoint.CP_ID_LEFT);
                           connector.createDynamicConnectionProgrammatic(cp, true);
                           ((IPersist) connector.getData()).load(getApp(), cRequestLinks, config);
                           break;
                        }
                     }
                  }
               }
            } else if (dataset.getId() == AppFigureFactory.RESULT_PAGE_ID) {
               UIConnectableFigure resultpage = dataset;
               if (resultpage.getData() instanceof OSResultPage) {
                  OSResultPage resultPage = (OSResultPage) resultpage.getData();
                  PSCollection requestLinks = resultPage.getRequestLinks();
                  if (requestLinks != null) {
                     // group request links with same target dataset name
                     Vector vLinkGroups = createRequestLinkGroups(requestLinks);
                     for (Object vLinkGroup : vLinkGroups) {
                        PSCollection cLinks = (PSCollection) vLinkGroup;
                        for (Component component : comps) {
                           UIConnectableFigure fig = (UIConnectableFigure) component;
                           if (fig.getId() == AppFigureFactory.DATASET_ID ||
                                   fig.getId() == AppFigureFactory.BINARY_RESOURCE_ID) {
                              OSDataset inputdataset = (OSDataset) fig.getData();
                              PSRequestLink requestLink = (PSRequestLink) cLinks.get(0);
                              if (requestLink.getTargetDataSet().equals(inputdataset.getName())) {
                                 UIConnector connector = (UIConnector) factory.createFigure(AppFigureFactory.DIRECTED_CONNECTION);
                                 add(connector);

                                 // attach connector between dataset and result page
                                 UIConnectionPoint cp = resultpage.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
                                 connector.createDynamicConnectionProgrammatic(cp, false);
                                 cp = fig.getConnectionPoint(UIConnectionPoint.CP_ID_LEFT);
                                 connector.createDynamicConnectionProgrammatic(cp, true);
                                 ((IPersist) connector.getData()).load(getApp(), cLinks, config);
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         // restore external interfaces from the applications user configuration
         Enumeration keys = config.propertyNames();
         while (keys.hasMoreElements())
         {
            String key = (String) keys.nextElement();
            if (key.startsWith(OSExternalInterface.KEY_EXTERNAL_INTERFACE_KEY))
            {
               // get all elements for this external interface
               String strKey = config.getProperty(key);
               int iKey = new Integer(strKey);
               String strConnectedFigures = config.getProperty(
                  OSExternalInterface.KEY_EXTERNAL_INTERFACE_FIGURES + strKey);

               // create the figure itself and set the figure ID store in the user
               // config, the call load to restore its location
                    uic = factory.createFigure(AppFigureFactory.EXTERNAL_INTERFACE);
               OSExternalInterface externalInterface = (OSExternalInterface) uic.getData();
               externalInterface.setId(iKey);
               externalInterface.setConnectedFigures(strConnectedFigures);
               ((IPersist) uic.getData()).load(getApp(), externalInterface, config);
               add(uic);

               // for all connected figures create a connector and connect it
               boolean found = false;
               UIConnectableFigure dataset = null;
               Vector<UIConnectableFigure> vectorDatasetsCopy =
                  new Vector<>(vectorDatasets);
               for (int i=0, n=externalInterface.getConnectedFigureCount(); i<n; i++)
               {
                  int instanceId = externalInterface.getConnectedFigureInstance(i);
                    for (int j=0; j<vectorDatasetsCopy.size(); j++)
                    {
                     dataset = vectorDatasetsCopy.elementAt(j);
                     if (dataset.getId() == AppFigureFactory.DATASET_ID &&
                         ((OSDataset) dataset.getData()).getId() == instanceId)
                     {
                        vectorDatasetsCopy.remove(j);
                        found = true;
                        break;
                     }
                  }

                  if (found)
                  {
                     // create all connectors and connect them to the external
                     // interface on one sid and to its target dataset on the
                     // other side
                     UIConnector connector = (UIConnector) factory.createFigure(AppFigureFactory.DIRECTED_CONNECTION);
                     add(connector);

                     // attach connector between dataset and result page
                     UIConnectionPoint cp = uic.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
                     connector.createDynamicConnectionProgrammatic(cp, false);
                     cp = dataset.getConnectionPoint(UIConnectionPoint.CP_ID_LEFT);
                     connector.createDynamicConnectionProgrammatic(cp, true);
                  }
               }
            }
              else if (key.startsWith(OSApplicationFile.KEY_APPLICATION_FILE_KEY))
            {
               // get all elements for this external interface
               String strKey = config.getProperty(key);
               int iKey = new Integer(strKey);
               String strInternalName = config.getProperty(
                  OSApplicationFile.KEY_APPLICATION_FILE_NAME + strKey);

               // create the figure itself and set the figure ID store in the user
               // config, the call load to restore its location
                uic = factory.createFigure(AppFigureFactory.APPLICATION_FILE);
               OSApplicationFile appFile = (OSApplicationFile) uic.getData();
               appFile.setId(iKey);
               ((IPersist) uic.getData()).load(getApp(), appFile, config);
                appFile.setInternalName(strInternalName);
               add(uic);
            }
         }

         // save the current user config for loading pipes and blow the
         // applications user config away for saves
         m_loadedUserConfig = config;
         getApp().setUserProperties(new Properties());
      }
      catch (IllegalArgumentException | FigureCreationException e)
      {
         PSDlgUtil.showError(e, true, getResources().getString("OpErrorTitle"));
      } finally
      {
         E2Designer.getApp().getMainFrame().setCursor(restoreCursor);
      }
   }

   /**
    * Get the user config for loading pipes.
    *
    * @return Properties the load user configuration
    */
   public Properties getLoadUserConfig()
   {
      return m_loadedUserConfig;
   }


   /**
    *
    */
   private boolean checkExits()
   {
      Component[] comps = m_drawingPane.getComponents();
      for (int index = comps.length-1; index >= 0; index--)
      {
         Debug.assertTrue(comps[index] instanceof UIConnectableFigure,
                        getResources(), "BadUICType", null);

         UIConnectableFigure fig = (UIConnectableFigure) comps[index];

           if (fig.getId() == AppFigureFactory.DATASET_ID)
         {
             Enumeration e = fig.getConnectionPoints();
            while (e.hasMoreElements())
            {
               Object o= e.nextElement();
               if( o  instanceof  UIRigidConnectionPoint )
               {
                  UIRigidConnectionPoint cp = (UIRigidConnectionPoint)o;
                  if (cp != null)
                  {
                       UIConnectableFigure attachment = cp.getAttached();
                     if (attachment != null)
                     {
                          Object pdata = attachment.getData();
                          if (pdata instanceof OSExitCallSet )
                        {
/*
                           OSExitCallSet exit=(OSExitCallSet)pdata;
                           if(  exit.isValid() == false )
                           {
                                PSDlgUtil.showMessageDialog(
                                 E2Designer.getResources().getString("InvalidExitText"),
                                 E2Designer.getResources().getString("InvalidExitTitle"),
                                 JOptionPane.OK_OPTION );

                              return(false);
                           }
*/
                        }
                     }
                  }// cp != null
               }// if o instance of
            }//while
         }
      }
      return(true);
   }

   /**
    * This method does all the work of saving a named app. It is the worker for
    * saveApp() and saveAsApp(). If errors occur, appropriate messages are
    * displayed to the user.
    *
    * @param close true if we should also close the frame after saving
    * @param validate true if validation is enabled while saving
    * @param overwrite true if we overwrite an existing application without
    *                  confirmation from the user
    * @return boolean true if saved, false if save was canceled
    */
   private boolean save(boolean close, boolean validate, boolean overwrite)
   {
        E2Designer.getApp().getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      boolean bSaved = false;

      try
      {
         OSLoadSaveHelper.startLoggingApplication(getApp().getName());

         // first clear the app of all properties that are not managed by the 
            // frame
         // return if save was canceled
         if (!prepareAppForSave(getApp()))
            return false;

         // copy existing application files to the new location if necessary
         for (PSApplicationFile file : m_applicationFiles) {
            getObjectStore().saveApplicationFile(getApp(), file, true, true);
            OSLoadSaveHelper.logCopyApplicationFile(file.getFileName());
         }

         m_applicationFiles = new Vector<>();

         if ( !saveComponents(close))
            // editors wouldn't close possibly
            return false;

         //Make sure we process deletes from the server first
         deleteComponents();

         if ( m_needsCallConversion )
         {
            m_needsCallConversion = false;
            convertCalls();
         }

         // warn the user if an application with this name already exists
         boolean localValidate = (m_saveAsDialog == null) ? validate : m_saveAsDialog.validateApplication();

         try
         {
            /* TODOph: Ideally, we wouldn't save app files until the application was saved,
               because if the app save is not successful (e.g. the app already exists
               and the user elects not to overwrite), we could overwrite valid app files.
               By saving files after the app save, we could get rid of the hack
               of creating the app to have a place for app files, then actually saving the app.
               However, when saving the app w/o the app files when validating, certain things on
               the server won't behave correctly. This behavior needs to be corrected
               on the server. NOTE: the whole save mechanism needs to be transaction
               based. */
            getApp().saveAppFiles( getObjectStore() );

            /* Similar to the app files, this must appear first in case the
               app is being validated. */
            getApp().saveUdfs();
            getObjectStore().saveApplication(getApp(), close, localValidate, overwrite);
            m_bIsNew = false;
            bSaved = true;
         }
         catch (PSNonUniqueException e)
         {
            if (JOptionPane.YES_OPTION ==
               PSDlgUtil.showConfirmDialog(
                                getResources().getString("ReplaceApplication"),
                                getResources().getString("ConfirmOperation"),
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
            {
               try
               {
                  // overwrite existing application
                  getObjectStore().saveApplication(getApp(), close, validate, false);
                  m_bIsNew = false;
                  bSaved = true;
               }
               catch (Exception ex)
               {
                  // this should never happen
                  PSDlgUtil.showError(ex);
               }
            }
         }
      }
      catch ( PSVersionConflictException | PSServerException | PSAuthorizationException | PSNotLockedException e )
      {
         PSDlgUtil.showError(e);
      } catch (PSValidationException e)
      {
         PSDlgUtil.showError(e,
                         false,
                         getResources().getString("ExceptionTitle"));
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
      finally
      {
         try
         {
            if (close && bSaved)
            {
               Vector editors = getEditingWindows( getDrawingPane(), !close );
               closeEditingWindows( editors /*true*/);
            }
            OSLoadSaveHelper.finishLoggingApplication(getApp().getName());
            E2Designer.getApp().getMainFrame().setCursor(Cursor.getDefaultCursor());
         }
         // rather than catching the 6 possible exceptions again, use the generic approach
         catch ( Exception e )
         {
            PSDlgUtil.showError(e);
         }

      }

      return bSaved;
   }

   /**
    * Delete the files from the server associated with the components that 
    * are deleted in the UI from the server.
    *   
    * @throws PSServerException If the server is not responding.
    * @throws PSAuthorizationException If the user does not have update access
    * on the application.
    * @throws PSAuthenticationFailedException If the user couldn't be authenticated
    * on the server.
    * @throws PSNotLockedException If a lock is not currently held (the timeout
    * already expired or getApplication was not used to lock the application).
    * @throws PSValidationException If a validation error is encountered.
    */
   private void deleteComponents()
      throws
         PSServerException,
         PSAuthorizationException,
         PSAuthenticationFailedException,
         PSNotLockedException,
         PSValidationException
   {
      //Collect all UI components that are deleted from UI.
      Iterator iter = m_deletedComps.iterator();
      while (iter.hasNext())
      {
         UIConnectableFigure element = (UIConnectableFigure) iter.next();
         Object obj = element.getData();
         if(obj instanceof IPersist)
         {
            IPersist externalInterface = (IPersist)obj;
            //Mark it to delete the file associated with it from server
            externalInterface.cleanup(getApp());
         }
      }
      //Reset the component list.
      m_deletedComps.clear();
      //Process actual deleting from the server
      getApp().removeAppFiles(getObjectStore());
   }

   /**
    * Removes all objects and clears all values for properties that are not
    * directly managed by the frame window. This is usually done before a save
    * sequence is commenced to allow the sub-objects to write their data to a
    * 'clean slate'.
    *
    * @param app the app to be saved, assumed not <code>null</code>.
    * 
    * @return <code>true</code> to proceed with the save, <code>false</code>
    * to abort the save
    */
   private boolean prepareAppForSave(PSApplication app)
   {
      try
      {
         // check for unattached objects which can't be saved
         Component[] comps = m_drawingPane.getComponents();

         for (int index = comps.length - 1; index >= 0; index--)
         {
            UIConnectableFigure fig = (UIConnectableFigure) comps[index];
            if (fig.getId() == AppFigureFactory.RESULT_PAGE_ID)
            {
               // get the dataset this page is contained by
               UTAppNavigator nav = new UTAppNavigator();
               Vector datasets = nav.getSourceDatasets(fig);
               if (datasets.size() == 0)
               {
                  PSDlgUtil.showErrorDialog(
                        Util.cropErrorMessage(
                              getResources().getString("LoosingApplicationElements")),
                              getResources().getString("OpErrorTitle"));
                     return false;
               }
            }
         }
         
         PSCollection datasets = app.getDataSets();
         if (datasets != null)
         {
            // clear all request links
            for (int i = 0, m = datasets.size(); i < m; i++)
            {
               PSDataSet dataset = (PSDataSet) datasets.get(i);
               if (dataset.getOutputRequestLink() != null)
                  dataset.setOutputRequestLink(null);

               PSResultPageSet resultPageSet = dataset.getOutputResultPages();
               if (resultPageSet != null)
               {
                  PSCollection results = resultPageSet.getResultPages();
                  if (results != null)
                     results.clear();
               }
            }

            // clear dataset
            datasets.clear();
         }
         app.setDataSets(null);

         return true;
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }

      return false;
   }


   /**
    * Similar to saveApp(), except it pops up a dialog allowing the user to
    * select/enter a new name for the application. The name defaults to the
    * current name of the app.
    *
    * @param close true if we should also close the frame after saving
    * @param useExistingName indicates whether existing application name
    * should be used to save the new application. If <code>false</code> user
    * will be requested for new name.
    * @return boolean true for success, false otherwise
    */
   public boolean saveAsApp(final boolean close, final boolean useExistingName)
   {
      final boolean[] result = {false};
      try
      {
         final Runnable saveAsTask = new Runnable()
         {
            public void run()
            {
               result[0] = saveAsApp(close,
                     useExistingName ? getApp().getName() : null);
            }
         };
         getMainFrame().lockSWTFor(saveAsTask);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
      return result[0];
   }

   /**
    * Does all the work for a saveAs. It pops up a dialog for the new application
    * name if none has been provided.
    *
    * @param close true if we should also close the frame after saving
    * @strNewName the new name or null
    * @return boolean true for success, false otherwise
    */
   private boolean saveAsApp(boolean close, String strNewName)
   /*
    * Andriy: This method is a mess - a lot of crufty old code, attempt to
    * keep old logic while integrating it with new model concept.
    */
   {
      try
      {
         String oldRevision = "";
         if (getApp().getRevisionHistory() != null)
            oldRevision = getApp().getRevisionHistory().getLatestVersion();

         // create a copy of the application, since several elements need to be
         // copied over to the new application in case of a saveAs action
         OSApplication originalApp = getApp();

         // create and show the saveAs dialog
         if (strNewName == null)
         {
            m_saveAsDialog = new SaveAsDialog(getApp());
            m_saveAsDialog.setVisible(true);

            // check the return code and return if the user canceled his operation
            if (m_saveAsDialog.getCommand() == SaveAsDialog.ReturnCommand.CANCEL)
            {
               return false;
            }

            strNewName = m_saveAsDialog.getNewName();
         }

         if (!isNewApplication())
         {
            // create a new vector each time, so we get rid of history
            m_applicationFiles = new Vector<PSApplicationFile>();

            // load all existing application files, we need to save them to the new
            // location later
            for (final String file : Util.getApplicationFiles(getApp().getRequestRoot()))
            {
               final PSApplicationFile psFile = new PSApplicationFile(new File(file));
               m_applicationFiles.add(getObjectStore().loadApplicationFile(getApp(), psFile));
            }
            // the user wants to create a new application through saveAs. release
            // the application lock of the existing application.
            try
            {
               doReleaseLock();
            }
            catch (PSLockedException e)
            {
               // this is fine if we opened in read only mode
               if (!m_readOnly)
                  PSDlgUtil.showError(e);
            }
            catch (PSAuthorizationException e)
            {
               // catching Auth.Exception here to allow new Application to be created
               // this application only has "read only" rights to it, plus it is not
               // "Locked" by another application.
               if (m_readOnly)
                  m_bGrantFullAccess = true;
            }

            // create a new application
            try
            {
               
               final IPSReference oldRef =
                     getXmlApplicationEditor().getApplicationRef();
               
               //release the lock from the old object reference
               getTracker().releaseLock(oldRef, true);

               // Clone the reference with the new name
               final PSReference newRef = (PSReference)
                     getTracker().create(oldRef.getObjectType(), strNewName, null);
               getXmlApplicationEditor().setApplicationRef(newRef);
               final OSApplication newApp =
                  (OSApplication) getTracker().load(newRef, true); 
               UIMainFrame.initApplication(newApp, strNewName);
               setApp(newApp);
               m_lockTimer.restart();
            }
            catch (PSNonUniqueException e)
            {
               // delete the application if it alredy exists and the user confirms
               if (!E2Designer.getApp().getMainFrame().deleteApplication(strNewName))
               {
                  return false;
               }

               // and now create a new one
               try
               {
                  setApp(new OSApplication(getObjectStore().createApplication(strNewName).toXml()));
                  UIMainFrame.initApplication(getApp(), strNewName);
                  m_lockTimer.restart();
               }
               catch (PSNonUniqueException e1)
               {
                  // this should never happen
                  e1.printStackTrace();
               }
               catch (PSUnknownDocTypeException dte)
               {
                  // this should never happen
                  E2Designer.FatalError( dte.getLocalizedMessage() );
               }
               catch (PSUnknownNodeTypeException nte)
               {
                  // this should never happen
                  E2Designer.FatalError( nte.getLocalizedMessage() );
               }
            }
            catch (PSUnknownDocTypeException dte)
            {
               // this should never happen
               E2Designer.FatalError( dte.getLocalizedMessage() );
            }
            catch (PSUnknownNodeTypeException nte)
            {
               // this should never happen
               E2Designer.FatalError( nte.getLocalizedMessage() );
            }

            // we created a new application during a saveAs action. copy all
            // saved elements from the original application
            assignFrom( originalApp );
         }
         else
         {
            getApp().setName(strNewName);
         }

         // reset read only flag
         m_readOnly = false;

         // setting new application name into the Title bar.
         setTitle(getApp().getName());
         repaint();

         // set the application root to the application name
         getApp().setRequestRoot(getApp().getName());

         // save the new application first, so the server knows the application
         // root for saving application files
         boolean enabled = getApp().isEnabled();
         try
         {
            getAppModel().save(getApplicationRef(), false);
         }
         catch (PSNonUniqueException e)
         {
            // delete the application if it alredy exists and the user confirms
        // TODO: BUG HERE! AppFrame already changed; cancelation does not work.
            if (!E2Designer.getApp().getMainFrame().deleteApplication(strNewName))
                 return false;

            // and now create a new one
            try
            {
               setApp(new OSApplication(getObjectStore().createApplication(strNewName).toXml()));
               UIMainFrame.initApplication(getApp(), strNewName);
               m_lockTimer.restart();
               assignFrom( originalApp );
            }
            catch (PSNonUniqueException e1)
            {
               // this should never happen
               e1.printStackTrace();
            }
            catch (PSUnknownDocTypeException dte)
            {
               // this should never happen
               E2Designer.FatalError( dte.getLocalizedMessage() );
            }
            catch (PSUnknownNodeTypeException nte)
            {
               // this should never happen
               E2Designer.FatalError( nte.getLocalizedMessage() );
            }

            try
            {
               // the server validates the application automatically if the application
               // saved is enabled. therefor we need to disable the application for the
               // save to create the application root and set it back to its original
               // value after the save.
               getApp().setEnabled(false);
               getObjectStore().saveApplication(getApp(), false, false, true);
               getApp().setEnabled(enabled);

               BrowserFrame.getBrowser().addAppToAppsTab(getApp());
            }
            catch (PSVersionConflictException | PSNonUniqueException e1)
            {
               // this should never happen
               PSDlgUtil.showError(e1);
            } catch (Exception e1)
            {
               PSDlgUtil.showError(e1, false, getResources().getString("ExceptionTitle"));
               getApp().setEnabled(!getApp().isEnabled());
               String newRevision = "";
               if (getApp().getRevisionHistory() != null)
                  newRevision = getApp().getRevisionHistory().getLatestVersion();
               if (!oldRevision.equals(newRevision))
               {
                  try
                  {
                     // if the version changed, we know that saving was o.k. but starting
                     // failed. in this case we disable the application again
                     getObjectStore().saveApplication(getApp(), false, false, false);
                  }
                  catch (Exception e2)
                  {
                     PSDlgUtil.showError(e2);
                  }
               }
            }
         }
         catch (PSVersionConflictException e)
         {
            PSDlgUtil.showError(e);
         }

         return save(close, false, false);
      }
      catch (IllegalArgumentException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSValidationException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSServerException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSNotLockedException e)
      {
         PSDlgUtil.showError(e);
      }
      catch (PSAuthorizationException e)
      {
         System.out.println("  (UIAppFrame) IN OUTER AUTHORIZATION EXCEPTION!!!");
         e.printStackTrace();
         PSDlgUtil.showError(e);
      }
      catch (PSAuthenticationFailedException e)
      {
         PSDlgUtil.showError(e, false,
            getResources().getString("ExceptionTitle"));
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }

      return false;
   }

   /**
    * Convenience method to access tracker.
    */
   private PSModelTracker getTracker()
   {
      return PSModelTracker.getInstance();
   }


   /**
    * Assigns all the properties in the passed in app to the one associated with
    * this frame. Only those items that are specific to the app are copied (i.e.
    * any objects that have a UIFigure aren't copied).
    */
   private void assignFrom( OSApplication srcApp )
   {
      if ( srcApp == getApp() )
         return;
      try
      {
         getApp().setAcl(srcApp.getAcl());
         getApp().setDataEncryptor(srcApp.getDataEncryptor());
         getApp().setDescription(srcApp.getDescription());
         getApp().setLogger(srcApp.getLogger());
         getApp().setMaxRequestsInQueue(srcApp.getMaxRequestsInQueue());
         getApp().setMaxRequestTime(srcApp.getMaxRequestTime());
         getApp().setMaxThreads(srcApp.getMaxThreads());
         getApp().setRequestTypeHtmlParamName(srcApp.getRequestTypeHtmlParamName());
         getApp().setRequestTypeValueDelete(srcApp.getRequestTypeValueDelete());
         getApp().setRequestTypeValueInsert(srcApp.getRequestTypeValueInsert());
         getApp().setRequestTypeValueQuery(srcApp.getRequestTypeValueQuery());
         getApp().setRequestTypeValueUpdate(srcApp.getRequestTypeValueUpdate());
         getApp().setRoles(srcApp.getRoles());

         /* We have to manually rebuild all udfs to get the correct context
            in the reference so they can be saved unique to this app. */
         PSCollection srcUdfs = srcApp.getUdfSet().getUdfs(OSUdfConstants.UDF_APP);
         PSUdfSet tgtUdfs = getApp().getUdfSet();
         int size = srcUdfs.size();
         for ( int i = 0; i < size; ++i )
         {
            IPSExtensionDef def = (IPSExtensionDef) srcUdfs.get(i);
            PSExtensionRef ref = new PSExtensionRef(
               def.getRef().getHandlerName(), getApp().getExtensionContext(),
               def.getRef().getExtensionName());

            Properties initParams = new Properties();
            Iterator iter = def.getInitParameterNames();
            while ( iter.hasNext())
            {
               String key = (String) iter.next();
               initParams.setProperty( key, def.getInitParameter(key));
            }

            ArrayList<IPSExtensionParamDef> runtimeParams = new ArrayList<IPSExtensionParamDef>();
            iter = def.getRuntimeParameterNames();
            while ( iter.hasNext())
            {
               String key = (String) iter.next();
               runtimeParams.add( def.getRuntimeParameter( key ));
            }

            PSExtensionDef newDef = new PSExtensionDef( ref,
               def.getInterfaces(), def.getResourceLocations(),
               initParams, runtimeParams.iterator());

            tgtUdfs.addApplicationUdf( newDef );
         }
         /* set a flag so all extension calls get converted later when all
            the objects have been saved to the app */
         m_needsCallConversion = true;

         if ( true == m_bGrantFullAccess )
            grantFullDesignerAccess(getApp().getAcl());
      }
      catch ( IllegalArgumentException e )
      {
         // this shouldn't happen since we are taking all data from an existing app
         e.printStackTrace();
      }
   }



   /**
    * Changes the extension context of all calls in this application that are
    * app specific to the
    * context obtained from the associated app. This method must not be called
    * until the components have been saved to the application object as it
    * gets all the objects from getApp().
    */
   private void convertCalls()
   {
      // now change the context for all extension calls
      PSCollection datasets = getApp().getDataSets();
      if ( null == datasets )
         return;

      try
      {
         int datasetCount = datasets.size();
         String context = getApp().getExtensionContext();
         for ( int i = 0; i < datasetCount; ++i )
         {
            PSDataSet ds = (PSDataSet) datasets.get(i);
            PSPipe pipe = ds.getPipe();
            if ( pipe != null )
            {
               // convert the mapper mappings that have extensions
               PSDataMapper mapper = pipe.getDataMapper();
               if(mapper != null)
               {
                  for ( int j = 0; j < mapper.size(); j++ )
                  {
                     PSDataMapping mapping = (PSDataMapping)mapper.get( j );

                     IPSBackEndMapping backendMapping = mapping.getBackEndMapping();
                     IPSDocumentMapping docMapping = mapping.getDocumentMapping();
                     OSExtensionCall call = null;

                     if ( null != backendMapping &&
                        backendMapping instanceof OSExtensionCall )
                     {
                        call = (OSExtensionCall) backendMapping;
                     }
                     else if ( null != docMapping &&
                        docMapping instanceof OSExtensionCall )
                     {
                        call = (OSExtensionCall) docMapping;
                     }
                     if ( null != call )
                     {
                        PSExtensionRef cur = call.getExtensionRef();
                        if ( cur.getContext().startsWith(
                           OSApplication.APP_CONTEXT_PREFIX))
                        {
                           call.setExtensionRef( new PSExtensionRef( cur.getHandlerName(),
                              context, cur.getExtensionName()));
                        }
                     }
                  }
               }
            }

            // convert the Join formula
            PSBackEndDataTank beTank = pipe.getBackEndDataTank();
            if ( beTank != null )
            {
               PSCollection colJoins = beTank.getJoins();
               for ( int j = 0; j < colJoins.size(); j++ )
               {
                  PSBackEndJoin join = (PSBackEndJoin)colJoins.get( j );
                  PSExtensionCall call = join.getTranslator();
                  if ( null != call && (call instanceof OSExtensionCall ))
                  {
                     PSExtensionRef cur = call.getExtensionRef();
                     if ( cur.getContext().startsWith(
                        OSApplication.APP_CONTEXT_PREFIX))
                     {
                        call.setExtensionRef( new PSExtensionRef( cur.getHandlerName(),
                           context, cur.getExtensionName()));
                     }
                  }
               }
            }
         }
      }
      catch ( IllegalArgumentException e )
      {
         /* this shouldn't happen because we never pass null into the
            set methods */
         System.out.println( "Unexpected exception: " + e.getLocalizedMessage());
         e.printStackTrace();
      }
   }





  /**
   * Takes the input ACL and searches for the ACL entry that matches the user
   * name and grants full access. This should only be called if the
   * application was opened ReadOnly and to be saved to a different name. If
   * the username ACL was not found, it will be created with full access.
   *
   * @param acl The ACL access to be altered with to full designer access.
   */
   private void grantFullDesignerAccess( PSAcl acl )
   {
      //System.out.println("  GRANTING FULL ACCESS!!!");
      PSCollection aclEntries = acl.getEntries();

      String userName = "";
      try {
         userName = getObjectStore().getUserConfiguration().getUserName();
      }
      catch (PSAuthenticationFailedException e)
      {
         e.printStackTrace();
      }
      catch (PSAuthorizationException e)
      {
         e.printStackTrace();
      }
      catch (PSServerException e)
      {
         e.printStackTrace();
      }

      PSAclEntry userAcl = null;
      for (int i = 0; i < aclEntries.size(); i++)
      {
         userAcl = (PSAclEntry)aclEntries.get(i);
         if (userAcl.getName().equals(userName))
         {
            //System.out.println("  USER NAME FOUND: "+ userAcl.getName());

            userAcl.setAccessLevel( PSAclEntry.AACE_DATA_CREATE |
                                    PSAclEntry.AACE_DATA_DELETE |
                                    PSAclEntry.AACE_DATA_QUERY |
                                    PSAclEntry.AACE_DATA_UPDATE |
                                    PSAclEntry.AACE_DESIGN_READ |
                                    PSAclEntry.AACE_DESIGN_UPDATE |
                                    PSAclEntry.AACE_DESIGN_DELETE |
                                    PSAclEntry.AACE_DESIGN_MODIFY_ACL );
            return;
         }
      }

      // code will get here if no ACL matching the username has been found;
      // creating an username ACL with full access.
      userAcl = new PSAclEntry( userName, PSAclEntry.ACE_TYPE_USER );
      userAcl.setAccessLevel( PSAclEntry.AACE_DATA_CREATE |
                              PSAclEntry.AACE_DATA_DELETE |
                              PSAclEntry.AACE_DATA_QUERY |
                              PSAclEntry.AACE_DATA_UPDATE |
                              PSAclEntry.AACE_DESIGN_READ |
                              PSAclEntry.AACE_DESIGN_UPDATE |
                              PSAclEntry.AACE_DESIGN_DELETE |
                              PSAclEntry.AACE_DESIGN_MODIFY_ACL );
      aclEntries.add(userAcl);
      acl.setEntries(aclEntries);
      return;
   }


   /**
    * Action that creates a new Application Properties Dialog.
    */
   public void viewAppProperties( )
   {
      try
      {
         new AppPropDialog(this, getApp());
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Refreshes all child editing windows with new name
    * This is called when application name has changed via properties
    *
    * @param newName name to update, must not be <code>null</code> or empty
    */
   public void refreshEditingWindowsTitle(String newName)
   {
      if(newName == null || newName.length() == 0)
         throw new IllegalArgumentException(
            "New Application Name can not be null or empty");

      // Get all child editing windows
      Vector editors = getEditingWindows(getDrawingPane(), false);

      if(editors != null && !editors.isEmpty())
      {
         for(int i = 0; i < editors.size(); i++)
         {
             String titleStr = ((IEditor) editors.get(i)).getTitle();
             int index;
             if((index = titleStr.indexOf("(")) != -1)
              ((IEditor) editors.get(i)).setTitle(titleStr.substring(0,index) +
                                                   "(" + newName + ")");
             else
               ((IEditor) editors.get(i)).setTitle(newName);
         }
      }
   }


   /**
    *
    */
   @Override
   public void setSelected(boolean bSelected) throws PropertyVetoException
   {
      super.setSelected( bSelected );
   }


  /*
   * Indicate that this application was new created, not loaded.
   */
   public void setNewApplication()
   {
      m_bIsNew = true;
   }


  /*
   * Returns wether or not this application was created new.
   *
   * @return boolean true if new created, false otherwise
   */
   public boolean isNewApplication()
   {
      return m_bIsNew;
   }

   /**
   * This function returns a PSCollection of the datasets in this application
   */
   public PSCollection getDatasets()
   {
      try
      {
         PSCollection datasets = new PSCollection("com.percussion.design.objectstore.PSDataSet");
         Component[] comps = m_drawingPane.getComponents();

         if(comps != null)
         {
            for (int index = comps.length-1; index >= 0; index--)
            {
               if(comps[index] instanceof UIConnectableFigure)
               {
                  UIConnectableFigure fig = (UIConnectableFigure) comps[index];
                  if (fig.getId() == AppFigureFactory.DATASET_ID ||
                     fig.getId() == AppFigureFactory.BINARY_RESOURCE_ID)
                  {
                     if(fig.getData() != null)
                     {
                        datasets.add(fig.getData());
                     }
                  }
               }
            }
         }

         return(datasets);
      }
      catch(ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      return(null);
   }


   /**
   * Renews the lock for the application every LOCK_EXTEND_MINS minutes
   */
   public void extendApplicationLock()
   {
      if(getApp() != null)
      {
         if(!m_readOnly && !isNewApplication())
         {
            try
            {
               //System.out.println("....Extending application lock for application "+getApp().getName()+" for "+LOCK_EXTEND_MINS+" minutes");
               getObjectStore().extendApplicationLock(getApp(), LOCK_EXTEND_MINS);
            }
            catch(PSServerException e)
            {
               PSDlgUtil.showError(e);
            }
            catch(PSAuthorizationException e)
            {
               PSDlgUtil.showError(e);
            }
          catch (PSAuthenticationFailedException e)
          {
             PSDlgUtil.showError(e, false, getResources().getString("ExceptionTitle"));
          }
            catch(PSLockedException e)
            {
               PSDlgUtil.showError(e);
            }
         }
      }
   }


   /**
    * Saves the objects for the application. This method is called from two
    * locations.
    * 1. From the save  method in which case the param close is passed in as is.
    * 2. From menu listener for MENU_APP_EXPORT in which case called with close
    * = false.
    *
    * @param close If true, will close the pipe frame windows if open.
    *
    * @return <code>true</code> If all open editors will close (if close is
    * <code>true</code>) and if the save from the editors to the component was
    * successful.
    */
   @SuppressWarnings("deprecation")
   private boolean saveComponents(boolean close)
   {
      try
      {
         // always start with an empty config
         Properties config = new Properties();
         getApp().setUserProperties(config);

         // save contents from right application pane

         Vector editors = getEditingWindows( getDrawingPane(), !close );
         // ask all windows if they can close before trying to save any
         //   This is recursive, so only need to call here.
         if ( close && !canCloseEditingWindows( editors ))
            return false;
         // tell any open editors to save themselves
         //   This is recursive, so only need to call here.
         if ( !saveEditingWindows( editors, close ))
            return false;

         Vector<UIConnectableFigure> externalInterfaces = new Vector<UIConnectableFigure>();
         Component[] comps = m_drawingPane.getComponents();
         for (int index = comps.length-1; index >= 0; index--)
         {
            //System.out.println("Saving object " + Integer.toString(index));
            Debug.assertTrue(comps[index] instanceof UIConnectableFigure,
                  getResources(), "BadUICType", null);

            Object data = null;
            UIConnectableFigure fig = (UIConnectableFigure) comps[index];

              // take special care for datasets and binary resources
            if (fig.getId() == AppFigureFactory.DATASET_ID ||
                fig.getId() == AppFigureFactory.BINARY_RESOURCE_ID)
            {
               data = fig.getData();

               // save dataset pipe first, if editor is open and close it if need
               if (fig.getId() == AppFigureFactory.DATASET_ID)
               {
                   Enumeration e = fig.getConnectionPoints();
                  while (e.hasMoreElements())
                  {
                     Object o= e.nextElement();
                     if( o  instanceof  UIRigidConnectionPoint )
                     {
                        UIRigidConnectionPoint cp = (UIRigidConnectionPoint)o;
                        if (cp != null)
                        {
                             UIConnectableFigure attachment = cp.getAttached();
                           if (attachment != null)
                           {
                                Object pdata = attachment.getData();
                                if (pdata instanceof OSExitCallSet )
                              {
                                 OSExitCallSet exit=(OSExitCallSet)pdata;
                                 exit.setFigure(attachment);
                                 exit.save(getApp(), data, config);
                              }
                           }
                           else
                           {
                              /* Basically dataset should not have any reference
                               * to the exits except in save process. As save
                               * process is not resetting the state of dataset
                               * after it is done, we always make sure that if
                               * there is an attached figure replace the exits
                               * of the resource with the exits defined in
                               * attached figures, otherwise null.
                               */
                              PSDataSet dataset = (PSDataSet)data;
                              PSPipe pipe = dataset.getPipe();

                              if(pipe != null)
                              {
                                 if(cp.getName().equals(
                                    AppFigureFactory.PRE_PROCESS_CP ))
                                 {
                                    pipe.setInputDataExtensions(null);
                                 }
                                 else if(cp.getName().equals(
                                    AppFigureFactory.POST_PROCESS_CP ))
                                 {
                                    pipe.setResultDataExtensions(null);
                                 }
                              }
                           }
                        }
                     }
                  }//while
               }

               // call save() method in OSDataset, plus its extension classes,
               // based on IPersist interface; OSBinaryDataset will save here as
               // well.
               if (data != null)
                  ((IPersist) data).save(getApp(), getApp(), config);

               // save the requestor
               /* The data in the requestor is a fake data object. Its presence is
                  only used to allow the editing mechanism to work in the current
                  model. The dataset contains the real requestor. */

               // save dataset request link
               UIConnectionPoint cp = fig.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
               if (cp != null && data instanceof OSDataset)
               {
                  for (int i=0; i<cp.getAttachedFigureCount(); i++)
                  {
                     UIConnectableFigure connector = cp.getAttached(i);
                     if (connector != null &&
                         connector.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID)
                     {
                        Enumeration e = connector.getDynamicConnections();
                        while (e.hasMoreElements())
                        {
                           UIConnectionPoint cp1 = (UIConnectionPoint) e.nextElement();
                           UIConnectableFigure fig1 = cp1.getOwner();
                           if (fig1 != null)
                           {
                              if ((fig1.getId() == AppFigureFactory.DATASET_ID ||
                                   fig1.getId() == AppFigureFactory.BINARY_RESOURCE_ID) &&
                                   fig1.getData() != data)
                              {
                                 OSDataset targetDataset = (OSDataset) fig1.getData();
                                 if (connector.getData() != null)
                                 {
                                    OSDataset dataset = (OSDataset) data;
                                    OSRequestLinkSet requestLinkSet = (OSRequestLinkSet) connector.getData();
                                    requestLinkSet.setTargetDataset(targetDataset.getName());
                                    PSCollection requestLinks = null;
                                    try
                                    {
                                       requestLinks = new PSCollection("com.percussion.design.objectstore.PSRequestLink");
                                    }
                                    catch(ClassNotFoundException ee)
                                    {
                                       ee.printStackTrace();
                                    }
                                    ((IPersist) requestLinkSet).save(getApp(), requestLinks, config);
                                    PSRequestLink requestLink = (PSRequestLink)requestLinks.get(0); // only one request link in this case
                                    dataset.setOutputRequestLink(requestLink);
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } // end saving dataset objects

            // take special care for result pages
            else if (fig.getId() == AppFigureFactory.RESULT_PAGE_ID)
            {
               // get all datasets this page is contained by
               UTAppNavigator nav = new UTAppNavigator();
               Vector datasets = nav.getSourceDatasets(fig);
               for (int i=0; i<datasets.size(); i++)
               {
                  UIConnectableFigure datasetFig = (UIConnectableFigure) datasets.elementAt(i);
                  if (datasetFig != null)
                  {
                     OSDataset dataset = (OSDataset) datasetFig.getData();
                     OSResultPage resultPage = (OSResultPage) fig.getData();
                     if (resultPage != null)
                     {
                        // save request links first
                        PSCollection requestLinks = resultPage.getRequestLinks();
                        if (requestLinks == null)
                        {
                           try
                           {
                              requestLinks = new PSCollection("com.percussion.design.objectstore.PSRequestLink");
                           }
                           catch(ClassNotFoundException e)
                           {
                              e.printStackTrace();
                           }
                        }
                        else
                           requestLinks.clear();

                        // get all connectors leaving the result page
                        UIConnectionPoint cp = fig.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
                        if (cp != null)
                        {
                           for (int j=0; j<cp.getAttachedFigureCount(); j++)
                           {
                              UIConnectableFigure connector = cp.getAttached(j);
                              if (connector != null &&
                                  connector.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID)
                              {
                                 Enumeration e = connector.getDynamicConnections();
                                 while (e.hasMoreElements())
                                 {
                                    UIConnectionPoint cp1 = (UIConnectionPoint) e.nextElement();
                                    UIConnectableFigure fig1 = cp1.getOwner();
                                    if (fig1 != null)
                                    {
                                       if (fig1.getId() == AppFigureFactory.DATASET_ID)
                                       {
                                          OSDataset targetDataset = (OSDataset) fig1.getData();
                                          if (connector.getData() != null &&
                                              targetDataset.getId() != dataset.getId())
                                          {
                                             OSRequestLinkSet requestLinkSet = (OSRequestLinkSet) connector.getData();
                                             requestLinkSet.setTargetDataset(targetDataset.getName());
                                             ( requestLinkSet).save(getApp(), requestLinks, config);
                                             break;
                                          }
                                       }
                                       else if (fig1.getId() == AppFigureFactory.BINARY_RESOURCE_ID)
                                       {
                                          OSBinaryDataset targetDataset = (OSBinaryDataset) fig1.getData();
                                          if (connector.getData() != null &&
                                              targetDataset.getId() != dataset.getId())
                                          {
                                             OSRequestLinkSet requestLinkSet = (OSRequestLinkSet) connector.getData();
                                             requestLinkSet.setTargetDataset(targetDataset.getName());
                                             ( requestLinkSet).save(getApp(), requestLinks, config);
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        resultPage.setRequestLinks(requestLinks);

                        // then save the result page itself
                        ((IPersist) resultPage).save(getApp(), dataset, config);
                     }
                  }
               }
            } // end saving result pages

            // take special care for external interfaces
            else if (fig.getId() == AppFigureFactory.EXTERNAL_INTERFACE_ID)
            {
               // because we need the latest instance ID's of all connected figures,
               // just remember the external interfaces for now. we will save them
               // later after we saved all other figures.
               externalInterfaces.addElement(fig);
            }
            // save all other objects but connectors. the connectors data have already
            // been saved in th dataset clause
            else if (fig.getId() != AppFigureFactory.DIRECTED_CONNECTION_ID)
            {
               data = fig.getData();
               if (data != null)
                  ((IPersist) data).save(getApp(), getApp(), config);
            }
         }

         // now is the time to save the external interfaces and the references to
         // all connected figures
         for (int index=0, n=externalInterfaces.size(); index<n; index++)
         {
            UIConnectableFigure fig = externalInterfaces.elementAt(index);
            OSExternalInterface externalInterface = (OSExternalInterface) fig.getData();
            externalInterface.clearConnectedFigures();

            UIConnectionPoint cp = fig.getConnectionPoint(UIConnectionPoint.CP_ID_RIGHT);
            if (cp != null)
            {
               for (int i=0; i<cp.getAttachedFigureCount(); i++)
               {
                  UIConnectableFigure connector = cp.getAttached(i);
                  if (connector != null &&
                      connector.getId() == AppFigureFactory.DIRECTED_CONNECTION_ID)
                  {
                     Enumeration e = connector.getDynamicConnections();
                     while (e.hasMoreElements())
                     {
                        UIConnectionPoint cp1 = (UIConnectionPoint) e.nextElement();
                        UIConnectableFigure fig1 = cp1.getOwner();
                        if (fig1 != null && fig1.getId() == AppFigureFactory.DATASET_ID)
                        {
                           OSDataset targetDataset = (OSDataset) fig1.getData();
                           externalInterface.addConnectedFigure(fig1.getId(),
                                                         targetDataset.getId());
                        }
                     }
                  }
               }
            }
            Object data = fig.getData();
            if (data != null)
               ((IPersist) data).save(getApp(), getApp(), config);
         }

         // add key to show that E2Designer saved this application
         config.setProperty(SAVED_FROM_E2, "Yes");
      }
      catch (IllegalArgumentException e)
      {
         PSDlgUtil.showError(e);
      }
      return true;
   }


   /**
    * Sets the name of the associated application and updates other properties
    * that are dependent on the name.
    *
    * @param name The new name of the application.
   **/
   public void setApplicationName( String name )
   {
      try
      {
         if ( null == name || 0 == name.trim().length() || name.length() > 50 )
            throw new IllegalArgumentException( "Invalid name parameter." );

         boolean bSame = getApp().getName().equals( getApp().getRequestRoot());
         getApp().setName( name );
         if ( bSame )
            getApp().setRequestRoot( name );
         setTitle( name );
      }
      catch ( IllegalArgumentException e )
      {
         // shouldn't happen, checked all failure cases ahead of time
         e.printStackTrace();
      }
   }


   /**
   * Creates and returns a vector that contains PSCollection objects. Each
   * PSCollection object contains collection of PSRequestLink objects that
   * have the same target dataset.
   *
   *@param cLinks - PSCollection of PSRequestLink objects that may have different
   * target datasets.
   *
   */
   private Vector createRequestLinkGroups(PSCollection cLinks)
   {
      Vector<String> vTargetDatasets = new Vector<String>();

      for(int i=0; i<cLinks.size(); i++)
      {
         PSRequestLink link = (PSRequestLink)cLinks.get(i);
         String strDataset = link.getTargetDataSet();
         boolean bAlreadyExists = false;
         for(int j=0; j<vTargetDatasets.size(); j++)
         {
            String strTargetDataset = vTargetDatasets.get(j);
            if(strDataset.equals(strTargetDataset))
            {
               bAlreadyExists = true;
               break;
            }
         }
         if(!bAlreadyExists)
            vTargetDatasets.add(strDataset);
      }

      Vector<PSCollection> vLinkGroups = new Vector<PSCollection>();
      for(int i=0; i<vTargetDatasets.size(); i++)
      {
         String strTargetDataset = vTargetDatasets.get(i);
         PSCollection c = null;
         try
         {
            c = new PSCollection("com.percussion.design.objectstore.PSRequestLink");
         }
         catch(ClassNotFoundException e)
         {
          e.printStackTrace();
         }

         for(int j=0; j<cLinks.size(); j++)
         {
            PSRequestLink link = (PSRequestLink)cLinks.get(j);
            String strDataset = link.getTargetDataSet();
            if(strDataset.equals(strTargetDataset))
            {
               c.add(link);
            }
         }
         vLinkGroups.add(c);
      }

      return vLinkGroups;
   }


   /**
    * Converts certain PS... objects to their equivalent OS... objects. If an
    * object is already of the correct type, it will not be modified. The
    * supplied app is modified directly by replacing the PS... object w/
    * it's new OS... object in the application.
    *
    * @param app The application containing objects to be converted.
    *
    * @param store A valid object store, used to get additional information
    * needed for conversion.
    */
   private void convertObjects( OSApplication app, PSObjectStore store )
   {
      if ( null == app )
         throw new IllegalArgumentException( "application can't be null" );

      if ( null == store )
         throw new IllegalArgumentException( "object store can't be null" );

      /* convert PSExtensionCall to OSExtensionCall. In the 1.1 extension model,
         the extension call had an extension def stored with it. This was
         removed in the 2.0 model and replaced w/ just a reference, which
         uniquely identified the extension. However, the GUI needs the def
         in many places. Rather than having to look up the def in all of the
         individual places, we add it to the call object here, when the app
         is loaded. If new calls are made, the def is available because it
         comes over in a catalog. */

      PSCollection datasets = app.getDataSets();
      if ( null == datasets )
         return;

      int datasetCount = datasets.size();
      for ( int i = 0; i < datasetCount; ++i )
      {
         PSDataSet ds = (PSDataSet) datasets.get(i);
         PSPipe pipe = ds.getPipe();
         if ( pipe != null )
         {
            // convert the mapper mappings that have extensions
            PSDataMapper mapper = pipe.getDataMapper();
            if(mapper != null)
            {
               for ( int j = 0; j < mapper.size(); j++ )
               {
               try
               {
                  PSDataMapping mapping = (PSDataMapping)mapper.get( j );

                  IPSBackEndMapping backendMapping = mapping.getBackEndMapping();
                  IPSDocumentMapping docMapping = mapping.getDocumentMapping();

                  if ( null != backendMapping &&
                     backendMapping instanceof PSExtensionCall )
                  {
                     mapping.setBackEndMapping( convertExtensionCall( app,
                        (PSExtensionCall) backendMapping, store ));
                  }

                  if ( null != docMapping &&
                     docMapping instanceof PSExtensionCall )
                  {
                     mapping.setDocumentMapping( convertExtensionCall( app,
                        (PSExtensionCall) docMapping, store ));
                  }
               }
               catch ( IllegalArgumentException e )
               {
                  /* this shouldn't happen because we never pass null into the
                  set methods */
                  System.out.println( "Unexpected exception: " + e.getLocalizedMessage());
                  e.printStackTrace();
               }
            }
            }
            // convert the Join formula
            PSBackEndDataTank beTank = pipe.getBackEndDataTank();
            if ( beTank != null )
            {
               PSCollection colJoins = beTank.getJoins();
               for ( int j = 0; j < colJoins.size(); j++ )
               {
                  PSBackEndJoin join = (PSBackEndJoin)colJoins.get( j );
                  PSExtensionCall call = join.getTranslator();
                  if ( null != call && !(call instanceof OSExtensionCall ))
                     join.setTranslator( convertExtensionCall( app, call, store ));
               }
            }

            // convert the input exits
            PSExtensionCallSet inputExits = pipe.getInputDataExtensions();
            if ( null != inputExits )
            {
               int exitCount = inputExits.size();
               PSExtensionCallSet newSet = new PSExtensionCallSet();
               for ( int j = 0; j < exitCount; ++j )
               {
                  PSExtensionCall call = (PSExtensionCall) inputExits.get(j);
                  OSExtensionCall newCall;
                  if ( null != call && !( call instanceof OSExtensionCall ))
                     newCall = convertExtensionCall( app, call, store );
                  else
                     newCall = (OSExtensionCall)call;
                  newSet.add( newCall );
               }
               if ( exitCount > 0 )
                  pipe.setInputDataExtensions( newSet );
            }

            // convert the result doc exits
            PSExtensionCallSet resultExits = pipe.getResultDataExtensions();
            if ( null != resultExits )
            {
               int exitCount = resultExits.size();
               PSExtensionCallSet newSet = new PSExtensionCallSet();
               for ( int j = 0; j < exitCount; ++j )
               {
                  PSExtensionCall call = (PSExtensionCall) resultExits.get(j);
                  OSExtensionCall newCall;
                  if ( null != call && !( call instanceof OSExtensionCall ))
                     newCall = convertExtensionCall( app, call, store );
                  else
                     newCall = (OSExtensionCall)call;
                  newSet.add( newCall );
               }
               if ( exitCount > 0 )
                  pipe.setResultDataExtensions( newSet );
            }
         }
      }
   }
   
   // see base class
   @Override
   protected JPopupMenu createEditPopup(MouseEvent e)
   {
      JPopupMenu popupMenu = super.createEditPopup(e);
      if (!isClickedOnFigure())
      {
         if (popupMenu == null)
         {
            popupMenu = new JPopupMenu();
         }
         popupMenu.add(new NotifierPropertiesAction());
         popupMenu.add(new SecurityAction());
      }
      return popupMenu;
   }

   /**
    * Converts the supplied call to an OSExtensionCall, adding additional
    * information needed by the latter. If the supplied call is already
    * of the correct type, it is returned w/o modification. The conversion
    * is done by creating a new object. If any exceptions occur, the user is
    * notified and an object is returned that has a <code>null</code> def.
    *
    * @param app A valid application that contains the call being converted.
    * If <code>null</code>, an exception is thrown.
    *
    * @param call The extension call to be converted. May be <code>null</code>.
    *
    * @param store A valid object store that is used to load the extension
    * def needed for the call.
    *
    * @return The converted call. If <code>null</code> is passed in, it is
    * returned.
    */
   private OSExtensionCall convertExtensionCall( OSApplication app,
      PSExtensionCall call, PSObjectStore store )
   {
      if ( null == app || null == store )
         throw new IllegalArgumentException(
            "application and object store can't be null" );

      if ( null == call || call instanceof OSExtensionCall )
         return (OSExtensionCall) call;

      IPSExtensionDef def = null;
      String errMsg = null;
      Exception excp = null;

      try
      {
         PSExtensionRef ref = call.getExtensionRef();
         String context = ref.getContext();
         if ( context.startsWith( OSApplication.APP_CONTEXT_PREFIX ))
            def = app.getUdf( ref );
         else
            def = store.loadExtensionDef( ref );
      }
      catch ( PSNotFoundException e )
      {
         errMsg = "CouldNotFindExtDef";
         excp = e;
      }
      catch ( Exception e )
      {
         /* many different exceptions are thrown by the loadExtensionDef call.
            The plan is to notify the user and not allow editing of any
            call whose extension can't be found. */
         errMsg = "ExtDefLoadFailed";
         excp = e;
      }
      if ( null != errMsg )
      {
         String msg = MessageFormat.format(
            getResources().getString( errMsg ),
            new Object[] { call.getExtensionRef().toString(), excp.getMessage() });
         PSDlgUtil.showErrorDialog(msg, getResources().getString("OpErrorTitle"));
      }

      OSExtensionCall newCall = new OSExtensionCall(call, def);
      return newCall;
   }

   /**
    * Override base class implementation to add the removed component to a 
    * list to do actual deletion later.
    * @param uic UI component just removed from the frame, 
    * never <code>null</code>.
    * 
    * @see #deleteComponents()
    */
   @Override
   public void componentRemoved(UIConnectableFigure uic)
   {
      if(uic==null)
         throw new IllegalArgumentException("uic must not be null");

      m_deletedComps.add(uic); 
   }
   
   /**
    * Current main frame.
    */
   private UIMainFrame getMainFrame()
   {
      return E2Designer.getApp().getMainFrame();
   }

   public OSApplication getApp()
   {
      return getXmlApplicationEditor().getApplication();
   }

   private void setApp(OSApplication app)
   {
      getXmlApplicationEditor().setApplication(app);
   }
   
   private class NotifierPropertiesAction extends AbstractAction
   {
      public NotifierPropertiesAction()
      {
         super(E2Designer.getResources()
               .getString("NotifierPropertiesDialogTitle"));
      }
      

      public void actionPerformed(@SuppressWarnings("unused") ActionEvent event)
      {
         try
         {
            final UIConnectableFigure figure =
               getFigureFactory().createFigure(AppFigureFactory.NOTIFIER);
            ((OSNotifier) figure.getData()).load(getApp(), getApp(), new Properties());
            final boolean changed = figure.onEdit(getApp());
            if (changed)
            {
               ((OSNotifier) figure.getData()).save(getApp(), getApp(), new Properties());
            }
         }
         catch (FigureCreationException e)
         {
            PSDlgUtil.showError(e);
         }
      }
   }
   
   private class SecurityAction extends AbstractAction
   {
      public SecurityAction()
      {
         super(E2Designer.getResources().getString("SecurityDialogTitle"));
      }

      public void actionPerformed(@SuppressWarnings("unused") ActionEvent event)
      {
         try
         {
            final UIConnectableFigure figure =
               getFigureFactory().createFigure(AppFigureFactory.SECURITY);
            figure.onEdit(getApp());
         }
         catch (FigureCreationException e)
         {
            PSDlgUtil.showError(e);
         }
      }
      
   }
   
   /**
    * The name of the factory that creates the figures used in this window.
    */
   private static final String sFIGURE_FACTORY =
      "com.percussion.E2Designer.AppFigureFactory";
   /**
    * The UserProperty name to identify that this application was created from
    * Rhythmyx Workbench.
    */
   public static final String SAVED_FROM_E2 = "savedFromWorkbench";
  /**
   * The UserProperty name to identify that this application was created from
   * Rhythmyx Workbench.
   */
   public static final String SAVED_FROM_DESIGNER = "savedFromE2Designer";

   // variables
   private Properties m_loadedUserConfig;
   private IAction[] m_ActionList;


   private SaveAsDialog  m_saveAsDialog;

   private Timer m_timer;
   private ActionListener m_timerListener;

   // related to extending the application lock
   private Timer m_lockTimer;
   // the minutes the lock is to be extended for
   // must be less than the 30 mins that the server grants the lock for
   private static final int LOCK_EXTEND_MINS = 25;


  // for ReadOnly application only. Can be set to grant full designer access
  // when saving ReadOnly app to a new application.
   private boolean m_bGrantFullAccess;
  // indicator whether or not this application is opened in read only mode.
  // if this is the case, we popup the saveAs dialog and add (ReadOnly) to the
  // application name in the app frame
   private boolean m_readOnly;
  // flag to indicate this application was created new. this will be set from
  // UIMainFrame.actionNewApplication only. its reset while saving.,
   private boolean m_bIsNew;
  // storage for application files in case of a saveAs oif an existing application
   private Vector<PSApplicationFile> m_applicationFiles = new Vector<PSApplicationFile>();

   /**
    * A flag to indicate that the context of extension calls in the app need
    * to be fixed up.
    */
   private boolean m_needsCallConversion;

   /**
    * List of the UI components deleted. An existing component when deleted 
    * is be added to this list so that any post processing around these 
    * components can be done later. Never <code>null</code>, may be empty.
    */
   private List<UIConnectableFigure> m_deletedComps =
         new ArrayList<UIConnectableFigure>();
}
