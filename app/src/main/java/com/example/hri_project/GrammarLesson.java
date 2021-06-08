package com.example.hri_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

public class GrammarLesson extends RobotActivity implements RobotLifecycleCallbacks {

    private QiContext myQiContext;
    private String level;
    private String[] grammarSentences;
    private String[] grammarExplanations;
    private int nSentences;
    private int currentSentenceIdx;

    // View Elements
    private TextView grammar_sentence_textview;
    private Button continue_button;

    // EASY LEVEL --------------------------------------------------------
    private String[] easyGrammarSentences = {
            "Il caffè e la gallina.",
            "I caffè e le galline",
            "L'italia"
    };
    private String[] easyGrammarExplanations = {
            "These articles are used to indicate the singular names, male and female.",
            "These articles, instead, are used for the plural, male and female.",
            "This article is used for both genres when the word starts with a vocal"
    };

    // MEDIUM LEVEL ------------------------------------------------------
    private String[] mediumGrammarSentences = {
            "Io mangio il pane.",
            "Loro partono per la Germania",
            "Noi saliamo sulla montagna"
    };
    private String[] mediumGrammarExplanations = {
            "In this lesson we'll see the present verbs with some examples. " +
                    "This one means I heat the bread.",
            "This one means They leave for Germany.",
            "This one means We go up the mountain"
    };

    // HARD LEVEL --------------------------------------------------------
    private String[] hardGrammarSentences = {
            "Noi verremmo in campeggio se potessimo",
            "Se ti piacesse studieresti meglio",
            "Se lui le chiedesse di giocare accetterebbe"
    };
    private String[] hardGrammarExplanations = {
            "In this lesson we'll see the conditional verbs with some examples. " +
                    "This one means We would come camping if we could",
            "This one means If you liked it you would study better",
            "This one means If he asked her to play, she would accept"
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_grammar_lesson);

        // Get the view elements
        grammar_sentence_textview = findViewById(R.id.grammar_sentence_textview);
        continue_button = findViewById(R.id.continue_button_2);

        // Get the level
        Intent myIntent = getIntent();
        level = myIntent.getStringExtra("level");

        // Initialize the exercise according to the level
        if(level.equals("HARD")) {
            grammarSentences = hardGrammarSentences;
            grammarExplanations = hardGrammarExplanations;
        } else if(level.equals("MEDIUM")) {
            grammarSentences = mediumGrammarSentences;
            grammarExplanations = mediumGrammarExplanations;;
        } else {
            grammarSentences = easyGrammarSentences;
            grammarExplanations = easyGrammarExplanations;
        }

        // Count the number of sentences
        nSentences = grammarSentences.length;

        // Initialize the first exercise idx
        currentSentenceIdx = 0;

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
        if(currentSentenceIdx < nSentences) {
            explainCurrentSentence();

        // Otherwise go back to the "choose lesson" activity
        } else {
            startChooseLessonIntent();
        }
    }


    private void explainCurrentSentence() {

        // Visualize the sentence on the screen
        grammar_sentence_textview.setText(grammarSentences[currentSentenceIdx]);

        // Explain the visualized sentence
        Phrase explanationPhrase = new Phrase(grammarExplanations[currentSentenceIdx]
                                                .concat("\nIf you want to continue, let me know."));
        Future<Say> explanationSay = SayBuilder.with(myQiContext)
                .withPhrase(explanationPhrase)
                .buildAsync();
        explanationSay.andThenConsume( say -> {
            say.run();
        });

        // Point to the next sentence
        currentSentenceIdx ++;
    }


    private void startChooseLessonIntent() {
        // Set this lesson as passed
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(level.concat("Grammar"), true);
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