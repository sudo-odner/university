package com.example.lab4.presentation.photo;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomImageView extends AppCompatImageView {
    private final Matrix matrix = new Matrix();
    private final ScaleGestureDetector detector;
    private float scale = 1f, lastX, lastY;
    public ZoomImageView(Context context) {
        super(context); setScaleType(ScaleType.MATRIX);
        detector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector d) {
                float next = Math.max(1f, Math.min(5f, scale * d.getScaleFactor()));
                float factor = next / scale; scale = next;
                matrix.postScale(factor, factor, d.getFocusX(), d.getFocusY()); setImageMatrix(matrix); return true;
            }
        });
    }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) { super.onSizeChanged(w,h,oldw,oldh); fit(); }
    @Override public void setImageDrawable(Drawable drawable) { super.setImageDrawable(drawable); fit(); }
    private void fit() {
        Drawable d = getDrawable();
        if (d == null || getWidth() == 0 || getHeight() == 0 || d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) return;
        matrix.reset(); scale = 1f;
        float fit = Math.min((float)getWidth()/d.getIntrinsicWidth(), (float)getHeight()/d.getIntrinsicHeight());
        matrix.postScale(fit, fit);
        matrix.postTranslate((getWidth()-d.getIntrinsicWidth()*fit)/2f, (getHeight()-d.getIntrinsicHeight()*fit)/2f);
        setImageMatrix(matrix);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        if (event.getPointerCount() == 1 && !detector.isInProgress()) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) { lastX=event.getX(); lastY=event.getY(); }
            else if (event.getActionMasked() == MotionEvent.ACTION_MOVE && scale > 1f) {
                matrix.postTranslate(event.getX()-lastX, event.getY()-lastY);
                setImageMatrix(matrix); lastX=event.getX(); lastY=event.getY();
            }
        }
        return true;
    }
}
