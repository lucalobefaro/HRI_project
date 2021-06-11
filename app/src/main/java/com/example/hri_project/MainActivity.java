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
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Topic topic;
    private QiContext myQiContext;
    private Chat greetingsChat;
    private QiChatbot qiGreetingsChatbot;
    private Future<Void> fGreetingsChat;
    private Chat chooseWayChat;
    private QiChatbot qiChooseWayChatbot;

    private BookmarkStatus bookmarkStatus;
    private Integer[] helloAnims;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        setContentView(R.layout.activity_main);
        helloAnims = new Integer[] { R.raw.hello_a002, R.raw.salute_right_b001 };

        // DEBUG: TO CLEAR PREFERENCES
        /*
        // Remove all the registered information about lessons passed
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        */
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Save the context
        myQiContext = qiContext;

        // Initialize all the chats
        createGreetingsChat(qiContext);

        // Run the greetings chat
        startGreetingsChat("greeting");

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
        topic = TopicBuilder.with(qiContext)      // Create the builder using the qiContext
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


    private void startGreetingsChat(String bookmarkName) {

        if(bookmarkName.equals("explaination")) {
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

            greetingsChat.addOnStartedListener( () -> {
                Map<String, Bookmark> bookmarks = topic.getBookmarks();
                qiGreetingsChatbot.goToBookmark(bookmarks.get(bookmarkName), AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
            });

        } else {
            qiGreetingsChatbot.addOnEndedListener(endReason -> {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPref.edit();

                // Save the user name as the current user name
                String userName = endReason;
                editor.putString("currentUser", userName);
                editor.commit();

                // Load the set containing all the already known users
                Set<String> savedUsers = sharedPref.getStringSet("savedUsers", new HashSet<String>());

                // Check if we already know this user, load its level and start the correct activity
                if(savedUsers.contains(userName)) {

                    // Get the level
                    String level = sharedPref.getString(userName.concat("_level"), "EASY");

                    // Close the chat
                    fGreetingsChat.requestCancellation();

                    // Deal with "old" users
                    runOnUiThread( () -> dealWithOldUsers(level));
                }

                // Otherwise save the user and start the new chat
                else {

                    // Save this user
                    savedUsers.add(userName);
                    editor.putStringSet("savedUsers", savedUsers);
                    editor.commit();

                    // Close the chat
                    fGreetingsChat.requestCancellation();

                    // Deal with "new" user
                    dealWithNewUsers();
                }
            });
        }

        // Start the chat
        fGreetingsChat = greetingsChat.async().run();
    }


    private void dealWithOldUsers(String level) {
        Log.i("DEBUG: ", "old user");

        // Say something
        Phrase explanationPhrase = new Phrase("I already know you! Well, don't waste our time, " +
                "I've prepared some lesson for you!");
        Future<Say> explanationSay = SayBuilder.with(myQiContext)
                .withPhrase(explanationPhrase)
                .buildAsync();
        explanationSay.andThenConsume( say -> {
            say.run();
            // Start the choose lesson intent
            Intent chooseLessonIntent = new Intent(this, ChooseLessonActivity.class);
            chooseLessonIntent.putExtra("level", level);
            startActivity(chooseLessonIntent);
        });
    }


    private void dealWithNewUsers() {
        Log.i("DEBUG: ", "new user");

        createGreetingsChat(myQiContext);
        startGreetingsChat("explaination");
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