package com.percussion.server;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class PSServerPropertiesStatic
{

   /**
    * Constant to specify 'DBMS' logging.
    */
   final static String LOG_DBMS = "DBMS";
   /**
    * Constant to specify 'FILE' logging.
    */
   final static String LOG_FILE = "FILE";
   /**
    * The property to define where the server has to log to.
    */
   final static String LOG_TYPE = "logTo";
   /**
    * The property to define the url for FILE logging.
    */
   final static String LOG_URL = "logUrl";
   /**
    * This property determines how content editor UIs will render groups. See
    * possible values named GROUP_RENDERING_TYPE_VALUE_XXX.
    */
   final static String CONTENT_EDITOR_GROUP_RENDERING_TYPE_PROP_NAME = "contentEditorGroupRenderingType";
   /**
    * One of the values for the
    * {@link #CONTENT_EDITOR_GROUP_RENDERING_TYPE_PROP_NAME} property.
    */
   final static String GROUP_RENDERING_TYPE_VALUE_COLLAPSIBLE = "collapsible";
   /**
    * One of the values for the
    * {@link #CONTENT_EDITOR_GROUP_RENDERING_TYPE_PROP_NAME} property.
    */
   final static String GROUP_RENDERING_TYPE_VALUE_TABS_LEFT = "tabsLeft";
   /**
    * The property to define the request url as case sensitive/insensitive.
    */
   final static String CASE_SENSITIVE_URL = "caseSensitiveUrl";
   /**
    * Constant for the dialog resource file.
    */
   static String DIALOG_RESOURCE_FILE =
      "com.percussion.server.PSServerPropertiesDialogResources";
   /**
    * The path of help set file relative to the rhythmyx root.
    */
   public static final String HELPSET_FILE =
      "Docs/Rhythmyx/Server_Properties_Editor/Server_Properties_Help.hs";
   /**
    * The property to define the objectstore properties file location.
    */
   final static String OBJSTORE_FILE = "objectStoreProperties";
   /**
    * The property to define the response close delay used to avoid closing
    * sockets too soon when returning HTTP responses.
    */
   final static String RESPONSE_CLOSE_DELAY = "responseCloseDelay";
   /**
    * The standard button size for all buttons.
    */
   static final Dimension STANDARD_BUTTON_SIZE = new Dimension(80, 24);


   /**
    * List of recognized server properties. This is used to find out the
    * properties to which we need to create controls automatically to edit their
    * values.
    */
   final static List ms_recProperties = new ArrayList(25);
   /**
    * The dialog resource bundle, initialized in <code>getResources()</code> and
    * never <code>null</code> after that.
    */
   static ResourceBundle ms_res;



   protected static void staticInit(){

         ms_recProperties.add(LOG_TYPE);
         ms_recProperties.add(LOG_URL);
         ms_recProperties.add(OBJSTORE_FILE);
         ms_recProperties.add(CASE_SENSITIVE_URL);
         ms_recProperties.add(RESPONSE_CLOSE_DELAY);
         ms_recProperties.add(PSServer.PROP_ALLOW_XSL_ENCODING_MODS);
         ms_recProperties.add(BROKEN_MANAGED_LINK_BEHAVIOR);
      }
/**
 * gets the app param, if it is null assunme the current directory.
 * @return
 */
   public static File getRxDeployDir(){
      String filePath =  
            System.getProperty("rxdeploydir", System.getProperty("user.dir"));
  
      return new File(filePath);
   
   }

   /**
    * This is a very simple cmd line argument processor. It converts the
    * supplied array of strings into a property list. Params may be specified
    * using - or /. Params have optional values. For every parameter found,
    * an entry is added to the properties file; the key is the param name, the
    * value is the following string, if there is one and it's not a param,
    * otherwise, the empty string is set as the value.
    *
    * @param args An array of 0 or more. If <code>null</code>, returns
    *    immediately.
    *
    * @return A valid object with 0 or more properties set. Never <code>null
    *    </code>.
    */
   static Properties processArgs( String [] args )
   {
      Properties props = new Properties();
      if ( null == args )
         return props;
   
      String param = null;
      for ( int i = 0; i < args.length; i++ )
      {
         String p = args[i];
         if ( p.charAt(0) == '/' || p.charAt(0) == '-' )
         {
            if ( null != param )
            {
               props.setProperty( param, "" );
               param = null;
            }
            param = p.substring(1);
         }
         else if ( null != param )
         {
            props.setProperty( param, args[i] );
            param = null;
         }
         // else skip value w/o param
      }
      // was the last element a param?
      if ( null != param )
         props.setProperty( param, "" );
      return props;
   }

   /**
    * Displays the command-line syntax on the console, and pauses until user
    * hits enter
    */
   static void showCmdLineUsage()
   {
      System.out.println(PSServerPropertiesStatic.getString("cmdUsage"));
      System.out.println("RhythmyxServerPropertiesEditor [-h[elp]]" +  
         " [-advanced]");
      
      BufferedReader conReader = new BufferedReader(
         new InputStreamReader(System.in));
      System.out.println("\n" + PSServerPropertiesStatic.getString("enterToContinue"));
      try
      {
         conReader.readLine();
      }
      catch (IOException e)
      {
         // unlikely, printout and keep going
         e.printStackTrace();
      }      
   }

   /**
    * Gets the resources for this dialog.
    *
    * @return may be <code>null</code> if it can not find the resource file to
    * load.
    */
   static ResourceBundle getResources()
   {
      try {
         if( null == ms_res)
         {
            ms_res = ResourceBundle.getBundle( DIALOG_RESOURCE_FILE,
               Locale.getDefault() );
         }
      }
      catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
      return ms_res;
   }

   /**
    * Gets the resource string of the specified key. If the string is not found
    * returns the key itself. Logs to <code>System.err</code> if resource string
    * is not found for the key.
    *
    * @param key the resource key, assumed not to be <code>null</code> and
    * empty.
    *
    * @return the resource string of the key if found, otherwise key itself,
    * never <code>null</code>.
    */
   static String getString( String key )
   {
      if ( null == key || key.trim().length() == 0 )
         throw new IllegalArgumentException( "key can't be null or empty" );
   
      String resourceValue = key;
      try
      {
         if (getResources() != null)
            resourceValue = getResources().getString( key );
      } catch (MissingResourceException e)
      {
         // not fatal; warn and continue
         System.err.println( PSServerPropertiesDialog.class );
         System.err.println( e );
      }
      return resourceValue;
   }
   

}
