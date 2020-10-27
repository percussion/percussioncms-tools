/******************************************************************************
 *
 * [ ProviderUrlSelectorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.security.PSJndiUtils;
import com.percussion.util.PSLineBreaker;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import org.apache.log4j.Logger;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A dialog used to select provider URL's from directory servers.
 */
public class ProviderUrlSelectorDialog extends PSDialog
{
   
   /**
    * The class logger.
    */
   private final static Logger ms_log = Logger.getLogger(ProviderUrlSelectorDialog.class);

   
   /**
    * Create a new provider URL selector editor.
    * 
    * @param parent the parent frame, may be <code>null</code>.
    * @param providerUrl the provider url string, may be <code>null</code>.
    * @param authentication the authentication to be selected during 
    *    initialization, may be <code>null</code> or empty, in which case 
    *    nothing is selected.
    * @param data the known directory service data at the current time,
    *    not <code>null</code>.
    */
   public ProviderUrlSelectorDialog(Frame parent, String providerUrl, 
      String authentication, DirectoryServiceData data)
   {
      super(parent);
      setTitle(getResources().getString("dlg.title"));
      
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");
      m_data = data;
      
      initDialog(authentication);
      initData(providerUrl);
   }
   
   /**
    * Initializes the dialog from the supplied data.
    * 
    * @param providerUrl the provider URL to initialize from, may be 
    *    <code>null</code> or empty.
    */
   private void initData(String providerUrl)
   {
      if (providerUrl != null && providerUrl.trim().length() > 0)
      {
         try
         {
            /*
             * Creating a URL using the LDAP protocol does not work with 
             * applets. We just need this URL to parse its details. Thats
             * why we just replace the protocol to do the pasing.
             */
            String localUrl = providerUrl.toLowerCase();
            if (localUrl.startsWith("ldaps"))
            {
               m_protocol = "ldaps";
               localUrl = localUrl.replaceFirst(m_protocol, "https");
            }
            else if (localUrl.startsWith("ldap"))
            {
               m_protocol = "ldap";
               localUrl = localUrl.replaceFirst(m_protocol, "http");
            }

            URL url = new URL(localUrl);
            
            m_host.setText(url.getHost());
            m_port.setText(Integer.toString(url.getPort()));
            
            String file = url.getFile();
            if (file.startsWith("/"))
               file = file.substring(1);
               
            if (file.length() == 0)
            {
               if (onFetch(true))
                  onCatalog();
            }
            else
            {
               m_baseDn.addItem(file);
               onCatalog();
            }
         }
         catch (MalformedURLException e)
         {
            Object[] args = 
            {
               e.getLocalizedMessage()
            };
            String message = MessageFormat.format(
               getResources().getString("error.msg.malformedprovider"), args);
            
            JOptionPane.showMessageDialog(getParent(), 
               PSLineBreaker.wrapString(message, 80, 30, null), 
               getResources().getString("error.title"), 
               JOptionPane.ERROR_MESSAGE);
         }
      }
      else
      {
         // set defaults
         m_protocol = "ldap";
         m_host.setText("localhost");
         m_port.setText("389");
      }
   }
   
   /**
    * Get the provider url for the selected tree object.
    * 
    * @return the complete provider url for the selected tree object, never
    *    <code>null</code> or empty.
    */
   public String getProviderUrl()
   {
      String providerUrl = getProviderUrl(false);
      
      TreePath path = m_tree.getSelectionPath();
      for (int i=path.getPathCount()-1; i>=0; i--)
      {
         providerUrl += path.getPathComponent(i).toString();
         if (i>0)
            providerUrl += ",";
      }
         
      return providerUrl;
   }
   
   /**
    * Get the selected provider url.
    * 
    * @param includeBaseDn <code>true</code> to include the base DN in the 
    *    returned url, <code>false</code> otherwise.
    * @return the selected provider URL, never <code>null</code> or empty.
    */
   private String getProviderUrl(boolean includeBaseDn)
   {
      String providerUrl = "/";
      
      try
      {
         if (includeBaseDn)
            providerUrl += (String) m_baseDn.getSelectedItem();

         URI uri = new URI(m_protocol, null, m_host.getText(), 
            Integer.parseInt(m_port.getText()), providerUrl, null, null);
         
         providerUrl = uri.toASCIIString();
      }
      catch (URISyntaxException e)
      {
         // this should never happen
         throw new RuntimeException("Unexpected URI Syntax error.", e);
      }
         
      return providerUrl;
   }
   
   /**
    * Overrides super class to validate that all required elements are 
    * available and a tree selection is made.
    */
   @Override
   public void onOk()
   {
      if (!activateValidation())
         return;
         
      if (m_tree.getSelectionCount() != 1)
      {
         JOptionPane.showMessageDialog(getParent(), 
            PSLineBreaker.wrapString(
               getResources().getString("error.msg.noselection"), 80, 30, null), 
            getResources().getString("error.title"), JOptionPane.ERROR_MESSAGE);
            
         return;
      }
      
      super.onOk();
   }
   
   /**
    * Initializes the dialogs UI.
    * 
    * @param authentication the authentication to be selected during 
    *    initialization, may be <code>null</code> or empty, in which case 
    *    nothing is selected.
    */
   private void initDialog(String authentication)
   {
      JPanel panel = new JPanel(new BorderLayout(20, 10));
      panel.setBorder((new EmptyBorder (5, 5, 5, 5)));
      getContentPane().add(panel);

      panel.add(createPropertyPanel(authentication), BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true), 
         BorderLayout.EAST);
      panel.add(bottomPanel, BorderLayout.SOUTH);

      setResizable(true);
      pack();
      center();
   }
   
   /**
    * Create the property panel.
    * 
    * @param authentication the authentication to be selected during 
    *    initialization, may be <code>null</code> or empty, in which case 
    *    nothing is selected.
    * @return the new created property panel, never <code>null</code>.
    */
   private JPanel createPropertyPanel(String authentication)
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(createGeneralPropertiesPanel(authentication));
      panel.add(Box.createVerticalStrut(10));
      panel.add(Box.createVerticalGlue());
      m_treePanel = createTreePanel();
      panel.add(m_treePanel);

      return panel;
   }
   
   /**
    * Create the general properties panel.
    * 
    * @param authentication the authentication to be selected during 
    *    initialization, may be <code>null</code> or empty, in which case 
    *    nothing is selected.
    * @return the new created general property panel, never <code>null</code>.
    */
   private JPanel createGeneralPropertiesPanel(String authentication)
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_host.setToolTipText(getResources().getString("ctrl.host.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.host"),
         new JComponent[] { m_host });
         
      m_port.setToolTipText(getResources().getString("ctrl.port.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.port"),
         new JComponent[] { m_port });

      Iterator authentications = m_data.getAuthentications().iterator();
      while (authentications.hasNext())
         m_authentication.addItem(
            ((PSAuthentication) authentications.next()).getName());
      m_authentication.setToolTipText(
         getResources().getString("ctrl.authentication.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.authentication"),
         new JComponent[] { m_authentication });
      
      if (authentication != null)
         m_authentication.setSelectedItem(authentication);
        
      m_baseDn.setEditable(true); 
      m_baseDn.setToolTipText(getResources().getString("ctrl.basedn.tip"));
      panel.addPropertyRow(getResources().getString("ctrl.basedn"),
         new JComponent[] { m_baseDn });
         
      m_fetchBaseDnButton = new UTFixedButton(
         getResources().getString("ctrl.fetchbasedn.button"));
      m_fetchBaseDnButton.setToolTipText(
         getResources().getString("ctrl.fetchbasedn.tip"));
      m_fetchBaseDnButton.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent event)
         {
            if (!onFetch(false))
               m_baseDn.requestFocusInWindow();            
         }
      });
      panel.addPropertyRow(getResources().getString("ctrl.fetchbasedn"), 
         new JComponent[] { m_fetchBaseDnButton });
         
      m_catalogButton = new UTFixedButton(
         getResources().getString("ctrl.catalog.button"));
      m_catalogButton.setToolTipText(
         getResources().getString("ctrl.catalog.tip"));
      m_catalogButton.addActionListener(new ActionListener()
      {
         @SuppressWarnings("unused")
         public void actionPerformed(ActionEvent event)
         {
            onCatalog();
         }
      });
      panel.addPropertyRow(getResources().getString("ctrl.catalog"), 
         new JComponent[] { m_catalogButton });
         
      return panel;
   }

   /**
    * Create the tree panel.
    * 
    * @return the new created tree panel, never <code>null</code>.
    */
   private JPanel createTreePanel()
   {
      JPanel panel = new JPanel(new FlowLayout());
      
      DirectoryObjectNode root = new DirectoryObjectNode("root");
      m_tree = new DirectoryTree(new DefaultTreeModel(root));

      panel.add(createScrollPane(m_tree));
      
      return panel;
   }
   
   /**
    * Creates a new scroll pane for the supplied component.
    * 
    * @param component the ccomponent to create the scroll pane for, assumed
    *    not <code>null</code>.
    * @return the new created scroll pane, never <code>null</code>.
    */
   private JScrollPane createScrollPane(Component component)
   {
      JScrollPane scroll = new JScrollPane(component);
      scroll.setPreferredSize(new Dimension(400, 200));
      scroll.setMaximumSize(new Dimension(2000, 2000));
      
      return scroll;
   }
   
   /**
    * Validates that all required parameters are supplied, then fetches the
    * base DN's from the specified server. Displays an error if the fetch
    * operation failed.
    * 
    * @param ignoreErrors <code>true</code> to ignore errors while fetching the
    *    base DN's, <code>false</code> to display the error to the user.
    * @return <code>true</code> if the operation was successful, 
    *    <code>false</code> otherwise.
    */
   private boolean onFetch(boolean ignoreErrors)
   {
      initValidationFramework(false);
      
      if (!activateValidation())
         return false;
        
      NamingEnumeration results = null;
      NamingEnumeration attributes = null;
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         
         // close context first before creating a new one
         if (m_context != null)
         {
            m_context.close();
            m_context = null;
         }
         
         m_context = createContext(false);
         
         SearchControls searchControls = new SearchControls();
         searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
         
         m_baseDn.removeAllItems(); 
         
         Attributes attrs = m_context.getAttributes("", new String[]{"namingContexts"}); 
         Attribute attr = attrs.get( "namingContexts" );
       
         if (attr != null)
         {
         {
         // this will happen if not supported
            String message = getResources().getString(
               "error.msg.noNamingCtxAttr");               
            JOptionPane.showMessageDialog(getParent(), 
               PSLineBreaker.wrapString(message, 80, 30, null), 
               getResources().getString("error.title"), 
               JOptionPane.ERROR_MESSAGE);
            
            return false;
         }
            
         }
         m_baseDn.addItem(attr.toString());
         
         return true;
      }
      catch (NamingException e)
      {
         ms_log.error("Error fetching base DN's from LDAP server to Development client : ",e);
         if (!ignoreErrors)
         {
            Object[] args = 
            {
               e.getRootCause().getLocalizedMessage()
            };
            String message = MessageFormat.format(
               getResources().getString("error.msg.fetch"), args);
               
            JOptionPane.showMessageDialog(getParent(), 
               PSLineBreaker.wrapString(message, 80, 30, null), 
               getResources().getString("error.title"), 
               JOptionPane.ERROR_MESSAGE);
         }
      }
      finally
      {
         if (attributes != null)
            try {attributes.close();} catch (NamingException e) {}
         
         if (results != null)
            try {results.close();} catch (NamingException e) {}
            
         setCursor(Cursor.getDefaultCursor());
      }
      
      return false;
   }
   
   /**
    * Validates that all required parameters are supplied, then catalogs all 
    * objects from the specified base DN. Displays an error if a required 
    * parameter is not available or if cataloging fails, initializes the object 
    * tree up to the first level otherwise.
    */
   private void onCatalog()
   {
      initValidationFramework(true);
      
      if (!activateValidation())
         return;
         
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

         // close context first before creating a new one
         if (m_context != null)
         {
            m_context.close();
            m_context = null;
         }
         
         m_context = createContext(true);
         initTree();
      }
      catch (AuthenticationException ae)
      {
         String message = getResources().getString("error.msg.badauthentication");
            
         JOptionPane.showMessageDialog(getParent(), 
            PSLineBreaker.wrapString(message, 80, 30, null), 
            getResources().getString("error.title"), 
            JOptionPane.ERROR_MESSAGE);        
      }
      catch (AuthenticationNotSupportedException ans)
      {
         Object[] args = 
         {
               ans.getLocalizedMessage()
         };
         String message = MessageFormat.format(
            getResources().getString("error.msg.authenticationnotsupported"), args);
            
         JOptionPane.showMessageDialog(getParent(), 
            PSLineBreaker.wrapString(message, 80, 30, null), 
            getResources().getString("error.title"), 
            JOptionPane.ERROR_MESSAGE);        
      }
      catch (NamingException e)
      {
         ms_log.error("Error cataloging objects from LDAP server to Development client : ",e);
         Object[] args = 
         {
            e.getRootCause().getLocalizedMessage()
         };
         String message = MessageFormat.format(
            getResources().getString("error.msg.catalog"), args);
            
         JOptionPane.showMessageDialog(getParent(), 
            PSLineBreaker.wrapString(message, 80, 30, null), 
            getResources().getString("error.title"), 
            JOptionPane.ERROR_MESSAGE);
      }
      finally
      {
         setCursor(Cursor.getDefaultCursor());
      }
   }
   
   /**
    * Initializes the directory object tree for the current context. Assumes
    * that the directory context is initialized.
    * 
    * @throws NamingException for any naming or directory errors.
    */
   private void initTree() throws NamingException
   {
      DirectoryObjectNode root = new DirectoryObjectNode(
         m_baseDn.getSelectedItem().toString());
         
      m_treePanel.removeAll();
      m_tree = new DirectoryTree(new DefaultTreeModel(root));
      m_treePanel.add(createScrollPane(m_tree));
      
      buildTree(root, m_context.listBindings(""));
   }
   
   /**
    * Builds the directory object tree up to teh first level.
    * 
    * @param root the tree root node, assumed not <code>null</code>.
    * @param bindings an enumeration with all directory objects to be added as
    *    children to the supplied root, assumed not <code>null</code>, may be 
    *    empty.  The enumeration will be closed by this method.
    * @throws NamingException for any naming or directory errors.
    */
   private void buildTree(DirectoryObjectNode root, 
      NamingEnumeration bindings) throws NamingException
   {
      try
      {
         while (bindings.hasMoreElements())
         {
            Binding binding = (Binding) bindings.nextElement();
            DirectoryObjectNode node = 
               new DirectoryObjectNode(binding.getName());
            insertNode(root, node);
         }
      }
      finally
      {
         bindings.close();
      }

      m_tree.setSelectionRow(0);
      m_tree.requestFocus();
      m_tree.expandPath(new TreePath(root.getPath()));
      
      m_treePanel.revalidate();
      m_treePanel.repaint();
   }

   /**
    * Inserts a new node keeping alphabetical order.
    * 
    * @param parent a parent node at which the child node will be inserted,
    *    assumed not <code>null</code>.
    * @param child the child nod to be inserted into the parent node,
    *    assumed not <code>null</code>
    */
   private void insertNode(DirectoryObjectNode parent,
      DirectoryObjectNode child)
   {
      DefaultTreeModel model = (DefaultTreeModel) m_tree.getModel();
      int iCount = model.getChildCount(parent);
      String childName = child.getUserObject().toString();

      Collator c = Collator.getInstance();
      c.setStrength(Collator.SECONDARY);
      int i = 0;
      if (iCount > 0)
      {
         for (i = 0; i < iCount; i++)
         {
            DirectoryObjectNode tempNode =
               (DirectoryObjectNode) parent.getChildAt(i);
            String tempNodeName = tempNode.getUserObject().toString();
            if (c.compare(childName, tempNodeName) < 0)
               break;
         }
      }
      if (i >= iCount)
         model.insertNodeInto(child, parent, iCount);
      else
         model.insertNodeInto(child, parent, i);
   }
   
   /**
    * Creates a new context for the supplied provider url.
    * 
    * @param includeBaseDn a flag to specify whether or not to include the
    *    base DN in the provider url used for the context environment.
    * @return a new directory context, never <code>null<code>. The caller is
    *    responsible to close it.
    * @throws NamingException if anything goes wrong creating the new context.
    */
   @SuppressWarnings("unchecked")
   private DirContext createContext(boolean includeBaseDn) 
      throws NamingException
   {
      PSAuthentication auth = m_data.getAuthentication(
         m_authentication.getSelectedItem().toString());

      Hashtable env = new Hashtable();

      // need to set the context factory (provided by user)
      env.put(Context.INITIAL_CONTEXT_FACTORY, PSDirectory.FACTORY_LDAP);
      String url = getProviderUrl(includeBaseDn);
      if(m_protocol.equals("ldaps"))
      {
         env.put(Context.SECURITY_PROTOCOL, "ssl");
         url.replace("ldaps", "ldap");
      }

      env.put(Context.PROVIDER_URL, url);
      PSJndiUtils.addConnectionPooling(env);
      // Authenticate with the specified uid/pw
      env.put(Context.SECURITY_AUTHENTICATION, auth.getScheme());
      env.put(Context.SECURITY_PRINCIPAL, auth.getPrincipal(
         getProviderUrl(auth.shouldAppendBaseDn())));
      env.put(Context.SECURITY_CREDENTIALS, auth.getCredentials());

      return new InitialDirContext(env);
   }
   
   /**
    * Initialize the validation framework for this dialog.
    * 
    * @param catalog <code>true</code> to initialize the validation framework
    *    for a cataloging operation, <code>false</code> for all other 
    *    operations.
    */
   @SuppressWarnings("unchecked")
   private void initValidationFramework(boolean catalog)
   {
      List comps = new ArrayList();
      List validations = new ArrayList();
      StringConstraint nonEmpty = new StringConstraint();

      // host: cannot be empty
      comps.add(m_host);
      validations.add(nonEmpty);

      // port: cannot be empty
      comps.add(m_port);
      validations.add(nonEmpty);

      // authentication: cannot be empty
      comps.add(m_authentication);
      validations.add(nonEmpty);
      
      if (catalog)
      {
         // baseDn: cannot be empty
         comps.add(m_baseDn);
         validations.add(nonEmpty);
      }

      Component[] components = new Component[comps.size()];
      comps.toArray(components);
      
      ValidationConstraint[] constraints = 
         new ValidationConstraint[validations.size()];
      validations.toArray(constraints);
      
      setValidationFramework(components, constraints);
   }
   
   /**
    * The directory tree node.
    */
   private class DirectoryObjectNode extends DefaultMutableTreeNode
   {
      /**
       * Creates a new node for the supplied name.
       * 
       * @param name the node name, assumed not <code>null</code>.
       */
      public DirectoryObjectNode(String name)
      {
         super(name);
      }
      
      /**
       * Overridden, assumes that this is not a leaf until otherwise set through
       * {@link DirectoryObjectNode#setIsLeaf(boolean)}.
       */
      @Override
      public boolean isLeaf()
      {
         return m_isLeaf;
      }
      
      /**
       * Set whether this node is a leaf node or not.
       * 
       * @param isLeaf <code>true</code> if this is a leaf node, 
       *    <code>false</code> otherwise.
       */
      public void setIsLeaf(boolean isLeaf)
      {
         m_isLeaf = isLeaf;
      }
      
      /**
       * Defines whether this node is a leaf node or not.
       */
      private boolean m_isLeaf = false;
   }
   
   /**
    * The directory tree.
    */
   private class DirectoryTree extends JTree
   {
      /**
       * Creates a new tree for the supplied model.
       * 
       * @param model the model to create the tree for, assumed not 
       *    <code>null</code>.
       */
      public DirectoryTree(TreeModel model)
      {
         super(model);
         
         getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
      }
      
      /**
       * Overridden to catalog all children on request.
       * 
       * @param path the path to be cataloged and expanded, assumed not 
       *    <code>null</code>.
       */
      @Override
      public void expandPath(TreePath path)
      {
         if (path.getParentPath() != null)
         {
            DirectoryObjectNode current = 
               (DirectoryObjectNode) path.getLastPathComponent();
            if (!current.isLeaf() && current.getChildCount() == 0)
            {
               NamingEnumeration children = null;
               try
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                  
                  String directoryPath = "";
                  for (int i=path.getPathCount()-1; i>0; i--)
                  {
                     directoryPath += path.getPathComponent(i).toString();
                     if (i>1)
                        directoryPath += ",";
                  }
                  
                  children = m_context.listBindings(directoryPath);
                  while (children.hasMore())
                  {
                     Binding child = (Binding) children.next();
                     DirectoryObjectNode node = 
                        new DirectoryObjectNode(child.getName());
                     insertNode(current, node);
                  }
                  
                  current.setIsLeaf(current.getChildCount() == 0);
               }
               catch (NamingException e)
               {
                  Object[] args = 
                  {
                     e.getLocalizedMessage()
                  };
                  String message = MessageFormat.format(
                     getResources().getString("error.msg.catalog"), args);
            
                  JOptionPane.showMessageDialog(getParent(), 
                     PSLineBreaker.wrapString(message, 80, 30, null), 
                     getResources().getString("error.title"), 
                     JOptionPane.ERROR_MESSAGE);
               }
               finally
               {
                  if (children != null)
                     try {children.close();} catch (NamingException e) {}
                  setCursor(Cursor.getDefaultCursor());
               }

               m_treePanel.revalidate();
               m_treePanel.repaint();
            }
         }
         
         super.expandPath(path);
      }
   }
   
   /**
    * Overridden to avoid obfuscation issues.
    */
   @Override
   protected ResourceBundle getResources()
   {
      return super.getResources();
   }
   
   /**
    * The directory service data known at construction time of this dialog,
    * never <code>null</code> after construction.
    */
   private DirectoryServiceData m_data = null;
   
   /**
    * The protocol used, initialized in {@link #initData(String)}, never 
    * <code>null</code>, empty or changed after that.
    */
   private String m_protocol = null;

   /**
    * The host to connect with, must be set to fetch base DN's and to connect.
    */
   private JTextField m_host = new JTextField();
   
   /**
    * The port used for the connection, must be set to fetch base DN's and 
    * to connect.
    */
   private JTextField m_port = new JTextField();
   
   /**
    * The authentication used for catalog requests, must be set to fetch base
    * DN's and to connect.
    */
   private JComboBox m_authentication = new JComboBox();
   
   /**
    * A list with all found base DN's. Initialized or updated in 
    * {@link #onFetch(boolean)}. Can be entered manually.
    */
   private JComboBox<String> m_baseDn = new JComboBox<String>();
   
   /**
    * The button to fetch all base DN's from the specified server.
    */
   private JButton m_fetchBaseDnButton = null;
   
   /**
    * The button to catalog all objects from the specified base DN.
    */
   private JButton m_catalogButton = null;
   
   /**
    * The directory context, initialized in {@link #onFetch(boolean)} or 
    * {@link #onCatalog()}, never <code>null</code> after that.
    */
   private DirContext m_context = null;
   
   /**
    * The tree panel, initialized in {@link #createTreePanel()}.
    */
   private JPanel m_treePanel = null;

   /**
    * The directory tree, initialized in {@link #createTreePanel()} or
    * {@link #initTree()}, never <code>null</code> after that.
    */
   private DirectoryTree m_tree = null;
}
