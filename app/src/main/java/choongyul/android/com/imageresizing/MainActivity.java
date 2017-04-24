package choongyul.android.com.imageresizing;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    Button btnCamera, btnTongsin, btnCamera2;
    ImageView img;
    private static final int REQ_GALLERY = 100;
    private Uri selectedImageUrl;
    int flag = 0;
    private static int REQ_PERMISSION = 101;
    private Uri imageUriInDrawable;
    private String baseURL = "http://192.168.0.9/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkVersion(REQ_PERMISSION);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera2 = (Button) findViewById(R.id.btnCamera2);
        btnTongsin = (Button) findViewById(R.id.button2);
        img = (ImageView) findViewById(R.id.imageView);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 1;
                goGallery();
            }
        });
        btnCamera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 2;
                goGallery();
            }
        });

        btnTongsin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawableImageRemove(imageUriInDrawable);
            }
        });

    }

    private void drawableImageRemove(Uri fileUri) {
        if( fileUri != null ) {
            Log.e("삭제", String.valueOf(fileUri));
            File imagefile = new File(String.valueOf(fileUri));
            imagefile.delete();
        }
    }
    private void goGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult( intent, REQ_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_GALLERY:
                if(resultCode == RESULT_OK) {
                    if( flag == 1) {
                        imgResizing(data);
                    } else if ( flag == 2) {
                        afterPictureSelect(data);
                    } else {
                        Toast.makeText(this, "사진을 세팅할수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "사진파일을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void imgResizing(Intent data){;

        Uri imgUri = data.getData();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Toast.makeText(this, width + " , " + height, Toast.LENGTH_SHORT).show();

        Bitmap resized = null;
        while (height > 400) {
            resized = Bitmap.createScaledBitmap(bitmap, (width * 400) / height, 400, true);
            height = resized.getHeight();
            width = resized.getWidth();
        }


        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File file = new File(extStorageDirectory, "temp.PNG");
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);

            if (resized != null) {
                resized.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            }
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("path",file.getPath());
        imageUriInDrawable = Uri.parse(file.getPath());
        uploadImage(imageUriInDrawable);
//        afterPictureSelect(file.getPath());
    }

    private void uploadImage(final Uri fileUri) {

        RequestBody pDataTitle = RequestBody.create(MultipartBody.FORM, "title0");
        RequestBody pDataContent = RequestBody.create(MultipartBody.FORM, "content0");
        int statusCode = 0;

        File originalFile = new File(String.valueOf(fileUri));
        RequestBody filePart = RequestBody.create(MediaType.parse("multipart/form-data"), originalFile);

        // 이미지 넣을때 키값
        MultipartBody.Part file = MultipartBody.Part.createFormData("image", originalFile.getName(), filePart);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        DataInterface client = retrofit.create(DataInterface.class);
        String token = "asdasd";
        Call<ResponseBody> call = client.upload(statusCode, file);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("", "코드 : " + response.code());
                Log.e("업로드", "통신성공");
                drawableImageRemove(fileUri);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("업로드", "통신실패");
                drawableImageRemove(fileUri);
            }
        });
    }

    private void afterPictureSelect(String data) {
        Glide.with(this)
                .load(data)
                .into(img);
    }
    private void afterPictureSelect(Intent data) {
        Glide.with(this)
                .load(data.getData())
                .into(img);
    }




    // mainActivity에서 권한체크 메소드로 활용을 하자!

    // === onCreate 에서 불러와야 할것 ===
    // checkVersion();
    public final String PERMISSION_ARRAY[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            // TODO 원하는 permission 추가 또는 수정하기
    };

    public void checkVersion(int REQ_PERMISSION) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if( checkPermission(REQ_PERMISSION) ) {
                return;
            }
        } else {
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkPermission(int REQ_PERMISSION) {
        // 1.1 런타임 권한체크 (권한을 추가할때 1.2 목록작성과 2.1 권한체크에도 추가해야한다.)
        boolean permCheck = true;
        for(String perm : PERMISSION_ARRAY) {
            if ( this.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED ) {
                permCheck = false;
                break;
            }
        }

        // 1.2 퍼미션이 모두 true 이면 프로그램 실행
        if(permCheck) {
            // TODO 퍼미션이 승인 되었을때 해야하는 작업이 있다면 여기에서 실행하자.

            return true;
        } else {
            // 1.3 퍼미션중에 false가 있으면 시스템에 권한요청
            this.requestPermissions(PERMISSION_ARRAY, REQ_PERMISSION);
            return false;
        }
    }


    //2. 권한체크 후 콜백 - 사용자가 확인 후 시스템이 호출하는 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == REQ_PERMISSION) {

            if( onCheckResult(grantResults)) {
                // TODO 퍼미션이 승인 되었을때 해야하는 작업이 있다면 여기에서 실행하자.

                return;
            } else {
                Toast.makeText(this, "권한을 활성화 해야 모든 기능을 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                // 선택 : 1 종료, 2 권한체크 다시물어보기, 3 권한 획득하지 못한 기능만 정지시키기
                // finish();
            }
        }
    }
    public static boolean onCheckResult(int[] grantResults) {

        boolean checkResult = true;
        // 권한 처리 결과 값을 반복문을 돌면서 확인한 후 하나라도 승인되지 않았다면 false를 리턴해준다.
        for(int result : grantResults) {
            if( result != PackageManager.PERMISSION_GRANTED) {
                checkResult = false;
                break;
            }
        }
        return checkResult;
    }
}
