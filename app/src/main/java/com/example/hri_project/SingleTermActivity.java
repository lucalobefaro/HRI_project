package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SingleTermActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext myQiContext;

    private Toolbar toolbar;
    private ActionBar ab;

    private String level;

    private ImageView imgView;
    private TextView txtView;
    private TextView subTxtView;
    private Button previousBtn;
    private Button nextBtn;

    private List<Integer> randomInitialization;
    private List<String> wordsList;
    private List<String> translationsList;
    private int imagesList[];

    // ----------------------------------- EASY LEVEL ---------------------------------- //
    private int foodEasyLevel[] = {R.drawable.tomato, R.drawable.meat,
            R.drawable.cake, R.drawable.strawberry, R.drawable.icecream};
    private int animalsEasyLevel[] = {R.drawable.dog, R.drawable.cat,
            R.drawable.giraffe, R.drawable.koala, R.drawable.zebra};
    private int objectsEasyLevel[] = {R.drawable.car, R.drawable.bed,
            R.drawable.umbrella, R.drawable.table,
            R.drawable.house, R.drawable.headphones};

    // --------------------------------- MEDIUM LEVEL --------------------------------- //
    private int foodMediumLevel[] = {R.drawable.blueberry, R.drawable.peach,
            R.drawable.carrot, R.drawable.salad, R.drawable.rice};
    private int animalsMediumLevel[] = {R.drawable.elephant, R.drawable.squirrel,
            R.drawable.curly, R.drawable.kangaroo, R.drawable.lion};
    private int objectsMediumLevel[] = {R.drawable.desk, R.drawable.book,
            R.drawable.display, R.drawable.bottle,
            R.drawable.lamp, R.drawable.gloves};

    // ---------------------------------- HARD LEVEL ---------------------------------- //
    private int foodHardLevel[] = {R.drawable.eggs, R.drawable.flour,
            R.drawable.honey, R.drawable.courgette, R.drawable.aubergine};
    private int animalsHardLevel[] = {R.drawable.dromedary, R.drawable.ladybug,
            R.drawable.meerkat, R.drawable.jellyfish, R.drawable.monkey};
    private int objectsHardLevel[] = {R.drawable.radiator, R.drawable.hook,
            R.drawable.factory, R.drawable.cinecamera,
            R.drawable.wardrobe, R.drawable.stool};

    private int counter = 0;
    private String article;
    private String verb;
    private String[] strPhrase = {"depicted here, that", "show here, that",
                                  "here and", "represented here and"};

    private Future<ListenResult> listenResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_term);

        QiSDK.register(this, this);

        String category = getIntent().getStringExtra("category");
        level = getIntent().getStringExtra("level");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(category);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

        // Otherwise, going back, loses the info of the current level
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleTermActivity.this, LearnVocabularyActivity.class);
                intent.putExtra("level", level);
                startActivity(intent);
            }
        });

        imgView = findViewById(R.id.card_img);
        txtView = findViewById(R.id.card_txt);
        subTxtView = findViewById(R.id.card_subtxt);

        if (level.equals("EASY") && category.equals("FOOD")) {

            wordsList = Arrays.asList("POMODORO", "CARNE", "TORTA", "FRAGOLA", "GELATO");
            translationsList = Arrays.asList("tomato", "meat", "cake", "strawberry", "icecream");
            imagesList = foodEasyLevel;

        } else if (level.equals("EASY") && category.equals("ANIMALS")) {

            wordsList = Arrays.asList("CANE", "GATTO", "GIRAFFA", "KOALA", "ZEBRA");
            translationsList = Arrays.asList("dog", "cat", "giraffe", "koala", "zebra");
            imagesList = animalsEasyLevel;

        } else if (level.equals("EASY") && category.equals("OBJECTS")) {

            wordsList = Arrays.asList("MACCHINA", "LETTO", "OMBRELLO", "TAVOLO", "CASA", "CUFFIE");
            translationsList = Arrays.asList("car", "bed", "umbrella", "table", "house", "headphones");
            imagesList = objectsEasyLevel;

        } else if (level.equals("MEDIUM") && category.equals("FOOD")) {

            wordsList = Arrays.asList("MIRTILLO", "PESCA", "CAROTA", "INSALATA", "RISO");
            translationsList = Arrays.asList("blueberry", "peach", "carrot", "salad", "rice");
            imagesList = foodMediumLevel;

        } else if (level.equals("MEDIUM") && category.equals("ANIMALS")) {

            wordsList = Arrays.asList("ELEFANTE", "SCOIATTOLO", "RICCIO", "CANGURO", "LEONE");
            translationsList = Arrays.asList("elephant", "squirrel", "curly", "kangaroo", "lion");
            imagesList = animalsMediumLevel;

        } else if (level.equals("MEDIUM") && category.equals("OBJECTS")) {

            wordsList = Arrays.asList("SCRIVANIA", "LIBRO", "SCHERMO", "BOTTIGLIA", "LAMPADA", "GUANTI");
            translationsList = Arrays.asList("desk", "book", "display", "bottle", "lamp", "gloves");
            imagesList = objectsMediumLevel;

        } else if (level.equals("HARD") && category.equals("FOOD")) {

            wordsList = Arrays.asList("UOVA", "FARINA", "MIELE", "ZUCCHINA", "MELANZANA");
            translationsList = Arrays.asList("eggs", "flour", "honey", "courgette", "aubergine");
            imagesList = foodHardLevel;

        } else if (level.equals("HARD") && category.equals("ANIMALS")) {

            wordsList = Arrays.asList("DROMEDARIO", "COCCINELLA", "SURICATO", "MEDUSA", "SCIMMIA");
            translationsList = Arrays.asList("dromedary", "ladybug", "meerkat", "jellyfish", "monkey");
            imagesList = animalsHardLevel;

        } else {

            wordsList = Arrays.asList("TERMOSIFONE", "GANCIO", "FABBRICA", "CINEPRESA", "ARMADIO", "SGABELLO");
            translationsList = Arrays.asList("radiator", "hook", "factory", "cinecamera", "wardrobe", "stool");
            imagesList = objectsHardLevel;

        }

        randomInitialization = new ArrayList<Integer>();
        for(int i = 0; i < imagesList.length; i++) {
            randomInitialization.add(i);
        }
        Collections.shuffle(randomInitialization);
        counter = randomInitialization.get(0);

        txtView.setText(wordsList.get(counter));
        subTxtView.setText(translationsList.get(counter));
        imgView.setImageResource(imagesList[counter]);

        previousBtn = findViewById(R.id.previous);;
        nextBtn = findViewById(R.id.next);

    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        myQiContext = qiContext;

        // Say
        int rnd = new Random().nextInt(strPhrase.length);
        composePhrase();
        Phrase phrase = new Phrase(article + " " + subTxtView.getText() + " " + verb + " " +
                strPhrase[rnd] + " " + verb + " " + txtView.getText().toString().toLowerCase()
        );
        Say say = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .build();
        say.run();

        Future<PhraseSet> phraseSetFuture = PhraseSetBuilder.with(qiContext)
                .withTexts("next", "previous", "change")
                .buildAsync();

        phraseSetFuture.andThenConsume(phraseSet -> {
            // Create a new listen action.
            Future<Listen> listenFuture = ListenBuilder.with(qiContext) // Create the builder with the QiContext.
                    .withPhraseSets(phraseSet) // Set the PhraseSets to listen to.
                    .buildAsync(); // Build the listen actions

            listenFuture.andThenConsume(listen -> {
                // Run the listen action and get the result.
                listenResult = listen.async().run();
                listenResult.andThenConsume(result -> {
                    if(result.getHeardPhrase().getText().equals("next")) runOnUiThread( () -> goAhead());
                    else if(result.getHeardPhrase().getText().equals("previous")) runOnUiThread( () -> goBack());
                    else {
                        Intent intent = new Intent(SingleTermActivity.this, LearnVocabularyActivity.class);
                        intent.putExtra("level", level);
                        startActivity(intent);
                    }
                });
            });
        });

        runOnUiThread( () -> updateInterface());

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    public void updateInterface() {

        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { goAhead(); }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { goBack(); }
        });

    }

    private void goAhead() {
        counter += 1;
        if(counter == wordsList.size()) counter = 0;
        txtView.setText(wordsList.get(counter));
        subTxtView.setText(translationsList.get(counter));
        imgView.setImageResource(imagesList[counter]);

        listenResult.requestCancellation();

        int rnd = new Random().nextInt(strPhrase.length);
        composePhrase();
        Phrase phrase = new Phrase(article + " " + subTxtView.getText() + " " + verb + " " +
                strPhrase[rnd] + " " + verb + " " + txtView.getText().toString().toLowerCase()
        );
        Future<Say> nextSay = SayBuilder.with(myQiContext)
                .withPhrase(phrase)
                .buildAsync();
        nextSay.andThenConsume( say -> {

            say.run();

            // Listen
            Future<PhraseSet> phraseSetFuture = PhraseSetBuilder.with(myQiContext)
                    .withTexts("next", "previous", "change")
                    .buildAsync();

            phraseSetFuture.andThenConsume(phraseSet -> {
                // Create a new listen action.
                Future<Listen> listenFuture = ListenBuilder.with(myQiContext) // Create the builder with the QiContext.
                        .withPhraseSets(phraseSet) // Set the PhraseSets to listen to.
                        .buildAsync(); // Build the listen actions

                listenFuture.andThenConsume(listen -> {
                    // Run the listen action and get the result.
                    listenResult = listen.async().run();
                    listenResult.andThenConsume(result -> {
                        if(result.getHeardPhrase().getText().equals("next")) runOnUiThread( () -> goAhead());
                        else if(result.getHeardPhrase().getText().equals("previous")) runOnUiThread( () -> goBack());
                        else {
                            Intent intent = new Intent(SingleTermActivity.this, LearnVocabularyActivity.class);
                            intent.putExtra("level", level);
                            startActivity(intent);
                        }
                    });
                });
            });
        });
    }

    private void goBack() {
        counter -= 1;
        if(counter < 0) counter = wordsList.size() - 1;
        txtView.setText(wordsList.get(counter));
        subTxtView.setText(translationsList.get(counter));
        imgView.setImageResource(imagesList[counter]);

        listenResult.requestCancellation();

        int rnd = new Random().nextInt(strPhrase.length);
        composePhrase();
        Phrase phrase = new Phrase(article + " " + subTxtView.getText() + " " + verb + " " +
                strPhrase[rnd] + " " + verb + " " + txtView.getText().toString().toLowerCase()
        );
        Future<Say> prevSay = SayBuilder.with(myQiContext)
                .withPhrase(phrase)
                .buildAsync();
        prevSay.andThenConsume( say -> {

            say.run();

            // Listen
            Future<PhraseSet> phraseSetFuture = PhraseSetBuilder.with(myQiContext)
                    .withTexts("next", "previous", "change")
                    .buildAsync();

            phraseSetFuture.andThenConsume(phraseSet -> {
                // Create a new listen action.
                Future<Listen> listenFuture = ListenBuilder.with(myQiContext) // Create the builder with the QiContext.
                        .withPhraseSets(phraseSet) // Set the PhraseSets to listen to.
                        .buildAsync(); // Build the listen actions

                listenFuture.andThenConsume(listen -> {
                    // Run the listen action and get the result.
                    listenResult = listen.async().run();
                    listenResult.andThenConsume(result -> {
                        if(result.getHeardPhrase().getText().equals("next")) runOnUiThread( () -> goAhead());
                        else if(result.getHeardPhrase().getText().equals("previous")) runOnUiThread( () -> goBack());
                        else {
                            Intent intent = new Intent(SingleTermActivity.this, LearnVocabularyActivity.class);
                            intent.putExtra("level", level);
                            startActivity(intent);
                        }
                    });
                });
            });
        });
    }

    private void composePhrase() {

        char firstCh = subTxtView.getText().toString().charAt(0);
        int length = subTxtView.getText().toString().length();
        char lastCh = subTxtView.getText().toString().charAt(length-1);
        if(firstCh=='a' || firstCh=='e' || firstCh=='i' || firstCh=='o' || firstCh=='u'){
            article = "an";
            verb = "is";
        }
        else if(subTxtView.getText().equals("meat") || subTxtView.getText().equals("rice") ||
                subTxtView.getText().equals("salad") || subTxtView.getText().equals("flour") ||
                subTxtView.getText().equals("honey")){
            article = "";
            verb = "is";
        }
        else if(lastCh=='s'){
            article = "";
            verb = "are";
        }
        else {
            article = "a";
            verb = "is";
        }
    }

}