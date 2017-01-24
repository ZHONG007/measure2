package com.compact.zhong.tachometer.Activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.nfc.Tag;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.compact.zhong.tachometer.R;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.attr.path;
import static android.content.Context.MODE_PRIVATE;
import static com.compact.zhong.tachometer.R.xml.pref_notification;
import static com.compact.zhong.tachometer.R.xml.setting_main;

public class MainActivity_save extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    private static final String TAG = "MainActivity";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    Handler mHandler;

    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    String filename_saved;
    String pathname=null;



    @BindView(R.id.titleMaxSpeed_unit)
    TextView titlemaxspeed_unit;

    @BindView(R.id.maxSpeed)
    TextView maxspeed;

    @BindView(R.id.averageSpeed)
    TextView averagespeed;

    @BindView(R.id.MinSpeed)
    TextView minspeed;



    TextView currentspeedsave;

    @BindView(R.id.buttonStartsave)  Button startbutton;
    @BindView(R.id.buttonStopsave) Button stopbutton;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            //if (pathname!=null)
            String data = null;
           // BufferedWriter bw = null;
           // FileWriter fw = null;

            //FileOutputStream outputStreamn=FileOutputStream(file);
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                //tvAppend(currentspeedsave, data);
                if (mHandler != null)
                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT, data).sendToTarget();
                File file = new File(pathname);
                try {
                    FileWriter fw = new FileWriter(file,true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.append(data);
                    bw.flush();
                    bw.close();
                }
                catch (IOException e)
                { }
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    };


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted =
                        intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            //setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                           // tvAppend(currentspeedsave,"Serial Connection Opened!\n");
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStartsave(startbutton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStopsave(stopbutton);
               // Log.d("SERIAL", "show off");

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain_save1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Measurement Save");
        Get_settinginformation();
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        currentspeedsave = (TextView) findViewById(R.id.currentSpeedsave);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                {
                    currentspeedsave.setText((String)msg.obj);
                }
                super.handleMessage(msg);
            }
        };




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this) ;
        String StoredValue=prefs.getString("sync_frequency", "rmp");
        //currentspeedsave.setText("StoredValue");

        TextView titlemaxspeed_unit = (TextView) findViewById(R.id.titleMaxSpeed_unit);
        titlemaxspeed_unit.setText(StoredValue);

        TextView titleaveragespeed_unit = (TextView) findViewById(R.id.titleAverageSpeed_unit);
        titleaveragespeed_unit.setText(StoredValue);

        TextView titleminspeed_unit = (TextView) findViewById(R.id.titleMinSpeed_unit);
        titleminspeed_unit.setText(StoredValue);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            startActivity(new Intent(this, activity_login.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
          //  return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            startActivity(new Intent(this, activity_login.class));
            finish();
        } else if(id == R.id.measure_only_nav) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(this, MainActivity_save.class));
            finish();
        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(this, setting_page.class));
            finish();
        } else if (id == R.id.nav_manage) {
            startActivity(new Intent(this, Aboutus_activity.class));
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClickStartsave(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();

        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            //tvAppend(currentspeedsave,"BBC");
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                SharedPreferences sharedPref = getSharedPreferences("setting_file", MODE_PRIVATE);
                pathname=sharedPref.getString("name",null);
               if (pathname==null){
                create_file();
               }
                if (deviceVID == 0x0403)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;

                } else {
                    connection = null;
                    device = null;
                    //tvAppend(currentspeedsave,"BBC2");
                }

                if (!keep)
                    break;
            }
        }
    }


    public void onClickStopsave(View view) {
       // serialPort.close();
        //currentspeedsave.setText("close port");
       // tvAppend(currentspeedsave,"Closed!");
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

       // runOnUiThread(new Runnable() {
           // @Override
            //public void run() {
                ftv.setText(ftext);
           // }
       // });
    }

    public static void Savedata(File file, String[] data)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());
                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }

    private void Createfolder()
    {
        String folder_main="tachometer";
        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    private void create_file()  {
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

        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;

            try {
                storage.createFile("Tacho_data", dateFormat.format(date)+".txt", "");
                File file = new File(dateFormat.format(date)+".txt");
                pathname = file.getPath();
                SharedPreferences.Editor editor = getSharedPreferences("setting_file", MODE_PRIVATE).edit();
                editor.putString("name", pathname);
                editor.commit();

            } catch (Exception e){
            }
    }


    private void Get_settinginformation()
    {
        SharedPreferences prefs = getSharedPreferences("setting_file", MODE_PRIVATE);
        String restoredText = prefs.getString("text", null);
        if (restoredText != null) {
            filename_saved = prefs.getString("name", "No name defined");//"No name defined" is the default value.
            int idName = prefs.getInt("idName", 0); //0 is the default value.
    }
    }


}
