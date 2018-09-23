package dodert.cuentakilometros3;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Created by dodert on 18/04/2016.
 */
public class CustomNumberPicker extends NumberPicker {
    private float _textSizeBenja;
    public boolean putoflag = true;

    public CustomNumberPicker(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomNumberPicker,
                0, 0);

        try {
            _textSizeBenja = a.getDimension(R.styleable.CustomNumberPicker_aaaaaatextSize, 20);
        } finally {
            a.recycle();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setTextSizeBenja(int textSizeBenja) {
        this._textSizeBenja = textSizeBenja;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextSize(50);
            ((EditText) view).setTextColor(Color.parseColor("#333333"));
        }
    }
}