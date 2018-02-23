package handlermessagesample.sample.hsiungsc.com.handlermessagesample;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import static android.view.View.FOCUS_DOWN;

public class MainActivity extends Activity {

    private static final String TAG = "HandlerMessageExample";

    private Handler mUIThreadHandler;

    // Runnable to update scroll view status content
    class TaskStatusUpdateRunnable implements Runnable {
        String mStatus;

        TaskStatusUpdateRunnable(String status) {
            mStatus = status;
        }

        @Override
        public void run() {
            TextView statusTextView = findViewById(R.id.TEXT_STATUS_ID);
            ScrollView scrollView = findViewById(R.id.SCROLLER_ID);

            String newStatus = statusTextView.getText() + "\n" + mStatus;
            statusTextView.setText(newStatus);
            scrollView.fullScroll(FOCUS_DOWN);
        }
    }

    // After the task is done, task runnable in worker thread sends message to
    // UI thread using UI thread handler
    class TaskRunnable implements Runnable {
        String mTasks;

        TaskRunnable(String task) {
            mTasks = task;
        }

        @Override
        public void run() {
            // Pretending has 5 seconds task work here
            try {
                Log.d(TAG, "Process '" + mTasks + " in Phase #1.");
                Thread.sleep(1000);
                Log.d(TAG, "Process '" + mTasks + " in Phase #2.");
                Thread.sleep(1000);
                Log.d(TAG, "Process '" + mTasks + " in Phase #3.");
                Thread.sleep(1000);
                Log.d(TAG, "Process '" + mTasks + " in Phase #4.");
                Thread.sleep(1000);
                Log.d(TAG, "Process '" + mTasks + " in Phase #5.");
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                Log.d(TAG, e.getMessage());
            }

            // Task is done and send update to UI thread
            Message msg = Message.obtain();
            msg.obj = mTasks + " is completed";

            // Sending status update to UI thread to update UI content
            mUIThreadHandler.sendMessage(msg);
        }
    }

    // No Delayed message handler
    class MessageHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;

        public MessageHandler(MainActivity myClassInstance) {
            myClassWeakReference = new WeakReference<MainActivity>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity myActivity = myClassWeakReference.get();
            if (myActivity != null) {
                String curStatus = "Status update : '" + msg.obj + "'";

                post(new TaskStatusUpdateRunnable(curStatus));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        try {
            Looper.myLooper().setMessageLogging(new LogPrinter(Log.DEBUG, TAG));
        } catch (NullPointerException e) {
            Log.d(TAG, e.getMessage());
        }
        */

        mUIThreadHandler = new MessageHandler(this);
    }

    private void createWorkerThreadToRunTask(String task)
    {
        // Update the current status before starting the task
        String curStatus = "Start task : '" + task + "'";
        TextView statusTextView = findViewById(R.id.TEXT_STATUS_ID);
        ScrollView scrollView = findViewById(R.id.SCROLLER_ID);

        String newStatus = statusTextView.getText() + "\n" + curStatus;
        statusTextView.setText(newStatus);
        scrollView.fullScroll(FOCUS_DOWN);

        Thread t = new Thread(new TaskRunnable(task));
        t.start();
    }

    public void onButtonClick(View v)
    {
        switch(v.getId())
        {
            case R.id.button1:
                createWorkerThreadToRunTask("Task 1");
                break;
            case R.id.button2:
                createWorkerThreadToRunTask("Task 2");
                break;
            case R.id.button3:
                createWorkerThreadToRunTask("Task 3");
                break;
            case R.id.buttonClear:
                TextView statusTextView = findViewById(R.id.TEXT_STATUS_ID);
                statusTextView.setText("");
                break;
            default:
                throw new RuntimeException("Unknow button ID");
        }
    }
}
