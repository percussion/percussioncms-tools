/******************************************************************************
 *
 * [ PSCalendarDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.workbench.ui.PSMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

/**
 * Dialog to use calendar.
 */
public class PSCalendarDialog
{
   /**
    * Creates new dialog.
    */
   public PSCalendarDialog(Display display)
   {
       this.m_display = display;
       m_shell = new Shell(display, SWT.APPLICATION_MODAL | SWT.CLOSE);
       m_shell.setText(PSMessages.getString("PSCalendarDialog.title"));         //$NON-NLS-1$
       m_shell.setLayout(new RowLayout());
       m_shell.addListener(SWT.Traverse, new Listener() {
            public void handleEvent(Event e)
            {
               if (e.character == SWT.ESC)
               {
                  closeShell();
               }
            }
         });
       m_calendar = new SWTCalendar(m_shell);
       m_closeListener = new SWTCalendarListener()
             {
               @SuppressWarnings("unused")
               public void dateChanged(SWTCalendarEvent e)
               {
                  final Calendar newDate = m_calendar.getCalendar();
                  if (!userSwitchedMonths(newDate))
                  {
                     closeShell();
                  }
                  m_lastSelectedDate = newDate;
               }

               /**
                * Returns <code>true</code> if user switched month or year
                * sinc {@link #m_lastSelectedDate}.
                */
               private boolean userSwitchedMonths(final Calendar newDate)
               {
                  return newDate.get(MONTH) != m_lastSelectedDate.get(MONTH)
                        || newDate.get(YEAR) != m_lastSelectedDate.get(YEAR);
               }
             };
   }

   /**
    * Opens modal dialog to pick date.
    */
   public void open()
   {
      m_shell.pack();
      center();

      m_calendar.addSWTCalendarListener(m_closeListener);
      m_lastSelectedDate = m_calendar.getCalendar();
      m_shell.open();
      while (!m_shell.isDisposed())
      {
         if (!m_display.readAndDispatch())
         {
            m_display.sleep();
         }
      }
   }

   private void center()
   {
      final Rectangle dlgRect = m_shell.getBounds();
      final Rectangle displayRect = m_display.getBounds();
      int x = (displayRect.width - dlgRect.width) / 2;
      int y = (displayRect.height - dlgRect.height) / 2;
      m_shell.setLocation(x, y);
   }

   public Calendar getCalendar()
   {
       return m_calendar.getCalendar();
   }

   public void setDate(Date date)
   {
       final Calendar calendar = Calendar.getInstance();
       calendar.setTime(date);
       m_calendar.setCalendar(calendar);
   }

   public void addDateChangedListener(SWTCalendarListener listener)
   {
      m_calendar.addSWTCalendarListener(listener);
   }

   /**
    * Asynchroniously closes the dialog shell.
    */
   private void closeShell()
   {
      m_shell.getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            m_shell.close();
         }
      });
   }

   /**
    * Shell to open.
    */
   private Shell m_shell;
   
   /**
    * The date picker control.
    */
   private SWTCalendar m_calendar;

   /**
    * Display to use for dialog.
    */
   private Display m_display;

   
   /**
    * Causes the calendar dialog to close after date is selected.
    */
   private final SWTCalendarListener m_closeListener;

   /**
    * Date selected last time. Is used to determine whether date change was done
    * by changing day or by switching month or year. 
    */
   private Calendar m_lastSelectedDate;
}