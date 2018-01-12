package com.linshixun.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author 东哥 2016年10月27日
 */
public class Pic2Ascii {

    /**
     * @param path 图片路径
     */
    public static void createAsciiPic(final String path) throws Exception {
        final String base = "♥♦☀☢☺✡";// 字符串由复杂到简单
        try {


            zoomImage(path,path,40,40);

            final BufferedImage image = ImageIO.read(new File(path));
            for (int y = 0; y < image.getHeight(); y += 2) {
                for (int x = 0; x < image.getWidth(); x++) {
                    final int pixel = image.getRGB(x, y);
                    final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                    final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                    final int index = Math.round(gray * (base.length() + 1) / 255);
                    System.out.print(index >= base.length() ? " " : String.valueOf(base.charAt(index)));
                }
                System.out.println();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test
     *
     * @param args
     */
    public static void main(final String[] args) {
        try {
            createAsciiPic("C:\\Users\\linzhen\\Desktop\\QQ截图20180112182417.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
   * 图片缩放,w，h为缩放的目标宽度和高度
   * src为源文件目录，dest为缩放后保存目录
   */
    public static void zoomImage(String src, String dest, int w, int h) throws Exception {

        double wr = 0, hr = 0;
        File srcFile = new File(src);
        File destFile = new File(dest);

        BufferedImage bufImg = ImageIO.read(srcFile); //读取图片
        Image img = bufImg.getScaledInstance(w, h, Image.SCALE_SMOOTH);
//        Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);//设置缩放目标图片模板


        bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = bufImg .createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

//        wr = w * 1.0 / bufImg.getWidth();     //获取缩放比例
//        hr = h * 1.0 / bufImg.getHeight();
//
//        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
//        Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write( bufImg, dest.substring(dest.lastIndexOf(".") + 1), destFile); //写入缩减后的图片
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
   * 图片按比率缩放
   * size为文件大小
   */
    public static void zoomImage(String src, String dest, Integer size) throws Exception {
        File srcFile = new File(src);
        File destFile = new File(dest);

        long fileSize = srcFile.length();
        if (fileSize < size * 1024)   //文件大于size k时，才进行缩放
            return;

        Double rate = (size * 1024 * 0.5) / fileSize; // 获取长宽缩放比例

        BufferedImage bufImg = ImageIO.read(srcFile);
        Image Itemp = bufImg.getScaledInstance(bufImg.getWidth(), bufImg.getHeight(), bufImg.SCALE_SMOOTH);

        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(rate, rate), null);
        Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) Itemp, dest.substring(dest.lastIndexOf(".") + 1), destFile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
