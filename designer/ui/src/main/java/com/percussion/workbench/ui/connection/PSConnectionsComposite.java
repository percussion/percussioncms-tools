/******************************************************************************
 *
 * [ PSConnectionsComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.connection;

import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.workbench.config.PSUiConfigManager;
import com.percussion.workbench.connections.PSUserConnection;
import com.percussion.workbench.connections.PSUserConnectionSet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.xml.serialization.PSObjectSerializer;
import com.percussion.xml.serialization.PSObjectSerializerException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;
import java.util.Iterator;

/**
 * This is a reusable piece of dialog area with all the controls required for
 * connection specification by the user.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSConnectionsComposite extends Composite implements
   SelectionListener, ModifyListener, IPSUiConstants
{
   /**
    * Ctor that takes the parent, style and the user connetcion list to render.
    * 
    * @param parent
    * @param style
    * @param userConnectionList User connections that are generally stored as
    * eclipse preferences as {@link PSUserConnectionSet} object. Must not be
    * <code>null</code>
    * @param lastUsedConn last used connection, may be <code>null</code>.
    * @param parentDlg
    */
   public PSConnectionsComposite(Composite parent, int style,
      PSUserConnectionSet userConnectionList, PSUserConnection lastUsedConn,
      PSConnectionsDialog parentDlg)
   {
      super(parent, style);
      if (userConnectionList == null)
      {
         throw new IllegalArgumentException(
            "userConnectionList must not be null"); //$NON-NLS-1$
      }
      if (parentDlg == null)
      {
         throw new IllegalArgumentException("parentDlg must not be null"); //$NON-NLS-1$
      }
      
      PSConnectionInfo info = PSCoreFactory.getInstance().getConnectionInfo();
      m_activeConnName = info != null ? info.getName() : null;
      m_connections = userConnectionList;
      m_defaultConnection = m_connections.getDefaultConnection();
      m_parentDlg = parentDlg;
      if (lastUsedConn == null)
         m_currentConnection = m_connections.getDefaultConnection();
      else
         m_currentConnection = lastUsedConn;
      
      if (m_currentConnection == null
         && m_connections.getConnections().hasNext())
      {
         m_currentConnection = m_connections.getConnections().next();
      }
      
      initialize();
      // This must be the last line in the ctor.
      m_initialized = true;
   }

   private void initialize()
   {
      this.setSize(new org.eclipse.swt.graphics.Point(600, 350));
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      createSashForm();
      m_sashForm.setWeights(new int[]
      {
         5, 6
      });
      this.setLayout(gridLayout);

      m_buttonApply.addSelectionListener(this);
      m_buttonApply.setData(PSMessages
         .getString("PSConnectionsComposite.label.button.apply")); //$NON-NLS-1$
      m_buttonApply.setEnabled(false);
      m_buttonDelete.addSelectionListener(this);
      m_buttonDelete.setData((PSMessages
         .getString("PSConnectionsComposite.label.button.delete"))); //$NON-NLS-1$
      m_buttonNew.addSelectionListener(this);
      m_buttonNew.setData((PSMessages
         .getString("PSConnectionsComposite.label.button.new"))); //$NON-NLS-1$
      if (m_currentConnection != null)
         m_list.setSelection(new StructuredSelection(m_currentConnection), true);

      PSConnectionInfo info = PSCoreFactory.getInstance().getConnectionInfo();
      if (info != null)
      {
         enableAll(!m_currentConnection.getName().equals(info.getName()));
      }

      
      m_list.setLabelProvider(new LabelProvider()
      {
         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
          */
         @Override
         public String getText(Object element)
         {
            String name = element.toString();
            boolean connected = m_activeConnName != null 
               && name.equalsIgnoreCase(m_activeConnName);
            String marker = "";
            if (m_defaultConnection != null 
               && m_defaultConnection == element)
            {
               marker = "*"; //$NON-NLS-1$
            }
            
            String decorator = connected ? getConnectButtonLabelDecorator() : ""; 
            String pattern = PSMessages.getString(
               "PSConnectionsComposite.label.pattern"); //$NON-NLS-1$
            name = MessageFormat.format(pattern, marker, name, decorator);
            return name;
         }
      });
      objectToUi();
      setFocusToFirstEmptyControl();
   }

   /**
    * Adds a listener to the list of connection choices contained in this
    * composite.
    * 
    * @param listener If not <code>null</code>, it is added as a selection
    * change lister on the list component.
    */
   public void addSelectionChangedListener(ISelectionChangedListener listener)
   {
      if (listener != null)
         m_list.addSelectionChangedListener(listener);
   }
   
   /**
    * Find the first control that has an empty field to be filled in and set
    * focus to that control. By default the password control gets the focus.
    */
   private void setFocusToFirstEmptyControl()
   {
      Control ctrl = m_pwd;
      if (m_name.getText().trim().length() == 0)
         ctrl = m_name;
      else if (m_server.getText().trim().length() == 0)
         ctrl = m_server;
      else if (m_port.getText().trim().length() == 0)
         ctrl = m_port;
      else if (m_uid.getText().trim().length() == 0)
         ctrl = m_uid;
      ctrl.forceFocus();
      if (ctrl instanceof Text)
         ((Text) ctrl).setSelection(((Text) ctrl).getText().length());
   }

   /**
    * Enables/disables all controls in the group that contains the editing
    * controls.
    * 
    * @param enable <code>true</code> to enable, <code>false</code> to disable.
    */
   private void enableAll(boolean enable)
   {
      m_name.setEnabled(enable);
      m_server.setEnabled(enable);
      m_port.setEnabled(enable);
      m_uid.setEnabled(enable);
      m_pwd.setEnabled(enable);
      m_checkSavePwd.setEnabled(enable);
      m_makeDefault.setEnabled(enable);
      m_checkSsl.setEnabled(enable);
      m_timeout.setEnabled(enable);
   }
   
   /**
    * This method initializes sashForm
    */
   private void createSashForm()
   {
      GridData gridData = new org.eclipse.swt.layout.GridData();
      gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      gridData.grabExcessVerticalSpace = true;
      gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
      gridData.minimumWidth = 200;
      gridData.minimumHeight = 300;
      m_sashForm = new SashForm(this, SWT.HORIZONTAL);
      m_sashForm.setLayoutData(gridData);
      createGroup1();
      createGroup();
      m_groupRight.setParent(m_sashForm);
      m_groupLeft.setParent(m_sashForm);
   }

   /**
    * The only purpose of this class is to update the validation error message
    * as the user tabs/moves through the fields. The message should always 
    * show the error for the current field first.
    *
    * @author paulhoward
    */
   private class FocusTracker extends FocusAdapter
   {
      //see base class method for details
      @Override
      public void focusGained(FocusEvent e)
      {
         uiToObject(e.getSource());
      }
   }

   /**
    * This method initializes the controls used to edit a connection.
    */
   private void createGroup()
   {
      m_groupRight = new Group(m_sashForm, SWT.NONE);
      m_groupRight.setLayout(new FormLayout());


      final Label labelName = new Label(m_groupRight, SWT.NONE);
      labelName.setText(PSMessages
         .getString("PSConnectionsComposite.label.name")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.right = new FormAttachment(25, -5);
         labelName.setLayoutData(data);
      }

      final FocusListener fl = new FocusTracker();
      m_name = new Text(m_groupRight, SWT.BORDER);
      m_name.addFocusListener(fl);
      m_name.addModifyListener(this);
      {
         FormData data = new FormData();
         data.left = new FormAttachment(25);
         data.right = new FormAttachment(100, -10);
         m_name.setLayoutData(data);
      }

      final Label labelServer = new Label(m_groupRight, SWT.NONE);
      labelServer.setText(PSMessages
         .getString("PSConnectionsComposite.label.server")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.right = new FormAttachment(25, -5);
         data.top = new FormAttachment(m_name, 5, SWT.DEFAULT);
         labelServer.setLayoutData(data);
      }

      m_server = new Text(m_groupRight, SWT.BORDER);
      m_server.addFocusListener(fl);
      m_server.addModifyListener(this);
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_name, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         data.right = new FormAttachment(100, -10);
         m_server.setLayoutData(data);
      }

      Label labelPort = new Label(m_groupRight, SWT.NONE);
      labelPort.setText(PSMessages
         .getString("PSConnectionsComposite.label.port")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.right = new FormAttachment(25, -5);
         data.top = new FormAttachment(m_server, 5, SWT.DEFAULT);
         labelPort.setLayoutData(data);
      }

      int avgCharWidth;
      m_port = new Text(m_groupRight, SWT.BORDER);
      m_port.addFocusListener(fl);
      m_port.addModifyListener(this);
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_server, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         GC gc = new GC(m_port);
         FontMetrics fontMetrics = gc.getFontMetrics();
         avgCharWidth =fontMetrics.getAverageCharWidth();
         data.width = avgCharWidth * 10;
         m_port.setLayoutData(data);
         gc.dispose();
      }

      final Label labelUid = new Label(m_groupRight, SWT.NONE);
      labelUid
         .setText(PSMessages.getString("PSConnectionsComposite.label.uid")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.right = new FormAttachment(25, -5);
         data.top = new FormAttachment(m_port, 5, SWT.DEFAULT);
         labelUid.setLayoutData(data);
      }
      
      m_uid = new Text(m_groupRight, SWT.BORDER);
      m_uid.addFocusListener(fl);
      m_uid.addModifyListener(this);
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_port, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         data.right = new FormAttachment(100, -10);
         m_uid.setLayoutData(data);
      }


      final Label labelPassword = new Label(m_groupRight, SWT.NONE);
      labelPassword.setText(PSMessages
         .getString("PSConnectionsComposite.label.password")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.right = new FormAttachment(25, -5);
         data.top = new FormAttachment(m_uid, 5, SWT.DEFAULT);
         labelPassword.setLayoutData(data);
      }
      
      m_pwd = new Text(m_groupRight, SWT.BORDER);
      m_pwd.addModifyListener(this);
      m_pwd.setEchoChar('*');
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_uid, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         data.right = new FormAttachment(100, -10);
         m_pwd.setLayoutData(data);
      }

      m_checkSavePwd = new Button(m_groupRight, SWT.CHECK);
      m_checkSavePwd.addSelectionListener(this);
      m_checkSavePwd.setText(PSMessages
         .getString("PSConnectionsComposite.label.save_password")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_pwd, 15, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         m_checkSavePwd.setLayoutData(data);
      }

      m_makeDefault = new Button(m_groupRight, SWT.CHECK);
      m_makeDefault.addSelectionListener(this);
      m_makeDefault.setText(PSMessages
         .getString("PSConnectionsComposite.label.make_default")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_checkSavePwd, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         m_makeDefault.setLayoutData(data);
      }

      m_checkSsl = new Button(m_groupRight, SWT.CHECK);
      m_checkSsl.addSelectionListener(this);
      m_checkSsl.setText(PSMessages
         .getString("PSConnectionsComposite.label.ssl")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_makeDefault, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         m_checkSsl.setLayoutData(data);
      }

      final Label labelTimeout = new Label(m_groupRight, SWT.NONE);
      labelTimeout.setText(PSMessages
         .getString("PSConnectionsComposite.label.timeout")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.right = new FormAttachment(25, -5);
         data.top = new FormAttachment(m_checkSsl, 5, SWT.DEFAULT);
         labelTimeout.setLayoutData(data);
      }
      
      m_timeout = new Text(m_groupRight, SWT.BORDER);
      m_timeout.addModifyListener(this);
      {
         final FormData data = new FormData();
         data.top = new FormAttachment(m_checkSsl, 5, SWT.DEFAULT);
         data.left = new FormAttachment(25);
         data.width = avgCharWidth * 5;
         m_timeout.setLayoutData(data);
      }

      final Label labelTimeoutUnits = new Label(m_groupRight, SWT.NONE);
      labelTimeoutUnits.setText(PSMessages
         .getString("PSConnectionsComposite.label.timeoutUnits")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.left = new FormAttachment(m_timeout, 5);
         data.top = new FormAttachment(m_checkSsl, 5, SWT.DEFAULT);
         labelTimeoutUnits.setLayoutData(data);
      }
      
      m_buttonApply = new Button(m_groupRight, SWT.NONE);
      m_buttonApply.setText(PSMessages
         .getString("PSConnectionsComposite.label.button.apply")); //$NON-NLS-1$
      {
         final FormData data = new FormData();
         data.bottom = new FormAttachment(100, -5);
         data.right = new FormAttachment(100, -10);
         data.width = 80;
         m_buttonApply.setLayoutData(data);
      }
   }

   /**
    * This method initializes the controls on the left side of the dialog, 
    * including the list of connections.
    */
   private void createGroup1()
   {
      FormData data = null;
      FormLayout formLayout1 = new FormLayout();
      m_groupLeft = new Group(m_sashForm, SWT.NONE);
      m_groupLeft.setLayout(formLayout1);

      Label labelConnections = new Label(m_groupLeft, SWT.NONE);
      labelConnections.setText(PSMessages
         .getString("PSConnectionsComposite.label.names")); //$NON-NLS-1$
      data = new FormData();
      data.left = new FormAttachment(5);
      labelConnections.setLayoutData(data);

      m_list = new ListViewer(m_groupLeft, SWT.BORDER | SWT.V_SCROLL);

      m_buttonDelete = new Button(m_groupLeft, SWT.NONE);
      m_buttonDelete.setText(PSMessages
         .getString("PSConnectionsComposite.label.button.delete")); //$NON-NLS-1$
      data = new FormData();
      data.bottom = new FormAttachment(100, -5);
      data.right = new FormAttachment(100, -10);
      data.width = BUTTON_WIDTH;
      m_buttonDelete.setLayoutData(data);

      m_buttonNew = new Button(m_groupLeft, SWT.NONE);
      m_buttonNew.setText(PSMessages
         .getString("PSConnectionsComposite.label.button.new")); //$NON-NLS-1$
      data = new FormData();
      data.bottom = new FormAttachment(100, -5);
      data.right = new FormAttachment(m_buttonDelete, -BUTTON_HSPACE_OFFSET,
         SWT.LEFT);
      data.width = BUTTON_WIDTH;
      m_buttonNew.setLayoutData(data);

      m_groupLeft.setTabList(new Control[] { m_list.getControl(), m_buttonNew,
            m_buttonDelete });
      
      data = new FormData();
      data.left = new FormAttachment(5);
      data.right = new FormAttachment(100, -10);
      data.top = new FormAttachment(labelConnections, 5);
      data.bottom = new FormAttachment(m_buttonNew, -5);
      m_list.getList().setLayoutData(data);
      m_list.setContentProvider(new IStructuredContentProvider()
      {
         @SuppressWarnings("unused")
         public Object[] getElements(Object inputElement)
         {
            Object[] conns = new Object[m_connections.size()];
            Iterator iter = m_connections.getConnections();
            int i = 0;
            while (iter.hasNext())
            {
               PSUserConnection connection = (PSUserConnection) iter.next();
               conns[i++] = connection;
            }
            return conns;
         }

         public void dispose()
         {}

         @SuppressWarnings("unused")
         public void inputChanged(Viewer viewer, Object oldInput,
            Object newInput)
         {
         }
      });
      m_list.setLabelProvider(new LabelProvider()
      {
         @Override
         @SuppressWarnings("unused")
         public Image getImage(Object element)
         {
            return null;
         }

         @Override
         public String getText(Object element)
         {
            return element.toString();
         }
      });
      
      PSConnectionInfo tmp = PSCoreFactory.getInstance().getConnectionInfo();
      m_connectionInfo = tmp == null ? null : m_connections
            .getConnectionByName(tmp.getName());

      m_list.addSelectionChangedListener(new ISelectionChangedListener()
      {
         public void selectionChanged(SelectionChangedEvent event)
         {
            if (mi_ignoreSelectionChange)
               return;
            if (m_initialized && !isValid())
            {
               String title = PSMessages.getString(
                     "PSConnectionsComposite.warning.cantSwitch.title");
               String msg = PSMessages.getString(
                     "PSConnectionsComposite.warning.cantSwitch.message");
               MessageDialog.openWarning(getShell(), title, msg);
               mi_ignoreSelectionChange = true;
               m_list.setSelection(
                     new StructuredSelection(m_currentConnection), true);
               //there's a small risk here w/o the try/catch, but I didn't deem
               // it large enough to add it to guarantee this flag got set
               mi_ignoreSelectionChange = false;
               return;
            }
            StructuredSelection sel = (StructuredSelection) event.getSelection();
            if(sel.isEmpty() || m_currentConnection == sel.getFirstElement())
               return;
            StructuredSelection selection = (StructuredSelection) event
               .getSelection();
            m_currentConnection = (PSUserConnection) selection
               .getFirstElement();
            // Render the object to UI
            objectToUi();
         }

         /**
          * A flag used to prevent an infinite loop when we change the selection
          * from within the selection changed listener. Defaults to 
          * <code>false</code>.
          */
         private boolean mi_ignoreSelectionChange = false;
      });
      m_list.setInput(m_connections);
      m_list.setSorter(new ViewerSorter()
      {
         @Override
         public int compare(Viewer viewer, Object e1, Object e2)
         {
            return super.compare(viewer, e1, e2);
         }
      });
   }

   /**
    * Checks validity of all controls that have such checks.
    * 
    * @return <code>true</code> if the data in all controls is OK,
    * <code>false</code> otherwise.
    */
   boolean isValid()
   {
      return validateAll() == null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      PSUserConnection conn = null;
      StructuredSelection selection = (StructuredSelection) m_list
         .getSelection();
      if (selection != null)
      {
         conn = (PSUserConnection) selection.getFirstElement();
      }
      if (e.getSource() == m_buttonApply)
      {
         applyChanges();
      }
      else if (e.getSource() == m_buttonNew)
      {
         if (conn == null)
         {
            conn = new PSUserConnection();
            conn.setName(PSMessages
               .getString("PSConnectionsComposite.label.new_connection")); //$NON-NLS-1$
         }
         else
         {
            try
            {
               conn = (PSUserConnection) PSObjectSerializer.getInstance()
                  .cloneObject(conn);
               if (conn != null)
                  conn
                     .setName(PSMessages
                        .getString("PSConnectionsComposite.label.name.new_prefix") + conn.getName()); //$NON-NLS-1$
            }
            catch (PSObjectSerializerException e1)
            {
               // XXX Auto-generated catch block
               e1.printStackTrace();
            }
         }
         int index = 1;
         String name = conn.getName();
         boolean repeat = false;
         do
         {
            repeat = false;
            if (m_connections.getConnectionByName(conn.getName()) != null)
            {
               repeat = true;
               conn.setName(name + index++);
            }
         }
         while (repeat == true);
         m_connections.addConnection(conn);
         m_list.setInput(m_connections);
         m_list.setSelection(new StructuredSelection(conn));
         setDataDirty();
      }
      else if (e.getSource() == m_buttonDelete)
      {
         if(m_list.getList().getItemCount() == 1)
         {
            m_parentDlg.setErrorMessage(PSMessages.getString(
               "PSConnectionsComposite.error.at_least_one_connection_required")); //$NON-NLS-1$
            return;
         }
         if (conn != null)
         {
            int[] sel = m_list.getList().getSelectionIndices();
            int index = sel.length > 0 ? sel[0] : 0;
            m_connections.remove(conn);
            m_list.setInput(m_connections);
            setSelected(index);
            setDataDirty();
         }
      }
      else if (e.getSource() == m_checkSsl || e.getSource() == m_checkSavePwd
         || e.getSource() == m_makeDefault)
      {
         uiToObject(e.getSource());
      }
   }

   /**
    * The list entry that contains the connection info used to connect to the
    * currently connected server is decorated with this value.
    * 
    * @return Never <code>null</code> or empty. Something like &lt;connected&gt;
    */
   protected String getConnectButtonLabelDecorator()
   {
      return PSMessages.getString(
         "PSConnectionsComposite.label.connectedDecorator"); //$NON-NLS-1$
   }


   
   /**
    * Validate the data and save changes.
    */
   void applyChanges()
   {
      if (m_defaultConnection != null)
      {
         // Only do this if a default connection has been specified
         m_connections.setDefault(m_defaultConnection.getName());
      }
      
      // Empty password fields before persisting for all connections with save
      // password flag false
      try
      {
         PSUserConnectionSet conns = (PSUserConnectionSet) PSObjectSerializer
            .getInstance().cloneObject(m_connections);
         Iterator iter = conns.getConnections();
         while (iter.hasNext())
         {
            PSUserConnection conn = (PSUserConnection) iter.next();
            if (!conn.isSavePassword())
               conn.setClearTextPassword(StringUtils.EMPTY);
         }
         PSUiConfigManager.getInstance().saveSectionConfig(conns);
         m_dirty = false;
         m_buttonApply.setEnabled(false);
      }
      catch (PSObjectSerializerException e)
      {
         String title = PSMessages.getString("common.error.title");
         String msg = PSMessages.getString(
               "PSConnectionsComposite.error.cloningConns", new Object[] {
                     e.getLocalizedMessage()});
         MessageDialog.openError(getShell(), title, msg);
      }
   }

   /**
    * Is the data associated with this composite dirty?
    * 
    * @return <code>true</code> if dirty <code>false</code> otherwise.
    */
   boolean isDirty()
   {
      return m_dirty;
   }

   /**
    * @param index
    */
   private void setSelected(int index)
   {
      int count = m_list.getList().getItemCount();
      if (count == 0)
         return;
      if (count == 1)
         index = 0;
      else if (index >= count)
      {
         index = count - 1;
      }
      m_list.setSelection(new StructuredSelection(m_list.getElementAt(index)));
   }

   /**
    * 
    */
   private void setDataDirty()
   {
      m_dirty = true;
      m_buttonApply.setEnabled(true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(SelectionEvent e)
   {
      System.out.println(e);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
    */
   public void modifyText(ModifyEvent e)
   {
      if (!m_ignoreTextChanges)
         uiToObject(e.getSource());
   }

   /**
    * Read data from corrent connection object to ui. Nothing happens if current
    * or selected connection is <code>null</code> which should not happen in
    * normal situations.
    */
   private void objectToUi()
   {
      if(m_currentConnection == null)
         return;

      m_ignoreTextChanges = true;
      
      try
      {
         m_name.setText(m_currentConnection.getName());
         m_server.setText(m_currentConnection.getServer());
         m_port.setText(StringUtils.EMPTY + m_currentConnection.getPort());
         m_uid.setText(m_currentConnection.getUserid());
         m_pwd.setText(m_currentConnection.getClearTextPassword());
         m_checkSsl.setSelection(m_currentConnection.isUseSsl());
         m_checkSavePwd.setSelection(m_currentConnection.isSavePassword());
         m_timeout.setText(String.valueOf(m_currentConnection.getTimeout()));
         if (m_defaultConnection != null)
         {
            boolean curDefault = m_currentConnection == m_defaultConnection;
            m_makeDefault.setSelection(curDefault);
         }
      }
      finally
      {
         m_ignoreTextChanges = false;
         configureEnablement();
      }
   }

   /**
    * Read data from UI to object after validation.
    */
   private void uiToObject(Object control)
   {
      if (m_currentConnection == null || !m_initialized)
         return;
      String source, target;
      
      boolean dirty = false;
      String errMsg = null;

      if (control == m_name)
      {
         source = m_currentConnection.getName();
         target = m_name.getText().trim();
         if (!source.equals(target))
         {
            errMsg = validateField(m_name);
            if (errMsg == null)
            {
               m_currentConnection.setName(target);
               //deal with the name changing for the default connection
               if (m_makeDefault.getSelection())
                  m_defaultConnection = m_currentConnection;
               dirty = true;
            }
         }
      }
      else if (control == m_server)
      {
         source = m_currentConnection.getServer();
         target = m_server.getText().trim();
         if (!source.equals(target))
         {
            errMsg = validateField(m_server);
            if (errMsg == null)
            {
               m_currentConnection.setServer(target);
               dirty = true;
            }
         }
      }
      else if (control == m_port)
      {
         source = m_currentConnection.getPort() + ""; //$NON-NLS-1$
         target = m_port.getText().trim();
         if (!source.equals(target))
         {
            errMsg = validateField(m_port);
            if (errMsg == null)
            {
               m_currentConnection.setPort(Integer.parseInt(target));
               dirty = true;
            }
         }
      }
      else if (control == m_uid)
      {
         source = m_currentConnection.getUserid();
         target = m_uid.getText().trim();
         if (!source.equals(target))
         {
            errMsg = validateField(m_uid);
            if (errMsg == null)
            {
               m_currentConnection.setUserid(target);
               dirty = true;
            }
         }
      }
      else if (control == m_pwd)
      {

         source = m_currentConnection.getClearTextPassword();
         target = m_pwd.getText();
         if (!source.equals(target))
         {
            m_currentConnection.setClearTextPassword(target);
            dirty = true;
         }
      }
      else if (control == m_checkSsl)
      {
         boolean checked = m_checkSsl.getSelection();
         if (m_currentConnection.isUseSsl() != checked)
         {
            m_currentConnection.setUseSsl(checked);
            dirty = true;
         }
      }
      else if (control == m_checkSavePwd)
      {
         boolean checked = m_checkSavePwd.getSelection();
         if (m_currentConnection.isSavePassword() != checked)
         {
            m_currentConnection.setSavePassword(checked);
            dirty = true;
         }
      }
      else if (control == m_timeout)
      {
         source = String.valueOf(m_currentConnection.getTimeout());
         target = m_timeout.getText().trim();
         if (!source.equals(target))
         {
            errMsg = validateField(m_timeout);
            if (errMsg == null)
            {
               m_currentConnection.setTimeout(Integer.parseInt(target));
               dirty = true;
            }
         }
      }
      else if (control == m_makeDefault)
      {
         if (m_makeDefault.getSelection())
         {
            if (m_currentConnection != null)
            {
               m_defaultConnection = m_currentConnection;
               dirty = true;
            }
         }
         else
         {
            if (m_defaultConnection != null)
            {
               m_defaultConnection = null;
               dirty = true;
            }
         }
      }
      
      boolean currentFieldValidationFailed = false;
      if (errMsg != null)
      {
         currentFieldValidationFailed = true;
      }
      else
      {
         errMsg = validateAll();
      }
      m_parentDlg.setErrorMessage(errMsg);

      if (!currentFieldValidationFailed)
      {
         if (dirty)
            //we don't call setDataDirty() because we don't want the enablement
            m_dirty = true;
         m_list.refresh(true);
      }
      configureEnablement();
}
   
   /**
    * Sets the enabled/disabled state for all controls and buttons in this
    * composite and the parent dialog based on the current state.
    */
   void configureEnablement()
   {
      boolean connectedSelected = m_currentConnection.equals(m_connectionInfo); 
      enableAll(!connectedSelected);
      m_buttonDelete.setEnabled(!connectedSelected);
      boolean valid = isValid();
      m_parentDlg.enableOkButton(!connectedSelected && valid);
      m_buttonApply.setEnabled(m_dirty && valid);
      m_buttonNew.setEnabled(valid);
   }
   
   /**
    * Walks all controls that need validation in tab order and checks their
    * values. The first one that is invalid returns an error message.
    * 
    * @return A message describing the validation problem, or <code>null</code>
    * if there are no problems.
    */
   private String validateAll()
   {
      String errMsg;
      errMsg = validateField(m_name);
      if (errMsg == null)
         errMsg = validateField(m_server);
      if (errMsg == null)
         errMsg = validateField(m_port);
      if (errMsg == null)
         errMsg = validateField(m_uid);
      if (errMsg == null)
         errMsg = validateField(m_timeout);
      return errMsg;
   }

   /**
    * Validates text fields not have empty values.
    * 
    * @param tField text field to validate, assumed not <code>null</code>.
    * @return <code>null</code> if validation succeeds and a message in case of
    * error.
    */
   private String validateField(Text tField)
   {
      String msg = null;
      String value = tField.getText();
      if(tField == m_name)
      {
         if(StringUtils.isBlank(value))
         {
            msg = PSMessages.getString(
               "PSConnectionsComposite.error.connection_name_cannot_be_empty"); //$NON-NLS-1$
         }
         int nameMatchCount = 0;
         for (Iterator<PSUserConnection> connIter = m_connections
               .getConnections(); connIter.hasNext();)
         {
            if (connIter.next().getName().equals(value))
               nameMatchCount++;
         }
         if (nameMatchCount > 1)
         {
            msg = PSMessages.getString(
               "PSConnectionsComposite.error.duplicateConnectionName"); //$NON-NLS-1$
         }
      }
      else if(tField == m_server)
      {
         if(StringUtils.isBlank(value))
         {
            msg = PSMessages.getString(
               "PSConnectionsComposite.error.server_cannot_be_empty"); //$NON-NLS-1$
         }
      }
      else if(tField == m_port)
      {
         try
         {
            Integer.parseInt(value);
         }
         catch (NumberFormatException e)
         {
            msg = PSMessages.getString("PSConnectionsComposite.error.invalid_port_number"); //$NON-NLS-1$
         }
      }
      else if(tField == m_timeout)
      {
         try
         {
            int timeout = Integer.parseInt(value);
            if (timeout < 10 || timeout > 3600)
            {
               msg = PSMessages
                     .getString("PSConnectionsComposite.error.invalid_timeout"); //$NON-NLS-1$
            }
         }
         catch (NumberFormatException e)
         {
            msg = PSMessages.getString("PSConnectionsComposite.error.invalid_timeout"); //$NON-NLS-1$
         }
      }
      else if(tField == m_uid)
      {
         if(StringUtils.isBlank(value))
            msg = PSMessages.getString("PSConnectionsComposite.error_userid_cannot_be_empty"); //$NON-NLS-1$
      }
      return msg;
   }

   /**
    * Get the currently selected connection.
    * 
    * @return currently selected connection, <code>null</code> if none
    * selected for any reason.
    */
   PSUserConnection getCurrentConnection()
   {
      return m_currentConnection;
   }

   /**
    * The name of the connection that was used to successfully logon in this
    * session. Either <code>null</code> or a non-empty string. The
    * connection's label whose name matches this is appended with
    * &lt;connected&gt; using a case-insensitive compare.
    */
   private final String m_activeConnName;

   /**
    * The connection set object this composite renders, never <code>null</code>
    * supplied via the ctor.
    */
   private PSUserConnectionSet m_connections = null;

   /**
    * This is the currently selected connection object or last used connection.
    * If non-<code>null</code> supplied to the ctor then this is used as the
    * selected connection. Otherwise the default one is used as the selected
    * one. After that it is reset every time the slection in the connection list
    * is changed.
    */
   private PSUserConnection m_currentConnection = null;

   /**
    * The default connection, set or unset when user clicks the check box to
    * make a connection default.
    */
   private PSUserConnection m_defaultConnection = null;

   /**
    * The server that the workbench is currently logged into, or
    * <code>null</code> if it is not logged in. This is one of the connections
    * in the {@link #m_connections} set, i.e. you can use == on it. It is set
    * once during init, then never modified.
    */
   private PSUserConnection m_connectionInfo;

   /**
    * This flag is used to skip text change notifications while we are
    * transferring data from the object to the ui. Defaults to
    * <code>false</code>.
    */
   private boolean m_ignoreTextChanges = false;

   /**
    * Dirty flag used to set and reset to enable or disable the apply button.
    */
   private boolean m_dirty = false;

   /**
    * Is the composite initialized yet. <code>false</code> initially and set
    * to <code>true</code> as a last step in the ctor.
    */
   private boolean m_initialized = false;

   // UI elements/controls
   private PSConnectionsDialog m_parentDlg = null;

   private SashForm m_sashForm = null;

   private Group m_groupLeft = null;

   private ListViewer m_list = null;

   private Group m_groupRight = null;

   private Text m_name = null;

   private Text m_server = null;

   private Text m_port = null;

   private Text m_uid = null;

   private Text m_pwd = null;
   
   /**
    * The control used to present the timeout value to the user. Initialized 
    * when the controls are created, then never <code>null</code> or modified.
    * I didn't use a Spinner because it doesn't send Modify events when the 
    * text is manually changed and there appears to be no way to get the 
    * changed text.
    */
   private Text m_timeout;
   
   private Button m_checkSavePwd = null;

   private Button m_makeDefault = null;

   private Button m_checkSsl = null;

   private Button m_buttonApply = null;

   private Button m_buttonNew = null;

   private Button m_buttonDelete = null;
} // @jve:decl-index=0:visual-constraint="74,-106"
