package com.example.pol_eng_translator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class FlashCards extends AppCompatActivity {

    //zmienna przechowująca informację o aktualnym trybie(1 - Eng->Pol, 2 - Pol->Eng)
    public static int mode = 1;

    public static void setMode(int user) {
        mode = user;
    }

    TextView head, word;
    Button show, delete;
    EditText answer;

    //obiekt do naszej bazy danych
    Database db = new Database(FlashCards.this);

    //obiekt przechowujący dane wylosowanego słowa
    Word_Model rand = new Word_Model(-1, "", "");

    //zmienna przeychowująca informacje o wciścnięciu przycisku "pokaż" (false - nie wciścnięto, true - wciśnięto)
    public boolean shoWord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schowanie domyślego panelu górnego
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_flash_cards);

        head = (TextView) findViewById(R.id.head2);
        word = (TextView) findViewById(R.id.word);
        show = (Button) findViewById(R.id.show);
        delete = (Button) findViewById(R.id.delete);
        answer = (EditText) findViewById(R.id.answer);

        if(mode == 1) {
            head.setText("Z angielskiego na polski");
        }
        else if (mode == 2) {
            head.setText("Z polskiego na angielski");
        }

        generateWord();

        answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){
            }
            @Override
            public void onTextChanged ( final CharSequence s, int start, int before, int count){
                if(shoWord == false) {
                    if (mode == 1) {
                        if (answer.getText().toString().toLowerCase(Locale.ROOT).equals(rand.Wordpl))
                            generateWord();
                    } else if (mode == 2) {
                        if (answer.getText().toString().toLowerCase(Locale.ROOT).equals(rand.Worden))
                            generateWord();
                    }
                }

            }
            @Override
            public void afterTextChanged ( final Editable s){
            }
        });
    }

    //metoda losująca fiszkę
    public void generateWord()
    {
        answer.setText("");

        if(db.countWord() == 0) {
            finish();
            Toast.makeText(FlashCards.this, "Brak fiszek", Toast.LENGTH_SHORT).show();
        }

        int wordId = rand.getId();

        do {
            List<Word_Model> randomWord = db.getRandomWord();
            rand = randomWord.get(0);
        } while(wordId == rand.getId() && db.countWord() != 1);


        if(mode == 1) word.setText(rand.Worden.toLowerCase(Locale.ROOT));
        else if(mode == 2) word.setText(rand.Wordpl.toLowerCase(Locale.ROOT));
    }

    //metoda zamykająca obecny intent
    public void back(View view)
    {
        finish();
    }

    public void showWord(View view) {
        if(show.getText().toString().equals("POKAŻ")) {
            shoWord = true;

            if (mode == 1) answer.setText(rand.Wordpl);
            else if (mode == 2) answer.setText(rand.Worden);

            answer.setEnabled(false);
            show.setText("NASTĘPNE SŁOWO");
        }
        else {
            shoWord = false;

            answer.setEnabled(true);
            show.setText("POKAŻ");
            answer.setText("");
            generateWord();
        }
    }

    //metoda pozwalająca na usunięcie słowa
    public void deleteWord(View view) {
        db.removeWord(rand.getId());

        if(db.countWord() == 0)
        {
            Toast.makeText(FlashCards.this, "Brak fiszek", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            Toast.makeText(FlashCards.this, "Usunięto", Toast.LENGTH_SHORT).show();
            generateWord();
        }
    }
}