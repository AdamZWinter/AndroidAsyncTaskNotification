package edu.greenriver.sdev450.asynctaskassignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private ProgressBar mDownloadProgressBar;
    String channelId = "MyNotification";
    int notificationId = 1;

    String[] urls = {"https://google.com/", "https://wikipedia.org/", "http://mit.edu/", "placeholder"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.editText = findViewById(R.id.editTextUrl);
        this.button = findViewById(R.id.button);
        this.mDownloadProgressBar = findViewById(R.id.progressBar);

        this.button.setOnClickListener(this::buttonClick);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void buttonClick(View view){
        String url = editText.getText().toString();
        this.urls[3] = url;
        DownloadUrlsTask task = new DownloadUrlsTask(this);
        //task.execute("https://google.com/", "https://wikipedia.org/", "http://mit.edu/", url);
        task.execute(this.urls);
    }

    public String[] getUrls(){return this.urls;}

    private boolean downloadUrl(String url) {
        try {
            // Put thread to sleep for one second
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            // Ignore
        }
        return true;
    }

    private static class DownloadUrlsTask extends AsyncTask<String, Integer, Integer> {

        private final WeakReference<MainActivity> mActivity;

        public DownloadUrlsTask(MainActivity context) {
            mActivity = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().mDownloadProgressBar.setProgress(0);
        }

        @Override
        protected Integer doInBackground(String... urls) {
            int downloadSuccess = 0;
            for (int i = 0; i < urls.length; i++) {
                MainActivity mainActivity = mActivity.get();
                if (mainActivity == null || mainActivity.isFinishing()) return downloadSuccess;

                if (mainActivity.downloadUrl(urls[i])) {
                    downloadSuccess++;
                }
                publishProgress((i + 1) * 100 / urls.length);
            }
            return downloadSuccess;
        }

        protected void onProgressUpdate(Integer... progress) {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null || mainActivity.isFinishing()) return;

            mainActivity.mDownloadProgressBar.setProgress(progress[0]);
        }

        protected void onPostExecute(Integer numDownloads) {
            MainActivity mainActivity = mActivity.get();
            mainActivity.notifyMe();
            if (mainActivity == null || mainActivity.isFinishing()) return;

        }
    }

    public void notifyMe(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setContentTitle("Download Notification");
        builder.setContentText(""+ urls[3] +" is downloaded.");
        builder.setSmallIcon(R.drawable.baseline_airline_stops_24);
        //builder.setLargeIcon(bitmap);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(notificationId++, builder.build());
    }


}