package com.example.openmusic.adpters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.openmusic.MyAudioFormat;
import com.example.openmusic.R;

import java.util.List;

public class RecyclerAdapterTest extends RecyclerView.Adapter<
        RecyclerAdapterTest.RecyclerViewHolder> {


    private int selectedStarPosition = 0;
    private List<MyAudioFormat> myAudioFormat;
    private AdapterView.OnItemClickListener onItemClickListener;
    private final LayoutInflater inflater;



    public RecyclerAdapterTest(Context context, List<MyAudioFormat> states) {
        this.myAudioFormat = states;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View v = inflater.inflate(R.layout.item_audio_format, viewGroup, false);
        return new RecyclerViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder viewHolder, final int position) {
         MyAudioFormat audioFormat = myAudioFormat.get(position);
        try {
            viewHolder.bindData(audioFormat, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return myAudioFormat.size();
    }


    public void setOnItemClickListener(AdapterView.OnItemClickListener
                                               onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }



    public void onItemHolderClick(RecyclerViewHolder holder) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(null, holder.itemView,
                    holder.getAdapterPosition(), holder.getItemId());
    }


    class RecyclerViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private RecyclerAdapterTest mAdapter;
        private RadioButton radioButton;
        private TextView txtFormat;

        public RecyclerViewHolder(View itemView, final RecyclerAdapterTest mAdapter) {
            super(itemView);
            this.mAdapter = mAdapter;

            txtFormat = itemView.findViewById(R.id.txtFormat);
            radioButton = itemView.findViewById(R.id.radioButton);
            itemView.setOnClickListener(this);
            radioButton.setOnClickListener(this);
        }


        public void bindData(MyAudioFormat audioFormat, int position) {
            radioButton.setChecked(position == selectedStarPosition);
            radioButton.setText(audioFormat.getAudioFormat().audioQuality().name());
            txtFormat.setText(audioFormat.getAudioFormat().extension().value());
        }

        @Override
        public void onClick(View v) {
            selectedStarPosition = getAdapterPosition();
            notifyItemRangeChanged(0, myAudioFormat.size());
            mAdapter.onItemHolderClick(RecyclerViewHolder.this);
        }
    }
}
