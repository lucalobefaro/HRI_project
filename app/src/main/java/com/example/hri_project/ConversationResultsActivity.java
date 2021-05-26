package com.example.hri_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.ArrayList;

public class ConversationResultsActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private String level;

    private ArrayList<String> testPassed;
    private boolean passedLevel;
    private int testTaken;

    private int happyFaceImage = R.drawable.happy_face;
    private int sadFaceImage = R.drawable.sad_face;



    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation_results);

        QiSDK.register(this, this);

        level = getIntent().getStringExtra("level");
        Log.i("ConversationResultsActivity", "level "+  level);

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
        if(passedLevel) {
            if(level.equals("HARD")) passedPhrase = new Phrase("Congratulations!");
            else passedPhrase = new Phrase("Congratulations! You are ready for the next level!");

            Intent intent = new Intent(this, ConversationActivity.class);
            switch (level) {
                case "EASY":
                    level = "MEDIUM";
                    break;
                case "MEDIUM":
                    level = "HARD";
                    break;
                case "HARD":
                    level = null;
                    break;
            }

            intent.putExtra("level", level);
            startActivity(intent);

        } else {
            passedPhrase = new Phrase("I'm sorry, you need to practice more");
        }
        Say passedSay = SayBuilder.with(qiContext)
                .withPhrase(passedPhrase)
                .build();
        passedSay.run();
    }


    @Override
    public void onRobotFocusLost() {

    }


    @Override
    public void onRobotFocusRefused(String reason) {

    }
}