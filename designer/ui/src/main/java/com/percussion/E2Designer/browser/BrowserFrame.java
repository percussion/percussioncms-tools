/******************************************************************************
 *
 * [ BrowserFrame.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.CatalogDatasources;
import com.percussion.E2Designer.CatalogExtensionCatalogHandler;
import com.percussion.E2Designer.CatalogServerExits;
import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.E2Designer.UserConfig;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.catalog.PSCataloger;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.PSStringComparator;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;


/**
 * The main frame window for the browser. It contains the tabbed pane and a
 * a static control indicating the current E2 server. It uses <code>BorderLayout</code>
 * as the layout manager, with the static control in the <code>NORTH</code> border and the
 * BrowserPane (tabbed pane) in the <code>CENTER</code> border. Only a single
 * instance of the browser is allowed.
 */
public class BrowserFrame extends JFrame
{
    //create static resource bundle object
    static ResourceBundle m_res = null;
    static
    {
      try
      {
        m_res = ResourceBundle.getBundle( "com.percussion.E2Designer.browser.BrowserFrameResources", Locale.getDefault( ) );
      }catch(MissingResourceException mre)
      {
        System.out.println( mre );
      }
    }
   // constructors
   /**
    * Gets the single instance of the browser, creating it the first time.
    * When the browser is created, state information is downloaded
    * from the server and the appropriate tabs and entries are created.
    * <p>
    * If a browser exists and the passed in server name doesn't match
    * (case insensitive compare), the existing browser is removed and a new
    * one is created.
    */
    public static BrowserFrame getBrowser(PSDesignerConnection connection,
                                        PSObjectStore os,
                                        String serverName)
   {
            if (null == ms_theBrowser)
            {
               System.out.println("Connecting to Rhythmyx server ...");

               PSCataloger cataloger = null;
               try
               {
                  // get the Cataloger
                  cataloger = new PSCataloger(connection);
               }
               catch (IllegalArgumentException e)
               {
                  System.out.println( e.getLocalizedMessage());
               }


               ms_theBrowser = new BrowserFrame( );
               // fill up the browser
               ms_theBrowser.m_Pane = new BrowserPane( );
               ms_theBrowser.m_UC = UserConfig.getConfig();
               ms_theBrowser.m_Connection = connection;
               ms_theBrowser.m_Cataloger = cataloger;
               ms_theBrowser.m_Store = os;

               ms_theBrowser.init(serverName);
            }

            return ms_theBrowser;
   }

   /**
    * Same as above, except if the current browser is null, null is returned.
    */
    public static BrowserFrame getBrowser( )
   {
      return ms_theBrowser;
   }

   /**
    * Made private to implement singleton model. Use <code>getBrowser</code> to
    * get the single instance.
    */
   private BrowserFrame( )
   {
         // add listener for capturing
         BrowserWindowListener bwl = new BrowserWindowListener( );
         this.addWindowListener( bwl );

   }

  /**
   * Our basic frame initialization. Creates all tabs and their data.
   *
   * @param serverName
   */
   private void init(String serverName)
   {
      this.setTitle(serverName + " " + m_res.getString("BROWSER"));

      if (m_UC == null)
      {
         System.out.println("User config is null");
         return;
      }

      /*
       * The tab creation methods create a BrowseTree object which is throwing
       * an exception, when a new application is created. This started happening
       * after upgrading the Eclipse to version 3.2.2 and JRE to 1.6. Bug
       * #RX-13903. This is leftover code from old SWING Workbench and getting
       * executed unnecessarily. Commented out this code to fix the issue.
       * 
       * createApplicationsTab();
       * 
       * createLocalFilesTab();
       * 
       * createServerObjectsTab();
       */

      Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());

      if (m_UC.getValue(BROWSER_SCHEMA) == null)
      {
         // value does not exist means initial setup for the data group
         m_UC.setValue(BROWSER_SCHEMA, BROWSER_CURRENT_SCHEMA_VALUE);
         this.setSize(screenSize.width / 5, screenSize.height * 4 / 5);
         this.setLocation(screenSize.width / 10, screenSize.height / 10);
      }
      else
      {
         int[] iaPos = this.getBrowserWindowPosFromUserConfig();
         if(iaPos == null || iaPos.length != 4)
         {
            this.setSize(screenSize.width / 5, screenSize.height * 4 / 5);
            this.setLocation(screenSize.width / 10, screenSize.height / 10);
         }
         else
         {
            this.setLocation(iaPos[0], iaPos[1]);
            this.setSize(iaPos[2], iaPos[3]);
         }

      }

      // add datasource tab
      //See the comments above other tab creation calls.
      //createDatasourceTab();

      // now save any changes to the server
      m_UC.flush();
   }

   // attributes
   public BrowserPane getBrowserPane( )
   {
      return m_Pane;
   }


   /**
    * Returns the PSDesignerConnection object created earlier from the connection
   * made to the E2 Server.
    */
  public PSDesignerConnection getConnection( )
  {
    return m_Connection;
  }

   /**
    * Returns the PSCataloger object
    */
  public PSCataloger getCataloger( )
  {
    return m_Cataloger;
  }


   /**
    * Returns the PSObjectStore object
    */
  public PSObjectStore getObjectStore( )
  {
    return m_Store;
  }


   /**
   * A generic method to wrap the calls made to set the user config values.
   *
   * @param strKey is the key name to be set in the user config.
   * @param strNewValues is an array of new values to be put in.
   * @param replace indicates whether to replace the key. If replace is <code>true</code>
   * the key in the user config is replaced by the new values. If replace is <code>false</code>
   * the values in the <code>strNewValues</code> array are searched in the existing value
   * corresponding to the <code>strKey</code> and only the values that are not found
   * are appended to the end.
    */
  public void setUserConfigFor(String strKey, String [] strNewValues, boolean replace)
  {
    if ( m_UC == null || m_UC.isEmpty())
    {
      System.out.println("User config is empty");
      return;
    }

    if (!replace)
    {
        for (String strNewValue : strNewValues) {
            if (!m_UC.isStringTokenPresent(strKey, strNewValue)) {
                m_UC.setValue(strKey, strNewValue, true);
            }
        }
    }
    else  //replace the values
    {
      m_UC.deleteEntry(strKey);
        for (String strNewValue : strNewValues) {
            m_UC.setValue(strKey, strNewValue, true);
        }
    }
  }




   /**
   * A generic method to wrap the calls made to get the values from the user config.
   *
   *@param strKey is the name of the key.
   *@return an array of <code>String</code> values for the specified key.
    */
  public String[] getValuesFromUserConfig(String strKey)
  {
    String [] strValues = null;
    if ( m_UC == null || m_UC.isEmpty())
    {
//      System.out.println("User config is empty");
      return null;
    }
    try
    {
      if (m_UC.getValue(BROWSER_SCHEMA) != null)
      {
        strValues = m_UC.getStringTokens(strKey);
      }
    }
    catch(Exception e)
    {
      System.out.println(e.toString());
    }
    return strValues;
  }


   /**
    * A generic method to wrap the calls made to get the values from the user
    * config.
    * 
    * @returns the value for the specified key.
    */
  public String getValueFromUserConfig(String strKey)
  {
    if (m_UC == null || m_UC.isEmpty())
      return null;

    return m_UC.getValue(strKey);
  }


   /**
    * Sets the Window information for the browser in the user config. The array
    * of <code>int</code> passed in contains four values:
    * <p>
    * <code>&ltleft&gt,&lttop&gt,&ltwidth&gt,&ltheight&gt</code>
    * <p>
    * Where <code>&ltleft&gt</code> and <code>&lttop&gt</code> are in screen
    * coords and <code>&ltwidth&gt</code> and <code>&ltheight&gt</code> are
    * in pixels.
    */
   public void setUserConfigForBrowserWindowPos(int [] iaValues)
   {
      m_UC.setIntArray(BROWSER_WINDOW_POS, iaValues);
   }

   public int[] getBrowserWindowPosFromUserConfig()
   {
      return m_UC.getIntArray(BROWSER_WINDOW_POS);
   }

  /**
    *   Returns the GroupID associated with the current tab.
    * @see BrowserPaneTabData
   */
   public int getGroupIDForCurrentTab()
   {
      BrowserPane btp = this.getBrowserPane();
      int iTabIndex = btp.getSelectedIndex();
      Vector v = btp.getTabData();
      BrowserPaneTabData bptd = (BrowserPaneTabData) v.elementAt(iTabIndex);
      return bptd.getGroupID();
   }

   /**
    * creates the tab for the server objects
    */
   public void createServerObjectsTab()
   {
      // make the root node
      DefaultBrowserNode root = new DefaultBrowserNode(
         E2Designer.getResources().getString("ServerObject") );
      m_serverObjects = new BrowserTree(root);
      DefaultTreeModel dtm = (DefaultTreeModel) m_serverObjects.getModel();

      //add Java Exits and its children
      String label = E2Designer.getResources().getString("JavaExits");
      DefaultBrowserNode javaExits = new DefaultBrowserNode( label );
      dtm.insertNodeInto(javaExits, root, 0);

      // make the two folders
      label = E2Designer.getResources().getString("PRE_JAVAEXIT");
      m_pre = new DefaultBrowserNode( label );
      m_pre.setFullPathName( label );
      dtm.insertNodeInto(m_pre, javaExits, 0);

      label = E2Designer.getResources().getString("POST_JAVAEXIT");
      m_post = new DefaultBrowserNode( label );
      m_post.setFullPathName( label );
      dtm.insertNodeInto(m_post, javaExits, 1);

      // check featureset for newer objects
      if(FeatureSet.getFeatureSet().isFeatureSupported(E2Designer.CX_FEATURE))
      {
         NodeInfo[] leafInfo =
         {
            new NodeInfo("SEARCHES", "gif_img"),
            new NodeInfo("DISPLAYFORMATS", "gif_img"),
            new NodeInfo("ACTIONMENUS", "gif_img"),
            new NodeInfo("RELATIONSHIPS", "gif_img")
         };

         Arrays.sort(leafInfo, new Comparator<NodeInfo>()
               {
                  public int compare(NodeInfo n1, NodeInfo n2)
                  {
                     return n1.m_label.compareToIgnoreCase(n2.m_label);
                  }
               });

         int index = 1;
         for (int i=0; i < leafInfo.length; i++)
         {
            label = leafInfo[i].m_label;
            CatalogEntry rsEntry = new CatalogEntry();
            rsEntry.setInternalName(label);
            if (null != leafInfo[i].m_icon)
               rsEntry.setIcon(leafInfo[i].m_icon);
            DefaultBrowserNode node = new DefaultBrowserNode(rsEntry);
            node.setAllowsChildren(false);
            dtm.insertNodeInto(node, root, index++);
         }
      }

      // set the cell renderer
      JavaExitCellRenderer renderer=new JavaExitCellRenderer();
      m_serverObjects.setCellRenderer(renderer);

      // new panel
      JScrollPane jsp = new JScrollPane(m_serverObjects);

      m_Pane.insertGroupedTab(
         E2Designer.getResources().getString("ServerObject"), null, jsp ,
         E2Designer.getResources().getString("ServerObject") ,
         BrowserPaneTabData.SERVER_OBJECTS, 1);

      m_Pane.setGroupBackgroundAt(BrowserPaneTabData.SERVER_OBJECTS,
         Color.lightGray);

      // get the name of the cataloger
      String requestName=
         CatalogExtensionCatalogHandler.JAVA_EXTENSION_HANDLER_NAME;

       java.util.List<IPSExtensionDef> vc = null;
      java.util.List<IPSExtensionDef> preCategory = new ArrayList<>();
       java.util.List<IPSExtensionDef> postCategory = new ArrayList<>();

      if( requestName != null )
      {
         // catalog the exits
         vc=CatalogServerExits.getCatalog(
            E2Designer.getDesignerConnection(), requestName, false);
      }

      if (vc != null)
      {
         /*
          * Split the exit vector into two vectors, one for pre-exits and
          * one for post exits.
          */
          for (IPSExtensionDef exit : vc) {
              if (exit.implementsInterface(OSExitCallSet.EXT_TYPE_RESULT_DOC_PROC))
                  postCategory.add(exit);
              if (exit.implementsInterface(OSExitCallSet.EXT_TYPE_REQUEST_PRE_PROC))
                  preCategory.add(exit);

          }


         /*
          * Create and insert the category folders for pre and post exits.
          */
         Iterator categories = getCategories (preCategory);
         while(categories.hasNext())
            createCategoryNodes(m_pre, m_nodePreExits,
               (String)categories.next(), dtm);

         categories = getCategories (postCategory);
          while(categories.hasNext())
            createCategoryNodes(m_post, m_nodePostExits,
               (String)categories.next(), dtm);

         /*
          * Finally add all exits to the appropriate category folder.
          */
         for (int count=0; count<vc.size(); count++)
         {
            IPSExtensionDef exit = (IPSExtensionDef) vc.get(count);
            addJavaExit(exit);
         }
      }
   }

   /**
    * A little structure to group a tree node's label and icon. Members must
    * be accessed directly.
    */
   private class NodeInfo
   {
      /**
       *
       * @param labelKey Used to lookup the actual label text from the resource
       *    bundle.
       *
       * @param iconKey Used to find the name of the resource. This name is
       *    then used to find the actual resource.
       */
      public NodeInfo(String labelKey, String iconKey)
      {
         try
         {
            m_label = E2Designer.getResources().getString(labelKey);
         }
         catch (MissingResourceException mre)
         {
            //ignore
         }
         finally
         {
            if (m_label == null || m_label.trim().length() == 0)
               m_label = "Missing Label for key: " + labelKey;
         }

         try
         {
            m_icon =
                  new ImageIcon(getClass().getResource(m_res.getString(iconKey)));
         }
         catch (MissingResourceException mre)
         {
            //ignore
         }
      }

      /**
       * The actual label text. Never <code>null</code> or empty. If the key
       * supplied in ctor can't be found, an error message is used for the
       * label.
       */
      public String m_label;

      /**
       * The icon for the associated node. May be <code>null</code> if the
       * requested keys can't be found.
       */
      public ImageIcon m_icon = null;
   }


   /**
    * Create all category folders (nodes) for the provided exits and add
    * them to the supplied root.
    *
    * @param root the root node to which this will add all category nodes,
    *    assumed not <code>null</code>.
    * @param exits a vector of exits to create the category folders for,
    *    assumed not <code>null</code>, may be empty.
    * @param categoryPath the remaining category path, delimited by "/",
    *    assumed not <code>null</code>.
    * @param dtm the tree model which contains the provided root, assumed
    *    not <code>null</code>.
    */
   private void createCategoryNodes(DefaultBrowserNode root, Vector exits,
      String categoryPath, DefaultTreeModel dtm)
   {
      if (categoryPath.indexOf("/") == -1)
      {
         boolean found = false;
         for (int k=0; k<dtm.getChildCount(root) && found == false; k++)
         {
            // Check to see if this node has already been inserted
            DefaultBrowserNode existingNode = (DefaultBrowserNode)
               dtm.getChild(root, k);
            if(existingNode == null)
               continue;

            String childName = (String) existingNode.getUserObject();
            if (childName != null && childName.equals(categoryPath))
               found = true;
         }

         if (!found)
         {
            DefaultBrowserNode newNode = new DefaultBrowserNode(categoryPath);
            String fullPath = root.getFullPathName() + "/" + categoryPath;
            newNode.setFullPathName(fullPath);
            dtm.insertNodeInto(newNode, root, dtm.getChildCount(root));
            exits.addElement(newNode);
         }
      }

      else
      {
         int pos = categoryPath.indexOf("/");
         String newCategory = categoryPath.substring(0, pos);
         String newCategoryPath = categoryPath.substring(pos+1);

         DefaultBrowserNode newRoot =  new DefaultBrowserNode(newCategory);
         DefaultBrowserNode existingNode = null;
         boolean isFound = false;

         for (int k = 0; k<dtm.getChildCount(root) && !isFound; k++)
         {
            existingNode = (DefaultBrowserNode) dtm.getChild(root, k);
            if(existingNode == null)
               break;
            String childName = (String) existingNode.getUserObject();
            if (childName != null && childName.equals(newCategory))
               isFound = true;

         }

         if(isFound)
            createCategoryNodes(existingNode, exits, newCategoryPath, dtm);

         else
         {
            String fullPath = root.getFullPathName() + "/" + newCategory;
            newRoot.setFullPathName(fullPath);

            dtm.insertNodeInto(newRoot, root, dtm.getChildCount(root));
            exits.addElement(newRoot);
            createCategoryNodes(newRoot, exits, newCategoryPath, dtm);
         }
      }
   }

  /**
  *add the java exit under the proper node
  *
  *@param exit element to be inserted
  *
  */
   public void addJavaExit(IPSExtensionDef exit)
   {
     // get the type
      if (exit.implementsInterface(OSExitCallSet.EXT_TYPE_RESULT_DOC_PROC))
      {
         final String exitPath =
               E2Designer.getResources().getString("POST_JAVAEXIT") + "/";
         addJavaExit(exitPath, m_nodePostExits, exit, m_post,
            JavaExitNode.RESULT_DOC_PROC_EXT);
      }

      if (exit.implementsInterface(OSExitCallSet.EXT_TYPE_REQUEST_PRE_PROC))
      {
         final String exitPath =
               E2Designer.getResources().getString("PRE_JAVAEXIT") + "/";
         addJavaExit(exitPath, m_nodePreExits, exit, m_pre,
            JavaExitNode.REQUEST_PRE_PROC_EXT);
      }
   }

   /**
    * Add the provided java exit to the provided categories.
    *
    * @param path the root path to which this should add the exit, assumed
    *    not <code>null</code>.
    * @param categories a vector of category folder nodes to which this will
    *    add the provided exit, assumed not <code>null</code>, may be empty.
    * @param exit the exit to add, assumed not <code>null</code>.
    * @param root the root node which contains the provided catagories,
    *    assumed not <code>null</code>.
    * @param type the exit type, assumes its one of
    *    JavaExitNode.RESULT_DOC_PROC_EXT or JavaExitNode.REQUEST_PRE_PROC_EXT.
    */
   private void addJavaExit(String path, Vector categories,
      IPSExtensionDef exit, DefaultBrowserNode root, int type)
   {
      DefaultTreeModel dtm = (DefaultTreeModel) m_serverObjects.getModel();

      String category = exit.getRef().getCategory();
      String exitName = exit.getRef().getExtensionName();

      if (path.trim().length() > 0)
         path += category + "/" + exitName;
      else
         path += exitName;

      if (!exit.isDeprecated())
      {
         JavaExitNode addedNode = new JavaExitNode(exit, exitName, path);
         addedNode.setAllowsChildren(false);
         addedNode.setJavaExitType(type);
         for (int k=0; k<categories.size(); k++)
         {
            DefaultBrowserNode node =
               (DefaultBrowserNode) categories.get(k);
            String nodePath =
               node.getFullPathName() + "/" + exitName;

            if (path.equals(nodePath))
               dtm.insertNodeInto(addedNode, node, dtm.getChildCount(node));
         }

         if (category.trim().length() == 0)
            dtm.insertNodeInto(addedNode, root, dtm.getChildCount(root));
      }
   }

   public void createLocalFilesTab()
   {
      DefaultBrowserNode root = new DefaultBrowserNode(m_res.getString("LOCALFILES"));
       BrowserTree bt = new BrowserTree(root);
         bt.setCellRenderer(new BrowserTreeNodeRenderer());

       DefaultTreeModel dtm = (DefaultTreeModel)bt.getModel();

      int i = 0;
      FileSystemView fsysview = FileSystemView.getFileSystemView();
      if(fsysview != null)
      {
         File[] roots = File.listRoots();
         if(roots != null)
         {
            for(i = 0; i < roots.length; ++i)
            {
                ServerNode subRoot =  new ServerNode(new CatalogEntry())
               {
                  @Override
                   public boolean hasMenu()
                  {
                     return(false);
                  }
               };

               //FileCataloger fileCataloger = new FileCataloger(walkerForType.getResultData("name"));
                subRoot.setConstraints(new FileHierarchyConstraints());
                subRoot.setType(FileHierarchyConstraints.NT_DIRECTORY);
                subRoot.setInternalName(roots[i].getPath());
               subRoot.setFullPathName(roots[i].getPath());
               subRoot.disableSorting();

               dtm.insertNodeInto(subRoot, root, i);
            }
         }
      }

       JScrollPane jsp = new JScrollPane(bt);

       m_Pane.insertGroupedTab(m_res.getString("LOCALFILES"), null, jsp ,
                       m_res.getString("LOCALFILESTIP"), BrowserPaneTabData.XML, 1);
      m_Pane.setGroupBackgroundAt(BrowserPaneTabData.XML, Color.lightGray);
//         m_Pane.setForegroundAt(m_Pane.indexOfTab(m_res.getString("LOCALFILES")), Color.white);

   }


   /**
    * Creates the Applications tab for the browser.
    */
  public void createApplicationsTab()
  {
//      DefaultBrowserNode root = new DefaultBrowserNode(m_res.getString("APPLICATIONS"));
      DefaultBrowserNode root = new DefaultBrowserNode(new CatalogEntry());
      root.setInternalName(m_res.getString("APPLICATIONS"));
      BrowserTree bt = new BrowserTree(root);
      bt.setCellRenderer(new BrowserTreeNodeRenderer());
    JScrollPane jsp = new JScrollPane(bt);

    m_Pane.insertGroupedTab(m_res.getString("APPLICATIONS"), null, jsp ,
                            m_res.getString("APPLICATIONSTIP"),
                            BrowserPaneTabData.APPLICATION, 0);
    getContentPane().add(m_Pane);
    m_Pane.setGroupBackgroundAt(BrowserPaneTabData.APPLICATION, Color.lightGray);
      addAppsToAppsTab();

  }

   /*
   * Function to get the application JTree
   */
   public BrowserTree getAppTree()
   {
      int appTabIndex = m_Pane.indexOfTab(m_res.getString("APPLICATIONS"));
      return((BrowserTree)((JScrollPane)m_Pane.getComponentAt(appTabIndex)).getViewport().getView());
   }

   /** Adds the applications to the Applications tab. Assumes that the Browser tree component has already
   * been created and has the root node to which the Application nodes will be added.
    */
   public void addAppsToAppsTab()
   {
      //set the cursor to wait cursor when loading children
      E2Designer e2designer = E2Designer.getApp();
      UIMainFrame uiMF = null;
      if(e2designer != null)
      {
         uiMF = e2designer.getMainFrame();
         if(uiMF != null)
            uiMF.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }

    int appTabIndex = m_Pane.indexOfTab(m_res.getString("APPLICATIONS"));

      BrowserTree bt = (BrowserTree)((JScrollPane)m_Pane.getComponentAt(appTabIndex)).getViewport().getView();
      bt.setCellRenderer(new BrowserTreeNodeRenderer());
      DefaultBrowserNode root = (DefaultBrowserNode)bt.getModel().getRoot();
    DefaultTreeModel dtm = (DefaultTreeModel)bt.getModel();

      AppCataloger appCat = new AppCataloger();
      Iterator appCatIter = appCat.iterator();
      ServerNode subRoot = null;
      Vector<ServerNode> vAppNodes = new Vector<ServerNode>();
      while (appCatIter.hasNext())
      {
         subRoot = new ServerNode((ICatalogEntry)(appCatIter.next()));
//        String strAppName = ((DefaultBrowserNode)subRoot).getInternalName();
//         System.out.println("...........App Name = "+strAppName);
         subRoot.setConstraints(new ApplicationHierarchyConstraints());
      subRoot.setType(ApplicationHierarchyConstraints.NT_APPLICATION);
         subRoot.setAllowsChildren(false);                                 // children of apps are not useful
         vAppNodes.add(subRoot);                                                // for V1: do not show children
      }

      // sort the application nodes in alphabetical order
      root.sortNodes(vAppNodes);

      for(int i=0; i<vAppNodes.size(); i++)
      {
         subRoot = (ServerNode)vAppNodes.get(i);
         dtm.insertNodeInto(subRoot, root, i);
      }
      root.setLoadedChildren(true);
      if(uiMF != null)
         uiMF.setCursor(Cursor.getDefaultCursor());
   }


   /* Adds the provided application to the Applications tab. Assumes that the
   * Browser tree component has already been created and has the root node to
   * which the Application node will be added.
   *
   * @param app the application to add
   */
  //////////////////////////////////////////////////////////////////////////////
   public void addAppToAppsTab(PSApplication app)
   {
    int tabIndex = m_Pane.indexOfTab(m_res.getString("APPLICATIONS"));

      BrowserTree tree = (BrowserTree) ((JScrollPane)m_Pane.getComponentAt(tabIndex)).getViewport().getView();
      DefaultBrowserNode root = (DefaultBrowserNode) tree.getModel().getRoot();
    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

      Vector<Object> apps = new Vector<>();
    int count = model.getChildCount(root);
    for (int i=0; i<count; i++)
      apps.add(model.getChild(root, i));

    // add the new node
    CatalogEntryEx entry = new CatalogEntryEx();
    ServerNode node = new ServerNode(entry);
    node.setInternalName(app.getName());
      if(app.isActive())
         entry.setIcon(new ImageIcon(getClass().getResource(m_res.getString("gif_app_active"))));
      else
         entry.setIcon(new ImageIcon(getClass().getResource(m_res.getString("gif_app"))));
    node.setConstraints(new ApplicationHierarchyConstraints());
    node.setType(ApplicationHierarchyConstraints.NT_APPLICATION);
    node.setAllowsChildren(false);
    apps.add(node);

      // sort the application nodes in alphabetical order
      root.sortNodes(apps);

      for (int i=0; i<apps.size(); i++)
      {
         node = (ServerNode) apps.get(i);
         model.insertNodeInto(node, root, i);
      }

      root.setLoadedChildren(true);
   }

  /**
   * Creates the browser tab for datasource. 
   */
  public void createDatasourceTab()
  {
      String title = m_res.getString("DATASOURCES");
      CatalogEntry entry = new CatalogEntry();
      entry.setType(SQLHierarchyConstraints.NT_DATASOURCE);
      entry.setInternalName(title);
      ServerNode node = new ServerNode(entry);
      node.setRemovable(false);
      node.setConstraints(new SQLHierarchyConstraints());
      
      // Sort with repository first, followed by others asc case-insensitive
      Comparator<DefaultBrowserNode> comparator = 
         new Comparator<DefaultBrowserNode>() 
      {
         public int compare(DefaultBrowserNode node1, DefaultBrowserNode node2)
         {
            if (CatalogDatasources.isRepository(node1.getInternalName()))
               return -1;
            else if (CatalogDatasources.isRepository(node2.getInternalName()))
               return 1;
            else 
            {
               return m_stringComp.compare(node1.getDisplayName(), 
                  node2.getDisplayName());
            }
         }
         private PSStringComparator m_stringComp = new PSStringComparator(
            PSStringComparator.SORT_CASE_INSENSITIVE_ASC);   
      };

      BrowserTree bt = new BrowserTree(node);
      bt.setCellRenderer(new BrowserTreeNodeRenderer());
      JScrollPane jsp = new JScrollPane(bt);

      m_Pane.insertGroupedTab(title, null, jsp, m_res
         .getString("DATASOURCESTIP"), BrowserPaneTabData.DATASOURCES, 1);

      m_Pane.setGroupBackgroundAt(BrowserPaneTabData.DATASOURCES,
         Color.lightGray);
  }




   /**
    * The auto refresh function of the browser recatalogs each time a node is
    * expanded. If auto refresh is off, a catalog occurs only the first time the
    * node is expanded. Each successive expansion uses the previously catalogged
    * values.
    * 
    * @return <code>true</code> if the browser's auto refresh is enabled. By
    * default, it is enabled.
    */
   public boolean isAutoRefreshOn()
   {
      if ( null == m_UC )
         return true;

      String autoRefresh = m_UC.getValue(AUTO_REFRESH);
      if ( null == autoRefresh || autoRefresh.equals( AUTO_REFRESH_ON ))
         return true;

      return false;
   }


   public void setAutoRefresh(boolean bAutoRefresh)
   {
      if (m_UC == null)
         return;

      if(bAutoRefresh == true)
         m_UC.setValue(AUTO_REFRESH, AUTO_REFRESH_ON );
      else
         m_UC.setValue(AUTO_REFRESH, AUTO_REFRESH_OFF );
   }


   class BrowserWindowListener extends java.awt.event.WindowAdapter
   {
       @Override
      public void windowClosing( java.awt.event.WindowEvent e )
      {
//         System.out.println("In Browser Frame Window Listener ....");
         Object object = e.getSource( );
         if (object == BrowserFrame.this)
            BrowserFrame_WindowClosing( e );
      }
   }

   void BrowserFrame_WindowClosing( java.awt.event.WindowEvent e )
   {
      if (e == null);
      
//      System.out.println("In BrowserFrame_WindowClosing()");
      setVisible( false );    // hide the Frame
   }

  /**
   * Saves the browser window position to the user config. This method is called from UIMainFrame class,
   * when all other information is saved to the userconfig for the mainframe window.
   */
   public void saveBrowserWindowPosition()
   {
      int[] iaPos = new int[4]; // to save position of the browser to user config
                                          // save x( left) , y (top),  width, height
      Point p = this.getLocation();
      iaPos[0] = p.x;
      iaPos[1] = p.y;
      iaPos[2] = this.getWidth();
      iaPos[3] = this.getHeight();

      setUserConfigForBrowserWindowPos(iaPos);
   }

   public ResourceBundle getResources()
   {
      return m_res;
   }

  /**
   * Gets the application summaries from the server and returns true if the
    * application is running on the server. (Checks for the isActive property
    * on the returned enumeration.)
    *
    *@returns true if the application is running (has been started) on the
    * server.
    * @deprecated Is not expected to be used. Content of the method is moved
    * to PSXmlApplicationModel.
    */
   public static boolean isApplicationRunning(String appName)
   {
      throw new AssertionError("Is not expected to be called");
   }
   /**
    * Returns a sorted collection of the server exits UDFs categories
    * @param vc a vector of exits, can not be <CODE>null</CODE>
    * @return categories a collection of the exits' categories
    * never <code>null</code>, may be empty
    */
   @SuppressWarnings("unchecked")
   private Iterator getCategories( java.util.List<IPSExtensionDef> vc)
   {
      if(vc == null)
         throw new IllegalArgumentException("UDF collection can not be null");

      /*use treemap to get unique categories' types and to sort them in
      alphabetical order*/
      final TreeMap<String, String> categories =
            new TreeMap<String, String>(
                  new PSStringComparator(
                        PSStringComparator.SORT_CASE_INSENSITIVE_ASC));

       for (IPSExtensionDef exit : vc) {
           String category = exit.getRef().getCategory();

           if (category.trim().length() != 0)
               categories.put(category, category);

       }
      return categories.values().iterator();
   }

   // storage
    private static BrowserFrame ms_theBrowser = null;

   private BrowserPane m_Pane = null;

   private PSDesignerConnection m_Connection = null;

   private PSCataloger m_Cataloger = null;

   private PSObjectStore m_Store = null;

   private UserConfig m_UC = null;

   // keys to be used for saving the user config info
   /**
    * Key associated with value for Browser Schema, maintains the Version of the
    * Browser.
    */
    public static final String BROWSER_SCHEMA = "Br_BrowserSchema";

    public static final String AUTO_REFRESH = "Br_AutoRefresh";

   /*
    * Names used as values for the auto refresh flag when it is stored in the
    * user config.
    */
    private static final String AUTO_REFRESH_ON = "on";

    private static final String AUTO_REFRESH_OFF = "off";

   /**
    * The value associated with the Browser Schema, the Current Version of the
    * Browser. For Example V1.0
    */
    public static final String BROWSER_CURRENT_SCHEMA_VALUE = "V1.0";

   /**
    * Key associated with delimiter separated list of integers specifying the
    * position and size of the browser.
    * <p>
    * Format: <code>&ltleft&gt,&lttop&gt,&ltwidth&gt,&ltheight&gt</code>
    * <p>
    * Where <code>&ltleft&gt</code> and <code>&lttop&gt</code> are in screen
    * coords and <code>&ltwidth&gt</code> and <code>&ltheight&gt</code> are
    * in pixels.
    */
    public static final String BROWSER_WINDOW_POS = "Br_BrowserWindowPos";

    public static final String UC_KEY_DELIM = ":";

   public BrowserTree m_setupTree = null;

   private BrowserTree m_serverObjects = null;

   /**
    * A root node where all java pre exits will be inserted at. Gets initialized
    * in
    * 
    * @link{createServerObjectsTab()}
    */
   private DefaultBrowserNode m_pre = null;

   /**
    * A root node where all java post exits will be inserted at. Gets
    * initialized in
    * 
    * @link{createServerObjectsTab()}
    */
   private DefaultBrowserNode m_post = null;

   /**
    * A vector of all preExits nodes. Gets initialized in
    * 
    * @link{createCategoryNodes(DefaultBrowserNode, Vector, String,
    * DefaultTreeModel)}
    */
   private Vector m_nodePreExits = new Vector();

   /**
    * A vector of all postExits nodes. Gets initialized in
    * 
    * @link{createCategoryNodes(DefaultBrowserNode, Vector, String,
    * DefaultTreeModel)}
    */
   private Vector m_nodePostExits = new Vector();
}





