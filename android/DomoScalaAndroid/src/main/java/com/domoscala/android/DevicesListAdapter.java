package com.domoscala.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;


public class DevicesListAdapter extends BaseExpandableListAdapter {

    private final List<DevicesListGroup> devicesListGroups;
    private final LayoutInflater inflater;
    private final DevicesActivity activity;


    public DevicesListAdapter(List<DevicesListGroup> devicesListGroups, DevicesActivity activity){
        this.devicesListGroups = devicesListGroups;
        this.activity = activity;
        inflater = LayoutInflater.from(this.activity);
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
            convertView = inflater.inflate(R.layout.listrowgroup_devices, parent, false);
        }
        CheckedTextView textView = (CheckedTextView) convertView;
        textView.setText(devicesListGroups.get(groupPosition).room);
        textView.setChecked(isExpanded);
        return textView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.listrowchild_devices, parent, false);
        }
        TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
        TextView deviceValueTextView = (TextView) convertView.findViewById(R.id.deviceValueTextView);
        final DevicesListGroup.DevicesListItem item = devicesListGroups.get(groupPosition).deviceItems.get(childPosition);
        deviceNameTextView.setText(item.deviceName);
        deviceValueTextView.setText(item.currentValue);
        // TODO set icon or something based on deviceType
        if(item.deviceType.equals("bulb") && !item.deviceName.contains("Button")){
            SeekBar seekBar = (SeekBar) convertView.findViewById(R.id.lampSeekBar);
            seekBar.setVisibility(View.VISIBLE);
            final int seekBarMax = 100;
            seekBar.setMax(seekBarMax);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    activity.setLampStatus(devicesListGroups.get(groupPosition).room, item.deviceName, seekBar.getProgress()/((float)seekBarMax));
                }
            });
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
