package com.example.facereader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;



import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String selectedImagePath, mCurrentPhotoPath;
    Uri imageUri;

    ImageView imageView;
    Button btnTakePicture, btnProcess, btnMakePhoto;
    TextView textView;

    //private String[] countFaces;
    private  String[] resulter, xrect, yrect, widthrect, heighrect;;
    //private String resulterStr, xrect, yrect, widthrect, heighrect;

    //EmotionServiceClient restClient = new EmotionServiceRestClient("4136e287e0b44a5e8b7a252876e75d69");





    int TAKE_PICTURE_CODE = 100, REQUEST_PERMISSION_CODE = 101, MAKE_PHOTO_CODE = 0;

    Bitmap mBitmap;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        initViews();

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
            }, REQUEST_PERMISSION_CODE);
        }
        textView.setText("");
    }

    private void initViews() {
        btnProcess = (Button) findViewById(R.id.btnProcess);
        btnTakePicture = (Button) findViewById(R.id.btnTakePic);
        btnMakePhoto = (Button) findViewById(R.id.btnMakePhoto);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);

        //Event




        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicFromGallery();
                textView.setText("");

            }
        });



        btnMakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                startActivityForResult(intent, MAKE_PHOTO_CODE);


            }

        });

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage();
            }
        });
    }


    private void processImage() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
       // Bitmap bitmap = BitmapFactory.decodeFile(Bitmap, options);
        //Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);

        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();


        //convert img to stream
        //final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
       // mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
       // final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        //create async task to process data
        //get : AsyncTask<String, String, String> processAsync = new AsyncTask < String, String, String>() {

        AsyncTask<byte[], String, String> processAsync = new AsyncTask <byte[], String, String>() {
            ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

            @Override
            protected void onPreExecute() {
                mDialog.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mDialog.setMessage(values[0]);

            }

            @Override
            protected String doInBackground (byte[]... byteArray){
                String responseString = null;
               // try {
                    connectServer(byteArray[0]);
                    //postRequest("http://192.168.1.105:5000/test/", "Hello");






                  return responseString;
                }



            protected void onPostExecute(String result){
                mDialog.dismiss();
                textView.setText(result);


            }
        };
        processAsync.execute(byteArray);

    }

    private Bitmap rotateImage(Bitmap bitmap){
        ExifInterface exifInterface =null;

        try{

            exifInterface = new ExifInterface(getRealPathFromURI(imageUri));
        }catch(IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
       imageView.setImageBitmap(rotatedBitmap);
      // mBitmap = rotatedBitmap;
        return rotatedBitmap;

    }







    void connectServer(byte[]... byteArray){
        String postUrl= "http://192.168.1.105:5000/classifier/run";

       // String postBodyText="Hello";
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
       // RequestBody postBody = RequestBody.create(mediaType, postBodyText);

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray[0]))
                .build();


        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.textView);
                        responseText.setText("Failed to Connect to Server");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.textView);
                        try {



// This gets you the first (zero indexed) element of the above array.

                            //JSONObject jsonObj = new JSONObject(response.body().string());



                            JSONArray jsonArray = new JSONArray(response.body().string());
                            int dlugosc = jsonArray.length();

                            String[] resulter = new String[dlugosc];
                            String[] xrect = new String[dlugosc];
                            String[] yrect = new String[dlugosc];
                            String[] widthrect = new String[dlugosc];
                            String[] heighrect = new String[dlugosc];





                            for(int i =0; i<jsonArray.length(); i++) {
                                JSONObject jsonObj = jsonArray.getJSONObject(i);
                                resulter[i] = jsonObj.getString("prediction");
                                xrect[i] = jsonObj.getString("xrect");
                                yrect[i] = jsonObj.getString("yrect");
                                widthrect[i] = jsonObj.getString("wrect");
                                heighrect[i] = jsonObj.getString("hrect");

                            }
                            imageView.setImageBitmap(ImageHelper.drawRectOnBitmap(mBitmap, dlugosc, xrect, yrect, heighrect, widthrect, resulter));


                            //imageView.setImageBitmap(bitmepper);


                           // responseText.setText(resulter[0] + resulter[1] );



                           //xrect = jsonObj.getString("xrect");

                           // yrect = jsonObj.getString("yrect");
                           //heighrect = jsonObj.getString("hrect");
                          //  widthrect = jsonObj.getString("wrect");

                            //ImageHelper.drawRectOnBitmap(mBitmap, Float.parseFloat(xrect), Float.parseFloat(yrect), Float.parseFloat(heighrect), Float.parseFloat(widthrect), resulter);
                            //imageView.setImageBitmap(mBitmap);
//DZIALO                            imageView.setImageBitmap(
//DZIALO                               ImageHelper.drawRectOnBitmap(mBitmap, Float.parseFloat(xrect), Float.parseFloat(yrect), Float.parseFloat(heighrect), Float.parseFloat(widthrect), resulter));
                            //mBitmap.recycle();

                           // String first = jsonObj.getJSONObject("arr").getString("a");
                            //System.out.println(first);

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }



                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK ) {
            Uri selectedImageUri = data.getData();
            //selectedImagePath = getPath(getApplicationContext(), selectedImageUri);

            InputStream in = null;
            try {
                in = getContentResolver().openInputStream(selectedImageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            mBitmap = BitmapFactory.decodeStream(in);
            //mBitmap = rotateImage(mBitmap);
            imageView.setImageBitmap(mBitmap);
            processImage();
        }
        else if(requestCode == MAKE_PHOTO_CODE && resultCode == RESULT_OK ) {

            InputStream in = null;

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                   rotateImage(mBitmap);

                    String imageurl = getRealPathFromURI(imageUri);
                    in = getContentResolver().openInputStream(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            mBitmap = BitmapFactory.decodeStream(in);
                mBitmap = rotateImage(mBitmap);
            processImage();
            //imageView.setImageBitmap(mBitmap);
            }






        }
       // super.onActivityResult(requestCode, resultCode, data);


//???????
    private Bitmap setReducedImageSize() {
        int targetImageViewWidth = 500;
        int targetImageViewHeight= 600;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(getRealPathFromURI(imageUri), bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth,cameraImageHeight/targetImageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(getRealPathFromURI(imageUri), bmOptions);
    }


        public String getRealPathFromURI(Uri contentUri) {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

    private void takePicFromGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, TAKE_PICTURE_CODE);
    }



    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}

