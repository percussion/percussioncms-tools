/******************************************************************************
 *
 * [ E2DesignerResources.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import org.eclipse.swt.SWT;

import java.awt.*;

/**
 * This class maintains the actual strings used by the core of the main program.
 * <p>
 * When adding strings, the naming convention described in the class ResourceHelper
 * should be followed.
 * <p>
 * When adding strings that require parameters, make a note of what the parameters
 * are for. This will simplify conversion later.
 *
 */
public class E2DesignerResources extends PSResources
{
   @Override
   protected Object [][] getContents()
   {
      Object [][] a =
      {
         { "ProductName", "Rhythmyx Workbench" },
         /* Param[0] is the product name, param[1] is the version info;
            e.g. 'Rhythmyx Workbench: Release 1.1 (Build 32, 03/17/00, 12:11)' */
         { "LoginVersionInfo", "{0}: {1}" },
         { "StatusBarDefault", "Status: Ready" },
         { "gif_ChildIconFilename", "images/main.gif" },
         /* used internally as temporary string when creating object store objects,
         but it is possible for user to see */
         { "Dummy", "<Unspecified>" },
         { "default", "Default" },

         /* Text for datatank label when it has no tables */
         { "NoTables", "<No Tables>" },
         /* Text for page tank label when it has no dtd */
         { "NoDTD", "<Unspecified>" },

         // for OSPageDatatank, DTD generation error
         { "CircJoinsNoSupport", "Creating a DTD for joined tables containing circular joins is not supported. Creating DTD for the first added table." },
         { "CircJoinsNoSupportTitle", "Unsupported" },

         // generic exception text
         { "UnexpectedProcessorError", "Unexpected processor error." },
         { "AuthorizationException", "You do not have designer access on this server." },
         { "AuthenticationException", "You could not be authenticated on this server. You must be authenticated to utilize the workbench." },
         { "ServerException", "A problem occurred trying to connect to the server, the following text was returned: \n{0}" },
         { "AuthenticationFailedTitle", "Login failed" },

         // version problems: 2 args expected, the string format of the version # for the server, then the server's
         // interface version
         { "InterfaceMismatch", "Build \"{0} Interface {1}\" of the server is not supported by this client." },
         { "VersionMismatchTitle", "Version Unsupported" },
         { "UnknownServerVersion", "Unknown" },

         // labels for pipe type
         { "QueryPipe", "Query" },
         { "UpdatePipe", "Update" },

         // SqlCataloger messages used for errors
         // each message expects a single argument that is the text of the actual exception
         { "CatalogerIOException", "An IO exception occurred during cataloging. The exception text is \n\"{0}\"" },
         { "CatalogerAuthException", "An authorization exception occurred during cataloging. The exception text is \n\"{0}\"" },
         { "CatalogerServerException", "A server exception occurred during cataloging. The exception text is \n\"{0}\"" },
         { "CatalogerExceptionTitle", "Cataloger Exception" },

         // Message to user when app is locked by himself
         { "OverrideLockTitle", "Override Lock?" },
         { "OverrideLock", "This application is currently locked by you in another session. This may have happened because the server or workbench crashed while the application was open. If you don't have this application open elsewhere, you may override this lock. Do you wish to override the lock? (Pressing No will open the app read/only, pressing Cancel will not open the app)" },
         { "OverrideLockFailedTitle", "Application Locked!" },
         { "OverrideLockFailed", "The old lock expired and someone else opened the file between the time the dialog appeared and you pressed the Yes button. The application will not be opened." },

         /* used whereever we want to indicate to a user that this text
            should be replaced by the real server root */
         { "ServerRootPlaceholder", "<Server root>" },

         /* Message when the text a user enters into a cell that needs to be
            converted to a DT... type failes */
         { "ConversionFailedTitle", "Conversion Failed" },

         /* Requires 3 parameters (int, String, String), position of the parse
            failure, first position value (0 or 1), and allowed format(s) */
         { "CantParseDate", "Unable to parse entry.\nUnrecognized character found at position {0,number,integer} ({1} is the first position).\n"
            + "Use one of the following formats (date or time may be omitted):\n{2}" },

         /* Requires 3 parameters (int, String, String), position of the parse
            failure, first position value (0 or 1), and allowed format */
         { "CantParseNumber", "Unable to parse entry.\nUnrecognized character found at position {0,number,integer} ({1} is the first position).\n"
                     + "Number expected in the following format (grouping separator is optional): {2}" },

         /* simpler version when format isn't known (requires first 2 params above */
         { "CantParseNumberNoFormat", "Unable to parse entry.\nUnrecognized character found at position {0,number,integer} ({1} is the first position).\n" },

         /* Error text when html mapping property file can't be found.
            1 arg is expected for the first msg, which is the name of the properties file */
         { "missingHelpMap", "The Help Id mapping file cannot be found or failed to load. Looking for \"{0}\". No help will be available for this session." },
         { "missingHelpMapTitle", "Missing File" },
         { "noHelp", "No help found for this topic." },
         { "noHelpTitle", "Missing Help" },
         { "missingHelpFile", "The help file for this topic is missing ({0})." },
         { "noHelpSet", "The Help file is not provided. So Help is not available for this session." },
         { "invalidHelpSetTitle", "Invalid HelpSet" },

         /* SAX errors */
         { "SaxErrorTitle", "SAX Error" },
         /* Expects 4 args, detail message, line # of error, column # of error and SystemId */
         { "SaxParseError", "An error occurred while trying to parse the file after running it through tidy. "
                  + "The following line/column numbers will give an approximate idea of where the error occurred. "
                  + "The reported error was: {0}, Line: {1}, Column: {2}, SystemID: {3}" },

         /* Expects 1 arg, detail message */
         { "SaxError", "An error occurred while trying to process the file after running it through tidy. "
               + "The reported error was: {0}" },

         /* Heading for multiple Sax parse errors, no args */
         { "SaxParseErrorHeader", "Errors occurred while trying to parse the file. "
                  + "The following line/column numbers will give an approximate idea of where each error occurred: \n"},

         /* Detail of single error, use with SaxParseErrorHeader.
            Expects 4 args, detail message, line # of error, column # of error and SystemId */
         { "SaxParseErrorDetail", "Error: {0}, Line: {1}, Column: {2}, SystemID: {3}\n" },

         /* Bad slave DTD warning (if the name of any child element in the slave
            tree matches the root element name of the master tree) */
         { "DtdMergeWarningTitle", "Warning: Bad DTD" },

         { "DtdReplaceWarningBody", "Dragging an HTML file onto an XML resource will replace the existing DTD file.\nYou may want to drag it onto an XSL resource instead.\n\nDo you still want to proceed?\n" },

         { "DtdMergeWarning", "A child element in the new DTD matches the root element name of the original DTD.\n\nThe DTD generated from {0} cannot be used." },

         { "NoRootForDTD", "{0}\n\nThis HTML file probably does not contain any dynamic variables.\n\nIt cannot be used." },

         // Data type resource key, key is the name of the DataType class
         // without the package prefix (PS, DT)
         { "CgiVariable", "DTCgiVariable" },
         { "SingleHtmlParameter", "DTSingleHtmlParameter" },
         { "HtmlParameter", "DTHtmlParameter" },
         { "Cookie", "DTCookie" },
         { "XMLField", "DTXMLField" },
         { "XmlField", "DTXMLField" },
         { "BackendColumn", "DTBackendColumn" },
         { "BackEndColumn", "DTBackendColumn" },
         { "ExtensionCall", "DTExtensionCall" },
         { "TextLiteral", "DTTextLiteral" },
         { "NumericLiteral", "DTNumericLiteral" },
         { "DateLiteral", "DTDateLiteral" },
         { "UserContext", "DTUserContext" },
         { "ContentItemStatus", "DTContentItemStatus" },
         { "RelationshipProperty", "DTRelationshipProperty" },
         { "OriginatingRelationshipProperty", "DTOriginatingRelationshipProperty" },
         { "Macro", "DTMacro" },

         // Data type names, key is the name of the DataType class
         { "DTCgiVariable", "CGI Variable" },
         { "DTSingleHtmlParameter", "Single HTML Parameter" },
         { "DTHtmlParameter", "HTML Parameter" },
         { "DTCookie", "Cookie" },
         { "DTXMLField", "XML Element" },
         { "DTBackendColumn", "Backend Column" },
         { "DTExtensionCall", "Extension Call" },
         { "DTTextLiteral", "Literal" },
         { "DTNumericLiteral", "Number" },
         { "DTDateLiteral", "Date" },
         { "DTUserContext", "User Context" },
         { "DTContentItemStatus", "Content Item Status" },
         { "DTContentItemData", "Content Item Data" },
         { "DTRelationshipProperty", "Current Relationship Property" },
         { "DTOriginatingRelationshipProperty", "Originating Relationship Property" },
         { "DTMacro", "Macro" },

//       { "", "" },

         { "ParameterMismatch", "The number of values found in the call ({0}) don''t match the number of defined parameters. This will happen if the exit definition changes the number of parameters after this call was created." },

         // Message when dropping xsl page on xsl resource
         // Expects 1 param, the name of the page used to replace the result page
         { "replaceResultPage", "Replaced result page with {0}" },


         //***********************
         // Images for different figures and connectors (may be same as figure shown
         //    in menu)
         //***********************
         { "gif_RigidConnPtLeftSideIcon", "images/cprigid_leftside.gif" },
         { "gif_RigidConnPtRightSideIcon", "images/cprigid_rightside.gif" },
         { "gif_RigidConnPtFrontIcon", "images/cprigid_front.gif" },
         //////////////////// need for transplant ///////////////////////
         { "gif_RigidConnPtPreIcon", "images/cprigid_front.gif" },
         { "gif_RigidConnPtPostIcon", "images/cprigid_front.gif" },
         ////////////////////////////////////////////////////////////////
         { "gif_FlexConnPtInputIcon", "images/cpflex_input.gif" },
         { "gif_FlexConnPtOutputIcon", "images/cpflex_output.gif" },

         { "gif_DupeIcon", "images/dupe.gif" },
         { "gif_DupePressedIcon", "images/dupe_pressed.gif" },
         { "gif_DupeButton", "images/dupeButton.gif" },
         { "gif_DupePressedButton", "images/dupeButton_pressed.gif" },
         /*
         { "gif_InsertIcon", "images/insert.gif" },
         { "gif_BrowseIcon", "images/optional.gif" },
         { "gif_DeleteIcon", "images/delete.gif" },
         { "gif_ListBoxTestIcon", "images/celltest.gif" },
         { "gif_ImageListControlLeft", "images/leftbutton.gif" },
         { "gif_ImageListControlRight", "images/rightbutton.gif" },
         */
         // images for app frame
         { "gif_Requestor", "images/role.gif"},
         { "gif_ResultPage", "images/resultpage.gif" },
         { "gif_AppSecurity", "images/appsec_icon.gif" },
         { "gif_ExtInterface", "images/extiface.gif" },
         { "gif_StylesheetSelector", "images/stylesheet_selector.gif" },
         { "gif_ApplicationFile", "images/genobj.gif" },
         { "gif_LoginWebpage_off", "images/loginpage_off.gif" },
         { "gif_ErrorWebpage_off", "images/errorpage_off.gif" },
         { "gif_Notifier_off", "images/notifier_off.gif" },
         { "gif_BinaryResource", "images/biresource.gif" },
         { "gif_XslFile", "images/extiface.gif" },
         { "gif_ContentEditor", "images/cdataset.gif" },

            // images for global app panel
         { "gif_LoginWebpage", "images/loginpage.gif" },
         { "gif_ErrorWebpage", "images/errorpage.gif" },
         { "gif_Notifier", "images/notifier.gif" },
         { "gif_ApplicationIcon", "images/appicon.gif" },

         // images for pipe frame
         { "gif_Selector", "images/selector.gif" },
         { "gif_QueryPipe", "images/qrypipe.gif" },
         { "gif_UpdatePipe", "images/updpipe.gif" },

         { "gif_Encryptor", "images/encryptor.gif" },
            /* offset to translate image from its center so it is properly placed
               on the pipe */
            { "pt_Encryptor", new Point( 0, 17 ) },

         { "gif_TransactionMgr", "images/transaction.gif" },
         { "gif_ResultPager", "images/pager.gif" },

         /////////////////// needed for transplant ////////////////////////
         { "gif_JavaExit", "images/formula_16.gif" },
        /* offset to translate image from its center so it is properly placed
            on the pipe */
         { "pt_JavaExit", new Point( 0, 0 ) },
         /////////////////// needed for transplant ////////////////////////

         { "gif_Mapper", "images/mapper.gif" },
         { "gif_Synchronizer", "images/synchronizer.gif" },
         { "gif_BEDatatank", "images/betank.gif" },
         { "gif_PageDatatank", "images/pagetank.gif" },
         { "gif_BEDatatank_32", "images/betank_32.gif" },
         { "gif_PageDatatank_32", "images/pagetank_32.gif" },
         { "gif_ControlPanel", "images/ctrlpanel.gif" },
         { "gif_Browser", "images/browser.gif" },
         { "gif_Conditionals", "images/conditionals.gif" },
         { "gif_NoConditionals", "images/no_conditionals.gif" },
         { "gif_Udfs", "images/udf.gif" },
         { "gif_Cgis", "images/udf.gif" },
         { "gif_UserContext", "images/udf.gif" },
         { "gif_FormFields", "images/pagetank.gif" },


         // cursor images and hotspots
         { "gif_ConnectCursor", "images/curconn.gif" },
         { "pt_ConnectCursor", new Point(15, 9) },
         { "gif_NoConnectCursor", "images/curnoconn.gif" },
         { "pt_NoConnectCursor", new Point(15, 9) },

         { "gif_Warning", "images/dupe_pressed.gif" },

         // button images
         { "gif_right", "images/right.gif" },

      //***********************
      // default names for update actions
      //***********************
         { "DefaultActionField", "DBActionType" },
         { "DefaultQueryActionName", "QUERY" },
         { "DefaultUpdateActionName", "UPDATE" },
         { "DefaultInsertActionName", "INSERT" },
         { "DefaultDeleteActionName", "DELETE" },

      //***********************
      // Error type messages
      //***********************
         { "MissingDtdFile", "The DTD file associated with this tank could not be found." },
         { "MissingDtdFileTitle", "Missing DTD" },

         // the expected param is the filename
         { "FileDesc", "File: {0}" },
         { "ConfigNotInit", "Configurator accessed before initialization." },
         { "LoadIconFail", "Missing icon file" },
         { "MissingResourceString", "Message missing from resources" },

         // the expected param is the resource key
         { "MissingResourceKey", "Message missing from resources for key: {0}" },

         // the expected params are detail message and the resource bundle name
         { "MissingResources", "{0}: {1}" },

         // deprecated control message param is the control that has been
         // deprecated, param 1 is the control to use in its place
         {"DeprecatedCtrlMessage", "The control: {0} has been deprecated.  " +
            "Use {1} instead without additional changes." },

         // If a replacement control has not been specified use this message
         {"DeprecatedCtrlMessageNoReplacement","The control: {0} has been " +
            "deprecated.  A replacement control has not been specified." },

         // title for the deprecated dialog box
         {"DeprecatedCtrlDialogTitle", "Deprecated Control"},

         // the expected param is the detail message for the error
         { "FactoryError", "An unrecoverable error has occurred trying to obtain " +
            "factory {0}. The operation cannot complete. The error text is: \n{1}" },

         // the expected param is the passed in factory name
         { "FigFactoryDerivedErr", "{0} must be derived from FigureFactory" },

         // the expected param is the passed in class name
         { "ClassNotFoundErr", "Class not found: {0}" },

         // the expected param is the passed in class name
         { "InstantiationErr", "Couldn't instantiate class: {0}" },

         // the expected param is the passed in class name
         { "ClassAccessErr", "Couldn't access class: {0}" },

         // the expected params are the exception message and the resource key
         { "MissingResourceExceptionFormat", "{0}: {1}" },

         // expected param is the method name
         { "NullArgFormat", "An unexpected null argument was passed to {0}" },

         // expected param is the detail message
         { "FatalException", "A fatal exception has occurred with the "
            + "following text:\n {0} \nThe program cannot continue." },

         // expected param is the name of the unsupported figure
         { "UnsupportedFigure", "Request received to create unsupported figure: {0}" },

         { "EmptyStringErr", "String was unexpectedly empty." },

         { "FlavorArrayTooSmall", "Increase size of Flavor array." },

         { "BadType",  "Bad type for internal name key. Internal keys must be strings." },

         // expected param is name of the resource bundle key
         { "PointNotFound",  "Point requested but not found for key: {0}" },

         // expected param is name of the resource bundle key
         { "WrongObjForPt", "Point requested but found different object for key: {0}" },

         { "LayoutMgrChanged", "Default layout manager for JPanel has changed." },

         { "DynActionsImplErr", "IDynamicActions implementation inconsistent" },

         // expected params are class names
         { "MustBeOverridden", "{0} must be overridden if {1} is overridden." },

         // expected param is the class name of the method parameter that can't be null
         { "CantBeNull", "{0} cannot be null" },

         { "UnsupportedCombo", "Unsupported preferred attach position combo" },

         { "NegativeNotAllowed", "Negative values not allowed." },

         { "InvalidPosFlags", "Invalid preferred position passed in." },

         { "UnprocessedPoints", "All points not processed when creating handles" },

         // expected param is class name of types stored in vector
         { "UnexpectedType", "Unexpected type in {0} vector." },

         { "BadSegIndex", "Bad segment index." },

         { "PtNotFoundForHandle", "Point not found for handle." },

         { "InvalidPtForLine", "Invalid point location for poly line point" },

         { "EndPtNotFd", "End Point not found." },

         { "ConnNotSupp", "This type of connection is not currently supported; using simple one." },

         { "MPNotInit", "midPoints not initialized" },

         { "NewAP", "New AP_ value added, check this method for errors." },

         { "InvTransformType", "Invalid transform type passed in: {0}" },

         { "StraightLinesOnly", "Only horizontal or vertical lines are supported by this method." },

         { "InvalidHandleType", "Invalid type for handle: {0}" },

         { "MissingHandles", "Insufficient handles for link, found {0}" },

         { "NoPoints", "No points in link." },

         { "OrigPtNotFd", "Original point not found on poly line." },

         { "ConnPtNotFd", "When trying to remove a connection point, it wasn't found on either end." },

         { "MissingPoints", "Invalid line, insufficient points" },

         { "NotImplForOtherSizes", "Code not implemented for other line sizes." },

         // the expected param is the invalid position
         { "InvalidAPFlags", "Invalid attach position passed in: {0}" },

         { "PaneMustBeInit", "Drawing pane must be set before this method is called." },

         { "BadMouseEvt", "Unexpected mouse event received." },

         { "InvHitPt", "Invalid hitPoint" },

         { "InvPtInArray", "Invalid points in point array." },

         { "LimitsNotInit", "Limits not initialized." },

         { "InvName", "Invalid name" },

         { "DragCompNotSet", "Dragging component not set." },

         { "MissingFlavor", "Found info flavor w/o data flavor" },

         { "ForgotCBwDI", "You forgot to set the clipboard with the drag info." },

         { "MissingParent", "Missing parent component" },

         // expected param is the name of the method
         { "UnexpectedNullReturn", "{0} returned null unexpectedly" },

         // expected param is the class name of the expected type
         { "IncorrectType", "Incorrect type passed, expected {0}" },

         { "ChangedPropKeys", "Property keys required for Object store have changed." },

         /* the expected param is the location and size of the window pos, concat
            as a single string */
         { "InvWindowPos", "Invalid window position values: {0}" },

         // expected param is the detail message about the format error
         { "BadPosFormat", "Window position configuration not in correct format: {0}" },

         { "OSNotInit", "Object store not initialized" },

         { "BadActionArray", "Null or empty action array returned" },

         // expected param is the supplied menu name
         { "BadDynMenuName", "Called with a non-dynamic menu name: {0}" },

         // expected param is the internal menu name
         { "BadDynMenuKey", "Dynamic actions present but menu item not found for key: {0}" },

         { "MenuNull", "Main menu is null" },

         // expected param is the action name for the missing action
         { "TBActionMissing", "Toolbar: No action for {0}" },

         { "BadObjTypeFromUConfig", "Got a non-String object from PSUserConfiguration" },

         { "IEditorNotImpl", "Editor doesn't implement the IEditor interface." },

         { "MissingEditClass", "Couldn't find the editor class for this figure." },

         { "MissingFigureType", "A new figure was added but a case to construct it is missing." },

         { "BadDataObj", "Data object is the wrong type." },

         { "ModalOnly", "Dialogs derived from PSEditorDialog must be modal." },

         // expected param is the detail message from the exception
         { "ServerConnException", "A failure occurred when trying to connect to the server. The following message was returned: \n{0}" },

         // expected param is the detail message from the exception
         { "AuthException", "You are not authorized to perform this action. The following message was returned: \n{0}" },

         // expected params: 0=start/stop 1=application name, 2=detail message from the exception
         { "Start", "start" },
         { "Stop", "stop" },
         { "StartStopApplicationException", "A failure occurred when trying to {0} application {1}. The following message was returned: \n{2}" },

         // expected param: 0=application name, 1=detail message from the exception
         { "ValidateApplicationException", "The application {0} is not valid. \nYou must have a valid application to enable and activate it. \nThe following message was returned: \n{1}" },

         // expected param: 0=application name, 1=detail message from the exception
         { "NotUniqueApplicationException", "The name of application {0} is not unique. \nYou must have a unique name in order to save this application. \nThe following message was returned: \n{1}" },

         { "ConnectingWhileConn", "A dynamic connection was created while one exists. This is not allowed." },

         { "OnlyUICAllowed", "Only UIConnectableFigures are allowed to be added to this container." },

         { "InvalidConstraint", "Invalid type for constraint passed, must be AttachConstraint." },

         { "SizeMismatch", "The number of elements in a supplied vector did not match the number of elements in the reference vector." },

         { "WrongObjectType", "The wrong class of object was found in a supplied vector." },

         { "NewTransactionType", "An unrecognized transaction type was discovered while creating a resource." },

         { "MissingInterface", "A required interface was not implemented by the supplied object." },

         { "NewTransactionType", "An unrecognized transaction was found." },

         { "UnrecognizedPipeType", "An unrecognized pipe type was passed." },

         { "BadUICType", "Non-UIConnectableFigure found in app frame." },

         { "OverwriteExitstingFile", "The file you have choosen already exists. \nPlease confirm to overwrite it." },

         { "NotAnApplication", "The file you have choosen is not a Rhythmyx application." },

         { "LoosingUnattachedObjects", "Your resource contains multiple pipes or / and unattached objects. \nHit OK to remove additional pipes and unattached objects. \nHit Cancel to return to this edit session." },

         { "ErrorNoBackEndTableForSelector", "To edit this Selector, at least one back end table must be available." },

         // expected param: 0==application name
         { "SaveApplication", "Do you want to save the application:\n{0}\nbefore closing?" },

         // expected param: 0==application name
         { "RemoveApplication", "This will remove the selected application and all of its associated files.  \nAre you sure you want to remove {0}?" },

         // expected param: 0==detail message from the exception
         { "InvalidURL", "The provided URL is not valid. You must correct this to save.\n{0}" },
         { "InvalidURLFilepath", "The filepath must be within the Rhythmyx root." },

         { "LoosingApplicationElements", "Your application contains unattached result pages which can't be saved. Delete these result pages or ensure they are attached to the appropriate dataset." },
         { "NoLinkProperties", "This link has no properties to edit." },
         { "NoLinkPropertiesTitle", "Link Properties" },


         {  "ExceptionTitle", "Exception Caught" },
         { "predefinedUdfs", "User Defined Functions" },
         { "cookieVariables", "Cookie"},
         {  "paramContext", "HTML Parameters"},

         { "cgiVariables", "CGI Variables" },
         { "userContext", "User Context" },
         { "formFields", "Form Fields" },

         { "MapperCreateFail", "Creation of mapper failed due to ClassCastException." },

         // expected param: 0=file name, 1=detail message from the exception
         { "FileNotFound", "File {0} not found.\n{1}"},

         // expected param: 0=file name, 1=detail message from the exception
         { "InvalidXmlDocument", "File {0} is not a valid XML, XSL or DTD file.\n{1}"},

         { "ReplaceApplication", "An application with this name already exists!\nDo you want to replace the application?"},

         // expected param: 0 = the name of the extension, 1 = exception error msg
         { "ExtDefLoadFailed", "Failed to load the extension definition for ''{0}''. The error was: {1}. You will not be able to edit this call during this session." },

         // expected param: 0 = the name of the extension
         { "CouldNotFindExtDef", "Could not find the extension definition for ''{0}''. You should replace this extension with a valid one." },

         // expected param: 0=locker
         { "ApplicationLocked", " (Locked by: {0})"},

         // expected param: 0=detail message from the exception
         { "GenericServerError", " (Server error:\n {0})"},

         // PSHelp.dll is expected in \Rhythmyx root\designer\bin directory
         { "HelpDLLMissingError", "PSHelp.dll is missing. Make sure this dll is in directory:\n\\bin\\" },

         // Rhythmyx.chm is expected in \Rhythmyx root\Docs directory
         { "HelpCHMMissingError", "Rhythmyx.chm is missing. Make sure this file is in directory:\n\\Docs\\" },

         // Tutorial.chm is expected in \Rhythmyx root\Docs directory
         { "TutorialCHMMissingError", "Tutorial.chm is missing. Make sure this file is in directory:\n\\Docs\\" },

         // tidy has failed
         { "tidyIOError", "Could not process the HTML file for querying. \"tidy\" has failed." },

         // not a valid HTML for query
         { "emptyDTD", "The provided HTML file does not have a base element. \nThis can't be used as a query resource." },

         // Auxiliary files directory is missing
         { "auxDirMissingError", "The requested import application \"{0}\" has auxiliary files but could not be found.\n\nMake sure the auxiliary files directory is the same name and exists in the same directory as the import application." },

         { "CannotDeleteApp", "Save failed!\nYou do not have write access to this application." },

         { "CantConvertInput", "Couldn't convert the input string into the specified type." },

         // Error getting feature set from server
         { "featureSetWarning", "Some features will not be available.  This may occur if you are connecting to an older version of the server." },
         { "featureSetNotInit", "FeatureSet accessed before initialization." },

         // trace apply called with no options selected
         { "TraceApplyNoOptions", "This will stop tracing since no trace options are selected.  Are you sure you want to stop tracing?" },

         // Error handling filenames that start with non-standard XML characters
         {"changeFileName","The file name may be used as an XML element. Any name "
               + "that does not follow XML naming standards will be changed by replacing "
               + "non-standard characters with an '_'."},

         // list entry for catalogers in case cataloging failed
         { "catalogerFailed", "Cataloger Failed"},

         // Resources have been selected for deleting
         {"deleteRes", "One or more resources are about to be deleted.\n"
               + "Do you want to continue?"},

         //System or shared definitions are not loaded
         { "sysSharedNotLoaded", "Content Editor System or Shared Definition is not loaded.\n"
               + "Content Editor user interface will not be supported"},

         // CONTENTTYPES table request URLs updated when saving content editor resource
         { "ContentTypeUpdateWarning", "The content editor ''{0}'' in this application will now be used to edit items of the ''{1}'' content type"},

         // expected param: 0=detail message from the exception
         { "i18nResourcesNotLoaded", "Internationalization support will be disabled as the 18n resources failed to load due to the following error: ''{0}''"},

         // Can only have one webImageFx control per CE
         {"wifxTooManyControls", "Cannot have more than one sys_webImageFx control for each content editor."},

         // wifx control must use name "uploadfilephoto"
         {"wifxWrongName", "The sys_webImageFx control must use the name \"uploadfilephoto\"."},

         // Wifx and sys_File cannot exist together
         {"wifxAndSysFile", "The sys_webImageFx control cannot exist with the sys_File control in the same content editor."},

         // Wifx requires uploadfilephoto_type field exist
         {"wifxAndMimeType", "Cannot find an \"uploadfilephoto_type\" field. The sys_webImageFx control requires an \"uploadfilephoto_type\" field exist in the same content editor."},
         
         // Ephox and WEP cannot exist on the same content editor
         {"ephoxAndWep", "The Ephox EditLive control and the Ektron eWebEditPro control cannot co-exist in the same editor."},

 
      //***********************
      // Window titles
      //***********************
         { "FatalDlgTitle", "Fatal Error" },
         { "FactoryErrorTitle", "Unrecoverable Error" },

         /*
          * Main Desginer window title, takes three params:
          * 0 - protocol: The name of the protocol used to connect to server,
          * which could be "http" or "https".
          * 1 - server: The name of the server the designer is connected to
          * 2 - port: the port number the connection to the server is using
          */
         { "MainFrameTitle", "Connected to: {0}://{1}:{2} - Rhythmyx Workbench" },
         { "OpErrorTitle", "Operation Error" },
         { "InputErrorTitle", "Input Error" },
         { "ServerConnErr", "Server Connection Error" },
         { "AuthErr", "Authorization Error" },
         { "LockErr", "Application Locked Error" },
         { "AssertionFailed", "An Assertion Failed" },
         { "ConfirmOperation", "Confirm operation" },
         { "ApplicationErr", "Application Error" },
         { "ServerErr", "Server Error" },
         { "errorFileName", "File Name Changed"},
         { "ExportDlgTitle", "Export"},
         // Error msg when cataloging DTD fails (probably a parse error)
         { "BadDTDTitle", "DTD Invalid" },
         { "NotifierPropertiesDialogTitle", "Notifier Properties" },
         { "SecurityDialogTitle", "Security" },

         // Read only Window title substring
         { "ReadAccessOnly", " (Read Only)" },

         { "Warning", "Warning" },
         { "deleteConf", "Delete Confirmation"},

         {"error" , "Error"},
         { "IOErrorTitle", "IO Error"},

         { "invalidXml" , "Invalid XML File" },
         { "ErrorEmptyXml", "Cannot extract XML from the specified file, as it is empty." },
         { "ErrorCannotReadXml", "Cannot extract XML from the specified file, as it cannot be read (perhaps it has been moved or deleted)." },

      //***********************
      // Menu items
      //***********************

      //***********************
      // Application menu
      //***********************
         { "menuApp", "Application" },
         { "mn_menuApp", 'A' },

         { "menuAppNew", "New" },
         { "mn_menuAppNew", 'N' },
         { "ks_menuAppNew", SWT.CTRL | 'N'},
         { "gif_menuAppNew", "images/new.gif" },
         { "tt_menuAppNew", "Create an empty application." },

         { "menuAppSave", "Save" },
         { "mn_menuAppSave", 'S' },
         { "ks_menuAppSave", SWT.CTRL | 'S'},
         { "gif_menuAppSave", "images/save.gif" },

         { "menuAppSaveAs", "Save As..." },
         { "mn_menuAppSaveAs", 'A' },

         { "menuAppImport", "Import..." },
         { "mn_menuAppImport", 'I' },

         { "menuAppExport", "Export..." },

         { "menuAppPageSetup", "Page Setup..." },
         { "mn_menuAppPageSetup", 'u' },

         { "menuAppPrint", "Print" },
         { "mn_menuAppPrint", 'P' },
         { "ks_menuAppPrint", SWT.CTRL | 'P'},
         { "gif_menuAppPrint", "images/print.gif" },

         { "menuAppSecurity", "Security..." },
         { "mn_menuAppSecurity", 'c' },

         { "menuAppProperties", "Properties" },
         { "mn_menuAppProperties", 'r' },

         { "menuAppEnable", "Start" },
         { "mn_menuAppEnable", 'a' },
         { "gif_menuAppEnable", "images/startapp_16.gif" },

         { "menuAppDisable", "Stop" },
         { "mn_menuAppDisable", 'p' },
         { "gif_menuAppDisable", "images/stopapp_16.gif" },

         { "menuAppOpen0", "0: " },
         { "mn_menuAppOpen0", '0' },
         { "menuAppOpen1", "1: " },
         { "mn_menuAppOpen1", '1' },
         { "menuAppOpen2", "2: " },
         { "mn_menuAppOpen2", '2' },
         { "menuAppOpen3", "3: " },
         { "mn_menuAppOpen3", '3' },

         { "menuAppExit", "Exit" },
         { "mn_menuAppExit", 'x' },

         { "linktitle", "Select a link to edit." },


      //***********************
      // Edit menu
      //***********************
         { "menuEdit", "Edit" },
         { "mn_menuEdit", 'E' },

         { "menuWindow", "Window" },
         { "mn_menuWindow", 'W' },
         { "WindowListTitle", "Window List" },

         { "requestorProperties", "Request Properties" },
         { "resultPageProperties", "Link Properties" },


         { "menuEditCut", "Cut" },
         { "mn_menuEditCut", 't' },
         { "ks_menuEditCut", SWT.CTRL | 'X'},
         { "gif_menuEditCut", "images/cut.gif" },
         { "tt_menuEditCut", "Remove selected item and place in clipboard." },

         { "menuEditCopy", "Copy" },
         { "mn_menuEditCopy", 'C' },
         { "ks_menuEditCopy", SWT.CTRL | 'C'},
         { "gif_menuEditCopy", "images/copy.gif" },
         { "tt_menuEditCopy", "Place a copy of the current selection on the clipboard." },

         { "menuEditPaste", "Paste" },
         { "mn_menuEditPaste", 'P' },
         { "ks_menuEditPaste", SWT.CTRL | 'V'},
         { "gif_menuEditPaste", "images/paste.gif" },
         { "tt_menuEditPaste", "Copy contents of the clipboard to the current window" },

         { "menuEditClear", "Clear" },
         { "mn_menuEditClear", 'a' },
         { "ks_menuEditClear", (int) SWT.DEL},
         { "tt_menuEditClear", "Delete all selected items in the current window." },

         { "menuEditSelectAll", "Select All" },
         { "mn_menuEditSelectAll", 'l' },
         { "ks_menuEditSelectAll", SWT.CTRL | 'L'},
         { "tt_menuEditSelectAll", "Select all items in current window." },

         { "menuEditDeselectAll", "Deselect All" },
         { "mn_menuEditDeselectAll", 'd' },
         { "ks_menuEditDeselectAll", SWT.CTRL | 'D'},
         { "tt_menuEditDeselectAll", "Deselect all items in current window." },


      //***********************
      // View menu
      //***********************
         { "menuView", "View" },
         { "mn_menuView", 'V' },

         { "menuViewBrowser", "Explorer" },
         { "mn_menuViewBrowser", 'E' },
//       { "gif_menuViewBrowser", "images/view_br.gif" },
         { "tt_menuViewBrowser", "Toggle visibility of the explorer pane." },

         { "menuViewToolBars", "Tool Bars" },
         { "mn_menuViewToolBars", 'T' },
//       { "gif_menuViewToolBars", "images/view_tb.gif" },
         { "tt_menuViewToolBars", "Toggle visibility of the tool bars." },

         { "menuViewStatusBar", "Status Bar" },
         { "mn_menuViewStatusBar", 'S' },
//       { "gif_menuViewStatusBar", "images/view_sb.gif" },
         { "tt_menuViewStatusBar", "Toggle visibility of the status bar." },

         { "menuViewProperties", "Properties" },
         { "mn_menuViewProperties", 'P' },
         { "tt_menuViewProperties", "Bring up the property dialog for the selected item." },

      //***********************
      // Insert menu
      //***********************
         { "menuInsert", "Insert" },
         { "mn_menuInsert", 'I' },

         { "menuInsertResultPage", "Page" },
         { "mn_menuInsertResultPage", 'P' },
         { "gif_menuInsertResultPage", "images/resultpage_16.gif" },
         { "tt_menuInsertResultPage", "Insert a new Web page object into current window" },

         { "menuInsertDirectedConnection", "Link" },
         { "mn_menuInsertDirectedConnection", 'L' },
         { "gif_menuInsertDirectedConnection", "images/connect_16.gif" },
         { "tt_menuInsertDirectedConnection", "Insert a new link." },

         { "menuInsertQueryDataset", "Query Resource" },
         { "mn_menuInsertQueryDataset", 'Q' },
         { "gif_menuInsertQueryDataset", "images/qdataset_16.gif" },
         { "tt_menuInsertQueryDataset", "Insert a new query resource into current window" },

         { "menuInsertUpdateDataset", "Update Resource" },
         { "mn_menuInsertUpdateDataset", 'U' },
         { "gif_menuInsertUpdateDataset", "images/udataset_16.gif" },
         { "tt_menuInsertUpdateDataset", "Insert a new update resource into current window" },

         { "menuInsertBinaryResource", "Non-text Resource" },
         { "mn_menuInsertBinaryResource", 'r' },
         { "gif_menuInsertBinaryResource", "images/biresource_16.gif" },
         { "tt_menuInsertBinaryResource", "Insert a new non-text resource" },

         { "menuInsertContentEditor", "Content Editor Resource" },
         { "mn_menuInsertContentEditor", 'c' },
         { "gif_menuInsertContentEditor", "images/cdataset_16.gif" },
         { "tt_menuInsertContentEditor", "Insert a new content editor resource into current window" },

         // pipe objects
         { "menuInsertSelector", "Selector" },
         { "mn_menuInsertSelector", 'S' },
         { "gif_menuInsertSelector", "images/selector_16.gif" },
         { "tt_menuInsertSelector", "Insert empty selector" },

         { "menuInsertEncryptor", "Encryption Checker" },
         { "mn_menuInsertEncryptor", 'E' },
         { "gif_menuInsertEncryptor", "images/encryptor_16.gif" },
         { "tt_menuInsertEncryptor", "Insert new encryption checker" },

         { "menuInsertTransactionMgr", "Transaction Manager" },
         { "mn_menuInsertTransactionMgr", 'T' },
         { "gif_menuInsertTransactionMgr", "images/transaction_16.gif" },
         { "tt_menuInsertTransactionMgr", "Insert transaction manager" },

         { "menuInsertResultPager", "Result Pager" },
         { "mn_menuInsertResultPager", 'P' },
         { "gif_menuInsertResultPager", "images/pager_16.gif" },
         { "tt_menuInsertResultPager", "Insert result pager" },


         { "menuInsertJavaExit", "Exit" },
         { "mn_menuInsertJavaExit", 'E' },
         { "gif_menuInsertJavaExit", "images/formula_16.gif" },
         { "tt_menuInsertJavaExit", "Insert empty exit" },


         { "menuInsertMapper", "Mapper" },
         { "mn_menuInsertMapper", 'M' },
         { "gif_menuInsertMapper", "images/mapper_16.gif" },
         { "tt_menuInsertMapper", "Insert empty mapper" },

         { "menuInsertSynchronizer", "Updater" },
         { "mn_menuInsertSynchronizer", 'U' },
         { "gif_menuInsertSynchronizer", "images/synchronizer_16.gif" },
         { "tt_menuInsertSynchronizer", "Insert default updater in current window" },

         { "menuInsertPageDatatank", "Page Dataset" },
         { "mn_menuInsertPageDatatank", 'g' },
         { "gif_menuInsertPageDatatank", "images/pagetank_16.gif" },
         { "tt_menuInsertPageDatatank", "Insert page dataset" },

         { "menuInsertBEDatatank", "Backend Dataset" },
         { "mn_menuInsertBEDatatank", 'B' },
         { "gif_menuInsertBEDatatank", "images/betank_16.gif" },
         { "tt_menuInsertBEDatatank", "Insert backend dataset" },

/* Template for new menu items
         { "menuInsert", "" },
         { "mn_menuInsert", '' },
         { "gif_menuInsert", "images/.gif" },
         { "tt_menuInsert", "" },
         { "ks_menuInsert", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_,
            java.awt.Event.CTRL_MASK, true) },
*/

      //***********************
      // Legacy menu
      //***********************
         { "menuLegacy", "Legacy UI" },
         { "mn_menuLegacy", 'L' },
         { "tt_menuLegacy", "Legacy UI Menu items (For construction only" },

      //***********************
      // Tools menu
      //***********************
         { "menuTools", "Tools" },
         { "mn_menuTools", 'T' },

         { "menuToolsDebugging", "Debugging..." },
         { "mn_menuToolsDebugging", 'D' },
         { "tt_menuToolsDebugging", "Edit options to enable tracing" },

         { "menuToolsOptions", "Options" },
         { "mn_menuToolsOptions", 'O' },


      //***********************
      // Help menu
      //***********************
         { "menuHelp", "Help" },
         { "mn_menuHelp", 'H' },

         { "menuHelpHelp", "Help for Rhythmyx Designers" },
         { "mn_menuHelpHelp", 'H' },
         { "gif_menuHelpHelp", "images/help.gif" },
         { "tt_menuHelpHelp", "Start the HTML help system." },

         { "menuHelpStartTutorial", "Start Tutorial" },
         { "mn_menuHelpStartOne", 'S' },

         { "menuHelpAbout", "About Rhythmyx..." },
         { "mn_menuHelpAbout", 'A' },


      //***********************
      // AppFigureFrame Drop action popup menu
      //***********************
         { "menuUpdate", "Update" },
         { "mn_menuUpdate", 'U' },
         { "gif_menuUpdate", "images/help.gif" },

         { "menuQuery", "Query" },
         { "mn_menuQuery", 'Q' },
         { "gif_menuQuery", "images/help.gif" },

         { "menuStatic", "Static Web Page" },
         { "mn_menuStatic", 'S' },
         { "gif_menuStatic", "images/help.gif" },

         { "menuXSL", "XSL" },
         { "mn_menuXSL", 'X' },
         { "gif_menuXSL", "images/help.gif" },

         { "menuImage", "Non-text" },
         { "mn_menuImage", 'I' },
         { "gif_menuImage", "images/help.gif" },

         { "menuCE", "Content Editor"},
         { "mn_menuCE", 'E'},
         { "gif_menuImage", "images/help.gif"},


      //***********************
      // Dataset Drop action popup menu
      //***********************
         { "menuReplaceQuery", "Replace DTD and add style sheet" },
         { "mn_menuReplace", 'R' },
         { "gif_menuReplace", "images/help.gif" },

         { "menuReplaceUpdate", "Replace DTD and add HTML page" },
         { "mn_menuReplace", 'R' },
         { "gif_menuReplace", "images/help.gif" },

         { "menuAddStylesheet", "Add Style Sheet" },
         { "mn_menuAddStylesheet", 'S' },
         { "gif_menuAddStylesheet", "images/help.gif" },

         { "menuAddHtmlPage", "Add Static Web Page" },
         { "mn_menuAddHtmlPage", 'P' },
         { "gif_menuAddHtmlPage", "images/help.gif" },

      //***********************
      // Webpage Drop action popup menu
      //***********************
         { "menuUpdateWebpage", "Update Web page with this one" },
         { "menuReplaceWebpage", "Replace Web page with this one" },
         { "menuCancelWebpage", "Cancel" },
         { "menuMoveWebpage", "Drop here" },

      //***********************
      // Selector popup menu
      //***********************
         { "menuFunction", "Function ..." },
         { "menuSingleValue", "Single Value ..." },

      //***********************
      // ContentEditor UI actions
      //***********************
         { "replaceContentMain",
               "The source and target editors have different names. Do you want to replace it anyway?" },
         { "replaceContentTitle", "Replace Editor?" },
         { "statusReplaced", "Replaced existing editor with new one" },
         { "statusUpdated", "Updated existing editor" },
         { "statusNew", "Created new content editor" },
         { "statusNewPageAssembler", "Created new page assembler" },
         { "statusNewSnippetAssembler", "Created new snippet assembler" },
         { "statusCanceled", "Operation canceled by user" },
         { "statusCopied", "Editor XML copied to clipboard" },
         { "ceLoadExceptionTitle", "Exception" },
         { "ceLoadException",
               "An exception occurred while loading the content editor. The text of the exception is: {0}" },
         { "invalidTemplate", "Multi-property simple child field sets are not supported in templates." },
         { "invalidTemplateTitle", "Invalid Template" },
         { "emptyTemplate", "The selected template cannot be used because it is empty." },
         { "cannotReadTemplate", "The selected template cannot be read; maybe the file been renamed or deleted." },
         { "pageAssemblerName", "page_assembler" },
         { "snippetAssemblerName", "snippet_assembler"},
         { "whitebox", "whitebox"},

      //***********************
      // Content Editor export, edit popup menu items
      //***********************
         { "exportXML", "Export XML..." },
         { "editXML", "Edit Source..." },
         { "copyXML", "Copy Source XML" },
         { "pasteXML", "Paste Source XML" },
         { "pasteAsAssembler", "Paste As Assembler" },
         { "pasteAssemblerPage", "Page" },
         { "pasteAssemblerSnippet", "Snippet" },

      //***********************
      // designer properties
      //***********************
         { "propsRoot", "installRoot"  },

         { "askwarningtitle", "Replace Object" },
         { "askwarning", "Do you want to replace the attached object?" },
         { "WARNING", "Replacing Application" },
         { "CONFIRM_REPLACE", "Do you want to replace this application?" },

      //****************************
      // auto joins properties
      //***********************
         { "databaseObjects", "Database Objects" },
         { "enablejoins"    , "Enable Auto joins guess" },
         { "JOIN_OPTION"    , "AutoJoins" },

      //**************************************
      // DTD repeat  gif icons
      //**************************************
         { "gif_repeatoneormore" ,   "images/DTDRepeatOneOrMore.gif"},
         { "gif_repeatzeroormore" , "images/DTDRepeatZeroOrMore.gif"},
         { "gif_repeatoptional",     "images/DTDRepeatOptional.gif"},
         { "gif_repeatexactlyonce",  "images/DTDRepeatOnce.gif"},
         { "gif_repeatat"         ,  "images/DTDRepeatAt.gif"},

      //**************************************
      // DTD repeat  text
      //**************************************
         { "DTDCanAppearExactlyOnce" , "1  Element can appear exactly once"},
         { "DTDCanAppearZeroOrMore"  , "?  Element can appear 0 or 1 times (is optional)" },
         { "DTDCanAppearOneOrMore"   , "1+ Element can appear 1 or more times"},
         { "DTDCanAppearOptional"    , "*  Element can appear 0 or more times"},

      //**************************************
      // UDF  gif icons
      //**************************************
         { "gif_appUdf",  "images/appUdf_icon.gif"},
         { "gif_globalUdf",  "images/globalUdf_icon.gif"},
         { "gif_genericIcon",  "images/generic_icon.gif"},

      //**************************************
      // xsplit settings
      //**************************************
         { "LaunchXsplit",      "Launch XSpLit program when dropping files"},
         { "LAUNCH_XSPLIT" ,    "LaunchXSPlit"},
         { "ShowTidyWarnings",  "Show Tidy Warnings"},
         { "SHOWTIDYMSG",       "ShowTidyMessages"},

      //**************************************
      // arrow settings settings
      //**************************************
         { "Display" ,    "Display"},
         { "EnableArrows", "Enable arrows on links"},
         { "ENABLEARROWS", "EnableArrow"},

      //**************************************
      // Server Objects settings settings
      //**************************************
         {"ServerObject",  "Server Objects"},
         {"JavaExits"   ,  "Java Exits"},
         {"POST_JAVAEXIT",  "Result document processing"},
         {"PRE_JAVAEXIT",   "Request pre-processing"},
         {"RELATIONSHIPS",  "Relationships"},
         {"SEARCHES", "Content Views and Searches"},
         {"DISPLAYFORMATS", "Display Formats"},
         {"ACTIONMENUS", "Action Menus"},


         {"OverrideLockMsg", "Do you want to override the lock?"},
         {"OverrideLockFailedTitle", "Override Lock Failed"},


         {"SmallJavaExit",      "images/formula_16.gif"},

         {"gif_QueryDataset",        "images/qdataset.gif" },
         {"gif_QueryDataset_with2",  "images/qdataset-ex2.gif"},
         {"gif_QueryDataset_left",   "images/qdataset-exl.gif"},
         {"gif_QueryDataset_right",  "images/qdataset-exr.gif"},

         {"gif_UpdateDataset",        "images/udataset.gif" },
         {"gif_UpdateDataset_with2",  "images/udataset-ex2.gif"},
         {"gif_UpdateDataset_left",   "images/udataset-exl.gif"},
         {"gif_UpdateDataset_right",  "images/udataset-exr.gif"},

         {"gif_ContentEditor",        "images/cdataset.gif" },
         {"gif_ContentEditor_with2",  "images/cdataset-ex2.gif"},
         {"gif_ContentEditor_left",   "images/cdataset-exl.gif"},
         {"gif_ContentEditor_right",  "images/cdataset-exr.gif"},

         {"gif_PostJavaExit",                 "images/java-exit.gif"},
         {"gif_PreJavaExit",                  "images/java-exit.gif"},

         {"mustBeAttached", "Object must be attached to be editable"},
         {"InvalidExitTitle"   , "JavaExit"},
         {"InvalidExitText",     "The attached exit(s) value(s) column field must be filled by the user"},
         {"InvalidEmptyExits",   "The Java Exit list can not be empty, to remove all the exits close this dialog and remove the exit"},

      //***************************************
      // MapBrowser UDF tree categories
      //***************************************
         {"MapUdfApplication", "Application"},
         {"MapUdfGlobal", "Global"},
         {"MapUdfPredefined", "Predefined"},
         {"MapUdfLocal", "Local"},

      //***************************************
      // Aux editor popup menu items
      //***************************************
         {"flushTables", "Flush Table Metadata..."},
         {"editFile", "Edit file..."},

      //**************************************
      // Content Editor required properties
      //**************************************
         {"TemplateDlgTitle","Content Editor Templates"},
         {"TemplateListTitle","Choose Template"},
         {"TemplateUnsupportMultiPropertyChild", "Templates do not support multi-property simple child field sets."},
         {"CENameNotChanged","The parent editor name was not changed after loading it from a template. The tables will not be created."},
         {"replacingLocator" , "Replacing the current table locator with the content editor table locator"},
         {"dtdPathNotFound" , "You will need to add the DOCTYPE manually to the exported XML,"
               + "since the 'DTD' directory could not be located."},
         {"childTable", "Child Table Warning"},
         {"treatChildAsParent", "The table you dropped is for the main editor, but it appears to be a child table. Do you want to use it as the main table?" },
         {"firstTable", "Content editor does not support dropping multiple tables, considering the first table in the list and ignoring remaining tables."},
         {"replaceParentMappings", "If you continue, all of the mappings in the main (parent) mapper will be replaced, Do you wish to continue?"},
         {"InvalidCETable", "The table you dropped cannot be used to create or modify a content editor, the resource will not be updated."},
         {"menuParent", "Parent"},
         {"menuSimpleChild", "SimpleChild"},
         {"menuMultiPropertySimpleChild", "MultiPropertySimpleChild"},
         {"menuComplexChild", "ComplexChild"},
         {"replaceMappings", "This table is already used. Do you want to replace the existing mappings?"},
         {"replaceDiffMappings", "This table is already used as {0}. Do you want to replace the existing mappings?"},
         {"invalidReplaceParent", "This table is already used for the parent editor so it can not be used with a child editor."},
         {"invalidReplaceChild", "This table is already used for the child editor so it can not be used with a parent editor."},
         {"typeParent", "Parent"},
         {"typeSimpleChild", "Simple Child"},
         {"typeMultiPropertySimpleChild", "Multi Property Simple Child"},
         {"typeComplexChild", "Complex Child"},
         {"invalidResource", "The resource is invalid for editing."},
         {"NoParentEditorFields", "The parent editor does not have any local field mappings, so no tables will be created at this time."},
         {"invalidNewLocalFields",
            "The mappers {0} contain fields which need" +
            " to be created but do not have a datatype and format specified." +
            " The columns corresponding to these fields will not be created and" +
            " thus the application will not initialize properly if you attempt to"+
            " start it."},
         {"invalidSharedChildMappings", "You have included one or more shared" +
            " child entries {0} in the parent mapper, but have not included" +
            " even one of the fields they reference. The application will not" +
            " initialize properly if you attempt to start it."},
         {"invalidLocalChildMappings", "You have included one or more child" +
            " entries {0} in the parent mapper, but have not defined any" +
            " fields for them. The application will not initialize properly if" +
            " you attempt to start it."},
         {"noTableReference",
            "The table with alias <{0}> is not found in table references list."},
         {"noTableAlias",
           "The table name/alias corresponding to the fieldset <{0}> is not found " +
            "either to create a new table or to add new columns for the new fields " +
            "added to existing editor"},
         {"invalidSingleOccurDep",
            "Different parameters are provided for Single-occurrence " +
            "dependency  extension <{0}>"},



      //***************************************
      // Table icons
      //***************************************
         {"gif_Column" , "images/empty_icon.gif"},
         {"gif_ColumnSel", "images/emptyBlue_icon.gif"},

      };

      return a;
   }
}
