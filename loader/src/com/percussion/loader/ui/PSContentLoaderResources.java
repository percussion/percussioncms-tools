/*[ PSContentLoaderResources.java ]********************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSResources;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;

/**
 * The class to hold all kinds of resources (generic titles, labels, images and
 * error messages) used by the content loader application.
 */
public class PSContentLoaderResources extends PSResources
{
   protected Object [][] getContents()
   {
      Object [][] a =
      {
         //Product Title and image
         { "title", "Rhythmyx Enterprise Content Connector" },
         {"about_gif", "images/aboutRhythmyx.gif"},
         { "gif_help", "images/help.gif" },
         { "up", "images/up.gif" },
         { "tt_up", "Moves up the selected extractor" },
         { "down", "images/down.gif" },
         { "tt_down", "Moves down the selected extractor" },
         { "delete", "images/delete.gif" },
         { "tt_delete", "Deletes the selected extractor" },
         { "node_error_gif", "images/errorNode.gif" },
         { "gif_previous", "images/previous.gif" },
         { "gif_next", "images/next.gif" },
         { "gif_start", "images/start.gif" },
         { "gif_stop", "images/stop.gif" },
         { "gif_reset", "images/reset.gif" },
         { "gif_empty", "images/empty.gif" },
         { "gif_empty24", "images/empty24.gif" },
         { "gif_info", "images/info.gif" },
         { "gif_current", "images/current.gif" },
         { "gif_done", "images/done.gif" },
         { "node_changed_gif", "images/nodeChanged.gif" },
         { "node_unchanged_gif", "images/unchangedNode.gif" },
         { "node_excluded_gif", "images/excludedNode.gif" },
         { "node_new_gif", "images/newNode.gif" },
         { "gif_main", "images/main.gif" },

         //Main Frame menu
         { "menuFile", "File" },
         { "mn_menuFile", new Character('F') },

         { "menuFileNew", "New..." },
         { "mn_menuFileNew", new Character('N') },
         { "ks_menuFileNew", KeyStroke.getKeyStroke(KeyEvent.VK_N,
               Event.CTRL_MASK, true)},
         { "tt_menuFileNew", "Start a new Loader Descriptor." },
         { "gif_newFile", "images/newFile.gif" },

         { "menuFileOpen", "Open..." },
         { "mn_menuFileOpen", new Character('O') },
         { "ks_menuFileOpen", KeyStroke.getKeyStroke(KeyEvent.VK_O,
               Event.CTRL_MASK, true)},
         { "tt_menuFileOpen", "Open an existing Loader Descriptor." },
         { "gif_openFile", "images/openFile.gif" },

         { "menuFileLoadStat", "Load Status" },
         { "mn_menuFileLoadStat", new Character('L') },
         { "ks_menuFileLoadStat", KeyStroke.getKeyStroke(KeyEvent.VK_L,
            Event.CTRL_MASK, true)},
         { "tt_menuFileLoadStat", "Show saved status for the Loader Descriptor."
         },

         { "menuFileSave", "Save" },
         { "mn_menuFileSave", new Character('S') },
         { "ks_menuFileSave", KeyStroke.getKeyStroke(KeyEvent.VK_S,
               Event.CTRL_MASK, true)},
         { "tt_menuFileSave", "Save current status to disk." },
         { "gif_saveFile", "images/saveFile.gif" },

         { "buttonFileSaveAs", "SaveAs" },

         { "menuFileSaveAs", "SaveAs..." },
         { "tt_menuFileSaveAs", "Save current descriptor with a new name." },
         
         { "menuFileExit", "Exit" },
         { "tt_menuFileExit", "Close all window and exit." },

         { "menuView", "View" },
         { "mn_menuView", new Character('V') },

         { "menuViewToolbar", "Toolbar" },
         { "mn_menuViewToolbar", new Character('T') },
         { "ks_menuViewToolbar", KeyStroke.getKeyStroke(KeyEvent.VK_T,
               Event.CTRL_MASK, true)},
         { "tt_menuViewToolbar", "View or hide Toolbar." },

         { "menuViewStatBar", "Status Bar"},
         { "mn_menuViewStatBar", new Character('B') },
         { "ks_menuViewStatBar", KeyStroke.getKeyStroke(KeyEvent.VK_B,
               Event.CTRL_MASK, true)},
         { "tt_menuViewStatBar", "View or hide Status Bar." },

         { "menuViewLog", "Log" },
         { "mn_menuViewLog", new Character('G') },
         { "ks_menuViewLog", KeyStroke.getKeyStroke(KeyEvent.VK_G,
            Event.CTRL_MASK, true)},
         { "tt_menuViewLog", "View or hide Log View."},

         { "menuViewMetaData", "Meta Data" },
         { "mn_menuViewMetaData", new Character('M') },
         { "ks_menuViewMetaData", KeyStroke.getKeyStroke(KeyEvent.VK_M,
               Event.CTRL_MASK, true)},
         { "tt_menuViewMetaData", "View or hide Meta Data view." },

         { "menuViewStatus", "Status" },
         { "mn_menuViewStatus", new Character('S') },
         { "ks_menuViewStatus", KeyStroke.getKeyStroke(KeyEvent.VK_S,
               Event.CTRL_MASK, true)},
         { "tt_menuViewStatus", "View or hide Status view." },

         { "menuActions", "Actions" },
         { "mn_menuActions", new Character('A') },
         { "ks_menuActions", KeyStroke.getKeyStroke(KeyEvent.VK_A,
               Event.CTRL_MASK, true)},

         { "menuActionsScan", "Scan" },
         { "mn_menuActionsScan", new Character('C') },
         { "ks_menuActionsScan", KeyStroke.getKeyStroke(KeyEvent.VK_C,
               Event.CTRL_MASK, true)},
         { "tt_menuActionsScan", "Start Scanning." },
         { "gif_scan", "images/scan.gif" },

         { "menuActionsUpload", "Upload" },
         { "mn_menuActionsUpload", new Character('U') },
         { "ks_menuActionsUpload", KeyStroke.getKeyStroke(KeyEvent.VK_U,
               Event.CTRL_MASK, true)},
         { "tt_menuActionsUpload", "Start Uploading." },
         { "gif_upload", "images/upload.gif" },

         { "menuActionsReload", "Reload" },
         { "menuActionsReloadAsNew", "Reload As New Item" },
         
         { "menuTools", "Tools" },
         { "mn_menuActions", new Character('L') },
         { "ks_menuActions", KeyStroke.getKeyStroke(KeyEvent.VK_L,
               Event.CTRL_MASK, true)},

         { "menuToolsDesc", "Descriptor Setup..." },
         { "mn_menuToolsDesc", new Character('P') },
         { "ks_menuToolsDesc", KeyStroke.getKeyStroke(KeyEvent.VK_P,
               Event.CTRL_MASK, true)},
         { "tt_menuToolsDesc", "Load Loader Descriptor Setup dialog." },

         { "menuToolsPrefs", "Preferences..." },
         { "mn_menuToolsPrefs", new Character('R') },
         { "ks_menuToolsPrefs", KeyStroke.getKeyStroke(KeyEvent.VK_R,
               Event.CTRL_MASK, true)},
         { "tt_menuToolsPrefs", "Load Preference dialog." },

         { "menuHelp", "Help" },
         { "mn_menuHelp", new Character('H') },

         { "menuHelpAbout", "About" },
         { "mn_menuHelpAbout", new Character('H') },
         { "ks_menuHelpAbout", KeyStroke.getKeyStroke(KeyEvent.VK_H,
               Event.CTRL_MASK, true)},
         { "tt_menuHelpAbout", "About the Rhythmyx Content Connector." },

         { "menuContentLoaderHelp", "Content Connector Help" },
         { "mn_menuContentLoaderHelp", new Character('E') },
         { "ks_menuContentLoaderHelp", KeyStroke.getKeyStroke(KeyEvent.VK_E,
               Event.CTRL_MASK, true)},
         { "tt_menuContentLoaderHelp", "About the Content Connector." },

         // Tool tip for toolbar buttons
         {"tt_previousToolBtn", "Go to the previous task"},
         {"tt_nextToolBtn", "Go to the next task"},
         {"tt_startToolBtn", "Start the currently selected task"},
         {"tt_stopToolBtn", "Stop the currently selected task"},
         {"tt_resetToolBtn", "Reset the status to start over the migration for the current Descriptor"},

         // Tool tip for log toolbar buttons
         {"tt_previousError", "Go to the previous error"},
         {"tt_nextError", "Go to the next error"},

         {"nodescriptorpathmessage", "No descriptor file defined"},
         {"nodescriptorpathtitle", "Descriptor path"},
         {"emptytreemessage", "No scanned content is available"},
         {"emptytreetitle", "Empty Tree"},

         {"error.msg.descmissing", "Descriptor file is missing in "},
         {"error.title.descmissing", "Descriptor file Error."},

         {"error.msg.wrongdesc", "Wrong descriptor file path, \"{0}\". The name of the descriptor file must be the same as the name of its directory."},
         {"error.title.wrongdesc", "Descriptor file Error."},

         {"desc.file.type", "Descriptor files (*.xml)"},

         {"error.title.loadexception", "Loader Exception"},

         {"error.title.processAborted", "Process aborted"},
         {"error.msg.processAborted", "{0}"},

         {"warning.title.fail2load.status", "Failed to load content status"},
         
         {"descriptor.name", "Descriptor name: "},
         
         {"descriptor.loader.error", "Failed to load descriptor dialog, due to error: {0}"}
      };

      return a;
   }

   /**
    * Gets the resource string identified by the specified key in the specified
    * resource bundle. If the resource cannot be found, the key itself is
    * returned.
    *
    * @param res the resource bundle to check for, may be <code>null</code>
    * @param key identifies the resource to be fetched; may not be <code>null
    * </code> or empty.
    *
    * @return String value of the resource identified by <code>key</code>, or
    * <code>key</code> itself if not found or supplied resource bundle is <code>
    * null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static String getResourceString(ResourceBundle res, String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");

      String resourceValue = key;
      try
      {
         if(res != null)
            resourceValue = res.getString( key );
         } catch (MissingResourceException e)
         {
            System.err.println( e );
         }
         return resourceValue;
   }

   /**
    * Gets and caches the resource bundle used by this application for menus,
    * generic labels and error messages. Displays the error message to the user
    * in case of missing resource bundle and terminates the application.
    *
    * @return the resources never <code>null</code>
    */
   public static PSResources getResources()
   {
      if (null == ms_res)
      {
         final String strResBaseName =
               "com.percussion.loader.ui.PSContentLoaderResources";
         try
         {
            // load the string resources for the application
            ms_res = (PSContentLoaderResources)ResourceBundle.getBundle(
                  strResBaseName);
         }
         catch(MissingResourceException e)
         {
            // gem: change message for content loader
            ErrorDialogs.FatalError(
                  "Missing resource bundle for loader client ui");
         }
      }
      return ms_res;
   }

   /**
    * Checks if the Object is <code>null</code> or empty.
    *
    * @param o, can be <code>null</code>
    * @return <code>true</code> if the supplied Object is <code>null</code> else
    * <code>false</code>
    */
   public static boolean nullOrEmpty(Object o)
   {
      if (o instanceof String)
      {
         String s = (String)o;
         return (s == null || s.length() == 0);
      }
      else
         return (o == null);
   }

   /**
    * The resource bundle for this client. Initialized the first time <code>
    * getResources()</code> is called and never <code>null</code> or modified
    * after that.
    */
   private static PSResources ms_res;

}