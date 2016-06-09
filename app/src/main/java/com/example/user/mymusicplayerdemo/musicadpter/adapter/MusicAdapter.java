package com.example.user.mymusicplayerdemo.musicadpter.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.user.mymusicplayerdemo.R;
import com.example.user.mymusicplayerdemo.musicadpter.model.Song;
import com.example.user.mymusicplayerdemo.musicadpter.activity.MainActivity;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.DataObjectHolder> {

    private static String TAG = "MusicAdapter";
    private ArrayList<Song> mSongList;
    private MainActivity mContext;
    private MusicAdapter mMusicAdapter;
    public MusicAdapter(ArrayList<Song> songs, MainActivity context) {
        mSongList = songs;
        mContext = context;
    }
    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View OrderItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_row, parent, false);
        DataObjectHolder dataObjectHolder = new DataObjectHolder(OrderItemView, this);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
       Song songs = mSongList.get(position);
        try {
            //holder.song_id.setText(""+songs.getID());
            if(songs.getArtist()!=null ){
                holder.song_tittle.setText(songs.getTitle());
            }else{
                holder.song_tittle.setText("No Tittle");
            }
            if(songs.getArtist()!=null){
                holder.song_artist.setText(songs.getArtist());
            }else{
                holder.song_artist.setText("UnKnown");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception" + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }


    public class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
       // TextView song_id;
        TextView song_tittle;
        TextView song_artist;



        public DataObjectHolder(View itemView, MusicAdapter musicAdapter) {

            super(itemView);
            mMusicAdapter = musicAdapter;
           // song_id = (TextView) itemView.findViewById(R.id.song_id);
            song_tittle = (TextView) itemView.findViewById(R.id.song_tittle);
            song_artist = (TextView) itemView.findViewById(R.id.song_artist);
            itemView.setOnClickListener(this);

        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.song_row_layout:
                    int position = getAdapterPosition();
                    Song songs=mSongList.get(position);
                    mContext.songSelect((position));
                    mMusicAdapter.notifyDataSetChanged();
                    break;
            }

        }

    }
}
