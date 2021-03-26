/******************************************************************************
 *
 * [ PSAclComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.workbench.ui.security;

import com.percussion.client.PSCoreFactory;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.security.PSAclDialog.EntryTypePair;
import com.percussion.workbench.ui.util.PSErrorDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This composite encapsulates and renders the data object of
 * {@link com.percussion.services.security.data.PSAclImpl} and is intended to be
 * part of the field editor
 * {@link com.percussion.workbench.ui.preferences.PSAclFieldEditor}.
 */
public class PSAclComposite extends Composite implements
   ISelectionChangedListener, SelectionListener, IPSUiConstants
{
   /**
    * Ctro taking the parent composite and style. Invokes the base class version
    * {@link Composite#Composite(org.eclipse.swt.widgets.Composite, int)} and
    * initializes the UI.
    * @param aclListTitle string to show above the list of ACLs
    * 
    * @see Composite#Composite(org.eclipse.swt.widgets.Composite, int)
    */
   public PSAclComposite(Composite parent, int style, String aclListTitle)
   {
      super(parent, style);

      setLayout(new FormLayout());

      m_sashForm = new SashForm(this, SWT.HORIZONTAL);
      FormData data = new FormData();
      data.top = new FormAttachment(0, 5);
      data.left = new FormAttachment(0, 5);
      data.right = new FormAttachment(100, -5);
      data.bottom = new FormAttachment(100, -5);
      m_sashForm.setLayoutData(data);


      m_groupLeft = new Group(m_sashForm, SWT.NONE);
      m_groupLeft.setLayout(new FormLayout());

      m_buttonDelete = new Button(m_groupLeft, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.bottom = new FormAttachment(100, -5);
      formData_1.right = new FormAttachment(100, -5);
      m_buttonDelete.setLayoutData(formData_1);
      m_buttonDelete.setText(PSMessages
         .getString("PSAclComposite.button.delete")); //$NON-NLS-1$
      m_buttonDelete.addSelectionListener(this);

      m_buttonNewAclEntry = new Button(m_groupLeft, SWT.NONE);
      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(m_buttonDelete, 0, SWT.BOTTOM);
      formData.right = new FormAttachment(m_buttonDelete,
         -BUTTON_HSPACE_OFFSET, SWT.LEFT);
      m_buttonNewAclEntry.setLayoutData(formData);
      m_buttonNewAclEntry.setText(PSMessages
         .getString("PSAclComposite.button.newAclEntry")); //$NON-NLS-1$

      m_buttonNewAclEntry.addSelectionListener(this);

      final Label aclEntries = new Label(m_groupLeft, SWT.NONE);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(0, -1);
      formData_2.left = new FormAttachment(0, 5);
      aclEntries.setLayoutData(formData_2);
      aclEntries.setText(aclListTitle);

      m_tableViewer = new TableViewer(m_groupLeft, SWT.BORDER);
      m_tableAclEntries = m_tableViewer.getTable();
      final FormData formData_3 = new FormData();
      formData_3.bottom = new FormAttachment(m_buttonDelete, -5, SWT.DEFAULT);
      formData_3.top = new FormAttachment(aclEntries, 2, SWT.DEFAULT);
      formData_3.right = new FormAttachment(100, -5);
      formData_3.left = new FormAttachment(0, 5);
      m_tableAclEntries.setLayoutData(formData_3);
      m_tableViewer.setContentProvider(new IStructuredContentProvider()
      {
         public Object[] getElements(Object inputElement)
         {
            IPSAcl acl = (IPSAcl) inputElement;
            Enumeration entries = acl.entries();
            java.util.List<IPSAclEntry> entryList = new ArrayList<IPSAclEntry>();
            while (entries != null && entries.hasMoreElements())
            {
               entryList.add((IPSAclEntry) entries.nextElement());
            }
            return entryList.toArray();
         }

         public void dispose()
         {
            // XXX Auto-generated method stub

         }

         @SuppressWarnings("unused")
         public void inputChanged(Viewer viewer, Object oldInput,
            Object newInput)
         {
         }
      });
      m_tableViewer.setLabelProvider(new LabelProvider()
      {

         @Override
         public Image getImage(Object element)
         {
            IPSAclEntry entry = (IPSAclEntry) element;
            return PSAclEntryDialog.getTypeImage(entry);
         }

         @Override
         public String getText(Object element)
         {
            IPSAclEntry entry = (IPSAclEntry) element;
            return entry.getPrincipal().getName();
         }
      });
      m_tableViewer.setSorter(new ViewerSorter()
      {
         @SuppressWarnings("deprecation")
         @Override
         public int compare(@SuppressWarnings("unused") Viewer viewer,
               Object e1, Object e2)
         {
            IPSAclEntry entry1 = (IPSAclEntry) e1;
            IPSAclEntry entry2 = (IPSAclEntry) e2;

            // First sort by the entry type
            int order = entry2.getType().getOrdinal()
               - entry1.getType().getOrdinal();
            if (order == 0)
            {
               // then sort by the name in the entry
               return entry1.getPrincipal().getName().compareTo(
                  entry2.getPrincipal().getName());
            }
            return order;
         }
      });
      m_tableViewer.setInput(m_acl);
      m_groupRight = new Group(m_sashForm, SWT.NONE);
      m_groupRight.setLayout(new FormLayout());

      m_tableViewer.addSelectionChangedListener(this);

      final Label designAccessLabel = new Label(m_groupRight, SWT.NONE);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(0, 20);
      formData_4.left = new FormAttachment(0, 10);
      designAccessLabel.setLayoutData(formData_4);
      designAccessLabel.setText(PSMessages
         .getString("PSAclComposite.label.designAccess")); //$NON-NLS-1$

      m_checkDesignRead = new Button(m_groupRight, SWT.CHECK);
      final FormData formData_6 = new FormData();
      formData_6.top = new FormAttachment(designAccessLabel, 10);
      formData_6.left = new FormAttachment(0, 20);
      m_checkDesignRead.setLayoutData(formData_6);
      m_checkDesignRead.setText(PSMessages
         .getString("PSAclComposite.checkbox.Read")); //$NON-NLS-1$
      m_checkDesignRead.addSelectionListener(this);

      m_checkDesignUpdate = new Button(m_groupRight, SWT.CHECK);
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_checkDesignRead, 0);
      formData_7.left = new FormAttachment(0, 20);
      m_checkDesignUpdate.setLayoutData(formData_7);
      m_checkDesignUpdate.setText(PSMessages
         .getString("PSAclComposite.checkbox.update")); //$NON-NLS-1$
      m_checkDesignUpdate.addSelectionListener(this);

      m_checkDesignDelete = new Button(m_groupRight, SWT.CHECK);
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_checkDesignUpdate, 0);
      formData_8.left = new FormAttachment(0, 20);
      m_checkDesignDelete.setLayoutData(formData_8);
      m_checkDesignDelete.setText(PSMessages
         .getString("PSAclComposite.checkbox.delete")); //$NON-NLS-1$
      m_checkDesignDelete.addSelectionListener(this);

      m_checkDesignModifyAcl = new Button(m_groupRight, SWT.CHECK);
      final FormData formData_9 = new FormData();
      formData_9.top = new FormAttachment(m_checkDesignDelete, 0);
      formData_9.left = new FormAttachment(0, 20);
      m_checkDesignModifyAcl.setLayoutData(formData_9);
      m_checkDesignModifyAcl.setText(PSMessages
         .getString("PSAclComposite.checkbox.modifyAcl")); //$NON-NLS-1$
      m_checkDesignModifyAcl.addSelectionListener(this);

      final Label runtimeAccessLabel = new Label(m_groupRight, SWT.NONE);
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_checkDesignModifyAcl, 10);
      formData_5.left = new FormAttachment(0, 10);
      runtimeAccessLabel.setLayoutData(formData_5);
      runtimeAccessLabel.setText(PSMessages
         .getString("PSAclComposite.label.runTimeAccess")); //$NON-NLS-1$

      m_checkRuntimeRead = new Button(m_groupRight, SWT.CHECK);
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(runtimeAccessLabel, 10);
      formData_10.left = new FormAttachment(0, 20);
      m_checkRuntimeRead.setLayoutData(formData_10);
      m_checkRuntimeRead.setText(PSMessages
         .getString("PSAclComposite.checkbox.runTimeRead")); //$NON-NLS-1$
      m_checkRuntimeRead.addSelectionListener(this);
      m_sashForm.setWeights(new int[]
      {
         3, 2
      });
      m_tableAclEntries.setFocus();
      //
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
    */
   public void selectionChanged(SelectionChangedEvent event)
   {
      if (event.getSource() == m_tableViewer)
      {
         // Read data from UI to object.
         uiToObject(null);
         StructuredSelection selection = (StructuredSelection) event
            .getSelection();
         m_currentEntry = (IPSAclEntry) selection.getFirstElement();
         // Render the object to UI
         objectToUi();
         enableControls();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("deprecation")
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_buttonNewAclEntry)
      {
         IPSAcl acl = getAcl();
         Enumeration enumEntries = acl.entries();
         List<EntryTypePair> pairs = new ArrayList<EntryTypePair>();
         while (enumEntries.hasMoreElements())
         {
            IPSAclEntry entry = (IPSAclEntry) enumEntries.nextElement();
            pairs.add(new EntryTypePair(entry.getType(), entry.getName()));
         }
         PSAclEntryDialog dlg = new PSAclEntryDialog(getShell(), pairs);
         int resp = dlg.open();
         if (resp == Dialog.OK)
         {
            List<EntryTypePair> newPairs = dlg.getEntries();
            newPairs = filterDuplicate(newPairs, acl);
            for (Iterator iterator = newPairs.iterator(); iterator.hasNext();)
            {
               EntryTypePair pair = (EntryTypePair) iterator.next();
               IPSAclEntry entry = acl.createEntry(new PSTypedPrincipal(
                  pair.name, pair.type));
               //New entries get Read/Runtime visible permissions by default
               if(pair.type.equals(PrincipalTypes.COMMUNITY))
                  entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
               else
                  entry.addPermission(PSPermissions.READ);
               try
               {
                  acl.addEntry(acl.getFirstOwner(), entry);
               }
               catch (NotOwnerException ex)
               {
                  displayAclError(ex);
                  return;
               }
            }
            setAcl(m_acl, true);
         }
         m_tableAclEntries.setFocus();
      }
      else if (e.getSource() == m_buttonDelete)
      {
         if (m_tableAclEntries.getSelectionCount() < 1)
            return;
         TableItem[] selEntries = m_tableAclEntries.getSelection();
         for (int i = selEntries.length - 1; i >= 0; i--)
         {
            try
            {
               IPSAclEntry entry = (IPSAclEntry) selEntries[i].getData();
               if (entry.isUser()
                  && entry.getPrincipal().equals(
                     PSCoreFactory.getInstance().getUserPrincipal()))
               {
                  boolean ok = MessageDialog
                     .openConfirm(
                        getShell(),
                        PSMessages
                           .getString("PSAclComposite.warning.title.remove_yourself"), //$NON-NLS-1$ 
                        PSMessages
                           .getString("PSAclComposite.warning.msg.removing_yourself")); //$NON-NLS-1$ 
                  if (!ok)
                     continue;

               }
               m_acl.removeEntry(m_acl.getFirstOwner(), entry);
               if (m_currentEntry.equals(entry))
                  m_currentEntry = null;
               m_tableViewer.remove(selEntries[i]);
            }
            catch (NotOwnerException e1)
            {
               new PSErrorDialog(getShell(), PSMessages
                  .getString("PSAclComposite.error.notOwner"), e1).open(); //$NON-NLS-1$
               return;
            }
            catch (SecurityException e1)
            {
               new PSErrorDialog(getShell(), PSMessages
                  .getString("PSAclComposite.eror.securityViolation"), e1).open(); //$NON-NLS-1$
               return;
            }
         }
         setAcl(m_acl, true);
         m_tableAclEntries.setFocus();
      }
      else
      {
         uiToObject(e.getSource());
      }
   }

   /**
    * Removes pairs matching to those in the supplied ACL. The match is found by
    * comapring the type and name (case insensitive).
    * 
    * @param pairs list of pairs to filter, assumed not <code>null</code>.
    * @param acl ACL from which the entries to compare are taken, assumed not
    * <code>null</code>.
    * @return supplied pairs after filtering out the matching pairs.
    */
   @SuppressWarnings("deprecation")
   private List<EntryTypePair> filterDuplicate(List<EntryTypePair> pairs,
      IPSAcl acl)
   {
      for (int i = pairs.size() - 1; i >= 0; i--)
      {
         EntryTypePair pair = pairs.get(i);
         Enumeration enumEntries = acl.entries();
         while (enumEntries.hasMoreElements())
         {
            IPSAclEntry entry = (IPSAclEntry) enumEntries.nextElement();
            if (pair.type.equals(entry.getType())
               && pair.name.equalsIgnoreCase(entry.getName()))
            {
               // Duplicate entry remove it
               pairs.remove(pair);
               break;
            }
         }
      }
      return pairs;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   @SuppressWarnings("unused")
   public void widgetDefaultSelected(SelectionEvent e)
   {
      // XXX Auto-generated method stub
   }

   /**
    * Get the ACL object rendered by this composite. The object will represent
    * the current state of the UI.
    * 
    * @return ACL object, never <code>null</code>.
    */
   public IPSAcl getAcl()
   {
      uiToObject(null);
      return m_acl;
   }

   /**
    * set the ACL object to be rendered by this composite. The UI is refreshed
    * to reflect the new object.
    * 
    * @param acl must not be <code>null</code>.
    * @param isOwner is curent user owner of the acl?
    */
   public void setAcl(IPSAcl acl, boolean isOwner)
   {
      if (acl == null)
      {
         throw new IllegalArgumentException("acl must not be null"); //$NON-NLS-1$
      }
      m_acl = acl;
      m_tableViewer.setInput(m_acl);
      m_isOwner = isOwner;
      m_tableAclEntries.setSelection(0);
      m_currentEntry = (IPSAclEntry) m_tableAclEntries.getSelection()[0]
         .getData();
      objectToUi();
      enableControls();
   }

   /**
    * Helper method to render the settings from the data object to the UI.
    */
   private void objectToUi()
   {
      if (m_currentEntry == null)
         return;
      boolean isOwner = m_acl.isOwner(m_currentEntry.getPrincipal());
      m_checkDesignRead.setSelection(m_currentEntry
         .checkPermission(PSPermissions.READ));
      m_checkDesignUpdate.setSelection(m_currentEntry
         .checkPermission(PSPermissions.UPDATE));
      m_checkDesignDelete.setSelection(m_currentEntry
         .checkPermission(PSPermissions.DELETE));
      m_checkDesignModifyAcl.setSelection(isOwner);
      m_checkRuntimeRead.setSelection(m_currentEntry
         .checkPermission(PSPermissions.RUNTIME_VISIBLE));
   }

   /**
    */
   @SuppressWarnings("deprecation")
   private void enableControls()
   {
      m_buttonNewAclEntry.setEnabled(m_isOwner);

      m_buttonDelete.setEnabled(m_isOwner
         && m_tableAclEntries.getSelectionCount() > 0
         && !m_currentEntry.isSystemEntry()
         && !m_currentEntry.isSystemCommunity());

      m_checkDesignRead.setEnabled(m_isOwner && m_currentEntry != null
         && !m_currentEntry.isCommunity());
      m_checkDesignUpdate.setEnabled(m_isOwner && m_currentEntry != null
         && !m_currentEntry.isCommunity());
      m_checkDesignDelete.setEnabled(m_isOwner && m_currentEntry != null
         && !m_currentEntry.isCommunity());
      m_checkDesignModifyAcl.setEnabled(m_isOwner && m_currentEntry != null
         && !m_currentEntry.isCommunity());
      m_checkRuntimeRead.setEnabled(m_isOwner && m_currentEntry != null
         && m_currentEntry.isCommunity());
   }

   /**
    * Helper method to read the settings to the data object from UI.
    * 
    * @param control the control from which the data is to be updated,
    * <code>null</code> means refresh data form all controls.
    */
   private void uiToObject(Object control)
   {
      if (m_currentEntry == null)
         return;
      if (control == null || control == m_checkDesignRead)
      {
         if (m_checkDesignRead.getSelection())
            m_currentEntry.addPermission(PSPermissions.READ);
         else
            m_currentEntry.removePermission(PSPermissions.READ);
      }
      if (control == null || control == m_checkDesignUpdate)
      {
         if (m_checkDesignUpdate.getSelection())
            m_currentEntry.addPermission(PSPermissions.UPDATE);
         else
            m_currentEntry.removePermission(PSPermissions.UPDATE);
      }
      if (control == null || control == m_checkDesignDelete)
      {
         if (m_checkDesignDelete.getSelection())
         {
            m_currentEntry.addPermission(PSPermissions.DELETE);
         }
         else
            m_currentEntry.removePermission(PSPermissions.DELETE);
      }
      if (control == null || control == m_checkDesignModifyAcl)
      {
         if (m_checkDesignModifyAcl.getSelection())
         {
            Exception ex = null;
            try
            {
               if (!m_acl.isOwner(m_currentEntry.getPrincipal()))
               {
                  m_acl.addOwner(m_acl.getFirstOwner(), m_currentEntry
                     .getPrincipal());
               }
            }
            catch (SecurityException e)
            {
               ex = e;
            }
            catch (NotOwnerException e)
            {
               ex = e;
            }
            if (ex != null)
            {
               displayAclError(ex);
               m_checkDesignModifyAcl.setSelection(false);
            }
         }
         else
         {
            Exception ex = null;
            try
            {
               if (m_acl.findEntry(m_currentEntry.getTypedPrincipal()) != null
                  && m_acl.isOwner(m_currentEntry.getPrincipal()))
               {
                  m_acl.deleteOwner(m_acl.getFirstOwner(), m_currentEntry
                     .getPrincipal());
               }
            }
            catch (SecurityException e)
            {
               ex = e;
            }
            catch (NotOwnerException e)
            {
               ex = e;
            }
            catch (LastOwnerException e)
            {
               ex = e;
            }
            if (ex != null)
            {
               displayAclError(ex);
               m_checkDesignModifyAcl.setSelection(true);
            }
         }
      }
      if (control == null || control == m_checkRuntimeRead)
      {
         if (m_checkRuntimeRead.getSelection())
         {
            m_currentEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
         }
         else
         {
            m_currentEntry.removePermission(PSPermissions.RUNTIME_VISIBLE);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   @Override
   public void dispose()
   {
      super.dispose();
      m_acl = null;
   }

   @Override
   protected void checkSubclass()
   {
   }

   private void displayAclError(Exception e)
   {
      String msg = PSMessages.getString("PSAclComposite.eror.unknown"); //$NON-NLS-1$
      if (e instanceof NotOwnerException)
      {
         msg = PSMessages.getString("PSAclComposite.error.userNotOwner"); //$NON-NLS-1$
      }
      if (e instanceof LastOwnerException)
      {
         msg = PSMessages
            .getString("PSAclComposite.error.lastOwnerCannotBeRemoved"); //$NON-NLS-1$
      }
      PSErrorDialog dlg = new PSErrorDialog(getShell(), msg, e);
      dlg.open();
   }

   IPSAcl m_acl = new PSAclImpl();

   // All ui components used
   private SashForm m_sashForm;

   private Group m_groupLeft;

   private Button m_buttonNewAclEntry;

   private Button m_buttonDelete;

   private TableViewer m_tableViewer;

   private Table m_tableAclEntries;

   private Group m_groupRight;

   private Button m_checkDesignRead;

   private Button m_checkDesignUpdate;

   private Button m_checkDesignDelete;

   private Button m_checkDesignModifyAcl;

   private Button m_checkRuntimeRead;

   private IPSAclEntry m_currentEntry = null;

   private boolean m_isOwner = true;
}
