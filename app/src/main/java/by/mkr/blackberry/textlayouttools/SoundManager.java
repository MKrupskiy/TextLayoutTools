package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;


enum SoundPattern {
    None,
    En,
    Ru,
    Switch,
    Reverse,
    Misprint,
    ClickXP,
    Exclamation,
    Stop;

    public static SoundPattern fromString(String x) {
        switch (x) {
            case "En":
                return En;
            case "Ru":
                return Ru;
            case "Switch":
                return Switch;
            case "Reverse":
                return Reverse;
            case "Misprint":
                return Misprint;
            case "ClickXP":
                return ClickXP;
            case "Exclamation":
                return Exclamation;
            case "Stop":
                return Stop;
            default:
                return None;
        }
    }
}

public class SoundManager {
    private Context _context;

    public SoundManager(Context context) {
        _context = context;
    }

    public void play(SoundPattern pattern) {
        if (pattern == SoundPattern.None || !isSoundOn()) {
            // No need to play sound
            return;
        }
        play(getPatternValue(pattern));
    }

    public void play(int soundResId) {
        MediaPlayer mp;
        mp = MediaPlayer.create(_context, soundResId);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                mp = null;
            }
        });
        mp.start();
    }

    public boolean isSoundOn() {
        AudioManager audio = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        boolean isSoundOn = false;

        switch( audio.getRingerMode() ){
            case AudioManager.RINGER_MODE_NORMAL:
                isSoundOn = true;
                Log.d("ReplacerLog", "Vibration ON: RINGER_MODE_NORMAL");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                isSoundOn = false;
                Log.d("ReplacerLog", "Vibration ON: RINGER_MODE_VIBRATE");
                break;
            case AudioManager.RINGER_MODE_SILENT:
                isSoundOn = false;
                Log.d("ReplacerLog", "Vibration ON: RINGER_MODE_SILENT");
                break;
            default: {
                Log.d("ReplacerLog", "Vibration ON: UNKNOWN");
                break;
            }
        }
        Log.d("ReplacerLog", "Sound ON: " + isSoundOn);
        return isSoundOn;
    }

    private int getPatternValue(SoundPattern pattern) {
        switch (pattern) {
            case En: {
                return R.raw.en_ps;
            }
            case Ru: {
                return R.raw.ru_ps;
            }
            case Switch: {
                return R.raw.switch_ps;
            }
            case Reverse: {
                return R.raw.reverse_ps;
            }
            case Misprint: {
                return R.raw.misprint_ps;
            }
            case ClickXP: {
                return R.raw.click_xp;
            }
            case Exclamation: {
                return R.raw.exclamation;
            }
            case Stop: {
                return R.raw.stop;
            }
            default: {
                return R.raw.switch_ps;
            }
        }
    }
}
