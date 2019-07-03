package com.example.android.album;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class DemoAdapter extends RecyclerView.Adapter<DemoAdapter.BaseViewHolder> {
    private ArrayList<String> dataList = new ArrayList<>();
    private Resources res;
    public void replaceAll(ArrayList<String> list) {
        dataList.clear();
        if (list != null && list.size() > 0) {
            dataList.addAll(list);
        }
        notifyDataSetChanged();
    }

    @Override
    public DemoAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OneViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.one, parent, false));
    }

    @Override
    public void onBindViewHolder(DemoAdapter.BaseViewHolder holder, int position) {

        holder.setData(dataList.get(position));
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
            ivImage = (ImageView) view.findViewById(R.id.ivImage);
            int width = ((Activity) ivImage.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = ivImage.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = width/3;
            params.height =  (int) (200 + Math.random() * 400) ;
            ivImage.setLayoutParams(params);
            res = itemView.getContext().getResources();
        }

        @Override
        void setData(Object data) {
            if (data != null) {
                String text = (String) data;
                Glide.with(itemView.getContext()).load(text).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.empty_photo).crossFade().into(ivImage);
                Bitmap bitmap = BitmapFactory.decodeResource(res,R.drawable.empty_photo);
                //异步获得bitmap图片颜色值
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        Palette.Swatch vibrant = palette.getVibrantSwatch();//有活力
                        Palette.Swatch c = palette.getDarkVibrantSwatch();//有活力 暗色
                        Palette.Swatch d = palette.getLightVibrantSwatch();//有活力 亮色
                        Palette.Swatch f = palette.getMutedSwatch();//柔和
                        Palette.Swatch a = palette.getDarkMutedSwatch();//柔和 暗色
                        Palette.Swatch b = palette.getLightMutedSwatch();//柔和 亮色

                        if (vibrant != null) {
                            int color1 = vibrant.getBodyTextColor();//内容颜色
                            int color2 = vibrant.getTitleTextColor();//标题颜色
                            int color3 = vibrant.getRgb();//rgb颜色

                            ivImage.setBackgroundColor(
                                    vibrant.getRgb());

                        }
                    }
                });
            }


        }
    }
}

