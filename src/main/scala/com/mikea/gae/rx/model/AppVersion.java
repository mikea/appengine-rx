package com.mikea.gae.rx.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class AppVersion {
    @Id String version;

    public static AppVersion forVersion(String appVersion) {
        AppVersion entity = new AppVersion();
        entity.version = appVersion;
        return entity;
    }
}
