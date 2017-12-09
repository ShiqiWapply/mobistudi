package cat.mobistudi;

import org.litepal.crud.DataSupport;

/**
 * @author Shiqi Wang
 * @date 2017/9/26
 */

public class TextErrorContent extends DataSupport{

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;
    private String errText;
    private String explainText;


    public String getExplainText() {
        return explainText;
    }

    public void setExplainText(String explainText) {
        this.explainText = explainText;
    }

    public String getErrText() {
        return errText;
    }

    public void setErrText(String errText) {
        this.errText = errText;
    }
}