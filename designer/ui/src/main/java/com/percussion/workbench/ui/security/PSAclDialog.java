/******************************************************************************
 *
 * [ PSAclDialog.java ]
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
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSBaseDialog;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;

import java.io.IOException;
import java.security.acl.AclEntry;
import java.security.acl.NotOwnerException;
import java.util.Enumeration;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

/**
 * ACL dialog. The composite that is rendered
 * {@link com.percussion.workbench.ui.security.PSAclComposite} in the dialog
 * area is built as separate composite using the Visual Editor.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSAclDialog extends PSBaseDialog implements IPSUiConstants
{
   /**
    * Reference to the compiosite that fills the main dialog area. Created in
    * {@link #createDialogArea(Composite)} and never <code>null</code> after
    * that.
    */
   private PSAclComposite m_aclComposite;

   /**
    * Reference to the object reference this dialog renders. Initialized in the
    * ctor, never <code>null</code> after that.
    */
   private IPSReference m_ref = null;

   /**
    * Reference to the object whose ACL is rendered by this dialog. Loaded in
    * the ctor, never <code>null</code> after that.
    */
   private IPSAcl m_acl = null;

   /**
    * This flag is used to track whether the acl's lock has been released during
    * shutdown of the dialog so it doesn't occur more than once. Initially set
    * to <code>false</code>.
    */
   private boolean m_lockReleased = false;

   /**
    * A clone of the acl provided in the ctor. It is used when OK is pressed to
    * determine whether to show a warning message that the user may lose her
    * access. If the acl is being opened by a non-owner, it is always
    * <code>null</code>, otherwise, it is never <code>null</code> or
    * modified after it is set in the ctor.
    */
   private final IPSAcl m_aclCopy;
   
   /**
    * The dialog will load the data associated with the supplied reference. It
    * can be retrieved by the derived class by calling {@link #getData()}.
    * 
    * @param parent
    * @param ref
    * @throws Exception If the acl can't be loaded for any reason. If 
    * this happens, a message has already been displayed to the user.
    */
   public PSAclDialog(Shell parent, IPSReference ref) throws Exception
   {
      super(parent, ref);
      if (ref == null)
      {
         throw new IllegalArgumentException("ref must not be null"); //$NON-NLS-1$
      }
      IPSCmsModel model;
      try
      {
         model = PSCoreFactory.getInstance().getModel(ref);
         int [] perms = ref.getPermissions();
         boolean isOwner = false;
         for (int i : perms)
         {
            if(i==PSPermissions.OWNER.getOrdinal())
            {
               isOwner = true;
               break;
            }
         }
         m_acl = (IPSAcl) model.loadAcl(ref, isOwner);
         if (isOwner)
         {
            ensureDefaultAclEntries(m_acl);
            m_aclCopy = createAclCopy(m_acl);
         }
         else
            m_aclCopy = null;
         m_ref = ref;
      }
      catch (Exception e)
      {
         new PSErrorDialog(getShell(), PSMessages
            .getString("PSAclDialog.eror.aclLoadingFailed"), e).open(); //$NON-NLS-1$
         PSUiUtils.log(PSMessages
            .getString("PSAclDialog.eror.aclLoadingFailed"), e);
         throw e;
      }
      setShellStyle(getShellStyle() | SWT.RESIZE);
   }

   /**
    * Creates a deep copy of the supplied acl.
    * 
    * @param acl Assumed not <code>null</code>.
    * 
    * @return The copy. Never <code>null</code>.
    * 
    * @throws IOException Should never happen given what we are doing.
    * @throws SAXException Should never happen given what we are doing.
    */
   private IPSAcl createAclCopy(IPSAcl acl)
      throws IOException, SAXException
   {
      PSAclImpl copy = new PSAclImpl();
      copy.fromXML(((PSAclImpl) acl).toXML());
      return copy;
   }

   /**
    * This method adds two special ACL entries, namely the default user and
    * default community to the supplied ACL if already does not exist. No
    * permissions are set for the entries.
    * 
    * @param acl acl to modify to ensure the existence of default entries.
    * Assumed not <code>null</code>.
    */
   private void ensureDefaultAclEntries(IPSAcl acl)
   {
      try
      {
         IPSAclEntry sysEntry = acl.createDefaultEntry(false);
         if (acl.findEntry(sysEntry.getTypedPrincipal()) == null)
            acl.addEntry(acl.getFirstOwner(), sysEntry);
         IPSAclEntry sysComm = acl.createDefaultEntry(true);
         if (acl.findEntry(sysComm.getTypedPrincipal()) == null)
            acl.addEntry(acl.getFirstOwner(), sysComm);
      }
      catch (SecurityException e)
      {
         // Should not happen since we take the first available ACL owner
         throw new RuntimeException(e);
      }
      catch (NotOwnerException e)
      {
         // Should not happen since we take the first available ACL owner
         throw new RuntimeException(e);
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

      m_aclComposite = new PSAclComposite(mainComp, SWT.NONE,
            PSMessages.getString("PSAclDialog.label.aclEntries"));
      FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.top = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, 0);
      fd1.bottom = new FormAttachment(100, 0);
      m_aclComposite.setLayoutData(fd1);

      GridData data = new GridData(GridData.FILL_HORIZONTAL
         | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      mainComp.setLayoutData(data);

      m_aclComposite.setAcl(m_acl, PSSecurityUtils.hasOwnerPermission(m_ref,
         false));

      return mainComp;
   }

   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      newShell.setText(PSMessages.getString("PSAclDialog.title.objectAcl")); //$NON-NLS-1$
   }

   @Override
   protected Point getInitialSize()
   {
      return new Point(500, 400);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.dialog.PSBaseDialog#okPressed()
    */
   @Override
   protected void okPressed()
   {
      try
      {
         IPSAcl acl = m_aclComposite.getAcl();
         // check if the only other entry is default
         IPSAclEntry entry = acl.findEntry(acl.createDefaultEntry(false)
            .getTypedPrincipal());
         boolean hasDefaultRead = false;
         boolean onlyDefaultEntry = checkOnlyDefaultEntry(acl);
         boolean entryHasOwnerAccess = checkAnyAccess(acl, PSPermissions.OWNER);
         boolean entryHasReadAccess = checkAnyAccess(acl, PSPermissions.READ);
         
         if (entry != null)
         {
            hasDefaultRead = entry.checkPermission(PSPermissions.READ);
         }

         if (!entryHasOwnerAccess)
         {
            //not allowed
            MessageDialog.openWarning(getShell(), 
                  PSMessages.getString(
                     "PSAclDialog.warning.notAllowed.title"),
                  PSMessages.getString(
                     "PSAclDialog.warning.mustHaveOwner.message"));
            return;
         }
         
         if (!entryHasReadAccess)
         {
            //not allowed
            MessageDialog.openWarning(getShell(), 
                  PSMessages.getString(
                     "PSAclDialog.warning.notAllowed.title"),
                  PSMessages.getString(
                     "PSAclDialog.warning.mustHaveReader.message"));
            return;
         }
         
         if (onlyDefaultEntry && !hasDefaultRead)
         {
            boolean ok = MessageDialog
               .openConfirm(
                  getShell(),
                  PSMessages
                     .getString("PSAclDialog.warning.youWillLoseAccess.title"), //$NON-NLS-1$
                  PSMessages
                     .getString("PSAclDialog.warning.noReadAccessForSelf.message")); //$NON-NLS-1$
            if (!ok)
               return;
         }
         
         IPSTypedPrincipal prin = PSCoreFactory.getInstance().getUserPrincipal();
         entry = acl.findEntry(prin);
         boolean userHasEntry = entry != null;
         boolean userHasEntryWithRead = userHasEntry
               && entry.checkPermission(PSPermissions.READ);
         boolean readChanged = aclChangedMeaningfully(m_aclCopy, m_acl,
               PSPermissions.READ);
         boolean ownerChanged = aclChangedMeaningfully(m_aclCopy, m_acl,
               PSPermissions.OWNER);
         
         if (userHasEntry && !userHasEntryWithRead)
         {
            boolean ok = MessageDialog
               .openConfirm(
                  getShell(),
                  PSMessages
                     .getString("PSAclDialog.warning.youWillLoseAccess.title"), //$NON-NLS-1$
                  PSMessages
                     .getString("PSAclDialog.warning.noReadAccessForSelf.message")); //$NON-NLS-1$
            if (!ok)
               return;
         }
         if (!userHasEntry && readChanged)
         {
            boolean ok = MessageDialog.openConfirm(getShell(), PSMessages
               .getString("PSAclDialog.warning.youMayLoseAccess.title"), //$NON-NLS-1$
               PSMessages.getString(
                     "PSAclDialog.warning.readAccessRemoved.message")); //$NON-NLS-1$
            if (!ok)
               return;
         }
         if (!userHasEntry && ownerChanged)
         {
            boolean ok = MessageDialog.openConfirm(getShell(), PSMessages
                  .getString("PSAclDialog.warning.youMayLoseAccess.title"), //$NON-NLS-1$
                  PSMessages.getString(
                        "PSAclDialog.warning.ownerAccessRemoved.message")); //$NON-NLS-1$
               if (!ok)
                  return;
         }
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(m_ref);
         model.saveAcl(m_ref, true);
         m_lockReleased = true;
      }
      catch (PSModelException e)
      {
         new PSErrorDialog(getShell(), PSMessages
            .getString("PSAclDialog.error.aclSaveFailed"), e).open(); //$NON-NLS-1$
         return;
      }
      super.okPressed();
   }

   /**
    * Walks all entries to determine if there are any non-community entries
    * that have the requested permission. 
    * 
    * @param acl Get entries from this acl. Assumed not <code>null</code>.
    * @param perm Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if at least 1 non-community entry has the
    * requested permission, <code>false</code> otherwise.
    */
   private static boolean checkAnyAccess(IPSAcl acl, PSPermissions perm)
   {
      Enumeration<AclEntry> entries = acl.entries();
      while (entries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry) entries.nextElement();
         IPSTypedPrincipal prin = (IPSTypedPrincipal) entry.getPrincipal();
         if (prin.isCommunity())
            continue;
         if (entry.checkPermission(perm))
            return true;
      }
      return false;
   }

   /**
    * Walks all entries to determine if there are any non-community entries
    * other than the Default entry.
    * 
    * @param acl Get entries from this acl. Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if there are no entries or Default is the only
    * non-community entry, <code>false</code> otherwise.
    */
   private static boolean checkOnlyDefaultEntry(IPSAcl acl)
   {
      Enumeration<AclEntry> entries = acl.entries();
      IPSAclEntry defaultEntry = acl.findEntry(acl.createDefaultEntry(false)
            .getTypedPrincipal());
      while (entries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry) entries.nextElement();
         IPSTypedPrincipal prin = (IPSTypedPrincipal) entry.getPrincipal();
         if (prin.isCommunity())
            continue;
         if (entry.equals(defaultEntry))
            continue;
         return false;
      }
      return true;
   }

   /**
    * Checks the requested permission of every role and group entry in the
    * original acl against the corresponding entry in the new acl to see if the
    * supplied permission was removed.
    * 
    * @param original The acl when the dialog was entered. Assumed not
    * <code>null</code>.
    * @param toCheck The modified acl. Assumed not <code>null</code>.
    * @param perm The permission to check for change. Assumed not
    * <code>null</code>.
    * @return <code>true</code> if any role or group entry in original had
    * read access but no longer has it or said entry has been removed,
    * otherwise, <code>false</code>.
    */
   private boolean aclChangedMeaningfully(IPSAcl original, IPSAcl toCheck,
         PSPermissions perm)
   {
      Enumeration<AclEntry> originalEntries = original.entries();
      while (originalEntries.hasMoreElements())
      {
         IPSAclEntry originalEntry = (IPSAclEntry) originalEntries
               .nextElement();
         IPSTypedPrincipal prin = (IPSTypedPrincipal) originalEntry
               .getPrincipal();
         if (prin.isCommunity() || prin.isSubject())
            continue;
         
         Enumeration<AclEntry> toCheckEntries = toCheck.entries();
         String originalEntryName = originalEntry.getName();
         boolean found = false;
         while (toCheckEntries.hasMoreElements())
         {
            IPSAclEntry toCheckEntry = (IPSAclEntry) toCheckEntries
                  .nextElement();
            prin = ((IPSTypedPrincipal) toCheckEntry.getPrincipal());
            if (prin.isCommunity() || prin.isSubject())
               continue;
            if (toCheckEntry.getName().equals(originalEntryName))
            {
               //was permission removed?
               if (originalEntry.checkPermission(perm)
                     && !toCheckEntry.checkPermission(perm))
               {
                  return true;
               }
               found = true;
            }
         }
         if (!found && originalEntry.checkPermission(perm))
            //an original entry with the permission was deleted
            return true;
      }
      return false;
   }

   /**
    * Releases the lock, showing a message to the user if any problems occur.
    */
   private void cleanup()
   {
      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(m_ref);
         model.releaseAclLock(m_ref);
      }
      catch (PSModelException e)
      {
         new PSErrorDialog(getShell(), PSMessages
            .getString("PSAclDialog.aclReleaseLockFailed"), e).open(); //$NON-NLS-1$
      }
   }
   
   //see base class method for details
   @Override
   public boolean close()
   {
      if (!m_lockReleased)
      {
         cleanup();
         m_lockReleased = true;
      }
      return super.close();
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

   /**
    * Helper class to hold the entry type and name for ACL entries.
    */
   static class EntryTypePair
   {
      /**
       * Ctro taking the entry type and name.
       * 
       * @param type entry type enumerated constant, must not be
       * <code>null</code>.
       * @param name name of the entry, must not be <code>null</code>.
       */
      public EntryTypePair(PrincipalTypes type, String name)
      {
         if (type == null)
         {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
         }
         if (name == null || name.length() == 0)
         {
            throw new IllegalArgumentException("name must not be null or empty"); //$NON-NLS-1$
         }
         this.type = type;
         this.name = name;
      }

      /**
       * Entry type enumerated constant as supplied to the ctor.
       */
      PrincipalTypes type;

      /**
       * Name of the entry, as supplied to the ctor.
       */
      String name;
   }
   
   
}
