package com.example.chaomianqiandao.utils;

import okhttp3.Headers;

public class ResponseInfo {
    public Headers HeaderInfo;
    public String BodyInfo;

    public ResponseInfo(Headers headerInfo, String bodyInfo) {
        HeaderInfo = headerInfo;
        BodyInfo = bodyInfo;
    }
}
