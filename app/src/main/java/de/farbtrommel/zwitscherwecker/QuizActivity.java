package de.farbtrommel.zwitscherwecker;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.Format;
import java.util.Date;
import java.util.Random;

public class QuizActivity extends FragmentActivity implements View.OnClickListener {
    //Delay
    private static final int ACTION_DELAY = 700;

    //Typoe of Run
    public static final String ARG_RUN_FLAG = "ARG_RUN_FLAG";
    public static final int ARG_RUN_FLAG_ALARM = 1;
    public static final int ARG_RUN_FLAG_TRAIN = 2;
    public static final int ARG_RUN_FLAG_ELSE = 3;
    private int mRunFlagValue = ARG_RUN_FLAG_ALARM;
    //Variable Container
    private AlarmSettings mAlarmSettings;

    //Layout
    private LinearLayout[] mBtnBackground;
    private ImageButton[] mBtn;
    private TextView[] mLbl;
    private TextView mLblTime;
    private TextView mLblDate;
    private View mFragmentDetails;

    //Quiz needed stuff
    private MediaPlayer mMediaPlayer;
    private int[] mQuizRandomNumberSet;
    private QuizSet mQuizSet;
    private QuizStats mQuizStats;
    private boolean mQuizIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_quiz);

        mFragmentDetails = (View) findViewById(R.id.quiz_details_fragment);
        mFragmentDetails.setVisibility(View.INVISIBLE);


        mAlarmSettings = new AlarmSettings(PreferenceManager.getDefaultSharedPreferences(this));
        //Setup Media Player
        mMediaPlayer = new MediaPlayer();



        mBtnBackground = new LinearLayout[]{
                (LinearLayout) findViewById(R.id.answer1),
                (LinearLayout) findViewById(R.id.answer2),
                (LinearLayout) findViewById(R.id.answer3),
                (LinearLayout) findViewById(R.id.answer4)
        };
        mBtn = new ImageButton[]{
                (ImageButton) findViewById(R.id.answerImage1),
                (ImageButton) findViewById(R.id.answerImage2),
                (ImageButton) findViewById(R.id.answerImage3),
                (ImageButton) findViewById(R.id.answerImage4)
        };
        mLbl = new TextView[]{
                (TextView) findViewById(R.id.answerLbl1),
                (TextView) findViewById(R.id.answerLbl2),
                (TextView) findViewById(R.id.answerLbl3),
                (TextView) findViewById(R.id.answerLbl4)
        };
        mLblTime = (TextView) findViewById(R.id.txtTime);
        mLblDate = (TextView) findViewById(R.id.txtDate);

        //Set on Click Event Listener
        for (ImageButton btn : mBtn) {
            btn.setOnClickListener(this);
        }

        //Get Intent parameter
        parseIntent();

        try {
            if (!mQuizIsRunning) {
                mQuizSet = new QuizSet(this);
                startGame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(ARG_RUN_FLAG))
            mRunFlagValue = intent.getIntExtra(ARG_RUN_FLAG, ARG_RUN_FLAG_ALARM);
        else
            mRunFlagValue = ARG_RUN_FLAG_ELSE;

    }

    protected void onPause() {
        super.onPause();
        //Attention: During the wake up process the activity get paused!!!
        if (!(mRunFlagValue == ARG_RUN_FLAG_ALARM))
            finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        closeNotification();

        //Clean Media Player shutdown
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();

    }

    private void startGame() {
        try {
            resetControlElements();
            createNotification(mAlarmSettings.getLabel());
            mQuizRandomNumberSet = generateCorrectAnswers(31);

            mQuizStats = new QuizStats(mQuizRandomNumberSet[mQuizRandomNumberSet[4]],
                    new int[]{mQuizRandomNumberSet[0],
                            mQuizRandomNumberSet[1],
                            mQuizRandomNumberSet[2],
                            mQuizRandomNumberSet[3]}, mAlarmSettings.getId()
            );

            setTime(mAlarmSettings.getHour(), mAlarmSettings.getMinute());
            setLabel(mAlarmSettings.getLabel());
            setImages();
            playAlarmSound(mQuizRandomNumberSet[mQuizRandomNumberSet[4]]);
            mQuizIsRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTime(int hour, int time) {
        mLblTime.setText(((hour < 10) ? "0" : "")
                + String.valueOf(hour) + ":" + ((time < 10) ? "0" : "") + String.valueOf(time));
        Format dateFormat = android.text.format
                .DateFormat.getLongDateFormat(getApplicationContext());
        mLblDate.setText(dateFormat.format(new Date()));
    }

    private void setLabel(String label) {
        setAlarmLabel(label);
        for (int i = 0; i < mLbl.length; i++) {
            String str = mQuizSet.get(mQuizRandomNumberSet[i]).mName;
            mLbl[i].setText(str);
        }
    }
    private void resetControlElements() {
        hideBirdDetails();
        for (int i = 0; i < mBtn.length; i++) {
            mBtn[i].setBackgroundColor(getResources().getColor(R.color.quiz_answer_default));
            mBtnBackground[i].setBackgroundColor(0x00000000);
            mLbl[i].setText("");
        }
    }
    private void setAlarmLabel(String str) {
        TextView txt = (TextView) findViewById(R.id.txtLabel);
        txt.setText(str);
        if (str.equals(""))
            txt.setVisibility(View.INVISIBLE);
        else
            txt.setVisibility(View.VISIBLE);
    }
    /**
     * Choose four number in the range from 0 to max.
     * @return [no0, ... , no3, correct_answer] there are no duplicates.
     */
    private int[] generateCorrectAnswers(int maxExclusive) {
        int[] rnd = new int[5];
        Random random = new Random();
        int i = 0;
        while (true) {
            rnd[i] = random.nextInt(maxExclusive);

            //values allow from 1 to maxExclusive
            if (rnd[i] == 0 || isNumberInList(i, rnd))
               continue;

            if (++i == 4) {
                //all four numbers are selected go further
                rnd[i] = random.nextInt(4); //The correct answer
                break;
            }
        }
        return rnd;
    }

    /**
     * Proves if the last added value already contains in the list.
     * @param i the last added value to list
     * @param list list numbers
     * @return true if a number is twice in list otherwise false
     */
    private boolean isNumberInList(int i, int[] list) {
        //No Number should be double in the list
        for (int j = 0; j < i; j++) {
            //oh, number is already there
            if (list[i] == list[j]) {
                //go back and generate new int
                return true;
            }
        }
        return false;
    }

    /**
     * Set all ImageButtons Background.
     */
    private void setImages() throws IOException {
        for (int i = 0; i < 4; i++) {
            setImage(i, mQuizRandomNumberSet[i]);
        }
    }

    /**
     * Set a Image from the asset folder to Image Button Background.
     * @param question Button position in array
     * @param imgId Image Id
     * @throws IOException
     */
    private void setImage(int question, int imgId) throws IOException {
        mBtnBackground[question].setBackgroundResource(
                getResources().getIdentifier("thumbnail_" + imgId, "drawable", getPackageName()));
    }

    /**
     * Play bird call as alarm sound.
     * @param id
     * @throws java.io.IOException
     */
    private void playAlarmSound(int id) throws IOException {
        AssetFileDescriptor afd = null;
        final AudioManager audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);

        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();

        mMediaPlayer.reset();

        afd = getAssets().openFd("sounds/" + String.valueOf(id) + ".mMp3");

        if (mRunFlagValue == ARG_RUN_FLAG_ALARM) {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.prepare();
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
            }
        } else {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepare();
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_information) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mQuizIsRunning) {
            startActivity(new Intent(this, AlarmActivity.class));
            finish();
        } else {
            //Do nothing.
        }
    }

    @Override
    public void onClick(View view) {
        //if quiz over don't allow click events
        if (!mQuizIsRunning || mQuizStats.getClickCount() == 2)
            return;
        mQuizStats.incClickCounter();
        boolean bool = false;
        for (int i = 0; i < mBtn.length; i++) {
            //which button is pressed?
            if (view.getId() == mBtn[i].getId()) {
                if (bool = showIsAnswerRightOrWrong(i)) {
                    quizFinal();
                }

            }
        }
        //Stop Quiz after two wrong tries and start new one
        if (!bool && mQuizStats.getClickCount() == 2) {
            //display all result
            for (int i = 0; i < mBtn.length; i++) {
                showIsAnswerRightOrWrong(i);
            }
            //send quiz data without end time stamp
            mQuizStats.transmit(true);

            //Start a new game with a delay
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startGame();
                }
            }, ACTION_DELAY);

        }

    }

    @SuppressLint("NewApi")
    private boolean showIsAnswerRightOrWrong(int btnId) {
        if (btnId == mQuizRandomNumberSet[4]) {
            //right answer?
            if (sdkOverJellyBean())
                mBtn[btnId].setBackground(getResources().getDrawable(R.drawable.answer_right));
            else
                mBtn[btnId].setBackgroundColor(getResources().getColor(R.color.quiz_answer_right));
            return true;
        //wrong answer
        } else {
            if (sdkOverJellyBean())
                mBtn[btnId].setBackground(getResources().getDrawable(R.drawable.answer_wrong));
            else
                mBtn[btnId].setBackgroundColor(getResources().getColor(R.color.quiz_answer_wrong));
            return false;
        }
    }

    private boolean sdkOverJellyBean() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    private void quizFinal() {
        mRunFlagValue = ARG_RUN_FLAG_ELSE;
        mMediaPlayer.stop();
        mQuizIsRunning = false;
        closeNotification();
        //Send
        mQuizStats.transmit(false);
        //Show Bird Details
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showBirdDetails(mQuizRandomNumberSet[mQuizRandomNumberSet[4]]);
            }
        }, ACTION_DELAY);

    }

    private void createNotification(String label) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(label)
                        .setOngoing(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, QuizActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(pIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void closeNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(0);
    }

    private void hideBirdDetails() {
        mFragmentDetails.setVisibility(View.INVISIBLE);
    }
    private void showBirdDetails(int birdId) {
        // Create fragment and give it an argument specifying the article it should show
        BirdDetailsFragment newFragment = new BirdDetailsFragment();
        Bundle args = new Bundle();
        QuizSet.Bird bird = mQuizSet.get(birdId);
        args.putInt(BirdDetailsFragment.ARG_BIRD_ID, birdId);
        args.putString(BirdDetailsFragment.ARG_BIRD_NAME, bird.mName);
        args.putString(BirdDetailsFragment.ARG_BIRD_WIKI_LINK, bird.mLink);
        args.putString(BirdDetailsFragment.ARG_BIRD_ABS, bird.mAbs);
        args.putString(BirdDetailsFragment.ARG_BIRD_IMG_LINK, bird.mLizenz.mOrigin);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(mFragmentDetails.getId(), newFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        mFragmentDetails.setVisibility(View.VISIBLE);
    }
}
