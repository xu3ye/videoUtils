package com.xuye.videoplay.h264;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xuye.videoplay.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by xu_ye.
 * Date: 2022/11/23 22:53
 */
public class H264Activity extends AppCompatActivity {

    /**
     * //存储权限，1.在xml加， 2.申请 3.xml加requestLegacyExternalStorage="true"
     * 当targetSdkVersion>=29： Environment.isExternalStorageLegacy()为 true，采用的是非分区存储方法
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h264);
        checkPermission();
        init();
    }

    H264Player h264Player;
    String TAG = "MainActivity1---tag---";

    void init() {
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Surface surface = surfaceHolder.getSurface();
                String path = new File(Environment.getExternalStorageDirectory(), "out.h264").getAbsolutePath();
                h264Player = new H264Player(surface, path);
                Log.d(TAG, Environment.getExternalStorageDirectory() + "  " + path + "  " + surface);
                new SystemOutLogger().println("------------------------test");
                h264Player.player();
            }
            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }
            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
            }
        });
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 1);

        }
    }

    public final class SystemOutLogger {

        private String getSimpleClassName(String name) {
            int lastIndex = name.lastIndexOf(".");
            return name.substring(lastIndex + 1);
        }


        public void println(String msg) {
            Thread currThread = Thread.currentThread();
            StackTraceElement[] trace = currThread.getStackTrace();

            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

            int methodCount = 1;
            int stackOffset = getStackOffset(trace);

            if (methodCount + stackOffset > trace.length) {
                methodCount = trace.length - stackOffset - 1;
            }

            for (int i = methodCount; i > 0; i--) {
                int stackIndex = i + stackOffset;
                if (stackIndex >= trace.length) {
                    continue;
                }
                StackTraceElement element = trace[stackIndex];

                StringBuilder builder = new StringBuilder();
                builder.append(time);
                builder.append(" [");
                builder.append(currThread.getId());
                builder.append("-");
                builder.append(currThread.getName());
                builder.append("]");
                builder.append(" ");
                builder.append(getSimpleClassName(element.getClassName()))
                        .append(".")
                        .append(element.getMethodName())
                        .append(" ")
                        .append(" (")
                        .append(element.getFileName())
                        .append(":")
                        .append(element.getLineNumber())
                        .append(")")
                        .append(" | ")
                        .append(msg);

                System.out.println(builder.toString());

            }
        }

        private int getStackOffset(StackTraceElement[] trace) {
            for (int i = 2; i < trace.length; i++) {
                StackTraceElement e = trace[i];
                String name = e.getClassName();
                if (!name.equals(SystemOutLogger.class.getName())) {
                    return --i;
                }
            }
            return -1;
        }
    }

}