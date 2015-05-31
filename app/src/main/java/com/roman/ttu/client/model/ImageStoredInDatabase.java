package com.roman.ttu.client.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class ImageStoredInDatabase implements Serializable {
    public int id;
    public File imageFile;
    public Date creationTime;

}
