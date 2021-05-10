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

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistsAdapter extends BaseAdapter {

    private final ArrayList<PlaylistSimple> mPlaylistArrayList;
    private Context mContext;

    public PlaylistsAdapter(Context context, ArrayList<PlaylistSimple> playlistArrayList ) {
        mContext = context;
        mPlaylistArrayList = playlistArrayList;
    }


    @Override
    public int getCount() {
        return mPlaylistArrayList.size();
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
            title.setText(mPlaylistArrayList.get(position).name);
            title.setSelected(true);

            if (mPlaylistArrayList.get(position).images.size() != 0) {
                Glide.with(mContext)
                        .load(mPlaylistArrayList.get(position).images.get(0).url)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imageView);

            } else {
                imageView.setImageResource(R.drawable.ic_track);
            }

            String artists = "";

//            subTitle.setText((mTrackArrayList.get(position).album.name));


        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}

