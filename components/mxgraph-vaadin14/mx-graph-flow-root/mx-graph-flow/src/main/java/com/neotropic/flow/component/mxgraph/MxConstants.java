/*
 * Copyright 2020 Neotropic SAS.
 *
 * Licensed under the Apache License; Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http =//www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing; software
 * distributed under the License is distributed on an "AS IS" BASIS;
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND; either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neotropic.flow.component.mxgraph;

/**
 * Internal Constants for mxGraph library 
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class MxConstants {

        

	/**
	 * Variable = STYLE_PERIMETER
	 * 
	 * Defines the key for the perimeter style. This is a function that defines
	 * the perimeter around a particular shape. Alternatively. Value is "perimeter".
	 */
	public static String STYLE_PERIMETER = "perimeter";
	
	/**
	 * Variable = STYLE_SOURCE_PORT
	 * 
	 * Defines the ID of the cell that should be used for computing the
	 * perimeter point of the source for an edge. This allows for graphically
	 * connecting to a cell while keeping the actual terminal of the edge.
	 * Value is "sourcePort".
	 */
	public static String STYLE_SOURCE_PORT = "sourcePort";
	
	/**
	 * Variable = STYLE_TARGET_PORT
	 * 
	 * Defines the ID of the cell that should be used for computing the
	 * perimeter point of the target for an edge. This allows for graphically
	 * connecting to a cell while keeping the actual terminal of the edge.
	 * Value is "targetPort".
	 */
	public static String STYLE_TARGET_PORT = "targetPort";

	/**
	 * Variable = STYLE_PORT_CONSTRAINT
	 * 
	 * Defines the direction(s) that edges are allowed to connect to cells in.
	 * Possible values are "DIRECTION_NORTH; DIRECTION_SOUTH; 
	 * DIRECTION_EAST" and "DIRECTION_WEST". Value is
	 * "portConstraint".
	 */
	public static String STYLE_PORT_CONSTRAINT = "portConstraint";

	/**
	 * Variable = STYLE_PORT_CONSTRAINT_ROTATION
	 * 
	 * Define whether port constraint directions are rotated with vertex
	 * rotation. 0 (default) causes port constraints to remain absolute; 
	 * relative to the graph; 1 causes the constraints to rotate with
	 * the vertex. Value is "portConstraintRotation".
	 */
	public static String STYLE_PORT_CONSTRAINT_ROTATION = "portConstraintRotation";

	/**
	 * Variable = STYLE_SOURCE_PORT_CONSTRAINT
	 * 
	 * Defines the direction(s) that edges are allowed to connect to sources in.
	 * Possible values are "DIRECTION_NORTH; DIRECTION_SOUTH; DIRECTION_EAST"
	 * and "DIRECTION_WEST". Value is "sourcePortConstraint".
	 */
	public static String STYLE_SOURCE_PORT_CONSTRAINT = "sourcePortConstraint";

	/**
	 * Variable = STYLE_TARGET_PORT_CONSTRAINT
	 * 
	 * Defines the direction(s) that edges are allowed to connect to targets in.
	 * Possible values are "DIRECTION_NORTH; DIRECTION_SOUTH; DIRECTION_EAST"
	 * and "DIRECTION_WEST". Value is "targetPortConstraint".
	 */
	public static String STYLE_TARGET_PORT_CONSTRAINT = "targetPortConstraint";

	/**
	 * Variable = STYLE_OPACITY
	 * 
	 * Defines the key for the opacity style. The type of the value is 
	 * numeric and the possible range is 0-100. Value is "opacity".
	 */
	public static String STYLE_OPACITY = "opacity";

	/**
	 * Variable = STYLE_FILL_OPACITY
	 * 
	 * Defines the key for the fill opacity style. The type of the value is 
	 * numeric and the possible range is 0-100. Value is "fillOpacity".
	 */
	public static String STYLE_FILL_OPACITY = "fillOpacity";

	/**
	 * Variable = STYLE_STROKE_OPACITY
	 * 
	 * Defines the key for the stroke opacity style. The type of the value is 
	 * numeric and the possible range is 0-100. Value is "strokeOpacity".
	 */
	public static String STYLE_STROKE_OPACITY = "strokeOpacity";

	/**
	 * Variable = STYLE_TEXT_OPACITY
	 * 
	 * Defines the key for the text opacity style. The type of the value is 
	 * numeric and the possible range is 0-100. Value is "textOpacity".
	 */
	public static String STYLE_TEXT_OPACITY = "textOpacity";

	/**
	 * Variable = STYLE_TEXT_DIRECTION
	 * 
	 * Defines the key for the text direction style. Possible values are
	 * "TEXT_DIRECTION_DEFAULT; TEXT_DIRECTION_AUTO; TEXT_DIRECTION_LTR"
	 * and "TEXT_DIRECTION_RTL". Value is "textDirection".
	 * The default value for the style is defined in <DEFAULT_TEXT_DIRECTION>.
	 * It is used is no value is defined for this key in a given style. This is
	 * an experimental style that is currently ignored in the backends.
	 */
	public static String STYLE_TEXT_DIRECTION = "textDirection";

	/**
	 * Variable = STYLE_OVERFLOW
	 * 
	 * Defines the key for the overflow style. Possible values are "visible";
	 * "hidden"; "fill" and "width". The default value is "visible". This value
	 * specifies how overlapping vertex labels are handled. A value of
	 * "visible" will show the complete label. A value of "hidden" will clip
	 * the label so that it does not overlap the vertex bounds. A value of
	 * "fill" will use the vertex bounds and a value of "width" will use the
	 * the vertex width for the label. See <mxGraph.isLabelClipped>. Note that
	 * the vertical alignment is ignored for overflow fill and for horizontal
	 * alignment; left should be used to avoid pixel offsets in Internet Explorer
	 * 11 and earlier or if foreignObjects are disabled. Value is "overflow".
	 */
	public static String STYLE_OVERFLOW = "overflow";

	/**
	 * Variable = STYLE_ORTHOGONAL
	 * 
	 * Defines if the connection points on either end of the edge should be
	 * computed so that the edge is vertical or horizontal if possible and
	 * if the point is not at a fixed location. Default is false. This is
	 * used in <mxGraph.isOrthogonal>; which also returns true if the edgeStyle
	 * of the edge is an elbow or entity. Value is "orthogonal".
	 */
	public static String STYLE_ORTHOGONAL = "orthogonal";

	/**
	 * Variable = STYLE_EXIT_X
	 * 
	 * Defines the key for the horizontal relative coordinate connection point
	 * of an edge with its source terminal. Value is "exitX".
	 */
	public static String STYLE_EXIT_X = "exitX";

	/**
	 * Variable = STYLE_EXIT_Y
	 * 
	 * Defines the key for the vertical relative coordinate connection point
	 * of an edge with its source terminal. Value is "exitY".
	 */
	public static String STYLE_EXIT_Y = "exitY";

	
	/**
	* Variable = STYLE_EXIT_DX
	* 
	* Defines the key for the horizontal offset of the connection point
	* of an edge with its source terminal. Value is "exitDx".
	*/
	public static String STYLE_EXIT_DX = "exitDx";

	/**
	* Variable = STYLE_EXIT_DY
	* 
	* Defines the key for the vertical offset of the connection point
	* of an edge with its source terminal. Value is "exitDy".
	*/
	public static String STYLE_EXIT_DY = "exitDy";
	
	/**
	 * Variable = STYLE_EXIT_PERIMETER
	 * 
	 * Defines if the perimeter should be used to find the exact entry point
	 * along the perimeter of the source. Possible values are 0 (false) and
	 * 1 (true). Default is 1 (true). Value is "exitPerimeter".
	 */
	public static String STYLE_EXIT_PERIMETER = "exitPerimeter";

	/**
	 * Variable = STYLE_ENTRY_X
	 * 
	 * Defines the key for the horizontal relative coordinate connection point
	 * of an edge with its target terminal. Value is "entryX".
	 */
	public static String STYLE_ENTRY_X = "entryX";

	/**
	 * Variable = STYLE_ENTRY_Y
	 * 
	 * Defines the key for the vertical relative coordinate connection point
	 * of an edge with its target terminal. Value is "entryY".
	 */
	public static String STYLE_ENTRY_Y = "entryY";

	/**
	 * Variable = STYLE_ENTRY_DX
	 * 
	* Defines the key for the horizontal offset of the connection point
	* of an edge with its target terminal. Value is "entryDx".
	*/
	public static String STYLE_ENTRY_DX = "entryDx";

	/**
	 * Variable = STYLE_ENTRY_DY
	 * 
	* Defines the key for the vertical offset of the connection point
	* of an edge with its target terminal. Value is "entryDy".
	*/
	public static String STYLE_ENTRY_DY = "entryDy";

	/**
	 * Variable = STYLE_ENTRY_PERIMETER
	 * 
	 * Defines if the perimeter should be used to find the exact entry point
	 * along the perimeter of the target. Possible values are 0 (false) and
	 * 1 (true). Default is 1 (true). Value is "entryPerimeter".
	 */
	public static String STYLE_ENTRY_PERIMETER = "entryPerimeter";

	/**
	 * Variable = STYLE_WHITE_SPACE
	 * 
	 * Defines the key for the white-space style. Possible values are "nowrap"
	 * and "wrap". The default value is "nowrap". This value specifies how
	 * white-space inside a HTML vertex label should be handled. A value of
	 * "nowrap" means the text will never wrap to the next line until a
	 * linefeed is encountered. A value of "wrap" means text will wrap when
	 * necessary. This style is only used for HTML labels.
	 * See <mxGraph.isWrapping>. Value is "whiteSpace".
	 */
	public static String STYLE_WHITE_SPACE = "whiteSpace";

	/**
	 * Variable = STYLE_ROTATION
	 * 
	 * Defines the key for the rotation style. The type of the value is 
	 * numeric and the possible range is 0-360. Value is "rotation".
	 */
	public static String STYLE_ROTATION = "rotation";

	/**
	 * Variable = STYLE_FILLCOLOR
	 * 
	 * Defines the key for the fill color. Possible values are all HTML color
	 * names or HEX codes; as well as special keywords such as "swimlane;
	 * "inherit" or "indicated" to use the color code of a related cell or the
	 * indicator shape. Value is "fillColor".
	 */
	public static String STYLE_FILLCOLOR = "fillColor";

	/**
	 * Variable = STYLE_POINTER_EVENTS
	 * 
	 * Specifies if pointer events should be fired on transparent backgrounds.
	 * This style is currently only supported in <mxRectangleShape>. Default
	 * is true. Value is "pointerEvents". This is typically set to
	 * false in groups where the transparent part should allow any underlying
	 * cells to be clickable.
	 */
	public static String STYLE_POINTER_EVENTS = "pointerEvents";

	/**
	 * Variable = STYLE_SWIMLANE_FILLCOLOR
	 * 
	 * Defines the key for the fill color of the swimlane background. Possible
	 * values are all HTML color names or HEX codes. Default is no background.
	 * Value is "swimlaneFillColor".
	 */
	public static String STYLE_SWIMLANE_FILLCOLOR = "swimlaneFillColor";

	/**
	 * Variable = STYLE_MARGIN
	 * 
	 * Defines the key for the margin between the ellipses in the double ellipse shape.
	 * Possible values are all positive numbers. Value is "margin".
	 */
	public static String STYLE_MARGIN = "margin";

	/**
	 * Variable = STYLE_GRADIENTCOLOR
	 * 
	 * Defines the key for the gradient color. Possible values are all HTML color
	 * names or HEX codes; as well as special keywords such as "swimlane;
	 * "inherit" or "indicated" to use the color code of a related cell or the
	 * indicator shape. This is ignored if no fill color is defined. Value is
	 * "gradientColor".
	 */
	public static String STYLE_GRADIENTCOLOR = "gradientColor";

	/**
	 * Variable = STYLE_GRADIENT_DIRECTION
	 * 
	 * Defines the key for the gradient direction. Possible values are
	 * <DIRECTION_EAST>; <DIRECTION_WEST>; <DIRECTION_NORTH> and
	 * <DIRECTION_SOUTH>. Default is <DIRECTION_SOUTH>. Generally; and by
	 * default in mxGraph; gradient painting is done from the value of
	 * <STYLE_FILLCOLOR> to the value of <STYLE_GRADIENTCOLOR>. Taking the
	 * example of <DIRECTION_NORTH>; this means <STYLE_FILLCOLOR> color at the 
	 * bottom of paint pattern and <STYLE_GRADIENTCOLOR> at top; with a
	 * gradient in-between. Value is "gradientDirection".
	 */
	public static String STYLE_GRADIENT_DIRECTION = "gradientDirection";

	/**
	 * Variable = STYLE_STROKECOLOR
	 * 
	 * Defines the key for the strokeColor style. Possible values are all HTML
	 * color names or HEX codes; as well as special keywords such as "swimlane;
	 * "inherit"; "indicated" to use the color code of a related cell or the
	 * indicator shape or "none" for no color. Value is "strokeColor".
	 */
	public static String STYLE_STROKECOLOR = "strokeColor";

	/**
	 * Variable = STYLE_SEPARATORCOLOR
	 * 
	 * Defines the key for the separatorColor style. Possible values are all
	 * HTML color names or HEX codes. This style is only used for
	 * <SHAPE_SWIMLANE> shapes. Value is "separatorColor".
	 */
	public static String STYLE_SEPARATORCOLOR = "separatorColor";

	/**
	 * Variable = STYLE_STROKEWIDTH
	 * 
	 * Defines the key for the strokeWidth style. The type of the value is 
	 * numeric and the possible range is any non-negative value larger or equal
	 * to 1. The value defines the stroke width in pixels. Note = To hide a
	 * stroke use strokeColor none. Value is "strokeWidth".
	 */
	public static String STYLE_STROKEWIDTH = "strokeWidth";

	/**
	 * Variable = STYLE_ALIGN
	 * 
	 * Defines the key for the align style. Possible values are <ALIGN_LEFT>;
	 * <ALIGN_CENTER> and <ALIGN_RIGHT>. This value defines how the lines of
	 * the label are horizontally aligned. <ALIGN_LEFT> mean label text lines
	 * are aligned to left of the label bounds; <ALIGN_RIGHT> to the right of
	 * the label bounds and <ALIGN_CENTER> means the center of the text lines
	 * are aligned in the center of the label bounds. Note this value doesn"t
	 * affect the positioning of the overall label bounds relative to the
	 * vertex; to move the label bounds horizontally; use
	 * <STYLE_LABEL_POSITION>. Value is "align".
	 */
	public static String STYLE_ALIGN = "align";

	/**
	 * Variable = STYLE_VERTICAL_ALIGN
	 * 
	 * Defines the key for the verticalAlign style. Possible values are
	 * <ALIGN_TOP>; <ALIGN_MIDDLE> and <ALIGN_BOTTOM>. This value defines how
	 * the lines of the label are vertically aligned. <ALIGN_TOP> means the
	 * topmost label text line is aligned against the top of the label bounds;
	 * <ALIGN_BOTTOM> means the bottom-most label text line is aligned against
	 * the bottom of the label bounds and <ALIGN_MIDDLE> means there is equal
	 * spacing between the topmost text label line and the top of the label
	 * bounds and the bottom-most text label line and the bottom of the label
	 * bounds. Note this value doesn"t affect the positioning of the overall
	 * label bounds relative to the vertex; to move the label bounds
	 * vertically; use <STYLE_VERTICAL_LABEL_POSITION>. Value is "verticalAlign".
	 */
	public static String STYLE_VERTICAL_ALIGN = "verticalAlign";

	/**
	 * Variable = STYLE_LABEL_WIDTH
	 * 
	 * Defines the key for the width of the label if the label position is not
	 * center. Value is "labelWidth".
	 */
	public static String STYLE_LABEL_WIDTH = "labelWidth";

	/**
	 * Variable = STYLE_LABEL_POSITION
	 * 
	 * Defines the key for the horizontal label position of vertices. Possible
	 * values are <ALIGN_LEFT>; <ALIGN_CENTER> and <ALIGN_RIGHT>. Default is
	 * <ALIGN_CENTER>. The label align defines the position of the label
	 * relative to the cell. <ALIGN_LEFT> means the entire label bounds is
	 * placed completely just to the left of the vertex; <ALIGN_RIGHT> means
	 * adjust to the right and <ALIGN_CENTER> means the label bounds are
	 * vertically aligned with the bounds of the vertex. Note this value
	 * doesn"t affect the positioning of label within the label bounds; to move
	 * the label horizontally within the label bounds; use <STYLE_ALIGN>.
	 * Value is "labelPosition".
	 */
	public static String STYLE_LABEL_POSITION = "labelPosition";

	/**
	 * Variable = STYLE_VERTICAL_LABEL_POSITION
	 * 
	 * Defines the key for the vertical label position of vertices. Possible
	 * values are <ALIGN_TOP>; <ALIGN_BOTTOM> and <ALIGN_MIDDLE>. Default is
	 * <ALIGN_MIDDLE>. The label align defines the position of the label
	 * relative to the cell. <ALIGN_TOP> means the entire label bounds is
	 * placed completely just on the top of the vertex; <ALIGN_BOTTOM> means
	 * adjust on the bottom and <ALIGN_MIDDLE> means the label bounds are
	 * horizontally aligned with the bounds of the vertex. Note this value
	 * doesn"t affect the positioning of label within the label bounds; to move
	 * the label vertically within the label bounds; use
	 * <STYLE_VERTICAL_ALIGN>. Value is "verticalLabelPosition".
	 */
	public static String STYLE_VERTICAL_LABEL_POSITION = "verticalLabelPosition";
	
	/**
	 * Variable = STYLE_IMAGE_ASPECT
	 * 
	 * Defines the key for the image aspect style. Possible values are 0 (do
	 * not preserve aspect) or 1 (keep aspect). This is only used in
	 * <mxImageShape>. Default is 1. Value is "imageAspect".
	 */
	public static String STYLE_IMAGE_ASPECT = "imageAspect";

	/**
	 * Variable = STYLE_IMAGE_ALIGN
	 * 
	 * Defines the key for the align style. Possible values are <ALIGN_LEFT>;
	 * <ALIGN_CENTER> and <ALIGN_RIGHT>. The value defines how any image in the
	 * vertex label is aligned horizontally within the label bounds of a
	 * <SHAPE_LABEL> shape. Value is "imageAlign".
	 */
	public static String STYLE_IMAGE_ALIGN = "imageAlign";

	/**
	 * Variable = STYLE_IMAGE_VERTICAL_ALIGN
	 * 
	 * Defines the key for the verticalAlign style. Possible values are
	 * <ALIGN_TOP>; <ALIGN_MIDDLE> and <ALIGN_BOTTOM>. The value defines how
	 * any image in the vertex label is aligned vertically within the label
	 * bounds of a <SHAPE_LABEL> shape. Value is "imageVerticalAlign".
	 */
	public static String STYLE_IMAGE_VERTICAL_ALIGN = "imageVerticalAlign";

	/**
	 * Variable = STYLE_GLASS
	 * 
	 * Defines the key for the glass style. Possible values are 0 (disabled) and
	 * 1(enabled). The default value is 0. This is used in <mxLabel>. Value is
	 * "glass".
	 */
	public static String STYLE_GLASS = "glass";

	/**
	 * Variable = STYLE_IMAGE
	 * 
	 * Defines the key for the image style. Possible values are any image URL;
	 * the type of the value is String. This is the path to the image that is
	 * to be displayed within the label of a vertex. Data URLs should use the
	 * following format = data =image/png;xyz where xyz is the base64 encoded
	 * data (without the "base64"-prefix). Note that Data URLs are only
	 * supported in modern browsers. Value is "image".
	 */
	public static String STYLE_IMAGE = "image";

	/**
	 * Variable = STYLE_IMAGE_WIDTH
	 * 
	 * Defines the key for the imageWidth style. The type of this value is
	 * int; the value is the image width in pixels and must be greater than 0.
	 * Value is "imageWidth".
	 */
	public static String STYLE_IMAGE_WIDTH = "imageWidth";

	/**
	 * Variable = STYLE_IMAGE_HEIGHT
	 * 
	 * Defines the key for the imageHeight style. The type of this value is
	 * int; the value is the image height in pixels and must be greater than 0.
	 * Value is "imageHeight".
	 */
	public static String STYLE_IMAGE_HEIGHT = "imageHeight";

	/**
	 * Variable = STYLE_IMAGE_BACKGROUND
	 * 
	 * Defines the key for the image background color. This style is only used
	 * in <mxImageShape>. Possible values are all HTML color names or HEX
	 * codes. Value is "imageBackground".
	 */
	public static String STYLE_IMAGE_BACKGROUND = "imageBackground";

	/**
	 * Variable = STYLE_IMAGE_BORDER
	 * 
	 * Defines the key for the image border color. This style is only used in
	 * <mxImageShape>. Possible values are all HTML color names or HEX codes.
	 * Value is "imageBorder".
	 */
	public static String STYLE_IMAGE_BORDER = "imageBorder";

	/**
	 * Variable = STYLE_FLIPH
	 * 
	 * Defines the key for the horizontal image flip. This style is only used
	 * in <mxImageShape>. Possible values are 0 and 1. Default is 0. Value is
	 * "flipH".
	 */
	public static String STYLE_FLIPH = "flipH";

	/**
	 * Variable = STYLE_FLIPV
	 * 
	 * Defines the key for the vertical flip. Possible values are 0 and 1.
	 * Default is 0. Value is "flipV".
	 */
	public static String STYLE_FLIPV = "flipV";

	/**
	 * Variable = STYLE_NOLABEL
	 * 
	 * Defines the key for the noLabel style. If this is true then no label is
	 * visible for a given cell. Possible values are true or false (1 or 0).
	 * Default is false. Value is "noLabel".
	 */
	public static String STYLE_NOLABEL = "noLabel";

	/**
	 * Variable = STYLE_NOEDGESTYLE
	 * 
	 * Defines the key for the noEdgeStyle style. If this is true then no edge
	 * style is applied for a given edge. Possible values are true or false
	 * (1 or 0). Default is false. Value is "noEdgeStyle".
	 */
	public static String STYLE_NOEDGESTYLE = "noEdgeStyle";

	/**
	 * Variable = STYLE_LABEL_BACKGROUNDCOLOR
	 * 
	 * Defines the key for the label background color. Possible values are all
	 * HTML color names or HEX codes. Value is "labelBackgroundColor".
	 */
	public static String STYLE_LABEL_BACKGROUNDCOLOR = "labelBackgroundColor";

	/**
	 * Variable = STYLE_LABEL_BORDERCOLOR
	 * 
	 * Defines the key for the label border color. Possible values are all
	 * HTML color names or HEX codes. Value is "labelBorderColor".
	 */
	public static String STYLE_LABEL_BORDERCOLOR = "labelBorderColor";

	/**
	 * Variable = STYLE_LABEL_PADDING
	 * 
	 * Defines the key for the label padding; ie. the space between the label
	 * border and the label. Value is "labelPadding".
	 */
	public static String STYLE_LABEL_PADDING = "labelPadding";

	/**
	 * Variable = STYLE_INDICATOR_SHAPE
	 * 
	 * Defines the key for the indicator shape used within an <mxLabel>.
	 * Possible values are all SHAPE_* constants or the names of any new
	 * shapes. The indicatorShape has precedence over the indicatorImage.
	 * Value is "indicatorShape".
	 */
	public static String STYLE_INDICATOR_SHAPE = "indicatorShape";

	/**
	 * Variable = STYLE_INDICATOR_IMAGE
	 * 
	 * Defines the key for the indicator image used within an <mxLabel>.
	 * Possible values are all image URLs. The indicatorShape has
	 * precedence over the indicatorImage. Value is "indicatorImage".
	 */
	public static String STYLE_INDICATOR_IMAGE = "indicatorImage";

	/**
	 * Variable = STYLE_INDICATOR_COLOR
	 * 
	 * Defines the key for the indicatorColor style. Possible values are all
	 * HTML color names or HEX codes; as well as the special "swimlane" keyword
	 * to refer to the color of the parent swimlane if one exists. Value is
	 * "indicatorColor".
	 */
	public static String STYLE_INDICATOR_COLOR = "indicatorColor";

	/**
	 * Variable = STYLE_INDICATOR_STROKECOLOR
	 * 
	 * Defines the key for the indicator stroke color in <mxLabel>.
	 * Possible values are all color codes. Value is "indicatorStrokeColor".
	 */
	public static String STYLE_INDICATOR_STROKECOLOR = "indicatorStrokeColor";

	/**
	 * Variable = STYLE_INDICATOR_GRADIENTCOLOR
	 * 
	 * Defines the key for the indicatorGradientColor style. Possible values
	 * are all HTML color names or HEX codes. This style is only supported in
	 * <SHAPE_LABEL> shapes. Value is "indicatorGradientColor".
	 */
	public static String STYLE_INDICATOR_GRADIENTCOLOR = "indicatorGradientColor";

	/**
	 * Variable = STYLE_INDICATOR_SPACING
	 * 
	 * The defines the key for the spacing between the label and the
	 * indicator in <mxLabel>. Possible values are in pixels. Value is
	 * "indicatorSpacing".
	 */
	public static String STYLE_INDICATOR_SPACING = "indicatorSpacing";

	/**
	 * Variable = STYLE_INDICATOR_WIDTH
	 * 
	 * Defines the key for the indicator width. Possible values start at 0 (in
	 * pixels). Value is "indicatorWidth".
	 */
	public static String STYLE_INDICATOR_WIDTH = "indicatorWidth";

	/**
	 * Variable = STYLE_INDICATOR_HEIGHT
	 * 
	 * Defines the key for the indicator height. Possible values start at 0 (in
	 * pixels). Value is "indicatorHeight".
	 */
	public static String STYLE_INDICATOR_HEIGHT = "indicatorHeight";

	/**
	 * Variable = STYLE_INDICATOR_DIRECTION
	 * 
	 * Defines the key for the indicatorDirection style. The direction style is
	 * used to specify the direction of certain shapes (eg. <mxTriangle>).
	 * Possible values are <DIRECTION_EAST> (default); <DIRECTION_WEST>;
	 * <DIRECTION_NORTH> and <DIRECTION_SOUTH>. Value is "indicatorDirection".
	 */
	public static String STYLE_INDICATOR_DIRECTION = "indicatorDirection";

	/**
	 * Variable = STYLE_SHADOW
	 * 
	 * Defines the key for the shadow style. The type of the value is Boolean.
	 * Value is "shadow".
	 */
	public static String STYLE_SHADOW = "shadow";
	
	/**
	 * Variable = STYLE_SEGMENT
	 * 
	 * Defines the key for the segment style. The type of this value is float
	 * and the value represents the size of the horizontal segment of the
	 * entity relation style. Default is ENTITY_SEGMENT. Value is "segment".
	 */
	public static String STYLE_SEGMENT = "segment";
	
	/**
	 * Variable = STYLE_ENDARROW
	 *
	 * Defines the key for the end arrow marker. Possible values are all
	 * constants with an ARROW-prefix. This is only used in <mxConnector>.
	 * Value is "endArrow".
	 *
	 * Example =
	 * (code)
	 * style[mxConstants.STYLE_ENDARROW] = mxConstants.ARROW_CLASSIC;
	 * (end)
	 */
	public static String STYLE_ENDARROW = "endArrow";

	/**
	 * Variable = STYLE_STARTARROW
	 * 
	 * Defines the key for the start arrow marker. Possible values are all
	 * constants with an ARROW-prefix. This is only used in <mxConnector>.
	 * See <STYLE_ENDARROW>. Value is "startArrow".
	 */
	public static String STYLE_STARTARROW = "startArrow";

	/**
	 * Variable = STYLE_ENDSIZE
	 * 
	 * Defines the key for the endSize style. The type of this value is numeric
	 * and the value represents the size of the end marker in pixels. Value is
	 * "endSize".
	 */
	public static String STYLE_ENDSIZE = "endSize";

	/**
	 * Variable = STYLE_STARTSIZE
	 * 
	 * Defines the key for the startSize style. The type of this value is
	 * numeric and the value represents the size of the start marker or the
	 * size of the swimlane title region depending on the shape it is used for.
	 * Value is "startSize".
	 */
	public static String STYLE_STARTSIZE = "startSize";

	/**
	 * Variable = STYLE_SWIMLANE_LINE
	 * 
	 * Defines the key for the swimlaneLine style. This style specifies whether
	 * the line between the title regio of a swimlane should be visible. Use 0
	 * for hidden or 1 (default) for visible. Value is "swimlaneLine".
	 */
	public static String STYLE_SWIMLANE_LINE = "swimlaneLine";

	/**
	 * Variable = STYLE_ENDFILL
	 * 
	 * Defines the key for the endFill style. Use 0 for no fill or 1 (default)
	 * for fill. (This style is only exported via <mxImageExport>.) Value is
	 * "endFill".
	 */
	public static String STYLE_ENDFILL = "endFill";

	/**
	 * Variable = STYLE_STARTFILL
	 * 
	 * Defines the key for the startFill style. Use 0 for no fill or 1 (default)
	 * for fill. (This style is only exported via <mxImageExport>.) Value is
	 * "startFill".
	 */
	public static String STYLE_STARTFILL = "startFill";

	/**
	 * Variable = STYLE_DASHED
	 * 
	 * Defines the key for the dashed style. Use 0 (default) for non-dashed or 1
	 * for dashed. Value is "dashed".
	 */
	public static String STYLE_DASHED = "dashed";

	/**
	 * Defines the key for the dashed pattern style in SVG and image exports.
	 * The type of this value is a space separated list of numbers that specify
	 * a custom-defined dash pattern. Dash styles are defined in terms of the
	 * length of the dash (the drawn part of the stroke) and the length of the
	 * space between the dashes. The lengths are relative to the line width = a
	 * length of "1" is equal to the line width. VML ignores this style and
	 * uses dashStyle instead as defined in the VML specification. This style
	 * is only used in the <mxConnector> shape. Value is "dashPattern".
	 */
	public static String STYLE_DASH_PATTERN = "dashPattern";

	/**
	 * Variable = STYLE_FIX_DASH
	 * 
	 * Defines the key for the fixDash style. Use 0 (default) for dash patterns
	 * that depend on the linewidth and 1 for dash patterns that ignore the
	 * line width. Value is "fixDash".
	 */
	public static String STYLE_FIX_DASH = "fixDash";

	/**
	 * Variable = STYLE_ROUNDED
	 * 
	 * Defines the key for the rounded style. The type of this value is
	 * Boolean. For edges this determines whether or not joins between edges
	 * segments are smoothed to a rounded finish. For vertices that have the
	 * rectangle shape; this determines whether or not the rectangle is
	 * rounded. Use 0 (default) for non-rounded or 1 for rounded. Value is
	 * "rounded".
	 */
	public static String STYLE_ROUNDED = "rounded";

	/**
	 * Variable = STYLE_CURVED
	 * 
	 * Defines the key for the curved style. The type of this value is
	 * Boolean. It is only applicable for connector shapes. Use 0 (default)
	 * for non-curved or 1 for curved. Value is "curved".
	 */
	public static String STYLE_CURVED = "curved";

	/**
	 * Variable = STYLE_ARCSIZE
	 * 
	 * Defines the rounding factor for a rounded rectangle in percent (without
	 * the percent sign). Possible values are between 0 and 100. If this value
	 * is not specified then RECTANGLE_ROUNDING_FACTOR * 100 is used. For
	 * edges; this defines the absolute size of rounded corners in pixels. If
	 * this values is not specified then LINE_ARCSIZE is used.
	 * (This style is only exported via <mxImageExport>.) Value is "arcSize".
	 */
	public static String STYLE_ARCSIZE = "arcSize";

	/**
	 * Variable = STYLE_ABSOLUTE_ARCSIZE
	 * 
	 * Defines the key for the absolute arc size style. This specifies if
	 * arcSize for rectangles is abolute or relative. Possible values are 1
	 * and 0 (default). Value is "absoluteArcSize".
	 */
	public static String STYLE_ABSOLUTE_ARCSIZE = "absoluteArcSize";

	/**
	 * Variable = STYLE_SOURCE_PERIMETER_SPACING
	 * 
	 * Defines the key for the source perimeter spacing. The type of this value
	 * is numeric. This is the distance between the source connection point of
	 * an edge and the perimeter of the source vertex in pixels. This style
	 * only applies to edges. Value is "sourcePerimeterSpacing".
	 */
	public static String STYLE_SOURCE_PERIMETER_SPACING = "sourcePerimeterSpacing";

	/**
	 * Variable = STYLE_TARGET_PERIMETER_SPACING
	 * 
	 * Defines the key for the target perimeter spacing. The type of this value
	 * is numeric. This is the distance between the target connection point of
	 * an edge and the perimeter of the target vertex in pixels. This style
	 * only applies to edges. Value is "targetPerimeterSpacing".
	 */
	public static String STYLE_TARGET_PERIMETER_SPACING = "targetPerimeterSpacing";

	/**
	 * Variable = STYLE_PERIMETER_SPACING
	 * 
	 * Defines the key for the perimeter spacing. This is the distance between
	 * the connection point and the perimeter in pixels. When used in a vertex
	 * style; this applies to all incoming edges to floating ports (edges that
	 * terminate on the perimeter of the vertex). When used in an edge style;
	 * this spacing applies to the source and target separately; if they
	 * terminate in floating ports (on the perimeter of the vertex). Value is
	 * "perimeterSpacing".
	 */
	public static String STYLE_PERIMETER_SPACING = "perimeterSpacing";

	/**
	 * Variable = STYLE_SPACING
	 * 
	 * Defines the key for the spacing. The value represents the spacing; in
	 * pixels; added to each side of a label in a vertex (style applies to
	 * vertices only). Value is "spacing".
	 */
	public static String STYLE_SPACING = "spacing";

	/**
	 * Variable = STYLE_SPACING_TOP
	 * 
	 * Defines the key for the spacingTop style. The value represents the
	 * spacing; in pixels; added to the top side of a label in a vertex (style
	 * applies to vertices only). Value is "spacingTop".
	 */
	public static String STYLE_SPACING_TOP = "spacingTop";

	/**
	 * Variable = STYLE_SPACING_LEFT
	 * 
	 * Defines the key for the spacingLeft style. The value represents the
	 * spacing; in pixels; added to the left side of a label in a vertex (style
	 * applies to vertices only). Value is "spacingLeft".
	 */
	public static String STYLE_SPACING_LEFT = "spacingLeft";

	/**
	 * Variable = STYLE_SPACING_BOTTOM
	 * 
	 * Defines the key for the spacingBottom style The value represents the
	 * spacing; in pixels; added to the bottom side of a label in a vertex
	 * (style applies to vertices only). Value is "spacingBottom".
	 */
	public static String STYLE_SPACING_BOTTOM = "spacingBottom";

	/**
	 * Variable = STYLE_SPACING_RIGHT
	 * 
	 * Defines the key for the spacingRight style The value represents the
	 * spacing; in pixels; added to the right side of a label in a vertex (style
	 * applies to vertices only). Value is "spacingRight".
	 */
	public static String STYLE_SPACING_RIGHT = "spacingRight";

	/**
	 * Variable = STYLE_HORIZONTAL
	 * 
	 * Defines the key for the horizontal style. Possible values are
	 * true or false. This value only applies to vertices. If the <STYLE_SHAPE>
	 * is "SHAPE_SWIMLANE" a value of false indicates that the
	 * swimlane should be drawn vertically; true indicates to draw it
	 * horizontally. If the shape style does not indicate that this vertex is a
	 * swimlane; this value affects only whether the label is drawn
	 * horizontally or vertically. Value is "horizontal".
	 */
	public static String STYLE_HORIZONTAL = "horizontal";

	/**
	 * Variable = STYLE_DIRECTION
	 * 
	 * Defines the key for the direction style. The direction style is used
	 * to specify the direction of certain shapes (eg. <mxTriangle>).
	 * Possible values are <DIRECTION_EAST> (default); <DIRECTION_WEST>;
	 * <DIRECTION_NORTH> and <DIRECTION_SOUTH>. Value is "direction".
	 */
	public static String STYLE_DIRECTION = "direction";

	/**
	 * Variable = STYLE_ANCHOR_POINT_DIRECTION
	 * 
	 * Defines the key for the anchorPointDirection style. The defines if the
	 * direction style should be taken into account when computing the fixed
	 * point location for connected edges. Default is 1 (yes). Set this to 0
	 * to ignore the direction style for fixed connection points. Value is
	 * "anchorPointDirection".
	 */
	public static String STYLE_ANCHOR_POINT_DIRECTION = "anchorPointDirection";

	/**
	 * Variable = STYLE_ELBOW
	 * 
	 * Defines the key for the elbow style. Possible values are
	 * <ELBOW_HORIZONTAL> and <ELBOW_VERTICAL>. Default is <ELBOW_HORIZONTAL>.
	 * This defines how the three segment orthogonal edge style leaves its
	 * terminal vertices. The vertical style leaves the terminal vertices at
	 * the top and bottom sides. Value is "elbow".
	 */
	public static String STYLE_ELBOW = "elbow";

	/**
	 * Variable = STYLE_FONTCOLOR
	 * 
	 * Defines the key for the fontColor style. Possible values are all HTML
	 * color names or HEX codes. Value is "fontColor".
	 */
	public static String STYLE_FONTCOLOR = "fontColor";

	/**
	 * Variable = STYLE_FONTFAMILY
	 * 
	 * Defines the key for the fontFamily style. Possible values are names such
	 * as Arial; Dialog; Verdana; Times New Roman. The value is of type String.
	 * Value is fontFamily.
	 */
	public static String STYLE_FONTFAMILY = "fontFamily";

	/**
	 * Variable = STYLE_FONTSIZE
	 * 
	 * Defines the key for the fontSize style (in px). The type of the value
	 * is int. Value is "fontSize".
	 */
	public static String STYLE_FONTSIZE = "fontSize";

	/**
	 * Variable = STYLE_FONTSTYLE
	 * 
	 * Defines the key for the fontStyle style. Values may be any logical AND
	 * (sum) of <FONT_BOLD>; <FONT_ITALIC> and <FONT_UNDERLINE>.
	 * The type of the value is int. Value is "fontStyle".
	 */
	public static String STYLE_FONTSTYLE = "fontStyle";
	
	/**
	 * Variable = STYLE_ASPECT
	 * 
	 * Defines the key for the aspect style. Possible values are empty or fixed.
	 * If fixed is used then the aspect ratio of the cell will be maintained
	 * when resizing. Default is empty. Value is "aspect".
	 */
	public static String STYLE_ASPECT = "aspect";

	/**
	 * Variable = STYLE_AUTOSIZE
	 * 
	 * Defines the key for the autosize style. This specifies if a cell should be
	 * resized automatically if the value has changed. Possible values are 0 or 1.
	 * Default is 0. See <mxGraph.isAutoSizeCell>. This is normally combined with
	 * <STYLE_RESIZABLE> to disable manual sizing. Value is "autosize".
	 */
	public static String STYLE_AUTOSIZE = "autosize";

	/**
	 * Variable = STYLE_FOLDABLE
	 * 
	 * Defines the key for the foldable style. This specifies if a cell is foldable
	 * using a folding icon. Possible values are 0 or 1. Default is 1. See
	 * <mxGraph.isCellFoldable>. Value is "foldable".
	 */
	public static String STYLE_FOLDABLE = "foldable";

	/**
	 * Variable = STYLE_EDITABLE
	 * 
	 * Defines the key for the editable style. This specifies if the value of
	 * a cell can be edited using the in-place editor. Possible values are 0 or
	 * 1. Default is 1. See <mxGraph.isCellEditable>. Value is "editable".
	 */
	public static String STYLE_EDITABLE = "editable";

	/**
	 * Variable = STYLE_BACKGROUND_OUTLINE
	 * 
	 * Defines the key for the backgroundOutline style. This specifies if a
	 * only the background of a cell should be painted when it is highlighted.
	 * Possible values are 0 or 1. Default is 0. Value is "backgroundOutline".
	 */
	public static String STYLE_BACKGROUND_OUTLINE = "backgroundOutline";

	/**
	 * Variable = STYLE_BENDABLE
	 * 
	 * Defines the key for the bendable style. This specifies if the control
	 * points of an edge can be moved. Possible values are 0 or 1. Default is
	 * 1. See <mxGraph.isCellBendable>. Value is "bendable".
	 */
	public static String STYLE_BENDABLE = "bendable";

	/**Boolean
	 * Variable = STYLE_MOVABLE
	 * 
	 * Defines the key for the movable style. This specifies if a cell can
	 * be moved. Possible values are 0 or 1. Default is 1. See
	 * <mxGraph.isCellMovable>. Value is "movable".
	 */
	public static String STYLE_MOVABLE = "movable";

	/**
	 * Variable = STYLE_RESIZABLE
	 * 
	 * Defines the key for the resizable style. This specifies if a cell can
	 * be resized. Possible values are 0 or 1. Default is 1. See
	 * <mxGraph.isCellResizable>. Value is "resizable".
	 */
	public static String STYLE_RESIZABLE = "resizable";

	/**
	 * Variable = STYLE_RESIZE_WIDTH
	 * 
	 * Defines the key for the resizeWidth style. This specifies if a cell"s
	 * width is resized if the parent is resized. If this is 1 then the width
	 * will be resized even if the cell"s geometry is relative. If this is 0
	 * then the cell"s width will not be resized. Default is not defined. Value
	 * is "resizeWidth".
	 */
	public static String STYLE_RESIZE_WIDTH = "resizeWidth";

	/**
	 * Variable = STYLE_RESIZE_WIDTH
	 * 
	 * Defines the key for the resizeHeight style. This specifies if a cell"s
	 * height if resize if the parent is resized. If this is 1 then the height
	 * will be resized even if the cell"s geometry is relative. If this is 0
	 * then the cell"s height will not be resized. Default is not defined. Value
	 * is "resizeHeight".
	 */
	public static String STYLE_RESIZE_HEIGHT = "resizeHeight";

	/**
	 * Variable = STYLE_ROTATABLE
	 * 
	 * Defines the key for the rotatable style. This specifies if a cell can
	 * be rotated. Possible values are 0 or 1. Default is 1. See
	 * <mxGraph.isCellRotatable>. Value is "rotatable".
	 */
	public static String STYLE_ROTATABLE = "rotatable";

	/**
	 * Variable = STYLE_CLONEABLE
	 * 
	 * Defines the key for the cloneable style. This specifies if a cell can
	 * be cloned. Possible values are 0 or 1. Default is 1. See
	 * <mxGraph.isCellCloneable>. Value is "cloneable".
	 */
	public static String STYLE_CLONEABLE = "cloneable";

	/**
	 * Variable = STYLE_DELETABLE
	 * 
	 * Defines the key for the deletable style. This specifies if a cell can be
	 * deleted. Possible values are 0 or 1. Default is 1. See
	 * <mxGraph.isCellDeletable>. Value is "deletable".
	 */
	public static String STYLE_DELETABLE = "deletable";

	/**
	 * Variable = STYLE_SHAPE
	 * 
	 * Defines the key for the shape. Possible values are all constants with
	 * a SHAPE-prefix or any newly defined shape names. Value is "shape".
	 */
	public static String STYLE_SHAPE = "shape";

	/**
	 * Variable = STYLE_EDGE
	 * 
	 * Defines the key for the edge style. Possible values are the functions
	 * defined in <mxEdgeStyle>. Value is "edgeStyle".
	 */
	public static String STYLE_EDGE = "edgeStyle";

	/**
	 * Variable = STYLE_JETTY_SIZE
	 * 
	 * Defines the key for the jetty size in <mxEdgeStyle.OrthConnector>.
	 * Default is 10. Possible values are all numeric values or "auto".
	 * Jetty size is the minimum length of the orthogonal segment before
	 * it attaches to a shape.
	 * Value is "jettySize".
	 */
	public static String STYLE_JETTY_SIZE = "jettySize";

	/**
	 * Variable = STYLE_SOURCE_JETTY_SIZE
	 * 
	 * Defines the key for the jetty size in <mxEdgeStyle.OrthConnector>.
	 * Default is 10. Possible values are numeric values or "auto". This has
	 * precedence over <STYLE_JETTY_SIZE>. Value is "sourceJettySize".
	 */
	public static String STYLE_SOURCE_JETTY_SIZE = "sourceJettySize";

	/**
	 * Variable = targetJettySize
	 * 
	 * Defines the key for the jetty size in <mxEdgeStyle.OrthConnector>.
	 * Default is 10. Possible values are numeric values or "auto". This has
	 * precedence over <STYLE_JETTY_SIZE>. Value is "targetJettySize".
	 */
	public static String STYLE_TARGET_JETTY_SIZE = "targetJettySize";

	/**
	 * Variable = STYLE_LOOP
	 * 
	 * Defines the key for the loop style. Possible values are the functions
	 * defined in <mxEdgeStyle>. Value is "loopStyle". Default is
	 * <mxGraph.defaultLoopStylean>.
	 */
	public static String STYLE_LOOP = "loopStyle";

	/**
	 * Variable = STYLE_ORTHOGONAL_LOOP
	 * 
	 * Defines the key for the orthogonal loop style. Possible values are 0 and
	 * 1. Default is 0. Value is "orthogonalLoop". Use this style to specify
	 * if loops with no waypoints and defined anchor points should be routed
	 * using <STYLE_LOOP> or not routed.
	 */
	public static String STYLE_ORTHOGONAL_LOOP = "orthogonalLoop";

	/**
	 * Variable = STYLE_ROUTING_CENTER_X
	 * 
	 * Defines the key for the horizontal routing center. Possible values are
	 * between -0.5 and 0.5. This is the relative offset from the center used
	 * for connecting edges. The type of this value is numeric. Value is
	 * "routingCenterX".
	 */
	public static String STYLE_ROUTING_CENTER_X = "routingCenterX";

	/**
	 * Variable = STYLE_ROUTING_CENTER_Y
	 * 
	 * Defines the key for the vertical routing center. Possible values are
	 * between -0.5 and 0.5. This is the relative offset from the center used
	 * for connecting edges. The type of this value is numeric. Value is
	 * "routingCenterY".
	 */
	public static String STYLE_ROUTING_CENTER_Y = "routingCenterY";

	/**
	 * Variable = FONT_BOLD
	 * 
	 * Constant for bold fonts. Default is 1.
	 */
	public static Integer FONT_BOLD = 1;

	/**
	 * Variable = FONT_ITALIC
	 * 
	 * Constant for italic fonts. Default is 2.
	 */
	public static Integer FONT_ITALIC = 2;

	/**
	 * Variable = FONT_UNDERLINE
	 * 
	 * Constant for underlined fonts. Default is 4.
	 */
	public static Integer FONT_UNDERLINE = 4;

	/**
	 * Variable = FONT_STRIKETHROUGH
	 * 
	 * Constant for strikthrough fonts. Default is 8.
	 */
	 public static Integer FONT_STRIKETHROUGH = 8;
	
	/**
	 * Variable = SHAPE_RECTANGLE
	 * 
	 * Name under which <mxRectangleShape> is registered in <mxCellRenderer>.
	 * Default is rectangle.
	 */
	public static String SHAPE_RECTANGLE = "rectangle";

	/**
	 * Variable = SHAPE_ELLIPSE
	 * 
	 * Name under which <mxEllipse> is registered in <mxCellRenderer>.
	 * Default is ellipse.
	 */
	public static String SHAPE_ELLIPSE = "ellipse";

	/**
	 * Variable = SHAPE_DOUBLE_ELLIPSE
	 * 
	 * Name under which <mxDoubleEllipse> is registered in <mxCellRenderer>.
	 * Default is doubleEllipse.
	 */
	public static String SHAPE_DOUBLE_ELLIPSE = "doubleEllipse";

	/**
	 * Variable = SHAPE_RHOMBUS
	 * 
	 * Name under which <mxRhombus> is registered in <mxCellRenderer>.
	 * Default is rhombus.
	 */
	public static String SHAPE_RHOMBUS = "rhombus";

	/**
	 * Variable = SHAPE_LINE
	 * 
	 * Name under which <mxLine> is registered in <mxCellRenderer>.
	 * Default is line.
	 */
	public static String SHAPE_LINE = "line";

	/**
	 * Variable = SHAPE_IMAGE
	 * 
	 * Name under which <mxImageShape> is registered in <mxCellRenderer>.
	 * Default is image.
	 */
	public static String SHAPE_IMAGE = "image";
	
	/**
	 * Variable = SHAPE_ARROW
	 * 
	 * Name under which <mxArrow> is registered in <mxCellRenderer>.
	 * Default is arrow.
	 */
	public static String SHAPE_ARROW = "arrow";
	
	/**
	 * Variable = SHAPE_ARROW_CONNECTOR
	 * 
	 * Name under which <mxArrowConnector> is registered in <mxCellRenderer>.
	 * Default is arrowConnector.
	 */
	public static String SHAPE_ARROW_CONNECTOR = "arrowConnector";
	
	/**
	 * Variable = SHAPE_LABEL
	 * 
	 * Name under which <mxLabel> is registered in <mxCellRenderer>.
	 * Default is label.
	 */
	public static String SHAPE_LABEL = "label";
	
	/**
	 * Variable = SHAPE_CYLINDER
	 * 
	 * Name under which <mxCylinder> is registered in <mxCellRenderer>.
	 * Default is cylinder.
	 */
	public static String SHAPE_CYLINDER = "cylinder";
	
	/**
	 * Variable = SHAPE_SWIMLANE
	 * 
	 * Name under which <mxSwimlane> is registered in <mxCellRenderer>.
	 * Default is swimlane.
	 */
	public static String SHAPE_SWIMLANE = "swimlane";
		
	/**
	 * Variable = SHAPE_CONNECTOR
	 * 
	 * Name under which <mxConnector> is registered in <mxCellRenderer>.
	 * Default is connector.
	 */
	public static String SHAPE_CONNECTOR = "connector";

	/**
	 * Variable = SHAPE_ACTOR
	 * 
	 * Name under which <mxActor> is registered in <mxCellRenderer>.
	 * Default is actor.
	 */
	public static String SHAPE_ACTOR = "actor";
		
	/**
	 * Variable = SHAPE_CLOUD
	 * 
	 * Name under which <mxCloud> is registered in <mxCellRenderer>.
	 * Default is cloud.
	 */
	public static String SHAPE_CLOUD = "cloud";
		
	/**
	 * Variable = SHAPE_TRIANGLE
	 * 
	 * Name under which <mxTriangle> is registered in <mxCellRenderer>.
	 * Default is triangle.
	 */
	public static String SHAPE_TRIANGLE = "triangle";
		
	/**
	 * Variable = SHAPE_HEXAGON
	 * 
	 * Name under which <mxHexagon> is registered in <mxCellRenderer>.
	 * Default is hexagon.
	 */
	public static String SHAPE_HEXAGON = "hexagon";

	/**
	 * Variable = ARROW_CLASSIC
	 * 
	 * Constant for classic arrow markers.
	 */
	public static String ARROW_CLASSIC = "classic";

	/**
	 * Variable = ARROW_CLASSIC_THIN
	 * 
	 * Constant for thin classic arrow markers.
	 */
	public static String ARROW_CLASSIC_THIN = "classicThin";

	/**
	 * Variable = ARROW_BLOCK
	 * 
	 * Constant for block arrow markers.
	 */
	public static String ARROW_BLOCK = "block";

	/**
	 * Variable = ARROW_BLOCK_THIN
	 * 
	 * Constant for thin block arrow markers.
	 */
	public static String ARROW_BLOCK_THIN = "blockThin";

	/**
	 * Variable = ARROW_OPEN
	 * 
	 * Constant for open arrow markers.
	 */
	public static String ARROW_OPEN = "open";

	/**
	 * Variable = ARROW_OPEN_THIN
	 * 
	 * Constant for thin open arrow markers.
	 */
	public static String ARROW_OPEN_THIN = "openThin";

	/**
	 * Variable = ARROW_OVAL
	 * 
	 * Constant for oval arrow markers.
	 */
	public static String ARROW_OVAL = "oval";

	/**
	 * Variable = ARROW_DIAMOND
	 * 
	 * Constant for diamond arrow markers.
	 */
	public static String ARROW_DIAMOND = "diamond";

	/**
	 * Variable = ARROW_DIAMOND_THIN
	 * 
	 * Constant for thin diamond arrow markers.
	 */
	public static String ARROW_DIAMOND_THIN = "diamondThin";

	/**
	 * Variable = ALIGN_LEFT
	 * 
	 * Constant for left horizontal alignment. Default is left.
	 */
	public static String ALIGN_LEFT = "left";

	/**
	 * Variable = ALIGN_CENTER
	 * 
	 * Constant for center horizontal alignment. Default is center.
	 */
	public static String ALIGN_CENTER = "center";

	/**
	 * Variable = ALIGN_RIGHT
	 * 
	 * Constant for right horizontal alignment. Default is right.
	 */
	public static String ALIGN_RIGHT = "right";

	/**
	 * Variable = ALIGN_TOP
	 * 
	 * Constant for top vertical alignment. Default is top.
	 */
	public static String ALIGN_TOP = "top";

	/**
	 * Variable = ALIGN_MIDDLE
	 * 
	 * Constant for middle vertical alignment. Default is middle.
	 */
	public static String ALIGN_MIDDLE = "middle";

	/**
	 * Variable = ALIGN_BOTTOM
	 * 
	 * Constant for bottom vertical alignment. Default is bottom.
	 */
	public static String ALIGN_BOTTOM = "bottom";

	/**
	 * Variable = DIRECTION_NORTH
	 * 
	 * Constant for direction north. Default is north.
	 */
	public static String DIRECTION_NORTH = "north";

	/**
	 * Variable = DIRECTION_SOUTH
	 * 
	 * Constant for direction south. Default is south.
	 */
	public static String DIRECTION_SOUTH = "south";

	/**
	 * Variable = DIRECTION_EAST
	 * 
	 * Constant for direction east. Default is east.
	 */
	public static String DIRECTION_EAST = "east";

	/**
	 * Variable = DIRECTION_WEST
	 * 
	 * Constant for direction west. Default is west.
	 */
	public static String DIRECTION_WEST = "west";

	/**
	 * Variable = TEXT_DIRECTION_DEFAULT
	 * 
	 * Constant for text direction default. Default is an empty string. Use
	 * this value to use the default text direction of the operating system. 
	 */
	public static String TEXT_DIRECTION_DEFAULT = "";

	/**
	 * Variable = TEXT_DIRECTION_AUTO
	 * 
	 * Constant for text direction automatic. Default is auto. Use this value
	 * to find the direction for a given text with <mxText.getAutoDirection>. 
	 */
	public static String TEXT_DIRECTION_AUTO = "auto";

	/**
	 * Variable = TEXT_DIRECTION_LTR
	 * 
	 * Constant for text direction left to right. Default is ltr. Use this
	 * value for left to right text direction.
	 */
	public static String TEXT_DIRECTION_LTR = "ltr";

	/**
	 * Variable = TEXT_DIRECTION_RTL
	 * 
	 * Constant for text direction right to left. Default is rtl. Use this
	 * value for right to left text direction.
	 */
	public static String TEXT_DIRECTION_RTL = "rtl";

	/**
	 * Variable = DIRECTION_MASK_NONE
	 * 
	 * Constant for no direction.
	 */
	public static Integer DIRECTION_MASK_NONE = 0;

	/**
	 * Variable = DIRECTION_MASK_WEST
	 * 
	 * Bitwise mask for west direction.
	 */
	public static Integer DIRECTION_MASK_WEST = 1;
	
	/**
	 * Variable = DIRECTION_MASK_NORTH
	 * 
	 * Bitwise mask for north direction.
	 */
	public static Integer DIRECTION_MASK_NORTH = 2;

	/**
	 * Variable = DIRECTION_MASK_SOUTH
	 * 
	 * Bitwise mask for south direction.
	 */
	public static Integer DIRECTION_MASK_SOUTH = 4;

	/**
	 * Variable = DIRECTION_MASK_EAST
	 * 
	 * Bitwise mask for east direction.
	 */
	public static Integer DIRECTION_MASK_EAST = 8;
	
	/**
	 * Variable = DIRECTION_MASK_ALL
	 * 
	 * Bitwise mask for all directions.
	 */
	public static Integer DIRECTION_MASK_ALL = 15;

	/**
	 * Variable = ELBOW_VERTICAL
	 * 
	 * Constant for elbow vertical. Default is horizontal.
	 */
	public static String ELBOW_VERTICAL = "vertical";

	/**
	 * Variable = ELBOW_HORIZONTAL
	 * 
	 * Constant for elbow horizontal. Default is horizontal.
	 */
	public static String ELBOW_HORIZONTAL = "horizontal";

	/**
	 * Variable = EDGESTYLE_ELBOW
	 * 
	 * Name of the elbow edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_ELBOW = "elbowEdgeStyle";

	/**
	 * Variable = EDGESTYLE_ENTITY_RELATION
	 * 
	 * Name of the entity relation edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_ENTITY_RELATION = "entityRelationEdgeStyle";

	/**
	 * Variable = EDGESTYLE_LOOP
	 * 
	 * Name of the loop edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_LOOP = "loopEdgeStyle";

	/**
	 * Variable = EDGESTYLE_SIDETOSIDE
	 * 
	 * Name of the side to side edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_SIDETOSIDE = "sideToSideEdgeStyle";
        /**
	 * Variable = EDGESTYLE_SIDETOSIDE
	 * 
	 * Name of the side to side edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_ISOMETRIC = "isometricEdgeStyle";

	/**
	 * Variable = EDGESTYLE_TOPTOBOTTOM
	 * 
	 * Name of the top to bottom edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_TOPTOBOTTOM = "topToBottomEdgeStyle";

	/**
	 * Variable = EDGESTYLE_ORTHOGONAL
	 * 
	 * Name of the generic orthogonal edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_ORTHOGONAL = "orthogonalEdgeStyle";
 
	/**
	 * Variable = EDGESTYLE_SEGMENT
	 * 
	 * Name of the generic segment edge style. Can be used as a string value
	 * for the STYLE_EDGE style.
	 */
	public static String EDGESTYLE_SEGMENT = "segmentEdgeStyle";
 
	/**
	 * Variable = PERIMETER_ELLIPSE
	 * 
	 * Name of the ellipse perimeter. Can be used as a string value
	 * for the STYLE_PERIMETER style.
	 */
	public static String PERIMETER_ELLIPSE = "ellipsePerimeter";

	/**
	 * Variable = PERIMETER_RECTANGLE
	 *
	 * Name of the rectangle perimeter. Can be used as a string value
	 * for the STYLE_PERIMETER style.
	 */
	public static String PERIMETER_RECTANGLE = "rectanglePerimeter";

	/**
	 * Variable = PERIMETER_RHOMBUS
	 * 
	 * Name of the rhombus perimeter. Can be used as a string value
	 * for the STYLE_PERIMETER style.
	 */
	public static String PERIMETER_RHOMBUS = "rhombusPerimeter";

	/**
	 * Variable = PERIMETER_HEXAGON
	 * 
	 * Name of the hexagon perimeter. Can be used as a string value 
	 * for the STYLE_PERIMETER style.
	 */
	public static String PERIMETER_HEXAGON = "hexagonPerimeter";

	/**
	 * Variable = PERIMETER_TRIANGLE
	 * 
	 * Name of the triangle perimeter. Can be used as a string value
	 * for the STYLE_PERIMETER style.
	 */
	public static String PERIMETER_TRIANGLE = "trianglePerimeter";
         /* Variable: NONE
	 * 
	 * Defines the value for none. Default is "none".
	 */
	public static String NONE = "none";

}
