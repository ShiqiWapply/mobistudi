package cat.mobistudi;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import chinaykc.mobistudi.R;

/**
 * @author Shiqi wang
 * @date 2017/11/17
 */

public class ErrTextAdapter  extends RecyclerView.Adapter<ErrTextAdapter.ErrTextViewHolder>{

    private List<String> errQuestionList;
    private List<String> errExplainList;
    private Context mContext;

    public ErrTextAdapter(List<String> errQuestionList, Context mContext) {
        this.errQuestionList = errQuestionList;
        this.mContext = mContext;
    }

    @Override
    public ErrTextAdapter.ErrTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.errtextitem,parent,false);
        final ErrTextViewHolder mViewHolder = new ErrTextViewHolder(view);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ErrTextAdapter.ErrTextViewHolder holder, int position) {

        holder.errQuestionText.setText(errQuestionList.get(position));
        holder.errExplainText.setText("解析内容");
    }

    @Override
    public int getItemCount() {
        return errQuestionList.size();
    }

    class ErrTextViewHolder extends RecyclerView.ViewHolder {

        TextView errQuestionText;
        TextView errExplainText;
        View errTextView;

        ErrTextViewHolder(View itemView) {
            super(itemView);
            errTextView = itemView;
            errQuestionText = (TextView) itemView.findViewById(R.id.errText);
            errExplainText = (TextView) itemView.findViewById(R.id.errExplain);
        }
    }
}