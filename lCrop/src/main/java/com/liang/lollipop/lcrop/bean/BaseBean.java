package com.liang.lollipop.lcrop.bean;

import java.io.Serializable;

/**
 * Created by Lollipop on 2017/07/31.
 * bean的基础类
 */
public class BaseBean implements Serializable {
    public int type = 0;

    public BaseBean() {
    }

    public BaseBean(int type) {
        this.type = type;
    }
}
