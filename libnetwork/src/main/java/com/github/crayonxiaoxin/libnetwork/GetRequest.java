package com.github.crayonxiaoxin.libnetwork;

public class GetRequest<T> extends Request<T, GetRequest> {
    public GetRequest(String url) {
        super(url);
    }

    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        builder.get().url(UrlCreator.createUrlFromParams(mUrl, params));
        return builder.build();
    }
}
