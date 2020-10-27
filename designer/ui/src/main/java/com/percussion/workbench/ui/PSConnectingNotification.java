/******************************************************************************
 *
 * [ PSConnectingNotification.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;

/**
 * Shows a dialog indicating that the application is in the process of
 * connecting to a server.
 * The server connection process should be done in the parallel thread.
 * Singleton.
 *
 * @author Andriy Palamarchuk
 *
 */
public class PSConnectingNotification
{
   /**
    * Show the notification dialog.
    * Returns after the connection attempt is completed.
    * Must be called from the SWT event thread.
    * @param connection The connection workbench is connecting to.
    * Not <code>null</code>.
    */
   public void open(final PSConnectionInfo connection)
   {
      assert connection != null;
      assert Display.getCurrent() != null;

      if (m_open)
      {
         return;
      }

      m_open = true;
      try
      {
         doOpen(connection);
      }
      finally
      {
         m_open = false;
      }
   }

   /**
    * Implementation of {@link #open(PSConnectionInfo)},
    * allowing that method to deal with {@link #m_open} indicator.
    */
   private void doOpen(final PSConnectionInfo connection)
   {
      // let the connection attempt to start
      Thread.yield();

      final Shell shell = new Shell(
            getActiveShell(), SWT.PRIMARY_MODAL | SWT.BORDER);

      final FillLayout layout = new FillLayout(SWT.VERTICAL);
      layout.marginHeight = COMMON_BORDER_OFFSET;
      layout.marginWidth = COMMON_BORDER_OFFSET;
      shell.setLayout(layout);

      final int dlgWidth = 400;
      final int dlgHeight = 56;
      shell.setSize(new Point(dlgWidth, dlgHeight));
      shell.setLocation(
            getDisplay().getBounds().width / 2 - dlgWidth / 2,
            getDisplay().getBounds().height / 2 - dlgHeight / 2);

      final Label label = new Label(shell, SWT.NONE);
      label.setText(
            PSMessages.getString("PSConnectingNotification.label.connecting", //$NON-NLS-1$
                  connection.getServer(),
                  Integer.toString(connection.getPort())));
                  
                  
      
      new ProgressBar (shell, SWT.INDETERMINATE);
      shell.open();
      while (!shell.isDisposed())
      {
         if (!PSCoreFactory.getInstance().isConnecting())
         {
            shell.dispose();
         }
         else if (!getDisplay().readAndDispatch())
         {
            getDisplay().sleep ();
         }
      }
   }

   /**
    * Shell for an active window.
    * @return active window shell. Returns <code>null</code> if not available.
    */
   private Shell getActiveShell()
   {
      return Display.getCurrent() == null ? null
            : Display.getCurrent().getActiveShell(); 
   }

   /**
    * The singleton instance accessor.
    */
   public static PSConnectingNotification getInstance()
   {
      return ms_instance;
   }
   
   /**
    * Returns current display. Never <code>null</code>.
    */
   private Display getDisplay()
   {
      return Display.getCurrent();
   }

   /**
    * Is <code>true</code> if the notification dialog is displayed.
    */
   private boolean m_open;

   final private static PSConnectingNotification ms_instance =
      new PSConnectingNotification(); 
}
