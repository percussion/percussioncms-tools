/******************************************************************************
 *
 * [ PSBrowserPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The panel that represents the left pane of the main frame.
 */
public class PSBrowserPanel extends JPanel
{
   /**
    * Constructs the panel with supplied tree.
    * 
    * @param tree the registered servers tree, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if tree is <code>null</code>
    */
   public PSBrowserPanel(PSServersTree tree)
   {
      if(tree == null)
         throw new IllegalArgumentException("tree may not be null.");
         
      m_serversTree = tree;
      init();
   }

   /**
    * Initializes this panel with a tabbed pane of a single tab containing the
    * servers tree.
    */
   private void init()
   {
      String serverGroupName = PSDeploymentClient.getResources().getString(
         "serverGroups");
         
      setLayout(new BorderLayout());
         
      JTabbedPane tabbedPane = new JTabbedPane();      
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());      
      panel.setBackground(Color.white);
      tabbedPane.add(serverGroupName, panel);

      panel.add(m_serversTree, BorderLayout.WEST);
      add(tabbedPane, BorderLayout.CENTER);
   }
   
   /**
    * Gets the registered servers from the tree and saves them to a file. Please
    * see {@link PSDeploymentClient#saveServers(Map) saveServers} for more 
    * details.
    */
   public void save()
   {
      PSDeploymentClient.saveServers(m_serversTree.getRegisteredServerMap());
   }
   
   /**
    * Gets the servers tree.
    * 
    * @return the tree, never <code>null</code>
    */
   public PSServersTree getServersTree()
   {
      return m_serversTree;
   }
   
   /**
    * The servers tree which represents the registered servers grouped by the 
    * repository they are using. Initialized during construction and never 
    * <code>null</code> after that. The tree structure will be modified as user
    * adds/deletes the registered servers.
    */
   private PSServersTree m_serversTree;
}
