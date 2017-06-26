package com.novoda.bonfire.login.service;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.novoda.bonfire.login.data.model.Authentication;
import com.novoda.bonfire.login.database.AuthDatabase;
import com.novoda.bonfire.user.database.UserDatabase;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class FirebaseLoginService implements LoginService {

    private final AuthDatabase authDatabase;
    private final UserDatabase userDatabase;
    private final BehaviorRelay<Authentication> authRelay;

    public FirebaseLoginService(AuthDatabase authDatabase, UserDatabase userDatabase) {
        this.authDatabase = authDatabase;
        this.userDatabase = userDatabase;
        authRelay = BehaviorRelay.create();
    }

    @Override
    public Observable<Authentication> getAuthentication() {
        return authRelay
                .startWith(initRelay());
    }

    private Observable<Authentication> initRelay() {
        return Observable.defer(new Callable<ObservableSource<? extends Authentication>>() {
            @Override
            public ObservableSource<? extends Authentication> call() throws Exception {
                if (authRelay.hasValue() && authRelay.getValue().isSuccess()) {
                    return Observable.empty();
                } else {
                    return fetchUser();
                }
            }
        });
    }

    private Observable<Authentication> fetchUser() {
        return authDatabase.readAuthentication()
                .doOnNext(authRelay)
                .ignoreElements()
                .toObservable();
    }

    @Override
    public void loginWithGoogle(String idToken) {
        authDatabase.loginWithGoogle(idToken)
                .subscribe(new Consumer<Authentication>() {
                    @Override
                    public void accept(@NonNull Authentication authentication) throws Exception {
                        if (authentication.isSuccess()) {
                            userDatabase.writeCurrentUser(authentication.getUser());
                        }
                        authRelay.accept(authentication);
                    }
                });
    }

}
