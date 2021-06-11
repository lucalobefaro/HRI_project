package com.example.hri_project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/*
// How to use:
Intent intent = new Intent(this, ObjectRecognitionExercise.class);
intent.putExtra("level", "HARD");
startActivity(intent);
 */
public class ObjectRecognitionExercise extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {

    // View Elements
    private QiContext myQiContext;
    private ImageView imageView;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    // Resources
    private List<Integer> randomAccessArray;
    private int currentExercise;
    private int nExercises;
    private int correctAnswers;
    private int[] images;
    private String[][] answers;
    private String[] englishWords;      // The english word of the correct answer

    // EASY LEVEL --------------------------------------------------------
    private int[] imagesEasyLevel = {R.drawable.umbrella,
                                     R.drawable.table,
                                     R.drawable.cake,
                                     R.drawable.house,
                                     R.drawable.headphones};
    private String[][] answersEasyLevel = {
            {"ombrello", "cinepresa", "bottiglia", "bastone"},
            {"tavolo", "casa", "tastiera", "busta"},
            {"torta", "piatto", "casa", "bottiglia"},
            {"casa", "villa", "pianoforte", "termosifone"},
            {"cuffie", "mouse", "schermo", "telecamera"}
    };
    private String[] englishWordsEasyLevel = {"umbrella",
                                              "table",
                                              "cake",
                                              "house",
                                              "headphones"};

    // MEDIUM LEVEL ------------------------------------------------------
    private int[] imagesMediumLevel = {R.drawable.desk,
                                       R.drawable.elephant,
                                       R.drawable.book,
                                       R.drawable.display,
                                       R.drawable.bottle};
    private String[][] answersMediumLevel = {
            {"scrivania", "tavolo", "sedia", "schermo"},
            {"elefante", "mucca", "giraffa", "dromedario"},
            {"libro", "quaderno", "telefono", "rivista"},
            {"schermo", "telefono", "televisione", "bottiglia"},
            {"bottiglia", "vaso", "bicchiere", "lavandino"}
    };
    private String[] englishWordsMediumLevel = {"desk",
                                                "elephant",
                                                "book",
                                                "display",
                                                "bottle"};

    // HARD LEVEL --------------------------------------------------------
    private int[] imagesHardLevel = {R.drawable.radiator,
                                     R.drawable.hook,
                                     R.drawable.factory,
                                     R.drawable.dromedary,
                                     R.drawable.cinecamera};
    private String[][] answersHardLevel = {
            {"termosifone", "cinepresa", "forno", "stufetta"},
            {"gancio", "presa", "attaccapanni", "lampada"},
            {"fabbrica", "fattoria", "televisione", "cinepresa"},
            {"dromedario", "cammello", "giraffa", "zebra"},
            {"cinepresa", "camera", "coinquilino", "cinema"}
    };
    private String[] englishWordsHardLevel = {"radiator",
                                              "hook",
                                              "factory",
                                              "dromedary",
                                              "cinecamera"};

    private String level;
    private boolean test;

    private Integer[] affirmAnims;
    private Integer[] posAnims;
    private Integer[] negAnims;

    private final int buttonBackgroundColor = 0xFF464141;
    private final Locale itLocale = new Locale(Language.ITALIAN, Region.ITALY);
    private Future<ListenResult> listenResultFuture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_object_recognition_exercise);

        // Get the view elements
        imageView = findViewById(R.id.objectImage);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        button1.setBackgroundColor(buttonBackgroundColor);
        button2.setBackgroundColor(buttonBackgroundColor);
        button3.setBackgroundColor(buttonBackgroundColor);
        button4.setBackgroundColor(buttonBackgroundColor);

        // Get the level
        Intent myIntent = getIntent();
        level = myIntent.getStringExtra("level");
        test = myIntent.getBooleanExtra("test", false);

        // Initialize the exercise according to the level
        if(level.equals("HARD")) {
            images = imagesHardLevel;
            answers = answersHardLevel;
            englishWords = englishWordsHardLevel;
        } else if(level.equals("MEDIUM")) {
            images = imagesMediumLevel;
            answers = answersMediumLevel;
            englishWords = englishWordsMediumLevel;
        } else {
            images = imagesEasyLevel;
            answers = answersEasyLevel;
            englishWords = englishWordsEasyLevel;
        }

        // Initialize useful parameters
        randomAccessArray = new ArrayList<Integer>();
        correctAnswers = 0;

        // Count how many exercise we have
        nExercises = images.length;

        // Randomize the exercises
        for(int i=0; i<nExercises; i++) {
            randomAccessArray.add(i);
        }
        Collections.shuffle(randomAccessArray);
        currentExercise = -1;


        affirmAnims = new Integer[] {
                R.raw.affirmation_a001,
                R.raw.affirmation_a002,
                R.raw.affirmation_a003,
                R.raw.affirmation_a004,
                R.raw.affirmation_a005,
                R.raw.affirmation_a006,
                R.raw.affirmation_a007,
                R.raw.affirmation_a008,
                R.raw.affirmation_a009,
                R.raw.affirmation_a010,
                R.raw.affirmation_a011
        };
        posAnims = new Integer[] {
                R.raw.nicereaction_a001,
                R.raw.nicereaction_a002
        };
        negAnims = new Integer[] {
                R.raw.negation_both_hands_a001,
                R.raw.negation_both_hands_a003,
                R.raw.negation_both_hands_a004,
                R.raw.negation_both_hands_a005
        };
    }


    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }


    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Memorize the context
        myQiContext = qiContext;

        // Set the button listeners
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);

        // Start interaction
        int rnd = new Random().nextInt(affirmAnims.length);
        Integer res = affirmAnims[rnd];
        MainActivity.animateAsync(res, qiContext);
        Phrase interactionPhrase = new Phrase("Let's start!");
        Say interactionSay = SayBuilder.with(qiContext)
                .withPhrase(interactionPhrase)
                .build();
        interactionSay.run();

        // Initialize the first exercise view
        runOnUiThread( () -> updateInterface() );
    }


    @Override
    public void onRobotFocusLost() {

    }


    @Override
    public void onRobotFocusRefused(String reason) {

    }


    @Override
    public void onClick(View v) {

        if (listenResultFuture != null && !listenResultFuture.isCancelled()) listenResultFuture.requestCancellation();

        // Get the correct answer
        int currentExerciseIdx = randomAccessArray.get(currentExercise);
        String correctAnswer = answers[currentExerciseIdx][0];

        // Get the text of the pressed button
        String userAnswer = ((Button)v).getText().toString();

        // Check if the answer is correct and update it in case
        boolean correct = userAnswer.equals(correctAnswer);
        if(correct) {
            correctAnswers ++;
        }

        // Interact with the user
        Phrase interactionPhrase;
        Integer[] feedbackAnims;
        Integer newColor;
        if(correct) {
            interactionPhrase = new Phrase("Great!");
            feedbackAnims = posAnims;
            newColor = Color.GREEN;
        } else {
            interactionPhrase = new Phrase("Maybe you need to study this word again.");
            feedbackAnims = negAnims;
            newColor = Color.RED;
        }
        ((Button) v).setBackgroundColor(newColor);
        Future<Say> interactionSay = SayBuilder.with(myQiContext)
                .withPhrase(interactionPhrase)
                .buildAsync();
        interactionSay.andThenConsume( say -> {
            say.run();
            runOnUiThread( () -> updateInterface() );
        });
        int rnd = new Random().nextInt(feedbackAnims.length);
        Integer res = feedbackAnims[rnd];
        MainActivity.animateBuildAsync(res, myQiContext);
    }


    private void updateInterface() {

        Log.i("TAG", "updating interface...");

        // Increase the exercise number
        currentExercise++;

        if(currentExercise < nExercises) {

            // Get a random exercise
            int currentExerciseIdx = randomAccessArray.get(currentExercise);

            // Get the image of the current exercise
            int currentImage = images[currentExerciseIdx];

            // Randomize the answers
            List<String> currentExerciseAnswers = Arrays.asList(answers[currentExerciseIdx].clone());
            Collections.shuffle(currentExerciseAnswers);

            // Update the interface
            imageView.setImageResource(currentImage);
            button1.setText(currentExerciseAnswers.get(0));
            button2.setText(currentExerciseAnswers.get(1));
            button3.setText(currentExerciseAnswers.get(2));
            button4.setText(currentExerciseAnswers.get(3));

            button1.setBackgroundColor(buttonBackgroundColor);
            button2.setBackgroundColor(buttonBackgroundColor);
            button3.setBackgroundColor(buttonBackgroundColor);
            button4.setBackgroundColor(buttonBackgroundColor);

            if (listenResultFuture != null) Log.d("TAG", "is cancelled:" + listenResultFuture.isCancelled());

            // Ask the question to the user
            Phrase interactionPhrase = new Phrase("What is the italian word for "
                                                    .concat(englishWords[currentExerciseIdx]).concat("?"));
            Future<Say> interactionSay = SayBuilder.with(myQiContext)
                    .withPhrase(interactionPhrase)
                    .buildAsync();
            interactionSay.andThenConsume( say -> {

                say.run();

                // Listen for the answer
                String ans1 = currentExerciseAnswers.get(0);
                String ans2 = currentExerciseAnswers.get(1);
                String ans3 = currentExerciseAnswers.get(2);
                String ans4 = currentExerciseAnswers.get(3);

                Future<PhraseSet> phraseSetFuture = PhraseSetBuilder.with(myQiContext) // Create the builder using the QiContext.
                        .withTexts(ans1, ans2, ans3, ans4) // Add the phrases Pepper will listen to.
                        .buildAsync(); // Build the PhraseSet.

                phraseSetFuture.andThenConsume(phraseSet -> {

                    // Create a new listen action.
                    Future<Listen> listenFuture = ListenBuilder.with(myQiContext) // Create the builder with the QiContext.
                            .withPhraseSets(phraseSet) // Set the PhraseSets to listen to.
                            .withLocale(itLocale)
                            .buildAsync(); // Build the listen actions

                    listenFuture.andThenConsume(listen -> {
                        // Run the listen action and get the result.
                        listenResultFuture = listen.async().run();

                        listenResultFuture.andThenConsume(listenResult -> {
                            String ans = listenResult.getHeardPhrase().getText();
                            ans = ans.replaceAll("\\s+","");
                            Log.i("TAG", "student answered verbally: " + ans);
                            Button ansBtn = null;
                            if (ans.equals(ans1)) ansBtn = button1;
                            else if (ans.equals(ans2)) ansBtn = button2;
                            else if (ans.equals(ans3)) ansBtn = button3;
                            else if (ans.equals(ans4)) ansBtn = button4;

                            Button finalAnsBtn = ansBtn;
                            runOnUiThread(() -> onClick(finalAnsBtn));
                        });
                    });
                });

            });
        } else {
            // Say that the exercise is ended
            Phrase interactionPhrase = new Phrase("Ok the exercise is finished!");
            Future<Say> interactionSay = SayBuilder.with(myQiContext)
                    .withPhrase(interactionPhrase)
                    .buildAsync();
            interactionSay.andThenConsume( say -> {
                say.run();
            });

            // Visualize the results page
            Intent objectRecognitionResultsIntent = new Intent(this, ObjectRecognitionResults.class);
            objectRecognitionResultsIntent.putExtra("nExercises", nExercises);
            objectRecognitionResultsIntent.putExtra("correctAnswers", correctAnswers);
            objectRecognitionResultsIntent.putExtra("passed", isPassed());
            objectRecognitionResultsIntent.putExtra("level", level);
            objectRecognitionResultsIntent.putExtra("test", test);
            startActivity(objectRecognitionResultsIntent);
        }
    }


    private boolean isPassed() {
        return correctAnswers > (nExercises/2);
    }

}