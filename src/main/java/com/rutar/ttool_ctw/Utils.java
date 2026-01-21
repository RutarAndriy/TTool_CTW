package com.rutar.ttool_ctw;

import java.nio.charset.*;

// ............................................................................
/// Корисні допоміжні методи
/// @author Rutar_Andriy
/// 14.01.2026

public class Utils {

// ============================================================================
/// Отримання коду символу в кодуванні cp1251
/// @param c символ
/// @return код символу в кодуванні cp1251

public static int fromCP1251CharToCode (char c) {
    
    return String.valueOf(c).getBytes(Charset.forName("cp1251"))[0] & 0xFF;
}

// ============================================================================
/// Отримання символу за його кодом в кодуванні cp1251
/// @param code код символу в кодуванні cp1251
/// @return відповідний символ

public static char fromCodeToCP1251Char (int code) {
    
    byte bCode = (byte) code;
    return new String(new byte[]{bCode}, Charset.forName("cp1251")).charAt(0);
}

// ============================================================================
/// Перетворення символу на рядок
/// @param c символ для перетворення
/// @return рядкове представлення символу

public static String fromCharToString (char c) {
    
    String result = String.valueOf(c);

    // Обробка усіх символів, які не можна використовувати в іменах 
    // файлів на Windows (\ / : * ? " < > |), а також символу "_"
    if (result.equals("\\") || result.equals("/")  ||
        result.equals(":")  || result.equals("*")  ||
        result.equals("?")  || result.equals("\"") ||
        result.equals("<")  || result.equals(">")  ||
        result.equals("|")  || result.equals("_")) {
        
        result = Integer.toString(Utils.fromCP1251CharToCode(c));
    
    }
    
    return result;
}

// ============================================================================
/// Перетворення рядка на символ
/// @param s рядок для перетворення
/// @return символьне представлення рядка

public static char fromStringToChar (String s) {
    
    if (s.length() == 1) { return s.charAt(0); }
    else { return fromCodeToCP1251Char(Integer.parseInt(s)); }  
}

// Кінець класу Utils =========================================================

}
