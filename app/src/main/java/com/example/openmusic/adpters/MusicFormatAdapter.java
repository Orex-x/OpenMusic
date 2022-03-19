package com.example.openmusic.adpters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openmusic.models.MyAudioFormat;
import com.example.openmusic.R;

import java.util.List;

public class MusicFormatAdapter extends RecyclerView.Adapter<MusicFormatAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private final List<MyAudioFormat> audioFormats;
    RadioGroup radioGroup;


    public MusicFormatAdapter(Context context, List<MyAudioFormat> states) {
        this.audioFormats = states;
        this.inflater = LayoutInflater.from(context);
        radioGroup = new RadioGroup(inflater.getContext());
    }

    @NonNull
    @Override
    public MusicFormatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_audio_format, parent, false);
        return new MusicFormatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicFormatAdapter.ViewHolder holder, int position) {
        MyAudioFormat audioFormat = audioFormats.get(position);
        ViewGroup.LayoutParams layoutParams = holder.radioButton.getLayoutParams();
        RadioButton radioButton = new RadioButton(inflater.getContext());
        radioGroup.addView(radioButton, holder.radioButton.getId(), layoutParams);
        holder.txtFormat.setText(audioFormat.getAudioFormat().type() + " " + audioFormat.getAudioFormat().extension().value());
    }

    @Override
    public int getItemCount() {
        return audioFormats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        final TextView txtFormat;
        final RadioButton radioButton;

        ViewHolder(View view){
            super(view);
            txtFormat = view.findViewById(R.id.txtFormat);
            radioButton = view.findViewById(R.id.radioButton);
        }
    }
}
