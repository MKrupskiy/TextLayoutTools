package by.mkr.blackberry.textlayouttools;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


public class LanguageDetector {
    public Language getTargetLanguage(String text, InputMethod inputMethod, WordsList wordsListRu, WordsList wordsListEn, String[] userDict) {
        text = text.toLowerCase();
        Language currentLang = LayoutConverter.getTextLanguage(text, inputMethod);
        String replacedWord = LayoutConverter.getReplacedText(text, currentLang);
        Language targetLanguage = currentLang;
        boolean isFoundRu = false;
        boolean isFoundEn = false;

        Log.d(LOG_TAG, "getTargetLanguage Lang: " + text + "; " + currentLang + "; " + replacedWord);

        switch (currentLang) {
            case Ru:
            case RuTrans:
            case RuFull:
            case RuQwertz: {
                // Check user's dict, both languages
                isFoundRu = checkInDict(text, userDict);
                if (isFoundRu) {
                    targetLanguage = Language.Ru;
                } else {
                    isFoundEn = checkInDict(replacedWord, userDict);
                    if (isFoundEn) {
                        targetLanguage = Language.En;
                    } else {
                        // Check main dictionary
                        targetLanguage = checkByLength(Language.Ru, Language.En, text, replacedWord, wordsListRu, wordsListEn);
                    }
                }

                break;
            }

            case En:
            case EnTrans:
            case EnFull:
            case EnQwertz: {
                // Check user's dict, both languages
                isFoundEn = checkInDict(text, userDict);
                if (isFoundEn) {
                    targetLanguage = Language.En;
                } else {
                    isFoundRu = checkInDict(replacedWord, userDict);
                    if (isFoundRu) {
                        targetLanguage = Language.Ru;
                    } else {
                        // Check main dictionary
                        targetLanguage = checkByLength(Language.En, Language.Ru, text, replacedWord, wordsListEn, wordsListRu);
                    }
                }

                break;
            }

            default: {
                break;
            }
        }

        return Language.getByInputMethod(targetLanguage, inputMethod);
    }

    public static Language checkByLength(Language lang1, Language lang2,
                                         String text1, String text2,
                                         WordsList words1, WordsList words2
    ) {
        Language targetLang = lang1;

        boolean isFound1 = false;
        boolean isFound2 = false;

        for (int i = text1.length(); i > 0; i--) {
            // Search by starts with first
            isFound1 = words1.startsWith(text1, i);
            if (!isFound1) {
                isFound2 = words2.startsWith(text2, i);
                if (isFound2) {
                    targetLang = lang2;
                }
            }

            // If not found in both dicts, search by contains
            if (!isFound1 && !isFound2) {
                isFound1 = words1.contains(text1, i);
                if (!isFound1) {
                    isFound2 = words2.contains(text2, i);
                    if (isFound2) {
                        targetLang = lang2;
                    }
                }
            }

            if (isFound1 || isFound2) {
                break;
            }
        }

        return targetLang;
    }

    public static boolean checkInDict(String text, String[] dict) {
        if (dict == null ||
                dict.length == 0 ||
                (dict.length == 1 && dict[0] != null && dict[0].trim().length() == 0)) {
            // Empty array
            return false;
        }

        boolean isFound = false;
        for (int i = 0; i < dict.length; i++) {
            if (text.contains(dict[i].trim())) {
                isFound = true;
                Log.d(LOG_TAG, "Found in user's dict: " + dict[i]);
                break;
            }
        }
        return isFound;
    }

    public static WordWithBoundaries getLastWord(String text, Language lang) {
        String regex = LayoutConverter.getWordRegex(lang);
        Pattern p = Pattern.compile("(" + regex + "+)$"); // "\\W*([\\w\\$\\€]+)$"
        Matcher m = p.matcher(text);
        int start = 0;
        int end = 0;

        if(m.find() && m.groupCount() > 0)
        {
            start = m.start();
            end = m.end();
            return new WordWithBoundaries(m.group(1), start, end);
        }
        return new WordWithBoundaries("", 0, 0);
    }

    public static WordWithBoundaries getWordAtPosition(String text, int cursorPosition, Language lang) {
        String currentWord = "";
        WordWithBoundaries curWord = new WordWithBoundaries("", 0, 0);

        if (cursorPosition < 0) {
            currentWord = "";
        } else if (cursorPosition == text.length() - 1) {
            // Last word
            curWord = getLastWord(text, lang);
        } else {
            // Get the word in the middle
            String regex = LayoutConverter.getWordRegex(lang);
            final Pattern pattern = Pattern.compile(regex + "+"); // "[\\w\\$\\€]+"
            final Matcher matcher = pattern.matcher(text);
            int start = 0;
            int end = 0;

            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                if (start <= cursorPosition && cursorPosition <= end) {
                    currentWord = text.subSequence(start, end).toString();
                    curWord = new WordWithBoundaries(currentWord, start, end);
                    break;
                }
            }
        }

        return curWord;
    }

    // Search to the left
    public static int getNearestWordEnd(String text, int startPosition, Language lang) {
        String regex = LayoutConverter.getWordRegex(lang);

        Log.d(LOG_TAG, "  getNearestWordEnd: '" + text + "'; start=" + startPosition + "'; lang=" + lang);
        int currentPos = startPosition;

        if (text == null || text.length() == 0) {
            Log.d(LOG_TAG, "  empty text, return -1");
            return -1;
        }

        // Check if word border to the right
        if (text.length() > currentPos && text.substring(currentPos, currentPos + 1).matches(regex)) {
            Log.d(LOG_TAG, "  text at right; return +1");
            return startPosition + 1;
        }

        // Go left
        while (
                currentPos > 0 &&
                !text.substring(currentPos - 1, currentPos).matches(regex)
        ) {
            Log.d(LOG_TAG, "  char:" + text.substring(currentPos - 1, currentPos));
            currentPos--;
        }
        Log.d(LOG_TAG, "  go left; return " + (currentPos - 1));

        return currentPos - 1;
    }

    public static boolean isWordLetter(Character character, Language lang) {
        String regex = LayoutConverter.getWordRegex(lang);
        return character.toString().matches(regex);
    }
}
