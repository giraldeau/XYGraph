package org.csstudio.swt.xygraph.toolbar;

import org.csstudio.swt.xygraph.Activator;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.figures.XYGraphFlags;
import org.csstudio.swt.xygraph.undo.AddAnnotationCommand;
import org.csstudio.swt.xygraph.undo.IOperationsManagerListener;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.csstudio.swt.xygraph.undo.RemoveAnnotationCommand;
import org.csstudio.swt.xygraph.undo.ZoomType;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonGroup;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToggleButton;
import org.eclipse.draw2d.ToggleModel;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;


/**The toolbar for an xy-graph.
 * @author Xihui Chen
 * @author Kay Kasemir (some zoom operations)
 */
public class XYGraphToolbar extends Figure {
    private final static int BUTTON_SIZE = 25;

    final private XYGraph xyGraph;
	
	final private ButtonGroup zoomGroup;
	
	/** Initialize
	 *  @param xyGraph XYGraph on which this toolbar operates
     *  @param flags Bitwise 'or' of flags
     *  @see XYGraphFlags#COMBINED_ZOOM
     *  @see XYGraphFlags#SEPARATE_ZOOM
	 */
	public XYGraphToolbar(final XYGraph xyGraph, final int flags) {		
		this.xyGraph = xyGraph;
		setLayoutManager(new WrappableToolbarLayout());
		
		final Button configButton = new Button(createImage("icons/Configure.png"));
		configButton.setToolTip(new Label("Configure Settings..."));
		addButton(configButton);
		configButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				XYGraphConfigDialog dialog = new XYGraphConfigDialog(
						Display.getCurrent().getActiveShell(), xyGraph);
				dialog.open();
			}
		});
			
		final Button addAnnotationButton = new Button(createImage("icons/Add_Annotation.png"));
		addAnnotationButton.setToolTip(new Label("Add Annotation..."));		
		addButton(addAnnotationButton);
		addAnnotationButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				AddAnnotationDialog dialog = new AddAnnotationDialog(
						Display.getCurrent().getActiveShell(), xyGraph);
				if(dialog.open() == Window.OK){
					xyGraph.addAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager().addCommand(
							new AddAnnotationCommand(xyGraph, dialog.getAnnotation()));
				}
			}
		});
		
		final Button delAnnotationButton = new Button(createImage("icons/Del_Annotation.png"));
		delAnnotationButton.setToolTip(new Label("Remove Annotation..."));
		addButton(delAnnotationButton);
		delAnnotationButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				RemoveAnnotationDialog dialog = new RemoveAnnotationDialog(
						Display.getCurrent().getActiveShell(), xyGraph);
				if(dialog.open() == Window.OK && dialog.getAnnotation() != null){
					xyGraph.removeAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager().addCommand(
							new RemoveAnnotationCommand(xyGraph, dialog.getAnnotation()));					
				}
			}
		});
		
		addSeparator();	
		if ((flags & XYGraphFlags.STAGGER) > 0)
		{	//stagger axes button
    		final Button staggerButton = new Button(createImage("icons/stagger.png"));
    		staggerButton.setToolTip(new Label("Stagger axes so they don't overlap"));
    		addButton(staggerButton);
    		staggerButton.addActionListener(new ActionListener(){
    			public void actionPerformed(ActionEvent event) {
    				xyGraph.performStagger();
    			}
    		});
		}
		else
		{	//auto scale button
            final Button autoScaleButton = new Button(createImage("icons/AutoScale.png"));
            autoScaleButton.setToolTip(new Label("Perform Auto Scale"));
            addButton(autoScaleButton);
            autoScaleButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent event) {
                    xyGraph.performAutoScale();
                }
            });
		}
		
		//zoom buttons
		zoomGroup = new ButtonGroup();
		createZoomButtons(flags);
	
		addSeparator();		
		addUndoRedoButtons();
		
		addSeparator();
		addSnapshotButton();
	}

//	@Override
//	public boolean isOpaque() {
//		return true;
//	}
	
	private static Image createImage(String path) {			
		Image image = XYGraphMediaFactory.getInstance().getImageFromPlugin(Activator.getDefault(),
				Activator.PLUGIN_ID, path);				
		return image;
	}
	
	private void addSnapshotButton() {
		Button snapShotButton = new Button(createImage("icons/camera.gif"));
		snapShotButton.setToolTip(new Label("Save Snapshot to PNG file"));
		addButton(snapShotButton);
		snapShotButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
			    // Prompt for file name
				FileDialog dialog = new FileDialog(Display.getDefault().getShells()[0], SWT.SAVE);
				dialog.setFilterNames(new String[] {"PNG Files", "All Files (*.*)" });
		        dialog.setFilterExtensions(new String[] { "*.png", "*.*" }); // Windows
			    String path = dialog.open();
			    if (path == null || path.length() <= 0)
			        return;
			    // Have valid name, so get image
                ImageLoader loader = new ImageLoader();
                Image image = xyGraph.getImage();
                loader.data = new ImageData[]{image.getImageData()};
                image.dispose();
				// Assert *.png at end of file name
			    if (! path.toLowerCase().endsWith(".png"))
			        path = path + ".png";
			    // Save
			    loader.save(path, SWT.IMAGE_PNG);
			}
		});
	}

	private void addUndoRedoButtons() {
		//undo button		
		final GrayableButton undoButton = new GrayableButton(createImage("icons/Undo.png"));
		undoButton.setToolTip(new Label("Undo"));
		undoButton.setEnabled(false);
		addButton(undoButton);		
		undoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				xyGraph.getOperationsManager().undo();
			}
		});
		xyGraph.getOperationsManager().addListener(new IOperationsManagerListener(){
			public void operationsHistoryChanged(OperationsManager manager) {
				if(manager.getUndoCommandsSize() > 0){
					undoButton.setEnabled(true);
					final String cmd_name = manager.getUndoCommands()[
					           manager.getUndoCommandsSize() -1].toString();
                    undoButton.setToolTip(new Label(NLS.bind("Undo {0}", cmd_name)));
				}else{
					undoButton.setEnabled(false);
					undoButton.setToolTip(new Label("Undo"));
				}			
			}
		});
		
		// redo button
		final GrayableButton redoButton = new GrayableButton(createImage("icons/Redo.png"));
		redoButton.setToolTip(new Label("Redo"));
		redoButton.setEnabled(false);
		addButton(redoButton);		
		redoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				xyGraph.getOperationsManager().redo();
			}
		});
		xyGraph.getOperationsManager().addListener(new IOperationsManagerListener(){
			public void operationsHistoryChanged(OperationsManager manager) {
				if(manager.getRedoCommandsSize() > 0){
					redoButton.setEnabled(true);
					final String cmd_name = manager.getRedoCommands()[
					           manager.getRedoCommandsSize() -1].toString();
                    redoButton.setToolTip(new Label(NLS.bind("Redo {0}", cmd_name)));
				}else{
					redoButton.setEnabled(false);
					redoButton.setToolTip(new Label("Redo"));
				}					
			}
		});
	}
	
	/** Create buttons enumerated in <code>ZoomType</code>
     *  @param flags Bitwise 'or' of flags
     *  @see XYGraphFlags#COMBINED_ZOOM
     *  @see XYGraphFlags#SEPARATE_ZOOM
	 */
	private void createZoomButtons(final int flags) {
		for(final ZoomType zoomType : ZoomType.values()){
		    if (! zoomType.useWithFlags(flags))
		        continue;
			final ImageFigure imageFigure =  new ImageFigure(zoomType.getIconImage());
			final Label tip = new Label(zoomType.getDescription());
			final ToggleButton button = new ToggleButton(imageFigure);
			button.setBackgroundColor(ColorConstants.button);
			button.setOpaque(true);
			final ToggleModel model = new ToggleModel();
			model.addChangeListener(new ChangeListener(){
				public void handleStateChanged(ChangeEvent event) {
					if(event.getPropertyName().equals("selected") && 
							button.isSelected()){
						xyGraph.setZoomType(zoomType);
					}				
				}
			});
			
			button.setModel(model);
			button.setToolTip(tip);
			addButton(button);
			zoomGroup.add(model);
			
			if(zoomType == ZoomType.NONE)
				zoomGroup.setDefault(model);
		}
	}
	
	public void addButton(Clickable button){
		button.setPreferredSize(BUTTON_SIZE, BUTTON_SIZE);
		add(button);
	}
	
	public void addSeparator() {
		ToolbarSeparator separator = new ToolbarSeparator();
		separator.setPreferredSize(BUTTON_SIZE/2, BUTTON_SIZE);
		add(separator);
	}
	
	class ToolbarSeparator extends Figure{
		
		private final Color GRAY_COLOR = XYGraphMediaFactory.getInstance().getColor(
				new RGB(130, 130, 130));
		
		@Override
		protected void paintClientArea(Graphics graphics) {
			super.paintClientArea(graphics);
			graphics.setForegroundColor(GRAY_COLOR);
			graphics.setLineWidth(1);
			graphics.drawLine(bounds.x + bounds.width/2, bounds.y, 
					bounds.x + bounds.width/2, bounds.y + bounds.height);
		}
	}
}
