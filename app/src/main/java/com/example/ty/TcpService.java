package com.example.ty;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TcpService extends Service {
    private static final String TAG = "TcpService";
    private boolean mIsSerivceDestoryed = false;
    private String[] mDefinedMessages = {"你好啊！哈哈", "请问你叫什么名呀?", "今天北京天气不错啊，shy"
            , "你知道吗？我可是可以和很多人同时聊天的哦！", "给你讲个笑话吧，据说爱笑的人运气不错，不知道真假"};

    public TcpService() {
    }

    @Override
    public void onCreate() {
        new Thread(new mTcpServer()).start();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class mTcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "run: 建立TcpServer失败，端口号8688");
                return;
            }
            while (!mIsSerivceDestoryed) {
                try {
                    final Socket client = serverSocket.accept();
                    Log.e(TAG, "run:接受 ");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException {
        //用于接受客服端信息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //用于向客服端发送信息
        final PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        out.println("欢迎来到聊天室");
        while (!mIsSerivceDestoryed) {
            String str = in.readLine();
            Log.e(TAG, "msg from client" + str);
            if (str == null) {
                Log.e(TAG, "responseClient: 客服端已关闭");
                break;
            }
            int y= (int) (Math.random()*mDefinedMessages.length);
//            int i = new Random().nextInt(mDefinedMessages.length);
            final String msg = mDefinedMessages[y];
            out.println(msg);

            Log.e(TAG, "TcpService发送信息" + msg+y);

        }
        Log.e(TAG, "客服端退出");
        client.close();
        out.close();
        in.close();
    }

    @Override
    public void onDestroy() {
        mIsSerivceDestoryed = true;
        super.onDestroy();
    }
}
