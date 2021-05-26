package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.Map;


public class ChooseLessonActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {

    private Toolbar toolbar;
    private ActionBar ab;

    private String level;


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
            lessonChatbot.goToBookmark(bookmarks.get("choose_lesson_proposal"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
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
            lessonIntent = new Intent(this, LearnVocabularyActivity.class);
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