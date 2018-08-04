package com.benputao.livewallpaper.snowwallpaper;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    private static final String PRE_FILE="treepre";
    private SeekBar seekBar;
    private TextView textView;
    private Spinner spinner;
    private String[] list=new String[]{"随时间变化","春季","夏季","秋季","冬季"};
    private int flow_num,season;
    private Button setwallpaper;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences=getSharedPreferences(PRE_FILE,MODE_PRIVATE);
        editor=preferences.edit();
        flow_num=preferences.getInt("flow_num",20);
        season=preferences.getInt("season",0);
        Log.i("---","season="+season+"   flow_num="+flow_num);

        setContentView(R.layout.activity_setting);
        seekBar= (SeekBar) findViewById(R.id.seekbar_setting);
        textView= (TextView) findViewById(R.id.textview_setting);
        spinner= (Spinner) findViewById(R.id.spinner_setting);
        setwallpaper= (Button) findViewById(R.id.setwallpaper);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("漂浮数量："+progress);
                flow_num=progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                season=position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                season=0;
            }
        });
        seekBar.setProgress(flow_num);
        spinner.setSelection(season,true);
        textView.setText("漂浮数量："+flow_num);

        setwallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName componentName = new ComponentName("com.benputao.livewallpaper.snowwallpaper", "com.benputao.livewallpaper.snowwallpaper.SnowLiveWallPaper");
                Intent intent;
                if (android.os.Build.VERSION.SDK_INT < 16) {
                    intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                } else {
                    intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
                    intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", componentName);
                }
                SettingActivity.this.startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.putInt("flow_num",flow_num);
        editor.putInt("season",season);
        editor.commit();
    }
}
