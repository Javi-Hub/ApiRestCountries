package com.sanvalero.apiRestCountries.service;

import com.sanvalero.apiRestCountries.domain.Country;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

import java.util.List;

/**
 * Creado por @author: Javier
 * el 11/04/2021
 */
public interface CountryApiService {

    @GET("/rest/v2/all")
    Observable<List<Country>> getAllCountries();

    @GET("/rest/v2/region/{region}")
    Observable<List<Country>> getCountriesContinent(@Path("region") String region);

}
