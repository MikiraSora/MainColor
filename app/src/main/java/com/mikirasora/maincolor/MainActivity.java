package com.mikirasora.maincolor;

import android.app.VoiceInteractor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.concurrent.*;
import java.io.FileInputStream;
import java.security.Provider;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inMutable = true;
            Bitmap bmp = BitmapFactory.decodeFile("/sdcard/1.jpg", opt);
            ((ImageView) findViewById(R.id.raw)).setImageBitmap(bmp);
            Log.i("Conver", String.format("bitmap size : %d b", bmp.getByteCount()));

            //new Thread(new HueRunnable(bmp,(ImageView) findViewById(R.id.Hue))).start();

            MainColor.Option option = new MainColor.Option();
            option.hintCount = 120;
            MainColor mainColor = new MainColor(bmp, option);
            /*
            long b = System.currentTimeMillis();
            MainColor.ColorAnalyzeResult analyzeResult = mainColor.Analyse();
            Log.i("Conver", String.format("finish analyse colors!,time = %d ms", System.currentTimeMillis() - b));
            int max = 0;
            float[] weights = analyzeResult.getWeights();
            for (int pos = 0; pos < weights.length; pos++) {
                if (weights[pos] > weights[max])
                    max = pos;
            }
            Log.i("ColorResult", String.format("main color id :%d", max));
            Log.i("ColorResult", String.format("main color :%d", analyzeResult.getColors()[max]));
            findViewById(R.id.bg).setBackgroundColor(analyzeResult.getColors()[max]);*/
        long w = System.currentTimeMillis();
        int color=mainColor.AnalyseMainColor();
        int r=Color.red(color),g=Color.green(color),b=Color.blue(color);
        findViewById(R.id.bg).setBackgroundColor(color);
        Log.i("Conver", String.format("finish analyse colors!,time = %d ms", System.currentTimeMillis() - w));
        System.gc();
    }
/*
    class HueRunnable implements Runnable{
        Bitmap bmp=null;
        ImageView imageView=null;
        public HueRunnable(Bitmap b,ImageView iv){
            bmp=b;
            imageView=iv;
        }

        @Override
        public void run() {
            Log.i("Conver","=========START===========");
            if(bmp==null||imageView==null)
                return;
            int newColor=0;
            float[] hsvColor=new float[3];
            int hintCount=40;
            int hintSkipCount=1000000;
            int skipCount=1000;
            int hintAngle=60;
            ArrayList<ArrayList<Float>> HSVtable=new ArrayList<ArrayList<Float>>();
            for(int i=0;i<hintCount;i++)
                HSVtable.add(new ArrayList<Float>());
            final Bitmap newBitmap= Bitmap.createBitmap(bmp.getWidth(),bmp.getHeight(),Bitmap.Config.RGB_565);
            long preTime=System.currentTimeMillis();
            int q=0;
            final long pixels=bmp.getHeight()*bmp.getWidth();
            int[] pixel;

            pixel=new int[bmp.getHeight()*bmp.getWidth()];
            long b=System.currentTimeMillis();
            bmp.getPixels(pixel,0,bmp.getWidth(),0,0,bmp.getWidth(),bmp.getHeight());
            Log.i("Conver",String.format("finish get pixels!,time = %d ms",System.currentTimeMillis()-b));

            int pixelPos=0;
            int rawColor=0;
            b=System.currentTimeMillis();

                    if(pixels>hintSkipCount){
                        if((pixelPos++)>pixels/skipCount)
                            pixelPos=0;
                        else
                            continue;
                    }*
                    Color.colorToHSV(rawColor, hsvColor);
                    //
                    try {
                        q = ((int) (hsvColor[0] == 360 ? 0 : hsvColor[0])) / (360/hintCount);
                        HSVtable.get(q).add(hsvColor[0]);
                    } catch (Exception e) {
                        Log.e("Color Picker", String.format("occured a error ,%s", e.getMessage()));
                    }
                    //
                }
            Log.i("Conver",String.format("finish pick pixels!,time = %d ms",System.currentTimeMillis()-b));
            int max=0;
            int maxSize=0;
            for(int i=0;i<HSVtable.size();i++){
                maxSize+=HSVtable.get(i).size();
                if(HSVtable.get(q).size()>HSVtable.get(max).size())
                    max=i;
            }
            Log.i("Color Picker",String.format("pick up color %s",max));
            b=System.currentTimeMillis();
            for(int pos=0;pos<pixels;pos++){
                    rawColor=pixel[pos];
                    Color.colorToHSV(rawColor,hsvColor);
                    float centerHue=(max+0.5f)*(360/hintCount);
                    if(isInColorHint(hsvColor[0],centerHue))
                        hsvColor[1]=0;
                    newColor=Color.HSVToColor(hsvColor);
                    pixel[pos]=newColor;
                }
            Log.i("Conver",String.format("finish cover pixels!,time = %d ms",System.currentTimeMillis()-b));
            int o=0;
            b=System.currentTimeMillis();
            newBitmap.setPixels(pixel,0,bmp.getWidth(),0,0,bmp.getWidth(),bmp.getHeight());
            Log.i("Conver",String.format("finish set pixels!,time = %d ms",System.currentTimeMillis()-b));
            for(ArrayList<Float> i : HSVtable)
                Log.i("Color Count",String.format("%d - %.2f%%(%d)",o++,((float)i.size())/maxSize*100,i.size()));
            Log.i("Conver",String.format("finish Hue Render!,time = %d ms",System.currentTimeMillis()-preTime));
            Handler handler=new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(newBitmap);
                }
            });
            Log.i("Conver","=========END===========");
        }

        boolean isInColorHint(float value,float hint){
            float min=hint-60;
            float max=hint+60;
            if(min>0&&max<360)
                return !(value>min&&value<max);
            min=min<0?360-min:min;
            max=max>360?max-360:max;
            float tmp=min;
            min=max;
            max=tmp;
            return (value>min&&value<max);
        }
    }
}*/
}
class MainColor {
    Bitmap src = null;
    Option opt = null;

    private MainColor() {
    }

    public MainColor(Bitmap s, Option o) {
        src = s;
        opt = o;
    }

    public static class Option {
        int hintCount = 40;
        int hintSkipCount = 1000000;
        int skipCount = 1000;
        int hintAngle = 60;


    }

    public static class ColorAnalyzeResult {
        public ColorAnalyzeResult(float[] w, int[] c) {
            weights = w;
            colors = c;
        }

        private float[] weights;
        private int[] colors;

        public int size() {
            return weights.length;
        }

        public float[] getWeights() {
            return weights;
        }

        public int[] getColors() {
            return colors;
        }
    }

    public int AnalyseMainColor(){
        if (src == null || opt == null)
            return -1;
        float[] hsvColor = new float[3];

        ArrayList<Integer> pixelArrayList=new ArrayList<Integer>();

        long preTime = System.currentTimeMillis();
        int q = 0;
        long pixels = src.getHeight() * src.getWidth();
        int[] pixel= new int[src.getHeight() * src.getWidth()];

        long b = System.currentTimeMillis();
        //src.getPixels(pixel, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
        Log.i("Conver", String.format("finish get pixels!,time = %d ms", System.currentTimeMillis() - b));
        //int pixelPos = 0;
        int rawColor=0;
        int width=src.getWidth(),height=src.getHeight();
        long maxRead=pixels > opt.hintSkipCount?pixels / opt.skipCount:pixels;
        int step=pixels > opt.hintSkipCount?opt.skipCount:1;


        for (int pixelPos =0;pixelPos<maxRead;pixelPos++) {
            int y=(int)((pixelPos*step)/width);
            int x=(int)((pixelPos*step)/width);
            rawColor=src.getPixel(x,y);
            //Color.colorToHSV(rawColor, hsvColor);
            //
            pixelArrayList.add(rawColor);
            //
        }



        pixelArrayList=pickColors(pixelArrayList,4,3);
        float[] tmpColor=new float[3];
        float[] tmpValue=new float[pixelArrayList.size()];
        float[] tmpSaturation=new float[pixelArrayList.size()];
        int nr=0;

        for(int color:pixelArrayList){
            Color.colorToHSV(color,hsvColor);
            tmpColor[0] += hsvColor[0] / pixelArrayList.size();
            tmpSaturation[nr]=hsvColor[1];
            tmpValue[nr]=hsvColor[2];
            nr++;
        }
        tmpColor[1] = getSaturation(tmpSaturation);
        tmpColor[2] = getValue(tmpValue);

        return Color.HSVToColor(tmpColor);
    }

    public ColorAnalyzeResult Analyse() {
        if (src == null || opt == null)
            return null;
        float[] hsvColor = new float[3];
        ArrayList<ArrayList<Integer>> HSVtable = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < opt.hintCount; i++)
            HSVtable.add(new ArrayList<Integer>());
        long preTime = System.currentTimeMillis();
        int q = 0;
        long pixels = src.getHeight() * src.getWidth();
        int[] pixel;
        //ArrayList<Integer> tmpArray=new ArrayList();
        pixel = new int[src.getHeight() * src.getWidth()];
        long b = System.currentTimeMillis();
        src.getPixels(pixel, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
        Log.i("Conver", String.format("finish get pixels!,time = %d ms", System.currentTimeMillis() - b));
        //ArrayList<Float> pixelArrayList=new ArrayList<Float>();
        int pixelPos = 0;
        for (int rawColor : pixel) {
            if (pixels > opt.hintSkipCount) {
                if ((pixelPos++) > pixels / opt.skipCount)
                    pixelPos = 0;
                else
                    continue;
            }
            Color.colorToHSV(rawColor, hsvColor);
            //
            try {
                q = ((int) (hsvColor[0] == 360 ? 0 : hsvColor[0])) / (360 / opt.hintCount);
                HSVtable.get(q).add(rawColor);
                //pixelArrayList.add(hsvColor[0]);
            } catch (Exception e) {
                Log.e("Color Picker", String.format("occured a error ,%s", e.getMessage()));
            }
            //
        }
        int max = 0;
        int maxSize = 0;
        for (int i = 0; i < HSVtable.size(); i++) {
            maxSize += HSVtable.get(i).size();
            if (HSVtable.get(q).size() > HSVtable.get(max).size())
                max = i;
        }
        Log.i("Color Picker", String.format("pick up color %s", max));

        int o = 0;
        for(ArrayList<Integer> i : HSVtable)
            Log.i("Color Count",String.format("%d - %.2f%%(%d)",o++,((float)i.size())/maxSize*100,i.size()));

        Log.i("ColorConver", String.format("finish !,time = %d ms", System.currentTimeMillis() - preTime));
        int[] colors = new int[opt.hintCount];
        float[] weights = new float[opt.hintCount];
        o = 0;
        float[] tmpHsv = new float[3];
        float[] tmpSaturation, tmpValue;
        int psize = 0;
        for (int x = 0; x < opt.hintCount; x++) {
            psize = HSVtable.get(x).size();
            tmpSaturation = new float[psize];
            tmpValue = new float[psize];
            o = 0;
            for (int p : HSVtable.get(x)) {
                Color.colorToHSV(p, hsvColor);
                tmpHsv[0] += hsvColor[0] / psize;
                tmpSaturation[o] = hsvColor[1];
                tmpValue[o] = hsvColor[2];
                o++;
            }
            tmpHsv[1] = getSaturation(tmpSaturation);
            //Log.i("ColorHSV", String.format("Saturation : %f", tmpHsv[1]));
            tmpHsv[2] = getValue(tmpValue);
            //Log.i("ColorHSV", String.format("Value : %f", tmpHsv[2]));
            colors[x] = Color.HSVToColor(tmpHsv);
            tmpHsv[0] = 0;
            weights[x] = ((float) HSVtable.get(x).size()) / maxSize * 100;
        }
        return new ColorAnalyzeResult(weights, colors);
    }

    float getSaturation(float[] data) {
        int[] count = new int[100];
        for (int i = 0; i < data.length; i++) {
            try {
                count[(int) (data[i] >= 1 ? count.length - 0.01 : data[i] * 100)]++;
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        int max = 0;
        for (int i = 0; i < count.length; i++) {
            if (count[i] > count[max])
                max = i;
        }
        return max * 0.01f;
    }

    float getValue(float[] data) {
        int[] count = new int[100];
        for (int i = 0; i < data.length; i++) {
            try {
                count[(int) (data[i] >= 1 ? count.length - 0.01 : data[i] * 100)]++;
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        int max = 0;
        for (int i = 0; i < count.length; i++) {
            if (count[i] > count[max])
                max = i;
        }
        return max * 0.01f;
    }

    ArrayList<Integer> pickColors(ArrayList<Integer> list,int layout,int time){
        if(360/layout==0)
            return null;
        ArrayList<ArrayList<Integer>> tmpArrayList=new ArrayList<ArrayList<Integer>>();
        for(int i=0;i<layout;i++)
            tmpArrayList.add(new ArrayList<Integer>());
        float[] hsv=new float[3];
        int index=0;
        for (int value : list) {
            try {
                Color.colorToHSV(value,hsv);
                index=(int)((hsv[0]>=360?360-hsv[0]:hsv[0])/(360/layout));
                tmpArrayList.get(index>=layout?layout-1:index).add(value);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }
        int max = 0;
        for (int i = 0; i < layout; i++) {
            if (tmpArrayList.get(i).size()> tmpArrayList.get(max).size())
                max = i;
        }

        return time==0?tmpArrayList.get(max):pickColors(tmpArrayList.get(max),layout*2,--time);
    }
}

