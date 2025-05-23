package com.example.final_api.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://holodex.net/api/v2/";    private static final String API_KEY = "0058d50a-6961-4d7c-9e35-f8b61bfb57f5"; // Get from holodex.net
    private static Retrofit retrofit = null;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create a custom logging interceptor to ensure we get complete responses
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    if (message.startsWith("{") || message.startsWith("[")) {
                        android.util.Log.d("HolodexAPI", "Response JSON: " + message);
                    } else {
                        android.util.Log.d("HolodexAPI", message);
                    }
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with auth header
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("X-APIKEY", API_KEY)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(loggingInterceptor)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static String getApiKey() {
        // Return only first few and last few characters for security
        String key = API_KEY;
        if (key == null || key.length() < 10) {
            return "Unknown";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}