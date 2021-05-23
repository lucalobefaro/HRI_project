package com.example.hri_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.aldebaran.qi.Future;
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
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LearnVocabularyActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Toolbar toolbar;
    private ActionBar ab;

    private LinearLayout buttonCategories;
    private List<String> categoriesList;
    private String category;

    private Topic topic;
    private Chat vocabulariesChat;
    private QiChatbot qiVocabulariesChatbot;
    private Bookmark proposalBookmark;
    private BookmarkStatus foodBookmarkStatus;
    private BookmarkStatus animalBookmarkStatus;
    private BookmarkStatus objectBookmarkStatus;
    private boolean vocabLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);

        setContentView(R.layout.activity_learn_vocabulary);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(R.string.learnVocabulary);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

        this.buttonCategories = findViewById(R.id.buttonCategoriesGroup);
        categoriesList = Arrays.asList("FOOD", "ANIMALS", "OBJECTS");

        final List<Button> buttons = new ArrayList<>();

        for (final Object i : categoriesList) {
            final Button btn = new Button(this);
            switch (String.valueOf(i)) {
                case "FOOD":
                    btn.setText("FOOD");
                    break;
                case "ANIMALS":
                    btn.setText("ANIMALS");
                    break;
                case "OBJECTS":
                    btn.setText("OBJECTS");
                    break;
            }
            buttons.add(btn);
            LinearLayout.LayoutParams btnparams = new LinearLayout.LayoutParams(250, 140);
            btn.setLayoutParams(btnparams);
            btnparams.setMargins(120, 0, 0, 0);
            btn.setBackgroundColor(Color.LTGRAY);
            btn.setClickable(true);
            buttonCategories.addView(btn);
            buttonCategories.setGravity(Gravity.CENTER_VERTICAL);

            Intent intent = new Intent(LearnVocabularyActivity.this, SingleTermActivity.class);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String categoryName = String.valueOf(i);
                    switch (categoryName) {
                        case "FOOD":
                            category = (String) btn.getText();
                            intent.putExtra("category", category);
                            startActivity(intent);
                            Log.d("LearnVocabularyActivity", "event: vocabulary food chosen");
                            break;
                        case "ANIMALS":
                            category = (String) btn.getText();
                            intent.putExtra("category", category);
                            startActivity(intent);
                            Log.d("LearnVocabularyActivity", "event: vocabulary animals chosen");
                            break;
                        case "OBJECTS":
                            category = (String) btn.getText();
                            intent.putExtra("category", category);
                            startActivity(intent);
                            Log.d("LearnVocabularyActivity", "event: vocabulary objects chosen");
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        super.onDestroy();
        QiSDK.unregister(this, this);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Initialize all the chats
        createVocabulariesChat(qiContext);

        // Reach bookmarks
        manageBookmarks();

        // Run the vocabularies chat
        startVocabulariesChat();

    }

    @Override
    public void onRobotFocusLost() {
        // Remove on started listeners from the Chat action.
        if (vocabulariesChat != null) {
            vocabulariesChat.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    private void sayProposal() {
        qiVocabulariesChatbot.goToBookmark(proposalBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
    }

    private void createVocabulariesChat(QiContext qiContext) {
        // The robot focus is gained.
        topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.vocabularies)
                .build();

        // Create a new QiChatbot.
        qiVocabulariesChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        vocabulariesChat = ChatBuilder.with(qiContext)
                .withChatbot(qiVocabulariesChatbot)
                .build();

        // Add an on started listener to the Chat action.
        vocabulariesChat.addOnStartedListener(this::sayProposal);
        Log.i("LearnVocabularyActivity", "event: Discussion started.");

    }

    private void manageBookmarks() {

        // Get the bookmarks from the topic.
        Map<String, Bookmark> bookmarks = topic.getBookmarks();

        // Get the proposal bookmark and all the others.
        proposalBookmark = bookmarks.get("vocab_proposal");
        Bookmark foodBookmark = bookmarks.get("food_bookmark");
        Bookmark animalBookmark = bookmarks.get("animal_bookmark");
        Bookmark objectBookmark = bookmarks.get("object_bookmark");

        // Create a BookmarkStatus for each bookmark.
        foodBookmarkStatus = qiVocabulariesChatbot.bookmarkStatus(foodBookmark);
        animalBookmarkStatus = qiVocabulariesChatbot.bookmarkStatus(animalBookmark);
        objectBookmarkStatus = qiVocabulariesChatbot.bookmarkStatus(objectBookmark);

        Intent intent = new Intent(LearnVocabularyActivity.this, SingleTermActivity.class);

        // Perform the intent just reached each bookmark.
        foodBookmarkStatus.addOnReachedListener(() -> {
            Log.i("LearnVocabularyActivity", "bookmark " + foodBookmark.getName());
            intent.putExtra("category", "FOOD");
            startActivity(intent);
        });
        animalBookmarkStatus.addOnReachedListener(() -> {
            Log.i("LearnVocabularyActivity", "bookmark " + animalBookmark.getName());
            intent.putExtra("category", "ANIMALS");
            startActivity(intent);
        });
        objectBookmarkStatus.addOnReachedListener(() -> {
            Log.i("LearnVocabularyActivity", "bookmark " + objectBookmark.getName());
            intent.putExtra("category", "OBJECTS");
            startActivity(intent);
        });
    }

    private void startVocabulariesChat() {
        Future<Void> chatFuture = vocabulariesChat.async().run();

        // Add a Lambda to the action execution.
        chatFuture.thenConsume(future -> {
            if (future.hasError()) {
                Log.e("LearnVocabularyActivity", "event:Discussion finished with error.", future.getError());
            }
        });

        // Stop the chat when the qichatbot is done.
        qiVocabulariesChatbot.addOnEndedListener(endReason -> {
            vocabLevel = endReason.equals("vocab");
            Log.i("LearnVocabularyActivity", "Discussion finished " + vocabLevel);
        });
    }

}