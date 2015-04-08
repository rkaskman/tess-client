package com.roman.ttu.client.rest.model;

public class ImagesWrapper {
    public ImageWrapper regNumberImage;
    public ImageWrapper totalCostImage;

    public ImagesWrapper(ImageWrapper regNumberImage, ImageWrapper totalCostImage) {
        this.regNumberImage = regNumberImage;
        this.totalCostImage = totalCostImage;
    }

    public static class ImageWrapper {
        public String encodedImage;
        public String fileExtension;

        public ImageWrapper(String encodedImage, String fileExtension) {
            this.encodedImage = encodedImage;
            this.fileExtension = fileExtension;
        }
    }

}
