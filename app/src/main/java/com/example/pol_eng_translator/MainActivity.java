package com.example.pol_eng_translator;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //uchwyty do elementów
    ImageButton informationButton, changeButton, speakerButton1, speakerButton2, cameraButton, microphoneButton, imageButton, clearButton, copyButton;
    EditText text1, text2;
    TextView langHead1, langHead2, lang1, lang2, countLetter;

    //zmienne sprawdzające czy modele tłumaczeń zostały pobrane (polishEnglish - Pol->Eng, englishPolish - Eng->Pol)
    static boolean polishEnglish = false;
    static boolean englishPolish = false;

    //zmienna sprawdzająca czy aktualnie chcemy tłumaczyć z języka polskiego na angielski
    boolean polishToEnglish = true;

    //modele tłumaczeń (polishEnglishTranslator - Pol->Eng, englishPolishTranslator - Eng->Pol)
    public Translator polishEnglishTranslator;
    public Translator englishPolishTranslator;

    //kody żądań
    private static final String TAG = "MAIN_TAG";
    private Uri imageUri = null;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    //uprawnienia
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;
    private TextRecognizer textRecognizer;

    //modele czytające tekst (ttsPL - po polsku, ttsEN - po angielsku)
    private TextToSpeech ttsPL, ttsEN;

    //łańcuch znaków do przetłumaczenia max 1000 znaków
    private String textToTranslate = "";

    //zmienne pomocnicze służące do uruchamomienia metody tłumaczącej tekst 500ms po zakończeniu przez użytkownika wpisywaniu tekstu
    long delay = 500;
    long last_text_edit = 0;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schowanie domyślego panelu górnego
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //tworzenie uchwytów do elementów i przycisków
        informationButton = (ImageButton) findViewById(R.id.informationButton);
        changeButton = (ImageButton) findViewById(R.id.changeButton);
        speakerButton1 = (ImageButton) findViewById(R.id.speakerButton1);
        speakerButton2 = (ImageButton) findViewById(R.id.speakerButton2);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        microphoneButton = (ImageButton) findViewById(R.id.microphoneButton);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        copyButton = (ImageButton) findViewById(R.id.copyButton);
        clearButton = (ImageButton) findViewById(R.id.clearButton);
        text1 = (EditText) findViewById(R.id.text1);
        text2 = (EditText) findViewById(R.id.text2);
        langHead1 = (TextView) findViewById(R.id.head);
        langHead2 = (TextView) findViewById(R.id.langHead2);
        lang1 = (TextView) findViewById(R.id.lang1);
        lang2 = (TextView) findViewById(R.id.lang2);
        countLetter = (TextView) findViewById(R.id.countLetter);

        text1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){
            }
            @Override
            public void onTextChanged ( final CharSequence s, int start, int before, int count){

                int counter = text1.getText().toString().length();

                if(counter > 1000) {
                    textToTranslate = text1.getText().toString().substring(0, 1000);
                    countLetter.setTextColor(Color.parseColor("#f79292"));
                }
                else {
                    textToTranslate = text1.getText().toString();
                    countLetter.setTextColor(Color.parseColor("#51545d"));
                }

                countLetter.setText(Integer.toString(counter)+" / 1000");

                handler.removeCallbacks(input_finish_checker);

                if(counter > 0) clearButton.setVisibility(View.VISIBLE);
                else clearButton.setVisibility(View.INVISIBLE);

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

        //tworzenie opcji tlumaczeń (options1 - Pol->Eng, option2 - Eng->Pol)
        TranslatorOptions options1 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.POLISH)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build();
        polishEnglishTranslator =
                Translation.getClient(options1);

        TranslatorOptions options2 =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.POLISH)
                        .build();
        englishPolishTranslator =
                Translation.getClient(options2);

        //pobranie modelu danego języka jeżeli nie są pobrane,
        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();

        englishPolishTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                englishPolish = true;
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                englishPolish = false;
                            }
                        });

        polishEnglishTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                polishEnglish = true;
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                polishEnglish = false;
                            }
                        });

        //uprawnienia
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Proszę czekać");
        progressDialog.setCanceledOnTouchOutside(false);

        //model rozpoznający tekst z grafiki
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //inicjalizacja modeli textToSpeach
        ttsPL = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) ttsPL.setLanguage(new Locale("PL-pl"));
            }
        });

        ttsEN = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR) ttsEN.setLanguage(new Locale("EN-us"));
            }
        });

        //utworzenie bazy danych ze słówkami jeżeli nie istnieje i wprowadzenie do niej przykładowych słówek
        Database db = new Database(MainActivity.this);
        Log.i("MOJE", db.countWord() + "");

    }

    //klasa wątku uruhcamiająca się 500ms po zakończaniu przez użytkownika wpraowadzania tekstu
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500)) {
                translate();
            }
        }
    };

    //metoda czytająca tekst wprowadzony przez użytkownika oraz przetłumaczony
    public void readText(View view)
    {
        if (view.getId() == R.id.speakerButton1) {
            if (polishToEnglish) {
                if (ttsEN.isSpeaking()) ttsEN.stop();
                if (ttsPL.isSpeaking()) ttsPL.stop();
                else ttsPL.speak(text1.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
            else {
                if (ttsPL.isSpeaking()) ttsPL.stop();
                if (ttsEN.isSpeaking()) ttsEN.stop();
                else ttsEN.speak(text1.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        } else if (view.getId() == R.id.speakerButton2) {
            if (polishToEnglish) {
                if (ttsPL.isSpeaking()) ttsPL.stop();
                if (ttsEN.isSpeaking()) ttsEN.stop();
                else ttsEN.speak(text2.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
            else {
                if (ttsEN.isSpeaking()) ttsEN.stop();
                if (ttsPL.isSpeaking()) ttsPL.stop();
                else ttsPL.speak(text2.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        }

    }

    //metoda pozwalająca na wprowadzenie tekstu głosowo po angielsku oraz po polsku
    public void voiceInputText(View view)
    {
        ttsEN.stop();
        ttsPL.stop();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if(polishToEnglish) intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "PL-pl");
        else intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "EN-us");

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Powiedz coś...");

        try{
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Wystąpił błąd", Toast.LENGTH_SHORT).show();
        }

    }

    //metoda zamieniająca głosowy dzwięk na tekst
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case REQUEST_CODE_SPEECH_INPUT:
            {
                if(resultCode == RESULT_OK && null != data)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text1.setText(result.get(0));
                    translate();
                }
            } break;
        }
    }

    //metoda tłumacząca tekst z angielskiego na polski i polskiego na angielski
    public void translate()
    {
        if(polishToEnglish)
        {
            polishEnglishTranslator.translate(textToTranslate)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull String translatedText) {
                                    text2.setText(translatedText);
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
        else
        {
            englishPolishTranslator.translate(textToTranslate)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@NonNull String translatedText) {
                                    text2.setText(translatedText);
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
    }

    //metoda pozwalająca na wprowadzenie tekstu ze zrobionego zdjęcia
    public void cameraInput(View view)
    {
        ttsPL.stop();
        ttsEN.stop();

        if(checkCameraPermission())
        {
            pickImageCamera();
            recognizeText();
        }
        else
        {
            requestCameraPermission();
        }
    }

    //metoda pozwalająca na wprowadzenie tekstu z wybranego zdjęcia
    public void galleryInput(View view)
    {
        ttsPL.stop();
        ttsEN.stop();

        if(checkStoragePermission())
        {
            pickImageGallery();
        }
        else
        {
            requestStoragePermission();
        }
    }

    //---------- KAMERA ----------//

    //metoda wyświetlająca okno pozwalające na wykoanie zdjęcia
    private void pickImageCamera()
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Sample text");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Some text");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        //image is taken from Camera
                        //imageUri = result.getData().getData();
                        recognizeText();
                    }
                    else
                    {
                        //canceled
                        Toast.makeText(MainActivity.this, "Anulowano...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    //metoda rozpoznająca tekst ze zdjęcia
    public void recognizeText()
    {
        if(imageUri == null)
        {
            Toast.makeText(MainActivity.this, "Proszę wybrać zdjęcie", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setMessage("Rozpoznawanie tekstu");
            progressDialog.show();

            try {

                InputImage inputImage = InputImage.fromFilePath(this, imageUri);
                progressDialog.setMessage("Rozpoznawanie tekstu");

                Task<Text> textTaskResult = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                progressDialog.dismiss();
                                String recognizedText = text.getText();
                                text1.setText(recognizedText);
                                //translacja
                                translate();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Błąd rozpoznawania tekstu", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
        }
    }

    //---------- GALERIA ----------//

    //metoda wyswietlająca okno wyboru zdjęcia
    private void pickImageGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK)
                    {
                        //wybrano zdjęcie
                        imageUri = result.getData().getData();
                        recognizeText();
                    }
                    else
                    {
                        //anulowano wybór zdjęcia
                        Toast.makeText(MainActivity.this, "Anulowano...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    //--------- UPRAWNIENIA ---------//

    //sprawdzenie czy uprawnienia pamięci zostały nadane
    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    //żądanie uprawnień pamięci od użytkownika
    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    //sprawdzenie czy uprawnienia kamery zostały nadane
    private boolean checkCameraPermission()
    {
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return cameraResult && storageResult;
    }

    //żądanie uprawnień kamery od użytkownika
    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //okno pytające użytkownika o uprawnienia dla pamięci i kamery
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);

        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted)
                    {
                        pickImageCamera();
                    }

                    else
                    {
                        Toast.makeText(this, "Anulowano...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length > 0)
                {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted)
                    {
                        pickImageGallery();
                    }

                    else
                    {
                        Toast.makeText(this, "Anulowano...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    //metoda zamieniająca aktualninie tłumaczony język
    public void changeLangInput(View view)
    {
        ttsPL.stop();
        ttsEN.stop();

        if(polishToEnglish) {
            polishToEnglish = false;
            lang1.setText("Angielski");
            lang2.setText("Polski");
            langHead1.setText("Angielski");
            langHead2.setText("Polski");
        }
        else {
            polishToEnglish = true;
            lang1.setText("Polski");
            lang2.setText("Angielski");
            langHead1.setText("Polski");
            langHead2.setText("Angielski");
        }

        text1.setText(text2.getText().toString());
        translate();
    }

    //metoda czyszcząca pole do wpisywania tekstu do tłumaczenia
    public void clearEditText(View view)
    {
        text1.setText("");
        ttsPL.stop();
        ttsEN.stop();
    }

    //metoda kopiująca przetłumaczony tekst
    public void copyText(View view)
    {
        if(text2.getText().toString().length() > 0) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Skopiowano", text2.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(MainActivity.this, "Skopiowano", Toast.LENGTH_SHORT).show();
        }
    }

    //metoda otwierająca nowy intent
    public void learnActivity(View view)
    {
        Intent intent = new Intent(this, Learn.class);
        startActivity(intent);
    }

    //metoda otwierająca nowy intent
    public void informationActivity(View view)
    {
        Intent intent = new Intent(this, Informations.class);
        startActivity(intent);
    }

}

