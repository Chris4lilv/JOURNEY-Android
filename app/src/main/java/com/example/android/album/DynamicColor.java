package com.example.android.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Map;

public class DynamicColor {
    private int red;
    private int green;
    private int blue;
    private int color;
    private Context context;

    public DynamicColor(){}

    public DynamicColor(Bitmap firstBitmap, Context context){
        this.context = context;
        getAverageColor(firstBitmap);

    }

    public void getAverageColor(Bitmap firstBitmap) {
        long redBucket = 0;
        long greenBucket = 0;
        long blueBucket = 0;
        long pixelCount = 0;
        for (int y = 0; y < firstBitmap.getHeight(); y++) {
            for (int x = 0; x < firstBitmap.getWidth(); x++) {
                int c = firstBitmap.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(c);
                greenBucket += Color.green(c);
                blueBucket += Color.blue(c);
            }
        }
        this.red = (int) (redBucket / pixelCount);
        this.green = (int) (greenBucket / pixelCount);
        this.blue = (int) (blueBucket / pixelCount);
        this.color = Color.rgb(this.red, this.green, this.blue);
    }

    /**
     * @return the dynamic average color
     */
    public int getColor(){
        return this.color;
    }
    /**
     *
     * @return the complementary of the average color
     */
    public int getComplementaryColor() {
        int maxPlusMin = max(red, green, blue) + min(red, green, blue);
        int rPrime = maxPlusMin - red;
        int bPrime = maxPlusMin - blue;
        int gPrime = maxPlusMin - green;
        return Color.rgb(rPrime, gPrime, bPrime);
    }

    /**
     * the max of three ints
     * @param r
     * @param b
     * @param g
     * @return
     */
    private int max(int r, int b, int g) {
        return Math.max(Math.max(r, g), b);
    }

    /**
     * the min of three ints
     * @param r
     * @param b
     * @param g
     * @return
     */
    private int min(int r, int b, int g) {
        return Math.min(Math.min(r, g), b);
    }
}
