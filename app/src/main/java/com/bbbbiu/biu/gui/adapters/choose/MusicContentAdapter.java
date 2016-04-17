package com.bbbbiu.biu.gui.adapters.choose;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbbbiu.biu.R;
import com.bbbbiu.biu.gui.choose.ChooseBaseActivity;
import com.bbbbiu.biu.util.SearchUtil;
import com.bbbbiu.biu.util.StorageUtil;
import com.bbbbiu.biu.util.db.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by fangdongliang on 16/3/26.
 * <p>
 * Updated by YieldNull
 */
public class MusicContentAdapter extends ContentBaseAdapter {
    private static final String TAG = MusicContentAdapter.class.getSimpleName();

    private Context context;

    private List<FileItem> mMusicList = new ArrayList<>();
    private List<FileItem> mChosenMusic = new ArrayList<>();

    private Map<String, List<FileItem>> mDirFileMap = new HashMap<>();

    public MusicContentAdapter(final ChooseBaseActivity context) {
        super(context);
        this.context = context;

        if (!readMusicList()) {
            notifyStartLoadingData();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SearchUtil.scanDisk(context);

                    readMusicList();

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                            notifyFinishLoadingData();
                        }
                    });
                }
            }).start();
        }
    }


    /**
     * 从数据库读取扫描得到的音乐
     *
     * @return 之前是否扫描过
     */
    private boolean readMusicList() {
        return FileItem.loadFile(FileItem.TYPE_MUSIC, mMusicList, mDirFileMap);
    }

    @Override
    public int getItemViewType(int position) {
        return mMusicList.get(position) == null ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    private void setItemChosen(int position) {
        FileItem item = mMusicList.get(position);

        if (mChosenMusic.contains(item)) {
            mChosenMusic.remove(item);
            notifyFileDismissed(item.path);
        } else {
            mChosenMusic.add(item);
            notifyFileChosen(item.path);
        }

        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView;

        if (viewType == VIEW_TYPE_HEADER) {
            itemView = inflater.inflate(R.layout.list_header_cate, parent, false);
            return new HeaderViewHolder(itemView);
        } else {
            itemView = inflater.inflate(R.layout.list_music_item, parent, false);
            return new MusicViewHolder(itemView);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder hd, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            MusicViewHolder holder = (MusicViewHolder) hd;

            Music music = new Music(mMusicList.get(position).path);
            holder.title.setText(music.title);
            holder.singer.setText(music.artist);
            holder.size.setText(music.size);
            holder.duration.setText(music.duration);

            if (mChosenMusic.contains(mMusicList.get(position))) {
                holder.setItemStyleChosen();
            } else {
                holder.setItemStyleChoosing();
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setItemChosen(position);
                }
            });

        } else {
            HeaderViewHolder holder = (HeaderViewHolder) hd;
            holder.headerText.setText(mMusicList.get(position + 1).getParentDir());
        }

    }

    @Override
    public Set<String> getChosenFiles() {
        Set<String> set = new HashSet<>();

        for (FileItem item : mChosenMusic) {
            set.add(item.path);
        }
        return set;
    }

    @Override
    public int getChosenCount() {
        return mChosenMusic.size();
    }

    @Override
    public void setFileAllChosen() {
        mChosenMusic.clear();
        for (FileItem fileItem : mMusicList) {
            if (fileItem != null) {
                mChosenMusic.add(fileItem);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public void setFileAllDismissed() {
        mChosenMusic.clear();
        notifyDataSetChanged();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.textView_title)
        TextView title;

        @Bind(R.id.textView_artist)
        TextView singer;

        @Bind(R.id.textView_duration)
        TextView duration;

        @Bind(R.id.textView_size)
        TextView size;

        @Bind(R.id.imageView_icon)
        ImageView iconImage;

        public MusicViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        /**
         * 未选样式
         */
        public void setItemStyleChoosing() {
            itemView.setBackgroundColor(context.getResources().getColor(android.R.color.background_light));
            iconImage.setBackgroundDrawable(null);
            iconImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_type_music));
        }

        /**
         * 已选样式
         */
        public void setItemStyleChosen() {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.file_item_chosen));
            iconImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_file_chosen));
            iconImage.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.shape_file_icon_bkg_chosen));
        }
    }

    class Music {
        String title;
        String artist;
        String duration;
        String size;

        Music(String path) {
            // 获取Music的Metadata
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            mediaMetadataRetriever.setDataSource(context, uri);

            title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            duration = formatTime(Long.valueOf(mediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION)));
            size = StorageUtil.getReadableSize(file.length());
        }

        /**
         * 格式化时间
         *
         * @param time time as long integer
         * @return 格式化之后的时间 如 05:20 表示5min 20sec
         */
        private String formatTime(long time) {
            String min = time / (1000 * 60) + "";
            String sec = time % (1000 * 60) + "";
            if (min.length() < 2)
                min = "0" + min;
            if (sec.length() == 4)
                sec = "0" + sec;
            else if (sec.length() <= 3)
                sec = "00" + sec;
            return min + ":" + sec.trim().substring(0, 2);
        }
    }
}