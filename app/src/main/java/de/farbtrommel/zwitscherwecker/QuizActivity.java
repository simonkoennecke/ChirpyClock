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
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class QuizActivity extends FragmentActivity implements View.OnClickListener {
    //Delay
    private static int ACTION_DELAY = 700;

    //Typoe of Run
    public static String ARG_RUN_FLAG = "ARG_RUN_FLAG";
    public static int ARG_RUN_FLAG_ALARM = 1;
    public static int ARG_RUN_FLAG_TRAIN = 2;
    public static int ARG_RUN_FLAG_ELSE = 3;
    private int ARG_RUN_FLAG_VALUE = ARG_RUN_FLAG_ALARM;
    //Variable Container
    private AlarmSettings _alarmSettings;

    //Layout
    private LinearLayout[] _btnBackground;
    private ImageButton[] _btn;
    private TextView[] _lbl;
    private TextView _lblTime;
    private TextView _lblDate;
    private View _fragmentDetails;

    //Quiz needed stuff
    private MediaPlayer _mMediaPlayer;
    private int[] _quizRandomNumberSet;
    private QuizSet _quizSet;
    private QuizStats _quizStats;
    private boolean _quizIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_quiz);

        _fragmentDetails = (View) findViewById(R.id.quiz_details_fragment);
        _fragmentDetails.setVisibility(View.INVISIBLE);


                _alarmSettings = new AlarmSettings(PreferenceManager.getDefaultSharedPreferences(this));
        //Setup Media Player
        _mMediaPlayer = new MediaPlayer();



        _btnBackground = new LinearLayout[]{
                (LinearLayout) findViewById(R.id.answer1),
                (LinearLayout) findViewById(R.id.answer2),
                (LinearLayout) findViewById(R.id.answer3),
                (LinearLayout) findViewById(R.id.answer4)
        };
        _btn = new ImageButton[]{
                (ImageButton) findViewById(R.id.answerImage1),
                (ImageButton) findViewById(R.id.answerImage2),
                (ImageButton) findViewById(R.id.answerImage3),
                (ImageButton) findViewById(R.id.answerImage4)
        };
        _lbl = new TextView[]{
                (TextView) findViewById(R.id.answerLbl1),
                (TextView) findViewById(R.id.answerLbl2),
                (TextView) findViewById(R.id.answerLbl3),
                (TextView) findViewById(R.id.answerLbl4)
        };
        _lblTime = (TextView) findViewById(R.id.txtTime);
        _lblDate = (TextView) findViewById(R.id.txtDate);

        //Set on Click Event Listener
        for(ImageButton btn : _btn){
            btn.setOnClickListener(this);
        }

        //Get Intent parameter
        parseIntent();

        try {
            if(!_quizIsRunning) {
                _quizSet = new QuizSet(this);
                startGame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }
/*
    protected void onRestart(){
        super.onRestart();
        parseIntent();
        if(!_quizIsRunning &&
            (ARG_RUN_FLAG_VALUE == ARG_RUN_FLAG_ALARM || ARG_RUN_FLAG_VALUE == ARG_RUN_FLAG_TRAIN)){
               startGame();
        }
    }
*/
    private void parseIntent(){
        Intent intent = getIntent();
        if(intent.hasExtra(ARG_RUN_FLAG))
            ARG_RUN_FLAG_VALUE = intent.getIntExtra(ARG_RUN_FLAG, ARG_RUN_FLAG_ALARM);
        else
            ARG_RUN_FLAG_VALUE = ARG_RUN_FLAG_ELSE;

    }

    protected void onPause(){
        super.onPause();
        //Attention: During the wake up process the activity get paused!!!
        if(!(ARG_RUN_FLAG_VALUE == ARG_RUN_FLAG_ALARM))
            finish();
    }

    protected void onDestroy(){
        super.onDestroy();
        closeNotification();

        //Clean Media Player shutdown
        if(_mMediaPlayer.isPlaying())
            _mMediaPlayer.stop();
        _mMediaPlayer.reset();
        _mMediaPlayer.release();

    }

    private void startGame(){
        try {
            resetControlElements();
            createNotification(_alarmSettings.getLabel());
            _quizRandomNumberSet = generateCorrectAnswers(31);

            _quizStats = new QuizStats(_quizRandomNumberSet[_quizRandomNumberSet[4]],
                    new int[]{_quizRandomNumberSet[0],
                            _quizRandomNumberSet[1],
                            _quizRandomNumberSet[2],
                            _quizRandomNumberSet[3]}, _alarmSettings.getId()
            );

            setTime(_alarmSettings.getHour(), _alarmSettings.getMinute());
            setLabel(_alarmSettings.getLabel());
            setImages();
            playAlarmSound(_quizRandomNumberSet[_quizRandomNumberSet[4]]);
            _quizIsRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTime(int hour, int time){
        _lblTime.setText(((hour < 10)?"0":"")+String.valueOf(hour)+":"+((time<10)?"0":"")+String.valueOf(time));
        Format dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
        _lblDate.setText(dateFormat.format(new Date()));
    }

    private void setLabel(String label){
        setAlarmLabel(label);
        for(int i=0; i < _lbl.length; i++){
            String str = _quizSet.get(_quizRandomNumberSet[i]).name;
            _lbl[i].setText(str);
        }
    }
    private void resetControlElements(){
        hideBirdDetails();
        for(int i=0; i < _btn.length; i++) {
            _btn[i].setBackgroundColor(getResources().getColor(R.color.quiz_answer_default));
            _btnBackground[i].setBackgroundColor(0x00000000);
            _lbl[i].setText("");
        }
    }
    private void setAlarmLabel(String str){
        TextView txt = (TextView) findViewById(R.id.txtLabel);
        txt.setText(str);
        if(str.equals(""))
            txt.setVisibility(View.INVISIBLE);
        else
            txt.setVisibility(View.VISIBLE);
    }
    /**
     * Choose four number in the range from 0 to max
     * @return [no0, ... , no3, correct_answer] there are no duplicates.
     */
    private int[] generateCorrectAnswers(int maxExclusive){
        int[] rnd = new int[5];
        Random random = new Random();
        int i=0;
        while(true){
            rnd[i] = random.nextInt(maxExclusive);

            //values allow from 1 to maxExclusive
            if(rnd[i]==0 || isNumberInList(i, rnd))
               continue;

            if(++i==4){//all four numbers are selected go further
                rnd[i] = random.nextInt(4);//The correct answer
                break;
            }
        }
        return rnd;
    }

    /**
     * Proves if the last added value already contains in the list
     * @param i the last added value to list
     * @param list list numbers
     * @return true if a number is twice in list otherwise false
     */
    private boolean isNumberInList(int i, int[] list){
        //No Number should be double in the list
        for(int j=0; j<i; j++){
            //oh, number is already there
            if(list[i] == list[j]){
                //go back and generate new int
                return true;
            }
        }
        return false;
    }

    /**
     * Set all ImageButtons Background
     */
    private void setImages() throws IOException {
        for(int i=0;i<4;i++){
            setImage(i, _quizRandomNumberSet[i]);
        }
    }

    /**
     * Set a Image from the asset folder to Image Button Background
     * @param question Button position in array
     * @param imgId Image Id
     * @throws IOException
     */
    private void setImage(int question, int imgId) throws IOException {
        _btnBackground[question].setBackgroundResource(getResources().getIdentifier("thumbnail_"+imgId, "drawable", getPackageName()));
    }

    /**
     * Play bird call as alarm sound
     * @param id
     * @throws java.io.IOException
     */
    private void playAlarmSound(int id) throws IOException {
        AssetFileDescriptor afd = null;
        final AudioManager audioManager = (AudioManager)getSystemService(this.AUDIO_SERVICE);

        if(_mMediaPlayer.isPlaying())
            _mMediaPlayer.stop();

        _mMediaPlayer.reset();

        afd = getAssets().openFd("sounds/"+String.valueOf(id)+".mp3");

        if(ARG_RUN_FLAG_VALUE == ARG_RUN_FLAG_ALARM) {
            _mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            _mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            _mMediaPlayer.prepare();
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                _mMediaPlayer.setLooping(true);
                _mMediaPlayer.start();
            }
        }
        else {
            _mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            _mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            _mMediaPlayer.prepare();
            if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
                _mMediaPlayer.setLooping(true);
                _mMediaPlayer.start();

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
        if(!_quizIsRunning) {
            startActivity(new Intent(this, AlarmActivity.class));
            finish();
        }
        else{
            //Do nothing.
        }
    }

    @Override
    public void onClick(View view) {
        //if quiz over don't allow click events
        if(!_quizIsRunning || _quizStats.getClickCount() == 2)
            return;
        _quizStats.incClickCounter();
        boolean bool=false;
        for(int i=0; i < _btn.length; i++){
            //which button is pressed?
            if(view.getId() == _btn[i].getId()){
                if(bool = showIsAnswerRightOrWrong(i)){
                    quizFinal();
                }

            }
        }
        //Stop Quiz after two wrong tries and start new one
        if(!bool && _quizStats.getClickCount() == 2) {
            //display all result
            for (int i = 0; i < _btn.length; i++) {
                showIsAnswerRightOrWrong(i);
            }
            //send quiz data without end time stamp
            _quizStats.transmit(true);

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
    private boolean showIsAnswerRightOrWrong(int btnId){
        if(btnId == _quizRandomNumberSet[4]){
            //right answer?
            if(sdkOverJellyBean())
                _btn[btnId].setBackground(getResources().getDrawable(R.drawable.answer_right));
            else
                _btn[btnId].setBackgroundColor(getResources().getColor(R.color.quiz_answer_right));
            return true;
        }
        //wrong answer
        else{
            if(sdkOverJellyBean())
                _btn[btnId].setBackground(getResources().getDrawable(R.drawable.answer_wrong));
            else
                _btn[btnId].setBackgroundColor(getResources().getColor(R.color.quiz_answer_wrong));
            return false;
        }
    }

    private boolean sdkOverJellyBean(){
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    private void quizFinal() {
        ARG_RUN_FLAG_VALUE = ARG_RUN_FLAG_ELSE;
        _mMediaPlayer.stop();
        _quizIsRunning=false;
        closeNotification();
        //Send
        _quizStats.transmit(false);
        //Show Bird Details
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showBirdDetails(_quizRandomNumberSet[_quizRandomNumberSet[4]]);
            }
        }, ACTION_DELAY);

    }

    private void createNotification(String label){
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

    private void closeNotification(){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(0);
    }

    private void hideBirdDetails(){
        _fragmentDetails.setVisibility(View.INVISIBLE);
    }
    private void showBirdDetails(int birdId){
        // Create fragment and give it an argument specifying the article it should show
        BirdDetailsFragment newFragment = new BirdDetailsFragment();
        Bundle args = new Bundle();
        QuizSet.Bird bird = _quizSet.get(birdId);
        args.putInt(BirdDetailsFragment.ARG_BIRD_ID, birdId);
        args.putString(BirdDetailsFragment.ARG_BIRD_NAME, bird.name);
        args.putString(BirdDetailsFragment.ARG_BIRD_WIKI_LINK, bird.link);
        args.putString(BirdDetailsFragment.ARG_BIRD_ABS, bird.abs);
        args.putString(BirdDetailsFragment.ARG_BIRD_IMG_LINK, bird.lizenz.origin);
        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(_fragmentDetails.getId(), newFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        _fragmentDetails.setVisibility(View.VISIBLE);
    }
}
