package com.example.idlefox.facebookapi.model;

/**
 * Created by idlefox on 15/1/13.
 */
public class FacebookPhotoModel {
    public String src_small;
    public static final String PHOTOS_SRC_SMALL = "src_small";

    public String src_big;
    public static final String PHOTOS_SRC_BIG = "src_big";

    public String owner;
    public static final String PHOTOS_OWNER = "owner";

    public FacebookPhotoModel(String small, String big, String owner){
        this.src_small = small;
        this.src_big = big;
        this.owner = owner;
    }

    public String getSmall(){
        return this.src_small;
    }

    public String getBig(){
        return this.src_big;
    }

    public String getOwner(){
        return this.owner;
    }
}