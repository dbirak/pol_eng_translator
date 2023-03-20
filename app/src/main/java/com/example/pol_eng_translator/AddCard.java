package com.example.pol_eng_translator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.regex.Pattern;

public class AddCard extends AppCompatActivity {

    //obiekt do naszej bazy danych
    Database db = new Database(AddCard.this);

    EditText pol, ang;
    Button add, correct;

    //modele tłumaczeń (polishEnglishTranslator - Pol->Eng, englishPolishTranslator - Eng->Pol)
    public Translator polishEnglishTranslator;

    //łańcuch znaków do przetłumaczenia max 1000 znaków
    private String textToTranslate = "";

    //zmienne pomocnicze służące do uruchamomienia metody tłumaczącej tekst 500ms po zakończeniu przez użytkownika wpisywaniu tekstu
    long delay = 500;
    long last_text_edit = 0;
    Handler handler = new Handler();

    //tryb tłumaczenia (true - tłumaczenie ręczne, false - tłumaczenie automatyczne)
    boolean mode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schowanie domyślego panelu górnego
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_card);

        pol = (EditText) findViewById(R.id.answer2);
        ang = (EditText) findViewById(R.id.answer);
        correct = (Button) findViewById(R.id.show);
        add = (Button) findViewById(R.id.delete);

        //tworzenie opcji tlumaczeń (options1 - Pol->Eng, option2 - Eng->Pol)
        TranslatorOptions options1 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.POLISH)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();
        polishEnglishTranslator =
                Translation.getClient(options1);

        //pobranie modelu danego języka jeżeli nie są pobrane,
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        polishEnglishTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

        pol.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){
            }
            @Override
            public void onTextChanged ( final CharSequence s, int start, int before, int count){

                if(pol.getText().toString().length() > 30) {
                    pol.setText(pol.getText().toString().substring(0, 30));
                    Toast.makeText(AddCard.this, "Maksymalna długość to 30 znaków", Toast.LENGTH_SHORT).show();
                }

                textToTranslate = pol.getText().toString();
                mode = false;
                correct.setText("POPRAW TŁUMACZENIE");
                ang.setEnabled(false);

            }
            @Override
            public void afterTextChanged ( final Editable s){

                if (s.length() >= 0) {
                    last_text_edit = System.currentTimeMillis();
                    handler.postDelayed(input_finish_checker, delay);
                } else {
                }
            }
        });

        ang.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){
            }
            @Override
            public void onTextChanged ( final CharSequence s, int start, int before, int count){

                if(ang.getText().toString().length() > 30) {
                    ang.setText(ang.getText().toString().substring(0, 30));
                    Toast.makeText(AddCard.this, "Maksymalna długość to 30 znaków", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void afterTextChanged ( final Editable s){
            }
        });
    }

    //klasa wątku uruhcamiająca się 500ms po zakończaniu przez użytkownika wpraowadzania tekstu
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                translate();
            }
        }
    };

    //metoda tłumacząca tekst z angielskiego na polski i polskiego na angielski
    public void translate() {

        polishEnglishTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                ang.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                            }
                        });

    }

    //metoda zamykająca obecny intent
    public void back(View view)
    {
        finish();
    }

    public void correctWord(View view) {
        if(mode == false) {
            if(pol.getText().toString().length() != 0) {
                mode = true;
                correct.setText("PRZETŁUMACZ AUTOMATYCZNIE");
                ang.setEnabled(true);
                ang.requestFocus();
            }
            else {
                Toast.makeText(AddCard.this, "Wprowadź tekst", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            mode = false;
            correct.setText("POPRAW TŁUMACZENIE");
            ang.setEnabled(false);
            translate();
        }
    }

    public void addWord(View view) {
        if(!Pattern.matches("^[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ _-]{1,30}$", pol.getText().toString()) || !Pattern.matches("^[A-Za-z _-]{1,30}$", ang.getText().toString()) ||
        pol.getText().toString().trim().length() == 0 || ang.getText().toString().trim().length() == 0)
        {
            Toast.makeText(AddCard.this, "Niepoprawna fiszka", Toast.LENGTH_SHORT).show();
        }
        else {
            if(db.checkExistWord(pol.getText().toString().trim().toLowerCase(), ang.getText().toString().trim().toLowerCase()))
            {
                Toast.makeText(AddCard.this, "Fiszka już istnieje", Toast.LENGTH_SHORT).show();
            }
            else {
                db.addWord(pol.getText().toString().trim().toLowerCase(), ang.getText().toString().trim().toLowerCase());
                Toast.makeText(AddCard.this, "Dodano", Toast.LENGTH_SHORT).show();

                mode = false;
                correct.setText("POPRAW TŁUMACZENIE");
                ang.setEnabled(false);
                ang.setText("");
                pol.setText("");
            }
        }
    }

}