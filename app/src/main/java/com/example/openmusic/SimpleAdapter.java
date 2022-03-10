package com.example.openmusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class SimpleAdapter extends BaseAdapter {
    private List<Song> list;
    private LayoutInflater layoutInflater;

    public SimpleAdapter(Context context, List<Song> list) {
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(view == null){
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }

        Song str = getStr(position);
        TextView textView = view.findViewById(R.id.txtListItem);
        textView.setText(str.getTitle());
        return view;
    }

    private Song getStr(int position){
        return (Song) getItem(position);
    }
}
