package com.thalicloud.auth.enums;

// M3.1 — owned/written by delivery-service; mirrored here since this entity
// shares the delivery_partners table (auth-service owns ddl-auto:create for it).
public enum DutyStatus {
    OFFLINE,
    ONLINE,
    ON_DELIVERY
}
