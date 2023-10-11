/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.client.models;

import com.percussion.client.PSModelException;
import com.percussion.extension.IPSExtensionDef;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains some constants that may be useful to users of this class.
 *
 * @author paulhoward
 */
public interface IPSExtensionModel extends IPSCmsModel
{
   /**
    * File name extension of template source for given assembler.
    * @param assembler the assembler plugin extension name.
    * Not <code>null</code> or empty.
    * @return the file extension. <code>null</code> if the file name extension
    * is not specified for the assembler or the assembler is not found.
    * @throws PSModelException when information retrieval fails. 
    * @see IPSExtensionDef#INIT_PARAM_ASSEMBLY_FILE_SUFFIX 
    */
   String getAssemblerSourceExt(final String assembler)
         throws PSModelException;
   
   /**
    * Contains an enumeration for each known extension handler. See the 
    * referenced handler implementations for details of the handlers.
    *
    * @author paulhoward
    */
   enum Handlers
   {
      JAVA
      {
         @Override
         public String getName()
         {
            return "Java";
         }
      },
      JAVASCRIPT
      {
         @Override
         public String getName()
         {
            return "JavaScript";
         }
      };

      /**
       * Name of the handler. This handler used for both - as human-readable
       * label and for machine processing.
       * 
       * @return Never <code>null</code> or empty.
       */
      public abstract String getName();
      
      /**
       * List of the handlers names. Never <code>null</code> or empty.
       */
      public static List<String> getNames()
      {
         final List<String> names = new ArrayList<>();
         for (final Handlers handler : values())
         {
            names.add(handler.getName());
         }
         return names;
      }
   }

   /**
    * Contains an enumeration for each known extension interface. Only
    * applicable to Java handlers. See the referenced interface for details of 
    * the extension.
    * 
    * @author paulhoward
    */
   enum Interfaces
   {
      ASSEMBLER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.services.assembly.IPSAssembler";
         }
      },
      
      ASSEMBLY_LOCATION
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSAssemblyLocation";
         }
      },
      
      CONTENT_LIST_GENERATOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.services.publisher.IPSContentListGenerator";
         }
      },

      RELATIONSHIP_EFFECT
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.relationship.IPSEffect";
         }
      },

      ITEM_FILTER_RULE
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.services.filter.IPSItemFilterRule";
         }
      },

      JEXL_EXPRESSION
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSJexlExpression";
         }
      },
      
      PASSWORD_FILTER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.security.IPSPasswordFilter";
         }
      },

      REQUEST_PREPROCESSOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSRequestPreProcessor";
         }
      },

      RESULT_DOC_PROCESSOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSResultDocumentProcessor";
         }
      },

      ITEM_INPUT_TRANSFORMER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSItemInputTransformer";
         }
      },

      ITEM_OUTPUT_TRANSFORMER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSItemOutputTransformer";
         }
      },

      ITEM_VALIDATOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSItemValidator";
         }
      },

      SEARCH_RESULTS_PROCESSOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.search.IPSSearchResultsProcessor";
         }
      },

      SLOT_CONTENT_FINDER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.services.assembly.IPSSlotContentFinder";
         }
      },

      TEMPLATE_EXPANDER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.services.publisher.IPSTemplateExpander";
         }
      },

      UDF_PROCESSOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSUdfProcessor";
         }
      },

      FIELD_INPUT_TRANSFORMER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSFieldInputTransformer";
         }
      },

      FIELD_OUTPUT_TRANSFORMER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSFieldOutputTransformer";
         }
      },

      FIELD_VALIDATOR
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSFieldValidator";
         }
      },

      FIELD_VISIBILITY_RULE
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSFieldVisibilityRule";
         }
      },

      FIELD_EDITABILITY_RULE
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSFieldEditabilityRule";
         }
      },

      WORKFLOW_ACTION
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.extension.IPSWorkflowAction";
         }
      },

      LUCENE_ANALYZER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.search.lucene.analyzer.IPSLuceneAnalyzer";
         }
      },
      
      LUCENE_TEXT_CONVERTER
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.search.lucene.textconverter." +
               "IPSLuceneTextConverter";
         }
      },
      
      SCHEDULED_TASK
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.services.schedule.IPSTask";
         }
      },

      EDITION_TASK
      {
         @Override
         public String getClassName()
         {
            return "com.percussion.rx.publisher.IPSEditionTask";
         }
      };
      
      /**
       * Returns the fully qualified name of the interface that defines this
       * extension.
       * 
       * @return Never <code>null</code> or empty.
       */
      public abstract String getClassName();
      
      /**
       * Class names list. The names are in the same order as actual classes.
       */
      public static List<String> getClassNames()
      {
         final List<String> names = new ArrayList<>();
         for (final Interfaces type : values())
         {
            names.add(type.getClassName());
         }
         return names;
      }
     
      /**
       * Returns interface enum matching the specified interface name.
       * <code>null</code> is returned if no interface with specified name is
       * found.
       */
      public static Interfaces findByClassName(final String interfaceName)
      {
         if (StringUtils.isBlank(interfaceName))
         {
            return null;
         }
         for (final Interfaces i : values())
         {
            if (i.getClassName().equals(interfaceName))
            {
               return i;
            }
         }
         return null;
      }
   }
}
