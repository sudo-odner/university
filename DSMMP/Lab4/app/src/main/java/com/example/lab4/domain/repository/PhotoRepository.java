package com.example.lab4.domain.repository;

import android.net.Uri;

public interface PhotoRepository {
    String importPhoto(Uri source) throws Exception;
    void delete(String path);
}
