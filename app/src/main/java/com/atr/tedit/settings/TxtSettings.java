package com.atr.tedit.settings;

import android.util.Log;

import com.atr.tedit.util.FontUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TxtSettings implements Cloneable {
    public boolean saved = true;

    public int scrollPosX = 0;
    public int scrollPosY = 0;
    public int selectionStart = 0;
    public int selectionEnd = 0;
    public int utilityBarLayer = 0;

    public boolean searchActive = false;
    public String searchPhrase = "";
    public String searchReplacePhrase = "";
    public boolean searchWholeWord = false;
    public boolean searchMatchCase = false;

    public String typeface = "";
    public int pointSize = -1;
    public int wordWrap = -1;
    public int textDirection = -1;

    public TxtSettings() {
    }

    public TxtSettings(final byte[] data) {
        fromByteArray(data);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DataOutputStream dOut = new DataOutputStream(bOut);
        try {
            dOut.writeBoolean(saved);
            dOut.writeInt(scrollPosX);
            dOut.writeInt(scrollPosY); //Scrollpos
            dOut.writeInt(selectionStart); //selection start
            dOut.writeInt(selectionEnd); //selection end
            dOut.writeInt(utilityBarLayer); //text bar layer
            dOut.writeBoolean(searchActive); //search active
            dOut.writeUTF(searchPhrase); //search phrase
            dOut.writeUTF(searchReplacePhrase); //search replace phrase
            dOut.writeBoolean(searchWholeWord); //search whole word
            dOut.writeBoolean(searchMatchCase); //search match case
            dOut.writeUTF(typeface); //typeface
            dOut.writeInt(pointSize); //pointsize
            dOut.writeInt(wordWrap); //word wrap
            dOut.writeInt(textDirection); //text direction
        } catch (IOException e) {
            Log.w("TEdit Text Settings", "An error occurred writing settings data: " + e.getMessage());
        }

        return bOut.toByteArray();
    }

    public TxtSettings fromByteArray(final byte[] data) {
        ByteArrayInputStream bIn = new ByteArrayInputStream(data);
        DataInputStream dIn = new DataInputStream(bIn);
        try {
            saved = dIn.readBoolean();
            scrollPosX = dIn.readInt();
            scrollPosY = dIn.readInt();
            selectionStart = dIn.readInt();
            selectionEnd = dIn.readInt();
            utilityBarLayer = dIn.readInt();
            searchActive = dIn.readBoolean();
            searchPhrase = dIn.readUTF();
            searchReplacePhrase = dIn.readUTF();
            searchWholeWord = dIn.readBoolean();
            searchMatchCase = dIn.readBoolean();
            typeface = dIn.readUTF();
            pointSize = dIn.readInt();
            wordWrap = dIn.readInt();
            textDirection = dIn.readInt();
        } catch (IOException e) {
            Log.w("TEdit Text Settings", "An error occurred reading settings data: " + e.getMessage());
        }

        return this;
    }

    @Override
    public TxtSettings clone() {
        return new TxtSettings(toByteArray());
    }
}
