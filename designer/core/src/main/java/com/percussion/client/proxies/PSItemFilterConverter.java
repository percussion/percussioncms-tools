/******************************************************************************
 *
 * [ PSItemFilterConverter.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.proxies;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.webservices.system.PSFilterRule;
import com.percussion.webservices.system.PSFilterRuleParam;
import org.apache.commons.beanutils.BeanUtilsBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Overrides the protected methods from the base class.
 */
public class PSItemFilterConverter extends
   com.percussion.webservices.transformation.converter.PSItemFilterConverter
{
   /**
    * @param beanUtils
    */
   public PSItemFilterConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   @Override
   protected void convertRules(
      com.percussion.webservices.system.PSItemFilter orig,
      PSItemFilter dest)
   {
      // Since this is on client side no loading from server and merging
      PSFilterRule[] rules = orig.getRules();
      Set<IPSItemFilterRuleDef> destRules = new HashSet<IPSItemFilterRuleDef>();
      for (PSFilterRule rule : rules)
      {
         IPSItemFilterRuleDef destRule = null;
         PSFilterRuleParam[] params = rule.getParameters();
         Map<String, String> destParams = new HashMap<String, String>();
         for (PSFilterRuleParam param : params)
            destParams.put(param.getName(), param.getValue());

         destRule = createRuleDef(rule.getName(), destParams);

         destRules.add(destRule);
      }
      dest.setRuleDefs(destRules);
   }

   /**
    * Override to create a dummy item filter with correct id.
    * 
    * @see com.percussion.webservices.transformation.converter.PSItemFilterConverter#loadFilter(long)
    */
   @Override
   protected IPSItemFilter loadFilter(long id)
   {
      PSItemFilter filter = new PSItemFilter("dummy", "dummy"); //$NON-NLS-1$ //$NON-NLS-2$
      filter.setGUID(new PSDesignGuid(PSTypeEnum.ITEM_FILTER, id));
      return filter;
   }

   @Override
   public IPSItemFilterRuleDef createRuleDef(String rule,
      Map<String, String> params)
   {
      if (rule == null)
      {
         throw new IllegalArgumentException("rule may not be null");
      }
      if (params == null)
      {
         throw new IllegalArgumentException("params may not be null");
      }
      IPSItemFilterRuleDef rval = new PSItemFilterRuleDef();
      rval.setGUID(PSGuidHelper.generateNext(PSTypeEnum.ITEM_FILTER_RULE_DEF));
      rval.setRule(rule);
      for (Map.Entry<String, String> entry : params.entrySet())
      {
         rval.setParam(entry.getKey(), entry.getValue());
      }
      return rval;
   }
}
