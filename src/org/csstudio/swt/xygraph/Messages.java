package org.csstudio.swt.xygraph;

import org.eclipse.osgi.util.NLS;

/** Externalized Strings
 *  @author Kay Kasemir
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.swt.xygraph.messages"; //$NON-NLS-1$
    public static String Annotation_Add;
    public static String Annotation_ChangeFont;
    public static String Annotation_Color;
    public static String Annotation_ColorFromYAxis;
    public static String Annotation_Cursor;
    public static String Annotation_DefaultNameFmt;
    public static String Annotation_Font;
    public static String Annotation_Name;
    public static String Annotation_NameTT;
    public static String Annotation_ShowInfo;
    public static String Annotation_ShowName;
    public static String Annotation_ShowPosition;
    public static String Annotation_Snap;
    public static String Annotation_SnapTT;
    public static String Annotation_SystemDefault;
    public static String Annotation_Trace;
    public static String Annotation_TraceSnapTT;
    public static String Annotation_XAxis;
    public static String Annotation_XAxisSnapTT;
    public static String Annotation_YAxis;
    public static String Annotation_YAxisSnapTT;
    public static String Zoom_Horiz;
    public static String Zoom_In;
    public static String Zoom_InHoriz;
    public static String Zoom_InVert;
    public static String Zoom_None;
    public static String Zoom_Out;
    public static String Zoom_OutHoriz;
    public static String Zoom_OutVert;
    public static String Zoom_Pan;
    public static String Zoom_Rubberband;
    public static String Zoom_Vert;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
        // Prevent instantiation
    }
}
