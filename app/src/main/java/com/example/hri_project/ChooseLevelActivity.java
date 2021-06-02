package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RawRes;

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


public class ChooseLevelActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {

    private Integer[] chooseAnims;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_choose_level);

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
    }


    @Override
    public void onClick(View v) {

        // Get the button text (level)
        String level = ((Button)v).getText().toString();

        // Start the choose lessons activity
        startChooseLessonIntent(level);
    }


    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        Topic choose_level_topic = TopicBuilder.with(qiContext)      // Create the builder using the qiContext
                                    .withResource(R.raw.choose_level)          // Set the topic resource.
                                    .build();                               // Build the topic

        // Create a new QiChatbot.
        QiChatbot levelChatbot = QiChatbotBuilder.with(qiContext)
                                .withTopic(choose_level_topic)
                                .build();

        // Create a new Chat action.
        Chat levelChat = ChatBuilder.with(qiContext)
                            .withChatbot(levelChatbot)
                            .build();

        // Start the bookmark
        Map<String, Bookmark> bookmarks = choose_level_topic.getBookmarks();
        levelChat.addOnStartedListener(() -> {
            int rnd = new Random().nextInt(chooseAnims.length);
            Integer res = chooseAnims[rnd];
            MainActivity.animateAsync(res, qiContext);
            levelChatbot.goToBookmark(bookmarks.get("choose_level_proposal"), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
        });

        // Add an on started listener to the Chat action.
        levelChatbot.addOnEndedListener(endReason -> {
            String level = endReason;

            // Start the choose lessons activity
            startChooseLessonIntent(level);
        });

        levelChat.async().run();
    }


    private void startChooseLessonIntent(String level) {
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