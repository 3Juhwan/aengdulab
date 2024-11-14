package com.aengdulab.trenditem.supports;

import com.aengdulab.trenditem.domain.Comment;
import com.aengdulab.trenditem.domain.Item;
import com.aengdulab.trenditem.domain.Like;
import java.util.ArrayList;
import java.util.List;

public class ItemReactions {

    private final List<Item> items;
    private final List<Comment> comments;
    private final List<Like> likes;

    public ItemReactions(List<Item> items, List<Comment> comments, List<Like> likes) {
        this.items = items;
        this.comments = comments;
        this.likes = likes;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public static ItemReactions concat(ItemReactions... itemReactions) {
        ItemReactions result = new ItemReactions(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (ItemReactions itemReaction : itemReactions) {
            result.items.addAll(itemReaction.items);
            result.comments.addAll(itemReaction.comments);
            result.likes.addAll(itemReaction.likes);
        }
        return result;
    }
}
