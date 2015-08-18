package com.example.lenovo.modifiedstepsview;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.lenovo.library.StepsView;

public class MainActivity extends AppCompatActivity {

    private boolean isStop = false;

    private StepsView stepsView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawable(null); //删除默认的背景，防止OVERDRAW

        stepsView = (StepsView) findViewById(R.id.steps_view);

        final String[] steps = {"step1", "step2", "step3", "step4"};
        stepsView.setLabels(steps)
                .setColorIndicator(Color.GRAY)
                .setBarColor(Color.GREEN)
                .setLabelColor(Color.BLACK);

        new Thread() {
            public void run() {
                for (int i = 0; i < steps.length; i++) {
                    if(isStop) {
                        break;
                    }
                    Message msg = new Message();
                    msg.obj = i;
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {
                    }
                }
                Message msg = new Message();
                msg.obj = steps.length;
                handler.sendMessage(msg);
            }
        }.start();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStop = true;
        handler.removeCallbacksAndMessages(null);
    }

    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int i = (int)msg.obj;
            stepsView.setCompletedPosition(i);
            return false;
        }
    });


}
