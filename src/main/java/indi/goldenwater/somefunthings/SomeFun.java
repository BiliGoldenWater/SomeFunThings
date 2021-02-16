package indi.goldenwater.somefunthings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SomeFun {
    public void FileToImage(File infile, File outfile, String imageType, boolean withAlpha) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(infile);
        int bytePerPixel = withAlpha ? 4 : 3;

        BigDecimal inFileLength = BigDecimal.valueOf(infile.length()).divide(BigDecimal.valueOf(bytePerPixel), RoundingMode.UP);
        BigDecimal widthB = BigDecimal.ONE, heightB = inFileLength;

        while (widthB.compareTo(heightB) < 0) {
            widthB = widthB.add(BigDecimal.ONE);
            heightB = inFileLength.divide(widthB, RoundingMode.UP);
        }

        int width = widthB.intValue(), height = heightB.intValue();
        byte[] dataBuff = new byte[width * bytePerPixel];
        int dataLength;

        BufferedImage image = new BufferedImage(width, height, withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            dataLength = fileInputStream.read(dataBuff);
            for (int x = 0; x < width; x++) {
                int[] rgb = new int[bytePerPixel];
                Color color;

                for (int i = 0; i < bytePerPixel; i++) {
                    int data = dataBuff[x * bytePerPixel + i];

                    if (dataLength > 0) {
                        rgb[i] = data < 0 ? data + 256 : data;
                    } else {
                        rgb[i] = 0;
                    }

                    dataLength--;
                }

                if (!withAlpha) {
                    color = new Color(rgb[0], rgb[1], rgb[2]);
                } else {
                    color = new Color(rgb[0], rgb[1], rgb[2], rgb[3]);
                }

                image.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(image, imageType, outfile);
        fileInputStream.close();
    }

    public void ImageToFile(File infile, File outfile, boolean withAlpha) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outfile);
        int bytePerPixel = withAlpha ? 4 : 3;

        BufferedImage image = ImageIO.read(infile);
        byte[] dataBuff = new byte[image.getWidth() * bytePerPixel];
        int i = 0;

        for (int y = 0; y < image.getHeight(); y++) {
//                long start = System.nanoTime();
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y), withAlpha);
                dataBuff[i] = (byte) color.getRed();
                dataBuff[i + 1] = (byte) color.getGreen();
                dataBuff[i + 2] = (byte) color.getBlue();
                if (withAlpha) {
                    dataBuff[i + 3] = (byte) color.getAlpha();
                }
                i += bytePerPixel;
            }
//                System.out.println(System.nanoTime()-start);
            fileOutputStream.write(dataBuff);
            i = 0;
        }
        fileOutputStream.close();
    }
}
