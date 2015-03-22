package com.roman.ttu.client.rest;

public class ImagesWrapper {
    public ImageWrapper regNumberImage;
    public ImageWrapper totalCostImage;

    public ImagesWrapper(ImageWrapper regNumberImage, ImageWrapper totalCostImage) {
        this.regNumberImage = regNumberImage;
        this.totalCostImage = totalCostImage;
    }

    public static class ImageWrapper {
        public String encodedImage;
        public String fileName;

        public ImageWrapper(String encodedImage, String fileName) {
            this.encodedImage = encodedImage;
            this.fileName = fileName;
        }
    }

}
