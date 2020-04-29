package com.hfs.jokevideo.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 点赞，分享
 */
public class Ugc implements Serializable {

    /**
     * likeCount : 153
     * shareCount : 0
     * commentCount : 4454
     * hasFavorite : false
     * hasLiked : true
     * hasdiss:false
     */

    public int likeCount;
    public int shareCount;
    public int commentCount;
    public boolean hasFavorite;
    public boolean hasLiked;
    public boolean hasdiss;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof Ugc))
            return false;
        Ugc newUgc = (Ugc) obj;
        return likeCount == newUgc.likeCount
                && shareCount == newUgc.shareCount
                && commentCount == newUgc.commentCount
                && hasFavorite == newUgc.hasFavorite
                && hasLiked == newUgc.hasLiked
                && hasdiss == newUgc.hasdiss;
    }
}
