/*[ UIRoleMember.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;


import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSRelativeSubject;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSSubject;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * A class that represents a single member (subject)of the role.  Each subject
 * will have the global set of the attributes and it will have the role
 * specific attributes.
 */
public class UIRoleMember
{
   /**
    * Constructs a single subject.
    * @param subject a PSSubject, can not be <code>null</code>
    * @param config a role configuration, can not be <code>null</code>
    * @throws if either a subject or a role config is <code>null</code>
    */
   public UIRoleMember(PSSubject subject, PSRoleConfiguration config )
   {
      if (subject == null || config == null)
         throw new IllegalArgumentException (
            "Either subject or role config is null");

      if (subject instanceof PSRelativeSubject)
      {
         m_memberSubject = (PSRelativeSubject)subject;
         m_memberName = subject.getName();
         m_config = config;

         m_memberAttribs = getMemberAttributes();
         m_roleSpecAttrib = new HashMap();
      }
   }

   /**
    * Gets the string representation for this object, this method has to be
    * overriden since the objects of this class will be used as the user objects
    * when building the tree node.
    * @return a name of this object, never <CODE>null</CODE>.
    */
   public String toString ()
   {
      return getMemberName();
   }

   /**
    * Gets the attributes of this member.
    *
    * @return an attribute list of the subject attributes
    * can be <code>null</code> if no attributes.
    *
    */
   public  PSAttributeList getMemberAttributes ()
   {
      try
      {
         //get subject attributes from the role config
         PSGlobalSubject subject = m_config.getGlobalSubject(
            m_memberSubject,true);
         if (subject != null)
         {
            PSAttributeList map = subject.getAttributes();
            m_memberAttribs = map;
         }
      }
      catch (IllegalArgumentException e)
      {
         //ignored, since m_memberSuject can not be null
      }
      return m_memberAttribs;
   }

   /**
    * Gets the role specific attributes for the member.
    * @return an attribute list of the role member attributes,
    * never <CODE>null</CODE>, but it might be empty.
    */
    public  PSAttributeList getMemberRoleAttributes()
    {
         return m_memberSubject.getAttributes();
    }

   /**
    * Sets the member's attributes.
    * @param attribs an attribute list of the members' attributes can not
    * be <CODE>null</CODE>, can be empty.
    */
    public void setMemberAttributes( PSAttributeList attribs)
    {
         PSGlobalSubject subject = m_config.getGlobalSubject(
            m_memberSubject,true);

         SecurityRolePanel.addRemoveAttributes(subject.getAttributes(),
            attribs);
    }

   /**
    * Gets the type of the member.
    * @return the type of the member which can be a user or a group,
    * never <CODE>null</CODE>.
    */
   public String getType ()
   {
      String type = null;
      if (m_memberSubject.isUser())
         type =  SecurityRolePanel.USER;
      if (m_memberSubject.isGroup())
         type = SecurityRolePanel.GROUP;

      return type;
   }

   /**
    * Gets the subject of this member.
    * @return the member subject, never <code>null</code>.
    */
   public PSSubject getSubject()
   {
      return m_memberSubject;
   }

   /**
    * Gets the name of the member, .
    * @return a member name, never <CODE>null</CODE> .
    */
    public String getMemberName()
    {
      return m_memberName;
    }

   /**
    * Gets the provider instnce for the member.
    * @return the provider instance if any, can not be <CODE>null</CODE>,
    * or an empty string.
    */
   public String getProviderInstance()
   {
      return ms_res.getString("any");
   }

   /**
    * Compare the object passed in to this object.  Comparation is done for
    * the member name, member type, security provider type and the security
    * provider type instance.
    * @param obj an object to be compared to this object.
    * @return <CODE>true</CODE> if the objects are the same,
    * <CODE>false</CODE>.
    */
    public boolean equals (Object obj)
    {
        boolean isEqual = false;
        if (!(obj instanceof UIRoleMember))
           return isEqual;
        else
        {
           UIRoleMember rolMember = (UIRoleMember)obj;
           if (m_memberName.equals(rolMember.getMemberName()) &&
              getType().equals(rolMember.getType()))
           isEqual = true;
        }
        return isEqual;
    }
    
   /**
    * Generates object hash code. 
    */
   @Override
   public int hashCode()
   {
      throw new UnsupportedOperationException("Not Implemented");
   }

   /**
    * Sets the map of the role name and subject role specific attributes
    * @param roleName a name of the role assumed not to be <code>null</code>
    * @param attribs a list of the attribs, assumed not to be <code>null</code>
    * can be empty
    */
   public void setRoleSpecAttributes(String roleName, PSAttributeList attribs)
   {
      m_roleSpecAttrib.put(roleName, attribs);
   }

   /**
    * Gets the subject role specific attribs for the role passed in
    * @return a list of the attributes, can be <CODE>null</CODE>
    */
   public PSAttributeList getRoleSpecAttributes (String roleName)
   {
     PSAttributeList list = (PSAttributeList)m_roleSpecAttrib.get(roleName);
     if (list == null)
         list = new PSAttributeList();
     return list;
   }

   public HashMap getTestAttribs()
   {
      return m_roleSpecAttrib;
   }

   /** A member name, gets initialized in the constructor*/
   private String m_memberName = null;

   /**
    * A role subjet that this member belongs to. Gets initialized in
    * the constructor.
    */
   private PSRelativeSubject m_memberSubject = null;

   /**
    * A collection of the member's specific attributes.
    * Gets initialized in the constructor.
    */
   private PSAttributeList m_memberAttribs;

   /** The role configuration, gets initialized in the constructor. */
   private PSRoleConfiguration m_config;

   /**The map that maps all the roles that this subject belongs to with the
    role specific attributes*/
    private HashMap m_roleSpecAttrib = null;

   /** The server resource strings. **/
   static ResourceBundle ms_res = null;
   static
   {
      try
      {
         ms_res = ResourceBundle.getBundle(
         "com.percussion.E2Designer.admin.PSServerAdminResources",
         Locale.getDefault() );
      }
       catch(MissingResourceException mre)
      {
         mre.printStackTrace();
      }
   }

}
