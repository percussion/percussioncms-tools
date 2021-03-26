/******************************************************************************
 *
 * [ SearchViewDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.FeatureSet;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSException;
import com.percussion.search.PSCommonSearchUtils;
import com.percussion.search.ui.PSFieldSelectionEditorDialog;
import com.percussion.search.ui.PSSearchAdvancedPanel;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.PSRemoteRequester;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
/**
 * Search/Views are used to edit and visualize a search cms layer object.
 */
public class SearchViewDialog extends PSDialog implements
   TreeSelectionListener, ActionListener, ChangeListener, IPSDbComponentUpdater
{
   /**
    * Constructs the display format dialog.
    *
    * @param parent Frame that contains this dialog. Assumed not 
    * <code>null</code>.
    * @param c collection of {@link PSSearch} objects to be viewed/modified by
    * this dialog, may not be <code>null</code>.
    * @param proxy processor proxy used to save and load objects used in this
    * dialog.
    * 
    * @throws PSAuthenticationFailedException if the user's session has expired.
    * @throws PSAuthorizationException if the user cannot access the server's
    * configuration to determine if an external search engine is available.
    * @throws PSServerException if there are any other errors retrieving the
    * server's configuration.
    */
   public SearchViewDialog(Frame parent, PSSearchCollection c,
      PSComponentProcessorProxy proxy) throws PSServerException, 
         PSAuthorizationException, PSAuthenticationFailedException
   {
      super(parent);

      if (c == null)
         throw new IllegalArgumentException(
            "collection of search objects must not be null");

      m_searches = c;
      init();

      // Save the proxy
      if (proxy != null)
         setProcessorProxy(proxy);

      // Save the searches object
      onUpdateData(m_searches, false, false);
   }

   /**
    * Calls same method on children.
    */
   public void onDataPersisted()
   {
      m_treePanel.onDataPersisted();
      m_editor.onDataPersisted();
   }

   /**
    * Initializes the dialog.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());

      setTitle(ms_res.getString("dlg.title"));
      getContentPane().add(createMainPanel());

      pack();
      center();
      setResizable(true);
   }

   // see interface for description
   public boolean onValidateData(IPSDbComponent comp, boolean isQuiet)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      Iterator listeners = m_listeners.iterator();

      while (listeners.hasNext())
      {
         IPSDbComponentUpdater updater =
               (IPSDbComponentUpdater) listeners.next();

         if (!updater.onValidateData(comp, isQuiet))
            return false;
      }
      return true;
   }

   // see interface for description
   public boolean onUpdateData(
      IPSDbComponent comp, boolean bDirection, boolean isQuiet)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp must not be null");

      // Disable the view from events
      m_bUpdating = true;

      try
      {
         Iterator listeners = m_listeners.iterator();

         while (listeners.hasNext())
         {
            IPSDbComponentUpdater updater = (IPSDbComponentUpdater)
               listeners.next();

            if (!updater.onUpdateData(comp, bDirection, isQuiet))
               return false;
         }
      }
      finally
      {
         m_bUpdating = false;
      }

      return true;
   }

   /**
    * Gets the last results performed by this dialog on the selected
    * cms object contained.
    *
    * @return results object. May be <code>null</code>
    */
   public PSSaveResults getLastResults()
   {
      return m_lastResults;
   }
   
   /**
    * Creates and initializes all components added to the
    * main content pane of this dialog
    *
    * @return panel containing all components. Never <code>null</code>
    */
   private JPanel createMainPanel()
   {
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());

      JPanel cmdPane = createCommandPanel();

      // 'this' is a tree selection listener
      m_treePanel = new SearchTreePanel(this);
      addDataListener((IPSDbComponentUpdater) m_treePanel);

      m_editor = new SearchViewEditorPanel(this, m_searches);
      addDataListener((IPSDbComponentUpdater) m_editor);

      m_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      m_split.setLeftComponent(m_treePanel);
      m_split.setRightComponent(m_editor);
      m_editor.setPreferredSize(new Dimension(400, 450));
      m_treePanel.setPreferredSize(new Dimension(300, 450));
      mainPane.setPreferredSize(new Dimension(800, 500));

      mainPane.add(m_split, BorderLayout.CENTER);
      mainPane.add(cmdPane, BorderLayout.SOUTH);
      return mainPane;
   }

   /**
    * Adds a component updater to the listener list to receive events.
    *
    * @param updater a object implementing {@link IPSDbComponentUpdater}
    *    Never <code>null</code>.
    */
   private void addDataListener(IPSDbComponentUpdater updater)
   {
      if (updater == null)
         throw new IllegalArgumentException(
            "updater must not be null");

      if (!m_listeners.contains(updater))
         m_listeners.add(updater);
   }

   /**
    * Initializes a command panel representing the buttons
    * ok, canel, apply, customize.
    *
    * @return command panel, never <code>null</code>
    */
   private JPanel createCommandPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      Border b = BorderFactory.createEmptyBorder(5, 10, 5, 10);
      panel.setBorder(BorderFactory.createEtchedBorder());
      // create "New Searches" button to enable configuring new search for each 
      //community.
      JButton newSearchesButton = 
            new UTFixedButton(ms_res.getString("newsearches"));
      newSearchesButton.setMnemonic(ms_res.getString("newsearches.mn").charAt(
            0));
      newSearchesButton.setDefaultCapable(true);
      newSearchesButton.setActionCommand("NewSearches");
      newSearchesButton.addActionListener(this);
      newSearchesButton.setPreferredSize(new Dimension(190, 24));
      
      JPanel searchPanel = new JPanel(new BorderLayout());
      searchPanel.setBorder(b);
      searchPanel.add(newSearchesButton, BorderLayout.WEST);
      
      
      // create ok button and handle all its actions
      m_okButton = new UTFixedButton(ms_res.getString("ok"));
      m_okButton.setDefaultCapable(true);
      m_okButton.setActionCommand("Ok");
      m_okButton.setMnemonic(ms_res.getString("ok.mn").charAt(0));
      m_okButton.addActionListener(this);
      m_okButton.setPreferredSize(BTN_SIZE);

      //create apply button and handle all its actions
      JButton applyButton = new UTFixedButton(ms_res.getString("apply"));
      applyButton.setDefaultCapable(true);
      applyButton.setActionCommand(ms_res.getString("apply"));
      applyButton.setMnemonic(ms_res.getString("apply.mn").charAt(0));
      applyButton.addActionListener(this);
      applyButton.setPreferredSize(BTN_SIZE);

      // create cancel button and handle all its actions
      JButton cancelButton = new UTFixedButton(ms_res.getString("cancel"));
      cancelButton.setActionCommand(ms_res.getString("cancel"));
      cancelButton.setMnemonic(ms_res.getString("cancel.mn").charAt(0));
      cancelButton.addActionListener(this);
      cancelButton.setPreferredSize(BTN_SIZE);

      // create help button and handle all its actions
      JButton helpButton = new UTFixedButton(ms_res.getString("help"));
      helpButton.setActionCommand(ms_res.getString("help"));
      helpButton.setMnemonic(ms_res.getString("help.mn").charAt(0));
      helpButton.addActionListener(this);
      helpButton.setPreferredSize(BTN_SIZE);

      // creating behavior that gives defaultButton focus back to the OK button
      // when focus leaves the CANCEL button.
      cancelButton.addFocusListener(new FocusAdapter()
      {
         public void focusLost(FocusEvent e)
         {
            getRootPane().setDefaultButton(m_okButton);
         }
      });

      // layout
      Box box = new Box(BoxLayout.X_AXIS);
      box.add(Box.createHorizontalGlue());
      box.add(Box.createHorizontalStrut(5));
      box.add(Box.createHorizontalStrut(5));
      box.add(m_okButton);
      box.add(Box.createHorizontalStrut(5));
      box.add(applyButton);
      box.add(Box.createHorizontalStrut(5));
      box.add(cancelButton);
      box.add(Box.createHorizontalStrut(5));
      box.add(helpButton);
      panel.setLayout(new BorderLayout());
      
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(b);
      cmdPanel.add(box, BorderLayout.EAST);
      
      panel.add(searchPanel, BorderLayout.WEST);
      panel.add(cmdPanel, BorderLayout.EAST);
      
      return panel;
   }

   // see ActionListener for details
   public void actionPerformed(ActionEvent e)
   {
      String strCmd = e.getActionCommand();

      try
      {
         Method m = getClass().getDeclaredMethod("on" + strCmd, null);
         m.invoke(this, null);
      }
      catch (Exception ignore)
      {
         ignore.printStackTrace(System.out);
      }
   }

   // see interface for description
   public void stateChanged(ChangeEvent e)
   {
      if (m_bUpdating)
         return;

      /**
       * No longer needed for tab changing events, although I can forsee
       * tab validation processing here @todo ...
       */
   }

   // see interface for description
   public void valueChanged(TreeSelectionEvent e)
   {
      if (m_bUpdating || m_restoringPreviousNode)
         return;

      TreePath prevPath = null;
      boolean mustRestore = false;
      try
      {
         TreePath newPath = e.getNewLeadSelectionPath();

         // might be expanding a tree:
         if(newPath == null)
            return;

         DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode)
               newPath.getLastPathComponent();
         if (nextNode == null)
            return;
         Object o = nextNode.getUserObject();
         PSSearch nextSearch = null;
         if (o instanceof PSSearch)
            nextSearch = (PSSearch)o;

         prevPath = e.getOldLeadSelectionPath();
         DefaultMutableTreeNode prevNode = null;
         PSSearch prevSearch = null;
         if (prevPath != null)
         {
            prevNode = (DefaultMutableTreeNode) prevPath.getLastPathComponent();
            o = prevNode.getUserObject();
            if (o instanceof PSSearch)
               prevSearch = (PSSearch)o;
         }

         //copy data from panels to 'previous' object
         if (prevSearch != null)
         {
            if (onValidateData(prevSearch, false))
            {
               onUpdateData(prevSearch, true, false);

               // update node:
               if(prevNode != null)
                  m_treePanel.refreshNode(prevNode);
            }
            else
            {
               /* prevent the node switch (it's too late to prevent, so we just
                  switch back) */
               mustRestore = true;
            }
         }

         //copy data from 'next' object to panels
         if (!mustRestore)
         {
            if (nextSearch != null)
               onUpdateData(nextSearch, false, false);
            else
               // we must be on a folder node
               m_editor.clearTabs();
         }
      }
      finally
      {
         /*We put this in a finally block so that in case any exceptions
            occur while validating, we won't get left in an odd state, ie.
            the new node is selected but the old search object data is still
            in the panels. Specifically, this happened when we got a missing
            resource exception.*/
         if (mustRestore && prevPath != null)
         {
            try
            {
               m_restoringPreviousNode = true;
               ((JTree) e.getSource()).setSelectionPath(prevPath);
            }
            finally
            {
               m_restoringPreviousNode = false;
            }
         }
      }
   }

   // event handling

   /**
    * event handler. Creates a new
    * {@link com.percussion.cms.objectstore.PSSearch}.
    */
   public void onNew()
   {
      boolean isView = true;
      try
      {
         PSSearch search = null;
         isView = (m_treePanel.isCustomViews() || m_treePanel.isStandardViews()) ? true : false;
           
         if (m_treePanel.isStandardViews() || // Web services view
             m_treePanel.isStandardSearches()) // Web services search
         {
            search = new PSSearch(getDefaultName(isView));
            //set the default expansion level for RW
            E2Designer workbench = E2Designer.getApp();
            if (null != workbench)
            {
               PSServerConfiguration cfg = workbench.getMainFrame()
                     .getObjectStore().getServerConfiguration();
               PSSearchConfig searchCfg = cfg.getSearchConfig();
               if (searchCfg.isFtsEnabled())
               {
                  String defaultExpansion = searchCfg.getCustomProp(
                        PSSearchConfig.SYNONYM_EXPANSION);
                  if (null != defaultExpansion 
                        && defaultExpansion.trim().length() > 0)
                  {
                     /* If the value is bogus, the UI will correct it, so we 
                      * don't need to validate it here.
                      */
                     search.setProperty(
                             PSCommonSearchUtils.PROP_SYNONYM_EXPANSION,
                           defaultExpansion);
                  }
               }
            }

            if(m_treePanel.isStandardViews())
               search.setType(PSSearch.TYPE_VIEW);
            else
               search.setType(PSSearch.TYPE_STANDARDSEARCH);
            
            // set both to use external search engine if enabled by default
            if (FeatureSet.isFTSearchEnabled())
               search.setProperty(PSSearch.PROP_SEARCH_ENGINE_TYPE, 
                  PSSearch.SEARCH_ENGINE_TYPE_EXTERNAL);
            else
               search.setProperty(PSSearch.PROP_SEARCH_ENGINE_TYPE, 
                  PSSearch.SEARCH_ENGINE_TYPE_INTERNAL);
         }
         else if (m_treePanel.isCustomViews() || // custom application based search
                  m_treePanel.isCustomSearches())// custom application based search
         {
            search = new PSSearch(getDefaultName(isView), true);
            search.setUrl(SAMPLE_URL);

            if(m_treePanel.isCustomViews())
               search.setType(PSSearch.TYPE_VIEW);
            else
               search.setType(PSSearch.TYPE_CUSTOMSEARCH);
            //Custom views and searches are not end user customizable
            search.setUserCustomizable(false);
         }
         else
         {
            ErrorDialogs.showErrorDialog(this,
               ms_res.getString("info.msg.selectnewnode"),
               ms_res.getString("info.title.selectnewnode"),
               JOptionPane.INFORMATION_MESSAGE);
            return;
         }
         //validate the temporary name
         while (m_searches.contains(search))
            search.setInternalName(getDefaultName(isView));

         //reset the display name to match
         String iname = search.getInternalName();
         search.setDisplayName(Character.toUpperCase(iname.charAt(0))
            + iname.substring(1, iname.length()));

         // add the search to the component collection
         m_searches.add(search);

         // Update the listeners, our data has changed
         onUpdateData(m_searches, false, false);

         // Set this object selected
         m_treePanel.setSelectedObject(search);
      }
      catch (PSException e)
      {
         //there are 4 exceptions being caught, all with the same message
         ErrorDialogs.showErrorDialog(this,
            e.getLocalizedMessage(),
            ms_res.getString("error.title.newfailure"),
            JOptionPane.ERROR_MESSAGE);
      }
   }

   /**
    * Builds a name suitable for use as the internal name of a search. The
    * name has a numeric suffix that is incremented each time this method is
    * called within the lifetime of this class.
    *
    * @param isView  set to <code>true</code> if the node is a View 
    *                 <code>false</code> if it is a Search.
    * @return Never <code>null</code> or empty. Contains no whitespace.
    */
   private String getDefaultName(boolean isView)
   {
       if (isView)
           return "view" + ms_nameSuffix++;
       else return "search" + ms_nameSuffix++;
   }

   /**
    * event handler. Deletes the selected
    * {@link com.percussion.cms.objectstore.PSSearch} object.
    */
   public void onDelete()
   {
      PSSearch search = m_treePanel.getSelectedObject();

      if (search == null)
      {
         ErrorDialogs.showErrorDialog(this,
            ms_res.getString("info.msg.selectdeletenode"),
            ms_res.getString("info.title.selectdeletenode"),
            JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      // Prompt with the are you sure you want to delete mesage
      int option = JOptionPane.showConfirmDialog(this,
            MessageFormat.format( ms_res.getString("info.msg.deletenode"),
               new String[] {search.getDisplayName()} ),
                  ms_res.getString("info.title.deletenode"),
                     JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
          if(option == JOptionPane.NO_OPTION)
          {
             return;
          }

      if (!m_searches.remove(search))
      {
         ErrorDialogs.showErrorDialog(this,
            ms_res.getString("error.removingsearch.title"),
            ms_res.getString("error.removingsearch.msg"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }

      onUpdateData(m_searches, false, false);

      if (m_searches.size() < 1)
      {
         // Select the root node of the tree
         m_treePanel.setSelectedObject(m_treePanel.getRoot().getUserObject());
      }
      else
      {
         // Set the first search selected - this is an arbitrary
         // decision feel free to modify what happens after delete
         // as one pleases.
         Iterator searchIter = m_searches.iterator();

         if (searchIter.hasNext())
         {
            // Set this object selected
            m_treePanel.setSelectedObject((PSSearch) searchIter.next());
         }
      }
   }

   // see base class
   public void onApply()
   {
      if(save())
      {
         onDataPersisted();
         m_treePanel.setupTreeModel(m_searches);

      }
   }

   /**
    * Saves the data for all modified searches to the database. The data from
    * the currently active panels is transferred to the object before the
    * db update is performed. If any problems occur, a message is displayed to
    * the user.
    * <p>The searches are validated for proper default-new-search 
    * configurations and if they fail, the configuration dialog is launched.
    *
    * @return <code>true</code> if the data is successfully transferred from
    *    the panels to the object and into the db. <code>false</code> if any
    *    validation errors occur or a db error occurs.
    */
   private boolean save()
   {
      try
      {
         PSSearch search = m_treePanel.getSelectedObject();

         // update the one currently being edited
         if (search != null && !onUpdateData(search, true, false))
            return false;

         if (!validateCommunitySearchConfig(m_searches))
         {
            JOptionPane.showMessageDialog(this, ms_res
                  .getString("error.msg.defaultsmisconfigured"), ms_res
                  .getString("error.title.defaultsmisconfigured"),
                  JOptionPane.WARNING_MESSAGE);
            onNewSearches();
            return false;
         }
         m_lastResults = getProcessorProxy().save(
               new IPSDbComponent [] {m_searches});
         return true;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();

         ErrorDialogs.showErrorDialog(this,
            ex.getLocalizedMessage(),
            E2Designer.getResources().getString("ExceptionTitle"),
            JOptionPane.ERROR_MESSAGE);
         return false;
      }
   }

   /**
    * Checks that the cxNewSearch and aadNewSearch properties are set as 
    * follows:
    * <ul>
    *    <li>exactly 1 search visible to all communities has the cxNewSearch 
    *    property set</li>
    *    <li>exactly 1 search visible to all communities has the aadNewSearch 
    *    property set</li>
    *    <li>at most, 1 search has the cxNewSearch property set for any given 
    *    community</li>
    *    <li>at most, 1 search has the aadNewSearch property set for any given 
    *    community</li>
    * </ul>
    * <p>If anything is mis-configured, a detailed report is shown to the user
    * and <code>false</code> is returned. The caller may wish to launch the
    * configuration dialog in that case.
    * 
    * @param searches Assumed not <code>null</code>.
    * @return <code>true</code> if all searches are properly configured 
    * regarding the cxNewSearch and aadNewSearch properties, <code>false</code>
    * otherwise.
    */
   private static boolean validateCommunitySearchConfig(
         PSSearchCollection searches) 
   {
      Iterator searchIter = searches.iterator();
      Set cxSearches = new HashSet();
      Set rcSearches = new HashSet();
      boolean validationSuccess = true;
      while (searchIter.hasNext() && validationSuccess)
      {
         PSSearch search = (PSSearch) searchIter.next();
         String[] communities = search.getCXNewSearchCommunities();
         for (int i = 0; i < communities.length; i++) 
         {
            if (cxSearches.contains(communities[i]))
            {
               validationSuccess = false;
               break;
            }
            else
               cxSearches.add(communities[i]);
         }

         communities = search.getAADNewSearchCommunities();
         for (int i = 0; i < communities.length; i++) 
         {
            if (rcSearches.contains(communities[i]))
            {
               validationSuccess = false;
               break;
            }
            else
               rcSearches.add(communities[i]);
         }
      }
      if (!(cxSearches.contains(PSSearch.PROP_COMMUNITY_ALL) 
            && rcSearches.contains(PSSearch.PROP_COMMUNITY_ALL)))
      {
         validationSuccess = false;
      }
      return validationSuccess;
   }

   /**
    * Open Configure New Searches for Communities dialog box 
    */
   public void onNewSearches()
   {
      PSSearch search = m_treePanel.getSelectedObject();

      // update the one currently being edited
      if (search != null && !onUpdateData(search, true, false))
         return;

      PSConfigureCommunityNewSearchesDialog dlg = null;
      dlg = new PSConfigureCommunityNewSearchesDialog(this, m_searches);
      dlg.setVisible(true);
      // Update the panels
      if (search != null)
         onUpdateData(search, false, false);
   }

   /**
    * Overriding the same method in {@link PSDialog}.
    */
   public void onOk()
   {
      // Perform any necessary saving
      if (!save())
         return;

      // base class behavior
      super.onOk();
   }

   public void onCancel()
   {
      boolean changed = m_searches.isModified();
      PSSearch search = m_treePanel.getSelectedObject();

      // update the one currently being edited
      if (!changed && search != null)
      {
         /* Has the current one been changed? It must be done this way because
            we don't want to force the user to correctly fill in fields if
            they are really cancelling. */
         if (onValidateData(search, true))
         {
            onUpdateData(search, true, true);
            changed = changed || search.isModified();
         }
         else
            //if it fails validation, they must have changed something
            changed = true;
      }

      if (changed)
      {
         int choice = JOptionPane.showConfirmDialog(this,
               ms_res.getString("error.msg.unsaveddata"),
               ms_res.getString("error.title.unsaveddata"),
               JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
         if (choice == JOptionPane.YES_OPTION)
         {
            onOk();
            return;
         }
         else if (choice == JOptionPane.CANCEL_OPTION)
            return;
      }
      super.onCancel();
   }

   /**
    * Handles the event when the customize button is clicked.  Saves the current
    * ui data in the search object and then displays the search field selection
    * editor dialog.  When that dialog returns, update the ui data displayed
    * appropriately.
    */
   public void onCustomize()
   {
      PSContentEditorFieldCataloger fieldCatlgObj = getCEFieldCatalog();
      if (fieldCatlgObj == null)
         return;

      PSSearch search = m_treePanel.getSelectedObject();

      // save the data in the panel to the object
      onUpdateData(search, true, false);

      if (search == null)
      {
         ErrorDialogs.showErrorDialog(this,
            ms_res.getString("error.msg.invalidtreeselection"),
            ms_res.getString("error.title.invalidtreeselection"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }

      PSFieldSelectionEditorDialog dlg =
         new PSFieldSelectionEditorDialog(this, search, fieldCatlgObj);
      dlg.setUseExternalSearchEngine(FeatureSet.isFTSearchEnabled() && 
         search.useExternalSearch());
      dlg.setVisible(true);

      // Update the panels
      onUpdateData(search, false, false);
   }

   public void onHelp()
   {
      super.onHelp();
   }

   //end event handling

   /**
    * Get the field catalog from the server.  This catalog is retrieved from the
    * server on the first call, and cached after that.
    *
    * @return The field catalog, includes hidden fields.  An error dialog is
    * displayed if it cannot be loaded and <code>null</code> is returned.
    */
   public PSContentEditorFieldCataloger getCEFieldCatalog()
   {

      if (m_fieldCat == null)
      {
         PSRemoteRequester appReq = new PSRemoteRequester(
            E2Designer.getLoginProperties());

         try
         {
            PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);
            m_fieldCat = new PSContentEditorFieldCataloger(remCatlg, null, 
               IPSFieldCataloger.FLAG_INCLUDE_HIDDEN |
               IPSFieldCataloger.FLAG_CTYPE_EXCLUDE_HIDDENFROMMENU);
         }
         catch (PSCmsException cmsEx)
         {
            ErrorDialogs.showErrorDialog(this,
               ms_res.getString(cmsEx.getLocalizedMessage()),
               ms_res.getString("error.unexpected"),
               JOptionPane.ERROR_MESSAGE);
         }
      }

      return m_fieldCat;
   }

   /**
    * Gets the processor proxy used be this dialog. Intended to be
    * set by a caller as this dialog may be used in different contexts.
    * May be overriden by base classes to provide a particular instance of
    * a given proxy processor.
    *
    * @return the currently set proxy processor.
    */
   protected PSComponentProcessorProxy getProcessorProxy()
   {
      if (m_proxyProcessor == null)
         throw new UnsupportedOperationException(
            "no processor is set saving and updating are unsupported");

      return m_proxyProcessor;
   }

   /**
    * Sets the processor proxy used be this dialog. Intended to be
    * set by a caller as this dialog may be used in different contexts.
    *
    * @param proxy a processor. Never <code>null</code>
    */
   public void setProcessorProxy(PSComponentProcessorProxy proxy)
   {
      if (proxy == null)
         throw new IllegalArgumentException(
            "processor proxy must not be null");

      m_proxyProcessor = proxy;
   }

   /**
    * Appends a string that corresponds to the active tab type.
    * @param helpId acts as the root of a unique help key. May be
    * <code>null</code>.
    * @return the unique help key string
    */
   protected String subclassHelpId( String helpId )
   {
      short type = m_editor.getEnabledTabPanelType();
      String[] tabs = {};
      if(type == SearchViewEditorPanel.STANDARD_VIEW_TAB_PANEL)
      {
         // NOTE: The order of strings in this array must match the actual tab
         // order
         String[] newTabs = {"General","Communities","Query","Properties"};
         tabs = newTabs;
      }
      else if(type == SearchViewEditorPanel.CUSTOM_VIEW_TAB_PANEL)
      {
         // NOTE: The order of strings in this array must match the actual tab
         // order
         String[] newTabs = {"General","Communities","URL","Properties"};
         tabs = newTabs;
      }
      else if(type == SearchViewEditorPanel.STANDARD_SEARCH_TAB_PANEL)
      {
         // NOTE: The order of strings in this array must match the actual tab
         // order
         String[] newTabs = {"General","Communities","Search Query",
               "Properties"};
         tabs = newTabs;
      }
      else if(type == SearchViewEditorPanel.CUSTOM_SEARCH_TAB_PANEL)
      {
         // NOTE: The order of strings in this array must match the actual tab
         // order
         String[] newTabs = {"General","Communities","Search_Query",
               "Properties"};
         tabs = newTabs;
      }

      int selected = m_editor.getSelectedTab();
      if(null != helpId && type != 0)
         helpId += "_"+tabs[selected];

      return helpId ;

   }

   /**
    * Returns a <code>PSSearchCollection</code> <code>Iterator</code>
    * of the <code>PSSearchCollection</code> used to create this dialog.
    *
    * @return see description.
    */
   Iterator getSearchCollection()
   {
      return m_searches.iterator();
   }

   /**
    * Returns the <code>SearchViewEditor</code> Panel, never <code>null</code>.
    */
    public SearchViewEditorPanel getEditorPanel()
    {
      return m_editor;
    }
   /**
    * Panel containing search/view tree. Initialized in the {@link #init()},
    * never <code>null</code>.
    */
   private SearchTreePanel m_treePanel;

   /**
    * Panel containing search/view editor components.
    * Initialized in the {@link #init()}, never <code>null</code>.
    */
   private SearchViewEditorPanel m_editor;

   /**
    * Split pane containing the left and right panel. The left panel shows the
    * tree and the right panel displays node sensitive panel. Initialized in the
    * {@link #init()}, never <code>null</code> or modified after that.
    */
   private JSplitPane m_split;

   /**
    * Resource bundle for this class. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   /**
    * the OK button
    */
   private JButton m_okButton = null;

   /**
    * Constant for the size of buttons displayed by this dialog.
    */
   public static final Dimension BTN_SIZE = new Dimension(95, 24);

   // Listeners of component updates. Initialized in definition,
   // contains list of objects implementing {@link #IPSDbComponentUpdater}.
   private Collection m_listeners = new ArrayList();

   /**
    * 'Lock' type resource around {@link #onUpdateData} so that
    * if we are performing an update we block other events.
    * Set and unset in {@link #onUpdateData}. Defaults to <code>false</code>
    */
   private boolean m_bUpdating = false;

   /**
    * While changing nodes, we may need to 'prevent' the change (because the
    * data in the current node is not valid and our rule says we cannot leave
    * a node until it passes validation). During processing of the valueChanged
    * method, we need to disable this method when we reset the path back to
    * its original value. Defaults to <code>false</code>.
    */
   private boolean m_restoringPreviousNode = false;

   /**
    * Saves the last processor operation. A caller may wish to
    * read this object using {@link #getLastResults()}. May be 
    * <code>null</code>.
    */
   private PSSaveResults m_lastResults = null;

   /**
    * Processor proxy used to save and load objects used in this dialog.
    * Set externally so as not to tie this dialog to a specific context.
    * May be <code>null</code> to show dialog 'disconnected', in this case,
    * saving and loading are unsupported.
    */
   private PSComponentProcessorProxy m_proxyProcessor = null;

   /**
    * Collection of db components. Passed in ctor, never <code>null</code>
    */
   private PSSearchCollection m_searches = null;

   /**
    * The field catalog used by this dialog, <code>null</code> until first
    * successful call to {@link #getCEFieldCatalog()}, may be <code>null</code>
    * after that if there is an error trying to load the catalog.
    */
   private PSContentEditorFieldCataloger m_fieldCat = null;

   /**
    * Used to make internal names unique. Use the value, then increment it.
    * Defaults to 1.
    */
   private static int ms_nameSuffix = 1;

   /**
    * Sample URL for custom search types which require a URL
    */
   private static final String SAMPLE_URL = "../sys_cxViews/inbox.xml";
}
