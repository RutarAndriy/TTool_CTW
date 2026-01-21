package com.rutar.ttool_ctw;

// ............................................................................
/// Параметри конкретного символу
/// @author Rutar_Andriy
/// 16.01.2026

public class Symbol {

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

public Symbol (String string) {

String[] params = string.split("_");

id = Integer.parseInt(params[0]);

sCode = params[2].split("code")[1];
sX    = params[3].split("x")   [1];
sY    = params[4].split("y")   [1];
sW    = params[5].split("w")   [1];
sH    = params[6].split("h")   [1];
sDx   = params[7].split("dx")  [1];
sDy   = params[8].split("dy")  [1];
sAdvx = params[9].split("advx")[1].split("\\.")[0];

if (params[1].equals("char"))
    { sChar = ""; }
else
    { sChar = "" + Utils.fromStringToChar(params[1].split("char")[1]); }

}

// ============================================================================

public Symbol (int id, String symb, String sCode, String sX, String sY,
               String sW, String sH, String sDx, String sDy, String sAdvx) {

    this.id = id;
    this.sChar = symb.isBlank() ? "" : Utils.fromCharToString(symb.charAt(0));
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
         + ", dx=" + sDx + ", dy=" + sDy + ", advx=" + sAdvx + " }";
}

// ============================================================================

public String getImageName() {
    return String.format("%03d_" + "char%s_" + "code%s_" + "x%s_" + "y%s_" +
                         "w%s_" + "h%s_" + "dx%s_" + "dy%s_" + "advx%s",
                          id, sChar, sCode, sX, sY, sW, sH, sDx, sDy, sAdvx);
}

// Кінець класу Symbol ========================================================

}