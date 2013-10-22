package com.mikea.gae.rx;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxUploadEvent extends RxHttpRequestEvent {
    public final Multimap<String, BlobInfo> blobInfos = HashMultimap.create();

    public RxUploadEvent(RxHttpRequestEvent event, Map<String, List<BlobInfo>> blobInfos) {
        super(event);
        for (String key : blobInfos.keySet()) {
            this.blobInfos.putAll(key, blobInfos.get(key));
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("blobInfos", blobInfos).toString();
    }
}
