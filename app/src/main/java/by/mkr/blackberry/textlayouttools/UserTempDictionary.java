package by.mkr.blackberry.textlayouttools;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;

public class UserTempDictionary {
    // Save as string with delimiters in settings
    public static final int MAX_OCCURENCES = 3;
    public static final int MAX_DICT_LENGTH = 100;

    private static final String WORD_COUNT_DELIMITER = "»";
    private static final String PAIRS_DELIMITER = "¦";

    private HashMap<String, Integer> _userTempDict;

    public UserTempDictionary() {
        _userTempDict = new HashMap<String, Integer>();
    }

    public UserTempDictionary(String dictionaryString) {
        _userTempDict = new HashMap<String, Integer>();
        try {
            if (dictionaryString != null && dictionaryString != "") {
                String[] pairs = dictionaryString.split(PAIRS_DELIMITER);
                for (int i = 0; i < pairs.length; i++) {
                    if (pairs[i] != null && pairs[i] != "") {
                        String pair[] = pairs[i].split(WORD_COUNT_DELIMITER);
                        _userTempDict.put(pair[0], Integer.parseInt(pair[1]));
                    }
                }
            }
        } catch (Exception ex) {
            Log.d(LOG_TAG, "! Ex UserTempDictionary: " + ex.toString());
        }
    }

    /**
     * Adds word to TempDict or increment number of corrections for it.
     * @param word Word to save
     * @return true if word reached max count of corrections, otherwise false
     */
    public boolean addToTempDict(String word) {
        if (word == null || word == "") {
            return false;
        }
        cleanDictIfNeeded();

        word = word.toLowerCase();
        boolean shouldPromote = false;
        if (!_userTempDict.containsKey(word)) {
            _userTempDict.put(word, 1);
        } else {
            int newWordCount = _userTempDict.get(word) + 1;
            if (newWordCount < MAX_OCCURENCES) {
                _userTempDict.replace(word, newWordCount);
            } else {
                //_userTempDict.replace(word, MAX_OCCURENCES);
                removeFromTempDict(word);
                shouldPromote = true;
            }
        }
        Log.d(LOG_TAG, "UserTempDictionary size: " + _userTempDict.size());

        return shouldPromote;
    }

    public void removeFromTempDict(String word) {
        _userTempDict.remove(word);
    }

    public void cleanDictIfNeeded() {
        // Remove words with 1 correction
        cleanDictIfNeeded(2);

        // Clean again if it didn't help. Remove words with 2 corrections
        if (_userTempDict.size() >= MAX_DICT_LENGTH) {
            cleanDictIfNeeded(3);
        }
    }

    public void cleanDictIfNeeded(int minCountToStay) {
        if (_userTempDict.size() > MAX_DICT_LENGTH) {
            Log.d(LOG_TAG, "Clean temp dict [" + minCountToStay + "] Before: " + _userTempDict.size());
            ArrayList<String> wordsToRemove = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : _userTempDict.entrySet()) {
                if (entry.getValue() < minCountToStay) {
                    wordsToRemove.add(entry.getKey());
                }
            }
            for (int i = 0; i < wordsToRemove.size(); i++) {
                UserTempDictionary.this.removeFromTempDict(wordsToRemove.get(i));
            }
            Log.d(LOG_TAG, "Clean temp dict [" + minCountToStay + "] After: " + _userTempDict.size());
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : _userTempDict.entrySet()) {
            sb.append(entry.getKey())
                .append(WORD_COUNT_DELIMITER)
                .append(entry.getValue())
                .append(PAIRS_DELIMITER);
        }
        return sb.toString();
    }
}
