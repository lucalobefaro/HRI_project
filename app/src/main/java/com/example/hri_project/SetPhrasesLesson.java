package com.example.hri_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class SetPhrasesLesson extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext myQiContext;
    private String level;
    private String[] setPhrases;
    private String[] setPhrasesExplanations;
    private int nSetPhrases;
    private int currentSetPhraseIdx;

    // View Elements
    private TextView set_phrases_textview;
    private Button continue_button;

    // EASY LEVEL --------------------------------------------------------
    private String[] easySetPhrases = {
            "Ciao mi chiamo Pepper.",
            "Oggi c'è il sole e fa caldo.",
            "Scusi, come posso arrivare al colosseo."
    };
    private String[] easySetPhrasesExplanations = {
            "This sentence is used to present to someone.",
            "This sentence is usually used to talk about weather.",
            "This is the way in which you can ask information about where to find the colosseum."
    };

    // MEDIUM LEVEL ------------------------------------------------------
    private String[] mediumSetPhrases = {
            "Nel tempo libero suono la chitarra.",
            "Quanto costa quel prodotto?",
            "Il mio animale domestico è un cane."
    };
    private String[] mediumSetPhrasesExplanations = {
            "With this sentence we can talk about our hobbies.",
            "Useful to ask the price of something in a market.",
            "This is used to talk about your pet."
    };

    // HARD LEVEL --------------------------------------------------------
    private String[] hardSetPhrases = {
            "La mattina faccio colazione e poi vado a correre.",
            "Qualche anno fa sono stato a Bari, è una città fantastica.",
            "Il mio piatto preferito è riso, patate e cozze."
    };
    private String[] hardSetPhrasesExplanations = {
            "With this sentence you can explain you morning routine.",
            "This can be useful to talk about your journey, especially if you like Puglia.",
            "With this sentence you can talk about your favourite food."
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_set_phrases_lesson);

        // Get the view elements
        set_phrases_textview = findViewById(R.id.set_phrases_textview);
        continue_button = findViewById(R.id.continue_button_3);

        // Get the level
        Intent myIntent = getIntent();
        level = myIntent.getStringExtra("level");

        // Initialize the exercise according to the level
        if(level.equals("HARD")) {
            setPhrases = hardSetPhrases;
            setPhrasesExplanations = hardSetPhrasesExplanations;
        } else if(level.equals("MEDIUM")) {
            setPhrases = mediumSetPhrases;
            setPhrasesExplanations = mediumSetPhrasesExplanations;;
        } else {
            setPhrases = easySetPhrases;
            setPhrasesExplanations = easySetPhrasesExplanations;
        }

        // Count the number of sentences
        nSetPhrases = setPhrases.length;

        // Initialize the first exercise idx
        currentSetPhraseIdx = 0;

        // Add the continue button listener
        continue_button.setOnClickListener( (View v) -> {
            continueDealer();
        });

    }


    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Memorize the context
        myQiContext = qiContext;

        // Start the lesson
        runOnUiThread( () -> continueDealer());
    }


    private void continueDealer() {

        // If there are sentences to explain
        // explain it
        if(currentSetPhraseIdx < nSetPhrases) {
            explainCurrentSentence();

            // Otherwise go back to the "choose lesson" activity
        } else {
            startChooseLessonIntent();
        }
    }


    private void explainCurrentSentence() {

        // Visualize the sentence on the screen
        set_phrases_textview.setText(setPhrases[currentSetPhraseIdx]);

        // Explain the visualized sentence
        Phrase explanationPhrase = new Phrase(setPhrases[currentSetPhraseIdx].concat("\n")
                .concat(setPhrasesExplanations[currentSetPhraseIdx])
                .concat("\nIf you want to continue, let me know."));
        Future<Say> explanationSay = SayBuilder.with(myQiContext)
                .withPhrase(explanationPhrase)
                .buildAsync();
        explanationSay.andThenConsume( say -> {
            say.run();
        });

        // Point to the next sentence
        currentSetPhraseIdx ++;
    }


    private void startChooseLessonIntent() {
        // Set this lesson as passed
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        String userName = sharedPref.getString("currentUser", "");
        editor.putBoolean(userName.concat("_").concat(level).concat("_SetPhrases"), true);
        editor.commit();

        Intent chooseLessonIntent = new Intent(this, ChooseLessonActivity.class);
        chooseLessonIntent.putExtra("level", level);
        startActivity(chooseLessonIntent);
    }


    @Override
    public void onRobotFocusLost() {

    }


    @Override
    public void onRobotFocusRefused(String reason) {

    }
}