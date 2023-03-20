package com.example.pol_eng_translator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class ExploreCards extends AppCompatActivity {

    TextView head, word, word2;
    ImageButton prev, next;
    ScrollView layout;

    //obiekt do naszej bazy danych
    Database db = new Database(ExploreCards.this);

    //pobranie wszystkich słow z bazy
    List<Word_Model> allWords;

    //index aktualnego słowa
    int index = 0;

    //model czytające tekst (ttsEN - po angielsku)
    private TextToSpeech ttsEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schowanie domyślego panelu górnego
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_explore_cards);

        word = (TextView) findViewById(R.id.word);
        word2 = (TextView) findViewById(R.id.word2);
        head = (TextView) findViewById(R.id.head2);
        prev = (ImageButton) findViewById(R.id.prev);
        next = (ImageButton) findViewById(R.id.next);
        layout = (ScrollView) findViewById(R.id.scrollView);

        head.setText("1 / " + db.countWord());

        allWords = db.getAllWords();

        word.setText(allWords.get(0).getWordpl());
        word2.setText(allWords.get(0).getWorden());

        layout.setOnTouchListener(new OnSwipeTouchListener(ExploreCards.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                next();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                prev();
            }
        });

        //inicjalizacja modelu czytającego
        ttsEN = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) ttsEN.setLanguage(new Locale("EN-us"));
            }
        });
    }

    //metoda zamykająca obecny intent
    public void back(View view)
    {
        finish();
    }

    //następne słowo
    public void nextButton(View view) { next(); };

    public void next()
    {
        if(index < allWords.size() - 1) {
            index++;

            word.setText(allWords.get(index).getWordpl());
            word2.setText(allWords.get(index).getWorden());

            int temp = index + 1;
            head.setText(temp + " / " + db.countWord());
        }
    }

    //poprzednie słowo
    public void prevButton(View view) { prev(); };

    public void prev()
    {
        if(index > 0) {
            index--;

            word.setText(allWords.get(index).getWordpl());
            word2.setText(allWords.get(index).getWorden());

            int temp = index + 1 ;
            head.setText(temp + " / " + db.countWord());
        }
    }

    //metoda czytająca tekst
    public void readText(View view)
    {
        ttsEN.speak(word2.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    //metoda kopiująca tekst
    public void copyText(View view)
    {
        if(word2.getText().toString().length() > 0) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Skopiowano", word2.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(ExploreCards.this, "Skopiowano", Toast.LENGTH_SHORT).show();
        }
    }
}