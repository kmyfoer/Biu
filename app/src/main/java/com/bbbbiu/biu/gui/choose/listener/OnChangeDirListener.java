package com.bbbbiu.biu.gui.choose.listener;

import java.io.File;

/**
 * Created by YieldNull at 4/7/16
 */
public interface OnChangeDirListener {
    void onEnterDir(File dir);

    void onExitDir(File dir);
}
