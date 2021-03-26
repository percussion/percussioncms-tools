/******************************************************************************
 *
 * [ IPSUiConstants.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

/**
 * This interface is used to standardize and centralize common constants
 * that will be used in the various Ui components such as layout offsets,
 * component sizes, etc...
 */
public interface IPSUiConstants
{
   // =========================================================================
   // Editor Constants
   // =========================================================================
   
   /**
    * The offset in pixels that the first control in an editor should be
    * vertically spaced from the top of the editor.
    */
   public static final int EDITOR_TOP_OFFSET = 20;
   
   /**
    * The numerator used in the right side <code>FormAttachement</code>
    * for a label that will appear on the left side of an editor. 
    * The denominator used with this numerator must be 100 which
    * is the default denominator.
    */
   public static final int EDITOR_LABEL_NUMERATOR = 30;
   
   /**
    * The numerator used in the right side <code>FormAttachement</code>
    * for a value input control that will appear on the right side of an editor. 
    * The denominator used with this numerator must be 100 which
    * is the default denominator.
    */
   public static final int EDITOR_VALUE_NUMERATOR = 100;
   
   /**
    * Used for controls that only take up around half the space of
    * a full input value control.
    * The numerator used in the right side <code>FormAttachement</code>
    * for a value input control that will appear on the right side of an editor. 
    * The denominator used with this numerator must be 100 which
    * is the default denominator.
    */
   public static final int EDITOR_HALF_VALUE_NUMERATOR = 70;
  
   
   // =========================================================================
   // Wizard Constants
   // =========================================================================
   
   /**
    * The offset in pixels that the first control in a wizard page should be
    * vertically spaced from the top of the page.
    */
   public static final int WIZARD_TOP_OFFSET = 20;
   
   /**
    * The numerator used in the right side <code>FormAttachement</code>
    * for a label that will appear on the left side of a wizard page. 
    * The denominator used with this numerator must be 100 which
    * is the default denominator.
    */   
   public static final int WIZARD_LABEL_NUMERATOR = 25;
   
   /**
    * The numerator used in the right side <code>FormAttachement</code>
    * for a value input control that will appear on the right side of a
    * wizard page. 
    * The denominator used with this numerator must be 100 which
    * is the default denominator.
    */   
   public static final int WIZARD_VALUE_NUMERATOR = 100;
   
   /**
    * Used for controls that only take up around half the space of
    * a full input value control.
    * The numerator used in the right side <code>FormAttachement</code>
    * for a value input control that will appear on the right side of an editor. 
    * The denominator used with this numerator must be 100 which
    * is the default denominator.
    */
   public static final int WIZARD_HALF_VALUE_NUMERATOR = 70;
   
   // =========================================================================
   // Common Constants - Used in both editor and wizards
   // =========================================================================
   
   /**
    * The offset that represents the horizontal spacing between a label and
    * another control.
    */
   public static final int LABEL_HSPACE_OFFSET = 5;
   
   /**
    * The offset that represents the horizontal spacing between a button and
    * another control.
    */
   public static final int BUTTON_HSPACE_OFFSET = 5;
   
   /**
    * The offset that represents the vertical spacing form the bottom of a
    * button control to the top of the next control that appears under the
    * button control.   
    */
   public static final int BUTTON_VSPACE_OFFSET = 2;
   
   /**
    * Default button width in pixels
    */
   public static final int BUTTON_HEIGHT = 25;
   
   /**
    * Default button width in pixels
    */
   public static final int BUTTON_WIDTH = 75;
   
   /**
    * The offset that represents the vertical spacing from the bottom of a
    * combo control to the top of the next control that appears under the
    * combo control.  
    */
   public static final int COMBO_VSPACE_OFFSET = 4;
   
   /**
    * The standard size for the text field used to represent a description
    */
   public static final int DESCRIPTION_FIELD_HEIGHT = 75;
         
   /**
    * The offset that represents the vertical spacing from the bottom of a
    * text control to the top of the next control that appears under the
    * text control.   
    */
   public static final int TEXT_VSPACE_OFFSET = 6;
   
   /**
    * The offset that represents the vertical spacing from the bottom of a
    * Label control to the top of the next Label control that appears under the
    * first Label control. This assumes the label's height is 13 px.  
    */   
   public static final int LABEL_VSPACE_OFFSET = 12;
   
   /**
    * The offset that represents the vertical spacing that needs to be added between
    * a label and the top point it is attached to which will make its text align
    * with the text in a text or combo control that is next to it. The text or combo
    * control should have its top attached to the top of the label with an offset of
    * -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET.
    *  This assumes the label's height is 13 px and the text's height is 19 px or
    *  the combo's height is 21 px.  
    */ 
   public static final int LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET = 3;
   
   /**
    * The offset used for that represents the padding used between the right and left
    * sides of an editor or wizard and the closest control to the side. This
    * number should be negated for an attachment to the right side of the editor or 
    * wizard.
    */
   public static final int COMMON_BORDER_OFFSET = 10;
   
   
   
   // =========================================================================
   // Numerators
   // =========================================================================
   /**
    * Numerator specifying 1/2 of default denominator (100).
    */
   public static final int HALF = 50;

   /**
    * Numerator specifying 1/3 of default denominator (100).
    */
   public static final int ONE_THIRD = 33;

   /**
    * Numerator specifying 1/4 of default denominator (100).
    */
   public static final int ONE_FORTH = 25;

   /**
    * Numerator specifying 3/4 of default denominator (100).
    */
   public static final int THREE_FORTH = 100 - ONE_THIRD;
   
   /**
    * Numerator specifying 1/5 of default denominator (100).
    */
   public static final int ONE_FIFTH = 20;

   /**
    * Numerator specifying 1/5 of default denominator (100).
    */
   public static final int ONE_TENTH = 10;
   
   // =========================================================================
   // IMAGES
   // =========================================================================
   /**
    * Image used to indicate adding something. It is a green plus sign (16 X 16)
    */
   public static final String IMAGE_ADD = "common/add16.gif";
   
   /**
    * Image used to indicate deleting something. It is a red X (16 X 16)
    */
   public static final String IMAGE_DELETE = "common/delete16.gif";
   
   /**
    * Image used to indicate editing something. It shows a pencil (16 X 16).
    */
   public static final String IMAGE_EDIT = "common/edit16.gif";
   
   /**
    * Image used to indicate navigating backward (16 X 16)
    */
   public static final String IMAGE_NAV_BACK = "common/backward_nav16.gif";
   
   /**
    * Image used to indicate navigating forward (16 X 16)
    */
   public static final String IMAGE_NAV_FORWARD = "common/forward_nav16.gif";
   
   /**
    * Triangle pointing in the up direction (16 X 16)
    */
   public static final String IMAGE_TRIANGLE_UP = "common/upTriangle16.gif";
   
   /**
    * Triangle pointing in the down direction (16 X 16)
    */
   public static final String IMAGE_TRIANGLE_DOWN = "common/downTriangle16.gif";
   
   /**
    * Triangle pointing in the right direction (16 X 16)
    */
   public static final String IMAGE_TRIANGLE_RIGHT = "common/rightTriangle16.gif";
   
   /**
    * Triangle pointing in the left direction (16 X 16)
    */
   public static final String IMAGE_TRIANGLE_LEFT = "common/leftTriangle16.gif";
   
   /**
    * Double Triangle pointing in the right direction (16 X 16)
    */
   public static final String IMAGE_TRIANGLE_DOUBLE_RIGHT = 
      "common/doubleTriangleRight16.gif";
   
   /**
    * Double Triangle pointing in the right direction (16 X 16)
    */
   public static final String IMAGE_TRIANGLE_DOUBLE_LEFT = 
      "common/doubleTriangleLeft16.gif";

   /**
    * Image used to indicate adding something. It is a green plus sign (16 X 16)
    */
   public static final String IMAGE_ADD_CHILD = "childTable16.gif";
   
   /**
    * Image used to indicate calendar (16 X 16).
    */
   public static final String IMAGE_CALENDAR = "cal16.gif";

   // =========================================================================
   // PROPERTY CHANGE HINTS
   // =========================================================================
   
   /**
    * A property change hint that indicates that an association
    * change has occurred.
    */
   public static final String CHANGE_HINT_ASSOCIATION = "association";
   
   /**
    * A property change hint that indicates that an type
    * change has occurred.
    */
   public static final String CHANGE_HINT_TYPE = "type";
   
}
