package net.cactii.ashfeeder;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  
    private Activity mMain;
    private ImageButton feedButton;
    private CheckBox testMode;
    private TextView lastFeed;
    private ProgressDialog mProgressDialog;
    private SharedPreferences mPreferences;
    private String lastFeedTime;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For now, a specific layout (fullscreen portrait)
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.main);
        this.mMain = this;
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        feedButton = (ImageButton)findViewById(R.id.feedButton);
        testMode = (CheckBox)findViewById(R.id.testMode);
        lastFeed = (TextView)findViewById(R.id.lastFeed);
       
        this.setLastFeed();
        
        feedButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            openFeedDialog();
          }
        });
    }
    
    public void setLastFeed() {
      this.lastFeedTime = this.mPreferences.getString("last_feed", "never");
      this.lastFeed.setText("I was last fed:\n" + this.lastFeedTime);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

      this.mProgressDialog = new ProgressDialog(this);
      this.mProgressDialog.setTitle("In progress");
      this.mProgressDialog.setMessage("Please wait...");
      this.mProgressDialog.setIndeterminate(false);
      this.mProgressDialog.setCancelable(true);
      return this.mProgressDialog;
    }
    
    public void openFeedDialog() {

      new AlertDialog.Builder(MainActivity.this).setTitle("Have I been good?")
              .setNegativeButton("No food!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  // pass
                }
              }).setPositiveButton("Yes, you have!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  showDialog(0);
                  new HttpRequestTask().execute(testMode.isChecked() ? "ping" : "feedme");
                }
              }).show();
    }
    
    private class HttpRequestTask extends AsyncTask<String, Void, String> {
      
      private String action;

      @Override
      protected String doInBackground(String... params) {
        SocketHandler handler = new SocketHandler();
        this.action = params[0];
        String response = handler.GetStringResponse(this.action);
        return response.replace("\n", "").replace("\r", "");
      }
      protected void onPostExecute(String result) {
        dismissDialog(0);
        
        
        if (result.equals("feedme successful")) {
          Toast.makeText(mMain, "Yum yum! Food!", Toast.LENGTH_SHORT).show();
          Calendar now = Calendar.getInstance();
          SimpleDateFormat dFormat = new SimpleDateFormat("EEE dd MMM @ HH:mm");
          
          Editor editor = mPreferences.edit();
          editor.putString("last_feed", dFormat.format(now.getTime()));
          editor.commit();
          
          setLastFeed();
        } else {
          Toast.makeText(mMain, "Feeder response: " + result, Toast.LENGTH_SHORT).show();
        }
      }
    }
}