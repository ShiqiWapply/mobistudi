package cat.mobistudi;

/**
 * After the test is completed, the page will skip to the current one and the test results are displayed 
 * @author Shiqi wang
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import chinaykc.mobistudi.R;

public class CATResult extends AppCompatActivity {

    private TextView textTheat;
    private TextView textCorrectNum;
    private String testTheat;
    private int correctNum;
    private RecyclerView errorQuestionRecyclerView;
    private int errTextPosition;
    private List<String> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catresult);

        testTheat = getIntent().getStringExtra("testTheat");
        correctNum = getIntent().getIntExtra("correctNum",0);

        textTheat = (TextView) findViewById(R.id.text_theat);
        textCorrectNum = (TextView) findViewById(R.id.text_correctNum);
        errorQuestionRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView_catError);

        final List<TextErrorContent> content = DataSupport.findAll(TextErrorContent.class);
        String temp = "";
        for (TextErrorContent str: content){
            temp += str.getErrText()+ "\n\n";
            dataList.add(temp.trim());
            temp = "";
        }

        textTheat.setText(testTheat);
        textCorrectNum.setText(String.valueOf(correctNum));
    }

    @Override
    protected void onStart() {

        super.onStart();
        CustomSGLayoutManager mCustomSGLayoutManager = new CustomSGLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mCustomSGLayoutManager.scrollToPositionWithOffset(errTextPosition,0);
        mCustomSGLayoutManager.setSpeedRatio(1.00);
        errorQuestionRecyclerView.setLayoutManager(mCustomSGLayoutManager);
        new LinearSnapHelper().attachToRecyclerView(errorQuestionRecyclerView);
        ErrTextAdapter errTextAdapter = new ErrTextAdapter(dataList,getApplicationContext());
        errorQuestionRecyclerView.setAdapter(errTextAdapter);
        errTextAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataSupport.deleteAll(TextErrorContent.class);
    }
}