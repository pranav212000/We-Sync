package com.example.wesync.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wesync.R;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class TracksAdapter extends BaseAdapter {

    private final ArrayList<Track> mTrackArrayList;
    private Context mContext;

    public TracksAdapter(Context context, ArrayList<Track> trackArrayList) {
        mContext = context;
        mTrackArrayList = trackArrayList;
    }


    @Override
    public int getCount() {
        return mTrackArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.item_track_grid, null);
            TextView title = (TextView) grid.findViewById(R.id.title);
//            TextView subTitle = (TextView) grid.findViewById(R.id.subtitle);
            ImageView imageView = (ImageView) grid.findViewById(R.id.track_image);
            title.setText(mTrackArrayList.get(position).name);

            if (mTrackArrayList.get(position).album.images.size() != 0)
                Glide.with(mContext)
                        .load(mTrackArrayList.get(position).album.images.get(0).url)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imageView);


            String artists = "";
            for (ArtistSimple artist : mTrackArrayList.get(position).artists) {
                artists = artists + ", " + artist.name;

            }
//            subTitle.setText((mTrackArrayList.get(position).album.name));


        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
