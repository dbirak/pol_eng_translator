package com.example.pol_eng_translator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeleteCards extends AppCompatActivity {

    ListView list;

    //obiekt do naszej bazy danych
    Database db = new Database(DeleteCards.this);

    //pobranie wszystkich słow z bazy
    List<Word_Model> allWords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schowanie domyślego panelu górnego
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_delete_cards);

        list = (ListView) findViewById(R.id.List);

        refresh();

        //wykrycie kliknięcia fiszki, a następnie jej usunięcie
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Word_Model word = (Word_Model) list.getItemAtPosition(i);

                db.removeWord(word.getId());

                if(db.countWord() == 0)
                {
                    Toast.makeText(DeleteCards.this, "Brak fiszek", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(DeleteCards.this, "Usunięto", Toast.LENGTH_SHORT).show();
                    refresh();
                }
            }
        });
    }

    //metoda zamykająca obecny intent
    public void back(View view)
    {
        finish();
    }

    //metoda odświeżająca listę
    public void refresh()
    {
        allWords = db.getAllWords();

        //ArrayAdapter<Word_Model> allWordsAdapter = new ArrayAdapter<Word_Model>(DeleteCards.this, android.R.layout.simple_list_item_1, allWords);
        ArrayAdapter<Word_Model> allWordsAdapter = new ArrayAdapter<Word_Model>(getApplicationContext(), R.layout.text_color_layout, allWords);
        list.setAdapter(allWordsAdapter);
    }
}