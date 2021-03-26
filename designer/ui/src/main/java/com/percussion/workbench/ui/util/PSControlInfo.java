/******************************************************************************
 *
 * [ PSControlInfo.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convenience class that holds some information about a
 * control.
 * @author erikserating
 *
 */
public class PSControlInfo
{
   /**
    * 
    * @param control the control, cannot be <code>null</code>
    * @param displayNameKey the display name resource key for the control that will
    * be looked up from the psmessages.properties file, 
    * usually the same key as for the associated label.
    * Cannot be <code>null</code> or empty.
    * @param validators array of <code>IPSControlValueValidator</code> objects,
    * may be <code>null</code>.
    * @param type of control that this is. One of the TYPE_XXX constants.
    * @param page the editor page the control belongs to on a multi-page editor.
    * set to -1 for a non multi-page editor
    * @param hint the property change hint for this control, may be
    * <code>null</code>.
    * @param helpOnly flag indicating that the control is registered only
    * so it will show help hints and will not call the update designer
    * object method
    */
   @SuppressWarnings("unchecked")
   public PSControlInfo(Control control, String displayNameKey, 
      IPSControlValueValidator[] validators, int type, int page,
      Map<String, String> hint, boolean helpOnly)
   {
      if(control == null)
         throw new IllegalArgumentException("control cannot be null.");
      if(displayNameKey == null || displayNameKey.trim().length() == 0)
         throw new IllegalArgumentException("displayNameKey cannot be null or empty.");
     
      m_displayNameKey = displayNameKey.trim();
      if(validators != null && validators.length > 0)
      {
         for(IPSControlValueValidator validator : validators)
            m_validators.add(validator);
      }
      m_control = control;
      m_type = type;
      m_page = page;
      m_propertyChangeHint = hint == null ? null : 
         UnmodifiableMap.decorate(hint);
      m_helpOnly = helpOnly;
   }
   
   /**
    * Convenience ctor calls 
    * PSControlInfo(Control, String, IPSControlValueValidator[],
    *  boolean, int, Map, false)
    */      
   public PSControlInfo(Control control, String displayNameKey, 
      IPSControlValueValidator[] validators, int type, int page,
      Map<String, String> hint)
   {
      this(control, displayNameKey, validators, type, page, hint, false);
   }   
      
   /**
    * Convenience ctor calls
    * PSControlInfo(
    * Control, String, IPSControlValueValidator[], boolean, -1,
    *  Map<String, String>), false.
    */
   public PSControlInfo(Control control, String displayNameKey, 
      IPSControlValueValidator[] validators, int type,
      Map<String, String> hint)
   {
      this(control, displayNameKey, validators, type, -1, hint, false);
   }
   
   /**
    * Convenience ctor calls
    * PSControlInfo(
    * Control, String, IPSControlValueValidator[], boolean, -1,
    *  Map<String, String>), boolean.
    */
   public PSControlInfo(Control control, String displayNameKey, 
      IPSControlValueValidator[] validators, int type,
      Map<String, String> hint, boolean helpOnly)
   {
      this(control, displayNameKey, validators, type, -1, hint, helpOnly);
   }
   
   /**
    * Convenience ctor calls
    * PSControlInfo(Control, String, IPSControlValueValidator[], TYPE_NORMAL,
    * Map<String, String>).
    */
   public PSControlInfo(Control control, String displayNameKey, 
      IPSControlValueValidator[] validators, Map<String, String> hint)
   {
      this(control, displayNameKey, validators, TYPE_NORMAL, hint);
   }
   
   /**
    * Add a validator to the control
    * @param validator the validator to be added, cannot be
    * <code>null</code>.
    */
   public void addValidator(IPSControlValueValidator validator)
   {
      if(validator == null)
         throw new IllegalArgumentException("validator cannot be null.");
      if(!m_validators.contains(validator))
         m_validators.add(validator);
   }
   
   /**
    * Remove a validator from the control
    * @param validator the validator to be removed, cannot be
    * <code>null</code>.
    */
   public void removeValidator(IPSControlValueValidator validator)
   {
      if(validator == null)
         throw new IllegalArgumentException("validator cannot be null.");
      if(m_validators.contains(validator))
         m_validators.remove(validator);
   }
   
   /**
    * The display name for the control, usually the text
    * from the associated label. An attempt is made to find
    * this string using the display name key passed in. If it
    * cannot find the string resource in the psmessages.properties
    * file then the key is returned.
    * @param prefix the name prefix, may be <code>null</code> or
    * empty.
    * @return display name text or key, never <code>null</code> or
    * empty.
    */
   public String getDisplayName(String prefix)
   {
      String name = m_displayNameKey;
      prefix = StringUtils.defaultString(prefix);
      if(PSMessages.stringExists(name))
      {
         name = PSMessages.getString(name, new Object[]{prefix}).replace("&", "");
         if(StringUtils.isBlank(prefix))
            name = StringUtils.capitalize(name.trim());
      }     
      
      // remove the labels trailing colon if it exists
      if(name.endsWith(":"))
         name = name.substring(
            0, name.length() - 1).trim();
      
      return name;
   }
   
   /**
    * The display name for the control, usually the text
    * from the associated label. An attempt is made to find
    * this string using the display name key passed in. If it
    * cannot find the string resource in the psmessages.properties
    * file then the key is returned.
    * @return display name text or key, never <code>null</code> or
    * empty.
    */
   public String getDisplayName()
   {
      return getDisplayName(null);
   }
   
   /**
    * The display name resource key 
    * @return display name text, never <code>null</code> or
    * empty.
    */
   public String getDisplayNameKey()
   {
      return m_displayNameKey;
   }
      
   /**
    * Gets the validators for this control
    * @return an array of <code>IPSControlValueValidator</code> objects,
    * may be <code>null</code>.
    */
   public IPSControlValueValidator[] getValidators()
   {
      if(m_validators.isEmpty())
         return null;
      return m_validators.toArray(new IPSControlValueValidator[]{});
   }
   
   /**
    * Get the control that this info represents
    * @return the <code>Control</code>, never <code>null</code>.
    */
   public Control getControl()
   {
      return m_control;
   }
   
   /**
    * Flag indicating that this control represents the name field
    * of a designer object. Used in the wizard.
    * @return <code>true</code> if this is an objects name.
    */
   public boolean isObjectName()
   {
      return m_type == TYPE_NAME;
   }
   
   /**
    * Flag indicating that this control represents the communities field
    * of a designer object. Used in the wizard.
    * @return <code>true</code> if this is an objects name.
    */
   public boolean isCommunitiesField()
   {
      return m_type == TYPE_COMMUNITY;
   }
   
   /**
    * The page that the control is part of in a multi-page 
    * editor.
    * @return the editor page or -1 if this is not a multi-page
    * editor.
    */
   public int getPage()
   {
      return m_page;
   }
   
   /**
    * Set the page that this control resides on in a multi-page
    * editor.
    * @param page the editor page 
    */
   public void setPage(int page)
   {
      m_page = page;
   }
   
   /**
    * @return the property change hint for the control, 
    * may be <code>null</code>.
    */
   public Map<String, String> getHint()
   {
      return m_propertyChangeHint;
   }
   
   /**
    * @return <code>true</code> if this control should only be registered
    * to diplay help and not to notify of modification events.
    */
   public boolean isHelpOnly()
   {
      return m_helpOnly;
   }
      
   String m_displayNameKey;
   Control m_control;
   List<IPSControlValueValidator> m_validators = 
      new ArrayList<IPSControlValueValidator>();
   int m_type;
   int m_page = -1;
   Map<String, String> m_propertyChangeHint;
   boolean m_helpOnly;
   
   /**
    * Type that indicates theat this is just a normal control
    * and is not considered to be special by the wizard.
    */
   public static final int TYPE_NORMAL = 0;
   
   /**
    * Type that indicates that this control represents the name
    * for the object to be created in the wizard.
    */
   public static final int TYPE_NAME = 1;
   
   /**
    * Type that indicates that this control represents the communities
    * for the object to be created in the wizard.
    */
   public static final int TYPE_COMMUNITY = 2;
   
}   
