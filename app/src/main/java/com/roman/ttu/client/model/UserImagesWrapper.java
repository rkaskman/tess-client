package com.roman.ttu.client.model;


import java.io.Serializable;
import java.util.Date;

public class UserImagesWrapper extends ImagesWrapper implements Serializable {
    public int id;
    public Date creationTime;

    public UserImagesWrapper(int id, ImageWrapper regNumberImage, ImageWrapper totalCostImage, Date insertedAt) {
        super(regNumberImage, totalCostImage);
        this.id = id;
        this.creationTime = insertedAt;
    }
}