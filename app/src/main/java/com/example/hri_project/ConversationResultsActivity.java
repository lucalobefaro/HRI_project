package com.example.hri_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.ArrayList;
import java.util.Random;

public class ConversationResultsActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private String level;
    private boolean test;

    private ArrayList<String> testPassed;
    private boolean passedLevel;
    private int testTaken;

    private int happyFaceImage = R.drawable.happy_face;
    private int sadFaceImage = R.drawable.sad_face;

    private Integer[] posAnims;
    private Integer[] negAnims;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation_results);

        QiSDK.register(this, this);

        level = getIntent().getStringExtra("level");
        Log.i("ConversationResultsActivity", "level "+  level);
        test = getIntent().getBooleanExtra("test", false);

        testTaken = 3;
        testPassed = getIntent().getStringArrayListExtra("testPassed");

        TextView nExercisesTextView = findViewById(R.id.totalTest);
        nExercisesTextView.setText(String.valueOf(testTaken));
        TextView correctAnswersTextView = findViewById(R.id.correctTest);
        correctAnswersTextView.setText(String.valueOf(testPassed.size()));

        if(testPassed.size() >= 2) passedLevel = true;
        else passedLevel = false;

        ImageView resultFaceImage = findViewById(R.id.resultFace);
        if(passedLevel) resultFaceImage.setImageResource(happyFaceImage);
        else resultFaceImage.setImageResource(sadFaceImage);

        // Add the listener to the "continue" button
        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener( (View v) -> {
            startNextActivity();
        });

        posAnims = new Integer[] {
                R.raw.nicereaction_a001,
                R.raw.nicereaction_a002
        };
        negAnims = new Integer[] {
                R.raw.negation_both_hands_a001,
                R.raw.negation_both_hands_a003,
                R.raw.negation_both_hands_a004,
                R.raw.negation_both_hands_a005
        };

    }


    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
    }


    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Explain the results
        Phrase resultsPhrase = new Phrase("You answer correctly on ".concat(String.valueOf(testPassed.size()))
                .concat(" tests over ").concat(String.valueOf(testTaken)));
        Say resultsSay = SayBuilder.with(qiContext)
                .withPhrase(resultsPhrase)
                .build();
        resultsSay.run();


        // Say if the exercise is passed
        Phrase passedPhrase;

        Integer[] feedbackAnims;
        if(passedLevel) {
            if(test && !level.equals("HARD")) passedPhrase = new Phrase("Great, I underestimated you. " +
                    "I need to test you again, but this time will be a little more difficult!");
            else if(level.equals("HARD")) passedPhrase = new Phrase("Congratulations!");
            else passedPhrase = new Phrase("Congratulations! It's time to offer you new, more challenging lessons!");
            feedbackAnims = posAnims;
        } else {
            if(test) passedPhrase = new Phrase("But don't worry, now I know your Italian level, " +
                    "it's time to learn something new!  Let's do some lesson.");
            else passedPhrase = new Phrase("I'm sorry, you need to practice more");
            feedbackAnims = negAnims;
        }

        int rnd = new Random().nextInt(feedbackAnims.length);
        Integer res = feedbackAnims[rnd];
        MainActivity.animateAsync(res, qiContext);

        Say passedSay = SayBuilder.with(qiContext)
                .withPhrase(passedPhrase)
                .build();
        passedSay.run();

        // Wait for a signal to continue
        Topic continueTopic = TopicBuilder.with(qiContext)
                .withResource(R.raw.continue_signal)
                .build();

        QiChatbot qiContinueChatbot = QiChatbotBuilder.with(qiContext)
                .withTopic(continueTopic)
                .build();

        Chat continueChat = ChatBuilder.with(qiContext)
                .withChatbot(qiContinueChatbot)
                .build();

        qiContinueChatbot.addOnEndedListener(endReason -> {
            startNextActivity();
        });

        continueChat.async().run();
    }


    @SuppressLint("LongLogTag")
    private void startNextActivity() {

        Intent intent;

        if(passedLevel) {
            if(test) {
                if(level.equals("HARD")) {
                    intent = new Intent(this, FinalActivity.class);
                } else {
                    intent = new Intent(this, ObjectRecognitionExercise.class);

                    // I upgrade the level to the following
                    switch (level) {
                        case "EASY":
                            level = "MEDIUM";
                            Log.i("ConversationResultsActivity", "GO TO level "+ level);
                            break;
                        case "MEDIUM":
                            level = "HARD";
                            Log.i("ConversationResultsActivity", "GO TO level "+ level);
                            break;
                    }
                }
            } else { // If I have chosen my level
                if(level.equals("HARD")) {
                    intent = new Intent(this, FinalActivity.class);
                } else {
                    intent = new Intent(this, ChooseLessonActivity.class);

                    // I upgrade the level to the following
                    switch (level) {
                        case "EASY":
                            level = "MEDIUM";
                            Log.i("ConversationResultsActivity", "GO TO level "+ level);
                            break;
                        case "MEDIUM":
                            level = "HARD";
                            Log.i("ConversationResultsActivity", "GO TO level "+ level);
                            break;
                    }
                }
            }
        } else { // If I have not passed the level
            intent = new Intent(this, ChooseLessonActivity.class);
        }
        intent.putExtra("level", level);
        intent.putExtra("test", test);
        startActivity(intent);
    }


    @Override
    public void onRobotFocusLost() {

    }


    @Override
    public void onRobotFocusRefused(String reason) {

    }
}