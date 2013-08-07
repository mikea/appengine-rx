package com.mikea.gae;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.oauth.OAuthService;
import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author mike.aizatsky@gmail.com
 */
public class GaeModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    BlobstoreService getBlobstoreService() {
        return BlobstoreServiceFactory.getBlobstoreService();
    }

    @Provides
    @Singleton
    BlobInfoFactory getBlobInfoFactory() {
        return new BlobInfoFactory();
    }

    @Provides
    @Singleton
    ImagesService getImagesService() {
        return ImagesServiceFactory.getImagesService();
    }

    @Provides
    @Singleton
    OAuthService getOAuthService() {
        return OAuthServiceFactory.getOAuthService();
    }

    @Provides
    @Singleton
    UserService getUserService() {
        return UserServiceFactory.getUserService();
    }

    @Provides
    @Singleton
    URLFetchService getURLFetchService() {
        return URLFetchServiceFactory.getURLFetchService();
    }

    @Provides
    @Singleton
    ChannelService getChannelService() {
        return ChannelServiceFactory.getChannelService();
    }

}