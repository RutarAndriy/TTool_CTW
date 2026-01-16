package com.rutar.ttool_ctw;

import java.io.*;
import java.util.*;
import org.jsoup.*;
import java.nio.file.*;
import javax.imageio.*;
import java.awt.image.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import static java.io.File.*;
import static java.lang.System.*;
import static java.nio.charset.StandardCharsets.*;

// ............................................................................
/// Обробка ігрових шрифтів
/// @author Rutar_Andriy
/// 16.01.2026

public class FontProcessor {

private final ArrayList<Symbol> symbols = new ArrayList<>();

// ============================================================================

public int decompileFont (File inputFile) {

String imageName = inputFile.getName();
imageName = imageName.substring(0, imageName.lastIndexOf("."));

File outputDir = new File(inputFile.getParent() + separator + imageName);
outputDir.mkdir();

imageName += ".png";
BufferedImage imFont, imSymbol;
File imageFile = new File(inputFile.getParent() + separator + imageName);

// ............................................................................

try {

imFont = ImageIO.read(imageFile);
Document doc = Jsoup.parse(inputFile);

Element fontTag = doc.getElementsByTag("font").first();
Attributes attributes = fontTag.attributes();

File desc = new File(outputDir.getPath() + separator + "desc.txt");
Files.writeString(desc.toPath(), attributes.html(), UTF_8);

Symbol symbol;
Elements chars = doc.getElementsByTag("char");

// ............................................................................

for (Element el : chars.asList()) {
    
    symbol = new Symbol(symbols.size() + 1,
                        el.attr("c"),  el.attr("code"),
                        el.attr("x"),  el.attr("y"),
                        el.attr("w"),  el.attr("h"),
                        el.attr("dx"), el.attr("dy"), el.attr("advx"));

    imSymbol = imFont.getSubimage(Integer.parseInt(symbol.getX()),
                                  Integer.parseInt(symbol.getY()),
                                  Integer.parseInt(symbol.getW()),
                                  Integer.parseInt(symbol.getH()));
    
    imageFile = new File(outputDir.getPath() + separator
                       + symbol.getImageName());
    
    ImageIO.write(imSymbol, "png", imageFile);
    symbols.add(symbol);

}

// ............................................................................

out.println("All OK");
return 0;

}

catch (IOException e) { return -1; }

}

// ............................................................................
/// Параметри конкретного символу
/// @author Rutar_Andriy
/// 16.01.2026

class Symbol {

private int id;
private String sChar;
private String sCode;
private String sX;
private String sY;
private String sW;
private String sH;
private String sDx;
private String sDy;
private String sAdvx;

// ============================================================================

public Symbol (int id, String sChar, String sCode, String sX, String sY,
               String sW, String sH, String sDx, String sDy, String sAdvx) {

    this.id = id;
    this.sChar = sChar.isBlank() ? "" : Utils.fromCharToString(sChar.charAt(0));
    this.sCode = sCode;
    this.sX = sX;
    this.sY = sY;
    this.sW = sW;
    this.sH = sH;
    this.sDx = sDx;
    this.sDy = sDy;
    this.sAdvx = sAdvx;

}

// ============================================================================

public int getId() { return id; }
public void setId (int id) { this.id = id; }

public String getChar() { return sChar; }
public void setChar (String sChar) { this.sChar = sChar; }

public String getCode() { return sCode; }
public void setCode (String sCode) { this.sCode = sCode; }

public String getX() { return sX; }
public void setX (String sX) { this.sX = sX; }

public String getY() { return sY; }
public void setY (String sY) { this.sY = sY; }

public String getW() { return sW; }
public void setW (String sW) { this.sW = sW; }

public String getH() { return sH; }
public void setH (String sH) { this.sH = sH; }

public String getDx() { return sDx; }
public void setDx (String sDx) { this.sDx = sDx; }

public String getDy() { return sDy; }
public void setDy (String sDy) { this.sDy = sDy; }

public String getAdvx() { return sAdvx; }
public void setAdvx (String sAdvx) { this.sAdvx = sAdvx; }

// ============================================================================

@Override
public String toString() {
    return "Symbol { id=" + id + ", char=" + sChar + ", code=" + sCode
         + ", x=" + sX + ", y=" + sY + ", w=" + sW + ", h=" + sH
         + ", Dx=" + sDx + ", Dy=" + sDy + ", Advx=" + sAdvx + " }";
}

// ============================================================================

public String getImageName() {
    return String.format("%03d_" + "cr=%s_" + "cd=%s_" + "x=%s_" + "y=%s_" +
                         "w=%s_" + "h=%s_" + "Dx=%s_" + "Dy=%s_" + "Advx=%s",
                          id, sChar, sCode, sX, sY, sW, sH, sDx, sDy, sAdvx);
}

// Кінець класу Symbol ========================================================

}

// Кінець класу FontProcessor =================================================

}