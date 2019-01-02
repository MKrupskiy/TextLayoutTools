package by.mkr.blackberry.textlayouttools;

//import android.arch.lifecycle.Lifecycle;
//import android.arch.lifecycle.LifecycleOwner;
//import android.arch.lifecycle.LifecycleRegistry;
//import android.arch.lifecycle.Observer;
//import android.arch.persistence.room.Room;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import java.util.List;
import android.util.Log;
import java.util.HashMap;

import DataBase.AppDatabase;
import DataBase.Correction;
import DataBase.CorrectionDao;


enum Language {
    En,
    Ru,
    RuTrans,
    EnTrans
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
        String text = "";
        Log.d("ReplacerLog", textToReplace.toString() + "; lang=" + fromLanguage);

        switch (fromLanguage) {
            case En: {
                textToReplace = textToReplace.toString().replaceAll("\\$\\$", "Á");
                textToReplace = textToReplace.toString().replaceAll("(?i)ll", "Ç");
                textToReplace = textToReplace.toString().replaceAll("(?i)oo", "È");
                textToReplace = textToReplace.toString().replaceAll("(?i)pp", "Ñ");
                textToReplace = textToReplace.toString().replaceAll("(?i)mm", "Ò");

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapEnRu.containsKey(textToReplace.charAt(i))) {
                        text += charsMapEnRu.get(textToReplace.charAt(i));
                    } else {
                        text += textToReplace.charAt(i);
                    }
                }
                break;
            }

            case Ru: {
                //Log.d("ReplacerLog", textToReplace.toString());

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapRuEn.containsKey(textToReplace.charAt(i))) {
                        text += charsMapRuEn.get(textToReplace.charAt(i));
                    } else {
                        text += textToReplace.charAt(i);
                    }
                }
                break;
            }

            case RuTrans: {
                //Log.d("ReplacerLog", textToReplace.toString());

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapRuTransEn.containsKey(textToReplace.charAt(i))) {
                        text += charsMapRuTransEn.get(textToReplace.charAt(i));
                    } else {
                        text += textToReplace.charAt(i);
                    }
                }
                break;
            }

            case EnTrans: {
                //Log.d("ReplacerLog", textToReplace.toString());
                textToReplace = textToReplace.toString().replaceAll("(?i)ww", "Ç");
                textToReplace = textToReplace.toString().replaceAll("(?i)ee", "È");
                textToReplace = textToReplace.toString().replaceAll("(?i)uu", "Ñ");
                textToReplace = textToReplace.toString().replaceAll("(?i)zz", "Ò");

                for (int i = 0; i < textToReplace.length(); i++) {
                    if (charsMapEnRuTrans.containsKey(textToReplace.charAt(i))) {
                        text += charsMapEnRuTrans.get(textToReplace.charAt(i));
                    } else {
                        text += textToReplace.charAt(i);
                    }
                }
                break;
            }

            default: {
                Log.d("ReplacerLog", "Unexpected language");
                text = textToReplace.toString();
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


        AppDatabase db = App.getDatabase();
        CorrectionDao correctionDao = db.correctionDao();

        for (Correction corr : correctionDao.getAll()) {
            text = text.replaceAll("(?i)" + corr.fromText, corr.toText);
        }


        /*
        Log.d("ReplacerLog", "! Corrections count=" + _corrections.size());
        for (Correction corr : _corrections) {
            text = text.replaceAll("(?i)" + corr.fromText, corr.toText);
        }
        */


        //Log.d("ReplacerLog", text);
        return text;
    }

    public static Language getTextLanguage(CharSequence text, boolean isTranslit) {
        Language targetLang = Language.En;
        String lowered = text.toString().toLowerCase();

        for (int i = 0; i < lowered.length(); i++) {
            if (lowered.charAt(i) >= 'a' && lowered.charAt(i) <= 'z') {
                targetLang = isTranslit ? Language.EnTrans : Language.En;
                break;
            } else if (lowered.charAt(i) >= 'а' && lowered.charAt(i) <= 'я') {
                targetLang = isTranslit ? Language.RuTrans : Language.Ru;
                break;
            }
        }

        return targetLang;
    }

    private static final HashMap<Character, String> charsMapEnRu = new HashMap<Character, String>() {{
        put('q',"й");                       put('Q',"Й");
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
        put('$',"б"); put('Á',"ю");         //put('$',"Б"); //put('Á',"Ю");
    }};

    private static final HashMap<Character, String> charsMapRuEn = new HashMap<Character, String>() {{
        put('й',"q");                       put('Й',"Q");
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
}
