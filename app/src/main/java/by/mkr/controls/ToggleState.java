package by.mkr.controls;

import by.mkr.blackberry.textlayouttools.R;

public enum ToggleState {
    Left,
    Middle,
    Right;

    public static ToggleState fromString(String x) {
        switch (x) {
            case "Left":
                return Left;
            case "Middle":
                return Middle;
            case "Right":
                return Right;
            default:
                return null;
        }
    }

    public ToggleState getRight() {
        switch (this) {
            case Left:
                return Middle;
            case Middle:
                return Right;
            case Right:
                return null;
            default:
                return null;
        }
    }

    public ToggleState getLeft() {
        switch (this) {
            case Left:
                return null;
            case Middle:
                return Left;
            case Right:
                return Middle;
            default:
                return null;
        }
    }

    public ToggleState getRightCirlular() {
        switch (this) {
            case Left:
                return Middle;
            case Middle:
                return Right;
            case Right:
                return Left;
            default:
                return null;
        }
    }

    /// Animations:
    /// https://medium.com/@burakcanekici/android-animation-example-with-animated-vector-drawable-and-svg-file-3e511b77cb0c
    public int getResource() {
        switch (this) {
            case Left:
                return R.drawable.vector_toggle_left;
            case Middle:
                return R.drawable.vector_toggle_middle;
            case Right:
                return R.drawable.vector_toggle_right;
            default:
                return R.drawable.vector_toggle_left;
        }
    }
}