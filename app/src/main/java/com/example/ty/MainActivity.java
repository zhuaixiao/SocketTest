package com.example.ty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PlaceholderFragment";
    private TextInputEditText et1, et2;
    private TextView tv;
    private Button bt;
    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;
    private PrintWriter mPrintWriter;
    private Socket mClientSocket;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG:
                    tv.setText(tv.getText()+(String) msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    tv.setText("连接成功");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        tv = findViewById(R.id.tv);
        bt = findViewById(R.id.bt);
        Intent intent = new Intent(MainActivity.this, TcpService.class);
        startService(intent);
        new Thread() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg = et1.getText().toString() + et2.getText().toString();
                if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mPrintWriter.println(msg);
                        }
                    }).start();

                    et1.setText("");
                    et2.setText("");
                    String time = formatDateTime(System.currentTimeMillis());
                    String showedMsg = "self" + time + ":" + msg + "\n";
                    tv.setText(tv.getText() + showedMsg);

                }
            }

        });
    }

    @Override
    public void onDestroy() {

        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                Log.e(TAG, "连接Server成功");
            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                Log.e(TAG, "连接Server不成功，正在重试...");
            }
        }
        try {
            BufferedReader br = new BufferedReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            while (!MainActivity.this.isFinishing()) {
                String msg = br.readLine();
                Log.e(TAG, "客户端接受到:" + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    String showedMsg = "server" + time + ":" + msg + "\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg).sendToTarget();
                }
            }
            mPrintWriter.close();
            br.close();
            socket.close();
            Log.e(TAG, "退出...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }
}

