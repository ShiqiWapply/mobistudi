package cat.mobistudi;

import android.graphics.Color;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chinaykc.mobistudi.R;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import xyz.kfdykme.mobistudi.bean.UserAllTheat;
import xyz.kfdykme.mobistudi.bean.UserLastTheat;
import xyz.kfdykme.mobistudi.eventbus.TheatFromCat;
import xyz.kfdykme.mobistudi.eventbus.TheatFromMain;
import xyz.kfdykme.mobistudi.eventbus.UserID;

/**
 * My ability
 * @author Shiqi wang
 */
public class MyLevel extends AppCompatActivity {

    private LineChartView abilityChart;
    private String[] userAllTheat;
    private TextView mTextView;
    private List<UserLastTheat> userLastTheatList;
    private List<UserLastTheat> tempList;
    private int rank;
    private String theatFromMain;
    private String theatFromCat;
    private String unSplitNull;
    private String responseText;

    /**
     * X轴的标注
     */
    String[] lastNum = {"1","2","3","4","5","6","7","8","9","10"};
    /**
     * 名次曲线，各点数据
     */
    private List<PointValue> mTheatPointValuesList = new ArrayList<>();
    private List<AxisValue> mAxisXValuesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_level);
        EventBus.getDefault().register(this);

        mTextView = (TextView) findViewById(R.id.levelText);
        abilityChart = (LineChartView) findViewById(R.id.abilityChart);

        //服务器中的能力值,
        if (theatFromCat == null){
            unSplitNull = theatFromMain;
        }else {
            unSplitNull = theatFromCat;
        }
        String[] s1 = unSplitNull.substring(1,unSplitNull.length()).split(";");
        userAllTheat = reverseArray(s1);
//        userLastTheatList = requestUserLastTheat();
        //获取X轴标注
        getAxisXLables();
        initTheatLineChart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestUserLastTheat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void getUserTheatFromMain(TheatFromMain theatFromMain1){
        theatFromMain = theatFromMain1.getCatTheat();
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void getUserTheatFromCat(TheatFromCat theatFromCat1){
        theatFromCat = theatFromCat1.getCatTheat();
    }

    /**
     * 获取最近一次的能力值
     */
    public void requestUserLastTheat(){
        String address = "http://202.115.30.153/testGetLastCatTheat.php";
        HttpUtil.sendOKHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(MyLevel.this,"获取信息失败.",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tempList = new Gson().fromJson(responseText,new TypeToken<List<UserLastTheat>>(){}.getType());
                        for (int i= 0;i<tempList.size();i++){
                            if (userAllTheat[0].equals(tempList.get(i).getLastCatTheat())){
                                rank = i+1;
                                break;
                            }
                        }
                        String text = "  您上次测验的能力值是"+ userAllTheat[0]
                                +"，在班级中的排名是"+rank+"........";
                        mTextView.setText(text);
                    }
                });
            }
        });
    }

    /**
     * 初始化abilityLineChart的设置
     */
    private void initTheatLineChart() {

        getAxisPoints();

        Line line = new Line(mTheatPointValuesList).setColor(Color.parseColor("#E9967A")).setCubic(true);
        List<Line> lines = new ArrayList<>();
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setFilled(true);
        line.setHasLabelsOnlyForSelected(false);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(false);
        axisX.setTextColor(Color.parseColor("#336699"));
        axisX.setName("最近测评的能力值");
        axisX.setTextSize(15);
        data.setAxisXBottom(axisX);

        Axis axisY = new Axis();
        axisY.setHasLines(true);
        axisY.setName("Score");
        axisY.setTextSize(11);
        axisY.setTextColor(Color.parseColor("#336699"));
        data.setAxisYLeft(axisY);

        abilityChart.setInteractive(true);
        Viewport tempPort = new Viewport();
        tempPort.top = 3;
        tempPort.bottom = -10;
        abilityChart.setMaximumViewport(tempPort);
        Viewport port = initViewPort(0,7);
        abilityChart.setCurrentViewportWithAnimation(port);
        //缩放类型
        abilityChart.setZoomType(ZoomType.HORIZONTAL);
        abilityChart.setLineChartData(data);
    }

    private Viewport initViewPort(float left, float right) {
        Viewport port = new Viewport();
        port.top = 3;
        port.bottom = -3;
        port.left = left;
        port.right = right;
        return port;
    }

    /**
     * 能力曲线的每个点的显示
     */
    private void getAxisPoints() {
        int j=0;
        for (String anUserAllTheat : userAllTheat) {
            mTheatPointValuesList.add(new PointValue((float) j, Float.parseFloat(anUserAllTheat)));
            j++;
            if (j == 7) {
                break;
            }
        }
    }

    /**
     * x轴的显示
     */
    private void getAxisXLables(){
        for (int i = 0; i< lastNum.length; i++){
            mAxisXValuesList.add(new AxisValue(i).setLabel(lastNum[i]));
        }
    }

    private static String[] reverseArray(String[] Array) {
        String[] new_array = new String[Array.length];
        for (int i = 0; i < Array.length; i++) {
            // 反转后数组的第一个元素等于源数组的最后一个元素：
            new_array[i] = Array[Array.length - i - 1];
        }
        return new_array;
    }
}