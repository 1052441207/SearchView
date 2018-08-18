package com.yiyeyealiyun.searchview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


/**
 * Created by zhaoyi on 2018/6/24.
 */

public class SearchView extends View implements View.OnClickListener{

    private Paint paint;
    //正在搜索的path
    private Path inSearchArcPath;

    //搜索的icon path
    private Path serchIconPath;

    private Path searchIconDestPath;

    private Path inSearchDestPath;

    private PathMeasure pathMeasure;

    private PathMeasure inSearchPathMeasure;

    private Matrix matrix;

    private SeachStatus seachStatus = SeachStatus.FINISH_SERRCH;

    private ValueAnimator startSearchAnim,inSearchAnim,endAnim;

    private float startAngle = 36;


    private float searchLength = 30f;

    public enum SeachStatus{
        START_SERRCH,
        IN_SERRCH,
        FINISH_SERRCH
    }

    public SearchView(Context context) {
        this(context,null);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        matrix = new Matrix();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);


        inSearchArcPath = new Path();
        RectF inSearchRectF = new RectF(-100,-100,100,100);
        inSearchArcPath.addArc(inSearchRectF,startAngle,359);

        pathMeasure = new PathMeasure(inSearchArcPath,true);

        int length = (int) pathMeasure.getLength();

        float[] a = new float[2];
        float[] b = new float[2];

        pathMeasure.getPosTan(0f,a,b);

        serchIconPath = new Path();
        RectF rectF = new RectF(-50,-50,50,50);
        serchIconPath.addArc(rectF,36,359);
        serchIconPath.lineTo(a[0],a[1]);

        searchIconDestPath = new Path();

        inSearchDestPath = new Path();

        setOnClickListener(this);


        initAnim();
    }

    private void initAnim() {
        //初始化开始搜索的动画

        pathMeasure.setPath(serchIconPath,false);
        pathMeasure.getSegment(0,pathMeasure.getLength(),searchIconDestPath,true);


        startSearchAnim = getStartEndAnim(0,pathMeasure.getLength());
        startSearchAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                seachStatus = SeachStatus.IN_SERRCH;
                inSearchAnim.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        endAnim = getStartEndAnim(pathMeasure.getLength(),0);


        inSearchPathMeasure = new PathMeasure(inSearchArcPath,false);

        inSearchAnim = ValueAnimator.ofFloat(0,inSearchPathMeasure.getLength());
        inSearchAnim.setDuration(2000);
        inSearchAnim.setRepeatCount(1);
        inSearchAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                endAnim.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        inSearchAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
               float curValue = (float) animation.getAnimatedValue();

                float start,stop;

                if (curValue > searchLength){
                    if (curValue > inSearchPathMeasure.getLength() - searchLength){
                        start = curValue - ((inSearchPathMeasure.getLength() - curValue));
                    }else {
                        start = curValue - searchLength;
                    }
                    stop = curValue;
                }else {
                    start = 0;
                    stop = curValue;
                }

                inSearchDestPath.reset();
                inSearchPathMeasure.getSegment(start,stop,inSearchDestPath,true);
                invalidate();
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth()/2,getHeight()/2);

        canvas.drawPath(searchIconDestPath,paint);
        switch (seachStatus){
            case IN_SERRCH:
                canvas.drawPath(inSearchDestPath,paint);
                break;
            case FINISH_SERRCH:
                canvas.drawPath(searchIconDestPath,paint);
                break;
//            case START_SERRCH:
//
//                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (seachStatus == SeachStatus.FINISH_SERRCH){
            startSearchAnim.start();
        }
    }

    private ValueAnimator getStartEndAnim(float start,float end){
        ValueAnimator animator =  ValueAnimator.ofFloat(start,end);
        animator.setDuration(3000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curValue = (float) animation.getAnimatedValue();
                Log.i("main",curValue + "");
                searchIconDestPath.reset();
                pathMeasure.getSegment(curValue,pathMeasure.getLength(),searchIconDestPath,true);
                invalidate();
            }
        });
        return animator;
    }
}
