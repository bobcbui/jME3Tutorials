package com.ooqn.jme.game;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.util.AnimMigrationUtils;
import com.jme3.app.ChaseCameraAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.ooqn.jme.logic.FloatControl;

/**
 * 测试RPG游戏中常见的运动方式。
 * 
 * @author yanmaoyuan
 *
 */
public class TestRPG2 extends SimpleApplication implements ActionListener, Observer {

    public static void main(String[] args) {
        TestRPG2 app = new TestRPG2();
        app.start();
    }

    // 平台
    private Spatial floor;
    
    // 标志
    private Spatial flag;

    private MotionControl motionControl;

    private AnimComposer animComposer;

    private boolean isWalking = false;
    private boolean isRunning = false;

    public TestRPG2() {
        super(new StatsAppState(), new ChaseCameraAppState(), new AiAppState(), new LightAppState());
    }
    
    @Override
    public void simpleInitApp() {
        initCamera();
        initKeys();
        initScene();
    }

    /**
     * 初始化跟踪摄像机
     */
    private void initCamera() {

        // 设置跟踪相机的参数
        ChaseCameraAppState chaseCam = stateManager.getState(ChaseCameraAppState.class);

        // 仰角最小10°，最大90°，默认30°
        chaseCam.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 50);

        // 摄像机到观察点的距离，最小5f，最大30f，默认5f
        chaseCam.setMinDistance(5f);
        chaseCam.setMaxDistance(30f);
        chaseCam.setDefaultDistance(30f);

        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setInvertHorizontalAxis(true);

        // 鼠标右键触发旋转
        chaseCam.setToggleRotationTrigger();

        stateManager.attach(chaseCam);
    }

    /**
     * 初始化按键
     */
    private void initKeys() {
        inputManager.addMapping("LeftClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("Run", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Q", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "LeftClick", "Jump", "Run");
    }

    /**
     * 初始化场景
     */
    private void initScene() {
        // 加载Jaime模型
        loadJaime();

        // 创建一个平面作为舞台
        this.floor = createFloor();

        // 创建一个目标点的标记
        this.flag = createTargetFlag();
    }

    /**
     * 加载Jaime模型
     * 
     * @return
     */
    private Spatial loadJaime() {
        // 加载模型
        Spatial jaime =  assetManager.loadModel("Models/Jaime/Jaime.j3o");
        AnimMigrationUtils.migrate(jaime);
        animComposer = jaime.getControl(AnimComposer.class);
        animComposer.setCurrentAction("Idle");
        jaime.scale(2);
        rootNode.attachChild(jaime);
        jaime.setShadowMode(ShadowMode.Cast);

        // 创造一个空心节点，作为摄像机的交点
        Node camPiovt = new Node("CamPiovt");
        camPiovt.move(0, 1f, 0);
        ((Node)jaime).attachChild(camPiovt);
        
        stateManager.getState(ChaseCameraAppState.class).setTarget(camPiovt);
        stateManager.getState(AiAppState.class).setPlayer(jaime);
        
        // 添加一个运动组件
        jaime.addControl(motionControl = new MotionControl(4.0f));
        motionControl.setObserver(this);

        return jaime;
    }

    /**
     * 创建一个平面作为Jaime行走的舞台
     * 
     * @return
     */
    private Spatial createFloor() {

        Quad q = new Quad(50, 50);
        q.scaleTextureCoordinates(new Vector2f(10, 10));
        Geometry stage = new Geometry("Stage", q);
        stage.setMaterial(assetManager.loadMaterial("Textures/Terrain/Pond/Pond.j3m"));

        stage.rotate(-FastMath.HALF_PI, 0, 0);
        stage.center();

        stage.setShadowMode(ShadowMode.Receive);
        rootNode.attachChild(stage);

        return stage;
    }

    /**
     * 创造一个标记点
     */
    private Spatial createTargetFlag() {

        Geometry arrow = new Geometry("Arrow", new Arrow(new Vector3f(0, -1f, 0)));
        arrow.move(0, 1.5f, 0);
        arrow.addControl(new FloatControl(0.3f, 1.5f));

        Geometry sphere = new Geometry("Sphere", new Sphere(6, 9, 0.2f));

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        mat.getAdditionalRenderState().setLineWidth(2f);

        Node flagNode = new Node();
        flagNode.attachChild(arrow);
        flagNode.attachChild(sphere);

        flagNode.setMaterial(mat);
        flagNode.setShadowMode(ShadowMode.Off);

        return flagNode;
    }


    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        /**
         * 若Jaime已经处于JumpStart/Jumping/JumpEnd状态，就不要再做其他动作了。
         */

        if (isPressed) {
            if ("LeftClick".equals(name)) {
                pickTarget();
            } else if ("Jump".equals(name)) {
            	// 跳跃时是原有速度的1.5倍
                if (isRunning) {
                	motionControl.setWalkSpeed(10.0f);
                } else {
                	motionControl.setWalkSpeed(1.5f);
                }
                // 播放“起跳”动画
                animComposer.setCurrentAction("JumpStart",AnimComposer.DEFAULT_LAYER, false);
            } else if ("Run".equals(name)) {
            	isRunning = !isRunning;
            	
            	if (isRunning) {
                	motionControl.setWalkSpeed(1.5f);
                    animComposer.setCurrentAction("Run",AnimComposer.DEFAULT_LAYER, false);
                } else {
                    motionControl.setWalkSpeed(1.0f);
                    animComposer.getAction("Walk");
                    animComposer.setCurrentAction("Walk", AnimComposer.DEFAULT_LAYER, false);
                    
                }
            	
            }
        }
    }

    /**
     * 拣选目标点
     */
    private void pickTarget() {

        Vector2f pos = inputManager.getCursorPosition();
        Vector3f orgin = cam.getLocation();
        Vector3f to = cam.getWorldCoordinates(pos, 0.3f);

        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(orgin, to.subtractLocal(orgin));
        floor.collideWith(ray, results);

        if (results.size() > 0) {
            // 设置目标点
            setTarget(results.getClosestCollision().getContactPoint());
        }
    }

    /**
     * 设置目标点
     * 
     * @param target
     */
    private void setTarget(Vector3f target) {
        flag.setLocalTranslation(target);
        rootNode.attachChild(flag);

        // 设置目标点
        motionControl.setTarget(target);

        if (!isWalking) {
            isWalking = true;
            if (isRunning) {
            	animComposer.setCurrentAction("Run");
            } else {
                animComposer.setGlobalSpeed(10);
            	animComposer.setCurrentAction("Walk");
            }
        }
    }

    @Override
    public void onReachTarget() {
        if (isRunning) {
            motionControl.setWalkSpeed(1.5f);
        } else {
            motionControl.setWalkSpeed(1.0f);
        }
        // 到达目标点，把动画改为Idle
        animComposer.setCurrentAction("Idle");
        isWalking = false;

    }

}
