/******************************************************************************
 *
 * [ AwtSwtModalDialogBridge.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.legacy;

import com.percussion.E2Designer.PSDlgUtil;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Blocks SWT interface while AWT/Swing modal dialog is displayed.
 * Usage:
 * <pre>
 *    final AwtSwtModalDialogBridge bridge = new AwtSwtModalDialogBridge(parent)
 *    JDialog dialog = new MyModalDialog();
 *    bridge.registerModalSwingDialog(dialog);
 *    dialog.setVisible(true);
 * </pre>
 * If you do not have access to a modal dialog because it is hidden behind
 * some API use {@link #lockSWTFor(Runnable)}.
 *
 * @author Andriy Palamarchuk
 */
public class AwtSwtModalDialogBridge
{
   /**
    * The bridge logger.
    */
   private static final Logger ms_log =
      Logger.getLogger(AwtSwtModalDialogBridge.class);

   /**
    * Must be created in SWT event handling thread, so it can use Display from the
    * provided parent.
    */
   public AwtSwtModalDialogBridge(Control parent)
   {
      super();
      m_parent = parent;
      m_display = parent.getShell().getDisplay();
   }
   
   /**
    * Locks SWT UI when Swing dialog is shown and unlocks when the dialog is dismissed.
    * Must be called before modal swing dialog is shown first time.
    * Don't register dialogs shown by registered dialog with this class.  
    * Thread-safe.
    */
   public void registerModalSwingDialog(JDialog dialog)
   {
      assert dialog.isModal();
      
      addSwtLockingListenersToDialog(dialog);
   }

   /**
    * Locks AWT UI while executing the provided task in Swing thread.
    * Is used when developer does not have access to the modal dialog itself
    * because it is hidded behind API.
    * Passes all the exceptions from the call outside.
    * Must be called from the Swing event dispatching thread.
    */
   public void lockSWTFor(final Runnable task)
         throws InvocationTargetException
   {
      assert SwingUtilities.isEventDispatchThread() :
            "This method must be called from AWT/Swing event thread!";
      ms_log.info("Locking UI for " + task);
      final Exception[] exceptionContainer = {null};
      runInDummyModalDialog(task, exceptionContainer);
      ms_log.info("Unlocked UI for " + task);
      if (exceptionContainer[0] != null)
      {
         throw new InvocationTargetException(exceptionContainer[0]);
      }
   }

   /**
    * Runs provided task in dummy local dialog
    * @param task task to run
    * @param exceptionContainer container to return exception. If encounts an
    * exception while running the task puts the exception in element 0 of this
    * container.
    */
   private void runInDummyModalDialog(final Runnable task,
         final Exception[] exceptionContainer)
   {
      final JDialog d = new JDialog((Frame) null, true);
      // hide the dialog
      d.setBounds(-1000, -1000, 1, 1);
      registerModalSwingDialog(d);
      d.addWindowListener(new WindowAdapter()
            {
               @Override
               @SuppressWarnings("unused")
               public void windowOpened(WindowEvent event)
               {
                  try
                  {
                     task.run();
                  }
                  catch (Exception e)
                  {
                     exceptionContainer[0] = e;
                  }
                  finally
                  {
                     d.setVisible(false);
                  }
               }
            });
      d.setVisible(true);
      d.dispose();
   }

   /**
    * Adds listeners to the provided dialog which lock/unlock SWT UI when
    * dialog is shown/hidden.
    */
   private void addSwtLockingListenersToDialog(JDialog dialog)
   {
      synchronized(m_registeredDialogs)
      {
         if (m_registeredDialogs.keySet().contains(dialog))
         {
            return;
         }
         m_registeredDialogs.put(dialog, null);
      }
      
      dialog.addComponentListener(
            new ComponentAdapter()
            {
               @Override
               public void componentShown(ComponentEvent e)
               {
                  lockSWT((JDialog) e.getSource());
               }
            });
      if (dialog.isVisible())
      {
         lockSWT(dialog);
      }
   }

   /**
    * Maked SWT UI locked as for modal dialog
    */
   private void lockSWT(JDialog dialog)
   {
      if (isSwtLocked())
      {
         // nested Swing dialog call. 
         return;
      }
      m_dialogLockFor = dialog;
      m_display.asyncExec(
            new Runnable()
            {
               public void run()
               {
                  ms_log.info("Lock SWT");
                  initializeLockingShell();
                  ms_lockingShell.open();
                  final Display display = ms_lockingShell.getDisplay();
                  // somehow ms_lockingShell can become null (see Rx-06-08-0066)
                  while (ms_lockingShell != null
                        && !ms_lockingShell.isDisposed())
                  {
                     if (!ms_lockingShell.getDisplay().readAndDispatch())
                     {
                        // Andriy: had to do this check here because
                        // in some situations there is no notifications on dialog closing
                        maybeUnlockSWT();
                     }
                     else
                     {
                        display.sleep();
                     }
                  }
                  ms_lockingShell = null;
               }
            });
   }

   private void unlockSWT(ComponentEvent e)
   {
      if (!isSwtLocked())
      {
         return;
      }
      if (m_dialogLockFor != null && !m_dialogLockFor.equals(e.getSource()))
      {
         // nested dialog
         return;
      }
      m_display.syncExec(
            new Runnable()
            {
               public void run()
               {
                  if (!isSwtLocked())
                  {
                     return;
                  }
                  ms_log.info("Unlock SWT");
                  ms_lockingShell.dispose();
                  m_dialogLockFor = null;
               }
            });
   }

   /**
    * Searches for the topmost swing window and brings it to front. 
    */
   private void bringTopSwingWindowToFront()
   {
      SwingUtilities.invokeLater(
            new Runnable()
            {
               public void run()
               {
                  if (PSDlgUtil.getActiveWindow() != null)
                  {
                     PSDlgUtil.getActiveWindow().toFront();
                  }
                  else
                  {
                     // this means all the Swing/AWT windows are dismissed
                  }
               }
            });
   }

   /**
    * Returns <code>true</code> if Swt interface is locked by this class.
    */
   public static boolean isSwtLocked()
   {
      return ms_lockingShell != null;
   }

   /**
    * Creates {@link Shell} blocking SWT UI during displaying Swing/AWT dialog. 
    */
   private void initializeLockingShell()
   {
      assert ms_lockingShell == null;
      ms_lockingShell = new Shell(m_parent.getShell(), SWT.APPLICATION_MODAL);
      
      {
         // an empty Shell closes when Escape is pressed
         final Composite composite = new Composite(ms_lockingShell, SWT.EMBEDDED);
         composite.setBounds(0, 0, 0, 0);
      }

      ms_lockingShell.setSize(0, 0);
      ms_lockingShell.addShellListener(
            new ShellAdapter()
            {
               @Override
               @SuppressWarnings("unused")
               public void shellActivated(ShellEvent e)
               {
                  bringTopSwingWindowToFront();
               }
            });
   }

   /**
    * Unlocks SWT if the swing dialog is invalid. 
    */
   private void maybeUnlockSWT()
   {
      if (m_dialogLockFor == null || !m_dialogLockFor.isDisplayable())
      {
         unlockSWT(new ComponentEvent(
               m_dialogLockFor, ComponentEvent.COMPONENT_HIDDEN));
      }
   }

   /**
    * SWT parent to use.
    */
   private final Control m_parent;
   
   /**
    * Parent display. Initialize beforehand otherwise it will be problematic
    * to retrieve it from {@link #m_parent} outside of SWT event thread.
    */
   private final Display m_display;
   
   /**
    * Shell locking the SWT application. Is initialized during time SWT
    * UI is locked. <code>null</code> otherwise.
    * Is static because there should be only 1 lock per application
    */
   private static Shell ms_lockingShell;
   
   /**
    * Map to track already registered dialogs. Used as a set, so values are
    * disregarded. Used to eliminate repeated registration of the same dialog. 
    */
   private Map<JDialog, Object> m_registeredDialogs = new WeakHashMap<JDialog, Object>();
   
   /**
    * Swing dialog which caused this class to lock SWT UI. 
    */
   private JDialog m_dialogLockFor;
}
