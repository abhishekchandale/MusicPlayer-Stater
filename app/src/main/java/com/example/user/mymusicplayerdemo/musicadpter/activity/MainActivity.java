package com.example.user.mymusicplayerdemo.musicadpter.activity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import in.co.teni.permissions.PermissionsActivity;

import com.example.user.mymusicplayerdemo.R;
import com.example.user.mymusicplayerdemo.musicadpter.adapter.MusicAdapter;
import com.example.user.mymusicplayerdemo.musicadpter.controller.MusicController;
import com.example.user.mymusicplayerdemo.musicadpter.service.MusicService;
import com.example.user.mymusicplayerdemo.musicadpter.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends PermissionsActivity implements MediaPlayerControl ,View.OnClickListener{

	//song list variables
	private ArrayList<Song> songList;
	private ListView songView;
	private RecyclerView mRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	private Button mBtn;
	private PermissionsActivity.IAppPermissionResult mReadExternalStorage=null;
	//service
	private MusicService musicSrv;
	private Intent playIntent;
	//binding
	private boolean musicBound=false;

	//controller
	private MusicController controller;

	//activity and playback pause flags
	private boolean paused=false, playbackPaused=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		//retrieve list view
		//songView = (ListView)findViewById(R.id.song_list);
		mRecyclerView=(RecyclerView)findViewById(R.id.music_recycler_view);
		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mBtn=(Button)findViewById(R.id.btn_open);
		//instantiate list
		songList = new ArrayList<Song>();
		//get songs from device
		//getSongList();
		checkReadExternalStorage();
		//sort alphabetically by title
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});
		//create and set adapter
		//SongAdapter songAdt = new SongAdapter(this, songList);
		//songView.setAdapter(songAdt);
		mBtn.setOnClickListener(this);
		//setup controller
		setController();
	}

	//connect to the service
	private ServiceConnection musicConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
			//get service
			musicSrv = binder.getService();
			//pass list
			musicSrv.setList(songList);
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	//start and bind the service when the activity starts
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent==null){
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	//user song select
	public void songPicked(View view){
		musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
		musicSrv.playSong();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

	public void songSelect(int position){
		musicSrv.setSong(position);
		musicSrv.playSong();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//menu item selected
		switch (item.getItemId()) {
		case R.id.action_shuffle:
			musicSrv.setShuffle();
			break;
		case R.id.action_end:
			stopService(playIntent);
			musicSrv=null;
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	//method to retrieve song info from device
	public void getSongList(){
		//query external audio
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		//iterate over results if valid
		if(musicCursor!=null && musicCursor.moveToFirst()){
			//get columns
			int titleColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.ARTIST);

			//add songs to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			} 
			while (musicCursor.moveToNext());
		}
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getPosn();
		else return 0;
	}

	@Override
	public int getDuration() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getDur();
		else return 0;
	}

	@Override
	public boolean isPlaying() {
		if(musicSrv!=null && musicBound)
			return musicSrv.isPng();
		return false;
	}

	@Override
	public void pause() {
		playbackPaused=true;
		musicSrv.pausePlayer();
	}

	@Override
	public void seekTo(int pos) {
		musicSrv.seek(pos);
	}

	@Override
	public void start() {
		musicSrv.go();
	}

	//set the controller up
	private void setController(){
		controller = new MusicController(this);
		//set previous and next button listeners
		controller.setPrevNextListeners(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPrev();
			}
		});
		//set and show
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
	}

	private void playNext(){
		musicSrv.playNext();
		if(playbackPaused){ 
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

	private void playPrev(){
		musicSrv.playPrev();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

	@Override
	protected void onPause(){
		super.onPause();
		paused=true;
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(paused){
			setController();
			paused=false;
		}
	}

	@Override
	protected void onStop() {
		controller.hide();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		stopService(playIntent);
		musicSrv=null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		MusicAdapter musicAdapter=new MusicAdapter(songList,MainActivity.this);
		mRecyclerView.setAdapter(musicAdapter);
	}

	public void checkReadExternalStorage(){
		if(isPermissionsGranted(STORAGE_PERMISSIONS)){
            getSongList();
		}else{
			checkForStoragePermission();
		}

	}
	private void checkForStoragePermission() {
		if (mReadExternalStorage == null) {
			mReadExternalStorage = new IAppPermissionResult() {
				@Override
				public void onGranted() {
					getSongList();
				}

				@Override
				public void onDenied(String[] strings) {
					requestOnNeverShowAgain(findViewById(android.R.id.content), "No Storage Permission");
				}

				@Override
				public void showRationale(String[] strings) {
					MainActivity.this.showRationale("We can't play music without this permission",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									checkForStoragePermission();
								}
							}, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}
					);
				}
			};
		}
		requestPermissions(PermissionsActivity.STORAGE_PERMISSIONS, mReadExternalStorage);
	}
}
