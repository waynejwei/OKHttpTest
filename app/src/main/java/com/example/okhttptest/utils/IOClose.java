package com.example.okhttptest.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOClose {

    public static void ioClose(Closeable closeable){
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
