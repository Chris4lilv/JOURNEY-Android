package com.example.android.album;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class SmallImageAdapter extends RecyclerView.Adapter<SmallImageAdapter.BaseViewHolder> {
    private Context mContext;
    private ArrayList<String> dataList = new ArrayList<>();
    private Resources res;

    public void replaceAll(ArrayList<String> list) {
        this.dataList.clear();
        if (list != null && list.size() > 0) {
            dataList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public SmallImageAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SmallImageAdapter.OneViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.small_image_one, parent, false));
    }

    @Override
    public void onBindViewHolder(SmallImageAdapter.BaseViewHolder holder, int position) {

        if(position == 0){
            Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_add_img);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap,350,300);
            holder.setData(bitmap);
        }else{
            holder.setData(dataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        void setData(Object data) {
        }
    }

    private class OneViewHolder extends BaseViewHolder {
        private ImageView ivImage;

        public OneViewHolder(View view) {
            super(view);
            ivImage = (ImageView) view.findViewById(R.id.smallImage);
            res = itemView.getContext().getResources();
        }

        @Override
        void setData(Object data) {
            if (data != null) {
                if(data instanceof Bitmap){
                    Bitmap bitmap = (Bitmap) data;
                    ivImage.setImageBitmap(bitmap);
                }else{
                    String text = (String) data;
                    Glide.with(itemView.getContext()).load(text).diskCacheStrategy(DiskCacheStrategy.ALL).into(ivImage);
                }

            }

        }
    }
}
