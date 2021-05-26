package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;


public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Chat greetingsChat;
    private QiChatbot qiGreetingsChatbot;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Initialize all the chats
        createGreetingsChat(qiContext);

        // Run the greetings chat
        startGreetingsChat();

    }

    @Override
    public void onRobotFocusLost() {
        // Remove on started listeners from the Chat action.
        greetingsChat.removeAllOnStartedListeners();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    private void createGreetingsChat(QiContext qiContext) {
        // The robot focus is gained.
        Topic topic = TopicBuilder.with(qiContext)      // Create the builder using the qiContext
                .withResource(R.raw.greetings)          // Set the topic resource.
                .build();                               // Build the topic

        // Create a new QiChatbot.
        qiGreetingsChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        greetingsChat = ChatBuilder.with(qiContext)
                .withChatbot(qiGreetingsChatbot)
                .build();

        // Add an on started listener to the Chat action.
        greetingsChat.addOnStartedListener(() -> Log.i("TAG", "Discussion started."));
    }


    private void startGreetingsChat() {

        // Stop the chat when the qichatbot is done
        qiGreetingsChatbot.addOnEndedListener(endReason -> {
            if(endReason.equals("test")) {
                // Start test level activity
                Intent testIntent = new Intent(this, ObjectRecognitionExercise.class);
                testIntent.putExtra("level", "EASY");
                testIntent.putExtra("test", true);
                startActivity(testIntent);

            } else {
                // Start choose level activity
                Intent chooseLevelIntent = new Intent(this, ChooseLevelActivity.class);
                startActivity(chooseLevelIntent);
            }
        });

        greetingsChat.async().run();
    }

}