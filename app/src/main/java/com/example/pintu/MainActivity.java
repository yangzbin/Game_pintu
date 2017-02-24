package com.example.pintu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.pintu.view.GamePintuLayout;

public class MainActivity extends AppCompatActivity {
    private GamePintuLayout gamePintuLayout;
    private TextView mLevel;
    private TextView mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gamePintuLayout = (GamePintuLayout) findViewById(R.id.id_gamepintu);
        gamePintuLayout.setTimeEnabled(true);
        mLevel = (TextView) findViewById(R.id.id_level);
        mTime = (TextView) findViewById(R.id.id_time);
        gamePintuLayout.setOnGamePintuListener(new GamePintuLayout.GamePintuListener() {
            @Override
            public void nextLevel(final int nextLevel) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Game info").setMessage("Level Up!!!")
                        .setPositiveButton("NEXT LEVEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gamePintuLayout.nextLevel();
                                mLevel.setText("Leave"+nextLevel);
                            }
                        }).setCancelable(false).show();
            }

            @Override
            public void timeChanged(int currentTime) {
                mTime.setText(currentTime+"s");
            }

            @Override
            public void gameOver() {
                new AlertDialog.Builder(MainActivity.this).setTitle("Game info").setMessage("Game Over!!!")
                        .setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gamePintuLayout.restart();
                            }
                        }).setNegativeButton("QUIT", new DialogInterface.OnClickListener() {
                            @Override
                             public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                }).setCancelable(false).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePintuLayout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gamePintuLayout.resume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            new AlertDialog.Builder(MainActivity.this).setTitle("Game info").setMessage("Are you sure to exit???")
                    .setPositiveButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).setNegativeButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setCancelable(false).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
