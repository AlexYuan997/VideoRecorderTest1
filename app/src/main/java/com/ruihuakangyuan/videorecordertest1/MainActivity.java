package com.ruihuakangyuan.videorecordertest1;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;


public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceView;
    private ImageButton mBtnStartStop;
    private ImageButton mBtnSet;
    private ImageButton mBtnShowFile;
    private Chronometer mTimer;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private Camera.Parameters mParameters;


  /*
   录像实现的步骤：
   1.Open Camera - 使用Camera.open()静态方法来获得camera对象实例。
    2.Connect Preview - 使用Camera.setPreviewDiaplay()方法将相机的预览画面显示在SurfaceView控件上。
    3.Start Preview - 使用Camera.startPreview()方法 开始启动预览画面.
    4.完成如下步骤：
             a.setCamera() - 设置camera用于录像。
            b.setAudioSource() - 设置录像音频来源, 使用麦克风 MediaRecorder.AudioSource.CAMCORDER作为音频来源.
             c.setVideoSource() - 设置录像视频来源, 使用Camera MediaRecorder.VideoSource.CAMERA作为视频来源.
             d.设置视频的输出格式和编码格式。 对于Android2.2或者更高版本使用 MediaRecorder.setProfile方法即可，使用方法CamcorderProfile.get()来获得一个配置信息。
             e.setOutputFile() - 设置视频输出保存到文件的路径。
             f.setPreviewDisplay() - 为你的MediaRecorder指定预览显示.使用第2步一样的参数即可。

    5.Stop Recording Video - 当你结束录像时调用如下方法：
             a. Stop MediaRecorder - 首先调用 MediaRecorder.stop()方法停止多媒体录像。
            b. Reset MediaRecorder - 调用MediaRecorder.reset()方法重置多媒体状态，调用该方法之后之前的所有MediaRecorder configuration将被移除，你如果还想再次录像，需要再次配置多媒体参数.
              c. Release MediaRecorder - 调用 MediaRecorder.release()方法释放多媒体资源.
             d. Lock the Camera - 调用Camera.lock()方法来给Camera硬件加锁. 在Android4.0及以后无需调用该方法，除非在调用MediaRecorder.prepare()失败时，才需要再次调用该方法。

     6.Stop the Preview - 当你的Activity已经不再使用camera时，调用Camera.stopPreview()方法来停止预览。

     7.Release Camera - 当不再使用Camera时，调用Camera.release()方法来释放camera，以便其他应用可以使用camera资源。
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                boolean b = checkCameraHardware(this);
        System.out.println("是否成功"+b);
        initWindowFeature();
//        setContentView(R.layout.activity_main);
        initView();//初始化各个按钮
        initCameraAndSurfaceViewHolder();
        prepareMediaRecorder();

    }

    private void initCameraAndSurfaceViewHolder() {
        mHolder = mSurfaceView.getHolder();


        if (mHolder==null){
            Log.w(TAG, "initCameraAndSurfaceViewHolder: mHolder没有初始化成功" );
        }
        mHolder.addCallback(this);
        // setType必须设置，要不出错.
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera=Camera.open();//打开摄像头开始摄像
    }


//检测硬件是否存在
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
// no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void initListeners() {
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"录像");
                if (isRecording()) {
                    Log.d(TAG,"停止录像");
                    stopRecording();
                    mTimer.stop();
                    mBtnStartStop.setBackgroundResource(R.drawable.rec_start);
                } else {
                    if (startRecording()) {
                        Log.d(TAG,"开始录像");
                        mTimer.setBase(SystemClock.elapsedRealtime());
                        mTimer.start();
                        mBtnStartStop.setBackgroundResource(R.drawable.rec_stop);
                    }
                }
            }
        });
        mBtnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"设置");
                Toast.makeText(MainActivity.this,"设置待开发...",Toast.LENGTH_SHORT).show();
            }
        });
        mBtnShowFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ShowVideoActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();

//        打开Camera资源
        mCamera.unlock();

//        setCamera() - 设置camera用于录像。
        mMediaRecorder.setCamera(mCamera);

//        setAudioSource() - 设置录像音频来源, 使用麦克风 MediaRecorder.AudioSource.CAMCORDER作为音频来源.
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        setVideoSource() - 设置录像视频来源, 使用Camera MediaRecorder.VideoSource.CAMERA作为视频来源.
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        设置视频的输出格式和编码格式
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        //   setPreviewDisplay() - 为你的MediaRecorder指定预览显示.使用第2步一样的参数即可
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());




//设置视屏质量
        //设置视屏的分辨率
        mMediaRecorder.setVideoSize(640, 480);
        //一秒30帧
        mMediaRecorder.setVideoFrameRate(30);
        //设置视屏的大小3、4、5，数值越大20秒钟里面的视频越大
        mMediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);




        //获取路径
        String path = getSDPath();
        if (path != null) {
            File dir = new File(path + "/VideoRecorderTest");
            if (!dir.exists()) {
                dir.mkdir();
            }
            path = dir + "/" + getDate() + ".mp4";
//            设置视频输出保存到文件的路径。
            mMediaRecorder.setOutputFile(path);
            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                releaseMediaRecorder();
                e.printStackTrace();
            }
        }
        return true;
    }

    private void releaseMediaRecorder() {


/*释放资源的步骤：
a. Stop MediaRecorder - 首先调用 MediaRecorder.stop()方法停止多媒体录像。
          b. Reset MediaRecorder - 调用MediaRecorder.reset()方法重置多媒体状态，调用该方法之后
        之前的所有MediaRecorder configuration将被移除，你如果还想再次录像，需要再次配置多媒体参数.
          c. Release MediaRecorder - 调用 MediaRecorder.release()方法释放多媒体资源.
          d调用Camera.lock()方法来给Camera硬件加锁.
          adroid4.0及以后无需调用该方法，除非在调用MediaRecorder.prepare()失败时，才需要再次调用该方法。*/


        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    //开始录像
    public boolean startRecording() {
        if (prepareMediaRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    //停止录像
    public void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            //释放资源需要先停止
        }
        releaseMediaRecorder();
    }

    private void initView() {
        mSurfaceView  = findViewById(R.id.capture_surfaceview);
        mBtnStartStop = findViewById(R.id.ib_stop);
        mBtnSet= findViewById(R.id.capture_imagebutton_setting);
        mBtnShowFile= findViewById(R.id.capture_imagebutton_showfiles);
        mTimer= findViewById(R.id.crm_count_time);
    }



    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        initView();
        initCameraAndSurfaceViewHolder();
        initListeners();
    }


    private void initWindowFeature() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 选择支持半透明模式,在有SurfaceView的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    /**
     * 获取系统时间
     */
    public static String getDate(){
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        String date = "" + year + (month + 1 )+ day + hour + minute + second;
        Log.d(TAG, "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     */
    public String getSDPath(){
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();// 获取外部存储的根目录
            return sdDir.toString();
        }

        return null;
    }


    //实现接口中Surface.Callback接口的方法，用来监听视频录制的情况
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

     /*   大部分Android 相机特性功能都可以通过 Camera.Parameters类来控制。首先你可以获得一个Camera实例，
        然后调用Camera.getParameters()方法的返回值来得到Caemra.Parameters实例，
        之后就可以通过Parameters.setParameters()系列方法来设置一些参数使用相机的一些特性功能*/


        mParameters=mCamera.getParameters();//获取预览对象

        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//设置视屏自动对焦

        mCamera.setParameters(mParameters);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
                    Log.d(TAG,"自动对焦成功");
                }
            }
        });
        try {
      /*      Connect Preview - 使用Camera.setPreviewDiaplay()方法将相机的预览画面显示在SurfaceView控件上。
            Start Preview - 使用Camera.startPreview()方法 开始启动预览画面.*/

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            //下面这个方法能帮我们获取到相机预览帧，我们可以在这里实时地处理每一帧
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Log.i(TAG, "获取预览帧...");
                    new ProcessFrameAsyncTask().execute(data);
                    Log.d(TAG,"预览帧大小："+String.valueOf(data.length));
                }
            });
        } catch (IOException e) {
            Log.d(TAG,"设置相机预览失败",e);
            e.printStackTrace();
        }
    }

    //surfaceChanged 用于使用监听布局有改变
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }


    //不使用资源最后释放资源
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private class ProcessFrameAsyncTask extends AsyncTask<byte[],Void,String> {

        @Override
        protected String doInBackground(byte[]... params) {
            processFrame(params[0]);
            return null;
        }

        private void processFrame(byte[] frameData) {

            Log.i(TAG, "正在处理预览帧...");
            Log.i(TAG, "预览帧大小"+String.valueOf(frameData.length));
            Log.i(TAG, "预览帧处理完毕...");
            //下面这段注释掉的代码是把预览帧数据输出到sd卡中，以.yuv格式保存
//            String path = getSDPath();
//            File dir = new File(path + "/FrameTest");
//            if (!dir.exists()) {
//                dir.mkdir();
//            }
//            path = dir + "/" + "testFrame"+".yuv";
//            File file =new File(path);
//            try {
//                FileOutputStream fileOutputStream=new FileOutputStream(file);
//                BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
//                bufferedOutputStream.write(frameData);
//                Log.i(TAG, "预览帧处理完毕...");
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }


    }
}

