/******************************************************************************
 *
 * [ PSServerRegistrations.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Collection of server registrations which is backed by a Set.
 * Will only allow one server entry for each host:port registration.
 */
public class PSServerRegistrations
{
   public PSServerRegistrations()
   {
      
   }
   
   /**
    * Constructor to allow object to be constructed from its xml
    * representation.
    * @param source the xml source element, cannot be <code>null</code>.
    * @throws PSUnknownNodeTypeException 
    */
   public PSServerRegistrations(Element source) throws PSUnknownNodeTypeException
   {
      fromXml(source);   
   }
   
   /**
    * Add a server registration entry to the Set. Will only allow one
    * server entry for each host:port registration.
    * @param server cannot be <code>null</code>.
    */
   public void add(PSServerRegistration server)
   {
      if(server == null)
         throw new IllegalArgumentException("server cannot be null.");
      m_servers.add(server);
   }
   
   /**
    * Removes the specified server entry from the set.
    * @param server cannot be <code>null</code>.
    */
   public void remove(PSServerRegistration server)
   {
      if(server == null)
         throw new IllegalArgumentException("server cannot be null.");
      m_servers.remove(server);
   }
   
   /**
    * Clears the server list.
    */
   public void clear()
   {
      m_servers.clear();
   }
   
   /**
    * Determines if list is empty.
    * @return <code>true</code> if list is empty.
    */
   public boolean isEmpty()
   {
      return m_servers.isEmpty();
   }
   
   /**
    * Gets all server registration entries as an iterator.
    * @return iterator, never <code>null</code>, may be empty.
    */
   public Iterator<PSServerRegistration> getServers()
   {
      return m_servers.iterator();
   }
   
   /**
    * List of recent connections which should be
    * in order from oldest to newest.
    * @return list of recent conections, may
    * be <code>null</code> or empty.
    */
   public List<String> getRecentConnections()
   {
      return m_recentConns;
   }
   
   /**
    * Set list of recent connections. Should be
    * in order of oldest to newest.
    * @param conns may be <code>null</code> or empty.
    */
   public void setRecentConnections(List<String> conns)
   {
      m_recentConns = conns;
   }
   
   /**
    * Gets the xml representation of this object.
    */   
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      for(PSServerRegistration sr : m_servers)
      {
         root.appendChild(sr.toXml(doc));
      }
      if(m_recentConns != null && !m_recentConns.isEmpty())
      {
         StringBuilder sb = new StringBuilder();
         boolean isFirst = true;
         for(String r : m_recentConns)
         {
            if(!isFirst)
               sb.append(",");
            isFirst = false;
            sb.append(r);               
         }
         root.setAttribute(XML_ATTR_RECENT_CONNS, sb.toString());
      }
      return root;
   }
   
   /**
    * Populates the members of this object from xml representation. Please refer
    * to {@link #toXml(Document) } for the format of the xml.
    * 
    * @param source the source element, may not be <code>null</code>
    * @throws PSUnknownNodeTypeException 
    */
   public void fromXml(Element source) throws PSUnknownNodeTypeException
   {
      NodeList nl = 
         source.getElementsByTagName(PSServerRegistration.XML_NODE_NAME);
      int len = nl.getLength();
      for(int i = 0; i < len; i++)
      {
         Element el = (Element)nl.item(i);
         m_servers.add(new PSServerRegistration(el));
      }
      String recentConns = source.getAttribute(XML_ATTR_RECENT_CONNS);
      if(recentConns != null)
      {
         m_recentConns = new ArrayList<String>();
         String[] recent = recentConns.split(",");
         for(String r : recent)
            m_recentConns.add(r);
      }
      else
      {
         m_recentConns = null;
      }
    }
   
   /**
    * List of servers
    */
   private Set<PSServerRegistration> m_servers = 
      new HashSet<PSServerRegistration>();
   
   /**
    * List of recent server registration connections by
    * host:port string.
    */
   private List<String> m_recentConns;
   
   /**
    * Root xml element for this object.
    */
   public static final String XML_NODE_NAME = "PSXServerRegistrations";
   
   /**
    * Xml attribute for recent connections.
    */
   public static final String XML_ATTR_RECENT_CONNS= "recentConns";
}
