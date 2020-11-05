package by.mkr.blackberry.textlayouttools;

enum ActionType {
    AltEnter,
    AltEnterReplace,
    ManualChange,
    AutoChange,
    CtrlSpace;

    public boolean isAuto() {
        switch(this) {
            case AutoChange:
                return true;
            default:
                return false;
        }
    }

    public boolean isManual() {
        return !isAuto();
    }
}
