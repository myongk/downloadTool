package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    Button button;
    EditText editText;
    TextView text;
    String myURL;
    String val;
    //问题③下载路径问题
    final String path = Environment.getExternalStorageDirectory().getPath() + "/Download/";


    //问题②动态权限
    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }




//    //动态获取内存存储权限
//    public static void verifyStoragePermissions(Activity activity) {
//        // Check if we have write permission
//        int permission = ActivityCompat.checkSelfPermission(activity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE);
//        }
//    }

    public void downLoadFile() throws IOException {
        String fileName = "base.apk";
        String addres = val;
        //Log.e("myongk", myURL );
        //String addres = "https://ip4167885180.mobgslb.tbcache.com/fs01/union_pack/PPAssistant_3376273_PP_67.apk?ali_redirect_domain=alissl.ucdl.pp.uc.cn&ali_redirect_ex_ftag=acc4082e9712c2aa13b9b4df78965687dc624573e138d533&ali_redirect_ex_tmining_ts=1632550148&ali_redirect_ex_tmining_expire=3600&ali_redirect_ex_hot=100";


        Log.e("myongk", "kkkkkk" );
        URL url = new URL(addres);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(60000);
        //问题⑤
        con.setDoOutput(false);
        con.setDoInput(true);

        //con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //String ff = connection.getHeaderField("filename");
        //问题④获取包的名字问题

        //获取下载文件的头
        //String raw = URLDecoder.decode(connection.getHeaderField("Content-Disposition"),"UTF-8");
//        String fn = URLDecoder.decode(connection.getURL().toString(),"UTF-8");
//        System.out.println(fn);
//        System.out.println("------------"+ret+fn);
//        System.out.println("------------"+ret+fn.substring(fn.lastIndexOf("/")+1));
//        if(raw != null && raw.indexOf("=") >0){
//            file_name = raw.split("=")[1];
//            file_name = new String(file_name.getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
//        }
//
//        System.out.print(file_name);
        String ff = con.getURL().getFile();
        System.out.println(ff);
        System.out.println(ff.substring(ff.lastIndexOf('/')+1));
        String ff1 = ff.substring(ff.lastIndexOf('/')+1);
        System.out.println(ff1);
        int loc = ff1.indexOf("?");
        System.out.println(ff1.substring(0,loc));
        int ret = con.getResponseCode();
        Log.e("myongk", "downLoadFile: "+ret);
        InputStream is = con.getInputStream();


        //获取文件大小，来计算下载进度
        int size = con.getContentLength();
        System.out.println(size);

        File saveDir = new File(path);
        Log.e("myongk", path);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file1 = new File(saveDir+File.separator+0+ff1.substring(0,loc));
        OutputStream file = new FileOutputStream(file1);
        Log.e("myongk", saveDir+ff1.substring(0,loc));
        OutputStream os = new BufferedOutputStream(file);
        byte[] buff = new byte[1024];
        int len = 0;
        int cur = 0;
        System.out.print("hello1");
        Log.e("myongk", "downLoadFile: "+size);
        while((len=is.read(buff))!=-1){
            os.write(buff,0,len);
            cur += len;
            //System.out.print("hello");
            float process = (float)cur/(float)size*100;
            text.setText(String.valueOf(process)+ "%");
            //System.out.println((int)process);
            //System.out.println(sum);
        }

        os.close();
        is.close();
        con.disconnect();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAllPower();
        button = findViewById(R.id.button);
        editText = findViewById(R.id.textView);
        text = findViewById(R.id.textView2);

        text.setText("0" + "%");
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //问题①：类型转换
                String myURL = editText.getText().toString();
                val = String.valueOf(myURL);
                //Toast.makeText(MainActivity.this, myURL, Toast.LENGTH_SHORT).show();
                System.out.println(val);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            downLoadFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                //Toast.makeText(getApplicationContext(),URL,Toast.LENGTH_LONG).show();
            }
        });
    }
}
