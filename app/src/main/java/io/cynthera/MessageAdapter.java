package io.cynthera;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



/**
 * Created by apoorvsaini on 11/23/15.
 */
public class MessageAdapter  extends ArrayAdapter<JSONObject> {
    private ArrayList<JSONObject> items;
    private Context context;
    View vv;
    Bitmap thumb;
    String to,from,stamp,body,from_name,id;

    public MessageAdapter(Context context, int textViewResourceId,
                       ArrayList<JSONObject> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        //thumb = BitmapFactory.decodeResource(context.getResources(),);


    }

    static class StreamViewHolder {
        TextView senderMsg,recMsg;

        RelativeLayout senderBubble,recBubble;

    }

    private void showtoast(String T) {
        Toast t = Toast.makeText(context, T, Toast.LENGTH_LONG);
        t.show();
    }

    public View getView(final int position, View convertView,  ViewGroup parent) {
        final StreamViewHolder viewHolder;
        View view = convertView;
        String desc="";
        //p=position;


        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.message_adapter, null);
            viewHolder = new StreamViewHolder();
            viewHolder.senderMsg = (TextView) view.findViewById(R.id.sender_text);
            viewHolder.recMsg = (TextView) view.findViewById(R.id.rec_text);
           // viewHolder.senderBubble = (RelativeLayout) view.findViewById(R.id.senderBubble);
            //viewHolder.recBubble = (RelativeLayout) view.findViewById(R.id.recieverBubble);


            Typeface font = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
            viewHolder.senderMsg.setTypeface(font);	//set roboto
            viewHolder.recMsg.setTypeface(font);
            view.setTag(viewHolder);
        } else {
            viewHolder = (StreamViewHolder) view.getTag();
        }
        vv = view;
        final JSONObject item = items.get(position);
        final JSONObject fitem = item;
        if (item != null) {
            try {

                body=item.getString("msg");
                from=item.getString("userid");
                //from_name=item.getString("username");
                SharedPreferences prefs =context. getSharedPreferences("0", context.MODE_WORLD_READABLE);
                id = prefs.getString("userid", "");
                if(id.equals(from))
                {
                    //user it the sender
                   // viewHolder.recBubble.setVisibility(View.GONE);
                    //viewHolder.senderBubble.setVisibility(View.VISIBLE);


                    viewHolder.recMsg.setVisibility(View.GONE);
                    viewHolder.senderMsg.setVisibility(View.VISIBLE);
                    viewHolder.senderMsg.setText(body);
                }
                else
                {
                    viewHolder.recMsg.setVisibility(View.VISIBLE);
                    viewHolder.senderMsg.setVisibility(View.GONE);
                    viewHolder.recMsg.setText(body);
                }






            }catch (JSONException e) {
                e.printStackTrace();
            }


        }
        final StreamViewHolder VH = viewHolder;





        // add button listener


        return view;
    }




}
