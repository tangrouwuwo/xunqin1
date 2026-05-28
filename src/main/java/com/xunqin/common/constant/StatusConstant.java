package com.xunqin.common.constant;

public interface StatusConstant {

    int USER_STATUS_DISABLED = 0;
    int USER_STATUS_ACTIVE = 1;

    int MISSING_INFO_PENDING = 0;
    int MISSING_INFO_APPROVED = 1;
    int MISSING_INFO_REJECTED = 2;

    int CLUE_PENDING = 0;
    int CLUE_VERIFIED = 1;
    int CLUE_REJECTED = 2;

    int TASK_PENDING = 0;
    int TASK_IN_PROGRESS = 1;
    int TASK_COMPLETED = 2;
    int TASK_CANCELLED = 3;
    int TASK_PENDING_REVIEW = 4;
    int TASK_REJECTED = 5;

    int POST_PENDING = 0;
    int POST_APPROVED = 1;
    int POST_REJECTED = 2;

    int NOTIFICATION_UNREAD = 0;
    int NOTIFICATION_READ = 1;

    int MATCH_PENDING = 0;
    int MATCH_CONFIRMED = 1;
    int MATCH_REJECTED = 2;
}
