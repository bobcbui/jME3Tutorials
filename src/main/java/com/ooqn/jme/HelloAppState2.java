package com.ooqn.jme;

import com.jme3.app.DebugKeysAppState;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import com.ooqn.jme.logic.InputAppState;
import com.ooqn.jme.logic.LightAppState;
import com.ooqn.jme.logic.VisualAppState;
import com.ooqn.jme.state.AxisAppState;

/**
 * SimpleApplication的最佳形式
 * 
 * @author yanmaoyuan
 *
 */
public class HelloAppState2 extends SimpleApplication {

    public static void main(String[] args) {
        HelloAppState2 app = new HelloAppState2();
        app.start();
    }

    /**
     * 在构造方法中初始化AppState
     */
    public HelloAppState2() {
        super(new StatsAppState(), new FlyCamAppState(), new AudioListenerState(), new DebugKeysAppState(),
               new AxisAppState(), new LightAppState(), new VisualAppState(), new InputAppState());
    }

    @Override
    public void simpleInitApp() {
        // 初始化摄像机
        cam.setLocation(new Vector3f(2.4611378f, 2.8119917f, 9.150583f));
        cam.setRotation(new Quaternion(-0.020502187f, 0.97873497f, -0.16252096f, -0.1234684f));
    }

}
