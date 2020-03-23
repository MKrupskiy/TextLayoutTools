package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.WINDOW_SERVICE;
import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


enum FloatingIconStyle {
    Flag,
    TextCustom;

    public static FloatingIconStyle fromString(String x) {
        switch (x) {
            case "Flag":
                return Flag;
            case "TextCustom":
                return TextCustom;
            default:
                return null;
        }
    }
}

enum FloatingIconGravityHoriz {
    Left,
    Center,
    Right;

    public static FloatingIconGravityHoriz fromString(String x) {
        switch (x) {
            case "Left":
            case "0":
                return Left;
            case "Center":
            case "1":
                return Center;
            case "Right":
            case "2":
                return Right;
            default:
                return null;
        }
    }
    public static FloatingIconGravityHoriz fromInt(int x) {
        return fromString("" + x);
    }
    public int toGravityInt() {
        switch (this) {
            case Left:
                return Gravity.LEFT;
            case Center:
                return Gravity.CENTER;
            case Right:
                return Gravity.RIGHT;
            default:
                return 0;
        }
    }
}

enum FloatingIconGravityVert {
    StatusBar,
    Top,
    Bottom;

    public static FloatingIconGravityVert fromString(String x) {
        switch (x) {
            case "StatusBar":
                return StatusBar;
            case "Top":
                return Top;
            case "Bottom":
                return Bottom;
            default:
                return null;
        }
    }
    public int toGravityInt() {
        switch (this) {
            case StatusBar:
                return Gravity.TOP;
            case Top:
                return Gravity.TOP;
            case Bottom:
                return Gravity.BOTTOM;
            default:
                return 0;
        }
    }
}


public class FloatingIndicatorManager implements View.OnClickListener {

    private Context _context;
    private static View _floatingIndicatorView;
    private static TextView _textView;
    private static ImageView _flagView;


    FloatingIndicatorManager(Context context) {
        _context = context;

        initView();
    }

    public void setLanguage(Language lang) {
        if (_floatingIndicatorView != null && _textView != null && _flagView != null) {
            AppSettings appSettings = ReplacerService.getAppSettings();

            if (!appSettings.isShowFloatingIcon) {
                return;
            }

            if (lang == Language.Ukr) {
                _flagView.setImageResource(R.drawable.ic_flag_ukraine_col);
                _textView.setVisibility(View.GONE);
                _flagView.setVisibility(View.VISIBLE);
            }
            if (lang.isRus()) {
                switch (appSettings.floatingIconStyleRu) {
                    case Flag: {
                        _flagView.setImageResource(R.drawable.ic_flag_russia_col);
                        _textView.setVisibility(View.GONE);
                        _flagView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case TextCustom: {
                        _textView.setText(appSettings.floatingIconTextRu);
                        _flagView.setVisibility(View.GONE);
                        _textView.setVisibility(View.VISIBLE);
                        break;
                    }
                    default:
                        break;
                }
            }
            if (lang.isEng()) {
                switch (appSettings.floatingIconStyleEn) {
                    case Flag: {
                        _flagView.setImageResource(R.drawable.ic_flag_gb_col);
                        _textView.setVisibility(View.GONE);
                        _flagView.setVisibility(View.VISIBLE);
                        break;
                    }
                    case TextCustom: {
                        _textView.setText(appSettings.floatingIconTextEn);
                        _flagView.setVisibility(View.GONE);
                        _textView.setVisibility(View.VISIBLE);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    public void clearLanguage() {
        _textView.setVisibility(View.GONE);
        _flagView.setVisibility(View.GONE);
    }

    public void updateSettings() {
        initView();
    }


    private void showFlag() {
        _flagViewCollapsed.setVisibility(View.VISIBLE);
        _flagViewExpanded.setVisibility(View.VISIBLE);
        _textViewCollapsed.setVisibility(View.GONE);
        _textViewExpanded.setVisibility(View.GONE);
    }
    private void showText() {
        _flagViewCollapsed.setVisibility(View.GONE);
        _flagViewExpanded.setVisibility(View.GONE);
        _textViewCollapsed.setVisibility(View.VISIBLE);
        _textViewExpanded.setVisibility(View.VISIBLE);
    }

    private static WindowManager.LayoutParams getLayoutParams(AppSettings appSettings) {
        int layout_params = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int isStatusBarFlag = appSettings.floatingIconGravityVert == FloatingIconGravityVert.StatusBar
                ? WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                : 0;
        String t = Settings.EXTRA_APP_PACKAGE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_params,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | isStatusBarFlag,
                PixelFormat.TRANSLUCENT);
        params.gravity = appSettings.floatingIconGravityVert.toGravityInt() | appSettings.floatingIconGravityHoriz.toGravityInt();
        params.x = 0;
        params.y = appSettings.floatingIconGravityVert == FloatingIconGravityVert.StatusBar
                ? 10
                : 0;

        return params;
    }

    private void initView() {
        try {
            Log.d(LOG_TAG, "initView");
            AppSettings appSettings = ReplacerService.getAppSettings();

            if (appSettings == null) {
                return;
            }

            WindowManager windowManager = (WindowManager) _context.getSystemService(WINDOW_SERVICE);

            if (!appSettings.isShowFloatingIcon) {
                windowManager.removeView(_floatingIndicatorView);
                _floatingIndicatorView = null;
                Log.d(LOG_TAG, "removeView");
                return;
            }

            if (_floatingIndicatorView != null) {
                windowManager.removeView(_floatingIndicatorView);
            }

            final WindowManager.LayoutParams params = getLayoutParams(appSettings);
            _floatingIndicatorView = (ConstraintLayout) LayoutInflater.from(_context).inflate(R.layout.activity_floating_indicator, null);
            windowManager.addView(_floatingIndicatorView, params);

            _textView = _floatingIndicatorView.findViewById(R.id.floatingIndicatorText);
            _flagView = _floatingIndicatorView.findViewById(R.id.floatingIndicatorFlag);

            _flagView.getLayoutParams().width = appSettings.floatingIconFlagSize;
            _flagView.requestLayout();

            _textView.setTextSize(appSettings.floatingIconTextSize);
            _textView.setTextColor(appSettings.floatingIconTextColor);
            _textView.requestLayout();

            _floatingIndicatorView.setBackgroundColor(appSettings.floatingIconBackgroundColor);
            _floatingIndicatorView.setAlpha(appSettings.opacity);
            _floatingIndicatorView.requestLayout();
            //createFloatingWindow();
            Log.d(LOG_TAG, "init complete");
        } catch (Exception ex) {
            Log.d(LOG_TAG, "FloatingIndicatorManager: " + ex.toString());
        }
    }


    private static View _mFloatingView;
    private static View _collapsedView;
    private static View _expandedView;
    private static TextView _textViewCollapsed;
    private static TextView _textViewExpanded;
    private static ImageView _flagViewCollapsed;
    private static ImageView _flagViewExpanded;
    private void createFloatingWindow() {
        if (_mFloatingView != null && _collapsedView != null && _expandedView != null) {
            return;
        }

        //getting the widget layout from xml using layout inflater
        _mFloatingView = LayoutInflater.from(_context).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        final WindowManager windowManager = (WindowManager) _context.getSystemService(WINDOW_SERVICE);
        windowManager.addView(_mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        _collapsedView = _mFloatingView.findViewById(R.id.layoutCollapsed);
        _expandedView = _mFloatingView.findViewById(R.id.layoutExpanded);

        // Flags
        _flagViewCollapsed = _mFloatingView.findViewById(R.id.collapsed_flag_view);
        _flagViewExpanded = _mFloatingView.findViewById(R.id.expanded_flag_view);
        // Text
        _textViewCollapsed = _mFloatingView.findViewById(R.id.collapsed_text_view);
        _textViewExpanded = _mFloatingView.findViewById(R.id.expanded_text_view);

        //adding click listener to close button and expanded view
        //_mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(this);
        _expandedView.setOnClickListener(this);

        //adding an touchlistener to make drag movement of the floating widget
        _mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        //when the drag is ended switching the state of the widget
                        _collapsedView.setVisibility(View.GONE);
                        _expandedView.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_UP:
                        //when the drag is ended switching the state of the widget
                        _collapsedView.setVisibility(View.VISIBLE);
                        _expandedView.setVisibility(View.GONE);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(_mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutExpanded:
                //switching views
                _collapsedView.setVisibility(View.VISIBLE);
                _expandedView.setVisibility(View.GONE);
                break;
            /*case R.id.buttonClose:
                //closing the widget
                //stopSelf();
                break;*/
        }
    }
}
