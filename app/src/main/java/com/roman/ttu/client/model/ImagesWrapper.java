package com.roman.ttu.client.model;

public class ImagesWrapper {
    public ImageWrapper receiptImage;
    public ImageWrapper totalCostImage;
    public String registrationId;

    public ImagesWrapper(ImageWrapper receiptImage, ImageWrapper totalCostImage) {
        this.receiptImage = receiptImage;
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
