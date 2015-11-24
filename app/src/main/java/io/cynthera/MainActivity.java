package io.cynthera;

import android.os.Handler;
import android.os.HandlerThread;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button btn1;
    EditText msg_text;
    ListView msg_list;
    FayeClient mClient;
    String user_id="2";
    String handshake_token="adhsu22u22_213";
    ArrayList<JSONObject> obj=new ArrayList<JSONObject>();
    MessageAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1=(Button) findViewById(R.id.button);
        msg_list=(ListView) findViewById(R.id.listView);
        msg_text=(EditText) findViewById(R.id.editText);
        MetaMessage meta = new MetaMessage();
        final JSONObject jsonExt = new JSONObject();
        JSONObject jsonId = new JSONObject();

        try {
            jsonExt.put("clientid", "/app/1785787rr6");
            jsonExt.put("token", "ano293h1ji_24u9214nrefDS_2349241412_df");
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
                fc.subscribeChannel("/messages/"+handshake_token);
            }

            @Override
            public void onDisconnectedServer(FayeClient fc) {
                Log.i("SOCKET CLOSE", "Disconnected");
            }

            @Override
            public void onReceivedMessage(FayeClient fc, String msg) {
                Log.i("SOCKET REC", "Message: " + msg);
                JSONObject msg_json;
                String sender_id;
                try {
                     msg_json = new JSONObject(msg);
                     sender_id=msg_json.getString("userid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    // Connect to server
        mClient.connectServer();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (msg_text.getText().toString().trim().length() > 0) {


                    // Include ext and id
                    JSONObject msg = new JSONObject();
                    JSONObject id= new JSONObject();
                    try {
                        msg.put("userid", user_id);
                        msg.put("msg", msg_text.getText().toString().trim());
                        id.put("id", 2);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    mClient.publish("/messages/" + handshake_token, msg.toString(),jsonExt.toString(),id.toString());
                    msg_text.setText("");
                    Log.d("SENT", jsonExt.toString());
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

}
