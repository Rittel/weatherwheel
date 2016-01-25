package greendot.android.weatherwheel.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Rittel on 04.06.2015.
 */
public class TransparentableView extends View {

    int layerTopColor;
    int alphaTop = 0;
    int sizeX;
    int sizeY;
    Paint p = new Paint();

    public TransparentableView(Context context) {
        super(context);
    }

    public TransparentableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransparentableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TransparentableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(int layerTopColor) {
        this.layerTopColor = layerTopColor;
        this.sizeX = getMeasuredWidth();
        this.sizeY = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        p.setColor(layerTopColor);
        p.setAlpha(alphaTop);
        canvas.drawRect(0, 0, sizeX, sizeY, p);
        super.onDraw(canvas);
    }

    public void update(int alphaTop) {
        this.alphaTop = alphaTop;
        invalidate();
    }
}
