package com.example.hri_project;

import androidx.appcompat.app.AppCompatActivity;

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

public class ObjectRecognitionResults extends RobotActivity implements RobotLifecycleCallbacks {

    private int correctAnswers;
    private int nExercises;
    private boolean passed;

    private int happyFaceImage = R.drawable.happy_face;
    private int sadFaceImage = R.drawable.sad_face;



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
        if(passed) {
            passedPhrase = new Phrase("Congratulation, you passed this exercise!");
        } else {
            passedPhrase = new Phrase("I'm sorry, you don't pass the exercise, you need to study a little bit more.");
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