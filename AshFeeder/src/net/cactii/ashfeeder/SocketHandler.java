package net.cactii.ashfeeder;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class SocketHandler {
  // Intentional syntax error. Set this!
  private static final String FEEDER_URL = "http://your.host.name:port/feed.cgi?";
  private static final String MSG_TAG = "AshFeeder";
  
  private HttpEntity SendHttpRequest(String action) {

    HttpClient client = new DefaultHttpClient();
    HttpGet request = new HttpGet(FEEDER_URL + action);
    try {
      HttpResponse response = client.execute(request);
      StatusLine status = response.getStatusLine();
      if (status.getStatusCode() == 200) {
        HttpEntity entity = response.getEntity();
        return entity;
      } else {
        Log.d(MSG_TAG, "Status code bad: " + status.getStatusCode());
      }
      Log.d(MSG_TAG, "Error making http request, server returned " + status.getStatusCode());
    } catch (IOException e) {
      Log.d(MSG_TAG, "Error making http request: " + e.getMessage());
      e.printStackTrace();
    }
    
    return null;
  }
  
  public String GetStringResponse(String action)  {

    HttpEntity entity = SendHttpRequest(action);
    if (entity == null) {
      return "error";
    }
    String contentType = entity.getContentType().getValue();
    try {  
      if (contentType.equals("text/plain"))
        return this.ReadTextInputStream(entity.getContent());
      else
        Log.d(MSG_TAG, "Got unknown content type: " + contentType);
    } catch (IOException e) {
      Log.d(MSG_TAG, e.getMessage());
      e.printStackTrace();
    }
    return "error";
  }
  
  private String ReadTextInputStream(InputStream stream)  {
    DataInputStream reader = new DataInputStream(stream);
    String result = "";
    String s;
    boolean found_magic = false;
    try {
      while ((s = reader.readLine()) != null) {
        result += s + "\n";
      }
      return result;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "error";
  }
}
