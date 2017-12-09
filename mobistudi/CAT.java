package cat.mobistudi;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import chinaykc.mobistudi.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import xyz.kfdykme.mobistudi.eventbus.TheatFromCat;
import xyz.kfdykme.mobistudi.eventbus.UserTheatInfo;

/**
 * ˮƽ����
 * @author Shiqi wang
 */

public class CAT extends AppCompatActivity {

    private int hasLearned = 0;
    private double difficulty;
    private Button nextQuestion;
    private NumberProgressBar progress1;
    private RadioGroup radioGroupAnswer;
    private String correctAnswer = "";
    private RadioButton radioButtonChecked;
    private RadioButton radioButtonA;
    private RadioButton radioButtonB;
    private RadioButton radioButtonC;
    private RadioButton radioButtonD;
    private TextView textContent;
    private String explainText;
    private String userTheat;
    private String userID;
    private String ability;
    private String[] allTheat;
    private String unUpdateTheat;
    /**
     * �ݶ�
     */
    private int courseId = 6;
    private CreateDBAbility dbHelper;
    private SQLiteDatabase db;
    private int correctNum = 0;
    int progress = 0;

    double y_cur = 0;
    /**
     * ��ʼ������ֵ
     */
    double x = 0;
    /**
     * ÿ�ε����Ĳ���
     */
    double step = 0.01;

    private List<TextErrorContent> errList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat);
        LitePal.initialize(this);
        EventBus.getDefault().register(this);

        //�����˴β�������ݿ�
        dbHelper = new CreateDBAbility(getApplicationContext(),"Ability.db",null,4);
        db = dbHelper.getWritableDatabase();
        db.delete("Ability",null,null);

        nextQuestion = (Button) findViewById(R.id.next_question);
        progress1 = (NumberProgressBar) findViewById(R.id.progress);
        radioGroupAnswer = (RadioGroup) findViewById(R.id.radioGroup_answer);
        textContent = (TextView) findViewById(R.id.textContent);
        radioButtonA = (RadioButton) findViewById(R.id.A);
        radioButtonB = (RadioButton) findViewById(R.id.B);
        radioButtonC = (RadioButton) findViewById(R.id.C);
        radioButtonD = (RadioButton) findViewById(R.id.D);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Double.parseDouble(userTheat) != 0){
            difficulty = 1.04;
            requestQuestion(courseId,difficulty);
        }else {
            difficulty = 0.86;
            requestQuestion(courseId,difficulty);
        }

        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < radioGroupAnswer.getChildCount(); i++){
                    ContentValues values = new ContentValues();
                    radioButtonChecked = (RadioButton) radioGroupAnswer.getChildAt(i);

                    if (radioButtonChecked.isChecked()){
                        Question selectedQuestions = DataSupport.findLast(Question.class);
                        String choiceNum = String.valueOf(radioButtonChecked.getText().charAt(0)).trim();
                        if (choiceNum.equals(correctAnswer.trim())){
                            values.put("discrimination",selectedQuestions.getDistinction());
                            values.put("difficult",difficulty);
                            values.put("result",1);
                            db.insert("Ability",null,values);
                            values.clear();
                            Theat theat = new Theat();
                            theat.getmin();
                            difficulty = add(difficulty,new Random().nextDouble()*0.02+0.03);
                            correctNum ++;
                            changeProgress();
                        }else{
                            values.put("discrimination",selectedQuestions.getDistinction());
                            values.put("difficult",difficulty);
                            values.put("result",0);
                            db.insert("Ability",null,values);
                            values.clear();
                            Theat theat = new Theat();
                            theat.getmin();
                            TextErrorContent content = new TextErrorContent();
                            content.setErrText(selectedQuestions.getDescription());
                            content.setExplainText(selectedQuestions.getExplain());
                            errList.add(content);
                            Log.d("��������list","" + content);
                            difficulty = sub(difficulty,new Random().nextDouble()*0.03);
                            changeProgress();
                        }
                        //����Ѷȵ���߽�����
                        if (difficulty<0.68){
                            difficulty = 0.98;
                        }else if (difficulty > 1.34){
                            difficulty = 1.12;
                        }
                    }
                }
                requestQuestion(courseId,difficulty);
                radioGroupAnswer.clearCheck();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void getUserTheatInfo(UserTheatInfo userTheatInfo){
        unUpdateTheat = userTheatInfo.getUserTheat().trim();
        allTheat = unUpdateTheat.split(";");
        userTheat = allTheat[allTheat.length-1];
        userID = userTheatInfo.getId();
    }

    private void changeProgress() {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        progress = progress + 5;
        if (progress > 95){

            ability = df.format(x);
            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
            editor.putString("theat",ability);
            editor.putInt("correctNum",correctNum);
            editor.apply();
            DataSupport.saveAll(errList);
            Intent intent = new Intent(getApplicationContext(),CATResult.class);
            intent.putExtra("testTheat",ability);
            intent.putExtra("correctNum",correctNum);
            //�����û����е�����ֵ
            updateUserTheat(unUpdateTheat+";"+ability);
            EventBus.getDefault().postSticky(new TheatFromCat(unUpdateTheat+";"+ability));
            //�����û�����β�������ֵ
            updateUserLastTheat(ability);
            startActivity(intent);
            finish();
        }
        progress1.setProgress(progress);
    }

    /**
     * �������һ������ֵ
     * @param ability
     */
    private void updateUserLastTheat(String ability) {
        String urlUpdateLearnDay = "http://202.115.30.153/testUpdatelastCatTheat.php?lastCatTheat="+ability+"&id="+userID;
        HttpUtil.sendOKHttpRequest(urlUpdateLearnDay, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"����ʧ��",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(String.valueOf(responseText));
                    int retCode = jsonObject.getInt("success");
                    if (retCode == 3) {
                        Looper.prepare();
                        Toast.makeText(CAT.this,"���³ɹ�!!!",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(CAT.this,"����ʧ��!!!",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ��������ֵ
     */
    private class Theat{

        private double derivative(double difficult, double discrimination, int result, double x){
            double temp=0.0;
            temp = temp + (0.25 - result + (0.75 * (1 / (1 + (Math.pow(Math.E,(-1.702 * discrimination) * (x - difficult)))))));
            return temp/20;
        }

        private double function(double difficult, double discrimination, double x){
            return 0.25 + 0.75 * (1 / (1 + (Math.pow(Math.E,(-1.702 * discrimination) * (x - difficult)))));
        }

        private void getmin( ){

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query("Ability",null,null,null,null,null,null);
            if (cursor.moveToFirst()){
                do {
                    Double disc = Double.valueOf(cursor.getString(cursor.getColumnIndex("discrimination")));
                    Double dif = Double.valueOf(cursor.getString(cursor.getColumnIndex("difficult")));
                    int result = cursor.getInt(cursor.getColumnIndex("result"));
                    Log.d("�Ѳ���������������Ѷ�ֵ ","difficult: " + dif);
                    y_cur=function(dif,disc,x);
                    //�½��ݶȵķ��ȱ仯��������������
                    for(int i=0;i < 2000;i++){
                        //���ݶȸ������ƶ�
                        x=x-step*derivative(dif,disc,result,x);
                        //yֵ����x�ƶ��仯��������һ�ֵ���
                        y_cur=function(dif,disc,x);
                    }
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    /**
     * ����courseId��difficulty�����ò��������
     */
    public void requestQuestion(final int courseId,final double difficulty){

        String courseUrl = "http://202.115.30.153/distinction.php?course_id=" + courseId + "&difficulty=" +  difficulty;
        HttpUtil.sendOKHttpRequest(courseUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CAT.this,"��ȡ��Ŀ��Ϣʧ��", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText = response.body().string();
                Boolean result = false;
                result = Utility.handleQuestionResponse(1,responseText,difficulty);
                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Question question= DataSupport.findLast(Question.class);
                            textContent.setText(question.getDescription());
                            radioButtonA.setText(question.getA());
                            radioButtonB.setText(question.getB()) ;
                            radioButtonC.setText(question.getC());
                            radioButtonD.setText(question.getD());
                            explainText = question.getExplain();
                            correctAnswer = question.getCorrect();
                        }
                    });
                }
            }
        });
    }

    /**
     * �����û�������ֵ
     */
    private void updateUserTheat(String ability) {

        String urlUpdateLearnDay = "http://202.115.30.153/testUpdateTheat.php?catTheat="+ability+"&id="+userID;
        HttpUtil.sendOKHttpRequest(urlUpdateLearnDay, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getApplicationContext(),"��������ֵʧ��",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(String.valueOf(responseText));
                    int retCode = jsonObject.getInt("success");
                    if (retCode == 3) {
                        Looper.prepare();
                        Toast.makeText(CAT.this,"���³ɹ�!!!",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(CAT.this,"����ʧ��!!!",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ��������ȷ����
     */
    public static double add(double d1,double d2){
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return Double.parseDouble(new java.text.DecimalFormat("#.00").format(b1.add(b2)));
    }

    public static double sub(double d1,double d2){
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return Double.parseDouble(new java.text.DecimalFormat("#.00").format(b1.subtract(b2)));
    }
}