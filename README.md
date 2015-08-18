# ModifiedStepsView
改进的StepsView

根据StepsView进行改进，添加了每一步进行的等待动画和每一步的执行结果动画
具体的如下图所示：


![image](https://github.com/gpfduoduo/ModifiedStepsView/tree/master/ModifiedStepsView/gif/device-2015-08-18-135551.png "效果图")


使用方法：

activity的xml文件中添加：

 <com.example.lenovo.library.StepsView
         android:id="@+id/steps_view"
         android:layout_height="wrap_content"
         android:layout_width="fill_parent"
         app:numOfSteps="4"></com.example.lenovo.library.StepsView>

在你的activity中调用


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
'''
