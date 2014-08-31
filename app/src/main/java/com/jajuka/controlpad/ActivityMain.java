package com.jajuka.controlpad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class ActivityMain extends Activity {
    private static final String TAG = ActivityMain.class.getSimpleName();
    private static final int TOAST_DELAY = 1000;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private View mTouchScreen;

    private boolean mIsConnected = false;
    private String mIpAddress;

    private long mLastToast = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTouchScreen = findViewById(R.id.touchscreen);
        mTouchScreen.setBackgroundColor(Color.RED);

        initializeFullscreen();

        mHandlerThread = new HandlerThread("Relay");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!mIsConnected) {
            if (System.currentTimeMillis() > mLastToast + TOAST_DELAY) {
                Toast.makeText(this, R.string.notConnected, Toast.LENGTH_LONG).show();
                mLastToast = System.currentTimeMillis();
            }
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final int height = getWindow().getDecorView().getHeight();
                    final int width = getWindow().getDecorView().getWidth();

                    if (event.getAction() == MotionEvent.ACTION_DOWN ||
                            event.getAction() == MotionEvent.ACTION_UP ||
                            event.getAction() == MotionEvent.ACTION_MOVE) {

                        Action action = Action.Touch;
                        Type type = null;

                        final List<NameValuePair> params = new LinkedList<NameValuePair>();
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                type = Type.Down;
                                params.add(new BasicNameValuePair("x", String.valueOf((int) (1920 * (event.getX() / width)))));
                                params.add(new BasicNameValuePair("y", String.valueOf((int) (1080 * (event.getY() / height)))));
                                break;
                            case MotionEvent.ACTION_UP:
                                type = Type.Up;
                                params.add(new BasicNameValuePair("x", String.valueOf((int) (1920 * (event.getX() / width)))));
                                params.add(new BasicNameValuePair("y", String.valueOf((int) (1080 * (event.getY() / height)))));
                                break;
                            case MotionEvent.ACTION_MOVE:
                                type = Type.Move;
                                params.add(new BasicNameValuePair("x", String.valueOf((int) (1920 * (event.getX() / width)))));
                                params.add(new BasicNameValuePair("y", String.valueOf((int) (1080 * (event.getY() / height)))));
                                break;
                        }

                        if (action != null && type != null) {
                            sendCommand(action, type, params);
                        }
                    }
                }
            });
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mIsConnected) {
            final List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("message", String.valueOf((char) event.getUnicodeChar())));
            sendCommand(Action.Type, null, params);
        }

        return super.dispatchKeyEvent(event);
    }

    private void sendCommand(final Action action, final Type type, final List<NameValuePair> parameters) {
        // Force the command off of the main thread
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendCommand(action, type, parameters);
                }
            });

            return;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("action=").append(action).append("&");
        builder.append("type=").append(type).append("&");

        for (NameValuePair pair : parameters) {
            builder.append(pair.getName()).append("=").append(pair.getValue()).append("&");
        }

        try {
            final URL url = new URL("http://192.168.2.14:8080?" + builder.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                final BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d(TAG, line);
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to send command due to an IOException", e);
        }
    }

    private void initializeFullscreen() {
        hideNavigation();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    getActionBar().show();
                } else {
                    getActionBar().hide();
                }
            }
        });
    }

    private void hideNavigation() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.disconnect).setVisible(mIsConnected);
        menu.findItem(R.id.connect).setVisible(!mIsConnected);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        final List<NameValuePair> params = new LinkedList<NameValuePair>();

        switch (id) {
            case R.id.back:
                params.add(new BasicNameValuePair("key", KeyCode.Back.toString()));
                sendCommand(Action.Press, Type.DownUp, params);
                break;
            case R.id.home:
                params.add(new BasicNameValuePair("key", KeyCode.Home.toString()));
                break;
            case R.id.fullscreen:
                hideNavigation();
                break;
            case R.id.connect:
                showIpDialog();
                break;
            case R.id.disconnect:
                mIsConnected = false;
                getActionBar().setTitle(getString(R.string.notConnected));
                break;
            case R.id.keyboard:
                showKeyboard();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mTouchScreen, InputMethodManager.SHOW_FORCED);
    }

    private void showIpDialog() {
        final EditText inputText = new EditText(this);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.cpsIpAddress)
                .setMessage(R.string.enterCpsIpAddress)
                .setView(inputText)
                .setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIpAddress = inputText.getText().toString();
                        mIsConnected = true;
                        getActionBar().setTitle(mIpAddress);
                        mTouchScreen.setBackgroundColor(Color.BLUE);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mIpAddress = null;
                        mIsConnected = false;
                        getActionBar().setTitle(getString(R.string.notConnected));
                        mTouchScreen.setBackgroundColor(Color.RED);
                    }
                })
                .show();
    }

    private enum Action {
        Touch,
        Type,
        Press;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private enum Type {
        Down,
        Up,
        DownUp,
        Move;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private enum KeyCode {
        Back,
        Home;

        @Override
        public String toString() {
            return "KEYCODE_" + name().toUpperCase();
        }
    }
}
