package com.rop.voicerecorder.ui.record;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rop.voicerecorder.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener{

    private NavController navController;
    private ImageButton listBtn, recordBtn;
    private boolean isRecording = false;
    TextView tvFileName;

    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;

    private MediaRecorder mediaRecorder;
    private String recordFile;
    private Chronometer recordTime;

    public RecordFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        listBtn = view.findViewById(R.id.btn_list);
        recordBtn = view.findViewById(R.id.btn_record);
        recordTime = view.findViewById(R.id.record_time);
        tvFileName = view.findViewById(R.id.record_filename);

        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_list:
                if (isRecording){
                Context context;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                        isRecording = false;
                    }
                });
                alertDialog.setNegativeButton("Cancel",null);
                alertDialog.setTitle("Audio still recording");
                alertDialog.setMessage("Are you sure you want to stop the recording?");
                alertDialog.create().show();
                }else{
                    navController.navigate(R.id.action_recordFragment_to_audioListFragment);
                }
                break;

            case R.id.btn_record:
                if(isRecording){
                    stopRecording();
                    recordBtn.setBackground(getResources().getDrawable(R.drawable.btn_normal));
                    isRecording = false;
                }else{
                    if(checkPermissions()){
                        isRecording = true;
                        startRecording();
                        recordBtn.setBackground(getResources().getDrawable(R.drawable.btn_recording));
                    }
                }
                break;
        }
    }

    private void startRecording() {
        recordTime.setBase(SystemClock.elapsedRealtime());
        recordTime.start();
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US);
        Date now = new Date();
        recordFile = "recording_"+formatter.format(now)+".3gp";

        tvFileName.setText("Recording file name: "+recordFile);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath+"/"+recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        mediaRecorder.start();
    }

    private void stopRecording() {
        tvFileName.setText("Recording stoped file saved: "+recordFile);
        recordTime.stop();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private boolean checkPermissions() {
        if(ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRecording();
    }
}