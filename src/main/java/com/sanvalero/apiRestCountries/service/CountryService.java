package com.sanvalero.apiRestCountries.service;

import com.sanvalero.apiRestCountries.domain.Country;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import java.util.List;

import static com.sanvalero.apiRestCountries.util.Constants.URL;

/**
 * Creado por @author: Javier
 * el 11/04/2021
 */
public class CountryService implements CountryApiService{

    private CountryApiService api;

    public CountryService(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        api = retrofit.create(CountryApiService.class);
    }

    @Override
    public Observable<List<Country>> getAllCountries() {
        return api.getAllCountries();
    }

    @Override
    public Observable<List<Country>> getCountriesContinent(String region) {
        return api.getCountriesContinent(region);
    }
}
