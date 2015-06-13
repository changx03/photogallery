package com.massey.fjy.photogallery.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.massey.fjy.photogallery.R;

import java.util.ArrayList;


public class ListFragment extends Fragment {
    public ListAdapter mAdapter;

    private ArrayList<ListElement> imageList;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //imageList get data
        imageList = new ArrayList<>();

        for (int i = 0; i < 100; i++)
            imageList.add(new ListElement("location" + i, "note" + i));
    }

    public class ListElement {
        public String location;
        public String note;

        public ListElement(String location, String note) {
            this.location = location;
            this.note = note;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.list, container, false);
        ListView lv = (ListView)root.findViewById(R.id.myList);
        mAdapter = new ListAdapter(getActivity(), imageList);
        lv.setAdapter(mAdapter);
        return root;
    }

    public class ListAdapter extends ArrayAdapter<ListElement> {
        public ListAdapter(Context context, ArrayList<ListElement> listImages) {
            super(context, 0, listImages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListElement le = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_adapter, parent, false);
            }

            TextView tvLocation = (TextView)convertView.findViewById(R.id.location);
            TextView tvNote = (TextView)convertView.findViewById(R.id.note);

            tvLocation.setText(le.location);
            tvNote.setText(le.note);

            ImageView iv = (ImageView)convertView.findViewById(R.id.image);
            iv.setImageResource(R.drawable.cat);

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

}
