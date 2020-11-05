package by.mkr.controls;

import android.content.Context;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import by.mkr.blackberry.textlayouttools.R;


public class ThreeStateToggleView extends LinearLayout
{
    private ImageView _imageView;
    private ToggleState _currentState;
    private ThreeStateToggleListener _listener;
    private int _startX;
    private ToggleState _swipeState;

    public ThreeStateToggleView(Context context)
    {
        super(context);
        initControl(context, ToggleState.Right);
    }

    public ThreeStateToggleView(Context context, AttributeSet attrs) {
        super(context, attrs); // This should be first line of constructor
        initControl(context, ToggleState.Right);
    }

    public ThreeStateToggleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initControl(context, ToggleState.Right);
    }



    public void addOnToggleListener(ThreeStateToggleListener listener) {
        _listener = listener;
    }

    public void setToggle(ToggleState state) {
        _currentState = state;
        _imageView.setImageResource(getAnimation(_currentState));
    }



    private void animateToggle(ToggleState state) {
        if (state == null) {
            //Log.d("ReplacerLog1", "No Change: " + _currentState);
            return;
        }

        ToggleState prevState = _currentState;
        _currentState = state;
        //Log.d("ReplacerLog1", "states: " + prevState + " -> " + _currentState);

        _imageView.setImageResource(getAnimation(prevState, _currentState));

        ((AnimatedVectorDrawable) _imageView.getDrawable()).registerAnimationCallback(new Animatable2.AnimationCallback() {
            public void onAnimationEnd(Drawable drawable) {
                //Log.d("ReplacerLog1", "onToggle");
                if (_listener != null) {
                    _listener.onToggle(_currentState);
                }
            }
        });
        ((AnimatedVectorDrawable) _imageView.getDrawable()).start();
    }

    private void initControl(Context context, ToggleState state)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.three_state_toggle, this);
        _imageView = findViewById(R.id.btnChangeState);

        _currentState = state;
        _imageView.setImageResource(state.getResource());
        /*_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ReplacerLog1", "onClick");
                animateToggle(_currentState.getRightCirlular());
            }
        });*/

        _imageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int endX = (int) event.getX();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //Log.d("ReplacerLog1", "ACTION_DOWN");
                        _swipeState = null;
                        _startX = endX;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Log.d("ReplacerLog1", "ACTION_MOVE: " + (endX - _startX));
                        if (endX - _startX > 0) {
                            _startX = endX;
                            _swipeState = _currentState.getRight();
                            return true;
                        }
                        else if (endX - _startX < 0) {
                            _startX = endX;
                            _swipeState = _currentState.getLeft();
                            return false;
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        //Log.d("ReplacerLog1", "ACTION_UP: " + _swipeState);
                        animateToggle(_swipeState != null
                                ? _swipeState
                                : _currentState.getRightCirlular()
                        );
                        return true;
                }
                return false;
            }
        });
    }

    private int getAnimation(ToggleState start, ToggleState finish) {
        int animRes = 0;
        switch (start) {
            case Left:
                animRes = finish == ToggleState.Middle
                        ? R.drawable.avd_toggle_left_middle
                        : 0;
                break;
            case Middle:
                animRes = finish == ToggleState.Right
                        ? R.drawable.avd_toggle_middle_right
                        : R.drawable.avd_toggle_middle_left;
                break;
            case Right:
                animRes = finish == ToggleState.Left
                        ? R.drawable.avd_toggle_right_left
                        : R.drawable.avd_toggle_right_middle;
                break;
            default:
                break;
        }
        return animRes;
    }

    private int getAnimation(ToggleState finish) {
        int animRes = 0;
        switch (finish) {
            case Left:
                animRes = R.drawable.avd_toggle_left_middle;
                break;
            case Middle:
                animRes = R.drawable.avd_toggle_middle_right;
                break;
            case Right:
                animRes = R.drawable.avd_toggle_right_left;
                break;
            default:
                break;
        }
        return animRes;
    }


    public interface ThreeStateToggleListener {
        void onToggle(ToggleState state);
    }
}


