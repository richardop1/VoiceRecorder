package com.rop.voicerecorder.ui.audiolist;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.rop.voicerecorder.R;
import com.rop.voicerecorder.adapters.AudioListAdapter;

import java.io.File;
import java.io.IOException;

public class AudioListFragment extends Fragment implements  AudioListAdapter.onItemListClick{
    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList;
    private File[] allFiles;
    private AudioListAdapter adapter;


    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay;

    //UI elements
    private ImageButton playBtn;
    private TextView tvPlayerHeader, tvPlayerFilename;
    private SeekBar playerSeekBar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.audio_list_view);

        playBtn = view.findViewById(R.id.player_play_btn);
        tvPlayerHeader = view.findViewById(R.id.player_header_title);
        tvPlayerFilename = view.findViewById(R.id.player_filename);
        playerSeekBar = view.findViewById(R.id.player_seekBar);



        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();
        adapter = new AudioListAdapter(allFiles,this);
        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(adapter);




        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    pauseAudio();
                }else{
                    if(fileToPlay != null){
                        resumeAudio();
                    }
                }
            }
        });

        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(fileToPlay != null){
                    pauseAudio();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(fileToPlay != null){
                    int progress = seekBar.getProgress();
                    resumeAudio();

                }
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
        if(isPlaying){
            stopAudio();
            playAudio(fileToPlay);
        }else{
            fileToPlay = file;
            playAudio(fileToPlay);
        }
    }

    private void stopAudio() {
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_play,null));
        isPlaying = false;
        tvPlayerHeader.setText("Playing");
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }

        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_pause,null));
        tvPlayerFilename.setText(fileToPlay.getName());
        tvPlayerHeader.setText("Playing");
        isPlaying = true;
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopAudio();
                tvPlayerHeader.setText("Finished");
            }
        });
        playerSeekBar.setMax(mediaPlayer.getDuration());
        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar,0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this,500);
            }
        };
    }

    private void pauseAudio(){
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_play,null));
        isPlaying=false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }
    private void resumeAudio(){
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_baseline_pause,null));
        isPlaying=true;
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar,0);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying){
            stopAudio();
        }
    }
}