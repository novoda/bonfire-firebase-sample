package com.novoda.bonfire.login.database;

import com.novoda.bonfire.login.data.model.Authentication;

import io.reactivex.Observable;

public interface AuthDatabase {

    Observable<Authentication> readAuthentication();

    Observable<Authentication> loginWithGoogle(String idToken);

}
