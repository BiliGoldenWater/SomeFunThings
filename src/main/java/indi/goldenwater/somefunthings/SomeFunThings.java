package indi.goldenwater.somefunthings;

import java.io.File;
import java.io.IOException;

public class SomeFunThings {
    public static void main(String[] args) {
        final SomeFun someFun = new SomeFun();

        final String inFileType = "txt";
        final String imageType = "png";
        final boolean withAlpha = true;

        try {
            someFun.FileToImage(new File("in." + inFileType), new File("out." + imageType), imageType, withAlpha);
            someFun.ImageToFile(new File("out." + imageType), new File("out." + inFileType), withAlpha);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
