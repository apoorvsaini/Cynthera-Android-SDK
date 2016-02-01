package io.cynthera;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.elirex.fayeclient.FayeClient;
import com.elirex.fayeclient.FayeClientListener;
import com.elirex.fayeclient.MetaMessage;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    Button btn1;
    EditText msg_text;
    ListView msg_list;
    FayeClient mClient;

    //data of customer
    String user_id="2"; //id of customer
    String name="test",location="",token="";

    ArrayList<JSONObject> obj=new ArrayList<JSONObject>();
    ArrayList<JSONObject> message_list=new ArrayList<JSONObject>();
    MessageAdapter adapter;
    JSONObject jsonExt = new JSONObject();

    //data of Client Auth
    String CLIENT_TOKEN,SECRET,HANDSHAKE_TOKEN,CS_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1=(Button) findViewById(R.id.button);
        msg_list=(ListView) findViewById(R.id.listView);
        msg_text=(EditText) findViewById(R.id.editText);
        msg_list.setDivider(null);

        //initialize auth variables
        CLIENT_TOKEN = "/app/1785787rr6";
        SECRET = "ano293h1ji_24u9214nrefDS_2349241412_df";
        HANDSHAKE_TOKEN = "";

        //remember userid
        //save JSON of contacts and move ahead
        Context context = getApplicationContext();
        SharedPreferences session = getSharedPreferences("0", context.MODE_WORLD_READABLE);
        SharedPreferences.Editor prefEditor = session.edit();
        prefEditor.putString("userid", user_id);
        prefEditor.commit();


        //connect to server
        getHandshakeToken();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HANDSHAKE_TOKEN.length() > 0) {
                    if (msg_text.getText().toString().trim().length() > 0) {
                        String msg2=msg_text.getText().toString().trim();

                        //send message
                        // Include ext and id
                        JSONObject msg = new JSONObject();
                        JSONObject id = new JSONObject();
                        try {
                            msg.put("userid", user_id);
                            msg.put("username", "Test 6");
                            msg.put("msg", msg_text.getText().toString().trim());
                            id.put("id", 2);

                        } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                        mClient.publish("/messages/" + HANDSHAKE_TOKEN, msg.toString(), jsonExt.toString(), id.toString());
                        msg_text.setText("");
                        Log.d("SENT", jsonExt.toString());


                        //send message for saving
                        try {
                            Ion.with(getApplicationContext())
                                    .load("https://www.wizters.com/api/cynthera/save.php?handshake_token="+HANDSHAKE_TOKEN+"&message="+ URLEncoder.encode(msg2, "UTF-8")+"&token="+token+"&photo=0")
                                    .asString()
                                    .setCallback(new FutureCallback<String>() {
                                        @Override
                                        public void onCompleted(Exception e, String result) {
                                            // if status = 1 send message through socket

                                            Log.d("TOKEN", result);

                                            JSONObject jObject = null; // json
                                            try {
                                                jObject = new JSONObject(result);
                                                String status = jObject.getString("status");
                                                Log.d("STATUS", status);
                                                if (status.equals("1")) {
                                                //All good and message saved


                                                }
                                                else
                                                {
                                                    //Error
                                                    //handle it
                                                }
                                            } catch (JSONException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


    }

    public void stopFayeClient() {
        HandlerThread thread = new HandlerThread("TerminateThread");
        thread.start();
        new Handler(thread.getLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mClient.isConnectedServer()) {
                    mClient.disconnectServer();
                }
            }
        });
    }


    public void startFayeCLient(){
        MetaMessage meta = new MetaMessage();
        try {
            jsonExt.put("clientid", CLIENT_TOKEN);
            jsonExt.put("token", SECRET);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        meta.setAllExt(jsonExt.toString());
        mClient = new FayeClient("ws://www.wizters.com:9292/faye", meta);
        // Set FayeClient listener
        mClient.setListener(new FayeClientListener() {
            @Override
            public void onConnectedServer(FayeClient fc) {
                Log.i("SOCKET OPEN", "Connected");
                fc.subscribeChannel("/messages/" + HANDSHAKE_TOKEN);

                //let other user know
                // Include ext and id
                JSONObject msg = new JSONObject();
                JSONObject id = new JSONObject();
                try {
                    msg.put("userid", user_id);
                    msg.put("username","Test Customer");
                    msg.put("handshake", HANDSHAKE_TOKEN);
                    id.put("id", 2);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mClient.publish("/connected/" + CS_ID, msg.toString(), jsonExt.toString(), id.toString());


            }

            @Override
            public void onDisconnectedServer(FayeClient fc) {
                Log.i("SOCKET CLOSE", "Disconnected");
            }

            @Override
            public void onReceivedMessage(FayeClient fc, String msg) {
                Log.i("SOCKET REC", "Message: " + msg);
                JSONObject msg_json;
                String sender_id, sender_name;
                try {
                    msg_json = new JSONObject(msg);
                    obj.add(msg_json); //add new message object to the json
                    if(msg_json.has("transfer"))
                    {
                        //the chat has been transferred
                       Log.d("TRANSFER","YES");

                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new MessageAdapter(MainActivity.this, R.layout.temp, obj);
                            msg_list.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    Log.i("TOTAL REC", "OBJ " + obj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        // Connect to server
        mClient.connectServer();

    }

    public void getHandshakeToken()
    {


        Ion.with(getApplicationContext())
                .load("https://www.wizters.com/api/cynthera/init.php?uuid=" + user_id + "&name=" + name + "&location="+location)
                .setHeader("Authorization", CLIENT_TOKEN+":"+SECRET)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String response) {
                        // do stuff with the result or error
                        Log.d("TOKEN", response);

                        JSONObject jObject = null; // json
                        try {
                            jObject = new JSONObject(response);
                            String status = jObject.getString("status");
                            Log.d("STATUS", status);
                            if (status.equals("1")) {
                                HANDSHAKE_TOKEN=jObject.getString("handshake");
                                CS_ID=jObject.getString("cs_id");
                                token=jObject.getString("user_token");

                                //all good, now connect to socket
                                       // stopFayeClient();
                                        startFayeCLient();
                                JSONObject msg_json2 = null; // json
                                try {
                                     msg_json2 = new JSONObject(jObject.getString("messages"));
                                    for (int i=0; i<msg_json2.length();i++)
                                    {
                                        String index=String.valueOf(i);
                                        obj.add((JSONObject) msg_json2.get(index)); //add new message object to the json
                                        Log.d("MESSAGE REC", String.valueOf((JSONObject) msg_json2.get(index)));
                                    }

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter = new MessageAdapter(MainActivity.this, R.layout.temp, obj);
                                            msg_list.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                    Log.i("TOTAL REC", "OBJ " + obj);

                                } catch (JSONException e3) {
                                    e3.printStackTrace();
                                }




                            } else {
                                //error
                                stopFayeClient();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });



    }

}
