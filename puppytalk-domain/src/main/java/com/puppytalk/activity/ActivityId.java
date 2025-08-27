package com.puppytalk.activity;

import com.puppytalk.support.EntityId;

public class ActivityId extends EntityId {

    private ActivityId(Long value) {
        super(value);
    }

    public static ActivityId from(Long value) {
        return new ActivityId(value);
    }

    public static ActivityId create() {
        return new ActivityId(null);
    }
}