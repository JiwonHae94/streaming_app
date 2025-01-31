package com.skku.jiwon_hae.graduate_project_android_streaming.video_call.friends;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skku.jiwon_hae.graduate_project_android_streaming.R;
import com.skku.jiwon_hae.graduate_project_android_streaming.image.transform.CircleTransform;
import com.skku.jiwon_hae.graduate_project_android_streaming.main;
import com.skku.jiwon_hae.graduate_project_android_streaming.video_call.constants;
import com.skku.jiwon_hae.graduate_project_android_streaming.video_call.video_call_con;
import com.pubnub.api.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import static com.skku.jiwon_hae.graduate_project_android_streaming.video_call.service.video_call_service.mPubNub;

//import com.example.jiwon_hae.streaming_application.video_call.video_call_con;
//import com.example.jiwon_hae.streaming_application.video_call.video_call_connect;


/**
 * Created by jiwon_hae on 2017. 11. 28..
 */

public class friendListAdapter extends BaseAdapter {
    private Context mContext;
    private Activity mActivity;
    private String username;
    private ArrayList<String> dataList;
    private ArrayList<String> searchList;
    private ArrayList<String> resultList;

    private LayoutInflater layoutInflater;
    private android.support.v7.app.AlertDialog callDialog;

    private static boolean commandLineRun = false;
    private SharedPreferences sharedPref;
    private String keyprefResolution;
    private String keyprefVideoBitrateType;
    private String keyprefFps;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefRoomServerUrl;

    private String TAG = "video_call_adapter";

    //constants
    private constants video_call_constants = new constants();

    //VideoCall url
    private String connectURL = "https://appr.tc";

    public friendListAdapter(Context context, Activity activity, String userName){
        this.mContext = context;
        this.mActivity = activity;
        this.layoutInflater = LayoutInflater.from(mContext);
        this.dataList = new ArrayList<>();
        this.searchList = new ArrayList<>();

        this.username = userName;
        this.resultList = dataList;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        setDefaultConfig();

        keyprefFps = mContext.getString(R.string.pref_fps_key);

    }

    private void setDefaultConfig(){
        keyprefRoomServerUrl = mContext.getString(R.string.pref_room_server_url_key);
        keyprefResolution = mContext.getString(R.string.pref_resolution_key);
        keyprefVideoBitrateType = mContext.getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = mContext.getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateValue = mContext.getString(R.string.pref_startaudiobitratevalue_key);
        keyprefAudioBitrateType = mContext.getString(R.string.pref_startaudiobitrate_key);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Viewholder {
        ImageView userProfile;
        TextView userName;
        LinearLayout userLayout;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Viewholder viewholder = new Viewholder();
        convertView = layoutInflater.inflate(R.layout.video_call_call_item, null);

        viewholder.userProfile = (ImageView)convertView.findViewById(R.id.callerImage);
        viewholder.userName = (TextView)convertView.findViewById(R.id.callerName);
        viewholder.userLayout = (LinearLayout)convertView.findViewById(R.id.friends_layout);

        viewholder.userName.setText(resultList.get(position));

        Picasso.with(mContext)
                .load("http://ec2-13-125-170-236.ap-northeast-2.compute.amazonaws.com/streaming_application/profile_pic/" + resultList.get(position)+ ".jpg")
                .fit()
                .placeholder(R.drawable.default_icon)
                .transform(new CircleTransform())
                .into(viewholder.userProfile);

        viewholder.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlertDialog(resultList.get(position));
            }
        });

        return convertView;
    }

    public void add(String friend){
        dataList.add(friend);
        notifyDataSetChanged();
    }

    public void search(String friend){
        searchList.clear();
        for(int i =0 ; i < dataList.size();i++){
            if(dataList.get(i).matches(".*"+ friend+".*")){
                searchList.add(dataList.get(i));
            }
        }
        resultList = searchList;
        notifyDataSetChanged();
    }

    public void createAlertDialog(final String receiverName){
        View callerDialog = layoutInflater.inflate(R.layout.video_call_dialog, null);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setView(callerDialog);

        ImageView callerImage = (ImageView)callerDialog.findViewById(R.id.video_call_callerImage);
        Picasso.with(mContext)
                .load("http://bdc01051.cafe24.com/profile_pic/" + receiverName+ ".jpg")
                .fit()
                .placeholder(R.drawable.default_icon)
                .transform(new CircleTransform())
                .into(callerImage);

        TextView callerName = (TextView)callerDialog.findViewById(R.id.video_call_userName);
        callerName.setText(receiverName);

        Button call = (Button)callerDialog.findViewById(R.id.video_call_start);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCall(receiverName);
            }
        });

        callDialog = builder.create();
        callDialog.setCanceledOnTouchOutside(true);
        callDialog.show();

        WindowManager.LayoutParams params = callDialog.getWindow().getAttributes();
        params.height = 700;
        callDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        callDialog.getWindow().setAttributes(params);
    }

    public void onPause() {
        if (callDialog != null) {
            if (callDialog.isShowing()) {
                callDialog.dismiss();
            }
        }
    }

    private String stdByChannel;

    public void dispatchCall(final String callNum){

        mPubNub.hereNow(callNum, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                Log.e("MA-dC", "HERE_NOW: " +" CH - " + callNum + " " + message.toString());

                try {
                    int occupancy = ((JSONObject) message).getInt(constants.JSON_OCCUPANCY);

                    if (occupancy == 0) {
                        showToast("사용자가 온라인이 아닙니다");
                        return;
                    }

                    JSONObject jsonCall = new JSONObject();
                    jsonCall.put(constants.JSON_CALL_USER, username);
                    jsonCall.put(constants.JSON_CALL_TIME, System.currentTimeMillis());

                    mPubNub.publish(callNum, jsonCall, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            connectToRoom(callNum, false, false, false, 0);
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            super.disconnectCallback(channel, message);
                            Intent to_main = new Intent(mContext, main.class);
                            mActivity.startActivity(to_main);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.valueOf(mContext.getString(defaultId));
        if (useFromIntent) {
            return mActivity.getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = mContext.getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = mContext.getString(defaultId);
        if (useFromIntent) {
            String value = mActivity.getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = mContext.getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = mContext.getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return mActivity.getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = mContext.getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    private void connectToRoom(String roomId, boolean commandLineRun, boolean loopback,
                               boolean useValuesFromIntent, int runTimeMs) {
        this.commandLineRun = commandLineRun;

        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }

        String roomUrl = sharedPref.getString(
                keyprefRoomServerUrl, mContext.getString(R.string.pref_room_server_url_default));

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                video_call_con.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                video_call_con.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, video_call_con.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                video_call_con.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                video_call_con.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                video_call_con.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                video_call_con.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                video_call_con.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                video_call_con.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                video_call_con.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                video_call_con.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                video_call_con.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                video_call_con.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                video_call_con.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Enable level control.
        boolean enableLevelControl = sharedPrefGetBoolean(R.string.pref_enable_level_control_key,
                video_call_con.EXTRA_ENABLE_LEVEL_CONTROL, R.string.pref_enable_level_control_key,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, video_call_con.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = mActivity.getIntent().getIntExtra(video_call_con.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = mActivity.getIntent().getIntExtra(video_call_con.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, mContext.getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = mActivity.getIntent().getIntExtra(video_call_con.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, mContext.getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                video_call_con.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = mActivity.getIntent().getIntExtra(video_call_con.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = mContext.getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, mContext.getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = mActivity.getIntent().getIntExtra(video_call_con.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = mContext.getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, mContext.getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                video_call_con.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, video_call_con.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                video_call_con.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, video_call_con.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                video_call_con.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                video_call_con.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, video_call_con.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, video_call_con.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                video_call_con.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.e(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);

        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(mContext, video_call_con.class);
            intent.setData(uri);
            intent.putExtra(video_call_con.EXTRA_ROOMID, roomId);
            intent.putExtra(video_call_con.EXTRA_LOOPBACK, loopback);
            intent.putExtra(video_call_con.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(video_call_con.EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(video_call_con.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(video_call_con.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(video_call_con.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(video_call_con.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(video_call_con.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(video_call_con.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(video_call_con.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(video_call_con.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(video_call_con.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(video_call_con.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(video_call_con.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(video_call_con.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(video_call_con.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(video_call_con.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(video_call_con.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(video_call_con.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(video_call_con.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
            intent.putExtra(video_call_con.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(video_call_con.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(video_call_con.EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(video_call_con.EXTRA_DISPLAY_HUD, displayHud);
            intent.putExtra(video_call_con.EXTRA_TRACING, tracing);
            intent.putExtra(video_call_con.EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(video_call_con.EXTRA_RUNTIME, runTimeMs);

            intent.putExtra(video_call_con.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(video_call_con.EXTRA_ORDERED, ordered);
                intent.putExtra(video_call_con.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(video_call_con.EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(video_call_con.EXTRA_PROTOCOL, protocol);
                intent.putExtra(video_call_con.EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(video_call_con.EXTRA_ID, id);
            }

            if (useValuesFromIntent) {
                if (mActivity.getIntent().hasExtra(video_call_con.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                    String videoFileAsCamera =
                            mActivity.getIntent().getStringExtra(video_call_con.EXTRA_VIDEO_FILE_AS_CAMERA);
                    intent.putExtra(video_call_con.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
                }

                if (mActivity.getIntent().hasExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                    String saveRemoteVideoToFile =
                            mActivity.getIntent().getStringExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                    intent.putExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
                }

                if (mActivity.getIntent().hasExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                    int videoOutWidth =
                            mActivity.getIntent().getIntExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                    intent.putExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
                }

                if (mActivity.getIntent().hasExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                    int videoOutHeight =
                            mActivity.getIntent().getIntExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                    intent.putExtra(video_call_con.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
                }
            }

            mActivity.startActivity(intent);
        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getText(R.string.invalid_url_title))
                .setMessage(mContext.getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }

    private void showToast(final String message){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clear(){
        resultList.clear();
        notifyDataSetChanged();
    }

    public void initialize(){
        resultList = dataList;
        searchList.clear();
        notifyDataSetChanged();
    }
}
