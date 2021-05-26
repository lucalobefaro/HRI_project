package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
import java.util.Map;

public class ConversationActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private String level;
    private String proposalOne;
    private String proposalTwo;
    private String proposalThree;


    private Topic topic;
    private Chat conversationChat;
    private QiChatbot qiConversationChatbot;
    private Bookmark proposalBookmark;
    Map<String, Bookmark> bookmarks;

    private int errors;
    private boolean passed;
    private ArrayList<String> testPassed;
    private int testTaken;

    private boolean convLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);

        setContentView(R.layout.activity_conversation);

        level = getIntent().getStringExtra("level");
        Log.i("ConversationActivity", "level "+ level);

        errors = 0;
        passed = true;
        testTaken = 1;
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
        if (conversationChat != null) {
            conversationChat.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }

    private void sayProposal() {
        qiConversationChatbot.goToBookmark(proposalBookmark, AutonomousReactionImportance.HIGH, AutonomousReactionValidity.IMMEDIATE);
    }

    private void createVocabulariesChat(QiContext qiContext) {
        // The robot focus is gained.
        topic = TopicBuilder.with(qiContext)
                .withResource(R.raw.conversation)
                .build();

        // Create a new QiChatbot.
        qiConversationChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(topic)
                .build();

        // Create a new Chat action.
        conversationChat = ChatBuilder.with(qiContext)
                .withChatbot(qiConversationChatbot)
                .build();

        // Add an on started listener to the Chat action.
        conversationChat.addOnStartedListener(this::sayProposal);
        Log.i("ConversationActivity", "event: Discussion started.");

    }

    private void manageBookmarks() {

        // Get the bookmarks from the topic.
        bookmarks = topic.getBookmarks();

        switch (level) {
            case "EASY":
                proposalOne = "presentation_proposal";
                proposalTwo = "weather_proposal";
                proposalThree = "directions_proposal";
                break;
            case "MEDIUM":
                proposalOne = "hobby_proposal";
                proposalTwo = "shop_proposal";
                proposalThree = "animals_proposal";
                break;
            case "HARD":
                proposalOne = "typical_day_proposal";
                proposalTwo = "travel_proposal";
                proposalThree = "dish_proposal";
                break;
        }

        // Get the bookmarks of the first proposal.
        proposalBookmark = bookmarks.get(proposalOne);

        // To manage other proposal
        Bookmark newProposalBookmark = bookmarks.get(proposalTwo);
        BookmarkStatus newProposalBookmarkStatus = qiConversationChatbot.bookmarkStatus(newProposalBookmark);
        newProposalBookmarkStatus.addOnReachedListener(() -> {
            if (passed) testPassed.add(String.valueOf(passed));
            testTaken += 1;
            errors = 0;
            passed = true;
        });

        Bookmark lastProposalBookmark = bookmarks.get(proposalThree);
        BookmarkStatus lastProposalBookmarkStatus = qiConversationChatbot.bookmarkStatus(lastProposalBookmark);
        lastProposalBookmarkStatus.addOnReachedListener(() -> {
            if (passed) testPassed.add(String.valueOf(passed));
            testTaken += 1;
            errors = 0;
            passed = true;
        });

        // To manage errors
        testPassed = new ArrayList<>();
        Bookmark errorBookmark = bookmarks.get("error");
        BookmarkStatus errorBookmarkStatus = qiConversationChatbot.bookmarkStatus(errorBookmark);
        errorBookmarkStatus.addOnReachedListener(() -> {
            errors += 1;
            if (errors >= 3) passed = false;
            Log.i("ConversationActivity", "errors "+  errors);
            Log.i("ConversationActivity", "passed "+  passed);
            Log.i("ConversationActivity", "test #"+  testTaken);

        });
    }

    private void startVocabulariesChat() {


        // Stop the chat when the qichatbot is done
        qiConversationChatbot.addOnEndedListener(endReason -> {
            convLevel = endReason.equals("conv");
            if (passed) testPassed.add(String.valueOf(passed));

            Log.i("ConversationActivity", "correctTest #"+  testPassed.size());

            Intent intent = new Intent(this, ConversationResultsActivity.class);
            intent.putStringArrayListExtra("testPassed", testPassed);
            intent.putExtra("level", level);
            startActivity(intent);

        });

        conversationChat.async().run();
    }
}