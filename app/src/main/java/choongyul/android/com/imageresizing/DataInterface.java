package choongyul.android.com.imageresizing;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by myPC on 2017-04-24.
 */

public interface DataInterface {

    @Multipart
    @POST("upload")
    Call<ResponseBody> upload(
            @Part("status") int code,
            @Part MultipartBody.Part file
    );
}
