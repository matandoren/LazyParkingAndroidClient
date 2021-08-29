package com.example.lazyparking;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

public class MapView extends View {
    public final float MIN_SCALE_FACTOR = 1.f;
    public final float MAX_SCALE_FACTOR = 2.5f;
    public final int CAR_IMAGE_WIDTH = 82; //originally 82
    public final int CAR_IMAGE_HEIGHT = 40;
    public final float FONT_SIZE = 35.f;
    private DriverActivity.Floor floor;
    private Bitmap car;
    private ScaleGestureDetector sd;
    private float scaleFactor = 1.f;
    private GestureDetector gd;
    private float offsetX = 0.f;
    private float offsetY = 0.f;
    private float pivotX = 0.0f; // the center of the scaling in the x axis
    private float pivotY = 0.0f; // the center of the scaling in the Y axis
    private float top = 0.0f; // the top of the scaled image
    private float left = 0.0f; // the left of the scaled image
    private Rect rect; // the measures of the view
    private RectF carRect;
    private boolean isMeasuresKnown = false;
    private float viewToImageVerticalRatio = 1.f;
    private float viewToImageHorizontalRatio = 1.f;
    private Paint textBackground;
    private Paint textColor;
    private Paint.FontMetrics fontMetrics;



    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        car = BitmapFactory.decodeResource(getResources(), R.drawable.car);

        sd = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float previousScaleFactor = scaleFactor;
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(scaleFactor, MAX_SCALE_FACTOR));
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                top = focusY - (focusY - top) * (scaleFactor / previousScaleFactor);
                left = focusX - (focusX - left) * (scaleFactor / previousScaleFactor);

                pivotX = (focusX - left) / scaleFactor;
                offsetX = focusX - pivotX;
                pivotY = (focusY - top) / scaleFactor;
                offsetY = focusY - pivotY;

                rectifyGoingOutOfBounds();

                invalidate();
                return true;
            }
        });

        gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {

                rectifyGoingOutOfBounds(distanceX, distanceY);
                invalidate();
                return true;
            }
        });
    }

    public void setFloor(DriverActivity.Floor floor) {
        this.floor = floor;
        resetView();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        sd.onTouchEvent(event);
        gd.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isMeasuresKnown) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            viewToImageHorizontalRatio = viewWidth / (float)floor.originalImageWidth;
            viewToImageVerticalRatio = viewHeight / (float)floor.originalImageHeight;
            rect = new Rect(0, 0, viewWidth, viewHeight);
            textBackground = new Paint();
            textBackground.setColor(Color.RED);
            textBackground.setAlpha(75);
            textColor = new Paint();
            textColor.setAntiAlias(true);
            textColor.setTextSize(FONT_SIZE);
            textColor.setColor(Color.WHITE);
            fontMetrics = textColor.getFontMetrics();
            carRect = new RectF();
            isMeasuresKnown = true;
        }
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scaleFactor, scaleFactor, pivotX, pivotY);
        canvas.drawBitmap(floor.image,null, rect, null);

        DriverActivity.ParkingSpot spot;
        for (int i = 0; i < floor.rows.length; i++)
            for (int j = 0; j < floor.rows[i].parkingSpots.length; j++) {
                spot = floor.rows[i].parkingSpots[j];
                if (spot.isValid) {
                    float scaledUpperLeftX = floor.rows[i].upperLeftX * viewToImageHorizontalRatio;
                    float scaledBottom = (floor.rows[i].upperLeftY + floor.rows[i].parkingSpotWidth * (j + 1)) * viewToImageVerticalRatio;

                    if (spot.isReserved) {
                        canvas.drawRect(scaledUpperLeftX,
                                (floor.rows[i].upperLeftY + floor.rows[i].parkingSpotWidth * j) * viewToImageVerticalRatio,
                                scaledUpperLeftX + floor.rows[i].parkingSpotHeight * viewToImageHorizontalRatio,
                                scaledBottom,
                                textBackground);
                        canvas.drawText(getContext().getString(R.string.reserved_for_message),
                                scaledUpperLeftX,
                                scaledBottom - fontMetrics.descent + fontMetrics.ascent,
                                textColor);
                        canvas.drawText(spot.reservedFor,
                                scaledUpperLeftX,
                                scaledBottom - fontMetrics.descent,
                                textColor);
                    }
                    if (spot.isOccupied) {
                        carRect.left = scaledUpperLeftX;
                        carRect.top = (floor.rows[i].upperLeftY + floor.rows[i].parkingSpotWidth * j) * viewToImageVerticalRatio;
                        carRect.right = (floor.rows[i].upperLeftX + CAR_IMAGE_WIDTH) * viewToImageHorizontalRatio;
                        carRect.bottom = (floor.rows[i].upperLeftY + floor.rows[i].parkingSpotWidth * j + CAR_IMAGE_HEIGHT) * viewToImageVerticalRatio;
                        canvas.drawBitmap(car, null, carRect, null);
                    }
                }
            }

        canvas.restore();
    }

    private void rectifyGoingOutOfBounds(float distanceX, float distanceY) {
        float newLeft = left - distanceX;
        float newRight = newLeft + rect.width() * scaleFactor;
        if (newLeft <= 0 && newRight >= rect.width()) {
            offsetX -= distanceX;
            left -= distanceX;
        }
        else if (newLeft > 0) {
            offsetX -= left;
            left = 0;
        }
        else {
            offsetX -= rect.width() * scaleFactor - (rect.width() - left);
            left = rect.width() * (1 - scaleFactor);
        }

        float newTop = top - distanceY;
        float newBottom = newTop + rect.height() * scaleFactor;
        if (newTop <= 0 && newBottom >= rect.height()) {
            offsetY -= distanceY;
            top -= distanceY;
        }
        else if (newTop > 0) {
            offsetY -= top;
            top = 0;
        }
        else {
            offsetY -= rect.height() * scaleFactor - (rect.height() - top);
            top = rect.height() * (1 - scaleFactor);
        }
    }

    private void rectifyGoingOutOfBounds() {
        rectifyGoingOutOfBounds(0.f, 0.f);
    }


    public Integer getParkingSpotAt(float x, float y) {
        x-=left;
        y-=top;
        float xFactor = viewToImageHorizontalRatio * scaleFactor;
        float yFactor = viewToImageVerticalRatio * scaleFactor;

        for (int i = 0; i < floor.rows.length; i++)
            if (x >= floor.rows[i].upperLeftX * xFactor && x <= (floor.rows[i].upperLeftX + floor.rows[i].parkingSpotHeight) * xFactor && y >= floor.rows[i].upperLeftY * yFactor && y <= (floor.rows[i].upperLeftY + floor.rows[i].parkingSpotWidth * floor.rows[i].parkingSpots.length) * yFactor) {
                return floor.rows[i].parkingSpots[(int)((y - floor.rows[i].upperLeftY * yFactor) / (floor.rows[i].parkingSpotWidth * yFactor))].id;
            }

        return null;
    }

    public void resetView() {
        scaleFactor = 1.f;
        offsetX = 0.f;
        offsetY = 0.f;
        pivotX = 0.0f;
        pivotY = 0.0f;
        top = 0.0f;
        left = 0.0f;
    }

}
