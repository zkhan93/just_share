package io.github.zkhan93.justshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import io.github.zkhan93.justshare.models.FileMeta;
import io.github.zkhan93.justshare.utils.LogUtil;

public class HttpServer extends NanoHTTPD {
    public static final String TAG = HttpServer.class.getSimpleName();
    private List<FileMeta> fileMetas;
    private String content;
    private WeakReference<InputStreamProvider> streamProvider;

    public HttpServer(@NonNull InputStreamProvider streamProvider) throws IOException {
        super(8000);
        fileMetas = new ArrayList<>();
        updateIndexPage();
        this.streamProvider = new WeakReference<>(streamProvider);
    }

    public void setFiles(@Nullable List<FileMeta> fileMetas) {
        if (fileMetas != null) {
            this.fileMetas = fileMetas;
            updateIndexPage();
        }
    }

    private void updateIndexPage() {
        String h1 = tag("h1", null, "Just Share");

        List<String> items = new ArrayList<>();
        Map<String, String> attrs = new HashMap<>();
        for (FileMeta fm : fileMetas) {
            attrs.put("href", fm.name);
            String a = tag("a", attrs, fm.name);
            String item = tag("li", null, a);
            items.add(item);
        }
        String list = tag("ul", null, items.toArray(new String[]{}));
        String body = tag("body", null, h1, list);

        LogUtil.d(TAG, "body: %s", body);
        content = tag("html", null, body);
    }

    private String tag(String tagName, @Nullable Map<String, String>
            attrs, @Nullable String... contents) {
        StringBuilder strb = new StringBuilder();
        strb.append('<').append(tagName).append(' ');
        if (attrs != null)
            for (Map.Entry me : attrs.entrySet()) {
                strb.append(me.getKey())
                        .append('=')
                        .append('"')
                        .append(me.getValue())
                        .append('"')
                        .append(' ');
            }
        strb.append('>');
        if (contents != null)
            for (String content : contents)
                strb.append(content);
        strb.append('<');
        strb.append('/');
        strb.append(tagName);
        strb.append('>');
        return strb.toString();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String path = session.getUri();
        LogUtil.d(TAG, path);
        Pattern filenamePattern = Pattern.compile("/(.+)", Pattern.DOTALL);
        Matcher matcher = filenamePattern.matcher(path);
        if (matcher.matches()) {
            String name = matcher.group(1);
            FileMeta fileMeta = null;
            for (FileMeta fm : fileMetas) {
                if (fm.name.equals(name))
                    fileMeta = fm;
            }
            if (fileMeta == null || fileMeta.uri == null || fileMeta.length == 0)
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "");
            try {
                LogUtil.d(TAG, "header: %s", session.getHeaders());
                String range = session.getHeaders().get("range");
                long startFrom = 0;
                long endAt = -1;
                if (range != null) {
                    //                range=bytes=35449440-
                    // Support (simple) skipping:
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                InputStream fr = streamProvider.get().getInputStream(fileMeta.uri);
                if(range != null && startFrom >= 0 && startFrom < fileMeta.length) {
                    long res = fr.skip(startFrom);
                    LogUtil.d(TAG, "skipped %s %s", startFrom, res);
                    long newLen = endAt - startFrom + 1;
                    return newFixedLengthResponse(Response.Status.OK, getMimeTypeForFile(fileMeta
                            .uri), fr, newLen);
                }else{
                    return newFixedLengthResponse(Response.Status.OK, getMimeTypeForFile(fileMeta
                            .uri), fr, fileMeta.length);
                }
            } catch (IOException ex) {
                LogUtil.e(TAG, "Exception while reading file : %s", ex.getLocalizedMessage());
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File " +
                        "not found on disk");
            }
        } else {
            return newFixedLengthResponse(Response.Status.OK, MIME_HTML, content);
        }
    }

    class Range {
        Integer start;
        Integer end;
        Integer suffixLength;
    }

    public interface InputStreamProvider {
        InputStream getInputStream(String uri) throws IOException;
    }
}
