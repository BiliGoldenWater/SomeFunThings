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
import java.util.Arrays;

public class SomeFun {
    public void FileToImage(File infile, File outfile, String imageType, boolean withAlpha) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(infile);
        int bytePerPixel = withAlpha ? 4 : 3; // 每个像素的字节数

        BigDecimal maxPixelNum = BigDecimal // 文件所需的最大像素数
                .valueOf(infile.length())
                .divide(BigDecimal.valueOf(bytePerPixel), RoundingMode.UP);
        BigDecimal width = BigDecimal.ONE, height = maxPixelNum; // 初始化图片大小为一列

        // 计算最接近正方形的图片宽高
        while (width.compareTo(height) < 0) { // 持续至宽大于等于高
            width = width.add(BigDecimal.ONE); // 宽++
            height = maxPixelNum.divide(width, RoundingMode.UP); // 计算高度
        }

        //创建图像
        BufferedImage image = new BufferedImage(
                width.intValue(),
                height.intValue(),
                withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
        );

        // 各种初始化
        byte[] dataBuff = new byte[image.getWidth() * bytePerPixel];
        int dataLength;

        // 转换为图片
        for (int y = 0; y < image.getHeight(); y++) {
            Arrays.fill(dataBuff, (byte) 0);
            dataLength = fileInputStream.read(dataBuff); // 读取图片一行像素能存的数据
            for (int x = 0; x < image.getWidth(); x++) {
                int[] rgb = new int[bytePerPixel];
                Color color;

                // 将4或3字节的数据存入数组, 取决于是否带Alpha通道
                for (int i = 0; i < bytePerPixel; i++) {
                    int data = dataBuff[x * bytePerPixel + i];

                    if (dataLength > 0) { // 如有剩余数据则存入数据, 否则存入0
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
        int bytePerPixel = withAlpha ? 4 : 3; // 每个像素的字节数

        BufferedImage image = ImageIO.read(infile); // 读取图片
        byte[] dataBuff = new byte[image.getWidth() * bytePerPixel]; // 根据 每行像素数 及 每个像素的字节数 初始化数据缓存
        int[] lastNotNull = new int[2];
        int i = 0;

        // 获取最后一个不为0的的像素位置
        getLastNotNullPixel:
        for (int y = image.getHeight() - 1; y > 0; y--) {
            for (int x = image.getWidth() - 1; x > 0; x--) { // 从最后一个像素开始向前循环
                // 获取像素信息
                Color color = new Color(image.getRGB(x, y), withAlpha);
                int r, g, b, a = 0;
                r = (byte) color.getRed();
                g = (byte) color.getGreen();
                b = (byte) color.getBlue();
                if (withAlpha) {
                    a = (byte) color.getAlpha();
                }

                if (!(r == 0 && g == 0 && b == 0 && a == 0)) { // 如果不为0则, 记录位置并退出循环
                    lastNotNull[0] = x;
                    lastNotNull[1] = y;
                    break getLastNotNullPixel;
                }
            }
        }

        // 转换并输出数据
        write:
        for (int y = 0; y < image.getHeight(); y++) {
            Arrays.fill(dataBuff, (byte) 0); // 清空缓存
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y), withAlpha); // 获取当前像素数据并存入缓存
                dataBuff[i] = (byte) color.getRed();
                dataBuff[i + 1] = (byte) color.getGreen();
                dataBuff[i + 2] = (byte) color.getBlue();
                if (withAlpha) {
                    dataBuff[i + 3] = (byte) color.getAlpha();
                }
                i += bytePerPixel;

                // 如果当前像素x,y为记录的最后一个非空像素x,y则
                if (y == lastNotNull[1] && x == lastNotNull[0]) {
                    // 减去空的像素数据值数量
                    if (color.getGreen() == 0 && color.getBlue() == 0 && (color.getAlpha() == 0 || !withAlpha)) {
                        i -= withAlpha ? 3 : 2;
                    } else if (color.getBlue() == 0 && (color.getAlpha() == 0 || !withAlpha)) {
                        i -= withAlpha ? 2 : 1;
                    } else if (color.getAlpha() == 0) {
                        i -= 1;
                    }

                    byte[] data = new byte[i];
                    System.arraycopy(dataBuff, 0, data, 0, i); // 将当前行不为空的数据复制到新数组
                    fileOutputStream.write(data); // 写入文件
                    break write;
                }
            }

            fileOutputStream.write(dataBuff);
            i = 0;
        }

        fileOutputStream.close();
    }
}
