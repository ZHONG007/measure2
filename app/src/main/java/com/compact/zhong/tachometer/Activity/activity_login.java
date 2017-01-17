package com.compact.zhong.tachometer.Activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.compact.zhong.tachometer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class activity_login extends AppCompatActivity {

    @BindView(R.id.Measureonly_button)  Button measureonly_button;
    @BindView(R.id.Measuresave_button) Button measuresave_button;
    @BindView(R.id.Setting_button)  Button setting_button;
    @BindView(R.id.aboutus_button) Button aboutus_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_menu);
        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

   @OnClick(R.id.Measureonly_button)
    public void measureBtnClick() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @OnClick(R.id.Measuresave_button)
    public void measuresaveBtnClick() {
        startActivity(new Intent(this, MainActivity_save.class));
        finish();
    }

    @OnClick(R.id.Setting_button)
    public void settingBtnClick() {
        startActivity(new Intent(this, setting_page.class));
        finish();
    }

    @OnClick(R.id.aboutus_button)
    public void aboutusBtnClick() {
        startActivity(new Intent(this, Aboutus_activity.class));
        finish();
    }


    @OnClick(R.id.aboutus_button)
    public void aboutusBtnCli() {
        startActivity(new Intent(this, Aboutus_activity.class));
        finish();
    }





}
