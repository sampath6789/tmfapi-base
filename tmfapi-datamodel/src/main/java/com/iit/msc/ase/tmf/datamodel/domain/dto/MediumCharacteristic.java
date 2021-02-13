package com.iit.msc.ase.tmf.datamodel.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude( JsonInclude.Include.NON_EMPTY )
public class MediumCharacteristic {

    @JsonIgnore
    private long primaryId;
    @JsonIgnore
    private String parentEntity;
    @JsonIgnore
    private String parentId;
    @JsonIgnore
    private String immediateParent;
    @JsonIgnore
    private String immediateParentId;

    private String country;
    private String city;
    private String contactType;
    private String socialNetworkId;
    private String emailAddress;
    private String phoneNumber;
    private String stateOrProvince;
    private String faxNumber;
    private String postCode;
    private String street1;
    private String street2;
    @JsonProperty( "@schemaLocation" )
    private String schemaLocation;
    @JsonProperty( "@type" )
    private String type;
    @JsonProperty( "@baseType" )
    private String baseType;

    @Override
    public String toString() {
        return "MediumCharacteristic{" +
                "primaryId=" + primaryId +
                ", parentEntity='" + parentEntity + '\'' +
                ", parentId='" + parentId + '\'' +
                ", immediateParent='" + immediateParent + '\'' +
                ", immediateParentId='" + immediateParentId + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", contactType='" + contactType + '\'' +
                ", socialNetworkId='" + socialNetworkId + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", stateOrProvince='" + stateOrProvince + '\'' +
                ", faxNumber='" + faxNumber + '\'' +
                ", postCode='" + postCode + '\'' +
                ", street1='" + street1 + '\'' +
                ", street2='" + street2 + '\'' +
                ", schemaLocation='" + schemaLocation + '\'' +
                ", type='" + type + '\'' +
                ", baseType='" + baseType + '\'' +
                '}';
    }

}