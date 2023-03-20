package com.example.pol_eng_translator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Learn extends AppCompatActivity {

    Button en2plButton, pl2enButton;
    ImageButton addButton, exploreButton, deleteButton;
    TextView countWord;

    //obiekt do naszej bazy danych
    Database db = new Database(Learn.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schowanie domyślego panelu górnego
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_learn);

        //uchwyty do elementów
        en2plButton = (Button) findViewById(R.id.en2plButton);
        pl2enButton = (Button) findViewById(R.id.pl2enButton);
        addButton = (ImageButton) findViewById(R.id.addButton);
        exploreButton = (ImageButton) findViewById(R.id.exploreButton);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        countWord = (TextView) findViewById(R.id.countWord);

        countWord.setText(db.countWord() + "");

    }

    @Override
    protected void onResume() {
        super.onResume();

        //zwrócenie aktualnej listy słówek z bazy
        Database db = new Database(Learn.this);
        countWord.setText(db.countWord() + "");
    }

    //metoda otwierająca nowy intent
    public void addWord(View view) {
        Intent intent = new Intent(this, AddCard.class);
        startActivity(intent);
    }

    //metoda otwierająca nowy intent
    public void exploreWord(View view) {
        if(db.countWord() == 0)
        {
            Toast.makeText(Learn.this, "Brak fiszek", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ExploreCards.class);
        startActivity(intent);
    }

    //metoda otwierająca nowy intent
    public void deleteWord(View view) {
        if(db.countWord() == 0)
        {
            Toast.makeText(Learn.this, "Brak fiszek", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DeleteCards.class);
        startActivity(intent);
    }

    //metoda otwierająca nowy intent wraz z odpowiednim trybem
    public void learnMode(View view) {
        if(db.countWord() == 0)
        {
            Toast.makeText(Learn.this, "Brak fiszek", Toast.LENGTH_SHORT).show();
            return;
        }

        FlashCards l = new FlashCards();

        if (view.getId() == R.id.en2plButton) {
            l.setMode(1);

        } else if (view.getId() == R.id.pl2enButton) {
            l.setMode(2);
        }

        Intent intent = new Intent(this, FlashCards.class);
        startActivity(intent);
    }

    //metoda otwierająca nowy intent
    public void informationActivity(View view)
    {
        Intent intent = new Intent(this, Informations.class);
        startActivity(intent);
    }

    //metoda zamykająca obecny intent
    public void translateActivity(View view)
    {
        finish();
    }
}