package com.ooqn.jme;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.BaseAction;
import com.jme3.anim.util.AnimMigrationUtils;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;

/**
 * 动画
 * 
 * @author yanmaoyuan
 *
 */
public class HelloAnimation extends SimpleApplication {

    public static void main(String[] args) {
        // 启动程序
        HelloAnimation app = new HelloAnimation();
        app.start();
    }

    /**
     * 按W键行走
     */
    private final static String WALK = "walk";

    /**
     * 按空格键跳跃
     */
    private final static String JUMP = "jump";

    /**
     * 记录Jaime的行走状态。
     */
    private boolean isWalking = false;

    /**
     * 动画模型
     */
    private Spatial spatial;

    // private AnimControl animControl;
    // private AnimChannel animChannel;

    AnimComposer animComposer;

    @Override
    public void simpleInitApp() {
        // 初始化摄像机
        initCamera();

        // 初始化灯光
        initLight();

        // 初始化按键输入
        initKeys();

        // 初始化场景
        initScene();

        // 动画控制器
        spatial = AnimMigrationUtils.migrate(spatial);

        animComposer = spatial.getControl(AnimComposer.class);

        System.out.println(animComposer.getAnimClipsNames());
        // Walk , Wave , Taunt , JumpEnd , Idle , Punches , SideKick , Run , JumpStart , Jumping
        animComposer.setCurrentAction("Idle");

        baseAction = new BaseAction(Tweens.sequence(
                animComposer.action("JumpStart"), 
                animComposer.action("Jumping"),
                animComposer.action("JumpEnd")
        ));
        animComposer.addAction("WalkOnce", baseAction);

    }

    BaseAction baseAction;
    


    /**
     * 初始化摄像机
     */
    private void initCamera() {
        // 禁用第一人称摄像机
        flyCam.setEnabled(false);

        cam.setLocation(new Vector3f(1, 2, 3));
        cam.lookAt(new Vector3f(0, 0.5f, 0), new Vector3f(0, 1, 0));
    }

    /**
     * 初始化光影
     */
    private void initLight() {
        // 定向光
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.setDirection(new Vector3f(-1, -2, -3));
        sunLight.setColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1f));

        // 环境光
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 1f));

        // 将光源添加到场景图中
        rootNode.addLight(sunLight);
        rootNode.addLight(ambientLight);
    }

    /**
     * 初始化按键
     */
    private void initKeys() {
        // 按W键行走
        inputManager.addMapping(WALK, new KeyTrigger(KeyInput.KEY_W));
        // 按空格键跳跃
        inputManager.addMapping(JUMP, new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(new ActionListener() {

            @Override
            public void onAction(String name, boolean isPressed, float tpf) {
                switch (name) {
                    case JUMP:
                        if (animComposer.getCurrentAction() != animComposer.getAction("WalkOnce")) {
                            animComposer.setCurrentAction("WalkOnce", "Default", false);
                        }
                        break;
                    case WALK:
                        if (animComposer.getCurrentAction() != animComposer.getAction("Walk")) {
                            animComposer.setCurrentAction("Walk");
                        }
                        break;
                }
            }
        }, WALK, JUMP);
    }
    
    

    @Override
    public void update() {
        super.update();
        if (animComposer.getCurrentAction() == null) {
            animComposer.setCurrentAction("Idle");
        }
    }

    /**
     * 初始化场景
     */
    private void initScene() {
        // 加载Jaime模型
        spatial = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        rootNode.attachChild(spatial);

        // 创建一个平面作为舞台
        Geometry stage = new Geometry("Stage", new Quad(2, 2));
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setColor("Ambient", ColorRGBA.Black);
        mat.setFloat("Shininess", 0);
        mat.setBoolean("UseMaterialColors", true);
        stage.setMaterial(mat);

        stage.rotate(-FastMath.HALF_PI, 0, 0);
        stage.center();
        rootNode.attachChild(stage);
    }



}
