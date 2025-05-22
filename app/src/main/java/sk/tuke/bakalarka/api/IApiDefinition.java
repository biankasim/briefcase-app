package sk.tuke.bakalarka.api;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IApiDefinition {
    @Multipart
    @POST("predict")
    Call<JsonObject> getPrediction(
            @Part MultipartBody.Part image,
            @Part("x") int x,
            @Part("y") int y,
            @Part("color_palette") String colorPalette
    );

    @Multipart
    @POST("classify")
    Call<JsonObject> getClassification(
            @Part MultipartBody.Part image,
            @Part("x") int x,
            @Part("y") int y
    );
}
