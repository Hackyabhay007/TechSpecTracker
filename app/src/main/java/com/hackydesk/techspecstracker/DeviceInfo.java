package com.hackydesk.techspecstracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceInfo {
    private Context context;

    public DeviceInfo(Context context) {
        this.context = context;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getModelName() {
        return Build.MODEL;
    }



    public  String getBrandName(){
        return Build.BRAND;
    }

    public String getModelNumber() {
        return Build.DEVICE;
    }
    public String getGpuInfo() {
        String renderer = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] version = new int[2];
            EGL14.eglInitialize(display, version, 0, version, 1);
            int[] configAttribs = {
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, configs.length, numConfigs, 0);
            int[] attribs = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            EGLContext context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, attribs, 0);
            EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], new int[]{EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE}, 0);
            EGL14.eglMakeCurrent(display, surface, surface, context);
            renderer = GLES20.glGetString(GLES20.GL_RENDERER);
            EGL14.eglDestroySurface(display, surface);
            EGL14.eglDestroyContext(display, context);
            EGL14.eglTerminate(display);
        }
        return renderer;
    }

    public String getCpuInfo() {
        String architecture = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (String abi : Build.SUPPORTED_ABIS) {
                if (abi.contains("arm")) {
                    architecture = "ARM";
                } else if (abi.contains("x86")) {
                    architecture = "x86";
                } else if (abi.contains("mips")) {
                    architecture = "MIPS";
                } else {
                    architecture = "Unknown";
                }
            }
        } else {
            String cpuInfo = "";
            try {
                Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    cpuInfo += line + "\n";
                }
                reader.close();
                process.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (cpuInfo.contains("ARM")) {
                architecture = "ARM";
            } else if (cpuInfo.contains("x86")) {
                architecture = "x86";
            } else if (cpuInfo.contains("MIPS")) {
                architecture = "MIPS";
            } else {
                architecture = "Unknown";
            }
        }
        return architecture;
    }



    public double  getTotalRam() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        double totalRam = memoryInfo.totalMem / 1073741824.0;
        return totalRam;
    }

    public String getImei()
    {
        String id ="NOT AVALIABLE";
        try{
            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
             id =  telephonyManager.getDeviceId();

        }catch (Exception e)
        {
            Log.e("imei error",String.valueOf(e));
        }

        return id;
    }
    public String getCamerasMegaPixel() throws CameraAccessException {
        String output = "";
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        String[] cameraIds = manager.getCameraIdList();

        Log.e("size", String.valueOf(cameraIds.length));
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIds[0]);
        output = "Back Camera  pixel:   " +  calculateMegaPixel(characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE).getWidth(),
                characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE).getHeight()) +"MP" +"\n";

        characteristics = manager.getCameraCharacteristics(cameraIds[1]);
        output +=  "Front Camera mega:  " + calculateMegaPixel(characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE).getWidth(),
                characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE).getHeight())  +"MP"+ "\n";
        return output;


    }
    public int calculateMegaPixel(float width, float height) {
        return  Math.round((width * height) / 1024000);
    }

    public long getAvailableStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long availableBlocks = statFs.getAvailableBlocksLong();
        long blockSize = statFs.getBlockSizeLong();
        return availableBlocks * blockSize;
    }

    public long getTotalStorage() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalBlocks = statFs.getBlockCountLong();
        long blockSize = statFs.getBlockSizeLong();
        return totalBlocks * blockSize;
    }

    public float getBatteryLevel() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level / (float) scale * 100.0f;
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }


}