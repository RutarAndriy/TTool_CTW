package com.rutar.ttool_ctw;

import java.io.*;
import java.awt.*;
import java.util.*;
import org.jsoup.*;
import java.nio.file.*;
import javax.imageio.*;
import java.awt.image.*;
import org.jsoup.nodes.*;

import java.util.List;

import static java.io.File.*;
import static java.awt.image.BufferedImage.*;
import static java.nio.charset.StandardCharsets.*;

// ............................................................................
/// Обробка ігрових шрифтів
/// @author Rutar_Andriy
/// 16.01.2026

public class FontProcessor {

private final ArrayList<Symbol> symbols = new ArrayList<>();
private final String charPattern = "code=\"%s\" x=\"%s\" y=\"%s\" "
                                 + "w=\"%s\" h=\"%s\" dx=\"%s\" dy=\"%s\" "
                                 + "advx=\"%s\"";

// ============================================================================
/// Розбирання *.fnt файлів на окремі символи
/// @param inputFile вхідний *.fnt файл
/// @return результат виконання операції: 0 - успіх, -1 - помилка

public int decompileFont (File inputFile) {

String imgName = inputFile.getName();
imgName = imgName.substring(0, imgName.lastIndexOf("."));

File outputDir = new File(inputFile.getParent() + separator + imgName);
outputDir.mkdir();

imgName += ".png";
BufferedImage imFont, imgSymbol;
ArrayList<String> outDesc = new ArrayList<>();
File imgFile = new File(inputFile.getParent() + separator + imgName);

// ............................................................................

try {

Symbol symbol;
imFont = ImageIO.read(imgFile);
List<String> inDesc = Files.readAllLines(inputFile.toPath(), UTF_8);

// ............................................................................

for (String line : inDesc) {
    
    // Якщо це не тег-char, то додаємо його у файл-деркриптор
    if (!line.trim().startsWith("<char")) { outDesc.add(line); }
    
    // Інакше обробляємо тег
    else {
        
        // Парсимо тег, щоб витягти з нього атрибути
        Element el = Jsoup.parse(line).getElementsByTag("char").first();
        
        // Створюємо екземпляр класу Symbol
        symbol = new Symbol(symbols.size() + 1,
                            el.attr("c"),  el.attr("code"),
                            el.attr("x"),  el.attr("y"),
                            el.attr("w"),  el.attr("h"),
                            el.attr("dx"), el.attr("dy"), el.attr("advx"));
        
        // Отримуємо окремий символ - вирізаємо частинку із заг. зображення
        imgSymbol = imFont.getSubimage(Integer.parseInt(symbol.getX()),
                                       Integer.parseInt(symbol.getY()),
                                       Integer.parseInt(symbol.getW()),
                                       Integer.parseInt(symbol.getH()));
    
        // Стаорюємо новий файл для збереження окремого символу
        imgFile = new File(outputDir.getPath() + separator
                         + symbol.getImageName() + ".png");
 
        // Зберігаємо символ як зображення і додаємо його в масив символів
        ImageIO.write(imgSymbol, "png", imgFile);
        symbols.add(symbol); } }

// ............................................................................

File descFile = new File(outputDir.getPath() + separator + "desc.txt");
Files.write(descFile.toPath(), outDesc, UTF_8);

return 0;

}

catch (Exception _) { return -1; }

}

// ============================================================================
/// Збирання окремих символів в загальний *.fnt файл
/// @param inputFile папка із вхідними даними
/// @return результат виконання операції: 0 - успіх, -1 - помилка

public int compileFont (File inputFile) {

Graphics g;
Symbol symb;
BufferedImage imgFont, imgSymb;
int fontImgW = -1, fontImgH = -1;
String[] allFiles = inputFile.list();
String number, name, charLine, charSymb;
String path = inputFile.getParent() + separator + inputFile.getName();
File descFile = new File(inputFile.getPath() + separator + "desc.txt");

File imgFile = new File(path + ".png");
File fntFile = new File(path + ".fnt");

// ............................................................................

try {

List<String> inDesc = Files.readAllLines(descFile.toPath(), UTF_8);
ArrayList<String> outDesc = new ArrayList<>();

// Витягуємо ширину і висоту зображення з тегу <font>
for (String param : inDesc.get(1).split("\" ")) {
    if (param.startsWith("imgWidth"))
        { fontImgW = Integer.parseInt(param.split("=\"")[1]); }
    if (param.startsWith("imgHeight"))
        { fontImgH = Integer.parseInt(param.split("=\"")[1]); } }

// ............................................................................

outDesc.add(inDesc.get(0));
outDesc.add(inDesc.get(1));

imgFont = new BufferedImage(fontImgW, fontImgH, TYPE_4BYTE_ABGR);
g = imgFont.getGraphics();

// ............................................................................
// Зчитуємо всі зображення та записуємо їхні параметри у *.fnt файл

for (int z = 1; z < allFiles.length; z++) {
    
    name = null;
    number = String.format("%03d", z);
    
    for (String currentFile : allFiles) {
        if (currentFile.startsWith(number))
            { name = currentFile;
              break; } }
    
    symb = new Symbol(name);
    
    // Заміна особливих символів
    switch (symb.getChar())
        { case "\"" -> charSymb = "&quot;";
          case "&"  -> charSymb = "&amp;";
          case "'"  -> charSymb = "&apos;";
          case "<"  -> charSymb = "&lt;";
          case ">"  -> charSymb = "&gt;";
          default   -> charSymb = symb.getChar(); }
    
    charLine = String.format(charPattern,  symb.getCode(),
                             symb.getX(),  symb.getY(), 
                             symb.getW(),  symb.getH(),
                             symb.getDx(), symb.getDy(),
                             symb.getAdvx());
    
    if (!symb.getChar().isEmpty())
        { charLine = "c=\"" + charSymb + "\" " + charLine; }
    
    charLine = String.format("    <char %s />", charLine);
    outDesc.add(charLine);
    
    // Малювання конкретного символу на загальному зображенні
    imgSymb = ImageIO.read(new File(inputFile.getPath() + separator + name));
    g.drawImage(imgSymb, Integer.parseInt(symb.getX()),
                         Integer.parseInt(symb.getY()), null);
}

// ............................................................................
// Запис необроблених параметрів у *.fnt файл

for (int z = 2; z < inDesc.size(); z++)
    { outDesc.add(inDesc.get(z)); }

// Запис зображення та опису шрифту
ImageIO.write(imgFont, "png", imgFile);
Files.write(fntFile.toPath(), String.join("\r\n", outDesc).getBytes(UTF_8));

return 0;
    
}

catch (Exception _) { return -1; }

}

// Кінець класу FontProcessor =================================================

}