package com.rutar.ttool_ctw;

import java.io.*;
import javax.swing.*;
import java.nio.charset.*;
import javax.swing.filechooser.*;

import static com.rutar.ttool_ctw.TToolCTW.*;

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

// ============================================================================
/// Отримання налаштованого JFileChooser'а
/// @param ext розширення файлів
/// @param selectionMode тип виділення (папки, файли, папки+файли)
/// @param desc опис розширення файлів
/// @return налаштований екземпляр JFileChooser'а

public static JFileChooser getFileChooser (String ext, int selectionMode,
                                           String desc) {
    
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(desc, ext);
    
    chooser.setFileSelectionMode(selectionMode);
    chooser.removeChoosableFileFilter(chooser
           .getChoosableFileFilters()[0]);
    chooser.addChoosableFileFilter(filter);
    chooser.setCurrentDirectory(HOME_DIR);
    
    return chooser;

}

// ============================================================================
/// Отримання папки, у якій міститься останній виділений файл/папка
/// @param chooser jFileChooser, який використовувався для вибору файлу
/// @return папка, у якій міститься останній виділений файл/папка

public static File getLastDir (JFileChooser chooser) {
    
    File file = chooser.getSelectedFile();
    
    // Якщо останього файлу немає - повертаємо null
    if (file == null)
        { return null; }
    // Якщо останній файл є папкою - повертаємо батьківську папку
    else if (file.isDirectory())
        { return new File(file.getParent()); }
    // Якщо останній файл є файлом - повертаємо шлях до його папки
    else
        { return new File(file.getPath().replace(file.getName(), "")); }

}

// ============================================================================
/// Заміна невикористовуваних символів у тексті
/// @param value текст із невикористовуваними символами
/// @return текст із заміненими символами

public static String replaceUnusedChars (String value) {
    
    return value.replace('’', '\'')
                .replace('Ґ', 'Г')
                .replace('ґ', 'г');
}

// Кінець класу Utils =========================================================

}
