package com.test.www;

import android.os.Environment;

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
    float process = 0;
    final String path = Environment.getExternalStorageDirectory().getPath() + "/Download/";
    String localPath = path;

    //File file = new File(saveDir+File.separator+0+ff1.substring(0,loc));

    public downloadTool(String downloadURL, int threadCount) throws IOException {
        this.downloadURL = downloadURL;
        this.threadCount = threadCount;
    }

    public void getFileName(HttpURLConnection con){
        String ff = con.getURL().getFile();
        System.out.println(ff);
        System.out.println(ff.substring(ff.lastIndexOf('/')+1));
        String ff1 = ff.substring(ff.lastIndexOf('/')+1);
        System.out.println(ff1);
        int loc = ff1.indexOf("?");
        System.out.println(ff1.substring(0,loc));
        localPath = path+File.separator+0+ff1.substring(0,loc);
    }


    public void startDownload() throws IOException {
//        URL url = new URL(downloadURL);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setConnectTimeout(10000);
//        connection.setRequestProperty("Range","bytes=0-20");
//        if(connection.getResponseCode()==206){
//            InputStream inputStream = connection.getInputStream();
//            //String result = getStringFromStream(inputStream);
//        }
        stop = false;
        cancel = false;
        URL url = new URL(downloadURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        //connection.setRequestProperty("Range","bytes=0-20");

        int code = connection.getResponseCode();
        if(code==200){
            getFileName(connection);
            //获取要下载的文件长度
            int length = connection.getContentLength();
            //在本地创建一个一样大小的文件
            RandomAccessFile file = new RandomAccessFile(localPath,"rw");
            file.setLength(length);
            //计算每一个线程要下载多少数据
            int blockSize = length/threadCount;

            //计算每一个线程要下载的数据范围
            for(int i=0;i<threadCount;i++){
                int startIndex = i+blockSize;
                int endIndex = (i+1)*blockSize -1;
                if(i == threadCount -1){
                    endIndex = length -1;
                }
                new downloadThread(startIndex,endIndex,i,threadCount).run();
            }
        }

    }

    public void stopDownload(){
        this.stop = true;
    }

    public void cancelDownload(){
        this.cancel = true;
    }

    public class downloadThread implements Runnable{

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
                if(temp!=null && temp.length()>0){
                    //说明日志文件有内容
                    FileInputStream fis = new FileInputStream(temp);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    String result = reader.readLine();
                    //读出记录下来的位置更新下载请求数据的起始位置
                    startIndex = Integer.parseInt(result);
                }



                //String fileName = "base.apk";
                File saveDir = new File(localPath);
                if (!saveDir.exists()) {
                    saveDir.mkdir();
                }

                URL url = new URL(downloadURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                //设置Range头用计算好的开始索引和结束索引到服务端请求数据
                connection.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);

                if(connection.getResponseCode()==206){
                    InputStream inputStream = connection.getInputStream();
                    int len = -1;
                    byte[] buffer = new byte[1024];
                    //RandomAccessFile file = new RandomAccessFile(getFileName(path),"rw");
                    RandomAccessFile file = new RandomAccessFile(saveDir,"rw");
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
                            float cur_process = (float)count/(float)(sum)*100;
                            process+=cur_process/threadNum;
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
