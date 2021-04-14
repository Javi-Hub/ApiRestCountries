package com.sanvalero.apiRestCountries.domain;

import com.google.gson.annotations.SerializedName;
import lombok.*;

/**
 * Creado por @author: Javier
 * el 11/04/2021
 */
@Data
@ToString
@Builder
public class Country {

    @SerializedName("name")
    private String country;
    @SerializedName("cioc")
    private String code;
    private String capital;
    @SerializedName("region")
    private String continent;
    private float area;
    private int population;
    private String demonym;
    private String flag;

}
