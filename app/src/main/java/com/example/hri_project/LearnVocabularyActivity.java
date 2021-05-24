package com.example.hri_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import java.util.Map;

public class LearnVocabularyActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnClickListener {

    private Toolbar toolbar;
    private ActionBar ab;

    private String level;

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

        level = getIntent().getStringExtra("level");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(R.string.learn_vocabulary);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

        // Otherwise, going back, loses the info of the current level
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LearnVocabularyActivity.this, ChooseLessonActivity.class);
                intent.putExtra("level", level);
                startActivity(intent);
            }
        });

        findViewById(R.id.food_button).setOnClickListener(this);
        findViewById(R.id.animals_button).setOnClickListener(this);
        findViewById(R.id.objects_button).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // Get the button text (level)
        String category = ((Button)v).getText().toString();

        // Start the chosen category activity
        Intent intent = new Intent(this, SingleTermActivity.class);
        intent.putExtra("category", category);
        intent.putExtra("level", level);
        Log.d("LearnVocabularyActivity", "event: vocabulary's category chosen is " + category + "LEVEL " + level);
        startActivity(intent);
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

        String vocabProposal = null;
        String strFoodBookmark = null, strAnimalBookmark = null, strObjectBookmark = null;

        if(level.equals("EASY")){
            vocabProposal = "easy_vocab_proposal";
            strFoodBookmark = "easy_food_bookmark";
            strAnimalBookmark = "easy_animal_bookmark";
            strObjectBookmark = "easy_object_bookmark";
        } else if(level.equals("MEDIUM")){
            vocabProposal = "medium_vocab_proposal";
            strFoodBookmark = "medium_food_bookmark";
            strAnimalBookmark = "medium_animal_bookmark";
            strObjectBookmark = "medium_object_bookmark";
        } else {
            vocabProposal = "hard_vocab_proposal";
            strFoodBookmark = "hard_food_bookmark";
            strAnimalBookmark = "hard_animal_bookmark";
            strObjectBookmark = "hard_object_bookmark";
        }

        // Get the proposal bookmark and all the others.
        proposalBookmark = bookmarks.get(vocabProposal);
        Bookmark foodBookmark = bookmarks.get(strFoodBookmark);
        Bookmark animalBookmark = bookmarks.get(strAnimalBookmark);
        Bookmark objectBookmark = bookmarks.get(strObjectBookmark);

        // Create a BookmarkStatus for each bookmark.
        foodBookmarkStatus = qiVocabulariesChatbot.bookmarkStatus(foodBookmark);
        animalBookmarkStatus = qiVocabulariesChatbot.bookmarkStatus(animalBookmark);
        objectBookmarkStatus = qiVocabulariesChatbot.bookmarkStatus(objectBookmark);

        Intent intent = new Intent(LearnVocabularyActivity.this, SingleTermActivity.class);
        intent.putExtra("level", level);

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

        // Stop the chat when the qichatbot is done.
        qiVocabulariesChatbot.addOnEndedListener(endReason -> {
            vocabLevel = endReason.equals("vocab");
            Log.i("LearnVocabularyActivity", "Discussion finished " + vocabLevel);
        });

        vocabulariesChat.async().run();
    }

}