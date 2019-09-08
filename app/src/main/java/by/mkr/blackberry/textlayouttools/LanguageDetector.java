package by.mkr.blackberry.textlayouttools;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LanguageDetector {
    final static String LOG_TAG = "ReplacerLog";

    private String[] _russianLemmas = {
            "мам",
            "пап",
            "при",
            "про",
            "мыл",
            "рам",
            "пере",
            "пре",
            "про",
            "выс",
            "электро",
            "лож",
            "админ",
            "афиш",
            "банк",
            "бар",
            "библи",
            "бизнес",
            "вет",
            "бирж",
            "бокс",
            "буч",
            "велик",
            "ветер",
            "волок",
            "встреч",
            "выдач",
            "галош",
            "гей",
            "генерал",
            "горбуш",
            "гор",
            "госдач",
            "грим",
            "груш",
            "грыж",
            "дач",
            "депеш",
            "дилер",
            "деле",

    };
    private String[] _russianImpossibleLemmas = {
            "аъ", "аы", "аь",
            "бй",
            "вй", "вэ",
            "гй", "гф", "гх", "гъ", "гь", "гэ",
            "дй",
            "еъ", "еы", "еь", "еэ",
            "ёъ", "еы", "еь", "ёэ", "ёа", "ёе", "ёё", "ёи", "ёу", "ёф", "ёя",
            "жй", "жф", "жх", "жш", "жщ", "жз",
            "зй", "зп", "зщ",
            "иъ", "иы", "иь",
            "йё", "йж", /*"йй",*/ "йъ", "йы", "йь", "йэ",
            "кй", "кщ", "къ", "кь", "кд",
            "лй", "лъ", "лэ",
            "мй", "мъ",
            "нй",
            "оъ", "оы", "оь",
            "пв", "пг", "пж", "пз", "пй", "пъ",
            "ръ",
            "сй",
            "тй",
            "уъ", "уы", "уь",
            "фб", "фж", "фз", "фй", "фп", "фх", "фц", "фъ", "фэ",
            "хё", "хж", "хй", "хш", "хы", "хь", "хю", "хя",
            "цб", "цё", "цж", "цй", "цф", "цх", "цч", "цщ", "цъ", "ць", "цэ", "цю", "ця",
            "чб", "чг", "чз", "чй", "чп", "чф", "чщ", "чъ", "чы", "чэ", "чю", "ча",
            "шд", "шж", "шз", "шй", "шш", "шщ", "шъ", "шы", "шэ",
            "щб", "щг", "щд", "щж", "щз", "щй", "щл", "щп", "щп", "щф", "щх", "щц", "щч", "щш", "щщ", "щъ", "щы", "щь", "щэ",
            "ъа", "ъб", "ъв", "ъг", "ъд", "ъж", "ъз", "ъи", "ъй", "ък", "ъл", "ъм", "ън", "ъп", "ър", "ъс", "ът", "ъу", "ъф", "ъх", "ъц", "ъч", "ъш", "ъщ", "ъъ", "ъы", "ъи", "ъэ", "ъ", "ъ", "ъ",
            "ыа", "ыё", "ыо", "ыф", "ыъ", "ыы", "ыь", "ыэ",
            "ьа", "ьй", "ьл", "ьу", "ьь", "ьы", "ьъ",
            "эа", "эе", "эё", "эц", "эч", "эъ", "эы", "эь", /*"ээ",*/ "эю",
            "юу", "юь", "юы", "юъ",
            "яа", "яё", "яо", "яъ", "яы", "яь", "яэ"
    };

    private String[] _englishLemmas = {"hello", "world"};

    private String[] _englishImpossibleLemmas = {
            "bq", "bz",
            "cf", "cj", "cv", "cx",
            "fq", "fv", "fx", "fz",
            "gq", "gv", "gx",
            "hx", "hz",
            "jb", "jd", "jf", "jg", "jh", "jl", "jm", "jp", "jq", "jr", "js", "jt", "jv", "jw", "jx", "jy", "jz",
            "kq", "kx", "kz",
            "mx", "mz",
            "pq", "pv", "px",
            "qb", "qc", "qd", "qf", "qg", "qh", "qj", "qk", "ql", "qm", "qn", "qp", "qq", "qv", "qw", "qx", "qy", "qz",
            "sx",
            "tq",
            "vb", "vf", "vh", "vj", "vk", "vm", "vp", "vq", "vw", "vx",
            "wq", "wv", "wx",
            "xd", "xj", "xk", "xr", "xz",
            "yq", "yy",
            "zf", "zr", "zx",
    };


    public Language getTargetLanguage(String text, boolean isTranslit, WordsList wordsListRu, WordsList wordsListEn, String[] userDict) {
        text = text.toLowerCase();
        Language currentLang = LayoutConverter.getTextLanguage(text, isTranslit);
        String replacedWord = LayoutConverter.getReplacedText(text, currentLang);
        Language targetLanguage = currentLang;
        boolean isFoundRu = false;
        boolean isFoundEn = false;

        Log.d(LOG_TAG, "getTargetLanguage Lang: " + text + "; " + currentLang + "; " + replacedWord);

        switch (currentLang) {
            case Ru: {
                // Check initial language
                /*
                isFoundRu = checkInDict(text, _russianImpossibleLemmas);
                isFoundEn = checkInDict(replacedWord, _englishImpossibleLemmas);
                if (!isFoundRu && isFoundEn) {
                    targetLanguage = Language.Ru;
                } else if (isFoundRu && isFoundEn) {
                    targetLanguage = Language.Ru;
                } else if (!isFoundRu) {
                    targetLanguage = Language.Ru;
                } else {
                    targetLanguage = Language.En;
                }
                */

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

            case En: {
                // Check initial language
                /*
                isFoundEn = checkInDict(text, _englishImpossibleLemmas);
                isFoundRu = checkInDict(replacedWord, _russianImpossibleLemmas);
                if (!isFoundEn && isFoundRu) {
                    targetLanguage = Language.En;
                } else if (isFoundEn && isFoundRu) {
                    targetLanguage = Language.En;
                } else if (!isFoundEn) {
                    targetLanguage = Language.En;
                } else {
                    targetLanguage = Language.Ru;
                }
                */

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

        return targetLanguage;
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

    public static WordWithBoundaries getLastWord(String text) {
        Pattern p = Pattern.compile("\\W*([\\w\\$]+)$");
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

    public static WordWithBoundaries getWordAtPosition(String text, int cursorPosition) {
        String currentWord = "";
        WordWithBoundaries curWord = new WordWithBoundaries("", 0, 0);

        if (cursorPosition < 0) {
            currentWord = "";
        } else if (cursorPosition == text.length() - 1) {
            // Last word
            curWord = getLastWord(text);
        } else {
            // Get the word in the middle
            final Pattern pattern = Pattern.compile("[\\w\\$]+");
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
    public static int getNearestWordEnd(String text, int startPosition) {
        Log.d(LOG_TAG, "  getNearestWordEnd: '" + text + "'; start=" + startPosition);
        int currentPos = startPosition;

        if (text == null || text.length() == 0) {
            Log.d(LOG_TAG, "  empty text, return -1");
            return -1;
        }

        // Check if word border to the right
        if (text.length() > currentPos && text.substring(currentPos, currentPos + 1).matches("[\\w\\$]")) {
            Log.d(LOG_TAG, "  text at right; return +1");
            return startPosition + 1;
        }

        // Go left
        while (
                currentPos > 0 &&
                !text.substring(currentPos - 1, currentPos).matches("[\\w\\$]")
        ) {
            Log.d(LOG_TAG, "  char:" + text.substring(currentPos - 1, currentPos));
            currentPos--;
        }
        Log.d(LOG_TAG, "  go left; return " + (currentPos - 1));

        return currentPos - 1;
    }

    public static boolean isWordLetter(Character character) {
        return character.toString().matches("[\\w\\$]");
    }
}
