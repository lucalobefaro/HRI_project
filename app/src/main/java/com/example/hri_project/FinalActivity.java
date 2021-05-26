package com.example.hri_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class FinalActivity extends RobotActivity implements RobotLifecycleCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_final);

        QiSDK.register(this, this);

        // Add the listener to the "continue" button
        Button continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener( (View v) -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

        // Explain the results
        Phrase resultsPhrase = new Phrase("Congratulations! You have passed all the tests! " +
                "I have nothing more to teach you! You are ready to take the exams!");
        Say resultsSay = SayBuilder.with(qiContext)
                .withPhrase(resultsPhrase)
                .build();
        resultsSay.run();

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}