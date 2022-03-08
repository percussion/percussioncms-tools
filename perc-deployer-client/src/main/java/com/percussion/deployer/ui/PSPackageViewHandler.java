/******************************************************************************
 *
 * [ PSPackageViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSLogDetail;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.PSResources;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The view handler that handles the 'Package' view.
 */
public class PSPackageViewHandler extends PSTableViewHandler
{

   /**
    * Overridden to update the table model to display the package logs in the
    * table. See <code>super.setData(Object)</code> for more description of
    * the method and its parameters.
    */
   public void setData(Object object) throws PSDeployException
   {
      super.setData(object);
      m_server = (PSDeploymentServer)m_object;
      setTableModel( m_server.getPackages(false) );
   }

   /**
    * Creates the pop-up menu items and corresponding actions that need to be
    * shown in this view.
    */
   protected void createPopupMenu()
   {
      Iterator actions = getActions();
      while(actions.hasNext())
      {
         m_popupMenu.add((Action)actions.next());
      }
   }

   //implements super's method, Does not enable or disabe any of menu items
   protected void updatePopupMenu()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");
   }

   /**
    * Creates all the actions for the popup menu items that is shown in this
    * view.
    *
    * @return iterator of actions list, never <code>null</code> or empty.
    */
   private Iterator getActions()
   {
      List m_actions = new ArrayList();

      PSResources res = PSDeploymentClient.getResources();

      m_actions.add(new AbstractAction(res.getString("export"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onExport();
         }
      });

      return m_actions.iterator();
   }
   
   /**
    * Displays a file browser dialog to export/save the package log information
    * for which the pop-up menu is shown to a text file. Displays error dialog
    * with appropriate message if exceptions happen in the process.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action is shown.
    */
   private void onExport()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");

      FileOutputStream out = null;
      try {
         int id = Integer.parseInt(m_tableModel.getID(m_curSelectedRow));
         PSLogSummary summary =
            m_server.getDeploymentManager().getLogSummary(id);
         PSLogDetail logDetail = summary.getLogDetail();
         
         File selectedFile = PSDeploymentClient.showFileDialog(
            PSDeploymentClient.getFrame(), 
            new File(summary.getPackage().getDisplayName() + ".xml"),
            "xml", "XML Files (*.xml)", JFileChooser.SAVE_DIALOG);
            
         if(selectedFile != null)
         {            
            out = new FileOutputStream(selectedFile);
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.write(logDetail.toXml(doc), out);
         }
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }
      catch(IOException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
      finally
      {
         try {
            if(out != null)
               out.close();
         } catch(IOException ie){}
      }
   }

   //see IPSViewHandler interface
   public boolean supportsDetailView()
   {
      return true;
   }

   //see IPSViewHandler interface
   public void showDetailView(int index)
   {
      try
      {
         int rc = m_tableModel.getRowCount();
         if ( index < 0 || index >= rc )
            throw new IllegalArgumentException("Index supplied is not vaild");
            
         int id = Integer.parseInt(m_tableModel.getID(index));
         PSLogSummary logSum =
            m_server.getDeploymentManager().getLogSummary(id);
         PSLogSummaryDialog dlg = new PSLogSummaryDialog(
            PSDeploymentClient.getFrame(), logSum,
            m_server.getLiteralIDTypes());
         dlg.setVisible(true);
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }      
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
   }

   /**
    * The data object that supports this view and its pop-up menu actions, same
    * as super class <code>m_object</code>, but properly casted to be easy to
    * use. <code>null</code> until first call to <code>setData(Object)</code>.
    */
   private PSDeploymentServer m_server = null;
}
