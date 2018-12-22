import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {

    private float[][] kernel;

    public MyConvolution(float[][] kernel) {
        this.kernel = kernel;
    }

    @Override
    public void processImage(FImage newImage) {

        //Creating a template for the resulting low-pass image
        FImage temp = new FImage(newImage.getWidth(), newImage.getHeight());

        int imageHeight = newImage.getHeight();
        int imageWidth = newImage.getWidth();

        //Getting the size of the kernel
        int kernelX = kernel[0].length; //Using this when moving the template horizontally
        int kernelY = kernel.length; //Using this when moving the template vertically


        //Going through each pixel in the image (and stopping when there are pixels left for exactly one more template)
        for (int image_p_height = 0; image_p_height < imageHeight - kernelX; image_p_height++) {
            for (int image_p_width = 0; image_p_width < imageWidth - kernelY; image_p_width++) {

                float newPixelVal = 0.0f;

                //The resulting pixel value in the low-pass version of the image is calculated by going through each
                //pixel in the rectangle given by the shape of the template and summing over the multiplications
                //between the values of the pixels and the values in the template

                for (int kernel_p_height = image_p_height; kernel_p_height < image_p_height + kernelX; kernel_p_height++) {
                    for (int kernel_p_width = image_p_width; kernel_p_width < image_p_width + kernelY; kernel_p_width++) {
                        newPixelVal += newImage.pixels[kernel_p_height][kernel_p_width] * kernel[kernel_p_height - image_p_height][kernel_p_width - image_p_width];
                    }
                }
                //Set the corresponding pixel value of the resulting low-pass image to the calculated new value
                temp.pixels[(image_p_height + kernelX / 2)][(image_p_width + kernelY / 2)] = newPixelVal;
            }
        }

        //By using internalAssign, the passed image is modified to retain only the low frequency values
        newImage.internalAssign(temp);
    }

}