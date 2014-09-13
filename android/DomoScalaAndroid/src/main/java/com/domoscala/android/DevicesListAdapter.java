package com.domoscala.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class DevicesListAdapter extends BaseExpandableListAdapter {

    private final List<DevicesListGroup> devicesListGroups;
    private final LayoutInflater inflater;
    private final Context context;


    public DevicesListAdapter(List<DevicesListGroup> devicesListGroups, Context context){
        this.devicesListGroups = devicesListGroups;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return devicesListGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return devicesListGroups.get(groupPosition).deviceItems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return devicesListGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return devicesListGroups.get(groupPosition).deviceItems.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.listrowgroup_devices, null);
        }
        CheckedTextView textView = (CheckedTextView) convertView;
        textView.setText(devicesListGroups.get(groupPosition).room);
        textView.setChecked(isExpanded);
        return textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.listrowchild_devices, null);
        }
        TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
        TextView deviceValueTextView = (TextView) convertView.findViewById(R.id.deviceValueTextView);
        DevicesListGroup.DevicesListItem item = devicesListGroups.get(groupPosition).deviceItems.get(childPosition);
        deviceNameTextView.setText(item.deviceName);
        deviceValueTextView.setText(item.currentValue);
        // TODO set icon or something based on deviceType
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO do something (open a dialog?) depending on the deviceType
                Toast.makeText(context, "Buuuu!!", Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
