package com.minhtetoo.systemfontchanger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv_is_rooted)
    TextView tvRooted;

    public static final String TARGET_BASE_PATH = "/data/data/com.minhtetoo.systemfontchanger/";
    private int myFont;
    private AlertDialog.Builder ab;
    private ProgressDialog pd;

    private String[] strName = new String[]{"DroidSans.tff", "DroidSans-Bold.tff"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this, this);

        ab = new AlertDialog.Builder(this);
        pd = new ProgressDialog(this);

        if (isRooted() == true) {

            tvRooted.setText("Device is rooted");
            tvRooted.setTextColor(Color.GREEN);
        } else {
            tvRooted.setText("Device is not rooted");
            tvRooted.setTextColor(Color.RED);
        }

        File restoreFile = new File(TARGET_BASE_PATH + "restore.ttf");

        if (!restoreFile.exists()) {
            copyFile("unicode.ttf");
            copyFile("zawgyi.ttf");
            backUpFile();
        }
    }

    public static boolean isRooted() {
        boolean isRooted = false;
        if (!isRooted) {
            String[] file = new String[]{"/sbin/", "/system/bin", "/system/xbin/", "/data/local/xbin/"
                    , "/system/bin/failsafe/", "/data/local"};

            for (String search : file) {
                if (new File("search" + "su").exists()) {
                    isRooted = true;
                    break;
                }
            }
        }
        return isRooted;
    }

    private void copyFile(String fileName) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            in = assetManager.open(fileName);
            newFileName = TARGET_BASE_PATH + fileName;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void backUpFile() {
        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        File font = new File("/system/fonts/DroidSans.ttf");
        try {
            in = new FileInputStream(font);
            newFileName = TARGET_BASE_PATH + "/restore.ttf";
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class Loading extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setTitle("Installation");
            pd.setMessage("Please Wait....");
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            systemWrite();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
            reboot();
        }
    }

    private void systemWrite() {
        String name = "";
        if (myFont == 0) {
            name = "unicode.ttf";
        } else if (myFont == 1) {
            name = "zawgyi.ttf";
        } else {
            name = "restore.ttf";
        }
        Process process;
        try {
            for (int i = 0; i < strName.length; i++) {
                process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());
                out.writeBytes("mount -o remount,rw -t yaffs2/dev/block/" +
                        "mtdblock3/system\n");
                out.writeBytes("cat" + TARGET_BASE_PATH + name + "> /system/fonts/" + strName[i] + "\n");
                out.writeBytes("exit\n");
                out.flush();
                process.waitFor();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void reboot() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_one)
    public void onTapZawGyi(View view) {
        if (isRooted()) {
            ab.setTitle("Action");
            ab.setMessage("Are you sure to install Zawgyi Font? ");
            ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Loading().execute();
                    myFont = 1;
                }
            }).setNegativeButton("Cancel", null).show();
        } else {
            Toast.makeText(getApplicationContext(), "Device not rooted", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_two)
    public void onTapUniCode(View view) {

        if (isRooted()) {
            ab.setTitle("Action");
            ab.setMessage("Are you sure to install Unicode Font? ");
            ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Loading().execute();
                    myFont = 0;
                }
            }).setNegativeButton("Cancel", null).show();
        } else {
            Toast.makeText(getApplicationContext(), "Device not rooted", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_three)
    public void onTapRestore(View view) {
        if (isRooted()) {
            ab.setTitle("Action");
            ab.setMessage("Are you sure to restore original Font? ");
            ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Loading().execute();
                    myFont = 2;
                }
            }).setNegativeButton("Cancel", null).show();
        } else {
            Toast.makeText(getApplicationContext(), "Device not rooted", Toast.LENGTH_LONG).show();
        }
    }
}
