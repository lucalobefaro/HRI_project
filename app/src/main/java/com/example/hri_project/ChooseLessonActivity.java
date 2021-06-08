package com.example.hri_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RawRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.Map;
import java.util.Random;

import com.example.hri_project.MainActivity;


public class ChooseLessonActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {

    private Toolbar toolbar;
    private ActionBar ab;

    private String level;

    private Integer[] chooseAnims;

    private boolean allLessonPassed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_choose_lesson);

        // Get the current level
        Intent myIntent = getIntent();
        level = myIntent.getStringExtra("level");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(R.string.choose_lesson);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

        // Set the button listeners
        findViewById(R.id.vocabulary_button).setOnClickListener(this);
        findViewById(R.id.grammar_button).setOnClickListener(this);
        findViewById(R.id.idioms_button).setOnClickListener(this);

        chooseAnims = new Integer[] {
                R.raw.question_both_hand_a001,
                R.raw.question_both_hand_a002,
                R.raw.question_both_hand_a003,
                R.raw.question_both_hand_a004,
                R.raw.question_both_hand_a005,
                R.raw.question_right_hand_a001
        };

        // Check if all the lesson are passed for the current level
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean vocabularyLessonPassed = sharedPref.getBoolean(level.concat("Vocabularies"), false);
        boolean grammarLessonPassed = sharedPref.getBoolean(level.concat("Grammar"), false);
        boolean setPhrasesLessonPassed = sharedPref.getBoolean(level.concat("SetPhrases"), false);
        if(vocabularyLessonPassed && grammarLessonPassed && setPhrasesLessonPassed) {
            allLessonPassed = true;
        } else {
            allLessonPassed = false;
        }

        Log.i("VOCAB LESSON PASSES", Boolean.toString(vocabularyLessonPassed));
        Log.i("GRAMMAR LESSON PASSES", Boolean.toString(grammarLessonPassed));
        Log.i("PHRASES LESSON PASSES", Boolean.toString(setPhrasesLessonPassed));
    }


    @Override
    public void onClick(View v) {

        // Get the button text (level)
        String lessonType = ((Button)v).getText().toString();

        // Start the choose lessons activity
        startLessonIntent(lessonType);
    }


    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        Topic choose_lesson_topic = TopicBuilder.with(qiContext)      // Create the builder using the qiContext
                                    .withResource(R.raw.choose_lesson)          // Set the topic resource.
                                    .build();                               // Build the topic

        // Create a new QiChatbot.
        QiChatbot lessonChatbot = QiChatbotBuilder.with(qiContext)
                                .withTopic(choose_lesson_topic)
                                .build();

        // Create a new Chat action.
        Chat lessonChat = ChatBuilder.with(qiContext)
                            .withChatbot(lessonChatbot)
                            .build();

        // Start the bookmark
        Map<String, Bookmark> bookmarks = choose_lesson_topic.getBookmarks();
        lessonChat.addOnStartedListener(() -> {
            int rnd = new Random().nextInt(chooseAnims.length);
            Integer res = chooseAnims[rnd];
            MainActivity.animateAsync(res, qiContext);

            // To behave in a different way when all the lessons are passed
            String bookmark;
            if(allLessonPassed){
                bookmark = "choose_lesson_proposal_ready_to_test";
            } else {
                bookmark = "choose_lesson_proposal";
            }
            lessonChatbot.goToBookmark(bookmarks.get(bookmark), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
        });

        // Add an on started listener to the Chat action.
        lessonChatbot.addOnEndedListener(endReason -> {
            String lessonType = endReason;

            // Start the choose lessons activity
            if(lessonType.equals("test")) {
                startTestIntent();
            } else {
                startLessonIntent(lessonType);
            }
        });

        lessonChat.async().run();
    }

    private void startLessonIntent(String lessonType) {

        Intent lessonIntent;

        // Initialize the correct intent
        if(lessonType.equals("vocabulary")) {
            lessonIntent = new Intent(this, LearnVocabularyActivity.class);
        } else if(lessonType.equals("grammar")) {
            lessonIntent = new Intent(this, GrammarLesson.class);
        } else {
            lessonIntent = new Intent(this, SetPhrasesLesson.class);
        }

        // Give the level information to the next intent
        lessonIntent.putExtra("level", level);

        // Start the intent
        startActivity(lessonIntent);
    }


    private void startTestIntent() {
        Intent testIntent = new Intent(this, ObjectRecognitionExercise.class);
        testIntent.putExtra("level", level);
        startActivity(testIntent);
    }


    @Override
    public void onRobotFocusLost() {

    }


    @Override
    public void onRobotFocusRefused(String reason) {

    }
}