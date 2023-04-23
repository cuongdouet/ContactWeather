package com.camellia.contactweather.webservice;

import com.camellia.contactweather.BuildConfig;
import com.camellia.contactweather.webservice.model.DataModel;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

  private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";

  private static ApiManager sInstance = null;

  private APIInterface api;

  private ApiManager() {
    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build();

    api = retrofit.create(APIInterface.class);
  }

  public static ApiManager getInstance() {
    if (sInstance == null) {
      sInstance = new ApiManager();
    }
    return sInstance;
  }

  public Call<DataModel> getCurrentWeather(double lat, double lon) {
    return api.getCurrentWeather(lat, lon, BuildConfig.OPEN_WEATHER_APP_ID);
  }

}
