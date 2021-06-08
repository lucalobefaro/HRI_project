package com.example.hri_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RawRes;

import com.aldebaran.qi.Future;
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
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.Map;
import java.util.Random;


public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Chat greetingsChat;
    private QiChatbot qiGreetingsChatbot;

    private BookmarkStatus bookmarkStatus;
    private Integer[] helloAnims;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        setContentView(R.layout.activity_main);
        helloAnims = new Integer[] { R.raw.hello_a002, R.raw.salute_right_b001 };

        // Remove all the registered information about lessons passed
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
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

        // Remove the listeners on each BookmarkStatus.
        if (bookmarkStatus != null) {
            bookmarkStatus.removeAllOnReachedListeners();
        }
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

        // Get the bookmarks from the topic.
        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        // Get the proposal bookmark.
        Bookmark proposalBookmark = bookmarks.get("greeting");
        bookmarkStatus = qiGreetingsChatbot.bookmarkStatus(proposalBookmark);
        bookmarkStatus.addOnReachedListener(() -> {
            // React when the greeting bookmark is reached.
            int rnd = new Random().nextInt(helloAnims.length);
            Integer res = helloAnims[rnd];
            animateAsync(res, qiContext);
        });
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

    public static void animateAsync(@RawRes Integer animResource, QiContext qiContext) {
        // Create an animation from the animation resource.
        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(animResource)
                .build();

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        // Run the animate action asynchronously.
        animate.async().run();
    }

    public static void animateBuildAsync(@RawRes Integer animResource, QiContext qiContext) {
        // Create an animation from the animation resource.
        Future<Animation> animationFuture = AnimationBuilder.with(qiContext)
                .withResources(animResource)
                .buildAsync();

        animationFuture.andThenConsume(anim -> {
            // Create an animate action.
            Future<Animate> animateFuture = AnimateBuilder.with(qiContext)
                    .withAnimation(anim)
                    .buildAsync();
            animateFuture.andThenConsume(animate -> animate.run());
        });
    }

}