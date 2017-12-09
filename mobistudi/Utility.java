package cat.mobistudi;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

/**
 * @author Shiqi wang
 * @date 2017/9/2
 */

public class Utility {

    public static Boolean handleQuestionResponse(int courseId, String response, double difficulty){

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allQuestion = new JSONArray(response);
                for (int i=0;i < allQuestion.length();i++){
                    JSONObject questionObject = allQuestion.getJSONObject(i);
                    Question question = new Question();
                    question.setExplain(questionObject.getString("answer"));
                    question.setDescription(questionObject.getString("description"));
                    question.setA(questionObject.getString("a"));
                    question.setB(questionObject.getString("b"));
                    question.setC(questionObject.getString("c"));
                    question.setD(questionObject.getString("d"));
                    question.setCorrect(questionObject.getString("correct"));
                    question.setDifficulty(difficulty);
                    question.setDistinction(questionObject.getDouble("distinction"));
                    question.setCourse_id(courseId);
                    question.save();
                    Question question1= DataSupport.findLast(Question.class);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}