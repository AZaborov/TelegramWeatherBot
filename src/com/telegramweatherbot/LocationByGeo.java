package com.telegramweatherbot;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class LocationByGeo {

    @SerializedName("Version")
    private Integer version;
    @SerializedName("Key")
    private String key;
    @SerializedName("Type")
    private String type;
    @SerializedName("Rank")
    private Integer rank;
    @SerializedName("LocalizedName")
    private String localizedName;
    @SerializedName("EnglishName")
    private String englishName;
    @SerializedName("PrimaryPostalCode")
    private String primaryPostalCode;
    @SerializedName("Region")
    private Region region;
    @SerializedName("Country")
    private Country country;
    @SerializedName("AdministrativeArea")
    private AdministrativeArea administrativeArea;
    @SerializedName("TimeZone")
    private TimeZone timeZone;
    @SerializedName("GeoPosition")
    private GeoPosition geoPosition;
    @SerializedName("IsAlias")
    private Boolean isAlias;
    @SerializedName("ParentCity")
    private ParentCity parentCity;
    @SerializedName("SupplementalAdminAreas")
    private List<SupplementalAdminArea> supplementalAdminAreas = null;
    @SerializedName("DataSets")
    private List<String> dataSets = null;

    @SerializedName("Version")
    public Integer getVersion() {
        return version;
    }

    @SerializedName("Version")
    public void setVersion(Integer version) {
        this.version = version;
    }

    @SerializedName("Key")
    public String getKey() {
        return key;
    }

    @SerializedName("Key")
    public void setKey(String key) {
        this.key = key;
    }

    @SerializedName("Type")
    public String getType() {
        return type;
    }

    @SerializedName("Type")
    public void setType(String type) {
        this.type = type;
    }

    @SerializedName("Rank")
    public Integer getRank() {
        return rank;
    }

    @SerializedName("Rank")
    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @SerializedName("LocalizedName")
    public String getLocalizedName() {
        return localizedName;
    }

    @SerializedName("LocalizedName")
    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    @SerializedName("EnglishName")
    public String getEnglishName() {
        return englishName;
    }

    @SerializedName("EnglishName")
    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    @SerializedName("PrimaryPostalCode")
    public String getPrimaryPostalCode() {
        return primaryPostalCode;
    }

    @SerializedName("PrimaryPostalCode")
    public void setPrimaryPostalCode(String primaryPostalCode) {
        this.primaryPostalCode = primaryPostalCode;
    }

    @SerializedName("Region")
    public Region getRegion() {
        return region;
    }

    @SerializedName("Region")
    public void setRegion(Region region) {
        this.region = region;
    }

    @SerializedName("Country")
    public Country getCountry() {
        return country;
    }

    @SerializedName("Country")
    public void setCountry(Country country) {
        this.country = country;
    }

    @SerializedName("AdministrativeArea")
    public AdministrativeArea getAdministrativeArea() {
        return administrativeArea;
    }

    @SerializedName("AdministrativeArea")
    public void setAdministrativeArea(AdministrativeArea administrativeArea) {
        this.administrativeArea = administrativeArea;
    }

    @SerializedName("TimeZone")
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @SerializedName("TimeZone")
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @SerializedName("GeoPosition")
    public GeoPosition getGeoPosition() {
        return geoPosition;
    }

    @SerializedName("GeoPosition")
    public void setGeoPosition(GeoPosition geoPosition) {
        this.geoPosition = geoPosition;
    }

    @SerializedName("IsAlias")
    public Boolean getIsAlias() {
        return isAlias;
    }

    @SerializedName("IsAlias")
    public void setIsAlias(Boolean isAlias) {
        this.isAlias = isAlias;
    }

    @SerializedName("ParentCity")
    public ParentCity getParentCity() {
        return parentCity;
    }

    @SerializedName("ParentCity")
    public void setParentCity(ParentCity parentCity) {
        this.parentCity = parentCity;
    }

    @SerializedName("SupplementalAdminAreas")
    public List<SupplementalAdminArea> getSupplementalAdminAreas() {
        return supplementalAdminAreas;
    }

    @SerializedName("SupplementalAdminAreas")
    public void setSupplementalAdminAreas(List<SupplementalAdminArea> supplementalAdminAreas) {
        this.supplementalAdminAreas = supplementalAdminAreas;
    }

    @SerializedName("DataSets")
    public List<String> getDataSets() {
        return dataSets;
    }

    @SerializedName("DataSets")
    public void setDataSets(List<String> dataSets) {
        this.dataSets = dataSets;
    }

}
