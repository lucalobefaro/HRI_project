package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



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

        // Get the level
        Intent myIntent = getIntent();
        String level = myIntent.getStringExtra("level");

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
        if(correct) {
            interactionPhrase = new Phrase("Great!");
        } else {
            interactionPhrase = new Phrase("Maybe you need to study this word again.");
        }
        Future<Say> interactionSay = SayBuilder.with(myQiContext)
                .withPhrase(interactionPhrase)
                .buildAsync();
        interactionSay.andThenConsume( say -> {
            say.run();
            runOnUiThread( () -> updateInterface() );
        });
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

            // Ask the question to the user
            Phrase interactionPhrase = new Phrase("What is the italian word for "
                                                    .concat(englishWords[currentExerciseIdx]).concat("?"));
            Future<Say> interactionSay = SayBuilder.with(myQiContext)
                    .withPhrase(interactionPhrase)
                    .buildAsync();
            interactionSay.andThenConsume( say -> {
                say.run();
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
            startActivity(objectRecognitionResultsIntent);
        }
    }


    private boolean isPassed() {
        return correctAnswers > (nExercises/2);
    }

}