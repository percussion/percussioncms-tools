/*[ Statistics.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.xml.PSXmlTreeWalker;

import java.util.ResourceBundle;

/**
 * This class wraps the functionality to access the statistics data from the
 * server and provide its output in a convenient form used within the sevrer
 * admin remote console.
 */
////////////////////////////////////////////////////////////////////////////////
public class Statistics
{
   /**
    * Construct the statistics using the passed server console. Use this
   * constuctor to initialize the statistics data from the current server.
    *
    * @param   console      the server remote console
    */
   //////////////////////////////////////////////////////////////////////////////
   public Statistics(ServerConsole console)
   {
     m_console = console;
     m_command = new String("show status server");
    System.out.println(m_command);
    ReadStatistics();
   }

   /**
    * Construct the statistics using the passed server console. Use this
   * constuctor to initialize the statistics data from the passed application.
    *
    * @param   console         the server remote console
   * @param application   the application we want the statistic from
    */
   //////////////////////////////////////////////////////////////////////////////
   public Statistics(ServerConsole console, String application)
   {
     m_console = console;
     m_command = new String("show status application " + application);
    System.out.println(m_command);
    ReadStatistics();
   }

   /**
    * Read statistics according to the command settings
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  private void ReadStatistics()
  {
     try
    {
         org.w3c.dom.Document doc   = m_console.execute(m_command);
         PSXmlTreeWalker   walker = new PSXmlTreeWalker(doc);
         //if (walker.getNextElement("PSXApplicationStatus") != null)
      //{
         if (walker.getNextElement("PSXStatistics", true, true) != null)
        {
          org.w3c.dom.Node node = walker.getCurrent();
        
           ReadEllapsedTime(walker);
          walker.setCurrent(node);
               ReadEventCounters(walker);
          walker.setCurrent(node);
          ReadEventTimers(walker);
        }
      //}
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
  }

   /**
    * Read ellapsed time since server/application was started using the passed
   * XML tree walker.
    *
   * @param walker   the XML tree walker correctly initialized to point to the PSXStatistics block
    */
  //////////////////////////////////////////////////////////////////////////////
  private void ReadEllapsedTime(PSXmlTreeWalker   walker)
  {
     try
    {
      if (walker.getNextElement("ElapsedTime") != null)
      {
        org.w3c.dom.Node node = walker.getCurrent();

        if (walker.getNextElement("days", true, true) != null)
        {  m_days = walker.getElementData("days", false);
          walker.setCurrent(node);
        }
        if (walker.getNextElement("hours", true, true) != null)
        {  m_hours = walker.getElementData("hours", false);
          walker.setCurrent(node);
        }
        if (walker.getNextElement("minutes", true, true) != null)
        {  m_minutes = walker.getElementData("minutes", false);
          walker.setCurrent(node);
        }
        if (walker.getNextElement("seconds", true, true) != null)  
        {  m_seconds = walker.getElementData("seconds", false);
          walker.setCurrent(node);
        }  
        if (walker.getNextElement("milliseconds", true, true) != null)
        {  m_milliseconds = walker.getElementData("milliseconds", false);
          walker.setCurrent(node);
        }
      }
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
  }

   /**
    * Read server/application event counters using the passed XML tree walker.
    *
   * @param walker   the XML tree walker correctly initialized to point to the Counters block
    */
  //////////////////////////////////////////////////////////////////////////////
  private void ReadEventCounters(PSXmlTreeWalker walker)
  {
     try
    {
       if (walker.getNextElement("Counters") != null)
      {
        org.w3c.dom.Node node = walker.getCurrent();

        if (walker.getNextElement("eventsProcessed", true, true) != null)
             {  
          m_processed = walker.getElementData("eventsProcessed", false);
          walker.setCurrent(node);
        }

        if (walker.getNextElement("eventsFailed", true, true) != null)
             {  
          m_failed = walker.getElementData("eventsFailed", false);
          walker.setCurrent(node);
        }
        if (walker.getNextElement("eventsPending", true, true) != null)
             {
          m_pending = walker.getElementData("eventsPending", false);
          walker.setCurrent(node);
        }
        if (walker.getNextElement("cacheHits", true, true) != null)
             {
          m_cacheHits = walker.getElementData("cacheHits", false);
          walker.setCurrent(node);
        }
        if (walker.getNextElement("cacheMisses", true, true) != null)
             {
          m_cacheMisses = walker.getElementData("cacheMisses", false);
          walker.setCurrent(node);
        }
      }
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
  }

   /**
    * Read server/application event timers using the passed XML tree walker.
    *
   * @param walker   the XML tree walker correctly initialized to point to the Timers block
    */
  //////////////////////////////////////////////////////////////////////////////
  private void ReadEventTimers(PSXmlTreeWalker walker)
  {
     try
    {
       if (walker.getNextElement("Timers") != null)
      {
        org.w3c.dom.Node node = walker.getCurrent();
        org.w3c.dom.Node subNode = null;
        String sDays = null;
        String sHours = null;
        String sMinutes = null;
        String sSeconds = null;
        String sMilliseconds = null;
        
         if (walker.getNextElement("EventAverage", true, true) != null)
        {
          subNode = walker.getCurrent();
          
          if (walker.getNextElement("days", true, true) != null)
          {  sDays = walker.getElementData("days", false);
             walker.setCurrent(subNode);
          }          
          if (walker.getNextElement("hours", true, true) != null)
          {  sHours = walker.getElementData("hours", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("minutes", true, true) != null)
          {  sMinutes = walker.getElementData("minutes", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("seconds", true, true) != null)
          {  sSeconds = walker.getElementData("seconds", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("milliseconds", true, true) != null)
          {  sMilliseconds = walker.getElementData("milliseconds", false);
             walker.setCurrent(subNode);
          }  
          m_average = getTimeInMilliseconds(sDays,
                                                             sHours,
                                            sMinutes,
                                            sSeconds,
                                            sMilliseconds);
          
          walker.setCurrent(node);
        }
         if (walker.getNextElement("EventMinimum") != null)
        {
          subNode = walker.getCurrent();
          
          if (walker.getNextElement("days", true, true) != null)
          {  sDays = walker.getElementData("days", false);
             walker.setCurrent(subNode);
          }          
          if (walker.getNextElement("hours", true, true) != null)
          {  sHours = walker.getElementData("hours", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("minutes", true, true) != null)
          {  sMinutes = walker.getElementData("minutes", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("seconds", true, true) != null)
          {  sSeconds = walker.getElementData("seconds", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("milliseconds", true, true) != null)
          {  sMilliseconds = walker.getElementData("milliseconds", false);
             walker.setCurrent(subNode);
          }  
          m_minimum = getTimeInMilliseconds(sDays,
                                                             sHours,
                                            sMinutes,
                                            sSeconds,
                                            sMilliseconds);

          walker.setCurrent(node);
        }
         if (walker.getNextElement("EventMaximum") != null)
        {
          subNode = walker.getCurrent();
          
          if (walker.getNextElement("days", true, true) != null)
          {  sDays = walker.getElementData("days", false);
             walker.setCurrent(subNode);
          }          
          if (walker.getNextElement("hours", true, true) != null)
          {  sHours = walker.getElementData("hours", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("minutes", true, true) != null)
          {  sMinutes = walker.getElementData("minutes", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("seconds", true, true) != null)
          {  sSeconds = walker.getElementData("seconds", false);
             walker.setCurrent(subNode);
          }
          if (walker.getNextElement("milliseconds", true, true) != null)
          {  sMilliseconds = walker.getElementData("milliseconds", false);
             walker.setCurrent(subNode);
          }  
          m_maximum = getTimeInMilliseconds(sDays,
                                                             sHours,
                                            sMinutes,
                                            sSeconds,
                                            sMilliseconds);

          walker.setCurrent(node);
        }
      }
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
  }

   /**
    * Format the passed strings into one string representing the time ellapsed in
   * milliseconds.
    *
   * @param days               the days ellapsed
   * @param hours               the hours ellapsed
   * @param minutes            the minutes ellapsed
   * @param seconds            the seconds ellapsed
   * @param milliseconds   the milliseconds ellapsed
    */
  //////////////////////////////////////////////////////////////////////////////
  private String getTimeInMilliseconds(String days, String hours, String minutes,
                                                          String seconds, String milliseconds)
  {
     int iDays = new Integer(days).intValue();
      int iHours = new Integer(hours).intValue();
      int iMinutes = new Integer(minutes).intValue();
      int iSeconds = new Integer(seconds).intValue();
      int iMilliseconds = new Integer(milliseconds).intValue();
    int iTime = iMilliseconds +
                             iSeconds * 1000 +
                      iMinutes * 60 * 1000 +
                       iHours * 60 * 60 * 1000 +
                       iDays * 24 * 60 * 60 * 1000;

      return new Integer(iTime).toString();
  }

   /**
    * Format the ellapsed time into a string of the format "days, hrs, min, sec".
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getUptime()
  {
     return m_days + dayDelimiter + m_hours + hrsDelimiter +
              m_minutes + minDelimiter + m_seconds + secDelimiter;
  }

   /**
    * Get the number of events processed so far.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getEventsProcessed()
  {
     return m_processed;
  }

   /**
    * Get the number of events failed so far.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getEventsFailed()
  {
     return m_failed;
  }

   /**
    * Get the number of events pending so far.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getEventsPending()
  {
     return m_pending;
  }

   /**
    * Get the minimal time used to process an event.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getMinimumEventTime()
  {
     return m_minimum;
  }

   /**
    * Get the maximal time used to process an event.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getMaximumEventTime()
  {
     return m_maximum;
  }

   /**
    * Get the average time used to process an event.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
  public String getAverageEventTime()
  {
     return m_average;
  }
   /**
    * the server console used
    */
   private ServerConsole m_console = null;
   /**
    * the XML command string
    */
   private String m_command = new String("");
   /**
    * days ellapsed
    */
   private String m_days = new String("0");
   /**
    * hours ellapsed
    */
  private String m_hours = new String("0");
   /**
    * minutes ellapsed
    */
  private String m_minutes = new String("0");
   /**
    * seconds ellapsed
    */
  private String m_seconds = new String("0");
   /**
    * milliseconds ellapsed
    */
  private String m_milliseconds = new String("0");
   /**
    * events processed
    */
  private String m_processed = new String("0");
   /**
    * events failed
    */
  private String m_failed = new String("0");
   /**
    * events pending
    */
  private String m_pending = new String("0");
   /**
    * cache hits
    */
  private String m_cacheHits = new String("0");
   /**
    * cache misses
    */
  private String m_cacheMisses = new String("0");
   /**
    * events minimal processing time
    */
  private String m_minimum = new String("0");
   /**
    * events maximal processing time
    */
  private String m_maximum = new String("0");
   /**
    * events average processing time
    */
  private String m_average = new String("0");
   /**
    * update interval, interval on which this object will be refreshed from the server
    */
  private String m_interval = new String("");
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();

  //////////////////////////////////////////////////////////////////////////////
  private static final String dayDelimiter = m_res.getString("dayDelimiter");
  private static final String hrsDelimiter = m_res.getString("hourDelimiter");
  private static final String minDelimiter = m_res.getString("minuteDelimiter");
  private static final String secDelimiter = m_res.getString("secondDelimiter");
}
