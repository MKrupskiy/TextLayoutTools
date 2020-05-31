package by.mkr.blackberry.textlayouttools;

import android.util.Log;
import java.util.HashMap;
import DataBase.Correction;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


enum Language {
    Unknown,
    En,
    Ru,
    RuTrans,
    EnTrans,
    RuFull,
    EnFull,
    RuQwertz,
    EnQwertz,

    Ukr;

    public static Language fromString(String x) {
        switch (x) {
            case "Unknown":
                return Unknown;
            case "En":
                return En;
            case "Ru":
                return Ru;
            case "EnTrans":
                return EnTrans;
            case "RuTrans":
                return RuTrans;
            case "EnFull":
                return EnFull;
            case "RuFull":
                return RuFull;
            case "EnQwertz":
                return EnQwertz;
            case "RuQwertz":
                return RuQwertz;
            case "Ukr":
                return Ukr;
        }
        return null;
    }

    public static Language getByInputMethod(Language lang /*Ru/En*/, InputMethod inputMethod) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull:
            case RuQwertz: {
                switch (inputMethod) {
                    case Qwerty:
                        return Ru;
                    case Translit:
                        return RuTrans;
                    case UsbKb:
                        return RuFull;
                    case Qwertz:
                        return RuQwertz;
                }
            }
            case En:
            case EnTrans:
            case EnFull:
            case EnQwertz: {
                switch (inputMethod) {
                    case Qwerty:
                        return En;
                    case Translit:
                        return EnTrans;
                    case UsbKb:
                        return EnFull;
                    case Qwertz:
                        return EnQwertz;
                }
            }
        }
        return null;
    }

    public boolean isRus() {
        return this == Language.Ru || this == Language.RuTrans || this == Language.RuFull || this == Language.RuQwertz;
    }
    public boolean isEng() {
        return this == Language.En || this == Language.EnTrans || this == Language.EnFull || this == Language.EnQwertz;
    }
    public Language getOpposite() {
        switch (this) {
            case En:
                return Ru;
            case Ru:
                return En;
            case EnTrans:
                return RuTrans;
            case RuTrans:
                return EnTrans;
            case EnFull:
                return RuFull;
            case RuFull:
                return EnFull;
            case EnQwertz:
                return RuQwertz;
            case RuQwertz:
                return EnQwertz;
            default:
                return Unknown;
        }
    }
}

enum InputMethod {
    Qwerty,
    Translit,
    UsbKb,
    Qwertz;

    public static InputMethod fromString(String x) {
        switch (x) {
            case "Qwerty":
            case "0":
                return Qwerty;
            case "Translit":
            case "1":
                return Translit;
            case "UsbKb":
            case "2":
                return UsbKb;
            case "Qwertz":
            case "3":
                return Qwertz;
        }
        return null;
    }
}


public class LayoutConverter {
    /*
    private static List<Correction> _corrections = App.getInstance().getDatabase().correctionDao().getAll();

    public static void setObserve(LifecycleOwner owner) {
        App.getInstance().getDatabase().correctionDao().getAllAsync().observe(owner, new Observer<List<Correction>>() {
            @Override
            public void onChanged(@Nullable List<Correction> corrections) {
                _corrections = corrections;
                Log.d("ReplacerLog", "! Corrections updated");
            }
        });
    }
    */


    public static String getReplacedText(CharSequence textToReplace, Language fromLanguage) {
        StringBuilder text = new StringBuilder();
        Log.d(LOG_TAG, textToReplace.toString() + "; lang=" + fromLanguage);

        switch (fromLanguage) {
            case En: {
                textToReplace = textToReplace.toString().replaceAll("\\$\\$", "Á");
                textToReplace = textToReplace.toString().replaceAll("(?i)ll", "Ç");
                textToReplace = textToReplace.toString().replaceAll("(?i)oo", "È");
                textToReplace = textToReplace.toString().replaceAll("(?i)pp", "Ñ");
                textToReplace = textToReplace.toString().replaceAll("(?i)mm", "Ò");
                textToReplace = textToReplace.toString().replaceAll("(?i)qq", "Ž");

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapEnRu.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapEnRu.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            case Ru: {
                //Log.d(LOG_TAG, textToReplace.toString());
                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapRuEn.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapRuEn.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            case RuTrans: {
                //Log.d(LOG_TAG, textToReplace.toString());
                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapRuTransEn.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapRuTransEn.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            case EnTrans: {
                //Log.d(LOG_TAG, textToReplace.toString());
                textToReplace = textToReplace.toString().replaceAll("(?i)ww", "Ç");
                textToReplace = textToReplace.toString().replaceAll("(?i)ee", "È");
                textToReplace = textToReplace.toString().replaceAll("(?i)uu", "Ñ");
                textToReplace = textToReplace.toString().replaceAll("(?i)zz", "Ò");

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapEnRuTrans.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapEnRuTrans.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            case RuFull: {
                //Log.d(LOG_TAG, textToReplace.toString());
                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapRuFullEn.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapRuFullEn.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            case EnFull: {
                //Log.d(LOG_TAG, textToReplace.toString());
                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapEnRuFull.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapEnRuFull.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }
            case EnQwertz: {
                textToReplace = textToReplace.toString().replaceAll("\\$\\$", "Á");
                textToReplace = textToReplace.toString().replaceAll("(?i)ll", "Ç");
                textToReplace = textToReplace.toString().replaceAll("(?i)oo", "È");
                textToReplace = textToReplace.toString().replaceAll("(?i)pp", "Ñ");
                textToReplace = textToReplace.toString().replaceAll("(?i)mm", "Ò");
                textToReplace = textToReplace.toString().replaceAll("(?i)qq", "Ž");

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapEnRuQwertz.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapEnRuQwertz.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            case RuQwertz: {
                //Log.d(LOG_TAG, textToReplace.toString());
                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapRuQwertzEn.containsKey(textToReplace.charAt(i))) {
                        text.append(charsMapRuQwertzEn.get(textToReplace.charAt(i)));
                    } else {
                        text.append(textToReplace.charAt(i));
                    }
                }
                break;
            }

            default: {
                Log.d(LOG_TAG, "Unexpected language");
                text = new StringBuilder(textToReplace.toString());
                break;
            }
        }

        // TODO APIs
        // http://google.com/complete/search?client=chrome&hl=ru&q=суюоту
        // http://suggestqueries.google.com/complete/search?client=chrome&hl=ru&q=суюоту


        // Handle exception words
        /*
        HashMap<String, String> exceptionWords = new HashMap<String, String>() {{
            put("пожо", "поддо");
            put("пожан", "поддан");
            put("пожерж", "поддерж");
            put("пожуб", "поддуб");
        }};

        for (HashMap.Entry<String, String> entry : exceptionWords.entrySet()) {
            text = text.replaceAll("(?i)" + entry.getKey(), entry.getValue());
        }
        */

        for (Correction corr : App.getCorrections()) {
            text = new StringBuilder(text.toString().replaceAll("(?i)" + corr.fromText, corr.toText));
        }


        /*
        Log.d(LOG_TAG, "! Corrections count=" + _corrections.size());
        for (Correction corr : _corrections) {
            text = text.replaceAll("(?i)" + corr.fromText, corr.toText);
        }
        */


        //Log.d(LOG_TAG, text);
        return text.toString();
    }

    public static Language getTextLanguage(CharSequence text, InputMethod inputMethod) {
        Language targetLang = Language.Unknown;
        String lowered = text.toString().toLowerCase();

        for (int i = 0; i < lowered.length(); i++) {
            if (lowered.charAt(i) >= 'a' && lowered.charAt(i) <= 'z') {
                switch (inputMethod) {
                    case Qwerty:
                        targetLang = Language.En;
                        break;
                    case Translit:
                        targetLang = Language.EnTrans;
                        break;
                    case UsbKb:
                        targetLang = Language.EnFull;
                        break;
                    case Qwertz:
                        targetLang = Language.EnQwertz;
                        break;
                    default:
                        break;
                }
                break;
            } else if (lowered.charAt(i) >= 'а' && lowered.charAt(i) <= 'я') {
                switch (inputMethod) {
                    case Qwerty:
                        targetLang = Language.Ru;
                        break;
                    case Translit:
                        targetLang = Language.RuTrans;
                        break;
                    case UsbKb:
                        targetLang = Language.RuFull;
                        break;
                    case Qwertz:
                        targetLang = Language.RuQwertz;
                        break;
                    default:
                        break;
                }
                break;
            }
        }

        return targetLang;
    }

    public static boolean isDoubled(char letter, Language language) {
        String doubledChars = language == Language.Ru ? "дбхзьй" : "шеуж";
        return doubledChars.indexOf(Character.toLowerCase(letter)) > -1;
    }

    public static char getSpareLetter(char letter, Language language) {
        char spare = letter;

        switch (language) {
            case Ru: {
                switch (letter) {
                    case 'д': { spare = 'ж'; break; }
                    case 'б': { spare = 'ю'; break; }
                    case 'х': { spare = 'э'; break; }
                    case 'з': { spare = 'щ'; break; }
                    case 'ь': { spare = 'ъ'; break; }
                    case 'й': { spare = 'ё'; break; }

                    case 'Д': { spare = 'Ж'; break; }
                    case 'Б': { spare = 'Ю'; break; }
                    case 'Х': { spare = 'Э'; break; }
                    case 'З': { spare = 'Щ'; break; }
                    case 'Ь': { spare = 'Ъ'; break; }
                    case 'Й': { spare = 'Ё'; break; }

                    default: { break; }
                }
                break;
            }
            case RuTrans: {
                switch (letter) {
                    case 'ш': { spare = 'щ'; break; }
                    case 'е': { spare = 'э'; break; }
                    case 'у': { spare = 'ю'; break; }
                    case 'ж': { spare = 'з'; break; }

                    case 'Ш': { spare = 'Щ'; break; }
                    case 'Е': { spare = 'Э'; break; }
                    case 'У': { spare = 'Ю'; break; }
                    case 'Ж': { spare = 'З'; break; }

                    default: { break; }
                }
                break;
            }
            default: { break; } //Lang
        }
        return spare;
    }

    public static boolean isNeedDoubleCapitalCorrection(CharSequence textToCorrect) {
        if (textToCorrect != null && textToCorrect.length() < 3) {
            return false;
        }
        /*boolean isAllUpper = true;
        for (int i = 0; i < textToCorrect.length(); i++) {
            if (Character.isLowerCase(textToCorrect.charAt(i))) {
                isAllUpper = false;
                break;
            }
        }*/
        // Correct 'ПРивет', but not 'HELLo'
        boolean isNeedsCorrection = Character.isUpperCase(textToCorrect.charAt(0))
                && Character.isUpperCase(textToCorrect.charAt(1))
                && Character.isLowerCase(textToCorrect.charAt(2));
        Log.d(LOG_TAG, "Needs corect double capital: " + isNeedsCorrection + "; " + textToCorrect);
        return isNeedsCorrection;
    }

    public static String getTextWithoutDoubledCapital(CharSequence textToCorrect) {
        if (isNeedDoubleCapitalCorrection(textToCorrect)) {
            StringBuilder corrected = new StringBuilder(textToCorrect);
            corrected.setCharAt(1, Character.toLowerCase(textToCorrect.charAt(1)));
            Log.d(LOG_TAG, "getTextWithoutDoubledCapital change: " + corrected.toString());
            return corrected.toString();
        } else {
            Log.d(LOG_TAG, "getTextWithoutDoubledCapital no change: " + textToCorrect.toString());
            return textToCorrect.toString();
        }
    }


    // Usual BB QWERTY
    private static final HashMap<Character, String> charsMapEnRu = new HashMap<Character, String>() {{
        put('q',"й"); put('Ž',"ё");         put('Q',"Й");
        put('w',"ц");                       put('W',"Ц");
        put('e',"у");                       put('E',"У");
        put('r',"к");                       put('R',"К");
        put('t',"е");                       put('T',"Е");
        put('y',"н");                       put('Y',"Н");
        put('u',"г");                       put('U',"Г");
        put('i',"ш");                       put('I',"Ш");
        put('o',"з"); put('È',"щ");         put('O',"З"); //put('È',"Щ");
        put('p',"х"); put('Ñ',"э");         put('P',"Х"); //put('Ñ',"Э");

        put('a',"ф");                       put('A',"Ф");
        put('s',"ы");                       put('S',"Ы");
        put('d',"в");                       put('D',"В");
        put('f',"а");                       put('F',"А");
        put('g',"п");                       put('G',"П");
        put('h',"р");                       put('H',"Р");
        put('j',"о");                       put('J',"О");
        put('k',"л");                       put('K',"Л");
        put('l',"д"); put('Ç',"ж");         put('L',"Д"); //put('Ç',"Ж");

        put('z',"я");                       put('Z',"Я");
        put('x',"ч");                       put('X',"Ч");
        put('c',"с");                       put('C',"С");
        put('v',"м");                       put('V',"М");
        put('b',"и");                       put('B',"И");
        put('n',"т");                       put('N',"Т");
        put('m',"ь"); put('Ò',"ъ");         put('M',"Ь"); //put('Ò',"Ъ");
        put('$',"б"); put('Á',"ю");         put('€',"Б"); //put('Á',"Ю");
    }};

    private static final HashMap<Character, String> charsMapRuEn = new HashMap<Character, String>() {{
        put('й',"q"); put('ё',"qq");        put('Й',"Q"); put('Ё',"Qq");
        put('ц',"w");                       put('Ц',"W");
        put('у',"e");                       put('У',"E");
        put('к',"r");                       put('К',"R");
        put('е',"t");                       put('Е',"T");
        put('н',"y");                       put('Н',"Y");
        put('г',"u");                       put('Г',"U");
        put('ш',"i");                       put('Ш',"I");
        put('з',"o"); put('щ',"oo");        put('З',"O"); put('Щ',"Oo");
        put('х',"p"); put('э',"pp");        put('Х',"P"); put('Э',"Pp");

        put('ф',"a");                       put('Ф',"A");
        put('ы',"s");                       put('Ы',"S");
        put('в',"d");                       put('В',"D");
        put('а',"f");                       put('А',"F");
        put('п',"g");                       put('П',"G");
        put('р',"h");                       put('Р',"H");
        put('о',"j");                       put('О',"J");
        put('л',"k");                       put('Л',"K");
        put('д',"l"); put('ж',"ll");        put('Д',"L"); put('Ж',"Ll");

        put('я',"z");                       put('Я',"Z");
        put('ч',"x");                       put('Ч',"X");
        put('с',"c");                       put('С',"C");
        put('м',"v");                       put('М',"V");
        put('и',"b");                       put('И',"B");
        put('т',"n");                       put('Т',"N");
        put('ь',"m"); put('ъ',"mm");        put('Ь',"M"); put('Ъ',"Mm");
        put('б',"$"); put('ю',"$$");        put('Б',"$"); put('Ю',"$$");
    }};



    // BB TRANSLIT
    private static final HashMap<Character, String> charsMapEnRuTrans = new HashMap<Character, String>() {{
        put('q',"я");                       put('Q',"Я");
        put('w',"ш"); put('Ç',"щ");         put('W',"Ш"); //put('Ç',"Щ");
        put('e',"е"); put('È',"э");         put('E',"Е"); //put('È',"Э");
        put('r',"р");                       put('R',"Р");
        put('t',"т");                       put('T',"Т");
        put('y',"ы");                       put('Y',"Ы");
        put('u',"у"); put('Ñ',"ю");         put('U',"У"); //put('Ñ',"Ю");
        put('i',"и");                       put('I',"И");
        put('o',"о");                       put('O',"О");
        put('p',"п");                       put('P',"П");

        put('a',"а");                       put('A',"А");
        put('s',"с");                       put('S',"С");
        put('d',"д");                       put('D',"Д");
        put('f',"ф");                       put('F',"Ф");
        put('g',"г");                       put('G',"Г");
        put('h',"ч");                       put('H',"Ч");
        put('j',"й");                       put('J',"Й");
        put('k',"к");                       put('K',"К");
        put('l',"л");                       put('L',"Л");

        put('z',"ж"); put('Ò',"з");         put('Z',"Ж"); //put('Ò',"З");
        put('x',"х");                       put('X',"Х");
        put('c',"ц");                       put('C',"Ц");
        put('v',"в");                       put('V',"В");
        put('b',"б");                       put('B',"Б");
        put('n',"н");                       put('N',"Н");
        put('m',"м");                       put('M',"М");
        put('$',"ь");                       //put('$',"Ь");
    }};

    private static final HashMap<Character, String> charsMapRuTransEn = new HashMap<Character, String>() {{
        put('я',"q");                       put('Я',"Q");
        put('ш',"w"); put('щ',"ww");        put('Ш',"W"); put('Щ',"Ww");
        put('е',"e"); put('э',"ee");        put('Е',"E"); put('Э',"Ee");
        put('р',"r");                       put('Р',"R");
        put('т',"t");                       put('Т',"T");
        put('ы',"y");                       put('Ы',"Y");
        put('у',"u"); put('ю',"uu");        put('У',"U"); put('Ю',"Uu");
        put('и',"i");                       put('И',"I");
        put('о',"o");                       put('О',"O");
        put('п',"p");                       put('П',"P");

        put('а',"a");                       put('А',"A");
        put('с',"s");                       put('С',"S");
        put('д',"d");                       put('Д',"D");
        put('ф',"f");                       put('Ф',"F");
        put('г',"g");                       put('Г',"G");
        put('ч',"h");                       put('Ч',"H");
        put('й',"j");                       put('Й',"J");
        put('к',"k");                       put('К',"K");
        put('л',"l");                       put('Л',"L");

        put('ж',"z"); put('з',"zz");        put('Ж',"Z"); put('З',"Zz");
        put('х',"x");                       put('Х',"X");
        put('ц',"c");                       put('Ц',"C");
        put('в',"v");                       put('В',"V");
        put('б',"b");                       put('Б',"B");
        put('н',"n");                       put('Н',"N");
        put('м',"m");                       put('М',"M");
        put('ь',"$");                       put('Ь',"$");
    }};



    // EXTERNAL FULL keyboard
    private static final HashMap<Character, String> charsMapEnRuFull = new HashMap<Character, String>() {{
        put('`',"ё");                       put('~',"Ё");

        put('q',"й");                       put('Q',"Й");
        put('w',"ц");                       put('W',"Ц");
        put('e',"у");                       put('E',"У");
        put('r',"к");                       put('R',"К");
        put('t',"е");                       put('T',"Е");
        put('y',"н");                       put('Y',"Н");
        put('u',"г");                       put('U',"Г");
        put('i',"ш");                       put('I',"Ш");
        put('o',"щ");                       put('O',"Щ");
        put('p',"з");                       put('P',"З");
        put('[',"х");                       put('{',"Х");
        put(']',"ъ");                       put('}',"Ъ");

        put('a',"ф");                       put('A',"Ф");
        put('s',"ы");                       put('S',"Ы");
        put('d',"в");                       put('D',"В");
        put('f',"а");                       put('F',"А");
        put('g',"п");                       put('G',"П");
        put('h',"р");                       put('H',"Р");
        put('j',"о");                       put('J',"О");
        put('k',"л");                       put('K',"Л");
        put('l',"д");                       put('L',"Д");
        put(';',"ж");                       put(':',"Ж");
        put('\'',"э");                      put('"',"Э");

        put('z',"я");                       put('Z',"Я");
        put('x',"ч");                       put('X',"Ч");
        put('c',"с");                       put('C',"С");
        put('v',"м");                       put('V',"М");
        put('b',"и");                       put('B',"И");
        put('n',"т");                       put('N',"Т");
        put('m',"ь");                       put('M',"Ь");
        put(',',"б");                       put('<',"Б");
        put('.',"ю");                       put('>',"Ю");

        put('/',".");                       put('?',",");
    }};

    private static final HashMap<Character, String> charsMapRuFullEn = new HashMap<Character, String>() {{
        put('ё',"`");                       put('Ё',"~");

        put('й',"q");                       put('Й',"Q");
        put('ц',"w");                       put('Ц',"W");
        put('у',"e");                       put('У',"E");
        put('к',"r");                       put('К',"R");
        put('е',"t");                       put('Е',"T");
        put('н',"y");                       put('Н',"Y");
        put('г',"u");                       put('Г',"U");
        put('ш',"i");                       put('Ш',"I");
        put('щ',"o");                       put('Щ',"O");
        put('з',"p");                       put('З',"P");
        put('х',"[");                       put('Х',"{");
        put('ъ',"]");                       put('Ъ',"}");

        put('ф',"a");                       put('Ф',"A");
        put('ы',"s");                       put('Ы',"S");
        put('в',"d");                       put('В',"D");
        put('а',"f");                       put('А',"F");
        put('п',"g");                       put('П',"G");
        put('р',"h");                       put('Р',"H");
        put('о',"j");                       put('О',"J");
        put('л',"k");                       put('Л',"K");
        put('д',"l");                       put('Д',"L");
        put('ж',";");                       put('Ж',":");
        put('э',"'");                       put('Э',"\"");

        put('я',"z");                       put('Я',"Z");
        put('ч',"x");                       put('Ч',"X");
        put('с',"c");                       put('С',"C");
        put('м',"v");                       put('М',"V");
        put('и',"b");                       put('И',"B");
        put('т',"n");                       put('Т',"N");
        put('ь',"m");                       put('Ь',"M");
        put('б',",");                       put('Б',"<");
        put('ю',".");                       put('Ю',">");

        put('.',"/");                       put(',',"?");
    }};

    // BB QWERTZ
    private static final HashMap<Character, String> charsMapEnRuQwertz = new HashMap<Character, String>() {{
        put('q',"й"); put('Ž',"ё");         put('Q',"Й");
        put('w',"ц");                       put('W',"Ц");
        put('e',"у");                       put('E',"У");
        put('r',"к");                       put('R',"К");
        put('t',"е");                       put('T',"Е");
        put('z',"н");                       put('Z',"Н");
        put('u',"г");                       put('U',"Г");
        put('i',"ш");                       put('I',"Ш");
        put('o',"з"); put('È',"щ");         put('O',"З"); //put('È',"Щ");
        put('p',"х"); put('Ñ',"э");         put('P',"Х"); //put('Ñ',"Э");

        put('a',"ф");                       put('A',"Ф");
        put('s',"ы");                       put('S',"Ы");
        put('d',"в");                       put('D',"В");
        put('f',"а");                       put('F',"А");
        put('g',"п");                       put('G',"П");
        put('h',"р");                       put('H',"Р");
        put('j',"о");                       put('J',"О");
        put('k',"л");                       put('K',"Л");
        put('l',"д"); put('Ç',"ж");         put('L',"Д"); //put('Ç',"Ж");

        put('y',"я");                       put('Y',"Я");
        put('x',"ч");                       put('X',"Ч");
        put('c',"с");                       put('C',"С");
        put('v',"м");                       put('V',"М");
        put('b',"и");                       put('B',"И");
        put('n',"т");                       put('N',"Т");
        put('m',"ь"); put('Ò',"ъ");         put('M',"Ь"); //put('Ò',"Ъ");
        put('$',"б"); put('Á',"ю");         put('€',"Б"); //put('Á',"Ю");
    }};

    private static final HashMap<Character, String> charsMapRuQwertzEn = new HashMap<Character, String>() {{
        put('й',"q"); put('ё',"qq");        put('Й',"Q"); put('Ё',"Qq");
        put('ц',"w");                       put('Ц',"W");
        put('у',"e");                       put('У',"E");
        put('к',"r");                       put('К',"R");
        put('е',"t");                       put('Е',"T");
        put('н',"z");                       put('Н',"Z");
        put('г',"u");                       put('Г',"U");
        put('ш',"i");                       put('Ш',"I");
        put('з',"o"); put('щ',"oo");        put('З',"O"); put('Щ',"Oo");
        put('х',"p"); put('э',"pp");        put('Х',"P"); put('Э',"Pp");

        put('ф',"a");                       put('Ф',"A");
        put('ы',"s");                       put('Ы',"S");
        put('в',"d");                       put('В',"D");
        put('а',"f");                       put('А',"F");
        put('п',"g");                       put('П',"G");
        put('р',"h");                       put('Р',"H");
        put('о',"j");                       put('О',"J");
        put('л',"k");                       put('Л',"K");
        put('д',"l"); put('ж',"ll");        put('Д',"L"); put('Ж',"Ll");

        put('я',"y");                       put('Я',"Y");
        put('ч',"x");                       put('Ч',"X");
        put('с',"c");                       put('С',"C");
        put('м',"v");                       put('М',"V");
        put('и',"b");                       put('И',"B");
        put('т',"n");                       put('Т',"N");
        put('ь',"m"); put('ъ',"mm");        put('Ь',"M"); put('Ъ',"Mm");
        put('б',"$"); put('ю',"$$");        put('Б',"$"); put('Ю',"$$");
    }};



    public static String getWordRegex(Language lang) {
        String regex = "[\\w\\$\\€]";

        switch (lang) {
            case En: {
                break;
            }
            case Ru: {
                break;
            }
            case RuTrans: {
                break;
            }
            case EnTrans: {
                break;
            }
            case RuFull: {
                regex = "[\\w,\\.~<>{}\\[\\]'\"\\`;:?/]";
                break;
            }
            case EnFull: {
                regex = "[\\w,\\.~<>{}\\[\\]'\"\\`;:?/]";
                break;
            }
            case EnQwertz: {
                break;
            }
            case RuQwertz: {
                break;
            }
            default: {
                break;
            }
        }
        return regex;
    }
}
