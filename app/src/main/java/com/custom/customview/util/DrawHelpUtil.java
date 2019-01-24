package com.custom.customview.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;

/**
 * 辅助画布
 */
public class DrawHelpUtil {

    public static boolean isDraw = true;

    /**
     * 绘制picture
     *
     * @param canvas
     * @param pictures
     */
    public static void draw(Canvas canvas, Picture... pictures) {
        if (!isDraw) {
            return;
        }

        for (Picture picture : pictures) {
            picture.draw(canvas);
        }
    }

    /**
     * 绘制网格
     */
    public static Point getWinSize(Context context) {
        Point point = new Point();
        point.set(DensityUtils.getScreenWidth(context), DensityUtils.getScreenHeight(context));
        return point;
    }


    /**
     * 绘制网格
     */
    public static Picture getGrid(Context context) {
        return getGrid(getWinSize(context));
    }

    /**
     * 绘制坐标系
     */
    public static Picture getCoo(Context context, Point coo) {
        return getCoo(coo, getWinSize(context));
    }


    /**
     * 绘制网格
     *
     * @param winSize 屏幕尺寸
     */
    private static Picture getGrid(Point winSize) {

        Picture picture = new Picture();
        Canvas recording = picture.beginRecording(winSize.x, winSize.y);
        Paint paint = getHelpPint();

        recording.drawPath(gridPath(50, winSize), paint);
        return picture;

    }

    @NonNull
    public static Paint getHelpPint() {
        return getHelpPint(Color.GRAY);
    }

    /**
     * 绘制点集
     *
     * @param canvas 画布
     * @param paint  画笔
     * @param points 点集
     */
    public static void drawPos(Canvas canvas, Paint paint, Point... points) {
        for (Point point : points) {
            canvas.drawPoint(point.x, point.y, paint);
        }
    }

    /**
     * 绘制点集
     *
     * @param canvas 画布
     * @param paint  画笔
     * @param points 点集
     */
    public static void drawPos(Canvas canvas, Paint paint, PointF... points) {
        for (PointF point : points) {
            canvas.drawPoint(point.x, point.y, paint);
        }
    }

    /**
     * 绘制点集
     *
     * @param canvas 画布
     * @param paint  画笔
     * @param points 点集
     */
    public static void drawLines(Canvas canvas, Paint paint, Point... points) {

        for (int i = 0; i < points.length; i += 2) {
            if (i > points.length - 2) {
                return;
            }
            canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, paint);
        }
    }

    /**
     * 绘制点集
     *
     * @param canvas 画布
     * @param paint  画笔
     * @param points 点集
     */
    public static void drawLines(Canvas canvas, Paint paint, PointF... points) {
        for (int i = 0; i < points.length; i += 2) {
            if (i > points.length - 2) {
                return;
            }
            canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, paint);
        }
    }


    @NonNull
    public static Paint getHelpPint(int color) {
        //初始化网格画笔
        Paint paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setColor(color);
        paint.setTextSize(50);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        //设置虚线效果new float[]{可见长度, 不可见长度},偏移值
        paint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        return paint;
    }

    /**
     * 绘制坐标系
     *
     * @param coo     坐标系原点
     * @param winSize 屏幕尺寸
     */
    private static Picture getCoo(Point coo, Point winSize) {
        Picture picture = new Picture();
        Canvas recording = picture.beginRecording(winSize.x, winSize.y);
        //初始化网格画笔
        Paint paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        //设置虚线效果new float[]{可见长度, 不可见长度},偏移值
        paint.setPathEffect(null);

        //绘制直线
        recording.drawPath(cooPath(coo, winSize), paint);
        //左箭头
        recording.drawLine(winSize.x, coo.y, winSize.x - 40, coo.y - 20, paint);
        recording.drawLine(winSize.x, coo.y, winSize.x - 40, coo.y + 20, paint);
        //下箭头
        recording.drawLine(coo.x, winSize.y, coo.x - 20, winSize.y - 40, paint);
        recording.drawLine(coo.x, winSize.y, coo.x + 20, winSize.y - 40, paint);
        //为坐标系绘制文字
        drawText4Coo(recording, coo, winSize, paint);
        return picture;
    }

    /**
     * 为坐标系绘制文字
     *
     * @param canvas  画布
     * @param coo     坐标系原点
     * @param winSize 屏幕尺寸
     * @param paint   画笔
     */
    private static void drawText4Coo(Canvas canvas, Point coo, Point winSize, Paint paint) {
        //绘制文字
        paint.setTextSize(50);
        canvas.drawText("x", winSize.x - 60, coo.y - 40, paint);
        canvas.drawText("y", coo.x - 40, winSize.y - 60, paint);
        paint.setTextSize(25);
        //X正轴文字
        for (int i = 1; i < (winSize.x - coo.x) / 50; i++) {
            paint.setStrokeWidth(2);
            canvas.drawText(100 * i + "", coo.x - 20 + 100 * i, coo.y + 40, paint);
            paint.setStrokeWidth(5);
            canvas.drawLine(coo.x + 100 * i, coo.y, coo.x + 100 * i, coo.y - 10, paint);
        }

        //X负轴文字
        for (int i = 1; i < coo.x / 50; i++) {
            paint.setStrokeWidth(2);
            canvas.drawText(-100 * i + "", coo.x - 20 - 100 * i, coo.y + 40, paint);
            paint.setStrokeWidth(5);
            canvas.drawLine(coo.x - 100 * i, coo.y, coo.x - 100 * i, coo.y - 10, paint);
        }

        //y正轴文字
        for (int i = 1; i < (winSize.y - coo.y) / 50; i++) {
            paint.setStrokeWidth(2);
            canvas.drawText(100 * i + "", coo.x + 20, coo.y + 10 + 100 * i, paint);
            paint.setStrokeWidth(5);
            canvas.drawLine(coo.x, coo.y + 100 * i, coo.x + 10, coo.y + 100 * i, paint);
        }

        //y负轴文字
        for (int i = 1; i < coo.y / 50; i++) {
            paint.setStrokeWidth(2);
            canvas.drawText(-100 * i + "", coo.x + 20, coo.y + 10 - 100 * i, paint);
            paint.setStrokeWidth(5);
            canvas.drawLine(coo.x, coo.y - 100 * i, coo.x + 10, coo.y - 100 * i, paint);
        }
    }

    /**
     * 绘制网格:注意只有用path才能绘制虚线
     *
     * @param step    小正方形边长
     * @param winSize 屏幕尺寸
     */
    public static Path gridPath(int step, Point winSize) {

        Path path = new Path();

        for (int i = 0; i < winSize.y / step + 1; i++) {
            path.moveTo(0, step * i);
            path.lineTo(winSize.x, step * i);
        }

        for (int i = 0; i < winSize.x / step + 1; i++) {
            path.moveTo(step * i, 0);
            path.lineTo(step * i, winSize.y);
        }
        return path;
    }

    /**
     * 坐标系路径
     *
     * @param coo     坐标点
     * @param winSize 屏幕尺寸
     * @return 坐标系路径
     */
    public static Path cooPath(Point coo, Point winSize) {
        Path path = new Path();
        //x正半轴线
        path.moveTo(coo.x, coo.y);
        path.lineTo(winSize.x, coo.y);
        //x负半轴线
        path.moveTo(coo.x, coo.y);
        path.lineTo(coo.x - winSize.x, coo.y);
        //y负半轴线
        path.moveTo(coo.x, coo.y);
        path.lineTo(coo.x, coo.y - winSize.y);
        //y负半轴线
        path.moveTo(coo.x, coo.y);
        path.lineTo(coo.x, winSize.y);
        return path;
    }
}
