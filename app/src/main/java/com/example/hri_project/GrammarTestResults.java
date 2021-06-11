package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.Random;

public class GrammarTestResults extends RobotActivity implements RobotLifecycleCallbacks {

    private int correctAnswers;
    private int nExercises;
    private boolean passed;
    private String level;
    private boolean test;

    private int happyFaceImage = R.drawable.happy_face;
    private int sadFaceImage = R.drawable.sad_face;

    private Integer[] posAnims;
    private Integer[] negAnims;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_recognition_results);
        QiSDK.register(this, this);

        // Get info on the exercise
        Intent myIntent = getIntent();
        nExercises = myIntent.getIntExtra("nExercises", 0);
        correctAnswers = myIntent.getIntExtra("correctAnswers", 0);
        passed = myIntent.getBooleanExtra("passed", false);
        level = myIntent.getStringExtra("level");
        test = myIntent.getBooleanExtra("test", false);

        // Visualize the result
        TextView nExercisesTextView = findViewById(R.id.nExercises);
        nExercisesTextView.setText(String.valueOf(nExercises));
        TextView correctAnswersTextView = findViewById(R.id.correctAnswers);
        correctAnswersTextView.setText(String.valueOf(correctAnswers));

        // Visualize the correct image
        ImageView resultFaceImage = findViewById(R.id.resultFace);
        if(passed) {
            resultFaceImage.setImageResource(happyFaceImage);
        } else {
            resultFaceImage.setImageResource(sadFaceImage);
        }

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
        Phrase resultsPhrase = new Phrase("You answer correctly on ".concat(String.valueOf(correctAnswers))
                                            .concat(" exercises over").concat(String.valueOf(nExercises)));
        Say resultsSay = SayBuilder.with(qiContext)
                            .withPhrase(resultsPhrase)
                            .build();
        resultsSay.run();

        // Say if the exercise is passed
        Phrase passedPhrase;
        Integer[] feedbackAnims;
        if(passed) {
            passedPhrase = new Phrase("Congratulation, you passed this exercise!");
            feedbackAnims = posAnims;
        } else {
            passedPhrase = new Phrase("I'm sorry, you didn't pass the exercise, you need to study a little bit more.");
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


    private void startNextActivity() {

        Intent nextIntent;

        // If the exercise is passed then continue with the next one
        if(passed) {
            nextIntent = new Intent(this, ConversationActivity.class);

        // Otherwise return to the choose lesson activity
        } else {
            nextIntent = new Intent(this, ChooseLessonActivity.class);
        }

        // Give the informations about the level to the next activity
        nextIntent.putExtra("level", level);
        nextIntent.putExtra("test", test);

        // Start the next activity
        startActivity(nextIntent);
    }


    @Override
    public void onRobotFocusLost() {

    }


    @Override
    public void onRobotFocusRefused(String reason) {

    }
}