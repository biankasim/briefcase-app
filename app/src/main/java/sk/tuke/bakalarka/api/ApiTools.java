package sk.tuke.bakalarka.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiTools {
    private static final String mBaseApiUrl = "https://biankin-klasifikator.azurewebsites.net/";
    private static Retrofit mRetrofitInstance;

    private static Retrofit createRetrofitApiInstance(){
        if(mRetrofitInstance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build();

            mRetrofitInstance = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(mBaseApiUrl)
                    .client(client)
                    .build();
        }
        return mRetrofitInstance;
    }

    public static IApiDefinition getApi(){
        return createRetrofitApiInstance().create(IApiDefinition.class);
    }
}
