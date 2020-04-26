package com.hfs.jokevideo.model;

import java.io.Serializable;

/**
 * 作者信息
 */
public class User implements Serializable {
    /**
     * id : 962
     * userId : 3223400206308231
     * name : 二师弟请随我来
     * avatar : https://p3-dy.byteimg.com/img/p1056/8c50025c85244140910a513345ae7358~200x200.webp
     * description :
     * likeCount : 0
     * topCommentCount : 0
     * followCount : 0
     * followerCount : 0
     * qqOpenId : null
     * expires_time : 0
     * score : 0
     * historyCount : 0
     * commentCount : 0
     * favoriteCount : 0
     * feedCount : 0
     * hasFollow : false
     */

    public int id;
    public long userId;
    public String name;
    public String avatar;
    public String description;
    public int likeCount;
    public int topCommentCount;
    public int followCount;
    public int followerCount;
    public String qqOpenId;
    public long expires_time;
    public int score;
    public int historyCount;
    public int commentCount;
    public int favoriteCount;
    public int feedCount;
    public boolean hasFollow;
}
