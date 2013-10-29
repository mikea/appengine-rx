package com.mikea.gae.objectify;

import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

import javax.inject.Singleton;

/**
 * @author mike.aizatsky@gmail.com
 */
public class ObjectifyModule extends ServletModule {
    @Override
    protected void configureServlets() {
        super.configureServlets();
        bind(ObjectifyFilter.class).in(Singleton.class);
        filter("/*").through(ObjectifyFilter.class);
    }
}
