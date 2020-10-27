/******************************************************************************
 *
 * [ PSAclEntryDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.security;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSBaseDialog;
import com.percussion.workbench.ui.security.PSAclDialog.EntryTypePair;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ACL entry dialog implemenetaion.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSAclEntryDialog extends PSBaseDialog
{
   /**
    * Ctor that takes the ACl Entry object and renders in the dialog fo
    * rmodifications.
    * 
    * @param parent parent shell to pass to super ctor.
    * @param entryTypePairs existing ACL entry pairs to filter off from
    * available lists, may be <code>null</code> or empty.
    */
   public PSAclEntryDialog(Shell parent, List<EntryTypePair> entryTypePairs)
   {
      super(parent, null);
      setShellStyle(getShellStyle() | SWT.RESIZE);
      m_selEntries = new ArrayList<EntryTypePair>();
      if (entryTypePairs == null)
         entryTypePairs = new ArrayList<EntryTypePair>();

      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.COMMUNITY);
         Collection<IPSReference> refs = model.catalog();
         m_availComms = new ArrayList<EntryTypePair>(refs.size());
         for (IPSReference ref : refs)
         {
            EntryTypePair pair = new EntryTypePair(PrincipalTypes.COMMUNITY, ref
               .getName());
            if (!entryTypePairs.contains(pair))
               m_availComms.add(pair);
         }
         model = PSCoreFactory.getInstance().getModel(PSObjectTypes.ROLE);
         refs = model.catalog();
         m_availRoles = new ArrayList<EntryTypePair>(refs.size());
         for (IPSReference ref : refs)
         {
            EntryTypePair pair = new EntryTypePair(PrincipalTypes.ROLE, ref
               .getName());
            if (!entryTypePairs.contains(pair))
               m_availRoles.add(pair);
         }
      }
      catch (PSModelException e)
      {
         // Error dlg
         e.printStackTrace();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite comp)
   {
      Composite mainComp = new Composite(comp, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      FormData fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(0, 0);
      mainComp.setLayoutData(fd);

      m_newAclComp = new PSNewAclEntryComposite(mainComp, SWT.NONE, PSMessages
         .getString("PSAclEntryDialog.label.roles"), //$NON-NLS-1$
         PSMessages.getString("PSAclEntryDialog.label.communities"), //$NON-NLS-1$
         PSMessages.getString("PSAclEntryDialog.labl.addToAcl"), //$NON-NLS-1$
         m_availRoles, m_availComms, m_selEntries, new LabelProvider()
         {
            @Override
            public Image getImage(Object element)
            {
               return getTypeImage(((EntryTypePair) element));
            }

            @Override
            public String getText(Object element)
            {
               return ((EntryTypePair) element).name;
            }
         });
      FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 10);
      fd1.top = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, -10);
      fd1.bottom = new FormAttachment(100, 0);
      m_newAclComp.setLayoutData(fd1);

      GridData data = new GridData(GridData.FILL_HORIZONTAL
         | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      mainComp.setLayoutData(data);
      return mainComp;
   }

   /**
    * Should perform validation and transfer data from the dialog to the object.
    * 
    * @return <code>true</code> if validation succeeded and the object is
    * ready to save, <code>false</code> otherwise. In the latter case, the
    * user should be notified of the problem.
    */
   @Override
   protected boolean accepted()
   {
      return false;
   }

   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages
         .getString("PSAclEntryDialog.title.addAclEntry")); //$NON-NLS-1$
   }

   @Override
   protected Point getInitialSize()
   {
      return new Point(500, 400);
   }

   /**
    * Same as {@link #getTypeImage(IPSAclEntry)} except it takes
    * {@link com.percussion.workbench.ui.security.PSAclDialog.EntryTypePair}.
    * It actually creates a dummy ACL entry and invokes the other version.
    * 
    * @param pair name-type pair, must not be <code>null</code>
    * @return image for the type, never <code>null</code>. If not found the
    * right one a default one will be returned.
    */
   public static Image getTypeImage(EntryTypePair pair)
   {
      if (pair == null)
         throw new IllegalArgumentException("pair must not be null");

      IPSAclEntry tempEntry = new PSAclEntryImpl(new PSTypedPrincipal(
         pair.name, pair.type));
      return getTypeImage(tempEntry);
   }

   /**
    * Get image for ACL entry. Does not need to be a valid entry as long as it
    * has the pricnipal and type set.
    * 
    * @param entry ACL entry, must not be <code>null</code>
    * @return image for the entry type, never <code>null</code>. If not found
    * the right one a default one will be returned.
    */
   @SuppressWarnings("deprecation")
   public static Image getTypeImage(IPSAclEntry entry)
   {
      if (entry == null)
         throw new IllegalArgumentException("entry must not be null");

      String key = null;
      if (entry.isSystemCommunity())
      {
         key = "icons/syscomm16.gif"; //$NON-NLS-1$
      }
      else if (entry.isSystemEntry())
      {
         key = "icons/sysentry16.gif"; //$NON-NLS-1$
      }
      else if (entry.isCommunity())
      {
         key = "icons/community16.gif"; //$NON-NLS-1$
      }
      else if (entry.isRole())
      {
         key = "icons/role16.gif"; //$NON-NLS-1$
      }
      else if (entry.isUser())
      {
         key = "icons/user16.gif"; //$NON-NLS-1$
      }
      return PSUiUtils.getImage(key);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.dialog.PSBaseDialog#okPressed()
    */
   @SuppressWarnings("unchecked")
   @Override
   protected void okPressed()
   {
      List entries = (List) m_newAclComp.m_selectedListViewer.getInput();
      m_selEntries.addAll(entries);
      super.okPressed();
   }

   /**
    * Access method for final selected entries which is the result from this
    * dialog.
    * 
    * @return may be <code>null</code> or empty.
    */
   public List<EntryTypePair> getEntries()
   {
      return m_selEntries;
   }

   /**
    * Reference to the compiosite that fills the main dialog area. Created in
    * {@link #createDialogArea(Composite)} and never <code>null</code> after
    * that.
    */
   public PSNewAclEntryComposite m_newAclComp;

   /**
    * List of available communities. Catalogged from the server in the ctor.
    */
   private List<EntryTypePair> m_availComms = null;

   /**
    * List of available roles. Catalogged from the server in the ctor.
    */
   private List<EntryTypePair> m_availRoles = null;

   /**
    * List of selected entries that cn be roles, communities or users.
    * 
    * @see #getEntries()
    */
   private List<EntryTypePair> m_selEntries = null;

}
