import org.openimaj.image.*;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.resize.ResizeProcessor;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        //Edit the following string values to specify a new path to images
        String f_FilePath = "/home/yoanapaleva/Documents/Computer-Vision/data/dog.bmp"; //The first image is going to be low-pass filtered
                                                                                        // and seen from long distance
        String s_FilePath = "/home/yoanapaleva/Documents/Computer-Vision/data/cat.bmp"; //The second image is going to be high-pass filtered
                                                                                        // and seen from close distance

        //Creating an image from a given file path
        MBFImage f_image = null;
        MBFImage s_image = null;
        try {
            f_image = ImageUtilities.readMBF(new File(f_FilePath));
            DisplayUtilities.display(f_image, "Original first image");
            s_image = ImageUtilities.readMBF(new File(s_FilePath));
            DisplayUtilities.display(s_image, "Original second image");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Creating a new kernel using the createKernel method with a sigma value of 5.0
        float[][] kernel = createKernel(5.0f);
        int kernelX = kernel[0].length;
        int kernelY = kernel.length;

        //Obtaining the low-pass versions of the two images
        MBFImage f_lowPass = lowPassFilter(f_image, kernel);
        MBFImage s_lowPass = lowPassFilter(s_image, kernel);
        //Setting the borders of the processed images to black
        setBorders(f_lowPass, kernelX, kernelY);
        setBorders(s_lowPass, kernelX, kernelY);
        //Displaying the low-pass images
        DisplayUtilities.display(f_lowPass, "Low-pass version of first image");
        DisplayUtilities.display(s_lowPass, "Low-pass version of second image");

        //Obtaining the high-pass version of the second image
        MBFImage highPass = highPassFilter(s_image, s_lowPass);
        //Setting the borders of the processed images to black
        setBorders(highPass, kernelX, kernelY);
        //Displaying the high-pass image
        DisplayUtilities.display(highPass, "High-pass version of second image");


        //Creating a hybrid image by convolving the low-pass version of first image
        //and high-pass version of second image
        MBFImage hybridImage = createHybridImage(f_lowPass, highPass);
        //Setting the borders of the processed images to black
        setBorders(hybridImage, kernelX, kernelY);
        //Displaying the final hybrid image
        DisplayUtilities.display(hybridImage, "Final hybrid image");

        //Visualising the hybrid image by scaling it down
        resizeAndDisplay(5, hybridImage, kernelX, kernelY);
    }

    private static float[][] createKernel(float sigma) {
        if (sigma < 0) {
            throw new IllegalArgumentException("Sigma value cannot be negative!");
        }
        if (sigma == 0) {
            throw new IllegalArgumentException("Sigma value of zero will result in blank images!");
        }
        //This method is adapted from the coursework spec:
        //This implies the window is +/- 4 sigmas from the centre of the Gaussian
        int size = (int) (8.0f * sigma + 1.0f);
        //Making sure that the size is odd
        if (size % 2 == 0) size++;
        return Gaussian2D.createKernelImage(size, sigma).pixels;
    }

    private static MBFImage createHybridImage(MBFImage lowPass, MBFImage highPass) {
        MBFImage hybridImage = lowPass.clone();
        for (int i = 0; i < lowPass.getHeight(); i++) {
            for (int j = 0; j < lowPass.getWidth(); j++) {

                //Setting each pixel in the new hybrid image to be equal to the sum of the
                //pixel value in the low pass version of the first image and the pixel value in the high-pass version of the second image
                //This results in a hybrid image

                hybridImage.getBand(0).pixels[i][j] = lowPass.getBand(0).pixels[i][j] + highPass.getBand(0).pixels[i][j];
                hybridImage.getBand(1).pixels[i][j] = lowPass.getBand(1).pixels[i][j] + highPass.getBand(1).pixels[i][j];
                hybridImage.getBand(2).pixels[i][j] = lowPass.getBand(2).pixels[i][j] + highPass.getBand(2).pixels[i][j];
            }
        }
        return hybridImage;
    }

    private static MBFImage lowPassFilter(MBFImage image, float[][] kernel) {
        MBFImage lowPass = image.clone();
        //Creating an instance of MyConvolution class with the custom kernel
        final MyConvolution myConvolution = new MyConvolution(kernel);

        //Processing a colour image using MyConvolution function
        //and returning the low-pass version
        return lowPass.processInplace(myConvolution);
    }

    private static MBFImage highPassFilter(MBFImage originalImage, MBFImage lowPassImage) {
        MBFImage highPassImage = originalImage.clone();
        for (int i = 0; i < originalImage.getHeight(); i++) {
            for (int j = 0; j < originalImage.getWidth(); j++) {

                //Setting each pixel in the new (high-pass) image to be equal to the difference between the
                //pixel value in the original image and the pixel value in the low-pass version of the original image
                //This results in only the high frequency values remaining

                highPassImage.getBand(0).pixels[i][j] = originalImage.getBand(0).pixels[i][j] - lowPassImage.getBand(0).pixels[i][j];
                highPassImage.getBand(1).pixels[i][j] = originalImage.getBand(1).pixels[i][j] - lowPassImage.getBand(1).pixels[i][j];
                highPassImage.getBand(2).pixels[i][j] = originalImage.getBand(2).pixels[i][j] - lowPassImage.getBand(2).pixels[i][j];
            }
        }
        return highPassImage;
    }

    private static void resizeAndDisplay(int numTimes, MBFImage hybridImage, int kernelX, int kernelY) {

        //Calculating the size of the new image without borders
        int newWidth = hybridImage.getWidth() - kernelX;
        int newHeight = hybridImage.getHeight() - kernelY;

        //Creating an empty template for the new image without borders
        MBFImage newImage = new MBFImage(newWidth, newHeight);

        //Going through each pixel in the new image and setting its value to the original image between the borders
        for (int i = 0; i < newImage.getHeight(); i++) {
            for (int j = 0; j < newImage.getWidth(); j++) {
                newImage.getBand(0).pixels[i][j] = hybridImage.getBand(0).pixels[kernelX / 2 + i][kernelY / 2 + j];
                newImage.getBand(1).pixels[i][j] = hybridImage.getBand(1).pixels[kernelX / 2 + i][kernelY / 2 + j];
                newImage.getBand(2).pixels[i][j] = hybridImage.getBand(2).pixels[kernelX / 2 + i][kernelY / 2 + j];
            }
        }

        //Calculating the final size of the visualisation template, based on the number
        //of nested images (number of times the image needs to be scaled down)
        //Starting with the original image's width:
        int finalWidth = newImage.getWidth();
        int div = 2;
        for (int i = 0; i < numTimes - 1; i++) {
            //each next image width is half the width of the previous one
            finalWidth += newImage.getWidth() / div;
            div *= 2;
        }

        //Creating the image display template
        MBFImage displayImagesBuffer = new MBFImage(finalWidth, newImage.getHeight());
        //Drawing the original image from position (0,0)
        displayImagesBuffer.drawImage(newImage, 0, 0);

        MBFImage image = newImage.clone();
        int width = image.getWidth();
        int height = image.getHeight() / 2;
        //Each new image starts from the end of the previous image, so new.x = previous.width
        //and half the height, so new.y = previous.height / 2
        for (int i = 1; i <= numTimes; i++) {
            //Scaling down the new image to be half the size of the previous one
            image = ResizeProcessor.halfSize(image);
            //and drawing it from the calculated position
            displayImagesBuffer.drawImage(image, width, height);
            //Updating the width and height for the next image
            width += image.getWidth();
            height += image.getHeight() / 2;
        }
        DisplayUtilities.display(displayImagesBuffer, "Hybrid image from different distances");
    }

    private static void setBorders(MBFImage image, int kernelX, int kernelY) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        //Calculating the size of the top/bottom & left/right borders based on the kernel size
        //E.g. if kernel is size 3x3, all borders should be 1 pixel, because the pixel resulting from the template
        //convolution will correspond to the one in the center of the template, which is at position (1,1)
        int borderTopBottomWidth = kernelY / 2;
        int borderLeftRightWidth = kernelX / 2;

        //Setting the borders above and below the resulting image, by going though each pixel and setting its value to 0.0
        //This is done for all 3 colour bands; Border counter corresponds to a row of pixels in the image, starting from 0 to border width
        int borderPixelCounterTB = 0;
        while (borderPixelCounterTB <= borderTopBottomWidth) {
            for (int i = 0; i < imageWidth; i++) {
                //Above template
                image.getBand(0).pixels[borderPixelCounterTB][i] = 0.0f;
                image.getBand(1).pixels[borderPixelCounterTB][i] = 0.0f;
                image.getBand(2).pixels[borderPixelCounterTB][i] = 0.0f;

                //Below template
                image.getBand(0).pixels[imageHeight - 1 - borderPixelCounterTB][i] = 0.0f;
                image.getBand(1).pixels[imageHeight - 1 - borderPixelCounterTB][i] = 0.0f;
                image.getBand(2).pixels[imageHeight - 1 - borderPixelCounterTB][i] = 0.0f;
            }
            borderPixelCounterTB++;
        }

        //Setting the borders left and right of the resulting image, by going though each pixel and setting its value to 0.0
        //This is done for all 3 colour bands; Border counter corresponds to a row of pixels in the image, starting from 0 to border width
        int borderPixelCounterLR = 0;
        while (borderPixelCounterLR <= borderLeftRightWidth) {
            for (int i = 0; i < imageHeight; i++) {
                //Left of template
                image.getBand(0).pixels[i][borderPixelCounterLR] = 0.0f;
                image.getBand(1).pixels[i][borderPixelCounterLR] = 0.0f;
                image.getBand(2).pixels[i][borderPixelCounterLR] = 0.0f;

                //Right of template
                image.getBand(0).pixels[i][imageWidth - 1 - borderPixelCounterLR] = 0.0f;
                image.getBand(1).pixels[i][imageWidth - 1 - borderPixelCounterLR] = 0.0f;
                image.getBand(2).pixels[i][imageWidth - 1 - borderPixelCounterLR] = 0.0f;
            }
            borderPixelCounterLR++;
        }

    }

}