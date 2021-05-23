package com.example.hri_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;

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
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConversationActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private Toolbar toolbar;
    private ActionBar ab;

    private Topic topic;
    private Chat conversationChat;
    private QiChatbot qiConversationChatbot;
    private Bookmark proposalBookmark;

    private boolean convLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);

        setContentView(R.layout.activity_conversation);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(R.string.conversation);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

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
        Map<String, Bookmark> bookmarks = topic.getBookmarks();

        // Randomly choose the proposal to start from.
        List<String> proposal = Arrays.asList("presentation_proposal", "directions_proposal", "shop_proposal");
        Random rand = new Random();
        proposalBookmark = bookmarks.get(proposal.get(rand.nextInt(proposal.size())));
    }

    private void startVocabulariesChat() {

        // Stop the chat when the qichatbot is done
        qiConversationChatbot.addOnEndedListener(endReason -> {
            convLevel = endReason.equals("conv");
            Log.i("ConversationActivity", "" + convLevel);

        });

        conversationChat.async().run();
    }
}