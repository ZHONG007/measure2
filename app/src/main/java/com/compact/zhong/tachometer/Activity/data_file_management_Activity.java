package com.compact.zhong.tachometer.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.compact.zhong.tachometer.R;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by User on 03/11/2016.
 */

public class data_file_management_Activity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_CODE = 0;
    public static final int FILE_PICKER_REQUEST_CODE = 1;
    boolean chooseable=false;
    boolean deleteable=false;
    String pathname=null;
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_management);
        Button choosefile = (Button) findViewById(R.id.choose_file);
        TextView choosedfile = (TextView) findViewById(R.id.choosed_file);
        EditText filename= (EditText) findViewById(R.id.file_name);
        Button createfile = (Button) findViewById(R.id.create_file);
        Button deletefile = (Button) findViewById(R.id.delete_file);

        SharedPreferences sharedPref = getSharedPreferences("setting_file", MODE_PRIVATE);
        pathname=sharedPref.getString("name",null);
        TextView choosedfilein = (TextView) findViewById(R.id.choosed_file);
        choosedfile.setText(pathname);


        //intial_folder();


        choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseable=true;
                checkPermissionsAndOpenFilePicker();
            }
        });

        createfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_file();
            }
        });

        deletefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteable=true;
                checkPermissionsAndOpenFilePicker();
            }
        });
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                //showError();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            openFilePicker();
        }
    }

    private void intial_folder(){
        String Dir="Tacho_data";
        Storage storage = null;
        if (SimpleStorage.isExternalStorageWritable()) {
            storage = SimpleStorage.getExternalStorage();
        }
        else {
            storage = SimpleStorage.getInternalStorage(this);
        }

        if (!storage.isDirectoryExists(Dir)){
            try{
                storage.createDirectory(Dir);
            } catch (Exception e){

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {
                    //showError();
                }
            }
        }
    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1)
                .withFilterDirectories(false)
                .withFilter(Pattern.compile(".*\\.txt$"))
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withHiddenFiles(true)
                .start();
    }

    private void create_file()  {
        String Dir="Tacho_data";
        EditText lastname = (EditText) findViewById(R.id.file_name);
        String Filename=lastname.getText().toString();
        String filename=Filename+".txt";
        if(filename.equals(".txt")){
            Toast.makeText(this, "please type file name", Toast.LENGTH_SHORT).show();
        }
        else{
        Storage storage = null;
        if (SimpleStorage.isExternalStorageWritable()) {
            storage = SimpleStorage.getExternalStorage();
        }
        else {
            storage = SimpleStorage.getInternalStorage(this);
        }

        if (!storage.isDirectoryExists(Dir)){
            try{
                storage.createDirectory(Dir);

            } catch (Exception e){

            }
        }

        try {
            storage.createFile("Tacho_data", filename, "");
            File file = new File(filename);
            String pathname_tem = file.getPath();
            SharedPreferences.Editor editor = getSharedPreferences("setting_file", MODE_PRIVATE).edit();
            editor.putString("name", pathname_tem);
            editor.commit();
            TextView choosedfile = (TextView) findViewById(R.id.choosed_file);
            choosedfile.setText(pathname_tem);
            Toast.makeText(this, filename+"file has been saved in folder Tacho_data to be used", Toast.LENGTH_LONG).show();
        } catch (Exception e)
        {}
        }
}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            final String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            if (path != null) {
                Log.d("Path: ", path);
                SharedPreferences.Editor editor = getSharedPreferences("setting_file", MODE_PRIVATE).edit();
                if (chooseable==true){
                    chooseable=false;
                    TextView choosedfile = (TextView) findViewById(R.id.choosed_file);
                    choosedfile.setText(path);
                    editor.putString("name", path);
                    editor.commit();
                    Toast.makeText(this, path+" file has been saved in folder Tacho_data to be used", Toast.LENGTH_LONG).show();
                }
                else if(deleteable==true){
                    deleteable=false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Delete this file ?"+path)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        File f = new File(path);
                                        Boolean deleted = f.delete();
                                        SharedPreferences.Editor editor = getSharedPreferences("setting_file", MODE_PRIVATE).edit();
                                        editor.putString("name", null);
                                        editor.commit();
                                        TextView choosedfile = (TextView) findViewById(R.id.choosed_file);
                                        choosedfile.setText("");
                                        Toast.makeText(getApplicationContext(), path+"has been deleted", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                   // dialog.cancel();
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }
    }

}
