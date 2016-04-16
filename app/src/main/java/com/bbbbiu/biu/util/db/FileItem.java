package com.bbbbiu.biu.util.db;

import com.bbbbiu.biu.util.SearchUtil;
import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by YieldNull at 4/15/16
 */
public class FileItem extends SugarRecord {
    public static final int TYPE_MUSIC = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_IMG = 3;
    public static final int TYPE_ARCHIVE = 4;
    public static final int TYPE_WORD = 5;
    public static final int TYPE_EXCEL = 6;
    public static final int TYPE_PPT = 7;
    public static final int TYPE_PDF = 8;

    public static final int TYPE_APK = 9; // APK 安装包

    @Unique
    public String path;
    public int type;

    public FileItem() {
    }

    public FileItem(String path, int type) {
        this.path = path;
        this.type = type;
    }

    /**
     * 存储将全盘扫描之后得到的文件分类
     *
     * @param type    在{@link SearchUtil} 中的文件分类
     * @param pathSet 文件绝对路径集合
     */
    public static void storeFile(int type, Set<String> pathSet) {
        for (String path : pathSet) {
            new FileItem(path, type).save();
        }
    }

    /**
     * 获取对应分类的所有文件
     *
     * @param type 在{@link SearchUtil} 中的文件分类
     * @return 文件路径集合。该分类下没有文件则返回空Set()，没有该分类则返回null
     */
    public static Set<String> getFile(int type) {
        Set<String> fileSet = new HashSet<>();

        for (FileItem cate : FileItem.find(FileItem.class, "type=?", String.valueOf(type))) {
            fileSet.add(cate.path);
        }
        return fileSet;
    }
}