package com.example.lab1.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lab1.R;
import com.example.lab1.model.PriceBreakdown;

public class TaxiPriceBreakdownView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint distancePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint optionsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final RectF segmentRect = new RectF();
    private final Path clipPath = new Path();

    private float[] currentProportions = new float[]{0.25f, 0.25f, 0.25f, 0.25f};
    private float[] targetProportions = new float[]{0.25f, 0.25f, 0.25f, 0.25f};
    private float[] valuesPx = new float[4];
    private final String[] labels = new String[]{"Подача", "Км", "Мин", "Опции"};

    private int barHeightPx;
    private boolean showLabels = true;
    private boolean animateChanges = true;
    private int selectedSegmentIndex = -1;

    @Nullable
    private ValueAnimator animator;
    @Nullable
    private PriceBreakdown currentBreakdown;

    public TaxiPriceBreakdownView(Context context) {
        super(context);
        init(context, null);
    }

    public TaxiPriceBreakdownView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TaxiPriceBreakdownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        int baseColor = Color.parseColor("#2E7D32");       // Green: Подача
        int distanceColor = Color.parseColor("#0288D1");   // Blue: Километраж
        int timeColor = Color.parseColor("#ED6C02");       // Orange: Время
        int optionsColor = Color.parseColor("#9C27B0");    // Purple: Опции

        float density = context.getResources().getDisplayMetrics().density;
        barHeightPx = Math.round(28 * density);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TaxiPriceBreakdownView);
            baseColor = a.getColor(R.styleable.TaxiPriceBreakdownView_baseColor, baseColor);
            distanceColor = a.getColor(R.styleable.TaxiPriceBreakdownView_distanceColor, distanceColor);
            timeColor = a.getColor(R.styleable.TaxiPriceBreakdownView_timeColor, timeColor);
            optionsColor = a.getColor(R.styleable.TaxiPriceBreakdownView_optionsColor, optionsColor);
            barHeightPx = a.getDimensionPixelSize(R.styleable.TaxiPriceBreakdownView_barHeight, barHeightPx);
            showLabels = a.getBoolean(R.styleable.TaxiPriceBreakdownView_showLabels, true);
            animateChanges = a.getBoolean(R.styleable.TaxiPriceBreakdownView_animateChanges, true);
            a.recycle();
        }

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#E0E0E0"));

        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(baseColor);

        distancePaint.setStyle(Paint.Style.FILL);
        distancePaint.setColor(distanceColor);

        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setColor(timeColor);

        optionsPaint.setStyle(Paint.Style.FILL);
        optionsPaint.setColor(optionsColor);

        textPaint.setTextSize(12 * density);
        textPaint.setColor(Color.parseColor("#1F1B16"));
        textPaint.setTextAlign(Paint.Align.CENTER);

        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3 * density);
        highlightPaint.setColor(Color.WHITE);
    }

    public void setBreakdown(@Nullable PriceBreakdown breakdown) {
        if (breakdown == null) return;
        this.currentBreakdown = breakdown;
        float[] newProps = breakdown.getProportions();

        valuesPx[0] = breakdown.getBaseFare();
        valuesPx[1] = breakdown.getDistanceFare();
        valuesPx[2] = breakdown.getDurationFare();
        valuesPx[3] = breakdown.getOptionsFare();

        if (animateChanges && getWidth() > 0) {
            animateToProportions(newProps);
        } else {
            System.arraycopy(newProps, 0, currentProportions, 0, 4);
            System.arraycopy(newProps, 0, targetProportions, 0, 4);
            invalidate();
        }
    }

    private void animateToProportions(float[] newProps) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        final float[] startProps = ArraysCopy(currentProportions);
        System.arraycopy(newProps, 0, targetProportions, 0, 4);

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            for (int i = 0; i < 4; i++) {
                currentProportions[i] = startProps[i] + (targetProportions[i] - startProps[i]) * fraction;
            }
            invalidate();
        });
        animator.start();
    }

    private float[] ArraysCopy(float[] src) {
        float[] dest = new float[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        int defaultWidth = Math.round(280 * density);
        int defaultHeight = barHeightPx + getPaddingTop() + getPaddingBottom();

        if (showLabels) {
            defaultHeight += Math.round(36 * density);
        }

        int width = resolveSize(defaultWidth, widthMeasureSpec);
        int height = resolveSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (contentWidth <= 0) return;

        float top = getPaddingTop();
        float cornerRadius = barHeightPx / 2.0f;

        barRect.set(getPaddingLeft(), top, getPaddingLeft() + contentWidth, top + barHeightPx);

        // Draw rounded base background
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, bgPaint);

        clipPath.reset();
        clipPath.addRoundRect(barRect, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clipPath);

        Paint[] paints = new Paint[]{basePaint, distancePaint, timePaint, optionsPaint};
        float currentLeft = barRect.left;

        for (int i = 0; i < 4; i++) {
            float segWidth = contentWidth * currentProportions[i];
            if (segWidth > 0) {
                segmentRect.set(currentLeft, barRect.top, currentLeft + segWidth, barRect.bottom);
                canvas.drawRect(segmentRect, paints[i]);

                if (i == selectedSegmentIndex) {
                    canvas.drawRect(segmentRect, highlightPaint);
                }

                currentLeft += segWidth;
            }
        }

        canvas.restore();

        // Draw labels left to right centered under each segment
        if (showLabels) {
            float legendY = barRect.bottom + Math.round(20 * getResources().getDisplayMetrics().density);
            float step = (float) contentWidth / 4f;

            for (int i = 0; i < 4; i++) {
                float segCenterX = getPaddingLeft() + step * i + step / 2f;
                int percent = Math.round(currentProportions[i] * 100f);
                String labelText = labels[i] + " (" + percent + "%)";

                textPaint.setColor(paints[i].getColor());
                textPaint.setFakeBoldText(i == selectedSegmentIndex);

                float textWidth = textPaint.measureText(labelText);
                float clampedX = Math.max(getPaddingLeft() + textWidth / 2f,
                                 Math.min(getWidth() - getPaddingRight() - textWidth / 2f, segCenterX));

                canvas.drawText(labelText, clampedX, legendY, textPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            if (touchY >= barRect.top && touchY <= barRect.bottom + 40) {
                int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
                float currentLeft = getPaddingLeft();

                for (int i = 0; i < 4; i++) {
                    float segWidth = contentWidth * currentProportions[i];
                    if (touchX >= currentLeft && touchX <= currentLeft + segWidth) {
                        selectedSegmentIndex = i;
                        invalidate();
                        showSegmentToast(i);
                        return true;
                    }
                    currentLeft += segWidth;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void showSegmentToast(int index) {
        if (currentBreakdown == null) return;
        String name = labels[index];
        float val = valuesPx[index];
        int percent = Math.round(currentProportions[index] * 100f);
        Toast.makeText(getContext(),
                name + ": " + Math.round(val) + " ₽ (" + percent + "% от стоимости)",
                Toast.LENGTH_SHORT).show();
    }
}
