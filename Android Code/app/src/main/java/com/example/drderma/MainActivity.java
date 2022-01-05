package com.example.drderma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    Button predict_bt;
    TextView prediction_tv;
    ImageButton getThroughGalleryButton, getThroughCameraButton;
    ImageView imgView;
    Bitmap img;

//    private InputStream image_inputStream;
    private static  final int CAMERA_REQUEST_CODE = 9996;
    private  static  final  int STORAGE_PERMISSION_CODE = 9998;
    private  static  final  int MY_CAMERA_PERMISSION_CODE = 9997;
    private  static final  int PICK_IMAGE_CODE = 9999;
    private static  final String FOLDER_URL =  "https://webs.iiitd.edu.in/raghava/drderma/";
    private String selectedImagePath;
    private String url = "http://" + "192.168.29.44" + ":" + 5000 + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkStoragePermission();
        checkCameraPermission();
        setContentView(R.layout.activity_main);
        predict_bt = findViewById(R.id.predict_bt);
        prediction_tv = findViewById(R.id.prediction_tv);
        imgView = findViewById(R.id.imageView);
        getThroughGalleryButton = findViewById(R.id.galleryButton);
        getThroughCameraButton = findViewById(R.id.cameraButton);
        HashMap<Integer, String> dis_label = new HashMap<Integer, String>();
        dis_label.put(0,"AKIEC");
        dis_label.put(1,"BCC");
        dis_label.put(2,"BKL");
        dis_label.put(3,"DF");
        dis_label.put(4,"MEL");
        dis_label.put(5,"NV");
        dis_label.put(6,"VASC");
        getThroughGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("Image Chooser" ,"Opened");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_CODE);
            }
        });

        getThroughCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                try{
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        predict_bt.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                try {
                    connectToServer();
                } catch (Exception e) {
                    Log.e("Upload Time Exception: ","Message: "+ e.getMessage()+"  Cause: "+ e.getCause());
                }


            }
        });

    }

    private void checkStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
            return;
        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
    }

    private void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Toast.makeText(getApplicationContext(), "You can't proceed without storage permissions", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == MY_CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Toast.makeText(getApplicationContext(), "You may not be able to click pictures without camera permissions", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CODE && data != null && data.getData() != null && resultCode == RESULT_OK){
            Uri image_uri = data.getData();
            Log.e("Image Chooser" ,"Image Selected");
            try {
//                selectedImagePath = getPath(getApplicationContext(), image_uri);
//                Log.e("Image Actual Path: " ,selectedImagePath);
                img = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), image_uri);
                imgView.setImageBitmap(img);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgView.setImageBitmap(imageBitmap);
            try {
                File image = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        selectedImagePath = image.getAbsolutePath();
        Log.e("Camera Image",selectedImagePath);
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    public void connectToServer(){


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path


//        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath,options);
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        Log.e("Byte array: ", byteArray.toString());
        String uid = UUID.randomUUID().toString();
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", uid+".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();
        Log.e("URL", url);
        Log.e("Post Body Image", postBodyImage.toString());
        postRequest(url, postBodyImage);


    }


    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Post Request:", "Failure");
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.e("Post Request:", "Success");
                            String disease = response.body().string();
                            prediction_tv.setText(disease);
                            prediction_tv.setClickable(true);
                            prediction_tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(),DiseaseDetailActivity.class);
                                    Bundle extras = new Bundle();
                                    if(disease.equals("Actinic Keratoses (akiec)")){
                                        extras.putInt("diseaseCode",0);
                                    }
                                    if(disease.equals("Basal cell carcinoma (bcc)")){
                                        extras.putInt("diseaseCode",1);
                                    }
                                    if(disease.equals("Benign keratosis (bkl)")){
                                        extras.putInt("diseaseCode",2);
                                    }
                                    if(disease.equals("Dermatofibroma (df)")){
                                        extras.putInt("diseaseCode",3);
                                    }
                                    if(disease.equals("Melanoma (mel)")){
                                        extras.putInt("diseaseCode",4);
                                    }
                                    if(disease.equals("Melanocytic nevi (nv)")){
                                        extras.putInt("diseaseCode",5);
                                    }
                                    if(disease.equals("Vascular skin (vasc)")){
                                        extras.putInt("diseaseCode",6);
                                    }
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // get selected image path
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
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