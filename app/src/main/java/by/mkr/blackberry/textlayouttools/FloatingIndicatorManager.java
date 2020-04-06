package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.INPUT_METHOD_SERVICE;
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


public class FloatingIndicatorManager implements View.OnClickListener {

    private Context _context;
    private static View _mFloatingView;
    private static View _collapsedView;
    private static View _expandedView;
    private static TextView _textViewCollapsed;
    private static TextView _textViewExpanded;
    private static ImageView _flagViewCollapsed;
    private static ImageView _flagViewExpanded;


    FloatingIndicatorManager(Context context) {
        _context = context;

        initView();
    }

    public void setLanguage(Language lang) {
        if (_mFloatingView != null && _textViewCollapsed != null && _flagViewCollapsed != null) {
            AppSettings appSettings = ReplacerService.getAppSettings();

            if (!appSettings.isShowFloatingIcon) {
                return;
            }

            if (lang == Language.Ukr) {
                showFlag(R.drawable.ic_flag_ukraine_col);
            }
            if (lang.isRus()) {
                switch (appSettings.floatingIconStyleRu) {
                    case Flag: {
                        showFlag(R.drawable.ic_flag_russia_col);
                        break;
                    }
                    case TextCustom: {
                        showText(appSettings.floatingIconTextRu);
                        break;
                    }
                    default:
                        break;
                }
            }
            if (lang.isEng()) {
                switch (appSettings.floatingIconStyleEn) {
                    case Flag: {
                        showFlag(R.drawable.ic_flag_gb_col);
                        break;
                    }
                    case TextCustom: {
                        showText(appSettings.floatingIconTextEn);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    public void clearLanguage() {
        _flagViewCollapsed.setVisibility(View.GONE);
        _flagViewExpanded.setVisibility(View.GONE);
        _textViewCollapsed.setVisibility(View.GONE);
        _textViewExpanded.setVisibility(View.GONE);
    }

    public void updateSettings() {
        initView();
    }


    private void showFlag(int flagResource) {
        _flagViewCollapsed.setImageResource(flagResource);
        _flagViewExpanded.setImageResource(flagResource);
        _flagViewCollapsed.setVisibility(View.VISIBLE);
        _flagViewExpanded.setVisibility(View.VISIBLE);
        _textViewCollapsed.setVisibility(View.GONE);
        _textViewExpanded.setVisibility(View.GONE);
    }
    private void showText(String text) {
        _textViewCollapsed.setText(text);
        _textViewExpanded.setText(text);
        _flagViewCollapsed.setVisibility(View.GONE);
        _flagViewExpanded.setVisibility(View.GONE);
        _textViewCollapsed.setVisibility(View.VISIBLE);
        _textViewExpanded.setVisibility(View.VISIBLE);
    }

    /*
    private static WindowManager.LayoutParams getLayoutParams(AppSettings appSettings) {
        int layout_params = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int isStatusBarFlag = appSettings.floatingIconGravityVert == FloatingIconGravityVert.StatusBar
                ? WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                : 0;

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
    */

    private void savePosition(int x, int y) {
        //Log.d(LOG_TAG, "POS [" + x + ", " + y + "]");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putInt(_context.getString(R.string.setting_floating_icon_position_x), x);
        prefsEditor.putInt(_context.getString(R.string.setting_floating_icon_position_y), y);
        prefsEditor.commit();
    }

    private void initView() {
        try {
            Log.d(LOG_TAG, "FloatingIndicator: initView");
            AppSettings appSettings = ReplacerService.getAppSettings();
            if (appSettings == null) {
                return;
            }

            final WindowManager windowManager = (WindowManager) _context.getSystemService(WINDOW_SERVICE);

            if (!appSettings.isShowFloatingIcon) {
                windowManager.removeView(_mFloatingView);
                _mFloatingView = null;
                Log.d(LOG_TAG, "FloatingIndicator: removeView");
                return;
            }

            if (_mFloatingView != null) {
                windowManager.removeView(_mFloatingView);
            }

            //getting the widget layout from xml using layout inflater
            _mFloatingView = LayoutInflater.from(_context).inflate(R.layout.layout_floating_widget, null);
            _mFloatingView.setBackgroundColor(appSettings.floatingIconBackgroundColor);
            _mFloatingView.setAlpha(appSettings.opacity);
            _mFloatingView.requestLayout();

            // If no actions required, disable touch events
            int flagNotTouchable = !appSettings.isFloatingIconUnlocked && !appSettings.isFloatingIconShowLangPicker
                    ? WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    : 0;

            //setting the layout parameters
            int layout_params = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layout_params,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | flagNotTouchable,
                    PixelFormat.TRANSLUCENT);

            params.x = appSettings.floatingIconPositionX;
            params.y = appSettings.floatingIconPositionY;


            //getting windows services and adding the floating view to it
            windowManager.addView(_mFloatingView, params);


            //getting the collapsed and expanded view from the floating view
            _collapsedView = _mFloatingView.findViewById(R.id.layoutCollapsed);
            _expandedView = _mFloatingView.findViewById(R.id.layoutExpanded);

            // Flags
            _flagViewCollapsed = _mFloatingView.findViewById(R.id.collapsed_flag_view);
            _flagViewCollapsed.getLayoutParams().width = appSettings.floatingIconFlagSize;
            _flagViewCollapsed.requestLayout();
            _flagViewExpanded = _mFloatingView.findViewById(R.id.expanded_flag_view);
            _flagViewExpanded.getLayoutParams().width = appSettings.floatingIconFlagSize;
            _flagViewExpanded.requestLayout();
            // Text
            _textViewCollapsed = _mFloatingView.findViewById(R.id.collapsed_text_view);
            _textViewCollapsed.setTextSize(appSettings.floatingIconTextSize);
            _textViewCollapsed.setTextColor(appSettings.floatingIconTextColor);
            _textViewCollapsed.requestLayout();
            _textViewExpanded = _mFloatingView.findViewById(R.id.expanded_text_view);
            _textViewExpanded.setTextSize(appSettings.floatingIconTextSize);
            _textViewExpanded.setTextColor(appSettings.floatingIconTextColor);
            _textViewExpanded.requestLayout();

            //adding click listener to expanded view
            _expandedView.setOnClickListener(this);

            //adding an touchlistener to make drag movement of the floating widget
            _mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private boolean isMoving;
                private boolean isMovingHappened;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    AppSettings appSettings = ReplacerService.getAppSettings();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (!isMoving) {
                                // Show dialog if allowed and not after moving
                                if (isMovingHappened) {
                                    isMovingHappened = false;
                                } else {
                                    showLanguagePicker();
                                }
                            } else {
                                savePosition(params.x, params.y);
                            }
                            if (!appSettings.isFloatingIconUnlocked) {
                                return false;
                            }
                            isMoving = false;
                            //when the drag is ended switching the state of the widget
                            _collapsedView.setVisibility(View.VISIBLE);
                            _expandedView.setVisibility(View.GONE);
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            if (appSettings.isFloatingIconUnlocked) {
                                if (!isMoving) {
                                    //when the drag is started switching the state of the widget
                                    _collapsedView.setVisibility(View.GONE);
                                    _expandedView.setVisibility(View.VISIBLE);
                                    isMoving = true;
                                }
                                //this code is helping the widget to move around the screen with fingers
                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                                windowManager.updateViewLayout(_mFloatingView, params);
                                return true;
                            } else {
                                isMovingHappened = true;
                            }
                    }
                    return false;
                }
            });
        } catch (Exception ex) {
            Log.d(LOG_TAG, "EX FloatingIndicator: " + ex.toString());
        }
    }

    private void showLanguagePicker() {
        AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings.isFloatingIconShowLangPicker) {
            InputMethodManager imeManager2 = (InputMethodManager) _context.getSystemService(INPUT_METHOD_SERVICE);
            imeManager2.showInputMethodPicker();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutExpanded:
                //switching views
                _collapsedView.setVisibility(View.VISIBLE);
                _expandedView.setVisibility(View.GONE);
                break;
        }
    }
}
