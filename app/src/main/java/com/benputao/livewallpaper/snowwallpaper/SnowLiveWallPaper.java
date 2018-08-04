package com.benputao.livewallpaper.snowwallpaper;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class SnowLiveWallPaper extends WallpaperService {
    /**
     * 壁纸设置保存文件名
     */
    private static final String PRE_FILE="treepre";
    private Bitmap love;
    private Bitmap bg,tree,flow1,flow2,flow3,flow4;
    /**
     * 设备屏幕宽高
     */
    private float windowWidth,windowHeight;
    /**<pre>
     * 设置的季节 0-随系统时间
     *            1-春季 ...
     * </pre>
     */
    private int season=0;
    /**
     * 设置的漂浮数量
     */
    private int flow_num=50;

    @Override
    public Engine onCreateEngine() {
        DisplayMetrics metrics=getResources().getDisplayMetrics();
        windowWidth=metrics.widthPixels;
        windowHeight=metrics.heightPixels;
        Log.i("init","window msg:  width="+windowWidth+"    height="+windowHeight);
        initImg();
        love= BitmapFactory.decodeResource(getResources(),R.mipmap.love);
        bg =  BitmapFactory.decodeResource(getResources(),R.mipmap.sky );
        Log.i("img memory","bg: w="+bg.getWidth()+" h="+bg.getHeight()+" size="+bg.getByteCount()/1024+"kb");
        Log.i("img memory","tree:w="+tree.getWidth()+" h="+tree.getHeight()+" size="+tree.getByteCount()/1024+"kb");
        Log.i("img memory","love:"+love.getByteCount()/1024+"kb");
        Log.i("img memory","flow1:"+flow1.getByteCount()/1024+"kb");
        Log.i("img memory","flow2:"+flow2.getByteCount()/1024+"kb");
        Log.i("img memory","flow3:"+flow4.getByteCount()/1024+"kb");
        Log.i("img memory","flow3:"+flow4.getByteCount()/1024+"kb");
        Log.i("img memory","-------count:"+
                (bg.getByteCount()+tree.getByteCount()+love.getByteCount()+flow1.getByteCount()+
                        flow2.getByteCount()+flow3.getByteCount()+flow4.getByteCount())/1024
                +"kb");
        return new MyEngine();
    }

    /**
     * 从文件中读取配置
     */
    public void readPre(){
        SharedPreferences preferences=getSharedPreferences(PRE_FILE,MODE_PRIVATE);
        flow_num=preferences.getInt("flow_num",20);
        season=preferences.getInt("season",0);
    }

    /**
     * 初始化图片资源
     */
    public void initImg(){
        readPre();
        if(tree!=null)tree.recycle();
        if (flow1!=null)flow1.recycle();
        if (flow2!=null)flow2.recycle();
        if (flow3!=null)flow3.recycle();
        if (flow4!=null)flow4.recycle();

        Calendar c=Calendar.getInstance();
        int month=c.get(Calendar.MONTH)+1;
        if(season!=0){
            month=season*3-1;
        }
        switch (month){
            case 2:
            case 3:
            case 4:{
                flow1=BitmapFactory.decodeResource(getResources(),R.mipmap.flower_1);
                flow2=BitmapFactory.decodeResource(getResources(),R.mipmap.flower_2);
                flow3=BitmapFactory.decodeResource(getResources(),R.mipmap.flower_3);
                flow4=BitmapFactory.decodeResource(getResources(),R.mipmap.flower_4);
                tree =BitmapFactory.decodeResource(getResources(),R.mipmap.tree_spring);
                break;
            }
            case 5:
            case 6:
            case 7:{
                flow1=flow2=flow3=flow4=BitmapFactory.decodeResource(getResources(),R.mipmap.leaf);
                tree =BitmapFactory.decodeResource(getResources(),R.mipmap.tree_sumer);
                break;
            }
            case 8:
            case 9:
            case 10:{
                flow1=flow2=flow3=flow4=BitmapFactory.decodeResource(getResources(),R.mipmap.leaf_autom);
                tree =BitmapFactory.decodeResource(getResources(),R.mipmap.tree_autom);
                break;
            }
            case 11:
            case 12:
            case 1:{
                flow1=BitmapFactory.decodeResource(getResources(),R.mipmap.snow_1);
                flow2=BitmapFactory.decodeResource(getResources(),R.mipmap.snow_2);
                flow3=BitmapFactory.decodeResource(getResources(),R.mipmap.snow_3);
                flow4=BitmapFactory.decodeResource(getResources(),R.mipmap.snow_4);
                tree =BitmapFactory.decodeResource(getResources(),R.mipmap.tree_winter);
                break;
            }
            default:break;
        }
    }

    class MyEngine extends Engine{
        private boolean mVisable;   //程序界面是否可见
        /**
         * 触碰时绘制love图片的间隔，每接受cct次触摸才绘制一次
         */
        private int cct=10;
        private Paint paint=new Paint();
        Handler mHander=new Handler();
        private ArrayList<DrHert>drawList=new ArrayList<>();
        private ArrayList<Flow>flows=new ArrayList<>();
        private SurfaceHolder holder;
        private final Runnable drawTarget=new Runnable() {
            @Override
            public void run() {
                drawFrame();
            }
        };
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            paint.setAntiAlias(true);
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHander.removeCallbacks(drawTarget);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            initImg();
            mVisable=visible;
            if (mVisable){
                drawFrame();
            }else {
                mHander.removeCallbacks(drawTarget);
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            initImg();
            drawFrame();
        }

        /**
         * 检测触摸事件，并添加心形对象到列表
         * @param event
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            if ( (event.getAction()==MotionEvent.ACTION_MOVE && cct==0) ||
                    event.getAction()==MotionEvent.ACTION_DOWN) {
                DrHert d = new DrHert();
                d.x=event.getX();
                d.y=event.getY();
                drawList.add(d);
            }
            if (cct--<=0){
                cct=5;
            }
            super.onTouchEvent(event);
        }

        private void drawFrame(){
            holder=getSurfaceHolder();
            Canvas canvas=null;
            try{
                canvas=holder.lockCanvas();
                if (canvas!=null){
                    paint.setAlpha(255);
                    RectF rectF=new RectF(0, 0, windowWidth, windowHeight);
                    canvas.drawBitmap(bg,null,rectF, paint);
                    canvas.drawBitmap(tree,null,rectF,paint);
                    drawFlows(canvas);
                    if (!drawList.isEmpty()){
                        drawTouchPoint(canvas);
                    }
                }
            }finally {
                if (canvas!=null)holder.unlockCanvasAndPost(canvas);
            }
            mHander.removeCallbacks(drawTarget);
            if (mVisable){
                mHander.postDelayed(drawTarget,30);
            }
        }

        /**
         * 绘制漂浮物
         */
        private void drawFlows(Canvas canvas){
            if (flows.size()<flow_num){
                flows.add(new Flow());
            }
            Flow fl;
            for (Iterator i=flows.iterator();i.hasNext();){
                fl= (Flow) i.next();

                switch (fl.type){
                    case 1:canvas.drawBitmap(flow1,fl.x,fl.y,paint);break;
                    case 2:canvas.drawBitmap(flow2,fl.x,fl.y,paint);break;
                    case 3:canvas.drawBitmap(flow3,fl.x,fl.y,paint);break;
                    case 4:canvas.drawBitmap(flow4,fl.x,fl.y,paint);break;
                    default:break;
                }
                fl.nn();
                if (fl.y>=canvas.getHeight()+20||fl.x<-20||fl.x>canvas.getWidth()+20){
                    i.remove();
                }
            }
        }
        /**
         * 在触摸位置绘制心形
         * @param canvas
         */
        private void drawTouchPoint(Canvas canvas){
            DrHert drHert;
            for (Iterator i=drawList.iterator();i.hasNext();){
                drHert= (DrHert) i.next();
                paint.setAlpha(10*drHert.times-1);
                canvas.drawBitmap(love,drHert.x,drHert.y,paint);
                drHert.rr();
                if (drHert.times<=0){
                    i.remove();
                }
            }
        }
    }

    class DrHert{
        public int times=0;
        public float x,y;

        public DrHert(){
            times=25;
        }

        public void rr() {
            times--;
            y+=5;
        }
    }

    class Flow{
        public float x,y;
        public double angle;   //0~30
        public int type;

        public Flow(){
            Random random=new Random(new Date().getTime());
            if (season==4){     //冬季雪花从屏幕顶落下
                this.x=Math.abs(random.nextInt()%windowWidth);
                this.y=0;
            }else {     //其它季节漂浮从树上掉落
                this.y=Math.abs(random.nextInt()%(int)(windowHeight*(1.0/2.0)));
                if(this.y<windowHeight*(1.0/4.0)){
                    this.x=Math.abs(random.nextInt());
                    this.x=random.nextInt()%(int)((2*this.y+windowWidth)*(2.0/5.0));
                }else {
                    this.x=Math.abs(random.nextInt()%(int)(windowWidth*(4.0/5.0)));
                }
            }
            this.angle=random.nextInt()%30;
            angle=Math.PI*angle/180;         //Math.sin/cos使用的的是弧度，故需要先转换
            this.type=Math.abs(random.nextInt()%4)+1;
        }
        public void nn(){
            x+=(5*Math.sin(angle));
            y+=(5*Math.cos(angle));
        }
    }
}

