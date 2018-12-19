package com.vily.vilynettyclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vily.vilynettyclient.nettyClient.NettyClient;
import com.vily.vilynettyclient.nettyClient.NettyListener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView tvNetty;
    TextView tvConnect;

    View vNettyStatus;

    /**
     * Netty 客户端连接处理
     */
    private NettyClient mNettyClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNetty = findViewById(R.id.tvNetty);
        tvConnect = findViewById(R.id.tvConnect);
        vNettyStatus = findViewById(R.id.vNettyStatus);

        findViewById(R.id.btnConn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectNettyServer("192.168.90.179", 1992);
            }
        });


        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsgToServer();
            }
        });

    }
    private void sendMsgToServer() {

        if (!mNettyClient.getConnectStatus()) {
            Toast.makeText(getApplicationContext(), "未连接,请先连接", LENGTH_SHORT).show();
        } else {
            final String msg = "你不是我兄dei，这条消息 来自NettyClient";


            mNettyClient.sendMsgToServer(msg, new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {                //4
                        Log.d(TAG, "Write auth successful");
                    } else {
                        Log.d(TAG, "Write auth error");
                    }
                }
            });
        }
    }
    /**
     * 连接Netty 服务端
     *
     * @param host 服务端地址
     * @param port 服务端端口 默认两端约定一致
     */
    private void connectNettyServer(String host, int port) {

        mNettyClient = new NettyClient(host, port);

        Log.i(TAG, "connectNettyServer");
        if (!mNettyClient.getConnectStatus()) {
            mNettyClient.setListener(new NettyListener() {
                @Override
                public void onMessageResponse(final Object msg) {
                    Log.i(TAG, "onMessageResponse:" + msg);
                    /**
                     *   接收服务端发送过来的 json数据解析
                     */
                    // TODO: 2018/6/1  do something
                    // QueueShowBean    queueShowBean = JSONObject.parseObject((String) msg, QueueShowBean.class);

                    //需要在主线程中刷新
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(MainActivity.this, msg + "", Toast.LENGTH_SHORT).show();

                            tvNetty.setText("Client received:" + msg);
                        }
                    });


                }

                @Override
                public void onServiceStatusConnectChanged(final int statusCode) {
                    /**
                     * 回调执行还在子线程中
                     */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (statusCode == NettyListener.STATUS_CONNECT_SUCCESS) {
                                Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                                vNettyStatus.setSelected(true);
                            } else {
                                Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                                vNettyStatus.setSelected(false);
                            }
                        }
                    });

                }
            });

            mNettyClient.connect();//连接服务器
        }
    }
}
