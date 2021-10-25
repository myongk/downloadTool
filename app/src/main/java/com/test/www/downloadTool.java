package com.test.www;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class downloadTool {


    String downloadURL;
    int threadCount;
    Boolean stop = false;
    Boolean cancel = false;
    public int process = 0;

    float sum_size = 0;
    float down_size = 0;
    Handler myHandler = null;


    final String path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
    String localPath = path;
    File target_file;

    //File file = new File(saveDir+File.separator+0+ff1.substring(0,loc));

//    public downloadTool(String downloadURL, int threadCount) throws IOException {
//        this.downloadURL = downloadURL;
//        this.threadCount = threadCount;
//
//        Log.e("myongk", "downloadTool: " + downloadURL);
//    }

    public downloadTool() throws IOException {
        Log.e("myongk", "downloadTool: " + downloadURL);
    }

    public void getFileName(HttpURLConnection con) throws IOException {
        String ff = con.getURL().getFile();
        System.out.println(ff);
        System.out.println(ff.substring(ff.lastIndexOf('/')+1));
        String ff1 = ff.substring(ff.lastIndexOf('/')+1);
        System.out.println(ff1);
        int loc = ff1.indexOf("?");
        System.out.println(ff1.substring(0,loc));
        //localPath = path+File.separator+0+ff1.substring(0,loc);
        localPath = path+File.separator+26+ff1.substring(0,loc);

        File saveDir = new File(path+File.separator);
        Log.e("kkk", "saveDir: "+ saveDir.exists());
        if (!saveDir.exists()) {
            Log.e("myongk", "saveDir: " + path);
            saveDir.mkdir();
            Log.e("myongk", "saveDir1: " + path);
        }

        //这里下载明在得加0，不然出现如下错误
        //java.io.FileNotFoundException: /storage/emulated/0/Download/PPAssistant_3376273_PP_67.apk: open failed: EEXIST (File exists)
        //该问题还没找到答案
        target_file = new File(localPath);
        Log.e("kkk", "target_file: "+localPath+"   "+ target_file.exists());
        if(!target_file.exists()){
            Log.e("kkk", "target_file: "+localPath);
            boolean flag = false;
            try {
                flag = target_file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();


            }
            Log.e("kkk", "target_file1: "+saveDir+File.separator+ff1.substring(0,loc)+"   "+flag);
        }
    }


    public void startDownload(String downloadURL, int threadCount, Handler handler) throws IOException {
//        URL url = new URL(downloadURL);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setConnectTimeout(10000);
//        connection.setRequestProperty("Range","bytes=0-20");
//        if(connection.getResponseCode()==206){
//            InputStream inputStream = connection.getInputStream();
//            //String result = getStringFromStream(inputStream);
//        }

//        URL url = new URL(downloadURL);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setConnectTimeout(10000);
//
//
//        connection.setDoOutput(false);
//        connection.setDoInput(true);

        this.downloadURL = downloadURL;
        this.threadCount = threadCount;
        this.myHandler = handler;

        this.cancel = false;
        this.stop = false;
        this.process = 0;
        this.down_size = 0;


        URL url = new URL(downloadURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(60000);
        //问题⑤ post类型DoOutput需要false
        connection.setDoOutput(false);
        connection.setDoInput(true);

        //connection.setRequestProperty("Range","bytes=0-20");
        Log.e("myongk1", "startDownload: ");
        int code = connection.getResponseCode();

        Log.e("myongk1", "startDownload2: ");
        if(code==200){
            Log.e("myongk1", "startDownload1: ");
            getFileName(connection);
            //获取要下载的文件长度
            int length = connection.getContentLength();
            this.sum_size = length;
            Log.e("myongk1", "startDownload1: "+length);
            Log.e("myongk1", "startDownloadkkkk: ");
            //在本地创建一个一样大小的文件
            RandomAccessFile file = new RandomAccessFile(target_file,"rw");


            Log.e("myongk1", "startDownloadkkkk: ");
            file.setLength(length);
            //计算每一个线程要下载多少数据
            int blockSize = length/threadCount;
            Log.e("myongk1", "startDownloadkkkk: ");
            //计算每一个线程要下载的数据范围
            for(int i=0;i<threadCount;i++){
                int startIndex = i*blockSize;
                int endIndex = (i+1)*blockSize -1;
                if(i == threadCount -1){
                    endIndex = length -1;
                }
                Log.e("myongk1", "startDownloadkkkk: " + startIndex + "   "+ endIndex);
                new downloadThread(startIndex,endIndex,i,threadCount).start();
            }
        }

    }

    public void stopDownload(){
        this.stop = true;
    }

    public void cancelDownload(){
        this.cancel = true;
    }

    public class downloadThread extends Thread{

        int startIndex;
        int endIndex;
        int threadId;
        int sum;
        int threadNum;

        public downloadThread(int startIndex, int endIndex, int threadId, int threadNum) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.threadId = threadId;
            this.threadNum = threadNum;
            this.sum = endIndex - startIndex;
        }

        @Override
        public void run() {
            try {

                //读取出记录下来的位置
                File temp = new File(localPath+threadId+".log");
                Log.e("myongk1", "run: "+threadId);
                if(temp!=null && temp.length()>0){
                    Log.e("myongk1", "temp: "+threadId);
                    //说明日志文件有内容
                    FileInputStream fis = new FileInputStream(temp);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    String result = reader.readLine();
                    //读出记录下来的位置更新下载请求数据的起始位置
                    int res = Integer.parseInt(result);
                    synchronized (this){
                        down_size  = down_size + (res-this.startIndex);
                    }
                    this.startIndex = res;
                }




                String fileName = "base.apk";
                File saveDir = new File(path+fileName);

                if (!saveDir.exists()) {
                    saveDir.mkdir();
                }

                URL url = new URL(downloadURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                //设置Range头用计算好的开始索引和结束索引到服务端请求数据
                connection.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);

                Log.e("myongk1", "code: "+connection.getResponseCode()+startIndex+"   " + endIndex);
                if(connection.getResponseCode()==206){

                    InputStream inputStream = connection.getInputStream();
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    //RandomAccessFile file = new RandomAccessFile(getFileName(path),"rw");
                    Log.e("myongk1", "kkkkk: ");
                    //RandomAccessFile file = new RandomAccessFile(saveDir,"rw");
                    RandomAccessFile file = new RandomAccessFile(target_file,"rw");
                    Log.e("myongk1", "kkkkk11111: ");
                    //一定不要忘记 要seek到startIndex位置 写入数据
                    file.seek(startIndex);
                    int count = 0;
                    while ((len=inputStream.read(buffer))!=-1){
                        file.write(buffer,0,len);
                        count+=len;
                        int position = count+startIndex;
                        RandomAccessFile temp1 = new RandomAccessFile(localPath+threadId+".log","rwd");
                        temp1.write(String.valueOf(position).getBytes());

                        synchronized (this){
                            Log.e("myongk1", "threadId: " + threadId + "   " + "count:" + count + "    "+"sum " + sum);
                            float cur_process = (float)count/(float)(sum)*100;
                            down_size+=len;
                            Log.e("myongk1", "threadId: " + threadId + "   " + "sum_size:" + sum_size );
                            //process+=(cur_process/threadNum);
                            process = (int) (down_size*100/sum_size);
                            if( (sum_size - down_size )<1 ){
                                process = 100;
                            }
                            Message msg = new Message();
                            msg.what = process;
                            myHandler.sendMessage(msg);

                            Log.e("myongk1", "threadId: " + threadId + "   " + "process:" + process );
                        }
                        if(cancel){
                            if(temp.exists()){
                                temp.delete();
                            }

                            if(saveDir.exists()){
                                saveDir.delete();
                            }
                            file.close();
                            return;
                        }
                        else if(stop){
                            file.close();
                            return;
                        }
                    }
                    file.close();
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
