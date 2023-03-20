package com.example.pol_eng_translator;

public class Word_Model {
    public int Id;
    public String Wordpl;
    public String Worden;

    public Word_Model(int id, String wordpl, String worden) {
        this.Id = id;
        this.Wordpl = wordpl;
        this.Worden = worden;
    }

    public int getId() {
        return Id;
    }

    public String getWordpl() {
        return Wordpl;
    }

    public String getWorden() {
        return Worden;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setWorden(String worden) {
        Worden = worden;
    }

    public void setWordpl(String wordpl) {
        Wordpl = wordpl;
    }

    @Override
    public String toString() {
        return getWordpl() + " - " + getWorden();
    }
}
