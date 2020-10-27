/******************************************************************************
 *
 * [ PSWebServicesProxyConfig.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This singleton class is the java version of the web services proxy
 * configuration file "WebServicesProxyConfig.xml" that is used by
 * {@link com.percussion.client.proxies.impl.PSCmsModelProxy} class that uses
 * java reflection to mak ethe proxy impl. independent of the objet type. The
 * config file assumed to be part of the JAR file. This assumes the java code
 * and the config file are sync any error in this a runtime error.
 */
public class PSWebServicesProxyConfig
{
   /**
    * Private ctor for the singleton. Loads the config file from the archive
    * extract full configuration.
    */
   private PSWebServicesProxyConfig()
   {
      InputStream is = null;
      try
      {
         is = this.getClass().getResourceAsStream("WebServicesProxyConfig.xml");
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(
            is), false);
         NodeList proxies = doc.getElementsByTagName("Proxy");
         for (int i = 0; i < proxies.getLength(); i++)
         {
            Element proxy = (Element) proxies.item(i);
            Map<String, Operation> operationMap = new HashMap<String, Operation>();
            m_proxies.put(proxy.getAttribute("forType"), operationMap);
            NodeList operations = proxy.getElementsByTagName("Operation");
            for (int j = 0; j < operations.getLength(); j++)
            {
               Element operation = (Element) operations.item(j);
               Operation op = new Operation(operation);
               operationMap.put(op.getName(), op);
            }
         }
      }
      catch (IOException e)
      {
         ms_logger.error(e);
         throw new RuntimeException(e);
      }
      catch (SAXException e)
      {
         ms_logger.error(e);
         throw new RuntimeException(e);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
            }
         }
      }

   }

   /**
    * Accessor to the singleton object.
    * 
    * @return the only object of this class, never <code>null</code>.
    */
   static public PSWebServicesProxyConfig getInstance()
   {
      if (ms_this == null)
      {
         ms_this = new PSWebServicesProxyConfig();
      }
      return ms_this;
   }

   /**
    * Get the operation given the object type string which is the primary type
    * from {@link com.percussion.client.PSObjectTypes} and the method name.
    * 
    * @param objectType
    * @param operationName
    * @return the named operation configured, may be <code>null</code> if not
    * configured one.
    */
   public Operation getOperation(String objectType, String operationName)
   {
      if (objectType == null || objectType.length() == 0)
      {
         throw new IllegalArgumentException(
            "objectType must not be null or empty");
      }
      if (operationName == null || operationName.length() == 0)
      {
         throw new IllegalArgumentException(
            "operationName must not be null or empty");
      }
      objectType = objectType.split(":")[0];
      Map<String, Operation> operations = m_proxies.get(objectType);
      if (operations == null)
         return null;

      return operations.get(operationName);
   }

   /**
    * Inner class represeting a method in the config file.
    * 
    */
   public class Operation extends BaseClass
   {
      private String name = null;

      private String methodName = null;

      private Request request = null;

      private Response response = null;

      public Operation(Element elem)
      {
         super(elem);
         name = elem.getAttribute("name");
         methodName = elem.getAttribute("methodName");
         PSXmlTreeWalker walker = new PSXmlTreeWalker(elem);
         Element requestElem = walker.getNextElement("Request", true);
         Element responseElem = walker.getNextElement("Response", true);
         if (requestElem != null)
            request = new Request(requestElem);
         if (responseElem != null)
            response = new Response(responseElem);

      }

      public String getMethodName()
      {
         return methodName;
      }

      public void setMethodName(String methodName)
      {
         this.methodName = methodName;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         if (name == null || name.length() == 0)
         {
            throw new IllegalArgumentException("name must not be null or empty");
         }
         this.name = name;
      }

      public Request getRequest()
      {
         return request;
      }

      public void setRequest(Request request)
      {
         this.request = request;
      }

      public Response getResponse()
      {
         return response;
      }

      public void setResponse(Response response)
      {
         this.response = response;
      }
   }

   /**
    * Request class that defines a set of set methods.
    */
   public class Request extends BaseClass
   {
      SetMethod[] setMethods;

      Request(Element elem)
      {
         super(elem);
         NodeList nl = elem.getElementsByTagName("SetMethod");
         setMethods = new SetMethod[nl.getLength()];
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element sm = (Element) nl.item(i);
            setMethods[i] = new SetMethod(sm);
         }
      }

      public SetMethod[] getSetMethods()
      {
         return setMethods;
      }

      public void setSetMethods(SetMethod[] setMethods)
      {
         this.setMethods = setMethods;
      }
   }

   public class Locator extends BaseClass
   {
      GetMethod getMethod = null;

      public Locator(Element elem)
      {
         super(elem);
         NodeList nl = elem.getElementsByTagName("GetMethod");
         getMethod = new GetMethod((Element) nl.item(0));
      }

      public GetMethod getGetMethod()
      {
         return getMethod;
      }

      public void setGetMethod(GetMethod getMethod)
      {
         this.getMethod = getMethod;
      }
   }

   /**
    * Set method definition.
    */
   public class SetMethod extends BaseMethod
   {
      public SetMethod(Element elem)
      {
         super(elem);
      }
   }

   /**
    * Get method definition.
    */
   public class GetMethod extends BaseMethod
   {
      Param returns;

      public GetMethod(Element elem)
      {
         super(elem);
         NodeList nl = elem.getElementsByTagName("Return");
         returns = new Param((Element) nl.item(0));
      }

      public Param getReturns()
      {
         return returns;
      }
   }

   /**
    * Base class for methods (get or set)
    */
   private class BaseMethod
   {
      String name;

      Param[] params;

      BaseMethod(Element elem)
      {
         name = elem.getAttribute("name");
         NodeList nl = elem.getElementsByTagName("Param");
         params = new Param[nl.getLength()];
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element param = (Element) nl.item(i);
            params[i] = new Param(param);
         }
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public Param[] getParams()
      {
         return params;
      }

      public void setParams(Param[] params)
      {
         this.params = params;
      }

      public Class[] getParamClasses()
      {
         Class[] classes = new Class[params.length];
         int i = 0;
         for (Param param : params)
         {
            classes[i++] = param.loadClass();
         }
         return classes;
      }
   }

   /**
    * Response class that holds a set of get methods.
    */
   public class Response extends BaseClass
   {
      GetMethod[] getMethods;

      Response(Element elem)
      {
         super(elem);
         NodeList nl = elem.getElementsByTagName("GetMethod");
         getMethods = new GetMethod[nl.getLength()];
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element gm = (Element) nl.item(i);
            getMethods[i] = new GetMethod(gm);
         }
      }

      public GetMethod[] getGetMethods()
      {
         return getMethods;
      }
   }

   /**
    * Parameter class that holds the value.
    */
   public class Param extends BaseClass
   {
      String value = null;

      Param(Element elem)
      {
         super(elem);
         if (elem == null)
            return;
         NodeList nl = elem.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node node = nl.item(i);
            if (node.getNodeType() != Node.TEXT_NODE)
               continue;
            value = ((Text) node).getData().trim();
            break;
         }
      }

      public String getValue()
      {
         return value;
      }

      public void setValue(String value)
      {
         this.value = value;
      }
   }

   /**
    * base class for many of the inner classes. Just holds a class name and has
    * methdos to access and load the class.
    */
   class BaseClass
   {
      String className;

      public BaseClass(Element elem)
      {
         if (elem != null)
            className = elem.getAttribute("class");
      }

      public String getClassName()
      {
         return className;
      }

      public void setClassName(String className)
      {
         this.className = className;
      }

      public Class loadClass()
      {
         try
         {
            return Class.forName(className);
         }
         catch (ClassNotFoundException e)
         {
            return null;
         }
      }
   }

   /**
    * Reference to the singleton object.
    * 
    * @see #getInstance()
    */
   private static PSWebServicesProxyConfig ms_this = null;

   /**
    * Map of all proxies configured in the config file. The key is the object
    * type name the proxy is intended for and the value is the map of all
    * operations defined. The key in this operation map is the name of the
    * operation and the value is the operation itself.
    */
   private Map<String, Map<String, Operation>> m_proxies = new HashMap<String, Map<String, Operation>>();

   /**
    * Logger to log errors.
    */
   private static Logger ms_logger = Logger
      .getLogger(PSWebServicesProxyConfig.class);
}
