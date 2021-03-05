/*[ IPSLinkExtractor.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.PSItemContext;

/**
 * Interface used by {@link #PSCrawler} responsible for loading 
 * content of a particular type and processing all associated traversable 
 * nodes. There may exist custom built link extractors to perform certain 
 * tasks on nodes based on their content type. For now, identifying which
 * link extractor is used on which node is determined by the 
 * {@link #IPSLinkExtractorFactory}. 
 * 
 * @see PSItemContext
 * @see IPSLinkExtractorFactory
*/
public interface IPSLinkExtractor
{  
   /**
    * Process this node using it as a description 
    * of the content. Once the content has been 
    * 'extracted' attach any neccessary data objects
    * to the {@link com.percussion.loader.PSItemContext}. 
    * 
    * @param node {@link com.percussion.loader.PSItemContext} 
    *    node. Never <code>null</code>.
    *   
    * @return {@link com.percussion.loader.PSItemContext} the 
    *    manipulated node. Never <code>null</code>.
    * 
    */
   public PSItemContext onItemExtract(PSItemContext node);

   /**
    * Public accessor to return the type of context this 
    * extractor will operate on.
    * 
    * @return String content type/mime type string representation
    *    of valid content for this extractor. Never <code>null</code>,
    *    may be empty.
    */
   public String getContentType();
}
