package com.novoda.bonfire.login.service;

import com.novoda.bonfire.login.data.model.Authentication;

import io.reactivex.Observable;

public interface LoginService {

    Observable<Authentication> getAuthentication();

    void loginWithGoogle(String idToken);

}
