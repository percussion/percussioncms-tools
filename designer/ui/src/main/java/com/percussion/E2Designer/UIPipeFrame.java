/******************************************************************************
 *
 * [ UIPipeFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSynchronizer;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSUpdateColumn;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditorActionBarContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Vector;

/**
 * This class extends the basic editor to handle editing pipes.
 */
public class UIPipeFrame extends UIFigureFrame implements IEditor, PSWidgetCloseStrategy
{
   // constructors
   /**
    * We must have a default constructor so we can be instantiated from our
    * class name.
    */
   public UIPipeFrame(final PSXmlApplicationEditor xmlApplicationEditor)
   {
      // the name will really get set when onEdit is called
      super("Dummy", new Dimension(FRAME_SIZE), xmlApplicationEditor);

      this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      m_timerListener = new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (e.getSource() == m_timer)
               m_timer.stop();
         }
      };
      m_timer = new Timer(2000, m_timerListener);

      m_vetoableListener = new VetoableChangeListener()
      {
         public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException
         {

            if (event.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY))
            {
               if (onClose())
                  closeWindow();
               else
               {
                  throw new PropertyVetoException("Canceling Close", event);
               }
            }
         }
      };
      this.addVetoableChangeListener(m_vetoableListener);

      try
      {
         m_actions = createInsertActions();

         // add custom drop actions
         addActionListener(new DroppedBackendDatatank());
         addActionListener(new DroppedPageDatatank());
      }
      catch (MissingResourceException e)
      {
         Object[] astrParams =
         {
            e.getLocalizedMessage(),
            e.getKey()
         };
         E2Designer.FatalError(MessageFormat.format(
            E2Designer.getResources().getString("MissingResourceExceptionFormat"),
            astrParams ));
      }
   }
   
   
   /**
    * This implementation returns <code>null</code> because pipe frame
    * does not have any actions in the menu.
    * @see com.percussion.E2Designer.UIFigureFrame#createEditPopup(java.awt.event.MouseEvent)
    */
   @Override
   protected JPopupMenu createEditPopup(MouseEvent e)
   {
      m_thePopupMouseEvent = e;
      UIConnectableFigure uic = getConnectable(e.getPoint());
      final Point point =
            SwingUtilities.convertPoint(m_glassPane, e.getPoint(), uic);
      return isClickedOnFigure()
            && uic.isHit(point) ? super.createEditPopup(e) : null; 
   }

   /**
    * Creates actions for "Insert" menu.
    */
   @SuppressWarnings("deprecation")
   private Vector<IAction> createInsertActions()
   {
      final Vector<IAction> v = new Vector<IAction>(10);
      final Enumeration eFigures =
            getFigureFactory().getFigureTypes(PipeFigureFactory.INSERTABLE);
      while (eFigures.hasMoreElements())
      {
         final String strFigureName = (String) eFigures.nextElement();
         final String id = getInsertIdFromFigureName(strFigureName);
         final IAction action = new Action(
               getActionName(id),
               ResourceHelper.getIcon2(getResources(), id))
               {
                  @Override
                  public void run()
                  {
                     add(strFigureName);
                  }
               };
         action.setId(id);
         assert ResourceHelper.getAccelKey(getResources(), id) == null
               : "Not implemented yet (Andriy)";
         action.setToolTipText(
               ResourceHelper.getToolTipText(getResources(), id));
         v.add(action);
      }
      return v;
   }

   /**
    * Closes the window without saving data.
    */
   @Override
   public void closeWindow()
   {
      if (isClosed())
         return;

      if (m_vetoableListener != null)
      {
         removeVetoableChangeListener(m_vetoableListener);
         m_vetoableListener = null;
      }

      if (m_timer != null)
      {
         m_timer.stop();
         m_timer.removeActionListener(m_timerListener);
         m_timer = null;
         m_timerListener = null;
      }

      super.closeWindow();
   }

   // Properties
   @Override
   public boolean hasPersistableData( )
   {
      return false;
   }

   @Override
   public Object getData()
   {
      return m_app;
   }

   public OSDataset getDataset()
   {
      return m_dataset;
   }

   /**
   * Override this so that if the user is dragging, we always check if the
   * pipe will accept the connection, even if they aren't over the pipe.
   */
   @Override
   public boolean isOverRigidConnection(Point pt)
   {
      if( null == m_currentInfo )
         return super.isOverRigidConnection(pt);

      // we're in a drag situation
      UIConnectableFigure uic = getPipe();

      return (null != uic && uic.willAcceptConnect( m_currentInfo.getID().getID()));
   }

   @Override
   public UIConnectableFigure getConnectable(Point pt)
   {
      if ( null == m_currentInfo )
         return super.getConnectable(pt);

      // we're in a drag situation
      return ( getPipe());
   }


   /**
    * A quick way to get the pipe figure.
    */
   UIConnectableFigure getPipe()
   {
      Component[] comps = getDrawingPane().getComponents();
      if(comps != null)
      {
         for(int iComp = 0; iComp < comps.length; ++ iComp)
         {
            if(comps[iComp] instanceof UIConnectableFigure)
            {
               UIConnectableFigure fig = (UIConnectableFigure)comps[iComp];
               if(fig.getId() == PipeFigureFactory.PIPE_ID)
               {
                  return(fig);
               }
            }
         }
      }

      return(null);
   }


   /**
    * Returns <code>true</code> if the menu id is for the insert menu.
    */
   @Override
   public boolean hasActionItems(String strMenuId)
   {
      if (strMenuId.equals(PSXmlApplicationEditorActionBarContributor.MENU_INSERT))
         return(true);
      else
         return super.hasActionItems(strMenuId);
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
         return m_actions.toArray(new IAction[0]);
      }
      else
         return super.getActionItems(strMenuId);
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
         // warn user, that he will loose all unattached objects if applicable
         Component[] comps = this.getDrawingPane().getComponents();
         for (int i=0; i<comps.length; i++)
         {
            Debug.assertTrue(comps[i] instanceof UIConnectableFigure,
            E2Designer.getResources(), "BadUICType", null);

            int pipeCounter = 0;
            UIConnectableFigure fig = (UIConnectableFigure) comps[i];
            if (fig.getData() instanceof PSPipe)
               pipeCounter++;

            if ((!fig.isAttached() && (!(fig.getData() instanceof PSPipe))) || (pipeCounter > 1))
            {
               int option = PSDlgUtil.showConfirmDialog(
                     E2Designer.getResources().getString("LoosingUnattachedObjects"),
                     E2Designer.getResources().getString("ConfirmOperation"),
                     JOptionPane.OK_CANCEL_OPTION,
                     JOptionPane.WARNING_MESSAGE);
               if (option == JOptionPane.CANCEL_OPTION)
               {
                  m_timer.start();
                  return false;
               }

               break;
            }
         }

         // in case the PipeFrame is empty (user deleted everything), create a
         // new pipe that is the same type as the previous pipe and continue.
         if (0 == comps.length)
         {
            // *NOTE* This may cause some implementation problems... this call
            // ( m_pipeCopy.getType() + "Pipe" ) is exactly the same as
            // ( PipeFigureFactory.QUERY_PIPE ) or ( PipeFigureFactory.UPDATE_PIPE )
            // we should make the pipe naming scheme between the Data Objects and
            // the figure factory GUI objects consistent.
            UIConnectableFigure pipeFig = this.add( m_pipeCopy.getType() + "Pipe" );
            IOSPipe pipe = (IOSPipe)pipeFig.getData();
            pipe.setDatasetName( m_dataset.getName() );
         }

         boolean bReturn = saveData(Boolean.TRUE);
         if (!bReturn)
         {
            m_timer.start();
         }
         else
         {
            closeWindow();
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
    * Saves the pipe and its attached objects into the app. Unattached objects
    * are not saved. File attachments are only saved when the window is closing.
    *
    * @param bClosing Should be <code>true</code> if this save is occurring just
    * before the window is to be closed
    *
    * @return <code>true</code> if everything was successful, <code>false</code>
    * if any errors, or the user cancelled for some reason.
    */
   @SuppressWarnings("deprecation")
   private boolean save( boolean bClosing )
   {
      try
      {
         // save the current settings of a pipe
         if (prepareDatasetForSave())
         {
            // get the user configuration
            Properties config = m_app.getUserProperties();

            Vector editors = getEditingWindows( getPipe(), !bClosing );
            if ( !saveEditingWindows( editors, bClosing ))
               return false;

            // save the contents of this pipe
            Component[] comps = this.getDrawingPane().getComponents();
            for (int i=0; i<comps.length; i++)
            {
               Debug.assertTrue(comps[i] instanceof UIConnectableFigure,
                            E2Designer.getResources(), "BadUICType", null);

               UIConnectableFigure fig = (UIConnectableFigure) comps[i];
               if (fig.isAttached() || fig.getData() instanceof PSPipe)
               {
                  Object data = fig.getData();
                  if (data != null)
                  {
                     if (data instanceof PSPipe)
                        savePipeAttachments(m_app, fig, config);

                     ((IPersist) data).save(m_app, m_dataset, config);
                  }
               }
            }
            if ( bClosing )
               closeEditingWindows( editors );
         }

         return true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
   }


   /**
    * Saves all attachements for the passed pipe
    */
   //////////////////////////////////////////////////////////////////////////////
   protected void savePipeAttachments(PSApplication app, UIConnectableFigure pipe,
                            Properties config)
   {
      try
      {
         if (pipe != null)
         {
            PSPipe dataPipe =(PSPipe) pipe.getData();
            preparePipeForSave( dataPipe );

            Enumeration e = pipe.getConnectionPoints();
            while (e.hasMoreElements())
            {
               UIRigidConnectionPoint cp = (UIRigidConnectionPoint) e.nextElement();
               if (cp != null)
               {
                  UIConnectableFigure attachment = cp.getAttached();
                  if (attachment != null)
                  {
                     // save attachments data
                     Object data = attachment.getData();
                     if (data instanceof OSPageDatatank ||
                        data instanceof OSDataEncryptor ||
                        data instanceof OSResultPager ||
                        data instanceof OSTransaction)
                     {
                        ((IPersist) data).save(app, m_dataset, config);
                     }
                     else
                        ((IPersist) data).save(app, pipe.getData(), config);
                  }
               }
            }

            // do any cross-object 'fixup' that needs to be done
            if ( dataPipe instanceof OSUpdatePipe )
            {
               /* Make sure every column in the mapper has a corresponding
                  entry in the updater. */
               OSUpdatePipe uPipe = (OSUpdatePipe) dataPipe;
               PSCollection mappings = uPipe.getDataMapper();
               PSDataSynchronizer sync = uPipe.getDataSynchronizer();
               PSCollection updCols = null;
               if ( null != sync )
                  updCols = sync.getUpdateColumns();
               if ( null != mappings && null != updCols )
               {
                  Vector<PSUpdateColumn> newUpdateCols = new Vector<PSUpdateColumn>(10);
                  /* The plan: check each entry in the mapper for its
                     presence in the updater. If it's not there, add it. */
                  int mappingCt = mappings.size();
                  int updateCt = updCols.size();

                  // remove all cols but keys, reverse order is more efficient
                  for (int i=updateCt-1; i >= 0; --i)
                  {
                     PSUpdateColumn col = (PSUpdateColumn) updCols.get(i);
                     if (!col.isKey())
                        updCols.remove(col);
                  }

                  // reset count in case any cols removed from collection
                  updateCt = updCols.size();
                  for (int i=0; i<mappingCt; i++)
                  {
                     PSDataMapping mapping = (PSDataMapping) mappings.get(i);
                     PSBackEndColumn beMapCol = (PSBackEndColumn) mapping.getBackEndMapping();
                     boolean bFound = false;
                     for (int k=0; k<updateCt && !bFound; k++)
                     {
                        PSUpdateColumn uCol = (PSUpdateColumn) updCols.get(k);
                        if ( uCol.getColumn().equals( beMapCol ))
                           bFound = true;
                     }
                     if ( !bFound )
                     {
                        // add entry to updater
                        PSUpdateColumn updCol = new PSUpdateColumn(beMapCol, false);
                        newUpdateCols.add(updCol);
                     }
                  }
                  // add the new cols to the updater
                  int newCols = newUpdateCols.size();
                  for ( int x=0; x < newCols; ++x )
                  {
                     PSUpdateColumn updCol = newUpdateCols.get(x);
                     updCol.setUpdateable(true);
                     updCols.add(updCol);
                  }
               }
            }

         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Removes all dataset pipe elements, assuming the user has deleted them.
    * The save operation will then fill in again whats still there.
    *
    * @return <code>true</code> always.
    */
   private boolean prepareDatasetForSave()
   {
      try
      {
         // delete pipe elements stored within dataset
         m_dataset.setResultPager( null );
         m_dataset.setDataEncryptor( null );
         m_dataset.setTransactionDisabled();
         m_dataset.setPageDataTank( null );
         m_dataset.setPipe( null );
      } catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }

      return true;
   }

   /**
    * Removes all pipe attachments, assuming the user has deleted them. The save
    * operation will then fill in again whats still there.
    *
    * @param pipe the pipe to prepare for save
    */
   private void preparePipeForSave(PSPipe pipe)
   {
      try
      {
         // delete attachments for all pipe types
         pipe.setDataMapper(null);
         try
         {
            pipe.setBackEndDataTank(null);
         }
         catch (IllegalArgumentException e)
         {
            // this try-catch block CAN BE REMOVED as soon as the object store allows
            // us to remove the page data tank
         }

         if (pipe instanceof OSQueryPipe)
         {
            OSQueryPipe query = (OSQueryPipe) pipe;

            // delete query pipe attachments
            query.setDataSelector(null);
            query.setResultDataExtensions(null);
         }
         else if (pipe instanceof OSUpdatePipe)
         {
            OSUpdatePipe update = (OSUpdatePipe) pipe;

            // delete update pipe attachments
            try
            {
               update.setDataSynchronizer(null);
            }
            catch (IllegalArgumentException e)
            {
               // this try-catch block CAN BE REMOVED as soon as the object store allows
               // us to remove the page data tank
            }
            update.setInputDataExtensions(null);
         }
      }
      catch (IllegalArgumentException e)
      {
         e.printStackTrace();
      }
   }

   // Operations
   /**
    * Returns the figure factory for this class. The exception that could occur
    * is a design time error, so it is allowed to propagate.
    */
   @Override
   protected FigureFactory getFigureFactory()
   {
      return FigureFactoryManager.getFactoryManager( ).getFactory(
         sFIGURE_FACTORY );
   }


   /**
    * All objects in the pipe frame are in the same layer.
    */
   @Override
   @SuppressWarnings("unused")
   protected Integer getLayer(UIConnectableFigure uic)
   {
      return 10;
   }


   // IEditor implementation
   /**
    * @param figure should be a UIConnectable figure with an ID of DATASET_ID.
    *
    * @param data should be of type PSApplication
    */
   @SuppressWarnings("deprecation")
   public boolean onEdit( UIFigure figure, final Object data )
   {
      Cursor restoreCursor = E2Designer.getApp().getMainFrame().getCursor();
      Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
      E2Designer.getApp().getMainFrame().setCursor(waitCursor);

      try
      {
         if ( null != m_dataset )
         {
            // we already have an editor up
            moveToFront();
            setSelected( true );
            return false;
         }

         if ( AppFigureFactory.DATASET_ID != figure.getId())
            throw new IllegalArgumentException( "Dataset editor called on non-dataset obj");

         Properties config = null;
         m_dataset = (OSDataset) figure.getData();
         m_app = (PSApplication) data;

         // only load the pipe user config once from the stored load user config
         if (m_dataset.getPipe() instanceof OSQueryPipe)
         {
            OSQueryPipe pipe = (OSQueryPipe) m_dataset.getPipe();
            if (pipe.isLoadedAtLeastOnce())
               config = m_app.getUserProperties();
         }
         else if (m_dataset.getPipe() instanceof OSUpdatePipe)
         {
            OSUpdatePipe pipe = (OSUpdatePipe) m_dataset.getPipe();
            if (pipe.isLoadedAtLeastOnce())
               config = m_app.getUserProperties();
         }

         IOSPipe pipe = null;
         if (config == null)
         {
            UIAppFrame appFrame = E2Designer.getApp().getMainFrame()
                  .getApplicationFrame(m_app.getName());
            if (appFrame == null)
               throw new IllegalArgumentException("need a valid application frame");

            config = appFrame.getLoadUserConfig();
            if (m_dataset.getPipe() instanceof OSQueryPipe)
            {
               pipe = (OSQueryPipe) m_dataset.getPipe();

               // TODO: bad way to do this; must change at a later date;
               ((OSQueryPipe)pipe).loadedAtLeastOnce();
            }
            else if (m_dataset.getPipe() instanceof OSUpdatePipe)
            {
               pipe = (OSUpdatePipe) m_dataset.getPipe();

               // TODO: bad way to do this; must change at a later date;
               ((OSUpdatePipe)pipe).loadedAtLeastOnce();
            }
         }

         // need to save a copy of the pipe in case the user decides to delete the
         // pipe from the frame...
         m_pipeCopy = pipe; //m_dataset.getPipe();

         // getting dataset name from the requestor. if it does not exist, put in
         // "<Unspecified>".
         String name = null;
         if (m_dataset.getRequestor().getRequestPage().trim().equals(""))
            name = E2Designer.getResources().getString("Dummy");
         else
            name = m_dataset.getRequestor().getRequestPage();

         setTitle(name);
         getXmlApplicationEditor().addFrameAsPage(this, getTitle());

         if (m_dataset.getPipe() != null)
         {
            final int PIPE_DEFAULT_OFFSET = 10;
         /* how much to move pipe if there is no datatank on left side, to leave
            horizontal room for it */
            final int PIPE_XOFFSET = 50;
        /* how much to move pipe if there are no datatanks at all, to leave
         vertical room for them */
            final int PIPE_YOFFSET = 40;
        // we need to load all pipe elements owned by this dataset right away,
        // so the GUI can be reconstructed from the current settings
            UIConnectableFigure pipeFig = null;
            if (m_dataset.getPipe() instanceof OSQueryPipe)
            {
               // update the action list according to the pipe type
               updateActionList(true);

               // recreate the figure
               pipeFig = this.add(PipeFigureFactory.QUERY_PIPE);

               // restore the data
               OSQueryPipe d = (OSQueryPipe) pipeFig.getData();
               if (d.load(m_app, m_dataset, config))
               {
                  // load pipe attachments
                  loadQueryPipe(pipeFig, d, config);

                /* leave a small border, plus enough space for the datatank, if
                     there isn't one present */
                  int xOff = PIPE_DEFAULT_OFFSET;
                  int yOff = PIPE_DEFAULT_OFFSET;
                  if ( null == d.getBackEndDataTank())
                  {
                     xOff += PIPE_XOFFSET;
                     if ( null == m_dataset.getPageDataTank())
                        yOff += PIPE_YOFFSET;
                  }
            /* set a default location in case there is no config saved, arbitrarily
               offset slightly from upper left corner to leave a small border */
                  pipeFig.setLocation( xOff, yOff );

            // reset location after everything is attached
                  OSLoadSaveHelper.loadOwner(d.getId(), config, d.getFigure());
                  setVisible(true);
               }
            }
            else if (m_dataset.getPipe() instanceof OSUpdatePipe)
            {
               // update the action list according to the pipe type
               updateActionList(false);

               // recreate the figure
               pipeFig = this.add(PipeFigureFactory.UPDATE_PIPE);

               // restore the data
               OSUpdatePipe d = (OSUpdatePipe) pipeFig.getData();
               if (d.load(m_app, m_dataset, config))
               {
                  // load pipe attachments
                  loadUpdatePipe(pipeFig, d, config);

                /* leave a small border, plus enough space for the datatank(s), if
                     there isn't one present */
                  int xOff = PIPE_DEFAULT_OFFSET;
                  int yOff = PIPE_DEFAULT_OFFSET;
                  if ( null == m_dataset.getPageDataTank())
                  {
                     xOff += PIPE_XOFFSET;
                     if ( null == d.getBackEndDataTank())
                        yOff += PIPE_YOFFSET;
                  }
            /* set a default location in case there is no config saved, arbitrarily
               offset slightly from upper left corner to leave a small border.
               More space is left on the side to leave room for a page tank. */
                  pipeFig.setLocation( xOff, yOff );

            // reset location after everything is attached
                  OSLoadSaveHelper.loadOwner(d.getId(), config, d.getFigure());
                  setVisible(true);
               }
            }
            else
               throw new IllegalArgumentException("Unsupported pipe type");

            return true;
         }

         return false;
      }
      catch (ClassCastException e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException();
      }
      catch ( PropertyVetoException e )
      {
         // do nothing
         return false;
      }
      finally
      {
         E2Designer.getApp().getMainFrame().setCursor(restoreCursor);
      }
   }
   
   /**
    * IEditor interface method. Implements the simple algorithm for now.
   **/
   public boolean isDataChanged()
   {
      // TODOph: Implement smarter algorithm
      return true;
   }

   /**
    * Update action list for the provided pipe type.
   *
   * @param forQuery the pipe type, true for query, false for update
    */
   //////////////////////////////////////////////////////////////////////////////
   private void updateActionList(boolean forQuery)
   {
      for (int i=0; i<m_actions.size(); i++)
      {
         final IAction action = m_actions.get(i);
         final String internalName = action.getId();
         if (forQuery)
         {
            if (internalName.equals(
                      getInsertIdFromFigureName(PipeFigureFactory.TRANSACTION_MGR)) ||
                internalName.equals(
                      getInsertIdFromFigureName(PipeFigureFactory.SYNCHRONIZER)))
            {
               m_actions.remove(i);
            }
         }
         else
         {
            if (internalName.equals(
                      getInsertIdFromFigureName(PipeFigureFactory.SELECTOR)) ||
                internalName.equals(
                      getInsertIdFromFigureName(PipeFigureFactory.RESULT_PAGER)))
            {
               m_actions.remove(i);
            }
         }
      }

      // force main frame to recreate its insert menu
      getXmlApplicationEditor().notifyPageSwitched();
   }

   /**
    * Load a generic pipe (objects used in all pipes).
    *
    * @param pipeFig the pipe figure
    * @param pipeData the pipe to load
    * @param config the user configuration to use
    */
   //////////////////////////////////////////////////////////////////////////////
   private void loadPipe(UIConnectableFigure pipeFig, PSPipe pipeData, Properties config)
   {
      UIConnectableFigure fig = null;
      UIConnectionPoint cp = null;

      // load data encryptor
      if (m_dataset.getDataEncryptor() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.ENCRYPTOR);

         // restore the data
         OSDataEncryptor d = (OSDataEncryptor) fig.getData();
         if (d.load(m_app, m_dataset, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_ENCRYPTOR);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }

      // load page datatank
      if (m_dataset.getPageDataTank() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.PAGE_DATATANK);

         // restore the data
         fig.setData(m_dataset.getPageDataTank());

         if (fig.getData() instanceof OSPageDatatank &&
             ((OSPageDatatank) m_dataset.getPageDataTank()).getFigure() != null)
         {
            ((OSPageDatatank) m_dataset.getPageDataTank()).getFigure().invalidateLabel();
         }
         cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_PAGE_TANK);
         if (cp != null)
            fig.createDynamicConnection(cp);
      }

      // load backend tank
      if (pipeData.getBackEndDataTank() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.BACKEND_DATATANK);

         // restore the data
         OSBackendDatatank d = (OSBackendDatatank) fig.getData();
         if (d.load(m_app, pipeData, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_BACKEND_TANK);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }

      // load data mapper
      if (pipeData.getDataMapper() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.MAPPER);

         // restore the data
         OSDataMapper d = (OSDataMapper) fig.getData();
         if (d.load(m_app, pipeData, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_MAPPER);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }
   }

   /**
    * Load a query pipe.
    *
    * @param pipeFig the pipe figure
    * @param pipeData the pipe to load
    * @param config the user configuration to use
    */
   //////////////////////////////////////////////////////////////////////////////
   private void loadQueryPipe(UIConnectableFigure pipeFig, OSQueryPipe pipeData,
                                        Properties config)
   {
      loadPipe(pipeFig, pipeData, config);

      UIConnectableFigure fig = null;
      UIConnectionPoint cp = null;

      // load data selector
      if (pipeData.getDataSelector() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.SELECTOR);

         // restore the data
         OSDataSelector d = (OSDataSelector) fig.getData();
         if (d.load(m_app, pipeData, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_SELECTOR);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }

      // load result pager
      if (m_dataset.getResultPager() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.RESULT_PAGER);

         // restore the data
         OSResultPager d = (OSResultPager) fig.getData();
         if (d.load(m_app, m_dataset, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_RESULT_PAGER);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }
   }

   /**
    * Load a update pipe.
    *
    * @param pipeFig the pipe figure
    * @param pipeData the pipe to load
    * @param config the user configuration to use
    */
   //////////////////////////////////////////////////////////////////////////////
   private void loadUpdatePipe(UIConnectableFigure pipeFig,
                               OSUpdatePipe pipeData,
                               Properties config)
   {
      loadPipe(pipeFig, pipeData, config);

      UIConnectableFigure fig = null;
      UIConnectionPoint cp = null;

      // load data synchronizer
      if (pipeData.getDataSynchronizer() != null)
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.SYNCHRONIZER);

         // restore the data
         OSDataSynchronizer d = (OSDataSynchronizer) fig.getData();
         if (d.load(m_app, pipeData, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_SYNCHRONIZER);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }

      // load transaction manager
      if (!m_dataset.isTransactionDisabled())
      {
         // recreate the figure
         fig = this.add(PipeFigureFactory.TRANSACTION_MGR);

         // restore the data
         OSTransaction d = (OSTransaction) fig.getData();
         if (d.load(m_app, m_dataset, config))
         {
            cp = pipeFig.getConnectionPoint(UIConnectionPoint.CP_ID_TRANSACTION_MGR);
            if (cp != null)
               fig.createDynamicConnection(cp);
         }
      }
   }

   /**
    * This editor is never modal.
    *
    * @returns Always <code>false</code>
    */
   public boolean isModal()
   {
      return false;
   }


   /**
    * Implements IEditor interface method.
    *
    * @param context must be a Boolean that indicates whether to close the
    * window.
    */
   public boolean saveData( Object context )
   {
      if ( !(context instanceof Boolean ))
         throw new IllegalArgumentException();

      return save(((Boolean) context).booleanValue());
   }


   /**
    * IEditor interface method. Checks if there are unattached objects. If
    * there are any, the user is asked if they want to loose them. If they
    * answer "yes", a non-zero cookie is returned, otherwise 0 is returned.
    * TODOph: Implement canceling for this window.
    *
    * @return A non-zero cookie if there are no unattached objects or the user
    * doesn't mind losing them, 0 otherwise.
    */
   public int canClose()
   {
      Vector editors = getEditingWindows( getPipe(), false );
      if ( !canCloseEditingWindows( editors ))
         return 0;

      // warn user, that he will loose all unattached objects if applicable
      Component[] comps = this.getDrawingPane().getComponents();
      for (int i=0; i<comps.length; i++)
      {
         Debug.assertTrue(comps[i] instanceof UIConnectableFigure,
         E2Designer.getResources(), "BadUICType", null);

         int pipeCounter = 0;
         UIConnectableFigure fig = (UIConnectableFigure) comps[i];
         if (fig.getData() instanceof PSPipe)
            pipeCounter++;

         if ((!fig.isAttached() && (!(fig.getData() instanceof PSPipe))) || (pipeCounter > 1))
         {
            int option = PSDlgUtil.showConfirmDialog(
                  E2Designer.getResources().getString("LoosingUnattachedObjects"),
                  E2Designer.getResources().getString("ConfirmOperation"),
                  JOptionPane.OK_CANCEL_OPTION,
                  JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.CANCEL_OPTION)
            {
               m_timer.start();
               return 0;
            }
            break;
         }
      }
      m_closeCookie = Util.getUniqueId();
      return m_closeCookie;
   }


   /**
    * Implements IEditor interface method. Closes any open child editors and
    * disposes the window w/o saving.
    *
    * @return Always <code>true</code>
   **/
   public boolean close( int cookie, boolean bForce )
   {
      if ( !bForce && cookie != m_closeCookie && 0 == canClose())
         return false;
      closeEditingWindows( getEditingWindows(getPipe(), false));
      return true;
   }


   /**
   * Returns the join editor if it is up and visible
   */
   public UIJoinMainFrame getJoinEditor()
   {
      Component[] comps = getDrawingPane().getComponents();
      if(comps != null)
      {
         for(int iComp = 0; iComp < comps.length; ++ iComp)
         {
            if(comps[iComp] instanceof UIConnectableFigure)
            {
               UIConnectableFigure fig = (UIConnectableFigure)comps[iComp];
               if(fig.getId() == PipeFigureFactory.PIPE_ID)
               {
                  Component[] children = fig.getComponents();
                  for(int iChild = 0; iChild < children.length; ++iChild)
                  {
                     if(children[iChild] instanceof UIConnectableFigure)
                     {
                        UIConnectableFigure childfig = (UIConnectableFigure)children[iChild];
                        if(childfig.getEditor() != null && childfig.getEditor() instanceof UIJoinMainFrame)
                        {
                           UIJoinMainFrame frame = (UIJoinMainFrame)childfig.getEditor();
                           if(frame.isVisible())
                           {
                              return(frame);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return(null);
   }
   
   /**
    * The name of the factory that creates the figures used in this window.
    */
   private static final String sFIGURE_FACTORY =
         "com.percussion.E2Designer.PipeFigureFactory";

   // variables
   /**
    * The application that this dataset is associated with.
    */
   OSDataset m_dataset;
   private PSApplication m_app;
   private Vector<IAction> m_actions;
   private static final Dimension FRAME_SIZE = new Dimension(480, 200);

   private Timer m_timer = null;
   private VetoableChangeListener m_vetoableListener = null;
   private ActionListener m_timerListener = null;

   private int m_closeCookie = 1;

  /**
   * A copy of the reference to this pipeFrame&apos;s pipe. Needed in case user
   * removes pipe object from the frame.
   */
   private IOSPipe m_pipeCopy = null;

@Override
public boolean isSelected()
{
   return super.isSelected();
}
}

