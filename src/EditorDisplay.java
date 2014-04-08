import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.JOptionPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ogre4j.Bone;
import org.ogre4j.ColourValue;
import org.ogre4j.HardwareBuffer;
import org.ogre4j.IAnimationState;
import org.ogre4j.IBone;
import org.ogre4j.ICamera;
import org.ogre4j.IColourValue;
import org.ogre4j.IEntity;
import org.ogre4j.ILight;
import org.ogre4j.IManualObject;
import org.ogre4j.IMeshManager;
import org.ogre4j.IMeshPtr;
import org.ogre4j.INameValuePairList;
import org.ogre4j.IRadian;
import org.ogre4j.IRay;
import org.ogre4j.IRaySceneQuery;
import org.ogre4j.IRaySceneQueryResult;
import org.ogre4j.IRaySceneQueryResultEntry;
import org.ogre4j.IRenderSystemList;
import org.ogre4j.IRenderWindow;
import org.ogre4j.IRoot;
import org.ogre4j.ISceneManager;
import org.ogre4j.ISceneNode;
import org.ogre4j.IVector3;
import org.ogre4j.IViewport;
import org.ogre4j.Light.LightTypes;
import org.ogre4j.MeshManager;
import org.ogre4j.MeshPtr;
import org.ogre4j.NameValuePairList;
import org.ogre4j.Node.TransformSpace;
import org.ogre4j.Plane;
import org.ogre4j.Quaternion;
import org.ogre4j.Radian;
import org.ogre4j.Ray;
import org.ogre4j.RenderOperation.OperationType;
import org.ogre4j.ResourceGroupManager;
import org.ogre4j.Root;
import org.ogre4j.StringUtil;
import org.ogre4j.Vector3;
import org.xbig.base.StringPointer;
import org.xbig.base.WithoutNativeObject;

public final class EditorDisplay extends JFrame implements MouseListener, KeyListener,
		MouseMoveListener, WindowListener {
	String fileLoaded = null;
	float frameSel, camDist, camHgt;
	public static final float PI = 3.14159265359f;
	public static final int SCALE_EPS = 1000;
	public static final float MAG_OFFSET = 2.f;
	public static final int NUM_MODELS = 2;
	
	
	int fighterID = 0;//1serenity;2yara
	int frX=0;//for slideframe mousepos
	int lastX = 0;
	int lastY = 0;
	float oriX = 0; //origin info
	float oriY=0;
	int hbIndStart = -1;
	int hbIndEnd = -1;
	int hitboxSelect = -1;// negative value denotes nothing selected
	int frameInd = -1;
	int priSel = -1;
	int pointVertFrom = -1;
	int pointVertTo = -1;
	int displayMode = 0;
	DataHolder dh = new DataHolder();
	CopyHelper copy = new CopyHelper();
	Combo cmb_vertex, cmb_attack, cmb_frame, cmb_type, cmb_track;
	Button chk_snap, btn_MakeKeyframe, btn_animType, btn_frmDn, btn_frmUp, btn_vertCopy, btn_DeleteKeyFrame, btn_sclMode;
	Button btn_magCopy, btn_wgtCopy, btn_dirCopy, btn_dmgCopy;
	Menu pMenu, sMenu, vertInsWarningMenu;
	MenuItem m_viewDefault, m_viewWgt, m_viewDmg, m_viewZoomIn, m_viewZoomOut, m_editUndo, m_editRedo, m_fileSave,m_fileSaveAs, m_fileExit, m_fileOpen,
	m_trackImport, m_trackCopy;
	MenuItem pMenuAddVert, pMenuAddHB, pMenuAddVPoint, pMenuDeleteVertex, pMenuDeleteHitbox, pMenuStandardizeHitbox, pMenuCopyVert, pMenuPaste,
	pMenuInvertHitbox, pMenuSetCentre, sMenuAddPriority;
	Scale slideFrame;
	boolean dataLoaded = false;
	boolean changed = false;
	boolean isSnapping = false;
	boolean mouseLook = false; // booleans record states for visual interaction
	boolean moveVert = false; // denotes dragging the vertex to a new position
	boolean moveDir = false; // denotes moving the directional arrow
	boolean moveFrame = false;
	boolean moveHitbox = true;
	
	final float snapRange = 0.01f;
	final float vertHndlRad = 0.2f;
	Text txt_xPos, txt_yPos, txt_mag, txt_wgt, txt_dir, txt_dmg, txt_frame, txt_pri;
	Plane fieldPlane;
	float xCursor, yCursor, xOff=0.f, yOff=0.f; // selected on plane z=0
	//UI colors
	RGB rgb_kf = new RGB(33, 150, 10);
	RGB rgb_kfSel = new RGB(150, 155, 70);
	RGB rgb_kfBack = new RGB(11, 50, 7);
	RGB rgb_kfBackSel = new RGB(10, 51, 17);
	RGB rgb_feedback = new RGB(255,255,10);
	RGB rgb_priBack = new RGB(82, 30, 15);
	RGB rgb_priBackSel = new RGB(94, 51, 30);
	RGB rgb_pri = new RGB(132, 50, 25);
	RGB rgb_priSel = new RGB(144, 71, 47);
	RGB rgb_unedit = new RGB(200, 200, 200);
	RGB rgb_edit = new RGB (150, 250, 150);
	Timer tmr_feedback = new Timer();
	Timer tmr_autosave = new Timer();
	Track tPtr=null;
	Frame fPtr=null;
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 640;
	public static final int DEFAULT = 0;
	public static final int WEIGHT = 1;
	public static final int DAMAGE = 2;
	public static final int feedbackTime=500;
	public static final int autosaveInterval = 200000;
	

	public static void main(String[] args) {
		new EditorDisplay();
	}

	public static void mainws(String locPath, String locType,
			String renderSystem) {
		System.loadLibrary("OgreMain");
		if ("Direct3D9".equals(renderSystem)) {
			System.loadLibrary("RenderSystem_Direct3D9");
		} else {
			System.loadLibrary("RenderSystem_GL");
		}
		new EditorDisplay(locPath, locType, renderSystem);
		// something blocks the exit of the app. force exit.
		System.exit(0);
	}
	private IRoot root;
	private ICamera camera;
	private IRenderWindow renderWindow;
	private ISceneNode mainNode;
	//Set to true after exiting event loop to avoid repaint after disposing of  canvas.
	private boolean isDisposing = false;
	private Display display;
	private Shell shell;
	private Canvas canvas;
	private ISceneManager sceneManager;
	private IAnimationState animState;
	private IEntity entity;
	private IViewport viewport;	
	// Sets media to "media" directory.
	public EditorDisplay() {
		this("media", "FileSystem", "OpenGL");
		
	}

	private EditorDisplay(String media, String locType, String renderSystem) {
		
		try {
			setup(media, locType, renderSystem);
			addWindowListener(this);
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			// enter event loop
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			cleanup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void cleanup() {
		// clean up
		isDisposing = true;
		shell.dispose();
		tmr_feedback.cancel();
        tmr_feedback.purge();
        tmr_autosave.cancel();
        tmr_autosave.purge();
		display.dispose();
		root.delete();
	}
	private void createPlane() {
		//creates the ground plane, and the field plane
		//field plane is at z=0; used for click detection
		Plane plane = new Plane(Vector3.getUNIT_Y(), 0);
		IMeshPtr meshPtr = new MeshPtr(
				WithoutNativeObject.I_WILL_DELETE_THIS_OBJECT);
		IMeshManager meshMgr = MeshManager.getSingleton();
		meshMgr.createPlane(meshPtr, "ground",
				ResourceGroupManager.getDEFAULT_RESOURCE_GROUP_NAME(), plane,
				85f, 85f, 20, 20, true, 1, 5, 5, Vector3.getUNIT_Z(),
				HardwareBuffer.Usage.HBU_STATIC_WRITE_ONLY,
				HardwareBuffer.Usage.HBU_STATIC_WRITE_ONLY, true, true);
		plane.delete();
		meshPtr.delete();
		IEntity entity = sceneManager.createEntity("GroundEntity", "ground");
		mainNode = sceneManager.getRootSceneNode().createChildSceneNode("GroundEntityNode", Vector3.getZERO(),Quaternion.getIDENTITY());
		mainNode.attachObject(entity);
		fieldPlane = new Plane(Vector3.getUNIT_Z(), 0);
		meshPtr = new MeshPtr(WithoutNativeObject.I_WILL_DELETE_THIS_OBJECT);
		meshMgr = MeshManager.getSingleton();
		meshMgr.createPlane(meshPtr, "field",
				ResourceGroupManager.getDEFAULT_RESOURCE_GROUP_NAME(),
				fieldPlane, 185f, 185f, 180, 180, true, 1, 5, 5,
				Vector3.getUNIT_Y(),
				HardwareBuffer.Usage.HBU_STATIC_WRITE_ONLY,
				HardwareBuffer.Usage.HBU_STATIC_WRITE_ONLY, true, true);
		meshPtr.delete();

		entity = sceneManager.createEntity("FieldEntity", "field");
		ISceneNode node = sceneManager.getRootSceneNode().createChildSceneNode(
				"FieldEntityNode", Vector3.getZERO(), Quaternion.getIDENTITY());
		node.attachObject(entity);
		entity.setVisible(false);
	}
	private void setup(String media, String locType, String renderSystem)
			throws Exception {
		// create Ogre Root
		root = new Root("", "", this.getClass().getName() + ".log");
		// SWT initialization+++
		display = new Display();
		shell = new Shell(display);
		shell.setText("GeminiEditor");
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = false;
		rowLayout.justify = true;
		rowLayout.type = SWT.VERTICAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 0;
		GridLayout gridLayout = new GridLayout(2, false);

		shell.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		shell.setLayout(gridLayout);
		// menusetup
		SelectionListener menuListener = new mainMenuListener();
		
		Menu menu = new Menu(shell, SWT.BAR);
		MenuItem file = new MenuItem(menu, SWT.CASCADE);
		MenuItem edit = new MenuItem(menu, SWT.CASCADE);
		MenuItem view = new MenuItem(menu, SWT.CASCADE);
		MenuItem trac =new MenuItem(menu, SWT.CASCADE);
		file.setText("File");
		edit.setText("Edit");
		view.setText("View");
		trac.setText("Track");
		Menu filemenu = new Menu(shell, SWT.DROP_DOWN);
		file.setMenu(filemenu);
		m_fileOpen = new MenuItem(filemenu, SWT.PUSH);
		m_fileOpen.setText("Open");
		m_fileOpen.addSelectionListener(menuListener);
		m_fileSave = new MenuItem(filemenu, SWT.PUSH);
		m_fileSave.setText("Save");
		m_fileSave.addSelectionListener(menuListener);
		m_fileSaveAs = new MenuItem(filemenu, SWT.PUSH);
		m_fileSaveAs.setText("Save As...");
		m_fileSaveAs.addSelectionListener(menuListener);
		m_fileExit = new MenuItem(filemenu, SWT.PUSH);
		m_fileExit.setText("Exit");
		m_fileExit.addSelectionListener(menuListener);
		Menu editmenu = new Menu(shell, SWT.DROP_DOWN);
		edit.setMenu(editmenu);
		m_editUndo = new MenuItem(editmenu, SWT.PUSH);
		m_editUndo.setText("Undo");
		m_editUndo.addSelectionListener(menuListener);
		m_editRedo = new MenuItem(editmenu, SWT.PUSH);
		m_editRedo.setText("Redo");
		m_editRedo.addSelectionListener(menuListener);
		m_editUndo.setEnabled(false);
		m_editRedo.setEnabled(false);
		Menu viewMenu = new Menu(shell, SWT.DROP_DOWN);
		view.setMenu(viewMenu);
		m_viewDefault = new MenuItem(viewMenu, SWT.PUSH);
		m_viewDefault.setText("Default");
		m_viewDefault.addSelectionListener(menuListener);
		m_viewWgt = new MenuItem(viewMenu, SWT.PUSH);
		m_viewWgt.setText("View Weights");
		m_viewWgt.addSelectionListener(menuListener);
		m_viewDmg = new MenuItem(viewMenu, SWT.PUSH);
		m_viewDmg.setText("View Damage");
		m_viewDmg.addSelectionListener(menuListener);
		m_viewZoomIn = new MenuItem(viewMenu, SWT.PUSH);
		m_viewZoomIn.setText("Zoom In");
		m_viewZoomIn.addSelectionListener(menuListener);
		m_viewZoomOut = new MenuItem(viewMenu, SWT.PUSH);
		m_viewZoomOut.setText("Zoom Out");
		m_viewZoomOut.addSelectionListener(menuListener);
		Menu trackMenu = new Menu(shell, SWT.DROP_DOWN);
		trac.setMenu(trackMenu);
		m_trackImport = new MenuItem(trackMenu, SWT.PUSH);
		m_trackImport.setText("Import Track");
		m_trackImport.addSelectionListener(menuListener);
		m_trackCopy = new MenuItem(trackMenu, SWT.PUSH);
		m_trackCopy.setText("Copy Track");
		m_trackCopy.addSelectionListener(menuListener);
		pMenu = new Menu(shell, SWT.POP_UP);
		pMenuAddVert = new MenuItem(pMenu, SWT.CASCADE);
		pMenuAddVert.setText("Add Vertex");
		pMenuAddHB = new MenuItem(pMenu, SWT.CASCADE);
		pMenuAddHB.setText("Add New Hitbox");
		pMenuAddVPoint = new MenuItem(pMenu, SWT.CASCADE);
		pMenuAddVPoint.setText("Point to another vertex...");
		pMenuDeleteVertex = new MenuItem(pMenu, SWT.CASCADE);
		pMenuDeleteHitbox = new MenuItem(pMenu, SWT.CASCADE);
		pMenuDeleteVertex.setText("Delete vertex");
		pMenuDeleteHitbox.setText("Delete hitbox");
		pMenuDeleteHitbox.setEnabled(false);
		pMenuDeleteHitbox.addSelectionListener(menuListener);
		pMenuStandardizeHitbox= new MenuItem(pMenu, SWT.CASCADE);
		pMenuStandardizeHitbox.setText("Copy hitbox to frames");
		pMenuStandardizeHitbox.setEnabled(false);
		pMenuInvertHitbox = new MenuItem(pMenu, SWT.CASCADE);
		pMenuInvertHitbox.setText("Invert hitbox");
		pMenuInvertHitbox.setEnabled(false);
		pMenuSetCentre = new MenuItem(pMenu, SWT.CASCADE);
		pMenuSetCentre.setText("Set Centre");
		pMenuCopyVert = new MenuItem(pMenu, SWT.CASCADE);
		pMenuCopyVert.setText("Copy Vertex");
		pMenuPaste = new MenuItem(pMenu, SWT.CASCADE);
		pMenuPaste.setText("(Nothing to paste!)");
		pMenuPaste.setEnabled(false);
		pMenuPaste.addSelectionListener(menuListener);
		pMenuAddVPoint.addSelectionListener(menuListener);
		pMenuAddVert.addSelectionListener(menuListener);
		pMenuAddHB.addSelectionListener(menuListener);
		pMenuDeleteVertex.addSelectionListener(menuListener);
		pMenuStandardizeHitbox.addSelectionListener(menuListener);
		pMenuInvertHitbox.addSelectionListener(menuListener);
		pMenuSetCentre.addSelectionListener(menuListener);
		pMenuCopyVert.addSelectionListener(menuListener);
		shell.setMenuBar(menu);
		
		

		String operatingSystem = System.getProperty("os.name").toLowerCase();
		if (operatingSystem.equals("linux")) {
			GLData data = new GLData();
			data.doubleBuffer = true;
			canvas = new GLCanvas(shell, SWT.NONE, data);
			((GLCanvas) canvas).setCurrent();
		} else {
			canvas = new Canvas(shell, SWT.NONE);
		}
		GridData canvasDat = new GridData(GridData.FILL, GridData.FILL, true,true);
		canvasDat.verticalSpan = 3;
		canvas.setLayoutData(canvasDat);
		canvas.setMenu(pMenu);
		Label atkLabel = new Label(shell, SWT.NONE);
		atkLabel.setText("Attack Selected:");
		
		// vertex info composite panel
		final Composite vComp = new Composite(shell, SWT.NONE);
		FillLayout vLayout = new FillLayout();
		vLayout.type = SWT.VERTICAL;
		vComp.setLayout(vLayout);
		Label vertLabel = new Label(vComp, SWT.NONE);
		vertLabel.setText("Vertex Details:");
		final Composite priComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		priComp.setLayout(new GridLayout(2, false));
		Label priLabel = new Label(priComp, SWT.NONE);
		priLabel.setText("Priority:");
		txt_pri = new Text(priComp, SWT.BORDER);
		// combobox info filled here
		final Composite atkComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		atkComp.setLayout(new GridLayout(2, false));
		cmb_attack = new Combo(atkComp, SWT.NONE);
		FillAttackCombo();
		
		btn_animType = new Button(atkComp, SWT.NONE);
		btn_animType.setText("Other Anims");
		btn_animType.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String s = btn_animType.getText();
				if (dataLoaded) {
					if(s.compareTo("Other Anims")==0){
						btn_animType.setText("Attacks");
						cmb_attack.removeAll();
						FillOtherCombo();}
					else if(s.compareTo("Attacks")==0){
						btn_animType.setText("Other Anims");
						cmb_attack.removeAll();
						FillAttackCombo();}
				}
			}
		});
		cmb_type = new Combo(vComp, SWT.NONE);
		cmb_type.add("single");
		cmb_type.add("multi");
		cmb_type.add("paralell");
		cmb_type.select(0);
		cmb_track = new Combo(vComp, SWT.NONE);
		cmb_track.setEnabled(false);
		cmb_vertex = new Combo(vComp, SWT.NONE);
		cmb_vertex.add("No verts!");
		cmb_attack.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (dataLoaded){
					if((btn_animType.getText().compareTo("Other Anims")==0))//denotes attacks are loaded incombo
						SetAttack();
					else//denotes other animations are loaded in combo
						SetAnims();
					}
				}			
		});
		cmb_vertex.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int vertSel = cmb_vertex.getSelectionIndex();
				int atkSel = cmb_attack.getSelectionIndex();
				SelectVertex(atkSel, vertSel);
			}
		});		
		cmb_type.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				int atkSel = cmb_attack.getSelectionIndex();
				SetAttackType(atkSel, cmb_type.getItem(cmb_type.getSelectionIndex()));
				cmb_track.select(0);
			}
		});
		cmb_track.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				int atkSel = cmb_attack.getSelectionIndex();
				SelectTrack(atkSel, cmb_track.getSelectionIndex());
				cmb_vertex.select(0);
				
			}
		});
		GridData stData = new GridData();
		stData.horizontalSpan = 2;
		final Composite posComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		posComp.setLayout(new GridLayout(3, false));
		Label posLabel = new Label(posComp, SWT.NONE);
		posLabel.setText("Position");
		txt_xPos = new Text(posComp, SWT.BORDER);
		txt_yPos = new Text(posComp, SWT.BORDER);
		final Composite dirComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		dirComp.setLayout(new GridLayout(3, false));
		Label dirLabel = new Label(dirComp, SWT.NONE);
		dirLabel.setText("Direction");
		txt_dir = new Text(dirComp, SWT.SINGLE | SWT.BORDER);
		dirLabel.setSize(22, 52);
		btn_dirCopy = new Button(dirComp, SWT.NONE);
		btn_dirCopy.setText("C");
		btn_dirCopy.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CopyToHitbox("dir");
				}
		});
		final Composite magComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		magComp.setLayout(new GridLayout(3, false));
		Label magLabel = new Label(magComp, SWT.NONE);
		magLabel.setText("Magnitude");
		txt_mag = new Text(magComp, SWT.BORDER);
		btn_magCopy = new Button(magComp, SWT.NONE);
		btn_magCopy.setText("C");
		btn_magCopy.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CopyToHitbox("mag");
				}
		});
		final Composite wgtComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		wgtComp.setLayout(new GridLayout(3, false));
		Label wgtLabel = new Label(wgtComp, SWT.NONE);
		wgtLabel.setText("Weight");
		txt_wgt = new Text(wgtComp, SWT.BORDER);
		btn_wgtCopy = new Button(wgtComp, SWT.NONE);
		btn_wgtCopy.setText("C");
		btn_wgtCopy.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CopyToHitbox("wgt");
				}
		});
		final Composite dmgComp = new Composite(vComp, SWT.SINGLE | SWT.NONE);
		dmgComp.setLayout(new GridLayout(3, false));
		Label dmgLabel = new Label(dmgComp, SWT.NONE);
		dmgLabel.setText("Damage");
		txt_dmg = new Text(dmgComp, SWT.BORDER);
		btn_dmgCopy = new Button(dmgComp, SWT.NONE);
		btn_dmgCopy.setText("C");
		btn_dmgCopy.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CopyToHitbox("wgt");
				}
		});
		cmb_frame = new Combo(vComp, SWT.NONE);
		cmb_frame.add("No frames!");
		cmb_frame.select(0);
		btn_vertCopy = new Button(vComp, SWT.BORDER);
		btn_vertCopy.setText("Vert Values to Hitbox");
		btn_vertCopy.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CopyToHitbox("all");
			}
		});
		Composite sComp = new Composite(shell, SWT.NONE);
		GridLayout sLayout = new GridLayout(6, false);
		sComp.setLayout(sLayout);
		btn_frmDn = new Button(sComp, SWT.BORDER);
		btn_frmDn.setText("<");
		btn_frmDn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FrameDown();
			}

		});
		slideFrame = new Scale(sComp, SWT.HORIZONTAL);
		slideFrame.setMinimum(0);
		slideFrame.setMaximum(SCALE_EPS);
		slideFrame.setIncrement(1);
		GridData slideDat = new GridData(GridData.FILL, GridData.FILL, true,false);
		slideDat.horizontalSpan = 2;
		slideDat.grabExcessHorizontalSpace = true;
		slideFrame.setLayoutData(slideDat);
		sComp.setLayoutData(slideDat);
		slideFrame.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
					int sCount = slideFrame.getSelection();
					frameSel = (float) sCount / SCALE_EPS;
					Track trk = GetCurTrack();
					if (isSnapping) { // snap if the checkbox has been selected
							int frameNum;
							int frameDiff;
							if (trk.keyFrame!=null){
								for (Frame f = trk.keyFrame; f!=null; f=f.next) {
									frameNum = (int) (f.time * (float)SCALE_EPS);
									frameDiff = sCount - frameNum;
									if (frameDiff < 0)
										frameDiff = frameDiff * (-1);
									if (frameDiff < (int) (snapRange * 1000)) {
										slideFrame.setSelection(frameNum);
										frameSel = f.time;
									}
								}
							}

					}
					txt_pri.setEditable(false);
					txt_pri.setBackground(new Color(display, rgb_unedit));
					for (Frame f = trk.keyFrame;f!=null;f=f.next){
						if((int)(f.time*SCALE_EPS)<slideFrame.getSelection())
							txt_pri.setText(Float.toString(f.value));
						else if((int)(f.time*SCALE_EPS)==slideFrame.getSelection()){
							txt_pri.setEditable(true);
							txt_pri.setBackground(new Color(display, rgb_edit));
							txt_pri.setText(Float.toString(f.value));
						}
						
					}
					
					FrameCheck(frameSel);
					UpdateAnim();
					if (trk.VertSize() > 0)
						RenderHitBox();
					else
						root.renderOneFrame();
			}
		});
		
		sMenu = new Menu(slideFrame);
		sMenuAddPriority = new MenuItem(sMenu, SWT.CASCADE);
		sMenuAddPriority.setText("Add Priority");
		sMenuAddPriority.addSelectionListener(menuListener);
		slideFrame.setMenu(sMenu);
		
		btn_frmUp = new Button(sComp, SWT.BORDER);
		btn_frmUp.setText(">");
		btn_frmUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FrameUp();
			}
		});
		SelectionAdapter slideMoveListener = new SlideMoveListener();
		KeyListener enterListener = new EnterListener();
		VerifyListener makeTextNumOnly = new NumOnlyTextListener();
		cmb_frame.addSelectionListener(slideMoveListener);
		// listener to handle all editing of textfields
		FocusListener commitChange = new CommitChangeListener();
		txt_xPos.addFocusListener(commitChange);
		txt_yPos.addFocusListener(commitChange);
		txt_mag.addFocusListener(commitChange);
		txt_dmg.addFocusListener(commitChange);
		txt_dir.addFocusListener(commitChange);
		txt_wgt.addFocusListener(commitChange);
		txt_pri.addFocusListener(commitChange);
		txt_xPos.addKeyListener(enterListener);
		txt_yPos.addKeyListener(enterListener);
		txt_mag.addKeyListener(enterListener);
		txt_dmg.addKeyListener(enterListener);
		txt_dir.addKeyListener(enterListener);
		txt_wgt.addKeyListener(enterListener);
		txt_pri.addKeyListener(enterListener);
		txt_xPos.addVerifyListener(makeTextNumOnly);
		txt_yPos.addVerifyListener(makeTextNumOnly);
		txt_mag.addVerifyListener(makeTextNumOnly);
		txt_dmg.addVerifyListener(makeTextNumOnly);
		txt_dir.addVerifyListener(makeTextNumOnly);
		txt_wgt.addVerifyListener(makeTextNumOnly);
		txt_pri.addVerifyListener(makeTextNumOnly);
		Label frmLabel = new Label(sComp, SWT.NONE);
		frmLabel.setText("Frame #:");
		txt_frame = new Text(sComp, SWT.BORDER);
		GridData txtDat = new GridData(GridData.BEGINNING, GridData.FILL, true,true);
		txt_frame.setLayoutData(txtDat);
		chk_snap = new Button(sComp, SWT.CHECK);
		chk_snap.setText("Snap-to-frame");
		chk_snap.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (chk_snap.getSelection())
					isSnapping = true;
				else
					isSnapping = false;
			}
		});
		chk_snap.setSelection(true);
		isSnapping = true;
		btn_DeleteKeyFrame = new Button(sComp, SWT.BORDER);
		btn_DeleteKeyFrame.setText("Delete Key Frame");
		btn_DeleteKeyFrame.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (dataLoaded) {
					DeleteKeyframe(frameSel);
				}
			}
		});
		btn_MakeKeyframe = new Button(sComp, SWT.BORDER);
		btn_MakeKeyframe.setText("Make Key Frame");
		btn_MakeKeyframe.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (dataLoaded) {
					int sCount = slideFrame.getSelection();
					AddKeyframe((float) sCount / SCALE_EPS);
				}
			}
		});
		slideFrame.addPaintListener(new FramePaintListener());
		shell.open();
		// load render system
		if ("Direct3D9".equals(renderSystem)) {
			root.loadPlugin("RenderSystem_Direct3D9");
		} else {
			root.loadPlugin("RenderSystem_GL");
		}

		// set render system
		IRenderSystemList list = root.getAvailableRenderers();
		if (list == null || list.size() == 0) {
			throw new Exception("No RenderSystem loaded!");
		}
		root.setRenderSystem(list.at(0));

		// initialise Ogre Root
		// its ok to get a null pointer exception here
		// ogre doesnt create a new IRenderWindow
		try {
			root.initialise(false, "", StringUtil.getBLANK());
		} catch (NullPointerException e) {
			// OK, method returns null
		}
		// create RenderWindow with SWT handle
		INameValuePairList params = new NameValuePairList();
		StringPointer windowHandlePointer;
		if (operatingSystem.equals("linux")) {
			windowHandlePointer = new StringPointer("true");
			params.insert("currentGLContext", windowHandlePointer);
		} else {
			windowHandlePointer = new StringPointer(
					Integer.toString(canvas.handle));
			params.insert("externalWindowHandle", windowHandlePointer);
		}
		renderWindow = root.createRenderWindow("", canvas.getBounds().width,
				canvas.getBounds().height, false, params);
		params.delete();
		windowHandlePointer.delete();
		// create scene manager
		sceneManager = root.createSceneManager(0, "Default");
		IColourValue ambientColour = new ColourValue(0.1f, 0.1f, 0.1f, 0);
		sceneManager.setAmbientLight(ambientColour);
		ambientColour.delete();
		// create light
		IColourValue lightColour = new ColourValue(0.7f, 0.7f, 0.7f, 0);
		ILight light = sceneManager.createLight("lightName");
		light.setDiffuseColour(lightColour);
		light.setSpecularColour(lightColour);
		lightColour.delete();
		light.setType(LightTypes.LT_DIRECTIONAL);
		light.setDirection(0, -0.5f, -1);
		light.setPosition(0, 50, 100);
		light.setVisible(true);

		// create camera
		camera = sceneManager.createCamera("Main Camera");
		camDist = 70;// note camDist is used to calculate coordinates in
						// mouseListeners
		camHgt = 20;
		camera.setPosition(0, camHgt, camDist);
		camera.lookAt(0, camHgt, 0);
		camera.setNearClipDistance(0.1f);
		viewport = renderWindow.addViewport(camera, 0, 0.0f, 0.0f, 1.0f, 1.0f);
		IColourValue backgroundColour = new ColourValue(0.3f, 0.3f, 0.3f, 1.0f);
		viewport.setBackgroundColour(backgroundColour);
		backgroundColour.delete();
		viewport.setOverlaysEnabled(false);
		viewport.setCamera(camera);
		camera.setAspectRatio((float) canvas.getBounds().width
				/ (float) canvas.getBounds().height);

		// connect camera with render window
		renderWindow.setActive(true);

		// load resources
		ResourceGroupManager.getSingleton().addResourceLocation(media, locType,
				"General", false);
		ResourceGroupManager.getSingleton().initialiseAllResourceGroups();

		// set mouse & key listeners
		this.addWindowListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		slideFrame.addMouseListener(this);
		slideFrame.addMouseMoveListener(this);
		// create a plane as ground
		createPlane();
		// react to paint events
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int height = canvas.getBounds().height;
				int width = canvas.getBounds().width;
				// circumvent a directx bug. directx crashed
				if (height == 0) 
					height = 1;
				if (width == 0) 
					width = 1;

				// redraw the ogre window
				if (isDisposing == true || camera == null
						|| renderWindow == null) {
					return;
				}
				camera.setAspectRatio((float) width / (float) height);
				renderWindow.windowMovedOrResized();

				root.renderOneFrame();
			}

		});
		// initalize controls to a meaningful display
		cmb_vertex.select(0);
		
		canvas.addKeyListener(this);

		// last but not least render first frame
		root.renderOneFrame();
	}



	private void FrameDown() {
		if (dataLoaded) {				
			for(int sCount = slideFrame.getSelection()-1; (sCount>=0)&&(!FrameCheck(sCount)); sCount--)//should cycle through
					slideFrame.setSelection(sCount);				
		}
		
	}
	protected void FrameUp() {
		if (dataLoaded) {
			int sCount = slideFrame.getSelection();
			//increment now or loop will always trigger on first item
			for(sCount=sCount+1; (sCount<=1000)&&(!FrameCheck(sCount)); sCount++);//should cycle through
			slideFrame.setSelection(sCount);
				
		}
		
	}

	protected void SetAttackType(int atkSel, String type) {
		
		dh.attack[atkSel].SetType(type);
		FillTrackCombo();
		cmb_track.select(0);	
		
	}
	private void FillTrackCombo() {
		int aSel=cmb_attack.getSelectionIndex();
		cmb_track.removeAll();
		if(dh.attack[aSel].trType>0){
		for(Track t = dh.attack[aSel];t!=null;t=t.next)
			cmb_track.add("track " + t.GetId());
		cmb_track.add("new track");
		cmb_track.add("cut last");
		cmb_track.setEnabled(true);
		}else
			cmb_track.setEnabled(false);
		
	}

	protected void SelectTrack(int aSel, int tSel){
		if(cmb_track.getItem(tSel).compareTo("new track")==0){
			tPtr=dh.attack[aSel].addTrack();
			FillTrackCombo();
			cmb_track.select(tPtr.GetId());
		}
		else if (cmb_track.getItem(tSel).compareTo("cut last")==0){
			dh.attack[aSel].CutTrack();
			FillTrackCombo();
			cmb_track.select(0);
		}
		RenderHitBox();
		cmb_vertex.select(0);
		FillFrameCombo();
		slideFrame.redraw();
	}

	private void FillAttackCombo() {
		cmb_attack.removeAll();
		cmb_attack.add("Neutral 'A'");
		cmb_attack.add("Side 'A'");
		cmb_attack.add("Down 'A'");
		cmb_attack.add("Up 'A'");
		cmb_attack.add("Neutral 'B'");
		cmb_attack.add("Side 'B'");
		cmb_attack.add("Down 'B'");
		cmb_attack.add("Up 'B'");
		cmb_attack.add("Neutral 'C'");
		cmb_attack.add("Side 'C'");
		cmb_attack.add("Down 'C'");
		cmb_attack.add("Up 'C'");
		cmb_attack.add("Dash Attack");
		cmb_attack.add("Neutral Air");
		cmb_attack.add("Forward Air");
		cmb_attack.add("Back Air");
		cmb_attack.add("Down Air");
		cmb_attack.add("Up Air");
		//Serenity special case
		if(fighterID==1){
			cmb_attack.add("Sword N Air");
			cmb_attack.add("Sword F Air");
			cmb_attack.add("Sword B Air");
			cmb_attack.add("Sword D Air");
			cmb_attack.add("Sword U Air");
		
		}
		
		
		cmb_attack.select(0);
		if(dh.attack[0].trType>0)
			cmb_track.setEnabled(true);
		if(dataLoaded){
			IEntity gEntity = sceneManager.getEntity("GroundEntity");
			gEntity.setVisible(true);
			shell.redraw();
			}
	}
	private void FillOtherCombo(){
		cmb_attack.add("Idle");
		cmb_attack.add("Idle2");
		cmb_attack.add("Jump");
		cmb_attack.add("Fall");
		cmb_attack.add("Walk");
		cmb_attack.add("Run");
		cmb_attack.add("AirRecover");
		cmb_attack.add("Knockback");
		cmb_attack.add("Run");
		cmb_attack.add("AirRecover");
		cmb_attack.add("Tumble");
		cmb_attack.add("SentFlying");
		cmb_attack.add("Grabbed");
		cmb_attack.add("GetUp");
		cmb_attack.add("LedgeGrab");
		cmb_attack.add("LedgeClimb");
		cmb_attack.add("SpotDodge");
		cmb_attack.add("Land");
		cmb_attack.add("TBLand");
		cmb_attack.add("Slam");
		cmb_attack.select(0);
		if(dataLoaded){
		IEntity gEntity = sceneManager.getEntity("GroundEntity");
		gEntity.setVisible(false);
		shell.redraw();
		
		//autosave
		tmr_autosave.schedule(new AutoSave(), autosaveInterval);
		}
	}

	private void LoadPlayer(String name) {
		if(sceneManager.hasEntity("model"))
			sceneManager.destroyEntity("model");
		entity = sceneManager.createEntity("model", name);
		ISceneNode mNode = mainNode.createChildSceneNode(
				Vector3.getZERO(), Quaternion.getIDENTITY());
		mNode.attachObject(entity);
		mNode.setPosition(0, 0, 0);
		IRadian yaw = new Radian(PI / 2.f);
		mNode.yaw(yaw, TransformSpace.TS_WORLD);
		//rescalings!
		if(name.compareTo("Yara.mesh")==0){
			mNode.setScale(24,24,24);
			Zoom(1.4f);
		}
		else{
			mNode.setScale(1,1,1);
			FillAttackCombo();
		}
		animState = entity.getAnimationState("NA");
		animState.setEnabled(true);
		animState.setTimePosition(0.1f);
		SetAttack();		
		tmr_autosave.schedule(new AutoSave(), autosaveInterval);
	}
	private void SetAnims(){
		animState.setEnabled(false);
		String animStr = cmb_attack.getText(); 
		animState = entity.getAnimationState(animStr);	
		animState.setEnabled(true);
		UpdateAnim();
		slideFrame.redraw();//to show the frames
		shell.redraw();
	}
	public void CopyToHitbox(String s){
		int vSel = cmb_vertex.getSelectionIndex();
		Vertex  b;
		Track trk = GetCurTrack();	
		if ((dataLoaded)&&(hitboxSelect>-1)&&(FrameCheck(frameSel))){
			b = trk.GetVertAtFrame(vSel, frameSel);
			SelectEntireHitbox(hitboxSelect);
			for(int i = hbIndStart; i<=hbIndEnd;i++){
				if((s.compareTo("all")==0)||(s.compareTo("dir")==0))
					trk.vertArr[i].SetDir( frameSel, b.dir);
				if((s.compareTo("all")==0)||(s.compareTo("dmg")==0))
					trk.vertArr[i].SetDmg(frameSel, b.dmg);
				if((s.compareTo("all")==0)||(s.compareTo("mag")==0))
					trk.vertArr[i].SetMag( frameSel, b.mag);
				if((s.compareTo("all")==0)||(s.compareTo("wgt")==0))
					trk.vertArr[i].SetWgt( frameSel,  b.wgt);
				RenderHitBox(); // update changes to screen
				shell.redraw();
			}
				
		}	
	}
	private void SetAttack() {
		// determine selected attack from combobox
		int aSel = cmb_attack.getSelectionIndex();


		animState.setEnabled(false);
		
		if (cmb_attack.getText().equals("Neutral 'A'")) 
			animState = entity.getAnimationState("NA");		
		else if (cmb_attack.getText().equals("Side 'A'")) 
			animState = entity.getAnimationState("SA");
		else if (cmb_attack.getText().equals("Down 'A'"))
			animState = entity.getAnimationState("DA");
		else if (cmb_attack.getText().equals("Up 'A'")) 
			animState = entity.getAnimationState("UA");
		else if (cmb_attack.getText().equals("Neutral 'B'")) 
			animState = entity.getAnimationState("NB");
		else if (cmb_attack.getText().equals("Side 'B'")) 
			animState = entity.getAnimationState("SB");
		else if (cmb_attack.getText().equals("Down 'B'")) 
			animState = entity.getAnimationState("DB");
		else if (cmb_attack.getText().equals("Up 'B'")) 
			animState = entity.getAnimationState("UB");
		else if (cmb_attack.getText().equals("Neutral 'C'")) 
					animState = entity.getAnimationState("NC");
		else if (cmb_attack.getText().equals("Side 'C'")) 
			animState = entity.getAnimationState("SC");
		else if (cmb_attack.getText().equals("Down 'C'")) 
			animState = entity.getAnimationState("DC");
		else if (cmb_attack.getText().equals("Up 'C'")) 
			animState = entity.getAnimationState("UC");
		else if(cmb_attack.getText().equals("Neutral Air"))
			animState = entity.getAnimationState("NAIR");
		else if(cmb_attack.getText().equals("Down Air"))
			animState = entity.getAnimationState("DAIR");
		else if(cmb_attack.getText().equals("Up Air"))
			animState = entity.getAnimationState("UAIR");
		else if(cmb_attack.getText().equals("Forward Air"))
			animState = entity.getAnimationState("FAIR");
		else if(cmb_attack.getText().equals("Back Air"))
			animState = entity.getAnimationState("BAIR");
		else if(cmb_attack.getText().equals("Dash Attack"))
			animState = entity.getAnimationState("DATK");
		
		//Serenity special anims
		if(fighterID==1){//serenity
			if(cmb_attack.getText().equals("Sword N Air"))
				animState = entity.getAnimationState("SNAIR");
			else if(cmb_attack.getText().equals("Sword D Air"))
				animState = entity.getAnimationState("SDAIR");
			else if(cmb_attack.getText().equals("Sword U Air"))
				animState = entity.getAnimationState("SUAIR");
			else if(cmb_attack.getText().equals("Sword F Air"))
				animState = entity.getAnimationState("SFAIR");
			else if(cmb_attack.getText().equals("Sword B Air"))
				animState = entity.getAnimationState("SBAIR");
			
			if (cmb_attack.getText().equals("Neutral 'B'")) 
				CamFocusSword();
			else if (cmb_attack.getText().equals("Side 'B'")) 
				CamFocusSword();
			else if (cmb_attack.getText().equals("Down 'B'")) 
				CamFocusSword();
			else if (cmb_attack.getText().equals("Up 'B'")) 
				CamFocusSword();
			else
				UnfocusSword();
			
			}
		animState.setEnabled(true);
		
		cmb_type.select(dh.attack[aSel].trType);
		if(dh.attack[aSel].trType!=0)
			cmb_track.setEnabled(true);
		else
			cmb_track.setEnabled(false);
		
		FillTrackCombo();
		cmb_track.select(0);
		UpdateAnim();
		FillVtxCombo();
		FillFrameCombo();
		if (dh.attack[aSel].VertSize() > 0)
			SelectVertex(aSel, 0);// keep selected vertex from falling out of array bounds
		slideFrame.redraw();//to show the frames
		RenderHitBox();
	}

	private void CamFocusSword() {
		//repositions the field so Serenity's sword is centered.
		//special case for this character only
		IBone sBone;
		mainNode.translate(-oriX, -oriY, 0, TransformSpace.TS_PARENT);
		sBone = entity.getSkeleton().getBone("Sword");
		oriX=-sBone._getDerivedPosition().getz();
		oriY=-sBone._getDerivedPosition().gety()+camHgt;
		camera.setPosition(0, camHgt, camDist/2);
		mainNode.translate(oriX, oriY, 0, TransformSpace.TS_PARENT);
	}
	private void UnfocusSword() {
		//repositions the field to uncenter Serenity's sword
		//special case for this character only
		camera.setPosition(0, camHgt, camDist);
		mainNode.translate(-oriX, -oriY, 0, TransformSpace.TS_PARENT);
		oriX=0;
		oriY=0;
	}

	private void FillFrameCombo() {
		// look at the framelist of selected attack/vertex and fill the combobox
		cmb_frame.removeAll();
		Track trk=null;
		if(dataLoaded)
			trk=GetCurTrack();
				
		
		if ((dataLoaded) && (trk!=null)) {			
			Frame f = trk.keyFrame;
			while (f != null) {
				cmb_frame.add("<frame=" + f.time + ">");
				f = f.next;
			}
		}
		if(cmb_frame.getItemCount()==0)//empty
			cmb_frame.add("No frames!");
	}
	public void Render(){
		//complete function to handle all needed render calls
		UpdateAnim();
		shell.redraw();
		slideFrame.redraw();
		RenderHitBox();
		root.renderOneFrame();
	}
	public void UpdateAnim() {
		if(animState!=null){
			float aLen = animState.getLength();
			animState.setTimePosition(aLen * frameSel);
			
			int aSel=cmb_attack.getSelectionIndex();
			if(fighterID==1)//serenity
				if((aSel>3)&&(aSel<8))//'B' attacks
					CamFocusSword();
				else if((oriX!=0)&&(oriY!=0))
					UnfocusSword();
			if (dataLoaded)
				RenderHitBox();	
			root.renderOneFrame();
		}
	}
	public void FillKeyframes(PaintEvent e){
		if(dataLoaded){
			int aSel=cmb_attack.getSelectionIndex();
			int tSel=0;
			if(dh.attack[aSel].trType>0)
				tSel=cmb_track.getSelectionIndex();
			Track trk = dh.attack[aSel].GetTrack(tSel);
			if (cmb_frame.getItem(0).compareTo("No frames!") != 0) {
				
				int x = 10;
				int y = 5;
				Color c3 = new Color(e.display, rgb_kf);
				Color s3 = new Color(e.display, rgb_kfSel);

			
				Color p3 = new Color(e.display, rgb_kfBack);
				Color q3 = new Color(e.display, rgb_kfBackSel);
				
			
				float frameNum;
				int i = 0;
				e.gc.setForeground(c3);// set the pen color
				for (Frame f = trk.keyFrame;f!=null;f=f.next) {
					if(i==frameInd)
						e.gc.setForeground(s3);
					else
						e.gc.setForeground(c3);
					frameNum = f.time;
					x = (int) (frameNum * (slideFrame.getSize().x-27)) + 13;// determine where to mark
					
					e.gc.drawLine(x, y + 50, x, y +5);// mark
					
					e.gc.drawRectangle(x-5, y-5, 10, 10);
					i++;
				}
				
				//draw priorities

				
				e.gc.setForeground(p3);// set the pen color
				int ind=0;
				int xNext=1;
				int priVal=0;
				for (Frame f = trk.keyFrame; f!=null; f=f.next) {
					if(ind==priSel)
						e.gc.setForeground(p3);
					else
						e.gc.setForeground(q3);
					
					frameNum = f.time;//to scale to slideframe's inset
					priVal=(int)f.value;
					if(priVal<0)
						priVal=0;
					if(priVal>50)
						priVal=50;
					priVal=30-priVal;
					if(f.next==null)
						xNext = (slideFrame.getSize().x-27) + 13;
					else
						xNext= (int) (f.next.time * (slideFrame.getSize().x-27)) + 13;
					x = (int) (frameNum * (slideFrame.getSize().x-27)) + 13;// determine where to mark
					e.gc.drawLine(x, y + priVal, xNext, y +priVal);// mark
					
					
					ind++;
				}
				s3.dispose();
				c3.dispose();
				p3.dispose();
				q3.dispose();
				e.gc.dispose();
			}
		}
	}
	private boolean CheckVtxBounds(float fSel, int aSel, int vSel) {
		if(GetCurTrack()!=null)
			return GetCurTrack().CheckVtxBounds(fSel, aSel, vSel);
		else
			return false;
	}
	public Vertex GetVertAtFrame(int aSel, int vSel, float fSel){
		//returns the vertex at the specified index
		//will return a vertex from another track if multitrack is enabled
		if(GetCurTrack()!=null)
			return GetCurTrack().GetVertAtFrame( vSel, fSel);
		else 
			return null;
	}
	public void MoveMultipleVerts(int aSel, float fSel, int hbIndStart2, int hbIndEnd2, float f, float g) {
		if(GetCurTrack()!=null)
			dh.MoveMultipleVerts(GetCurTrack(), fSel, hbIndStart2, hbIndEnd2, f, g);
	}
	public void FillVtxCombo() {
		String toAdd;
		int aSel = cmb_attack.getSelectionIndex();
		int dVerts = GetCurTrack().VertSize();
		cmb_vertex.removeAll();
		if(dVerts==0)
			cmb_vertex.add("No verts!");
		else		
			for (int i = 0; i < dVerts; i++) {
				toAdd = "Vertex " + i;
				cmb_vertex.add(toAdd);
			}
		cmb_vertex.select(0);

			
		RenderHitBox();
	}
	
	//~Listeners
	class CommitChangeListener implements FocusListener {
		public void focusGained(FocusEvent e) {
		}
		public void focusLost(FocusEvent e) {
			if (dataLoaded) 
				CommitChange();
		}
	}
	class EnterListener implements KeyListener {
		public void keyPressed(KeyEvent k) {
			System.out.println(k.keyCode);
			if(k.keyCode == 13){ //KeyEvent.VK_ENTER
				CommitChange();
			}
		}
		public void keyReleased(KeyEvent arg0) {
			//nothing, don't care		
		}		
	}
	private class FramePaintListener implements PaintListener {
		// Listener to paint green keyframe markers over the slideframe
		public void paintControl(PaintEvent e) {
			FillKeyframes(e);
		}
	}
	
 	class mainMenuListener implements SelectionListener {
		public void widgetSelected(SelectionEvent s) {
			if(dataLoaded){
				if(s.getSource() == m_viewWgt)
					SetDisplay(WEIGHT);
				else if(s.getSource()==m_viewDmg)
					SetDisplay(DAMAGE);
				else if(s.getSource()==m_viewDefault)
					SetDisplay(DEFAULT);
				else if(s.getSource()==m_viewZoomIn)
					Zoom(0.75f);
				else if(s.getSource()==m_viewZoomOut)
					Zoom(1.2f);
				else if(s.getSource()==m_fileSave)
					SaveFile();
				else if(s.getSource()==m_fileSaveAs)
					SaveFileAs();
				else if(s.getSource()==m_editUndo)
					Undo();
				else if(s.getSource()==m_editRedo)
					Redo();
				else if(s.getSource()==m_trackImport)
					ImportTrack();
				else if(s.getSource()==m_trackCopy)
					CopyTrack();
				else if(s.getSource()==pMenuAddVPoint)
					PointToVertex();
				else if(s.getSource()==pMenuAddVert)
					InsertVertex();
				else if(s.getSource()==pMenuAddHB)
					AddHitbox();
				else if(s.getSource()==pMenuDeleteVertex)
					DeleteVertex();
				else if(s.getSource()==pMenuDeleteHitbox)
					DeleteHitbox();
				else if(s.getSource()==pMenuStandardizeHitbox)
					StandardizeHitBox();
				else if(s.getSource()==pMenuInvertHitbox)
					InvertHitbox();
				else if(s.getSource()==pMenuSetCentre)
					SetCentre();
				else if(s.getSource()==pMenuPaste)
					Paste();
				else if(s.getSource()==pMenuCopyVert)
					CopyVertex();
				else if(s.getSource()==sMenuAddPriority)
					AddPriority();
				}
			if(s.getSource()==m_fileExit)
				Quit();//quit
			else if(s.getSource()==m_fileOpen){
				dataLoaded=false;
				FileRead();
				FillVtxCombo();
				FillTrackCombo();
				if(cmb_type.getSelectionIndex()>0)
					cmb_track.setEnabled(true);
				else
					cmb_track.setEnabled(false);
				SetAttack();}
		}

		private void ImportTrack() {
			int tSel = cmb_track.getSelectionIndex();
			int aSel = cmb_attack.getSelectionIndex();
			Track trk = GetCurTrack();
			int nTr = dh.attack[aSel].GetNumTracks();
			ImportTrackDlg imTr= new ImportTrackDlg(shell, nTr, tSel);
			int trCopy = imTr.open();
			Track copyTr = dh.attack[aSel].GetTrack(trCopy);
			int tId = trk.GetId();
			trk = new Track(copyTr);//clone from user selected track
			trk.SetId(tId);
			if(tSel>0)
				copyTr = dh.attack[aSel].GetTrack(tSel-1);
			else
				copyTr=dh.attack[aSel];
			copyTr.next = trk;
			shell.redraw();
			FillFrameCombo();
			slideFrame.redraw();
			
		}

		public void InvertHitbox() {
			int aSel = cmb_attack.getSelectionIndex();
			SelectEntireHitbox(hitboxSelect);
			Track trk = GetCurTrack();
			dh.Commit();
			int j = hbIndEnd;
			boolean iBrk, jBrk;
			Vertex holder;
			for(int i =hbIndStart; i < j;i++){
				holder = trk.vertArr[i];
				iBrk=trk.vertArr[i].getBreak();
				jBrk=trk.vertArr[j].getBreak();
				trk.vertArr[i]=trk.vertArr[j];
				trk.vertArr[j]=holder;
				trk.vertArr[i].SetBreak(iBrk);
				trk.vertArr[j].SetBreak(jBrk);
				
				//adjust outside vertex pointers
				for(int k = 0; k < trk.VertSize();k++)
					if(trk.vertArr[k].vPtr==i)
						trk.vertArr[k].xLoc=j;
					else if(dh.attack[aSel].vertArr[k].vPtr==j)
						trk.vertArr[k].xLoc=i;
				j--;
			}
			RenderHitBox();
			
		}
		private void StandardizeHitBox() {
			int aSel = cmb_attack.getSelectionIndex();
			SelectEntireHitbox(hitboxSelect);
			dh.Commit();
			Vertex v, o;
			for(int i = hbIndStart;i<=hbIndEnd;i++){
				o=GetVertAtFrame(aSel, i, frameSel);
				v=dh.attack[aSel].vertArr[i];
				if(v.vPtr<0){
					while(v!=null){
						if(v.keyframe!=o.keyframe){
							dh.attack[aSel].vertArr[i].SetVertex(v.keyframe.time, o.xLoc, o.yLoc);
							dh.attack[aSel].vertArr[i].SetWgt(v.keyframe.time, o.wgt);
							dh.attack[aSel].vertArr[i].SetMag(v.keyframe.time, o.mag);
							dh.attack[aSel].vertArr[i].SetDmg(v.keyframe.time, o.dmg);
							dh.attack[aSel].vertArr[i].SetDir(v.keyframe.time, o.dir);
						}
						v=v.next;
						
					}
				}
			}			
		}
		private void DeleteHitbox() {
			if(dataLoaded){
				int aSel = cmb_attack.getSelectionIndex();
				SelectEntireHitbox(hitboxSelect);
				dh.Commit();
				for(int i = hbIndStart;i<=hbIndEnd;i++){
					dh.DeleteVertex(aSel, hbIndStart);
					
					FlushVertex(dh.attack[aSel].VertSize() - 1);
				}
				FillVtxCombo();
				FillFrameCombo();
				RenderHitBox();
			}
			
		}
		public void widgetDefaultSelected(SelectionEvent s) {
			widgetSelected(s);
		}
	}
	class NumOnlyTextListener implements VerifyListener{
		public void verifyText(VerifyEvent k) {
			switch (k.keyCode) {
			case SWT.BS:           // Backspace  
            case SWT.DEL:          // Delete  
            case SWT.HOME:         // Home  
            case SWT.END:          // End  
            case SWT.ARROW_LEFT:   // Left arrow  
            case SWT.ARROW_RIGHT:  // Right arrow
            case 46:			   // '.' key      
            case 0:				   //\0 null key
                return;  
        }  
			 if (!Character.isDigit(k.character)) {  
		            k.doit = false;  // disallow the action  
		        }  
		}
		
	}
	class SlideMoveListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			//int aSel = cmb_attack.getSelectionIndex();
			if( (dataLoaded)&&(cmb_frame.getText().compareTo("No frames!")!=0) ){
				if (cmb_frame.getText().contains("frame")) {
					String fStr = cmb_frame.getText().substring(7,cmb_frame.getText().length() - 1);
					int fNum = (int) (Float.parseFloat(fStr) * (float) SCALE_EPS);
					slideFrame.setSelection(fNum);
					float frameCheck=frameSel;
					frameSel=fNum/SCALE_EPS;
					FrameCheck(Float.parseFloat(fStr));
					RenderHitBox();
					UpdateAnim();

					shell.redraw();
					
					
					
				}
			}
			else if(dataLoaded){
				int fNum=0;
				Track trk = GetCurTrack();
				for(Frame f = trk.keyFrame;f!=null;f=f.next){
					fNum = (int) (f.time * (float) SCALE_EPS);
					if(frameSel==fNum){
						txt_pri.setText(Float.toString(f.value));
					}
				}
			}
		}
	}
	
	//menu functions
	public void SetDisplay(int opt) {
		displayMode = opt;
		RenderHitBox();
	}

	
	public void Paste() {
		int type = copy.GetContentType();
		if(type==copy.VERTEX){
			Vertex ins = InsertVertex();
			for(Vertex v = copy.GetVert(); v!=null;v=v.next){
				ins.xLoc = v.xLoc;
				ins.yLoc = v.yLoc;
				ins.dir = v.dir;
				ins.mag=v.mag;
				ins.wgt=v.wgt;
				ins.keyframe=v.keyframe;
				ins.dmg = v.dmg;
				ins=ins.next;
			
			}
			
		}else if(type==copy.TRACK){
			Track trk = GetCurTrack();
			if(trk.VertSize()!=0){
				//int conf = 0;//
			//	JOptionPane.showConfirmDialog(this,"Pasting a track will destroy the existing track. Continue?","Dr00 sux", JOptionPane.OK_CANCEL_OPTION);
				//if(conf!=JOptionPane.OK_OPTION)
					return;
					
			//because none of this shows over top of the window.
			}
				int aSel = cmb_attack.getSelectionIndex();
				int tSel = cmb_track.getSelectionIndex();
				if(trk.trType==0){//normal track
					dh.attack[aSel]= new Track(copy.GetTrack());
					trk = dh.attack[aSel];
				}else{
					Track nTrk = trk.next;
					Track lTrk = trk.last;
					trk = new Track(copy.GetTrack());
					trk.next = nTrk;
					if(nTrk!=null)
						nTrk.last = trk;
					trk.last = lTrk;
					if(lTrk!=null)
						lTrk.next=trk;
				}
				FillVtxCombo();
				FillFrameCombo();
			}
	}
	public void CopyTrack() {
		Track trk = GetCurTrack();
		if(trk!=null){
			copy.Copy(trk);
			pMenuPaste.setText(CopyHelper.TRTEXT);
			pMenuPaste.setEnabled(true);
		}
	}

	public void CopyVertex() {
		int vSel = cmb_vertex.getSelectionIndex();
		if(vSel!=-1)
		{
			Track trk = GetCurTrack();
			copy.Copy(trk.GetVertAtFrame(vSel, frameSel));
			pMenuPaste.setText(CopyHelper.VTEXT);
			pMenuPaste.setEnabled(true);
		}
	}

	public void Zoom(float f) {
		if(f==1.f){
			camDist=70;
			camHgt=20;
		}else if((camDist*f>22.f)&&(camDist*f<120.f)){//don't zoom in or out too far
			camDist=camDist*f;
			camHgt=camHgt*f;
		}
		camera.setPosition(0, camHgt, camDist);
		RenderHitBox();
		Render();
	}

	public void SetCentre() {
		Track trk = GetCurTrack();
		if((dataLoaded)&&(FrameCheck(frameSel))){
			dh.Commit();
			trk.SetCentre(frameSel, xCursor, yCursor);
			RenderHitBox();
		}
		
	}

	public void CommitChange() {
		int vSel = cmb_vertex.getSelectionIndex();
		Track trk = GetCurTrack();
		Color fb = new Color(display, rgb_feedback);
		if((trk.VertSize()>0)&& (FrameCheck(frameSel))) {
			
			if((vSel>=0)&&(txt_xPos.getText().length()>0)&&(txt_yPos.getText().length()>0)){
				trk.vertArr[vSel].SetVertex(frameSel,Float.parseFloat(txt_xPos.getText()),Float.parseFloat(txt_yPos.getText()));
				txt_xPos.setBackground(fb);
				txt_yPos.setBackground(fb);
			}
			if(txt_dir.getText().length()>0){
				trk.vertArr[vSel].SetDir(frameSel, Float.parseFloat(txt_dir.getText())*(float)Math.PI/180.f);
				txt_dir.setBackground(fb);
			}
			if(txt_mag.getText().length()>0){
				trk.vertArr[vSel].SetMag(frameSel, Float.parseFloat(txt_mag.getText()));
				txt_mag.setBackground(fb);
			}
			if(txt_dmg.getText().length()>0){
				trk.vertArr[vSel].SetDmg(frameSel, Float.parseFloat(txt_dmg.getText()));
				txt_dmg.setBackground(fb);
			}
			if(txt_wgt.getText().length()>0){
				trk.vertArr[vSel].SetWgt(frameSel, Float.parseFloat(txt_wgt.getText()));
				txt_wgt.setBackground(fb);
			}
			
				tmr_feedback.schedule(new FeedbackCaller(), feedbackTime);
		}
		for (Frame f = trk.keyFrame;f!=null;f=f.next){
			if(f.time==frameSel){
				if(txt_pri.getText().length()>0)
					f.value= Float.parseFloat(txt_pri.getText());
				else
					f.value = 5;
				
			}
			
		}
	
	RenderHitBox();
		
	}

	public void Quit() {
		cleanup();
		System.exit(0);
		
	}
	class FeedbackCaller extends TimerTask{
		public void run(){
			display.syncExec(new Feedback());
		}
	}
	class Feedback extends TimerTask {
	    //times member represent calling times.
	    private int times = 0;
	 
	 
	    public void run() {
	        
	        if (times == 0) {
	            FrameCheck(frameSel);
	            shell.redraw();
	            times=1;
	        } else {
	            //Stop Timer.
	            this.cancel();
	            tmr_feedback.cancel();
	            tmr_feedback.purge();
	        }
	    }
	}
	
	
	class AutoSave extends TimerTask {
	    //times member represent calling times.

	 
	 
	    public void run() {
	        
	      AutoSaveFile();
	    }
	}

	public void SaveFile(){
		if (dataLoaded && (fileLoaded != null)) {
			StringBuffer output = new StringBuffer();
			if(fighterID==1)
			 output = dh.SaveFile("serenity");
			else if(fighterID==2)
				 output = dh.SaveFile("yara");
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileLoaded));
				if((fileLoaded.length()<7)||(fileLoaded.substring(fileLoaded.length()-7).compareTo(".script")!=0))
					fileLoaded = fileLoaded + ".script";//force '.script' suffix to file
				out.write(output.toString());
				out.close();
			} catch (IOException e) {
			}
			tmr_feedback.schedule(new FeedbackCaller(), feedbackTime);
		}
	}
	public void AutoSaveFile(){
		if (dataLoaded && (fileLoaded != null)) {
			StringBuffer output = new StringBuffer();
			String fileName= fileLoaded.substring(fileLoaded.length()-5);
			if((fileLoaded.length()>5)&&(fileLoaded.substring(fileLoaded.length()-5).compareTo("_auto")==0))
				fileName=fileLoaded;
			else
				fileName = fileLoaded + "_auto";
			if(fighterID==1)
			 output = dh.SaveFile("serenity");
			else if(fighterID==2)
				 output = dh.SaveFile("yara");
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
				if((fileLoaded.length()<7)||(fileLoaded.substring(fileLoaded.length()-7).compareTo(".script")!=0))
					fileLoaded = fileLoaded + ".script";//force '.script' suffix to file
				out.write(output.toString());
				out.close();
			} catch (IOException e) {
			}
			tmr_autosave.schedule(new AutoSave(), autosaveInterval);
		}
	}
	
	
	public void SaveFileAs(){
		FileDialog dialog = new FileDialog(shell, SWT.NULL);
		String path = dialog.open();
		if (path != null) {
			if (dataLoaded && (fileLoaded != null)) {
				StringBuffer output= new StringBuffer();
				if(fighterID==1)
					 output = dh.SaveFile("serenity");
				else if(fighterID==2)
					 output = dh.SaveFile("yara");
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(path));					
					out.write(output.toString());
					out.close();
					fileLoaded = path;
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void FileRead() {
		//opens and parses a file to be read into the editor
		boolean noCentreFlag=false;
		cmb_frame.removeAll();
		StringBuffer strFile;
		FileDialog dialog = new FileDialog(shell, SWT.NULL);
		FileInputStream fis = null;
		int moveSel = 0;
		float frameSel = 0.0f;
		Frame keyFrame = null;
		int track = 0;
		String path = dialog.open();
		if (path != null) {
			File file = new File(path);
			if (file.isFile()) {
				try {
					fis = new FileInputStream(file.getAbsolutePath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				fileLoaded = path;
				int ch = 1;
				String sNum;
				int vInd = 1;
				strFile = new StringBuffer("");
				try {
					while (ch != -1) {
						strFile.delete(0, strFile.length());
						while (((ch = fis.read()) != '\n') && (ch != '\r')&& (ch != -1))
							strFile.append((char) ch);
						if ((strFile.length() > 7)
								&& (strFile.substring(0, 7).compareTo("<model=") == 0)) {
							sNum = strFile.substring(7,strFile.length() - 1);
							if (sNum.compareTo("serenity") == 0){
								fighterID=1;
								LoadPlayer("Serenity.mesh"); }
							else if (sNum.compareTo("yara") == 0){
								LoadPlayer("Yara.mesh"); 
								fighterID=2;}
						}
						if (strFile.toString().compareTo("</frame>") == 0) {
							// Do nothing
						}
						if (strFile.toString().compareTo("<NA>") == 0)
							moveSel = 0;// Neutral A
						if (strFile.toString().compareTo("<SA>") == 0)
							moveSel = 1;// Side A
						if (strFile.toString().compareTo("<DA>") == 0)
							moveSel = 2;// Down A
						if (strFile.toString().compareTo("<UA>") == 0)
							moveSel = 3;// Side A
						if (strFile.toString().compareTo("<NB>") == 0)
							moveSel = 4;// Neutral B
						if (strFile.toString().compareTo("<SB>") == 0)
							moveSel = 5;// Side B
						if (strFile.toString().compareTo("<DB>") == 0)
							moveSel = 6;// Down B
						if (strFile.toString().compareTo("<UB>") == 0)
							moveSel = 7;// Side B
						if (strFile.toString().compareTo("<NC>") == 0)
							moveSel = 8;// Neutral C
						if (strFile.toString().compareTo("<SC>") == 0)
							moveSel = 9;// Side C
						if (strFile.toString().compareTo("<DC>") == 0)
							moveSel = 10;// Down C
						if (strFile.toString().compareTo("<UC>") == 0)
							moveSel = 11;// Side C
						if (strFile.toString().compareTo("<DATK>") == 0)
							moveSel = 12;// Dash
						if (strFile.toString().compareTo("<NAIR>") == 0)
							moveSel = 13;// Neutral Air
						if (strFile.toString().compareTo("<FAIR>") == 0)
							moveSel = 14;// Forward Air
						if (strFile.toString().compareTo("<BAIR>") == 0)
							moveSel = 15;// Back Air
						if (strFile.toString().compareTo("<DAIR>") == 0)
							moveSel = 16;// Down Air
						if (strFile.toString().compareTo("<UAIR>") == 0)
							moveSel = 17;// Up Air
						if(fighterID==1){//serenity
							if (strFile.toString().compareTo("<SNAIR>") == 0)
								moveSel = 18;// Sword Neutral Air
							if (strFile.toString().compareTo("<SFAIR>") == 0)
								moveSel = 19;// Sword Forward Air
							if (strFile.toString().compareTo("<SBAIR>") == 0)
								moveSel = 20;// Sword Back Air
							if (strFile.toString().compareTo("<SDAIR>") == 0)
								moveSel = 21;// Sword Down Air
							if (strFile.toString().compareTo("<SUAIR>") == 0)
								moveSel = 22;// Sword Up Air
						}
						
						if ((strFile.length() > 8)&& (strFile.substring(0, 8).compareTo("<vertex=") == 0)) {
							sNum = strFile.substring(8,strFile.length() - 2);
							int iNum = Integer.parseInt(sNum);
							dh.attack[moveSel].GetTrack(track).SetVertNumber(iNum); // record the number of vertices
						}
						if ((strFile.length() > 6)&& (strFile.substring(0, 7).compareTo("<multi>") == 0)) {
							dh.attack[moveSel].trType=1;
							track = 0;
						}
						if ((strFile.length() > 8)&& (strFile.substring(0, 8).compareTo("<centre=") == 0)) {
							sNum = strFile.substring(8,strFile.length() - 1);
							String sSplitNum[] = sNum.split(",");
							float iX = Float.parseFloat(sSplitNum[0]);
							float iY = Float.parseFloat(sSplitNum[1]);
							if(keyFrame==null)//avoid old format where center comes before the frame
								noCentreFlag=true;
							if(!noCentreFlag)
								dh.attack[moveSel].GetTrack(track).SetCentre(keyFrame, iX, iY);
						}
						if ((strFile.length() > 6)&& (strFile.substring(0, 7).compareTo("<paral>") == 0)) {
							dh.attack[moveSel].trType=2;
							track = 0;
						}
						if ((strFile.length() > 7)&& (strFile.substring(0, 7).compareTo("<track ") == 0)) {
							sNum = strFile.substring(0,strFile.length() - 1);
							String sSplitNum[] = sNum.split(" ");
							track = Integer.parseInt(sSplitNum[1].substring(0, sSplitNum[1].length()));
							if(track>0)
								dh.attack[moveSel].GetTrack(track-1).addTrack();
						}
						if ((strFile.length() > 7)&& (strFile.substring(0, 8).compareTo("</track>") == 0)) {
							track = 0;//reset to proper position in case this track is last.
						}
						if ((strFile.length() > 10)&& (strFile.substring(0, 10).compareTo("<priority=") == 0)) {
							sNum = strFile.substring(10,strFile.length() - 1);
							if(!sNum.contains("=")){//holdover to import artifact files from previous version
							float iNum = Float.parseFloat(sNum);
							keyFrame.value=iNum;}
						}
						if ((strFile.length() > 7)&& (strFile.substring(0, 7).compareTo("<break=") == 0)) {
							sNum = strFile.substring(7,strFile.length() - 1);
							int bNum = Integer.parseInt(sNum);
							dh.attack[moveSel].GetTrack(track).SetBreak( bNum, true); 
						}
						if ((strFile.length() > 7)&& (strFile.substring(0, 7).compareTo("<frame=") == 0)) {
							sNum = strFile.substring(7,strFile.length() - 1);
							// only add if current attack is selected
							frameSel = Float.parseFloat(sNum); // record the frame
							keyFrame = new Frame(frameSel);
							System.out.println("Frame:" + frameSel);
							dh.attack[moveSel].GetTrack(track).AppendKeyFrame( keyFrame);
						}
						if ((strFile.length() > 5)&& (strFile.substring(0, 4).compareTo("<vtx") == 0)) {
							sNum = strFile.substring(4,strFile.length() - 2);
							String sSplitNum[] = sNum.split("=");
							vInd = Integer.parseInt(sSplitNum[0]);
							sNum = sSplitNum[1];
							if ((sNum.length() > 3)&& (sNum.substring(0, 3).compareTo("vtx") == 0)) { // point to existing vtx
								sNum = sNum.substring(3);
								int to = Integer.parseInt(sNum);
								dh.attack[moveSel].GetTrack(track).SetVertex( keyFrame, vInd, 0,0);
								dh.PointToVertex(moveSel, vInd, to);
							} else { // make new vertex
								String s2SplitNum[] = sSplitNum[1].split(",");
								float iX = Float.parseFloat(s2SplitNum[0]);
								float iY = Float.parseFloat(s2SplitNum[1]);
								System.out.println(moveSel +", " +vInd);
								dh.attack[moveSel].GetTrack(track).SetVertex(keyFrame, vInd,iX,iY);
							}
						}
						if ((strFile.length() > 3)&& (strFile.substring(0, 3).compareTo("mag") == 0)) {
							sNum = strFile.substring(3, strFile.length());
							String sSplitNum[] = sNum.split(" ");					
							vInd = Integer.parseInt(sSplitNum[0]);
							float iM = Float.parseFloat(sSplitNum[1]);
							dh.attack[moveSel].GetTrack(track).SetVertexMag(frameSel, vInd, iM); // record the number of vertices
							
						}
						if ((strFile.length() > 4)&& (strFile.substring(0, 3).compareTo("wgt") == 0)) {
							sNum = strFile.substring(3, strFile.length());
							String sSplitNum[] = sNum.split(" ");
							vInd = Integer.parseInt(sSplitNum[0]);
							float iM = Float.parseFloat(sSplitNum[1]);
							dh.attack[moveSel].GetTrack(track).SetVertexWgt(frameSel, vInd, iM); // record the number of vertices
							
						}
						if ((strFile.length() > 4)&& (strFile.substring(0, 3).compareTo("dmg") == 0)) {
							sNum = strFile.substring(3, strFile.length());
							String sSplitNum[] = sNum.split(" ");
							vInd = Integer.parseInt(sSplitNum[0]);
							float iM = Float.parseFloat(sSplitNum[1]);
							dh.attack[moveSel].GetTrack(track).SetVertexDmg( frameSel, vInd, iM); // record the number of vertices
							
						}
						if ((strFile.length() > 4)&& (strFile.substring(0, 3).compareTo("dir") == 0)) {
							sNum = strFile.substring(3, strFile.length());
							String sSplitNum[] = sNum.split(" ");			
							vInd = Integer.parseInt(sSplitNum[0]);
							float iM = Float.parseFloat(sSplitNum[1]);
							dh.attack[moveSel].GetTrack(track).SetVertexDir(frameSel, vInd, iM); // record the number of vertices
						}
					}
					dataLoaded = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (cmb_frame.getItemCount() == 0)
				cmb_frame.add("No frames!");
		}
	}	
	private void FlushVertex(int vInd) {
		// flushes the vertex from the stage so it is no longer drawn to screen
		if (sceneManager.hasManualObject("vCirc" + vInd))
			sceneManager.destroyManualObject("vCirc" + vInd);
		if (sceneManager.hasManualObject("vTri" + vInd))
			sceneManager.destroyManualObject("vTri" + vInd);
		if (sceneManager.hasManualObject("vArrow" + vInd))
			sceneManager.destroyManualObject("vArrow" + vInd);
	}
	protected void PointToVertex() {
		if (dataLoaded) {
			int vSel = cmb_vertex.getSelectionIndex();
			pointVertFrom = vSel;
			pointVertTo = vSel;
		}
	}
	private void SelectVertex(int atkSel, int vertSel) {
		if (dataLoaded) {
			cmb_vertex.select(vertSel);
			txt_xPos.setText(String.valueOf(GetVertAtFrame(atkSel, vertSel,frameSel).xLoc));
			txt_yPos.setText(String.valueOf(GetVertAtFrame(atkSel, vertSel,frameSel).yLoc - dh.HGT_DIV2));
			txt_wgt.setText(String.valueOf(GetVertAtFrame(atkSel, vertSel,frameSel).wgt));
			txt_mag.setText(String.valueOf(GetVertAtFrame(atkSel, vertSel,frameSel).mag));
			txt_dir.setText(String.valueOf(GetVertAtFrame(atkSel, vertSel,frameSel).dir*180.f/Math.PI));
			txt_dmg.setText(String.valueOf(GetVertAtFrame(atkSel, vertSel,frameSel).dmg));
			int hitBFinder = 0;
			for (int i = 0; i < dh.attack[atkSel].VertSize(); i++) {
				if (i == vertSel)
					i = dh.attack[atkSel].VertSize();// force exit
				else if (GetVertAtFrame(atkSel, i, 0).getBreak()) // reached the end of a hitbox
					hitBFinder++;
			}// hitBFinder should now containthe index of the hitbox to select
			SelectHitBox(hitBFinder);
			
			RenderHitBox(); // update changes to screen
		}
	}
	protected void DeleteVertex() {
		int aSel = cmb_attack.getSelectionIndex();
		int vSel = cmb_vertex.getSelectionIndex();
		Track trk = GetCurTrack();
		if (dataLoaded) {
			dh.Commit(); //save instance for undo
			trk.DeleteVertex(vSel);
			FillVtxCombo();
			FillFrameCombo();
			FlushVertex(dh.attack[aSel].VertSize());
		}
		if(vSel == trk.VertSize())
			vSel--;
		if(GetVertAtFrame(aSel, vSel, frameSel).getBreak())
			vSel++;
	}
	protected void AddHitbox() {

		Track trk = GetCurTrack();
		int sCount=slideFrame.getSelection();
		if (dataLoaded) {
			dh.Commit();
			trk.NewHitBox(xCursor, yCursor);
			Frame kf = trk.GetKeyFrame( frameSel);
			if(kf==null){//need to add keyframe
				AddKeyframe((float) sCount / SCALE_EPS);
				kf = trk.GetKeyFrame( frameSel);}
			if(trk.VertSize()==1){//just added new hitbox
				trk.vertArr[0].SetVertex(kf, xCursor, yCursor);
				slideFrame.redraw();
			}
			FillVtxCombo();
			FillFrameCombo();
			SelectHitBox(trk.FaceSize() - 1);
		}
	}
	
	private void AddPriority() {
		Track trk = GetCurTrack();
		double prPos =  Math.floor(((float)frX-13)/(slideFrame.getSize().x+27)*1000);		
		prPos=prPos/1000.0;
		trk.SetPriority((float)prPos, 1);//default to 1; can adjust later
		slideFrame.redraw();		
	}
	protected Vertex InsertVertex() {
		int aSel = cmb_attack.getSelectionIndex();
		int vSel = cmb_vertex.getSelectionIndex();
		int hbCount = 0; // counts vertices in selected hitbox
		Vertex retV = null;
		float hbX = 0;
		float hbY = 0;
		int hbAt = 0;
		int hbStart = -1;
		Vertex v = null;
		Track trk = GetCurTrack();
		if (!CheckVtxBounds(frameSel, aSel, vSel)) {
			Warning("Cannot add vertex to concave hitbox");
			return null;
		}
		if (dataLoaded) 
			if(trk.VertSize() > 0) {
				dh.Commit();

				for (int i = 0; i <trk.VertSize(); i++) { // iterate through verts until selected hitbox is represented
					if (hbAt == hitboxSelect) { // we are at the current hitbox
						hbX += trk.GetVertAtFrame(i, frameSel).xLoc;
						hbY += trk.GetVertAtFrame(i, frameSel).yLoc;
						hbCount++;
						if (hbStart < 0)
							hbStart = i;
					}
					if (trk.GetVertAtFrame(i, frameSel).getBreak()) // update to next hitbox
						hbAt++;
				}
				if (hbStart < 0)
					return null;// nothing selected get out before it crashes.
							//take summed results; average will define a center point for the selected hitbox
				hbY = hbY / (float) hbCount;
				hbX = hbX / (float) hbCount;
				float[] angArr = new float[hbCount];
				float angCur = (float) Math.atan2(hbY - (yCursor + dh.HGT_DIV2),hbX - xCursor); // measure angle from center to cursor
				// fill the array
				float lAng = 0;
				for (int i = 0; i < hbCount; i++) {// iterate through relevant vertices
					v = trk.GetVertAtFrame(i + hbStart, frameSel);
					angArr[i] = (float) Math.atan2(hbY - v.yLoc, hbX - v.xLoc);
				}
				for (int i = 0; i < hbCount; i++) {// iterate through relevant vertices
					if (i == 0)
						lAng = angArr[hbCount - 1];
					else
						lAng = angArr[i - 1];
					if (lAng > angArr[i]) { // special case for angle wraparound
						if ((angCur > lAng) || (angCur < angArr[i])) { // angCur falls outside/ of values
							retV = trk.InsertVertex( i + hbStart, xCursor, yCursor);
							i = hbCount;
						} // force exit
					} else if ((angCur > lAng) && (angCur < angArr[i])) { // angCur falls between values
						retV = trk.InsertVertex(i + hbStart, xCursor, yCursor);
						i = hbCount;// force exit
					}
				}
				// special case for a hitbox of only one vert
				if (hbCount == 1)
					retV = trk.InsertVertex( hbStart, xCursor, yCursor);
			}else{
				if(trk.keyFrame==null)//no frames
					trk.AppendKeyFrame(new Frame(frameSel));
				retV = trk.InsertVertex(0, xCursor, yCursor);
			}
		FillFrameCombo();
		FillVtxCombo();
		return retV;
	}

	

	private void Warning(String string) {
		System.out.println(string);
	}
	private void SelectHitBox(int hbInd) {
		hitboxSelect = hbInd;
		hbIndStart=-1;
		hbIndEnd=-1;
		RenderHitBox();// post results to screen
	}
	public void SelectEntireHitbox(int hbInd){
	//set param to -1 for a sentinel value	
		Track trk= GetCurTrack();
		hbIndStart = trk.VertSize();
		hbIndEnd = 0;
		int curHitInd=0;
		Vertex v;
		if(hbInd>=0)
			hitboxSelect = hbInd;
		for(int i = 0; i < trk.VertSize();i++){
			v = trk.GetVertAtFrame( i, frameSel);
			if(curHitInd==hitboxSelect){
				if(hbIndStart>i)//set on the first instance
					hbIndStart = i;
				hbIndEnd = i;//set here
			}
			if(v.getBreak())			
				curHitInd++;
		}
		
		RenderHitBox();// post results to screen
	}
	public float GetFrameWeight() {
		Vertex v;
		int aSel = cmb_attack.getSelectionIndex();
		int tSel=0;
		float lastFrame, aWgt, fwdFrame;
		aWgt = 0;
		lastFrame = 0;
		fwdFrame = 0;
		if(dh.attack[aSel].trType>0)
			tSel=cmb_track.getSelectionIndex();
		Track trk = dh.attack[aSel].GetTrack(tSel);
		if(trk.VertSize()>0) {
			v =trk.vertArr[0];

			while ((v.next != null) && (frameSel >= v.keyframe.time)) {//cycle through until we arive at the last frame
				lastFrame = v.keyframe.time;
				v = v.next;
			}
			if (trk.vertArr[0].next != null){
				fwdFrame = v.keyframe.time;	//assign forward frame if it exists
				if (lastFrame - fwdFrame == 0) //don't div by 0
					aWgt = 0;
				else
					aWgt = (frameSel - lastFrame) / (fwdFrame - lastFrame); // alpha weight for interpolation
			}else{//only had one frame
				aWgt = 1;
				
			}
		}
		return aWgt;
	}
	public void AddKeyframe(float newFrame) {
		Track trk = GetCurTrack();
	    Frame f = new Frame(newFrame, 0);
	    Float fWgt = GetFrameWeight();
	    trk.InsertFrameAll(f, fWgt);	
		dh.Commit();			
		
						
		
		FillFrameCombo();
		slideFrame.redraw();
	}
	private void DeleteKeyframe(float f) {
		Track trk =GetCurTrack();
		String frameRdr;
		float frameNum;
		if(FrameCheck(f)){
			dh.Commit();
			for(int i = 0; i < cmb_frame.getItemCount();i++){
				frameRdr = cmb_frame.getItem(i).substring(7, cmb_frame.getItem(i).length() - 1);
				frameNum = Float.parseFloat(frameRdr);
				if(frameNum==f){
					trk.RemoveFrameAll(i);
					cmb_frame.remove(i);
					if(cmb_frame.getItemCount()==0)
						cmb_frame.add("No frames!");
					slideFrame.redraw();
				}
			}
		}								
	}
	public boolean FrameCheck(float frame) {
		boolean returnVal = false;
		txt_frame.setText(Float.toString(frame));
		float fVal;
		String sVal;
		int numFrames = cmb_frame.getItemCount();		
		if ((cmb_frame.getItemCount() > 0) && (cmb_frame.getItem(0).compareTo("No frames!") != 0))
			for (int i = 0; i < numFrames; i++) {
				sVal = cmb_frame.getItem(i).substring(7,cmb_frame.getItem(i).length() - 1);
				fVal = Float.parseFloat(sVal);
				if (frame == fVal)
					returnVal = true;
			}
		if (returnVal) {
			txt_dmg.setBackground(new Color(display, rgb_edit));
			txt_dir.setBackground(new Color(display, rgb_edit));
			txt_wgt.setBackground(new Color(display, rgb_edit));
			txt_xPos.setBackground(new Color(display, rgb_edit));
			txt_yPos.setBackground(new Color(display, rgb_edit));
			txt_mag.setBackground(new Color(display, rgb_edit));
		} else {
			txt_dmg.setBackground(new Color(display, rgb_unedit));
			txt_dir.setBackground(new Color(display, rgb_unedit));
			txt_wgt.setBackground(new Color(display, rgb_unedit));
			txt_xPos.setBackground(new Color(display, rgb_unedit));
			txt_yPos.setBackground(new Color(display, rgb_unedit));
			txt_mag.setBackground(new Color(display, rgb_unedit));
		}
		return returnVal;
	}
	public boolean FrameCheck(int frame) {
		boolean returnVal = false;
		txt_frame.setText(Float.toString(frame));
		int iVal;
		String sVal;
		int numFrames = cmb_frame.getItemCount();		
		if ((cmb_frame.getItemCount() > 0) && (cmb_frame.getItem(0).compareTo("No frames!") != 0))
			for (int i = 0; i < numFrames; i++) {
				sVal = cmb_frame.getItem(i).substring(7,cmb_frame.getItem(i).length() - 1);
				iVal = (int)(Float.parseFloat(sVal)*SCALE_EPS);
				if (frame == iVal)
					returnVal = true;
			}
		if (returnVal) {
			txt_dmg.setBackground(new Color(display, rgb_edit));
			txt_dir.setBackground(new Color(display, rgb_edit));
			txt_wgt.setBackground(new Color(display, rgb_edit));
			txt_xPos.setBackground(new Color(display, rgb_edit));
			txt_yPos.setBackground(new Color(display, rgb_edit));
			txt_mag.setBackground(new Color(display, rgb_edit));
		} else {
			txt_dmg.setBackground(new Color(display, rgb_unedit));
			txt_dir.setBackground(new Color(display, rgb_unedit));
			txt_wgt.setBackground(new Color(display, rgb_unedit));
			txt_xPos.setBackground(new Color(display,rgb_unedit));
			txt_yPos.setBackground(new Color(display, rgb_unedit));
			txt_mag.setBackground(new Color(display, rgb_unedit));
		}
		return returnVal;
	}
	
	public void PrintVertex(String mat, IManualObject m, Vertex v) {
		//this function prints one vertex of a 2d polygon.
		//this differs from RenderVertex() in that 
		//RenderVertex() paints a vertex marker on the screen.
		ColourValue c = new ColourValue(1, 1, 1, 1);
		m.position(v.xLoc, v.yLoc + dh.HGT_DIV2, 0);// offset height to origin coordinates
		if (displayMode == WEIGHT) {
			c = new ColourValue(v.wgt, v.wgt, 0, v.wgt);
			m.colour(c);
		} 
		else if (displayMode == DAMAGE) {
			float damLmt = 128.f;
			float dmg = v.dmg;			
			if (dmg > damLmt)
				dmg = damLmt;
			dmg = dmg / damLmt;
			c = new ColourValue(dmg, 1.f - dmg, 0, 1);
			m.colour(c);
		}
	}
	public void RenderHitBox() {
		//start by updating meny visibility
		if(dh.next==null)
			m_editRedo.setEnabled(false);
		else
			m_editRedo.setEnabled(true);
		if(dh.last==null)
			m_editUndo.setEnabled(false);
		else
			m_editUndo.setEnabled(true);
		int aSel = cmb_attack.getSelectionIndex();
		Track trk=null;
		String material = "BaseWhiteNoLighting";
		OperationType oT;
		int n, hitInd, point_index;
		Vertex v;
			trk = GetCurTrack();			
			oT = OperationType.OT_TRIANGLE_FAN;
				n = 0;
				hitInd = 0;

			if ((dataLoaded) &&(btn_animType.getText().compareTo("Other Anims")==0)) {
				if((trk.centre==null)||(trk.centre.keyframe==null)||(trk.centre.keyframe.value==-1))//-1 is a sentinel value for undefined keyframes
					trk.SetCentreFrames();
				v = trk.GetCentre(frameSel);
				RenderVertex(v, -1);//print the centre
				n = trk.VertSize();
				ISceneNode rootSceneNode = sceneManager.getRootSceneNode();
				IManualObject[] tri= new IManualObject[n];

				
					
					if(n>0){//prime the first iteration of work to be done before the loop
						if (sceneManager.hasManualObject("HITBOX0")) // delete last instance
						sceneManager.destroyManualObject("HITBOX0");
					tri[hitInd] = sceneManager.createManualObject("HITBOX0");			
					if (CheckVtxBounds(frameSel, aSel, 0)) //draws as solid for convex polygons
						oT = OperationType.OT_TRIANGLE_FAN;
					else
						oT = OperationType.OT_LINE_STRIP;   //outlines are drawn for invalid, concave polygons
					tri[hitInd].setDynamic(true);
					material = FetchHitboxMat(hitInd);	//choose the mateiral based on options selected
					tri[hitInd].begin(material, oT);
					
					
				}else
					hitInd = -1;
				
				point_index = 0;       //needed for outlines
				for (int i = 0; i < n; i++) {
					if (i == pointVertFrom)
						v = trk.GetVertAtFrame(pointVertTo, frameSel); // replace with preview
					else
						v = trk.GetVertAtFrame(i, frameSel);
					PrintVertex(material, tri[hitInd], v);
					if (oT == OperationType.OT_LINE_STRIP)
						tri[hitInd].index(point_index); // required case for line stip
					point_index++; 
					if (v.vPtr<0)// don't render vertex pointers
						RenderVertex(v, i);
					else
						FlushVertex(i);	
					// properly populate the textfields here
					if (cmb_vertex.getSelectionIndex() == i) {
						txt_xPos.setText(Float.toString(v.xLoc));
						txt_yPos.setText(Float.toString(v.yLoc));
						txt_dmg.setText(Float.toString(v.dmg));
						txt_dir.setText(Float.toString((float) (v.dir*180.f/Math.PI)));
						txt_wgt.setText(Float.toString(v.wgt));
						txt_mag.setText(Float.toString(v.mag));
					}
	
					if ((i != pointVertFrom) && (v.getBreak())) {
						// make sure we don't consider it an end node for point vertex f'n()
						if (oT == OperationType.OT_LINE_STRIP)
							tri[hitInd].index(0); // Rejoins the last point to the 1st
						tri[hitInd].end();
						rootSceneNode.attachObject(tri[hitInd]);
						hitInd++;
						if (sceneManager.hasManualObject("HITBOX" + hitInd)) // delete last instance
							sceneManager.destroyManualObject("HITBOX" + hitInd);
						tri[hitInd] = sceneManager.createManualObject("HITBOX"+ hitInd);
						if (i < n - 1) {
							// check conditions of material for new hitbox
							if (CheckVtxBounds(frameSel, aSel, i + 1))
								oT = OperationType.OT_TRIANGLE_FAN;
							else
								oT = OperationType.OT_LINE_STRIP;
						}
						material = FetchHitboxMat(hitInd);
						tri[hitInd].begin(material, oT);
						point_index = 0;
					}
				}
				if (sceneManager.hasManualObject("HITBOX" + hitInd)&&(n>0)) {// finish last triangle
					if (oT == OperationType.OT_LINE_STRIP)
						tri[hitInd].index(0); // Rejoins the last point to the first.
					if(point_index > 0) //fixes a crash for nonexistant object
						tri[hitInd].end();
					rootSceneNode.attachObject(tri[hitInd]);
				}
			} else
				// nothing drawn; clean up existing objects
				hitInd = -1; // will incriment to 0 before use.
			// clean up abandoned hitboxes and vertices
			int vertCleanCounter = 8;
			while (vertCleanCounter > 0) { // quit if nothing found in last 8
											// iterations
				if (sceneManager.hasManualObject("vCirc" + n)) {
					FlushVertex(n);
					vertCleanCounter = 8;
				}// found a vertex; reset the counter.
				n++;
				vertCleanCounter--;
			}
			hitInd++;
			while (sceneManager.hasManualObject("HITBOX" + hitInd)) {
				sceneManager.destroyManualObject("HITBOX" + hitInd);
				hitInd++;
			}
		
		// vertex rendering
		root.renderOneFrame();
	}
	public void RenderVertex(Vertex v, int ind) {
		float radius = 0.6f;
		float accuracy = 35;
		int point_index=0;
		ISceneNode rootSceneNode = sceneManager.getRootSceneNode();
		if(ind==-1){//sentinel value for the centre
			if (sceneManager.hasManualObject("CENTRE"))
				sceneManager.destroyManualObject("CENTRE");
			IManualObject centre = sceneManager.createManualObject("CENTRE");
			centre.begin("MagArrow", OperationType.OT_TRIANGLE_FAN);
			for (float theta = 0; theta <= 2 * PI; theta += PI / accuracy) {
				centre.position(v.xLoc + radius * (float) Math.cos(theta), v.yLoc
						+ dh.HGT_DIV2 + radius * (float) Math.sin(theta), 0);
				centre.index(point_index++);
			}
			centre.index(0); // Rejoins the last point to the first.
			centre.end();
			rootSceneNode.attachObject(centre);
			return;
		}
		
		FlushVertex(ind);
		IManualObject vCirc = sceneManager.createManualObject("vCirc" + ind);
		
		// see if this vertex needs to be highlighted
		if (ind == cmb_vertex.getSelectionIndex())
			vCirc.begin("HitBoxAllSelect", OperationType.OT_LINE_STRIP);
		else
			vCirc.begin("HitBox", OperationType.OT_LINE_STRIP);

		point_index = 0;
		for (float theta = 0; theta <= 2 * PI; theta += PI / accuracy) {
			vCirc.position(v.xLoc + radius * (float) Math.cos(theta), v.yLoc
					+ dh.HGT_DIV2 + radius * (float) Math.sin(theta), 0);
			vCirc.index(point_index++);
		}
		vCirc.index(0); // Rejoins the last point to the first.
		vCirc.end();
		
		rootSceneNode.attachObject(vCirc);
		// render the arrow
		IManualObject vArrow = sceneManager.createManualObject("vArrow" + ind);
		if (ind == cmb_vertex.getSelectionIndex())
			vArrow.begin("MagArrowSelect", OperationType.OT_LINE_STRIP);
		else
			vArrow.begin("MagArrow", OperationType.OT_LINE_STRIP);
		vArrow.position(v.xLoc, v.yLoc + dh.HGT_DIV2, 0);
		float cosDir = (float) Math.cos(v.dir);
		float sinDir = (float) Math.sin(v.dir);
		float dirX = (float) (v.mag +MAG_OFFSET)* cosDir;
		float dirY = (float) (v.mag +MAG_OFFSET)* sinDir;
		vArrow.position(v.xLoc + dirX, v.yLoc + dirY + dh.HGT_DIV2, 0);
		vArrow.end();
		rootSceneNode.attachObject(vArrow);
		IManualObject vTri = sceneManager.createManualObject("vTri" + ind);
		if (ind == cmb_vertex.getSelectionIndex())
			vTri.begin("HitBoxSelect", OperationType.OT_TRIANGLE_FAN);
		else
			vTri.begin("MagArrow", OperationType.OT_TRIANGLE_FAN);
		vTri.position(v.xLoc + dirX + 2*cosDir, v.yLoc + dirY + 2*sinDir + dh.HGT_DIV2, 0); 
		// cosDir,sinDir added to extend arrow an additional unit length
		float backX = (float) (v.xLoc + dirX);
		float backY = (float) (v.yLoc + dirY + dh.HGT_DIV2);
		float offX = (float) (Math.cos(v.dir + Math.PI / 2.f))/2;
		float offY =  (float) (Math.sin(v.dir + Math.PI / 2.f))/2;
		vTri.position(backX + offX, backY + offY, 0);
		vTri.position(backX - offX, backY - offY, 0);
		vTri.end();
		rootSceneNode.attachObject(vTri);
		
	}

	private String FetchHitboxMat(int ind) {
		String retStr = "";
		if(IsInFrame()){
			if (displayMode == DEFAULT) {
				if (ind == hitboxSelect)
					if(hbIndStart>=0)
						retStr = "HitBoxAllSelect";
					else
						retStr = "HitBoxSelect";
				else
					retStr = "HitBox";
			} else if (displayMode == WEIGHT) {
					retStr = "BaseWhiteNoLighting";
			} else if (displayMode == DAMAGE) {
					retStr = "BaseWhiteNoLighting";
			}
		}else{
			if (displayMode == DEFAULT) {
				if (ind == hitboxSelect)
					retStr = "HitBoxSelectOutFrame";
				else
					retStr = "HitBoxOutFrame";
			} else if (displayMode == WEIGHT) {
					retStr = "HitBoxOutFrame";
			} else if (displayMode == DAMAGE) {
					retStr = "HitBoxOutFrame";
			}
			
		}
		return retStr;
	}
	private boolean IsInFrame() {
		boolean flag = false;
		Frame f;
		Track trk = GetCurTrack();
		float first=0,last=0;
		int aSel = cmb_attack.getSelectionIndex();
		if(trk.keyFrame==null)
			return false;
		if(aSel>=0){
			System.out.println("!!" + dh.attack[aSel].VertSize());
			f = trk.keyFrame;
			first = trk.keyFrame.time;  //first frame in list
			while(f!=null){ //find last frame in list
				last = f.time;
				f=f.next;
			}
			if((first<=frameSel)&&(last>=frameSel))//check against first/last
			flag = true;			
		}
		return flag;
	}
	
	public int IdentifyHitBox(int aInd, int vInd){
		int hbInd = 0;
		for (int i = 0; i < vInd; i++)
			if (GetVertAtFrame(aInd, i, 0.f).getBreak())
				hbInd++;
		return hbInd;
	}
	public void Undo(){
		if((dataLoaded)&&(dh.last!=null)){
			dh.last.next = dh; //mark spot for possible redo
			dh = dh.last;
			FillFrameCombo();
			RenderHitBox();
			slideFrame.redraw();
		}
	}
	public void Redo(){
		if((dataLoaded)&&(dh.next!=null)){
			dh = dh.next;
			FillFrameCombo();
			RenderHitBox();
			slideFrame.redraw();
		}
	}
	public void mouseMove(MouseEvent e) {
		// cursor update
		Track trk = GetCurTrack();
		float rDist;
		float x = 1.0f / canvas.getClientArea().width * e.x;
		float y = 1.0f / canvas.getClientArea().height* e.y;
		IRay ray = new Ray(WithoutNativeObject.I_WILL_DELETE_THIS_OBJECT);
		camera.getCameraToViewportRay(ray, x, y);
		IRaySceneQuery rsq = sceneManager.createRayQuery(ray, 0xFFFFFFFF);
		String strSel;
		rsq.setSortByDistance(true, 10);
		IVector3 rayHit = new Vector3(0, 0, 0);
		IRaySceneQueryResult rsqr = rsq.execute();
		if (rsqr.size() > 0) {
			for (int i = 0; i < rsqr.size(); i++) {
				IRaySceneQueryResultEntry rsqre = rsqr.at(i);
				strSel = rsqre.getmovable().getName();
				if ((strSel.length() > 10)
						&& (strSel.substring(0, 11).compareTo("FieldEntity") == 0)) {// only one case to care about
					strSel = rsqre.getmovable().getName();
					rDist = rsqre.getdistance();
					ray.getPoint(rayHit, rDist); // see where on plane we hit
					xCursor = rayHit.getx();
					yCursor = rayHit.gety() - dh.HGT_DIV2;
				}
			}
		}
		int aSel = cmb_attack.getSelectionIndex();
		int vSel = cmb_vertex.getSelectionIndex();
		float vDist, vMinDist;
		Vertex v;
		
		vMinDist = 100;// initilize to high value
		if( pointVertFrom >= 0) {// in the middle of vertex point selection
			for (int i = 0; i < dh.attack[aSel].VertSize(); i++) {
				v = GetVertAtFrame(aSel, i, frameSel);
				vDist = (xCursor - v.xLoc) * (xCursor - v.xLoc)
						+ (yCursor - v.yLoc) * (yCursor - v.yLoc);
				vDist = (float) Math.sqrt(vDist);
				if ((vDist < vMinDist) && (i != pointVertFrom)
						&& (v.vPtr < 0)&&(IdentifyHitBox( aSel, pointVertFrom)!= IdentifyHitBox(aSel, i))) { // make sure we don't point to itself or a vertex pointer!
					pointVertTo = i;
					vMinDist = vDist;
				}

			}
			RenderHitBox();
		}
		if ((mouseLook) && (FrameCheck(frameSel))) {
			if (moveVert)
				dh.MoveVertex(trk, frameSel, vSel, xCursor, yCursor); // move vertex
			if (moveDir)
				dh.MoveDirectional(trk, frameSel, vSel, xCursor, yCursor); // move arrow
			if(moveHitbox)
				MoveMultipleVerts(aSel, frameSel, hbIndStart, hbIndEnd, xCursor+xOff, yCursor+yOff);
			RenderHitBox(); // call to display changes
		}
		if(moveFrame){
			float dF = ((float)e.x - 13.f)/( (float)slideFrame.getSize().x -27);
			int tSel = cmb_track.getSelectionIndex();
			if(tSel<0)
				tSel=0;
			//float fX = (float)e.x - (int) (frameInd * slideFrame.getSize().x) + 13;// determine where to mark
			dh.MoveFrame(aSel, tSel, fPtr, dF);
			slideFrame.redraw();
			FillFrameCombo();
		}
	}
	

	private Track GetCurTrack() {
		//returns the track currently selected by the controls
		int aSel = cmb_attack.getSelectionIndex();
		int tSel = cmb_track.getSelectionIndex();
		return dh.attack[aSel].GetTrack(tSel);
	}

	public void mouseDoubleClick(MouseEvent e) {
		String strSel;
		int indSel = -1;
		if (dataLoaded) {
			if (e.button == 1) {
				mouseLook = true;
				lastX = e.x;
				lastY = e.y;
				
			}

			float x = 1.0f / canvas.getClientArea().width * e.x;
			float y = 1.0f / canvas.getClientArea().height * e.y;
			IRay ray = new Ray(WithoutNativeObject.I_WILL_DELETE_THIS_OBJECT);
			camera.getCameraToViewportRay(ray, x, y);
			IRaySceneQuery rsq = sceneManager.createRayQuery(ray,0xFFFFFFFF);
			rsq.setSortByDistance(true, 10);
			IRaySceneQueryResult rsqr = rsq.execute();
			IRaySceneQueryResultEntry rsqre;
			if (rsqr.size() > 0) {
				for (int i = 0; i < rsqr.size(); i++) {
					rsqre = rsqr.at(i);
					strSel = rsqre.getmovable().getName();
					if ((strSel.length() > 6) && (strSel.substring(0, 6).compareTo("HITBOX") == 0)){ 
						indSel = Integer.parseInt(strSel.substring(6,strSel.length()));// extract the index from the string
						SelectEntireHitbox(indSel);
					}
				}
				
		ray.delete();
		sceneManager.destroyQuery(rsq);
			}
		}
	}
	public void Deselect(int mBut){
		
		if(mBut==1){
			mouseLook = false;
			xOff=0;
			yOff=0;
			fPtr=null;
			if(moveFrame){

				GetCurTrack().SortFrames();
				FillFrameCombo();
				slideFrame.redraw();
			}
			moveFrame=false;
			moveVert=false;
			moveHitbox=false;
			moveDir=false;
			frameInd=-1;//sentinel value to deselect
			RenderHitBox();
		}
		
	}
	@Override
	public void mouseUp(MouseEvent e) {
			Deselect(e.button);		
	}
	@Override
	public void mouseDown(MouseEvent e) {
		
		
		String strSel;
		int indSel = -1;
		int aSel = cmb_attack.getSelectionIndex();
		if (dataLoaded) {
			
			if(e.button==3){//menu
				if(e.getSource()==canvas){
					if(moveVert||moveHitbox||moveDir){
						Undo();
						moveVert=false;
						moveHitbox=false;
						moveDir=false;
					}
					if(hitboxSelect>=0){
						pMenuDeleteHitbox.setEnabled(true);
						pMenuStandardizeHitbox.setEnabled(true);
						pMenuInvertHitbox.setEnabled(true);
					}
					else{
						pMenuDeleteHitbox.setEnabled(false);
						pMenuStandardizeHitbox.setEnabled(false);
						pMenuInvertHitbox.setEnabled(false);
					}	
				}
			}
			else if (e.button == 1) {//mouselook
				moveVert = false;
				moveDir = false;
				moveHitbox = false;
				moveFrame = false;
				mouseLook = true;
				lastX = e.x;
				lastY = e.y;
				if ((pointVertTo >= 0)&&(IdentifyHitBox( aSel, pointVertFrom)!= IdentifyHitBox(aSel, pointVertTo))) {
					//redundant if pointing to invalid selections is restricted
					//EDIT: Nope,not really, this stops us from making a shitty commit.
					dh.Commit();
					dh.PointToVertex(aSel, pointVertFrom, pointVertTo);
					FlushVertex(pointVertFrom);//clean up newly pointed vertex
					pointVertFrom = -1;
					pointVertTo = -1;
					
				}
				//TODO: warn the user of invalid selection
			}

				if(e.getSource()==slideFrame){
					if(e.button==1){
						mouseLook = false;
						moveDir = false;
						moveVert = false;
						moveHitbox = false;
						String frameRdr;
						float frameNum;
						int pX;
						int i =0;
						Track trk = GetCurTrack();
						for(Frame f =trk.keyFrame;f!=null;f=f.next){
								pX = (int)(f.time   * (slideFrame.getSize().x-27)) + 13;// determine where to mark
								if((e.x>pX-5)&&(e.x<pX+5)&&(e.y>7)&&(e.y<20)){
									dh.Commit();
									moveFrame=true;
								    frameInd = i;//record to move later
								    fPtr = f;
									}
								i++;
							}
						}
					else if(e.button==3){
						frX = e.x;
					}
					slideFrame.redraw();
				}else
					{
					float x = 1.0f / canvas.getClientArea().width * e.x;
					float y = 1.0f / canvas.getClientArea().height * e.y;
					IRay ray = new Ray(WithoutNativeObject.I_WILL_DELETE_THIS_OBJECT);
					camera.getCameraToViewportRay(ray, x, y);
					IRaySceneQuery rsq = sceneManager.createRayQuery(ray,0xFFFFFFFF);
					rsq.setSortByDistance(true, 10);
					IRaySceneQueryResult rsqr = rsq.execute();
					int priority = 0;
					int rInd = -1;
					int vSel = cmb_vertex.getSelectionIndex();
					IRaySceneQueryResultEntry rsqre;
					if (rsqr.size() > 0) {
						for (int i = 0; i < rsqr.size(); i++) {
							rsqre = rsqr.at(i);
							strSel = rsqre.getmovable().getName();
							if ((strSel.length() > 4) && (strSel.substring(0, 4).compareTo("vTri") == 0)&&(3>priority)){ 
								String foo = strSel.substring(4, strSel.length());
								if(strSel.substring(4, strSel.length()).compareTo(Integer.toString(vSel))==0){
								priority = 3;
								rInd = i;
								indSel = Integer.parseInt(strSel.substring(4,strSel.length()));// extract the index from the string}
								}
							}if ((strSel.length() > 5) && (strSel.substring(0, 5).compareTo("vCirc") == 0)&&(2>priority)){ 
								priority = 2;
								rInd = i;
								indSel = Integer.parseInt(strSel.substring(5,strSel.length()));// extract the index from the string}
							}if ((strSel.length() > 6) && (strSel.substring(0, 6).compareTo("HITBOX") == 0)&&(1>priority)){ 
								priority = 1;
								rInd = i;
								indSel = Integer.parseInt(strSel.substring(6,strSel.length()));// extract the index from the string
							}
						}
						if(rInd>-1) {
							rsqre = rsqr.at(rInd);
							strSel = rsqre.getmovable().getName();
							float rDist = rsqre.getdistance();
							IVector3 rayHit = new Vector3(0, 0, 0);
							ray.getPoint(rayHit, rDist); // see where on plane we hit
							xCursor = rayHit.getx();
							yCursor = rayHit.gety() - dh.HGT_DIV2;
							System.out.println("selected: " + strSel);
							if (priority==2) { // if a vertex is selected		
								SelectVertex(aSel, indSel);
								moveVert = true; // drag the vertex on screen
								moveDir = false;
								moveHitbox=false;						
								dh.PrepareToCommit();
							} else if (priority==3) { // if a Direction is selected							
								SelectVertex(aSel, indSel);
								moveVert = false;
								moveDir = true; // drag the vertex on screen
								moveHitbox=false;
								dh.PrepareToCommit();
							} else if (priority==1) {
								if(indSel !=hitboxSelect){
									SelectHitBox(indSel);
									moveVert = false; // not dragging vertex on screen
									moveDir = false;
									moveHitbox = false;
									hbIndStart = -1;
									hbIndEnd = -1;
									dh.PrepareToCommit();
								}else if(indSel ==hitboxSelect){
								if(hbIndStart >=0){
									moveHitbox = true;
									xOff = GetVertAtFrame(aSel, hbIndStart, frameSel).xLoc-xCursor;
									yOff = GetVertAtFrame(aSel, hbIndStart, frameSel).yLoc-yCursor;
									}
								}
								dh.PrepareToCommit();
							} 
	
						}
						
						ray.delete();
						sceneManager.destroyQuery(rsq);
					}
				}
			
			
		}
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		int vSel = cmb_vertex.getSelectionIndex();
		if( (e.keyCode==SWT.DEL) || (e.keyCode == SWT.BS))
			DeleteVertex();
		//ctrl codes
		if( ((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 's'))
			SaveFile();
		if( ((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'z'))
			Undo();
		if( ((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'y'))
			Redo();
		if( ((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == 'h'))
			SelectEntireHitbox(vSel);
		if( ((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode == '-'))
			FrameDown();
		if( (((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.stateMask & SWT.SHIFT) == SWT.SHIFT)&&(e.keyCode == '='))
			FrameUp();
		if( ((e.stateMask & SWT.PAGE_UP) == SWT.PAGE_UP))
			Zoom(0.92f);
		if( ((e.stateMask & SWT.PAGE_DOWN) == SWT.PAGE_DOWN))
			Zoom(1.09f);
		if( ((e.stateMask & SWT.HOME) == SWT.HOME))
			Zoom(1.f);//reset
		if((e.stateMask & SWT.DEL) == SWT.DEL)
			if( ((e.stateMask & SWT.SHIFT) == SWT.SHIFT))
				DeleteKeyframe(frameSel);
			else
				DeleteVertex();
		if(e.keyCode=='i')
			if(!FrameCheck(frameSel))
			AddKeyframe(frameSel);
	}


	@Override
	public void windowClosing(WindowEvent e){
		Quit();
	}
	@Override
	public void keyReleased(KeyEvent arg0){}	
	@Override//    N     O      T
	public void windowActivated(WindowEvent arg0){}
	@Override
	public void windowClosed(WindowEvent arg0){}
	@Override//         E      V      E       N
	public void windowDeactivated(WindowEvent arg0){}
	@Override
	public void windowDeiconified(WindowEvent arg0){}
	@Override//    N   E   E   D   E   D
	public void windowIconified(WindowEvent arg0){}
	@Override
	public void windowOpened(WindowEvent arg0){}
}
class ImportTrackDlg extends Dialog {
	  private String message;
	  private int input;
	  private Combo trSelector;
	  private Label label;
      public int nTracks;
      public int cTrack;
	  /**
	   * InputDialog constructor
	   * 
	   * @param parent the parent
	   */
	  public ImportTrackDlg(Shell parent) {
	    // Pass the default styles here
	    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	    cTrack=0;
	    nTracks=1;
	    input=-1;
	  }
	  public ImportTrackDlg(Shell parent, int style) {
		    // Let users override the default styles
		    super(parent, style);
		    setText("Import Track");
		    cTrack=0;
		    nTracks=1;
		    input=-1;
		  }
	  public ImportTrackDlg(Shell parent, int nt, int ct) {
		    // Pass the default styles here
		    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		    nTracks=nt;
		    cTrack=ct;
		    input=-1;
		  }
	  public String getMessage() {
		    return message;
		  }

		  /**
		   * Sets the message
		   * 
		   * @param message the new message
		   */
		  public void setMessage(String message) {
		    this.message = message;
		  }

		  /**
		   * Gets the input
		   * 
		   * @return String
		   */
		  public int getInput() {
		    return input;
		  }

		  /**
		   * Sets the input
		   * 
		   * @param input the new input
		   */
		  public void setInput(int input) {
		    this.input = input;
		  }
	  public int open() {
		    // Create the dialog window
		    Shell shell = new Shell(getParent(), getStyle());
		    shell.setText(getText());
		    createContents(shell);
		    shell.pack();
		    shell.open();
		    Display display = getParent().getDisplay();
		    while (!shell.isDisposed()) {
		      if (!display.readAndDispatch()) {
		        display.sleep();
		      }
		    }
		    // Return the entered value, or null
		    return input;
		  }
	  private void createContents(final Shell shell) {
		    shell.setLayout(new GridLayout(2, true));

		    // Show the message
		    Label label = new Label(shell, SWT.NONE);
		    label.setText("Choose Track");
		    GridData data = new GridData();
		    data.horizontalSpan = 2;
		    label.setLayoutData(data);

		    // Display the input box
		    final Combo trCmb = new Combo(shell, SWT.BORDER);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    data.horizontalSpan = 2;
		    trCmb.setLayoutData(data);
		    for(int i=0;i<nTracks;i++){
		    	if(i!=cTrack)
		    		trCmb.add("Track " + i);
		    }
		    trCmb.select(0);
		    // Create the OK button and add a handler
		    // so that pressing it will set input
		    // to the entered value
		    Button ok = new Button(shell, SWT.PUSH);
		    ok.setText("OK");
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    ok.setLayoutData(data);
		    ok.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        input = trCmb.getSelectionIndex();
		        System.out.println(input +" "+cTrack);
		        if(input>=cTrack)
		        	input++;
		        System.out.println(input +" "+cTrack);
		        shell.close();
		      }
		    });

		    // Create the cancel button and add a handler
		    // so that pressing it will set input to null
		    Button cancel = new Button(shell, SWT.PUSH);
		    cancel.setText("Cancel");
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    cancel.setLayoutData(data);
		    cancel.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        input = -1;
		        shell.close();
		      }
		    });

		    // Set the OK button as the default, so
		    // user can type input and press Enter
		    // to dismiss
		    shell.setDefaultButton(ok);
		  }
	  public void AddTrackOption(int i)
	  {
		  trSelector.add("Track "+i);
	  }
}


