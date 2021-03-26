/*[ AdminPropertyCataloger.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.ApplicationRequestor;
import com.percussion.E2Designer.Util;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to implement property cataloging using 'sys_rxlookup' resource of
 * hidden application 'sys_adminCataloger'.
 **/
public class AdminPropertyCataloger
{
   /**
    * Catalogs properties of given lookup type. Returns the map of category and
    * name of 'RXADMINLOOKUP' table. The names with category as
    * 'SYS_ATTRIBUTENAME' should be considered as property names. All others
    * become the values of category which is a property name.
    *
    * @param lookupType the property lookup type for cataloging, may not be
    * <code>null</code>
    * @param serverRoot the request root of the server, may not be
    * <code>null</code>
    * @param showErrMsg flag to show error message when exception happens. If
    * <code>true</code>, error message dialog will pop up, otherwise just logs
    * to system output stream.
    *
    * @return map of property name and values, never <code>null</code> may be
    * empty.
    *
    * @throws IllegalArgumentException if lookup type or server root is
    * <code>null</code> or empty.
    **/
   public static Map catalogProperties(String lookupType, String serverRoot,
      boolean showErrMsg)
   {
      if(lookupType == null || lookupType.length() == 0)
         throw new IllegalArgumentException(
            "The lookup type for property cataloging can not be null or empty");

      if(serverRoot == null || serverRoot.length() == 0)
         throw new IllegalArgumentException(
            "The server root can not be null or empty. " +
            "Required for application request");

      InputStream in = null;
      Map propertyNames = new HashMap();

      String error = "";

      try 
      {
         Map params = new HashMap();
         params.put(ms_propertyCatalogKey, lookupType);
      
         ApplicationRequestor appRes = new ApplicationRequestor(
            AppletMainDialog.getServerConnection().getConnection(),serverRoot);
         Document xmlDoc = appRes.makeRequest(ms_propertyCatalogApp, 
               ms_propertyCatalogRes, params);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(xmlDoc);

         Element propertyElement;

         while ((propertyElement = tree.getNextElement("Property", true, true))
            != null)
         {
            String name = tree.getElementData(propertyElement).trim();
            String category = propertyElement.getAttribute("Category").trim();
            String limitToList = 
                   propertyElement.getAttribute("LimitToList");
            String catalogUrl = 
                   propertyElement.getAttribute("CatalogUrl");
            if(category != null && name != null)
            {
               ArrayList properties = (ArrayList)propertyNames.get(category);         
               if( properties != null)
               {
                   properties.add(new AdminPropertyValues(
                      catalogUrl,name,limitToList));
               }
               else 
               {
                    properties = new ArrayList();
                    properties.add(new AdminPropertyValues(
                      catalogUrl,name,limitToList));
                    propertyNames.put(category, properties);
               }
            }
         }

      }
      catch(MalformedURLException mue)
      {
         //should not happen
      }
      catch(IOException e)
      {
         String[] appName = new String[1];
         appName[0] = ms_propertyCatalogApp;

         error = MessageFormat.format(
            PSServerAdminApplet.getResources().getString("catalogAppError"),
            appName);
      }
      catch(Exception e)
      {
         error = e.getLocalizedMessage();
      }

      if(error.length() > 0)
      {
         error = PSServerAdminApplet.getResources().getString(
            "catalogPropertyError") + "\n" + Util.cropErrorMessage(error);
         if(showErrMsg)
         {
            String title = PSServerAdminApplet.getResources().getString(
               "error");
            JOptionPane.showMessageDialog(null, error,
               title, JOptionPane.ERROR_MESSAGE);
         }
         else
            System.out.println(error);
      }

      return  propertyNames;
   }
   
   /**
    * Catalogs values for the supplied type, objectname and attirbutename 
    * from the specified url. If the url is not correctly formed, assumes the 
    * url is in the format 'application/resource.xml?parameter=value' and tries
    * to make the request for the specified application and resource with 
    * supplied parameters.
    * @param type type of the catalog request, can not be <code>null</code> or
    * empty. 
    * @param objectname the name of the object that needs cataloging, may be 
    * <code>null</code> if it's new role/subject. 
    * @param catalogUrl url of the cataloging application, may not be 
    * <code>null</code>.  
    * @param attributename attibutename of the catalog request, can not be 
    * <code>null</code> 
    * @param serverRoot the request root of the server, may not be
    * <code>null</code>
    * @return List of values from the catalog request, may not be null, can be 
    * empty. 
    */
    public static List catalogNameValue(String type, String objectName,
      String catalogUrl, String attributeName, String serverRoot)
   {
      if(type == null || type.length() == 0)
         throw new IllegalArgumentException(
            "The lookup type for property cataloging can not be null or empty");
      
      if(attributeName == null || attributeName.length() == 0)
         throw new IllegalArgumentException(
            "The attribute name for property cataloging can not be" + 
            "null or empty");
            
      if(serverRoot == null || serverRoot.length() == 0)
         throw new IllegalArgumentException(
            "The server root can not be null or empty." +
            "Required for application request");
            
      if(catalogUrl == null || catalogUrl.length() == 0)
         throw new IllegalArgumentException(
            "The catalog url can not be null or empty.");
      
      String appName =null;
      String resourceName = null;
      String error = "";
      Map params = new HashMap();
      List values = new ArrayList();
      Document xmlDoc=null;
      
      //add parameter based on the value given. 
      params.put(TYPE,type);
      params.put(OBJECTNAME,objectName);
      params.put(ATTRIBUTE,attributeName);
      
      ApplicationRequestor appRes = new ApplicationRequestor(
            AppletMainDialog.getServerConnection().getConnection(),serverRoot);
      try 
      {
         URL url = new URL(catalogUrl);
         xmlDoc = appRes.makeRequest(catalogUrl,params);
      }
      catch(MalformedURLException e)
      {
         //Proccess url to get the application name, resource name and 
         //parameters
         int start = 0;
         int end = catalogUrl.indexOf("/");
         if(end == start)
         {
            //first character of the url is "/"
            start++;
            end = catalogUrl.indexOf("/",start);
         } 
         appName = catalogUrl.substring(start,end);
         start = end + 1;
         end = catalogUrl.indexOf("?",start);
         if(end == -1)
            resourceName = catalogUrl.substring(start);
         else
         {
            resourceName = catalogUrl.substring(start,end);
            do
            {
               start = end + 1;
               end = catalogUrl.indexOf("=",start);
               String paramName = catalogUrl.substring(start,end);
               start= end + 1;
               end = catalogUrl.indexOf("&",start);
               String paramValue;
               if(end != -1)
                  paramValue = catalogUrl.substring(start,end);
               else 
                  paramValue = catalogUrl.substring(start);
               params.put(paramName,paramValue);
            }while(catalogUrl.indexOf("=",start) != -1);
         }    
      }
      catch(IOException e)
      {
         String [] apps = new String[1];;
         apps[0] = catalogUrl;
         error = MessageFormat.format(
            PSServerAdminApplet.getResources().getString("catalogAppError"),
               apps); 
      }
      
      try
      {
         if(xmlDoc == null && appName != null)   
            xmlDoc = appRes.makeRequest(appName,resourceName,params);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(xmlDoc);
         Element propertyElement;
         // Only get the Display text not the value. 
         while ((propertyElement = tree.getNextElement("PSXDisplayText"))
            != null)
         {
            String name = tree.getElementData(propertyElement).trim();
            values.add(name);
         }
      }
      catch( Exception e)
      {
         //ignore second error 
         if(error.length() == 0)
         error = e.getLocalizedMessage();
      }
      
      if(error.length() > 0)
      {
         error = PSServerAdminApplet.getResources().getString(
            "catalogPropertyError") + "\n" + Util.cropErrorMessage(error);
         String title = PSServerAdminApplet.getResources().getString(
            "error");
         JOptionPane.showMessageDialog(null, error,
            title, JOptionPane.ERROR_MESSAGE);
      }
      
      return values;
      
   }
   

   /**
    * The name of property cataloging application.
    **/
   private final static String ms_propertyCatalogApp = "sys_adminCataloger";

   /**
    * The name of resource for property cataloging.
    **/
   private final static String ms_propertyCatalogRes = "sys_rxlookup.xml";

   /**
    * The key which is lookup type for cataloging properties. The allowed
    * lookup types are 'role' and 'subject'.
    **/
   private final static String ms_propertyCatalogKey = "sys_key";

   /**
    * The type parameter name that needs to be appended to the url that needs
    * to be cataloged for values of a property 
    */
   public final static String TYPE = "type";
   
   /**
    * The object parameter name that needs to be appended to the url that needs
    * to be cataloged for values of a property 
    */
   public final static String OBJECTNAME = "objectname";
   /**
    * The attribute  parameter name that needs to be appended to the url 
    * that needs to be cataloged for values of a property
    */
   public final static String ATTRIBUTE = "attributename";
   
}
