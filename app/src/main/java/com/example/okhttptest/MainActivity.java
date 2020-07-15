package com.example.okhttptest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.example.okhttptest.domain.CommentItem;
import com.example.okhttptest.utils.IOClose;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    private static final String BASIC_URL = "http://10.0.2.2:9102";
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //动态请求权限
        int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }


    /*
     * 处理申请用户权限的操作
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE) {
            //判断结果
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"has permissions..");
                //有权限
            } else {
                Log.d(TAG,"no permissionS...");
                //没权限
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_CALENDAR)&&!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CALENDAR)) {
                    //走到这里，说明用户之前用户禁止权限的同时，勾选了不再询问
                    //那么，你需要弹出一个dialog，提示用户需要权限，然后跳转到设置里头去打开。
                    Log.d(TAG,"用户之前勾选了不再询问...");
                    //TODO:弹出一个框框，然后提示用户说需要开启权限。
                    //TODO:用户点击确定的时候，跳转到设置里去
                    //Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //Uri uri = Uri.fromParts("package", getPackageName(), null);
                    //intent.setData(uri);
                    ////在activity结果范围的地方，再次检查是否有权限
                    //startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST_CODE);
                    //请求权限
                    Log.d(TAG,"请求权限...");
                }
            }
        }
    }
    /*
    * 请求网络，并获取网页内容
    * */
    public void getText(View view){

        //请求客户端，相当于浏览器
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .build();

        //请求内容
        final Request request = new Request.Builder()
                .get()
                .url(BASIC_URL+"/get/text")
                .build();

        //通过客户端响应任务
        Call task = client.newCall(request);

        //异步请求
        task.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure-error: "+e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();//响应结果码
                Log.d(TAG, "onResponse-responseCode: "+code);
                if (code == HttpURLConnection.HTTP_OK) {
                    ResponseBody body = response.body();
                    Log.d(TAG, "onResponse-body: "+body.string());
                }

            }
        });
    }


    /*
    * 发送post请求
    * */
    public void postRequest(View view){

        //创建客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000,TimeUnit.MILLISECONDS)
                .build();

        //post上传内容的设置
        //上传对象/内容
        CommentItem commentItem = new CommentItem("4153","太累了！");
        Gson gson = new Gson();
        String jsonStr = gson.toJson(commentItem);
        MediaType type = MediaType.parse("application/json");  //post发送过去的类型，这里是json格式
        RequestBody requestBody = RequestBody.create(jsonStr,type);

        //请求内容
        Request request = new Request.Builder()
                .post(requestBody)
                .url(BASIC_URL+"/post/comment")
                .build();

        //响应任务
        Call task = client.newCall(request);

        //异步响应
        task.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure-error: "+e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();
                Log.d(TAG, "onResponse-responseCode: "+code);
                if (code == HttpURLConnection.HTTP_OK) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        Log.d(TAG, "onResponse-body: "+body.string());
                    }
                }
            }
        });
    }


    /*
    * 单文件上传
    * */
    public void postFile(View view){

        //创建客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000,TimeUnit.MILLISECONDS)
                .build();

        //文件处理
        File file = new File("/storage/emulated/0/Download/naruto.jpg");
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody fileBody = RequestBody.create(file,mediaType);
        //请求内容处理
        final RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("file",file.getName(),fileBody)
                .build();

        //创建请求
        Request request = new Request.Builder()
                .post(requestBody)
                .url(BASIC_URL+"/file/upload")
                .build();

        //请求任务
        Call task = client.newCall(request);

        //异步请求
        task.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure-error: "+e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int code = response.code();
                Log.d(TAG, "onResponse-responseCode: "+code);
                if (code == HttpURLConnection.HTTP_OK) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        Log.d(TAG, "onResponse-responseBody: "+responseBody.string());
                    }
                }
            }
        });
    }



    /*
    * 多文件上传
    * */
    public void postFiles(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //创建客户端
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30000,TimeUnit.MILLISECONDS)
                        .build();

                //文件处理
                File file1 = new File("/storage/emulated/0/Download/naruto.jpg");
                File file2 = new File("/storage/emulated/0/Download/timg.jpeg");
                File file3 = new File("/storage/emulated/0/Download/u=2221979916,487946013&fm=26&gp=0.jpg");
                File file4 = new File("/storage/emulated/0/Download/u=3394313492,1153728278&fm=26&gp=0.jpg");
                MediaType mediaType = MediaType.parse("image/jpeg");
                RequestBody fileBody1 = RequestBody.create(file1,mediaType);
                RequestBody fileBody2 = RequestBody.create(file2,mediaType);
                RequestBody fileBody3 = RequestBody.create(file3,mediaType);
                RequestBody fileBody4 = RequestBody.create(file4,mediaType);
                //请求内容处理
                final RequestBody requestBody = new MultipartBody.Builder()
                        .addFormDataPart("files",file1.getName(),fileBody1)
                        .addFormDataPart("files",file2.getName(),fileBody2)
                        .addFormDataPart("files",file3.getName(),fileBody3)
                        .addFormDataPart("files",file4.getName(),fileBody4)
                        .build();

                //创建请求
                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(BASIC_URL+"/files/upload")
                        .build();

                //请求任务
                Call task = client.newCall(request);

                //异步请求
                task.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure-error: "+e.toString());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        int code = response.code();
                        Log.d(TAG, "onResponse-responseCode: "+code);
                        if (code == HttpURLConnection.HTTP_OK) {
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                Log.d(TAG, "onResponse-responseBody: "+responseBody.string());
                            }
                        }
                    }
                });
            }
        }).start();
    }


    /*
    * 下载文件
    * */
    public void downloadFile(View view){

        //创建客户端
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000,TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .get()
                .url(BASIC_URL+"/download/11")
                .build();

        final Call call = client.newCall(request);

        new Thread(new Runnable() {

//            private FileOutputStream fos = null;
//            private InputStream inputStream = null;

            @Override
            public void run() {
                try {
                    Response execute = call.execute(); //同步请求
                    int code = execute.code();
                    Log.d(TAG, "code:"+code);
                    if (code == HttpURLConnection.HTTP_OK) {
                        //从头部信息中获取文件名信息
                        Headers headers = execute.headers();
                        for (int i = 0; i < headers.size(); i++) {
                            String key = headers.name(i);
                            String value = headers.value(i);
                            Log.d(TAG, "downloadFile: "+key+" == "+value);
                        }

                        MainActivity.this.downloadFile(execute, headers);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    * 处理文件下载的类
    * 包括获取文件名、创建文件、将从网络中的获取的读入到文件中
    * */
    private void downloadFile(Response execute, Headers headers) throws IOException {
        String contentType = headers.get("Content-disposition");
        String fileName = contentType.replace("attachment; filename=","");

        //创建文件：外存路径/文件名
        File outputFile = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)+File.separator+fileName);
        Log.d(TAG, "outputFile:"+outputFile);
        //文件夹/文件不存在时创建
        if (!outputFile.getParentFile().exists()) {
            outputFile.mkdirs();
        }
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(outputFile);
        InputStream inputStream = null;
        //向之前创建的文件中写内容
        if (execute.body() != null) {
            //接收到的内容
            inputStream = execute.body().byteStream();
            byte[] buffer = new byte[1024];
            int len;
            while((len = inputStream.read(buffer,0,buffer.length)) != -1){
                fos.write(inputStream.read(buffer,0,len));
            }
            fos.flush();
        }
        IOClose.ioClose(fos);
        IOClose.ioClose(inputStream);
    }
}
