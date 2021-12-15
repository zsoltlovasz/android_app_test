package com.neti.celoxisinoutstatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private class StatusGetterSetter extends AsyncTask<String, Void, String> {
    
        @Override
        protected String doInBackground(String... params) {
            BufferedReader in = null;
            String data = null;
            String line = null;
            final String url = "http://target.server.name/status.cgi";
    
            try{
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = null;
                
                if(params[0].length()>0){
                    HttpPost httppost = new HttpPost(url);
                    List<NameValuePair> postParams = new ArrayList<NameValuePair>(2);
                    postParams.add(new BasicNameValuePair("inout", params[0]));
                    postParams.add(new BasicNameValuePair("status", params[1]));
                    httppost.setEntity(new UrlEncodedFormEntity(postParams));
                    response = client.execute(httppost);
                }
                
                URI website = new URI(url);
                HttpGet request = new HttpGet();
                request.setURI(website);
                response = client.execute(request);
    
                try{
                    data = new String();
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    while((line = in.readLine()) != null)data += line.trim();
                }
                finally{
                    in.close();
                }
            } 
            catch (Exception e){
                data += "Exception: "+e.toString();
            }
            return data;
        }
        
        @Override
        protected void onPostExecute(String result) {
            TextView act_inout_status = (TextView) findViewById(R.id.act_inout_status);
            TextView act_status_message = (TextView) findViewById(R.id.act_status_message);
            try{
                String[] parts = result.split(":", 2);
                if(parts.length>0){
    	            if(parts[0].equals("t"))act_inout_status.setText("Jelenleg be vagy jelentkezve.");
    	            if(parts[0].equals("f"))act_inout_status.setText("Jelenleg ki vagy jelentkezve.");
    	            if(parts.length>1)act_status_message.setText(parts[1]);
    	            else act_status_message.setText("(Nem adtál meg szöveges státuszt)");
                }else{
    	            act_status_message.setText("Hibas HTTP Response: "+result);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    
    }
    
    private boolean sendStatus(String inout){
    	//repa cuccos
    	EditText status_message = (EditText) findViewById(R.id.status_message);
        String status = status_message.getText().toString();
        
        new StatusGetterSetter().execute(inout, status);
        
    	return true;
    }
    
    public void sendLogout(View view) {
    	sendStatus("out");
    }

    public void sendLogin(View view) {
    	sendStatus("in");
    }

    public void sendRefresh(View view) {
    	sendStatus("");
    }

}
