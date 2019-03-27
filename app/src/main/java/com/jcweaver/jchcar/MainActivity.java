package com.jcweaver.jchcar;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;



import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


public class MainActivity extends AppCompatActivity implements IVLCVout.Callback {

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;


    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    private String rtspUrl = "rtsp://192.168.12.3:554/user=admin_password=tlJwpbo6_channel=1_stream=1.sdp?real_stream";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);

        mSurface = findViewById(R.id.surface_view);
        holder = mSurface.getHolder();
        //holder.addCallback(this);


        ArrayList<String> options = new ArrayList<String>();
        //options.add("-vvv"); // verbosity
        //options.add("--avcodec-codec=h265");
        options.add("--network-caching=200");
        //options.add("--sout-x264-preset=ultrafast");
        //options.add("--sout-x264-tune=zerolatency");

        libvlc = new LibVLC(getApplicationContext(), options);
        holder.setKeepScreenOn(true);

        // Create media player
        mMediaPlayer = new MediaPlayer(libvlc);


        mMediaPlayer.setEventListener(mPlayerListener);

        // Set up video output
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setVideoView(mSurface);
        vout.setWindowSize(1920,1080);
        vout.addCallback(this);
        vout.attachViews();

        Media m = new Media(libvlc, Uri.parse(rtspUrl));
        m.setHWDecoderEnabled(true, true);

        mMediaPlayer.setMedia(m);

        mMediaPlayer.play();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // createPlayer(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
       // Log.d("JCH","scale "+mSurface.getHeight()/720.0f);
       // mMediaPlayer.setScale(mSurface.getHeight()/720.0f);
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    public void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;
    }

    private static String TAG = "PlayerListener";
    class MyPlayerListener implements MediaPlayer.EventListener {


        private WeakReference<MainActivity> mOwner;


        public MyPlayerListener(MainActivity owner) {
            mOwner = new WeakReference<MainActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            MainActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

}
