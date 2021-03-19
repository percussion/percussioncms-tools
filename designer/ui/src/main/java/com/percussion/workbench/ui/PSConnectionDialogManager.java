/******************************************************************************
 *
 * [ PSConnectionDialogManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSinvalidConnectionListener;
import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.workbench.config.PSUiConfigManager;
import com.percussion.workbench.ui.connection.PSConnectionLocalesDialog;
import com.percussion.workbench.ui.connection.PSConnectionsDialog;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class manages the connection dialog and feedback from the core about
 * invalid connection registration information.
 *
 * @author paulhoward
 */
public class PSConnectionDialogManager implements IPSinvalidConnectionListener
{

   /**
    * The only ctor.
    * @param wb Used to get the active window's shell. May be <code>null</code>.
    */
   public PSConnectionDialogManager(IWorkbench wb)
   {
      m_workbench = wb;
      m_invalidConnectionListener = PSCoreFactory.getInstance()
            .getInvalidConnectionListener();
      PSCoreFactory.getInstance().setInvalidConnectionListener(this);
   }

   /**
    * Convenience method that calls
    * {@link #connect(PSConnectionInfo, Object, String) 
    * connect(null, null, closeButtonLabel}.
    */
   public PSConnectionInfo open(String closeButtonLabel)
   {
      return connect(null, null, closeButtonLabel, null);
   }
   
   
   //see interface
   public PSConnectionInfo correctConnection(PSConnectionInfo lastConn,
         Object error, List<String> locales)
   {
      return connect(lastConn, error,
            "PSWorkbenchPlugin.connectDialog.closeButton.label", locales);
   }


   /**
    * Let user choose an enabled locale from a list of MORE THAN ONE enabled.
    * This is *ONLY shown when en_us locale(the default locale) has been
    * disabled *AND* has more than one choice of enabled locales
    * 
    * @param locList the list of enabled locales
    * @return an enabled locale, never <code>null</code> or empty
    */
   private String chooseLocaleDialog(List<String> locList)
   {
      Shell shell = null;
      if (m_workbench != null)
      {
         IWorkbenchWindow win = m_workbench.getActiveWorkbenchWindow();
         shell = win == null ? null : win.getShell();
      }

      PSConnectionLocalesDialog dlg = new PSConnectionLocalesDialog(shell,
            locList);
      dlg.open();
      return dlg.getLocaleSelection();
   }

   /**
    * Launch the connection manager and return the connection info object
    * selected/created by the user.
    * 
    * @param lastConn See param for
    * {@link #correctConnection(PSConnectionInfo, Object, List)}.
    * @param error See param for
    * {@link #correctConnection(PSConnectionInfo, Object, List)}.
    * @param closeButtonLabel The key to use to find the text for the button
    * used to close the dialog. Never <code>null</code> or empty.
    * @param locales the valid locales for this connection see param for
    * {@link #correctConnection(PSConnectionInfo, Object, List)}.
    * @return A valid connection registration, or <code>null</code> if the
    * user cancels the dialog.
    */
   public PSConnectionInfo connect(PSConnectionInfo lastConn,
         Object error, String closeButtonLabel, List<String> locales)
   {
      if (m_dlg == null)
      {
         Shell shell = null;
         if (m_workbench != null)
         {
            IWorkbenchWindow win = m_workbench.getActiveWorkbenchWindow();
            shell = win == null ? null : win.getShell();
         }
         m_dlg = new PSConnectionsDialog(shell, closeButtonLabel);
      }

      String errorMsg = PSMessages
            .getString("PSWorkbenchPlugin.msg.default.titlearea.choose_or_configure_connection");
      if (lastConn != null)
         m_dlg.setLastConnection(lastConn);

      if (error != null)
      {
         errorMsg = PSCoreFactory.getInstance().getFaultMessage(error);
         if(errorMsg != null && errorMsg.contains("com.percussion.conn.PSServerException: null")){
            errorMsg = "Connection to Server failed due to unknown reason.";
         }
         // if we still don't have a valid error message, show the default
         if (StringUtils.isBlank(errorMsg))
            errorMsg = PSMessages.getString(
                  "PSWorkbenchPlugin.msg.default.titlearea.unknown_error",
                  error.getClass().getName());
         if (locales != null)
         {
            if (locales.size() > 0)
               return correctConnectionForLocale(lastConn, locales);
            else if (locales.size() == 0)
               m_dlg
                     .setErrorMsg(PSMessages
                           .getString("PSWorkbenchPlugin.error."
                                 + "nolocalesEnabled"));
         }
         m_dlg.setErrorMsg(errorMsg);
      }
      int dialogResponseCode = m_dlg.open();
      
      // If the dialog box was cancelled
      if (dialogResponseCode != Dialog.OK)
         return null;
      return m_dlg.getSelectedConnection();
   }

   /**
    * Method to choose a locale from a list. If there is only one locale, use it
    * implicitly. If there is more than one locale, but has only one english 
    * based locale use it. If there are only non-english locales, let user
    * choose the locale
    * @param conn the last connection that has failed, assumed never 
    * <code>null</code>
    * @param locales a list of valid locales returned by the server never 
    * <code>null</code>
    * @return a connection that has valid locale information
    */
   private PSConnectionInfo correctConnectionForLocale(PSConnectionInfo conn, 
         List<String> locales)
   {
      if ( conn == null )
         throw new IllegalArgumentException("connection may not be null");
      
      String loc = PSI18nUtils.DEFAULT_LANG;
      if (locales.size() == 1)
         loc = locales.get(0);
      else if (locales.size() > 1)
      {
         List<String> englishLocales = new ArrayList<String>();
         Iterator<String> it = locales.iterator();
         while ( it.hasNext() )
         {
            String l = it.next();
            if ( l.startsWith("en"))
               englishLocales.add(l);
         }
         if ( englishLocales.size() == 1 )
            loc = englishLocales.get(0);
         else
            loc = chooseLocaleDialog(locales);
      }
      if ( StringUtils.isNotBlank(loc))
      {
         conn.setLocale(loc);
         PSUiConfigManager uiMgr = PSUiConfigManager.getInstance();
         uiMgr.saveUserConnection(conn);
      }
      return conn;
   }

   

   /**
    * Must be called by the caller when they have finished with this class. 
    * Performs required cleanup.
    */
   public void dispose()
   {
      m_dlg = null;
      PSCoreFactory.getInstance().setInvalidConnectionListener(
            m_invalidConnectionListener);
   } 

   /**
    * The value supplied in the ctor. May be <code>null</code>.
    */
   private final IWorkbench m_workbench;
   
   /**
    * The listener present before we set us as the listener. It is used to
    * restore after this class is disposed. May be <code>null</code>.
    */
   private final IPSinvalidConnectionListener m_invalidConnectionListener;
   
   /**
    * The connection manager dialog.
    */
   private PSConnectionsDialog m_dlg;
}
