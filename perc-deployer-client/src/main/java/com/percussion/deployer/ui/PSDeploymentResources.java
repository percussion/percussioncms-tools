/******************************************************************************
 *
 * [ PSDeploymentResources.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.guitools.PSResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The class to hold all kinds of resources (generic titles, labels, images and
 * error messages) used by the deployment client application.
 */
public class PSDeploymentResources extends PSResources
{
   protected Object [][] getContents()
   {
      Object [][] a =
      {
         //Product Title and image
         {"title", "Percussion Deployer"},
         {"gif_main", "images/deployer.gif"},

         //Main Frame menu
         { "menuAction", "Action" },
         { "mn_menuAction", new Character('A') },

         { "menuActionRegister", "Register Server" },
         { "mn_menuActionRegister", new Character('R') },
         { "ks_menuActionRegister", KeyStroke.getKeyStroke(KeyEvent.VK_R,
            Event.CTRL_MASK, true)},
         { "tt_menuActionRegister", "Register a new server." },

         { "menuActionCreateArchive", "Create Package" },
         { "mn_menuActionCreateArchive", new Character('C') },

         { "ks_menuActionCreateArchive", KeyStroke.getKeyStroke(KeyEvent.VK_C,
            Event.CTRL_MASK, true)},
         { "tt_menuActionCreateArchive", "Create a new package." },

         { "menuActionInstallArchive", "Install Package" },
         { "mn_menuActionInstallArchive", new Character('I') },

         { "ks_menuActionInstallArchive", KeyStroke.getKeyStroke(KeyEvent.VK_I,
            Event.CTRL_MASK, true)},
         { "tt_menuActionInstallArchive", "Install a package." },

         { "menuActionExit", "Exit" },
         { "mn_menuActionExit", new Character('E') },

         { "ks_menuActionExit", KeyStroke.getKeyStroke(KeyEvent.VK_E,
            Event.CTRL_MASK, true)},
         { "tt_menuActionExit", "Exit from application." },

         { "menuEdit", "Edit" },
         { "mn_menuEdit", new Character('E') },

         { "menuEditRegister", "Registration" },
         { "mn_menuActionRegister", new Character('R') },
         { "ks_menuActionRegister", KeyStroke.getKeyStroke(KeyEvent.VK_R,
            Event.CTRL_MASK, true)},
         { "tt_menuActionRegister", "Edit selected server registration." },

         { "menuEditSettings", "Policy Settings" },
         { "mn_menuEditSettings", new Character('P') },
         { "ks_menuEditSettings", KeyStroke.getKeyStroke(KeyEvent.VK_P,
            Event.CTRL_MASK, true)},
         { "tt_menuEditSettings", "Edit policy settings of the server." },

         { "menuEditIDTypes", "Identify ID Types" },
         { "mn_menuEditIDTypes", new Character('I') },
         { "ks_menuEditIDTypes", KeyStroke.getKeyStroke(KeyEvent.VK_I,
            Event.CTRL_MASK, true)},
         { "tt_menuEditIDTypes", "Identify Literal ID Types used in server." },

         { "menuEditTransforms", "Define Transforms" },
         { "mn_menuEditTransforms", new Character('T') },
         { "ks_menuEditTransforms", KeyStroke.getKeyStroke(KeyEvent.VK_T,
            Event.CTRL_MASK, true)},
         { "tt_menuEditTransforms", "Define transformations for the external dbms credentials and objects." },

         { "menuHelp", "Help" },
         { "mn_menuHelp", new Character('H') },

         { "menuHelpAbout", "About" },
         { "mn_menuHelpAbout", new Character('A') },
         { "ks_menuHelpAbout", KeyStroke.getKeyStroke(KeyEvent.VK_A,
            Event.CTRL_MASK, true)},
         { "tt_menuHelpAbout", "About the Deployer Product." },

         { "multiServerMgrHelp", "Deployer Help" },
         { "mn_multiServerMgrHelp", new Character('D') },
         { "ks_multiServerMgrHelp", KeyStroke.getKeyStroke(KeyEvent.VK_D,
            Event.CTRL_MASK, true)},
         { "tt_multiServerMgrHelp", "About the Deployer." },


         //Additional menus to be shown for a pop-up menu on server node
         //selection.
         { "menuConnect", "Connect" },
         { "menuDisconnect", "Disconnect" },
         { "menuDelete", "Delete" },

         //Generic error messages
         {"errorTitle", "Error"},
         {"warningTitle", "Warning"},
         {"ioReadError", "Error reading the file: {0}, Reason: {1} "},
         {"ioWriteError", "Error writting to the file: {0}, Reason: {1}"},
         {"ioParseError", "Error parsing the xml document from file: {0}, Reason: {1}"},
         {"ioDocError", "Error getting the contents from the xml document: {0}, Reason: {1}"},
         {"unableToLoadUserProps", "Error loading the user properties from the file: {0}, Reason: {1}"},
         {"unableToSaveUserProps", "Error saving the user properties to the file: {0}, Reason: {1}"},
         {"notLicensedServer", "This action may not be performed on a server that is not licensed for Deployer. Please contact Percussion Software to purchase a Deployer license for this server."},

         //Menu action error messages
         {"notConnected", "The selected server must be connected to perform this action"},
         {"noTargetServerstoTransform", "No Target servers to define the transforms for the selected source server"},
         {"deleteServerTitle", "Delete server"},
         {"deleteServerMsg", "Are you sure you want to remove the registered server <{0}>?"},
         {"invalidLogID", "The log id must be a number"},
         {"installWarning", "It is strongly recommended that you backup your Rhythmyx server and repository prior to package installation. Do you want to continue?"},
         {"archiveNotFound", "The package corresponding to the selected descriptor is not found.  Please choose the package in the wizard."},
         {"noArchiveManifest", "Unable to export the package manifest. Reason: Manifest is not found for the selected package."},
         {"noIDTypes", "There are no server objects that have literal ids."},


         //Servers tree resources
         {"serverGroups", "Server Groups"},
         {"source", "Source"},
         {"descriptors", "Descriptors"},
         {"target", "Target"},
         {"archives", "Packages"},
         {"packages", "Elements"},
         {"gif_repository", "images/database_16.gif"},
         {"gif_server", "images/server_16.gif"},
         {"gif_descriptors", "images/descriptor.gif"},
         {"gif_packages", "images/package.gif"},
         {"gif_archives", "images/archives.gif"},


         //Message for repository information changed for a server
         {"repositoryInfoChangeTitle", "Repository Change"},
         {"repositoryInfoChangeConfirm", "The server repository information has"
         + " changed from {0} to {1}. This will move the server from this "
         + "repository to the repository matching the current "
         + "repository information. Do you want to continue?"},

         //icons used in application
         {"gif_up", "images/up.gif"},
         {"gif_down", "images/down.gif"},
         {"gif_exclamation", "images/exclamation.gif"},
         {"gif_transparent", "images/transparent.gif"},

         //View Status messages
         {"repositoryView", "Driver:{0}, Server:{1}, Database:{2}, Origin:{3}"},
         {"serverConnView", "Server:{0}, Port:{1}, Version:{2}, Datasource:{3}, Connected:Yes"},
         {"serverNotConnView", "Server:{0}, Port:{1}, Connected:No"},

         //table view pop-up menu items
         {"delete", "Delete"},
         {"createArchive", "Create Package"},
         {"deploy", "Install"},
         {"exportManifest", "Export Manifest"},
         {"redeploy", "Re-install"},
         {"export", "Export"},
         {"viewLog", "View Log"},

         //Error Messages for pop-up menu actions
         {"validationTitle", "Package validation error"},

         //generic labels or messages used in the wizard dialogs
         {"cancelConfirmMsg", "Do you want to cancel this process?"},
         {"cancelConfirmTitle", "Cancel"},
         {"deleteConfirmTitle", "Confirm Delete"},
         {"deleteDescMsg", "Are you sure you want to delete the descriptor <{0}>?"},
         {"deleteArchiveMsg", "Deleting the package <{0}> deletes the package and all package and element logs related to the package. Are you sure you want to continue? "},
         {"next", "Next >"},
         {"back", "< Back"},
         {"finish", "Finish"},
         {"cancel", "Cancel"},
         {"help", "Help"},
         // mnemonics for generic buttons - command buttons
         {"mn_next", new Character('N')},
         {"mn_back", new Character('B')},
         {"mn_finish", new Character('F')},
         {"mn_cancel", new Character('C')},
         {"mn_help", new Character('H')},

         
         {"exportStatusTitle", "Package Status"},
         {"exportJob", "Creating package on server:"},
         {"copyArchiveFromServer", "Copying package from server:"},
         {"importStatusTitle", "Install Status"},
         {"importJob", "Installing package on server:"},
         {"copyArchiveToServer", "Copying package to server:"},
         {"validtionJobTitle", "Elements Validation Status"},
         {"validationJob", "Validating elements to install:"},
         {"publisherLockedTitle", "Edition in Progress"},
         {"publisherLockedAbortConfirmTitle", "Confirm Abort"},
         {"publisherLockedMsg", "The server is currently publishing an Edition.  You must wait until the server has finished before you may proceed.  Click \"OK\" to try again if the server has finished, or \"Cancel\" to abort this installation."},
         {"publisherLockedAbortConfirmMsg", "This will abort the current installation.  Are you sure you wish to abort the installation?"},
         {"show", "Show"},
         {"mn_show", new Character('S')},
         {"allResults", "All Results"},
         {"errorsOnly", "Errors Only"},

         //Dependency Tree Node Labels and icons
         {"dependencies", "Dependencies"},
         {"ancestors", "Ancestors"},
         {"local", "Local"},
         {"shared", "Shared"},
         {"system", "System"},
         {"server", "Server"},
         {"user", "User Defined"},
         {"gif_showMulti", "images/multiple_deps.gif"},         

         //lock related labels/messages/titles
         {"lockErrTitle", "Locked Connection"},
         {"overrideLock", "Do you want to override the lock?"},
         
         // help about resources
         {"aboutTitle", "About Rhythmyx Deployer"},
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
}
