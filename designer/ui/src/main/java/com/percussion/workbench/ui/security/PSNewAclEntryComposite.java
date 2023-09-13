/******************************************************************************
 *
 * [ PSNewAclEntryComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.security;

import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSButtonFactory;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.security.PSAclDialog.EntryTypePair;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * ACL entry selection composite. This has two list boxes on the left and one
 * table on the right. The list boxes render the roles and communities while the
 * table renders the selected entries. Buttons exist to move selected or all
 * roles or communities to the selected table. Also, buttons exist to removed
 * selected or all roles and communities from the table. Buttons exist to add a
 * user or renive the user.
 */
public class PSNewAclEntryComposite extends Composite implements IPSUiConstants
{
   /**
    * The ctor
    * 
    * @param parent the parent control for this control
    * @param style the style hints for this control
    * @param availRolesLabel the label for the available (left-bottom) list,
    * cannot be <code>null</code> or empty.
    * @param availCommLabel the label for the available (left) list, cannot be
    * <code>null</code> or empty.
    * @param selectLabel the label for the selected (right) list, cannot be
    * <code>null</code> or empty.
    * @param labelProvider the label provider to display the list entries,
    * cannot be <code>null</code>.
    */
   public PSNewAclEntryComposite(Composite parent, int style,
      String availCommLabel, String availRolesLabel, String selectLabel,
      ILabelProvider labelProvider)
   {
      this(parent, style, availCommLabel, availRolesLabel, selectLabel, null,
         null, null, labelProvider);
   }

   /**
    * The ctor
    * 
    * @param parent the parent control for this control
    * @param style the style hints for this control
    * @param availRolesLabel the label for the available (left-bottom) list,
    * cannot be <code>null</code> or empty.
    * @param availCommLabel the label for the available (left) list, cannot be
    * <code>null</code> or empty.
    * @param selectLabel the label for the selected (right) list, cannot be
    * <code>null</code> or empty.
    * @param roles list of objects representing items that will appear in the
    * available (left-top) list. May be <code>null</code>or empty.
    * @param communities list of objects representing items that will appear in
    * the available (left) list. May be <code>null</code>or empty.
    * @param selections list of objects representing items that will appear in
    * the selected (right) list. may be <code>null</code> or empty.
    * @param labelProvider the label provider to display the list entries,
    * cannot be <code>null</code>.
    */
   public PSNewAclEntryComposite(Composite parent, int style,
      String availRolesLabel, String availCommLabel, String selectLabel,
      java.util.List roles, java.util.List communities,
      java.util.List selections, ILabelProvider labelProvider)
   {
      super(parent, style);
      if (StringUtils.isBlank(availRolesLabel))
         throw new IllegalArgumentException(
            "availRolesLabel cannot be null or empty."); //$NON-NLS-1$
      if (StringUtils.isBlank(availCommLabel))
         throw new IllegalArgumentException(
            "availCommLabel cannot be null or empty."); //$NON-NLS-1$
      if (StringUtils.isBlank(selectLabel))
         throw new IllegalArgumentException(
            "selectLabel cannot be null or empty."); //$NON-NLS-1$
      if (labelProvider == null)
         throw new IllegalArgumentException("Label provider cannot be null."); //$NON-NLS-1$

      setLayout(new FormLayout());

      // Available values composite TOP
      final Composite availableComp = new Composite(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.bottom = new FormAttachment(100, 0);
      formData.right = new FormAttachment(45, 0);
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 0);
      availableComp.setLayoutData(formData);
      availableComp.setLayout(new FormLayout());

      final Composite availableCompTop = new Composite(availableComp, SWT.NONE);
      final FormData formData_1 = new FormData();
      formData_1.bottom = new FormAttachment(50, 0);
      formData_1.right = new FormAttachment(100, 0);
      formData_1.top = new FormAttachment(0, 0);
      formData_1.left = new FormAttachment(0, 0);
      availableCompTop.setLayoutData(formData_1);
      availableCompTop.setLayout(new FormLayout());

      final Label availableLabel = new Label(availableCompTop, SWT.NONE);
      final FormData formData_1a = new FormData();
      formData_1a.right = new FormAttachment(100, 0);
      formData_1a.top = new FormAttachment(0, 0);
      formData_1a.left = new FormAttachment(0, 0);
      availableLabel.setLayoutData(formData_1a);
      availableLabel.setText(availRolesLabel);

      m_rolesListViewer = new ListViewer(availableCompTop, SWT.V_SCROLL
         | SWT.MULTI | SWT.BORDER);
      m_rolesListViewer.setContentProvider(new PSDefaultContentProvider());
      m_rolesListViewer.setLabelProvider(labelProvider);
      m_rolesListViewer.setSorter(new ViewerSorter());
      m_rolesList = m_rolesListViewer.getList();
      m_rolesList.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")//$NON-NLS-1$
         public void widgetSelected(SelectionEvent e)
         {
            m_selectedList.deselectAll();
            updateButtonStatus();
         }
      });
      final FormData formData_2 = new FormData();
      formData_2.bottom = new FormAttachment(100, 0);
      formData_2.right = new FormAttachment(100, 0);
      formData_2.top = new FormAttachment(availableLabel, 0, SWT.BOTTOM);
      formData_2.left = new FormAttachment(0, 0);
      m_rolesList.setLayoutData(formData_2);

      // Available values composite BOTTOM

      final Composite availableCompBottom = new Composite(availableComp,
         SWT.NONE);
      final FormData formData_B = new FormData();
      formData_B.bottom = new FormAttachment(100, 0);
      formData_B.right = new FormAttachment(100, 0);
      formData_B.top = new FormAttachment(50, 0);
      formData_B.left = new FormAttachment(0, 0);
      availableCompBottom.setLayoutData(formData_B);
      availableCompBottom.setLayout(new FormLayout());

      final Label availableLabelBottom = new Label(availableCompBottom,
         SWT.NONE);
      final FormData formData_B1 = new FormData();
      formData_B1.right = new FormAttachment(100, 0);
      formData_B1.top = new FormAttachment(0, 0);
      formData_B1.left = new FormAttachment(0, 0);
      availableLabelBottom.setLayoutData(formData_B1);
      availableLabelBottom.setText(availCommLabel);

      m_communitiesListViewer = new ListViewer(availableCompBottom,
         SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
      m_communitiesListViewer
         .setContentProvider(new PSDefaultContentProvider());
      m_communitiesListViewer.setLabelProvider(labelProvider);
      m_communitiesListViewer.setSorter(new ViewerSorter());
      m_communitiesList = m_communitiesListViewer.getList();
      m_communitiesList.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")//$NON-NLS-1$
         public void widgetSelected(SelectionEvent e)
         {
            m_selectedList.deselectAll();
            updateButtonStatus();
         }
      });
      final FormData formData_B2 = new FormData();
      formData_B2.bottom = new FormAttachment(100, 0);
      formData_B2.right = new FormAttachment(100, 0);
      formData_B2.top = new FormAttachment(availableLabelBottom, 0, SWT.BOTTOM);
      formData_B2.left = new FormAttachment(0, 0);
      m_communitiesList.setLayoutData(formData_B2);

      // filler composite
      final Composite fillerComp1 = new Composite(this, SWT.NONE);
      fillerComp1.setLayout(new FormLayout());
      final FormData formData_100 = new FormData();
      formData_100.right = new FormAttachment(55, 0);
      formData_100.top = new FormAttachment(0, 13);
      formData_100.left = new FormAttachment(availableComp, 0, SWT.RIGHT);
      formData_100.bottom = new FormAttachment(100, 0);
      fillerComp1.setLayoutData(formData_100);

      // filler composite
      final Composite fillerComp2 = new Composite(fillerComp1, SWT.NONE);
      final FormData formData_200 = new FormData();
      formData_200.bottom = new FormAttachment(100, 0);
      formData_200.top = new FormAttachment(0, 0);
      formData_200.left = new FormAttachment(0, 0);
      formData_200.right = new FormAttachment(0, 0);
      fillerComp2.setLayoutData(formData_200);

      // Top Button composite
      final Composite buttonComp = new Composite(fillerComp1, SWT.NONE);
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, 0);
      formData_4.top = new FormAttachment(25,
         -(BUTTON_HEIGHT + BUTTON_VSPACE_OFFSET / 2));
      formData_4.left = new FormAttachment(0, 0);
      buttonComp.setLayoutData(formData_4);
      buttonComp.setLayout(new FormLayout());

      m_selectRoleButton = PSButtonFactory.createRightButton(buttonComp);
      m_selectRoleButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            select(PrincipalTypes.ROLE, false);
         }
      });
      final FormData formData_5 = new FormData();
      formData_5.height = BUTTON_HEIGHT;
      formData_5.right = new FormAttachment(100, -BUTTON_HSPACE_OFFSET);
      formData_5.top = new FormAttachment(0, 0);
      formData_5.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_selectRoleButton.setLayoutData(formData_5);
      m_selectRoleButton.setToolTipText(PSMessages
         .getString("PSNewAclEntryComposite.select.tooltip")); //$NON-NLS-1$

      m_selectallRolesButton = PSButtonFactory
         .createDoubleRightButton(buttonComp);
      m_selectallRolesButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            select(PrincipalTypes.ROLE, true);
         }
      });
      final FormData formData_7 = new FormData();
      formData_7.height = BUTTON_HEIGHT;
      formData_7.right = new FormAttachment(100, -BUTTON_HSPACE_OFFSET);
      formData_7.top = new FormAttachment(m_selectRoleButton, 2, SWT.BOTTOM);
      formData_7.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_selectallRolesButton.setLayoutData(formData_7);
      m_selectallRolesButton.setToolTipText(PSMessages
         .getString("PSNewAclEntryComposite.selectall.tooltip")); //$NON-NLS-1$

      // Bottom Button composite
      final Composite buttonComp21 = new Composite(fillerComp1, SWT.NONE);
      final FormData formData_421 = new FormData();
      formData_421.right = new FormAttachment(100, 0);
      formData_421.top = new FormAttachment(50,
         -(BUTTON_HEIGHT + BUTTON_VSPACE_OFFSET / 2));
      formData_421.left = new FormAttachment(0, 0);
      buttonComp21.setLayoutData(formData_421);
      buttonComp21.setLayout(new FormLayout());

      m_unselectButton = PSButtonFactory.createLeftButton(buttonComp21);
      m_unselectButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            unSelect(false);
         }
      });

      final FormData formData_6 = new FormData();
      formData_6.height = BUTTON_HEIGHT;
      formData_6.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      formData_6.right = new FormAttachment(100, -BUTTON_HSPACE_OFFSET);
      formData_6.top = new FormAttachment(m_selectallRolesButton, 2, SWT.BOTTOM);
      new FormAttachment(0, 0);
      m_unselectButton.setLayoutData(formData_6);
      m_unselectButton.setToolTipText(PSMessages
         .getString("PSNewAclEntryComposite.unselect.tooltip")); //$NON-NLS-1$

      m_unselectallButton = PSButtonFactory
         .createDoubleLeftButton(buttonComp21);
      m_unselectallButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            unSelect(true);
         }
      });
      final FormData formData_8 = new FormData();
      formData_8.height = BUTTON_HEIGHT;
      formData_8.right = new FormAttachment(100, -BUTTON_HSPACE_OFFSET);
      formData_8.top = new FormAttachment(m_unselectButton, 2, SWT.BOTTOM);
      formData_8.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_unselectallButton.setLayoutData(formData_8);
      m_unselectallButton.setToolTipText(PSMessages
         .getString("PSNewAclEntryComposite.unselectall.tooltip")); //$NON-NLS-1$

      // Bottom Button composite
      final Composite buttonComp2 = new Composite(fillerComp1, SWT.NONE);
      final FormData formData_42 = new FormData();
      formData_42.right = new FormAttachment(100, 0);
      formData_42.left = new FormAttachment(0, 0);
      formData_42.top = new FormAttachment(75,
         -(BUTTON_HEIGHT + BUTTON_VSPACE_OFFSET / 2));
      buttonComp2.setLayoutData(formData_42);
      buttonComp2.setLayout(new FormLayout());

      m_selectCommunitiesButton = PSButtonFactory
         .createRightButton(buttonComp2);
      m_selectCommunitiesButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            select(PrincipalTypes.COMMUNITY, false);
         }
      });
      final FormData formData_52 = new FormData();
      formData_52.height = BUTTON_HEIGHT;
      formData_52.right = new FormAttachment(100, -BUTTON_HSPACE_OFFSET);
      formData_52.top = new FormAttachment(0, 0);
      formData_52.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_selectCommunitiesButton.setLayoutData(formData_52);
      m_selectCommunitiesButton.setToolTipText(PSMessages
         .getString("PSNewAclEntryComposite.select.tooltip")); //$NON-NLS-1$

      m_selectallCommunitiesButton = PSButtonFactory
         .createDoubleRightButton(buttonComp2);
      m_selectallCommunitiesButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            select(PrincipalTypes.COMMUNITY, true);
         }
      });

      final FormData formData_72 = new FormData();
      formData_72.height = BUTTON_HEIGHT;
      formData_72.right = new FormAttachment(100, -BUTTON_HSPACE_OFFSET);
      formData_72.top = new FormAttachment(m_selectCommunitiesButton, 2,
         SWT.BOTTOM);
      formData_72.left = new FormAttachment(0, BUTTON_HSPACE_OFFSET);
      m_selectallCommunitiesButton.setLayoutData(formData_72);
      m_selectallCommunitiesButton.setToolTipText(PSMessages
         .getString("PSNewAclEntryComposite.selectall.tooltip")); //$NON-NLS-1$

      // Selected values composite
      final Composite selectedComp = new Composite(this, SWT.NONE);
      final FormData formData_3 = new FormData();
      formData_3.bottom = new FormAttachment(100,
         -(BUTTON_HEIGHT + 2 * BUTTON_VSPACE_OFFSET));
      formData_3.top = new FormAttachment(0, 0);
      formData_3.right = new FormAttachment(100, 0);
      formData_3.left = new FormAttachment(fillerComp1, 0, SWT.RIGHT);
      selectedComp.setLayoutData(formData_3);
      selectedComp.setLayout(new FormLayout());

      final Label selectedLabel = new Label(selectedComp, SWT.NONE);
      final FormData formData_1_1 = new FormData();
      formData_1_1.top = new FormAttachment(0, 0);
      formData_1_1.right = new FormAttachment(100, 0);
      formData_1_1.left = new FormAttachment(0, 0);
      selectedLabel.setLayoutData(formData_1_1);
      selectedLabel.setText(selectLabel);

      m_selectedListViewer = new TableViewer(selectedComp, SWT.V_SCROLL
         | SWT.MULTI | SWT.BORDER);
      m_selectedListViewer.setContentProvider(new PSDefaultContentProvider());
      m_selectedListViewer.setLabelProvider(labelProvider);
      m_selectedListViewer.setSorter(new ViewerSorter());
      m_selectedListViewer.setSorter(new ViewerSorter()
      {
         @Override
         public int compare(@SuppressWarnings("unused") Viewer viewer,
               Object e1, Object e2)
         {
            EntryTypePair pair1 = (EntryTypePair) e1;
            EntryTypePair pair2 = (EntryTypePair) e2;

            // First sort by the entry type
            int order = pair2.type.getOrdinal() - pair1.type.getOrdinal();
            if (order == 0)
            {
               // then sort by the name in the entry
               return pair1.name.compareTo(pair2.name);
            }
            return order;
         }
      });

      m_selectedList = m_selectedListViewer.getTable();
      m_selectedList.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            m_rolesList.deselectAll();
            updateButtonStatus();
         }
      });

      final FormData formData_2_1 = new FormData();
      formData_2_1.bottom = new FormAttachment(100, 0);
      formData_2_1.top = new FormAttachment(selectedLabel, 0, SWT.BOTTOM);
      formData_2_1.right = new FormAttachment(100, 0);
      formData_2_1.left = new FormAttachment(0, 0);
      m_selectedList.setLayoutData(formData_2_1);

      m_deleteUserButton = new Button(this, SWT.NONE);
      final FormData formData_bt2 = new FormData();
      formData_bt2.top = new FormAttachment(selectedComp, BUTTON_VSPACE_OFFSET,
         SWT.BOTTOM);
      formData_bt2.right = new FormAttachment(100, 0);
      formData_bt2.height = BUTTON_HEIGHT;
      m_deleteUserButton.setLayoutData(formData_bt2);
      m_deleteUserButton.setText(PSMessages
         .getString("PSNewAclEntryComposite.button.deleteUser")); //$NON-NLS-1$
      m_deleteUserButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            deleteUsers();
         }
      });

      m_newUserButton = new Button(this, SWT.NONE);
      final FormData formData_bt1 = new FormData();
      formData_bt1.top = new FormAttachment(selectedComp, BUTTON_VSPACE_OFFSET,
         SWT.BOTTOM);
      formData_bt1.right = new FormAttachment(m_deleteUserButton,
         -BUTTON_HSPACE_OFFSET, SWT.LEFT);
      formData_bt1.height = BUTTON_HEIGHT;
      m_newUserButton.setLayoutData(formData_bt1);
      m_newUserButton.setText(PSMessages
         .getString("PSNewAclEntryComposite.button.newUser")); //$NON-NLS-1$
      m_newUserButton.addSelectionListener(new SelectionAdapter()
      {
         @Override
         @SuppressWarnings("unused")
         public void widgetSelected(SelectionEvent e)
         {
            addUser();
         }
      });

      setValues(roles, communities, selections);
      //

   }

   /**
    * 
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   protected void addUser()
   {
      PSTextInputDialog dlg = new PSTextInputDialog(getShell(), PSMessages
         .getString("PSNewAclEntryComposite.PSTextInputDialog.title")) //$NON-NLS-1$
      {
         @Override
         protected boolean isInputValid(String userName)
         {
            if (userName.length() == 0 || userName.length()>50)
            {
               MessageDialog
                  .openError(
                     getShell(),
                     PSMessages
                        .getString("PSNewAclEntryComposite.mbox.label.error"), //$NON-NLS-1$
                     PSMessages
                        .getString("PSNewAclEntryComposite.mbox.msg.invalidUsername")); //$NON-NLS-1$
            }
            return (PSStringUtils.validateUserName(userName) != null && !MessageDialog
               .openConfirm(getShell(), PSMessages
                  .getString("PSNewAclEntryComposite.mbox.label.warning"), //$NON-NLS-1$
                  PSMessages
                     .getString("PSNewAclEntryComposite.mbox.msg.specialChars"))); //$NON-NLS-1$
         }
      }; //$NON-NLS-1$
      if (dlg.open() == Dialog.OK)
      {
         String userName = dlg.getTextValue();
         java.util.List from = (java.util.List) m_selectedListViewer.getInput();
         from.add(new EntryTypePair(PrincipalTypes.USER, userName));
         fireSelectionEvent();
         m_selectedListViewer.refresh();
         updateButtonStatus();
      }
   }

   /**
    * 
    */
   protected void deleteUsers()
   {
      java.util.List from = (java.util.List) m_selectedListViewer.getInput();
      if (m_selectedList.getSelectionCount() > 0)
      {
         TableItem[] items = m_selectedList.getSelection();
         for (int i = items.length - 1; i >= 0; i--)
         {
            TableItem item = items[i];
            EntryTypePair pair = (EntryTypePair) item.getData();
            if (pair.type.equals(PrincipalTypes.USER))
               from.remove(pair);
         }
         fireSelectionEvent();
         m_selectedListViewer.refresh();
         updateButtonStatus();
      }
   }

   @Override
   public Point computeSize(@SuppressWarnings("unused") int w, int h,
         boolean bool)
   {
      // Suggest that the control have an initial width of
      // WIDTH_HINT
      return super.computeSize(WIDTH_HINT, h, bool);
   }

   /**
    * Get all the selected items for this control.
    * 
    * @return list of selected objects, never <code>null</code>, may be
    * empty. This list is not sorted.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public java.util.List getSelections()
   {
      return (java.util.List) m_selectedListViewer.getInput();
   }

   /**
    * Get all the available items for this control.
    * 
    * @return list of available objects, never <code>null</code>, may be
    * empty. This list is not sorted.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public java.util.List getAvailable()
   {
      return (java.util.List) m_rolesListViewer.getInput();
   }

   /**
    * Sets the values for the two list boxes in this control
    * 
    * @param roles list of roles representing items that will appear in the
    * available (left) list. May be <code>null</code> or empty.
    * @param communities list of communities representing items that will appear
    * in the available (left) list. May be <code>null</code> or empty.
    * @param selections list of objects representing items that will appear in
    * the selected (right) list. May be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   public void setValues(java.util.List roles, java.util.List communities,
      java.util.List selections)
   {
      java.util.List clonedSelections = new ArrayList();
      java.util.List clonedAvailableRoles = new ArrayList();
      java.util.List clonedAvailableCommunites = new ArrayList();

      if (roles != null)
         clonedAvailableRoles.addAll(roles);
      if (communities != null)
         clonedAvailableCommunites.addAll(communities);

      if (selections != null)
      {
         clonedSelections.addAll(selections);
         // remove any items that exist in both lists from the available
         // list
         Iterator it = clonedSelections.iterator();
         while (it.hasNext())
         {
            Object obj = it.next();
            if (clonedAvailableRoles.contains(obj))
               clonedAvailableRoles.remove(obj);
            else if (clonedAvailableCommunites.contains(obj))
               clonedAvailableCommunites.remove(obj);
         }
      }
      m_selectedListViewer.setInput(clonedSelections);
      m_rolesListViewer.setInput(clonedAvailableRoles);
      m_communitiesListViewer.setInput(clonedAvailableCommunites);
      updateButtonStatus();
   }

   /**
    * Add a selection listener to be notified when a selection event occurs.
    * 
    * @param listener cannot be <code>null</code>.
    */
   public void addSelectionListener(SelectionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if (!m_selectionListeners.contains(listener))
         m_selectionListeners.add(listener);
   }

   /**
    * Removes the specified selection listener
    * 
    * @param listener cannot be <code>null</code>.
    */
   public void removeSelectionListener(SelectionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener cannot be null"); //$NON-NLS-1$
      if (m_selectionListeners.contains(listener))
         m_selectionListeners.remove(listener);
   }

   /**
    * Fires a <code>SelectionEvent</code> for all registered
    * <code>SelectionListeners</code>.
    */
   private void fireSelectionEvent()
   {
      Event e = new Event();
      e.item = this;
      e.widget = this;
      SelectionEvent event = new SelectionEvent(e);
      for (SelectionListener listener : m_selectionListeners)
      {
         listener.widgetSelected(event);
      }
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }

   /**
    * Utility method to select items from the role and community lists.
    * 
    * @param type entry type enumeration, assumed not <code>null</code>
    * @param isAll <code>true</code> to select all entries of the supplied
    * type.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private void select(PrincipalTypes type, boolean isAll)
   {
      ListViewer fromList = null;
      switch (type)
      {
         case ROLE:
            fromList = m_rolesListViewer;
            break;
         case COMMUNITY:
            fromList = m_communitiesListViewer;
            break;

         default:
            break;
      }
      if (fromList == null)
         return;
      java.util.List from = (java.util.List) fromList.getInput();
      java.util.List to = (java.util.List) m_selectedListViewer.getInput();
      if (isAll && !from.isEmpty())
         fromList.getList().selectAll();
      if (fromList.getList().getSelectionCount() > 0)
      {
         StructuredSelection selection = (StructuredSelection) fromList
            .getSelection();
         Iterator it = selection.iterator();
         while (it.hasNext())
         {
            Object obj = it.next();
            from.remove(obj);
            to.add(obj);
         }

         fireSelectionEvent();
         fromList.refresh();
         m_selectedListViewer.refresh();
         updateButtonStatus();
      }
   }

   /**
    * Utility method to unselect roles and communities from the table.
    * 
    * @param isAll if <code>true</code> all roles and communities will be
    * moved to thier respective lists on the left, otherwise only the selected
    * entries move.
    */
   @SuppressWarnings("unchecked")//$NON-NLS-1$
   private void unSelect(boolean isAll)
   {
      java.util.List from = (java.util.List) m_selectedListViewer.getInput();
      // java.util.List to = (java.util.List) toList.getInput();
      if (isAll && !from.isEmpty())
         m_selectedList.selectAll();
      if (m_selectedList.getSelectionCount() > 0)
      {
         TableItem[] items = m_selectedList.getSelection();
         for (int i = items.length - 1; i >= 0; i--)
         {
            TableItem item = items[i];
            EntryTypePair pair = (EntryTypePair) item.getData();
            if (pair.type.equals(PrincipalTypes.ROLE))
            {
               ((java.util.List) m_rolesListViewer.getInput()).add(pair);
            }
            else if (pair.type.equals(PrincipalTypes.COMMUNITY))
            {
               ((java.util.List) m_communitiesListViewer.getInput()).add(pair);
            }
            else
            {
               continue;
            }
            from.remove(pair);
         }

         fireSelectionEvent();
         m_rolesListViewer.refresh();
         m_communitiesListViewer.refresh();
         m_selectedListViewer.refresh();
         updateButtonStatus();
      }
   }

   /**
    * Updates the enable status of the buttons
    */
   protected void updateButtonStatus()
   {
      m_deleteUserButton.setEnabled(getSelectedUserCount() > 0);
      m_selectRoleButton.setEnabled(m_rolesList.getSelectionCount() > 0);
      m_unselectButton.setEnabled(m_selectedList.getSelectionCount() > 0);
      m_selectallRolesButton.setEnabled(m_rolesList.getItemCount() > 0);
      m_unselectallButton.setEnabled(m_selectedList.getItemCount() > 0);
      m_selectCommunitiesButton.setEnabled(m_communitiesList
         .getSelectionCount() > 0);
      m_selectallCommunitiesButton
         .setEnabled(m_communitiesList.getItemCount() > 0);
   }

   /**
    * Helper method to get the selected user count.
    * 
    * @return user count computed from the entries of type
    * {@link PrincipalTypes#USER}
    */
   private int getSelectedUserCount()
   {
      int count = 0;
      if (m_selectedList.getSelectionCount() > 0)
      {
         TableItem[] items = m_selectedList.getSelection();
         for (int i = items.length - 1; i >= 0; i--)
         {
            TableItem item = items[i];
            EntryTypePair pair = (EntryTypePair) item.getData();
            if (pair.type.equals(PrincipalTypes.USER))
               count++;
         }
      }
      return count;
   }

   /**
    * List control displaying available items.
    * 
    * @return available list box, never <code>null</code>.
    */
   public List getAvailableList()
   {
      return m_rolesList;
   }

   /**
    * Table control displaying selected entries.
    * 
    * @return table control rendering the selected entries, never
    * <code>null</code>
    */
   public Table getSelectedList()
   {
      return m_selectedList;
   }

   /**
    * Adds a filter for the available list.
    * 
    * @param filter cannot be <code>null</code>.
    */
   public void addAvailableListFilter(ViewerFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("filter cannot be null"); //$NON-NLS-1$
      if (m_rolesListViewer != null)
      {
         m_rolesListViewer.addFilter(filter);
      }
   }

   /**
    * Removes the specified filter from the available list.
    * 
    * @param filter cannot be <code>null</code>.
    */
   public void removeAvailableListFilter(ViewerFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("filter cannot be null"); //$NON-NLS-1$
      if (m_rolesListViewer != null)
      {
         m_rolesListViewer.removeFilter(filter);
      }
   }

   /**
    * Controls
    */
   private Table m_selectedList;

   private List m_rolesList;

   private List m_communitiesList;

   protected ListViewer m_rolesListViewer;

   protected ListViewer m_communitiesListViewer;

   TableViewer m_selectedListViewer;

   private Button m_selectRoleButton;

   private Button m_unselectButton;

   private Button m_selectallRolesButton;

   private Button m_unselectallButton;

   private Button m_selectCommunitiesButton;

   private Button m_selectallCommunitiesButton;

   private Button m_newUserButton;

   private Button m_deleteUserButton;

   /**
    * List of selection listeners
    */
   private java.util.List<SelectionListener> m_selectionListeners = new ArrayList<SelectionListener>();

   @SuppressWarnings("unused")//$NON-NLS-1$
   private static final int MIN_HEIGHT = 130;

   private static final int WIDTH_HINT = 500;
}
