package com.medassi.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class listAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private List<String> mCourseNames;
    private List<Integer> mProductIds;

    public listAdapter(Context context, List<String> courseNames, List<Integer> productIds) {
        super(context, R.layout.custom_layout, courseNames);
        mContext = context;
        mCourseNames = courseNames;
        mProductIds = productIds;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.custom_layout, parent, false);
        }

        TextView courseName = listItem.findViewById(R.id.course_name);
        Button sendIdButton = listItem.findViewById(R.id.button_send_id);

        courseName.setText(mCourseNames.get(position));

        // Set onClickListener for the button to send the ID
        sendIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call your function with the ID as JSON
                try {
                    JSONObject idJson = new JSONObject();
                    idJson.put("id", mProductIds.get(position));
                    // Call your function here passing idJson
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return listItem;
    }
}