package com.example.localisationsmartphone;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("createPosition.php")
    Call<String> sendPosition(@FieldMap Map<String, String> fields);
}
