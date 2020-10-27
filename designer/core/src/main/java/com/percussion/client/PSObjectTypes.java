/******************************************************************************
 *
 * [ PSObjectTypes.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unique identifiers for all object categories initially known by the workbench
 * core, including primary types and their sub types. If a type has a sub type,
 * then both the primary and one of the sub types are required to uniquely
 * categorize an object.
 * <p>
 * Both types are kept together using the {@link PSObjectType} class.
 * 
 * @author paulhoward
 * @version 6.0
 */
public enum PSObjectTypes implements IPSPrimaryObjectType
{
   /**
    * This primary type is used when dealing with all auto-translations known to
    * the system. There is no object type for individual auto-translations.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * <code>Set&lt;{@link com.percussion.services.content.data.PSAutoTranslation}&gt;</code>.
    */
   AUTO_TRANSLATION_SET,

   /**
    * This primary type is used when dealing with a single content type design
    * object. A content type contains a set of fields and meta data to enforce
    * implementer defined business logic.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.client.objectstore.PSUiItemDefinition}
    */
   CONTENT_TYPE(true),

   /**
    * This primary type is used when dealing with a plain file that is
    * associated with the CMS system. A sub type is required.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.system.data.PSMimeContentAdapter} for all
    * sub types that are files, <code>null</code> for folder sub types.
    */
   RESOURCE_FILE(false, true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return FileSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with a single locale design object.
    * The locale defines the language and country that can be used for items.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.i18n.PSLocale}
    */
   LOCALE(true),

   /**
    * This primary type is used when dealing with a single keyword design
    * object. Keywords are used in many places within the system, including
    * content types.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.content.data.PSKeyword}.
    */
   KEYWORD(true),

   /**
    * This primary type is used when dealing with the collection of all shared
    * fields. Shared fields are linked into content types.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.client.objectstore.PSUiContentEditorSharedDef}.
    */
   SHARED_FIELDS,

   /**
    * This primary type is used when dealing with the collection of content type
    * meta data shared by all content types.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.design.objectstore.PSContentEditorSystemDef}.
    */
   CONTENT_TYPE_SYSTEM_CONFIG,

   /**
    * This primary type is used when dealing with templates that define how to
    * render an item and associated meta data. This includes objects known as
    * variants in pre v6 system. Pre v6 templates (variants) can be identified
    * because they use the legacy assembler. A sub-type is required.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.client.objectstore.IPSUiAssemblyTemplate} for all
    * sub-types.
    */
   TEMPLATE(true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return TemplateSubTypes.values();
      }      
   },

   /**
    * This primary type is used when dealing with workflows. Currently, only
    * names of workflows are supported. Full support will be added at a later
    * time. 
    * <p>
    * No object is returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load}. The names are returned in the
    * catalog.
    */
   WORKFLOW(true),

   /**
    * This primary type is used when dealing with template slots that define
    * where to link items within a template definition and associated meta data.
    * This type includes pre v6 slot objects as well. They are identified by the
    * absence of a slot content finder.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.assembly.IPSTemplateSlot}.
    */
   SLOT(true),

   /**
    * This primary type is used when dealing with communities, which is just a
    * set of roles. The community no longer contains the visibility links that
    * used to be part of a pre v6 community. They are now stored with the
    * object's ACL.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.security.data.PSCommunity}.
    */
   COMMUNITY,

   /**
    * This primary type is used when dealing with roles. Currently, only names
    * of roles are supported. Full support is available through the server
    * admin.
    * <p>
    * No object is returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} (throws
    * UnsupportedOperationException.) The names are returned in the catalog.
    */
   ROLE,

   /**
    * This primary type is used when dealing with menus that are shown in the
    * content explorer. A sub-type is required.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.cms.objectstore.PSAction} for all sub types.
    */
   UI_ACTION_MENU(true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return UiActionMenuSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with views that are shown in the
    * content explorer. A sub-type is required.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is <code>PSView</code>.
    */
   UI_VIEW(true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return SearchSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with searches that are shown in the
    * content explorer. A sub-type is required.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is <code>PSSearch</code>.
    */
   UI_SEARCH(true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return SearchSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with display formats that are shown
    * in the content explorer.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is <{@link com.percussion.cms.objectstore.PSDisplayFormat}.
    */
   UI_DISPLAY_FORMAT(true),

   /**
    * This primary type is used when dealing with a plain file that contains
    * templates used to render content type controls. A sub type is required.
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.system.data.PSMimeContentAdapter} for both
    * sub types.
    */
   CONTENT_EDITOR_CONTROLS(false, true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return ContentEditorControlSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with relationship configurations.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.design.objectstore.PSRelationshipConfig}.
    */
   RELATIONSHIP_TYPE,

   /**
    * This primary type is used when dealing with extension registrations.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.extension.PSExtensionDef}.
    */
   EXTENSION,

   /**
    * This primary type is used when dealing with server configurations that
    * don't have their own editors.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.system.data.PSMimeContentAdapter}
    * regardless of sub type.
    */
   CONFIGURATION_FILE(false, true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return ConfigurationFileSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with database cataloging.
    * <p>
    * Only cataloging is supported.
    */
   DB_TYPE
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return DataBaseObjectSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with old-style xml applications.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.design.objectstore.PSApplication}.
    */
   XML_APPLICATION
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return XmlApplicationSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with files and folders owned by an
    * <code>XML_APPLICATION</code> object.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.design.objectstore.PSApplication} for the
    * <code>FILE</code> sub type and <code>null</code> for the
    * <code>FOLDER</code> sub type.
    */
   XML_APPLICATION_FILE(false, true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return FileSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with server configurations that are
    * expected to be dropped in a future version. They generally don't have
    * their own editor.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.design.objectstore.PSApplication} for all sub types.
    */
   LEGACY_CONFIGURATION(false, true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return LegacyConfigurationSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with file and folder placeholders
    * used by the workbench.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is <code>null</code> for all sub
    * types.
    */
   USER_FILE
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return UserFileSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with files and folders on the local
    * file system.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is <code>Reader</code> for file sub
    * types and <code>null</code> for folder sub types.
    */
   LOCAL_FILE(false, true)
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return FileSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with properties that are shared
    * among multiple clients. These properties are first-class citizens in that
    * they can be locked and have an ACL.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.system.data.PSSharedProperty}.
    */
   SHARED_PROPERTY,

   /**
    * This primary type is used when dealing with ui menu modes and contexts and
    * other items that are needed by the Action menu gui's
    */
   UI_ACTION_MENU_MISC
   {
      // see base class method for details
      @Override
      protected Enum[] getSubTypeValues()
      {
         return UiActionMenuMiscSubTypes.values();
      }
   },

   /**
    * This primary type is used when dealing with item filters used generally
    * for publishing. It has no sub-types.
    * <p>
    * The object type returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load} is
    * {@link com.percussion.services.filter.data.PSItemFilter}.
    */
   ITEM_FILTER,
   
   /**
    * This primary type is used when dealing with sites. Currently, only
    * cataloging of sites are supported. Full support will be added at a later
    * time.
    * <p>
    * No object is returned by
    * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean) IPSCmsModel.load}. The names are returned in the
    * catalog.
    */
   SITE(true);

   /**
    * Default ctor calls {@link #PSObjectTypes(boolean) this(false)}.
    */
   private PSObjectTypes(){
      
   }
   
   /**
    * Default ctor calls {@link #PSObjectTypes(boolean) this(false)}.
    */
   private PSObjectTypes(boolean supportsAcls)
   {
      this(supportsAcls, false);
   }
   
   /**
    * Provided to allow setting of the file type support and acls support
    * property. See {@link #isFileType()} and {@link #supportsAcls()}.
    */
   private PSObjectTypes(boolean supportsAcls, boolean isFileType)
   {
      m_fileType = isFileType;
      m_supportsAcls = supportsAcls;
   }
   
   // see interface for details
   public boolean hasSubTypes()
   {
      return getSubTypeValues().length > 0;
   }

   // see interface for details
   public Set<PSObjectType> getTypes()
   {
      Set<PSObjectType> results = new HashSet<PSObjectType>();
      if (hasSubTypes())
      {
         for (Enum subType : getSubTypeValues())
         {
            results.add(new PSObjectType(this, subType));
         }
      }
      else
         results.add(new PSObjectType(this, null));
      return Collections.unmodifiableSet(results);
   }

   // see interface for details
   public boolean isAllowedType(Enum subType)
   {
      for (Enum existingType : getSubTypeValues())
      {
         if (existingType == subType)
            return true;
      }
      return false;
   }

   // see interface
   public boolean isFileType()
   {
      return m_fileType;
   }
   
   // see interface
   public boolean supportsAcls()
   {
      return m_supportsAcls;
   }
   
   /**
    * Default behavior is to return an empty array. Types with sub-types must
    * override this method and return all the sub type values. All other methods
    * in the interface are implemented based on this method.
    * 
    * @return Never <code>null</code>, default implementation is always
    * empty.
    */
   protected Enum[] getSubTypeValues()
   {
      return new Enum[0];
   }

   /**
    * Defines the sub types for several primary types.
    */
   public enum FileSubTypes
   {
      FILE, 
      FOLDER
   }

   /**
    * Defines the sub types for searches and views.
    */
   public enum SearchSubTypes
   {
      /**
       * The <code>isStandardxxx()</code> method returns <code>true</code>.
       */
      STANDARD, 

      /**
       * The <code>isCustomxxx()</code> method returns <code>true</code>.
       */
      CUSTOM
   }

   /**
    * Defines the sub types for the
    * {@link PSObjectTypes#CONTENT_EDITOR_CONTROLS} primary type.
    */
   public enum ContentEditorControlSubTypes
   {
      /**
       * Control templates that can be modified by the implementer.
       */
      USER,

      /**
       * Control types supplied by the system. Can be used as a basis for new or
       * modified controls.
       */
      SYSTEM
   }

   /**
    * Defines the sub types for the {@link #CONFIGURATION_FILE} primary type.
    */
   public enum ConfigurationFileSubTypes
   {
      /**
       * log4j properties used by JBoss
       */
      LOGGER_PROPERTIES,

      /**
       * navigation.properties
       */
      NAVIGATION_PROPERTIES,

      /**
       * rxW2KserverPageTags.xml
       */
      SERVER_PAGE_TAG_PROPERTIES,

      /**
       * rxW2Ktidy.properties
       */
      TIDY_PROPERTIES,

      /**
       * addThumbnailURL.properties
       */
      ADD_THUMBNAIL_URL_PROPERTIES,

      /**
       * rxworkflow.properties
       */
      WORKFLOW_PROPERTIES,

      /**
       * Indicates a configuration containing the System Velocity macros
       */
      SYSTEM_VELOCITY_MACROS,

      /**
       * Indicates a configuration containing the User Velocity macro overrides
       */
      USER_VELOCITY_MACROS,
      
      /**
       * Files that don't match any of the above types.
       */
      OTHER
   }

   // /**
   // * Defines the sub types for the {@link #DB_TYPE} primary type.
   // */
   // public enum DbTypeSubTypes
   // {
   // DATA_SOURCE,
   // DB_OBJECT_TYPE,
   // DB_OBJECT_INSTANCE
   // }

   /**
    * Defines the sub types for the {@link #LEGACY_CONFIGURATION} primary type.
    */
   public enum LegacyConfigurationSubTypes
   {
      AUTH_TYPES
   }

   /**
    * Defines the sub types for the {@link #USER_FILE} primary type.
    */
   public enum UserFileSubTypes
   {
      WORKBENCH_FOLDER, 
      PLACEHOLDER
   }

   /**
    * Defines the sub types for the {@link #XML_APPLICATION} primary type.
    */
   public enum XmlApplicationSubTypes
   {
      /**
       * Apps whose application type is SYSTEM.
       */
      SYSTEM, 
      
      /**
       * All apps whose type is not one of the above types.
       */
      USER;
   }

   /**
    * Defines the sub types for the {@link #TEMPLATE} primary type.
    */
   public enum TemplateSubTypes
   {
      /**
       * Templates whose OutputFormat is Global.
       */
      GLOBAL,

      /**
       * Templates whose TemplateType is Shared and OutputFormat is Snippet,
       * Page or Binary.
       */
      SHARED,

      /**
       * Templates whose TemplateType is Local and OutputFormat is Snippet, Page
       * or Binary.
       */
      LOCAL,

      /**
       * V5 style templates
       */
      VARIANT,

      /**
       * Templates that don't fall into one of the above categories.
       */
      OTHER
   }

   /**
    * Defines the sub types for the {@link #UI_ACTION_MENU} primary type.
    */
   public enum UiActionMenuSubTypes
   {
      /**
       * Actions of this type are containers for menu entries that are used by
       * the client. They do not actually perform an action. More specifically,
       * 'system' entries have a CLIENT handler.
       * 
       * @see #MENU_ENTRY_SYSTEM
       */
      MENU_CASCADING_SYSTEM,
      
      /**
       * Actions of this type are containers for menu entries that are used by
       * the client. They do not actually perform an action. Menus of this type
       * cannot have children attached by the implementer, the entries are
       * calculated at use time. More specifically, 'system' entries have a
       * CLIENT handler.
       * 
       * @see #MENU_ENTRY_SYSTEM
       */
      MENU_DYNAMIC_SYSTEM,
      
      /**
       * Actions of this type are containers for menu entries that are processed
       * by the server. They do not actually perform an action. The entries are 
       * specified by the implementer.
       */
      MENU_CASCADING_USER,
      
      /**
       * Actions of this type are containers for menu entries that are processed
       * by the server. They do not actually perform an action. Menus of this
       * type cannot have children attached by the implementer, the entries are
       * calculated at use time.
       */
      MENU_DYNAMIC_USER,

      /**
       * Actions of this type perform useful work when activated. Generally,
       * they are associated with an action of sub-type MENU. More specifically,
       * 'system' entries have a CLIENT handler.
       */
      MENU_ENTRY_SYSTEM,

      /**
       * Actions of this type perform useful work when activated. Generally,
       * they are associated with an action of sub-type MENU. More specifically,
       * 'user' entries have a SERVER handler.
       */
      MENU_ENTRY_USER,
   }

   /**
    * Defines the sub types for the {@link #UI_ACTION_MENU_MISC} primary type.
    */
   public enum UiActionMenuMiscSubTypes
   {
      /**
       * The object type returned by
       * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
       * boolean, boolean) IPSCmsModel.load} is <code>PSMenuMode</code>.
       */
      MODES,

      /**
       * The object type returned by
       * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
       * boolean, boolean) IPSCmsModel.load} is <code>PSMenuContext</code>.
       */
      CONTEXTS,

      /**
       * The object type returned by
       * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
       * boolean, boolean) IPSCmsModel.load} 
       * is @code{Collection<PSPair<String, String>>}.
       */
      VISIBILITY_CONTEXTS,

      /**
       * The object type returned by
       * {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
       * boolean, boolean) IPSCmsModel.load} is <code>String</code>.
       */
      CONTEXT_PARAMETERS
   }
   
   /**
    * Defines the sub types for the {@link #DB_TYPE} primary type.
    */
   public enum DataBaseObjectSubTypes
   {
      /**
       * Data sources
       */
      DATASOURCE, 
      
      /**
       * Object Type Category
       */
      CATEGORY,
      
      /**
       * Database user table
       */
      TABLE,

      /**
       * Database user view
       */
      VIEW
   }

   /**
    * See {@link #isFileType()} for details. Defaults to <code>false</code>.
    */
   private boolean m_fileType = false;
   
   /**
    * See {@link #supportsAcls()} for details. Defaults to <code>false</code>.
    */
   private boolean m_supportsAcls = false;
}
