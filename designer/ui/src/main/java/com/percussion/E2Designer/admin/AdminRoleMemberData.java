/******************************************************************************
 *
 * [ AdminRoleMemberData.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSAttributeList;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper Class for  role member data, which encapsulates member subject,
 * global attributes and role specific attributes.
 **/
public class AdminRoleMemberData
{
   /**
    * Initializes all variables.
    **/
   public AdminRoleMemberData()
   {
      m_strName = "";
      m_strMemberType = "";
      m_iSecProvType = 0;
      m_strSecProvInst = "";

      m_globalAttributes = new PSAttributeList();
      m_roleSpecAttributes = new HashMap();

      setResources();
   }

   /**
    * Gets name of member.
    *
    * @return  member name never <code>null</code>, may be empty
    **/
   public String getName()
   {
      return m_strName;
   }

   /**
    * Sets name of member.
    *
    * @param name member name, must not be <code>null</code> or empty
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty
    **/
   public void setName(String name)
   {
      if(name == null || name.length()==0)
         throw new IllegalArgumentException(
            "Member name can not be null or empty");
      m_strName = name;
   }

   /**
    * Gets member type (user/group).
    *
    * @return member type never <code>null</code>, may be empty.
    **/
   public String getMemberType()
   {
      return m_strMemberType;
   }

   /**
    * Sets member type.
    *
    * @param type member type, must not be <code>null</code>.
    * @throws IllegalArgumentException if name is <code>null</code> or
    * not one of valid types(User/Group).
    **/
   public void setMemberType(String type)
   {
      if(type == null)
         throw new IllegalArgumentException(
            "Member type can not be null");

      if( !type.equals(user_type) && !type.equals(group_type) )
         throw new IllegalArgumentException(
            "Member type must be a valid type (user/group)");

      m_strMemberType = type;
   }

   /**
    * Gets security provider type of member as id.
    *
    * @return security provider type id
    **/
   public int getSecProvType()
   {
      return m_iSecProvType;
   }

   /**
    * Sets security provider type of member.
    *
    * @param type security provider type id
    **/
   public void setSecProvType(int type)
   {
      m_iSecProvType = type;
   }

   /**
    * Gets security provider instance of member.
    *
    * @return security provider instance never <code>null</code>, may be empty
    **/
   public String getSecProvInst()
   {
      return m_strSecProvInst;
   }

   /**
    * Sets security provider instance of member.
    *
    * @param inst security provider instance, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if instance is <code>null</code>.
    **/
   public void setSecProvInst(String inst)
   {
      if(inst == null)
         throw new IllegalArgumentException(
            "Security Provider Instance can not be null");
      m_strSecProvInst = inst;
   }

   /**
    * Returns <code>true</code> if the member type is user.
    *
    * @return <code>true</code> if the member type is user,
    * otherwise <code>false</code>.
    **/
   public boolean isUser()
   {
      return getMemberType().equals(user_type);
   }

   /**
    * Returns <code>true</code> if the member type is group.
    *
    * @return <code>true</code> if the member type is group.
    **/
   public boolean isGroup()
   {
      return getMemberType().equals(group_type);
   }

   /**
    * Gets global attributes of a member.
    *
    * @return global attributes as PSAttributeList, never <code>null</code>
    **/
   public PSAttributeList getGlobalAttributes()
   {
      return m_globalAttributes;
   }

   /**
    * Sets Global attributes of a member.If passed in attributes is
    * <code>null</code>, creates new <code>PSAttributeList</code> object and sets.
    *
    * @param attrs Attributes as PSAttributeList, May be <code>null</code>
    **/
   public void setGlobalAttributes(PSAttributeList attrs)
   {
      if(attrs != null)
         m_globalAttributes = attrs;
      else
         m_globalAttributes = new PSAttributeList();
   }

   /**
    * Gets attributes of a member for passed in role.
    *
    * @param role must not be <code>null</code>.
    *
    * @return member attributes as PSAttributeList for the specified role,
    * may be <code>null</code> if member doesn't belong to passed in role.
    *
    * @throws  IllegalArgumentException if role is <code>null</code>.
    **/
   public PSAttributeList getAttributes(String role)
   {
      if(role == null)
         throw new IllegalArgumentException(
            "Role to look for member attributes can not be null");
      return (PSAttributeList)m_roleSpecAttributes.get(role);
   }

   /**
    * Sets Role specific attributes of a member. If passed in attributes is
    * <code>null</code>, creates new <code>PSAttributeList</code> object and sets
    *
    * @param attrs Attributes as Map, May be <code>null</code>.
    **/
   public void setRoleSpecAttributes(Map attrs)
   {
      if(attrs != null)
         m_roleSpecAttributes = attrs;
      else
         m_roleSpecAttributes = new HashMap();
   }

   /**
    * Gets attributes of member for all roles the member belongs to.
    *
    * @return  a map of role name and member attributes for that role,
    * never <code>null</code>.
    **/
   public Map getRoleSpecAttributes()
   {
      return m_roleSpecAttributes ;
   }

   /**
    * Shallow copy the passed in object values.
    *
    * @param data object of this class, must not be <code>null</code>.
    *
    * @throws IllegalArgumentException if data is code>null</code>.
    **/
   public void copyFrom(AdminRoleMemberData data)
   {
      if(data == null)
         throw new IllegalArgumentException(
            "data to copy from should not be null");
      m_strName = data.getName();
      m_strMemberType = data.getMemberType();
      m_iSecProvType = data.getSecProvType();
      m_strSecProvInst = data.getSecProvInst();

      m_globalAttributes = data.getGlobalAttributes();
      m_roleSpecAttributes = data.getRoleSpecAttributes();
   }

   /**
    * Sets resource strings for member types if they are <code>null</code>.
    **/
   private void setResources()
   {
      if(user_type != null && group_type != null)
         return;

      user_type = SecurityRolePanel.USER;
      group_type = SecurityRolePanel.GROUP;

      ResourceBundle res = PSServerAdminApplet.getResources();
      if(res != null)
      {
         try {
            user_type =  res.getString("user_type");
            group_type = res.getString("group_type");
         }
         catch(MissingResourceException e)
         {
           //don't need to do anything here, as they have default values.
         }
      }
    }

   /**
    * Member Name, gets initialized in constructor and never <code>null</code>
    * after that.
    **/
   private String m_strName;

   /**
    * Member Type, gets initialized in constructor and never <code>null</code>
    * after that.
    **/
   private String m_strMemberType;

   /** Security Provider Type of member, gets initialized in constructor. **/
   private int m_iSecProvType;

   /**
    * Security Provider Instance of member, gets initialized in constructor
    * and never <code>null</code> after that.
    **/
   private String m_strSecProvInst;

   /**
    * Global Attributes of member, gets initialized in constructor and never
    * <code>null</code> after that.
    **/
   private PSAttributeList m_globalAttributes;

   /**
    * Role Specific Attributes of member, gets initialized in constructor and
    * never <code>null</code> after that.
    **/
   private Map m_roleSpecAttributes;

   /**
    * Constant to indicate user member type. Initially set to <code>null</code>
    * and updated in <code>setResources()</code> and never <code>null</code>
    * after that.
    **/
   private static String user_type = null;

   /**
    * Constant to indicate group member type. Initially set to <code>null</code>
    * and updated in <code>setResources()</code> and never <code>null</code>
    * after that.
    **/
   private static String group_type = null;
}
