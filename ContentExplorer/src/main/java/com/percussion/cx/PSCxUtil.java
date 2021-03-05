/******************************************************************************
 *
 * [ PSCxUtil.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

 package com.percussion.cx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.percussion.cx.objectstore.PSNode;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.guitools.PSDialog;
import com.percussion.xml.PSXmlDocumentBuilder;


/**
 * Utility class of static helper method for the Content Explorer
 * applet. Can not be instantiated.
 */
 public final class PSCxUtil
 {
   // Private constructor so that this class can not be
   // instantiated.
   private PSCxUtil(){}


   /**
    * Adjusts popup location to try to keep the popup within the
    * specified bounds.
    * @param popup the popup to be adjusted. Can not be <code>null</code>.
    * @param loc the preferred location points for the popup. Can
    * not be <code>null</code>.
    * @param bounds the dimensional bounds to keep the popup within. Can
    * not be <code>null</code>.
    * @return Point representing the new location
    */
   public static Point adjustPopupLocation(JPopupMenu popup)
   {
      Dimension popupDims = popup.getSize();
      Point loc = popup.getLocationOnScreen();
      
      Rectangle bounds = PSDialog.getScreenBoundsAt(loc);
     
      int x = loc.x;
      int y = loc.y;

      // Do we need to adjust the Y coordinate?
      if((bounds.getMaxY() - (popupDims.getHeight() + loc.getY())) < 0)
      y = (int)Math.max(
         loc.getY() - Math.max(popupDims.getHeight() -
            (bounds.getMaxY() - loc.getY()), 0.0) - 40, 0.0);
      // Do we need to adjust the X coordinate?
      if((bounds.getMaxX() - (popupDims.getWidth() + loc.getX())) < 0)
      x = (int)Math.max(
         loc.getX() - Math.max(popupDims.getWidth() -
            (bounds.getMaxX() - loc.getX()), 0.0), 0.0);
      loc.setLocation(x, y);

      popup.setLocation(loc);

      return loc;

   }
   
   

   /**
    * Will determine if this is a mouse menu gesture, for Windows this
    * would be a right button click and for Mac this would be a control-click.
    * This is only used in a mouse event that captures a button click
    * or release.
    * @param event the <code>MouseEvent</code> object, cannot be
    * <code>null</code>.
    * @return <code>true</code> if this is a menu event.
    */
   public static boolean isMouseMenuGesture(MouseEvent event, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      if(applet.isMacPlatform() && event.isControlDown() ||
         SwingUtilities.isRightMouseButton(event))
      {
         return true;
      }
      return false;
   }


   /**
    * Returns a valid window background color for windows and
    * Mac
    * @return valid window background Color, Never <code>null</code>
    */
   public static Color getWindowBkgColor(PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      return applet.isMacPlatform()
         ? Color.white
         : SystemColor.window;
   }
   
   /**
    * Indicates if the folder should be marked for publish, either because it
    * is itself marked or one of its ancestors is marked.
    * 
    * @param node PSNode representing a folder, may be <code>null</code>.
    * @param parentnode PSNode representing the parent folder, 
    * may be <code>null</code>.
    * @param skipSelfCheck, if <code>true</code> then only check for marked
    * ancestors and ignore if self is marked
    * @return <code>true</code> if the folder should be marked.
    */
   public static boolean shouldFolderBeMarked(
      PSNode node, PSNode parentnode, boolean skipSelfCheck, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      boolean shouldMark = false;
      boolean firstIteration = true;
      if(node == null && parentnode == null)
         return false;
      // If node passed in is null but parentnode exists
      // then we start the check at the parentnode level.
      // (this is the case for a new folder)
      if(node == null && parentnode != null)
      {
         node = parentnode;
         skipSelfCheck = false;
      }
      TreeNode treeNode = node.getAssociatedTreeNode();
      while(treeNode != null)
      {
         if(!(firstIteration && skipSelfCheck))
         {
            PSNode data = 
               (PSNode)((PSNavigationTree.PSTreeNode)treeNode)
                  .getUserObject();
            if (data.isAnyFolderType() 
               && applet.getFlaggedFolderSet().contains(
                     data.getContentId()))
            {   
               shouldMark = true;
               break;
            }
         }  
         treeNode = treeNode.getParent();
         firstIteration = false;
      }
      
      return shouldMark;
   }
   
   /**
    * Gets the icon paths for the supplied item locators.
    * 
    * @param items list of locators must not be <code>null</code>.
    * @return Map of content ids and their icon paths. Never <code>null</code>
    *         may be empty if there is an error getting the icons. The path may
    *         be empty for content ids whose contenttypes do not have icons
    *         associated with them.
    */
   public static Map<String,String> getItemIcons(List<PSLocator> items, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      Map<String, String> itemIconMap = new HashMap<String, String>();
      // create xml document from the given list of items
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "ItemLocators");
      for (PSLocator locator : items)
      {
         root.appendChild(locator.toXml(doc));
      }
      try
      {
         Map<String, String> params = new HashMap<String, String>();
         params.put("ItemLocators", PSXmlDocumentBuilder.toString(doc));
         Document respDoc = applet.getApplet().getXMLDocument(
               "../sys_cxSupport/getItemIcons.xml", params);
         NodeList nl = respDoc.getElementsByTagName("Item");
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            itemIconMap.put(elem.getAttribute("cid"), elem
                  .getAttribute("path"));
         }
      }
      catch (Exception e)
      {
         applet.getApplet().debugMessage(e);
      }
      return itemIconMap;
   }
 }
